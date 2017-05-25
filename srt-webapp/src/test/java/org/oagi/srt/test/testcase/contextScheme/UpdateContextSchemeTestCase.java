package org.oagi.srt.test.testcase.contextScheme;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class UpdateContextSchemeTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateContextScheme() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Select Business Process Context Scheme.
        click(By.linkText("Context Management"));
        click(By.linkText("Context Scheme"));
        click(By.linkText("Business Process Context Scheme"));

        // Update Business Process Conext Scheme (add values).
        click(By.id("form:addBtn"));
        waitForElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td//input"));
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td//input"), "EDM");
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]//input"), "Engineering change management");
        click(By.id("form"));
        click(By.id("form:updateBtn"));

        // Verify added value.
        click(By.linkText("Business Process Context Scheme"));
        assertTextEqual(By.cssSelector("div.ui-cell-editor-output"), "EDM");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]"), "Engineering change management");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]"), "ProcessPO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]"), "Purchase order processing");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]"), "ProcessSO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]"), "Sales order processing");

        // Update value.
        click(By.cssSelector("div.ui-cell-editor-output"));
        type(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"), "ECM");
        click(By.id("form"));
        click(By.id("form:updateBtn"));

        // Verify updated value.
        click(By.linkText("Business Process Context Scheme"));
        assertTextEqual(By.cssSelector("div.ui-cell-editor-output"), "ECM");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]"), "Engineering change management");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]"), "ProcessPO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]"), "Purchase order processing");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]"), "ProcessSO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]"), "Sales order processing");

        // Delete value.
        click(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[3]/button"));
        click(By.id("form:updateBtn"));

        // Verify deleted value.
        click(By.linkText("Business Process Context Scheme"));
        assertTextEqual(By.cssSelector("div.ui-cell-editor-output"), "ProcessPO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]"), "Purchase order processing");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]"), "ProcessSO");
        assertTextEqual(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]"), "Sales order processing");
    }

}
