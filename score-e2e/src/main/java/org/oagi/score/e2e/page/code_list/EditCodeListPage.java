package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface EditCodeListPage extends Page {
    void setDefinition(String test_definition);

    void setDefinitionSource(String test_definition_source);

    WebElement getDefinitionField();

    WebElement getDefinitionSourceField();

    void hitUpdateButton();

    WebElement getUpdateButton();

    EditCodeListValueDialog addCodeListValue();

    WebElement getAddCodeListValueButton();

    AddCommentDialog hitAddCommentButton();

    WebElement getAddCommentButton();

    void hitRevise();

    WebElement getReviseButton();

    WebElement getCodeListNameField();

    WebElement getVersionField();

    WebElement getDeprecatedSelectField();

    WebElement getNamespaceSelectField();

    WebElement getReleaseField();

    WebElement getRevisionField();

    WebElement getAgencyIDListField();

    void setName(String codeListName);

    void selectCodeListValue(String valueCode);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void removeCodeListValue();

    WebElement getRemoveValueButton();

    WebElement getDeriveCodeListBasedOnThisButton();

    void hitDeriveCodeListBasedOnThisButton();

    void setVersion(String version);

    String getDefinitionWarningDialogMessage();

    WebElement getUpdateAnywayButton();

    void hitUpdateAnywayButton();

    void setNamespace(NamespaceObject namespace);

    EditCodeListValueDialog editCodeListValue(String value);

    WebElement getStateField();

    WebElement getGuidField();

    WebElement getListIDField();

    void hitCancelButton();

    WebElement getCancelButton();

    void valueExists(String value);

    WebElement getOwnerField();

    void moveToDraft();

    WebElement getMoveToDraftButton();

    void backToWIP();

    void moveToCandidate();

    WebElement getBackToWIPButton();

    WebElement getMoveToCandidateButton();

    void hitDeleteButton();

    WebElement getDeleteButton();

    void hitRestoreButton();

    WebElement getRestoreButton();

    WebElement getRemarkField();

    WebElement getAgencyIDListValueField();
}
