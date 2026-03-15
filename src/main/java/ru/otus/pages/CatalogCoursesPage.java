package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.BoundingBox;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import ru.otus.annotations.Path;
import ru.otus.utils.UiActions;

@Path("/catalog/courses")
public class CatalogCoursesPage extends AbsBasePage {

    private final Locator heading;
    private final Locator allDirection;
    private final Locator anyLevel;
    private final Locator sliderNowStart;
    private final Locator sliderNowEnd;
    private final Locator sliderDateActual;
    private final Locator tableCoursesDate;
    private final Locator tableCoursesName;
    private final Locator resetFilter;

    private final UiActions uiActions;

    private static final String DIRECTION = "//div[label [text() = '%s']]//input";

    @Inject
    public CatalogCoursesPage(final Page page, final UiActions uiActions) {
        super(page);
        this.uiActions = uiActions;
        this.heading = page.locator(".sc-v6opgt-1 div");
        this.allDirection = page.locator("//div[label [text() = 'Все направления']]//input");
        this.anyLevel = page.locator("//div[label [text() = 'Любой уровень']]//input");
        this.sliderNowStart = page.locator("div[aria-valuenow='0']");
        this.sliderNowEnd = page.locator("div[aria-valuenow='15']");
        this.sliderDateActual = page.locator(".sc-1i4kf3x-0");
        this.tableCoursesDate = page.locator("//div[@class = 'sc-18q05a6-0 incGfX']//a//div[contains(text(), 'месяц')]");
        this.tableCoursesName = page.locator("//div[@class = 'sc-18q05a6-0 incGfX']//a//h6//div").first();
        this.resetFilter = page.locator("//button[text() = 'Очистить фильтры']");
    }

    public CatalogCoursesPage checkOpenPage() {
        Assertions.assertTrue(
                heading.getByText("Каталог").first().isVisible(),
                "Страница каталога не открылась"
        );
        return this;
    }

    public CatalogCoursesPage checkDefaulFilterChecked() {
        Assertions.assertTrue(allDirection.isChecked(), "Фильтр 'Все направления' не выбран");
        Assertions.assertTrue(anyLevel.isChecked(), "Фильтр 'Любой уровень' не выбран");
        return this;
    }

    public CatalogCoursesPage moveStartSlaider(final int month) {
        uiActions.highlight(sliderNowStart);

        final BoundingBox box = sliderNowStart.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Не удалось получить boundingBox стартового слайдера");
        }

        final int max = findActualSettingsDate().get(1);

        final Response response = page.waitForResponse(
                resp -> resp.url().contains("api/catalog.entity.list")
                        && resp.url().contains("duration=%s".formatted(month))
                        && resp.url().contains("duration=%s".formatted(max)),
                () -> {
                    sliderNowStart.hover();
                    page.mouse().down();
                    page.mouse().move(box.x + 16.6 * month, box.y);
                    page.mouse().up();
                }
        );

        Assertions.assertEquals(200, response.status(), "API должен вернуть 200 OK");
        return this;
    }

    public CatalogCoursesPage moveEndSlaider(final int month) {
        uiActions.highlight(sliderNowEnd);

        final BoundingBox box = sliderNowEnd.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Не удалось получить boundingBox конечного слайдера");
        }

        final int maxDuration = 15 - month;

        sliderNowEnd.hover();
        page.mouse().down();
        page.mouse().move(box.x - 13 * maxDuration, box.y);
        page.mouse().up();

        return this;
    }

    public CatalogCoursesPage checkTableCoursesDate(final int minMonth, final int maxMonth) {
        final int count = tableCoursesDate.count();

        for (int i = 0; i < count; i++) {
            final Locator course = tableCoursesDate.nth(i);
            final int actualDuration = findDurationCourse(course);
            final boolean matches = actualDuration <= maxMonth && actualDuration >= minMonth;

            if (!matches) {
                throw new AssertionError("Карточка курса не соответствует ожидаемой продолжительности");
            }
        }

        return this;
    }

    public CatalogCoursesPage checkedDirection(final String directionName) {
        final String before = tableCoursesName.textContent();
        final Locator direction = page.locator(DIRECTION.formatted(directionName));

        uiActions.highlight(direction);
        direction.check();

        Assertions.assertTrue(direction.isChecked(), "Фильтр %s не выбран".formatted(directionName));
        page.waitForTimeout(2000);

        Assertions.assertNotEquals(
                before,
                tableCoursesName.textContent(),
                "Первая плитка курса не изменилась"
        );

        return this;
    }

    public CatalogCoursesPage resetFilter() {
        final String before = tableCoursesName.textContent();

        uiActions.click(resetFilter);
        page.waitForTimeout(1000);

        Assertions.assertTrue(allDirection.isChecked(), "Фильтр 'Все направления' не выбран");
        Assertions.assertTrue(anyLevel.isChecked(), "Фильтр 'Любой уровень' не выбран");
        Assertions.assertNotEquals(before, tableCoursesName.textContent());

        return this;
    }

    private List<Integer> findActualSettingsDate() {
        final List<Integer> numbers = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\d+").matcher(sliderDateActual.textContent());

        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        return numbers;
    }

    private int findDurationCourse(final Locator info) {
        final Matcher matcher = Pattern.compile("(\\d+)\\s+месяц").matcher(info.textContent());

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        throw new IllegalStateException("Не найдена информация о продолжительности курса");
    }

}