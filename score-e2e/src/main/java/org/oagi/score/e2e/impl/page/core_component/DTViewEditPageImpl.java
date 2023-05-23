package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.code_list.AddCommentDialogImpl;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class DTViewEditPageImpl extends BasePageImpl implements DTViewEditPage {

    private static final By CORE_COMPONENT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Core Component\")]//ancestor::mat-form-field//input");

    private static final By RELEASE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Release\")]//ancestor::mat-form-field//input");

    private static final By REVISION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Revision\")]//ancestor::mat-form-field//input");

    private static final By STATE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"State\")]//ancestor::mat-form-field//input");

    private static final By OWNER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::mat-form-field//input");
    private static final By QUALIFIER_FIELD_LOCATOR =
            By.xpath("//label/span[contains(text(), \"Qualifier\")]//ancestor::mat-form-field//input");

    private static final By GUID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"GUID\")]//ancestor::mat-form-field//input");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");

    private static final By DATA_TYPE_TERM_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Data Type Term\")]//ancestor::mat-form-field//input");

    private static final By REPRESENTATION_TERM_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Representation Term\")]//ancestor::mat-form-field//input");

    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Namespace\")]//ancestor::mat-form-field//mat-select");

    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By VALUE_DOMAIN_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Value Domain\")]");

    private static final By ADD_VALUE_DOMAIN_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    private static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    public static final By CONTINUE_TO_UPDATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button/span");
    public static final By DEFAULT_VALUE_DOMAIN_SELECT_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"Default\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By COMMENTS_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Comments\")]");
    public static final By CONTINUE_TO_RESTORE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Restore\")]//ancestor::button/span");
    private static final By BASED_DATA_TYPE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Based Data Type\")]//ancestor::mat-form-field//input");
    private static final By SIX_HEXADECIMAL_IDENTIFIER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Six Hexadecimal Identifier\")]//ancestor::mat-form-field//input");
    private static final By CONTENT_COMPONENT_DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Content Component Definition\")]//ancestor::mat-form-field//textarea");
    private final DTObject dt;

    public DTViewEditPageImpl(BasePage parent, DTObject dt) {
        super(parent);
        this.dt = dt;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component/dt/" + this.dt.getDtManifestId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        String expectedType = (dt.getBasedDtManifestId() == null) ? "CDT" : "BDT";
        assert expectedType.equals(getCoreComponentTypeFieldValue());
        assert getText(getTitle()).equals(dt.getDen());
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.cssSelector("div.mat-tab-list div.mat-tab-label"));
    }

    @Override
    public WebElement getCoreComponentTypeField() {
        return visibilityOfElementLocated(getDriver(), CORE_COMPONENT_FIELD_LOCATOR);
    }

    @Override
    public String getCoreComponentTypeFieldValue() {
        return getText(getCoreComponentTypeField());
    }

    @Override
    public WebElement getReleaseField() {
        return visibilityOfElementLocated(getDriver(), RELEASE_FIELD_LOCATOR);
    }

    @Override
    public String getReleaseFieldValue() {
        return getText(getReleaseField());
    }

    @Override
    public WebElement getRevisionField() {
        return visibilityOfElementLocated(getDriver(), REVISION_FIELD_LOCATOR);
    }

    @Override
    public String getRevisionFieldValue() {
        return getText(getRevisionField());
    }

    @Override
    public WebElement getStateField() {
        return visibilityOfElementLocated(getDriver(), STATE_FIELD_LOCATOR);
    }

    @Override
    public String getStateFieldValue() {
        return getText(getStateField());
    }

    @Override
    public WebElement getOwnerField() {
        return visibilityOfElementLocated(getDriver(), OWNER_FIELD_LOCATOR);
    }

    @Override
    public String getOwnerFieldValue() {
        return getText(getOwnerField());
    }

    @Override
    public WebElement getGUIDField() {
        return visibilityOfElementLocated(getDriver(), GUID_FIELD_LOCATOR);
    }

    @Override
    public String getGUIDFieldValue() {
        return getText(getGUIDField());
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public String getDENFieldValue() {
        return getText(getDENField());
    }

    @Override
    public WebElement getDataTypeTermField() {
        return visibilityOfElementLocated(getDriver(), DATA_TYPE_TERM_FIELD_LOCATOR);
    }

    @Override
    public String getDataTypeTermFieldLabel() {
        return getDataTypeTermField().getAttribute("data-placeholder");
    }

    @Override
    public String getDataTypeTermFieldValue() {
        return getText(getDataTypeTermField());
    }

    @Override
    public WebElement getRepresentationTermField() {
        return visibilityOfElementLocated(getDriver(), REPRESENTATION_TERM_FIELD_LOCATOR);
    }

    @Override
    public String getRepresentationTermFieldLabel() {
        return getRepresentationTermField().getAttribute("data-placeholder");
    }

    @Override
    public String getRepresentationTermFieldValue() {
        return getText(getRepresentationTermField());
    }

    @Override
    public WebElement getNamespaceField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public String getNamespaceFieldValue() {
        return getText(getNamespaceField());
    }

    @Override
    public WebElement getDefinitionSourceField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
    }

    @Override
    public String getDefinitionSourceFieldValue() {
        return getText(getDefinitionSourceField());
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public String getDefinitionFieldValue() {
        return getText(getDefinitionField());
    }

    @Override
    public void showValueDomain() {
        click(getShowValueDomain());
    }
    @Override
    public WebElement getShowValueDomain() {
        return elementToBeClickable(getDriver(), VALUE_DOMAIN_LOCATOR);
    }
    @Override
    public WebElement getAddValueDomainButton() {
        return elementToBeClickable(getDriver(), ADD_VALUE_DOMAIN_LOCATOR);
    }

    @Override
    public void addCodeListValueDomain(String valueDomainName) {
        click(getAddValueDomainButton());
        WebElement tr = getTheLastTableRecord();
        WebElement tdDomainType = getColumnByName(tr, "type");
        click(tdDomainType);
        click(elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \"Code List\")]//ancestor::mat-option[1]")));
        WebElement tdDomainName = getColumnByName(tr, "name");
        click(tdDomainName);
        click(elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \""+ valueDomainName +"\")]//ancestor::mat-option[1]")));
    }
    @Override
    public WebElement getTheLastTableRecord() {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-expansion-panel//table//tbody//tr[last()]"));
    }
    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
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
    public void hitUpdateAnywayButton() {
        retry(() -> {
            click(getUpdateAnywayButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getUpdateAnywayButton() {
        return elementToBeClickable(getDriver(), CONTINUE_TO_UPDATE_BUTTON_IN_DIALOG_LOCATOR);
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
    public void setQualifier(String qualifier) {
        sendKeys(getQualifierField(), qualifier);
    }
    @Override
    public WebElement getQualifierField() {
        return visibilityOfElementLocated(getDriver(), QUALIFIER_FIELD_LOCATOR);
    }

    @Override
    public void codeListIdMarkedAsDeleted(String name) {
        WebElement tr = getTheLastTableRecord();
        WebElement tdDomainName = getColumnByName(tr, "name");
        click(tdDomainName);
        WebElement codeList = findElement(getDriver(), By.xpath("//span[contains(text(), \""+ name +"\")]//ancestor::mat-option[1]"));
        codeList.findElement(By.xpath("//span[@class=\"text-line-through\"]"));
    }

    @Override
    public void changeCodeListValueDomain(String codeListName) {
        WebElement tr = getTheLastTableRecord();
        WebElement tdDomainType = getColumnByName(tr, "type");
        click(tdDomainType);
        click(elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \"Code List\")]//ancestor::mat-option[1]")));
        WebElement tdDomainName = getColumnByName(tr, "name");
        click(tdDomainName);
        click(elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \""+ codeListName +"\")]//ancestor::mat-option[1]")));
    }

    @Override
    public void setDefaultValueDomain(String name) {
        click(getDefaultValueDomainField());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \""+ name +"\")]//ancestor::mat-option[1]")));
    }
    @Override
    public WebElement getDefaultValueDomainField() {
        return visibilityOfElementLocated(getDriver(), DEFAULT_VALUE_DOMAIN_SELECT_LOCATOR);
    }

    @Override
    public void setNamespace(NamespaceObject namespace) {
        click(getNamespaceField());
        waitFor(ofMillis(1000L));
        WebElement option = elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \"" + namespace.getUri() + "\")]//ancestor::mat-option"));
        click(option);
    }

    @Override
    public AddCommentDialog hitAddCommentButton(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(visibilityOfElementLocated(getDriver(), COMMENTS_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(visibilityOfElementLocated(getDriver(), COMMENTS_OPTION_LOCATOR));
        }
        AddCommentDialog addCodeListCommentDialog = new AddCommentDialogImpl(this);
        assert addCodeListCommentDialog.isOpened();
        return addCodeListCommentDialog;
    }

    @Override
    public WebElement clickOnDropDownMenuByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        String nodeName = nodes[nodes.length - 1];
        WebElement node = getNodeByName(nodeName);
        click(node);
        new Actions(getDriver()).sendKeys("O").perform();
        try {
            if (visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed()) {
                return node;
            }
        } catch (WebDriverException ignore) {
        }
        WebElement contextMenuIcon = getContextMenuIconByNodeName(nodeName);
        click(contextMenuIcon);
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed();
        return node;
    }

    private WebElement goToNode(String path) {
        click(getSearchField());
        WebElement node = sendKeys(visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR), path);
        node.sendKeys(Keys.ENTER);
        click(node);
        clear(getSearchField());
        return node;
    }
    @Override
    public WebElement getSearchField() {
        return visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR);
    }

    public WebElement getNodeByName(String name) {
        return elementToBeClickable(getDriver(), By.xpath(
                "//cdk-virtual-scroll-viewport//*[contains(text(), \"" + name + "\")]" +
                        "//ancestor::div[contains(@class, \"mat-tree-node\")]"));
    }

    @Override
    public WebElement getContextMenuIconByNodeName(String nodeName) {
        WebElement node = getNodeByName(nodeName);
        return node.findElement(By.xpath("//mat-icon[contains(text(), \"more_vert\")]"));
    }

    @Override
    public WebElement getReviseButton() {
        return elementToBeClickable(getDriver(), REVISE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getRestoreButton() {
        return elementToBeClickable(getDriver(), RESTORE_BUTTON_LOCATOR);
    }

    @Override
    public void hitRestoreButton() {
        retry(() -> {
            click(getRestoreButton());
            waitFor(ofMillis(1000L));
            click(elementToBeClickable(getDriver(), CONTINUE_TO_RESTORE_BUTTON_IN_DIALOG_LOCATOR));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public String getBasedDataTypeFieldValue() {
        return getText(getBasedDataTypeField());
    }

    @Override
    public WebElement getBasedDataTypeField() {
        return visibilityOfElementLocated(getDriver(), BASED_DATA_TYPE_FIELD_LOCATOR);
    }

    @Override
    public String getQualifierFieldValue() {
        return getText(getQualifierField());
    }

    @Override
    public WebElement getSixHexadecimalIdentifierField() {
        return visibilityOfElementLocated(getDriver(), SIX_HEXADECIMAL_IDENTIFIER_FIELD_LOCATOR);
    }

    @Override
    public String getContentComponentDefinitionFieldValue() {
        return getText(getContentComponentDefinitionField());
    }

    @Override
    public WebElement getContentComponentDefinitionField() {
        return visibilityOfElementLocated(getDriver(), CONTENT_COMPONENT_DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public void setDefinitionSource(String definitionSource) {
        sendKeys(getDefinitionSourceField(), definitionSource);
    }

    @Override
    public void setContentComponentDefinition(String contentComponentDefinition) {
        sendKeys(getContentComponentDefinitionField(), contentComponentDefinition);
    }

    @Override
    public WebElement getValueDomainByTypeNameAndXSDExpression(String valueDomainType, String valueDomainName, String XSDExpression) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(),\""+valueDomainType+"\")]" +
                "/ancestor::tr[1]//*[contains(text(),\""+valueDomainName+"\")]//ancestor::tr//*[contains(text(),\""+XSDExpression+"\")]"));
    }

    @Override
    public WebElement getCheckboxForValueDomainByTypeAndName(String valueDomainType, String valueDomainName) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(),\""+valueDomainType+"\")]/ancestor::tr[1]//*[contains(text()" +
                ",\""+valueDomainName+"\")]//ancestor::tr/td[1]//label/span[1]//input"));
    }
}
