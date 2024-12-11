package org.oagi.score.e2e.TS_44_BIEInheritance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_44_3_CreateInheritedBIEWithBaseReusedBIE extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_44_3_1")
    public void enduser_create_InheritedBIE_with_BaseReusedBIE() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject bomHeaderAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBOMHeaderBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomHeaderAsccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        TopLevelASBIEPObject baseBOMBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomAsccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBOMHeaderBIE);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String baseBOMHeaderBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMHeaderRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMHeaderVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMHeaderBIE.setVersion(baseBOMHeaderVersion);
        String baseBOMHeaderStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMHeaderBIE.setStatus(baseBOMHeaderStatus);
        String baseBOMHeaderContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(baseBOMHeaderBusinessTerm);
        topLevelASBIEPPanel.setRemark(baseBOMHeaderRemark);
        topLevelASBIEPPanel.setVersion(baseBOMHeaderVersion);
        topLevelASBIEPPanel.setStatus(baseBOMHeaderStatus);
        topLevelASBIEPPanel.setContextDefinition(baseBOMHeaderContextDefinition);

        WebElement securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Security Classification");
        EditBIEPage.ASBIEPanel securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        int securityClassificationCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int securityClassificationCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String securityClassificationRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String securityClassificationContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        securityClassificationAsbiePanel.toggleUsed();
        securityClassificationAsbiePanel.setCardinalityMin(securityClassificationCardinalityMin);
        securityClassificationAsbiePanel.setCardinalityMax(securityClassificationCardinalityMax);
        securityClassificationAsbiePanel.setRemark(securityClassificationRemark);
        securityClassificationAsbiePanel.setContextDefinition(securityClassificationContextDefinition);

        WebElement statusAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Status");
        EditBIEPage.ASBIEPanel statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        int statusCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int statusCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String statusRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String statusContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        statusAsbiePanel.toggleUsed();
        statusAsbiePanel.setCardinalityMin(statusCardinalityMin);
        statusAsbiePanel.setCardinalityMax(statusCardinalityMax);
        statusAsbiePanel.setRemark(statusRemark);
        statusAsbiePanel.setContextDefinition(statusContextDefinition);

        WebElement effectivityAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Effectivity");
        EditBIEPage.ASBIEPanel effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        int effectivityCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int effectivityCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String effectivityRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String effectivityContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        effectivityAsbiePanel.toggleUsed();
        effectivityAsbiePanel.setCardinalityMin(effectivityCardinalityMin);
        effectivityAsbiePanel.setCardinalityMax(effectivityCardinalityMax);
        effectivityAsbiePanel.setRemark(effectivityRemark);
        effectivityAsbiePanel.setContextDefinition(effectivityContextDefinition);

        editBIEPage.hitUpdateButton();

        // End of the Base "BOM Header"

        editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBOMBIE);
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String baseBOMBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMBIE.setVersion(baseBOMVersion);
        String baseBOMStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMBIE.setStatus(baseBOMStatus);
        String baseBOMContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(baseBOMBusinessTerm);
        topLevelASBIEPPanel.setRemark(baseBOMRemark);
        topLevelASBIEPPanel.setVersion(baseBOMVersion);
        topLevelASBIEPPanel.setStatus(baseBOMStatus);
        topLevelASBIEPPanel.setContextDefinition(baseBOMContextDefinition);

        WebElement bomOptionAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Option");
        EditBIEPage.ASBIEPanel bomOptionAsbiePanel = editBIEPage.getASBIEPanel(bomOptionAsbieNode);

        int bomOptionCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int bomOptionCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String bomOptionRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String bomOptionContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        bomOptionAsbiePanel.toggleUsed();
        bomOptionAsbiePanel.setCardinalityMin(bomOptionCardinalityMin);
        bomOptionAsbiePanel.setCardinalityMax(bomOptionCardinalityMax);
        bomOptionAsbiePanel.setRemark(bomOptionRemark);
        bomOptionAsbiePanel.setContextDefinition(bomOptionContextDefinition);

        WebElement actionCodeBbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/Action Code");
        EditBIEPage.BBIEPanel actionCodeBbiePanel = editBIEPage.getBBIEPanel(actionCodeBbieNode);

        int actionCodeCardinalityMin = 1;
        int actionCodeCardinalityMax = 1;
        String actionCodeRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String actionCodeContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        actionCodeBbiePanel.toggleUsed();
        actionCodeBbiePanel.setCardinalityMin(actionCodeCardinalityMin);
        actionCodeBbiePanel.setCardinalityMax(actionCodeCardinalityMax);
        actionCodeBbiePanel.setRemark(actionCodeRemark);
        actionCodeBbiePanel.setContextDefinition(actionCodeContextDefinition);

        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(baseBOMHeaderBIE);

        // @TODO: Fix the issue in Selenium where clicking on a reused node opens a new tab.
        editBIEPage.openPage();
        WebElement bomHeaderAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        EditBIEPage.ASBIEPanel bomHeaderAsbiePanel = editBIEPage.getASBIEPanel(bomHeaderAsbieNode);

        int bomHeaderCardinalityMin = 1;
        int bomHeaderCardinalityMax = 1;
        String bomHeaderContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        bomHeaderAsbiePanel.setCardinalityMin(bomHeaderCardinalityMin);
        bomHeaderAsbiePanel.setCardinalityMax(bomHeaderCardinalityMax);
        bomHeaderAsbiePanel.setContextDefinition(bomHeaderContextDefinition);

        editBIEPage.hitUpdateButton();

        // End of the Base "BOM"

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(bomHeaderAsccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(bomHeaderAsccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr); // Create Inherited "BOM Header"
        viewEditBIEPage.hitSearchButton();

        assertEquals(2, viewEditBIEPage.getTotalNumberOfItems());

        viewEditBIEPage.setDEN(bomAsccp.getDen());
        viewEditBIEPage.hitSearchButton();

        tr = viewEditBIEPage.getTableRecordByValue(bomAsccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr); // Create Inherited "BOM"
        viewEditBIEPage.hitSearchButton();

        assertEquals(2, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = null;
        {
            WebElement tr1 = viewEditBIEPage.getTableRecordAtIndex(1);
            WebElement td1 = viewEditBIEPage.getColumnByName(tr1, "den");
            String td1Text = getText(td1);

            WebElement tr2 = viewEditBIEPage.getTableRecordAtIndex(2);
            WebElement td2 = viewEditBIEPage.getColumnByName(tr2, "den");
            String td2Text = getText(td2);

            if (td1Text.contains("Based on:")) {
                inheritedBieTr = tr1;
            } else if (td2Text.contains("Based on:")) {
                inheritedBieTr = tr2;
            }
        }

        assertNotNull(inheritedBieTr);
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);

        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEnabled(topLevelASBIEPPanel.getBusinessTermField());
        assertEnabled(topLevelASBIEPPanel.getRemarkField());
        assertEnabled(topLevelASBIEPPanel.getVersionField());
        assertEnabled(topLevelASBIEPPanel.getStatusField());
        assertEnabled(topLevelASBIEPPanel.getContextDefinitionField());

        assertEquals(baseBOMBusinessTerm, getText(topLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(baseBOMRemark, getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals(baseBOMVersion, getText(topLevelASBIEPPanel.getVersionField()));
        assertEquals(baseBOMStatus, getText(topLevelASBIEPPanel.getStatusField()));
        assertEquals(baseBOMContextDefinition, getText(topLevelASBIEPPanel.getContextDefinitionField()));

        EditBIEPage.TopLevelASBIEPPanel baseTopLevelASBIEPPanel = topLevelASBIEPPanel.getBaseTopLevelASBIEPPanel();
        assertDisabled(baseTopLevelASBIEPPanel.getBusinessTermField());
        assertDisabled(baseTopLevelASBIEPPanel.getRemarkField());
        assertDisabled(baseTopLevelASBIEPPanel.getVersionField());
        assertDisabled(baseTopLevelASBIEPPanel.getStatusField());
        assertDisabled(baseTopLevelASBIEPPanel.getContextDefinitionField());

        assertEquals(baseBOMBusinessTerm, getText(baseTopLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(baseBOMRemark, getText(baseTopLevelASBIEPPanel.getRemarkField()));
        assertEquals(baseBOMVersion, getText(baseTopLevelASBIEPPanel.getVersionField()));
        assertEquals(baseBOMStatus, getText(baseTopLevelASBIEPPanel.getStatusField()));
        assertEquals(baseBOMContextDefinition, getText(baseTopLevelASBIEPPanel.getContextDefinitionField()));

        bomOptionAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Option");
        bomOptionAsbiePanel = editBIEPage.getASBIEPanel(bomOptionAsbieNode);

        assertEnabled(bomOptionAsbiePanel.getUsedCheckbox());
        assertEnabled(bomOptionAsbiePanel.getCardinalityMinField());
        assertEnabled(bomOptionAsbiePanel.getCardinalityMaxField());
        assertEnabled(bomOptionAsbiePanel.getRemarkField());
        assertEnabled(bomOptionAsbiePanel.getContextDefinitionField());

        assertChecked(bomOptionAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(bomOptionCardinalityMin), getText(bomOptionAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(bomOptionCardinalityMax), getText(bomOptionAsbiePanel.getCardinalityMaxField()));
        assertEquals(bomOptionRemark, getText(bomOptionAsbiePanel.getRemarkField()));
        assertEquals(bomOptionContextDefinition, getText(bomOptionAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseBOMOptionAsbiePanel = bomOptionAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseBOMOptionAsbiePanel.getUsedCheckbox());
        assertDisabled(baseBOMOptionAsbiePanel.getCardinalityMinField());
        assertDisabled(baseBOMOptionAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseBOMOptionAsbiePanel.getRemarkField());
        assertDisabled(baseBOMOptionAsbiePanel.getContextDefinitionField());

        assertChecked(baseBOMOptionAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(bomOptionCardinalityMin), getText(baseBOMOptionAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(bomOptionCardinalityMax), getText(baseBOMOptionAsbiePanel.getCardinalityMaxField()));
        assertEquals(bomOptionRemark, getText(baseBOMOptionAsbiePanel.getRemarkField()));
        assertEquals(bomOptionContextDefinition, getText(baseBOMOptionAsbiePanel.getContextDefinitionField()));

        actionCodeBbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/Action Code");
        actionCodeBbiePanel = editBIEPage.getBBIEPanel(actionCodeBbieNode);

        assertEnabled(actionCodeBbiePanel.getUsedCheckbox());
        assertEnabled(actionCodeBbiePanel.getCardinalityMinField());
        assertEnabled(actionCodeBbiePanel.getCardinalityMaxField());
        assertEnabled(actionCodeBbiePanel.getRemarkField());
        assertEnabled(actionCodeBbiePanel.getContextDefinitionField());

        assertChecked(actionCodeBbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(actionCodeCardinalityMin), getText(actionCodeBbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(actionCodeCardinalityMax), getText(actionCodeBbiePanel.getCardinalityMaxField()));
        assertEquals(actionCodeRemark, getText(actionCodeBbiePanel.getRemarkField()));
        assertEquals(actionCodeContextDefinition, getText(actionCodeBbiePanel.getContextDefinitionField()));

        EditBIEPage.BBIEPanel baseActionCodeBbiePanel = actionCodeBbiePanel.getBaseBBIEPanel();
        assertDisabled(baseActionCodeBbiePanel.getUsedCheckbox());
        assertDisabled(baseActionCodeBbiePanel.getCardinalityMinField());
        assertDisabled(baseActionCodeBbiePanel.getCardinalityMaxField());
        assertDisabled(baseActionCodeBbiePanel.getRemarkField());
        assertDisabled(baseActionCodeBbiePanel.getContextDefinitionField());

        assertChecked(baseActionCodeBbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(actionCodeCardinalityMin), getText(baseActionCodeBbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(actionCodeCardinalityMax), getText(baseActionCodeBbiePanel.getCardinalityMaxField()));
        assertEquals(actionCodeRemark, getText(baseActionCodeBbiePanel.getRemarkField()));
        assertEquals(actionCodeContextDefinition, getText(baseActionCodeBbiePanel.getContextDefinitionField()));

        // @TODO: Fix the issue in Selenium where clicking on a reused node opens a new tab.
        editBIEPage.openPage();
        bomHeaderAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        bomHeaderAsbiePanel = editBIEPage.getASBIEPanel(bomHeaderAsbieNode);

        assertEnabled(bomHeaderAsbiePanel.getUsedCheckbox());
        assertEnabled(bomHeaderAsbiePanel.getCardinalityMinField());
        assertEnabled(bomHeaderAsbiePanel.getCardinalityMaxField());
        assertEnabled(bomHeaderAsbiePanel.getContextDefinitionField());

        assertChecked(bomHeaderAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(bomHeaderCardinalityMin), getText(bomHeaderAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(bomHeaderCardinalityMax), getText(bomHeaderAsbiePanel.getCardinalityMaxField()));
        assertEquals(bomHeaderContextDefinition, getText(bomHeaderAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseBOMHeaderAsbiePanel = bomHeaderAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseBOMHeaderAsbiePanel.getUsedCheckbox());
        assertDisabled(baseBOMHeaderAsbiePanel.getCardinalityMinField());
        assertDisabled(baseBOMHeaderAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseBOMHeaderAsbiePanel.getContextDefinitionField());

        assertChecked(baseBOMHeaderAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(bomHeaderCardinalityMin), getText(baseBOMHeaderAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(bomHeaderCardinalityMax), getText(baseBOMHeaderAsbiePanel.getCardinalityMaxField()));
        assertEquals(bomHeaderContextDefinition, getText(baseBOMHeaderAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ReusedASBIEPanel bomHeaderReusedAsbiePanel = editBIEPage.getReusedASBIEPanel(bomHeaderAsbieNode);
        assertEquals(baseBOMHeaderBusinessTerm, getText(bomHeaderReusedAsbiePanel.getLegacyBusinessTermField()));
        assertEquals(baseBOMHeaderRemark, getText(bomHeaderReusedAsbiePanel.getRemarkField()));
        assertEquals(baseBOMHeaderVersion, getText(bomHeaderReusedAsbiePanel.getVersionField()));
        assertEquals(baseBOMHeaderStatus, getText(bomHeaderReusedAsbiePanel.getStatusField()));
        assertEquals(baseBOMHeaderContextDefinition, getText(bomHeaderReusedAsbiePanel.getContextDefinitionField()));

        securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Security Classification");
        securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        assertDisabled(securityClassificationAsbiePanel.getUsedCheckbox());
        assertDisabled(securityClassificationAsbiePanel.getCardinalityMinField());
        assertDisabled(securityClassificationAsbiePanel.getCardinalityMaxField());
        assertDisabled(securityClassificationAsbiePanel.getRemarkField());
        assertDisabled(securityClassificationAsbiePanel.getContextDefinitionField());

        assertChecked(securityClassificationAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(securityClassificationCardinalityMin), getText(securityClassificationAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(securityClassificationCardinalityMax), getText(securityClassificationAsbiePanel.getCardinalityMaxField()));
        assertEquals(securityClassificationRemark, getText(securityClassificationAsbiePanel.getRemarkField()));
        assertEquals(securityClassificationContextDefinition, getText(securityClassificationAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseSecurityClassificationAsbiePanel = securityClassificationAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseSecurityClassificationAsbiePanel.getUsedCheckbox());
        assertDisabled(baseSecurityClassificationAsbiePanel.getCardinalityMinField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getRemarkField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getContextDefinitionField());

        assertChecked(baseSecurityClassificationAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(securityClassificationCardinalityMin), getText(baseSecurityClassificationAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(securityClassificationCardinalityMax), getText(baseSecurityClassificationAsbiePanel.getCardinalityMaxField()));
        assertEquals(securityClassificationRemark, getText(baseSecurityClassificationAsbiePanel.getRemarkField()));
        assertEquals(securityClassificationContextDefinition, getText(baseSecurityClassificationAsbiePanel.getContextDefinitionField()));

        statusAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Status");
        statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        assertDisabled(statusAsbiePanel.getUsedCheckbox());
        assertDisabled(statusAsbiePanel.getCardinalityMinField());
        assertDisabled(statusAsbiePanel.getCardinalityMaxField());
        assertDisabled(statusAsbiePanel.getRemarkField());
        assertDisabled(statusAsbiePanel.getContextDefinitionField());

        assertChecked(statusAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(statusCardinalityMin), getText(statusAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(statusCardinalityMax), getText(statusAsbiePanel.getCardinalityMaxField()));
        assertEquals(statusRemark, getText(statusAsbiePanel.getRemarkField()));
        assertEquals(statusContextDefinition, getText(statusAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseStatusAsbiePanel = statusAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseStatusAsbiePanel.getUsedCheckbox());
        assertDisabled(baseStatusAsbiePanel.getCardinalityMinField());
        assertDisabled(baseStatusAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseStatusAsbiePanel.getRemarkField());
        assertDisabled(baseStatusAsbiePanel.getContextDefinitionField());

        assertChecked(baseStatusAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(statusCardinalityMin), getText(baseStatusAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(statusCardinalityMax), getText(baseStatusAsbiePanel.getCardinalityMaxField()));
        assertEquals(statusRemark, getText(baseStatusAsbiePanel.getRemarkField()));
        assertEquals(statusContextDefinition, getText(baseStatusAsbiePanel.getContextDefinitionField()));

        effectivityAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Effectivity");
        effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        assertDisabled(effectivityAsbiePanel.getUsedCheckbox());
        assertDisabled(effectivityAsbiePanel.getCardinalityMinField());
        assertDisabled(effectivityAsbiePanel.getCardinalityMaxField());
        assertDisabled(effectivityAsbiePanel.getRemarkField());
        assertDisabled(effectivityAsbiePanel.getContextDefinitionField());

        assertChecked(effectivityAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(effectivityCardinalityMin), getText(effectivityAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(effectivityCardinalityMax), getText(effectivityAsbiePanel.getCardinalityMaxField()));
        assertEquals(effectivityRemark, getText(effectivityAsbiePanel.getRemarkField()));
        assertEquals(effectivityContextDefinition, getText(effectivityAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseEffectivityAsbiePanel = effectivityAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseEffectivityAsbiePanel.getUsedCheckbox());
        assertDisabled(baseEffectivityAsbiePanel.getCardinalityMinField());
        assertDisabled(baseEffectivityAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseEffectivityAsbiePanel.getRemarkField());
        assertDisabled(baseEffectivityAsbiePanel.getContextDefinitionField());

        assertChecked(baseEffectivityAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(effectivityCardinalityMin), getText(baseEffectivityAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(effectivityCardinalityMax), getText(baseEffectivityAsbiePanel.getCardinalityMaxField()));
        assertEquals(effectivityRemark, getText(baseEffectivityAsbiePanel.getRemarkField()));
        assertEquals(effectivityContextDefinition, getText(baseEffectivityAsbiePanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_44_3_2")
    public void enduser_override_BaseReusedBIE_in_InheritedBIE_with_BaseReusedBIE() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.11");
        ASCCPObject bomHeaderAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBOMHeaderBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomHeaderAsccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        TopLevelASBIEPObject baseBOMBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(library, bomAsccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBOMHeaderBIE);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String baseBOMHeaderBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMHeaderRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMHeaderVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMHeaderBIE.setVersion(baseBOMHeaderVersion);
        String baseBOMHeaderStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMHeaderBIE.setStatus(baseBOMHeaderStatus);
        String baseBOMHeaderContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(baseBOMHeaderBusinessTerm);
        topLevelASBIEPPanel.setRemark(baseBOMHeaderRemark);
        topLevelASBIEPPanel.setVersion(baseBOMHeaderVersion);
        topLevelASBIEPPanel.setStatus(baseBOMHeaderStatus);
        topLevelASBIEPPanel.setContextDefinition(baseBOMHeaderContextDefinition);

        WebElement securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Security Classification");
        EditBIEPage.ASBIEPanel securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        int securityClassificationCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int securityClassificationCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String securityClassificationRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String securityClassificationContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        securityClassificationAsbiePanel.toggleUsed();
        securityClassificationAsbiePanel.setCardinalityMin(securityClassificationCardinalityMin);
        securityClassificationAsbiePanel.setCardinalityMax(securityClassificationCardinalityMax);
        securityClassificationAsbiePanel.setRemark(securityClassificationRemark);
        securityClassificationAsbiePanel.setContextDefinition(securityClassificationContextDefinition);

        WebElement statusAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Status");
        EditBIEPage.ASBIEPanel statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        int statusCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int statusCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String statusRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String statusContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        statusAsbiePanel.toggleUsed();
        statusAsbiePanel.setCardinalityMin(statusCardinalityMin);
        statusAsbiePanel.setCardinalityMax(statusCardinalityMax);
        statusAsbiePanel.setRemark(statusRemark);
        statusAsbiePanel.setContextDefinition(statusContextDefinition);

        WebElement effectivityAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Effectivity");
        EditBIEPage.ASBIEPanel effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        int effectivityCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int effectivityCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String effectivityRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String effectivityContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        effectivityAsbiePanel.toggleUsed();
        effectivityAsbiePanel.setCardinalityMin(effectivityCardinalityMin);
        effectivityAsbiePanel.setCardinalityMax(effectivityCardinalityMax);
        effectivityAsbiePanel.setRemark(effectivityRemark);
        effectivityAsbiePanel.setContextDefinition(effectivityContextDefinition);

        editBIEPage.hitUpdateButton();

        // End of the Base "BOM Header"

        editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBOMBIE);
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String baseBOMBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String baseBOMVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMBIE.setVersion(baseBOMVersion);
        String baseBOMStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        baseBOMBIE.setStatus(baseBOMStatus);
        String baseBOMContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(baseBOMBusinessTerm);
        topLevelASBIEPPanel.setRemark(baseBOMRemark);
        topLevelASBIEPPanel.setVersion(baseBOMVersion);
        topLevelASBIEPPanel.setStatus(baseBOMStatus);
        topLevelASBIEPPanel.setContextDefinition(baseBOMContextDefinition);

        WebElement bomOptionAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Option");
        EditBIEPage.ASBIEPanel bomOptionAsbiePanel = editBIEPage.getASBIEPanel(bomOptionAsbieNode);

        int bomOptionCardinalityMin = RandomUtils.secure().randomInt(2, 5);
        int bomOptionCardinalityMax = RandomUtils.secure().randomInt(5, 10);
        String bomOptionRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String bomOptionContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        bomOptionAsbiePanel.toggleUsed();
        bomOptionAsbiePanel.setCardinalityMin(bomOptionCardinalityMin);
        bomOptionAsbiePanel.setCardinalityMax(bomOptionCardinalityMax);
        bomOptionAsbiePanel.setRemark(bomOptionRemark);
        bomOptionAsbiePanel.setContextDefinition(bomOptionContextDefinition);

        WebElement actionCodeBbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/Action Code");
        EditBIEPage.BBIEPanel actionCodeBbiePanel = editBIEPage.getBBIEPanel(actionCodeBbieNode);

        int actionCodeCardinalityMin = 1;
        int actionCodeCardinalityMax = 1;
        String actionCodeRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String actionCodeContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        actionCodeBbiePanel.toggleUsed();
        actionCodeBbiePanel.setCardinalityMin(actionCodeCardinalityMin);
        actionCodeBbiePanel.setCardinalityMax(actionCodeCardinalityMax);
        actionCodeBbiePanel.setRemark(actionCodeRemark);
        actionCodeBbiePanel.setContextDefinition(actionCodeContextDefinition);

        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(baseBOMHeaderBIE);

        // @TODO: Fix the issue in Selenium where clicking on a reused node opens a new tab.
        editBIEPage.openPage();
        WebElement bomHeaderAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        EditBIEPage.ASBIEPanel bomHeaderAsbiePanel = editBIEPage.getASBIEPanel(bomHeaderAsbieNode);

        int bomHeaderCardinalityMin = 1;
        int bomHeaderCardinalityMax = 1;
        String bomHeaderContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        bomHeaderAsbiePanel.setCardinalityMin(bomHeaderCardinalityMin);
        bomHeaderAsbiePanel.setCardinalityMax(bomHeaderCardinalityMax);
        bomHeaderAsbiePanel.setContextDefinition(bomHeaderContextDefinition);

        editBIEPage.hitUpdateButton();

        // End of the Base "BOM"

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(bomHeaderAsccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(bomHeaderAsccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr); // Create Inherited "BOM Header"
        viewEditBIEPage.hitSearchButton();

        assertEquals(2, viewEditBIEPage.getTotalNumberOfItems());

        WebElement inheritedBieTr = null;
        {
            WebElement tr1 = viewEditBIEPage.getTableRecordAtIndex(1);
            WebElement td1 = viewEditBIEPage.getColumnByName(tr1, "den");
            String td1Text = getText(td1);

            WebElement tr2 = viewEditBIEPage.getTableRecordAtIndex(2);
            WebElement td2 = viewEditBIEPage.getColumnByName(tr2, "den");
            String td2Text = getText(td2);

            if (td1Text.contains("Based on:")) {
                inheritedBieTr = tr1;
            } else if (td2Text.contains("Based on:")) {
                inheritedBieTr = tr2;
            }
        }

        assertNotNull(inheritedBieTr);

        // Edit Inherited "BOM Header"
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        TopLevelASBIEPObject inheritedBOMHeaderBIE = editBIEPage.getTopLevelASBIEP();

        String inheritedBOMHeaderBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String inheritedBOMHeaderRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String inheritedBOMHeaderVersion = "version_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        inheritedBOMHeaderBIE.setVersion(inheritedBOMHeaderVersion);
        String inheritedBOMHeaderStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        inheritedBOMHeaderBIE.setStatus(inheritedBOMHeaderStatus);
        String inheritedBOMHeaderContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(inheritedBOMHeaderBusinessTerm);
        topLevelASBIEPPanel.setRemark(inheritedBOMHeaderRemark);
        topLevelASBIEPPanel.setVersion(inheritedBOMHeaderVersion);
        topLevelASBIEPPanel.setStatus(inheritedBOMHeaderStatus);
        topLevelASBIEPPanel.setContextDefinition(inheritedBOMHeaderContextDefinition);

        securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Security Classification");
        securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        String inheritedSecurityClassificationRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String inheritedSecurityClassificationContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        securityClassificationAsbiePanel.setRemark(inheritedSecurityClassificationRemark);
        securityClassificationAsbiePanel.setContextDefinition(inheritedSecurityClassificationContextDefinition);

        statusAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Status");
        statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        String inheritedStatusRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String inheritedStatusContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        statusAsbiePanel.setRemark(inheritedStatusRemark);
        statusAsbiePanel.setContextDefinition(inheritedStatusContextDefinition);

        effectivityAsbieNode = editBIEPage.getNodeByPath("/" + bomHeaderAsccp.getPropertyTerm() + "/Effectivity");
        effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        String inheritedEffectivityRemark = "remark_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String inheritedEffectivityContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        effectivityAsbiePanel.setRemark(inheritedEffectivityRemark);
        effectivityAsbiePanel.setContextDefinition(inheritedEffectivityContextDefinition);

        editBIEPage.hitUpdateButton();

        // End of Inherited "BOM Header"

        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(bomAsccp.getDen());
        viewEditBIEPage.hitSearchButton();

        tr = viewEditBIEPage.getTableRecordByValue(bomAsccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr); // Create Inherited "BOM"
        viewEditBIEPage.hitSearchButton();

        assertEquals(2, viewEditBIEPage.getTotalNumberOfItems());

        inheritedBieTr = null;
        {
            WebElement tr1 = viewEditBIEPage.getTableRecordAtIndex(1);
            WebElement td1 = viewEditBIEPage.getColumnByName(tr1, "den");
            String td1Text = getText(td1);

            WebElement tr2 = viewEditBIEPage.getTableRecordAtIndex(2);
            WebElement td2 = viewEditBIEPage.getColumnByName(tr2, "den");
            String td2Text = getText(td2);

            if (td1Text.contains("Based on:")) {
                inheritedBieTr = tr1;
            } else if (td2Text.contains("Based on:")) {
                inheritedBieTr = tr2;
            }
        }

        assertNotNull(inheritedBieTr);

        // Override Base Reuse BIE
        editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedBieTr);
        selectProfileBIEToReuseDialog =
                editBIEPage.openOverrideBaseReusedBIEDialog("/" + bomAsccp.getPropertyTerm() + "/BOM Header");
        selectProfileBIEToReuseDialog.selectBIEToReuse(inheritedBOMHeaderBIE);

        editBIEPage.openPage();
        securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Security Classification");
        securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        assertDisabled(securityClassificationAsbiePanel.getUsedCheckbox());
        assertDisabled(securityClassificationAsbiePanel.getCardinalityMinField());
        assertDisabled(securityClassificationAsbiePanel.getCardinalityMaxField());
        assertDisabled(securityClassificationAsbiePanel.getRemarkField());
        assertDisabled(securityClassificationAsbiePanel.getContextDefinitionField());

        assertChecked(securityClassificationAsbiePanel.getUsedCheckbox());
        assertEquals(inheritedSecurityClassificationRemark, getText(securityClassificationAsbiePanel.getRemarkField()));
        assertEquals(inheritedSecurityClassificationContextDefinition, getText(securityClassificationAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseSecurityClassificationAsbiePanel = securityClassificationAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseSecurityClassificationAsbiePanel.getUsedCheckbox());
        assertDisabled(baseSecurityClassificationAsbiePanel.getCardinalityMinField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getRemarkField());
        assertDisabled(baseSecurityClassificationAsbiePanel.getContextDefinitionField());

        assertChecked(baseSecurityClassificationAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(securityClassificationCardinalityMin), getText(baseSecurityClassificationAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(securityClassificationCardinalityMax), getText(baseSecurityClassificationAsbiePanel.getCardinalityMaxField()));
        assertEquals(securityClassificationRemark, getText(baseSecurityClassificationAsbiePanel.getRemarkField()));
        assertEquals(securityClassificationContextDefinition, getText(baseSecurityClassificationAsbiePanel.getContextDefinitionField()));

        statusAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Status");
        statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        assertDisabled(statusAsbiePanel.getUsedCheckbox());
        assertDisabled(statusAsbiePanel.getCardinalityMinField());
        assertDisabled(statusAsbiePanel.getCardinalityMaxField());
        assertDisabled(statusAsbiePanel.getRemarkField());
        assertDisabled(statusAsbiePanel.getContextDefinitionField());

        assertChecked(statusAsbiePanel.getUsedCheckbox());
        assertEquals(inheritedStatusRemark, getText(statusAsbiePanel.getRemarkField()));
        assertEquals(inheritedStatusContextDefinition, getText(statusAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseStatusAsbiePanel = statusAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseStatusAsbiePanel.getUsedCheckbox());
        assertDisabled(baseStatusAsbiePanel.getCardinalityMinField());
        assertDisabled(baseStatusAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseStatusAsbiePanel.getRemarkField());
        assertDisabled(baseStatusAsbiePanel.getContextDefinitionField());

        assertChecked(baseStatusAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(statusCardinalityMin), getText(baseStatusAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(statusCardinalityMax), getText(baseStatusAsbiePanel.getCardinalityMaxField()));
        assertEquals(statusRemark, getText(baseStatusAsbiePanel.getRemarkField()));
        assertEquals(statusContextDefinition, getText(baseStatusAsbiePanel.getContextDefinitionField()));

        effectivityAsbieNode = editBIEPage.getNodeByPath("/" + bomAsccp.getPropertyTerm() + "/BOM Header/Effectivity");
        effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        assertDisabled(effectivityAsbiePanel.getUsedCheckbox());
        assertDisabled(effectivityAsbiePanel.getCardinalityMinField());
        assertDisabled(effectivityAsbiePanel.getCardinalityMaxField());
        assertDisabled(effectivityAsbiePanel.getRemarkField());
        assertDisabled(effectivityAsbiePanel.getContextDefinitionField());

        assertChecked(effectivityAsbiePanel.getUsedCheckbox());
        assertEquals(inheritedEffectivityRemark, getText(effectivityAsbiePanel.getRemarkField()));
        assertEquals(inheritedEffectivityContextDefinition, getText(effectivityAsbiePanel.getContextDefinitionField()));

        EditBIEPage.ASBIEPanel baseEffectivityAsbiePanel = effectivityAsbiePanel.getBaseASBIEPanel();
        assertDisabled(baseEffectivityAsbiePanel.getUsedCheckbox());
        assertDisabled(baseEffectivityAsbiePanel.getCardinalityMinField());
        assertDisabled(baseEffectivityAsbiePanel.getCardinalityMaxField());
        assertDisabled(baseEffectivityAsbiePanel.getRemarkField());
        assertDisabled(baseEffectivityAsbiePanel.getContextDefinitionField());

        assertChecked(baseEffectivityAsbiePanel.getUsedCheckbox());
        assertEquals(Integer.toString(effectivityCardinalityMin), getText(baseEffectivityAsbiePanel.getCardinalityMinField()));
        assertEquals(Integer.toString(effectivityCardinalityMax), getText(baseEffectivityAsbiePanel.getCardinalityMaxField()));
        assertEquals(effectivityRemark, getText(baseEffectivityAsbiePanel.getRemarkField()));
        assertEquals(effectivityContextDefinition, getText(baseEffectivityAsbiePanel.getContextDefinitionField()));
    }

}
