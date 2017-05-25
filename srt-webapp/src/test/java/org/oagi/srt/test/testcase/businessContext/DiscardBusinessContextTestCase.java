package org.oagi.srt.test.testcase.businessContext;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class DiscardBusinessContextTestCase extends BaseTestCase {
    
    @Test
    public void testCaseDiscardBusinessContextA2AProcessPO() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Find B2B Process PO Business Context.
        click(By.linkText("Context Management"));
        click(By.linkText("Business Context"));
        click(By.linkText("A2A Process PO"));
        click(By.id("form:discardBtn"));
        click(By.id("form:acceptBtn"));

        // Verify that Business Context is deleted.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Business Context");
        assertTextEqual(By.cssSelector("td"), "No records found.");
    }

}

