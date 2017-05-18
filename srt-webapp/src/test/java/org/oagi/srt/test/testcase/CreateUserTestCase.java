package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
public class CreateUserTestCase extends BaseTestCase {
    
    @Test
    public void testCaseCreateAccount() throws Exception {
        open("/views/user/login.jsf");

        // Create sample user: testuser/testtest.
        createUser("testuser", "testtest");

        // Login.
        login("testuser", "testtest");

        assertTextEqual(By.linkText("testuser"), "testuser");

        // Logout.
        logout("testuser");

        assertTextEqual(By.cssSelector("h1.text-center"), "Sign in to OAGi SRT Application");
        assertElementPresent(By.id("signInBtn"));
        assertElementPresent(By.id("newAccountLink"));
    }

}
