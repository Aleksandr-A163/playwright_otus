package ru.otus.utils;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

public final class UiActions {

    private final Page page;

    @Inject
    public UiActions(final Page page) {
        this.page = page;
    }

    public void click(final Locator locator) {
        locator.scrollIntoViewIfNeeded();
        highlight(locator);
        locator.click(new Locator.ClickOptions().setTimeout(10000));
    }

    public void clickAtCenter(final Locator locator) {
        locator.scrollIntoViewIfNeeded();
        highlight(locator);

        final BoundingBox box = locator.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Не удалось получить boundingBox для клика по центру");
        }

        final double x = box.x + box.width / 2.0;
        final double y = box.y + box.height / 2.0;

        page.mouse().move(x, y);
        page.waitForTimeout(150);
        page.mouse().click(x, y);
    }

    public void highlight(final Locator locator) {
        locator.evaluate("""
            element => {
                const previousOutline = element.style.outline;
                const previousOutlineOffset = element.style.outlineOffset;
                element.style.outline = '3px solid red';
                element.style.outlineOffset = '2px';
                setTimeout(() => {
                    element.style.outline = previousOutline;
                    element.style.outlineOffset = previousOutlineOffset;
                }, 700);
            }
        """);
    }

    public void dragHorizontally(final Locator locator, final int deltaX) {
        locator.scrollIntoViewIfNeeded();
        highlight(locator);

        final BoundingBox box = locator.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Не удалось получить boundingBox для drag target");
        }

        final double startX = box.x + box.width * 0.75;
        final double endX = startX + deltaX;
        final double y = box.y + box.height / 2.0;

        page.mouse().move(startX, y);
        page.waitForTimeout(200);
        page.mouse().down();
        page.waitForTimeout(200);
        page.mouse().move(startX - 120, y, new Mouse.MoveOptions().setSteps(10));
        page.waitForTimeout(100);
        page.mouse().move(startX - 260, y, new Mouse.MoveOptions().setSteps(10));
        page.waitForTimeout(100);
        page.mouse().move(endX, y, new Mouse.MoveOptions().setSteps(20));
        page.waitForTimeout(250);
        page.mouse().up();
        page.waitForTimeout(1500);
    }
}
