package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.code_list.AddCommentDialogImpl;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.LogObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.math.BigInteger;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ASCCPViewEditPageImpl extends BasePageImpl implements ASCCPViewEditPage {

    public static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");
    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");
    public static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    public static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");
    public static final By CONFIRM_CANCEL_REVISION_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Okay\")]//ancestor::button/span");
    public static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    public static final By CONFIRM_DELETE_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Delete anyway\")]//ancestor::button/span");
    public static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    public static final By CONFIRM_RESTORE_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Restore\")]//ancestor::button/span");
    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");
    private static final By MOVE_TO_DRAFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
    private static final By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");
    private static final By DEPRECATED_CHECKBOX_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-checkbox");
    private static final By OPEN_IN_NEW_TAB_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Open in new tab\")]");
    private static final By CHANGE_ACC_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Change ACC\")]");
    private static final By COMMENTS_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Comments\")]");
    private static final By SHOW_HISTORY_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Show History\")]");
    private static final By PROPERTY_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Property Term\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");

    private final ASCCPObject asccp;

    public ASCCPViewEditPageImpl(BasePage parent, ASCCPObject asccp) {
        super(parent);
        this.asccp = asccp;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component/asccp/" + this.asccp.getAsccpManifestId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert "ASCCP".equals(getText(getASCCPPanel().getCoreComponentField()));
        assert getText(getTitle()).startsWith(asccp.getPropertyTerm());
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.cssSelector("div.mat-tab-list div.mat-tab-label-content"));
    }

    @Override
    public WebElement getSearchInputTextField() {
        return visibilityOfElementLocated(getDriver(), SEARCH_INPUT_TEXT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
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
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void hitCancelButton() {
        click(getCancelButton());
        click(elementToBeClickable(getDriver(), CONFIRM_CANCEL_REVISION_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Canceled".equals(getSnackBarMessage(getDriver()));
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
    public WebElement getRestoreButton() {
        return elementToBeClickable(getDriver(), RESTORE_BUTTON_LOCATOR);
    }

    @Override
    public void hitRestoreButton() {
        click(getRestoreButton());
        click(elementToBeClickable(getDriver(), CONFIRM_RESTORE_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Restored".equals(getSnackBarMessage(getDriver()));
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
    public void moveToQA() {
        click(getMoveToQAButton(true));
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
    public void moveToProduction() {
        click(getMoveToProduction(true));
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
    public WebElement getMoveToQAButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        }
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
    public void toggleDeprecated() {
        click(getDeprecatedCheckbox());
    }

    @Override
    public WebElement getDeprecatedCheckbox() {
        return elementToBeClickable(getDriver(), DEPRECATED_CHECKBOX_LOCATOR);
    }

    @Override
    public ACCViewEditPage openACCInNewTab(WebElement accNode) {
        try {
            click(visibilityOfElementLocated(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(accNode);
            new Actions(getDriver()).sendKeys("O").perform();
            click(visibilityOfElementLocated(getDriver(), OPEN_IN_NEW_TAB_OPTION_LOCATOR));
        }

        switchToNextTab(getDriver());
        String url = getDriver().getCurrentUrl();
        int idx = url.lastIndexOf("/");

        BigInteger manifestId = new BigInteger(url.substring(idx + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(manifestId);
        ACCViewEditPage accViewEditPage = new ACCViewEditPageImpl(this, acc);
        assert accViewEditPage.isOpened();
        return accViewEditPage;
    }

    @Override
    public ASCCPChangeACCDialog openChangeACCDialog(String path) {
        return retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(visibilityOfElementLocated(getDriver(), CHANGE_ACC_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(visibilityOfElementLocated(getDriver(), CHANGE_ACC_OPTION_LOCATOR));
            }
            waitFor(ofMillis(500L));
            ASCCPChangeACCDialog asccpChangeACCDialog =
                    new ASCCPChangeACCDialogImpl(this);
            assert asccpChangeACCDialog.isOpened();
            return asccpChangeACCDialog;
        });
    }

    @Override
    public WebElement getNodeByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        return getNodeByName(nodes[nodes.length - 1]);
    }

    @Override
    public SelectAssociationDialog changeACC(String path) {

        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(visibilityOfElementLocated(getDriver(), CHANGE_ACC_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(visibilityOfElementLocated(getDriver(), CHANGE_ACC_OPTION_LOCATOR));
        }
        SelectAssociationDialog selectAssociationDialog =
                new SelectAssociationDialogImpl(this, "Change ACC");
        //assert selectAssociationDialog.isOpened();
        return selectAssociationDialog;
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

    @Override
    public WebElement getContextMenuIconByNodeName(String nodeName) {
        WebElement node = getNodeByName(nodeName);
        return node.findElement(By.xpath("//mat-icon[contains(text(), \"more_vert\")]"));
    }

    private WebElement goToNode(String path) {
        click(getSearchInputTextField());
        WebElement node = sendKeys(visibilityOfElementLocated(getDriver(), SEARCH_INPUT_TEXT_FIELD_LOCATOR), path);
        node.sendKeys(Keys.ENTER);
        click(node);
        clear(getSearchInputTextField());
        return node;
    }

    private WebElement getNodeByName(String nodeName) {
        By nodeLocator = By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]");
        return visibilityOfElementLocated(getDriver(), nodeLocator);
    }

    @Override
    public ASCCPPanel getASCCPPanel() {
        return getASCCPPanel(null);
    }

    @Override
    public ASCCPPanel getASCCPPanel(WebElement asccpNode) {
        return retry(() -> {
            if (asccpNode != null) {
                click(asccpNode);
                waitFor(ofMillis(500L));
            }
            return new ASCCPPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][2]");
        });
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
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDefinitionSourceField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_SOURCE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public WebElement getPropertyTermField() {
        return visibilityOfElementLocated(getDriver(), PROPERTY_TERM_FIELD_LOCATOR);
    }

    @Override
    public AddCommentDialog openCommentsDialog(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(visibilityOfElementLocated(getDriver(), COMMENTS_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("C").perform();
        }

        AddCommentDialog addCodeListCommentDialog = new AddCommentDialogImpl(this);
        assert addCodeListCommentDialog.isOpened();
        return addCodeListCommentDialog;
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

                @Override
                public DTPanel getDTPanel() {
                    return new DTPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][3]");
                }
            };
        });
    }

    @Override
    public HistoryPage showHistory() {
        String path = "/" + this.asccp.getPropertyTerm();
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            retry(() -> click(visibilityOfElementLocated(getDriver(), SHOW_HISTORY_OPTION_LOCATOR)));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            retry(() -> click(visibilityOfElementLocated(getDriver(), SHOW_HISTORY_OPTION_LOCATOR)));
        }
        switchToNextTab(getDriver());

        LogObject logObject = new LogObject();
        logObject.setReference(this.asccp.getGuid());
        logObject.setType("ASCCP");
        logObject.setManifestId(this.asccp.getAsccpManifestId());

        HistoryPage historyPage = new HistoryPageImpl(this, logObject);
        assert historyPage.isOpened();
        return historyPage;
    }

    private WebElement getInputFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/input"));
    }

    private WebElement getSelectFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/mat-select"));
    }

    private WebElement getAlternativeSelectFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]"));
    }

    private WebElement getCheckboxByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[1]"));
    }

    private WebElement getTextAreaFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/textarea"));
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
        public WebElement getComponentTypeSelectField() {
            return getSelectFieldByName(baseXPath, "Component Type");
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
        public WebElement getDefinitionSourceField() {
            return getInputFieldByName(baseXPath, "Definition Source");
        }

        @Override
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
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
        public WebElement getCardinalityMaxField() {
            return getInputFieldByName(baseXPath, "Cardinality Max");
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
        public void setPropertyTerm(String propertyTerm) {
            sendKeys(getPropertyTermField(), propertyTerm);
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
        public void toggleReusable() {
            click(getReusableCheckbox());
        }

        @Override
        public WebElement getNillableCheckbox() {
            return getCheckboxByName(baseXPath, "Nillable");
        }

        @Override
        public void toggleNillable() {
            click(getNillableCheckbox());
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
            try {
                click(getNamespaceSelectField());
            } catch (ElementClickInterceptedException e) {
                click(getAlternativeSelectFieldByName(baseXPath, "Namespace"));
            }
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + namespace + "\")]//ancestor::mat-option"));
            click(option);
            waitFor(ofMillis(1000L));
            assert getText(getNamespaceSelectField()).equals(namespace);
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
        public WebElement getCardinalityMinField() {
            return getInputFieldByName(baseXPath, "Cardinality Min");
        }

        @Override
        public WebElement getCardinalityMaxField() {
            return getInputFieldByName(baseXPath, "Cardinality Max");
        }

        @Override
        public WebElement getEntityTypeSelectField() {
            return getSelectFieldByName(baseXPath, "Entity Type");
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
        public WebElement getFixedValueField() {
            return getInputFieldByName(baseXPath, "Fixed Value");
        }

        @Override
        public WebElement getDefaultValueField() {
            return getInputFieldByName(baseXPath, "Default Value");
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
    }

    private class DTPanelImpl implements DTPanel {

        private final String baseXPath;

        private DTPanelImpl(String baseXPath) {
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
        public WebElement getDataTypeTermField() {
            return getInputFieldByName(baseXPath, "Data Type Term");
        }

        @Override
        public WebElement getQualifierField() {
            return getInputFieldByName(baseXPath, "Qualifier");
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
}
