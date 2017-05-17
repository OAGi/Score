package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class UpdateContextSchemeTestCase extends BaseTestCase {

    @Test
    public void testCaseUpdateContextScheme() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Select Business Process Context Scheme.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Scheme")).click();
        getDriver().findElement(By.linkText("Business Process Context Scheme")).click();
        // Update Business Process Conext Scheme (add values).
        getDriver().findElement(By.id("form:addBtn")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td//input"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td//input")).sendKeys("EDM");
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]//input")).sendKeys("Engineering change management");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:updateBtn")).click();
        // Verify added value.
        getDriver().findElement(By.linkText("Business Process Context Scheme")).click();
        try {
            assertEquals("EDM", getDriver().findElement(By.cssSelector("div.ui-cell-editor-output")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Engineering change management", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessPO", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Purchase order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessSO", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Sales order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        // Update value.
        getDriver().findElement(By.cssSelector("div.ui-cell-editor-output")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).sendKeys("ECM");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:updateBtn")).click();
        // Verify updated value.
        getDriver().findElement(By.linkText("Business Process Context Scheme")).click();
        try {
            assertEquals("ECM", getDriver().findElement(By.cssSelector("div.ui-cell-editor-output")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Engineering change management", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessPO", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Purchase order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessSO", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[1]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Sales order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[3]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        // Delete value.
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[3]/button")).click();
        getDriver().findElement(By.id("form:updateBtn")).click();
        // Verify deleted value.
        getDriver().findElement(By.linkText("Business Process Context Scheme")).click();
        try {
            assertEquals("ProcessPO", getDriver().findElement(By.cssSelector("div.ui-cell-editor-output")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Purchase order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessSO", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Sales order processing", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
