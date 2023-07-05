package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ACCExtensionViewEditPageImpl extends BasePageImpl implements ACCExtensionViewEditPage {

    public static final By CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");
    public static final By CONTINUE_DELETE_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Delete anyway\")]//ancestor::button/span");
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
            By.xpath("//span[contains(text(), \"Object Class Term\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Namespace\")]//ancestor::mat-form-field//mat-select");
    private static final By DEFINITION_SOURCE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition Source\")]//ancestor::mat-form-field//input");
    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//textarea");
    private static final By SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");
    private static final By APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Append Property at Last\")]");
    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By DELETE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");
    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");
    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");
    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");
    private static final By AMEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]");

    public ACCExtensionViewEditPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component/extension").toString();
    }

    @Override
    public boolean isOpened() {
        invisibilityOfLoadingContainerElement(getDriver());
        return super.isOpened();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "ACC".equals(getCoreComponentTypeFieldValue());
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.cssSelector("mat-tab-header div.mat-tab-label"));
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
    public String getObjectClassTermFieldLabel() {
        return getObjectClassTermField().getAttribute("data-placeholder");
    }

    @Override
    public String getDENFieldLabel() {
        return visibilityOfElementLocated(getDriver(), DEN_COMPONENT_LOCATOR).findElement(By.tagName("mat-label")).getText();
    }

    @Override
    public String getObjectClassTermFieldValue() {
        return getText(getObjectClassTermField());
    }

    @Override
    public WebElement getNamespaceField() {
        return elementToBeClickable(getDriver(), NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public String getNamespaceFieldValue() {
        return getText(getNamespaceField());
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
    public void setDefinition(String definition) {
        clear(getDefinitionField());
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public void hitDeleteButton() {
        retry(() -> click(getDeleteButton(true)));
        invisibilityOfLoadingContainerElement(getDriver());
        click(elementToBeClickable(getDriver(), CONTINUE_DELETE_BUTTON_IN_DIALOG_LOCATOR));
        waitFor(ofMillis(1000L));
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
    public WebElement getNodeByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        return getNodeByName(nodes[nodes.length - 1]);
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
    public SelectAssociationDialog appendPropertyAtLast(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(visibilityOfElementLocated(getDriver(), APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(visibilityOfElementLocated(getDriver(), APPEND_PROPERTY_AT_LAST_OPTION_LOCATOR));
        }
        SelectAssociationDialog selectAssociationDialog =
                new SelectAssociationDialogImpl(this, "Append Property at Last");
        assert selectAssociationDialog.isOpened();
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
    public WebElement getSearchField() {
        return visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR);
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
        waitFor(ofMillis(1000L));
        assert "Updated".equals(getSnackBarMessage(getDriver()));
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
    public WebElement getAmendButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), AMEND_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), AMEND_BUTTON_LOCATOR);
        }
    }

    @Override
    public void hitAmendButton() {
        click(getAmendButton(true));
        click(elementToBeClickable(getDriver(), CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(Duration.ofMillis(500L));
        assert "Amended".equals(getSnackBarMessage(getDriver()));
    }

    private WebElement getInputFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/input"));
    }

    @Override
    public ACCPanel getACCPanel(WebElement accNode) {
        return retry(() -> {
            click(accNode);
            waitFor(ofMillis(500L));
            return new ACCPanelImpl("//div[contains(@class, \"cc-node-detail-panel\")][1]");
        });
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

        private WebElement getCheckboxByName(String baseXPath, String name) {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[1]"));
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
        public WebElement getDefinitionField() {
            return getTextAreaFieldByName(baseXPath, "Definition");
        }

        private WebElement getTextAreaFieldByName(String baseXPath, String name) {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/textarea"));
        }

        private WebElement getSelectFieldByName(String baseXPath, String name) {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/mat-select"));
        }

        private WebElement getInputFieldByName(String baseXPath, String name) {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/input"));
        }
    }


}
