package org.oagi.score.e2e.page.release;

import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
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
     * @param creator  userId
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
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the UI element of the 'State' select field with the given type.
     * @param state
     */
    void setState(String state);



    /**
     * Return the UI element of the 'DEN' field.
     *
     * @return the UI element of the 'DEN' field
     */
    WebElement getDENField();

    /**
     * Return the 'DEN' field label text.
     *
     * @return the 'DEN' field label text
     */
    String getDENFieldLabel();

    /**
     * Set the 'DEN' field with given text.
     *
     * @param den DEN text
     */
    void setDEN(String den);

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Set the 'Definition' field with given text.
     *
     * @param definition Definition text
     */
    void setDefinition(String definition);

    /**
     * Return the UI element of the 'Module' field.
     *
     * @return the UI element of the 'Module' field
     */
    WebElement getModuleField();

    /**
     * Return the 'Module' field label text.
     *
     * @return the 'Module' field label text
     */
    String getModuleFieldLabel();

    /**
     * Set the 'Module' field with given text.
     *
     * @param module Module text
     */
    void setModule(String module);

    /**
     * Return the UI element of the 'Component Type' select field.
     *
     * @return the UI element of the 'Component Type' select field
     */
    WebElement getComponentTypeSelectField();

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
     * Open the page of the ACC filtered by `den` and `branch`.
     *
     * @param den DEN text
     * @param branch Branch text
     * @return the ACC page object
     */
    ACCViewEditPage openACCViewEditPageByDenAndBranch(String den, String branch);

    /**
     * Open the page of the ACC by its manifest ID.
     *
     * @param accManifestID manifest ID
     * @return the ACC page object
     */
    ACCViewEditPage openACCViewEditPageByManifestID(BigInteger accManifestID);

    /**
     * Open the page of the ASCCP filtered by `den` and `branch`.
     *
     * @param den DEN text
     * @param branch Branch text
     * @return the ASCCP page object
     */
    ASCCPViewEditPage openASCCPViewEditPageByDenAndBranch(String den, String branch);

    /**
     * Open the page of the ASCCP by its manifest ID.
     *
     * @param asccpManifestID manifest ID
     * @return the ASCCP page object
     */
    ASCCPViewEditPage openASCCPViewEditPageByManifestID(BigInteger asccpManifestID);

    /**
     * Open the page of the BCCP filtered by `den` and `branch`.
     *
     * @param den DEN text
     * @param branch Branch text
     * @return the BCCP page object
     */
    BCCPViewEditPage openBCCPViewEditPageByDenAndBranch(String den, String branch);
}
