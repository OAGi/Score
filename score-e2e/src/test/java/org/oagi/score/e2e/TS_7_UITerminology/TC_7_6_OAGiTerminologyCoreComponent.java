package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_6_OAGiTerminologyCoreComponent extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private String release = "10.8.4";

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_7_6_TA_1")
    public void core_component_page_uses_core_component_title() {
        AppUserObject appUser = getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        appUser.setPassword("oagis");

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        assertEquals("Core Component", getText(viewEditCoreComponentPage.getTitle()));
    }

    @Test
    @DisplayName("TC_7_6_TA_2_and_TA_3")
    public void core_component_search_panel_uses_search_by_den_and_module_labels() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
        ASCCPObject asccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.showAdvancedSearchPanel();

        String denFieldLabel = viewEditCoreComponentPage.getDENFieldLabel();
        assertEquals("Search by DEN", denFieldLabel);

        String moduleFieldLabel = viewEditCoreComponentPage.getModuleFieldLabel();
        assertEquals("Module (Part of schema file path, no extension)", moduleFieldLabel);
    }

    @Test
    @DisplayName("TC_7_6_TA_4")
    @Disabled
    public void acc_tree_panel_uses_component_type_or_group_definition_title() {
        //The title of the left pane where the tree of an ACC is displayed should be “ACC (Component Type or Group Definition)”.

    }

    @Test
    @DisplayName("TC_7_6_TA_5_and_TA_6")
    public void acc_pages_use_oagi_terminology_for_object_class_term_and_den_labels() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
        ASCCPObject asccp;
        ACCObject acc;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();

        viewEditCoreComponentPage.toggleToDevView();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        String objectClassTermFieldACCPageTitle = accViewEditPage.getObjectClassTermFieldLabel();
        assertEquals("Object Class Term (Space Separated Name)", objectClassTermFieldACCPageTitle);

        String denFieldACCPageTitle = accViewEditPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldACCPageTitle);

        //CHECK THE SAME FOR THE LOCAL BIE EXTENSION

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        String objectClassTermFieldBIEPageTitleLocalExtension = accExtensionViewEditPage.getObjectClassTermFieldLabel();
        assertEquals("Object Class Term (Space Separated Name)", objectClassTermFieldBIEPageTitleLocalExtension);

        String denFieldBIEPageTitleLocalExtension = accExtensionViewEditPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldBIEPageTitleLocalExtension);

        // CHECK THE SAME FOR THE GLOBAL BIE EXTENSION

        BusinessContextObject contextGlobalExtension = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiepGlobalExtension = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextGlobalExtension), asccp, endUser, "WIP");
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepGlobalExtension);
        accExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        String objectClassTermFieldBIEPageTitleGlobalExtension = accExtensionViewEditPage.getObjectClassTermFieldLabel();
        assertEquals("Object Class Term (Space Separated Name)", objectClassTermFieldBIEPageTitleGlobalExtension);

        String denFieldBIEPageTitleGlobalExtension = accExtensionViewEditPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldBIEPageTitleGlobalExtension);
    }

    @Test
    @DisplayName("TC_7_6_TA_7")
    @Disabled
    public void asccp_tree_panel_uses_component_or_group_definition_title() {
        //The title of the left pane where the tree of an ASCCP is displayed should be “ASCCP (Component or Group Definition)”.
    }

    @Test
    @DisplayName("TC_7_6_TA_8")
    @Disabled
    public void asccp_detail_panel_uses_associated_component_title() {
        //The title of the right pane where the details of an ASCCP are displayed should be “ASCCP (Associated Component) Detail”.

    }

    @Test
    @DisplayName("TC_7_6_TA_9_and_TA_10")
    public void asccp_detail_panel_uses_component_name_and_dictionary_entry_name_labels() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
        ASCCPObject asccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());

        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), release.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        String propertyTermFieldLabel = asccpPanel.getPropertyTermFieldLabel();
        assertEquals("Property Term (Component Name)", propertyTermFieldLabel);

        String denFieldLabel = asccpPanel.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldLabel);
    }

    @Test
    @DisplayName("TC_7_6_TA_11")
    @Disabled
    public void bccp_detail_panel_uses_associated_field_title() {
        //The title of the right pane where the details of a BCCP are displayed should be “BCCP (Associated Field) Detail”.
    }

    @Test
    @DisplayName("TC_7_6_TA_12_and_TA_13")
    public void bccp_detail_panel_uses_field_name_and_dictionary_entry_name_labels() {
        LibraryObject library;
        ReleaseObject branch;
        BCCPObject bccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject dataType = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Identifier. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            bccp = coreComponentAPI.createRandomBCCP(branch, dataType, developer, namespace, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), branch.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        String propertyTermFieldLabel = bccpPanel.getPropertyTermFieldLabel();
        assertEquals("Property Term (Field Name)", propertyTermFieldLabel);

        String denFieldLabel = bccpPanel.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldLabel);
    }

    @Test
    @DisplayName("TC_7_6_TA_14")
    @Disabled
    public void ascc_detail_panel_uses_component_association_title() {
        //The title of the right pane where the details of an ASCC are displayed should be “ASCC (Component Association) Detail”. (It cannot be checked yet).

    }

    @Test
    @DisplayName("TC_7_6_TA_15_and_TA_16")
    public void ascc_detail_panel_uses_cardinality_max_and_dictionary_entry_name_labels() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
        ASCCPObject asccp;
        ACCObject accFrom;
        ACCObject accTo;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            accTo = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(accTo, developer, namespace, "Published");
            accFrom = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendASCC(accFrom, asccp, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(accFrom.getDen(), release.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        WebElement node = accViewEditPage.getNodeByPath("/" + accFrom.getDen() + "/" + accTo.getObjectClassTerm());
        click(node);
        waitFor(ofMillis(500L));
        String denFieldTitleForASCC = accViewEditPage.getDenFieldLabelForASCC();
        assertEquals("DEN (Dictionary Entry Name)", denFieldTitleForASCC);
        String cardinalityMaxTitle = accViewEditPage.getCardinalityLabel();
        assertEquals("Cardinality Max (-1 for unbounded)", cardinalityMaxTitle);
    }

    @Test
    @DisplayName("TC_7_6_TA_17")
    @Disabled
    public void bcc_detail_panel_uses_field_association_title() {
        //The title of the right pane where the details of a BCC are displayed should be “BCC (Field Association) Detail”.

    }

    @Test
    @DisplayName("TC_7_6_TA_18_and_TA_19")
    public void bcc_detail_panel_uses_cardinality_max_and_dictionary_entry_name_labels() {
        LibraryObject library;
        ReleaseObject branch;
        ACCObject acc;
        BCCPObject bccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject dataType = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Identifier. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            acc = coreComponentAPI.createRandomACC(developer, branch, namespace, "Published");
            bccp = coreComponentAPI.createRandomBCCP(branch, dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), branch.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        click(bccNode);
        waitFor(ofMillis(500L));
        String denFieldTitleForBCC = accViewEditPage.getDenFieldLabelForBCC();
        assertEquals("DEN (Dictionary Entry Name)", denFieldTitleForBCC);
        String cardinalityMaxTitle = accViewEditPage.getCardinalityLabel();
        assertEquals("Cardinality Max (-1 for unbounded)", cardinalityMaxTitle);
    }

    @Test
    @DisplayName("TC_7_6_TA_20")
    @Disabled
    public void supplementary_component_panel_uses_field_metadata_title() {
        //The title of the right pane where the details of a supplementary component are displayed should be
        // “Supplementary Component (Field Metadata)”. (It cannot be checked yet).

    }

    @Test
    @DisplayName("TC_7_6_TA_21_to_TA_23")
    public void supplementary_and_data_type_components_use_cardinality_and_dictionary_entry_name_labels() {
        LibraryObject library;
        ReleaseObject branch;
        ACCObject acc;
        BCCPObject bccp;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject dataType = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Identifier. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            acc = coreComponentAPI.createRandomACC(developer, branch, namespace, "Published");
            bccp = coreComponentAPI.createRandomBCCP(branch, dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), branch.getReleaseNumber());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        WebElement bdtScNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm() + "/" + "Identifier. Scheme Version. Identifier");
        click(bdtScNode);
        waitFor(ofMillis(500L));
        String cardinalityMaxTitle = accViewEditPage.getCardinalityLabel();
        assertEquals("Cardinality Max (-1 for unbounded)", cardinalityMaxTitle);
        String denFieldTitleSuplementary = accViewEditPage.getDENFieldLabel();
        assertEquals("DEN (Dictionary Entry Name)", denFieldTitleSuplementary);

        WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        click(bccNode);
        waitFor(ofMillis(500L));
        String denFieldTitleDT = accViewEditPage.getDENFieldLabelDT();
        assertEquals("DEN (Dictionary Entry Name)", denFieldTitleDT);
    }

    /*@Test
    public void test() {
        for (String loginID : Arrays.asList("eu_K316X", "dev_azHuS2k7m")) {
            thisAccountWillBeDeletedAfterTests(getAPIFactory().getAppUserAPI().getAppUserByLoginID(loginID));
        }
    }*/

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

}
