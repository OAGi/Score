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
import org.oagi.score.e2e.page.bie.BieBusinessTermAssignDialog;
import org.oagi.score.e2e.page.bie.BieOpenAPIDocumentAddDialog;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectBaseProfileBIEDialog;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.business_term.AssignBusinessTermBTPage;
import org.oagi.score.e2e.page.business_term.BusinessTermAssignmentPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Wait;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditBIEPageImpl extends BasePageImpl implements EditBIEPage {

    private static final int DEFAULT_PATH_RETRY_COUNT = 2;
    private static final int MAX_PATH_RETRY_COUNT = 5;

    private static final By SEARCH_INPUT_TEXT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-form-field//input[@type=\"search\"]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"tree-search-box\")]//mat-icon[normalize-space(text()) = \"search\" or normalize-space(text()) = \"repeat\"]");

    private static final By UPDATE_LOADING_OVERLAY_LOCATOR =
            By.cssSelector(".main-content-loading-overlay");

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

    private static final By VALUE_DOMAIN_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-form-field[.//mat-label[normalize-space(text()) = \"Value Domain\"]]//mat-select");

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

    private static final By USE_BASE_BIE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Use Base BIE\")]");

    private static final By OVERRIDE_BASE_REUSED_BIE_OPTION_LOCATOR =
            By.xpath("//span[contains(text(), \"Override Base Reused BIE\")]");

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
        invisibilityOfLoadingContainerElement(getDriver());
        getSearchInputTextField();
    }

    @Override
    public WebElement getTitle() {
        invisibilityOfLoadingContainerElement(getDriver());
        return visibilityOfElementLocated(PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                By.xpath("//mat-tab-header//div[@role=\"tab\"][1]"));
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
        return retry(() -> clickOnDropDownMenuByPathAndLevelOnce(path, dataLevel), null,
                DEFAULT_PATH_RETRY_COUNT);
    }

    private WebElement clickOnDropDownMenuByPathAndLevelOnce(String path, int dataLevel) {
        goToNode(path);
        WebElement node = findNodeByPath(path, dataLevel);
        try {
            WebElement menuIcon = node.findElement(By.xpath(".//mat-icon[contains(@class, 'context-menu-icon') or contains(text(), 'more_vert')]"));
            click(getDriver(), menuIcon);
        } catch (Exception e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
        }
        try {
            if (visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]")).isDisplayed()) {
                return node;
            }
        } catch (WebDriverException ignore) {
        }
        click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        return node;
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
        // Do not retry creation after switching tabs: it can hide the original failure
        // by searching for the BIE path on the extension page.
        Set<String> existingWindowHandles = getDriver().getWindowHandles();
        String sourceWindowHandle = getDriver().getWindowHandle();
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(elementToBeClickable(getDriver(), ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(elementToBeClickable(getDriver(), ABIE_GLOBAL_EXTENSION_OPTION_LOCATOR));
        }
        click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        waitForBIEUpdateToFinish();

        String currentUrl = retry(() -> {
            waitFor(ofMillis(1000L));
            for (String handle : getDriver().getWindowHandles()) {
                if (existingWindowHandles.contains(handle)) {
                    continue;
                }
                getDriver().switchTo().window(handle);
                String url = getDriver().getCurrentUrl();
                if (url.contains("/core_component/extension/")) {
                    return url;
                }
            }
            getDriver().switchTo().window(sourceWindowHandle);
            String url = getDriver().getCurrentUrl();
            if (url.contains("/core_component/extension/")) {
                return url;
            }
            throw new WebDriverException("The global extension page did not open for " + path);
        });

        BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
        ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this, acc);
        assert accExtensionViewEditPage.isOpened();
        return accExtensionViewEditPage;
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
        // Do not retry creation after switching tabs: it can hide the original failure
        // by searching for the BIE path on the extension page.
        Set<String> existingWindowHandles = getDriver().getWindowHandles();
        String sourceWindowHandle = getDriver().getWindowHandle();
            WebElement node = clickOnDropDownMenuByPath(path);
            try {
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
            } catch (TimeoutException e) {
                click(node);
                new Actions(getDriver()).sendKeys("O").perform();
                click(elementToBeClickable(getDriver(), ABIE_LOCAL_EXTENSION_OPTION_LOCATOR));
        }
        click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
        waitForBIEUpdateToFinish();

        String currentUrl = retry(() -> {
                waitFor(ofMillis(1000L));
                for (String handle : getDriver().getWindowHandles()) {
                    if (existingWindowHandles.contains(handle)) {
                        continue;
                    }
                    getDriver().switchTo().window(handle);
                    String url = getDriver().getCurrentUrl();
                    if (url.contains("/core_component/extension/")) {
                        return url;
                    }
                }
                getDriver().switchTo().window(sourceWindowHandle);
                String url = getDriver().getCurrentUrl();
                if (url.contains("/core_component/extension/")) {
                    return url;
                }
                throw new WebDriverException("The local extension page did not open for " + path);
            });

            BigInteger accManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
            ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
            ACCExtensionViewEditPage accExtensionViewEditPage = new ACCExtensionViewEditPageImpl(this, acc);
            assert accExtensionViewEditPage.isOpened();
            return accExtensionViewEditPage;
    }

    private void waitForBIEUpdateToFinish() {
        try {
            // A fast response can navigate away and destroy the BIE component before Selenium
            // observes the overlay. In that case the URL wait below is the completion signal.
            visibilityOfElementLocated(
                    PageHelper.wait(getDriver(), Duration.ofSeconds(2L), ofMillis(100L)),
                    UPDATE_LOADING_OVERLAY_LOCATOR);
        } catch (TimeoutException ignored) {
            return;
        }
        invisibilityOfElementLocated(
                PageHelper.wait(getDriver(), Duration.ofSeconds(30L), ofMillis(100L)),
                UPDATE_LOADING_OVERLAY_LOCATOR);
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
            //click(getDriver().findElement(By.tagName("body"))); // To close overlay-container
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
        invisibilityOfLoadingContainerElement(getDriver());
        waitForSnackBarToDisappear(getDriver());
        return elementToBeClickable(PageHelper.wait(getDriver(), Duration.ofSeconds(30L), ofMillis(500L)), SEARCH_INPUT_TEXT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getDeprecatedFlag() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_FLAG_LOCATOR);
    }

    private WebElement goToNode(String path) {
        WebElement searchInput = getSearchInputTextField();
        click(getDriver(), searchInput);
        clear(searchInput);
        WebElement node = sendKeys(searchInput, path);
        try {
            WebElement searchBtn = getSearchButton();
            click(getDriver(), searchBtn);
        } catch (TimeoutException e) {
            node.sendKeys(Keys.ENTER);
            waitFor(ofMillis(500L));
        }
        waitFor(ofMillis(1000L));
        return node;
    }

    public TopLevelASBIEPPanel getTopLevelASBIEPPanel() {
        WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                "//mat-tab-header//div[@role=\"tab\"][1]"));
        click(tab);
        return new TopLevelASBIEPPanelImpl();
    }

    // Issue #1519: the BIE-root 'OpenAPI Document Information' panel, scoped by its panel title so it is never
    // confused with the sibling 'Supporting Documentation' panel (both carry the 'info-panel-header-add' '+').
    private static final String OAS_INFO_PANEL_XPATH =
            "//mat-expansion-panel[.//mat-panel-title[normalize-space(.) = \"OpenAPI Document Information\"]]";

    @Override
    public boolean isOpenAPIDocumentInformationPanelDisplayed() {
        // The panel renders on the BIE root only after the detail has loaded AND the asynchronous
        // "does any OpenAPI Document exist" check has resolved, so settle before deciding (an absent panel
        // returns immediately from findElements without a long wait).
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1500L));
        return !getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH)).isEmpty();
    }

    @Override
    public EditBIEPage.OpenAPIDocumentInformationPanel openOpenAPIDocumentInformationPanel() {
        invisibilityOfLoadingContainerElement(getDriver());
        WebElement header = visibilityOfElementLocated(getDriver(),
                By.xpath(OAS_INFO_PANEL_XPATH + "//mat-expansion-panel-header"));
        if (!"true".equals(header.getAttribute("aria-expanded"))) {
            click(getDriver(), visibilityOfElementLocated(getDriver(),
                    By.xpath(OAS_INFO_PANEL_XPATH + "//mat-panel-title[normalize-space(.) = \"OpenAPI Document Information\"]")));
            waitFor(ofMillis(500L));
        }
        return new OpenAPIDocumentInformationPanelImpl();
    }

    @Override
    public void expandTree(String nodeName) {
        try {
            By chevronRightLocator = By.xpath(
                    "//div[contains(@class, \"mat-tree-node\")][.//span[contains(@class, \"node-label\") and contains(., \"" + nodeName + "\")]]//button[.//mat-icon[contains(text(), \"chevron_right\")]]");
            click(elementToBeClickable(getDriver(), chevronRightLocator));
        } catch (TimeoutException maybeAlreadyExpanded) {
        }

        By expandMoreLocator = By.xpath(
                "//div[contains(@class, \"mat-tree-node\")][.//span[contains(@class, \"node-label\") and contains(., \"" + nodeName + "\")]]//button[.//mat-icon[contains(text(), \"expand_more\")]]");
        assert elementToBeClickable(getDriver(), expandMoreLocator).isEnabled();
    }

    private WebElement getNodeByName(String nodeName) {
        return getNodeByNameAndDataLevel(nodeName, -1);
    }

    private WebElement getNodeByNameAndDataLevel(String nodeName, int dataLevel) {
        String xpathExpr = "//div[contains(@class, \"mat-tree-node\")]";
        if (dataLevel >= 0) {
            xpathExpr += "[@data-level=\"" + dataLevel + "\"]";
        }
        xpathExpr += "[.//span[contains(@class, \"node-label\") and normalize-space(.) = \"" + nodeName + "\"]]";
        By nodeLocator = By.xpath(xpathExpr);
        try {
            return visibilityOfElementLocated(getDriver(), nodeLocator);
        } catch (TimeoutException e) {
            List<WebElement> present = getDriver().findElements(nodeLocator);
            if (present.isEmpty()) {
                throw new NoSuchElementException(
                        "BIE tree node was not found: name='" + nodeName + "', dataLevel=" + dataLevel, e);
            }
            ((JavascriptExecutor) getDriver()).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", present.get(0));
            return visibilityOfElementLocated(shortWait(getDriver()), nodeLocator);
        }
    }

    private WebElement findNodeByPath(String path, int dataLevel) {
        String[] nodes = path.split("/");
        int effectiveDataLevel = dataLevel >= 0 ? dataLevel : nodes.length - 2;
        String nodeExpr = "//div[contains(@class, \"mat-tree-node\")]";
        if (effectiveDataLevel >= 0) {
            nodeExpr += "[@data-level=\"" + effectiveDataLevel + "\"]";
        }
        String queryPath = path.replaceFirst("^/", "").replaceAll("\\s+", "");
        By exactPathLocator = By.xpath(nodeExpr + "[@data-query-path=" + xpathLiteral(queryPath) + "]");
        try {
            return visibilityOfElementLocated(pathWait(), exactPathLocator);
        } catch (TimeoutException timeout) {
            TimeoutException pathTimeout = new TimeoutException(
                    "BIE path search result was not rendered: path='" + path + "', dataLevel=" + dataLevel);
            pathTimeout.initCause(timeout);
            throw pathTimeout;
        }
    }

    private Wait<WebDriver> pathWait() {
        return PageHelper.wait(getDriver(), Duration.ofSeconds(5L), ofMillis(100L));
    }

    @Override
    public WebElement getNodeByPath(String path) {
        return getNodeByPath(path, DEFAULT_PATH_RETRY_COUNT);
    }

    @Override
    public WebElement getNodeByPath(String path, int retry) {
        int attemptCount = Math.min(Math.max(retry, 1), MAX_PATH_RETRY_COUNT);
        return retry(() -> {
            // Keep one bounded retry boundary around one search and lookup
            // attempt. This prevents nested waits and repeated Enter bursts.
            goToNode(path);
            return findNodeByPath(path, -1);
        }, null, attemptCount);
    }

    private void clickLabel(WebElement node) {
        WebElement label = node.findElement(By.cssSelector(".node-label"));
        click(getDriver(), label);
    }

    @Override
    public WebElement getUsedCheckboxAtNode(WebElement node) {
        return node.findElement(By.xpath(".//mat-checkbox[contains(@class, \"bie-checkbox\")]"));
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
    public SelectBaseProfileBIEDialog openUseBaseBIEDialog() {
        WebElement node = clickOnDropDownMenuByPath("/" + this.asbiep.getPropertyTerm());
        try {
            click(getDriver(), elementToBeClickable(getDriver(), USE_BASE_BIE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), USE_BASE_BIE_OPTION_LOCATOR));
        }
        waitFor(ofMillis(1000L));

        SelectBaseProfileBIEDialog selectBaseProfileBIEDialog = new SelectBaseProfileBIEDialogImpl(this, "Use Base BIE");
        assert selectBaseProfileBIEDialog.isOpened();
        return selectBaseProfileBIEDialog;
    }

    @Override
    public SelectProfileBIEToReuseDialog openOverrideBaseReusedBIEDialog(String path) {
        WebElement node = clickOnDropDownMenuByPath(path);
        try {
            click(getDriver(), elementToBeClickable(getDriver(), OVERRIDE_BASE_REUSED_BIE_OPTION_LOCATOR));
        } catch (TimeoutException e) {
            click(getDriver(), node);
            new Actions(getDriver()).sendKeys("O").perform();
            click(getDriver(), elementToBeClickable(getDriver(), OVERRIDE_BASE_REUSED_BIE_OPTION_LOCATOR));
        }
        waitFor(ofMillis(1000L));

        SelectProfileBIEToReuseDialog selectProfileBIEToReuse = new SelectProfileBIEToReuseDialogImpl(this, "Override Base Reused BIE");
        assert selectProfileBIEToReuse.isOpened();
        return selectProfileBIEToReuse;
    }

    @Override
    public ASBIEPanel getASBIEPanel(WebElement asccpNode) {
        return retry(() -> {
            // Clicking the tree-node itself uses its center point.  For a reused ASBIEP that
            // point can land on the Reuse icon, which intentionally opens the reused BIE tab.
            // Click the label so the tree-node click handler selects this node instead.
            clickLabel(asccpNode);
            By firstTabLocator = By.xpath("//mat-tab-header//div[@role=\"tab\"][1]");
            WebElement tab = elementToBeClickable(getDriver(), firstTabLocator);
            click(tab);
            By selectedFirstTabLocator = By.xpath("//mat-tab-header//div[@role=\"tab\"][1][@aria-selected=\"true\"]");
            Wait<WebDriver> panelWait = PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L));
            panelWait.until(driver -> {
                String nodeText = getText(asccpNode);
                WebElement selectedTab = driver.findElement(selectedFirstTabLocator);
                String panelTitle = getText(selectedTab);
                return nodeText != null && panelTitle != null && nodeText.contains(panelTitle.trim());
            });
            String nodeText = getText(asccpNode);
            String panelTitle = getText(elementToBeClickable(getDriver(), selectedFirstTabLocator));
            if (!nodeText.contains(panelTitle.trim())) {
                throw new WebDriverException("Panel title (" + panelTitle + ") does not match node (" + nodeText + ")");
            }
            return new ASBIEPanelImpl();
        });
    }

    @Override
    public BBIEPanel getBBIEPanel(WebElement bccpNode) {
        return retry(() -> {
            click(bccpNode);
            waitFor(ofMillis(1000L));
            WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-tab-header//div[@role=\"tab\"][1]"));
            click(tab);
            String nodeText = getText(bccpNode);
            String panelTitle = getText(getTitle());
            if (!nodeText.contains(panelTitle.trim())) {
                throw new WebDriverException("Panel title (" + panelTitle + ") does not match node (" + nodeText + ")");
            }
            return new BBIEPanelImpl();
        });
    }

    @Override
    public BBIESCPanel getBBIESCPanel(WebElement bdtScNode) {
        return retry(() -> {
            click(bdtScNode);
            waitFor(ofMillis(1000L));
            WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-tab-header//div[@role=\"tab\"][1]"));
            click(tab);
            String nodeText = getText(bdtScNode);
            String panelTitle = getText(getTitle());
            if (!nodeText.contains(panelTitle.trim())) {
                throw new WebDriverException("Panel title (" + panelTitle + ") does not match node (" + nodeText + ")");
            }
            return new BBIESCPanelImpl();
        });
    }

    private WebElement getInputFieldByName(String name) {
        return getInputFieldByName("", name);
    }

    private WebElement getInputFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//input[contains(@placeholder, \"" + name + "\")]"));
    }

    private WebElement getCheckboxByName(String name) {
        return getCheckboxByName("", name);
    }

    private WebElement getCheckboxByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }

    private WebElement getTextAreaFieldByName(String name) {
        return getTextAreaFieldByName("", name);
    }

    private WebElement getTextAreaFieldByName(String baseXPath, String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                baseXPath + "//*[@placeholder = \"" + name + "\"]//ancestor::div[1]/textarea"));
    }

    private WebElement getIconButtonByName(String iconName) {
        return getIconButtonByName("", iconName);
    }

    private WebElement getIconButtonByName(String baseXPath, String iconName) {
        return elementToBeClickable(getDriver(), By.xpath(
                baseXPath + "//mat-icon[contains(text(), \"" + iconName + "\")]//ancestor::button"));
    }

    private WebElement getButtonInActiveDetailTab(String activeDetailTabXPath, String buttonName, boolean enabled) {
        By locator = By.xpath(activeDetailTabXPath +
                "//button[.//*[normalize-space(.) = \"" + buttonName + "\"] or normalize-space(.) = \"" + buttonName + "\"]");
        if (enabled) {
            return elementToBeClickable(getDriver(), locator);
        }
        return visibilityOfElementLocated(getDriver(), locator);
    }

    // --- Issue #1754: in-place 'Business Terms' chip field helpers (shared by ASBIE/BBIE panels) ---
    //
    // The chip field is a mat-chip-grid.bt-chip-set carrying data-bie-type. For the CURRENT (editable)
    // tab it lives inside a .bt-badges-field that is NOT .bt-badges-field-readonly; the base
    // (inherited) tab renders a second, read-only copy. Scoping the current-tab lookups to the
    // non-readonly wrapper keeps them off the base-tab copy.

    private WebElement getBusinessTermChipFieldByType(String bieType, boolean readonly) {
        String readonlyPredicate = readonly
                ? "[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field-readonly \")]"
                : "[not(contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field-readonly \"))]";
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-form-field[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field \")]"
                        + readonlyPredicate
                        + "//mat-chip-grid[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-set \")]"
                        + "[@data-bie-type=\"" + bieType + "\"]"));
    }

    private List<WebElement> getBusinessTermChips(WebElement chipField) {
        return chipField.findElements(By.xpath(
                ".//mat-chip-row[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip \")]"));
    }

    private WebElement getBusinessTermChipByTerm(WebElement chipField, String businessTerm) {
        return chipField.findElement(By.xpath(
                ".//mat-chip-row[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip \")]"
                        + "[.//span[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-term \")]"
                        + "[normalize-space(.)=\"" + businessTerm + "\"]]"));
    }

    private WebElement getPreferredStar(WebElement chip) {
        return chip.findElement(By.xpath(
                ".//mat-icon[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-star \")]"));
    }

    private boolean isChipPreferred(WebElement chip) {
        String klass = getPreferredStar(chip).getAttribute("class");
        return klass != null && klass.contains("bt-chip-star-on");
    }

    private void clickPreferredStar(WebElement chip) {
        click(getDriver(), getPreferredStar(chip));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    private String getChipTypeCode(WebElement chip) {
        List<WebElement> pills = chip.findElements(By.xpath(
                ".//span[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-type \")]"));
        return pills.isEmpty() ? null : getText(pills.get(0));
    }

    private void startTypeCodeInlineEdit(WebElement chip) {
        // Avoid PageHelper.click's post-click pause: the badge hover preview can open over the chip
        // before Selenium begins typing, which blurs and cancels the inline editor.
        chip.findElement(By.xpath(
                ".//span[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-term \")]"))
                .click();
        visibilityOfElementLocated(getDriver(), By.cssSelector("input.bt-chip-type-input"));
    }

    private void setTypeCodeInlineEditValue(String typeCode) {
        WebElement input = visibilityOfElementLocated(getDriver(), By.cssSelector("input.bt-chip-type-input"));
        // Clearing the input blurs it and cancels the inline editor. Replace its contents in one
        // keyboard action so Angular keeps the editor mounted while ngModel receives the new value.
        Keys selectAllModifier = System.getProperty("os.name", "").toLowerCase().contains("mac")
                ? Keys.COMMAND : Keys.CONTROL;
        input.sendKeys(Keys.chord(selectAllModifier, "a"), typeCode);
    }

    private void saveTypeCodeInlineEdit() {
        click(getDriver(), elementToBeClickable(getDriver(), By.xpath(
                "//button[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-chip-type-action \")]")));
        waitFor(ofMillis(500L));
    }

    private String getTypeCodeInlineError(WebElement chipField) {
        // The mat-error lives on the same .bt-badges-field wrapper as the chip grid.
        List<WebElement> errors = chipField.findElements(By.xpath(
                "ancestor::mat-form-field[1]//mat-error"));
        if (errors.isEmpty()) {
            return "";
        }
        String text = getText(errors.get(0));
        return text == null ? "" : text;
    }

    private WebElement getAddBusinessTermButton(String bieType) {
        // The '+' button is a matSuffix sibling of the chip grid within the same .bt-badges-field.
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-form-field[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field \")]"
                        + "[not(contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field-readonly \"))]"
                        + "[.//mat-chip-grid[@data-bie-type=\"" + bieType + "\"]]"
                        + "//button[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-add-btn \")]"));
    }

    private BieBusinessTermAssignDialog openBusinessTermAssignDialog(String bieType) {
        click(getDriver(), elementToBeClickable(getDriver(), By.xpath(
                "//mat-form-field[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field \")]"
                        + "[not(contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field-readonly \"))]"
                        + "[.//mat-chip-grid[@data-bie-type=\"" + bieType + "\"]]"
                        + "//button[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-add-btn \")]")));
        waitFor(ofMillis(1000L));
        BieBusinessTermAssignDialog dialog = new BieBusinessTermAssignDialogImpl(this);
        assert dialog.isOpened();
        return dialog;
    }

    private void removeBusinessTermChip(WebElement chip) {
        click(getDriver(), chip.findElement(By.xpath(".//button[@matChipRemove]")));
        // A "Remove this business term assignment?" confirmation dialog appears; its confirm action
        // button is labelled "Remove" (bie-edit.component.ts, dialogConfig.data.action = 'Remove').
        click(getDriver(), getDialogButtonByName(getDriver(), "Remove"));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    private WebElement getBusinessTermHoverCard(WebElement chip) {
        new Actions(getDriver()).moveToElement(chip).perform();
        waitFor(ofMillis(500L));
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-card[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-hover-card \")]"));
    }

    private void switchToNewWindow(Set<String> windowHandlesBeforeClick) {
        PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L))
                .until(driver -> driver.getWindowHandles().size() > windowHandlesBeforeClick.size());
        for (String winHandle : getDriver().getWindowHandles()) {
            if (!windowHandlesBeforeClick.contains(winHandle)) {
                getDriver().switchTo().window(winHandle);
                return;
            }
        }
        throw new TimeoutException("Failed to switch to a newly opened window.");
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

        @Override
        public TopLevelASBIEPPanel getBaseTopLevelASBIEPPanel() {
            WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-tab-header//div[@role=\"tab\"][2]"));
            click(tab);
            return this;
        }
    }

    @Override
    public ReusedASBIEPanel getReusedASBIEPanel(WebElement asccpNode) {
        return retry(() -> {
            // A reused ASBIEP node can open the reused BIE when its center lands on the Reused icon.
            // Select the node through its label so the detail panel stays in the current tab.
            clickLabel(asccpNode);
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
            return getInputFieldByName(this.baseXPath, "Release");
        }

        @Override
        public WebElement getStateField() {
            return getInputFieldByName(this.baseXPath, "State");
        }

        @Override
        public WebElement getOwnerField() {
            return getInputFieldByName(this.baseXPath, "Owner");
        }

        @Override
        public WebElement getBusinessContextField() {
            return getInputFieldByName(this.baseXPath, "Business Context");
        }

        @Override
        public WebElement getLegacyBusinessTermField() {
            return getInputFieldByName(this.baseXPath, "Legacy Business Term");
        }

        @Override
        public WebElement getRemarkField() {
            return getInputFieldByName(this.baseXPath, "Remark");
        }

        @Override
        public WebElement getVersionField() {
            return getInputFieldByName(this.baseXPath, "Version");
        }

        @Override
        public WebElement getStatusField() {
            return getInputFieldByName(this.baseXPath, "Status");
        }

        @Override
        public WebElement getContextDefinitionField() {
            return getTextAreaFieldByName(this.baseXPath, "Context Definition");
        }
    }

    private class ASBIEPanelImpl implements ASBIEPanel {

        private static final String ACTIVE_ASBIE_DETAIL_TAB_XPATH =
                "(//div[contains(@class, \"bie-edit-detail-panel\")]//mat-tab-body[" +
                        "(contains(@class, \"mat-mdc-tab-body-active\") or contains(@class, \"mat-tab-body-active\"))" +
                        " and .//textarea[@placeholder = \"Association Definition\"]" +
                        " and .//button[.//*[normalize-space(.) = \"Assign Business Term\"] or normalize-space(.) = \"Assign Business Term\"]])[1]";

        @Override
        public BusinessTermAssignmentPage clickShowBusinessTermsButton() {
            Set<String> windowHandlesBeforeClick = new HashSet<>(getDriver().getWindowHandles());
            click(getShowBusinessTermsButton());
            switchToNewWindow(windowHandlesBeforeClick);
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
            Set<String> windowHandlesBeforeClick = new HashSet<>(getDriver().getWindowHandles());
            click(getAssignBusinessTermButton(true));
            switchToNewWindow(windowHandlesBeforeClick);
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
            return getButtonInActiveDetailTab(ACTIVE_ASBIE_DETAIL_TAB_XPATH, "Show Business Terms", true);
        }

        @Override
        public WebElement getAssignBusinessTermButton(boolean enabled) {
            return getButtonInActiveDetailTab(ACTIVE_ASBIE_DETAIL_TAB_XPATH, "Assign Business Term", enabled);
        }

        // --- Issue #1754: in-place 'Business Terms' chip field ---

        @Override
        public WebElement getBusinessTermChipField() {
            return getBusinessTermChipFieldByType("ASBIE", false);
        }

        @Override
        public List<WebElement> getBusinessTermChips() {
            return EditBIEPageImpl.this.getBusinessTermChips(getBusinessTermChipField());
        }

        @Override
        public WebElement getBusinessTermChipByTerm(String businessTerm) {
            return EditBIEPageImpl.this.getBusinessTermChipByTerm(getBusinessTermChipField(), businessTerm);
        }

        @Override
        public WebElement getPreferredStar(WebElement chip) {
            return EditBIEPageImpl.this.getPreferredStar(chip);
        }

        @Override
        public boolean isChipPreferred(WebElement chip) {
            return EditBIEPageImpl.this.isChipPreferred(chip);
        }

        @Override
        public void clickPreferredStar(WebElement chip) {
            EditBIEPageImpl.this.clickPreferredStar(chip);
        }

        @Override
        public String getChipTypeCode(WebElement chip) {
            return EditBIEPageImpl.this.getChipTypeCode(chip);
        }

        @Override
        public void startTypeCodeInlineEdit(WebElement chip) {
            EditBIEPageImpl.this.startTypeCodeInlineEdit(chip);
        }

        @Override
        public void setTypeCodeInlineEditValue(String typeCode) {
            EditBIEPageImpl.this.setTypeCodeInlineEditValue(typeCode);
        }

        @Override
        public void saveTypeCodeInlineEdit() {
            EditBIEPageImpl.this.saveTypeCodeInlineEdit();
        }

        @Override
        public String getTypeCodeInlineError() {
            return EditBIEPageImpl.this.getTypeCodeInlineError(getBusinessTermChipField());
        }

        @Override
        public WebElement getAddBusinessTermButton() {
            return EditBIEPageImpl.this.getAddBusinessTermButton("ASBIE");
        }

        @Override
        public BieBusinessTermAssignDialog openBusinessTermAssignDialog() {
            return EditBIEPageImpl.this.openBusinessTermAssignDialog("ASBIE");
        }

        @Override
        public void removeBusinessTermChip(WebElement chip) {
            EditBIEPageImpl.this.removeBusinessTermChip(chip);
        }

        @Override
        public WebElement getBusinessTermHoverCard(WebElement chip) {
            return EditBIEPageImpl.this.getBusinessTermHoverCard(chip);
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

        @Override
        public ASBIEPanel getBaseASBIEPanel() {
            By inheritedTabLocator = By.xpath(
                    "//div[contains(@class, \"bie-edit-detail-panel\")]//mat-tab-header//div[@role=\"tab\"]" +
                            "[.//span[contains(normalize-space(.), \"Inherits from\")]]");
            Wait<WebDriver> panelWait = PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L));
            WebElement tab = panelWait.until(driver -> driver.findElements(inheritedTabLocator).stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(null));
            click(tab);
            return this;
        }
    }

    private class BBIEPanelImpl implements BBIEPanel {

        private static final String ACTIVE_BBIE_DETAIL_TAB_XPATH =
                "(//div[contains(@class, \"bie-edit-detail-panel\")]//mat-tab-body[" +
                        "(contains(@class, \"mat-mdc-tab-body-active\") or contains(@class, \"mat-tab-body-active\"))" +
                        " and .//input[@placeholder = \"Example\"]" +
                        " and .//button[.//*[normalize-space(.) = \"Assign Business Term\"] or normalize-space(.) = \"Assign Business Term\"]])[1]";

        @Override
        public WebElement getBusinessTermField() {
            return getInputFieldByName("Business Term");
        }

        @Override
        public WebElement getShowBusinessTermsButton() {
            return getButtonInActiveDetailTab(ACTIVE_BBIE_DETAIL_TAB_XPATH, "Show Business Terms", true);
        }

        @Override
        public WebElement getAssignBusinessTermButton(boolean enabled) {
            return getButtonInActiveDetailTab(ACTIVE_BBIE_DETAIL_TAB_XPATH, "Assign Business Term", enabled);
        }

        // --- Issue #1754: in-place 'Business Terms' chip field ---

        @Override
        public WebElement getBusinessTermChipField() {
            return getBusinessTermChipFieldByType("BBIE", false);
        }

        @Override
        public List<WebElement> getBusinessTermChips() {
            return EditBIEPageImpl.this.getBusinessTermChips(getBusinessTermChipField());
        }

        @Override
        public WebElement getBusinessTermChipByTerm(String businessTerm) {
            return EditBIEPageImpl.this.getBusinessTermChipByTerm(getBusinessTermChipField(), businessTerm);
        }

        @Override
        public WebElement getPreferredStar(WebElement chip) {
            return EditBIEPageImpl.this.getPreferredStar(chip);
        }

        @Override
        public boolean isChipPreferred(WebElement chip) {
            return EditBIEPageImpl.this.isChipPreferred(chip);
        }

        @Override
        public void clickPreferredStar(WebElement chip) {
            EditBIEPageImpl.this.clickPreferredStar(chip);
        }

        @Override
        public String getChipTypeCode(WebElement chip) {
            return EditBIEPageImpl.this.getChipTypeCode(chip);
        }

        @Override
        public void startTypeCodeInlineEdit(WebElement chip) {
            EditBIEPageImpl.this.startTypeCodeInlineEdit(chip);
        }

        @Override
        public void setTypeCodeInlineEditValue(String typeCode) {
            EditBIEPageImpl.this.setTypeCodeInlineEditValue(typeCode);
        }

        @Override
        public void saveTypeCodeInlineEdit() {
            EditBIEPageImpl.this.saveTypeCodeInlineEdit();
        }

        @Override
        public String getTypeCodeInlineError() {
            return EditBIEPageImpl.this.getTypeCodeInlineError(getBusinessTermChipField());
        }

        @Override
        public WebElement getAddBusinessTermButton() {
            return EditBIEPageImpl.this.getAddBusinessTermButton("BBIE");
        }

        @Override
        public BieBusinessTermAssignDialog openBusinessTermAssignDialog() {
            return EditBIEPageImpl.this.openBusinessTermAssignDialog("BBIE");
        }

        @Override
        public void removeBusinessTermChip(WebElement chip) {
            EditBIEPageImpl.this.removeBusinessTermChip(chip);
        }

        @Override
        public WebElement getBusinessTermHoverCard(WebElement chip) {
            return EditBIEPageImpl.this.getBusinessTermHoverCard(chip);
        }

        @Override
        public BusinessTermAssignmentPage clickShowBusinessTermsButton() {
            Set<String> windowHandlesBeforeClick = new HashSet<>(getDriver().getWindowHandles());
            click(getShowBusinessTermsButton());
            switchToNewWindow(windowHandlesBeforeClick);
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
            Set<String> windowHandlesBeforeClick = new HashSet<>(getDriver().getWindowHandles());
            click(getAssignBusinessTermButton(true));
            switchToNewWindow(windowHandlesBeforeClick);
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
                    "//mat-form-field[.//mat-label[contains(text(), \"Value Constraint\")]]//mat-select"));
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
                    "//mat-form-field[.//mat-label[contains(text(), \"Value Domain Restriction\")]]//mat-select"));
        }

        @Override
        public void setValueDomainRestriction(String valueDomainRestriction) {
            click(getValueDomainRestrictionSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomainRestriction + "\")]//ancestor::mat-option[1]")));
            escape(getDriver());
            invisibilityOfLoadingContainerElement(getDriver());
            waitFor(ofMillis(1000L));
        }

        @Override
        public WebElement getValueDomainField() {
            return elementToBeClickable(getDriver(), VALUE_DOMAIN_SELECT_FIELD_LOCATOR);
        }

        @Override
        public void setValueDomain(String valueDomain) {
            retry(() -> {
                WebElement valueDomainField = getValueDomainField();
                click(getDriver(), valueDomainField);
                waitFor(ofMillis(500L));
                WebElement dropdownSearchField;
                try {
                    dropdownSearchField = visibilityOfElementLocated(
                            PageHelper.wait(getDriver(), Duration.ofSeconds(2L), ofMillis(100L)),
                            DROPDOWN_SEARCH_FIELD_LOCATOR);
                } catch (TimeoutException e) {
                    try {
                        WebElement trigger = valueDomainField.findElement(By.cssSelector(".mat-mdc-select-trigger"));
                        click(getDriver(), trigger);
                    } catch (Exception ignored) {
                        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", valueDomainField);
                    }
                    dropdownSearchField = visibilityOfElementLocated(
                            PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                            DROPDOWN_SEARCH_FIELD_LOCATOR);
                }
                sendKeys(dropdownSearchField, valueDomain);
                waitFor(ofMillis(500L));
                click(getDriver(), elementToBeClickable(
                        PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                        By.xpath("//mat-option//span[contains(text(), \"" + valueDomain + "\")]")));
                escape(getDriver());
            });
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
            return retry(() -> {
                WebElement valueDomainSelect = openMatSelect(getDriver(), VALUE_DOMAIN_SELECT_FIELD_LOCATOR);
                try {
                    sendKeys(matSelectSearchField(getDriver(), valueDomainSelect), valueDomain);
                    WebElement valueDomainElement = visibilityOfElementLocated(getDriver(), By.xpath(
                            "//mat-option//span[contains(text(), \"" + valueDomain + "\")]//ancestor::mat-option[1]/span/div"));
                    new Actions(getDriver()).moveToElement(valueDomainElement).perform(); // mouse over
                    return getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-tooltip-component")));
                } finally {
                    pressEscape();
                }
            });
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

        @Override
        public BBIEPanel getBaseBBIEPanel() {
            WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-tab-header//div[@role=\"tab\"][2]"));
            click(tab);
            return this;
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
                    "//mat-form-field[.//mat-label[contains(text(), \"Value Constraint\")]]//mat-select"));
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
                    "//mat-form-field[.//mat-label[contains(text(), \"Value Domain Restriction\")]]//mat-select"));
        }

        @Override
        public void setValueDomainRestriction(String valueDomainRestriction) {
            click(getValueDomainRestrictionSelectField());
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + valueDomainRestriction + "\")]//ancestor::mat-option[1]")));
            escape(getDriver());
            invisibilityOfLoadingContainerElement(getDriver());
            waitFor(ofMillis(1000L));
        }

        @Override
        public WebElement getValueDomainField() {
            return visibilityOfElementLocated(getDriver(), By.xpath(
                    "//mat-form-field[.//mat-label[normalize-space(text()) = \"Value Domain\"]]//mat-select"));
        }

        @Override
        public void setValueDomain(String valueDomain) {
            retry(() -> {
                WebElement valueDomainField = getValueDomainField();
                click(getDriver(), valueDomainField);
                waitFor(ofMillis(500L));
                WebElement dropdownSearchField;
                try {
                    dropdownSearchField = visibilityOfElementLocated(
                            PageHelper.wait(getDriver(), Duration.ofSeconds(2L), ofMillis(100L)),
                            DROPDOWN_SEARCH_FIELD_LOCATOR);
                } catch (TimeoutException e) {
                    try {
                        WebElement trigger = valueDomainField.findElement(By.cssSelector(".mat-mdc-select-trigger"));
                        click(getDriver(), trigger);
                    } catch (Exception ignored) {
                        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", valueDomainField);
                    }
                    dropdownSearchField = visibilityOfElementLocated(
                            PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                            DROPDOWN_SEARCH_FIELD_LOCATOR);
                }
                sendKeys(dropdownSearchField, valueDomain);
                waitFor(ofMillis(500L));
                click(getDriver(), elementToBeClickable(
                        PageHelper.wait(getDriver(), Duration.ofSeconds(10L), ofMillis(100L)),
                        By.xpath("//mat-option//span[contains(text(), \"" + valueDomain + "\")]")));
                escape(getDriver());
            });
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
        public BBIESCPanel getBaseBBIESCPanel() {
            WebElement tab = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-tab-header//div[@role=\"tab\"][2]"));
            click(tab);
            return this;
        }
    }

    /**
     * Issue #1519: drives the BIE-root 'OpenAPI Document Information' panel — one card per binding. Every
     * setter reuses the same Angular Material controls the OpenAPI Document editor uses, and the panel saves
     * through the same backend endpoints, so an edit here lands in the database exactly as it would on the
     * OpenAPI Document screen.
     */
    private class OpenAPIDocumentInformationPanelImpl implements EditBIEPage.OpenAPIDocumentInformationPanel {

        // Exact class-token match: a substring contains() would also catch the child 'oas-binding-card-header'
        // and 'oas-binding-card-body' divs, inflating the card count.
        private static final String CARD =
                "//div[contains(concat(\" \", normalize-space(@class), \" \"), \" oas-binding-card \")]";

        private WebElement fieldSelect(WebElement card, String label) {
            return card.findElement(By.xpath(
                    ".//mat-form-field[.//mat-label[normalize-space(.) = \"" + label + "\"]]//mat-select"));
        }

        private WebElement fieldInput(WebElement card, String label) {
            return card.findElement(By.xpath(
                    ".//mat-form-field[.//mat-label[normalize-space(.) = \"" + label + "\"]]//input"));
        }

        private WebElement checkbox(WebElement card, String label) {
            return card.findElement(By.xpath(
                    ".//mat-checkbox[contains(normalize-space(.), \"" + label + "\")]"));
        }

        private void chooseOption(String optionText) {
            WebElement option = elementToBeClickable(getDriver(),
                    By.xpath("//mat-option[.//span[normalize-space(.) = \"" + optionText + "\"]]"));
            click(getDriver(), option);
            waitFor(ofMillis(300L));
        }

        private String fieldError(WebElement card, String label) {
            try {
                return getText(card.findElement(By.xpath(
                        ".//mat-form-field[.//mat-label[normalize-space(.) = \"" + label + "\"]]//mat-error")));
            } catch (NoSuchElementException e) {
                return "";
            }
        }

        @Override
        public boolean isEmptyStateDisplayed() {
            List<WebElement> empties = getDriver().findElements(By.xpath(
                    OAS_INFO_PANEL_XPATH + "//div[contains(@class, \"oas-doc-information-empty\")]"));
            return !empties.isEmpty() && empties.get(0).isDisplayed();
        }

        @Override
        public boolean isAddButtonDisplayed() {
            return !getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH +
                    "//mat-expansion-panel-header//button[contains(@class, \"info-panel-header-add\")]")).isEmpty();
        }

        @Override
        public int getBindingCardCount() {
            return getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH + CARD)).size();
        }

        @Override
        public WebElement getBindingCard(BigInteger oasDocId) {
            return visibilityOfElementLocated(getDriver(), By.xpath(OAS_INFO_PANEL_XPATH + CARD +
                    "[.//a[substring-after(@href, \"/oas_doc/\") = \"" + oasDocId + "\"]]"));
        }

        @Override
        public WebElement getBindingCardByOperationId(String operationId) {
            for (WebElement card : getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH + CARD))) {
                if (operationId.equals(getOperationId(card))) {
                    return card;
                }
            }
            throw new NoSuchElementException("No binding card with Operation ID: " + operationId);
        }

        @Override
        public List<WebElement> getBindingCards() {
            return getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH + CARD));
        }

        @Override
        public String getDocumentChipText(WebElement card) {
            return getText(card.findElement(By.xpath(".//span[contains(@class, \"oas-doc-chip-text\")]")));
        }

        @Override
        public String getVerb(WebElement card) {
            return getText(fieldSelect(card, "Verb"));
        }

        @Override
        public void setVerb(WebElement card, String verb) {
            click(getDriver(), fieldSelect(card, "Verb"));
            chooseOption(verb);
        }

        @Override
        public String getMessageBody(WebElement card) {
            return getText(fieldSelect(card, "Message Body"));
        }

        @Override
        public void setMessageBody(WebElement card, String messageBody) {
            click(getDriver(), fieldSelect(card, "Message Body"));
            chooseOption(messageBody);
        }

        @Override
        public String getResourceName(WebElement card) {
            return fieldInput(card, "Resource Name").getAttribute("value");
        }

        @Override
        public void setResourceName(WebElement card, String resourceName) {
            WebElement input = fieldInput(card, "Resource Name");
            clear(input);
            sendKeys(input, resourceName);
        }

        @Override
        public String getResourceNameError(WebElement card) {
            return fieldError(card, "Resource Name");
        }

        @Override
        public String getOperationId(WebElement card) {
            return fieldInput(card, "Operation ID").getAttribute("value");
        }

        @Override
        public void setOperationId(WebElement card, String operationId) {
            WebElement input = fieldInput(card, "Operation ID");
            clear(input);
            sendKeys(input, operationId);
        }

        @Override
        public String getOperationIdError(WebElement card) {
            return fieldError(card, "Operation ID");
        }

        @Override
        public String getTag(WebElement card) {
            return fieldInput(card, "Tag").getAttribute("value");
        }

        @Override
        public void setTag(WebElement card, String tag) {
            WebElement input = fieldInput(card, "Tag");
            clear(input);
            sendKeys(input, tag);
        }

        @Override
        public boolean isArrayChecked(WebElement card) {
            return isChecked(checkbox(card, "Make as an array"));
        }

        @Override
        public void setArray(WebElement card, boolean checked) {
            WebElement box = checkbox(card, "Make as an array");
            if (isChecked(box) != checked) {
                click(getDriver(), box.findElement(By.tagName("input")));
                waitFor(ofMillis(300L));
            }
        }

        @Override
        public boolean isSuppressRootChecked(WebElement card) {
            return isChecked(checkbox(card, "Suppress a root property"));
        }

        @Override
        public void setSuppressRoot(WebElement card, boolean checked) {
            WebElement box = checkbox(card, "Suppress a root property");
            if (isChecked(box) != checked) {
                click(getDriver(), box.findElement(By.tagName("input")));
                waitFor(ofMillis(300L));
            }
        }

        @Override
        public String getErrorResponseBodyType(WebElement card) {
            return getText(fieldSelect(card, "Error Response"));
        }

        @Override
        public void setErrorResponseBodyType(WebElement card, String label) {
            click(getDriver(), fieldSelect(card, "Error Response"));
            chooseOption(label);
        }

        @Override
        public boolean isDeleteRequestBodyIgnoredWarningDisplayed(WebElement card) {
            List<WebElement> warnings = card.findElements(
                    By.xpath(".//div[contains(@class, \"oas-delete-body-warning\")]"));
            return !warnings.isEmpty() && warnings.get(0).isDisplayed();
        }

        @Override
        public void unbind(WebElement card) {
            click(getDriver(), card.findElement(By.xpath(".//button[contains(@class, \"oas-binding-discard\")]")));
            WebElement confirmRemoveButton = elementToBeClickable(getDriver(), By.xpath(
                    "//mat-dialog-container//span[contains(text(), \"Remove\")]//ancestor::button[1]"));
            click(getDriver(), confirmRemoveButton);
            invisibilityOfLoadingContainerElement(getDriver());
            assert "Removed from OpenAPI Document.".equals(getSnackBarMessage(getDriver()));
        }

        @Override
        public BieOpenAPIDocumentAddDialog openAddDialog() {
            click(getDriver(), elementToBeClickable(getDriver(), By.xpath(OAS_INFO_PANEL_XPATH +
                    "//mat-expansion-panel-header//button[contains(@class, \"info-panel-header-add\")]")));
            waitFor(ofMillis(1000L));
            BieOpenAPIDocumentAddDialog dialog = new BieOpenAPIDocumentAddDialogImpl(EditBIEPageImpl.this);
            assert dialog.isOpened();
            return dialog;
        }

        @Override
        public boolean isUpdateButtonEnabled() {
            List<WebElement> buttons = getDriver().findElements(By.xpath(OAS_INFO_PANEL_XPATH +
                    "//button[normalize-space(.) = \"Update OpenAPI Information\"]"));
            return !buttons.isEmpty() && buttons.get(0).isEnabled();
        }

        @Override
        public void hitUpdateButton() {
            click(getDriver(), elementToBeClickable(getDriver(), By.xpath(OAS_INFO_PANEL_XPATH +
                    "//button[normalize-space(.) = \"Update OpenAPI Information\"]")));
            invisibilityOfLoadingContainerElement(getDriver());
            assert "Updated OpenAPI Document information.".equals(getSnackBarMessage(getDriver()));
        }
    }

}
