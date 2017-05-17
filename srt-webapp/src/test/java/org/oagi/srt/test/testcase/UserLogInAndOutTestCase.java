package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class UserLogInAndOutTestCase extends BaseTestCase {

    @Test
    public void testCaseValidRegularUserLoginAndOut() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.jsf");
        // Login.
        getDriver().findElement(By.id("username")).clear();
        getDriver().findElement(By.id("username")).sendKeys("testuser");
        getDriver().findElement(By.id("password")).clear();
        getDriver().findElement(By.id("password")).sendKeys("testtest");
        getDriver().findElement(By.id("signInBtn")).click();
        // Verify that we are on index page.
        try {
            assertEquals("Profile BODs", getDriver().findElement(By.linkText("Profile BODs")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("Profile BOD Expression", getDriver().findElement(By.linkText("Profile BOD Expression")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertEquals("To advance semantic precision of interface definitions exposing OAGIS", getDriver().findElement(By.cssSelector("p")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        // Logout.
        getDriver().findElement(By.linkText("testuser")).click();
        getDriver().findElement(By.linkText("Sign out")).click();
    }

}

