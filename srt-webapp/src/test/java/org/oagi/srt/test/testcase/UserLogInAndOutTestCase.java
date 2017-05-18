package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class UserLogInAndOutTestCase extends BaseTestCase {

    @Test
    public void testCaseValidRegularUserLoginAndOut() throws Exception {
        open("/views/user/login.jsf");

        // Login.
        login("testuser", "testtest");

        // Verify that we are on index page.
        assertTextEqual(By.linkText("Profile BODs"), "Profile BODs");
        assertTextEqual(By.linkText("Profile BOD Expression"), "Profile BOD Expression");
        assertTextEqual(By.cssSelector("p"), "To advance semantic precision of interface definitions exposing OAGIS");

        // Logout.
        logout("testuser");
    }

}

