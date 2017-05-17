package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateBusinessContextTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateBusinessContextB2BProcessPO() throws Exception {
        getDriver().get(getBaseUrl() + "/index.jsf");
        // Create Business Context.
        getDriver().findElement(By.linkText("Context Management")).click();
        getDriver().findElement(By.linkText("Business Context")).click();
        getDriver().findElement(By.id("listForm:createBtn")).click();
        getDriver().findElement(By.id("form:name")).clear();
        getDriver().findElement(By.id("form:name")).sendKeys("B2B Process PO");
        // Add Business Context Values.
        getDriver().findElement(By.id("form:addBtn")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).sendKeys("Business Process Context Category");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).sendKeys(Keys.TAB);

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys("Business Process Context Scheme");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys(Keys.TAB);

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys("ProcessPO");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys(Keys.TAB);

        getDriver().findElement(By.id("form:addBtn")).click();

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).sendKeys("Integration Context Category");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).sendKeys(Keys.TAB);

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys("Integration Context Scheme");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).sendKeys(Keys.TAB);

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/button"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).click();
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys("B2B");

        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-autocomplete-query"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }
        getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).sendKeys(Keys.TAB);

        getDriver().findElement(By.id("form:createBtn")).click();
        // Verify that Business Context is created.
        try {
            assertEquals("B2B Process PO", getDriver().findElement(By.linkText("B2B Process PO")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        getDriver().findElement(By.linkText("B2B Process PO")).click();
        try {
            assertEquals("B2B Process PO", getDriver().findElement(By.id("form:name")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business Process Context Category", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[1]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Business Process Context Scheme", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[2]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("ProcessPO", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[1]/td[3]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Integration Context Category", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[1]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Integration Context Scheme", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[2]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("B2B", getDriver().findElement(By.xpath("//table[@role='grid']/tbody/tr[2]/td[3]/span/input[1]")).getAttribute("value"));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
