package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.ExpressBIEPage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/** Changes the global BIE Schema filename expression, so it must not overlap any test that generates BIEs. */
@Isolated("The BIE Schema filename expression is a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class TC_5_7_OAGISDeveloperBIESchemaFilenameExpression extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();
    private String originalExpression;

    @BeforeAll
    void captureExpression() {
        originalExpression = getAPIFactory().getApplicationSettingsAPI().getBIESchemaFilenameExpression();
    }

    @AfterAll
    void restoreExpression() {
        getAPIFactory().getApplicationSettingsAPI().setBIESchemaFilenameExpression(originalExpression);
        randomAccounts.forEach(account -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(account.getLoginId()));
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_5_7_TA_1")
    public void generated_xml_filename_follows_the_configured_bie_schema_filename_expression() {
        getAPIFactory().getApplicationSettingsAPI().setBIESchemaFilenameExpression("{BIE Property Term:separator(' ')}");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        randomAccounts.add(developer);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library,
                "Person Name. Person Name", latestRelease.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(randomBusinessContext), asccp, developer, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBIEPage.selectBIEForExpression(topLevelASBIEP);

        File generatedBIEExpression = null;
        try {
            String expectedFilename = asccp.getPropertyTerm() + ".xsd";
            generatedBIEExpression = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                    (filename) -> expectedFilename.equals(filename));
            assertNotNull(generatedBIEExpression);
        } finally {
            if (generatedBIEExpression != null) {
                generatedBIEExpression.delete();
            }
        }
    }
}
