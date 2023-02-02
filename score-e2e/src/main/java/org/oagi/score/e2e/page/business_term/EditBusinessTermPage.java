package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Edit Business Term' page.
 */
public interface EditBusinessTermPage extends Page {
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
     * Return the UI element of the 'External Reference ID' field.
     *
     * @return the UI element of the 'External Reference ID' field
     */
    WebElement getExternalReferenceIDField();

    /**
     * Set {@code externalReferenceID} text to the 'External Reference ID' field.
     *
     * @param externalReferenceID External Reference ID text input
     */
    void setExternalReferenceID(String externalReferenceID);

    /**
     * Return the text of the 'External Reference ID' field.
     *
     * @return the text of the 'External Reference ID' field
     */
    String getExternalReferenceIDFieldText();

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Return the text of the 'Definition' field.
     *
     * @return the text of the 'Definition' field
     */
    String getDefinitionFieldText();

    /**
     * Return the UI element of the 'Comment' field.
     *
     * @return the UI element of the 'Comment' field
     */
    WebElement getCommentField();

    /**
     * Set {@code comment} text to the 'Comment' field.
     *
     * @param comment Comment text
     */
    void setComment(String comment);

    /**
     * Return the text of the 'Comment' field.
     *
     * @return the text of the 'Comment' field
     */
    String getCommentFieldText();

    /**
     * Return the UI element of the 'Update' button.
     *
     * @return the UI element of the 'Update' button
     */
    WebElement getUpdateButton();

    /**
     * Update a business term with the given information.
     *
     * @param businessTerm business term information for updating
     */
    void updateBusinessTerm(BusinessTermObject businessTerm);

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Discard the business term
     *
     * @return 'View/Edit Business Term' page object
     */
    ViewEditBusinessTermPage discard();

    /**
     * Discard the business term.
     *
     * @return 'View/Edit Business Term' page object
     */
    ViewEditBusinessTermPage discardBusinessTerm();


}
