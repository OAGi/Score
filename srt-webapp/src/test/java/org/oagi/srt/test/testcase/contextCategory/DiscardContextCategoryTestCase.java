package org.oagi.srt.test.testcase.contextCategory;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class DiscardContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextCategory() throws Exception {
        open("/index.jsf");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Business Process Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.linkText("Business Process Context Category"));

        // Discard Business Process Context Category.
        click(By.id("form:discardBtn"));
        click(By.id("form:acceptBtn"));

        // Verify Business Process Context Category is deleted.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Category");
        assertTextEqual(By.cssSelector("td"), "No records found.");

    }

}
