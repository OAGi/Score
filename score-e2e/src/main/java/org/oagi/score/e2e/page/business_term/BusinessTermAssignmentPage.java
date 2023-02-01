package org.oagi.score.e2e.page.business_term;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface for 'Business Term Assignment' page
 */
public interface BusinessTermAssignmentPage extends Page {

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

    /**
     * Return the UI element of the 'Type' field.
     *
     * @return the UI element of the 'Type' field
     */
    WebElement getTypeField();

    /**
     * Set the 'Type' select field with the given text.
     *
     * @param bieType BBIE or ASBIE
     */
    void setType(String bieType);

    /**
     *Return the UI element of the 'BIE DEN' field
     * @return the UI element of the 'BIE DEN' field
     */
    WebElement getBIEDenField();

    /**
     * Set the 'BIE DEN' field with the given BIE DEN
     */
    void setBIEDenField(String bieDen);

    /**
     * Return the UI element of the 'Business Term' field.
     *
     * @return the UI element of the 'Business Term' field
     */
    WebElement getBusinessTermField();

    /**
     * Set {@code businessTerm} text to the 'Business Term' field.
     *
     * @param businessTerm Business Term text
     */
    void setBusinessTerm(String businessTerm);

    /**
     * Return the text of the 'Business Term' field.
     *
     * @return the text of the 'Business Term' field
     */
    String getBusinessTermFieldText();

    /**
     * Return the UI element of the 'External Reference URI' field.
     *
     * @return the UI element of the 'External Reference URI' field
     */
    WebElement getExternalReferenceURIField();

    /**
     * Set {@code externalReferenceURI} text to the 'External Reference URI' field.
     *
     * @param externalReferenceURI External Reference URI text input
     */
    void setExternalReferenceURI(String externalReferenceURI);

    /**
     * Return the text of the 'External Reference URI' field.
     *
     * @return the text of the 'External Reference URI' field
     */
    String getExternalReferenceURIFieldText();

    /**
     * Return the UI element of the 'Type Code' field.
     *
     * @return the UI element of the 'Type Code' field
     */
    WebElement getTypeCodeField();

    /**
     * Return the text of the 'Type Code' field.
     *
     * @return the text of the 'Type Code' field
     */
    String getTypeCodeFieldText();

    /**
     * Set {@code typeCode} text to the 'Type Code' field.
     * @param typeCode input for 'Type Code' field
     */
    void setTypeCodeField(String typeCode);

    /**
     * Return the UI element of the 'Preferred Only' checkbox.
     * @return the UI element of the 'Preferred Only' checkbox
     */
    WebElement getPreferredOnlyCheckbox();

    /**
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Return the UI element of the 'Turn Off' button.
     * @return the UI element of the 'Turn Off' button
     */
    WebElement getTurnOffButton();


    /**
     * Return the UI element of the 'Assign Business Term' button.
     * @return the UI element of the 'Assign Business Term' button.
     */
    WebElement getAssignBusinessTermButton();

    /**
     * Assign business term.
     *
     * @return 'Assign Business Term' page object
     */
    AssignBusinessTermPage assignBusinessTerm();

    /**
     * Return the UI element of the 'Search By Selected BIE' button
     * @return the UI element of the 'Search By Selected BIE' button.
     */
    WebElement getSearchBySelectedBIEButton();

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Discard the business term.
     *
     * @return 'Business Term Assignment' page object
     */
    BusinessTermAssignmentPage discardBusinessTerm();

}
