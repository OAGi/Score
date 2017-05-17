package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class CreateUserWithUnmatchedPasswordsTestCase extends BasicTestCase {

    @Test
    public void testCaseCreateARegularUserWithUnmatchedPasswords() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.xhtml");
        // Create new user.
        getDriver().findElement(By.id("newAccountLink")).click();
        getDriver().findElement(By.id("sign_up_form:username")).clear();
        getDriver().findElement(By.id("sign_up_form:username")).sendKeys("jane");
        getDriver().findElement(By.id("sign_up_form:user_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_password")).sendKeys("janejane");
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).sendKeys("janeserm");
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
            assertEquals("Passwords do not match.", getDriver().findElement(By.cssSelector("span.ui-messages-error-detail")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
