package org.oagi.score.e2e.page;

import org.oagi.score.e2e.AccountUpdateException;
import org.openqa.selenium.WebElement;

/**
 * An interface of Settings page.
 */
public interface SettingsPage extends Page {

    /**
     * Return the UI element of 'Old Password' field.
     *
     * @return the UI element of 'Old Password' field
     */
    WebElement getOldPasswordField();

    /**
     * Set {@code password} text to the 'Old Password' field.
     *
     * @param oldPassword old password text
     */
    void setOldPassword(String oldPassword);

    /**
     * Return the UI element of 'New Password' field.
     *
     * @return the UI element of 'New Password' field
     */
    WebElement getNewPasswordField();

    /**
     * Set {@code newPassword} text to the 'New Password' field.
     *
     * @param newPassword new password text
     */
    void setNewPassword(String newPassword);

    /**
     * Return the UI element of 'Confirm New Password' field.
     *
     * @return the UI element of 'Confirm New Password' field
     */
    WebElement getConfirmNewPasswordField();

    /**
     * Set {@code confirmPassword} text to the 'Confirm New Password' field.
     *
     * @param confirmNewPassword confirm new password text
     */
    void setConfirmNewPassword(String confirmNewPassword);

    /**
     * Return the UI element of 'Update' button.
     *
     * @return the UI element of 'Update' button
     */
    WebElement getUpdateButton();

    /**
     * Update the password of the account with the given new password.
     *
     * @param oldPassword old password
     * @param newPassword new password
     * @return home page object
     * @throws AccountUpdateException if the update request failed
     */
    HomePage updatePassword(String oldPassword, String newPassword) throws AccountUpdateException;

    /**
     * Update the password of the account with the given new password.
     *
     * @param oldPassword        old password
     * @param newPassword        new password
     * @param confirmNewPassword confirm new password
     * @return home page object
     * @throws AccountUpdateException if the update request failed
     */
    HomePage updatePassword(String oldPassword, String newPassword, String confirmNewPassword)
            throws AccountUpdateException;

    /**
     * Return an error message on the password field if exists.
     *
     * @return An error message on the password field
     */
    String getPasswordErrorMessage();

}
