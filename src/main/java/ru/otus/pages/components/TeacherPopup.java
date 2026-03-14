package ru.otus.pages.components;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import ru.otus.utils.UiActions;

public final class TeacherPopup {

    private final Page page;
    private final UiActions uiActions;

    @Inject
    public TeacherPopup(final Page page, final UiActions uiActions) {
        this.page = page;
        this.uiActions = uiActions;
    }

    public void waitUntilOpened() {
        activeSlide().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        activeTeacherTitle().waitFor(new Locator.WaitForOptions().setTimeout(15000));
    }

    public String getOpenedTeacherName() {
        final String openedTeacherName = normalize(activeTeacherTitle().textContent());
        System.out.println("Opened popup teacher name: " + openedTeacherName);
        return openedTeacherName;
    }

    public String clickNextAndGetOpenedTeacherName() {
        final String current = getOpenedTeacherName();
        uiActions.click(nextButton());
        page.waitForTimeout(1200);
        waitUntilTeacherChangedFrom(current);
        return getOpenedTeacherName();
    }

    public String clickPreviousAndGetOpenedTeacherName() {
        final String current = getOpenedTeacherName();
        uiActions.click(previousButton());
        page.waitForTimeout(1200);
        waitUntilTeacherChangedFrom(current);
        return getOpenedTeacherName();
    }

    private void waitUntilTeacherChangedFrom(final String currentTeacher) {
        page.waitForCondition(
                () -> {
                    final String actual = normalize(activeTeacherTitle().textContent());
                    return !actual.equals(currentTeacher);
                },
                new Page.WaitForConditionOptions().setTimeout(10000)
        );
    }

    private Locator portalRoot() {
        return page.locator("#__PORTAL__").first();
    }

    private Locator activeSlide() {
        final Locator active = portalRoot().locator(
                ".swiper-slide-active, [class*='swiper-slide-active']"
        ).first();
        if (active.count() > 0) {
            return active;
        }
        return portalRoot();
    }

    private Locator activeTeacherTitle() {
        final Locator slide = activeSlide();
        final String[] selectors = {
                "h3", "h2", "[class*='title']", "[class*='name']"
        };

        for (final String selector : selectors) {
            final Locator elements = slide.locator(selector);
            final int count = elements.count();
            for (int i = 0; i < count; i++) {
                final Locator element = elements.nth(i);
                if (!element.isVisible()) {
                    continue;
                }

                final String text = normalize(element.textContent());
                if (looksLikeTeacherName(text)) {
                    return element;
                }
            }
        }

        throw new IllegalStateException("Не удалось найти имя преподавателя в активном popup-слайде");
    }

    private Locator nextButton() {
        return navigationButton(true);
    }

    private Locator previousButton() {
        return navigationButton(false);
    }

    private Locator navigationButton(final boolean next) {
        final Locator buttons = portalRoot().locator("button:visible");
        final int count = buttons.count();
        if (count == 0) {
            throw new IllegalStateException("В popup не найдены видимые кнопки навигации");
        }

        double bestScore = next ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        Locator bestButton = null;

        for (int i = 0; i < count; i++) {
            final Locator button = buttons.nth(i);
            final var box = button.boundingBox();
            if (box == null || box.width < 20 || box.height < 20) {
                continue;
            }

            if (next) {
                if (box.x > bestScore) {
                    bestScore = box.x;
                    bestButton = button;
                }
            } else {
                if (box.x < bestScore) {
                    bestScore = box.x;
                    bestButton = button;
                }
            }
        }

        if (bestButton == null) {
            throw new IllegalStateException("Не удалось определить кнопку навигации в popup");
        }

        return bestButton;
    }

    private boolean looksLikeTeacherName(final String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (text.length() < 5 || text.length() > 80) {
            return false;
        }
        if (text.matches(".*\\d.*")) {
            return false;
        }
        return text.matches("^[А-ЯЁA-Z][а-яёa-z]+(?:[- ][А-ЯЁA-Z][а-яёa-z]+){1,2}$");
    }

    private String normalize(final String value) {
        return value == null ? "" : value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
