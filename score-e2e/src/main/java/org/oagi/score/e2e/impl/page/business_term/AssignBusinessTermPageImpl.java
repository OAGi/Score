package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class AssignBusinessTermPageImpl extends BasePageImpl implements AssignBusinessTermPage {

    private final BusinessTermAssignmentPageImpl parent;

    public AssignBusinessTermPageImpl(BusinessTermAssignmentPageImpl parent) {
        super(parent);
        this.parent = parent;
    }
    @Override
    protected String getPageUrl() {return getConfig().getBaseUrl().resolve("/business_term_management/assign_business_term/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Assign Business Term".equals(getText(getTitle()));

    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }
}
