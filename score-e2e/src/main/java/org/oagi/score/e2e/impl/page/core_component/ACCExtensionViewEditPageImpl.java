package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ACCExtensionViewEditPageImpl extends BasePageImpl implements ACCExtensionViewEditPage {

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
        return visibilityOfElementLocated(getDriver(), NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public String getNamespaceFieldValue() {
        return getText(getNamespaceField());
    }

    @Override
    public void setNamespace(NamespaceObject namespace) {
        click(getNamespaceField());
        WebElement option = elementToBeClickable(getDriver(), By.xpath(
                "//span[contains(text(), \"" + namespace.getUri() + "\")]//ancestor::mat-option"));
        click(option);
        assert getNamespaceFieldValue().equals(namespace.getUri());
    }

    @Override
    public void setDefinition(String definition) {
        clear(getDefinitionField());
        sendKeys(getDefinitionField(), definition);
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
    }

    @Override
    public WebElement getAmendButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), AMEND_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), AMEND_BUTTON_LOCATOR);
        }
    }
}
