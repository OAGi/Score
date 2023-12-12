package org.oagi.score.e2e.impl.page.namespace;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.namespace.EditNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditNamespacePageImpl extends BasePageImpl implements EditNamespacePage {

    private static final By URI_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"URI\")]//ancestor::div[1]/input");
    private static final By PREFIX_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Prefix\")]//ancestor::div[1]/input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::div[1]/textarea");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By STANDARD_CHECKBOX_FIELD_LOCATOR =
            By.xpath("//span[contains(text(),\"Standard\")]//ancestor::mat-checkbox[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By BACK_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back\")]//ancestor::button[1]");

    private final ViewEditNamespacePageImpl parent;

    private final NamespaceObject namespace;

    public EditNamespacePageImpl(ViewEditNamespacePageImpl parent, NamespaceObject namespace) {
        super(parent);
        this.parent = parent;
        this.namespace = namespace;
    }


    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/namespace/" + this.namespace.getNamespaceId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Namespace Detail".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public WebElement getURIField() {
        return visibilityOfElementLocated(getDriver(), URI_FIELD_LOCATOR);
    }

    @Override
    public void setURI(String uri) {
        sendKeys(getURIField(), uri);
    }

    @Override
    public WebElement getPrefixField() {
        return visibilityOfElementLocated(getDriver(), PREFIX_FIELD_LOCATOR);
    }

    @Override
    public void setPrefix(String prefix) {
        sendKeys(getPrefixField(), prefix);
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
    public WebElement getStandardCheckboxField() {
        return visibilityOfElementLocated(getDriver(), STANDARD_CHECKBOX_FIELD_LOCATOR);
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
        return visibilityOfElementLocated(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitDiscardButton() {
        retry(() -> {
            click(getDiscardButton());
            waitFor(ofMillis(1000L));
        });
        WebElement confirmDiscardButton = elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"
        ));
        click(confirmDiscardButton);
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getDiscardButton() {
        return visibilityOfElementLocated(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public void hitBackButton() {
        retry(() -> {
            click(getBackButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getBackButton() {
        return visibilityOfElementLocated(getDriver(), BACK_BUTTON_LOCATOR);
    }
}
