package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateContextCategoryTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextCategory() throws Exception {
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

        // Verify that context category is created.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"),"Context Category");
        assertTextEqual(By.linkText("Business Process Context Category"), "Business Process Context Category");
        click(By.linkText("Business Process Context Category"));
        assertTextEqual(By.id("form:description"), "Use business process context category to indidate that a context classification scheme is about business process.");
    }

}
