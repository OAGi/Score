package org.oagi.srt.test.testcase.contextCategory;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class DiscardContextCategoryIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextCategoryIntegration() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Integration Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.linkText("Integration Context Category"));

        // Discard Integration Context Category.
        click(By.id("form:discardBtn"));
        click(By.id("form:acceptBtn"));

        // Verify Integration Context Category is deleted.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Category");
        assertTextEqual(By.linkText("Business Process Context Category"), "Business Process Context Category");
    }

}

