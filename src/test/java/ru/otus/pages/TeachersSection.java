package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import ru.otus.utils.UiActions;

public final class TeachersSection {

    private static final Pattern TEACHER_NAME_PATTERN =
            Pattern.compile("[А-ЯЁA-Z][а-яёa-z]+(?:[- ][А-ЯЁA-Z][а-яёa-z]+){1,2}");

    private final Page page;
    private final UiActions uiActions;

    @Inject
    public TeachersSection(final Page page, final UiActions uiActions) {
        this.page = page;
        this.uiActions = uiActions;
    }

    public void waitUntilVisible() {
        page.locator("h2")
                .filter(new Locator.FilterOptions().setHasText("Преподаватели"))
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(30000));

        root().scrollIntoViewIfNeeded();
        root().waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }

    public List<String> getVisibleTeacherNames() {
        final List<String> names = new ArrayList<>();
        final List<Locator> cards = visibleNonDuplicateTeacherCards();

        for (final Locator card : cards) {
            final String teacherName = tryExtractTeacherName(card);
            if (!teacherName.isBlank()) {
                names.add(teacherName);
            }
        }

        if (names.isEmpty()) {
            throw new IllegalStateException(
                    "Не удалось найти ни одной видимой карточки преподавателя с именем"
            );
        }

        return names;
    }

    public List<String> dragCarouselLeftAndGetVisibleTeacherNames() {
        final List<String> beforeDrag = getVisibleTeacherNames();
        final String beforeTransform = getWrapperTransform();

        System.out.println("Teachers before drag: " + beforeDrag);
        System.out.println("Wrapper transform before drag: " + beforeTransform);

        final Locator dragTarget = dragSurface();
        System.out.println(
                "Drag target teacher: " + tryExtractTeacherName(firstFullyVisibleNonDuplicateTeacherCard())
        );

        uiActions.dragHorizontally(dragTarget, -800);
        page.waitForTimeout(1800);

        String afterTransform = getWrapperTransform();
        if (beforeTransform.equals(afterTransform)) {
            uiActions.dragHorizontally(dragTarget, -1000);
            page.waitForTimeout(1800);
            afterTransform = getWrapperTransform();
        }

        final List<String> afterDrag = getVisibleTeacherNames();

        System.out.println("Teachers after drag: " + afterDrag);
        System.out.println("Wrapper transform after drag: " + afterTransform);

        if (beforeTransform.equals(afterTransform)) {
            throw new AssertionError("Swiper не прокрутился — transform wrapper не изменился");
        }

        return afterDrag;
    }

    public String clickFirstVisibleTeacher() {
        final Locator card = firstFullyVisibleNonDuplicateTeacherCard();
        final String teacherName = extractTeacherName(card);
        final Locator clickable = findClickableElement(card);

        System.out.println("Clicked teacher card name: " + teacherName);

        uiActions.clickAtCenter(clickable);
        page.waitForTimeout(1500);

        return teacherName;
    }

    private Locator root() {
        return page.locator("section").filter(
                new Locator.FilterOptions().setHas(
                        page.locator("h2")
                                .filter(new Locator.FilterOptions().setHasText("Преподаватели"))
                )
        ).first();
    }

    private Locator teacherCards() {
        Locator cards = root().locator(".swiper-slide");
        if (cards.count() == 0) {
            cards = root().locator("[class*='swiper-slide']");
        }
        return cards;
    }

    private Locator dragSurface() {
        final Locator swiper = root().locator(".swiper").first();
        if (swiper.count() > 0 && swiper.isVisible()) {
            return swiper;
        }

        final Locator wrapper = root().locator(".swiper-wrapper").first();
        if (wrapper.count() > 0 && wrapper.isVisible()) {
            return wrapper;
        }

        return firstFullyVisibleNonDuplicateTeacherCard();
    }

    private String getWrapperTransform() {
        final Locator wrappers = root().locator(".swiper-wrapper");
        if (wrappers.count() == 0) {
            return "";
        }

        final Object value = wrappers.first().evaluate(
                "element => window.getComputedStyle(element).transform"
        );
        return value == null ? "" : value.toString();
    }

    private Locator firstFullyVisibleNonDuplicateTeacherCard() {
        final List<Locator> cards = visibleNonDuplicateTeacherCards();
        for (final Locator card : cards) {
            if (isCardFullyVisible(card)) {
                return card;
            }
        }

        if (!cards.isEmpty()) {
            return cards.get(0);
        }

        throw new IllegalStateException(
                "В блоке преподавателей не найдено ни одной видимой недублированной карточки"
        );
    }

    private List<Locator> visibleNonDuplicateTeacherCards() {
        final Locator cards = teacherCards();
        final int count = cards.count();
        final List<Locator> result = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            final Locator card = cards.nth(index);

            if (!isCardUsable(card)) {
                continue;
            }

            final String className = normalize(card.getAttribute("class"));
            if (className.contains("swiper-slide-duplicate")) {
                continue;
            }

            final String teacherName = tryExtractTeacherName(card);
            if (teacherName.isBlank()) {
                continue;
            }

            result.add(card);
        }

        return result;
    }

    private boolean isCardUsable(final Locator card) {
        if (!card.isVisible()) {
            return false;
        }

        final BoundingBox box = card.boundingBox();
        if (box == null) {
            return false;
        }

        return box.width >= 120 && box.height >= 120;
    }

    private boolean isCardFullyVisible(final Locator card) {
        final BoundingBox box = card.boundingBox();
        if (box == null) {
            return false;
        }

        final int viewportWidth = page.viewportSize() == null ? 1280 : page.viewportSize().width;
        final int viewportHeight = page.viewportSize() == null ? 900 : page.viewportSize().height;

        return box.x >= 0
                && box.y >= 0
                && box.x + box.width <= viewportWidth
                && box.y + box.height <= viewportHeight;
    }

    private String tryExtractTeacherName(final Locator card) {
        try {
            return extractTeacherName(card);
        } catch (IllegalStateException ignored) {
            return "";
        }
    }

    private String extractTeacherName(final Locator card) {
        return normalize(findTeacherNameElement(card).textContent());
    }

    private Locator findTeacherNameElement(final Locator card) {
        final String[] selectors = {"h3", "h4", "strong", "b", "p", "div", "span"};

        for (final String selector : selectors) {
            final Locator elements = card.locator(selector);
            final int count = elements.count();

            for (int index = 0; index < count; index++) {
                final Locator element = elements.nth(index);
                if (!element.isVisible()) {
                    continue;
                }

                final String text = normalize(element.textContent());
                if (looksLikeTeacherName(text)) {
                    return element;
                }
            }
        }

        throw new IllegalStateException("Не удалось извлечь имя преподавателя из карточки");
    }

    private Locator findClickableElement(final Locator card) {
        final String[] selectors = {"a[href]", "a", "button", "[role='button']"};

        for (final String selector : selectors) {
            final Locator elements = card.locator(selector);
            final int count = elements.count();

            for (int index = 0; index < count; index++) {
                final Locator element = elements.nth(index);
                if (!element.isVisible()) {
                    continue;
                }

                final BoundingBox box = element.boundingBox();
                if (box != null && box.width > 20 && box.height > 20) {
                    return element;
                }
            }
        }

        return card;
    }

    private boolean looksLikeTeacherName(final String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if ("Руководитель курса".equalsIgnoreCase(text)) {
            return false;
        }
        if (text.length() < 5 || text.length() > 60) {
            return false;
        }
        if (text.matches(".*\\d.*")) {
            return false;
        }

        return TEACHER_NAME_PATTERN.matcher(text).matches();
    }

    private String normalize(final String value) {
        return value == null ? "" : value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}