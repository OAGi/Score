package org.oagi.score.e2e.page.release;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * An interface of 'View/Edit Release' page.
 */
public interface ViewEditReleasePage extends Page {
    /**
     * Return the UI element of the 'Creator' select field.
     *
     * @return the UI element of the 'Creator' select field
     */
    WebElement getCreatorSelectField();

    /**
     * Set the 'Creator' select field.
     *
     * @param creator userId
     */
    void setCreator(String creator);

    /**
     * Return the UI element of the 'Created Start Date' field.
     *
     * @return the UI element of the 'Created Start Date' field
     */
    WebElement getCreatedStartDateField();

    /**
     * Set the 'Created Start Date' field with the given date.
     *
     * @param createdStartDate Created Start Date
     */
    void setCreatedStartDate(LocalDateTime createdStartDate);

    /**
     * Return the UI element of the 'Created End Date' field.
     *
     * @return the UI element of the 'Created End Date' field
     */
    WebElement getCreatedEndDateField();

    /**
     * Set the 'Created End Date' field with the given date.
     *
     * @param createdEndDate Created End Date
     */
    void setCreatedEndDate(LocalDateTime createdEndDate);

    /**
     * Return the UI element of the 'Updater' select field.
     *
     * @return the UI element of the 'Updater' select field
     */
    WebElement getUpdaterSelectField();

    /**
     * Set the 'Updater' select field.
     *
     * @param updater userId
     */
    void setUpdater(String updater);

    /**
     * Return the UI element of the 'Updated Start Date' field.
     *
     * @return the UI element of the 'Updated Start Date' field
     */
    WebElement getUpdatedStartDateField();

    /**
     * Set the 'Updated Start Date' field with the given date.
     *
     * @param updatedStartDate Updated Start Date
     */
    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    /**
     * Return the UI element of the 'Updated End Date' field.
     *
     * @return the UI element of the 'Updated End Date' field
     */
    WebElement getUpdatedEndDateField();

    /**
     * Set the 'Updated End Date' field with the given date.
     *
     * @param updatedEndDate Updated End Date
     */
    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    /**
     * Return the UI element of the 'Namespace' select field.
     *
     * @return the UI element of the 'Namespace' select field
     */
    WebElement getNamespaceSelectField();

    /**
     * Set the 'Namespace' select field.
     *
     * @param namespace Namespace
     */
    void setNamespace(String namespace);

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the UI element of the 'State' select field with the given type.
     *
     * @param state
     */
    void setState(String state);

    /**
     * Return the UI element of the 'Release Num' field.
     *
     * @return the UI element of the 'Release Num' field
     */
    WebElement getReleaseNumField();

    /**
     * Set the 'Release Num' field with given text.
     *
     * @param releaseNum release num text
     */
    void setReleaseNum(String releaseNum);

    /**
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Hit the 'Search' button.
     */
    void hitSearchButton();

    /**
     * Open the page of the Release filtered by `Release Num` and `state`.
     *
     * @param releaseNum Release Num text
     * @param State      "Initialized", "Draft" and "Published"
     * @return the Release page object
     */
    EditReleasePage openReleaseViewEditPageByReleaseAndState(String releaseNum, String State);

    /**
     * Open the page of the Release by its ID.
     *
     * @param releaseId release ID
     * @return the Release page object
     */
    EditReleasePage openReleaseViewEditPageByID(BigInteger releaseId);

    /**
     * Return the UI element of the 'New Release' button.
     *
     * @return the UI element of the 'New Release' button
     */
    WebElement getNewReleaseButton();

    /**
     * Create a new Release.
     *
     * @return the Release page object
     */
    CreateReleasePage createRelease();

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
     * Return the UI element of the 'Context Menu' icon for the release node.
     *
     * @param releaseNum release Number
     * @return the UI element of the 'Context Menu' icon
     */
    WebElement getContextMenuIconByReleaseNum(String releaseNum);

    /**
     * Return the UI element of the 'Back To Initialized' button.
     *
     * @param releaseNum release Number
     * @return the UI element of the 'Back To Initialized' button
     */
    WebElement getMoveBackToInitializedButton(String releaseNum);

    /**
     * Hit the 'BackToInitialized' button.
     *
     * @param releaseNum release Number
     */
    void MoveBackToInitialized(String releaseNum);

    void hitDiscardButton(String releaseNumber);

    WebElement getTableRecordAtIndex(int idx);

    WebElement clickOnDropDownMenu(WebElement element);
}
