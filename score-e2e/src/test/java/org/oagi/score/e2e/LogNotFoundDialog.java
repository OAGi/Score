package org.oagi.score.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * The dialog that asks the user whether a new history (log) may be written, when the one a revision goes on from, or a
 * cancel goes back to, cannot be found.
 */
public final class LogNotFoundDialog {

    private static final By HEADER = By.xpath("//mat-dialog-container//*[contains(text(), \"Log information not found\")]");

    private LogNotFoundDialog() {
    }

    /** Wait until the dialog is on the screen. */
    public static void awaitOpened(WebDriver driver) {
        visibilityOfElementLocated(driver, HEADER);
    }

    /** The user lets the new history be written. */
    public static void createAndContinue(WebDriver driver) {
        click(driver, elementToBeClickable(driver, By.xpath(
                "//mat-dialog-container//button[contains(normalize-space(.), \"Create and continue\")]")));
    }

    /** The user does not let it. */
    public static void decline(WebDriver driver) {
        click(driver, elementToBeClickable(driver, By.xpath(
                "//mat-dialog-container//button[normalize-space(.) = \"Cancel\"]")));
        invisibilityOfElementLocated(driver, HEADER);
    }

    /** Confirm the dialog every cancel and every revision asks before it goes on (the 'Okay' or the 'Revise'/'Amend' one). */
    public static void confirm(WebDriver driver, String action) {
        click(driver, elementToBeClickable(driver, By.xpath(
                "//mat-dialog-container//button[contains(normalize-space(.), \"" + action + "\")]")));
    }
}
