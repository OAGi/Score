package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.CreateBusinessTermPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class CreateBusinessTermPageImpl extends BasePageImpl implements CreateBusinessTermPage {
    public CreateBusinessTermPageImpl(BasePage parent){ super(parent);}

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/business_term/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Business Term".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }
}
