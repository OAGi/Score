package org.oagi.score.e2e.page;

import org.oagi.score.e2e.menu.*;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of home page.
 */
public interface HomePage extends Page {

    /**
     * Return the login ID of the user in the current session.
     *
     * @return the login ID of the user in the current session
     */
    String getLoginID();

    /**
     * Return the UI element of the Score logo.
     *
     * @return the UI element of the Score logo
     */
    WebElement getScoreLogo();

    /**
     * Return the menu object of the 'BIE' menu.
     *
     * @return the menu object of the 'BIE' menu
     */
    BIEMenu getBIEMenu();

    /**
     * Return the menu object of the 'Context' menu.
     *
     * @return the menu object of the 'Context' menu
     */
    ContextMenu getContextMenu();

    /**
     * Return the menu object of the 'Core Component' menu.
     *
     * @return the menu object of the 'Core Component' menu
     */
    CoreComponentMenu getCoreComponentMenu();

    /**
     * Return the menu object of the 'Module' menu.
     *
     * @return the menu object of the 'Module' menu
     */
    ModuleMenu getModuleMenu();

    /**
     * Return the menu object of the 'Admin' menu.
     *
     * @return the menu object of the 'Admin' menu
     */
    AdminMenu getAdminMenu();

    /**
     * Return the menu object of the 'Help' menu.
     *
     * @return the menu object of the 'Help' menu
     */
    HelpMenu getHelpMenu();

    /**
     * Return the menu object of the menu with the login ID.
     *
     * @return the menu object of the menu with the login ID
     */

    LoginIDMenu getLoginIDMenu();

    /**
     * Return the UI element of the NIST/OAGi logo.
     *
     * @return the UI element of the NIST/OAGi logo
     */
    WebElement getNISTOAGiLogo();

    /**
     * Return the UI element of the 'BIEs' tab.
     *
     * @return the UI element of the 'BIEs' tab
     */
    WebElement getBIEsTab();

    /**
     * Return the UI element of the 'User Extensions' tab.
     *
     * @return the UI element of the 'User Extensions' tab
     */
    WebElement getUserExtensionsTab();

    /**
     * Log out of the current account.
     *
     * @return Login page
     */
    LoginPage logout();

    WebElement getBranchSelectField();

    void setBranch(String branch);

    TotalBIEsByStatesPanel openTotalBIEsByStatesPanel();

    MyBIEsByStatesPanel openMyBIEsByStatesPanel();

    BIEsByUsersAndStatesPanel openBIEsByUsersAndStatesPanel();

    MyRecentBIEsPanel openMyRecentBIEsPanel();

    TotalUEsByStatesPanel openTotalUEsByStatesPanel();

    UEsByUsersAndStatesPanel openUEsByUsersAndStatesPanel();

    MyUEsByStatesPanel openMyUEsByStatesPanel();

    MyUnusedUEsInBIEsPanel openMyUnusedUEsInBIEsPanel();

    interface TotalBIEsByStatesPanel {
        WebElement getStateProgressBarByState(String state);

        ViewEditBIEPage clickStateProgressBar(String state);
    }

    interface MyBIEsByStatesPanel {
        WebElement getStateProgressBarByState(String state);

        ViewEditBIEPage clickStateProgressBar(String state);
    }

    interface BIEsByUsersAndStatesPanel {

        /**
         * Return the UI element of the 'User' select field.
         *
         * @return the UI element of the 'User' select field
         */
        WebElement getUsernameSelectField();

        /**
         * Set the username to the 'User' select field.
         *
         * @param username the username
         */
        void setUsername(String username);

        /**
         * Return a list of the UI elements of the table records.
         *
         * @return a list of the UI elements of the table records
         */
        List<WebElement> getTableRecords();

        /**
         * Return the UI element of the table record at the given index, which starts from 1.
         *
         * @param idx The index of the table record.
         * @return the UI element of the table record at the given index
         */
        WebElement getTableRecordAtIndex(int idx);

        /**
         * Return the UI element of the table record containing the given value.
         *
         * @param value value
         * @return the UI element of the table record
         */
        WebElement getTableRecordByValue(String value);

