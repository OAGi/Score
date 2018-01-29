package org.oagi.srt.test.testcase.contextScheme;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class DiscardContextSchemeTestCase extends BaseTestCase {

    @Test
    public void testCaseDiscardContextScheme() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Business Process Context Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.linkText("Business Process Context Scheme"));

        // Discard Business Process Context Scheme.
        click(By.id("form:discardBtn"));
        click(By.id("form:acceptBtn3"));

        // Verify that Business Process Context Scheme is deleted.
        assertElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Scheme");
        assertTextEqual(By.cssSelector("td"), "No records found.");
    }

}
