package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class DiscardContextSchemeTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextScheme() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Business Process Context Scheme.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Scheme")).click();
        getDriver().findElement(By.linkText("Business Process Context Scheme")).click();
        // Discard Business Process Context Scheme.
        getDriver().findElement(By.id("form:discardBtn")).click();
        getDriver().findElement(By.id("form:acceptBtn3")).click();
        // Verify that Business Process Context Scheme is deleted.
        try {
            assertTrue(isElementPresent(By.cssSelector("h2.subhead-heading")));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Context Scheme", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
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
