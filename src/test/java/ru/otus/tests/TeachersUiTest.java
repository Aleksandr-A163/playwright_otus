package ru.otus.tests;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.otus.extensions.PlaywrightExtension;
import ru.otus.pages.OtusClickHousePage;

@ExtendWith(PlaywrightExtension.class)
public class TeachersUiTest {

    @Inject
    private OtusClickHousePage otusClickHousePage;

    @Test
    void teachersPopupNavigationTest() {
        otusClickHousePage.open();

        otusClickHousePage
                .checkTeachersVisible()
                .scrollTeachers()
                .openTeacherPopup()
                .checkNextTeacher()
                .checkPreviousTeacher();
    }
}