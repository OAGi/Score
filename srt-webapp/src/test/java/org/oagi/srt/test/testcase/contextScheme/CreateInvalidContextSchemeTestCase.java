package org.oagi.srt.test.testcase.contextScheme;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateInvalidContextSchemeTestCase extends BaseTestCase {
    
    @Test
    public void testCaseCreateInvalidContextScheme() throws Exception {
        open("/index.jsf");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Business Process Context 2 Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:inputContextCategory_input"), "Business Process Context Category");
        type(By.id("form:name"), "Business Process Context Scheme 2");
        type(By.id("form:schemeId"), "Oracle BP Context Scheme");
        type(By.id("form:agencyId"), "Oracle");
        type(By.id("form:version"), "1.0");
        type(By.id("form:description"), "Classification scheme for business process context values.");
        click(By.id("form"));
        click(By.id("form:createBtn"));

        // Verify that error message is shown.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Can't create same identity of Context Scheme.");

        // Update to correct value.
        type(By.id("form:schemeId"), "Oracle BP Context Scheme 2");

        // Add Context Scheme Values.
        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"), "ProcessPO");
        click(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input"), "Purchase order processing");
        click(By.id("form"));

        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"), "ProcessSO");
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input"), "Sales order processing");
        click(By.id("form"));

        // Add duplicate Context Scheme Value.
        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]//input"), "ProcessSO");
        click(By.id("form"));
        click(By.id("form:createBtn"));

        // Verify that error message is shown.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "It doesn't allow duplicate 'Value' fields.");
    }

}

