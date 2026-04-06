package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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
  private static final String DIRECTION = "//div[label[text() = '%s']]//input";
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
  private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s+месяц");

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

  @Inject
  public CatalogCoursesPage(final Page page, final UiActions uiActions) {
    super(page);
    this.uiActions = uiActions;

    this.heading = page.getByText("Каталог").first();
    this.allDirection = page.locator("//div[label[text() = 'Все направления']]//input");
    this.anyLevel = page.locator("//div[label[text() = 'Любой уровень']]//input");
    this.sliderNowStart = page.locator("div[aria-valuenow='0']").first();
    this.sliderNowEnd = page.locator("div[aria-valuenow='15']").first();
    this.sliderDateActual = page.locator("xpath=//div[contains(., 'От') and contains(., 'месяц')]").first();

    this.tableCoursesDate = page.locator(
        "xpath=(//div[.//text()[normalize-space()='Каталог']])[1]/following::a//div[contains(., 'месяц')]"
    );
    this.tableCoursesName = page.locator(
        "xpath=(//div[.//text()[normalize-space()='Каталог']])[1]/following::a//h6//div"
    ).first();

    this.resetFilter = page.getByRole(
        com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Очистить фильтры")
    );
  }

  public CatalogCoursesPage checkOpenPage() {
    Assertions.assertTrue(heading.isVisible(), "Страница каталога не открылась");
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

    sliderNowStart.hover();
    page.mouse().down();
    page.mouse().move(box.x + 16.6 * month, box.y);
    page.mouse().up();

    page.waitForCondition(() -> {
      final List<Integer> values = findActualSettingsDate();
      return values.size() >= 2 && values.get(0) == month;
    });

    page.waitForTimeout(1000);
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

    page.waitForCondition(() -> {
      final List<Integer> values = findActualSettingsDate();
      return values.size() >= 2 && values.get(1) == month;
    });

    page.waitForTimeout(1000);
    return this;
  }

  public CatalogCoursesPage checkTableCoursesDate(final int minMonth, final int maxMonth) {
    final int count = tableCoursesDate.count();
    Assertions.assertTrue(count > 0, "После применения фильтра не найдено ни одной карточки курса");

    for (int i = 0; i < count; i++) {
      final Locator course = tableCoursesDate.nth(i);
      final String rawText = course.textContent();
      final int actualDuration = findDurationCourse(course);

      final boolean matches = actualDuration >= minMonth && actualDuration <= maxMonth;
      if (!matches) {
        throw new AssertionError(
            "Карточка курса не соответствует ожидаемой продолжительности. "
                + "Текст: '" + rawText + "', длительность: " + actualDuration
                + ", ожидался диапазон: [" + minMonth + ", " + maxMonth + "]"
        );
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
    page.waitForLoadState();
    page.waitForTimeout(1500);

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
    page.waitForLoadState();
    page.waitForTimeout(1000);

    Assertions.assertTrue(allDirection.isChecked(), "Фильтр 'Все направления' не выбран");
    Assertions.assertTrue(anyLevel.isChecked(), "Фильтр 'Любой уровень' не выбран");
    Assertions.assertNotEquals(before, tableCoursesName.textContent());

    return this;
  }

  private List<Integer> findActualSettingsDate() {
    final List<Integer> numbers = new ArrayList<>();
    final String text = sliderDateActual.textContent();
    final Matcher matcher = NUMBER_PATTERN.matcher(text);

    while (matcher.find()) {
      numbers.add(Integer.parseInt(matcher.group()));
    }

    return numbers;
  }

  private int findDurationCourse(final Locator info) {
    final String text = info.textContent();
    final Matcher matcher = DURATION_PATTERN.matcher(text);

    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }

    throw new IllegalStateException("Не найдена информация о продолжительности курса: " + text);
  }
}