        /**
         * Return the UI element of the column of the given table record with the column name.
         *
         * @param tableRecord the table record
         * @param columnName  the column name
         * @return the UI element of the column
         */
        WebElement getColumnByName(WebElement tableRecord, String columnName);

        /**
         * Open the 'View/Edit BIE' page by clicking the cell in the table based on the username and the column name.
         *
         * @param user       the username
         * @param columnName "WIP", "QA", "Production", and "total"
         * @return the 'View/Edit BIE' page object
         */
        ViewEditBIEPage openViewEditBIEPageByUsernameAndColumnName(String user, String columnName);

    }

    interface MyRecentBIEsPanel {

        /**
         * Return a list of the UI elements of the table records.
         *
         * @return a list of the UI elements of the table records
         */
        List<WebElement> getTableRecords();

        /**
         * Return the UI element of the table record at the given index, which starts from 1.
         *
         * @param idx The index of the table record.
         * @return the UI element of the table record at the given index
         */
        WebElement getTableRecordAtIndex(int idx);

        /**
         * Return the UI element of the table record containing the given value.
         *
         * @param value value
         * @return the UI element of the table record
         */
        WebElement getTableRecordByValue(String value);

        /**
         * Return the UI element of the column of the given table record with the column name.
         *
         * @param tableRecord the table record
         * @param columnName  the column name
         * @return the UI element of the column
         */
        WebElement getColumnByName(WebElement tableRecord, String columnName);

        /**
         * Open the 'Edit BIE' page by clicking the cell in the table based on the DEN.
         *
         * @param den DEN in the "My recent BIEs" panel
         * @return the 'View/Edit BIE' page object
         */
        EditBIEPage openEditBIEPageByDEN(String den);

    }

    interface TotalUEsByStatesPanel {

        WebElement getStateProgressBarByState(String state);

        ViewEditCoreComponentPage clickStateProgressBar(String state);

    }

    interface UEsByUsersAndStatesPanel {

        /**
         * Return the UI element of the 'User' select field.
         *
         * @return the UI element of the 'User' select field
         */
        WebElement getUsernameSelectField();

        /**
         * Set the username to the 'User' select field.
         *
         * @param username the username
         */
        void setUsername(String username);

        /**
         * Return a list of the UI elements of the table records.
         *
         * @return a list of the UI elements of the table records
         */
        List<WebElement> getTableRecords();

        /**
         * Return the UI element of the table record at the given index, which starts from 1.
         *
         * @param idx The index of the table record.
         * @return the UI element of the table record at the given index
         */
        WebElement getTableRecordAtIndex(int idx);

        /**
         * Return the UI element of the table record containing the given value.
         *
         * @param value value
         * @return the UI element of the table record
         */
        WebElement getTableRecordByValue(String value);

        /**
         * Return the UI element of the column of the given table record with the column name.
         *
         * @param tableRecord the table record
         * @param columnName  the column name
         * @return the UI element of the column
         */
        WebElement getColumnByName(WebElement tableRecord, String columnName);

        /**
         * Open the 'View/Edit Core Component' page by clicking the cell in the table based on the username and the column name.
         *
         * @param user       the username
         * @param columnName "WIP", "QA", "Production", "Deleted" and "total"
         * @return the 'View/Edit Core Component' page object
         */
        ViewEditCoreComponentPage openViewEditCCPageByUsernameAndColumnName(String user, String columnName);

    }

    interface MyUEsByStatesPanel {

        WebElement getStateProgressBarByState(String state);

        ViewEditCoreComponentPage clickStateProgressBar(String state);

    }

    interface MyUnusedUEsInBIEsPanel {

        /**
         * Return the table record in the table based on the user extension name and association DEN
         *
         * @param ueName   user extension name
         * @param assocDEN Association DEN
         * @return the table record UI element
         */
        WebElement getTableRecordByUEAndDEN(String ueName, String assocDEN);

        /**
         * Open the 'View/Edit Core Component' page by clicking the cell in the table based on the user extension and association DEN
         *
         * @param ueName   user extension name
         * @param assocDEN Association DEN
         * @return the 'View/Edit Core Component' page object
         */
        ViewEditCoreComponentPage openViewEditCCPageByUEAndDEN(String ueName, String assocDEN);

    }

}
