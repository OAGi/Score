package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateDuplicateContextSchemeTestCase extends BaseTestCase {

        @Test
        public void testCaseCreateDuplicateContextScheme() throws Exception {
            open("/index.jsf");

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

            // Verify that error message is shown.
            waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
            assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Can't create same identity of Context Scheme.");
        }

    }
