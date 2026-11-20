package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.TS_25_DeveloperBIEManagement.ValueDomainOptions.Offer.NOT_OFFERED;
import static org.oagi.score.e2e.TS_25_DeveloperBIEManagement.ValueDomainOptions.Offer.SELECTABLE;
import static org.oagi.score.e2e.TS_25_DeveloperBIEManagement.ValueDomainOptions.Offer.SHOWN_BUT_DISABLED;

/**
 * Test Case 25.4: the Code Lists that a developer is offered as the Value Domain of a BBIE, and the ones that a developer
 * cannot assign.
 *
 * The Value Domain of a BBIE built on a data type that names no Code List offers the Code Lists of the release of the BIE.
 * A developer is offered only the Published and Production ones. A Production Code List is the one of an end user, which a
 * developer sees but cannot assign: the option is shown disabled.
 *
 * See docs/test_cases/TestSuite25.md (Test Case 25.4).
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_25_4_CodeListsADeveloperIsOfferedForAValueDomain extends BaseTest {

    // A "Text" BDT (release 10.8.8) with a Token primitive that names no Code List and no Agency ID List.
    private static final String TEXT_BDT_GUID = "89be97039be04d6f9cfda107d75926b5";

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

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_25_4")
    public void developer_is_offered_published_and_production_code_lists_and_cannot_assign_a_production_one() {
        AppUserObject developer;
        ASCCPObject asccp;
        BCCPObject bccp;
        TopLevelASBIEPObject developerBIE;
        CodeListObject publishedDeveloperList, wipDeveloperList, productionEndUserList, wipEndUserList;

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        String releaseNumber = "10.8.8";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, releaseNumber);
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);
            NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            publishedDeveloperList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, developerNamespace, release, "Published");
            wipDeveloperList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, developerNamespace, release, "WIP");
            productionEndUserList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, endUserNamespace, release, "Production");
            wipEndUserList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, endUserNamespace, release, "WIP");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
            CoreComponentAPI cc = getAPIFactory().getCoreComponentAPI();
            DTObject textBdt = cc.getBDTByGuidAndReleaseNum(library, TEXT_BDT_GUID, releaseNumber);
            ACCObject acc = cc.createRandomACC(developer, release, developerNamespace, "Published");
            bccp = cc.createRandomBCCP(release, textBdt, developer, developerNamespace, "Published");
            cc.appendBCC(acc, bccp, "Published");
            asccp = cc.createRandomASCCP(acc, developer, developerNamespace, "Published");
            developerBIE = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(developerBIE);

        WebElement node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");

        // Assertion #25.4.1: a Published Code List can be chosen.
        assertEquals(SELECTABLE, ValueDomainOptions.offerOf(getDriver(), bbiePanel, publishedDeveloperList.getName()));
        // Assertion #25.4.2: a Code List of a developer that is not Published is not offered, nor is one of an end user that is
        // not Production.
        assertEquals(NOT_OFFERED, ValueDomainOptions.offerOf(getDriver(), bbiePanel, wipDeveloperList.getName()));
        assertEquals(NOT_OFFERED, ValueDomainOptions.offerOf(getDriver(), bbiePanel, wipEndUserList.getName()));
        // Assertion #25.4.3: a Production Code List of an end user is shown, and cannot be chosen.
        assertEquals(SHOWN_BUT_DISABLED, ValueDomainOptions.offerOf(getDriver(), bbiePanel, productionEndUserList.getName()));
    }
}
