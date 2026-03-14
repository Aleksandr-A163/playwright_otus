package ru.otus.tests;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.otus.extensions.PlaywrightExtension;
import ru.otus.pages.CatalogCoursesPage;

@ExtendWith(PlaywrightExtension.class)
public class CatalogCoursesFilterTest {

    private static final int MIN_MONTH = 3;
    private static final int MAX_MONTH = 10;

    @Inject
    private CatalogCoursesPage catalogCoursesPage;

    @Test
    void catalogCoursesFilterTest() {
        catalogCoursesPage.open();
        catalogCoursesPage
                .checkOpenPage()
                .checkDefaulFilterChecked()
                .moveStartSlaider(MIN_MONTH)
                .moveEndSlaider(MAX_MONTH)
                .checkTableCoursesDate(MIN_MONTH, MAX_MONTH)
                .checkedDirection("Архитектура")
                .resetFilter();
    }
}
