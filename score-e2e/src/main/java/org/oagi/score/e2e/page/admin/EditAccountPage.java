package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.AccountUpdateException;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Edit Account' page
 */
public interface EditAccountPage extends Page {

    /**
     * Return the UI element of 'Login ID' field.
     *
     * @return the UI element of 'Login ID' field
     */
    WebElement getLoginIDField();

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
     * @param confirmPassword confirmPassword text
     */
    void setConfirmPassword(String confirmPassword);

    /**
     * Return the UI element of 'Update' button.
     *
     * @return the UI element of 'Update' button
     */
    WebElement getUpdateButton();

    /**
     * Update the password of the account.
     *
     * @param newPassword new password
     * @return the parent 'Accounts' page object
     * @throws AccountUpdateException if the update request failed
     */
    AccountsPage updatePassword(String newPassword) throws AccountUpdateException;

    /**
     * Return an error message on the password field if exists.
     *
     * @return An error message on the password field
     */
    String getPasswordErrorMessage();

    /**
     * Return the UI element of the 'Enable this account' button.
     *
     * @return the UI element of the 'Enable this account' button
     */
    WebElement getEnableThisAccountButton();

    /**
     * Enable the account.
     */
    void enableAccount();

    /**
     * Return the UI element of the 'Disable this account' button.
     *
     * @return the UI element of the 'Disable this account' button
     */
    WebElement getDisableThisAccountButton();

    /**
     * Disable the account.
     */
    void disableAccount();

    /**
     * Return {@code true} if the 'Delete Account' button is present, otherwise {@code false}.
     *
     * @return {@code true} if the 'Delete Account' button is present, otherwise {@code false}
     */
    boolean deleteAccountButtonIsPresent();

}
