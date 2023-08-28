package org.oagi.score.e2e.page.release;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.WebElement;

public interface CreateReleasePage extends BasePage {
    /**
     * Return the UI element of the 'Release Number' field.
     *
     * @return the UI element of the 'Release Number' field
     */
    WebElement getReleaseNumberField();

    /**
     * Set {@code releaseNumber} text to the 'Release Number' field.
     *
     * @param releaseNumber releaseNumber text
     */
    void setReleaseNumber(String releaseNumber);

    /**
     * Return the UI element of the 'Release Namespace' field.
     *
     * @return the UI element of the 'Release Namespace' field
     */
    WebElement getReleaseNamespaceSelectField();

    /**
     * Set {@code releaseNamespace} text to the 'Release Namespace' field.
     *
     * @param releaseNamespace namespace object
     */
    void setReleaseNamespace(NamespaceObject releaseNamespace);

    /**
     * Return the UI element of the 'Release Note' field.
     *
     * @return the UI element of the 'Release Note' field
     */
    WebElement getReleaseNoteField();

    ;

    /**
     * Set {@code releaseNote} text to the 'Release Note' field.
     *
     * @param releaseNote Release Note
     */
    void setReleaseNote(String releaseNote);

    /**
     * Return the UI element of the 'Release License' field.
     *
     * @return the UI element of the 'Release License' field
     */
    WebElement getReleaseLicenseField();

    /**
     * Set {@code  releaseLicense} text to the 'Release License' field.
     *
     * @param releaseLicense release License text
     */
    void setReleaseLicense(String releaseLicense);

    WebElement getCreateButton();

    void hitCreateButton();
}
