package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CreateModuleSetPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateModuleSetPageImpl extends BasePageImpl implements CreateModuleSetPage {
    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-mdc-form-field//input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-mdc-form-field//textarea");
   private static final By RELEASE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text()= \"Release\"]//ancestor::mat-mdc-form-field[1]//mat-select/div/div[1]");
    private static final By MODULE_SET_RELEASE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[text()= \"Copy CC assignment from Module Set Release\"]//ancestor::mat-mdc-form-field[1]//mat-select/div/div[1]");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    public CreateModuleSetPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Module Set".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }
    @Override
    public void toggleCreateModuleSetRelease() {
        click(getCreateModuleSetReleaseSelectField().findElement(By.tagName("label")));
    }

    @Override
    public WebElement getCreateModuleSetReleaseSelectField() {
        return getCheckboxByName("Create Module Set Release");
    }
    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }

    @Override
    public void setRelease(String releaseNumber) {
        retry(() -> {
            click(getReleaseSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + releaseNumber + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }
    @Override
    public WebElement getReleaseSelectField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setModuleSetRelease(String moduleSetRelease) {
        retry(() -> {
            click(getModuleSetReleaseSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + moduleSetRelease + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }
    @Override
    public WebElement getModuleSetReleaseSelectField() {
        return visibilityOfElementLocated(getDriver(), MODULE_SET_RELEASE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void hitCreateButton() {
        click(getCreateButton());
        waitFor(ofMillis(1000L));

        // Creating module set along with the module set release would take a few minutes.
        invisibilityOfLoadingContainerElement(PageHelper.wait(getDriver(), Duration.ofSeconds(180L), ofMillis(500L)));
        assert "Created".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }
}
