package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.bie.BieBusinessTermAssignDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Implementation of the multi-select 'Assign Business Term' dialog (Issue #1754). Selectors mirror
 * {@code bie-business-term-assign-dialog.component.html}.
 */
public class BieBusinessTermAssignDialogImpl implements BieBusinessTermAssignDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//span[contains(@class, \"assign-title\")]");

    // The search bar's main input. score-search-bar renders a single input in its main-search area.
    private static final By SEARCH_INPUT_LOCATOR =
            By.xpath(BASE_XPATH + "//score-search-bar[contains(@class, \"bt-assign-search-bar\")]"
                    + "//div[contains(@class, \"main-search\")]//input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//score-search-bar[contains(@class, \"bt-assign-search-bar\")]"
                    + "//div[contains(@class, \"main-search\")]//button[1]");

    private static final By MASTER_CHECKBOX_LOCATOR =
            By.xpath(BASE_XPATH + "//th[contains(@class, \"bt-col-select\")]//mat-checkbox");

    private static final By TYPE_CODE_INPUT_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-form-field[contains(@class, \"bt-assign-typecode\")]//input");

    private static final By ASSIGN_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-dialog-actions//button[@color=\"primary\"]");

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-dialog-actions//button[normalize-space(.)=\"Cancel\"]");

    private static final By DIALOG_CONTAINER_LOCATOR = By.cssSelector("mat-dialog-container");

    private final BasePageImpl parent;

    public BieBusinessTermAssignDialogImpl(BasePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
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
    public void setSearchBusinessTerm(String businessTerm) {
        sendKeys(visibilityOfElementLocated(getDriver(), SEARCH_INPUT_LOCATOR), businessTerm);
    }

    @Override
    public void hitSearch() {
        click(getDriver(), elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getRowCheckboxByTerm(String businessTerm) {
        // Scope to the Business Term cell that carries a link labelled with the term, then reach the
        // sibling select-cell checkbox in the same row.
        return elementToBeClickable(getDriver(), By.xpath(BASE_XPATH
                + "//tr[@mat-row][.//a[normalize-space(.)=\"" + businessTerm + "\"]]"
                + "//td[contains(@class, \"bt-col-select\")]//mat-checkbox"));
    }

    @Override
    public WebElement getRowCheckboxAtIndex(int index) {
        return elementToBeClickable(getDriver(), By.xpath("(" + BASE_XPATH
                + "//tr[@mat-row]//td[contains(@class, \"bt-col-select\")]//mat-checkbox)[" + index + "]"));
    }

    @Override
    public void toggleMasterCheckbox() {
        click(getDriver(), elementToBeClickable(getDriver(), MASTER_CHECKBOX_LOCATOR));
        waitFor(ofMillis(300L));
    }

    @Override
    public boolean isMasterIndeterminate() {
        WebElement checkbox = visibilityOfElementLocated(getDriver(), MASTER_CHECKBOX_LOCATOR);
        String klass = checkbox.getAttribute("class");
        if (klass != null && klass.contains("indeterminate")) {
            return true;
        }
        // Material MDC surfaces the indeterminate ("mixed") state as aria-checked="mixed" on the inner
        // <input>; Material 21 no longer carries the state on the <mat-checkbox> host, so reading the
        // host's aria-checked / *-indeterminate class alone misses it.
        WebElement input = checkbox.findElement(By.cssSelector("input[type=\"checkbox\"]"));
        return "mixed".equals(input.getAttribute("aria-checked"));
    }

    @Override
    public void setTypeCode(String typeCode) {
        sendKeys(visibilityOfElementLocated(getDriver(), TYPE_CODE_INPUT_LOCATOR), typeCode);
    }

    @Override
    public WebElement getAssignButton() {
        return visibilityOfElementLocated(getDriver(), ASSIGN_BUTTON_LOCATOR);
    }

    @Override
    public String getAssignButtonText() {
        return getText(getAssignButton());
    }

    @Override
    public void hitAssign() {
        click(getDriver(), elementToBeClickable(getDriver(), ASSIGN_BUTTON_LOCATOR));
        invisibilityOfLoadingContainerElement(getDriver());
        invisibilityOfElementLocated(getDriver(), DIALOG_CONTAINER_LOCATOR);
    }

    @Override
    public void cancel() {
        click(getDriver(), elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), DIALOG_CONTAINER_LOCATOR);
    }
}
