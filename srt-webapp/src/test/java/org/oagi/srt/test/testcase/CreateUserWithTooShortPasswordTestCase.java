package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateUserWithTooShortPasswordTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateARegularUserWithTooShortPassword() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.xhtml");
        // Create new user (with short password): jane/jane.
        getDriver().findElement(By.id("newAccountLink")).click();
        getDriver().findElement(By.id("sign_up_form:username")).clear();
        getDriver().findElement(By.id("sign_up_form:username")).sendKeys("jane");
        getDriver().findElement(By.id("sign_up_form:user_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_password")).sendKeys("jane");
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).sendKeys("jane");
        getDriver().findElement(By.id("sign_up_form:create_account")).click();
        // Verify message.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-messages-error-detail"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("Password is too short (minimum is 5 characters)", getDriver().findElement(By.cssSelector("span.ui-messages-error-detail")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }
}

