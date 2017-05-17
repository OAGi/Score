package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class UserLogInTestCase extends BaseTestCase {

    @Test
    public void testCaseValidRegularUserLogin() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.jsf");
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(By.id("username"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }

        // Login user testuser/testtest.
        getDriver().findElement(By.id("username")).clear();
        getDriver().findElement(By.id("username")).sendKeys("testuser");
        getDriver().findElement(By.id("password")).clear();
        getDriver().findElement(By.id("password")).sendKeys("testtest");
        getDriver().findElement(By.id("signInBtn")).click();
        // Verify user is logged in.
        try {
            assertEquals("testuser", getDriver().findElement(By.linkText("testuser")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}