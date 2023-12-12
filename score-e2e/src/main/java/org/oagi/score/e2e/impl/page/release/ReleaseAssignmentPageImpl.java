package org.oagi.score.e2e.impl.page.release;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ReleaseAssignmentPageImpl extends BasePageImpl implements ReleaseAssignmentPage {
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-mdc-form-field//input");
    private static final By TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Type\")]//ancestor::mat-mdc-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Owner\")]//ancestor::mat-mdc-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");
    private static final By ASSIGN_ALL_BUTTON_LOCATOR =
            By.xpath("//fa-icon[@mattooltip=\"Assign All\"]//ancestor::span");
    private static final By VALIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(),\"Validate\")]//ancestor::button[1]");
    public static final By CONTINUE_CREATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button/span");

    private final ReleaseObject releaseObject;

    public ReleaseAssignmentPageImpl(BasePage parent, ReleaseObject releaseObject) {
        super(parent);
        this.releaseObject = releaseObject;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/release/" + this.releaseObject.getReleaseId() + "/assign").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert getText(getTitle()).endsWith("Release Assignment");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getTypeSelectField() {
        return visibilityOfElementLocated(getDriver(), TYPE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setType(String type) {
        retry(() -> {
            click(getTypeSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + type + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });

    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        retry(() -> {
            click(getTypeSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + owner + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });

    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());

    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitCreateButton() {
        retry(() -> {
            click(getCreateButton());
            waitFor(ofMillis(1000L));
        });

        click(elementToBeClickable(getDriver(), CONTINUE_CREATE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getAssignAllButton() {
        return elementToBeClickable(getDriver(), ASSIGN_ALL_BUTTON_LOCATOR);
    }

    @Override
    public void hitAssignAllButton() {
        retry(() -> {
            click(getAssignAllButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getValidateButton() {
        return elementToBeClickable(getDriver(), VALIDATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitValidateButton() {
        retry(() -> {
            click(getValidateButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
}
