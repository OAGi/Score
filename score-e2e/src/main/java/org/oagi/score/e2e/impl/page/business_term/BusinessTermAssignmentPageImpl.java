package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class BusinessTermAssignmentPageImpl extends BasePageImpl implements BusinessTermAssignmentPage {
    public BusinessTermAssignmentPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/assign_business_term").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Business Term Assignment".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }
}
