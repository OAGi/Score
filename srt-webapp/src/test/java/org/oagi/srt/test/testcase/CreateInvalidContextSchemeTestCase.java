package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateInvalidContextSchemeTestCase extends BaseTestCase {
    
    @Test
    public void testCaseCreateInvalidContextScheme() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Business Process Context 2 Scheme.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Scheme")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:inputContextCategory_input")).clear();
        getDriver().findElement(By.id("form:inputContextCategory_input")).sendKeys("Business Process Context Category");
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("Business Process Context Scheme 2");
        getDriver().findElement(By.id("form:schemeId")).clear();
        getDriver().findElement(By.id("form:schemeId")).sendKeys("Oracle BP Context Scheme");
        getDriver().findElement(By.id("form:agencyId")).clear();
        getDriver().findElement(By.id("form:agencyId")).sendKeys("Oracle");
        getDriver().findElement(By.id("form:version")).clear();
        getDriver().findElement(By.id("form:version")).sendKeys("1.0");
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Classification scheme for business process context values.");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that error message is shown.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-messages-error-detail"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("Can't create same identity of Context Scheme.", getDriver().findElement(By.cssSelector("span.ui-messages-error-detail")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        // Update to correct value.
        getDriver().findElement(By.id("form:schemeId")).clear();
        getDriver().findElement(By.id("form:schemeId")).sendKeys("Oracle BP Context Scheme 2");
        // Add Context Scheme Values.
        getDriver().findElement(By.id("form:addBtn")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).sendKeys("ProcessPO");
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input")).sendKeys("Purchase order processing");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:addBtn")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input")).sendKeys("ProcessSO");
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input")).sendKeys("Sales order processing");
        getDriver().findElement(By.id("form")).click();
        // Add duplicate Context Scheme Value.
        getDriver().findElement(By.id("form:addBtn")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]//input"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]//input")).sendKeys("ProcessSO");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that error message is shown.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-messages-error-detail"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("It doesn't allow duplicate 'Value' fields.", getDriver().findElement(By.cssSelector("span.ui-messages-error-detail")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}

