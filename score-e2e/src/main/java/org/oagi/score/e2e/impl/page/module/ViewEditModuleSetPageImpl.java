package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.code_list.UpliftCodeListPageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.oagi.score.e2e.page.module.EditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditModuleSetPageImpl extends BasePageImpl implements ViewEditModuleSetPage {
    private static final By NEW_MODULE_SET_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Module Set\")]//ancestor::button[1]");

    public ViewEditModuleSetPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Module Set".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNewModuleSetButton() {
        return elementToBeClickable(getDriver(), NEW_MODULE_SET_BUTTON_LOCATOR);
    }

    @Override
    public EditModuleSetPage hitNewModuleSetButton() {
        retry(() -> click(getNewModuleSetButton()));
        EditModuleSetPage editModuleSetPage = new EditModuleSetPageImpl(this);
        assert editModuleSetPage.isOpened();
        return editModuleSetPage;
    }
}
