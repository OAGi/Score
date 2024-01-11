package org.oagi.score.e2e.impl.page.namespace;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.namespace.CreateNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateNamespacePageImpl extends BasePageImpl implements CreateNamespacePage {
    private static final By URI_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"URI\")]//ancestor::div[1]/input");
    private static final By PREFIX_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Prefix\")]//ancestor::div[1]/input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::div[1]/textarea");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");
    private static final By STANDARD_CHECKBOX_FIELD_LOCATOR =
            By.xpath("//span[contains(text(),\"Standard\")]//ancestor::mat-checkbox[1]");
    private static final By BACK_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back\")]//ancestor::button[1]");

    private ViewEditNamespacePageImpl parent;

    public CreateNamespacePageImpl(ViewEditNamespacePageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/namespace/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Namespace".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
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
    public void hitCreateButton() {
        retry(() -> {
            click(getCreateButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());

    }

    @Override
    public WebElement getCreateButton() {
        return visibilityOfElementLocated(getDriver(), CREATE_BUTTON_LOCATOR);
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
