package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.TenantAssociationPage;
import org.openqa.selenium.By;

import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

abstract class AbstractTenantAssociationPageImpl extends AbstractTenantTablePageImpl implements TenantAssociationPage {

    private static final By SWITCH_BUTTON_LOCATOR =
            By.xpath("//score-list-page//button[normalize-space(.) = \"Add User\" or normalize-space(.) = \"Add Business Context\""
                    + " or normalize-space(.) = \"Done\"]");

    private final String switchToAddViewText;

    private final String pageUrlPrefix;

    AbstractTenantAssociationPageImpl(BasePage parent, String pageUrlPrefix, String switchToAddViewText) {
        super(parent);
        this.pageUrlPrefix = pageUrlPrefix;
        this.switchToAddViewText = switchToAddViewText;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve(pageUrlPrefix).toString();
    }

    @Override
    public void openPage() {
        throw new UnsupportedOperationException("Open it from the 'Tenant' page.");
    }

    @Override
    public List<String> getListedKeys() {
        return getStableKeys();
    }

    @Override
    public void switchToAddView() {
        retry(() -> click(elementToBeClickable(getDriver(), By.xpath(
                "//score-list-page//button[normalize-space(.) = \"" + switchToAddViewText + "\"]"))));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public void switchToListView() {
        retry(() -> click(elementToBeClickable(getDriver(), By.xpath(
                "//score-list-page//button[normalize-space(.) = \"Done\"]"))));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public void add(String key) {
        retry(() -> click(buttonOfRow(key, "Add")));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void remove(String key) {
        retry(() -> click(buttonOfRow(key, "Remove")));
        invisibilityOfLoadingContainerElement(getDriver());
    }

}
