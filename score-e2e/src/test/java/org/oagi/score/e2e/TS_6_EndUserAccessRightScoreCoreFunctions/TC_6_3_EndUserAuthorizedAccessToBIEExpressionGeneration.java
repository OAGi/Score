package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

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
import org.oagi.score.e2e.page.bie.ExpressBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.openqa.selenium.TimeoutException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.AssertionHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_6_3_EndUserAuthorizedAccessToBIEExpressionGeneration extends BaseTest {

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
    @DisplayName("TC_6_3_TA_1")
    public void test_TA_1() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject earilerRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Earlier release
             */

            ACCObject accOne = coreComponentAPI.createRandomACC(usera, earilerRelease, namespace, "Published");
            ASCCPObject asccpOne = coreComponentAPI.createRandomASCCP(accOne, usera, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpOne, usera, "WIP");
            biesForTesting.add(useraBIEWIP);

            ACCObject accTwo = coreComponentAPI.createRandomACC(usera, earilerRelease, namespace, "Published");
            ASCCPObject asccpTwo = coreComponentAPI.createRandomASCCP(accTwo, usera, namespace, "Published");

            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpTwo, usera, "Production");
            biesForTesting.add(useraBIEProduction);

            ACCObject accThree = coreComponentAPI.createRandomACC(usera, earilerRelease, namespace, "Published");
            ASCCPObject asccpThree = coreComponentAPI.createRandomASCCP(accThree, usera, namespace, "Published");

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpThree, usera, "QA");
            biesForTesting.add(useraBIEQA);

            /**
             * The latest release
             */
            accOne = coreComponentAPI.createRandomACC(usera, latestRelease, namespace, "Published");
            asccpOne = coreComponentAPI.createRandomASCCP(accOne, usera, namespace, "Published");

            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpOne, usera, "WIP");
            biesForTesting.add(useraBIEWIP);

            accTwo = coreComponentAPI.createRandomACC(usera, latestRelease, namespace, "Published");
            asccpTwo = coreComponentAPI.createRandomASCCP(accTwo, usera, namespace, "Published");

            useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpTwo, usera, "Production");
            biesForTesting.add(useraBIEProduction);

            accThree = coreComponentAPI.createRandomACC(usera, latestRelease, namespace, "Published");
            asccpThree = coreComponentAPI.createRandomASCCP(accThree, usera, namespace, "Published");

            useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpThree, usera, "QA");
            biesForTesting.add(useraBIEQA);
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertEquals(usera.getAppUserId(), topLevelAsbiep.getOwnwerUserId());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            expressBIEPage.hitGenerateButton();
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_2")
    public void test_TA_2() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "WIP");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnwerUserId());
            assertEquals("WIP", topLevelAsbiep.getState());
            assertThrows(TimeoutException.class, () -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_3")
    public void test_TA_3() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnwerUserId());
            assertEquals("QA", topLevelAsbiep.getState());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            expressBIEPage.hitGenerateButton();
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_4")
    public void test_TA_4() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnwerUserId());
            assertEquals("Production", topLevelAsbiep.getState());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            expressBIEPage.hitGenerateButton();
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_5")
    public void test_TA_5() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            assertNotChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            expressBIEPage.hitGenerateButton();
        }
    }
    @Test
    @DisplayName("TC_6_3_TA_6")
    public void test_TA_6() throws Exception {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            expressBIEPage.toggleBIECCTSMetaData();
            assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            expressBIEPage.toggleIncludeCCTSDefinitionTag();
            expressBIEPage.toggleBIEGUID();
            expressBIEPage.toggleBusinessContext();
            expressBIEPage.toggleBIEOAGIScoreMetaData();
            assertChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            expressBIEPage.toggleIncludeWHOColumns();
            expressBIEPage.toggleBasedCCMetaData();

            File generatedBIEExpression = null;
            try {
                generatedBIEExpression = expressBIEPage.hitGenerateButton();

                // TODO: BIE expression validation
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(generatedBIEExpression);
                Element rootElement = document.getDocumentElement();
                assertEquals("xsd:schema", rootElement.getTagName());
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_7")
    public void test_TA_7() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            expressBIEPage.toggleBIECCTSMetaData();
            assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            assertNotChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            expressBIEPage.hitGenerateButton();
        }
    }
    @Test
    @DisplayName("TC_6_3_TA_8")
    public void test_TA_8() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            expressBIEPage.toggleBIEOAGIScoreMetaData();
            assertChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            expressBIEPage.hitGenerateButton();
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_9")
    public void test_TA_9() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        }
    }
    @Test
    @DisplayName("TC_6_3_TA_10")
    public void test_TA_10() {
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);;
            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().maximize();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow( () ->{expressBIEPage.selectBIEForExpression(topLevelAsbiep);});
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        }
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
