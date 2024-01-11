package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.business_term.EditBusinessTermPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBusinessTermPageImpl extends BasePageImpl implements EditBusinessTermPage {

    private static final By BUSINESS_TERM_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Business Term\")]//ancestor::mat-form-field//input[1]");

    private static final By EXTERNAL_REFERENCE_URI_FIELD_LOCATOR = By.xpath("//span[contains(text(), \"External Reference URI\")]//ancestor::mat-form-field//input[1]");

    private static final By EXTERNAL_REFERENCE_ID_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"External Reference ID\")]//ancestor::mat-form-field//input[1]");

    private static final By DEFINITION_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea[1]");

    private static final By COMMENT_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Comment\")]//ancestor::mat-form-field//textarea[1]");

    private static final By UPDATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button/span");

    private final ViewEditBusinessTermPageImpl parent;

    private final BusinessTermObject businessTerm;

    public EditBusinessTermPageImpl(ViewEditBusinessTermPageImpl parent,
                                    BusinessTermObject businessTerm) {
        super(parent);
        this.parent = parent;
        this.businessTerm = businessTerm;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/business_term_management/business_term/" + this.businessTerm.getBusinessTermId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Business Term".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getBusinessTermField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessTerm(String businessTerm) {
        sendKeys(getBusinessTermField(), businessTerm);
    }

    @Override
    public String getBusinessTermFieldText() {
        return getText(getBusinessTermField());
    }

    @Override
    public WebElement getExternalReferenceURIField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_URI_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceURI(String externalReferenceURI) {
        sendKeys(getExternalReferenceURIField(), externalReferenceURI);
    }

    @Override
    public String getExternalReferenceURIFieldText() {
        return getText(getExternalReferenceURIField());
    }

    @Override
    public WebElement getExternalReferenceIDField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_ID_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceID(String externalReferenceID) {
        sendKeys(getExternalReferenceIDField(), externalReferenceID);
    }

    @Override
    public String getExternalReferenceIDFieldText() {
        return getText(getExternalReferenceIDField());
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public String getDefinitionFieldText() {
        return getText(getDefinitionField());
    }

    @Override
    public WebElement getCommentField() {
        return visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR);
    }

    @Override
    public void setComment(String comment) {
        sendKeys(getCommentField(), comment);
    }

    @Override
    public String getCommentFieldText() {
        return getText(getCommentField());
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public void updateBusinessTerm(BusinessTermObject businessTerm) {
        setBusinessTerm(businessTerm.getBusinessTerm());
        setExternalReferenceURI(businessTerm.getExternalReferenceUri());
        setExternalReferenceID(businessTerm.getExternalReferenceId());
        setComment(businessTerm.getComment());
        click(getUpdateButton());
        assert getSnackBar(getDriver(), "Updated").isEnabled();
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public ViewEditBusinessTermPage discard() {
        click(getDiscardButton());
        WebElement confirmDiscardButton = elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"
        ));
        click(confirmDiscardButton);
        return this.parent;
    }

    @Override
    public ViewEditBusinessTermPage discardBusinessTerm() {
        click(getDiscardButton());
        click(elementToBeClickable(getDriver(), DISCARD_BUTTON_IN_DIALOG_LOCATOR));
        assert getSnackBar(getDriver(), "Discarded").isDisplayed();
        return this.parent;
    }
}
