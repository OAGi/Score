package org.oagi.score.e2e.page;

import org.oagi.score.e2e.SignInException;
import org.openqa.selenium.WebElement;

/**
 * An interface of Login page.
 */
public interface LoginPage extends Page {

    /**
     * Return the UI element of the alert message box.
     *
     * @return the UI element of the alert message box
     */
    WebElement getAlert();

    /**
     * Return the UI element of the username input textbox.
     *
     * @return the UI element of the username input textbox
     */
    WebElement getUsernameInput();

    /**
     * Return the UI element of the password input textbox.
     *
     * @return the UI element of the password input textbox
     */
    WebElement getPasswordInput();

    /**
     * Return the UI element of the 'Sign In' button.
     *
     * @return the UI element of the 'Sign In' button
     */
    WebElement getSignInButton();

    /**
     * Set the username text to the username field.
     *
     * @param username username text
     */
    void setUsername(String username);

    /**
     * Set the password text to the password field.
     *
     * @param password password text
     */
    void setPassword(String password);

    /**
     * Click the 'Sign in' button.
     */
    void clickSignIn();

    /**
     * Sign in with the username and the password.
     *
     * @param username username text
     * @param password password text
     * @return Home page
     * @throws SignInException if it has failed sign-in attempts
     */
    HomePage signIn(String username, String password) throws SignInException;

}
