package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.UpliftBIEVerificationPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftBIEVerificationPageImpl extends BasePageImpl implements UpliftBIEVerificationPage {

    private final BasePage parent;

    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");

    private static final By UPLIFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");

    private static final By UNSELECTED_REUSE_WARNING_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Proceed without selecting reuse BIEs\")]");

    private static final By UNSELECTED_REUSE_CONTINUE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Continue\")]//ancestor::button[1]");

    private static final By SOURCE_SEARCH_INPUT_LOCATOR =
            By.xpath("(//score-bie-uplift//div[contains(@class, 'tree-search-box')]//input[@type='search'])[1]");

    private static final By TARGET_SEARCH_INPUT_LOCATOR =
            By.xpath("(//score-bie-uplift//div[contains(@class, 'tree-search-box')]//input[@type='search'])[2]");

    public UpliftBIEVerificationPageImpl(BasePage parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/uplift").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Uplift BIE".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    public void expandNodeInSourceBIE(String node) {
        By chevronRightLocator = By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[1]//cdk-virtual-scroll-viewport//span[contains(text(), \"" + node + "\")]" +
                "//ancestor::div[1]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]"
        );
        click(elementToBeClickable(getDriver(), chevronRightLocator));

    }

    public void expandNodeInTargetBIE(String node) {
        By chevronRightLocator = By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//span[contains(text(), \"" + node + "\")]" +
                "//ancestor::div[1]//mat-icon[contains(text(), \"chevron_right\")]//ancestor::button[1]"
        );
        click(elementToBeClickable(getDriver(), chevronRightLocator));
    }

    @Override
    public WebElement goToNodeInSourceBIE(String nodePath) {
        return goToNodeBySearch(1, nodePath);
    }

    @Override
    public WebElement goToNodeInTargetBIE(String nodePath) {
        return goToNodeBySearch(2, nodePath);
    }

    @Override
    public WebElement goToNodeInSourceBIEByExpandingPath(String nodePath) {
        return goToNodeBySearch(1, nodePath);
    }

    @Override
    public WebElement goToNodeInTargetBIEByExpandingPath(String nodePath) {
        return goToNodeBySearch(2, nodePath);
    }

    private void clickLabel(WebElement node) {
        try {
            WebElement label = node.findElement(By.xpath(".//span[contains(@class, 'inside') or contains(@class, 'outside')]"));
            click(getDriver(), label);
        } catch (Exception ignored) {
            click(getDriver(), node);
        }
    }

    private WebElement goToNodeBySearch(int treeIndex, String nodePath) {
        searchTree(treeIndex, nodePath);
        WebElement node = retry(() -> visibilityOfElementLocated(getDriver(), nodeLocator(treeIndex, nodePath)));
        clickLabel(node);
        return node;
    }

    private void searchTree(int treeIndex, String nodePath) {
        retry(() -> {
            WebElement searchInput = (treeIndex == 1) ? getSearchInputOfSourceTree() : getSearchInputOfTargetTree();
            click(getDriver(), searchInput);
            clear(searchInput);
            WebElement input = sendKeys(searchInput, nodePath);
            if (!nodePath.equals(input.getAttribute("value"))) {
                throw new WebDriverException();
            }
            for (int i = 0; i < 3; ++i) {
                input.sendKeys(Keys.ENTER);
                waitFor(ofMillis(300L));
            }
            try {
                WebElement searchBtn = getDriver().findElement(By.xpath(
                        "(//mat-card-content//mat-icon[normalize-space(.)='search' or normalize-space(.)='repeat']//ancestor::button[1])[" + treeIndex + "]"));
                if (searchBtn.isDisplayed() && searchBtn.isEnabled()) {
                    click(getDriver(), searchBtn);
                }
            } catch (Exception ignored) {
            }
            waitFor(ofMillis(800L));
        });
    }

    private By nodeLocator(int treeIndex, String nodePath) {
        String queryPath = nodePath.replaceFirst("^/", "").replaceAll("\\s+", "");
        String tree = "//mat-card-content[contains(@class, \"mat-mdc-card-content\")]/div[2]/div[" + treeIndex + "]";
        if (nodePath.startsWith("/")) {
            return By.xpath(tree + "//div[contains(@class, \"mat-tree-node\")][@data-query-path=" +
                    xpathLiteral(queryPath) + "]");
        }
        return By.xpath(tree + "//div[contains(@class, \"mat-tree-node\")][.//span[contains(@class, 'node-label') and " +
                "normalize-space(.) = " + xpathLiteral(nodePath) + "]]");
    }

    @Override
    public WebElement getSearchInputOfSourceTree() {
        return elementToBeClickable(getDriver(), SOURCE_SEARCH_INPUT_LOCATOR);
    }

    @Override
    public WebElement getSearchInputOfTargetTree() {
        return elementToBeClickable(getDriver(), TARGET_SEARCH_INPUT_LOCATOR);
    }

    @Override
    public WebElement getCheckBoxOfNodeInTargetBIE(String node) {
        WebElement targetNode = visibilityOfElementLocated(getDriver(), nodeLocator(2, node));
        return targetNode.findElement(By.xpath("./mat-checkbox[1]"));
    }

    @Override
    public void mapNode(String sourcePath, String targetPath) {
        changeNodeMapping(sourcePath, targetPath, true);
    }

    @Override
    public void cancelNodeMapping(String sourcePath, String targetPath) {
        changeNodeMapping(sourcePath, targetPath, false);
    }

    private void changeNodeMapping(String sourcePath, String targetPath, boolean accept) {
        goToNodeInSourceBIE(sourcePath);
        goToNodeInTargetBIE(targetPath);
        By targetCheckbox = new ByChained(nodeLocator(2, targetPath),
                By.cssSelector("mat-checkbox"));
        By targetCheckboxTouchTarget = new ByChained(nodeLocator(2, targetPath),
                By.cssSelector("mat-checkbox .mat-mdc-checkbox-touch-target"));
        WebElement checkbox = visibilityOfElementLocated(getDriver(), targetCheckbox);
        WebElement checkboxInput = checkbox.findElement(By.cssSelector("input[type='checkbox']"));
        if (!checkboxInput.isEnabled()) {
            String reason = checkboxInput.getAttribute("aria-label");
            if (reason == null || reason.isBlank()) {
                reason = checkbox.getAttribute("aria-label");
            }
            if (reason == null || reason.isBlank()) {
                reason = "Target mapping checkbox is disabled";
            }
            throw new IllegalArgumentException("Cannot map '" + sourcePath + "' to '" + targetPath
                    + "': " + reason);
        }
        boolean previouslySelected = isCheckboxSelected(checkbox);
        if (accept && previouslySelected) {
            return;
        }
        // Angular Material attaches its stable click handler to the touch
        // target. Clicking it avoids depending on the internal input or on an
        // empty label that Material may hide when no label text is supplied.
        click(getDriver(), elementToBeClickable(getDriver(), targetCheckboxTouchTarget));

        // Handle the optional mapping confirmation dialog.
        By confirmation = By.xpath("//mat-dialog-container[.//*[normalize-space(.)='Mapping confirmation']]");
        try {
            visibilityOfElementLocated(accept ? shortWait(getDriver()) : defaultWait(getDriver()), confirmation);
            String action = accept ? "Continue" : "Cancel";
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//mat-dialog-container[.//*[normalize-space(.)='Mapping confirmation']]"
                            + "//button[.//span[normalize-space(.)='" + action + "']]")));
            invisibilityOfElementLocated(getDriver(), confirmation);
        } catch (TimeoutException e) {
            if (!getDriver().findElements(confirmation).isEmpty()) {
                throw e;
            }

            // Some compatible mappings do not open a confirmation dialog. A
            // cancel request must still preserve the selection that existed
            // before the click, so undo the toggle when it changed the state.
            if (!accept && isCheckboxSelected(getDriver().findElement(targetCheckbox)) != previouslySelected) {
                click(getDriver(), elementToBeClickable(getDriver(), targetCheckboxTouchTarget));
                waitForCheckboxSelection(targetCheckbox, previouslySelected);
                return;
            }
        }
        waitForCheckboxSelection(targetCheckbox, accept || previouslySelected);
    }

    private void waitForCheckboxSelection(By checkboxLocator, boolean expectedSelection) {
        defaultWait(getDriver()).until(driver -> {
            try {
                return isCheckboxSelected(driver.findElement(checkboxLocator)) == expectedSelection;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                return false;
            }
        });
    }

    private boolean isCheckboxSelected(WebElement checkbox) {
        try {
            return checkbox.findElement(By.cssSelector("input[type='checkbox']")).isSelected();
        } catch (NoSuchElementException e) {
            // Newer Material builds can render the host before its native
            // input. Fall back to the host state exposed by Material.
        }

        String ariaChecked = checkbox.getAttribute("aria-checked");
        if (ariaChecked != null) {
            return Boolean.parseBoolean(ariaChecked);
        }

        String reflectedChecked = checkbox.getAttribute("ng-reflect-checked");
        if (reflectedChecked != null) {
            return Boolean.parseBoolean(reflectedChecked);
        }

        String classes = checkbox.getAttribute("class");
        if (classes != null) {
            return classes.contains("mat-mdc-checkbox-checked")
                    || classes.contains("mat-checkbox-checked")
                    || classes.contains("mdc-checkbox--selected");
        }

        return false;
    }

    @Override
    public SelectProfileBIEToReuseDialog reuseBIEOnNode(String path, String nodeName) {
        return openReuseBIEDialog(goToNodeInTargetBIE(path));
    }

    @Override
    public SelectProfileBIEToReuseDialog reuseBIEOnNodeByExpandingPath(String path, String nodeName) {
        return openReuseBIEDialog(goToNodeInTargetBIEByExpandingPath(path));
    }

    private SelectProfileBIEToReuseDialog openReuseBIEDialog(WebElement nodeInTargetBIE) {
        try {
            click(nodeInTargetBIE.findElement(By.xpath(
                    "./ancestor-or-self::div[contains(@class, \"mat-tree-node\")][1]" +
                            "//fa-icon[@mattooltip=\"Select BIE\" or @mattooltip=\"Reused\"]")));
        } catch (TimeoutException | NoSuchElementException e) {
            click(nodeInTargetBIE);
            new Actions(getDriver()).sendKeys("O").perform();
            click(nodeInTargetBIE.findElement(By.xpath(
                    "./ancestor-or-self::div[contains(@class, \"mat-tree-node\")][1]" +
                            "//fa-icon[@mattooltip=\"Select BIE\" or @mattooltip=\"Reused\"]")));
        }
        waitFor(ofMillis(1000L));

        SelectProfileBIEToReuseDialog selectProfileBIEToReuse = new SelectProfileBIEToReuseDialogImpl(this, "Reuse BIE");
        assert selectProfileBIEToReuse.isOpened();
        return selectProfileBIEToReuse;
    }

    @Override
    public WebElement getReusedIconOfNodeInTargetBIE(String nodeName) {
        WebElement targetNode = visibilityOfElementLocated(getDriver(), nodeLocator(2, nodeName));
        return targetNode.findElement(By.xpath(".//fa-icon[@mattooltip=\"Select BIE\"]"));
    }

    @Override
    public EditBIEPage uplift() {
        submitUpliftReport();
        return openUpliftedBIE();
    }

    @Override
    public void submitUpliftReport() {
        invisibilityOfLoadingContainerElement(getDriver());
        click(getDriver(), getNextButton());
        // The verification page and the report dialog both use the generic
        // loading-container class. Scope this wait to the dialog; otherwise a
        // stale page-level loader can make every report submission appear to
        // retry indefinitely even after the report has finished loading.
        invisibilityOfElementLocated(PageHelper.wait(getDriver(), ofSeconds(120L), ofMillis(100L)),
                By.cssSelector("mat-dialog-container .loading-container"));
        click(getDriver(), elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
    }

    @Override
    public WebElement getUnselectedReuseWarning() {
        return visibilityOfElementLocated(getDriver(), UNSELECTED_REUSE_WARNING_LOCATOR);
    }

    @Override
    public EditBIEPage confirmUnselectedReuseAndUplift() {
        click(getDriver(), elementToBeClickable(getDriver(), UNSELECTED_REUSE_CONTINUE_BUTTON_LOCATOR));
        return openUpliftedBIE();
    }

    /**
     * Wait for the uplift to complete and open the resulting uplifted BIE, whose
     * id is taken from the {@code /profile_bie/{id}} URL the app navigates to.
     */
    private EditBIEPage openUpliftedBIE() {
        invisibilityOfElementLocated(PageHelper.wait(getDriver(), ofSeconds(120L), ofMillis(100L)),
                By.cssSelector("score-bie-uplift .loading-container"));
        String topLevelAsbiepIdText = PageHelper.wait(getDriver(), ofSeconds(120L), ofMillis(500L)).until(driver -> {
            Matcher matcher = Pattern.compile(".*/profile_bie/(\\d+)(?:[?#].*)?$")
                    .matcher(driver.getCurrentUrl());
            return matcher.matches() ? matcher.group(1) : null;
        });
        BigInteger topLevelAsbiepId = new BigInteger(topLevelAsbiepIdText);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        EditBIEPageImpl editBIEPage = new EditBIEPageImpl(parent, topLevelASBIEP);
        editBIEPage.openPage();
        return editBIEPage;
    }
}
