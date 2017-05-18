package org.oagi.srt.test.testcase;

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Created by Miroslav Ljubicic.
 */
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


