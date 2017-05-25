package org.oagi.srt.test.testcase.contextCategory;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class UpdateContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateContextCategory() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Business Process Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.linkText("Business Process Context Category"));

        // Update Business Process Category description.
        type(By.id("form:description"), "Use business process context category to indicate that a context classification scheme is about business process.");
        click(By.id("form:updateBtn"));

        // Verify update.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"),"Context Category");
        assertTextEqual(By.linkText("Business Process Context Category"), "Business Process Context Category");
        click(By.linkText("Business Process Context Category"));
        assertAttributeEquals(By.id("form:description"), "value", "Use business process context category to indicate that a context classification scheme is about business process.");
    }

}
