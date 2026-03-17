package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.module.CoreComponentAssignmentPage;
import org.oagi.score.e2e.page.module.EditModuleSetReleasePage;
import org.oagi.score.e2e.page.module.ModuleSetReleaseXMLSchemaValidationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditModuleSetReleasePageImpl extends BasePageImpl implements EditModuleSetReleasePage {

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea");
    private static final By MODULE_SET_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Module Set\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By RELEASE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Release\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By DEFAULT_CHECKBOX_LOCATOR =
            By.xpath("//label[contains(text(), \"Default\")]//ancestor::mat-checkbox");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By EXPORT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Export\")]//ancestor::button[1]");
    private static final By VALIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Validate\")]//ancestor::button[1]");
    private static final By XML_SCHEMA_MENU_ITEM_LOCATOR =
            By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]//button//span[contains(text(), \"XML Schema\")]//ancestor::button[1]");
    private static final By ASSIGN_CC_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Assign CCs\")]//ancestor::button[1]");
    private static final By VIEW_ASSIGNED_CC_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"View Assigned CCs\")]//ancestor::button[1]");

    private ModuleSetReleaseObject moduleSetRelease;

    public EditModuleSetReleasePageImpl(BasePage parent, ModuleSetReleaseObject moduleSetRelease) {
        super(parent);
        this.moduleSetRelease = moduleSetRelease;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/module_management/module_set_release/" + this.moduleSetRelease.getModuleSetReleaseId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Module Set Release".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
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
    public WebElement getModuleSetSelectField() {
        return visibilityOfElementLocated(getDriver(), MODULE_SET_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getReleaseSelectField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDefaultCheckbox() {
        return visibilityOfElementLocated(getDriver(), DEFAULT_CHECKBOX_LOCATOR);
    }

    @Override
    public void toggleDefault() {
        click(getDefaultCheckbox().findElement(By.tagName("label")));
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> {
            click(getUpdateButton(true));
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getUpdateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), UPDATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public File hitExportButton() {
        long startedAt = System.currentTimeMillis();
        retry(() -> {
            click(getDriver(), getExportButton());
            waitFor(ofMillis(500L));
            click(getDriver(), elementToBeClickable(getDriver(), XML_SCHEMA_MENU_ITEM_LOCATOR));
            waitFor(ofMillis(500L));
        });
        try {
            return waitForDownloadFile(Duration.ofMillis(60000), startedAt);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public WebElement getExportButton() {
        return elementToBeClickable(getDriver(), EXPORT_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getValidateButton() {
        return elementToBeClickable(getDriver(), VALIDATE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getAssignCCsbutton() {
        return elementToBeClickable(getDriver(), ASSIGN_CC_BUTTON_LOCATOR);
    }

    @Override
    public ModuleSetReleaseXMLSchemaValidationDialog hitValidateButton() {
        retry(() -> {
            click(getDriver(), getValidateButton());
            waitFor(ofMillis(500L));
            click(getDriver(), elementToBeClickable(getDriver(), XML_SCHEMA_MENU_ITEM_LOCATOR));
            waitFor(ofMillis(500L));
        });
        return new ModuleSetReleaseXMLSchemaValidationDialogImpl(this);
    }


    @Override
    public CoreComponentAssignmentPage hitAssignCCsButton(ModuleSetReleaseObject moduleSetRelease) {
        click(getAssignCCsbutton());
        CoreComponentAssignmentPage coreComponentAssignmentPage = new CoreComponentAssignmentPageImpl(this, moduleSetRelease);
        assert coreComponentAssignmentPage.isOpened();
        return coreComponentAssignmentPage;
    }

    @Override
    public CoreComponentAssignmentPage viewAssignedCCs(ModuleSetReleaseObject latestModuleSetRelease) {
        click(getViewAssignedCCsbutton());
        CoreComponentAssignmentPage coreComponentAssignmentPage = new CoreComponentAssignmentPageImpl(this, moduleSetRelease);
        assert coreComponentAssignmentPage.isOpened();
        return coreComponentAssignmentPage;
    }

    @Override
    public WebElement getViewAssignedCCsbutton() {
        return elementToBeClickable(getDriver(), VIEW_ASSIGNED_CC_BUTTON_LOCATOR);
    }

    private File waitForDownloadFile(Duration duration, long startedAt) throws IOException, InterruptedException {
        String userHome = System.getProperty("user.home");
        Path path = Paths.get(new File(userHome, "Downloads").toURI());
        Optional<Path> existingZip = Files.list(path)
                .filter(child -> child.toFile().getName().endsWith(".zip"))
                .filter(child -> child.toFile().lastModified() >= startedAt)
                .max((left, right) -> Long.compare(left.toFile().lastModified(), right.toFile().lastModified()));
        if (existingZip.isPresent() && existingZip.get().toFile().length() > 0L) {
            return existingZip.get().toFile();
        }

        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        long timeout = duration.toMillis();
        WatchKey key;
        do {
            key = watchService.poll(1000L, TimeUnit.MILLISECONDS);
            if (key != null && key.isValid()) {
                File downloadedFile = null;
                for (WatchEvent<?> event : key.pollEvents()) {
                    File candidate = new File(path.toFile(), event.context().toString());
                    if (!candidate.getName().endsWith(".zip")) {
                        continue;
                    }
                    if (!candidate.exists() || candidate.length() == 0L) {
                        continue;
                    }
                    if (candidate.lastModified() < startedAt) {
                        continue;
                    }
                    downloadedFile = candidate;
                }
                key.reset();
                if (downloadedFile != null) {
                    return downloadedFile;
                }
            }
            timeout -= 1000L;
        } while (timeout > 0L);

        throw new FileNotFoundException();
    }
}
