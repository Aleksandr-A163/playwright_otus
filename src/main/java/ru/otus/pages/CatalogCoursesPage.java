package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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
  private static final int DEFAULT_MIN_DURATION = 0;
  private static final int DEFAULT_MAX_DURATION = 15;

  private final Locator heading;
  private final Locator allDirection;
  private final Locator anyLevel;
  private final Locator sliderNowStart;
  private final Locator sliderNowEnd;
  private final Locator sliderDateActual;
  private final Locator courseCards;
  private final Locator firstCourseCard;
  private final Locator resetFilter;
  private final UiActions uiActions;

  @Inject
  public CatalogCoursesPage(final Page page, final UiActions uiActions) {
    super(page);
    this.uiActions = uiActions;

    this.heading = page.getByText("Каталог").first();
    this.allDirection = page.locator("//div[label[text() = 'Все направления']]//input");
    this.anyLevel = page.locator("//div[label[text() = 'Любой уровень']]//input");

    this.sliderNowStart = page.locator("[role='slider']").first();
    this.sliderNowEnd = page.locator("[role='slider']").nth(1);
    this.sliderDateActual =
        page.locator("xpath=//div[contains(., 'От') and contains(., 'месяц')]").first();

    this.courseCards =
        page.locator("xpath=(//a[.//div[contains(., 'месяц')]])[position() <= 20]");
    this.firstCourseCard = courseCards.first();

    this.resetFilter =
        page.getByRole(
            com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Очистить фильтры"));
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
    setSliderValue(sliderNowStart, month);
    waitSliderValues(month, getSliderValue(sliderNowEnd));
    page.waitForTimeout(1000);
    return this;
  }

  public CatalogCoursesPage moveEndSlaider(final int month) {
    setSliderValue(sliderNowEnd, month);
    waitSliderValues(getSliderValue(sliderNowStart), month);
    page.waitForTimeout(1000);
    return this;
  }

  public CatalogCoursesPage checkTableCoursesDate(final int minMonth, final int maxMonth) {
    page.waitForTimeout(1500);

    final int count = courseCards.count();
    Assertions.assertTrue(count > 0, "После применения фильтра не найдено ни одной карточки курса");

    for (int i = 0; i < count; i++) {
      final Locator card = courseCards.nth(i);
      final String rawText = card.textContent();
      final int actualDuration = findDurationCourse(card);

      final boolean matches = actualDuration >= minMonth && actualDuration <= maxMonth;
      if (!matches) {
        throw new AssertionError(
            "Карточка курса не соответствует ожидаемой продолжительности. "
                + "Текст: '"
                + rawText
                + "', длительность: "
                + actualDuration
                + ", ожидался диапазон: ["
                + minMonth
                + ", "
                + maxMonth
                + "]");
      }
    }

    return this;
  }

  public CatalogCoursesPage checkDirection(final String directionName) {
    final Locator direction = page.locator(DIRECTION.formatted(directionName));
    final List<String> beforeCards = getFirstCourseCardsTexts(5);

    uiActions.highlight(direction);
    direction.check();

    Assertions.assertTrue(
        direction.isChecked(),
        "Фильтр '%s' не выбран".formatted(directionName));

    page.waitForLoadState();
    page.waitForTimeout(1500);

    final int afterCount = courseCards.count();
    Assertions.assertTrue(
        afterCount > 0,
        "После выбора фильтра '%s' не найдено ни одной карточки курса"
            .formatted(directionName));

    final List<String> afterCards = getFirstCourseCardsTexts(5);

    Assertions.assertTrue(
        !beforeCards.equals(afterCards),
        "После выбора фильтра '%s' плитки каталога не изменились".formatted(directionName));

    return this;
  }

  public CatalogCoursesPage resetFilter() {

      final String beforeFirstCourse = firstCourseCard.textContent();
      final int beforeCount = courseCards.count();

      uiActions.click(resetFilter);
      page.waitForLoadState();
      waitSliderValues(DEFAULT_MIN_DURATION, DEFAULT_MAX_DURATION);
      page.waitForTimeout(1000);

      Assertions.assertTrue(allDirection.isChecked(), "Фильтр 'Все направления' не выбран");
      Assertions.assertTrue(anyLevel.isChecked(), "Фильтр 'Любой уровень' не выбран");

      Assertions.assertEquals(
          DEFAULT_MIN_DURATION,
          getSliderValue(sliderNowStart),
          "Минимальная длительность должна сброситься в 0");

      Assertions.assertEquals(
          DEFAULT_MAX_DURATION,
          getSliderValue(sliderNowEnd),
          "Максимальная длительность должна сброситься в 15");

      final String afterFirstCourse = firstCourseCard.textContent();
      final int afterCount = courseCards.count();

      Assertions.assertTrue(afterCount > 0, "После сброса фильтра не найдено ни одной карточки курса");

      Assertions.assertTrue(
          beforeCount != afterCount || !beforeFirstCourse.equals(afterFirstCourse),
          "После сброса фильтра плитки каталога не изменились");

      return this;
  }

  private void setSliderValue(final Locator slider, final int targetValue) {
    uiActions.highlight(slider);

    final int currentValue = getSliderValue(slider);
    final int diff = targetValue - currentValue;

    if (diff == 0) {
      return;
    }

    slider.click();

    final String key = diff > 0 ? "ArrowRight" : "ArrowLeft";
    for (int i = 0; i < Math.abs(diff); i++) {
      slider.press(key);
      page.waitForTimeout(150);
    }

    waitSliderValue(slider, targetValue);
  }

  private void waitSliderValue(final Locator slider, final int expectedValue) {
    page.waitForCondition(() -> getSliderValue(slider) == expectedValue);
  }

  private void waitSliderValues(final int expectedMin, final int expectedMax) {
    page.waitForCondition(
        () ->
            getSliderValue(sliderNowStart) == expectedMin
                && getSliderValue(sliderNowEnd) == expectedMax);
  }

  private int getSliderValue(final Locator slider) {
    final String value = slider.getAttribute("aria-valuenow");
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("У ползунка отсутствует aria-valuenow");
    }
    return Integer.parseInt(value);
  }

  private List<String> getFirstCourseCardsTexts(final int limit) {
    final List<String> cards = new ArrayList<>();
    final int count = Math.min(courseCards.count(), limit);

    for (int i = 0; i < count; i++) {
      cards.add(courseCards.nth(i).textContent().trim());
    }

    return cards;
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