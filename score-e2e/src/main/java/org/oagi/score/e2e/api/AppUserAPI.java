package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;

import java.math.BigInteger;

/**
 * APIs for the account management.
 */
public interface AppUserAPI {

    /**
     * Return the account object associated with the given login ID.
     *
     * @param loginID login ID
     * @return account object
     */
    AppUserObject getAppUserByLoginID(String loginID);

    /**
     * Return the account object associated with the given user ID.
     *
     * @param appUserId user ID
     * @return account object
     */
    AppUserObject getAppUserByID(BigInteger appUserId);

    /**
     * Create the account as requested.
     *
     * @param appUser account object
     * @return generated account ID
     */
    BigInteger createAppUser(AppUserObject appUser);

    /**
     * Create a random developer account.
     *
     * @param admin {@code true} if the account should be an admin, otherwise {@code false}
     * @return a created account object
     */
    AppUserObject createRandomDeveloperAccount(boolean admin);

    /**
     * Create a random end-user account.
     *
     * @param admin {@code true} if the account should be an admin, otherwise {@code false}
     * @return a created account object
     */
    AppUserObject createRandomEndUserAccount(boolean admin);

    /**
     * Disable the account if it is enabled.
     *
     * @param appUser account object.
     */
    void disableAccount(AppUserObject appUser);

    /**
     * Delete the account by the given account ID.
     *
     * @param appUserId account ID
     */
    void deleteAppUserById(BigInteger appUserId);

    /**
     * Delete the account by the given login ID.
     *
     * @param loginId login ID
     */
    void deleteAppUserByLoginId(String loginId);

}
