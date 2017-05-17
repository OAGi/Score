package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/17/2017.
 */
public class UpdateContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateContextCategory() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Business Process Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.linkText("Business Process Context Category")).click();
        // Update Business Process Category description.
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Use business process context category to indicate that a context classification scheme is about business process.");
        getDriver().findElement(By.id("form:updateBtn")).click();
        // Verify update.
        try {
            for (int second = 0; ; second++) {
                if (second >= 60) fail("timeout");
                try {
                    if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break;
                } catch (Exception e) {
                }
                Thread.sleep(1000);
            }

            assertEquals("Context Category", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business Process Context Category", getDriver().findElement(By.linkText("Business Process Context Category")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        getDriver().findElement(By.linkText("Business Process Context Category")).click();
        try {
            assertEquals("Use business process context category to indicate that a context classification scheme is about business process.", getDriver().findElement(By.id("form:description")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
