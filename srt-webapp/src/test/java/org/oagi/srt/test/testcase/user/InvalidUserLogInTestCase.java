package org.oagi.srt.test.testcase.user;

import org.junit.Test;
import org.oagi.srt.test.testcase.BaseTestCase;
import org.openqa.selenium.By;

public class InvalidUserLogInTestCase extends BaseTestCase {

    @Test
    public void testCaseInvalidRegularUserLogin() throws Exception {
        open("/views/user/login.xhtml");

        // Invalid login.
        login("testuser", "testuser");

        // Warning: verifyTextPresent may require manual changes
        try {
            assertTrue(getDriver().findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Invalid username or password[\\s\\S]*$"));
//            assertEquals("× Invalid username or password", getDriver().findElement(By.xpath("//form[@id='loginForm']/div/div")).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

}


