package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.TimeoutException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.SAME_THREAD)
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
    public void end_user_can_generate_expression_for_owned_bie_in_any_state_across_releases() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject earilerRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

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
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertEquals(usera.getAppUserId(), topLevelAsbiep.getOwnerUserId());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
                waitFor(ofMillis(1000L));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }

            expressBIEPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_2")
    public void end_user_cannot_generate_expression_for_another_users_wip_bie() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "WIP");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1200));
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnerUserId());
            assertEquals("WIP", topLevelAsbiep.getState());
            assertThrows(TimeoutException.class, () -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_3")
    public void end_user_can_generate_expression_for_another_users_qa_bie() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnerUserId());
            assertEquals("QA", topLevelAsbiep.getState());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_4")
    public void end_user_can_generate_expression_for_another_users_production_bie() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertNotEquals(usera.getAppUserId(), topLevelAsbiep.getOwnerUserId());
            assertEquals("Production", topLevelAsbiep.getState());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_5")
    public void end_user_can_generate_single_xml_schema_in_same_package_without_annotations() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            assertNotChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_6")
    public void end_user_can_generate_single_xml_schema_in_same_package_with_all_annotations() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            expressBIEPage.toggleBIECCTSMetaData();
            assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            expressBIEPage.toggleIncludeCCTSDefinitionTag();
            expressBIEPage.toggleBIEGUID();
            expressBIEPage.toggleBusinessContext();
            expressBIEPage.toggleBIEOAGIConnectCenterMetaData();
            assertChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            expressBIEPage.toggleIncludeWHOColumns();
            expressBIEPage.toggleBasedCCMetaData();

            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_7")
    public void end_user_can_generate_single_xml_schema_with_ccts_metadata_without_definition_tag() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1200));
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            expressBIEPage.toggleBIECCTSMetaData();
            assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            assertNotChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_8")
    public void end_user_can_generate_single_xml_schema_with_score_metadata_without_who_columns() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertNotChecked(expressBIEPage.getBIEGUIDCheckbox());
            assertNotChecked(expressBIEPage.getBusinessContextCheckbox());
            expressBIEPage.toggleBIEOAGIConnectCenterMetaData();
            assertChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_9")
    public void end_user_cannot_select_include_ccts_definition_tag_without_ccts_metadata() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_10")
    public void end_user_cannot_select_include_who_columns_without_score_metadata() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertNotChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_11")
    public void end_user_can_generate_single_json_schema_in_same_package_without_annotations() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        getDriver().manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1200));
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            expressBIEPage.toggleBIEDefinition();
            assertNotChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertEnabled(jsonSchemaExpressionOptions.getMakeAsAnArrayCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getMakeAsAnArrayCheckbox());
            assertEnabled(jsonSchemaExpressionOptions.getIncludeMetaHeaderCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getIncludeMetaHeaderCheckbox());
            assertEnabled(jsonSchemaExpressionOptions.getIncludePaginationResponseCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getIncludePaginationResponseCheckbox());
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_12")
    public void end_user_can_generate_single_json_schema_with_bie_definition_annotation() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertEnabled(jsonSchemaExpressionOptions.getMakeAsAnArrayCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getMakeAsAnArrayCheckbox());
            assertEnabled(jsonSchemaExpressionOptions.getIncludeMetaHeaderCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getIncludeMetaHeaderCheckbox());
            assertEnabled(jsonSchemaExpressionOptions.getIncludePaginationResponseCheckbox());
            assertNotChecked(jsonSchemaExpressionOptions.getIncludePaginationResponseCheckbox());
            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_13")
    public void end_user_can_only_use_based_cc_metadata_option_for_json_schema() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEWIP);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            assertDoesNotThrow(() -> expressBIEPage.selectBIEForExpression(topLevelAsbiep));
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            expressBIEPage.selectJSONSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            assertDisabled(expressBIEPage.getBIECCTSMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
            assertDisabled(expressBIEPage.getBIEGUIDCheckbox());
            assertDisabled(expressBIEPage.getBusinessContextCheckbox());
            assertDisabled(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
            assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
            assertEnabled(expressBIEPage.getBasedCCMetaDataCheckbox());
            assertNotChecked(expressBIEPage.getBasedCCMetaDataCheckbox());

            File generatedBIEExpression = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_14")
    public void end_user_can_generate_multiple_xml_schemas_in_same_package_with_selected_annotations() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);

        assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
        expressBIEPage.selectXMLSchemaExpression();
        expressBIEPage.selectPutAllSchemasInTheSameFile();
        expressBIEPage.toggleBIECCTSMetaData();
        assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        expressBIEPage.toggleIncludeCCTSDefinitionTag();
        expressBIEPage.toggleBIEGUID();
        expressBIEPage.toggleBusinessContext();
        expressBIEPage.toggleBIEOAGIConnectCenterMetaData();
        assertChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        expressBIEPage.toggleIncludeWHOColumns();
        expressBIEPage.toggleBasedCCMetaData();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                    (filename) -> Pattern.matches("[0-9a-f]{32}.xsd", filename));
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_15")
    public void end_user_can_generate_multiple_xml_schemas_in_different_packages_with_selected_annotations() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);

        assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
        expressBIEPage.selectXMLSchemaExpression();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();
        expressBIEPage.toggleBIECCTSMetaData();
        assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        expressBIEPage.toggleIncludeCCTSDefinitionTag();
        expressBIEPage.toggleBIEGUID();
        expressBIEPage.toggleBusinessContext();
        expressBIEPage.toggleBIEOAGIConnectCenterMetaData();
        assertChecked(expressBIEPage.getBIEOAGIConnectCenterMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        expressBIEPage.toggleIncludeWHOColumns();
        expressBIEPage.toggleBasedCCMetaData();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML, true);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_16")
    public void end_user_can_generate_multiple_json_schemas_in_same_package_with_bie_definition() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);

        assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.selectPutAllSchemasInTheSameFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                    (filename) -> Pattern.matches("[0-9a-f]{32}.json", filename));
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_17")
    public void end_user_can_generate_multiple_json_schemas_in_different_packages_with_bie_definition() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);

        assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_18")
    public void end_user_can_generate_single_json_schema_with_meta_header_object() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);

            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("metaHeader").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_18a")
    public void end_user_can_generate_single_json_schema_with_meta_header_make_array_and_keep_meta_header_as_object() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleMakeAsAnArray();
            jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);

            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("metaHeader").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_18b")
    public void end_user_can_generate_single_json_schema_with_meta_header_and_pagination_response_as_objects() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            jsonSchemaExpressionOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("metaHeader").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_18c")
    public void end_user_can_generate_single_json_schema_with_meta_header_pagination_response_and_make_array_while_keeping_objects() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            assertChecked(expressBIEPage.getBIEDefinitionCheckbox());
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleMakeAsAnArray();
            jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            jsonSchemaExpressionOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("metaHeader").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_19")
    public void end_user_can_generate_multiple_json_schemas_with_selected_meta_header() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
        jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();
        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }

    }

    @Test
    @DisplayName("TC_6_3_TA_20")
    public void end_user_can_search_bies_in_express_bie_page() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, releaseOne, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, releaseTwo, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_21")
    public void end_user_can_see_matching_bie_count_in_grid_and_index_summary() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        BusinessContextObject context;
        ASCCPObject asccp;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        expressBIEPage.setBranch(release.getReleaseNumber());
        int numberOfBIEsDisplayed = expressBIEPage.getNumberOfBIEsInTable();
        int numberOfBIEsInIndexBox = expressBIEPage.getTotalNumberOfItems();
        assertEquals(numberOfBIEsDisplayed, numberOfBIEsInIndexBox);
    }

    @Test
    @DisplayName("TC_6_3_TA_22")
    public void end_user_can_generate_xml_expression_for_bie_with_multiple_business_contexts() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, releaseOne, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            /**
             * The end user can generate an expression of a single BIE with multiple business contexts assigned
             */
            BusinessContextObject contextOne = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            BusinessContextObject contextTwo = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextOne, contextTwo), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            expressBIEPage.selectXMLSchemaExpression();
            expressBIEPage.selectPutAllSchemasInTheSameFile();
            expressBIEPage.toggleBusinessContext();
            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".xsd";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_23")
    public void end_user_can_generate_single_openapi_yaml_expression_with_end_user_owned_meta_header_and_pagination_response() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "Production");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();

            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleMakeAsAnArray();
            assertNotEquals(usera.getAppUserId(), metaHeaderASBIEP.getOwnerUserId());
            assertEquals(userb.getAppUserId(), metaHeaderASBIEP.getOwnerUserId());
            assertFalse(userb.isDeveloper());
            List<String> acceptedStates = Arrays.asList("QA", "Production");
            assertTrue(acceptedStates.contains(metaHeaderASBIEP.getState()));
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            assertNotEquals(usera.getAppUserId(), paginationResponseASBIEP.getOwnerUserId());
            assertEquals(userb.getAppUserId(), paginationResponseASBIEP.getOwnerUserId());
            assertTrue(acceptedStates.contains(paginationResponseASBIEP.getState()));
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);

            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleMakeAsAnArray();
            postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_24")
    public void end_user_can_generate_single_openapi_json_expression_with_developer_owned_meta_header_and_pagination_response() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "Production");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectJSONOpenAPIFormat();

            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleMakeAsAnArray();
            assertNotEquals(usera.getAppUserId(), metaHeaderASBIEP.getOwnerUserId());
            assertEquals(userb.getAppUserId(), metaHeaderASBIEP.getOwnerUserId());
            assertTrue(userb.isDeveloper());
            List<String> acceptedStates = Arrays.asList("QA", "Production");
            assertTrue(acceptedStates.contains(metaHeaderASBIEP.getState()));
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            assertNotEquals(usera.getAppUserId(), paginationResponseASBIEP.getOwnerUserId());
            assertEquals(userb.getAppUserId(), paginationResponseASBIEP.getOwnerUserId());
            assertTrue(acceptedStates.contains(paginationResponseASBIEP.getState()));
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);

            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleMakeAsAnArray();
            postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_25")
    public void end_user_can_generate_single_json_schema_with_pagination_response_object() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("paginationResponse").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_25a")
    public void end_user_can_generate_single_json_schema_with_pagination_response_make_array_and_keep_pagination_response_as_object() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject paginationResponseASBIEP;
        BusinessContextObject context;
        ASCCPObject asccp;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            });
            ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
            jsonSchemaExpressionOptions.toggleMakeAsAnArray();
            jsonSchemaExpressionOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            File file = null;
            try {
                String expectedFilename = topLevelAsbiep.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("properties");
                String metaHeaderNodeType = rootNode.get("paginationResponse").getNodeType().toString();
                assertEquals("OBJECT", metaHeaderNodeType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_26")
    public void end_user_cannot_put_all_json_schemas_in_same_file_when_meta_header_or_pagination_response_is_selected() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ReleaseObject release;
        TopLevelASBIEPObject paginationResponseASBIEP;
        TopLevelASBIEPObject metaHeaderASBIEP;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
        jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        assertDisabled(expressBIEPage.getPutAllSchemasInTheSameFileRadioButton());
        jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        jsonSchemaExpressionOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
        assertDisabled(expressBIEPage.getPutAllSchemasInTheSameFileRadioButton());
    }

    @Test
    @DisplayName("TC_6_3_TA_27")
    public void end_user_can_generate_expression_for_reusing_bie_referenced_multiple_times() {
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject reusableASCCP;
        BusinessContextObject context;
        ReleaseObject release;
        TopLevelASBIEPObject reusableBIE;
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject accReusable = coreComponentAPI.createRandomACC(usera, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(release, dataType, usera, namespace, "Published");
            coreComponentAPI.appendBCC(accReusable, bccp, "Published");
            reusableASCCP = coreComponentAPI.createRandomASCCP(accReusable, usera, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            reusableBIE = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), reusableASCCP, usera, "QA");

            ACCObject accOne = coreComponentAPI.createRandomACC(usera, release, namespace, "Published");
            coreComponentAPI.appendASCC(accOne, reusableASCCP, "Published");
            ASCCPObject asccpOne = coreComponentAPI.createRandomASCCP(accOne, usera, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpOne, usera, "WIP");
            biesForTesting.add(useraBIEOne);
            bieASCCPMap.put(useraBIEOne, asccpOne);

            ACCObject accTwo = coreComponentAPI.createRandomACC(usera, release, namespace, "Published");
            coreComponentAPI.appendASCC(accTwo, reusableASCCP, "Published");
            ASCCPObject asccpTwo = coreComponentAPI.createRandomASCCP(accTwo, usera, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIETwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpTwo, usera, "WIP");
            biesForTesting.add(useraBIETwo);
            bieASCCPMap.put(useraBIETwo, asccpTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(bie);
            ASCCPObject asccp = bieASCCPMap.get(bie);
            SelectProfileBIEToReuseDialog selectProfileBIEToReuse =
                    editBIEPage.reuseBIEOnNode("/" + asccp.getPropertyTerm() + "/" + reusableASCCP.getPropertyTerm());
            selectProfileBIEToReuse.selectBIEToReuse(reusableBIE);
        }

        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectBIEForExpression(reusableBIE);
        });
        expressBIEPage.selectXMLSchemaExpression();
        File file = null;
        try {
            String expectedFilename = reusableBIE.getPropertyTerm().replaceAll(" ", "") + ".xsd";
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                    (filename) -> expectedFilename.equals(filename));
        } finally {
            if (file != null) {
                file.delete();
            }
        }

        expressBIEPage.selectJSONSchemaExpression();
        file = null;
        try {
            String expectedFilename = reusableBIE.getPropertyTerm().replaceAll(" ", "") + ".json";
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                    (filename) -> expectedFilename.equals(filename));
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_28")
    public void end_user_can_generate_single_openapi_yaml_expression_with_code_generation_friendly_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_29")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_code_generation_friendly_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_30")
    public void end_user_can_generate_single_openapi_json_expression_with_code_generation_friendly_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectJSONOpenAPIFormat();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_31")
    public void end_user_can_generate_multiple_openapi_json_expressions_with_code_generation_friendly_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
        } finally {
            if (file != null) {
                file.delete();
            }

        }
    }

    @Test
    @DisplayName("TC_6_3_TA_32")
    public void end_user_can_generate_single_openapi_yaml_expression_with_get_template_make_array_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleMakeAsAnArray();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_33")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_get_template_make_array_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }

        }
    }

    @Test
    @DisplayName("TC_6_3_TA_34")
    public void end_user_can_generate_single_openapi_yaml_expression_with_get_template_meta_header() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_35")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_get_template_meta_header() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_36")
    public void end_user_can_generate_single_openapi_yaml_expression_with_get_template_meta_header_and_pagination_response() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_37")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_get_template_meta_header_and_pagination_response() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_38")
    public void end_user_can_generate_single_openapi_yaml_expression_with_post_template_make_array_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleMakeAsAnArray();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_39")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_post_template_make_array_option() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_40")
    public void end_user_can_generate_single_openapi_yaml_expression_with_post_template_meta_header() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_41")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_post_template_meta_header() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        ReleaseObject release;
        TopLevelASBIEPObject metaHeaderASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_42")
    public void end_user_can_generate_single_openapi_yaml_expression_with_get_template_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            getOperationOptions.toggleMakeAsAnArray();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_43")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_get_template_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
        getOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_44")
    public void end_user_can_generate_single_openapi_yaml_expression_with_get_and_post_templates_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectYAMLOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            getOperationOptions.toggleMakeAsAnArray();

            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            postOperationOptions.toggleMakeAsAnArray();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".yml";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_45")
    public void end_user_can_generate_multiple_openapi_yaml_expressions_with_get_and_post_templates_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        postOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_46")
    public void end_user_can_generate_single_openapi_json_expression_with_get_and_post_templates_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        for (TopLevelASBIEPObject bie : biesForTesting) {
            assertDoesNotThrow(() -> {
                expressBIEPage.selectBIEForExpression(bie);
            });
            ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
            openAPIExpressionOptions.selectJSONOpenAPIFormat();
            ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
            getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
            getOperationOptions.toggleMakeAsAnArray();

            ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
            postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
            postOperationOptions.toggleMakeAsAnArray();
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            File file = null;
            try {
                String expectedFilename = bie.getPropertyTerm().replaceAll(" ", "") + ".json";
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                        (filename) -> expectedFilename.equals(filename));
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_3_TA_47")
    public void end_user_can_generate_multiple_openapi_json_expressions_with_get_and_post_templates_meta_header_pagination_response_and_make_array() {
        AppUserObject usera;
        AppUserObject userb;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccp;
        BusinessContextObject context;
        TopLevelASBIEPObject metaHeaderASBIEP;
        TopLevelASBIEPObject paginationResponseASBIEP;
        ReleaseObject release;
        {
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, this.release);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);

            thisAccountWillBeDeletedAfterTests(usera);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "Production");
            biesForTesting.add(useraBIEProduction);

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Published");

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, userb, "QA");
            biesForTesting.add(useraBIEQA);

            ASCCPObject metaHeaderASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Meta Header. Meta Header", release.getReleaseNumber());
            metaHeaderASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), metaHeaderASCCP, userb, "QA");

            ASCCPObject paginationResponseASCCP = getAPIFactory().getCoreComponentAPI().
                    getASCCPByDENAndReleaseNum(library, "Pagination Response. Pagination Response", release.getReleaseNumber());
            paginationResponseASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), paginationResponseASCCP, userb, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        assertDoesNotThrow(() -> {
            expressBIEPage.selectMultipleBIEsForExpression(release, biesForTesting);
        });
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponseASBIEP, context);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeaderASBIEP, context);
        postOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File file = null;
        try {
            file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
        } finally {
            if (file != null) {
                file.delete();
            }
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
