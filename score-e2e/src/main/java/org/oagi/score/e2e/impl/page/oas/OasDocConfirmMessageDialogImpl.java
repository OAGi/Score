package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.oas.OasDocConfirmMessageDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.isElementPresent;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;
import static org.oagi.score.e2e.impl.PageHelper.xpathLiteral;

import static java.time.Duration.ofMillis;

/**
 * Implementation of {@link OasDocConfirmMessageDialog} (Issue #1347). The dialog is a clone of the
 * 'Include Meta Header' / 'Pagination Response' BIE pickers, locked to the standard 'Confirm Message'
 * BIE: a full BIE-list table with a disabled search box, a disabled Branch (the connected BIE's release),
 * no Library selector, and a paginator. Candidates share the locked DEN, so they are addressed by their
 * Business Context.
 */
public class OasDocConfirmMessageDialogImpl implements OasDocConfirmMessageDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";

    private static final By TITLE_LOCATOR =
            By.xpath(BASE_XPATH + "//div[contains(@class, \"header\")]/span");
    private static final By SELECT_BUTTON_LOCATOR =
            By.cssSelector("mat-dialog-container #btn-confirm-message-dialog-select");
    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//div[contains(@class, \"actions\")]//button[.//span[normalize-space(.) = \"Cancel\"]]");

    private final BasePageImpl parent;

    public OasDocConfirmMessageDialogImpl(BasePageImpl parent) {
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

    // A candidate row (a BIE-list table row) whose DEN column contains the given DEN.
    private By candidateRowByDen(String den) {
        return By.xpath(BASE_XPATH + "//table//tr[.//td[contains(concat(\" \", normalize-space(@class), \" \"),"
                + " \" mat-column-den \")][contains(normalize-space(.), " + xpathLiteral(den) + ")]]");
    }

    // A candidate row whose Business Context column contains the given business context name. Every row
    // shares the locked DEN, so the Business Context is what uniquely identifies a specific candidate.
    private By candidateRowByBusinessContext(String businessContextName) {
        return By.xpath(BASE_XPATH + "//table//tr[.//td[contains(concat(\" \", normalize-space(@class), \" \"),"
                + " \" mat-column-businessContexts \")][contains(normalize-space(.), " + xpathLiteral(businessContextName) + ")]]");
    }

    @Override
    public boolean isCandidatePresent(String den) {
        return isElementPresent(getDriver(), candidateRowByDen(den));
    }

    @Override
    public boolean isCandidatePresentByBusinessContext(String businessContextName) {
        return isElementPresent(getDriver(), candidateRowByBusinessContext(businessContextName));
    }

    @Override
    public void selectCandidateByBusinessContext(String businessContextName) {
        // Click the row's select checkbox (NOT the row/DEN cell — the DEN cell holds a link that would navigate).
        WebElement row = visibilityOfElementLocated(getDriver(), candidateRowByBusinessContext(businessContextName));
        WebElement checkbox = row.findElement(By.cssSelector("td.mat-column-select mat-checkbox input"));
        click(getDriver(), checkbox);
        waitFor(ofMillis(300L));
    }

    @Override
    public boolean isSelectEnabled() {
        WebElement button = visibilityOfElementLocated(getDriver(), SELECT_BUTTON_LOCATOR);
        String klass = button.getAttribute("class");
        return button.isEnabled() && (klass == null || !klass.contains("mat-mdc-button-disabled"));
    }

    @Override
    public void hitSelect() {
        click(getDriver(), elementToBeClickable(getDriver(), SELECT_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }

    @Override
    public void cancel() {
        click(getDriver(), elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR));
        invisibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }
}
