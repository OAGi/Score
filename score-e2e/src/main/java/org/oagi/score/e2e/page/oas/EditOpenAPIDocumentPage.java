package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Edit OpenAPI Document' page.
 */
public interface EditOpenAPIDocumentPage extends Page {

    /**
     * Return the UI element of the 'OpenAPI Version' select field.
     *
     * @return the UI element of the 'OpenAPI Version' select field
     */
    WebElement getOpenAPIVersionSelectField();

    /**
     * Set the 'OpenAPI Version' select field with the given text.
     *
     * @param openAPIVersion OpenAPI Version
     */
    void setOpenAPIVersion(String openAPIVersion);

    /**
     * Return the UI element of the 'Title' field.
     *
     * @return the UI element of the 'Title' field
     */
    WebElement getTitleField();

    /**
     * Set the 'Title' field with the given text.
     *
     * @param title Title
     */
    void setTitle(String title);

    /**
     * Return the UI element of the 'Document Version' field.
     *
     * @return the UI element of the 'Document Version' field
     */
    WebElement getDocumentVersionField();

    /**
     * Set the 'Document Version' field with the given text.
     *
     * @param documentVersion Document Version
     */
    void setDocumentVersion(String documentVersion);

    /**
     * Return the UI element of the 'Terms of Service' field.
     *
     * @return the UI element of the 'Terms of Service' field
     */
    WebElement getTermsOfServiceField();

    /**
     * Set the 'Terms of Service' field with the given text.
     *
     * @param termsOfService Terms of Service
     */
    void setTermsOfService(String termsOfService);

    /**
     * Return the UI element of the 'Contact Name' field.
     *
     * @return the UI element of the 'Contact Name' field
     */
    WebElement getContactNameField();

    /**
     * Set the 'Contact Name' field with the given text.
     *
     * @param contactName Contact Name
     */
    void setContactName(String contactName);

    /**
     * Return the UI element of the 'Contact URL' field.
     *
     * @return the UI element of the 'Contact URL' field
     */
    WebElement getContactURLField();

    /**
     * Set the 'Contact URL' field with the given text.
     *
     * @param contactURL Contact URL
     */
    void setContactURL(String contactURL);

    /**
     * Return the UI element of the 'Contact Email' field.
     *
     * @return the UI element of the 'Contact Email' field
     */
    WebElement getContactEmailField();

    /**
     * Set the 'Contact Email' field with the given text.
     *
     * @param contactEmail Contact Email
     */
    void setContactEmail(String contactEmail);

    /**
     * Return the UI element of the 'License Name' field.
     *
     * @return the UI element of the 'License Name' field
     */
    WebElement getLicenseNameField();

    /**
     * Set the 'License Name' field with the given text.
     *
     * @param licenseName License Name
     */
    void setLicenseName(String licenseName);

    /**
     * Return the UI element of the 'License URL' field.
     *
     * @return the UI element of the 'License URL' field
     */
    WebElement getLicenseURLField();

    /**
     * Set the 'License URL' field with the given text.
     *
     * @param licenseURL License URL
     */
    void setLicenseURL(String licenseURL);

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set the 'Description' field with the given text.
     *
     * @param description Description
     */
    void setDescription(String description);

    /**
     * Return the UI element of the 'Update' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Create' button
     */
    WebElement getUpdateButton(boolean enabled);

    /**
     * Hit the 'Update' button
     */
    void hitUpdateButton();

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Hit the 'Discard' button.
     *
     * @return 'OpenAPI Document' page object
     */
    OpenAPIDocumentPage hitDiscardButton();

    /**
     * Return the UI element of the 'Add' button.
     *
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton();

    /**
     * Open 'Add BIE for OpenAPI Document' dialog.
     *
     * @return 'Add BIE for OpenAPI Document' dialog
     */
    AddBIEForOpenAPIDocumentDialog openAddBIEForOpenAPIDocumentDialog();

}
