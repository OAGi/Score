package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

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
    @DisplayName("TC_5_6_TA_3")
    public void test_TA_3() {
    }

    @Test
    @DisplayName("TC_5_6_TA_4")
    public void test_TA_4() {
    }

    @Test
    @DisplayName("TC_5_6_TA_5")
    public void test_TA_5() {
    }

    @Test
    @DisplayName("TC_5_6_TA_6")
    public void test_TA_6() {
    }

    @Test
    @DisplayName("TC_5_6_TA_7")
    public void test_TA_7() {
    }

    @Test
    @DisplayName("TC_5_6_TA_8")
    public void test_TA_8() {
    }

    @Test
    @DisplayName("TC_5_6_TA_9")
    public void test_TA_9() {
    }

    @Test
    @DisplayName("TC_5_6_TA_10")
    public void test_TA_10() {
    }

    @Test
    @DisplayName("TC_5_6_TA_11")
    public void test_TA_11() {
    }

    @Test
    @DisplayName("TC_5_6_TA_12")
    public void test_TA_12() {
    }

    @Test
    @DisplayName("TC_5_6_TA_13")
    public void test_TA_13() {
    }

    @Test
    @DisplayName("TC_5_6_TA_14")
    public void test_TA_14() {
    }

    @Test
    @DisplayName("TC_5_6_TA_15")
    public void test_TA_15() {
    }

    @Test
    @DisplayName("TC_5_6_TA_16")
    public void test_TA_16() {
    }

    @Test
    @DisplayName("TC_5_6_TA_17")
    public void test_TA_17() {
    }

    @Test
    @DisplayName("TC_5_6_TA_18")
    public void test_TA_18() {
    }

    @Test
    @DisplayName("TC_5_6_TA_19")
    public void test_TA_19() {
    }

    @Test
    @DisplayName("TC_5_6_TA_20")
    public void test_TA_20() {
    }

    @Test
    @DisplayName("TC_5_6_TA_21")
    public void test_TA_21() {
    }

    @Test
    @DisplayName("TC_5_6_TA_22")
    public void test_TA_22() {
    }

    @Test
    @DisplayName("TC_5_6_TA_23")
    public void test_TA_23() {
    }

    @Test
    @DisplayName("TC_5_6_TA_24")
    public void test_TA_24() {
    }

    @Test
    @DisplayName("TC_5_6_TA_25")
    public void test_TA_25() {
    }

    @Test
    @DisplayName("TC_5_6_TA_26")
    public void test_TA_26() {
    }

    @Test
    @DisplayName("TC_5_6_TA_27")
    public void test_TA_27() {
    }

    @Test
    @DisplayName("TC_5_6_TA_28")
    public void test_TA_28() {
    }

    @Test
    @DisplayName("TC_5_6_TA_29")
    public void test_TA_29() {
    }

    @Test
    @DisplayName("TC_5_6_TA_30")
    public void test_TA_30() {
    }

    @Test
    @DisplayName("TC_5_6_TA_31")
    public void test_TA_31() {
    }

    @Test
    @DisplayName("TC_5_6_TA_32")
    public void test_TA_32() {
    }

    @Test
    @DisplayName("TC_5_6_TA_33")
    public void test_TA_33() {
    }

    @Test
    @DisplayName("TC_5_6_TA_34")
    public void test_TA_34() {
    }

    @Test
    @DisplayName("TC_5_6_TA_35")
    public void test_TA_35() {
    }

    @Test
    @DisplayName("TC_5_6_TA_36")
    public void test_TA_36() {
    }

    @Test
    @DisplayName("TC_5_6_TA_37")
    public void test_TA_37() {
    }

    @Test
    @DisplayName("TC_5_6_TA_38")
    public void test_TA_38() {
    }

    @Test
    @DisplayName("TC_5_6_TA_39")
    public void test_TA_39() {
    }

    @Test
    @DisplayName("TC_5_6_TA_40")
    public void test_TA_40() {
    }

    @Test
    @DisplayName("TC_5_6_TA_41")
    public void test_TA_41() {
    }

    @Test
    @DisplayName("TC_5_6_TA_42")
    public void test_TA_42() {
    }

    @Test
    @DisplayName("TC_5_6_TA_43")
    public void test_TA_43() {
    }

    @Test
    @DisplayName("TC_5_6_TA_44")
    public void test_TA_44() {
    }

    @Test
    @DisplayName("TC_5_6_TA_45")
    public void test_TA_45() {
    }

    @Test
    @DisplayName("TC_5_6_TA_46")
    public void test_TA_46() {
    }

    @Test
    @DisplayName("TC_5_6_TA_Test_Refreshing_page")
    public void test_TA_Test_Refreshing_page() {
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
