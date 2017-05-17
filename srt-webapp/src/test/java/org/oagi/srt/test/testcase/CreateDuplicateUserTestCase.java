package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic on 5/16/2017.
 */
public class CreateDuplicateUserTestCase extends BasicTestCase {
    
    @Test
    public void testCaseDuplicateAccountCreation() throws Exception {
        getDriver().get(getBaseUrl() + "/views/user/login.xhtml");
        // Create new user (with existing username): testuser/miroslav1234.
        getDriver().findElement(By.id("newAccountLink")).click();
        getDriver().findElement(By.id("sign_up_form:username")).clear();
        getDriver().findElement(By.id("sign_up_form:username")).sendKeys("testuser");
        getDriver().findElement(By.id("sign_up_form:user_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_password")).sendKeys("miroslav1234");
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).sendKeys("miroslav1234");
        getDriver().findElement(By.id("sign_up_form:create_account")).click();
        // Verify message.
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("div.ui-messages-error.ui-corner-all"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        try {
            assertEquals("'testuser' username is already used.", getDriver().findElement(By.cssSelector("div.ui-messages-error.ui-corner-all")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}
