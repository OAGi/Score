package org.oagi.score.e2e.impl.page.bie;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.ExpressBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ExpressBIEPageImpl extends BasePageImpl implements ExpressBIEPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");
    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");
    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By GENERATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Generate\")]//ancestor::button[1]");
    private static final By OPEN_API_FORMAT_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Format\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ExpressBIEPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/express").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Express BIE".equals(getText(getTitle()));
    }

    private String getCheckedAttribute(WebElement element) {
        return element.findElement(By.xpath("label/span[1]/input")).getAttribute("aria-checked");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public void selectBIEForExpression(TopLevelASBIEPObject topLevelASBIEP) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(topLevelASBIEP.getReleaseId());
        setBranch(release.getReleaseNumber());
        setDEN(topLevelASBIEP.getDen());
        hitSearchButton();

        retry(() -> {
            WebElement tr = getTableRecordByValue(topLevelASBIEP.getDen());
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        });
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getOpenAPIFormatSelectField() {
        return visibilityOfElementLocated(getDriver(), OPEN_API_FORMAT_SELECT_FIELD_LOCATOR);
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
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public File hitGenerateButton(ExpressionFormat format) {
        return hitGenerateButton(format, false);
    }

    @Override
    public File hitGenerateButton(ExpressionFormat format, boolean compressed) {
        click(getGenerateButton());
        try {
            return waitForDownloadFile(Duration.ofMillis(30000), getValidator(format, compressed));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Function<File, Boolean> getValidator(ExpressionFormat format, boolean compressed) {
        Function<File, Boolean> validator;
        switch (format) {
            case XML:
                validator = xmlValidator();
                break;
            case JSON:
                validator = jsonValidator();
                break;
            case YML:
                validator = ymlValidator();
                break;
            default:
                throw new IllegalArgumentException("Unsupported expression format: " + format);
        }

        if (compressed) {
            return file -> {
                if (!file.getName().endsWith(".zip")) {
                    return false;
                }

                try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
                    ZipEntry entry = zipInputStream.getNextEntry();
                    while (entry != null) {
                        File entryFile = new File(FileUtils.getTempDirectory(), entry.getName());
                        try (OutputStream outputStream = new FileOutputStream(entryFile)) {
                            IOUtils.copy(zipInputStream, outputStream);
                        }
                        if (!validator.apply(entryFile)) {
                            return false;
                        }

                        entry = zipInputStream.getNextEntry();
                    }
                    zipInputStream.closeEntry();
                } catch (IOException ignore) {
                    return false;
                }

                return true;
            };
        }
        return validator;
    }

    private Function<File, Boolean> xmlValidator() {
        return file -> {
            if (!file.getName().endsWith(".xsd")) {
                return false;
            }

            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                Element rootElement = document.getDocumentElement();
                if (!"xsd:schema".equals(rootElement.getTagName())) {
                    return false;
                }
            } catch (Exception e) {
                logger.trace("Can't parse " + file, e);
                return false;
            }

            return true;
        };
    }

    private Function<File, Boolean> jsonValidator() {
        return file -> {
            if (!file.getName().endsWith(".json")) {
                return false;
            }

            try {
                String str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(str);
            } catch (Exception e) {
                logger.trace("Can't parse " + file, e);
                return false;
            }

            return true;
        };
    }

    private Function<File, Boolean> ymlValidator() {
        return file -> {
            if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
                return false;
            }

            try {
                String str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                Map<String, Object> schema = new Yaml().load(str);
            } catch (Exception e) {
                logger.trace("Can't parse " + file, e);
                return false;
            }

            return true;
        };
    }

    private File waitForDownloadFile(Duration duration, Function<File, Boolean> validator) throws IOException, InterruptedException {
        String userHome = System.getProperty("user.home");
        Path path = Paths.get(new File(userHome, "Downloads").toURI());

        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        long timeout = duration.toMillis();
        File downloadedFile = null;
        WatchKey key;
        do {
            key = watchService.poll(1000L, TimeUnit.MILLISECONDS);
            if (key != null && key.isValid()) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    downloadedFile = new File(path.toFile(), event.context().toString());
                    if (validator.apply(downloadedFile)) {
                        return downloadedFile;
                    }
                }
                key.reset();
            }
            if (downloadedFile != null && validator.apply(downloadedFile)) {
                return downloadedFile;
            }
            timeout -= 1000L;
        } while (timeout > 0L);

        throw new FileNotFoundException();
    }

    @Override
    public WebElement getGenerateButton() {
        return elementToBeClickable(getDriver(), GENERATE_BUTTON_LOCATOR);
    }

    @Override
    public void toggleBIECCTSMetaData() {
        click(getBIECCTSMetaDataCheckbox());
    }

    @Override
    public WebElement getBIECCTSMetaDataCheckbox() {
        return getCheckboxByName("BIE CCTS Meta Data");
    }

    @Override
    public void toggleIncludeCCTSDefinitionTag() {
        click(getIncludeCCTSDefinitionTagCheckbox().findElement(By.tagName("label")));
    }

    @Override
    public WebElement getIncludeCCTSDefinitionTagCheckbox() {
        return getCheckboxByName("Include CCTS_Definition Tag");
    }

    @Override
    public void toggleBIEGUID() {
        click(getBIEGUIDCheckbox());
    }

    @Override
    public WebElement getBIEGUIDCheckbox() {
        return getCheckboxByName("BIE GUID");
    }

    @Override
    public void toggleBIEOAGIScoreMetaData() {
        click(getBIEOAGIScoreMetaDataCheckbox());
    }

    @Override
    public WebElement getBIEOAGIScoreMetaDataCheckbox() {
        return getCheckboxByName("BIE OAGi/Score Meta Data");
    }

    @Override
    public void toggleIncludeWHOColumns() {
        click(getIncludeWHOColumnsCheckbox().findElement(By.tagName("label")));
    }

    @Override
    public WebElement getIncludeWHOColumnsCheckbox() {
        return getCheckboxByName("Include WHO Columns");
    }

    @Override
    public void toggleBasedCCMetaData() {
        click(getBasedCCMetaDataCheckbox());
    }

    @Override
    public WebElement getBasedCCMetaDataCheckbox() {
        return getCheckboxByName("Based CC Meta Data");
    }

    @Override
    public void toggleBusinessContext() {
        click(getBusinessContextCheckbox());
    }

    @Override
    public WebElement getBusinessContextCheckbox() {
        return getCheckboxByName("Business Context");
    }

    @Override
    public void toggleBIEDefinition() {
        click(getBIEDefinitionCheckbox());
    }

    @Override
    public WebElement getBIEDefinitionCheckbox() {
        return getCheckboxByName("BIE Definition");
    }

    @Override
    public void selectXMLSchemaExpression() {
        click(getXMLSchemaExpressionRadioButton());
    }

    @Override
    public JSONSchemaExpressionOptions selectJSONSchemaExpression() {
        click(getJSONSchemaExpressionRadioButton());
        return new JSONSchemaExpressionOptionsImpl();
    }

    @Override
    public WebElement getXMLSchemaExpressionRadioButton() {
        return getRadioButtonByName("XML Schema");
    }

    @Override
    public WebElement getJSONSchemaExpressionRadioButton() {
        return getRadioButtonByName("JSON Schema");
    }

    @Override
    public WebElement getOpenAPIExpressionRadioButton() {
        return getRadioButtonByName("Open API 3.0 (Template)");
    }

    @Override
    public void selectPutAllSchemasInTheSameFile() {
        click(getPutAllSchemasInTheSameFileRadioButton());
    }

    @Override
    public WebElement getPutAllSchemasInTheSameFileRadioButton() {
        return getRadioButtonByName("Put all schemas in the same file");
    }

    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }

    private WebElement getCheckboxByNameAndClassInCheckbox(String name, String classInCheckbox) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[contains(@class, \"" + classInCheckbox + "\")]"));
    }

    private WebElement getRadioButtonByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-radio-button[1]"));
    }

    @Override
    public void selectMultipleBIEsForExpression(ReleaseObject release, ArrayList<TopLevelASBIEPObject> biesForSelection) {
        setBranch(release.getReleaseNumber());
        for (TopLevelASBIEPObject bie : biesForSelection) {
            retry(() -> {
                WebElement tr = getTableRecordByValue(bie.getDen());
                WebElement td = getColumnByName(tr, "select");
                click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
            });
        }
    }

    @Override
    public void selectPutEachSchemaInAnIndividualFile() {
        click(getPutEachSchemaInAnIndividualFileRadioButton());
    }

    @Override
    public WebElement getPutEachSchemaInAnIndividualFileRadioButton() {
        return getRadioButtonByName("Put each schema in an individual file");
    }

    @Override
    public int getNumberOfBIEsInIndexBox() {
        WebElement paginatorRangeLabelElement = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"mat-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
    }

    @Override
    public int getNumberfBIEsInTable() {
        List<WebElement> rows = getDriver().findElements(By.xpath("//td//span/ancestor::tr"));
        int numberOfBIEs = rows.size();
        return numberOfBIEs;
    }

    @Override
    public OpenAPIExpressionOptions selectOpenAPIExpression() {
        click(getOpenAPIExpressionRadioButton());
        return new OpenAPIExpressionOptionsImpl();
    }

    @Override
    public void selectJSONOpenAPIFormat() {
        retry(() -> {
            click(getOpenAPIFormatSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option/span[contains(text(), \"JSON\")]"));
            click(optionField);
        });
    }

    private class JSONSchemaExpressionOptionsImpl implements JSONSchemaExpressionOptions {
        @Override
        public WebElement getMakeAsAnArrayCheckbox() {
            return getCheckboxByName("Make as an array");
        }

        @Override
        public void toggleMakeAsAnArray() {
            click(getMakeAsAnArrayCheckbox().findElement(By.tagName("label")));
        }

        @Override
        public WebElement getIncludeMetaHeaderCheckbox() {
            return getCheckboxByName("Include Meta Header");
        }

        @Override
        public void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context) {
            String checked = getCheckedAttribute(getIncludeMetaHeaderCheckbox());
            if (checked.equals("true")) {
                click(getIncludeMetaHeaderCheckbox());
            } else {
                click(getIncludeMetaHeaderCheckbox());
                IncludeMetaHeaderProfileBIEDialogImpl includeMetaHeaderProfileBIEDialog =
                        new IncludeMetaHeaderProfileBIEDialogImpl(ExpressBIEPageImpl.this);
                assert includeMetaHeaderProfileBIEDialog.isOpened();
                includeMetaHeaderProfileBIEDialog.selectMetaHeaderProfile(metaHeaderASBIEP, context);
            }
        }

        @Override
        public WebElement getIncludePaginationResponseCheckbox() {
            return getCheckboxByName("Include Pagination Response");
        }

        @Override
        public void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context) {
            String checked = getCheckedAttribute(getIncludePaginationResponseCheckbox());
            if (checked.equals("true")) {
                click(getIncludePaginationResponseCheckbox());
            } else {
                click(getIncludePaginationResponseCheckbox());
                IncludePaginationResponseProfileBIEDialogImpl includePaginationResponseProfileBIEDialog =
                        new IncludePaginationResponseProfileBIEDialogImpl(ExpressBIEPageImpl.this);
                assert includePaginationResponseProfileBIEDialog.isOpened();
                includePaginationResponseProfileBIEDialog.selectPaginationResponseProfile(paginationResponseASBIEP, context);
            }
        }
    }

    private class OpenAPIExpressionOptionsImpl implements OpenAPIExpressionOptions {
        @Override
        public void selectYAMLOpenAPIFormat() {
            retry(() -> {
                click(getOpenAPIFormatSelectField());
                WebElement optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option/span[contains(text(), \"YAML\")]"));
                click(optionField);
            });
        }

        @Override
        public WebElement getGETOperationTemplateCheckbox() {
            return getCheckboxByName("GET Operation Template");
        }

        @Override
        public OpenAPIExpressionGETOperationOptions toggleGETOperationTemplate() {
            click(getGETOperationTemplateCheckbox());
            return new OpenAPIExpressionGETOperationOptionsImpl();
        }

        @Override
        public WebElement getPOSTOperationTemplateCheckbox() {
            return getCheckboxByName("POST Operation Template");
        }

        @Override
        public OpenAPIExpressionPOSTOperationOptions togglePOSTOperationTemplate() {
            click(getPOSTOperationTemplateCheckbox());
            return new OpenAPIExpressionPOSTOperationOptionsImpl();
        }

        @Override
        public void selectJSONOpenAPIFormat() {
            retry(() -> {
                click(getOpenAPIFormatSelectField());
                WebElement optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option/span[contains(text(), \"JSON\")]"));
                click(optionField);
            });
        }
    }

    private class OpenAPIExpressionGETOperationOptionsImpl implements OpenAPIExpressionGETOperationOptions {

        @Override
        public WebElement getMakeAsAnArrayCheckbox() {
            return getCheckboxByNameAndClassInCheckbox("Make as an array", "get-operation-template");
        }

        @Override
        public void toggleMakeAsAnArray() {
            click(getMakeAsAnArrayCheckbox().findElement(By.tagName("label")));
        }

        @Override
        public WebElement getIncludeMetaHeaderCheckbox() {
            return getCheckboxByNameAndClassInCheckbox("Include Meta Header", "get-operation-template");
        }

        @Override
        public void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context) {
            String checked = getCheckedAttribute(getIncludeMetaHeaderCheckbox());
            if (checked.equals("true")) {
                click(getIncludeMetaHeaderCheckbox());
            } else {
                click(getIncludeMetaHeaderCheckbox());
                IncludeMetaHeaderProfileBIEDialogImpl includeMetaHeaderProfileBIEDialog =
                        new IncludeMetaHeaderProfileBIEDialogImpl(ExpressBIEPageImpl.this);
                assert includeMetaHeaderProfileBIEDialog.isOpened();
                includeMetaHeaderProfileBIEDialog.selectMetaHeaderProfile(metaHeaderASBIEP, context);
            }
        }

        @Override
        public WebElement getIncludePaginationResponseCheckbox() {
            return getCheckboxByNameAndClassInCheckbox("Include Pagination Response", "get-operation-template");
        }

        @Override
        public void toggleIncludePaginationResponse(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context) {
            String checked = getCheckedAttribute(getIncludePaginationResponseCheckbox());
            if (checked.equals("true")) {
                click(getIncludePaginationResponseCheckbox());
            } else {
                click(getIncludePaginationResponseCheckbox());
                IncludePaginationResponseProfileBIEDialogImpl includePaginationResponseProfileBIEDialog =
                        new IncludePaginationResponseProfileBIEDialogImpl(ExpressBIEPageImpl.this);
                assert includePaginationResponseProfileBIEDialog.isOpened();
                includePaginationResponseProfileBIEDialog.selectPaginationResponseProfile(paginationResponseASBIEP, context);
            }
        }
    }

    private class OpenAPIExpressionPOSTOperationOptionsImpl implements OpenAPIExpressionPOSTOperationOptions {

        @Override
        public WebElement getMakeAsAnArrayCheckbox() {
            return getCheckboxByNameAndClassInCheckbox("Make as an array", "post-operation-template");
        }

        @Override
        public void toggleMakeAsAnArray() {
            click(getMakeAsAnArrayCheckbox().findElement(By.tagName("label")));
        }

        @Override
        public WebElement getIncludeMetaHeaderCheckbox() {
            return getCheckboxByNameAndClassInCheckbox("Include Meta Header", "post-operation-template");
        }

        @Override
        public void toggleIncludeMetaHeader(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context) {
            String checked = getCheckedAttribute(getIncludeMetaHeaderCheckbox());
            if (checked.equals("true")) {
                click(getIncludeMetaHeaderCheckbox());
            } else {
                click(getIncludeMetaHeaderCheckbox());
                IncludeMetaHeaderProfileBIEDialogImpl includeMetaHeaderProfileBIEDialog =
                        new IncludeMetaHeaderProfileBIEDialogImpl(ExpressBIEPageImpl.this);
                assert includeMetaHeaderProfileBIEDialog.isOpened();
                includeMetaHeaderProfileBIEDialog.selectMetaHeaderProfile(metaHeaderASBIEP, context);
            }
        }
    }
}
