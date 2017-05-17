package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateContextCategoryIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextCategoryIntegration() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Integration Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("Integration Context Category");
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Use integration context category to indidate that a context classification scheme is about various types of integrations.");
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that context category is created.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("Context Category", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Integration Context Category", getDriver().findElement(By.linkText("Integration Context Category")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        getDriver().findElement(By.linkText("Integration Context Category")).click();
        try {
            assertEquals("Use integration context category to indidate that a context classification scheme is about various types of integrations.", getDriver().findElement(By.id("form:description")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
