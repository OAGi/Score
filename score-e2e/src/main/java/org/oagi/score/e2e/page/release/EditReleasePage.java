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
}
