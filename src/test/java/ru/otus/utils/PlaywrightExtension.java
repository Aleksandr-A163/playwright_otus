package ru.otus.utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.otus.di.UiTestModule;

public final class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PlaywrightExtension.class);
    private static final String PLAYWRIGHT_KEY = "playwright";
    private static final String BROWSER_KEY = "browser";
    private static final String CONTEXT_KEY = "context";

    @Override
    public void beforeEach(final ExtensionContext context) {
        final Playwright playwright = Playwright.create();
        final Browser browser = launchBrowser(playwright);
        final BrowserContext browserContext = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1280, 900)
        );

        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        final Page page = browserContext.newPage();
        final Injector injector = Guice.createInjector(
                new UiTestModule(playwright, browser, browserContext, page)
        );
        injector.injectMembers(context.getRequiredTestInstance());

        final ExtensionContext.Store store = context.getStore(NAMESPACE);
        store.put(PLAYWRIGHT_KEY, playwright);
        store.put(BROWSER_KEY, browser);
        store.put(CONTEXT_KEY, browserContext);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws IOException {
        final ExtensionContext.Store store = context.getStore(NAMESPACE);
        final BrowserContext browserContext = store.remove(CONTEXT_KEY, BrowserContext.class);
        final Browser browser = store.remove(BROWSER_KEY, Browser.class);
        final Playwright playwright = store.remove(PLAYWRIGHT_KEY, Playwright.class);

        if (browserContext != null) {
            Files.createDirectories(Path.of("traces"));
            final Path tracePath = Path.of("traces", buildTraceFileName(context));
            browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            browserContext.close();
        }

        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }

    private Browser launchBrowser(final Playwright playwright) {
        final String browserName = System.getProperty("browser", "chromium").toLowerCase();
        final boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        final BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);

        return switch (browserName) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };
    }

    private String buildTraceFileName(final ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName()
                + "_"
                + context.getRequiredTestMethod().getName()
                + ".zip";
    }
}