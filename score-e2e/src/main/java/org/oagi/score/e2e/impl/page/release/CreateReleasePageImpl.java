package org.oagi.score.e2e.impl.page.release;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateReleasePageImpl extends BasePageImpl implements CreateReleasePage {
    private static final By RELEASE_NUMBER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release Number\")]//ancestor::mat-form-field//input");
    private static final By RELEASE_NAMESPACE_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Release Namespace\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By RELEASE_NOTE_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Release Note\")]//ancestor::div[1]/textarea");
    private static final By RELEASE_LICENSE_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Release License\")]//ancestor::div[1]/textarea");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final ViewEditReleasePageImpl parent;

    public CreateReleasePageImpl(ViewEditReleasePageImpl parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/release/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Releases Detail".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getReleaseNumberField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_NUMBER_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseNumber(String releaseNumber) {
        sendKeys(getReleaseNumberField(), releaseNumber);
    }

    @Override
    public WebElement getReleaseNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public void setReleaseNamespace(NamespaceObject releaseNamespace) {
        retry(() -> {
            click(getReleaseNamespaceSelectField());
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
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitCreateButton() {
        retry(() -> {
            click(getCreateButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
}
