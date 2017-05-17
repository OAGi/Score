package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/17/2017.
 */
public class CreateContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextCategory() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Business Process Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("Business Process Context Category");
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Use business process context category to indidate that a context classification scheme is about business process.");
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that context category is created.
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
            assertEquals("Use business process context category to indidate that a context classification scheme is about business process.", getDriver().findElement(By.id("form:description")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }
}
