package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'New Account' page
 */
public interface NewAccountPage extends Page {

    /**
     * Return the UI element of 'Login ID' field.
     *
     * @return the UI element of 'Login ID' field
     */
    WebElement getLoginIDField();

    /**
     * Set {@code loginID} text to the 'Login ID' field.
     *
     * @param loginID loginID text
     */
    void setLoginID(String loginID);

    /**
     * Return the UI element of 'Name' field.
     *
     * @return the UI element of 'Name' field
     */
    WebElement getNameField();

    /**
     * Set {@code name} text to the 'Name' field.
     *
     * @param name name text
     */
    void setName(String name);

    /**
     * Return the UI element of 'Organization' field.
     *
     * @return the UI element of 'Organization' field
     */
    WebElement getOrganizationField();

    /**
     * Set {@code organization} text to the 'Organization' field.
     *
     * @param organization organization text
     */
    void setOrganization(String organization);

    /**
     * Return the UI element of 'Developer' checkbox.
     *
     * @return the UI element of 'Developer' checkbox
     */
    WebElement getDeveloperCheckbox();

    /**
     * Set the new user as a developer if the parameter is {@code true}.
     *
     * @param developer whether it is a developer or an end-user
     */
    void setDeveloper(boolean developer);

    /**
     * Return the UI element of 'Admin' checkbox.
     *
     * @return the UI element of 'Admin' checkbox
     */
    WebElement getAdminCheckbox();

    /**
     * Set the new user as an administrator if the parameter is {@code true}.
     *
     * @param admin whether it is an admin or not
     */
    void setAdmin(boolean admin);

    /**
     * Return the UI element of 'Password' field.
     *
     * @return the UI element of 'Password' field
     */
    WebElement getPasswordField();

    /**
     * Set {@code password} text to the 'Password' field.
     *
     * @param password password text
     */
    void setPassword(String password);

    /**
     * Return the UI element of 'Confirm Password' field.
     *
     * @return the UI element of 'Confirm Password' field
     */
    WebElement getConfirmPasswordField();

    /**
     * Set {@code confirmPassword} text to the 'Confirm Password' field.
     *
     * @param confirmPassword confirm password text
     */
    void setConfirmPassword(String confirmPassword);

    /**
     * Return the UI element of 'Create' button.
     *
     * @return the UI element of 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Create a new user with the given parameters.
     *
     * @param user parameters for the new user
     */
    void createNewAccount(AppUserObject user);

    /**
     * Return an error message on the password field if exists.
     *
     * @return An error message on the password field
     */
    String getPasswordErrorMessage();

}
