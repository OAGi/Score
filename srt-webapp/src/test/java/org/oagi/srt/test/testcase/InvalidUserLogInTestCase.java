package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class InvalidUserLogInTestCase extends BasicTestCase {

    @Test
    public void testCaseInvalidRegularUserLogin() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.xhtml");
        // Invalid login.
        getDriver().findElement(By.id("username")).clear();
        getDriver().findElement(By.id("username")).sendKeys("testuser");
        getDriver().findElement(By.id("password")).clear();
        getDriver().findElement(By.id("password")).sendKeys("testuser");
        getDriver().findElement(By.id("signInBtn")).click();
        // Warning: verifyTextPresent may require manual changes
        try {
            assertTrue(getDriver().findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Invalid username or password[\\s\\S]*$"));
//            assertEquals("× Invalid username or password", getDriver().findElement(By.xpath("//form[@id='loginForm']/div/div")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}


