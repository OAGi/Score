package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.BCCPChangeBDTDialog;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class BCCPViewEditPageImpl extends BasePageImpl implements BCCPViewEditPage {

    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");
    private static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    private static final By RESTORE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Restore\")]//ancestor::button[1]");
    private static final By CHANGE_BDT_OPTION_LOCATOR =
            By.xpath("//button/span[contains(text(), \"Change BDT\")]");
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
    private static final By PROPERTY_TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Property Term\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Namespace\")]//ancestor::mat-form-field//mat-select");
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By DEN_COMPONENT_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"DEN\")]//ancestor::mat-form-field");
    private static final By PROPERTY_TERM_COMPONENT_LOCATOR =
            By.xpath("//span[contains(text(), \"Property Term\")]//ancestor::label");
    public static final By REVISE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]");
    public static final By CONTINUE_REVISE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Revise\")]//ancestor::button/span");
    public static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");
    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");
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

    private final BCCPObject bccp;

    public BCCPViewEditPageImpl(BasePage parent, BCCPObject bccp) {
        super(parent);
        this.bccp = bccp;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component/bccp/" + this.bccp.getBccpManifestId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        invisibilityOfLoadingContainerElement(getDriver());
        assert "BCCP".equals(getText(getBCCPPanelContainer().getBCCPPanel().getCoreComponentField()));
        assert getText(getTitle()).equals(bccp.getDen());
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.cssSelector("div.mat-tab-list div.mat-tab-label"));
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
    public WebElement getMoveToProduction(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        }
    }

    @Override
    public WebElement getNodeByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        return getNodeByName(nodes[nodes.length - 1]);
    }

    @Override
    public WebElement getContextMenuIconByNodeName(String nodeName) {
        return elementToBeClickable(getDriver(), By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]" +
                        "//mat-icon[contains(text(), \"more_vert\")]"));
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
    public BCCPChangeBDTDialog openChangeBDTDialog() {
        return retry(() -> {
            String propertyTerm = getText(getBCCPPanelContainer().getBCCPPanel().getPropertyTermField());
            WebElement node = clickOnDropDownMenuByPath("/" + propertyTerm);
            try {
                click(visibilityOfElementLocated(getDriver(), CHANGE_BDT_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(visibilityOfElementLocated(getDriver(), CHANGE_BDT_OPTION_LOCATOR));
            }
            waitFor(ofMillis(500L));
            BCCPChangeBDTDialog bccpChangeBDTDialog = new BCCPChangeBDTDialogImpl(this);
            assert bccpChangeBDTDialog.isOpened();
            return bccpChangeBDTDialog;
        });
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
        retry(() -> click(getUpdateButton(true)));
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Updated".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void hitCancelButton() {
        retry(() -> {
            click(getCancelButton());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//score-confirm-dialog//span[contains(text(), \"Okay\")]//ancestor::button[1]")));
        });
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
        retry(() -> {
            click(getRestoreButton());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//score-confirm-dialog//span[contains(text(), \"Restore\")]//ancestor::button[1]")));
        });
        invisibilityOfLoadingContainerElement(getDriver());
        assert "Restored".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public BCCPPanelContainer getBCCPPanelContainer() {
        return getBCCPPanelContainer(null);
    }

    @Override
    public BCCPPanelContainer getBCCPPanelContainer(WebElement bccpNode) {
        return retry(() -> {
            if (bccpNode != null) {
                click(bccpNode);
                waitFor(ofMillis(500L));
            }
            return new BCCPPanelContainer() {
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
                @Override
                public DTPanel getDTPanel() {
                    return new DTPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][3]");
                }
            };
        });
    }

    private WebElement getInputFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/input"));
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
        public void setPropertyTerm(String propertyTerm) {
            sendKeys(getPropertyTermField(), propertyTerm);
        }

        @Override
        public String getPropertyTermFieldLabel() {
            return getText(getPropertyTermField().findElement(By.xpath("parent::div//label")));
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
        public WebElement getValueConstraintSelectField() {
            return getSelectFieldByName(baseXPath, "Value Constraint");
        }

        @Override
        public void setValueConstraint(String value) {
            click(getValueConstraintSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + value + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getFixedValueField() {
            return getInputFieldByName(baseXPath, "Fixed Value");
        }

        @Override
        public void setFixedValue(String fixedValue) {
            sendKeys(getFixedValueField(), fixedValue);
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
        public WebElement getDeprecatedCheckbox() {
            return getCheckboxByName(baseXPath, "Deprecated");
        }

        @Override
        public WebElement getNamespaceSelectField() {
            return getSelectFieldByName(baseXPath, "Namespace");
        }

        @Override
        public void setNamespace(String namespace) {
            click(getNamespaceSelectField());
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
