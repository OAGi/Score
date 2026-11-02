package org.oagi.score.e2e.TS_46_BIEPackageManagement;

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
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPackagePage;
import org.oagi.score.e2e.page.bie.ViewBIEPackagePage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Suite 46 / Test Case 46.3 - the BIE Package export names its schema files by the configured
 * BIE Package Schema filename expression (issue #1870, BIE Package follow-up).
 *
 * <p>Changes a global application setting, so it must not overlap any test that generates BIE Packages.</p>
 */
@Isolated("The BIE Package Schema filename expression is a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class TC_46_3_BIEPackageSchemaFilenameExpression extends BaseTest {

    private static final String EXPRESSION = "PKGEXPR-{BIE Property Term:separator('_')}";

    private final List<AppUserObject> randomAccounts = new ArrayList<>();
    private String originalExpression;

    @BeforeAll
    void captureExpression() {
        originalExpression = getAPIFactory().getApplicationSettingsAPI().getBIEPackageSchemaFilenameExpression();
    }

    @AfterAll
    void restoreExpression() {
        getAPIFactory().getApplicationSettingsAPI().setBIEPackageSchemaFilenameExpression(originalExpression);
        randomAccounts.forEach(account -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(account.getLoginId()));
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_46_3_1")
    public void generated_package_schema_filename_follows_the_configured_bie_package_schema_filename_expression() throws IOException {
        getAPIFactory().getApplicationSettingsAPI().setBIEPackageSchemaFilenameExpression(EXPRESSION);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        randomAccounts.add(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library,
                "Person Name. Person Name", release.getReleaseNumber());
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage editBIEPackagePage = viewBIEPackagePage.hitNewBIEPackageButton();
        editBIEPackagePage.setName("TC 46.3 Package " + endUser.getLoginId());
        editBIEPackagePage.setDescription("BIE Package for TC_46_3.");
        editBIEPackagePage.hitUpdateButton();
        getAPIFactory().getBusinessInformationEntityAPI().addBieToBiePackage(
                editBIEPackagePage.getBiePackageId(), bie.getTopLevelAsbiepId(), endUser.getAppUserId());
        editBIEPackagePage.openPage(editBIEPackagePage.getBiePackageId());

        File generatedZip = editBIEPackagePage.clickGenerateAndDownloadZip();
        try (ZipFile zipFile = new ZipFile(generatedZip)) {
            String expectedFilename = "PKGEXPR-" + asccp.getPropertyTerm().replace(' ', '_') + ".xsd";
            List<String> names = new ArrayList<>();
            zipFile.stream().map(ZipEntry::getName).forEach(names::add);
            assertTrue(names.stream().anyMatch(n -> n.equals(expectedFilename) || n.endsWith("/" + expectedFilename)),
                    "Expected a schema file named '" + expectedFilename + "' in the BIE Package ZIP but found " + names);
        } finally {
            generatedZip.delete();
        }
    }
}
