package org.oagi.score.e2e.TS_24_EndUserBIEManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * The tree search of the BIE editor finds the text of the BIE (definition, remark, ...) through the server, together
 * with the attributes the browser holds, and judges the text that is typed and not saved yet by what is typed.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_24_3_SearchBIETextOnServer extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(randomAccount ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId()));
    }

    /** What the search bar says about the matches it found, e.g. "1/1". */
    private void assertMatchCount(String expected) {
        WebElement count = visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"tree-search-box\")]//*[normalize-space(text()) = \"" + expected + "\"]"));
        // the count may be followed by how much was searched, e.g. "1/1 in 2 nodes"
        assertEquals(expected, count.getText().trim().split(" in ")[0]);
    }

    private void search(EditBIEPage editBIEPage, String expression) {
        WebElement field = editBIEPage.getSearchInputTextField();
        field.clear();
        field.sendKeys(expression);
        field.sendKeys(Keys.ENTER);
    }

    @Test
    public void search_the_text_of_a_bie_on_the_server() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        String currentRelease = "10.8.8";
        ReleaseObject releaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, currentRelease);
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        this.randomAccounts.add(usera);
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);

        ACCObject acc = coreComponentAPI.createRandomACC(usera, releaseObject, namespace, "Production");
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", releaseObject.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(releaseObject, dataType, usera, namespace, "Production");
        coreComponentAPI.appendBCC(acc, bccp, "Production");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");

        String unique = "srv" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(currentRelease);
        viewEditBIEPage.setDEN(bie.getDen());
        viewEditBIEPage.hitSearchButton();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(viewEditBIEPage.getTableRecordAtIndex(1));

        // text that is typed and not saved yet is judged by what is typed
        EditBIEPage.TopLevelASBIEPPanel panel = editBIEPage.getTopLevelASBIEPPanel();
        panel.setContextDefinition("The definition " + unique.toUpperCase());
        search(editBIEPage, "//*[contains(definition, \"" + unique + "\")]");
        assertMatchCount("1/1");

        // saved text is found by the server, whatever the case; and a text that is nowhere is not
        editBIEPage.hitUpdateButton();
        editBIEPage.openPage();
        search(editBIEPage, "//*[contains(definition, \"" + unique + "\")]");
        assertMatchCount("1/1");
        search(editBIEPage, "//*[contains(definition, \"" + unique + "-nowhere\")]");
        assertMatchCount("0/0");

        // a condition on the server mixed with one the browser judges
        search(editBIEPage, "//*[used = true and contains(definition, \"" + unique + "\")]");
        assertMatchCount("1/1");
        search(editBIEPage, "//*[not(contains(definition, \"" + unique + "\")) and type = ABIE]");
        assertMatchCount("0/0");
    }
}
