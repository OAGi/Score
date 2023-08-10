package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.ExpressBIEPage;
import org.openqa.selenium.TimeoutException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_6_OAGISDeveloperAuthorizedAccessToBIEExpressionGeneration extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_5_6_TA_1")
    public void test_TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Price List. Price List", "10.6");

        for (String state : Arrays.asList("WIP", "QA", "Production")) {
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                    Arrays.asList(randomBusinessContext), asccp, developer, state);

            expressBIEPage.openPage();
            expressBIEPage.selectBIEForExpression(topLevelASBIEP);
            File generatedBIEExpression = null;
            try {
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
                assertNotNull(generatedBIEExpression);
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_2")
    public void test_TA_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Person Name. Person Name",
                latestRelease.getReleaseNumber());

        for (String state : Arrays.asList("WIP", "QA", "Production")) {
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                    Arrays.asList(randomBusinessContext), asccp, developer, state);

            expressBIEPage.openPage();
            expressBIEPage.selectBIEForExpression(topLevelASBIEP);
            File generatedBIEExpression = null;
            try {
                generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
                assertNotNull(generatedBIEExpression);
            } finally {
                if (generatedBIEExpression != null) {
                    generatedBIEExpression.delete();
                }
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_2 (Verify BIE per Branch)")
    public void test_TA_2_verify_BIE_per_branch() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Price List. Price List", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP_nonLatest = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Person Name. Person Name",
                latestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP_latest = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.setBranch(nonLatestRelease.getReleaseNumber());
        expressBIEPage.setOwner(developer.getLoginId());
        expressBIEPage.hitSearchButton();

        assertNotNull(expressBIEPage.getTableRecordByValue(topLevelASBIEP_nonLatest.getPropertyTerm()));
        assertThrows(TimeoutException.class, () -> expressBIEPage.getTableRecordByValue(topLevelASBIEP_latest.getPropertyTerm()));

        expressBIEPage.openPage();
        expressBIEPage.setBranch(latestRelease.getReleaseNumber());
        expressBIEPage.setOwner(developer.getLoginId());
        expressBIEPage.hitSearchButton();

        assertNotNull(expressBIEPage.getTableRecordByValue(topLevelASBIEP_latest.getPropertyTerm()));
        assertThrows(TimeoutException.class, () -> expressBIEPage.getTableRecordByValue(topLevelASBIEP_nonLatest.getPropertyTerm()));
    }

    @Test
    @DisplayName("TC_5_6_TA_3 (Another Developer)")
    public void test_TA_3_another_developer() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Receiver. Receiver", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.setBranch(nonLatestRelease.getReleaseNumber());
        expressBIEPage.setOwner(anotherDeveloper.getLoginId());
        expressBIEPage.hitSearchButton();

        assertThrows(TimeoutException.class, () -> expressBIEPage.getTableRecordByValue(topLevelASBIEP.getPropertyTerm()));
    }

    @Test
    @DisplayName("TC_5_6_TA_3 (Another End-User)")
    public void test_TA_3_another_enduser() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Receiver. Receiver", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.setBranch(nonLatestRelease.getReleaseNumber());
        expressBIEPage.setOwner(endUser.getLoginId());
        expressBIEPage.hitSearchButton();

        assertThrows(TimeoutException.class, () -> expressBIEPage.getTableRecordByValue(topLevelASBIEP.getPropertyTerm()));
    }

    @Test
    @DisplayName("TC_5_6_TA_4 (Another Developer)")
    public void test_TA_4_another_developer() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Signature. Signature", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "QA");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_4 (Another End-User)")
    public void test_TA_4_another_enduser() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Signature. Signature", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, endUser, "QA");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_5 (Another Developer)")
    public void test_TA_5_another_developer() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherDeveloper);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Sender. Sender", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, anotherDeveloper, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_5 (Another End-User)")
    public void test_TA_5_another_enduser() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Sender. Sender", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, endUser, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_6")
    public void test_TA_6() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        expressBIEPage.selectPutAllSchemasInTheSameFile();
        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                    (filename) -> Pattern.matches("[0-9a-f]{32}.xsd", filename));
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_7")
    public void test_TA_7() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
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
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_8")
    public void test_TA_8() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectPutAllSchemasInTheSameFile();
        expressBIEPage.toggleBIECCTSMetaData();
        assertChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_9")
    public void test_TA_9() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectPutAllSchemasInTheSameFile();
        expressBIEPage.toggleBIEOAGIScoreMetaData();
        assertChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
        assertEnabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        assertNotChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_10")
    public void test_TA_10() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectPutAllSchemasInTheSameFile();

        assertNotChecked(expressBIEPage.getBIECCTSMetaDataCheckbox());
        expressBIEPage.toggleIncludeCCTSDefinitionTag();
        assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());

        expressBIEPage.toggleBIECCTSMetaData();
        expressBIEPage.toggleIncludeCCTSDefinitionTag();
        assertChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());

        expressBIEPage.toggleBIECCTSMetaData();
        assertNotChecked(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
    }

    @Test
    @DisplayName("TC_5_6_TA_11")
    public void test_TA_11() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectPutAllSchemasInTheSameFile();

        assertNotChecked(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
        expressBIEPage.toggleIncludeWHOColumns();
        assertNotChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());

        expressBIEPage.toggleBIEOAGIScoreMetaData();
        expressBIEPage.toggleIncludeWHOColumns();
        assertChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());

        expressBIEPage.toggleBIEOAGIScoreMetaData();
        assertNotChecked(expressBIEPage.getIncludeWHOColumnsCheckbox());
    }

    @Test
    @DisplayName("TC_5_6_TA_12")
    public void test_TA_12() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectJSONSchemaExpression();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_13")
    public void test_TA_13() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.toggleBIEDefinition();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_14")
    public void test_TA_14() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectJSONSchemaExpression();
        assertDisabled(expressBIEPage.getBIECCTSMetaDataCheckbox());
        assertDisabled(expressBIEPage.getIncludeCCTSDefinitionTagCheckbox());
        assertDisabled(expressBIEPage.getBIEGUIDCheckbox());
        assertDisabled(expressBIEPage.getBusinessContextCheckbox());
        assertDisabled(expressBIEPage.getBIEOAGIScoreMetaDataCheckbox());
        assertDisabled(expressBIEPage.getIncludeWHOColumnsCheckbox());
        assertDisabled(expressBIEPage.getBasedCCMetaDataCheckbox());
    }

    @Test
    @DisplayName("TC_5_6_TA_15")
    public void test_TA_15() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
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
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_16")
    public void test_TA_16() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();
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
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_17")
    public void test_TA_17() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.selectPutAllSchemasInTheSameFile();
        expressBIEPage.toggleBIEDefinition();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON,
                    (filename) -> Pattern.matches("[0-9a-f]{32}.json", filename));
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_18")
    public void test_TA_18() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();
        expressBIEPage.toggleBIEDefinition();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_19")
    public void test_TA_19() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.selectJSONSchemaExpression();
        ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
        jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_20")
    public void test_TA_20() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        expressBIEPage.selectJSONSchemaExpression();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();
        ExpressBIEPage.JSONSchemaExpressionOptions jsonSchemaExpressionOptions = expressBIEPage.selectJSONSchemaExpression();
        jsonSchemaExpressionOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Disabled
    @Test
    @DisplayName("TC_5_6_TA_21")
    public void test_TA_21() {
        // Duplicate with test_TA_2_verify_BIE_per_branch
    }

    @Test
    @DisplayName("TC_5_6_TA_22")
    public void test_TA_22() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.setBranch(nonLatestRelease.getReleaseNumber());
        expressBIEPage.setOwner(developer.getLoginId());
        expressBIEPage.hitSearchButton();

        int numberOfBIEsDisplayed = expressBIEPage.getNumberOfBIEsInTable();
        int numberOfBIEsInIndexBox = expressBIEPage.getTotalNumberOfItems();
        assertEquals(topLevelASBIEPs.size(), numberOfBIEsInIndexBox);
        assertEquals(numberOfBIEsDisplayed, numberOfBIEsInIndexBox);
    }

    @Test
    @DisplayName("TC_5_6_TA_23")
    public void test_TA_23() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        List<BusinessContextObject> randomBusinessContexts = Arrays.asList(
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer),
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer)
        );
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                randomBusinessContexts, getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.toggleBusinessContext();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_24")
    public void test_TA_24() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();

        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        postOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_25")
    public void test_TA_25() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();

        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        postOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_26")
    public void test_TA_26() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        openAPIExpressionOptions.toggleGETOperationTemplate();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Disabled
    @Test
    @DisplayName("TC_5_6_TA_27")
    public void test_TA_27() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        openAPIExpressionOptions.toggleGETOperationTemplate();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_28")
    public void test_TA_28() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        openAPIExpressionOptions.toggleGETOperationTemplate();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_29")
    public void test_TA_29() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        openAPIExpressionOptions.toggleGETOperationTemplate();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_30")
    public void test_TA_30() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_31")
    public void test_TA_31() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_32")
    public void test_TA_32() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_33")
    public void test_TA_33() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_34")
    public void test_TA_34() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_35")
    public void test_TA_35() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_36")
    public void test_TA_36() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_37")
    public void test_TA_37() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_38")
    public void test_TA_38() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_39")
    public void test_TA_39() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_40")
    public void test_TA_40() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_41")
    public void test_TA_41() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();
        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Disabled
    @Test
    @DisplayName("TC_5_6_TA_42")
    public void test_TA_42() {
        // Duplicate with TC_5_6_TA_25
    }

    @Test
    @DisplayName("TC_5_6_TA_43")
    public void test_TA_43() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectYAMLOpenAPIFormat();

        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        postOperationOptions.toggleMakeAsAnArray();

        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.YML, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Disabled
    @Test
    @DisplayName("TC_5_6_TA_44")
    public void test_TA_44() {
        // Duplicate with TC_5_6_TA_24
    }

    @Test
    @DisplayName("TC_5_6_TA_45")
    public void test_TA_45() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        List<TopLevelASBIEPObject> topLevelASBIEPs = Arrays.asList(
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Sender. Sender", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Person Name. Person Name", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Signature. Signature", nonLatestRelease.getReleaseNumber()), developer, "WIP"),
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                                "Work Location. Location", nonLatestRelease.getReleaseNumber()), developer, "WIP")
        );
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");
        TopLevelASBIEPObject paginationResponse = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Pagination Response. Pagination Response", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectMultipleBIEsForExpression(nonLatestRelease, topLevelASBIEPs);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();

        ExpressBIEPage.OpenAPIExpressionGETOperationOptions getOperationOptions = openAPIExpressionOptions.toggleGETOperationTemplate();
        getOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        getOperationOptions.toggleIncludePaginationResponse(paginationResponse, randomBusinessContext);
        getOperationOptions.toggleMakeAsAnArray();

        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);
        postOperationOptions.toggleMakeAsAnArray();

        expressBIEPage.selectPutEachSchemaInAnIndividualFile();

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON, true);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_46")
    public void test_TA_46() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Sender. Sender", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        String version = "V1.0";
        topLevelASBIEP.setVersion(version);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelASBIEP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        expressBIEPage.toggleIncludeVersionInFilename();

        File generatedBIEExpression = null;
        try {
            String expectedFilename = asccp.getPropertyTerm() + "-" + version.replaceAll(".", "_") + ".xsd";
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                    (filename) -> expectedFilename.equals(filename));
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_5_6_TA_Test_Refreshing_page")
    public void test_TA_Test_Refreshing_page() {
        // we had an issue that the Meta-header does not listed in the dialog after refreashing the page

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject nonLatestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                "Sender. Sender", nonLatestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, developer, "WIP");
        TopLevelASBIEPObject metaHeader = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                        "Meta Header. Meta Header", nonLatestRelease.getReleaseNumber()), developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        ExpressBIEPage.OpenAPIExpressionOptions openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        ExpressBIEPage.OpenAPIExpressionPOSTOperationOptions postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);

        // Refresh
        expressBIEPage.openPage();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);
        openAPIExpressionOptions = expressBIEPage.selectOpenAPIExpression();
        openAPIExpressionOptions.selectJSONOpenAPIFormat();
        postOperationOptions = openAPIExpressionOptions.togglePOSTOperationTemplate();
        postOperationOptions.toggleIncludeMetaHeader(metaHeader, randomBusinessContext);

        File generatedBIEExpression = null;
        try {
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
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
