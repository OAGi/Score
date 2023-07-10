package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CreateModuleSetReleasePage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.click;

public class ViewEditModuleSetReleasePageImpl extends BasePageImpl implements ViewEditModuleSetReleasePage {
    private static final By NEW_MODULE_SET_RELEASE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Module Set Release\")]//ancestor::button[1]");

    public ViewEditModuleSetReleasePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set_release").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Module Set Release".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public CreateModuleSetReleasePage hitNewModuleSetReleaseButton() {
        retry(() -> click(getNewModuleSetReleaseButton()));
        CreateModuleSetReleasePage createModuleSetReleasePage = new CreateModuleSetReleasePageImpl(this);
        assert createModuleSetReleasePage.isOpened();
        return createModuleSetReleasePage;
    }

    @Override
    public WebElement getNewModuleSetReleaseButton() {
        return elementToBeClickable(getDriver(), NEW_MODULE_SET_RELEASE_BUTTON_LOCATOR);
    }
}
