package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

public final class OtusClickHousePage {

    private final Page page;

    @Inject
    public OtusClickHousePage(final Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate(
                System.getProperty("baseUrl", "https://otus.ru/lessons/clickhouse/"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        );

        page.locator("h2")
                .filter(new Locator.FilterOptions().setHasText("Преподаватели"))
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }
}