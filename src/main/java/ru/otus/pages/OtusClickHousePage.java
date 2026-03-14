package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import ru.otus.pages.components.TeacherPopup;
import ru.otus.pages.components.TeachersSection;

public final class OtusClickHousePage {

    private final Page page;
    private final TeachersSection teachersSection;
    private final TeacherPopup teacherPopup;

    private String clickedTeacherName;
    private String openedTeacherName;
    private String previousTeacherName;

    @Inject
    public OtusClickHousePage(
            final Page page,
            final TeachersSection teachersSection,
            final TeacherPopup teacherPopup
    ) {
        this.page = page;
        this.teachersSection = teachersSection;
        this.teacherPopup = teacherPopup;
    }

    public OtusClickHousePage open() {
        page.navigate(
                System.getProperty("baseUrl", "https://otus.ru/lessons/clickhouse/"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        );

        page.locator("h2")
                .filter(new Locator.FilterOptions().setHasText("Преподаватели"))
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(30000));

        return this;
    }

    public OtusClickHousePage checkTeachersVisible() {
        teachersSection.waitUntilVisible();

        final List<String> teachersBeforeDrag = teachersSection.getVisibleTeacherNames();
        Assertions.assertFalse(
                teachersBeforeDrag.isEmpty(),
                "В блоке преподавателей должны отображаться плитки"
        );

        return this;
    }

    public OtusClickHousePage scrollTeachers() {
        final List<String> teachersBeforeDrag = teachersSection.getVisibleTeacherNames();
        final List<String> teachersAfterDrag = teachersSection.dragCarouselLeftAndGetVisibleTeacherNames();

        Assertions.assertFalse(
                teachersAfterDrag.isEmpty(),
                "После прокрутки должны отображаться плитки преподавателей"
        );

        Assertions.assertNotEquals(
                teachersBeforeDrag,
                teachersAfterDrag,
                "После drag and drop список преподавателей должен измениться"
        );

        return this;
    }

    public OtusClickHousePage openTeacherPopup() {
        clickedTeacherName = teachersSection.clickFirstVisibleTeacher();

        teacherPopup.waitUntilOpened();
        openedTeacherName = teacherPopup.getOpenedTeacherName();

        Assertions.assertEquals(
                clickedTeacherName,
                openedTeacherName,
                "Должен открыться popup того преподавателя, по которому был клик"
        );

        return this;
    }

    public OtusClickHousePage checkNextTeacher() {
        previousTeacherName = openedTeacherName;
        final String nextTeacherName = teacherPopup.clickNextAndGetOpenedTeacherName();

        Assertions.assertNotEquals(
                previousTeacherName,
                nextTeacherName,
                "После нажатия > должен открыться другой преподаватель"
        );

        openedTeacherName = nextTeacherName;
        return this;
    }

    public OtusClickHousePage checkPreviousTeacher() {
        final String currentTeacherName = teacherPopup.clickPreviousAndGetOpenedTeacherName();

        Assertions.assertEquals(
                previousTeacherName,
                currentTeacherName,
                "После нажатия < должен открыться предыдущий преподаватель"
        );

        openedTeacherName = currentTeacherName;
        return this;
    }
}