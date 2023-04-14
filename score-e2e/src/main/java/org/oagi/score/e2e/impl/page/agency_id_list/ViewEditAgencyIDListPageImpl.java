package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

public class ViewEditAgencyIDListPageImpl extends BasePageImpl implements ViewEditAgencyIDListPage {
    private static final By NEW_AGENCY_ID_LIST_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Agency ID List\")]//ancestor::button[1]");
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

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

    @Override
    public EditAgencyIDListPage openNewAgencyIDList(AppUserObject user, String release) {
        setBranch(release);
        retry(() -> {
            click(getNewAgencyIDListButton());
            waitFor(ofMillis(1000L));
        });

        invisibilityOfLoadingContainerElement(getDriver());
        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().getNewlyCreatedAgencyIDList(user, release);
        EditAgencyIDListPage editAgencyIDListPage = new EditAgencyIDListPageImpl(this, agencyIDList);
        assert editAgencyIDListPage.isOpened();
        return editAgencyIDListPage;
    }

    @Override
    public WebElement getNewAgencyIDListButton() {
        return elementToBeClickable(getDriver(), NEW_AGENCY_ID_LIST_BUTTON_LOCATOR);
    }
    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }
}
