package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.math.BigInteger;

/**
 * An interface of the 'Edit BIE Package' (BIE Package detail) page.
 */
public interface EditBIEPackagePage extends Page {

    /**
     * Open the detail page of the BIE Package with the given identifier.
     */
    void openPage(BigInteger biePackageId);

    /**
     * The identifier of the BIE Package currently shown by this page object.
     */
    BigInteger getBiePackageId();

    //
    // Header metadata fields
    //

    WebElement getNameField();

    void setName(String name);

    String getName();

    void setVersionId(String versionId);

    void setVersionName(String versionName);

    void setDescription(String description);

    //
    // Revision Reason (issue #1733)
    //

    /**
     * Whether the 'Revision Reason' form field is rendered (it is rendered only for a revised
     * package, i.e. one with a prior version).
     */
    boolean isRevisionReasonFieldPresent();

    WebElement getRevisionReasonField();

    void setRevisionReason(String revisionReason);

    String getRevisionReason();

    /**
     * Whether the 'Revision Reason' field is editable (enabled).
     */
    boolean isRevisionReasonFieldEnabled();

    //
    // Toolbar actions
    //

    void hitUpdateButton();

    void moveToQA();

    void moveToProduction();

    void backToWIP();

    boolean isReviseButtonPresent();

    /**
     * Click 'Revise', confirm, and return the page object for the new (WIP) revision.
     */
    EditBIEPackagePage revise();

    //
    // Generation
    //

    /**
     * Select the export expression. Accepts {@code "XML"} or {@code "JSON"}.
     */
    void selectExpression(String expression);

    /**
     * Click 'Generate' and wait for the downloaded BIE Package ZIP.
     */
    File clickGenerateAndDownloadZip();
}
