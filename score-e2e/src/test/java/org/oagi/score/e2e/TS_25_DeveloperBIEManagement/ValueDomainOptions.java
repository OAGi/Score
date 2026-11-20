package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * What the Value Domain drop-down of a BBIE offers for one list: it is not in the drop-down, it is there but cannot be
 * chosen (the option is disabled), or it can be chosen. Nothing is chosen by looking.
 */
public final class ValueDomainOptions {

    public enum Offer {
        NOT_OFFERED, SHOWN_BUT_DISABLED, SELECTABLE
    }

    private ValueDomainOptions() {
    }

    public static Offer offerOf(WebDriver driver, EditBIEPage.BBIEPanel bbiePanel, String listName) {
        click(driver, bbiePanel.getValueDomainField());
        waitFor(ofMillis(500L));
        WebElement search = visibilityOfElementLocated(driver, By.xpath("//input[@aria-label=\"dropdown search\"]"));
        // The search box keeps what was typed the last time the drop-down was open.
        for (int i = 0; i < 80; i++) {
            search.sendKeys(Keys.BACK_SPACE);
        }
        search.sendKeys(listName);
        waitFor(ofMillis(1000L));

        List<WebElement> options = driver.findElements(By.xpath(
                "//mat-option[.//span[normalize-space(text()) = \"" + listName + "\"]]"));
        Offer offer;
        if (options.isEmpty()) {
            offer = Offer.NOT_OFFERED;
        } else {
            offer = "true".equals(options.get(0).getAttribute("aria-disabled")) ? Offer.SHOWN_BUT_DISABLED : Offer.SELECTABLE;
        }
        escape(driver);
        waitFor(ofMillis(300L));
        return offer;
    }
}
