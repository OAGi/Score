package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateContextCategoryNoNameTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateInvalidContextCategory() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Business Process Context Category.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Category")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("");
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Use business process context category to indidate that a context classification scheme is about business process.");
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that error message is shown.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-messages-error-detail"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("Please fill out 'Name' field.", getDriver().findElement(By.cssSelector("span.ui-messages-error-detail")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}


