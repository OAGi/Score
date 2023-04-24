package org.oagi.score.e2e.page.agency_id_list;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Agency ID List' page.
 */
public interface ViewEditAgencyIDListPage extends Page {
    EditAgencyIDListPage openNewAgencyIDList(AppUserObject user, String release);

    WebElement getNewAgencyIDListButton();

    void setBranch(String branch);

    WebElement getBranchSelectField();
}
