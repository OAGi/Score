package org.oagi.score.e2e.impl.page.oas;

import org.apache.commons.io.FileUtils;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.AddOperationForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OasSecurityRequirementDialog;
import org.oagi.score.e2e.page.oas.OasSecuritySchemeDialog;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditOpenAPIDocumentPageImpl extends BasePageImpl implements EditOpenAPIDocumentPage {

    private static final By OPENAPI_VERSION_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"OpenAPI Version\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By TITLE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Title\")]//ancestor::mat-form-field//input");

    private static final By DOCUMENT_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Document Version\")]//ancestor::mat-form-field//input");

    private static final By TERMS_OF_SERVICE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Terms of Service\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact Name\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_URL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact URL\")]//ancestor::mat-form-field//input");

    private static final By CONTACT_EMAIL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Contact Email\")]//ancestor::mat-form-field//input");

    private static final By LICENSE_NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"License Name\")]//ancestor::mat-form-field//input");

    private static final By LICENSE_URL_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"License URL\")]//ancestor::mat-form-field//input");

    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Description\")]//ancestor::mat-form-field//textarea");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By ADD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");

    private static final By ADD_BIE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add BIE\")]//ancestor::button[1]");

    private static final By ADD_OPERATION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Add Operation\")]//ancestor::button[1]");

    private static final By CONFIRM_DIALOG_LOCATOR =
            By.cssSelector("score-confirm-dialog");

    private static final By GENERATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Generate\")]//ancestor::button[1]");

    private static final By SECURITY_SCHEMES_SECTION_LOCATOR =
            By.xpath("//span[normalize-space(.) = \"Security Schemes\"]");

    private static final By ADD_SECURITY_SCHEME_BUTTON_LOCATOR =
            By.xpath("//button[contains(., \"Add Security Scheme\")]");

    private static final By NO_SCHEME_HINT_LOCATOR =
            By.xpath("//span[contains(normalize-space(.), \"No scheme configured\")]");

    private static final By DOCUMENT_SECURITY_BUTTON_LOCATOR =
            By.cssSelector("button.oas-doc-security-btn");

    private static final By DOCUMENT_SECURITY_LABEL_LOCATOR =
            By.cssSelector("span.oas-doc-security-label");

    private static final By SCHEME_CARD_LOCATOR =
            By.cssSelector("mat-card.oas-scheme-card");

    private BasePage parent;

    private OpenAPIDocumentObject openAPIDocument;

    public EditOpenAPIDocumentPageImpl(BasePage parent, OpenAPIDocumentObject openAPIDocument) {
        super(parent);
        this.parent = parent;
        this.openAPIDocument = openAPIDocument;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/express/oas_doc/" + openAPIDocument.getOasDocId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit OpenAPI Document".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getOpenAPIVersionSelectField() {
        return visibilityOfElementLocated(getDriver(), OPENAPI_VERSION_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOpenAPIVersion(String openAPIVersion) {
        click(getOpenAPIVersionSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), openAPIVersion);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[text() = \"" + openAPIVersion + "\"]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getTitleField() {
        return visibilityOfElementLocated(getDriver(), TITLE_FIELD_LOCATOR);
    }

    @Override
    public void setTitle(String title) {
        sendKeys(getTitleField(), title);
    }

    @Override
    public WebElement getDocumentVersionField() {
        return visibilityOfElementLocated(getDriver(), DOCUMENT_VERSION_FIELD_LOCATOR);
    }

    @Override
    public void setDocumentVersion(String documentVersion) {
        sendKeys(getDocumentVersionField(), documentVersion);
    }

    @Override
    public WebElement getTermsOfServiceField() {
        return visibilityOfElementLocated(getDriver(), TERMS_OF_SERVICE_FIELD_LOCATOR);
    }

    @Override
    public void setTermsOfService(String termsOfService) {
        sendKeys(getTermsOfServiceField(), termsOfService);
    }

    @Override
    public WebElement getContactNameField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setContactName(String contactName) {
        sendKeys(getContactNameField(), contactName);
    }

    @Override
    public WebElement getContactURLField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_URL_FIELD_LOCATOR);
    }

    @Override
    public void setContactURL(String contactURL) {
        sendKeys(getContactURLField(), contactURL);
    }

    @Override
    public WebElement getContactEmailField() {
        return visibilityOfElementLocated(getDriver(), CONTACT_EMAIL_FIELD_LOCATOR);
    }

    @Override
    public void setContactEmail(String contactEmail) {
        sendKeys(getContactEmailField(), contactEmail);
    }

    @Override
    public WebElement getLicenseNameField() {
        return visibilityOfElementLocated(getDriver(), LICENSE_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setLicenseName(String licenseName) {
        sendKeys(getLicenseNameField(), licenseName);
    }

    @Override
    public WebElement getLicenseURLField() {
        return visibilityOfElementLocated(getDriver(), LICENSE_URL_FIELD_LOCATOR);
    }

    @Override
    public void setLicenseURL(String licenseURL) {
        sendKeys(getLicenseURLField(), licenseURL);
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
    public WebElement getUpdateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), UPDATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void hitUpdateButton() {
        // Driver-aware click: clear any lingering snackbar, scroll into view, JS-click on intercept.
        click(getDriver(), getUpdateButton(true));
        waitFor(Duration.ofMillis(500));
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public OpenAPIDocumentPage hitDiscardButton() {
        retry(() -> {
            click(getDiscardButton());
            waitFor(ofMillis(1000L));
        });
        WebElement confirmDiscardButton = elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]"
        ));
        click(confirmDiscardButton);
        invisibilityOfLoadingContainerElement(getDriver());

        OpenAPIDocumentPage openAPIDocumentPage;
        if (parent instanceof OpenAPIDocumentPage) {
            openAPIDocumentPage = (OpenAPIDocumentPage) parent;
        } else {
            openAPIDocumentPage = new OpenAPIDocumentPageImpl(this);
        }

        assert openAPIDocumentPage.isOpened();
        return openAPIDocumentPage;
    }

    @Override
    public void clickDiscardButtonToOpenDialog() {
        retry(() -> {
            click(getDiscardButton());
            waitFor(ofMillis(1000L));
        });
        visibilityOfElementLocated(getDriver(), CONFIRM_DIALOG_LOCATOR);
    }

    @Override
    public WebElement getAddButton() {
        return elementToBeClickable(getDriver(), ADD_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getAddBIEButton() {
        return elementToBeClickable(getDriver(), ADD_BIE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getAddOperationButton() {
        return elementToBeClickable(getDriver(), ADD_OPERATION_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getGenerateButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), GENERATE_BUTTON_LOCATOR);
        }
        return visibilityOfElementLocated(getDriver(), GENERATE_BUTTON_LOCATOR);
    }

    @Override
    public File clickGenerateAndDownload() {
        long startedAt = System.currentTimeMillis();
        // Driver-aware click: clear any lingering snackbar, scroll into view, JS-click on intercept.
        click(getDriver(), getGenerateButton(true));
        // The generated file name embeds the document title, so filter on it to avoid picking up a
        // file produced by another OpenAPI Document generating concurrently.
        String filenameContains = (openAPIDocument == null) ? null : openAPIDocument.getTitle();
        try {
            return waitForDownloadedYaml(ofMillis(60000L), startedAt, filenameContains);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isParseableYaml(File file) {
        String name = file.getName();
        if (!name.endsWith(".yml") && !name.endsWith(".yaml")) {
            return false;
        }
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            Map<String, Object> loaded = new Yaml().load(content);
            return loaded != null;
        } catch (Exception e) {
            return false;
        }
    }

    private File waitForDownloadedYaml(Duration duration, long startedAt, String filenameContains)
            throws IOException, InterruptedException {
        String userHome = System.getProperty("user.home");
        Path path = Paths.get(new File(userHome, "Downloads").toURI());
        try (var files = Files.list(path)) {
            Optional<Path> matchedFile = files
                    .filter(child -> child.toFile().lastModified() >= startedAt)
                    .filter(child -> filenameContains == null || child.toFile().getName().contains(filenameContains))
                    .filter(child -> child.toFile().exists() && child.toFile().length() > 0L)
                    .filter(child -> isParseableYaml(child.toFile()))
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
                    if (filenameContains != null && !candidate.getName().contains(filenameContains)) {
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
                if (downloadedFile != null && isParseableYaml(downloadedFile)) {
                    return downloadedFile;
                }
            }
            timeout -= 1000L;
        } while (timeout > 0L);

        throw new FileNotFoundException();
    }

    @Override
    public AddBIEForOpenAPIDocumentDialog openAddBIEForOpenAPIDocumentDialog() {
        retry(() -> {
            // Use the driver-aware click: it waits out any lingering snackbar overlay, scrolls the
            // button to center, and falls back to a JS click on an intercept. The bare click(element)
            // form passes a null driver, so it skips all three and rethrows ElementClickInterceptedException
            // (the transient post-load overlay near the Add BIE button made this flake intermittently).
            click(getDriver(), getAddBIEButton());
            waitFor(ofMillis(1000L));
        });

        AddBIEForOpenAPIDocumentDialog addBIEForOpenAPIDocumentDialog = new AddBIEForOpenAPIDocumentDialogImpl(this);
        assert addBIEForOpenAPIDocumentDialog.isOpened();
        return addBIEForOpenAPIDocumentDialog;
    }

    @Override
    public AddOperationForOpenAPIDocumentDialog openAddOperationDialog() {
        retry(() -> {
            click(getAddOperationButton());
            waitFor(ofMillis(1000L));
        });

        AddOperationForOpenAPIDocumentDialog dialog = new AddOperationForOpenAPIDocumentDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    /* ----------------------------------------------------- Issue #1729: security */

    private By schemeCardByName(String schemeName) {
        return By.xpath("//mat-card[contains(@class, \"oas-scheme-card\")]"
                + "[.//div[contains(@class, \"oas-scheme-card-name\") and normalize-space(.) = "
                + xpathLiteral(schemeName) + "]]");
    }

    @Override
    public boolean isSecuritySchemesSectionDisplayed() {
        return isElementPresent(getDriver(), SECURITY_SCHEMES_SECTION_LOCATOR);
    }

    @Override
    public WebElement getAddSecuritySchemeButton() {
        return elementToBeClickable(getDriver(), ADD_SECURITY_SCHEME_BUTTON_LOCATOR);
    }

    @Override
    public boolean isNoSchemeHintDisplayed() {
        return isElementPresent(getDriver(), NO_SCHEME_HINT_LOCATOR);
    }

    @Override
    public String getNoSchemeHint() {
        if (!isNoSchemeHintDisplayed()) {
            return "";
        }
        String text = getText(visibilityOfElementLocated(getDriver(), NO_SCHEME_HINT_LOCATOR));
        return text == null ? "" : text;
    }

    @Override
    public OasSecuritySchemeDialog openAddSecuritySchemeDialog() {
        retry(() -> {
            click(getAddSecuritySchemeButton());
            waitFor(ofMillis(1000L));
        });
        OasSecuritySchemeDialog dialog = new OasSecuritySchemeDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    @Override
    public int getSecuritySchemeCardCount() {
        return getDriver().findElements(SCHEME_CARD_LOCATOR).size();
    }

    @Override
    public boolean hasSecuritySchemeCard(String schemeName) {
        return isElementPresent(getDriver(), schemeCardByName(schemeName));
    }

    @Override
    public String getSecuritySchemeCardType(String schemeName) {
        WebElement card = visibilityOfElementLocated(getDriver(), schemeCardByName(schemeName));
        return getText(card.findElement(By.cssSelector(".oas-scheme-card-type")));
    }

    @Override
    public String getSecuritySchemeCardSummary(String schemeName) {
        WebElement card = visibilityOfElementLocated(getDriver(), schemeCardByName(schemeName));
        return getText(card.findElement(By.cssSelector(".oas-scheme-card-summary")));
    }

    @Override
    public OasSecuritySchemeDialog clickSecuritySchemeCard(String schemeName) {
        retry(() -> {
            WebElement card = visibilityOfElementLocated(getDriver(), schemeCardByName(schemeName));
            click(getDriver(), card.findElement(By.cssSelector(".oas-scheme-card-name")));
            waitFor(ofMillis(1000L));
        });
        OasSecuritySchemeDialog dialog = new OasSecuritySchemeDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    @Override
    public void removeSecuritySchemeCard(String schemeName) {
        WebElement card = visibilityOfElementLocated(getDriver(), schemeCardByName(schemeName));
        click(getDriver(), card.findElement(By.tagName("button")));
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getDocumentSecurityButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), DOCUMENT_SECURITY_BUTTON_LOCATOR);
        }
        return visibilityOfElementLocated(getDriver(), DOCUMENT_SECURITY_BUTTON_LOCATOR);
    }

    @Override
    public boolean isDocumentSecurityButtonEnabled() {
        WebElement button = visibilityOfElementLocated(getDriver(), DOCUMENT_SECURITY_BUTTON_LOCATOR);
        String klass = button.getAttribute("class");
        return button.isEnabled() && (klass == null || !klass.contains("mat-mdc-button-disabled"));
    }

    @Override
    public String getDocumentSecuritySummary() {
        String text = getText(visibilityOfElementLocated(getDriver(), DOCUMENT_SECURITY_LABEL_LOCATOR));
        if (text == null) {
            return "";
        }
        String prefix = "Document Security:";
        if (text.startsWith(prefix)) {
            return text.substring(prefix.length()).trim();
        }
        return text;
    }

    @Override
    public OasSecurityRequirementDialog openDocumentSecurityDialog() {
        retry(() -> {
            click(getDocumentSecurityButton(true));
            waitFor(ofMillis(1000L));
        });
        OasSecurityRequirementDialog dialog = new OasSecurityRequirementDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    @Override
    public OasSecurityRequirementDialog openOperationSecurityDialog(WebElement tableRecord) {
        retry(() -> {
            WebElement cell = getColumnByName(tableRecord, "security");
            click(getDriver(), cell.findElement(By.cssSelector("div.oas-security-cell")));
            waitFor(ofMillis(1000L));
        });
        OasSecurityRequirementDialog dialog = new OasSecurityRequirementDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    @Override
    public boolean isRowSecurityNotApplicable(WebElement tableRecord) {
        WebElement cell = getColumnByName(tableRecord, "security");
        return !cell.findElements(By.cssSelector("span.oas-security-na")).isEmpty();
    }

    @Override
    public String getRowSecuritySummary(WebElement tableRecord) {
        WebElement cell = getColumnByName(tableRecord, "security");
        if (!cell.findElements(By.cssSelector("span.oas-security-na")).isEmpty()) {
            return "—"; // em dash placeholder
        }
        return getText(cell.findElement(By.cssSelector("span.oas-security-text")));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(),
                By.xpath("//td//*[contains(normalize-space(.), " +
                        org.oagi.score.e2e.impl.PageHelper.xpathLiteral(value) + ")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void toggleSelect(WebElement tableRecord) {
        WebElement selectCell = getColumnByName(tableRecord, "select");
        click(selectCell.findElement(By.tagName("mat-checkbox")));
    }

    @Override
    public WebElement getRemoveButton(boolean enabled) {
        By removeButtonLocator = By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]");
        if (enabled) {
            return elementToBeClickable(getDriver(), removeButtonLocator);
        }
        return visibilityOfElementLocated(getDriver(), removeButtonLocator);
    }

    @Override
    public void removeSelectedBIEs() {
        click(getRemoveButton(true));
        WebElement confirmRemoveButton = elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Remove\")]//ancestor::button[1]"
        ));
        click(confirmRemoveButton);
        waitFor(ofMillis(500L));
        assert "Removed".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public void setRowArrayIndicator(WebElement tableRecord, boolean checked) {
        WebElement checkbox = getColumnByName(tableRecord, "arrayIndicator").findElement(By.tagName("mat-checkbox"));
        if (isChecked(checkbox) != checked) {
            // Driver-aware click on the inner <input> (not the mat-checkbox host): the "Added"/"Updated"
            // snackbar can overlap this checkbox (bottom-center), so this waits it out, scrolls into view,
            // and falls back to a JS click on intercept. Clicking the inner input (as setRowSuppressRoot does)
            // reliably fires Angular Material's toggle; a JS click on the host element would not.
            click(getDriver(), checkbox.findElement(By.tagName("input")));
        }
    }

    @Override
    public void setRowSuppressRoot(WebElement tableRecord, boolean checked) {
        WebElement checkbox = getColumnByName(tableRecord, "suppressRootIndicator").findElement(By.tagName("mat-checkbox"));
        if (isChecked(checkbox) != checked) {
            // Driver-aware click: clear any lingering snackbar, scroll into view, JS-click on intercept.
            click(getDriver(), checkbox.findElement(By.tagName("input")));
        }
    }

    @Override
    public boolean isRowSuppressRootChecked(WebElement tableRecord) {
        WebElement checkbox = getColumnByName(tableRecord, "suppressRootIndicator").findElement(By.tagName("mat-checkbox"));
        return isChecked(checkbox);
    }

    @Override
    public boolean isRowArrayIndicatorChecked(WebElement tableRecord) {
        WebElement checkbox = getColumnByName(tableRecord, "arrayIndicator").findElement(By.tagName("mat-checkbox"));
        return isChecked(checkbox);
    }

    @Override
    public String getRowVerb(WebElement tableRecord) {
        return getText(getColumnByName(tableRecord, "verb"));
    }

    @Override
    public void setRowVerb(WebElement tableRecord, String verb) {
        WebElement verbCell = getColumnByName(tableRecord, "verb");
        click(getDriver(), verbCell.findElement(By.tagName("mat-select")));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + verb + "\"]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public String getRowOperationId(WebElement tableRecord) {
        return getColumnByName(tableRecord, "operationId").findElement(By.tagName("input")).getAttribute("value");
    }

    @Override
    public String getRowDen(WebElement tableRecord) {
        String den = getText(getColumnByName(tableRecord, "den"));
        return den == null ? "" : den;
    }

    @Override
    public String getRowResourceName(WebElement tableRecord) {
        return getColumnByName(tableRecord, "resourceName").findElement(By.tagName("input")).getAttribute("value");
    }

    @Override
    public void setRowResourceName(WebElement tableRecord, String resourceName) {
        sendKeys(getColumnByName(tableRecord, "resourceName").findElement(By.tagName("input")), resourceName);
    }

    @Override
    public String getRowTagName(WebElement tableRecord) {
        return getColumnByName(tableRecord, "tagName").findElement(By.tagName("input")).getAttribute("value");
    }

    @Override
    public void setRowOperationId(WebElement tableRecord, String operationId) {
        sendKeys(getColumnByName(tableRecord, "operationId").findElement(By.tagName("input")), operationId);
    }

    @Override
    public String getRowOperationIdError(WebElement tableRecord) {
        try {
            return getText(getColumnByName(tableRecord, "operationId").findElement(By.tagName("mat-error")));
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    @Override
    public boolean isRowArrayIndicatorDisabled(WebElement tableRecord) {
        return !getColumnByName(tableRecord, "arrayIndicator")
                .findElement(By.tagName("input")).isEnabled();
    }

    @Override
    public boolean isRowSuppressRootDisabled(WebElement tableRecord) {
        return !getColumnByName(tableRecord, "suppressRootIndicator")
                .findElement(By.tagName("input")).isEnabled();
    }

    @Override
    public boolean isRowMessageBodyDisabled(WebElement tableRecord) {
        WebElement select = getColumnByName(tableRecord, "messageBody").findElement(By.tagName("mat-select"));
        return "true".equals(select.getAttribute("aria-disabled"))
                || select.getAttribute("class").contains("mat-mdc-select-disabled");
    }
}
