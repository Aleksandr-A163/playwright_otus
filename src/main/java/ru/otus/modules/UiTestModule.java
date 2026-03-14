package ru.otus.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import ru.otus.utils.UiActions;

public final class UiTestModule extends AbstractModule {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext browserContext;
    private final Page page;

    public UiTestModule(
            final Playwright playwright,
            final Browser browser,
            final BrowserContext browserContext,
            final Page page
    ) {
        this.playwright = playwright;
        this.browser = browser;
        this.browserContext = browserContext;
        this.page = page;
    }

    @Override
    protected void configure() {
        // no-op
    }

    @Provides
    @Singleton
    public Playwright providePlaywright() {
        return playwright;
    }

    @Provides
    @Singleton
    public Browser provideBrowser() {
        return browser;
    }

    @Provides
    @Singleton
    public BrowserContext provideBrowserContext() {
        return browserContext;
    }

    @Provides
    @Singleton
    public Page providePage() {
        return page;
    }

    @Provides
    @Singleton
    public UiActions provideUiActions(final Page providedPage) {
        return new UiActions(providedPage);
    }
}
