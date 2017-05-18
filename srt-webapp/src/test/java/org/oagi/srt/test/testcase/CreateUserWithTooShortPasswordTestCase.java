package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateUserWithTooShortPasswordTestCase extends BaseTestCase {

    @Test
    public void testCaseCreateARegularUserWithTooShortPassword() throws Exception {
        open("/views/user/login.xhtml");

        // Create new user (with short password): jane/jane.
        createUser("jane", "jane");

        // Verify message.
        waitForElementPresent(By.cssSelector("span.ui-messages-error-detail"));
        assertTextEqual(By.cssSelector("span.ui-messages-error-detail"), "Password is too short (minimum is 5 characters)");
    }

}

