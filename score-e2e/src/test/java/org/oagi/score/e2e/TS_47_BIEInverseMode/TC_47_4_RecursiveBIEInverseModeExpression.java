package org.oagi.score.e2e.TS_47_BIEInverseMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.BCCObject;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.obj.ASCCObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.ExpressBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getDialogButtonByName;
import static org.oagi.score.e2e.impl.PageHelper.click;

/**
 * Inverse Mode changes a shared application setting, so this class must run alone
 * in the JUnit Jupiter engine. The saved setting is restored even when an assertion
 * or expression generation fails.
 */
@Isolated("BIE Inverse Mode changes a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class TC_47_4_RecursiveBIEInverseModeExpression extends BaseTest {

    private boolean originalInverseModeSetting;

    private AppUserObject developer;
    private BusinessContextObject businessContext;
    private TopLevelASBIEPObject bie;
    private TopLevelASBIEPObject secondBie;
    private TopLevelASBIEPObject inheritedBie;
    private ASCCPObject rootAsccp;
    private ASCCPObject recursiveChildAsccp;
    private ASCCPObject zeroMaxChildAsccp;
    private BCCPObject rootBccp;
    private BCCObject rootBcc;
    private final List<File> generatedFiles = new ArrayList<>();

    @BeforeAll
    void captureAndEnableInverseMode() {
        originalInverseModeSetting = getAPIFactory().getApplicationSettingsAPI().isBIEInverseModeEnabled();
        getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(true);
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject firstAcc = getAPIFactory().getCoreComponentAPI()
                .createRandomACC(developer, release, namespace, "Published");
        rootAsccp = getAPIFactory().getCoreComponentAPI()
                .createRandomASCCP(firstAcc, developer, namespace, "Published");
        rootBccp = getAPIFactory().getCoreComponentAPI().getBCCPByDENAndReleaseNum(
                library, "Absence Type Code. Open_ Code", release.getReleaseNumber());
        rootBcc = getAPIFactory().getCoreComponentAPI().appendBCC(firstAcc, rootBccp, "Published");
        ACCObject secondAcc = getAPIFactory().getCoreComponentAPI()
                .createRandomACC(developer, release, namespace, "Published");
        recursiveChildAsccp = getAPIFactory().getCoreComponentAPI()
                .createRandomASCCP(secondAcc, developer, namespace, "Published");
        ACCObject thirdAcc = getAPIFactory().getCoreComponentAPI()
                .createRandomACC(developer, release, namespace, "Published");
        zeroMaxChildAsccp = getAPIFactory().getCoreComponentAPI()
                .createRandomASCCP(thirdAcc, developer, namespace, "Published");

        // Construct A -> A and A -> B -> A with fresh, test-owned components.
        getAPIFactory().getCoreComponentAPI().appendASCC(firstAcc, rootAsccp, "Published");
        getAPIFactory().getCoreComponentAPI().appendASCC(firstAcc, recursiveChildAsccp, "Published");
        ASCCObject zeroMaxAssociation = getAPIFactory().getCoreComponentAPI()
                .appendASCC(firstAcc, zeroMaxChildAsccp, "Published");
        zeroMaxAssociation.setCardinalityMax(0);
        getAPIFactory().getCoreComponentAPI().updateASCC(zeroMaxAssociation);
        getAPIFactory().getCoreComponentAPI().appendASCC(secondAcc, rootAsccp, "Published");

        bie = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                List.of(businessContext), rootAsccp, developer, "WIP");
        getAPIFactory().getBusinessInformationEntityAPI()
                .createBbieNodesForUsedElements(bie.getTopLevelAsbiepId(), developer.getAppUserId());
        getAPIFactory().getBusinessInformationEntityAPI().seedAllBbieProfiling(
                bie.getTopLevelAsbiepId(), false, rootBcc.getCardinalityMin(), rootBcc.getCardinalityMax(), null);
    }

    @Test
    @DisplayName("TC_47_4_TA_1: inverse-mode recursion produces finite valid expressions")
    public void recursive_bie_expression_generation_is_finite_across_formats() throws IOException {
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ExpressBIEPage expressionPage = homePage.getBIEMenu().openExpressBIESubMenu();
        final ExpressBIEPage inverseModeExpressionPage = expressionPage;
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        expressionPage.selectBIEForExpression(bie);

        File inverseModeXsd = assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.XML,
                inverseModeExpressionPage::selectXMLSchemaExpression);
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.JSON,
                () -> inverseModeExpressionPage.selectJSONSchemaExpression().selectVersion("Draft-04"));
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.JSON,
                () -> inverseModeExpressionPage.selectJSONSchemaExpression().selectVersion("2020-12"));
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.YML,
                () -> {
                    ExpressBIEPage.OpenAPIExpressionOptions options = inverseModeExpressionPage.selectOpenAPIExpression();
                    options.selectVersion("3.1");
                    options.selectYAMLOpenAPIFormat();
                });
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.YML,
                () -> {
                    ExpressBIEPage.OpenAPIExpressionOptions options = inverseModeExpressionPage.selectOpenAPIExpression();
                    options.selectVersion("3.0");
                    options.selectYAMLOpenAPIFormat();
                });
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.AVRO,
                inverseModeExpressionPage::selectAvroExpression);
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.ODS,
                () -> inverseModeExpressionPage.selectODFExpression("ODS"));
        assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.FODS,
                () -> inverseModeExpressionPage.selectODFExpression("FODS"));
        File repeatedInverseModeXsd = assertValidDownload(inverseModeExpressionPage, ExpressBIEPage.ExpressionFormat.XML,
                inverseModeExpressionPage::selectXMLSchemaExpression);
        assertEquals(inverseModeXsd.length(), repeatedInverseModeXsd.length(),
                "Repeated generation must not retain recursion state or change the expansion.");

        bie.setInverseMode(false);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        ExpressBIEPage ordinaryModeExpressionPage = homePage.getBIEMenu().openExpressBIESubMenu();
        ordinaryModeExpressionPage.selectBIEForExpression(bie);
        File ordinaryModeXsd = assertValidDownload(ordinaryModeExpressionPage, ExpressBIEPage.ExpressionFormat.XML,
                ordinaryModeExpressionPage::selectXMLSchemaExpression);
        assertTrue(ordinaryModeXsd.length() < inverseModeXsd.length(),
                "Inverse Mode should expand the absent recursive associations in the expression.");
        assertFalse(Files.readString(inverseModeXsd.toPath()).contains("name=\"AbsenceTypeCode\""),
                "An explicitly stored false BBIE must be omitted from the Inverse Mode expression.");
    }

    @Test
    @DisplayName("TC_47_2_TA_1: inverse mode is saved on the individual BIE")
    public void inverse_mode_is_saved_and_reloaded_for_the_selected_bie() {
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        var viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        var editBIEPage = viewEditBIEPage.openEditBIEPage(bie);
        var inverseModeCheckbox = editBIEPage.getTopLevelASBIEPPanel().getInverseModeCheckbox();

        assertTrue(inverseModeCheckbox.isDisplayed());
        assertFalse(inverseModeCheckbox.getAttribute("class").contains("mat-mdc-checkbox-checked"),
                "A new BIE starts with Inverse Mode disabled.");

        editBIEPage.getTopLevelASBIEPPanel().toggleInverseMode();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(
                getDriver(), java.time.Duration.ofSeconds(10));
        wait
                .until(ignored -> getAPIFactory().getBusinessInformationEntityAPI()
                        .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode());
        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode());

        String childPath = "/" + rootAsccp.getPropertyTerm() + "/" + recursiveChildAsccp.getPropertyTerm();
        String bbiePath = "/" + rootAsccp.getPropertyTerm() + "/" + rootBccp.getPropertyTerm();
        WebElement childNode = editBIEPage.getNodeByPath(childPath);
        WebElement childUsedCheckbox = editBIEPage.getUsedCheckboxAtNode(childNode);
        wait.until(ignored -> childUsedCheckbox.getAttribute("class").contains("mat-mdc-checkbox-checked"));
        WebElement bbieNode = editBIEPage.getNodeByPath(bbiePath);
        assertFalse(editBIEPage.getUsedCheckboxAtNode(bbieNode).getAttribute("class")
                        .contains("mat-mdc-checkbox-checked"),
                "An explicitly stored false BBIE must stay unchecked while Inverse Mode is on.");
        click(childUsedCheckbox);
        click(getDialogButtonByName(getDriver(), "Uncheck anyway"));
        editBIEPage.hitUpdateButton();
        childNode = editBIEPage.getNodeByPath(childPath);
        WebElement uncheckedChildCheckbox = editBIEPage.getUsedCheckboxAtNode(childNode);
        wait.until(ignored -> !uncheckedChildCheckbox.getAttribute("class").contains("mat-mdc-checkbox-checked"));

        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        childNode = editBIEPage.getNodeByPath(childPath);
        assertFalse(editBIEPage.getUsedCheckboxAtNode(childNode).getAttribute("class")
                        .contains("mat-mdc-checkbox-checked"),
                "An explicit false ASBIE selection must override the mode default after reopening.");
        assertTrue(editBIEPage.getTopLevelASBIEPPanel().getInverseModeCheckbox()
                        .getAttribute("class").contains("mat-mdc-checkbox-checked"),
                "Reopening the BIE must retain its own Inverse Mode value.");

        editBIEPage.getTopLevelASBIEPPanel().toggleInverseMode();
        wait.until(ignored -> !getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode());
        childNode = editBIEPage.getNodeByPath(childPath);
        assertFalse(editBIEPage.getUsedCheckboxAtNode(childNode).getAttribute("class")
                        .contains("mat-mdc-checkbox-checked"),
                "A stored false ASBIE remains disabled when Inverse Mode is off.");
    }

    @Test
    @DisplayName("TC_47_2_TA_1: Inverse Mode is stored independently for each top-level BIE")
    public void inverse_mode_values_are_independent_per_top_level_bie() {
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        secondBie = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                List.of(businessContext), rootAsccp, developer, "WIP");
        secondBie.setInverseMode(false);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(secondBie);

        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode());
        assertFalse(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(secondBie.getTopLevelAsbiepId()).isInverseMode());
    }

    @Test
    @DisplayName("TC_47_5_TA_1: an inherited BIE persists its own Inverse Mode independently")
    public void inherited_bie_uses_its_own_inverse_mode() {
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(businessContext.getName());
        viewEditBIEPage.setOwner(developer.getLoginId());
        viewEditBIEPage.setDEN(rootAsccp.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement baseBieRow = viewEditBIEPage.getTableRecordByValue(rootAsccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(baseBieRow);

        inheritedBie = getAPIFactory().getBusinessInformationEntityAPI()
                .getLatestInheritedTopLevelASBIEP(bie.getTopLevelAsbiepId(), developer.getAppUserId());
        assertNotNull(inheritedBie, "Creating an inherited BIE should create a child top-level BIE.");
        inheritedBie.setInverseMode(false);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(inheritedBie);

        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode());
        assertFalse(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(inheritedBie.getTopLevelAsbiepId()).isInverseMode());
    }

    @Test
    @DisplayName("TC_47_5_TA_3: a reused BIE keeps its own Inverse Mode")
    public void reused_bie_keeps_its_own_inverse_mode() {
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        secondBie = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                List.of(businessContext), rootAsccp, developer, "WIP");
        secondBie.setInverseMode(false);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(secondBie);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        var editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        String reusedPath = "/" + rootAsccp.getPropertyTerm() + "/" + rootAsccp.getPropertyTerm();
        SelectProfileBIEToReuseDialog reuseDialog = editBIEPage.reuseBIEOnNode(reusedPath);
        reuseDialog.selectBIEToReuse(secondBie);
        editBIEPage.openPage();

        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                        .getReusedTopLevelAsbiepIds(bie.getTopLevelAsbiepId())
                        .contains(secondBie.getTopLevelAsbiepId()),
                "The selected BIE must be persisted as a reused profile on the owning BIE.");

        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode(),
                "The owning BIE retains its own enabled mode after saving a reused occurrence.");
        assertFalse(getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(secondBie.getTopLevelAsbiepId()).isInverseMode(),
                "Saving the owning BIE must not overwrite the separately-owned reused BIE's mode.");
    }

    @Test
    @DisplayName("TC_47_2_TA_7: inverse mode does not make a max-zero association usable")
    public void max_zero_associations_remain_unusable_in_inverse_mode() {
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        var editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        String zeroMaxPath = "/" + rootAsccp.getPropertyTerm() + "/" + zeroMaxChildAsccp.getPropertyTerm();
        WebElement zeroMaxNode = editBIEPage.getNodeByPath(zeroMaxPath);
        WebElement usedCheckbox = editBIEPage.getUsedCheckboxAtNode(zeroMaxNode);
        assertFalse(usedCheckbox.findElement(org.openqa.selenium.By.tagName("input")).isEnabled(),
                "Inverse Mode must not make a max=0 association selectable.");
    }

    @Test
    @DisplayName("TC_47_1_TA_5: disabling the global setting hides the control without clearing a BIE value")
    public void disabling_global_setting_hides_the_control_but_preserves_bie_inverse_mode() {
        bie.setInverseMode(true);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
        getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(false);
        try {
            HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            var editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
            assertFalse(getDriver().findElements(org.openqa.selenium.By.xpath(
                            "//mat-checkbox[contains(normalize-space(.), 'Inverse Mode')]"))
                            .stream().anyMatch(WebElement::isDisplayed),
                    "The Inverse Mode control must be hidden when the global feature is disabled.");
            assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                            .getTopLevelASBIEPByID(bie.getTopLevelAsbiepId()).isInverseMode(),
                    "Hiding the control must not clear the BIE's saved inverseMode value.");
        } finally {
            getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(true);
        }
    }

    private File assertValidDownload(ExpressBIEPage page,
                                     ExpressBIEPage.ExpressionFormat format,
                                     Runnable selectFormat) {
        selectFormat.run();
        File file = page.hitGenerateButton(format);
        generatedFiles.add(file);
        assertNotNull(file, format + " expression must download successfully.");
        assertTrue(file.isFile() && file.length() > 0,
                format + " expression must be a non-empty valid download.");
        return file;
    }

    @AfterEach
    @Override
    public void tearDown() {
        try {
            generatedFiles.forEach(File::delete);
            generatedFiles.clear();
            if (bie != null) {
                getAPIFactory().getBusinessInformationEntityAPI().deleteTopLevelASBIEPByTopLevelASBIEPId(bie);
                bie = null;
            }
            if (secondBie != null) {
                getAPIFactory().getBusinessInformationEntityAPI().deleteTopLevelASBIEPByTopLevelASBIEPId(secondBie);
                secondBie = null;
            }
            if (inheritedBie != null) {
                getAPIFactory().getBusinessInformationEntityAPI().deleteTopLevelASBIEPByTopLevelASBIEPId(inheritedBie);
                inheritedBie = null;
            }
            if (developer != null) {
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(developer.getLoginId());
                developer = null;
            }
        } finally {
            super.tearDown();
        }
    }

    @AfterAll
    void restoreInverseModeSetting() {
        getAPIFactory().getApplicationSettingsAPI().setBIEInverseModeEnable(originalInverseModeSetting);
    }
}
