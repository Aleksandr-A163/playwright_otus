package ru.otus.pages;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import ru.otus.annotations.Path;
import ru.otus.datamodule.OptionsSubscriptionEnum;
import ru.otus.utils.UiActions;

@Path("/subscription")
public class SubscriptionPage extends AbsBasePage {

    private final Locator header;
    private final Locator headOptionSubsc;
    private final Locator optionsSubs;
    private final Locator moreDetailButton;
    private final Locator dopInformation;

    private final UiActions uiActions;

    private final List<String> optionsSubsName = List.of(
            OptionsSubscriptionEnum.BASIC_M6.getOptionName(),
            OptionsSubscriptionEnum.STANDARD_M6.getOptionName(),
            OptionsSubscriptionEnum.PROFESSIONAL_M6.getOptionName()
    );

    @Inject
    public SubscriptionPage(final Page page, final UiActions uiActions) {
        super(page);
        this.uiActions = uiActions;
        this.header = page.locator(".sc-1x3lr1v-3");
        this.headOptionSubsc = page.locator(".sc-1ta5213-1");
        this.optionsSubs = page.locator(".sc-1a5myy-0");
        this.moreDetailButton = page.locator(".sc-1a5myy-2 button");
        this.dopInformation = page.locator(".sc-1fugrkh-2");
    }

    public SubscriptionPage checkOpenPage() {
        Assertions.assertTrue(
                header.getByText("Подписка на курсы OTUS").first().isVisible(),
                "Страница подписки не открылась"
        );
        return this;
    }

    public SubscriptionPage checkOptionalSubscription() {
        Assertions.assertTrue(
                headOptionSubsc.getByText("Варианты подписки").first().isVisible(),
                "Заголовок 'Варианты подписки' не отображается"
        );

        final int count = optionsSubs.count();
        for (int i = 0; i < count && i < optionsSubsName.size(); i++) {
            final String actualName = optionsSubs.nth(i).locator("h4").textContent().trim();
            Assertions.assertEquals(optionsSubsName.get(i), actualName);
        }

        return this;
    }

    public SubscriptionPage clickMoreDetail(final OptionsSubscriptionEnum option) {
        final Locator detailButton = moreDetailButton.nth(option.getId());

        Assertions.assertEquals("Подробнее", detailButton.textContent(), "Название кнопки должно соответствовать");
        Assertions.assertFalse(dopInformation.nth(option.getId()).isVisible(), "Доп. информация должна быть скрыта");

        uiActions.click(detailButton);

        Assertions.assertEquals("Свернуть", detailButton.textContent(), "Название кнопки должно было измениться");
        dopInformation.nth(option.getId()).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(1000)
        );

        return this;
    }

    public SubscriptionPage clickHideMoreDetail(final OptionsSubscriptionEnum option) {
        final Locator detailButton = moreDetailButton.nth(option.getId());

        Assertions.assertEquals("Свернуть", detailButton.textContent(), "Название кнопки должно соответствовать");
        Assertions.assertTrue(dopInformation.nth(option.getId()).isVisible(), "Доп. информация должна быть видна");

        uiActions.click(detailButton);

        Assertions.assertEquals("Подробнее", detailButton.textContent(), "Название кнопки должно было измениться");
        dopInformation.nth(option.getId()).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(1000)
        );

        return this;
    }
}