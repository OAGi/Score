package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPackagePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBIEPackagePageImpl extends BasePageImpl implements EditBIEPackagePage {

    private static final By TITLE_LOCATOR =
            By.className("title");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Update']]");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Move to QA']]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Move to Production']]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Back to WIP']]");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Revise']]");
    private static final By GENERATE_BUTTON_LOCATOR =
            By.xpath("//button[.//span[normalize-space(.) = 'Generate']]");

    private BigInteger biePackageId;

    public EditBIEPackagePageImpl(BasePage parent) {
        super(parent);
    }

    public EditBIEPackagePageImpl(BasePage parent, BigInteger biePackageId) {
        super(parent);
        this.biePackageId = biePackageId;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/bie_package").toString();
    }

    @Override
    public void openPage() {
        if (biePackageId == null) {
            throw new IllegalStateException("BIE Package id is not set; use openPage(biePackageId).");
        }
        openPage(biePackageId);
    }

    @Override
    public void openPage(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
        String url = getConfig().getBaseUrl().resolve("/bie_package/" + biePackageId).toString();
        getDriver().get(url);
        assert "Edit BIE Package".equals(getText(getTitle()));
    }

    @Override
    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }

    private WebElement getFieldByLabel(String label, String inputTag) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-form-field[.//mat-label[normalize-space(.) = " + xpathLiteral(label) + "]]//" + inputTag));
    }

    @Override
    public WebElement getNameField() {
        return getFieldByLabel("Package Name", "input");
    }

    @Override
    public void setName(String name) {
        WebElement field = getNameField();
        clear(field);
        sendKeys(field, name);
    }

    @Override
    public String getName() {
        return getNameField().getAttribute("value");
    }

    @Override
    public void setVersionId(String versionId) {
        WebElement field = getFieldByLabel("Version ID", "input");
        clear(field);
        sendKeys(field, versionId);
    }

    @Override
    public void setVersionName(String versionName) {
        WebElement field = getFieldByLabel("Version Name", "input");
        clear(field);
        sendKeys(field, versionName);
    }

    @Override
    public void setDescription(String description) {
        WebElement field = getFieldByLabel("Description", "textarea");
        clear(field);
        sendKeys(field, description);
    }

    @Override
    public boolean isRevisionReasonFieldPresent() {
        return isElementPresent(getDriver(), By.xpath(
                "//mat-form-field[.//mat-label[normalize-space(.) = 'Revision Reason']]//textarea"));
    }

    @Override
    public WebElement getRevisionReasonField() {
        return getFieldByLabel("Revision Reason", "textarea");
    }

    @Override
    public void setRevisionReason(String revisionReason) {
        WebElement field = getRevisionReasonField();
        clear(field);
        if (revisionReason != null && !revisionReason.isEmpty()) {
            sendKeys(field, revisionReason);
        }
    }

    @Override
    public String getRevisionReason() {
        return getRevisionReasonField().getAttribute("value");
    }

    @Override
    public boolean isRevisionReasonFieldEnabled() {
        return getRevisionReasonField().isEnabled();
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> click(getDriver(), elementToBeClickable(getDriver(),UPDATE_BUTTON_LOCATOR)));
        assert "Updated".equals(getSnackBarMessage(getDriver()));
        waitForSnackBarToDisappear(getDriver());
    }

    private void transitionState(By buttonLocator) {
        retry(() -> click(getDriver(), elementToBeClickable(getDriver(),buttonLocator)));
        click(getDriver(), getDialogButtonByName(getDriver(), "Update"));
        assert "State updated".equals(getSnackBarMessage(getDriver()));
        waitForSnackBarToDisappear(getDriver());
    }

    @Override
    public void moveToQA() {
        transitionState(MOVE_TO_QA_BUTTON_LOCATOR);
    }

    @Override
    public void moveToProduction() {
        transitionState(MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
    }

    @Override
    public void backToWIP() {
        transitionState(BACK_TO_WIP_BUTTON_LOCATOR);
    }

    @Override
    public boolean isReviseButtonPresent() {
        return isElementPresent(getDriver(), REVISE_BUTTON_LOCATOR);
    }

    @Override
    public EditBIEPackagePage revise() {
        retry(() -> click(getDriver(), elementToBeClickable(getDriver(),REVISE_BUTTON_LOCATOR)));
        click(getDriver(), getDialogButtonByName(getDriver(), "Revise"));
        // The app navigates to /bie_package/{newRevisionId}; wait for the URL to change.
        String previousUrl = getConfig().getBaseUrl().resolve("/bie_package/" + biePackageId).toString();
        Wait<WebDriver> wait = longWait(getDriver());
        wait.until(driver -> !driver.getCurrentUrl().equals(previousUrl)
                && driver.getCurrentUrl().matches(".*/bie_package/\\d+$"));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger revisedId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
        EditBIEPackagePageImpl revised = new EditBIEPackagePageImpl(this, revisedId);
        assert revised.isOpened();
        return revised;
    }

    @Override
    public void selectExpression(String expression) {
        String label = "JSON".equalsIgnoreCase(expression) ? "JSON Schema" : "XML Schema";
        WebElement radio = elementToBeClickable(getDriver(), By.xpath(
                "//mat-radio-button[.//*[normalize-space(.) = " + xpathLiteral(label) + "]]"));
        retry(() -> click(getDriver(), radio));
    }

    @Override
    public File clickGenerateAndDownloadZip() {
        long startedAt = System.currentTimeMillis();
        retry(() -> click(getDriver(), elementToBeClickable(getDriver(),GENERATE_BUTTON_LOCATOR)));
        try {
            return waitForDownloadedZip(ofMillis(60000L), startedAt);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isReadableBiePackageZip(File file) {
        // Skip browser partial-download placeholders; otherwise rely on the ZIP being readable and
        // containing manifest.json rather than on the file extension.
        if (file.getName().endsWith(".crdownload") || file.getName().endsWith(".part")) {
            return false;
        }
        try (ZipFile zipFile = new ZipFile(file)) {
            return zipFile.stream().anyMatch(entry -> entry.getName().endsWith("manifest.json"));
        } catch (Exception e) {
            return false;
        }
    }

    private File waitForDownloadedZip(Duration duration, long startedAt)
            throws IOException, InterruptedException {
        String userHome = System.getProperty("user.home");
        Path path = Paths.get(new File(userHome, "Downloads").toURI());
        try (var files = Files.list(path)) {
            Optional<Path> matchedFile = files
                    .filter(child -> child.toFile().lastModified() >= startedAt)
                    .filter(child -> child.toFile().exists() && child.toFile().length() > 0L)
                    .filter(child -> isReadableBiePackageZip(child.toFile()))
                    .max((left, right) -> Long.compare(left.toFile().lastModified(), right.toFile().lastModified()));
            if (matchedFile.isPresent()) {
                return matchedFile.get().toFile();
            }
        }

        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

        long timeout = duration.toMillis();
        WatchKey key;
        do {
            key = watchService.poll(1000L, TimeUnit.MILLISECONDS);
            if (key != null && key.isValid()) {
                File downloadedFile = null;
                for (WatchEvent<?> event : key.pollEvents()) {
                    File candidate = new File(path.toFile(), event.context().toString());
                    if (!candidate.exists() || candidate.length() == 0L) {
                        continue;
                    }
                    if (candidate.lastModified() < startedAt) {
                        continue;
                    }
                    downloadedFile = candidate;
                }
                key.reset();
                if (downloadedFile != null && isReadableBiePackageZip(downloadedFile)) {
                    return downloadedFile;
                }
            }
            timeout -= 1000L;
        } while (timeout > 0L);

        throw new FileNotFoundException();
    }
}
