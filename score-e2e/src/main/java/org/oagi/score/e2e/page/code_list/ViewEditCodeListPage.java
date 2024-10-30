package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Code List' page.
 */
public interface ViewEditCodeListPage extends Page, SearchBarPage {

    EditCodeListPage openCodeListViewEditPage(CodeListObject codeList);

    /**
     * Open the code list edit page by the given code list.
     *
     * @param codeList             code list
     * @param openWithoutSearching {@code true} if it needs to open without searching through the UI, otherwise {@code false}
     * @return the code list edit page
     */
    EditCodeListPage openCodeListViewEditPage(CodeListObject codeList, boolean openWithoutSearching);

    WebElement getNameField();

    void setName(String name);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    /**
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

    WebElement getTableRecordByValue(String value);

    void hitSearchButton();

    void setBranch(String branch);

    WebElement getBranchSelectField();

    void searchCodeListByNameAndBranch(String name, String releaseNumber);

    void searchCodeListByNameAndDeprecation(CodeListObject cl, String releaseNumber);

    void setDeprecated(CodeListObject codeList);

    WebElement getDeprecatedSelectField();

    void setState(String state);

    WebElement getStateSelectField();

    void setOwner(String owner);

    WebElement getOwnerSelectField();

    void searchCodeListByDefinitionAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getDefinitionField();

    void searchCodeListByModuleAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getModuleField();

    void searchCodeListByUpdatedDateAndBranch(CodeListObject codeList, String releaseNumber);

    WebElement getUpdatedDateField();

    EditCodeListPage openNewCodeList(AppUserObject user, String releaseNumber);

    WebElement getNewCodeListButton();
}
