package ru.otus.pages;

import com.microsoft.playwright.Page;
import ru.otus.annotations.Path;
import ru.otus.exceptions.PathNotFoundException;

public abstract class AbsBasePage {

    private final String siteUrl = System.getProperty("siteUrl", "https://otus.ru");
    protected final Page page;

    protected AbsBasePage(final Page page) {
        this.page = page;
    }

    private String getPath() {
        final Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(Path.class)) {
            return clazz.getDeclaredAnnotation(Path.class).value();
        }
        throw new PathNotFoundException();
    }

    public void open() {
        page.navigate(siteUrl + getPath());
    }
}
