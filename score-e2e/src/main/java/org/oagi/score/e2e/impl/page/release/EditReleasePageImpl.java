package org.oagi.score.e2e.impl.page.release;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditReleasePageImpl extends BasePageImpl implements EditReleasePage {
    private static final By RELEASE_NUMBER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release Number\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_NAMESPACE_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Release Namespace\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By RELEASE_NOTE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release Note\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_LICENSE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release License\")]//ancestor::mat-form-field//input");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By CREATE_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create Draft\")]//ancestor::button[1]");

    private final ReleaseObject release;

    public EditReleasePageImpl(BasePage parent, ReleaseObject release) {
        super(parent);
        this.release = release;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/release/" + this.release.getReleaseId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert getText(getTitle()).equals("Releases Detail");
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-card-title/span[1]"));
    }

    @Override
    public WebElement getReleaseNumberField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_NUMBER_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseNum(String releaseNum) {
        sendKeys(getReleaseNumberField(), releaseNum);
    }

    @Override
    public WebElement getReleaseNamespaceField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseNamespace(NamespaceObject releaseNamespace) {
        retry(() -> {
            click(getReleaseNamespaceField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + releaseNamespace.getUri() + "\")]//ancestor::mat-option[1]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getReleaseNoteField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_NOTE_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseNote(String releaseNote) {
        sendKeys(getReleaseNoteField(), releaseNote);
    }

    @Override
    public WebElement getReleaseLicenseField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_LICENSE_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseLicense(String releaseLicense) {
        sendKeys(getReleaseLicenseField(), releaseLicense);
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
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
    public WebElement getCreateDraftButton() {
        return elementToBeClickable(getDriver(), CREATE_DRAFT_BUTTON_LOCATOR);
    }

    @Override
    public void hitCreateDraftButton() {
        retry(() -> {
            click(getCreateDraftButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
}
