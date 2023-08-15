package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.code_list.AddCommentDialogImpl;
import org.oagi.score.e2e.obj.CodeListObject;
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
    private static final By OBJECT_CLASS_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Object Class Term\")]//ancestor::mat-form-field//input");
    private static final By PROPERTY_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Property Term\")]//ancestor::mat-form-field//input");
    private static final By REPRESENTATION_TERM_SELECTOR_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Representation Term\")]//ancestor::mat-form-field//mat-select");

    private static final By DATA_TYPE_TERM_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Data Type Term\")]//ancestor::mat-form-field//input");

    private static final By REPRESENTATION_TERM_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Representation Term\")]//ancestor::mat-form-field//input");

    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Namespace\")]//ancestor::mat-form-field//mat-select");
    private static final By CARDINALITY_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Cardinality\")]//ancestor::mat-form-field//mat-select");
    private static final By VALUE_CONSTRAINT_TYPE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Value Constraint\")]//ancestor::mat-form-field//mat-select");

    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By VALUE_DOMAIN_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Value Domain\")]");

    private static final By ADD_VALUE_DOMAIN_LOCATOR =
            By.xpath("//span[contains(text(), \"Add\")]//ancestor::button[1]");
    private static final By DISCARD_VALUE_DOMAIN_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    private static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    public static final By CONTINUE_TO_UPDATE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update anyway\")]//ancestor::button/span");
    public static final By CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Delete anyway\")]//ancestor::button/span");
    public static final By DEFAULT_VALUE_DOMAIN_SELECT_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Default\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By COMMENTS_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Comments\")]");
    private static final By SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Add Supplementary Component\")]");
    private static final By REMOVE_SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Remove\")]");
    public static final By CONTINUE_TO_RESTORE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Restore\")]//ancestor::button/span");
    private static final By BASED_DATA_TYPE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Based Data Type\")]//ancestor::mat-form-field//input");
    private static final By SIX_HEXADECIMAL_IDENTIFIER_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Six Hexadecimal Identifier\")]//ancestor::mat-form-field//input");
    private static final By CONTENT_COMPONENT_DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Content Component Definition\")]//ancestor::mat-form-field//textarea");
    private static final By DEFINITION_EMPTY_WARNING_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");
    private static final By DELETE_ANYWAY_WARNING_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");
    private static final By MOVE_TO_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");
    private static final By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");
    private static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    public static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");
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
        click(getDriver(), getShowValueDomain());
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
    public void codeListIdMarkedAsDeleted(CodeListObject codeList) {
        String codeListName = codeList.getName();
        WebElement tr = getTableRecordByValue(codeListName);
        WebElement tdDomainName = getColumnByName(tr, "name");
        click(tdDomainName);

        visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + codeListName + "\")]//ancestor::mat-option[1]//span[@class=\"text-line-through\"]"));
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
        waitFor(ofMillis(1000L));
        assert getNamespaceFieldValue().equals(namespace.getUri());
    }

    @Override
    public AddCommentDialog hitAddCommentButton(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), COMMENTS_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), COMMENTS_OPTION_LOCATOR));
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
        click(getDriver(), node);
        new Actions(getDriver()).sendKeys("O").perform();
        waitFor(ofMillis(1000L));
        try {
            if (visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed()) {
                return node;
            }
        } catch (WebDriverException ignore) {
        }
        WebElement contextMenuIcon = getContextMenuIconByNodeName(nodeName);
        click(getDriver(), contextMenuIcon);
        waitFor(ofMillis(1000L));
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed();
        return node;
    }

    @Override
    public WebElement goToNode(String path) {
        click(getSearchField());
        WebElement node = retry(() -> {
            WebElement e = sendKeys(getSearchField(), path);
            if (!path.equals(getText(getSearchField()))) {
                throw new WebDriverException();
            }
            return e;
        });
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
        By nodeLocator = By.xpath(
                "//*[text() = \"" + name + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]");
        return visibilityOfElementLocated(getDriver(), nodeLocator);
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

    @Override
    public void discardValueDomain() {
        click(getDiscardValueDomainButton());
    }

    @Override
    public WebElement getDiscardValueDomainButton(){
        return elementToBeClickable(getDriver(), DISCARD_VALUE_DOMAIN_LOCATOR);
    }

    @Override
    public String getDefinitionWarningDialogMessage() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_EMPTY_WARNING_DIALOG_MESSAGE_LOCATOR).getText();
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-expansion-panel//table//tbody//span[contains(text(), \""+value+"\")]/ancestor::tr"));
    }

    @Override
    public void selectValueDomain(String name) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(name);
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        });

        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void addSupplementaryComponent(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR));
        }
    }
    @Override
    public WebElement getNodeByPath(String path) {
        return retry(() -> {
            goToNode(path);
            String[] nodes = path.split("/");
            return getNodeByName(nodes[nodes.length - 1]);
        });
    }

    @Override
    public void removeSupplementaryComponent(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), REMOVE_SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), REMOVE_SUPPLEMENTARY_COMPONENT_OPTION_LOCATOR));
        }
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Remove anyway\")]//ancestor::button[1]")));
    }

    @Override
    public void hitReviseButton() {
        click(getReviseButton());
        click(elementToBeClickable(getDriver(), CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Revised".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public String getDefaultValueDomainFieldValue() {
        return getText(getDefaultValueDomainField());
    }

    @Override
    public WebElement getMoveToDraft(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_DRAFT_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_DRAFT_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToDraft() {
        click(getMoveToDraft(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }
    @Override
    public WebElement getMoveToCandidate(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToCandidate() {
        click(getMoveToCandidate(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }
    @Override
    public WebElement getBackToWIPButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
        }
    }

    @Override
    public void backToWIP() {
        click(getBackToWIPButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }
    @Override
    public WebElement getDeleteButton() {
        return elementToBeClickable(getDriver(), DELETE_BUTTON_LOCATOR);
    }

    @Override
    public void hitDeleteButton() {
        retry(() -> {
            click(getDeleteButton());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//score-confirm-dialog//span[contains(text(), \"Delete anyway\")]//ancestor::button[1]")));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Deleted".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public String getDeleteWarningDialogMessage() {
        return visibilityOfElementLocated(getDriver(), DELETE_ANYWAY_WARNING_DIALOG_MESSAGE_LOCATOR).getText();
    }

    @Override
    public void hitDeleteAnywayButton() {
        retry(() -> {
            click(getDeleteAnywayButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getAmendButton() {
        return elementToBeClickable(getDriver(), AMEND_BUTTON_LOCATOR);
    }

    @Override
    public void hitAmendButton() {
        click(getAmendButton());
        click(elementToBeClickable(getDriver(), CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Amended".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getDeleteAnywayButton() {
        return elementToBeClickable(getDriver(), CONTINUE_TO_DELETE_BUTTON_IN_DIALOG_LOCATOR);
    }

    @Override
    public WebElement getMoveToQAButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToQA() {
        click(getMoveToQAButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }

    @Override
    public WebElement getMoveToProductionButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToProduction() {
        click(getMoveToProductionButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }

    @Override
    public String getInvalidStateIconText(WebElement node) {
        WebElement element = node.findElement(By.xpath("//mat-icon[contains(text(), \"error\")]"));
        return element.getAttribute("mattooltip").toString();
    }

    @Override
    public SupplementaryComponentPanel getSCPanel(WebElement scNode) {
        return retry(() -> {
            click(scNode);
            waitFor(ofMillis(1000L));
            String nodeText = getText(scNode);
            String panelTitle = getText(getTitle());
            assert nodeText.contains(panelTitle);
            return new SupplementaryComponentPanelImpl();
        });
    }

    private class SupplementaryComponentPanelImpl implements DTViewEditPage.SupplementaryComponentPanel {
        @Override
        public void setCardinality(String cardinality) {
            click(getCardinalityField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + cardinality + "\")]//ancestor::mat-option"));
            click(option);
        }
        @Override
        public WebElement getCardinalityField() {
            return visibilityOfElementLocated(getDriver(), CARDINALITY_FIELD_LOCATOR);
        }

        @Override
        public void setValueConstraintType(String valueConstraintType) {
            click(getValueConstraintTypeField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueConstraintType + "\")]//ancestor::mat-option"));
            click(option);
        }
        @Override
        public WebElement getValueConstraintTypeField() {
            return visibilityOfElementLocated(getDriver(), VALUE_CONSTRAINT_TYPE_FIELD_LOCATOR);
        }

        @Override
        public void setValueConstraint(String constraintValue) {
            sendKeys(getValueConstraintField(), constraintValue);
        }
        @Override
        public WebElement getValueConstraintField() {
            String selectedValueConstraintType = getValueConstraintTypeFieldValue();
            if (selectedValueConstraintType.equals("None")){
                return visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \"No value constraints\")]/ancestor::mat-form-field//input"));
            } else{
                return visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \"" + selectedValueConstraintType +
                        "\")]/ancestor::mat-form-field//input"));
            }
        }

        @Override
        public String getCardinalityFieldValue() {
            return getText(getCardinalityField());
        }

        @Override
        public String getValueConstraintTypeFieldValue() {
            return getText(getValueConstraintTypeField());
        }

        @Override
        public String getValueConstraintFieldValue() {
            return getText(getValueConstraintField());
        }

        @Override
        public String getObjectClassTermFieldValue() {
            return getText(getObjectClassTermField());
        }

        @Override
        public WebElement getObjectClassTermField() {
            return visibilityOfElementLocated(getDriver(), OBJECT_CLASS_TERM_FIELD_LOCATOR);
        }

        @Override
        public String getDefinitionFieldValue() {
            return getText(getDefinitionField());
        }

        @Override
        public String getDefinitionSourceFieldValue() {
            return getText(getDefinitionSourceField());
        }

        @Override
        public void setDefinition(String definition) {
            sendKeys(getDefinitionField(), definition);
        }

        @Override
        public String getPropertyTermFieldValue() {
            return getText(getPropertyTermField());
        }

        @Override
        public WebElement getPropertyTermField() {
            return visibilityOfElementLocated(getDriver(), PROPERTY_TERM_FIELD_LOCATOR);
        }

        @Override
        public WebElement getRepresentationSelectField() {
            return visibilityOfElementLocated(getDriver(), REPRESENTATION_TERM_SELECTOR_LOCATOR);
        }

        @Override
        public WebElement getDefinitionField() {
            return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
        }

        @Override
        public void selectRepresentationTerm(String representationTerm) {
            click(getRepresentationSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[text()=\""+ representationTerm +"\"]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getTableRecordByValue(String value) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//span[contains(text(), \""+value+"\")]/ancestor::tr"));
        }

        @Override
        public String getDefaultValueDomainFieldValue() {
            return getText(getDefaultValueDomainField());
        }

        @Override
        public void showValueDomain() {
            click(getDriver(), getShowValueDomain());
        }

        @Override
        public void setPropertyTerm(String propertyTerm) {
            sendKeys(getPropertyTermField(), propertyTerm);
        }

        @Override
        public void setDefaultValueDomain(String valueDomain) {
            click(getDefaultValueDomainField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(),\"" + valueDomain + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getDefaultValueDomainField() {
            return visibilityOfElementLocated(getDriver(), DEFAULT_VALUE_DOMAIN_SELECT_LOCATOR);
        }

        @Override
        public void setDefinitionSource(String definitionSource) {
            sendKeys(getDefinitionSourceField(), definitionSource);
        }

        @Override
        public String getRepresentationSelectFieldValue() {
            return getText(getRepresentationSelectField());
        }
    }
}
