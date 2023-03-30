package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditCodeListPageImpl extends BasePageImpl implements EditCodeListPage {

    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By CODE_LIST_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By OWNER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::mat-form-field//input");
    private static final By REVISION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Revision\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release\")]//ancestor::mat-form-field//input");
    private static final By STATE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"State\")]//ancestor::mat-form-field//input");
    private static final By LIST_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"List ID\")]//ancestor::mat-form-field//input");
    private static final By GUID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"GUID\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By CANCEL_REVISION_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");
    private static final By MOVE_TO_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
    private static final By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");
    private static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    private static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    private static final By ADD_CODE_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");
    private static final By REMOVE_CODE_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]");
    private static final By ADD_COMMENT_ICON_LOCATOR =
            By.xpath("//span/mat-icon[contains(text(), \"comments\")]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    public static final By CONTINUE_TO_RESTORE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Restore\")]//ancestor::button/span");
    public static final By CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Delete anyway\")]//ancestor::button/span");
    public static final By CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button/span");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-checkbox");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-select[@placeholder = \"Namespace\"]");
    private static final By AGENCY_ID_LIST_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text()= \"Agency ID List\"]//ancestor::div[1]/mat-select");
    public static final By CONTINUE_REMOVE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Remove\")]//ancestor::button/span");
    private static final By DERIVE_CODE_LIST_BASED_ON_THIS_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Derive Code List based on this\")]//ancestor::button[1]");
    private static final By DEFINITION_EMPTY_WARNING_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");
    private static final By UPDATE_ANYWAY_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update Anyway\")]//ancestor::button/span");
    private static final By CONTINUE_CANCEL_REVISION_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Okay\")]//ancestor::button/span");
    private final CodeListObject codeList;

    public EditCodeListPageImpl(BasePage parent, CodeListObject codeList) {
        super(parent);
        this.codeList = codeList;
    }

    @Override
    protected String getPageUrl() {
        if (this.codeList.getCodeListManifestId()!=null){
            return getConfig().getBaseUrl().resolve("/code_list/" + this.codeList.getCodeListManifestId()).toString();
        }else{
            return getConfig().getBaseUrl().resolve("/code_list/" + this.codeList.getCodeListId()).toString();
        }
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert getText(getTitle()).equals("Edit Code List");
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-card-title/span[1]"));
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public void setDefinitionSource(String definitionSource) {
        sendKeys(getDefinitionSourceField(), definitionSource);
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }
    @Override
    public WebElement getDefinitionSourceField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
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
    public EditCodeListValueDialog addCodeListValue() {
        click(getAddCodeListValueButton());
        EditCodeListValueDialog editCodeListValueDialog = new EditCodeListValueDialogImpl(this);
        assert editCodeListValueDialog.isOpened();
        return editCodeListValueDialog;
    }

    @Override
    public WebElement getAddCodeListValueButton() {
        return elementToBeClickable(getDriver(), ADD_CODE_LIST_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public AddCommentDialog hitAddCommentButton() {
        click(getAddCommentButton());
        AddCommentDialog addCodeListCommentDialog = new AddCommentDialogImpl(this);
        assert addCodeListCommentDialog.isOpened();
        return addCodeListCommentDialog;
    }

    @Override
    public WebElement getAddCommentButton() {
        return elementToBeClickable(getDriver(), ADD_COMMENT_ICON_LOCATOR);
    }

    @Override
    public void hitRevise() {
        click(getReviseButton());
        click(elementToBeClickable(getDriver(), CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Revised".equals(getSnackBarMessage(getDriver()));
    }
    @Override
    public WebElement getReviseButton() {
        return elementToBeClickable(getDriver(), REVISE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getCodeListNameField() {
        return visibilityOfElementLocated(getDriver(), CODE_LIST_NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getVersionField() {
        return visibilityOfElementLocated(getDriver(), VERSION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }
    @Override
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }
    @Override
    public WebElement getReleaseField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_FIELD_LOCATOR);
    }
    @Override
    public WebElement getRevisionField() {
        return visibilityOfElementLocated(getDriver(), REVISION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getAgencyIDListField() {
        return visibilityOfElementLocated(getDriver(), AGENCY_ID_LIST_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setName(String codeListName) {
        sendKeys(getCodeListNameField(), codeListName);
    }

    @Override
    public void selectCodeListValue(String valueCode) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(valueCode);
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        });
    }
    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }
    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void removeCodeListValue() {
        retry(() -> {
            click(getRemoveValueButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_REMOVE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
    @Override
    public WebElement getRemoveValueButton() {
        return elementToBeClickable(getDriver(), REMOVE_CODE_LIST_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getDeriveCodeListBasedOnThisButton() {
        return elementToBeClickable(getDriver(), DERIVE_CODE_LIST_BASED_ON_THIS_BUTTON_LOCATOR);
    }
    @Override
    public void hitDeriveCodeListBasedOnThisButton() {
        retry(() -> {
            click(getDeriveCodeListBasedOnThisButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void setVersion(String version) {
        sendKeys(getVersionField(), version);
    }

    @Override
    public String getDefinitionWarningDialogMessage() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_EMPTY_WARNING_DIALOG_MESSAGE_LOCATOR).getText();
    }

    @Override
    public WebElement getUpdateAnywayButton() {
        return visibilityOfElementLocated(getDriver(), UPDATE_ANYWAY_BUTTON_LOCATOR);
    }

    @Override
    public void hitUpdateAnywayButton() {
        retry(() -> {
            click(getUpdateAnywayButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
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
    public EditCodeListValueDialog editCodeListValue(String value) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(value);
            click(tr);
        });
        EditCodeListValueDialog editCodeListValueDialog = new EditCodeListValueDialogImpl(this);
        assert editCodeListValueDialog.isOpened();
        return editCodeListValueDialog;
    }

    @Override
    public WebElement getStateField() {
        return visibilityOfElementLocated(getDriver(), STATE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getGuidField() {
        return visibilityOfElementLocated(getDriver(), GUID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getListIDField() {
        return visibilityOfElementLocated(getDriver(), LIST_ID_FIELD_LOCATOR);
    }

    @Override
    public void hitCancelButton() {
        retry(() -> {
            click(getCancelButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_CANCEL_REVISION_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_REVISION_VALUE_BUTTON_LOCATOR);
    }

    @Override
    public void valueExists(String value) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(value);
        });
    }

    @Override
    public WebElement getOwnerField() {
        return visibilityOfElementLocated(getDriver(), OWNER_FIELD_LOCATOR);
    }

    @Override
    public void moveToDraft() {
        retry(() -> {
            click(getMoveToDraftButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getMoveToDraftButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_DRAFT_BUTTON_LOCATOR);
    }

    @Override
    public void backToWIP() {
        retry(() -> {
            click(getBackToWIPButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public void moveToCandidate() {
        retry(() -> {
            click(getMoveToCandidateButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_CHANGE_STATE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getBackToWIPButton() {
        return elementToBeClickable(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
    }
    @Override
    public WebElement getMoveToCandidateButton() {
        return elementToBeClickable(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitDeleteButton() {
        retry(() -> {
            click(getDeleteButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getDeleteButton() {
        return elementToBeClickable(getDriver(), DELETE_BUTTON_LOCATOR);
    }

    @Override
    public void hitRestoreButton() {
        retry(() -> {
            click(getRestoreButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_RESTORE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getRestoreButton() {
        return elementToBeClickable(getDriver(), RESTORE_BUTTON_LOCATOR);
    }
}
