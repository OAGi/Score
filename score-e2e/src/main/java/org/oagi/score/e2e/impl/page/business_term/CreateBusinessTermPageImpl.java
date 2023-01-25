package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.business_term.CreateBusinessTermPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateBusinessTermPageImpl extends BasePageImpl implements CreateBusinessTermPage {

    private static final By BUSINESS_TERM_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Business Term\")]//ancestor::mat-form-field//input[1]");

    private static final By EXTERNAL_REFERENCE_URI_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"External Reference URI\")]//ancestor::mat-form-field//input[1]");

    private static final By EXTERNAL_REFERENCE_ID_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"External Reference ID\")]//ancestor::mat-form-field//input[1]");

    private static final By COMMENT_FIELD_LOCATOR = By.xpath("//mat-label[contains(text(), \"Comment\")]//ancestor::mat-form-field//input[1]");

    private static final By CREATE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final ViewEditBusinessTermPageImpl parent;
    public CreateBusinessTermPageImpl(ViewEditBusinessTermPageImpl parent){ super(parent); this.parent = parent;}

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

    @Override
    public WebElement getBusinessTermField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessTerm(String businessTerm) {
        sendKeys(getBusinessTermField(), businessTerm);
    }

    @Override
    public WebElement getExternalReferenceURIField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_URI_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceURI(String externalReferenceURI){sendKeys(getExternalReferenceURIField(), externalReferenceURI);}

    @Override
    public WebElement getExternalReferenceIDField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_ID_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceID(String externalReferenceID){sendKeys(getExternalReferenceURIField(), externalReferenceID);}

    @Override
    public WebElement getCommentField() {
        return visibilityOfElementLocated(getDriver(), COMMENT_FIELD_LOCATOR);
    }

    @Override
    public void setComment(String comment) {
        sendKeys(getCommentField(), comment);
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public ViewEditBusinessTermPage createBusinessTerm(BusinessTermObject businessTerm) {
        setBusinessTerm(businessTerm.getBusinessTerm());
        setExternalReferenceURI(businessTerm.getExternalReferenceUri());
        setExternalReferenceID(businessTerm.getExternalReferenceId());
        setComment(businessTerm.getComment());
        click(getCreateButton());
        assert getSnackBar(getDriver(), "Created").isDisplayed();
        return this.parent;
    }


}
