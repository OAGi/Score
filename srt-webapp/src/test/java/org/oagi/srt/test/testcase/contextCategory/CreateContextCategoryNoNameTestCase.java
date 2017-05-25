package org.oagi.srt.test.testcase.contextCategory;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateContextCategoryNoNameTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateInvalidContextCategory() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Business Process Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:name"), "");
        type(By.id("form:description"), "Use business process context category to indidate that a context classification scheme is about business process.");
        click(By.id("form:createBtn"));

        // Verify that error message is shown.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Please fill out 'Name' field.");
    }

}


