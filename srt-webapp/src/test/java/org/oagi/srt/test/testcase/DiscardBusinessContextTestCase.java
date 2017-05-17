package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class DiscardBusinessContextTestCase extends BaseTestCase {
    
    @Test
    public void testCaseDiscardBusinessContextA2AProcessPO() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Find B2B Process PO Business Context.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Business Context")).click();
        getDriver().findElement(By.linkText("A2A Process PO")).click();
        getDriver().findElement(By.id("form:discardBtn")).click();
        getDriver().findElement(By.id("form:acceptBtn")).click();
        // Verify that Business Context is deleted.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("Business Context", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
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

