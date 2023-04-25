package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditAgencyIDListPageImpl extends BasePageImpl implements EditAgencyIDListPage {
    private static final By AGENCY_ID_LIST_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-select[@placeholder = \"Namespace\"]");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    public static final By CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button/span");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");
    private static final By VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By ADD_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");

    private final AgencyIDListObject agencyIDList;

    public EditAgencyIDListPageImpl(BasePage parent, AgencyIDListObject agencyIDList) {
        super(parent);
        this.agencyIDList = agencyIDList;
    }

    @Override
    protected String getPageUrl() {
        if (this.agencyIDList.getAgencyIDListManifestId()!=null){
            return getConfig().getBaseUrl().resolve("/agency_id_list/" + this.agencyIDList.getAgencyIDListManifestId()).toString();
        }else{
            return getConfig().getBaseUrl().resolve("/agency_id_list/" + this.agencyIDList.getAgencyIDListId()).toString();
        }
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert getText(getTitle()).equals("Edit Agency ID List");
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-card-title/span[1]"));
    }

    @Override
    public void setName(String agencyIDListName) {
        sendKeys(getAgencyIDListNameField(), agencyIDListName);
    }
    @Override
    public WebElement getAgencyIDListNameField() {
        return visibilityOfElementLocated(getDriver(), AGENCY_ID_LIST_NAME_FIELD_LOCATOR);
    }
    @Override
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }
    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public void setNamespace(NamespaceObject namespace) {
        retry(() -> {
            click(getNamespaceSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + namespace.getUri() + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }
    @Override
    public void hitUpdateButton() {
        retry(() -> {
            click(getUpdateButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }
    @Override
    public void moveToQA() {
        retry(() -> {
            click(getMoveToQAButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getMoveToQAButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
    }

    @Override
    public void moveToProduction() {
        retry(() -> {
            click(getMoveToProductionButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getMoveToProductionButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
    }

    @Override
    public void setVersion(String version) {
        sendKeys(getVersionField(), version);
    }

    @Override
    public WebElement getVersionField() {
        return visibilityOfElementLocated(getDriver(), VERSION_FIELD_LOCATOR);
    }

    @Override
    public EditAgencyIDListValueDialog addAgencyIDListValue() {
        click(getAddAgencyIDListValueButton());
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = new EditAgencyIDListValueDialogImpl(this);
        assert editAgencyIDListValueDialog.isOpened();
        return editAgencyIDListValueDialog;
    }

    @Override
    public WebElement getAddAgencyIDListValueButton() {
        return elementToBeClickable(getDriver(), ADD_AGENCY_ID_LIST_VALUE_BUTTON_LOCATOR);
    }

}
