package ru.otus.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.otus.pages.OtusClickHousePage;
import ru.otus.pages.TeacherPopup;
import ru.otus.pages.TeachersSection;
import ru.otus.utils.PlaywrightExtension;

@ExtendWith(PlaywrightExtension.class)
public class TeachersUiTest {

    @Inject
    private OtusClickHousePage otusClickHousePage;

    @Inject
    private TeachersSection teachersSection;

    @Inject
    private TeacherPopup teacherPopup;

    @Test
    void shouldOpenTeacherPopupAndNavigateBetweenTeachers() {
        otusClickHousePage.open();

        teachersSection.waitUntilVisible();

        final List<String> teachersBeforeDrag = teachersSection.getVisibleTeacherNames();
        assertFalse(
                teachersBeforeDrag.isEmpty(),
                "В блоке преподавателей должны отображаться плитки"
        );

        /*
         * Проверка прокрутки уже находится внутри TeachersSection:
         * если swiper не сдвинется, метод сам бросит AssertionError.
         * Дополнительно сравнивать списки имен нельзя — для swiper это нестабильный критерий.
         */
        teachersSection.dragCarouselLeftAndGetVisibleTeacherNames();

        final String clickedTeacherName = teachersSection.clickFirstVisibleTeacher();

        teacherPopup.waitUntilOpened();
        final String openedTeacherName = teacherPopup.getOpenedTeacherName();

        assertEquals(
                clickedTeacherName,
                openedTeacherName,
                "Должен открыться popup того преподавателя, по которому был клик"
        );

        final String nextTeacherName = teacherPopup.clickNextAndGetOpenedTeacherName();
        assertNotEquals(
                openedTeacherName,
                nextTeacherName,
                "После нажатия > должен открыться другой преподаватель"
        );

        final String previousTeacherName = teacherPopup.clickPreviousAndGetOpenedTeacherName();
        assertEquals(
                openedTeacherName,
                previousTeacherName,
                "После нажатия < должен открыться предыдущий преподаватель"
        );
    }
}