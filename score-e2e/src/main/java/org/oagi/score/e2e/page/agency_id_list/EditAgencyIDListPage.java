package org.oagi.score.e2e.page.agency_id_list;

import org.oagi.score.e2e.obj.AgencyIDListValueObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditAgencyIDListPage extends Page {

    /**
     * Return the UI element of the 'Core Component' field.
     *
     * @return the UI element of the 'Core Component' field
     */
    WebElement getCoreComponentField();

    /**
     * Return the UI element of the 'GUID' field.
     *
     * @return the UI element of the 'GUID' field
     */
    WebElement getGUIDField();

    /**
     * Return the UI element of the 'Release' field.
     *
     * @return the UI element of the 'Release' field
     */
    WebElement getReleaseField();

    /**
     * Return the UI element of the 'Revision' field.
     *
     * @return the UI element of the 'Revision' field
     */
    WebElement getRevisionField();

    /**
     * Return the UI element of the 'State' field.
     *
     * @return the UI element of the 'State' field
     */
    WebElement getStateField();

    /**
     * Return the UI element of the 'Owner' field.
     *
     * @return the UI element of the 'Owner' field
     */
    WebElement getOwnerField();

    /**
     * Return the UI element of the 'Based Agency ID List' field.
     *
     * @return the UI element of the 'Based Agency ID List' field
     */
    WebElement getBasedAgencyIDListField();

    /**
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getAgencyIDListNameField();

    /**
     * Set the 'Name' field.
     *
     * @param agencyIDListName Name
     */
    void setName(String agencyIDListName);

    /**
     * Return the UI element of the 'List ID' field.
     *
     * @return the UI element of the 'List ID' field
     */
    WebElement getListIDField();

    /**
     * Set the 'List ID' field.
     *
     * @param listId List ID
     */
    void setListID(String listId);

    /**
     * Return the UI element of the 'Version' field.
     *
     * @return the UI element of the 'Version' field
     */
    WebElement getVersionField();

    /**
     * Set the 'Version' field.
     *
     * @param version Version
     */
    void setVersion(String version);

    /**
     * Return the UI element of the 'Agency ID List Value' select field.
     *
     * @return the UI element of the 'Agency ID List Value' select field
     */
    WebElement getAgencyIDListValueSelectField();

    /**
     * Set the 'Agency ID List Value' select field.
     *
     * @param agencyIDListValue Agency ID List Value object
     */
    void setAgencyIDListValue(AgencyIDListValueObject agencyIDListValue);

    /**
     * Return the UI element of the 'Namespace' select field.
     *
     * @return the UI element of the 'Namespace' select field
     */
    WebElement getNamespaceSelectField();

    /**
     * Set the 'Namespace' select field.
     *
     * @param namespace Namespace object
     */
    void setNamespace(NamespaceObject namespace);

    /**
     * Return the UI element of the 'Deprecated' checkbox.
     *
     * @return the UI element of the 'Deprecated' checkbox
     */
    WebElement getDeprecatedCheckbox();

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Set the 'Definition' select field.
     *
     * @param definition Definition
     */
    void setDefinition(String definition);

    /**
     * Return the UI element of the 'Definition Source' field.
     *
     * @return the UI element of the 'Definition Source' field
     */
    WebElement getDefinitionSourceField();

    /**
     * Set the 'Definition Source' select field.
     *
     * @param definitionSource Definition Source
     */
    void setDefinitionSource(String definitionSource);

    /**
     * Return the UI element of the 'Remark' field.
     *
     * @return the UI element of the 'Remark' field
     */
    WebElement getRemarkField();

    /**
     * Set the 'Remark' select field.
     *
     * @param remark Remark
     */
    void setRemark(String remark);

    /**
     * Return the UI element of the 'Update' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Update' button
     */
    WebElement getUpdateButton(boolean enabled);

    /**
     * Hit the 'Update' button.
     */
    void hitUpdateButton();

    /**
     * Return the UI element of the 'Delete' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Delete' button
     */
    WebElement getDeleteButton(boolean enabled);

    /**
     * Delete the agency ID list.
     */
    void delete();

    /**
     * Return the UI element of the 'Restore' button.
     *
     * @return the UI element of the 'Delete' button
     */
    WebElement getRestoreButton();

    /**
     * Restore the deleted agency ID list.
     */
    void restore();

    /**
     * Return the UI element of the 'Comment' button.
     *
     * @return the UI element of the 'Comment' button
     */
    WebElement getCommentButton();

    /**
     * Open the 'Comment' dialog.
     *
     * @return the 'Comment' dialog object
     */
    AddCommentDialog openCommentDialog();

    /**
     * Return the UI element of the 'Revise' button.
     *
     * @return the UI element of the 'Revise' button
     */
    WebElement getReviseButton();

    /**
     * Revise the current Agency ID List.
     */
    void revise();

    /**
     * Return the UI element of the 'Amend' button.
     *
     * @return the UI element of the 'Amend' button
     */
    WebElement getAmendButton();

    /**
     * Amend the current Agency ID List.
     */
    void amend();

    /**
     * Return the UI element of the 'Cancel' button.
     *
     * @return the UI element of the 'Cancel' button
     */
    WebElement getCancelButton();

    /**
     * Cancel revising of the current Agency ID List.
     */
    void cancel();

    /**
     * Return the UI element of the 'Move to Draft' button.
     *
     * @return the UI element of the 'Move to Draft' button
     */
    WebElement getMoveToDraftButton();

    /**
     * Move the state of the agency ID list to 'Draft'.
     */
    void moveToDraft();

    /**
     * Return the UI element of the 'Move to Candidate' button.
     *
     * @return the UI element of the 'Move to Candidate' button
     */
    WebElement getMoveToCandidateButton();

    /**
     * Move the state of the agency ID list to 'Candidate'.
     */
    void moveToCandidate();

    /**
     * Return the UI element of the 'Move to QA' button.
     *
     * @return the UI element of the 'Move to QA' button
     */
    WebElement getMoveToQAButton();

    /**
     * Move the state of the agency ID list to 'QA'.
     */
    void moveToQA();

    /**
     * Return the UI element of the 'Move to Production' button.
     *
     * @return the UI element of the 'Move to Production' button
     */
    WebElement getMoveToProductionButton();

    /**
     * Move the state of the agency ID list to 'Production'.
     */
    void moveToProduction();

    /**
     * Return the UI element of the 'Back to WIP' button.
     *
     * @return the UI element of the 'Back to WIP' button
     */
    WebElement getBackToWIPButton();

    /**
     * Move the state of the agency ID list back to 'WIP'.
     */
    void backToWIP();

    /**
     * Return the UI element of the 'Derived Agency ID List' button.
     *
     * @return the UI element of the 'Derived Agency ID List' button
     */
    WebElement getDeriveAgencyIDListButton();

    /**
     * Hit the 'Derived Agency ID List' button.
     */
    void hitDeriveAgencyIDListButton();

    /**
     * Return the UI element of the 'Add' button for the new agency ID list value.
     *
     * @return the UI element of the 'Add' button for the new agency ID list value
     */
    WebElement getAddAgencyIDListValueButton();

    /**
     * Open the Agency ID List Value dialog to add a new one.
     *
     * @return the Agency ID List Value dialog
     */
    EditAgencyIDListValueDialog addAgencyIDListValue();

    /**
     * Return the UI element of the 'Remove' button for the agency ID list value(s).
     *
     * @return the UI element of the 'Remove' button for the agency ID list value(s)
     */
    WebElement getRemoveAgencyIDListValueButton();

    /**
     * Hit the 'Remove' button for the agency ID list value(s).
     */
    void hitRemoveAgencyIDListValueButton();

    EditAgencyIDListValueDialog openAgencyIDListValueDialogByValue(String value);

    /**
     * Return the UI element of the 'Search' field.
     *
     * @return the UI element of the 'Search' field
     */
    WebElement getSearchField();

    /**
     * Set the 'Search' field with the given text.
     *
     * @param search search
     */
    void setSearch(String search);

    /**
     * Return the UI element of 'Search' button.
     *
     * @return the UI element of 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Hit the 'Search' button
     */
    void hitSearchButton();

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
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

    /**
     * Return the total number of items being paged.
     *
     * @return the total number of items being paged
     */
    int getTotalNumberOfItems();

    /**
     * Return the UI element of the 'Previous Page' button in the paginator.
     *
     * @return the UI element of the 'Previous Page' button in the paginator
     */
    WebElement getPreviousPageButton();

    /**
     * Return the UI element of the 'Next Page' button in the paginator.
     *
     * @return the UI element of the 'Next Page' button in the paginator
     */
    WebElement getNextPageButton();

}
