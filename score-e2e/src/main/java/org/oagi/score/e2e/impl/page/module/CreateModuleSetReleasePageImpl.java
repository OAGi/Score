package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CreateModuleSetReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateModuleSetReleasePageImpl extends BasePageImpl implements CreateModuleSetReleasePage {
    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-mdc-form-field//input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-mdc-form-field//textarea");
    private static final By RELEASE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text() = \"Release\"]//ancestor::mat-mdc-form-field[1]//mat-select");
    private static final By MODULE_SET_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text() = \"Module Set\"]//ancestor::mat-mdc-form-field[1]//mat-select");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    public CreateModuleSetReleasePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set_release/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Module Set Release".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public void setName(String moduleSetReleaseName) {
        sendKeys(getNameField(), moduleSetReleaseName);
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }
    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setModuleSet(String name) {
        retry(() -> {
            click(getModuleSetSelectField());
            WebElement optionField = elementToBeClickable(getDriver(),
                    By.xpath("//span[contains(text(), \"" + name + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public WebElement getModuleSetSelectField() {
        return visibilityOfElementLocated(getDriver(), MODULE_SET_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setRelease(String releaseNumber) {
        retry(() -> {
            click(getReleaseSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), releaseNumber);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + releaseNumber + "\")]"));
            click(searchedSelectField);
            waitFor(ofMillis(500L));
            escape(getDriver());
        });
    }
    @Override
    public WebElement getReleaseSelectField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void hitCreateButton() {
        click(getCreateButton());
        waitFor(ofMillis(500L));

        // Creating the module set release would take a few minutes.
        invisibilityOfLoadingContainerElement(PageHelper.wait(getDriver(), Duration.ofSeconds(180L), ofMillis(500L)));
        assert "Created".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void toggleDefault() {
        click(getDefaultSelectField().findElement(By.tagName("label")));
    }

    @Override
    public WebElement getDefaultSelectField() {
        return getCheckboxByName("Default");
    }
    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }
}
