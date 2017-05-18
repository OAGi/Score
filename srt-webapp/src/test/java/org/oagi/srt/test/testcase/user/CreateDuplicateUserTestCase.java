package org.oagi.srt.test.testcase.user;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateDuplicateUserTestCase extends BaseTestCase {
    
    @Test
    public void testCaseDuplicateAccountCreation() throws Exception {
        open("/views/user/login.xhtml");

        // Create new user (with existing username): testuser/miroslav1234.
        createUser("testuser", "miroslav1234");

        // Verify message.
        waitForElementPresent(By.cssSelector("div.ui-messages-error.ui-corner-all"));
        assertTextEqual(By.cssSelector("div.ui-messages-error.ui-corner-all"), "'testuser' username is already used.");
    }

}
