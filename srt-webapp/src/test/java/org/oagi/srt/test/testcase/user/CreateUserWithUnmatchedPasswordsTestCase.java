package org.oagi.srt.test.testcase.user;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateUserWithUnmatchedPasswordsTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateARegularUserWithUnmatchedPasswords() throws Exception {
        open("/signin");

        // Create new user.
        click(By.id("newAccountLink"));
        type(By.id("sign_up_form:username"), "jane");
        type(By.id("sign_up_form:user_password"), "janejane");
        type(By.id("sign_up_form:user_confirm_password"), "janeserm");
        click(By.id("sign_up_form:create_account"));

        // Verify message.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Passwords do not match.");
    }

}
