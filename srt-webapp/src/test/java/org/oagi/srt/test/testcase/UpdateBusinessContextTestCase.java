package org.oagi.srt.test.testcase;


import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

/**
 * Created by Miroslav Ljubicic.
 */
public class UpdateBusinessContextTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateBusinessContextB2BProcessPOToA2AProcessPO() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Find B2B Process PO Business Context.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Business Context")).click();
        getDriver().findElement(By.linkText("B2B Process PO")).click();
        // Update Business Context.
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("A2A Process PO");
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).click();
//        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys(Keys.BACK_SPACE + Keys.BACK_SPACE + Keys.BACK_SPACE + "A2A");
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys(Keys.BACK_SPACE);
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys(Keys.BACK_SPACE);
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys(Keys.BACK_SPACE);
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).sendKeys("A2A");
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//div[@id='form:tbl:1:ctxSchemeValueAuto_panel']/ul/li[@data-item-label=\"A2A\"]")).click();
        getDriver().findElement(By.id("form:updateBtn")).click();
        // Verify that Business Context is updated.
        try {
            assertEquals("A2A Process PO", getDriver().findElement(By.linkText("A2A Process PO")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        getDriver().findElement(By.linkText("A2A Process PO")).click();
        try {
            assertEquals("A2A Process PO", getDriver().findElement(By.id("form:name")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("A2A", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
