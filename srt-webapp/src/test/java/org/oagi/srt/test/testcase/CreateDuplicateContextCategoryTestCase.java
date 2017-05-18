package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateDuplicateContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateDuplicateContextCategory() throws Exception {
        open("/index.jsf");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Business Process Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:name"), "Business Process Context Category");
        type(By.id("form:description"), "Use business process context category to indidate that a context classification scheme is about business process.");
        click(By.id("form:createBtn"));

        // Verify that error message is shown.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Name is already taken.");
    }
}
