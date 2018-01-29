package org.oagi.srt.test.testcase.businessContext;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class CreateBusinessContextTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateBusinessContextB2BProcessPO() throws Exception {
        open("/");

        // Login user testuser/testtest.
        waitForElementPresent(By.id("username"));
        login("testuser", "testtest");

        // Create Business Context.
        click(By.linkText("Context Management"));
        click(By.linkText("Business Context"));
        click(By.id("listForm:createBtn"));
        type(By.id("form:name"), "B2B Process PO");

        // Add Business Context Values.
        click(By.id("form:addBtn"));

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"), "Business Process Context Category");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"), Keys.TAB);

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]"));
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys("Business Process Context Scheme");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]"), Keys.TAB);

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]"));
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys("ProcessPO");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]"), Keys.TAB);

        click(By.id("form:addBtn"));

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"));
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).sendKeys("Integration Context Category");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"), Keys.TAB);

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]"));
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys("Integration Context Scheme");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]"), Keys.TAB);

        waitForElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/button"));
        pause(1000);
        click(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]"));
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys("B2B");
        waitForElementPresent(By.cssSelector("span.ui-autocomplete-query"));
        sendKeys(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]"), Keys.TAB);

        click(By.id("form:createBtn"));

        // Verify that Business Context is created.
        assertTextEqual(By.linkText("B2B Process PO"), "B2B Process PO");
        click(By.linkText("B2B Process PO"));
        assertAttributeEquals(By.id("form:name"), "value", "B2B Process PO");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]"), "value", "Business Process Context Category");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]"), "value", "Business Process Context Scheme");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]"), "value", "ProcessPO");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[2]/td[1]/span/input[1]"), "value", "Integration Context Category");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[2]/td[2]/span/input[1]"), "value", "Integration Context Scheme");
        assertAttributeEquals(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]"), "value", "B2B");
    }

}
