package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class BCCPViewEditPageImpl extends BasePageImpl implements BCCPViewEditPage {

    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");

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
    public static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");

    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");

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
        assert "BCCP".equals(getBCCPPanel());
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
    public WebElement getNodeByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        return getNodeByName(nodes[nodes.length - 1]);
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
    public BCCPPanel getBCCPPanel() {
        return getBCCPPanel(null);
    }

    @Override
    public BCCPPanel getBCCPPanel(WebElement bccpNode) {
        return retry(() -> {
            if (bccpNode != null) {
                click(bccpNode);
                waitFor(ofMillis(500L));
            }
            return new BCCPPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][2]");
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
        public String getPropertyTermFieldLabel() {
            return getText(getPropertyTermField().findElement(By.xpath("parent::div//label")));
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

    @Override
    public void hitAmendButton() {
        click(elementToBeClickable(getDriver(), AMEND_BUTTON_LOCATOR));
        click(elementToBeClickable(getDriver(), CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR));
    }
}
