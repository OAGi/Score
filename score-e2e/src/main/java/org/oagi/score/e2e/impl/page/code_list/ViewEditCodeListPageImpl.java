package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class ViewEditCodeListPageImpl extends BasePageImpl implements ViewEditCodeListPage {

    public ViewEditCodeListPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/code_list").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Code List".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

}
