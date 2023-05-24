package org.oagi.score.e2e.page.namespace;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'View/Edit Namespace' page.
 */
public interface ViewEditNamespacePage extends Page {
    WebElement getOwerSelectField();

    void setOwner(String owner);

    WebElement getStandardSelectField();

    void toggleStandard(boolean isStandard);

    WebElement getURIField();

    void setURI(String uri);

    WebElement getPrefixField();

    void setPrefix(String prefix);

    /**
     * Return the UI element of the 'Updater' select field.
     *
     * @return the UI element of the 'Updater' select field
     */
    WebElement getUpdaterSelectField();

    /**
     * Set the 'Updater' select field with the given text.
     *
     * @param updater Updater
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

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getTableRecordByValue(String value);

    /**
     * Return the UI element of the table record at the given index, which starts from 1.
     *
     * @param idx The index of the table record.
     * @return the UI element of the table record at the given index
     */
    WebElement getTableRecordAtIndex(int idx);

    void hitSearchButton();

    WebElement getSearchButton();

    WebElement getNewNamespaceButton();
    CreateNamespacePage hitNewNamespaceButton();

    EditNamespacePage openNamespaceByURIAndOwner(String uri, String owner);

    /**
     * Open the 'Transfer Namespace Ownership' dialog.
     *
     * @param tr the table record
     * @return the 'Transfer Namespace Ownership' dialog object
     */
    TransferNamespaceOwnershipDialog openTransferNamespaceOwnershipDialog(WebElement tr);
}
