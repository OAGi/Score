package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class UserLogInTestCase extends BaseTestCase {

    @Test
    public void testCaseValidRegularUserLogin() throws Exception {
        open("/views/user/login.jsf");

        waitForElementPresent(By.id("username"));

        // Login user testuser/testtest.
        login("testuser", "testtest");

        // Verify user is logged in.
        assertTextEqual(By.linkText("testuser"), "testuser");
    }

}