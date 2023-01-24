package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.core_component.ACCExtensionViewEditPageImpl;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.openqa.selenium.*;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBIEPageImpl extends BasePageImpl implements EditBIEPage {

    private static final By SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-placeholder[contains(text(), \"Search\")]//ancestor::mat-form-field//input");

    private static final By ABIE_LOCAL_EXTENSION_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create ABIE Extension Locally\")]");

    private static final By ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create ABIE Extension Globally\")]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");

    private static final By SETTINGS_ICON_LOCATOR =
            By.xpath("//mat-icon[text() = \"settings\"]");

    private static final By HIDE_CARDINALITY_CHECKBOX_LOCATOR =
            By.xpath("//*[contains(text(), \"Hide cardinality\")]//ancestor::mat-checkbox");

    private static final By HIDE_UNUSED_CHECKBOX_LOCATOR =
            By.xpath("//*[contains(text(), \"Hide unused\")]//ancestor::mat-checkbox");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to QA\")]//ancestor::button[1]");

    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Back to WIP\")]//ancestor::button[1]");

    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Move to Production\")]//ancestor::button[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By ATTENTION_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");

    private static final By YES_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Yes\")]//ancestor::button/span");

    private static final By RESET_BUTTON_LOCATOR =
            By.xpath("//button[@mattooltip=\"Reset detail\"]");

    private static final By CONTINUE_RESET_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Reset\")]//ancestor::button/span");

    private static final By RESET_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");

    private final TopLevelASBIEPObject asbiep;

    public EditBIEPageImpl(BasePage parent, TopLevelASBIEPObject asbiep) {
        super(parent);
        this.asbiep = asbiep;
    }

    @Override
    public boolean isOpened() {
        invisibilityOfLoadingContainerElement(getDriver());
        return super.isOpened();
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/" + asbiep.getTopLevelAsbiepId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert getText(getTitle()).equals(asbiep.getPropertyTerm());
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.cssSelector("mat-tab-header div.mat-tab-label"));
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void clickOnDropDownMenuByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        String nodeName = nodes[nodes.length - 1];
        By menuLocator = By.xpath(
                "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[1]//mat-icon[contains(text(), \"more_vert\")]");
        click(visibilityOfElementLocated(getDriver(), menuLocator));
    }

    @Override
    public ACCExtensionViewEditPage extendBIEGloballyOnNode(String path) {
        clickOnDropDownMenuByPath(path);
        click(visibilityOfElementLocated(getDriver(), ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR));
        waitFor(ofMillis(500L));
        ACCExtensionViewEditPage ACCExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this);
        assert ACCExtensionViewEditPage.isOpened();
        return ACCExtensionViewEditPage;
    }

    @Override
    public ACCExtensionViewEditPage extendBIELocallyOnNode(String path) {
        clickOnDropDownMenuByPath(path);
        click(visibilityOfElementLocated(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
        waitFor(ofMillis(500L));
        invisibilityOfLoadingContainerElement(getDriver());
        ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this);
        assert accExtensionViewEditPage.isOpened();
        return accExtensionViewEditPage;
    }

    @Override
    public ACCExtensionViewEditPage continueToExtendBIEOnNode() {
        click(elementToBeClickable(getDriver(), YES_BUTTON_IN_DIALOG_LOCATOR));
        waitFor(ofMillis(500L));
        ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this);
        switchToNextTab(getDriver());
        assert accExtensionViewEditPage.isOpened();
        return accExtensionViewEditPage;
    }

    @Override
    public WebElement getSearchField() {
        return visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR);
    }

    private WebElement goToNode(String path) {
        click(getSearchField());
        WebElement node = sendKeys(visibilityOfElementLocated(getDriver(), SEARCH_FIELD_LOCATOR), path);
        node.sendKeys(Keys.ENTER);
        click(node);
        clear(getSearchField());
        return node;
    }

    public TopLevelASBIEPPanel getTopLevelASBIEPPanel() {
        return new TopLevelASBIEPPanelImpl();
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

    private WebElement getNodeByName(String nodeName) {
        By nodeLocator = By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]");
        return visibilityOfElementLocated(getDriver(), nodeLocator);
    }

    @Override
    public WebElement getNodeByPath(String path) {
        goToNode(path);
        String[] nodes = path.split("/");
        return getNodeByName(nodes[nodes.length - 1]);
    }

    @Override
    public boolean isDeprecated(WebElement node) {
        try {
            return node.findElement(By.xpath("//*[contains(@class, \"deprecated\")]")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public WebElement getSettingIcon() {
        return elementToBeClickable(getDriver(), SETTINGS_ICON_LOCATOR);
    }

    @Override
    public WebElement getHideCardinalityCheckbox() {
        return elementToBeClickable(getDriver(), HIDE_CARDINALITY_CHECKBOX_LOCATOR);
    }

    @Override
    public void toggleHideCardinality() {
        click(getSettingIcon());
        waitFor(ofMillis(500L));
        click(getHideCardinalityCheckbox());
    }

    @Override
    public WebElement getHideUnusedCheckbox() {
        return elementToBeClickable(getDriver(), HIDE_UNUSED_CHECKBOX_LOCATOR);
    }

    @Override
    public void toggleHideUnused() {
        click(getSettingIcon());
        waitFor(ofMillis(500L));
        click(getHideUnusedCheckbox());
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
    }

    @Override
    public String getAttentionDialogMessage() {
        return visibilityOfElementLocated(getDriver(), ATTENTION_DIALOG_MESSAGE_LOCATOR).getText();
    }

    @Override
    public ASBIEPanel getASBIEPanel(WebElement asccpNode) {
        click(asccpNode);
        String nodeText = getText(asccpNode);
        String panelTitle = getText(getTitle());
        assert nodeText.contains(panelTitle);
        return new ASBIEPanelImpl();
    }

    @Override
    public BBIEPanel getBBIEPanel(WebElement bccpNode) {
        click(bccpNode);
        String nodeText = getText(bccpNode);
        String panelTitle = getText(getTitle());
        assert nodeText.contains(panelTitle);
        return new BBIEPanelImpl();
    }

    @Override
    public BBIESCPanel getBBIESCPanel(WebElement bdtScNode) {
        click(bdtScNode);
        String nodeText = getText(bdtScNode);
        String panelTitle = getText(getTitle());
        assert nodeText.contains(panelTitle);
        return new BBIESCPanelImpl();
    }

    private WebElement getInputFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/input"));
    }

    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox[1]"));
    }

    private WebElement getTextAreaFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"" + name + "\")]//ancestor::div[1]/textarea"));
    }

    private class TopLevelASBIEPPanelImpl implements TopLevelASBIEPPanel {
        @Override
        public WebElement getReleaseField() {
            return getInputFieldByName("Release");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName("State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName("Owner");
        }

        @Override
        public WebElement getBusinessContextField() {
            return visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-chip-list[contains(@aria-label, \"Business Contexts\")]"));
        }

        @Override
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }

        @Override
        public void setBusinessTerm(String businessTerm) {
            sendKeys(getBusinessTermField(), businessTerm);
        }

        @Override
        public WebElement getRemarkField() {
            return getInputFieldByName("Remark");
        }

        @Override
        public void setRemark(String remark) {
            sendKeys(getRemarkField(), remark);
        }

        @Override
        public WebElement getVersionField() {
            return getInputFieldByName("Version");
        }

        @Override
        public void setVersion(String version) {
            sendKeys(getVersionField(), version);
        }

        @Override
        public WebElement getStatusField() {
            return getInputFieldByName("Status");
        }

        @Override
        public void setStatus(String status) {
            sendKeys(getStatusField(), status);
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName("Context Definition");
        }

        @Override
        public void setContextDefinition(String contextDefinition) {
            sendKeys(getContextDefinitionField(), contextDefinition);
        }

        @Override
        public WebElement getComponentDefinitionField() {
            return getTextAreaFieldByName("Component Definition");
        }

        @Override
        public WebElement getTypeDefinitionField() {
            return getTextAreaFieldByName("Type Definition");
        }
    }

    private class ASBIEPanelImpl implements ASBIEPanel {

        @Override
        public WebElement getUsedCheckbox() {
            return getCheckboxByName("Used");
        }

        @Override
        public void toggleUsed() {
            click(getUsedCheckbox());
        }

        @Override
        public WebElement getNillableCheckbox() {
            return getCheckboxByName("Nillable");
        }

        @Override
        public void toggleNillable() {
            click(getNillableCheckbox());
        }

        @Override
        public WebElement getCardinalityMinField() {
            return getInputFieldByName("Cardinality Min");
        }

        @Override
        public void setCardinalityMin(int cardinalityMin) {
            sendKeys(getCardinalityMinField(), Integer.toString(cardinalityMin));
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
        public WebElement getRemarkField() {
            return getInputFieldByName("Remark");
        }

        @Override
        public void setRemark(String remark) {
            sendKeys(getRemarkField(), remark);
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName("Context Definition");
        }

        @Override
        public void setContextDefinition(String contextDefinition) {
            sendKeys(getContextDefinitionField(), contextDefinition);
        }

        @Override
        public WebElement getAssociationDefinitionField() {
            return getTextAreaFieldByName("Association Definition");
        }

        @Override
        public WebElement getComponentDefinitionField() {
            return getTextAreaFieldByName("Component Definition");
        }

        @Override
        public WebElement getTypeDefinitionField() {
            return getTextAreaFieldByName("Type Definition");
        }

        @Override
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }
    }

    private class BBIEPanelImpl implements BBIEPanel {

        @Override
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }

        @Override
        public WebElement getUsedCheckbox() {
            return getCheckboxByName("Used");
        }

        @Override
        public void toggleUsed() {
            click(getUsedCheckbox());
        }

        @Override
        public WebElement getNillableCheckbox() {
            return getCheckboxByName("Nillable");
        }

        @Override
        public void toggleNillable() {
            click(getNillableCheckbox());
        }

        @Override
        public WebElement getCardinalityMinField() {
            return getInputFieldByName("Cardinality Min");
        }

        @Override
        public void setCardinalityMin(int cardinalityMin) {
            sendKeys(getCardinalityMinField(), Integer.toString(cardinalityMin));
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
        public WebElement getRemarkField() {
            return getInputFieldByName("Remark");
        }

        @Override
        public void setRemark(String remark) {
            sendKeys(getRemarkField(), remark);
        }

        @Override
        public WebElement getExampleField() {
            return getInputFieldByName("Example");
        }

        @Override
        public void setExample(String example) {
            sendKeys(getExampleField(), example);
        }

        @Override
        public WebElement getValueConstraintSelectField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//mat-label[contains(text(), \"Value Constraint\")]//ancestor::div[1]/mat-select"));
        }

        @Override
        public WebElement getValueConstraintFieldByValue(String value) {
            switch (value) {
                case "None":
                    return getInputFieldByName("No value constraints");
                case "Fixed Value":
                    return getInputFieldByName("Fixed Value");
                case "Default Value":
                    return getInputFieldByName("Default Value");
            }
            throw new UnsupportedOperationException("Unknown value: '" + value + "'");
        }

        @Override
        public void setValueConstraint(String value) {
            click(getValueConstraintSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + value + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getFixedValueField() {
            return getInputFieldByName("Fixed Value");
        }

        @Override
        public void setFixedValue(String fixedValue) {
            sendKeys(getFixedValueField(), fixedValue);
        }

        @Override
        public WebElement getDefaultValueField() {
            return getInputFieldByName("Default Value");
        }

        @Override
        public void setDefaultValue(String defaultValue) {
            sendKeys(getDefaultValueField(), defaultValue);
        }

        @Override
        public WebElement getValueDomainRestrictionSelectField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//span[contains(text(), \"Value Domain Restriction\")]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomainRestriction(String valueDomainRestriction) {
            click(getValueDomainRestrictionSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomainRestriction + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getValueDomainField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//span[text() = \"Value Domain\"]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomain(String valueDomain) {
            click(getValueDomainField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), valueDomain);
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomain + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName("Context Definition");
        }

        @Override
        public void setContextDefinition(String contextDefinition) {
            sendKeys(getContextDefinitionField(), contextDefinition);
        }

        @Override
        public WebElement getAssociationDefinitionField() {
            return getTextAreaFieldByName("Association Definition");
        }

        @Override
        public WebElement getComponentDefinitionField() {
            return getTextAreaFieldByName("Component Definition");
        }

        @Override
        public void setBusinessTerm(String business_term) {
            sendKeys(getBusinessTermField(), business_term);
        }

        @Override
        public void hitResetButton() {
            click(elementToBeClickable(getDriver(), RESET_BUTTON_LOCATOR));
        }

        @Override
        public void confirmToReset() {
            click(elementToBeClickable(getDriver(), CONTINUE_RESET_BUTTON_IN_DIALOG_LOCATOR));
        }

        @Override
        public String getResetDialogMessage() {
            return visibilityOfElementLocated(getDriver(), RESET_DIALOG_MESSAGE_LOCATOR).getText();
        }
    }

    private class BBIESCPanelImpl implements BBIESCPanel {

        @Override
        public WebElement getUsedCheckbox() {
            return getCheckboxByName("Used");
        }

        @Override
        public void toggleUsed() {
            click(getUsedCheckbox());
        }

        @Override
        public WebElement getCardinalityMinField() {
            return getInputFieldByName("Cardinality Min");
        }

        @Override
        public void setCardinalityMin(int cardinalityMin) {
            sendKeys(getCardinalityMinField(), Integer.toString(cardinalityMin));
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
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }

        @Override
        public void setBusinessTerm(String businessTerm) {
            sendKeys(getBusinessTermField(), businessTerm);
        }

        @Override
        public WebElement getRemarkField() {
            return getInputFieldByName("Remark");
        }

        @Override
        public void setRemark(String remark) {
            sendKeys(getRemarkField(), remark);
        }

        @Override
        public WebElement getExampleField() {
            return getInputFieldByName("Example");
        }

        @Override
        public void setExample(String example) {
            sendKeys(getExampleField(), example);
        }

        @Override
        public WebElement getValueConstraintSelectField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//mat-label[contains(text(), \"Value Constraint\")]//ancestor::div[1]/mat-select"));
        }

        @Override
        public WebElement getValueConstraintFieldByValue(String value) {
            switch (value) {
                case "None":
                    return getInputFieldByName("No value constraints");
                case "Fixed Value":
                    return getInputFieldByName("Fixed Value");
                case "Default Value":
                    return getInputFieldByName("Default Value");
            }
            throw new UnsupportedOperationException("Unknown value: '" + value + "'");
        }

        @Override
        public void setValueConstraint(String value) {
            click(getValueConstraintSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + value + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getFixedValueField() {
            return getInputFieldByName("Fixed Value");
        }

        @Override
        public void setFixedValue(String fixedValue) {
            sendKeys(getFixedValueField(), fixedValue);
        }

        @Override
        public WebElement getDefaultValueField() {
            return getInputFieldByName("Default Value");
        }

        @Override
        public void setDefaultValue(String defaultValue) {
            sendKeys(getDefaultValueField(), defaultValue);
        }

        @Override
        public WebElement getValueDomainRestrictionSelectField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//span[contains(text(), \"Value Domain Restriction\")]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomainRestriction(String valueDomainRestriction) {
            click(getValueDomainRestrictionSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomainRestriction + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getValueDomainField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//span[text() = \"Value Domain\"]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomain(String valueDomain) {
            click(getValueDomainField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), valueDomain);
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomain + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName("Context Definition");
        }

        @Override
        public void setContextDefinition(String contextDefinition) {
            sendKeys(getContextDefinitionField(), contextDefinition);
        }

        @Override
        public WebElement getComponentDefinitionField() {
            return getTextAreaFieldByName("Component Definition");
        }

    }

}
