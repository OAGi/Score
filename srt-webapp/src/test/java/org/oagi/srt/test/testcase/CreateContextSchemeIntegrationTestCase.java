package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateContextSchemeIntegrationTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateContextSchemeIntegration() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Integration Context Scheme.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Context Scheme")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:inputContextCategory_input")).clear();
        getDriver().findElement(By.id("form:inputContextCategory_input")).sendKeys("Integration Context Category");
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("Integration Context Scheme");
        getDriver().findElement(By.id("form:schemeId")).clear();
        getDriver().findElement(By.id("form:schemeId")).sendKeys("Oracle Integration Context Scheme");
        getDriver().findElement(By.id("form:agencyId")).clear();
        getDriver().findElement(By.id("form:agencyId")).sendKeys("Oracle");
        getDriver().findElement(By.id("form:version")).clear();
        getDriver().findElement(By.id("form:version")).sendKeys("1.0");
        getDriver().findElement(By.id("form:description")).clear();
        getDriver().findElement(By.id("form:description")).sendKeys("Classification scheme for types of integrations.");
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
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).sendKeys("A2A");
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr/td[2]//input")).sendKeys("Application-to-Application");
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
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input")).sendKeys("B2B");
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]")).click();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input")).clear();
        getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input")).sendKeys("Business-to-Business");
        getDriver().findElement(By.id("form")).click();
        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify if Scheme is added.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("h2.subhead-heading"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

//        try {
//            assertEquals("Context Scheme", getDriver().findElement(By.cssSelector("h2.subhead-heading")).getText());
//        } catch (Error e) {
//            getVerificationErrors().append(e.toString());
//        }
        try {
            assertEquals("Integration Context Scheme", getDriver().findElement(By.linkText("Integration Context Scheme")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        getDriver().findElement(By.linkText("Integration Context Scheme")).click();
        try {
            assertEquals("Integration Context Category", getDriver().findElement(By.id("form:inputContextCategory_input")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Integration Context Scheme", getDriver().findElement(By.id("form:name")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Oracle Integration Context Scheme", getDriver().findElement(By.id("form:schemeId")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Oracle", getDriver().findElement(By.id("form:agencyId")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("1.0", getDriver().findElement(By.id("form:version")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Classification scheme for types of integrations.", getDriver().findElement(By.id("form:description")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("A2A", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[1]//input")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Application-to-Application", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[1]/td[2]//input")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("B2B", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[1]//input")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business-to-Business", getDriver().findElement(By.xpath("//tbody[@id='form:tbl_data']/tr[2]/td[2]//input")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
