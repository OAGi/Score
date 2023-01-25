package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.WebElement;

public interface CreateBusinessTermPage extends BasePage {

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
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Create a new business term with the given business term object.
     *
     * @param businessTerm Business Term object
     * @return the 'View/Edit Business Term' page object
     */
    ViewEditBusinessTermPage createBusinessTerm(BusinessTermObject businessTerm);

}
