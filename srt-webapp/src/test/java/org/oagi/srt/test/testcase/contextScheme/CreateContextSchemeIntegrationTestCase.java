package org.oagi.srt.test.testcase.contextScheme;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateContextSchemeIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextSchemeIntegration() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Integration Context Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:inputContextCategory_input"), "Integration Context Category");
        type(By.id("form:name"), "Integration Context Scheme");
        type(By.id("form:schemeId"), "Oracle Integration Context Scheme");
        type(By.id("form:agencyId"), "Oracle");
        type(By.id("form:version"), "1.0");
        type(By.id("form:description"), "Classification scheme for types of integrations.");

        // Add Context Scheme Values.
        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"), "A2A");
        click(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input"), "Application-to-Application");
        click(By.id("form"));

        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"), "B2B");
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input"), "Business-to-Business");
        click(By.id("form"));

        click(By.id("form:createBtn"));

        // Verify if Scheme is added.
        waitForElementPresent(By.cssSelector("h2.subhead-heading"));
        pause(1000);
        assertTextEqual(By.cssSelector("h2.subhead-heading"),"Context Scheme");
//        try {
//            assertEquals("Context Scheme", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
//        } catch (Error e) {
//            getVerificationErrors().append(e.toString());
//        }
        assertTextEqual(By.linkText("Integration Context Scheme"), "Integration Context Scheme");
        click(By.linkText("Integration Context Scheme"));
        assertAttributeEquals(By.id("form:inputContextCategory_input"), "value", "Integration Context Category");
        assertAttributeEquals(By.id("form:name"), "value", "Integration Context Scheme");
        assertAttributeEquals(By.id("form:schemeId"), "value", "Oracle Integration Context Scheme");
        assertAttributeEquals(By.id("form:agencyId"), "value", "Oracle");
        assertAttributeEquals(By.id("form:version"), "value", "1.0");
        assertAttributeEquals(By.id("form:description"), "value", "Classification scheme for types of integrations.");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"), "value", "A2A");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]//input"), "value", "Application-to-Application");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"), "value", "B2B");
        assertAttributeEquals(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input"), "value", "Business-to-Business");
    }

}
