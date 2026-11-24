package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.business_term.BusinessTermWhereUsedDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.retry;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Implementation of the 'Where Used' dialog of the Business Term list. Selectors mirror
 * {@code business-term-where-used-dialog.component.html}.
 */
public class BusinessTermWhereUsedDialogImpl implements BusinessTermWhereUsedDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";

    private static final By TITLE_LOCATOR = By.xpath(BASE_XPATH + "//div[contains(@class, \"dialog-title\")]/span");

    private static final By GROUP_LOCATOR = By.cssSelector("mat-dialog-container fieldset.bt-usage-tree");

    private static final By BODY_LOCATOR = By.cssSelector("mat-dialog-container .dialog-body");

    private final BasePageImpl parent;

    public BusinessTermWhereUsedDialogImpl(BasePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return parent.getDriver();
    }

    private Object script(String script, Object... args) {
        return ((JavascriptExecutor) getDriver()).executeScript(script, args);
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }

    @Override
    public String getTitleText() {
        // The title says Where Used while the BIEs are loaded, and the count (or Nothing found) when they are.
        retry(() -> {
            if (getText(getTitle()).equals("Where Used")) {
                throw new IllegalStateException("The BIEs are still loading.");
            }
        });
        return getText(getTitle());
    }

    private List<WebElement> groups() {
        return getDriver().findElements(GROUP_LOCATOR);
    }

    @Override
    public int getNumberOfBIEs() {
        getTitleText();
        return groups().size();
    }

    @Override
    public WebElement getBIEGroup(int bieIndex) {
        return retry(() -> groups().get(bieIndex - 1));
    }

    @Override
    public String getRelease(int bieIndex) {
        return getBIEGroup(bieIndex).findElement(By.tagName("legend")).getText().trim();
    }

    @Override
    public WebElement getUpdatedElement(int bieIndex) {
        return getBIEGroup(bieIndex).findElement(By.className("bt-usage-updated"));
    }

    @Override
    public String getUpdatedText(int bieIndex) {
        return getUpdatedElement(bieIndex).getText().replaceAll("\\s+", " ").trim();
    }

    @Override
    public String getState(int bieIndex) {
        return getBIEGroup(bieIndex).findElement(By.className("bt-usage-state")).getText().trim();
    }

    @Override
    public void expand(int bieIndex) {
        WebElement group = getBIEGroup(bieIndex);
        if (!group.findElements(By.tagName("score-tree-viewport")).isEmpty()) {
            return;
        }
        click(getDriver(), group.findElement(By.cssSelector(".score-tree-rows .mat-tree-node")));
        retry(() -> {
            if (getBIEGroup(bieIndex).findElements(By.tagName("score-tree-viewport")).isEmpty()) {
                throw new IllegalStateException("The tree is not loaded yet.");
            }
        }, 30);
        waitFor(ofMillis(300L));
    }

    private List<WebElement> rows(int bieIndex) {
        return getBIEGroup(bieIndex).findElements(By.cssSelector("score-tree-viewport .mat-tree-node"));
    }

    private static String labelOf(WebElement row) {
        return row.findElement(By.cssSelector(".node-label")).getText().trim();
    }

    @Override
    public List<String> getNodeLabels(int bieIndex) {
        List<String> labels = new ArrayList<>();
        for (WebElement row : rows(bieIndex)) {
            labels.add(labelOf(row));
        }
        return labels;
    }

    private WebElement row(int bieIndex, String nodeLabel) {
        return retry(() -> rows(bieIndex).stream()
                .filter(row -> labelOf(row).equals(nodeLabel))
                .findFirst()
                .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("No row with the label " + nodeLabel)));
    }

    private static List<Badge> badgesOf(WebElement row) {
        List<Badge> badges = new ArrayList<>();
        for (WebElement badge : row.findElements(By.className("bt-badge"))) {
            List<WebElement> type = badge.findElements(By.className("bt-badge-type"));
            badges.add(new Badge(
                    badge.findElement(By.className("bt-badge-term")).getText().trim(),
                    !badge.findElements(By.className("bt-badge-star")).isEmpty(),
                    type.isEmpty() ? null : type.get(0).getText().trim()));
        }
        return badges;
    }

    @Override
    public List<Badge> getBadges(int bieIndex, String nodeLabel) {
        return badgesOf(row(bieIndex, nodeLabel));
    }

    @Override
    public int getNumberOfBadges(int bieIndex) {
        return getBIEGroup(bieIndex).findElements(By.cssSelector("score-tree-viewport .bt-badge")).size();
    }

    @Override
    public void clickNode(int bieIndex, String nodeLabel) {
        click(getDriver(), row(bieIndex, nodeLabel).findElement(By.cssSelector(".node-label")));
    }

    @Override
    public WebElement getBody() {
        return visibilityOfElementLocated(getDriver(), BODY_LOCATOR);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getScrollMetrics() {
        return (Map<String, Object>) script(
                "const measure = e => e ? {scrollHeight: e.scrollHeight, clientHeight: e.clientHeight, "
                        + "overflowY: getComputedStyle(e).overflowY} : null;"
                        + "return {body: measure(document.querySelector('mat-dialog-container .dialog-body')),"
                        + " container: measure(document.querySelector('mat-dialog-container')),"
                        + " pane: measure(document.querySelector('.cdk-overlay-pane'))};");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getTreeRowStyle(int bieIndex) {
        WebElement row = rows(bieIndex).get(0);
        Map<String, String> style = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) script(
                "const row = arguments[0], label = row.querySelector('.node-label');"
                        + "const pick = (e, names) => Object.fromEntries(names.map(n => [n, getComputedStyle(e).getPropertyValue(n)]));"
                        + "return {...pick(row, ['height', 'font-size', 'font-family', 'font-weight', 'line-height', 'color', 'display']),"
                        + " ...Object.fromEntries(Object.entries(pick(label, ['font-size', 'font-family', 'font-weight', 'color']))"
                        + "   .map(([k, v]) => ['label ' + k, v]))};", row)).entrySet()) {
            style.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return style;
    }

    @Override
    public void close() {
        click(getDriver(), elementToBeClickable(getDriver(), By.cssSelector("mat-dialog-container .dialog-close")));
    }
}
