package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.oas.CreateOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateOpenAPIDocumentPageImpl extends BasePageImpl implements CreateOpenAPIDocumentPage {

    private static final By OPENAPI_VERSION_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"OpenAPI Version\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By TITLE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Title\")]//ancestor::mat-form-field//input");

    private static final By DOCUMENT_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Document Version\")]//ancestor::mat-form-field//input");

    private static final By TERMS_OF_SERVICE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Terms of Service\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact Name\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_URL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact URL\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_EMAIL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact Email\")]//ancestor::mat-form-field//input");

    private static final By LICENSE_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"License Name\")]//ancestor::mat-form-field//input");

    private static final By LICENSE_URL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"License URL\")]//ancestor::mat-form-field//input");

    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea");

    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private BasePage parent;

    public CreateOpenAPIDocumentPageImpl(BasePage parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/express/oas_doc/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create OpenAPI Document".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }


    @Override
    public WebElement getOpenAPIVersionSelectField() {
        return visibilityOfElementLocated(getDriver(), OPENAPI_VERSION_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOpenAPIVersion(String openAPIVersion) {
        click(getOpenAPIVersionSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + openAPIVersion + "\")]"));
        click(optionField);
        escape(getDriver());
    }

    @Override
    public WebElement getTitleField() {
        return visibilityOfElementLocated(getDriver(), TITLE_FIELD_LOCATOR);
    }

    @Override
    public void setTitle(String title) {
        sendKeys(getTitleField(), title);
    }

    @Override
    public WebElement getDocumentVersionField() {
        return visibilityOfElementLocated(getDriver(), DOCUMENT_VERSION_FIELD_LOCATOR);
    }

    @Override
    public void setDocumentVersion(String documentVersion) {
        sendKeys(getDocumentVersionField(), documentVersion);
    }

    @Override
    public WebElement getTermsOfServiceField() {
        return visibilityOfElementLocated(getDriver(), TERMS_OF_SERVICE_FIELD_LOCATOR);
    }

    @Override
    public void setTermsOfService(String termsOfService) {
        sendKeys(getTermsOfServiceField(), termsOfService);
    }

    @Override
    public WebElement getContactNameField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setContactName(String contactName) {
        sendKeys(getContactNameField(), contactName);
    }

    @Override
    public WebElement getContactURLField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_URL_FIELD_LOCATOR);
    }

    @Override
    public void setContactURL(String contactURL) {
        sendKeys(getContactURLField(), contactURL);
    }

    @Override
    public WebElement getContactEmailField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_EMAIL_FIELD_LOCATOR);
    }

    @Override
    public void setContactEmail(String contactEmail) {
        sendKeys(getContactEmailField(), contactEmail);
    }

    @Override
    public WebElement getLicenseNameField() {
        return visibilityOfElementLocated(getDriver(), LICENSE_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setLicenseName(String licenseName) {
        sendKeys(getLicenseNameField(), licenseName);
    }

    @Override
    public WebElement getLicenseURLField() {
        return visibilityOfElementLocated(getDriver(), LICENSE_URL_FIELD_LOCATOR);
    }

    @Override
    public void setLicenseURL(String licenseURL) {
        sendKeys(getLicenseURLField(), licenseURL);
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }

    @Override
    public WebElement getCreateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), CREATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public OpenAPIDocumentPage create() {
        click(getCreateButton(true));
        waitFor(Duration.ofMillis(500));
        assert "Created".equals(getSnackBarMessage(getDriver()));

        OpenAPIDocumentPage openAPIDocumentPage;
        if (parent instanceof OpenAPIDocumentPage) {
            openAPIDocumentPage = (OpenAPIDocumentPage) parent;
        } else {
            openAPIDocumentPage = new OpenAPIDocumentPageImpl(this);
        }

        assert openAPIDocumentPage.isOpened();
        return openAPIDocumentPage;
    }
}
