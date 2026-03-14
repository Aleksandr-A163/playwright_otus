package ru.otus.tests;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.otus.datamodule.OptionsSubscriptionEnum;
import ru.otus.extensions.PlaywrightExtension;
import ru.otus.pages.SubscriptionPage;

@ExtendWith(PlaywrightExtension.class)
public class SubscriptionTest {

    @Inject
    private SubscriptionPage subscriptionPage;

    @Test
    void subscriptionTest() {
        subscriptionPage.open();
        subscriptionPage
                .checkOpenPage()
                .checkOptionalSubscription()
                .clickMoreDetail(OptionsSubscriptionEnum.STANDARD_M6)
                .clickHideMoreDetail(OptionsSubscriptionEnum.STANDARD_M6);
    }
}
