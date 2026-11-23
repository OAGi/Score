package org.oagi.score.e2e.page.release;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditReleasePage extends Page {

    WebElement getReleaseNumberField();

    void setReleaseNum(String releaseNum);

    WebElement getReleaseNamespaceField();

    void setReleaseNamespace(NamespaceObject releaseNamespace);

    WebElement getReleaseNoteField();

    void setReleaseNote(String releaseNote);


    WebElement getReleaseLicenseField();

    void setReleaseLicense(String releaseLicense);

    WebElement getUpdateButton();

    void hitUpdateButton();

    WebElement getCreateDraftButton();

    ReleaseAssignmentPage hitCreateDraftButton();

    void backToInitialized();

    WebElement getBackToInitializedButton();

    WebElement getPublishButton();

    void publish();

    /**
     * The notice that says that the release is being worked on (it is being drafted, taken back to 'Initialized' or published),
     * which is shown in place of the usual buttons.
     */
    WebElement getInProgressNotice();

    WebElement getRecoverButton();

    /**
     * Gives a release that was left in the middle of its work back to the state it was in before, and waits for the answer.
     */
    void recover();
}
