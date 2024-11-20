package org.oagi.score.e2e.impl.page.bie;

import org.apache.commons.lang3.StringUtils;
import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.business_term.AssignBusinessTermBTPageImpl;
import org.oagi.score.e2e.impl.page.business_term.BusinessTermAssignmentPageImpl;
import org.oagi.score.e2e.impl.page.core_component.ACCExtensionViewEditPageImpl;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBIEPageImpl extends BasePageImpl implements EditBIEPage {

    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-form-field//input[@type=\"search\"]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[text() = \"search\"]");

    private static final By ENABLE_CHILDREN_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Enable Children\")]");

    private static final By SET_CHILDREN_MAX_CARDINALITY_TO_ONE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Set Children Max Cardinality to 1\")]");

    private static final By ABIE_LOCAL_EXTENSION_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create ABIE Extension Locally\")]");

    private static final By ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Create ABIE Extension Globally\")]");

    private static final By RETAINED_REUSED_BIE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Retain Reused BIE\")]");

    private static final By MAKE_BIE_REUSABLE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Make BIE reusable\")]");

    private static final By SETTINGS_ICON_LOCATOR =
            By.xpath("//mat-icon[text() = \"settings\"]//ancestor::button[1]");

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
            By.xpath("//mat-dialog-container//span[contains(text(), \"Yes\")]//ancestor::button");

    private static final By RESET_BUTTON_LOCATOR =
            By.xpath("//button[@mattooltip=\"Reset detail\"]");

    private static final By CONTINUE_RESET_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Reset\")]//ancestor::button");

    private static final By RESET_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");

    private static final By DEPRECATED_FLAG_LOCATOR =
            By.xpath("//span[contains(@class,'deprecated')]");

    private static final By ASSIGN_BUSINESS_TERM_LOCATOR = By.xpath("//span[contains(text(), \"Assign Business Term\")]//ancestor::button[1]");

    private static final By TURNOFF_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Turn off\")]//ancestor::button[1]");

    private static final By REUSE_BIE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Reuse BIE\")]");

    private final TopLevelASBIEPObject asbiep;
    private BasePage parent;

    public EditBIEPageImpl(BasePage parent, TopLevelASBIEPObject asbiep) {
        super(parent);
        this.asbiep = asbiep;
        this.parent = parent;
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
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-tab-header//div[@class=\"mat-mdc-tab-labels\"]/div[contains(@class, \"mdc-tab\")][1]"));
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getContextMenuIconByNodeName(String nodeName) {
        return elementToBeClickable(getDriver(), By.xpath(
                "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]" +
                        "//mat-icon[contains(text(), \"more_vert\")]"));
    }

    @Override
    public WebElement clickOnDropDownMenuByPath(String path) {
        return clickOnDropDownMenuByPathAndLevel(path, -1);
    }

    @Override
    public WebElement clickOnDropDownMenuByPathAndLevel(String path, int dataLevel) {
        return retry(() -> {
            goToNode(path);
            String[] nodes = path.split("/");
            String nodeName = nodes[nodes.length - 1];
            WebElement node = getNodeByNameAndDataLevel(nodeName, dataLevel);
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            try {
                if (visibilityOfElementLocated(getDriver(),
                        By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed()) {
                    return node;
                }
            } catch (WebDriverException ignore) {
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container

            WebElement contextMenuIcon = getContextMenuIconByNodeName(nodeName);
            click(contextMenuIcon);
            assert visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed();
            return node;
        });
    }

    @Override
    public TopLevelASBIEPObject getTopLevelASBIEP() {
        return asbiep;
    }

    @Override
    public void retainReusedBIEOnNode(String path) {
        retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), RETAINED_REUSED_BIE_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), RETAINED_REUSED_BIE_OPTION_LOCATOR));
            }

            click(elementToBeClickable(getDriver(), By.xpath(
                    "//mat-dialog-container//span[contains(text(), \"Retain\")]//ancestor::button[1]")));
            invisibilityOfLoadingContainerElement(getDriver());
            waitFor(ofMillis(1000L));
        });
    }

    @Override
    public void MakeBIEReusableOnNode(String path) {
        retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), MAKE_BIE_REUSABLE_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), MAKE_BIE_REUSABLE_OPTION_LOCATOR));
            }

            click(elementToBeClickable(getDriver(), By.xpath(
                    "//mat-dialog-container//span[contains(text(), \"Make\")]//ancestor::button[1]")));
            invisibilityOfLoadingContainerElement(getDriver());
            waitFor(ofMillis(2000L));
        });
    }

    @Override
    public ACCExtensionViewEditPage extendBIEGloballyOnNode(String path) {
        return retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR));
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container

            String currentUrl = retry(() -> {
                waitFor(ofMillis(1000L));
                String url = getDriver().getCurrentUrl();
                if (url.contains("core_component")) {
                    return url;
                }
                throw new WebDriverException();
            });
            BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
            ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
            ACCExtensionViewEditPage ACCExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this, acc);
            assert ACCExtensionViewEditPage.isOpened();
            return ACCExtensionViewEditPage;
        });
    }

    @Override
    public void enableChildren(String path) {
        retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), ENABLE_CHILDREN_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), ENABLE_CHILDREN_OPTION_LOCATOR));
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        });
    }

    @Override
    public void setChildrenMaxCardinalityToOne(String path) {
        retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), SET_CHILDREN_MAX_CARDINALITY_TO_ONE_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), SET_CHILDREN_MAX_CARDINALITY_TO_ONE_OPTION_LOCATOR));
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        });
    }

    @Override
    public ACCExtensionViewEditPage extendBIELocallyOnNode(String path) {
        return retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container

            String currentUrl = retry(() -> {
                waitFor(ofMillis(1000L));
                String url = getDriver().getCurrentUrl();
                if (url.contains("core_component")) {
                    return url;
                }
                throw new WebDriverException();
            });

            BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
            ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
            ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this, acc);
            assert accExtensionViewEditPage.isOpened();
            return accExtensionViewEditPage;
        });
    }

    @Override
    public void getExtendBIELocallyOnNode(String path) {
        retry(() -> {
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
            }
            click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        });
    }

    @Override
    public ACCExtensionViewEditPage continueToExtendBIEOnNode() {
        click(elementToBeClickable(getDriver(), YES_BUTTON_IN_DIALOG_LOCATOR));
        waitFor(ofMillis(1000L));

        switchToNextTab(getDriver());
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
        ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this, acc);
        assert accExtensionViewEditPage.isOpened();
        return accExtensionViewEditPage;
    }

    @Override
    public WebElement getSearchInputTextField() {
        return elementToBeClickable(getDriver(), SEARCH_INPUT_TEXT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDeprecatedFlag() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_FLAG_LOCATOR);
    }

    private WebElement goToNode(String path) {
        return goToNode(path, 0);
    }

    private WebElement goToNode(String path, int retry) {
        return retry(() -> {
            WebElement searchInput = getSearchInputTextField();
            click(getDriver(), searchInput);
            WebElement node = sendKeys(searchInput, path);
            for (int i = 0; i < (retry + 1); ++i) {
                node.sendKeys(Keys.ENTER);
                waitFor(ofMillis(500L));
            }
            click(getDriver(), node);
            clear(searchInput);
            return node;
        });
    }

    public TopLevelASBIEPPanel getTopLevelASBIEPPanel() {
        return new TopLevelASBIEPPanelImpl();
    }

    @Override
    public void expandTree(String nodeName) {
        try {
            By chevronRightLocator = By.xpath(
                    "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[contains(@class, \"mat-tree-node\")]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]");
            click(elementToBeClickable(getDriver(), chevronRightLocator));
        } catch (TimeoutException maybeAlreadyExpanded) {
        }

        By expandMoreLocator = By.xpath(
                "//*[contains(text(), \"" + nodeName + "\")]//ancestor::div[contains(@class, \"mat-tree-node\")]//mat-icon[contains(text(), \"expand_more\")]//ancestor::button[1]");
        assert elementToBeClickable(getDriver(), expandMoreLocator).isEnabled();
    }

    private WebElement getNodeByName(String nodeName) {
        return getNodeByNameAndDataLevel(nodeName, -1);
    }

    private WebElement getNodeByNameAndDataLevel(String nodeName, int dataLevel) {
        String xpathExpr = "//*[text() = \"" + nodeName + "\"]//ancestor::div[contains(@class, \"mat-tree-node\")]";
        if (dataLevel >= 0) {
            xpathExpr += "[@data-level=\"" + dataLevel + "\"]";
        }
        By nodeLocator = By.xpath(xpathExpr);
        return visibilityOfElementLocated(getDriver(), nodeLocator);
    }

    @Override
    public WebElement getNodeByPath(String path) {
        return getNodeByPath(path, 0);
    }

    @Override
    public WebElement getNodeByPath(String path, int retry) {
        return retry(() -> {
            goToNode(path, retry);
            String[] nodes = path.split("/");
            int dataLevel = nodes.length - 2;
            if (dataLevel > 0) {
                return getNodeByNameAndDataLevel(nodes[nodes.length - 1], dataLevel);
            } else {
                return getNodeByName(nodes[nodes.length - 1]);
            }
        });
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
    public String getAttentionDialogMessage() {
        return visibilityOfElementLocated(getDriver(), ATTENTION_DIALOG_MESSAGE_LOCATOR).getText();
    }

    @Override
    public SelectProfileBIEToReuseDialog reuseBIEOnNode(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), REUSE_BIE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), REUSE_BIE_OPTION_LOCATOR));
        }
        waitFor(ofMillis(1000L));

        SelectProfileBIEToReuseDialog selectProfileBIEToReuse = new SelectProfileBIEToReuseDialogImpl(this, "Reuse BIE");
        assert selectProfileBIEToReuse.isOpened();
        return selectProfileBIEToReuse;
    }

    @Override
    public SelectProfileBIEToReuseDialog reuseBIEOnNodeAndLevel(String path, int dataLevel) {
        WebElement node = clickOnDropDownMenuByPathAndLevel(path, dataLevel);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), REUSE_BIE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), REUSE_BIE_OPTION_LOCATOR));
        }
        waitFor(ofMillis(1000L));

        SelectProfileBIEToReuseDialog selectProfileBIEToReuse = new SelectProfileBIEToReuseDialogImpl(this, "Reuse BIE");
        assert selectProfileBIEToReuse.isOpened();
        return selectProfileBIEToReuse;
    }

    @Override
    public ASBIEPanel getASBIEPanel(WebElement asccpNode) {
        return retry(() -> {
            click(asccpNode);
            waitFor(ofMillis(1000L));
            String nodeText = getText(asccpNode);
            String panelTitle = getText(getTitle());
            assert nodeText.contains(panelTitle.trim());
            return new ASBIEPanelImpl();
        });
    }

    @Override
    public BBIEPanel getBBIEPanel(WebElement bccpNode) {
        return retry(() -> {
            click(bccpNode);
            waitFor(ofMillis(1000L));
            String nodeText = getText(bccpNode);
            String panelTitle = getText(getTitle());
            assert nodeText.contains(panelTitle.trim());
            return new BBIEPanelImpl();
        });
    }

    @Override
    public BBIESCPanel getBBIESCPanel(WebElement bdtScNode) {
        return retry(() -> {
            click(bdtScNode);
            waitFor(ofMillis(1000L));
            String nodeText = getText(bdtScNode);
            String panelTitle = getText(getTitle());
            assert nodeText.contains(panelTitle.trim());
            return new BBIESCPanelImpl();
        });
    }

    private WebElement getInputFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//input[contains(@placeholder, \"" + name + "\")]"));
    }

    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }

    private WebElement getTextAreaFieldByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[@placeholder = \"" + name + "\"]//ancestor::div[1]/textarea"));
    }

    private WebElement getIconButtonByName(String iconName) {
        return elementToBeClickable(getDriver(), By.xpath(
                "//mat-icon[contains(text(), \"" + iconName + "\")]//ancestor::button"));
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
        public WebElement getBusinessContextInputField() {
            return elementToBeClickable(getDriver(),
                    By.xpath("//input[@placeholder = \"Business Context\"]"));
        }

        @Override
        public List<WebElement> getBusinessContextList() {
            return visibilityOfAllElementsLocatedBy(getDriver(),
                    By.xpath("//mat-label[contains(text(), \"Business Contexts\")]//ancestor::mat-form-field//mat-chip-grid//mat-chip-row"));
        }

        @Override
        public void addBusinessContext(BusinessContextObject businessContext) {
            addBusinessContext(businessContext.getName());
        }

        @Override
        public void addBusinessContext(String businessContextName) {
            WebElement businessContextInput = getBusinessContextInputField();
            // TODO:
            // The <mat-chip-list> for the business context field is not working without clicking the field and typing characters.
            {
                click(businessContextInput);
            }
            sendKeys(businessContextInput, businessContextName.substring(0, 3));
            WebElement businessContextButton = elementToBeClickable(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + businessContextName + "\")]"));
            click(businessContextButton);
            waitFor(ofMillis(500L));
        }

        @Override
        public void removeBusinessContext(BusinessContextObject businessContext) {
            removeBusinessContext(businessContext.getName());
        }

        @Override
        public void removeBusinessContext(String businessContextName) {
            WebElement businessContextChipCancelButton = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-label[contains(text(), \"Business Contexts\")]//ancestor::mat-form-field//mat-chip-grid" +
                            "//*[contains(text(), \"" + businessContextName + "\")]//ancestor::mat-chip-row//mat-icon[text() = \"cancel\"]//ancestor::button"));
            click(businessContextChipCancelButton);
            assert "Updated".equals(getSnackBarMessage(getDriver()));
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

        @Override
        public WebElement getResetDetailButton() {
            return getIconButtonByName("refresh");
        }

        @Override
        public void resetDetail() {
            click(getResetDetailButton());
            click(getDialogButtonByName(getDriver(), "Reset"));
            assert "Reset".equals(getSnackBarMessage(getDriver()));
        }
    }

    @Override
    public ReusedASBIEPanel getReusedASBIEPanel(WebElement asccpNode) {
        return retry(() -> {
            click(asccpNode);
            waitFor(ofMillis(500L));
            return new ReusedASBIEPanelImpl("//div[contains(@class, \"detail-reused\")][1]");
        });
    }

    private class ReusedASBIEPanelImpl implements ReusedASBIEPanel {

        private final String baseXPath;

        public ReusedASBIEPanelImpl(String baseXPath) {
            this.baseXPath = baseXPath;
        }

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
            return getInputFieldByName("Business Context");
        }

        @Override
        public WebElement getLegacyBusinessTermField() {
            return getInputFieldByName("Legacy Business Term");
        }

        @Override
        public WebElement getRemarkField() {
            return getInputFieldByName("Remark");
        }

        @Override
        public WebElement getVersionField() {
            return getInputFieldByName("Version");
        }

        @Override
        public WebElement getStatusField() {
            return getInputFieldByName("Status");
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName("Context Definition");
        }
    }

    private class ASBIEPanelImpl implements ASBIEPanel {

        @Override
        public BusinessTermAssignmentPage clickShowBusinessTermsButton() {
            //Store the current window handle
            String winHandleBefore = getDriver().getWindowHandle();
            click(getShowBusinessTermsButton());
            for (String winHandle : getDriver().getWindowHandles()) {
                getDriver().switchTo().window(winHandle);
            }
            String url = getDriver().getCurrentUrl();
            String bieTypes = StringUtils.substringAfter(url, "bieType=");
            Integer bieId = Integer.parseInt(StringUtils.substringBetween(url, "bieId=", "&"));
            BusinessTermAssignmentPage businessTermAssignmentPage =
                    new BusinessTermAssignmentPageImpl(parent, Arrays.asList(bieTypes), BigInteger.valueOf(bieId.intValue()));
            assert businessTermAssignmentPage.isOpened();
            return businessTermAssignmentPage;
        }

        @Override
        public AssignBusinessTermBTPage clickAssignBusinessTermButton() {
            //Store the current window handle
            String winHandleBefore = getDriver().getWindowHandle();
            click(getAssignBusinessTermButton(true));
            for (String winHandle : getDriver().getWindowHandles()) {
                getDriver().switchTo().window(winHandle);
            }
            String url = getDriver().getCurrentUrl();
            String bieTypes = StringUtils.substringAfter(url, "bieTypes=");
            Integer bieId = Integer.parseInt(StringUtils.substringBetween(url, "bieIds=", "&"));
            AssignBusinessTermBTPage assignBusinessTermBTPage = new AssignBusinessTermBTPageImpl(parent, Arrays.asList(bieTypes), BigInteger.valueOf(bieId.intValue()));
            assert assignBusinessTermBTPage.isOpened();
            return assignBusinessTermBTPage;
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

        public WebElement getShowBusinessTermsButton() {
            return elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Show Business Terms\")]//ancestor::button[1]"));
        }

        @Override
        public WebElement getAssignBusinessTermButton(boolean enabled) {
            if (enabled) {
                return elementToBeClickable(getDriver(), ASSIGN_BUSINESS_TERM_LOCATOR);
            } else {
                return visibilityOfElementLocated(getDriver(), ASSIGN_BUSINESS_TERM_LOCATOR);
            }
        }

        @Override
        public WebElement getResetDetailButton() {
            return getIconButtonByName("refresh");
        }

        @Override
        public void resetDetail() {
            click(getResetDetailButton());
            click(getDialogButtonByName(getDriver(), "Reset"));
            assert "Reset".equals(getSnackBarMessage(getDriver()));
        }
    }

    private class BBIEPanelImpl implements BBIEPanel {
        @Override
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }

        @Override
        public WebElement getShowBusinessTermsButton() {
            return elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Show Business Terms\")]//ancestor::button[1]"));
        }

        @Override
        public WebElement getAssignBusinessTermButton(boolean enabled) {
            if (enabled) {
                return elementToBeClickable(getDriver(), ASSIGN_BUSINESS_TERM_LOCATOR);
            } else {
                return visibilityOfElementLocated(getDriver(), ASSIGN_BUSINESS_TERM_LOCATOR);
            }
        }

        @Override
        public BusinessTermAssignmentPage clickShowBusinessTermsButton() {
            //Store the current window handle
            String winHandleBefore = getDriver().getWindowHandle();
            click(getShowBusinessTermsButton());
            for (String winHandle : getDriver().getWindowHandles()) {
                getDriver().switchTo().window(winHandle);
            }
            String url = getDriver().getCurrentUrl();
            String bieTypes = StringUtils.substringAfter(url, "bieType=");
            Integer bieId = Integer.parseInt(StringUtils.substringBetween(url, "bieId=", "&"));
            BusinessTermAssignmentPage businessTermAssignmentPage =
                    new BusinessTermAssignmentPageImpl(parent, Arrays.asList(bieTypes), BigInteger.valueOf(bieId.intValue()));
            assert businessTermAssignmentPage.isOpened();
            return businessTermAssignmentPage;
        }

        @Override
        public AssignBusinessTermBTPage clickAssignBusinessTermButton() {
            //Store the current window handle
            String winHandleBefore = getDriver().getWindowHandle();
            click(getAssignBusinessTermButton(true));
            for (String winHandle : getDriver().getWindowHandles()) {
                getDriver().switchTo().window(winHandle);
            }
            String url = getDriver().getCurrentUrl();
            String bieTypes = StringUtils.substringAfter(url, "bieTypes=");
            Integer bieId = Integer.parseInt(StringUtils.substringBetween(url, "bieIds=", "&"));
            AssignBusinessTermBTPage assignBusinessTermBTPage = new AssignBusinessTermBTPageImpl(parent, Arrays.asList(bieTypes), BigInteger.valueOf(bieId.intValue()));
            assert assignBusinessTermBTPage.isOpened();
            return assignBusinessTermBTPage;
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
            return elementToBeClickable(getDriver(), By.xpath(
                    "//mat-label[contains(text(), \"Value Domain Restriction\")]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomainRestriction(String valueDomainRestriction) {
            click(getValueDomainRestrictionSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomainRestriction + "\")]//ancestor::mat-option[1]")));
        }

        @Override
        public WebElement getValueDomainField() {
            return elementToBeClickable(getDriver(), By.xpath(
                    "//mat-label[text() = \"Value Domain\"]//ancestor::div[1]/mat-select"));
        }

        @Override
        public void setValueDomain(String valueDomain) {
            click(getDriver(), getValueDomainField());
            waitFor(ofMillis(1000L));
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), valueDomain);
            click(getDriver(), elementToBeClickable(getDriver(), By.xpath(
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

        @Override
        public String getValueDomainWarningMessage(String valueDomain) {
            click(getDriver(), getValueDomainField());
            waitFor(ofMillis(1000L));
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), valueDomain);
            WebElement valueDomainElement = findElement(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomain + "\")]//ancestor::mat-option[1]/span/div"));
            new Actions(getDriver()).moveToElement(valueDomainElement).perform(); // mouse over
            String message = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-tooltip-component")));
            pressEscape();
            return message;
        }

        @Override
        public WebElement getResetDetailButton() {
            return getIconButtonByName("refresh");
        }

        @Override
        public void resetDetail() {
            click(getResetDetailButton());
            click(getDialogButtonByName(getDriver(), "Reset"));
            assert "Reset".equals(getSnackBarMessage(getDriver()));
        }
    }

    private void pressEscape() {
        waitFor(Duration.ofMillis(500));
        Actions action = new Actions(getDriver());
        action.sendKeys(Keys.ESCAPE).build().perform();
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
                    "//mat-select[@placeholder = \"Value Domain Restriction\"]"));
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
                    "//mat-select[@placeholder = \"Value Domain\"]"));
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
