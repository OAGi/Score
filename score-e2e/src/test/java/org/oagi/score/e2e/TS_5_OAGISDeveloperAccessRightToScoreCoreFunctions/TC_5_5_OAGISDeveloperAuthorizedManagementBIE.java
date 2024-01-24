package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_5_OAGISDeveloperAuthorizedManagementBIE extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_5_5_TA_1")
    public void test_TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        for (String releaseNum : Arrays.asList("10.6", "10.7.1", "10.8")) {
            CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                    homePage.getBIEMenu().openViewEditBIESubMenu().openCreateBIEPage();
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                    createBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
            EditBIEPage editBIEPage =
                    createBIEForSelectTopLevelConceptPage.createBIE("Get BOM. Get BOM", releaseNum);
            assertEquals(releaseNum, getText(editBIEPage.getTopLevelASBIEPPanel().getReleaseField()));
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_2_1 (Change Based ACC)")
    public void test_TA_2_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject roleOfAcc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ACCObject basedACC = coreComponentAPI.getACCByDENAndReleaseNum(
                "Change Acknowledge Risk Control Library. Details", release.getReleaseNumber());
        coreComponentAPI.updateBasedACC(roleOfAcc, basedACC);

        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(roleOfAcc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Intermediary").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Intermediary/Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Data Area/Change Acknowledge").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Data Area/Change Acknowledge/Original Application Area").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Data Area/Risk Control Library/Control Process").isDisplayed());

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        ACCObject revisedRoleOfAcc = coreComponentAPI.createRevisedACC(roleOfAcc, developer, nextRelease, "Published");
        ACCObject newBasedACC = coreComponentAPI.getACCByDENAndReleaseNum(
                "Acknowledge Inventory Count. Details", nextRelease.getReleaseNumber());
        coreComponentAPI.updateBasedACC(revisedRoleOfAcc, newBasedACC);

        ASCCPObject revisedAsccp = coreComponentAPI.createRevisedASCCP(asccp, revisedRoleOfAcc,
                developer, nextRelease, "Published");

        TopLevelASBIEPObject nextTopLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), revisedAsccp, developer, "WIP");

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(nextTopLevelASBIEP);

        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Application Area").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Application Area/Intermediary").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Application Area/Intermediary/Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Data Area/Acknowledge").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Data Area/Acknowledge/Original Application Area").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + revisedAsccp.getPropertyTerm() + "/Data Area/Inventory Count/Inventory Count Header").isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_2_2 (Additional BCCs and ASCCs)")
    public void test_TA_2_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        ACCObject revisedAcc = coreComponentAPI.createRevisedACC(acc, developer, nextRelease, "Published");
        ASCCObject asccAcceptableNameValueRange = coreComponentAPI.appendASCC(
                revisedAcc,
                coreComponentAPI.getASCCPByDENAndReleaseNum("Acceptable Name Value Range. Name Value Range", nextRelease.getReleaseNumber()),
                "Published"
        );
        BCCObject bccAbsenceTypeCode = coreComponentAPI.appendBCC(
                revisedAcc,
                coreComponentAPI.getBCCPByDENAndReleaseNum("Absence Type Code. Open_ Code", nextRelease.getReleaseNumber()),
                "Published"
        );
        ASCCObject asccFinancialAccountIdentifiersGroup = coreComponentAPI.appendASCC(
                revisedAcc,
                coreComponentAPI.getASCCPByDENAndReleaseNum("Financial Account Identifiers Group. Financial Account Identifiers Group", nextRelease.getReleaseNumber()),
                "Published"
        );
        ASCCObject asccAcknowledgeInventoryCount = coreComponentAPI.appendASCC(
                revisedAcc,
                coreComponentAPI.getASCCPByDENAndReleaseNum("Acknowledge Inventory Count. Acknowledge Inventory Count", nextRelease.getReleaseNumber()),
                "Published"
        );
        asccAcknowledgeInventoryCount.setCardinalityMax(1);
        coreComponentAPI.updateASCC(asccAcknowledgeInventoryCount);

        BCCObject bccDensityConversionFactorNumber = coreComponentAPI.appendBCC(
                revisedAcc,
                coreComponentAPI.getBCCPByDENAndReleaseNum("Density Conversion Factor Number. Open_ Number", nextRelease.getReleaseNumber()),
                "Published"
        );
        bccDensityConversionFactorNumber.setCardinalityMax(1);
        coreComponentAPI.updateBCC(bccDensityConversionFactorNumber);

        ASCCPObject revisedAsccp = coreComponentAPI.createRevisedASCCP(asccp, revisedAcc,
                developer, nextRelease, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        assertThrows(TimeoutException.class, () -> editBIEPage.expandTree(asccp.getPropertyTerm()));

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), revisedAsccp, developer, "WIP");
        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Acceptable Name Value Range").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Absence Type Code").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/BBANID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/IBANID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/UPICID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Acknowledge Inventory Count").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Density Conversion Factor Number").isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_2_3 (BCCP was revised to be NOT nillable)")
    public void test_TA_2_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        // Date Time. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        bccp.setNillable(false);
        coreComponentAPI.updateBCCP(bccp);

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        BCCObject bcc = coreComponentAPI.appendBCC(
                acc, bccp, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());

        EditBIEPage.BBIEPanel BBIEPanel = editBIEPage.getBBIEPanel(node);
        WebElement nillable = BBIEPanel.getNillableCheckbox();
        assertNotChecked(nillable);
        assertDisabled(nillable);

        // Move to next release for the revised BCCP
        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        DTObject nextDataType = coreComponentAPI.getBDTByGuidAndReleaseNum(dataType.getGuid(), nextRelease.getReleaseNumber());
        BCCPObject revisedBccp = coreComponentAPI.createRevisedBCCP(bccp, nextDataType, developer, nextRelease, "Published");
        revisedBccp.setNillable(true);
        coreComponentAPI.updateBCCP(revisedBccp);

        ACCObject newAcc = coreComponentAPI.createRandomACC(developer, nextRelease, namespace, "Published");
        ASCCPObject newAsccp = coreComponentAPI.createRandomASCCP(newAcc, developer, namespace, "Published");
        BCCObject newBcc = coreComponentAPI.appendBCC(
                newAcc, revisedBccp, "Published");

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), newAsccp, developer, "WIP");

        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        node = editBIEPageForRevised.getNodeByPath(
                "/" + newAsccp.getPropertyTerm() + "/" + revisedBccp.getPropertyTerm());
        assertTrue(node.isDisplayed());

        BBIEPanel = editBIEPageForRevised.getBBIEPanel(node);
        nillable = BBIEPanel.getNillableCheckbox();
        assertChecked(nillable);
        assertEnabled(nillable);
    }

    @Test
    @DisplayName("TC_5_5_TA_2_4 (ASCCP was revised to be NOT nillable)")
    public void test_TA_2_4() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject roleOfAcc = coreComponentAPI.getACCByDENAndReleaseNum("Name Value Range. Details", release.getReleaseNumber());
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(roleOfAcc, developer, namespace, "Published");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        coreComponentAPI.appendASCC(acc, asccp, "Published");
        ASCCPObject topLevelAsccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), topLevelAsccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + topLevelAsccp.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());

        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        WebElement nillable = ASBIEPanel.getNillableCheckbox();
        assertNotChecked(nillable);
        assertDisabled(nillable);

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        roleOfAcc = coreComponentAPI.getACCByDENAndReleaseNum("Name Value Range. Details", nextRelease.getReleaseNumber());
        ASCCPObject revisedAsccp = coreComponentAPI.createRevisedASCCP(asccp, roleOfAcc, developer, nextRelease, "Published");
        revisedAsccp.setNillable(true);
        coreComponentAPI.updateASCCP(revisedAsccp);

        acc = coreComponentAPI.createRandomACC(developer, nextRelease, namespace, "Published");
        coreComponentAPI.appendASCC(acc, revisedAsccp, "Published");
        topLevelAsccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), topLevelAsccp, developer, "WIP");
        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        node = editBIEPageForRevised.getNodeByPath(
                "/" + topLevelAsccp.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());

        ASBIEPanel = editBIEPageForRevised.getASBIEPanel(node);
        nillable = ASBIEPanel.getNillableCheckbox();
        assertChecked(nillable);
        assertEnabled(nillable);
    }

    @Test
    @DisplayName("TC_5_5_TA_2_5 (Additional ASCC to an ASCCP that is a group)")
    public void test_TA_2_5() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        coreComponentAPI.appendASCC(
                acc,
                coreComponentAPI.getASCCPByDENAndReleaseNum("Financial Account Identifiers Group. Financial Account Identifiers Group", release.getReleaseNumber()),
                "Published"
        );
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        // Shouldn't be displayed 'Financial Account Identifiers Group' node in paths.
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Identifier").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/BBANID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/IBANID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/UPICID").isDisplayed());
        assertTrue(editBIEPageForRevised.getNodeByPath("/" + asccp.getPropertyTerm() + "/Account Identifiers").isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_2_6 (Max cardinality of a BCC and an ASCC changed from unbounded to 1)")
    public void test_TA_2_6() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        ASCCObject asccAcknowledgeInventoryCount = coreComponentAPI.appendASCC(
                acc,
                coreComponentAPI.getASCCPByDENAndReleaseNum("Acknowledge Inventory Count. Acknowledge Inventory Count", release.getReleaseNumber()),
                "Published"
        );
        asccAcknowledgeInventoryCount.setCardinalityMax(1);
        coreComponentAPI.updateASCC(asccAcknowledgeInventoryCount);

        BCCObject bccDensityConversionFactorNumber = coreComponentAPI.appendBCC(
                acc,
                coreComponentAPI.getBCCPByDENAndReleaseNum("Density Conversion Factor Number. Open_ Number", release.getReleaseNumber()),
                "Published"
        );
        bccDensityConversionFactorNumber.setCardinalityMax(1);
        coreComponentAPI.updateBCC(bccDensityConversionFactorNumber);

        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement asccpNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Acknowledge Inventory Count");
        assertTrue(asccpNode.isDisplayed());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asccpNode);
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));

        WebElement bccpNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Density Conversion Factor Number");
        assertTrue(bccpNode.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bccpNode);
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_2_7 (BCCP has its BDT changed)")
    public void test_TA_2_7() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        // Identifier. Type
        String identifierTypeGuid = "bea4dcd433d54aa698db2176cab33c19";
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(identifierTypeGuid, release.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(identifierTypeGuid, nextRelease.getReleaseNumber());
        BCCPObject revisedBccp = coreComponentAPI.createRevisedBCCP(bccp, dataType, developer, nextRelease, "Published");

        // Control Component_ Code. Type
        dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("31e2c152a1c8458e8d0ecda03db61206", nextRelease.getReleaseNumber());
        coreComponentAPI.updateBasedDT(revisedBccp, dataType);

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        coreComponentAPI.appendBCC(acc, bccp, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm() + "/" +
                "Scheme Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm() + "/" +
                "Scheme Agency Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm() + "/" +
                "Scheme Version Identifier").isDisplayed());

        acc = coreComponentAPI.createRandomACC(developer, nextRelease, namespace, "Published");
        coreComponentAPI.appendBCC(acc, revisedBccp, "Published");
        asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + revisedBccp.getPropertyTerm() + "/" +
                "List Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + revisedBccp.getPropertyTerm() + "/" +
                "List Agency Identifier").isDisplayed());
        assertTrue(editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + revisedBccp.getPropertyTerm() + "/" +
                "List Version Identifier").isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_2_9 (ASCCP is deprecated)")
    public void test_TA_2_9() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        ACCObject roleOfAcc = coreComponentAPI.getACCByDENAndReleaseNum("Name Value Range. Details", release.getReleaseNumber());
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(roleOfAcc, developer, namespace, "Published");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        coreComponentAPI.appendASCC(acc, asccp, "Published");
        ASCCPObject topLevelAsccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), topLevelAsccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + topLevelAsccp.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        assertFalse(editBIEPage.isDeprecated(node));

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        roleOfAcc = coreComponentAPI.getACCByDENAndReleaseNum("Name Value Range. Details", nextRelease.getReleaseNumber());
        ASCCPObject revisedAsccp = coreComponentAPI.createRevisedASCCP(asccp, roleOfAcc, developer, nextRelease, "Published");
        revisedAsccp.setDeprecated(true);
        coreComponentAPI.updateASCCP(revisedAsccp);

        acc = coreComponentAPI.createRandomACC(developer, nextRelease, namespace, "Published");
        coreComponentAPI.appendASCC(acc, revisedAsccp, "Published");
        topLevelAsccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), topLevelAsccp, developer, "WIP");
        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        node = editBIEPageForRevised.getNodeByPath(
                "/" + topLevelAsccp.getPropertyTerm() + "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        assertTrue(editBIEPageForRevised.isDeprecated(node));
    }

    @Test
    @DisplayName("TC_5_5_TA_2_11 (BCCP has its default value revised from no default value to a default value)")
    public void test_TA_2_11() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        // Identifier. Type
        String identifierTypeGuid = "bea4dcd433d54aa698db2176cab33c19";
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(identifierTypeGuid, release.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
        coreComponentAPI.appendBCC(acc, bccp, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        assertEquals("None", getText(bbiePanel.getValueConstraintSelectField()));
        assertTrue(StringUtils.isEmpty(getText(bbiePanel.getValueConstraintFieldByValue("None"))));

        ReleaseObject nextRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
        DTObject nextDataType = coreComponentAPI.getBDTByGuidAndReleaseNum(identifierTypeGuid, nextRelease.getReleaseNumber());
        BCCPObject revisedBccp = coreComponentAPI.createRevisedBCCP(bccp, nextDataType, developer, nextRelease, "Published");
        revisedBccp.setDefaultValue("default_value_" + randomAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(revisedBccp);

        acc = coreComponentAPI.createRandomACC(developer, nextRelease, namespace, "Published");
        coreComponentAPI.appendBCC(acc, revisedBccp, "Published");
        asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");

        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        EditBIEPage editBIEPageForRevised = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        node = editBIEPageForRevised.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + revisedBccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        assertEquals("Default Value", getText(bbiePanel.getValueConstraintSelectField()));
        assertEquals(revisedBccp.getDefaultValue(), getText(bbiePanel.getValueConstraintFieldByValue("Default Value")));
    }

    @Test
    @DisplayName("TC_5_5_TA_3")
    public void developer_cannot_create_BIE_based_on_working_branch_ASCCP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
        WebElement branchSelectField = createBIEForSelectTopLevelConceptPage.getBranchSelectField();
        click(branchSelectField);

        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-option//*[contains(text(), \"10.8.4\")]")).isDisplayed());
        assertThrows(TimeoutException.class, () -> visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-option//*[contains(text(), \"Working\")]")));
    }

    @Test
    @DisplayName("TC_5_5_TA_4")
    public void developer_can_see_in_BIE_list_page_all_BIEs_owned_by_any_user() {
        List<AppUserObject> appUsers = new ArrayList<>();

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        appUsers.add(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        appUsers.add(endUser);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        appUsers.add(developerAdmin);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        appUsers.add(endUserAdmin);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());

        for (AppUserObject appUser : appUsers) {
            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
            for (String state : Arrays.asList("WIP", "QA", "Production")) {
                getAPIFactory().getBusinessInformationEntityAPI()
                        .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, appUser, state);
            }
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(release.getReleaseNumber());

        for (AppUserObject appUser : appUsers) {
            viewEditBIEPage.setOwner(appUser.getLoginId());
            for (String state : Arrays.asList("WIP", "QA", "Production")) {
                viewEditBIEPage.setState(state);
                viewEditBIEPage.hitSearchButton();

                WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
                WebElement td = viewEditBIEPage.getColumnByName(tr, "den");
                assertTrue(getText(td).contains(asccp.getPropertyTerm()));

                td = viewEditBIEPage.getColumnByName(tr, "owner");
                assertTrue(getText(td).contains(appUser.getLoginId()));

                // to uncheck the selected state
                viewEditBIEPage.setState(state);
            }
            // to uncheck the selected owner
            viewEditBIEPage.setOwner(appUser.getLoginId());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_5")
    public void developer_can_view_details_of_BIE_that_is_in_WIP_state_and_owned_by_him() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(release.getReleaseNumber(), getText(topLevelASBIEPPanel.getReleaseField()));
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        assertEquals(topLevelASBIEP.getVersion(), getText(topLevelASBIEPPanel.getVersionField()));
        assertEquals(topLevelASBIEP.getStatus(), getText(topLevelASBIEPPanel.getStatusField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_6")
    public void developer_cannot_view_details_of_BIE_that_is_in_WIP_state_and_owned_by_another_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<AppUserObject> otherUsers = new ArrayList<>();

        AppUserObject anotherUser;
        anotherUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        for (AppUserObject appUser : otherUsers) {
            click(homePage.getScoreLogo());

            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, appUser, "WIP");

            assertThrows(NoSuchElementException.class, () -> homePage.getBIEMenu().openViewEditBIESubMenu()
                    .openEditBIEPage(topLevelASBIEP));
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_7")
    public void developer_can_view_details_of_BIE_that_is_in_QA_state_and_owned_by_any_user_but_cannot_make_any_change() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<AppUserObject> otherUsers = new ArrayList<>();

        AppUserObject anotherUser;
        anotherUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        for (AppUserObject appUser : otherUsers) {
            click(homePage.getScoreLogo());

            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, appUser, "QA");

            EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

            EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
            assertDisabled(topLevelASBIEPPanel.getBusinessTermField());
            assertDisabled(topLevelASBIEPPanel.getRemarkField());
            assertDisabled(topLevelASBIEPPanel.getVersionField());
            assertDisabled(topLevelASBIEPPanel.getStatusField());
            assertDisabled(topLevelASBIEPPanel.getContextDefinitionField());

            WebElement applicationAreaNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area");
            assertTrue(applicationAreaNode.isDisplayed());
            EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(applicationAreaNode);
            assertDisabled(asbiePanel.getUsedCheckbox());
            assertDisabled(asbiePanel.getNillableCheckbox());
            assertDisabled(asbiePanel.getCardinalityMinField());
            assertDisabled(asbiePanel.getCardinalityMaxField());
            assertDisabled(asbiePanel.getRemarkField());
            assertDisabled(asbiePanel.getContextDefinitionField());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_8")
    public void developer_can_view_details_of_production_BIE_owned_by_any_user_but_cannot_make_any_change() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<AppUserObject> otherUsers = new ArrayList<>();

        AppUserObject anotherUser;
        anotherUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        for (AppUserObject appUser : otherUsers) {
            click(homePage.getScoreLogo());

            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, appUser, "Production");

            EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

            EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
            assertDisabled(topLevelASBIEPPanel.getBusinessTermField());
            assertDisabled(topLevelASBIEPPanel.getRemarkField());
            assertDisabled(topLevelASBIEPPanel.getVersionField());
            assertDisabled(topLevelASBIEPPanel.getStatusField());
            assertDisabled(topLevelASBIEPPanel.getContextDefinitionField());

            WebElement applicationAreaNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area");
            assertTrue(applicationAreaNode.isDisplayed());
            EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(applicationAreaNode);
            assertDisabled(asbiePanel.getUsedCheckbox());
            assertDisabled(asbiePanel.getNillableCheckbox());
            assertDisabled(asbiePanel.getCardinalityMinField());
            assertDisabled(asbiePanel.getCardinalityMaxField());
            assertDisabled(asbiePanel.getRemarkField());
            assertDisabled(asbiePanel.getContextDefinitionField());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_9")
    public void developer_can_change_state_of_own_BIE_after_all_changes_have_been_saved_from_WIP_to_QA() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String version = "version_" + randomAlphanumeric(5, 10);
        String status = "status_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        topLevelASBIEPPanel.setBusinessTerm(businessTerm);
        topLevelASBIEPPanel.setRemark(remark);
        topLevelASBIEPPanel.setVersion(version);
        topLevelASBIEPPanel.setStatus(status);
        topLevelASBIEPPanel.setContextDefinition(contextDefinition);

        editBIEPage.hitUpdateButton();

        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
        editBIEPage.moveToQA();

        // Refresh the page to make sure to check the final state
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("QA", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_10")
    public void developer_cannot_change_BIE_state_from_WIP_to_QA_if_he_has_already_made_some_changes() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String version = "version_" + randomAlphanumeric(5, 10);
        String status = "status_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        topLevelASBIEPPanel.setBusinessTerm(businessTerm);
        topLevelASBIEPPanel.setRemark(remark);
        topLevelASBIEPPanel.setVersion(version);
        topLevelASBIEPPanel.setStatus(status);
        topLevelASBIEPPanel.setContextDefinition(contextDefinition);

        assertDisabled(editBIEPage.getMoveToQAButton(false));
    }

    @Test
    @DisplayName("TC_5_5_TA_11")
    public void developer_can_change_the_state_of_own_BIE_from_QA_back_to_WIP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "QA");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("QA", getText(topLevelASBIEPPanel.getStateField()));
        editBIEPage.backToWIP();

        // Refresh the page to make sure to check the final state
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("WIP", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_12")
    public void developer_can_move_own_BIE_which_is_in_QA_state_to_Production_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "QA");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("QA", getText(topLevelASBIEPPanel.getStateField()));
        editBIEPage.moveToProduction();

        // Refresh the page to make sure to check the final state
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals("Production", getText(topLevelASBIEPPanel.getStateField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_13")
    public void developer_cannot_make_any_change_to_own_production_BIE() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        assertThrows(TimeoutException.class, () -> editBIEPage.getUpdateButton(false));
        assertThrows(TimeoutException.class, () -> editBIEPage.getBackToWIPButton(false));
        assertThrows(TimeoutException.class, () -> editBIEPage.getMoveToQAButton(false));
        assertThrows(TimeoutException.class, () -> editBIEPage.getMoveToProductionButton(false));

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertDisabled(topLevelASBIEPPanel.getBusinessTermField());
        assertDisabled(topLevelASBIEPPanel.getRemarkField());
        assertDisabled(topLevelASBIEPPanel.getVersionField());
        assertDisabled(topLevelASBIEPPanel.getStatusField());
        assertDisabled(topLevelASBIEPPanel.getContextDefinitionField());

        WebElement applicationAreaNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area");
        assertTrue(applicationAreaNode.isDisplayed());
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(applicationAreaNode);
        assertDisabled(asbiePanel.getUsedCheckbox());
        assertDisabled(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getCardinalityMinField());
        assertDisabled(asbiePanel.getCardinalityMaxField());
        assertDisabled(asbiePanel.getRemarkField());
        assertDisabled(asbiePanel.getContextDefinitionField());
    }

    @Test
    @DisplayName("TC_5_5_TA_14")
    public void developer_can_update_his_own_BIE_that_is_in_WIP_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Debit Transfer. Cancel Acknowledge Debit Transfer", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String version = "version_" + randomAlphanumeric(5, 10);
        String status = "status_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        topLevelASBIEPPanel.setBusinessTerm(businessTerm);
        topLevelASBIEPPanel.setRemark(remark);
        topLevelASBIEPPanel.setVersion(version);
        topLevelASBIEPPanel.setStatus(status);
        topLevelASBIEPPanel.setContextDefinition(contextDefinition);

        assertTrue(editBIEPage.getUpdateButton(true).isEnabled());
        editBIEPage.hitUpdateButton();
        assertDisabled(editBIEPage.getUpdateButton(false));
    }

    @Test
    @DisplayName("TC_5_5_TA_15")
    // Testing for the Production state would be tested by TC_5_5_TA_13
    public void developer_cannot_update_his_own_BIE_which_is_in_QA_or_production_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "QA");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        assertThrows(TimeoutException.class, () -> editBIEPage.getUpdateButton(false));
        assertTrue(editBIEPage.getBackToWIPButton(false).isEnabled());
        assertTrue(editBIEPage.getMoveToProductionButton(false).isEnabled());

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertDisabled(topLevelASBIEPPanel.getBusinessTermField());
        assertDisabled(topLevelASBIEPPanel.getRemarkField());
        assertDisabled(topLevelASBIEPPanel.getVersionField());
        assertDisabled(topLevelASBIEPPanel.getStatusField());
        assertDisabled(topLevelASBIEPPanel.getContextDefinitionField());
    }

    @Test
    @DisplayName("TC_5_5_TA_16")
    public void developer_cannot_discard_BIE_owned_by_another_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<AppUserObject> otherUsers = new ArrayList<>();

        AppUserObject anotherUser;
        anotherUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        otherUsers.add(anotherUser);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        for (AppUserObject appUser : otherUsers) {
            click(homePage.getScoreLogo());

            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, appUser, "WIP");

            ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
            viewEditBIEPage.setBranch(release.getReleaseNumber());
            viewEditBIEPage.setOwner(appUser.getLoginId());
            viewEditBIEPage.setDEN(asccp.getDen());
            viewEditBIEPage.hitSearchButton();

            invisibilityOfLoadingContainerElement(getDriver());
            waitFor(ofMillis(500L));

            WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getPropertyTerm());
            WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
            assertDisabled(td.findElement(By.tagName("input")));

            assertThrows(TimeoutException.class, () -> viewEditBIEPage.getDiscardButton(false));
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_17")
    public void developer_cannot_discard_a_Production_BIE_he_owns() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getPropertyTerm());
        WebElement td = viewEditBIEPage.getColumnByName(tr, "select");
        assertDisabled(td.findElement(By.tagName("input")));

        assertThrows(TimeoutException.class, () -> viewEditBIEPage.getDiscardButton(false));
    }

    @Test
    @DisplayName("TC_5_5_TA_18")
    public void developer_can_discard_own_BIE_that_is_in_WIP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.discard(topLevelASBIEP);

        click(homePage.getScoreLogo());
        ViewEditBIEPage viewEditBIEPageForCheck = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPageForCheck.setBranch(topLevelASBIEP.getReleaseNumber());
        viewEditBIEPageForCheck.setOwner(getAPIFactory().getAppUserAPI().getAppUserByID(topLevelASBIEP.getOwnerUserId()).getLoginId());
        viewEditBIEPageForCheck.setDEN(topLevelASBIEP.getPropertyTerm());
        viewEditBIEPageForCheck.hitSearchButton();

        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));

        assertThrows(TimeoutException.class, () -> viewEditBIEPageForCheck.getTableRecordByValue(topLevelASBIEP.getPropertyTerm()));
    }

    @Test
    @DisplayName("TC_5_5_TA_19_a")
    public void developer_can_update_BIE_for_root_BIE_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String version = "version_" + randomAlphanumeric(5, 10);
        String status = "status_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        topLevelASBIEPPanel.setBusinessTerm(businessTerm);
        topLevelASBIEPPanel.setRemark(remark);
        topLevelASBIEPPanel.setVersion(version);
        topLevelASBIEPPanel.setStatus(status);
        topLevelASBIEPPanel.setContextDefinition(contextDefinition);

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(businessTerm, getText(topLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(remark, getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals(version, getText(topLevelASBIEPPanel.getVersionField()));
        assertEquals(status, getText(topLevelASBIEPPanel.getStatusField()));
        assertEquals(contextDefinition, getText(topLevelASBIEPPanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_19_b")
    public void developer_can_update_BIE_for_ASBIE_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis";
        WebElement asbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);

        int cardinalityMin = nextInt(2, 5);
        int cardinalityMax = nextInt(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        asbiePanel.toggleUsed();
        asbiePanel.setCardinalityMin(cardinalityMin);
        asbiePanel.setCardinalityMax(cardinalityMax);
        asbiePanel.setRemark(remark);
        asbiePanel.setContextDefinition(contextDefinition);

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        asbieNode = editBIEPage.getNodeByPath(path);
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);

        assertChecked(asbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(cardinalityMin), getText(asbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(cardinalityMax), getText(asbiePanel.getCardinalityMaxField()));
        assertEquals(remark, getText(asbiePanel.getRemarkField()));
        assertEquals(contextDefinition, getText(asbiePanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_19_c (BBIE)")
    public void developer_can_update_BIE_for_BBIE_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Data Area/Item Certificate Of Analysis/Item Certificate Of Analysis Header/Description";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        int cardinalityMin = nextInt(2, 5);
        int cardinalityMax = nextInt(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String example = "example_" + randomAlphanumeric(5, 10);
        String fixedValue = "fixed_value_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(cardinalityMin);
        bbiePanel.setCardinalityMax(cardinalityMax);
        bbiePanel.setRemark(remark);
        bbiePanel.setExample(example);
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue(fixedValue);
        bbiePanel.setContextDefinition(contextDefinition);

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(cardinalityMin), getText(bbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(cardinalityMax), getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(remark, getText(bbiePanel.getRemarkField()));
        assertEquals(example, getText(bbiePanel.getExampleField()));
        assertEquals("Fixed Value", getText(bbiePanel.getValueConstraintSelectField()));
        assertEquals(fixedValue, getText(bbiePanel.getFixedValueField()));
        assertEquals(contextDefinition, getText(bbiePanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_19_c (BBIE_SC)")
    public void developer_can_update_BIE_for_BBIE_SC_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Agency Identifier";
        WebElement bbieScNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);

        int cardinalityMin = 1;
        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String example = "example_" + randomAlphanumeric(5, 10);
        String fixedValue = "fixed_value_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100);

        bbieScPanel.toggleUsed();
        bbieScPanel.setCardinalityMin(cardinalityMin);
        bbieScPanel.setBusinessTerm(businessTerm);
        bbieScPanel.setRemark(remark);
        bbieScPanel.setExample(example);
        bbieScPanel.setValueConstraint("Fixed Value");
        bbieScPanel.setFixedValue(fixedValue);
        bbieScPanel.setContextDefinition(contextDefinition);

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieScNode = editBIEPage.getNodeByPath(path);
        bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);

        assertChecked(bbieScPanel.getUsedCheckbox());
        assertEquals(Integer.toString(cardinalityMin), getText(bbieScPanel.getCardinalityMinField()));
        assertEquals("1", getText(bbieScPanel.getCardinalityMaxField()));
        assertEquals(businessTerm, getText(bbieScPanel.getBusinessTermField()));
        assertEquals(remark, getText(bbieScPanel.getRemarkField()));
        assertEquals(example, getText(bbieScPanel.getExampleField()));
        assertEquals("Fixed Value", getText(bbieScPanel.getValueConstraintSelectField()));
        assertEquals(fixedValue, getText(bbieScPanel.getFixedValueField()));
        assertEquals(contextDefinition, getText(bbieScPanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20 (BBIE)")
    public void test_TA_20_for_BBIE_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Cost Center Identifier";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        clear(bbiePanel.getCardinalityMinField());
        sendKeys(bbiePanel.getCardinalityMinField(), "");
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieErrorMessage != null);
        });

        String randStrForCardinalityMin = randomAlphanumeric(5, 10);
        clear(bbiePanel.getCardinalityMinField());
        sendKeys(bbiePanel.getCardinalityMinField(), randStrForCardinalityMin);
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieErrorMessage != null);
        });

        clear(bbiePanel.getCardinalityMaxField());
        sendKeys(bbiePanel.getCardinalityMaxField(), "");
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieErrorMessage != null);
        });

        String randStrForCardinalityMax = randomAlphanumeric(5, 10);
        clear(bbiePanel.getCardinalityMaxField());
        sendKeys(bbiePanel.getCardinalityMaxField(), randStrForCardinalityMax);
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieErrorMessage != null);
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_20 (BBIE_SC)")
    public void test_TA_20_for_BBIE_SC_node() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Cost Center Identifier/Type Code";
        WebElement bbieScNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);

        bbieScPanel.toggleUsed();

        sendKeys(bbieScPanel.getCardinalityMinField(), "");
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieScErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieScErrorMessage != null);
        });

        String randStrForCardinalityMin = randomAlphanumeric(5, 10);
        sendKeys(bbieScPanel.getCardinalityMinField(), randStrForCardinalityMin);
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieScErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieScErrorMessage != null);
        });

        sendKeys(bbieScPanel.getCardinalityMaxField(), "");
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieScErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieScErrorMessage != null);
        });

        String randStrForCardinalityMax = randomAlphanumeric(5, 10);
        sendKeys(bbieScPanel.getCardinalityMaxField(), randStrForCardinalityMax);
        waitFor(ofMillis(1000L)); // wait for popping the error message up
        retry(() -> {
            String bbieScErrorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-error")));
            assertTrue(bbieScErrorMessage != null);
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_20_1")
    public void when_the_original_min_cardinality_is_0_the_developer_can_specify_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Cost Center Identifier";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        bbiePanel.setCardinalityMin(2);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-error[contains(text(), \"Cardinality Min must be less than or equals to 1\")]")).isDisplayed());
        assertFalse(editBIEPage.getUpdateButton(false).isEnabled());

        bbiePanel.setCardinalityMin(1);
        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(1), getText(bbiePanel.getCardinalityMinField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20_2")
    public void when_the_original_min_cardinality_is_0_and_max_cardinality_is_unbounded_the_developer_can_change_the_min_to_any_integer_more_than_zero() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Description";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        int randCardinalityMin = nextInt(5, 100);
        bbiePanel.setCardinalityMin(randCardinalityMin);
        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(randCardinalityMin), getText(bbiePanel.getCardinalityMinField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20_3")
    public void when_the_original_min_cardinality_is_0_and_max_cardinality_is_1_the_developer_can_change_the_max_to_0() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Profit Center Identifier";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(1), getText(bbiePanel.getCardinalityMaxField()));

        bbiePanel.setCardinalityMax(0);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-hint[contains(text(), \"Context Definition should define why Cardinality Max is zero (0)\")]")).isDisplayed());
        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMaxField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20_4")
    public void when_the_original_min_cardinality_is_1_the_min_or_the_max_cannot_be_changed_to_zero() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Item Certificate Of Analysis. Get Item Certificate Of Analysis", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Data Area/Get/Expression";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(1), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        bbiePanel.setCardinalityMin(0);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-error[contains(text(), \"Cardinality Min must be greater than or equals to 1\")]")).isDisplayed());

        bbiePanel.setCardinalityMax(0);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-hint[contains(text(), \"Context Definition should define why Cardinality Max is zero (0)\")]")).isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_20_5")
    public void min_cardinality_cannot_be_changed_to_negative_number() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Profit Center Identifier";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        int randCardinalityMin = -nextInt(1, 100);
        bbiePanel.setCardinalityMin(randCardinalityMin);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-error[contains(text(), \"Cardinality Min must be greater than or equals to 0\")]")).isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_20_6")
    public void when_the_original_min_cardinality_is_0_and_max_cardinality_is_unbounded_the_developer_can_change_max_to_any_number_between_0_and_inf() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        int randCardinalityMax = nextInt(10, 100);
        bbiePanel.setCardinalityMax(randCardinalityMax);
        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(randCardinalityMax), getText(bbiePanel.getCardinalityMaxField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20_7")
    public void developer_can_never_change_the_min_to_be_more_than_the_max() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        int randCardinalityMax = nextInt(10, 100);
        bbiePanel.setCardinalityMax(randCardinalityMax);

        int randCardinalityMin = nextInt(randCardinalityMax + 1, randCardinalityMax + 100);
        bbiePanel.setCardinalityMin(randCardinalityMin);
        assertTrue(visibilityOfElementLocated(getDriver(), By.xpath(
                "//mat-error[contains(text(), \"Cardinality Min must be less than or equals to " + randCardinalityMax + "\")]")).isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_20_8")
    public void developer_can_set_the_max_cardinality_to_negative_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        int randCardinalityMax = nextInt(10, 100);
        bbiePanel.setCardinalityMax(randCardinalityMax);
        bbiePanel.setCardinalityMax(-1);

        // The text '-1' for the cardinality max must be replaced with the text 'unbounded' by the system.
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_20_9")
    public void developer_can_set_the_max_cardinality_to_unbounded() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();

        assertEquals(Integer.toString(0), getText(bbiePanel.getCardinalityMinField()));
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));

        sendKeys(bbiePanel.getCardinalityMaxField(), "unbounded");

        editBIEPage.hitUpdateButton();

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath(path);
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_21_1")
    public void developer_cannot_update_BIE_by_specifying_value_domain_type_field_but_not_its_corresponding_field() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Acknowledge Maintenance Order. Cancel Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String nodeName = "Correlation Identifier";
        String path = "/" + asccp.getPropertyTerm() + "/Application Area/" + nodeName;
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        assertThrows(AssertionError.class, () -> editBIEPage.hitUpdateButton());
        assertEquals("Value Domain is required in " + nodeName, getSnackBarMessage(getDriver()));

        editBIEPage.openPage();

        nodeName = "Scheme Agency Identifier";
        path = "/" + asccp.getPropertyTerm() + "/Application Area/Correlation Identifier/" + nodeName;
        WebElement bbieScNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);

        bbieScPanel.toggleUsed();
        bbieScPanel.setValueDomainRestriction("Code");
        assertThrows(AssertionError.class, () -> editBIEPage.hitUpdateButton());
        assertEquals("Value Domain is required in " + nodeName, getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_5_5_TA_21_2")
    public void developer_cannot_update_BIE_using_an_incompatible_primitive_with_the_based_CC() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Process Maintenance Order. Process Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String nodeName = "Creation Date Time";
        String path = "/" + asccp.getPropertyTerm() + "/Application Area/" + nodeName;
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForDateTime = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForDateTime.toggleUsed();
        bbiePanelForDateTime.setValueDomain("date");
        bbiePanelForDateTime.setValueDomain("date time");
        bbiePanelForDateTime.setValueDomain("token");
        assertThrows(WebDriverException.class, () -> bbiePanelForDateTime.setValueDomain("string"));

        editBIEPage.openPage();
        nodeName = "Correlation Identifier";
        path = "/" + asccp.getPropertyTerm() + "/Application Area/" + nodeName;
        bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForString = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForString.toggleUsed();
        bbiePanelForString.setValueDomain("string");
        bbiePanelForString.setValueDomain("token");
        bbiePanelForString.setValueDomain("normalized string");
        assertThrows(WebDriverException.class, () -> bbiePanelForString.setValueDomain("date time"));

        editBIEPage.openPage();
        nodeName = "Scheme Agency Identifier";
        path = "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/" + nodeName;
        WebElement bbieScNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIESCPanel bbieScPanelForString = editBIEPage.getBBIESCPanel(bbieScNode);

        bbieScPanelForString.toggleUsed();
        bbieScPanelForString.setValueDomain("string");
        bbieScPanelForString.setValueDomain("token");
        bbieScPanelForString.setValueDomain("normalized string");
        assertThrows(WebDriverException.class, () -> bbieScPanelForString.setValueDomain("date time"));
    }

    @Test
    @DisplayName("TC_5_5_TA_21_3")
    public void developer_cannot_update_BIE_using_an_incompatible_code_list_with_the_based_CC() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Cancel Maintenance Order. Cancel Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String nodeName = "Version Identifier";
        String path = "/" + asccp.getPropertyTerm() + "/" + nodeName;
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForIdentifier = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForIdentifier.toggleUsed();
        bbiePanelForIdentifier.setValueDomainRestriction("Code");
        bbiePanelForIdentifier.setValueDomain("clm6DateFormatCode1_DateFormatCode");
        bbiePanelForIdentifier.setValueDomain("oacl_ControlCode");
        bbiePanelForIdentifier.setValueDomain("oacl_CurrencyCode");

        nodeName = "System Environment Code";
        path = "/" + asccp.getPropertyTerm() + "/" + nodeName;
        bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForCode = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForCode.toggleUsed();
        bbiePanelForCode.setValueDomainRestriction("Code");
        bbiePanelForCode.setValueDomain("oacl_SystemEnvironmentCode");
        assertThrows(WebDriverException.class, () -> bbiePanelForCode.setValueDomain("oacl_CurrencyCode"));
    }

    @Test
    @DisplayName("TC_5_5_TA_22")
    public void code_list_which_is_extension_of_default_code_list_used_by_BBIE_must_show_up_for_the_code_list_selection() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(developer, namespace, latestRelease, "Published");

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Maintenance Order. Sync Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String nodeName = "Version Identifier";
        String path = "/" + asccp.getPropertyTerm() + "/" + nodeName;
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForIdentifier = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForIdentifier.toggleUsed();
        bbiePanelForIdentifier.setValueDomainRestriction("Code");
        bbiePanelForIdentifier.setValueDomain("oacl_ResourceTypeCode");
        bbiePanelForIdentifier.setValueDomain("oacl_CharacterSetCode");
        bbiePanelForIdentifier.setValueDomain("clmIANACharacterSetCode20131220_CharacterSetCode");
        bbiePanelForIdentifier.setValueDomain("clm6Recommendation205_MeasurementUnitCommonCode");
        bbiePanelForIdentifier.setValueDomain(randomCodeList.getName());
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();

        bbieNode = editBIEPage.getNodeByPath(path);
        assertEquals(randomCodeList.getName() + " (" + randomCodeList.getVersionId() + ")",
                getText(editBIEPage.getBBIEPanel(bbieNode).getValueDomainField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_22_1")
    public void only_published_compatible_code_lists_in_the_same_release_as_the_BIE_shall_be_included() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        List<CodeListObject> randomCodeLists = new ArrayList<>();
        randomCodeLists.add(
                getAPIFactory().getCodeListAPI().createRandomCodeList(developer, namespace, latestRelease, "WIP"));
        randomCodeLists.add(
                getAPIFactory().getCodeListAPI().createRandomCodeList(developer, namespace, latestRelease, "Draft"));
        randomCodeLists.add(
                getAPIFactory().getCodeListAPI().createRandomCodeList(developer, namespace, latestRelease, "Candidate"));

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Notify Maintenance Order. Notify Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        String nodeName = "Version Identifier";
        String path = "/" + asccp.getPropertyTerm() + "/" + nodeName;
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForIdentifier = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanelForIdentifier.toggleUsed();
        bbiePanelForIdentifier.setValueDomainRestriction("Code");
        for (CodeListObject codeList : randomCodeLists) {
            assertThrows(WebDriverException.class, () -> bbiePanelForIdentifier.setValueDomain(codeList.getName()));
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_23")
    public void developer_cannot_update_BIE_by_changing_the_fields_of_node_that_is_not_used() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Show Maintenance Order. Show Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement asbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area");
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);

        assertDisabled(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getCardinalityMinField());
        assertDisabled(asbiePanel.getCardinalityMaxField());
        assertDisabled(asbiePanel.getRemarkField());
        assertDisabled(asbiePanel.getContextDefinitionField());

        WebElement bbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Version Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueConstraintFieldByValue("None"));
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());

        WebElement bbieScNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Version Identifier");
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);

        assertDisabled(bbieScPanel.getCardinalityMinField());
        assertDisabled(bbieScPanel.getCardinalityMaxField());
        assertDisabled(bbieScPanel.getRemarkField());
        assertDisabled(bbieScPanel.getExampleField());
        assertDisabled(bbieScPanel.getValueConstraintSelectField());
        assertDisabled(bbieScPanel.getValueConstraintFieldByValue("None"));
        assertDisabled(bbieScPanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbieScPanel.getValueDomainField());
        assertDisabled(bbieScPanel.getContextDefinitionField());
    }

    @Test
    @DisplayName("TC_5_5_TA_24")
    public void developer_can_hide_and_unhide_unused_nodes_in_the_BIE_tree() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Maintenance Order. Change Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP);

        WebElement asbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area");
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();

        WebElement bbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Version Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();

        WebElement bbieScNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Version Identifier");
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bbieScNode);
        bbieScPanel.toggleUsed();

        editBIEPage.hitUpdateButton();
        editBIEPage.openPage();

        editBIEPage.toggleHideUnused();

        editBIEPage.expandTree(asccp.getPropertyTerm());
        editBIEPage.expandTree("Application Area");
        editBIEPage.expandTree("Scenario Identifier");

        assertThrows(TimeoutException.class, () ->
                editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Language Code"));
        assertThrows(TimeoutException.class, () ->
                editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Sender"));
        assertThrows(TimeoutException.class, () ->
                editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Identifier"));
    }

    @Test
    @DisplayName("TC_5_5_TA_25")
    public void developer_cannot_copy_BIE_created_by_another_user_and_which_is_in_the_WIP_state() {
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get Maintenance Order. Get Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "WIP");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
        copyBIEForSelectBIEPage.setBranch(releaseNumber);
        copyBIEForSelectBIEPage.setDEN(asccp.getDen());
        copyBIEForSelectBIEPage.hitSearchButton();

        assertThrows(TimeoutException.class, () -> copyBIEForSelectBIEPage.getTableRecordByValue(asccp.getDen()));
    }

    @Test
    @DisplayName("TC_5_5_TA_26")
    public void developer_can_copy_a_BIE_that_he_owns_and_which_is_in_WIP_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        BusinessContextObject randomBusinessContextForCopyBIE =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContextForCopyBIE));

        ViewEditBIEPage viewEditBIEPage = copyBIEForSelectBIEPage.copyBIE(asccp.getDen(), releaseNumber);
        waitFor(ofMillis(2000L)); // wait for 2 secs.
        viewEditBIEPage.setBusinessContext(randomBusinessContextForCopyBIE.getName());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement tdForBusinessContexts = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContextForCopyBIE.getName(), getText(tdForBusinessContexts));
        WebElement tdForState = viewEditBIEPage.getColumnByName(tr, "state");
        assertEquals("WIP", getText(tdForState));
    }

    @Test
    @DisplayName("TC_5_5_TA_27 (QA state - developer)")
    public void developer_can_copy_IE_which_is_in_QA_state_and_owned_by_another_user() {
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Maintenance Order. Sync Response Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "QA");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContextForCopyBIE =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContextForCopyBIE));

        ViewEditBIEPage viewEditBIEPage = copyBIEForSelectBIEPage.copyBIE(asccp.getDen(), releaseNumber);
        waitFor(ofMillis(2000L)); // wait for 2 secs.
        viewEditBIEPage.setBusinessContext(randomBusinessContextForCopyBIE.getName());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement tdForBusinessContexts = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContextForCopyBIE.getName(), getText(tdForBusinessContexts));
        WebElement tdForState = viewEditBIEPage.getColumnByName(tr, "state");
        assertEquals("WIP", getText(tdForState));
    }

    @Test
    @DisplayName("TC_5_5_TA_27 (Production state - developer)")
    public void developer_can_copy_IE_which_is_in_Production_state_and_owned_by_another_user() {
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "Production");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContextForCopyBIE =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContextForCopyBIE));

        ViewEditBIEPage viewEditBIEPage = copyBIEForSelectBIEPage.copyBIE(asccp.getDen(), releaseNumber);
        waitFor(ofMillis(2000L)); // wait for 2 secs.
        viewEditBIEPage.setBusinessContext(randomBusinessContextForCopyBIE.getName());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement tdForBusinessContexts = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContextForCopyBIE.getName(), getText(tdForBusinessContexts));
        WebElement tdForState = viewEditBIEPage.getColumnByName(tr, "state");
        assertEquals("WIP", getText(tdForState));
    }

    @Test
    @DisplayName("TC_5_5_TA_27 (QA state - end-user with Extension)")
    public void developer_can_copy_IE_which_is_in_QA_state_and_owned_by_end_user() {
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String releaseNumber = "10.8.3";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(anotherDeveloper, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, anotherDeveloper, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, anotherDeveloper, namespace, "Published");
        }

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

        SelectAssociationDialog selectAssociationDialog =
                ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectAssociationDialog.selectAssociation("Validation Indicator. Indicator");

        topLevelAsbiep.setState("QA");
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelAsbiep);
        homePage.logout();
        // end of the pre-condition

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContextForCopyBIE =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContextForCopyBIE));

        viewEditBIEPage = copyBIEForSelectBIEPage.copyBIE(asccp.getDen(), releaseNumber);
        waitFor(ofMillis(2000L)); // wait for 2 secs.
        viewEditBIEPage.setBusinessContext(randomBusinessContextForCopyBIE.getName());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement tdForBusinessContexts = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContextForCopyBIE.getName(), getText(tdForBusinessContexts));
        WebElement tdForState = viewEditBIEPage.getColumnByName(tr, "state");
        assertEquals("WIP", getText(tdForState));

        EditBIEPage editBIEPageForExtension = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPageForExtension.expandTree(asccp.getPropertyTerm());
        editBIEPageForExtension.expandTree("Extension");
        assertThrows(TimeoutException.class, () ->
                editBIEPageForExtension.getNodeByPath("/" + asccp.getPropertyTerm() + "/Extension/Validation Indicator"));
    }

    @Test
    @DisplayName("TC_5_5_TA_27 (Production state - end-user with Extension)")
    public void developer_can_copy_IE_which_is_in_Production_state_and_owned_by_end_user() {
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String releaseNumber = "10.8.3";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(anotherDeveloper, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, anotherDeveloper, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, anotherDeveloper, namespace, "Published");
        }

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

        SelectAssociationDialog selectAssociationDialog =
                ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectAssociationDialog.selectAssociation("Validation Indicator. Indicator");

        topLevelAsbiep.setState("Production");
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelAsbiep);
        homePage.logout();
        // end of the pre-condition

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContextForCopyBIE =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContextForCopyBIE));

        viewEditBIEPage = copyBIEForSelectBIEPage.copyBIE(asccp.getDen(), releaseNumber);
        waitFor(ofMillis(2000L)); // wait for 2 secs.
        viewEditBIEPage.setBusinessContext(randomBusinessContextForCopyBIE.getName());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement tdForBusinessContexts = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContextForCopyBIE.getName(), getText(tdForBusinessContexts));
        WebElement tdForState = viewEditBIEPage.getColumnByName(tr, "state");
        assertEquals("WIP", getText(tdForState));

        EditBIEPage editBIEPageForExtension = viewEditBIEPage.openEditBIEPage(tr);
        editBIEPageForExtension.expandTree(asccp.getPropertyTerm());
        editBIEPageForExtension.expandTree("Extension");
        assertThrows(TimeoutException.class, () ->
                editBIEPageForExtension.getNodeByPath("/" + asccp.getPropertyTerm() + "/Extension/Validation Indicator"));
    }

    @Test
    @DisplayName("TC_5_5_TA_28")
    public void developer_can_transfer_ownership_of_BIE_only_to_another_developer() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        // end of the pre-condition

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();
        invisibilityOfLoadingContainerElement(getDriver());

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement td = viewEditBIEPage.getColumnByName(tr, "transferOwnership");
        assertTrue(td.findElement(By.tagName("mat-icon")).isEnabled());

        TransferBIEOwnershipDialog transferBIEOwnershipDialog =
                viewEditBIEPage.openTransferBIEOwnershipDialog(tr);
        transferBIEOwnershipDialog.transfer(anotherDeveloper.getLoginId());

        homePage.logout();

        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setOwner(anotherDeveloper.getLoginId());
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.hitSearchButton();

        tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        td = viewEditBIEPage.getColumnByName(tr, "transferOwnership");
        assertTrue(td.findElement(By.tagName("mat-icon")).isEnabled());

        td = viewEditBIEPage.getColumnByName(tr, "businessContexts");
        assertEquals(randomBusinessContext.getName(), getText(td));
    }

    @Test
    @DisplayName("TC_5_5_TA_29 (QA)")
    public void developer_cannot_transfer_ownership_of_the_BIE_in_QA_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Maintenance Order. Sync Response Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "QA");
        // end of the pre-condition

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();
        invisibilityOfLoadingContainerElement(getDriver());

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement td = viewEditBIEPage.getColumnByName(tr, "transferOwnership");
        assertThrows(NoSuchElementException.class, () -> td.findElement(By.tagName("button")));
    }

    @Test
    @DisplayName("TC_5_5_TA_29 (Production)")
    public void developer_cannot_transfer_ownership_of_the_BIE_in_Production_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNumber = "10.8.5";
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Maintenance Order. Sync Response Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "Production");
        // end of the pre-condition

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();
        invisibilityOfLoadingContainerElement(getDriver());

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        WebElement td = viewEditBIEPage.getColumnByName(tr, "transferOwnership");
        assertThrows(NoSuchElementException.class, () -> td.findElement(By.tagName("button")));
    }

    @Test
    @DisplayName("TC_5_5_TA_30 (Create BIE Business Contexts - Updater/Name field)")
    public void test_search_feature_using_updater_name_field_in_create_BIE_business_contexts() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<BusinessContextObject> businessContexts = IntStream.range(0, 5)
                .mapToObj(idx -> getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer))
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();

        // Test 'Updater' field
        createBIEForSelectBusinessContextsPage.setUpdater(developer.getLoginId());
        for (BusinessContextObject businessContext : businessContexts) {
            createBIEForSelectBusinessContextsPage.setName(businessContext.getName());
            createBIEForSelectBusinessContextsPage.hitSearchButton();
            assertBusinessContextNameInTheSearchResultsAtFirst(
                    createBIEForSelectBusinessContextsPage, businessContext.getName());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_30 (Create BIE Business Contexts - Update Start/End Date fields)")
    public void test_search_feature_using_date_fields_in_create_BIE_business_contexts() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<BusinessContextObject> businessContexts = IntStream.range(0, 5)
                .mapToObj(idx -> {
                    BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(developer);
                    randomBusinessContext.setCreationTimestamp(LocalDateTime.of(
                            2000 + (idx * 2),
                            RandomUtils.nextInt(1, 13),
                            RandomUtils.nextInt(1, 29),
                            RandomUtils.nextInt(0, 24),
                            RandomUtils.nextInt(0, 60),
                            RandomUtils.nextInt(0, 60)
                    ));
                    randomBusinessContext.setLastUpdateTimestamp(LocalDateTime.of(
                            2000 + ((idx * 2) + 1),
                            RandomUtils.nextInt(1, 13),
                            RandomUtils.nextInt(1, 29),
                            RandomUtils.nextInt(0, 24),
                            RandomUtils.nextInt(0, 60),
                            RandomUtils.nextInt(0, 60)
                    ));
                    return getAPIFactory().getBusinessContextAPI()
                            .createBusinessContext(randomBusinessContext);
                })
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        // Test 'Update Start Date'/'Update End Date' field
        for (BusinessContextObject businessContext : businessContexts) {
            homePage.openPage();
            CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                    homePage.getBIEMenu().openCreateBIESubMenu();
            createBIEForSelectBusinessContextsPage.setUpdater(developer.getLoginId());
            createBIEForSelectBusinessContextsPage.setUpdatedStartDate(businessContext.getCreationTimestamp());
            createBIEForSelectBusinessContextsPage.setUpdatedEndDate(businessContext.getLastUpdateTimestamp());
            createBIEForSelectBusinessContextsPage.hitSearchButton();
            assertBusinessContextNameInTheSearchResultsAtFirst(
                    createBIEForSelectBusinessContextsPage, businessContext.getName());
        }
    }

    private void assertBusinessContextNameInTheSearchResultsAtFirst(
            CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage, String name) {
        retry(() -> {
            WebElement tr = createBIEForSelectBusinessContextsPage.getTableRecordAtIndex(1);
            WebElement td = createBIEForSelectBusinessContextsPage.getColumnByName(tr, "name");
            assertEquals(name, getText(td.findElement(By.cssSelector("a"))));
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_30 (Create BIE Top-Level Concept - DEN field)")
    public void test_search_feature_using_den_field_in_create_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        createBIEForSelectTopLevelConceptPage.setBranch("10.8.5");
        createBIEForSelectTopLevelConceptPage.setDEN("\"Cancel Test Results. Cancel Test Results\"");
        createBIEForSelectTopLevelConceptPage.hitSearchButton();

        assertAsccpDenInTheSearchResultsAtFirst(createBIEForSelectTopLevelConceptPage,
                "Cancel Test Results. Cancel Test Results");
    }

    @Test
    @DisplayName("TC_5_5_TA_30 (Create BIE Top-Level Concept - Definition field)")
    public void test_search_feature_using_definition_field_in_create_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        createBIEForSelectTopLevelConceptPage.setBranch("10.8.5");
        createBIEForSelectTopLevelConceptPage.setDefinition("test result cancel");
        createBIEForSelectTopLevelConceptPage.hitSearchButton();

        assertAsccpDenInTheSearchResultsAtFirst(createBIEForSelectTopLevelConceptPage,
                "Cancel Test Results. Cancel Test Results");
    }

    @Test
    @DisplayName("TC_5_5_TA_30 (Create BIE Top-Level Concept - Module field)")
    public void test_search_feature_using_module_field_in_create_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        createBIEForSelectTopLevelConceptPage.setBranch("10.8.5");
        createBIEForSelectTopLevelConceptPage.setModule("Model\\Platform\\2_7\\Nouns\\CodeList");
        createBIEForSelectTopLevelConceptPage.hitSearchButton();

        assertAsccpDenInTheSearchResultsAtFirst(createBIEForSelectTopLevelConceptPage,
                "Code List. Code List");
    }

    private void assertAsccpDenInTheSearchResultsAtFirst(
            CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage, String den) {
        retry(() -> {
            WebElement tr = createBIEForSelectTopLevelConceptPage.getTableRecordAtIndex(1);
            WebElement td = createBIEForSelectTopLevelConceptPage.getColumnByName(tr, "den");
            assertEquals(den, getText(td.findElement(By.cssSelector("a"))));
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_31 (View BIE - State field)")
    public void test_search_feature_using_state_field_in_BIE_list() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");
        TopLevelASBIEPObject topLevelASBIEP_QA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Accounting Period. Accounting Period", releaseNum),
                        developer, "QA");
        TopLevelASBIEPObject topLevelASBIEP_Production = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Work Time Period. Time Period", releaseNum),
                        developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setState("WIP");
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_WIP.getDen());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setState("QA");
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_QA.getDen());

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setState("Production");
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_Production.getDen());
    }

    @Test
    @DisplayName("TC_5_5_TA_31 (View BIE - Update Start/End Date fields)")
    public void test_search_feature_using_date_fields_in_BIE_list() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setUpdatedStartDate(topLevelASBIEP_WIP.getLastUpdateTimestamp());
        viewEditBIEPage.setUpdatedEndDate(topLevelASBIEP_WIP.getLastUpdateTimestamp());
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_WIP.getDen());
    }

    @Test
    @DisplayName("TC_5_5_TA_31 (View BIE - DEN field)")
    public void test_search_feature_using_den_field_in_BIE_list() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setDEN(topLevelASBIEP_WIP.getDen());
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_WIP.getDen());
    }

    @Test
    @DisplayName("TC_5_5_TA_31 (View BIE - Updater/Name field Business Context field)")
    public void test_search_feature_using_business_context_field_in_BIE_list() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setBusinessContext(businessContext.getName());
        viewEditBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, topLevelASBIEP_WIP.getDen());
    }

    private void assertBieDenInTheSearchResultsAtFirst(
            ViewEditBIEPage viewEditBIEPage, String den) {
        retry(() -> {
            WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
            WebElement td = viewEditBIEPage.getColumnByName(tr, "den");
            assertEquals(den, getText(td.findElement(By.cssSelector("a"))));
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_32 (Copy BIE Business Contexts - Updater/Name field)")
    public void test_search_feature_using_updater_name_field_in_copy_BIE_business_contexts() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<BusinessContextObject> businessContexts = IntStream.range(0, 5)
                .mapToObj(idx -> getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer))
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();

        // Test 'Updater' field
        copyBIEForSelectBusinessContextsPage.setUpdater(developer.getLoginId());
        for (BusinessContextObject businessContext : businessContexts) {
            copyBIEForSelectBusinessContextsPage.setName(businessContext.getName());
            copyBIEForSelectBusinessContextsPage.hitSearchButton();
            assertBusinessContextNameInTheSearchResultsAtFirst(
                    copyBIEForSelectBusinessContextsPage, businessContext.getName());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_32 (Copy BIE Business Contexts - Update Start/End Date fields)")
    public void test_search_feature_using_date_fields_in_copy_BIE_business_contexts() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<BusinessContextObject> businessContexts = IntStream.range(0, 5)
                .mapToObj(idx -> {
                    BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(developer);
                    randomBusinessContext.setCreationTimestamp(LocalDateTime.of(
                            2000 + (idx * 2),
                            RandomUtils.nextInt(1, 13),
                            RandomUtils.nextInt(1, 29),
                            RandomUtils.nextInt(0, 24),
                            RandomUtils.nextInt(0, 60),
                            RandomUtils.nextInt(0, 60)
                    ));
                    randomBusinessContext.setLastUpdateTimestamp(LocalDateTime.of(
                            2000 + ((idx * 2) + 1),
                            RandomUtils.nextInt(1, 13),
                            RandomUtils.nextInt(1, 29),
                            RandomUtils.nextInt(0, 24),
                            RandomUtils.nextInt(0, 60),
                            RandomUtils.nextInt(0, 60)
                    ));
                    return getAPIFactory().getBusinessContextAPI()
                            .createBusinessContext(randomBusinessContext);
                })
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        // Test 'Update Start Date'/'Update End Date' field
        for (BusinessContextObject businessContext : businessContexts) {
            homePage.openPage();
            CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                    homePage.getBIEMenu().openCopyBIESubMenu();
            copyBIEForSelectBusinessContextsPage.setUpdater(developer.getLoginId());
            copyBIEForSelectBusinessContextsPage.setUpdatedStartDate(businessContext.getCreationTimestamp());
            copyBIEForSelectBusinessContextsPage.setUpdatedEndDate(businessContext.getLastUpdateTimestamp());
            copyBIEForSelectBusinessContextsPage.hitSearchButton();
            assertBusinessContextNameInTheSearchResultsAtFirst(
                    copyBIEForSelectBusinessContextsPage, businessContext.getName());
        }
    }

    private void assertBusinessContextNameInTheSearchResultsAtFirst(
            CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage, String name) {
        retry(() -> {
            WebElement tr = copyBIEForSelectBusinessContextsPage.getTableRecordAtIndex(1);
            WebElement td = copyBIEForSelectBusinessContextsPage.getColumnByName(tr, "name");
            assertEquals(name, getText(td.findElement(By.cssSelector("a"))));
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_32 (Copy BIE Top-Level Concept - DEN field)")
    public void test_search_feature_using_den_field_in_copy_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        copyBIEForSelectBIEPage.setBranch("10.8.5");
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.setDEN(topLevelASBIEP_WIP.getDen());
        copyBIEForSelectBIEPage.hitSearchButton();

        assertBieDenInTheSearchResultsAtFirst(copyBIEForSelectBIEPage, topLevelASBIEP_WIP.getDen());
    }

    @Test
    @DisplayName("TC_5_5_TA_32 (Copy BIE Top-Level Concept - Business Context field)")
    public void test_search_feature_using_business_context_field_in_copy_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        copyBIEForSelectBIEPage.setBranch("10.8.5");
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.setBusinessContext(businessContext.getName());
        copyBIEForSelectBIEPage.hitSearchButton();

        assertBieDenInTheSearchResultsAtFirst(copyBIEForSelectBIEPage, topLevelASBIEP_WIP.getDen());
    }

    @Test
    @DisplayName("TC_5_5_TA_32 (Copy BIE Top-Level Concept - State field)")
    public void test_search_feature_using_state_field_in_copy_BIE_top_level_concept() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");
        TopLevelASBIEPObject topLevelASBIEP_QA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Accounting Period. Accounting Period", releaseNum),
                        developer, "QA");
        TopLevelASBIEPObject topLevelASBIEP_Production = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Work Time Period. Time Period", releaseNum),
                        developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        copyBIEForSelectBIEPage.setBranch("10.8.5");
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.setState("WIP");
        copyBIEForSelectBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(copyBIEForSelectBIEPage, topLevelASBIEP_WIP.getDen());

        copyBIEForSelectBIEPage.openPage();
        copyBIEForSelectBIEPage.setBranch("10.8.5");
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.setState("QA");
        copyBIEForSelectBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(copyBIEForSelectBIEPage, topLevelASBIEP_QA.getDen());

        copyBIEForSelectBIEPage.openPage();
        copyBIEForSelectBIEPage.setBranch("10.8.5");
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.setState("Production");
        copyBIEForSelectBIEPage.hitSearchButton();
        assertBieDenInTheSearchResultsAtFirst(copyBIEForSelectBIEPage, topLevelASBIEP_Production.getDen());
    }

    private void assertBieDenInTheSearchResultsAtFirst(
            CopyBIEForSelectBIEPage copyBIEForSelectBIEPage, String den) {
        retry(() -> {
            WebElement tr = copyBIEForSelectBIEPage.getTableRecordAtIndex(1);
            WebElement td = copyBIEForSelectBIEPage.getColumnByName(tr, "den");
            assertEquals(den, getText(td.findElement(By.cssSelector("a"))));
        });
    }

    @Test
    @DisplayName("TC_5_5_TA_33")
    public void issue_653() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Tool Actual. Tool Actual", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_WIP);
        WebElement identifier1st =
                editBIEPage.getNodeByPath("/" + topLevelASBIEP_WIP.getPropertyTerm() + "/Identifier");
        assertTrue(identifier1st.isDisplayed());
        WebElement identifierSet1st =
                editBIEPage.getNodeByPath("/" + topLevelASBIEP_WIP.getPropertyTerm() + "/Identifier Set");
        assertTrue(identifierSet1st.isDisplayed());
        //check that the second Identifier is not expanded automatically
        WebElement identifier2nd = visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[text() = \"Identifier\"]//ancestor::div[@data-level = \"2\"]//mat-icon[contains(text(), \"chevron_right\")]"
        ));
        assertTrue(identifier2nd.isDisplayed());
        WebElement typeCode =
                editBIEPage.getNodeByPath("/" + topLevelASBIEP_WIP.getPropertyTerm() + "/Identifier Set/Identifier/Type Code");
        assertTrue(typeCode.isDisplayed());
    }

    @Test
    @DisplayName("TC_5_5_TA_34")
    public void the_number_of_bies_in_copy_BIE_page_are_the_same_with_the_number_of_the_bies_displayed_on_the_right_bottom_index_of_the_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");
        TopLevelASBIEPObject topLevelASBIEP_QA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Accounting Period. Accounting Period", releaseNum),
                        developer, "QA");
        TopLevelASBIEPObject topLevelASBIEP_Production = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Work Time Period. Time Period", releaseNum),
                        developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.hitSearchButton();
        int totalNumberOfItemsInBIEs = viewEditBIEPage.getTotalNumberOfItems();

        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCopyBIESubMenu();
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(businessContext));
        copyBIEForSelectBIEPage.setBranch(releaseNum);
        copyBIEForSelectBIEPage.setOwner(developer.getLoginId());
        copyBIEForSelectBIEPage.hitSearchButton();
        int totalNumberOfItemsInCopyBIEs = copyBIEForSelectBIEPage.getTotalNumberOfItems();

        assertEquals(totalNumberOfItemsInBIEs, totalNumberOfItemsInCopyBIEs);
    }

    @Test
    @DisplayName("TC_5_5_TA_35")
    public void developer_can_assign_multiple_business_contexts_to_BIE_during_BIE_creation() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );

        String asccpDen = "Accounting Period. Accounting Period";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(businessContexts);
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpDen, releaseNum);

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(releaseNum);
        viewEditBIEPage.setOwner(developer.getLoginId());
        for (BusinessContextObject businessContext : businessContexts) {
            viewEditBIEPage.setBusinessContext(businessContext.getName());
            viewEditBIEPage.hitSearchButton();

            assertBieDenInTheSearchResultsAtFirst(viewEditBIEPage, asccpDen);
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_36")
    public void developer_can_assign_multiple_business_contexts_to_BIE_in_WIP_state_he_owns_via_updating_it() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );

        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_WIP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.addBusinessContext(businessContexts.get(1));
        topLevelASBIEPPanel.addBusinessContext(businessContexts.get(2));

        String businessContextTexts = topLevelASBIEPPanel.getBusinessContextList().stream()
                .map(e -> getText(e).replaceAll("cancel", "").trim())
                .collect(Collectors.joining(","));
        assertEquals(businessContexts.stream()
                        .map(BusinessContextObject::getName).collect(Collectors.joining(",")),
                businessContextTexts);

        // refresh the page to check the changes were updated.
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        businessContextTexts = topLevelASBIEPPanel.getBusinessContextList().stream()
                .map(e -> getText(e).replaceAll("cancel", "").trim())
                .collect(Collectors.joining(","));
        assertEquals(businessContexts.stream()
                        .map(BusinessContextObject::getName).collect(Collectors.joining(",")),
                businessContextTexts);
    }

    @Test
    @DisplayName("TC_5_5_TA_37")
    public void developer_cannot_assign_multiple_business_contexts_to_BIE_not_WIP_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );

        TopLevelASBIEPObject topLevelASBIEP_QA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "QA");

        TopLevelASBIEPObject topLevelASBIEP_Production = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Work Time Period. Time Period", releaseNum),
                        developer, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_QA);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel_QA = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel_QA.addBusinessContext(businessContexts.get(1)));

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_Production);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel_Production = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel_Production.addBusinessContext(businessContexts.get(1)));
    }

    @Test
    @DisplayName("TC_5_5_TA_38")
    public void developer_cannot_assign_multiple_business_contexts_to_BIE_in_not_WIP_state_and_he_does_not_own_via_updating_it() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject enduser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(enduser);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(enduser),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(enduser)
        );

        TopLevelASBIEPObject topLevelASBIEP_QA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        enduser, "QA");

        TopLevelASBIEPObject topLevelASBIEP_Production = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Work Time Period. Time Period", releaseNum),
                        enduser, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_QA);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel_QA = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel_QA.addBusinessContext(businessContexts.get(1)));

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_Production);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel_Production = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel_Production.addBusinessContext(businessContexts.get(1)));
    }

    @Test
    @DisplayName("TC_5_5_TA_39")
    public void developer_cannot_assign_the_same_business_context_more_than_one_times_in_BIE() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );

        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContexts.get(0)),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_WIP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel.addBusinessContext(businessContexts.get(0)));
    }

    @Test
    @DisplayName("TC_5_5_TA_40")
    public void developer_can_remove_an_assigned_business_context_from_BIE_in_WIP_state_he_owns() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        List<BusinessContextObject> businessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );

        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(businessContexts,
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_WIP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.removeBusinessContext(businessContexts.get(0));

        String businessContextTexts = topLevelASBIEPPanel.getBusinessContextList().stream()
                .map(e -> getText(e).replaceAll("cancel", "").trim())
                .collect(Collectors.joining(","));
        assertEquals(businessContexts.subList(1, businessContexts.size()).stream()
                        .map(BusinessContextObject::getName).collect(Collectors.joining(",")),
                businessContextTexts);

        topLevelASBIEPPanel.removeBusinessContext(businessContexts.get(1));
        businessContextTexts = topLevelASBIEPPanel.getBusinessContextList().stream()
                .map(e -> getText(e).replaceAll("cancel", "").trim())
                .collect(Collectors.joining(","));
        assertEquals(businessContexts.subList(2, businessContexts.size()).stream()
                        .map(BusinessContextObject::getName).collect(Collectors.joining(",")),
                businessContextTexts);

        // refresh the page to check the changes were updated.
        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        businessContextTexts = topLevelASBIEPPanel.getBusinessContextList().stream()
                .map(e -> getText(e).replaceAll("cancel", "").trim())
                .collect(Collectors.joining(","));
        assertEquals(businessContexts.subList(2, businessContexts.size()).stream()
                        .map(BusinessContextObject::getName).collect(Collectors.joining(",")),
                businessContextTexts);
    }

    @Test
    @DisplayName("TC_5_5_TA_41")
    public void developer_cannot_remove_all_business_contexts_from_BIE_there_must_be_at_least_one_business_context_assigned() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String releaseNum = "10.8.5";
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        TopLevelASBIEPObject topLevelASBIEP_WIP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum("Customer Price List Price. Price", releaseNum),
                        developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu()
                .openEditBIEPage(topLevelASBIEP_WIP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertThrows(TimeoutException.class, () -> topLevelASBIEPPanel.removeBusinessContext(businessContext));
    }

    @Test
    @DisplayName("TC_5_5_TA_42")
    public void example_input_text_field_should_exist_in_BBIEPs_and_BBIE_SC_where_example_of_data_of_node_can_be_inserted() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        WebElement bccpNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bccpNode);
        bbiePanel.toggleUsed();
        assertTrue(bbiePanel.getExampleField().isEnabled());
        bbiePanel.setExample("example_" + randomAlphanumeric(5, 10));
        editBIEPage.hitUpdateButton();

        WebElement bdtScNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Identifier");
        EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(bdtScNode);
        bbieScPanel.toggleUsed();
        assertTrue(bbieScPanel.getExampleField().isEnabled());
        bbieScPanel.setExample("example_" + randomAlphanumeric(5, 10));
        editBIEPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_5_5_TA_43")
    public void fixed_and_default_values_should_be_mutually_exclusive() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        // Testing for BBIE Node
        String bccpNodePath = "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier";
        WebElement bccpNode = editBIEPage.getNodeByPath(bccpNodePath);
        EditBIEPage.BBIEPanel bbiePanel_1 = editBIEPage.getBBIEPanel(bccpNode);
        bbiePanel_1.toggleUsed();
        String randomFixedValue = "fixed_value_" + randomAlphanumeric(5, 10);
        bbiePanel_1.setValueConstraint("Fixed Value");
        bbiePanel_1.setFixedValue(randomFixedValue);
        editBIEPage.hitUpdateButton();

        // refresh the page to check whether the BBIE has a valid fixed value or not.
        editBIEPage.openPage();
        bccpNode = editBIEPage.getNodeByPath(bccpNodePath);
        EditBIEPage.BBIEPanel bbiePanel_2 = editBIEPage.getBBIEPanel(bccpNode);
        assertEquals(randomFixedValue, getText(bbiePanel_2.getFixedValueField()));
        assertThrows(TimeoutException.class, () -> bbiePanel_2.getDefaultValueField());

        String randomDefaultValue = "default_value_" + randomAlphanumeric(5, 10);
        bbiePanel_2.setValueConstraint("Default Value");
        bbiePanel_2.setDefaultValue(randomDefaultValue);
        editBIEPage.hitUpdateButton();

        // refresh the page to check whether the BBIE has a valid default value or not.
        editBIEPage.openPage();
        bccpNode = editBIEPage.getNodeByPath(bccpNodePath);
        EditBIEPage.BBIEPanel bbiePanel_3 = editBIEPage.getBBIEPanel(bccpNode);
        assertEquals(randomDefaultValue, getText(bbiePanel_3.getDefaultValueField()));
        assertThrows(TimeoutException.class, () -> bbiePanel_3.getFixedValueField());

        // Testing for BBIE_SC Node
        String bdtScNodePath = "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier/Scheme Identifier";
        WebElement bdtScNode = editBIEPage.getNodeByPath(bdtScNodePath);
        EditBIEPage.BBIESCPanel bbieScPanel_1 = editBIEPage.getBBIESCPanel(bdtScNode);
        bbieScPanel_1.toggleUsed();
        randomFixedValue = "fixed_value_" + randomAlphanumeric(5, 10);
        bbieScPanel_1.setValueConstraint("Fixed Value");
        bbieScPanel_1.setFixedValue(randomFixedValue);
        editBIEPage.hitUpdateButton();

        // refresh the page to check whether the BBIE_SC has a valid fixed value or not.
        editBIEPage.openPage();
        bdtScNode = editBIEPage.getNodeByPath(bdtScNodePath);
        EditBIEPage.BBIESCPanel bbieScPanel_2 = editBIEPage.getBBIESCPanel(bdtScNode);
        assertEquals(randomFixedValue, getText(bbieScPanel_2.getFixedValueField()));
        assertThrows(TimeoutException.class, () -> bbieScPanel_2.getDefaultValueField());

        randomDefaultValue = "default_value_" + randomAlphanumeric(5, 10);
        bbieScPanel_2.setValueConstraint("Default Value");
        bbieScPanel_2.setDefaultValue(randomDefaultValue);
        editBIEPage.hitUpdateButton();

        // refresh the page to check whether the BBIE_SC has a valid default value or not.
        editBIEPage.openPage();
        bdtScNode = editBIEPage.getNodeByPath(bdtScNodePath);
        EditBIEPage.BBIESCPanel bbieScPanel_3 = editBIEPage.getBBIESCPanel(bdtScNode);
        assertEquals(randomDefaultValue, getText(bbieScPanel_3.getDefaultValueField()));
        assertThrows(TimeoutException.class, () -> bbieScPanel_3.getFixedValueField());
    }

    @Test
    @DisplayName("TC_5_5_TA_44")
    public void cardinality_min_is_greater_than_zero_in_bcc_or_ascc_they_must_be_enabled_and_they_cannot_be_disabled_unused() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Acknowledge Match Document. Change Acknowledge Match Document", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        WebElement applicationAreaNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Application Area");
        EditBIEPage.ASBIEPanel applicationAreaPanel = editBIEPage.getASBIEPanel(applicationAreaNode);
        applicationAreaPanel.toggleUsed();
        assertEquals("1", getText(applicationAreaPanel.getCardinalityMinField()));

        WebElement creationDateTimeNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Application Area/Creation Date Time"); // required BBIE field.
        EditBIEPage.BBIEPanel creationDateTimePanel = editBIEPage.getBBIEPanel(creationDateTimeNode);
        assertEnabled(creationDateTimePanel.getUsedCheckbox());
        assertChecked(creationDateTimePanel.getUsedCheckbox());

        WebElement dataAreaNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Data Area"); // required ASBIE field.
        EditBIEPage.ASBIEPanel dataAreaPanel = editBIEPage.getASBIEPanel(dataAreaNode);
        assertEnabled(dataAreaPanel.getUsedCheckbox());
        assertChecked(dataAreaPanel.getUsedCheckbox());
    }

    @Test
    @DisplayName("TC_5_5_TA_45")
    public void developer_can_select_BIE_from_BIE_List_page_navigate_thought_different_paginator_pages_while_the_forenamed_BIE_remains_checked() {
        List<String> asccpDens = Arrays.asList("Acknowledge Allocate Resource. Acknowledge Allocate Resource",
                "Acknowledge Batch Certificate Of Analysis. Acknowledge Batch Certificate Of Analysis",
                "Acknowledge BOM. Acknowledge BOM",
                "Acknowledge Carrier Route. Acknowledge Carrier Route",
                "Acknowledge Catalog. Acknowledge Catalog",
                "Acknowledge Chart Of Accounts. Acknowledge Chart Of Accounts",
                "Acknowledge Code List. Acknowledge Code List",
                "Acknowledge Commercial Invoice. Acknowledge Commercial Invoice",
                "Acknowledge Configuration. Acknowledge Configuration",
                "Acknowledge Confirm WIP. Acknowledge Confirm WIP",
                "Acknowledge Corrective Action Plan. Acknowledge Corrective Action Plan",
                "Acknowledge Corrective Action Request. Acknowledge Corrective Action Request",
                "Acknowledge Corrective Action. Acknowledge Corrective Action",
                "Acknowledge Credit Status. Acknowledge Credit Status",
                "Acknowledge Credit Transfer. Acknowledge Credit Transfer");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.1");
        List<TopLevelASBIEPObject> topLevelASBIEPs = asccpDens.stream().map(den -> {
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum(den, release.getReleaseNumber());
            return getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        }).collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.setBranch(release.getReleaseNumber());
        viewEditBIEPage.hitSearchButton();

        assertTrue(viewEditBIEPage.getTotalNumberOfItems() >= topLevelASBIEPs.size());
        WebElement firstRecord = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement firstRecordCheckbox = viewEditBIEPage.getColumnByName(firstRecord, "select");
        click(firstRecordCheckbox);

        click(viewEditBIEPage.getNextPageButton());
        invisibilityOfLoadingContainerElement(getDriver());

        WebElement firstRecordInSecondPage = viewEditBIEPage.getTableRecordAtIndex(1);
        WebElement firstRecordCheckboxInSecondPage = viewEditBIEPage.getColumnByName(firstRecordInSecondPage, "select");
        click(firstRecordCheckboxInSecondPage);

        click(viewEditBIEPage.getPreviousPageButton());
        invisibilityOfLoadingContainerElement(getDriver());

        firstRecord = viewEditBIEPage.getTableRecordAtIndex(1);
        firstRecordCheckbox = viewEditBIEPage.getColumnByName(firstRecord, "select");
        assertEnabled(firstRecordCheckbox);
    }

    @Test
    @DisplayName("TC_5_5_TA_46")
    public void developer_can_neither_create_local_or_global_extension_to_the_BIE() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Acknowledge Match Document. Change Acknowledge Match Document", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String extensionPath = "/" + asccp.getPropertyTerm() + "/Application Area/Extension";
        assertThrows(WebDriverException.class, () -> editBIEPage.extendBIELocallyOnNode(extensionPath));
        WebElement createABIEExtensionLocallyButton = visibilityOfElementLocated(getDriver(),
                By.xpath("//span[text() = \"Create ABIE Extension Locally\"]/ancestor::button"));
        assertFalse(createABIEExtensionLocallyButton.isEnabled());

        assertThrows(WebDriverException.class, () -> editBIEPage.extendBIEGloballyOnNode(extensionPath));
        WebElement createABIEExtensionGloballyButton = visibilityOfElementLocated(getDriver(),
                By.xpath("//span[text() = \"Create ABIE Extension Globally\"]/ancestor::button"));
        assertFalse(createABIEExtensionGloballyButton.isEnabled());
    }

    @Test
    @DisplayName("TC_5_5_TA_47")
    public void developer_cannot_create_BIE_without_business_context_assigned() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();

        assertThrows(TimeoutException.class, () -> createBIEForSelectBusinessContextsPage.next(Collections.emptyList()));
    }

    @Test
    @DisplayName("TC_5_5_TA_48")
    public void developer_can_set_the_Version_metadata_field_and_automatically_the_Fixed_Value_of_the_Version_Identifier_node_is_synchronized() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get BOM. Get BOM", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        String version = "version_" + randomAlphanumeric(5, 10);
        topLevelASBIEPPanel.setVersion(version);
        String versionIdentifierPath = "/" + asccp.getPropertyTerm() + "/Version Identifier";
        WebElement versionIdentifierNode = editBIEPage.getNodeByPath(versionIdentifierPath);
        EditBIEPage.BBIEPanel versionIdentifierPanel = editBIEPage.getBBIEPanel(versionIdentifierNode);
        versionIdentifierPanel.toggleUsed();

        assertEquals("Synchronized \'Version\' value with the the fixed value.", getSnackBarMessage(getDriver()));
        assertEquals("Fixed Value", getText(versionIdentifierPanel.getValueConstraintSelectField()));
        assertEquals(version, getText(versionIdentifierPanel.getFixedValueField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_49")
    public void developer_can_change_the_Fixed_Value_of_the_Version_Identifier_node_even_if_it_was_previously_synchronized() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Get BOM. Get BOM", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        String version = "version_" + randomAlphanumeric(5, 10);
        topLevelASBIEPPanel.setVersion(version);
        String versionIdentifierPath = "/" + asccp.getPropertyTerm() + "/Version Identifier";
        WebElement versionIdentifierNode = editBIEPage.getNodeByPath(versionIdentifierPath);
        EditBIEPage.BBIEPanel versionIdentifierPanel = editBIEPage.getBBIEPanel(versionIdentifierNode);
        versionIdentifierPanel.toggleUsed();

        assertEquals("Synchronized \'Version\' value with the the fixed value.", getSnackBarMessage(getDriver()));
        assertEquals("Fixed Value", getText(versionIdentifierPanel.getValueConstraintSelectField()));
        assertEquals(version, getText(versionIdentifierPanel.getFixedValueField()));
        waitFor(ofMillis(3000L)); // wait until the snack-bar disappears
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        versionIdentifierNode = editBIEPage.getNodeByPath(versionIdentifierPath);
        versionIdentifierPanel = editBIEPage.getBBIEPanel(versionIdentifierNode);
        String anotherVersion = "version_" + randomAlphanumeric(5, 10);
        versionIdentifierPanel.setFixedValue(anotherVersion);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(version, getText(topLevelASBIEPPanel.getVersionField()));
        assertNotEquals(anotherVersion, getText(topLevelASBIEPPanel.getVersionField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_50")
    public void default_value_of_the_primitive_date_time_BCCPs_should_be_date_time() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Sync Response Table. Sync Response Table", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        WebElement creationDateTimeNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Application Area/Creation Date Time");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel creationDateTimePanel = editBIEPage.getBBIEPanel(creationDateTimeNode);
        assertEquals("Primitive", getText(creationDateTimePanel.getValueDomainRestrictionSelectField()));
        assertEquals("date time", getText(creationDateTimePanel.getValueDomainField()));

        WebElement lastModificationDateTimeNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Data Area/Table/Last Modification Date Time");
        EditBIEPage.BBIEPanel lastModificationDateTimePanel = editBIEPage.getBBIEPanel(lastModificationDateTimeNode);
        assertEquals("Primitive", getText(lastModificationDateTimePanel.getValueDomainRestrictionSelectField()));
        assertEquals("date time", getText(lastModificationDateTimePanel.getValueDomainField()));
    }

    @Test
    @DisplayName("TC_5_5_TA_51")
    public void developer_cannot_create_new_BIE_from_ASCCP_whose_ACC_has_group_component_type() {
        String groupAsccpDen = "Entity Identifiers Group. Entity Identifiers Group";

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage =
                viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(groupAsccpDen, release.getReleaseNumber());
        WebElement node = asccpViewEditPage.getNodeByPath("/Entity Identifiers Group/Entity Identifiers Group. Details");
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(node);
        assertEquals("Semantic Group", getText(accPanel.getComponentTypeSelectField()));

        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                homePage.getBIEMenu().openCreateBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
        assertThrows(NoSuchElementException.class, () ->
                createBIEForSelectTopLevelConceptPage.createBIE(groupAsccpDen, release.getReleaseNumber()));
    }

    @Test
    @DisplayName("TC_5_5_TA_52 (Enable Children - ASBIE)")
    public void test_TA_52_asbie() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = "/" + asccp.getPropertyTerm() + "/Application Area";
        WebElement applicationArea = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel applicationAreaPanel = editBIEPage.getASBIEPanel(applicationArea);
        applicationAreaPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        editBIEPage.enableChildren(path);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        for (String asbieChild : Arrays.asList("Sender", "Intermediary", "Receiver", "Signature", "Extension")) {
            WebElement childPanel = editBIEPage.getNodeByPath(path + "/" + asbieChild);
            EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(childPanel);
            assertEnabled(asbiePanel.getUsedCheckbox());
        }
        for (String bbieChild : Arrays.asList("Creation Date Time", "Scenario Identifier", "Correlation Identifier", "BOD Identifier")) {
            WebElement childPanel = editBIEPage.getNodeByPath(path + "/" + bbieChild);
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(childPanel);
            assertEnabled(bbiePanel.getUsedCheckbox());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_52 (Enable Children - BBIE)")
    public void test_TA_52_bbie() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = "/" + asccp.getPropertyTerm() + "/Application Area/Sender/Component Identifier";
        WebElement componentIdentifier = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel componentIdentifierPanel = editBIEPage.getBBIEPanel(componentIdentifier);
        componentIdentifierPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        editBIEPage.enableChildren(path);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        for (String child : Arrays.asList("Scheme Agency Identifier", "Scheme Identifier", "Scheme Version Identifier")) {
            WebElement childPanel = editBIEPage.getNodeByPath(path + "/" + child);
            EditBIEPage.BBIESCPanel bbieScPanel = editBIEPage.getBBIESCPanel(childPanel);
            assertEnabled(bbieScPanel.getUsedCheckbox());
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_53")
    public void test_TA_53() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = "/" + asccp.getPropertyTerm() + "/Application Area";
        WebElement applicationArea = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel applicationAreaPanel = editBIEPage.getASBIEPanel(applicationArea);
        applicationAreaPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        editBIEPage.enableChildren(path);
        editBIEPage.setChildrenMaxCardinalityToOne(path);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage();
        for (String asbieChild : Arrays.asList("Sender", "Intermediary", "Receiver", "Signature", "Extension")) {
            WebElement childPanel = editBIEPage.getNodeByPath(path + "/" + asbieChild);
            EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(childPanel);
            assertEquals(1, Integer.valueOf(getText(asbiePanel.getCardinalityMaxField())));
        }
        for (String bbieChild : Arrays.asList("Creation Date Time", "Scenario Identifier", "Correlation Identifier", "BOD Identifier")) {
            WebElement childPanel = editBIEPage.getNodeByPath(path + "/" + bbieChild);
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(childPanel);
            assertEquals(1, Integer.valueOf(getText(bbiePanel.getCardinalityMaxField())));
        }
    }

    @Test
    @DisplayName("TC_5_5_TA_54")
    public void test_TA_54() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.2");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Acknowledge Maintenance Order. Acknowledge Maintenance Order", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setRemark(randomPrint(50, 100).trim());
        topLevelASBIEPPanel.setVersion("version_" + randomAlphanumeric(5, 10));
        topLevelASBIEPPanel.setStatus("status_" + randomAlphanumeric(5, 10));
        topLevelASBIEPPanel.setContextDefinition(randomPrint(50, 100).trim());
        editBIEPage.hitUpdateButton();

        topLevelASBIEPPanel.resetDetail();
        assertTrue(StringUtils.isEmpty(getText(topLevelASBIEPPanel.getRemarkField())));
        assertTrue(StringUtils.isEmpty(getText(topLevelASBIEPPanel.getVersionField())));
        assertTrue(StringUtils.isEmpty(getText(topLevelASBIEPPanel.getStatusField())));
        assertTrue(StringUtils.isEmpty(getText(topLevelASBIEPPanel.getContextDefinitionField())));

        editBIEPage.openPage();
        String path = "/" + asccp.getPropertyTerm() + "/Application Area";
        WebElement applicationArea = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel applicationAreaPanel = editBIEPage.getASBIEPanel(applicationArea);
        applicationAreaPanel.toggleUsed();
        applicationAreaPanel.setRemark(randomPrint(50, 100).trim());
        applicationAreaPanel.setContextDefinition(randomPrint(50, 100).trim());
        editBIEPage.hitUpdateButton();

        applicationAreaPanel.resetDetail();
        assertTrue(StringUtils.isEmpty(getText(applicationAreaPanel.getRemarkField())));
        assertTrue(StringUtils.isEmpty(getText(applicationAreaPanel.getContextDefinitionField())));

        editBIEPage.openPage();
        path = "/" + asccp.getPropertyTerm() + "/Application Area/Scenario Identifier";
        WebElement scenarioIdentifier = editBIEPage.getNodeByPath(path);
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel scenarioIdentifierPanel = editBIEPage.getBBIEPanel(scenarioIdentifier);
        scenarioIdentifierPanel.toggleUsed();
        scenarioIdentifierPanel.setRemark(randomPrint(50, 100).trim());
        scenarioIdentifierPanel.setExample(randomPrint(50, 100).trim());
        scenarioIdentifierPanel.setContextDefinition(randomPrint(50, 100).trim());
        editBIEPage.hitUpdateButton();

        scenarioIdentifierPanel.resetDetail();
        assertTrue(StringUtils.isEmpty(getText(scenarioIdentifierPanel.getRemarkField())));
        assertTrue(StringUtils.isEmpty(getText(scenarioIdentifierPanel.getExampleField())));
        assertTrue(StringUtils.isEmpty(getText(scenarioIdentifierPanel.getContextDefinitionField())));
    }

    @Disabled
    @Test
    @DisplayName("TC_5_5_TA_55")
    public void test_TA_55() {
        // 'Exclude SCs' option has been removed.
    }

    @Test
    @DisplayName("TC_5_5_issue1234")
    public void test_issue1234() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject prevRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ReleaseObject curRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.1");

        /*
         * 'Alternate UOM Code. Unit_ Code' in 10.6 changed to 'Alternate UOM Code. Open_ Code' in 10.7.0.1
         */
        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().getBCCPByDENAndReleaseNum(
                "Alternate UOM Code. Unit_ Code", prevRelease.getReleaseNumber());
        bccp.setDefaultValue(randomAlphabetic(5, 10));
        getAPIFactory().getCoreComponentAPI().updateBCCP(bccp);
        try {
            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Item Master. Item Master", prevRelease.getReleaseNumber());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

            HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
            String path = "/" + asccp.getPropertyTerm() + "/Alternate UOM Code";
            WebElement node = editBIEPage.getNodeByPath(path);
            waitFor(Duration.ofMillis(2000));
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            assertEquals(bccp.getDefaultValue(), getText(bbiePanel.getDefaultValueField()));

            asccp = getAPIFactory().getCoreComponentAPI()
                    .getASCCPByDENAndReleaseNum("Item Master. Item Master", curRelease.getReleaseNumber());
            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
            editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
            node = editBIEPage.getNodeByPath(path);
            waitFor(Duration.ofMillis(2000));
            bbiePanel = editBIEPage.getBBIEPanel(node);

            assertEquals("None", getText(bbiePanel.getValueConstraintSelectField()));
        } finally {
            bccp.setDefaultValue(null);
            getAPIFactory().getCoreComponentAPI().updateBCCP(bccp);
        }
    }

    @Test
    public void test_TA_5_5_56() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Acknowledge Match Document. Change Acknowledge Match Document", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm();
        WebElement node = editBIEPage.getNodeByPath(path);
        String nodeText = getText(node);
        assertTrue(nodeText.contains(asccp.getPropertyTerm()));
        assertTrue(nodeText.contains("1..1"));

        path = "/" + asccp.getPropertyTerm() + "/Version Identifier";
        node = editBIEPage.getNodeByPath(path);
        nodeText = getText(node);
        assertTrue(nodeText.contains("Version Identifier"));
        assertTrue(nodeText.contains("0..1"));

        path = "/" + asccp.getPropertyTerm() + "/Application Area/Receiver";
        node = editBIEPage.getNodeByPath(path);
        nodeText = getText(node);
        assertTrue(nodeText.contains("Receiver"));
        assertTrue(nodeText.contains("0..∞"));
    }

    @Test
    public void test_TA_5_5_56a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Acknowledge Match Document. Change Acknowledge Match Document", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        editBIEPage.toggleHideCardinality(); // Turn off cardinality

        String path = "/" + asccp.getPropertyTerm();
        WebElement node = editBIEPage.getNodeByPath(path);
        String nodeText = getText(node);
        assertTrue(nodeText.contains(asccp.getPropertyTerm()));
        assertFalse(nodeText.contains("1..1"));

        path = "/" + asccp.getPropertyTerm() + "/Version Identifier";
        node = editBIEPage.getNodeByPath(path);
        nodeText = getText(node);
        assertTrue(nodeText.contains("Version Identifier"));
        assertFalse(nodeText.contains("0..1"));

        path = "/" + asccp.getPropertyTerm() + "/Application Area/Receiver";
        node = editBIEPage.getNodeByPath(path);
        nodeText = getText(node);
        assertTrue(nodeText.contains("Receiver"));
        assertFalse(nodeText.contains("0..∞"));
    }

    @Test
    public void test_TA_5_5_56b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("Change Acknowledge Match Document. Change Acknowledge Match Document", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Version Identifier";
        WebElement node = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(1);
        waitFor(ofMillis(1000L));
        String nodeText = getText(node);
        assertTrue(nodeText.contains("Version Identifier"));
        assertTrue(nodeText.contains("1..1"));

        path = "/" + asccp.getPropertyTerm() + "/Application Area/Receiver";
        node = editBIEPage.getNodeByPath(path);
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(node);
        asbiePanel.toggleUsed();
        int randomCardinalityMax = Integer.valueOf(randomNumeric(2, 3));
        asbiePanel.setCardinalityMax(randomCardinalityMax);
        waitFor(ofMillis(1000L));
        nodeText = getText(node);
        assertTrue(nodeText.contains("Receiver"));
        assertTrue(nodeText.contains("0.." + randomCardinalityMax));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

}
