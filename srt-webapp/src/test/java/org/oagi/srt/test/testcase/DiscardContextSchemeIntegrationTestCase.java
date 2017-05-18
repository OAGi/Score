package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class DiscardContextSchemeIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextSchemeIntegration() throws Exception {
        open("/index.jsf");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Integration Context Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.linkText("Integration Context Scheme"));

        // Discard Integration Context Scheme.
        click(By.id("form:discardBtn"));
        click(By.id("form:acceptBtn3"));

        // Verify that Integration Context Scheme is deleted.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Scheme");
        assertTextEqual(By.linkText("Business Process Context Scheme"), "Business Process Context Scheme");
    }

}

