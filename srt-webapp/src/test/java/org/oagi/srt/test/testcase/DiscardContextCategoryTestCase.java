package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/17/2017.
 */
public class DiscardContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextCategory() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Business Process Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.linkText("Business Process Context Category")).click();
        // Discard Business Process Context Category.
        getDriver().findElement(By.id("form:discardBtn")).click();
        getDriver().findElement(By.id("form:acceptBtn")).click();
        // Verify Business Process Context Category is deleted.
        try {
            for (int second = 0;; second++) {
                if (second >= 60) fail("timeout");
                try { if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break; } catch (Exception e) {}
                Thread.sleep(1000);
            }

            assertEquals("Context Category", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("No records found.", getDriver().findElement(By.cssSelector("td")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }

    }

}
