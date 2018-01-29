package org.oagi.srt.test.testcase.user;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class CreateUserTestCase extends BaseTestCase {
    
    @Test
    public void testCaseCreateAccount() throws Exception {
        open("/signup");

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
