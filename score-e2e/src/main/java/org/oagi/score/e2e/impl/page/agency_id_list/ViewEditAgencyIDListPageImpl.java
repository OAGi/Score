package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class ViewEditAgencyIDListPageImpl extends BasePageImpl implements ViewEditAgencyIDListPage {

    public ViewEditAgencyIDListPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/agency_id_list").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Agency ID List".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

}
