package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.page.Page;

/**
 * An interface of the page that edits a tenant: its name can be changed, and the tenant can be discarded.
 */
public interface TenantEditPage extends Page {

    /**
     * Return the name that the page shows.
     *
     * @return the name that the page shows
     */
    String getName();

    /**
     * Replace the name.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Click 'Update' and return the message of the snackbar that answers it.
     *
     * @return the message of the snackbar
     */
    String update();

    /**
     * Click 'Discard' and return what the confirmation dialog says, including how many accounts and business contexts
     * are associated with the tenant. The dialog stays open.
     *
     * @return the text of the confirmation dialog
     */
    String openDiscardDialog();

    /**
     * Confirm the open discard dialog and return the message of the snackbar that answers it.
     *
     * @return the message of the snackbar
     */
    String confirmDiscard();

    /**
     * Cancel the open discard dialog.
     */
    void cancelDiscard();

}
