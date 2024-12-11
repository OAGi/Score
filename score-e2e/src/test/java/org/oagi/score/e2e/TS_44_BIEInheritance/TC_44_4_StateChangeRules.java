package org.oagi.score.e2e.TS_44_BIEInheritance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_44_4_StateChangeRules extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_44_4_1")
    public void validate_compliance_with_state_change_rules_in_case_of_InheritedBIE_Move_to_QA() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, asccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId()); // Base BIE belongs to the endUser.
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);

        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(anotherEndUser.getLoginId()); // Inherited BIE belongs to the anotherEndUser.
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(inheritedBieTr);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        editBIEPage.moveToQA();

        String errorMessage = getText(getMultiActionSnackBar(getDriver()).getMessageElement());
        assertNotNull(errorMessage);
    }

    @Test
    @DisplayName("TC_44_4_2")
    public void validate_compliance_with_state_change_rules_in_case_of_BaseBIE_Back_to_WIP() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, asccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId()); // Base BIE belongs to the endUser.
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);

        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        // Base BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement baseBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(baseBieTr);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(baseBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        homePage.logout();
        homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());

        // Inherited BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(anotherEndUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(inheritedBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        // Base BIE 'Back to WIP'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        baseBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(baseBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(baseBieTr);
        editBIEPage.backToWIP();

        String errorMessage = getText(getMultiActionSnackBar(getDriver()).getMessageElement());
        assertNotNull(errorMessage);
    }

    @Test
    @DisplayName("TC_44_4_3")
    public void validate_compliance_with_state_change_rules_in_case_of_InheritedBIE_and_BaseBIE_Back_to_WIP() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, asccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId()); // Base BIE belongs to the endUser.
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);

        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        // Base BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement baseBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(baseBieTr);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(baseBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        homePage.logout();
        homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());

        // Inherited BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(anotherEndUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(inheritedBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        // Inherited BIE 'Back to WIP'
        editBIEPage.backToWIP();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        // Base BIE 'Back to WIP'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        baseBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(baseBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(baseBieTr);
        editBIEPage.backToWIP();
        assertEquals("State updated", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_44_4_4")
    public void validate_compliance_with_state_change_rules_in_case_of_InheritedBIE_Move_to_Production() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, asccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId()); // Base BIE belongs to the endUser.
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);

        homePage.logout();
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        // Base BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement baseBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(baseBieTr);
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(baseBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        homePage.logout();
        homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());

        // Inherited BIE 'Move to QA'
        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(anotherEndUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        assertEquals(1, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = viewEditBIEPage.getTableRecordAtIndex(1);
        assertNotNull(inheritedBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        editBIEPage.moveToQA();
        assertEquals("State updated", getSnackBarMessage(getDriver()));

        // Inherited BIE 'Move to Production'
        editBIEPage.moveToProduction();

        String errorMessage = getText(getMultiActionSnackBar(getDriver()).getMessageElement());
        assertNotNull(errorMessage);
    }


}
