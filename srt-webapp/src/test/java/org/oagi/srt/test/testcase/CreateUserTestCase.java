package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class CreateUserTestCase extends BaseTestCase {
    
    @Test
    public void testCaseCreateAccount() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.jsf");
        // Create sample user: testuser/testtest.
        getDriver().findElement(By.id("newAccountLink")).click();
        getDriver().findElement(By.id("sign_up_form:username")).clear();
        getDriver().findElement(By.id("sign_up_form:username")).sendKeys("testuser");
        getDriver().findElement(By.id("sign_up_form:user_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_password")).sendKeys("testtest");
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).sendKeys("testtest");
        getDriver().findElement(By.id("sign_up_form:create_account")).click();
        // Login.
        getDriver().findElement(By.id("username")).clear();
        getDriver().findElement(By.id("username")).sendKeys("testuser");
        getDriver().findElement(By.id("password")).clear();
        getDriver().findElement(By.id("password")).sendKeys("testtest");
        getDriver().findElement(By.id("signInBtn")).click();
        try {
            assertEquals("testuser", getDriver().findElement(By.linkText("testuser")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        // Logout.
        getDriver().findElement(By.linkText("testuser")).click();
        getDriver().findElement(By.linkText("Sign out")).click();
        try {
            assertEquals("Sign in to OAGi SRT Application", getDriver().findElement(By.cssSelector("h1.text-center")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertTrue(isElementPresent(By.id("signInBtn")));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
        try {
            assertTrue(isElementPresent(By.id("newAccountLink")));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }
}
