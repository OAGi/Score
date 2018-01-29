package org.oagi.srt.test.testcase.contextCategory;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateContextCategoryIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextCategoryIntegration() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Integration Context Category.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Category"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:name"), "Integration Context Category");
        type(By.id("form:description"), "Use integration context category to indidate that a context classification scheme is about various types of integrations.");
        click(By.id("form:createBtn"));

        // Verify that context category is created.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Category");
        assertTextEqual(By.linkText("Integration Context Category"), "Integration Context Category");
        click(By.linkText("Integration Context Category"));
        assertTextEqual(By.id("form:description"), "Use integration context category to indidate that a context classification scheme is about various types of integrations.");
    }

}
