package org.oagi.srt.test.testcase.contextScheme;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateContextSchemeTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextScheme() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Business Process Context Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:inputContextCategory_input"), "Business Process Context Category");
        type(By.id("form:name"), "Business Process Context Scheme");
        type(By.id("form:schemeId"), "Oracle BP Context Scheme");
        type(By.id("form:agencyId"), "Oracle");
        type(By.id("form:version"), "1.0");
        type(By.id("form:description"), "Classification scheme for business process context values.");

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

        click(By.id("form:createBtn"));

        // Verify if Scheme is added.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));

        pause(1000);

        assertTextEqual(By.cssSelector("h2.subhead-heading"), "Context Scheme");
        assertTextEqual(By.linkText("Business Process Context Scheme"), "Business Process Context Scheme");
        click(By.linkText("Business Process Context Scheme"));

        assertAttributeEquals(By.id("form:inputContextCategory_input"), "value", "Business Process Context Category");
        assertAttributeEquals(By.id("form:name"), "value", "Business Process Context Scheme");
        assertAttributeEquals(By.id("form:schemeId"), "value", "Oracle BP Context Scheme");
        assertAttributeEquals(By.id("form:agencyId"), "value", "Oracle");
        assertAttributeEquals(By.id("form:version"), "value", "1.0");
        assertAttributeEquals(By.id("form:description"), "value", "Classification scheme for business process context values.");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"), "value", "ProcessPO");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]//input"), "value", "Purchase order processing");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"), "value", "ProcessSO");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input"), "value", "Sales order processing");
    }

}

