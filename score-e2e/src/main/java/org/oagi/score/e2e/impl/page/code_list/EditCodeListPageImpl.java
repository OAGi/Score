package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.AddCodeListCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditCodeListPageImpl extends BasePageImpl implements EditCodeListPage {

    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By CODE_LIST_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By REVISION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Revision\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    private static final By ADD_CODE_LIST_VALUE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");
    private static final By ADD_COMMENT_ICON_LOCATOR =
            By.xpath("//span/mat-icon[contains(text(), \"comments\")]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-checkbox");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Namespace\")]//ancestor::div[1]/mat-select//span");
    private static final By AGENCY_ID_LIST_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text()= \"Agency ID List\"]//ancestor::div[1]/mat-select//span");
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
    public AddCodeListCommentDialog hitAddCommentButton() {
        click(getAddCommentButton());
        AddCodeListCommentDialog addCodeListCommentDialog = new AddCodeListCommentDialogImpl(this);
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
}
