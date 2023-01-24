package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;

public class ACCViewEditPageImpl extends BasePageImpl implements ACCViewEditPage {

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
    private static final By CARDINALITY_COMPONENT_LOCATOR=
            By.xpath("//span[contains(text(), \"Cardinality Max\")]//ancestor::mat-form-field//input");

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
            By.xpath("//mat-dialog-container//span[contains(text(), \"Amend\")]//ancestor::button/span");

    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");


    private final ACCObject acc;

    public ACCViewEditPageImpl(BasePage parent, ACCObject acc) {
        super(parent);
        this.acc = acc;
    }
 
    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/core_component/acc/" + this.acc.getAccManifestId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "ACC".equals(getCoreComponentTypeFieldValue());
        assert getText(getTitle()).equals(acc.getDen());
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.cssSelector("div.mat-tab-list div.mat-tab-label"));
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
    public String getDenFieldLabelForASCC(){
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
    public void hitAmendButton() {
        click(elementToBeClickable(getDriver(), AMEND_BUTTON_LOCATOR));
        click(elementToBeClickable(getDriver(), CONTINUE_AMEND_BUTTON_IN_DIALOG_LOCATOR));
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
    public void expandTree(String nodeName) {
        try {
            By chevronRightLocator = By.xpath(
                    "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[1]/button/span/mat-icon[contains(text(), \"chevron_right\")]//ancestor::span[1]");
            click(elementToBeClickable(getDriver(), chevronRightLocator));
        } catch (TimeoutException maybeAlreadyExpanded) {
        }

        By expandMoreLocator = By.xpath(
                "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[1]/button/span/mat-icon[contains(text(), \"expand_more\")]//ancestor::span[1]");
        assert elementToBeClickable(getDriver(), expandMoreLocator).isEnabled();
    }

    @Override
    public void goToNode(String nodeName) {
        WebElement node = getNodeByName(nodeName);
        click(node);
    }

    private WebElement getNodeByName(String nodeName) {
        By nodeLocator = By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]");
        return visibilityOfElementLocated(getDriver(), nodeLocator);
    }

    @Override
    public String getCardinalityLabel() {
        return visibilityOfElementLocated(getDriver(), CARDINALITY_COMPONENT_LOCATOR).getAttribute("data-placeholder");
    }

    @Override
    public void moveToQA() {
        click(getMoveToQAButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
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
    public void toggleDeprecated() {
        click(getDeprecatedCheckbox());
    }

    @Override
    public WebElement getDeprecatedCheckbox() {
        return getCheckboxByName("Deprecated");
    }

    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[1]"));
    }

    @Override
    public void hitUpdateButton() {
        retry(() -> click(getUpdateButton(true)));
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
}
