package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.openqa.selenium.WebElement;

public class AssignBusinessTermPageImpl extends BasePageImpl implements AssignBusinessTermPage {

    private final BusinessTermAssignmentPageImpl parent;

    public AssignBusinessTermPageImpl(BusinessTermAssignmentPageImpl parent) {
        super(parent);
        this.parent = parent;
    }
    @Override
    protected String getPageUrl() {
        return null;
    }

    @Override
    public void openPage() {

    }

    @Override
    public WebElement getTitle() {
        return null;
    }
}
