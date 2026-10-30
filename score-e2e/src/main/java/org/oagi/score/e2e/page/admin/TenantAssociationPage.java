package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.page.Page;

import java.util.List;

/**
 * An interface of a page that lists what is associated with a tenant, and what can be: the 'Manage Users' page
 * (the key of a row is the login ID) and the 'Manage Business Context' page (the key of a row is the name).
 * The page starts with the associated ones; the add view lists the ones that are not.
 */
public interface TenantAssociationPage extends Page {

    /**
     * Search the table with the given text and wait for the result.
     *
     * @param text the login ID or the name
     */
    void search(String text);

    /**
     * Return the keys of the listed rows, once the table has loaded.
     *
     * @return the keys of the listed rows
     */
    List<String> getListedKeys();

    /**
     * Click 'Add User' / 'Add Business Context' to list what can be associated with the tenant.
     */
    void switchToAddView();

    /**
     * Click 'Done' to list what is associated with the tenant again.
     */
    void switchToListView();

    /**
     * Click 'Add' of the row in the add view.
     *
     * @param key the key of the row
     */
    void add(String key);

    /**
     * Click 'Remove' of the row in the list view.
     *
     * @param key the key of the row
     */
    void remove(String key);

}
