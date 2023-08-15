package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;

/**
 * An interface of 'View/Edit Code List' page.
 */
public interface ViewEditCodeListPage extends Page {

    EditCodeListPage openCodeListViewEditPageByNameAndBranch(String name, String branch);

    EditCodeListPage openCodeListViewEditPageByManifestId(BigInteger codeListManifestId);

    WebElement getNameField();

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getTableRecordByValue(String value);

    void hitSearchButton();

    WebElement getSearchButton();

    void setBranch(String branch);

    WebElement getBranchSelectField();

    void searchCodeListByNameAndBranch(String name, String releaseNumber);

    void searchCodeListByNameAndDeprecation(CodeListObject cl, String releaseNumber);

    void setDeprecated(CodeListObject codeList);

    WebElement getDeprecatedSelectField();

    void toggleState(String state);

    WebElement getStateSelectField();

    void searchCodeListByDefinitionAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getDefinitionField();

    void searchCodeListByModuleAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getModuleField();

    void searchCodeListByUpdatedDateAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getUpdatedDateField();

    EditCodeListPage openNewCodeList(AppUserObject user, String releaseNumber);

    WebElement getNewCodeListButton();
}
