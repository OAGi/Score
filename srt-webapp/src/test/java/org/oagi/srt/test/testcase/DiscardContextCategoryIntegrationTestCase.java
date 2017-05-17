package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class DiscardContextCategoryIntegrationTestCase extends BaseTestCase{

    @Test
    public void testCaseDiscardContextCategoryIntegration() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Integration Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.linkText("Integration Context Category")).click();
        // Discard Integration Context Category.
        getDriver().findElement(By.id("form:discardBtn")).click();
        getDriver().findElement(By.id("form:acceptBtn")).click();
        // Verify Integration Context Category is deleted.
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }

        try {
            assertEquals("Context Category", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business Process Context Category", getDriver().findElement(By.linkText("Business Process Context Category")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}

