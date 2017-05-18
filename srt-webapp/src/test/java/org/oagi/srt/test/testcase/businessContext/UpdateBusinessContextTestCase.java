package org.oagi.srt.test.testcase.businessContext;


import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class UpdateBusinessContextTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateBusinessContextB2BProcessPOToA2AProcessPO() throws Exception {
        open("/index.jsf");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Find B2B Process PO Business Context.
        click(By.linkText("Context Management"));
        click(By.linkText("Business Context"));
        click(By.linkText("B2B Process PO"));

        // Update Business Context.
        type(By.id("form:name"), "A2A Process PO");
        click(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"));
//        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys(Keys.BACK_SPACE + Keys.BACK_SPACE + Keys.BACK_SPACE + "A2A");
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), Keys.BACK_SPACE);
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), Keys.BACK_SPACE);
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), Keys.BACK_SPACE);
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), "A2A");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        click(By.xpath("//div[@id='form:tbl:1:ctxSchemeValueAuto_panel']/ul/li[@data-item-label=\"A2A\"]"));
        click(By.id("form:updateBtn"));

        // Verify that Business Context is updated.
        assertTextEqual(By.linkText("A2A Process PO"), "A2A Process PO");
        click(By.linkText("A2A Process PO"));
        assertAttributeEquals(By.id("form:name"), "value", "A2A Process PO");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), "value", "A2A");
    }

}
