package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class DiscardContextSchemeIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextSchemeIntegration() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Integration Context Scheme.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Scheme")).click();
        getDriver().findElement(By.linkText("Integration Context Scheme")).click();
        // Discard Integration Context Scheme.
        getDriver().findElement(By.id("form:discardBtn")).click();
        getDriver().findElement(By.id("form:acceptBtn3")).click();
        // Verify that Integration Context Scheme is deleted.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("Context Scheme", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business Process Context Scheme", getDriver().findElement(By.linkText("Business Process Context Scheme")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}

