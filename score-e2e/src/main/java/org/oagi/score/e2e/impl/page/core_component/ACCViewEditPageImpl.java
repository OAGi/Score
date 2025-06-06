package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.code_list.AddCommentDialogImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.math.BigInteger;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ACCViewEditPageImpl extends BasePageImpl implements ACCViewEditPage {

    public static final By DEN_COMPONENT_LOCATOR_FOR_ASCC =
            By.xpath("//mat-label[contains(text(), \"Core Component\")]//ancestor::mat-form-field//input[@Value=\"ASCC\"]" +
                    "//ancestor::mat-tab-group//descendant::mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field");
    public static final By DEN_COMPONENT_LOCATOR_FOR_BCC =
            By.xpath("//mat-label[contains(text(), \"Core Component\")]//ancestor::mat-form-field//input[@Value=\"BCC\"]" +
                    "//ancestor::mat-tab-group//descendant::mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field");
    public static final By DEN_COMPONENT_LOCATOR_FOR_DT =
            By.xpath("//mat-label[contains(text(), \"Core Component\")]//ancestor::mat-form-field//input[@Value=\"DT\"]" +
                    "//ancestor::mat-tab-group//descendant::mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field");
    public static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");
    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button");
    public static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    public static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    public static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button");

    public static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");
    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-form-field//input[@type=\"search\"]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");
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
    private static final By GUID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"GUID\")]//ancestor::mat-form-field//input");
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");
    private static final By DEN_COMPONENT_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field");
    private static final By OBJECT_CLASS_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Object Class Term\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Namespace\")]//ancestor::mat-form-field//mat-select");
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//textarea[@placeholder=\"Definition\"]");
    private static final By CARDINALITY_COMPONENT_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Cardinality Max\")]//ancestor::mat-form-field//input");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");

    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");

    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By MOVE_TO_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");

    private static final By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");

    private static final By SET_BASE_ACC_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Set Base ACC\")]//ancestor::button[1]");

    private static final By APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Append Property at Last\")]//ancestor::button[1]");

    private static final By OPEN_IN_NEW_TAB_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Open in new tab\")]//ancestor::button[1]");

    private static final By WHERE_USED_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Where Used\")]//ancestor::button[1]");

    private static final By CREATE_ASCCP_FROM_THIS_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create ASCCP from this\")]//ancestor::button[1]");

    private static final By COMMENTS_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Comments\")]//ancestor::button[1]");

    private static final By SHOW_HISTORY_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Show History\")]//ancestor::button[1]");

    private static final By CREATE_OAGI_EXTENSION_COMPONENT_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create OAGi Extension Component\")]//ancestor::button[1]");

    private static final By INSERT_PROPERTY_BEFORE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Insert Property Before\")]//ancestor::button[1]");

    private static final By INSERT_PROPERTY_AFTER_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Insert Property After\")]//ancestor::button[1]");

    private static final By REFACTOR_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Refactor\")]//ancestor::button[1]");

    private static final By REFACTOR_TO_BASE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Refactor to Base\")]//ancestor::button[1]");

    private static final By UNGROUP_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Ungroup\")]//ancestor::button[1]");

    private static final By REMOVE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]");
    private static final By DELETE_OPTION_LOCATOR =
            By.xpath("//div[contains(@class, \"cdk-overlay-container\")]//span[contains(text(),\"Delete\")]");

    private final ACCObject acc;

    public ACCViewEditPageImpl(BasePage parent, ACCObject acc) {
        super(parent);
        this.acc = acc;
    }

    @Override
    protected String getPageUrl() {
        if (this.acc.isLocalExtension()) {
            return getConfig().getBaseUrl().resolve("/core_component/extension/" + this.acc.getAccManifestId()).toString();
        } else {
            return getConfig().getBaseUrl().resolve("/core_component/acc/" + this.acc.getAccManifestId()).toString();
        }
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofSeconds(2L));
        assert "ACC".equals(getCoreComponentTypeFieldValue());
        assert getText(getTitle()).equals(acc.getDen());
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-tab-header//div[@class=\"mat-mdc-tab-labels\"]/div[contains(@class, \"mdc-tab\")][1]"));
    }

    @Override
    public WebElement getSearchInputTextField() {
        return elementToBeClickable(getDriver(), SEARCH_INPUT_TEXT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
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
    public WebElement getObjectClassTermField() {
        return visibilityOfElementLocated(getDriver(), OBJECT_CLASS_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setObjectClassTerm(String objectClassTerm) {
        sendKeys(getObjectClassTermField(), objectClassTerm);
    }

    @Override
    public String getObjectClassTermFieldLabel() {
        return getObjectClassTermField().getAttribute("placeholder");
    }

    @Override
    public String getDENFieldLabel() {
        return visibilityOfElementLocated(getDriver(), DEN_COMPONENT_LOCATOR).findElement(By.tagName("mat-label")).getText();
    }

    @Override
    public String getDenFieldLabelForASCC() {
        return visibilityOfElementLocated(getDriver(), DEN_COMPONENT_LOCATOR_FOR_ASCC).findElement(By.tagName("mat-label")).getText();
    }

    @Override
    public String getDenFieldLabelForBCC() {
        return visibilityOfElementLocated(getDriver(), DEN_COMPONENT_LOCATOR_FOR_BCC).findElement(By.tagName("mat-label")).getText();
    }

    @Override
    public String getDENFieldLabelDT() {
        return visibilityOfElementLocated(getDriver(), DEN_COMPONENT_LOCATOR_FOR_DT).findElement(By.tagName("mat-label")).getText();
    }

    @Override
    public String getObjectClassTermFieldValue() {
        return getText(getObjectClassTermField());
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
    public WebElement getContextMenuIconByNodeName(String nodeName) {
        WebElement node = getNodeByName(nodeName);
        return getContextMenuIcon(node);
    }

    public WebElement getContextMenuIcon(WebElement node) {
        return node.findElement(By.xpath("//mat-icon[contains(text(), \"more_vert\")]"));
    }

    @Override
    public WebElement clickOnDropDownMenuByPath(String path) {
        WebElement node = goToNode(path);

        click(node);
        new Actions(getDriver()).sendKeys("O").perform();
        waitFor(ofMillis(1000L));
        try {
            if (visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed()) {
                return node;
            }
        } catch (WebDriverException ignore) {
        }

        WebElement contextMenuIcon = getContextMenuIcon(node);
        click(getDriver(), contextMenuIcon);
        waitFor(ofMillis(1000L));
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed();
        return node;
    }

    @Override
    public boolean isDeleted(WebElement node) {
        try {
            WebElement elm = node.findElement(By.xpath(
                    "//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]"));
            return elm != null && elm.isDisplayed();
        } catch (WebDriverException e) {
            return false;
        }
    }

    @Override
    public ACCSetBaseACCDialog setBaseACC(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), SET_BASE_ACC_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), SET_BASE_ACC_OPTION_LOCATOR));
        }
        ACCSetBaseACCDialog accSetBaseACCDialog =
                new ACCSetBaseACCDialogImpl(this);
        assert accSetBaseACCDialog.isOpened();
        return accSetBaseACCDialog;
    }

    @Override
    public SelectAssociationDialog appendPropertyAtLast(String path) {
        waitFor(ofMillis(1000L));
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR));
        } catch (WebDriverException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR));
        }
        SelectAssociationDialog selectAssociationDialog =
                new SelectAssociationDialogImpl(this, "Append Property at Last");
        assert selectAssociationDialog.isOpened();
        return selectAssociationDialog;
    }

    @Override
    public BCCPViewEditPage openBCCPInNewTab(WebElement bccNode) {
        try {
            click(elementToBeClickable(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(bccNode);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        }

        switchToNextTab(getDriver());
        String url = getDriver().getCurrentUrl();
        int idx = url.lastIndexOf("/");

        BigInteger manifestId = new BigInteger(url.substring(idx + 1));
        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(manifestId);
        BCCPViewEditPage bccpViewEditPage = new BCCPViewEditPageImpl(this, bccp);
        assert bccpViewEditPage.isOpened();
        return bccpViewEditPage;
    }

    @Override
    public ASCCPViewEditPage openASCCPInNewTab(WebElement accNode) {
        try {
            click(elementToBeClickable(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(accNode);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        }

        switchToNextTab(getDriver());
        String url = getDriver().getCurrentUrl();
        int idx = url.lastIndexOf("/");

        BigInteger manifestId = new BigInteger(url.substring(idx + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(manifestId);
        ASCCPViewEditPage asccpViewEditPage = new ASCCPViewEditPageImpl(this, asccp);
        assert asccpViewEditPage.isOpened();
        return asccpViewEditPage;
    }

    @Override
    public FindWhereUsedDialog findWhereUsed(String path) {
        clickOnDropDownMenuByPath(path);
        WebElement whereUsedMenu = elementToBeClickable(getDriver(), WHERE_USED_OPTION_LOCATOR);
        click(getDriver(), whereUsedMenu);
        waitFor(ofMillis(1000L));
        FindWhereUsedDialog findWhereUsedDialog =
                new FindWhereUsedDialogImpl(this, "Where Used");
        assert findWhereUsedDialog.isOpened();
        return findWhereUsedDialog;
    }

    @Override
    public WebElement createASCCPfromThis(String path) {
        WebElement confirmDialog;
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            confirmDialog = click(elementToBeClickable(getDriver(), CREATE_ASCCP_FROM_THIS_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            confirmDialog = click(elementToBeClickable(getDriver(), CREATE_ASCCP_FROM_THIS_OPTION_LOCATOR));
        }
        return confirmDialog;

    }

    @Override
    public void deleteBaseACC(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), DELETE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), DELETE_OPTION_LOCATOR));
        }
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]")).isDisplayed();

        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Delete anyway\")]//ancestor::button[1]")));
    }

    @Override
    public ACCViewEditPage createOAGiExtensionComponent(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), CREATE_OAGI_EXTENSION_COMPONENT_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), CREATE_OAGI_EXTENSION_COMPONENT_OPTION_LOCATOR));
        }
        assert this.isOpened();
        return this;
    }

    @Override
    public SelectAssociationDialog insertPropertyBefore(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), INSERT_PROPERTY_BEFORE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), INSERT_PROPERTY_BEFORE_OPTION_LOCATOR));
        }
        SelectAssociationDialog selectAssociationDialog =
                new SelectAssociationDialogImpl(this, "Insert Property Before");
        assert selectAssociationDialog.isOpened();
        return selectAssociationDialog;
    }

    @Override
    public SelectAssociationDialog insertPropertyAfter(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), INSERT_PROPERTY_AFTER_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), INSERT_PROPERTY_AFTER_OPTION_LOCATOR));
        }
        SelectAssociationDialog selectAssociationDialog =
                new SelectAssociationDialogImpl(this, "Insert Property After");
        assert selectAssociationDialog.isOpened();
        return selectAssociationDialog;
    }

    @Override
    public SelectBaseACCToRefactorDialog refactorToBaseACC(String path, String associationPropertyTerm) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), REFACTOR_OPTION_LOCATOR));
            click(getDriver(), elementToBeClickable(getDriver(), REFACTOR_TO_BASE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), REFACTOR_OPTION_LOCATOR));
            click(getDriver(), elementToBeClickable(getDriver(), REFACTOR_TO_BASE_OPTION_LOCATOR));
        }
        SelectBaseACCToRefactorDialog selectBaseACCToRefactorDialog = new SelectBaseACCToRefactorDialogImpl(this,
                associationPropertyTerm);
        assert selectBaseACCToRefactorDialog.isOpened();
        return selectBaseACCToRefactorDialog;
    }

    @Override
    public void unGroup(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), REFACTOR_OPTION_LOCATOR));
            click(elementToBeClickable(getDriver(), UNGROUP_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), REFACTOR_OPTION_LOCATOR));
            click(elementToBeClickable(getDriver(), UNGROUP_OPTION_LOCATOR));
        }
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//score-confirm-dialog//div[contains(@class, \"header\")]")).isDisplayed();

        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Ungroup anyway\")]//ancestor::button[1]")));
        assert "Ungrouped".equals(getSnackBarMessage(getDriver()));

    }

    @Override
    public ACCViewEditPage removeAssociation(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), REMOVE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), REMOVE_OPTION_LOCATOR));
        }
        assert visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]")).isDisplayed();

        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Remove anyway\")]//ancestor::button[1]")));
        assert this.isOpened();
        return this;
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void hitCancelButton() {
        click(getCancelButton());
        click(elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//span[contains(text(), \"Okay\")]//ancestor::button")));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Canceled".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getDeleteButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), DELETE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), DELETE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void hitDeleteButton() {
        retry(() -> {
            click(getDeleteButton(true));
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//score-confirm-dialog//span[contains(text(), \"Delete anyway\")]//ancestor::button[1]")));
        });
        assert "Deleted".equals(getSnackBarMessage(getDriver()));

    }

    @Override
    public WebElement getRestoreButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), RESTORE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), RESTORE_BUTTON_LOCATOR);
        }
    }

    @Override
    public void hitRestoreButton() {
        retry(() -> {
            click(getRestoreButton(true));
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//score-confirm-dialog//span[contains(text(), \"Restore\")]//ancestor::button[1]")));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Restored".equals(getSnackBarMessage(getDriver()));

    }

    @Override
    public WebElement getReviseButton() {
        return elementToBeClickable(getDriver(), REVISE_BUTTON_LOCATOR);
    }

    @Override
    public void hitReviseButton() {
        click(getReviseButton());
        click(elementToBeClickable(getDriver(), CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Revised".equals(getSnackBarMessage(getDriver()));
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
    public void expandTree(String nodeName) {
        try {
            By chevronRightLocator = By.xpath(
                    "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[1]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]");
            click(elementToBeClickable(getDriver(), chevronRightLocator));
        } catch (TimeoutException maybeAlreadyExpanded) {
        }

        By expandMoreLocator = By.xpath(
                "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[1]//mat-icon[contains(text(), \"expand_more\")]//ancestor::button[1]");
        assert elementToBeClickable(getDriver(), expandMoreLocator).isEnabled();
    }

    private WebElement goToNode(String path) {
        WebElement searchInput = getSearchInputTextField();
        click(getDriver(), searchInput);
        retry(() -> {
            sendKeys(searchInput, path);
            if (!path.equals(getText(searchInput))) {
                throw new WebDriverException();
            }
        });
        searchInput.sendKeys(Keys.ENTER);
        searchInput.sendKeys("");
        clear(searchInput);

        String[] nodes = path.split("/");
        String nodeName = nodes[nodes.length - 1];
        return getNodeByName(nodeName);
    }

    private WebElement getNodeByName(String nodeName) {
        By nodeLocator = By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]");
        return visibilityOfElementLocated(getDriver(), nodeLocator);
    }

    @Override
    public String getCardinalityLabel() {
        return visibilityOfElementLocated(getDriver(), CARDINALITY_COMPONENT_LOCATOR).getAttribute("placeholder");
    }

    @Override
    public void moveToProduction() {
        click(getMoveToProduction(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
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
    public WebElement getMoveToProduction(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        }
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
    public void backToWIP() {
        click(getBackToWIPButton(true));
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
    public WebElement getCardinalityMaxField() {
        return getInputFieldByName("Cardinality Max");
    }

    @Override
    public void setCardinalityMax(int cardinalityMax) {
        sendKeys(getCardinalityMaxField(), Integer.toString(cardinalityMax));
    }

    @Override
    public WebElement getNodeByPath(String path) {
        return retry(() -> goToNode(path));
    }

    @Override
    public ACCPanel getACCPanel(WebElement accNode) {
        return retry(() -> {
            click(accNode);
            waitFor(ofMillis(500L));
            return new ACCPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][1]");
        });
    }

    @Override
    public ASCCPanelContainer getASCCPanelContainer(WebElement asccNode) {
        return retry(() -> {
            click(asccNode);
            waitFor(ofMillis(500L));
            return new ASCCPanelContainer() {
                @Override
                public ASCCPanel getASCCPanel() {
                    return new ASCCPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][1]");
                }

                @Override
                public ASCCPPanel getASCCPPanel() {
                    return new ASCCPPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][2]");
                }
            };
        });
    }

    @Override
    public BCCPanelContainer getBCCPanelContainer(WebElement bccNode) {
        return retry(() -> {
            click(bccNode);
            waitFor(ofMillis(500L));
            return new BCCPanelContainer() {
                @Override
                public BCCPanel getBCCPanel() {
                    return new BCCPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][1]");
                }

                @Override
                public BCCPPanel getBCCPPanel() {
                    return new BCCPPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][2]");
                }
            };
        });
    }

    @Override
    public AddCommentDialog openCommentsDialog(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), COMMENTS_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("C").perform();
        }

        AddCommentDialog addCodeListCommentDialog = new AddCommentDialogImpl(this);
        assert addCodeListCommentDialog.isOpened();
        return addCodeListCommentDialog;
    }

    private WebElement getInputFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]//input"));
    }

    private WebElement getSelectFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/mat-select"));
    }

    private WebElement getCheckboxByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[1]"));
    }

    private WebElement getTextAreaFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//textarea[@placeholder=\"" + name + "\"]"));
    }

    private WebElement getInputFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]//input"));
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> {
            click(getUpdateButton(true));
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
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
    public HistoryPage showHistory() {
        String path = "/" + this.acc.getDen();
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            retry(() -> click(elementToBeClickable(getDriver(), SHOW_HISTORY_OPTION_LOCATOR)));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            retry(() -> click(elementToBeClickable(getDriver(), SHOW_HISTORY_OPTION_LOCATOR)));
        }
        switchToNextTab(getDriver());

        LogObject logObject = new LogObject();
        logObject.setReference(this.acc.getGuid());
        logObject.setType("ACC");
        logObject.setManifestId(this.acc.getAccManifestId());

        HistoryPage historyPage = new HistoryPageImpl(this, logObject);
        assert historyPage.isOpened();
        return historyPage;
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
    public void moveToCandidate() {
        click(getMoveToCandidate(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
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
    public WebElement getMoveToCandidate(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_CANDIDATE_BUTTON_LOCATOR);
        }
    }

    private class ACCPanelImpl implements ACCPanel {

        private final String baseXPath;

        public ACCPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

        @Override
        public WebElement getCoreComponentField() {
            return getInputFieldByName(baseXPath, "Core Component");
        }

        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName(baseXPath, "Release");
        }

        @Override
        public WebElement getRevisionField() {
            return getInputFieldByName(baseXPath, "Revision");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(baseXPath, "Owner");
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName(baseXPath, "GUID");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName(baseXPath, "DEN");
        }

        @Override
        public WebElement getObjectClassTermField() {
            return getInputFieldByName(baseXPath, "Object Class Term");
        }

        @Override
        public void setObjectClassTerm(String objectClassTerm) {
            sendKeys(getObjectClassTermField(), objectClassTerm);
        }

        @Override
        public WebElement getComponentTypeSelectField() {
            return getSelectFieldByName(baseXPath, "Component Type");
        }

        @Override
        public void setComponentType(String componentType) {
            click(getComponentTypeSelectField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + componentType + "\")]//ancestor::mat-option"));
            click(option);
            waitFor(ofMillis(1000L));
            assert getText(getComponentTypeSelectField()).equals(componentType);
        }

        @Override
        public WebElement getAbstractCheckbox() {
            return getCheckboxByName(baseXPath, "Abstract");
        }

        @Override
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getNamespaceSelectField() {
            return getSelectFieldByName(baseXPath, "Namespace");
        }

        @Override
        public void setNamespace(String namespace) {
            retry(() -> {
                click(getNamespaceSelectField());
                waitFor(ofSeconds(2L));
                WebElement optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//span[contains(text(), \"" + namespace + "\")]//ancestor::mat-option[1]"));
                click(optionField);
            });
        }

        @Override
        public void setNamespace(NamespaceObject namespace) {
            setNamespace(namespace.getUri());
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public void setDefinitionSource(String definitionSource) {
            sendKeys(getDefinitionSourceField(), definitionSource);
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }

        @Override
        public void setDefinition(String definition) {
            sendKeys(getDefinitionField(), definition);
        }
    }

    private class ASCCPanelImpl implements ASCCPanel {

        private final String baseXPath;

        private ASCCPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

        @Override
        public WebElement getCoreComponentField() {
            return getInputFieldByName(baseXPath, "Core Component");
        }

        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName(baseXPath, "Release");
        }

        @Override
        public WebElement getRevisionField() {
            return getInputFieldByName(baseXPath, "Revision");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(baseXPath, "Owner");
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName(baseXPath, "GUID");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName(baseXPath, "DEN");
        }

        @Override
        public WebElement getCardinalityMinField() {
            return getInputFieldByName(baseXPath, "Cardinality Min");
        }

        @Override
        public void setCardinalityMinField(String cardinalityMin) {
            sendKeys(getCardinalityMinField(), cardinalityMin);
        }

        @Override
        public WebElement getCardinalityMaxField() {
            return getInputFieldByName(baseXPath, "Cardinality Max");
        }

        @Override
        public void setCardinalityMaxField(String cardinalityMax) {
            sendKeys(getCardinalityMaxField(), cardinalityMax);
        }

        @Override
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }

        @Override
        public void setDefinition(String newDefinition) {
            sendKeys(getDefinitionField(), newDefinition);
        }
    }

    private class ASCCPPanelImpl implements ASCCPPanel {

        private final String baseXPath;

        private ASCCPPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

        @Override
        public WebElement getCoreComponentField() {
            return getInputFieldByName(baseXPath, "Core Component");
        }

        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName(baseXPath, "Release");
        }

        @Override
        public WebElement getRevisionField() {
            return getInputFieldByName(baseXPath, "Revision");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(baseXPath, "Owner");
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName(baseXPath, "GUID");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName(baseXPath, "DEN");
        }

        @Override
        public String getDENFieldLabel() {
            return getText(getDENField().findElement(By.xpath("parent::div//label")));
        }

        @Override
        public WebElement getPropertyTermField() {
            return getInputFieldByName(baseXPath, "Property Term");
        }

        @Override
        public String getPropertyTermFieldLabel() {
            return getText(getPropertyTermField().findElement(By.xpath("parent::div//label")));
        }

        @Override
        public WebElement getReusableCheckbox() {
            return getCheckboxByName(baseXPath, "Reusable");
        }

        @Override
        public WebElement getNillableCheckbox() {
            return getCheckboxByName(baseXPath, "Nillable");
        }

        @Override
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getNamespaceSelectField() {
            return getSelectFieldByName(baseXPath, "Namespace");
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }
    }

    private class BCCPanelImpl implements BCCPanel {

        private final String baseXPath;

        private BCCPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

        @Override
        public WebElement getCoreComponentField() {
            return getInputFieldByName(baseXPath, "Core Component");
        }

        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName(baseXPath, "Release");
        }

        @Override
        public WebElement getRevisionField() {
            return getInputFieldByName(baseXPath, "Revision");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(baseXPath, "Owner");
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName(baseXPath, "GUID");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName(baseXPath, "DEN");
        }

        @Override
        public WebElement getPropertyTermField() {
            return getInputFieldByName(baseXPath, "Property Term");
        }

        @Override
        public WebElement getCardinalityMinField() {
            return getInputFieldByName(baseXPath, "Cardinality Min");
        }

        @Override
        public void setCardinalityMinField(String cardinalityMin) {
            sendKeys(getCardinalityMinField(), cardinalityMin);
        }

        @Override
        public WebElement getCardinalityMaxField() {
            return getInputFieldByName(baseXPath, "Cardinality Max");
        }

        @Override
        public void setCardinalityMaxField(String cardinalityMax) {
            sendKeys(getCardinalityMaxField(), cardinalityMax);
        }

        @Override
        public WebElement getEntityTypeSelectField() {
            return getSelectFieldByName(baseXPath, "Entity Type");
        }

        @Override
        public void setEntityType(String entityType) {
            click(getEntityTypeSelectField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + entityType + "\")]//ancestor::mat-option"));
            click(option);
            waitFor(ofMillis(1000L));
            assert getText(getEntityTypeSelectField()).contains(entityType);

        }

        @Override
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getValueConstraintSelectField() {
            return getSelectFieldByName(baseXPath, "Value Constraint");
        }

        @Override
        public void setValueConstraint(String valueConstraint) {
            click(getValueConstraintSelectField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueConstraint + "\")]//ancestor::mat-option"));
            click(option);
            waitFor(ofMillis(1000L));
            assert getText(getValueConstraintSelectField()).contains(valueConstraint);
        }

        @Override
        public WebElement getNamespaceSelectField() {
            return getSelectFieldByName(baseXPath, "Namespace");
        }

        @Override
        public WebElement getFixedValueField() {
            return getInputFieldByName(baseXPath, "Fixed Value");
        }

        @Override
        public WebElement getDefaultValueField() {
            return getInputFieldByName(baseXPath, "Default Value");
        }

        @Override
        public void setDefaultValue(String defaultValue) {
            sendKeys(getDefaultValueField(), defaultValue);
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public void setDefinitionSource(String definitionSource) {
            sendKeys(getDefinitionSourceField(), definitionSource);
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }

        @Override
        public void setDefinition(String definition) {
            sendKeys(getDefinitionField(), definition);
        }
    }

    private class BCCPPanelImpl implements BCCPPanel {

        private final String baseXPath;

        private BCCPPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

        @Override
        public WebElement getCoreComponentField() {
            return getInputFieldByName(baseXPath, "Core Component");
        }

        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName(baseXPath, "Release");
        }

        @Override
        public WebElement getRevisionField() {
            return getInputFieldByName(baseXPath, "Revision");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(baseXPath, "Owner");
        }

        @Override
        public WebElement getGUIDField() {
            return getInputFieldByName(baseXPath, "GUID");
        }

        @Override
        public WebElement getDENField() {
            return getInputFieldByName(baseXPath, "DEN");
        }

        @Override
        public WebElement getPropertyTermField() {
            return getInputFieldByName(baseXPath, "Property Term");
        }

        @Override
        public WebElement getNillableCheckbox() {
            return getCheckboxByName(baseXPath, "Nillable");
        }

        @Override
        public WebElement getValueConstraintSelectField() {
            return getSelectFieldByName(baseXPath, "Value Constraint");
        }

        @Override
        public WebElement getFixedValueField() {
            return getInputFieldByName(baseXPath, "Fixed Value");
        }

        @Override
        public WebElement getDefaultValueField() {
            return getInputFieldByName(baseXPath, "Default Value");
        }

        @Override
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getNamespaceSelectField() {
            return getSelectFieldByName(baseXPath, "Namespace");
        }

        @Override
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }

        @Override
        public void setDefinition(String definition) {
            sendKeys(getDefinitionField(), definition);
        }
    }

}
