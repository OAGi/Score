package org.oagi.score.e2e.TS_44_BIEInheritance;

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
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_44_1_CreateInheritedBIE extends BaseTest {

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
    @DisplayName("TC_44_1_1")
    public void enduser_create_inherited_BIE() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.11");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum("BOM Header. BOM Header", release.getReleaseNumber());
        TopLevelASBIEPObject baseBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(asccp.getDen(), release.getReleaseNumber()),
                        endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBIE);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String businessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String remark = "remark_" + randomAlphanumeric(5, 10);
        String version = "version_" + randomAlphanumeric(5, 10);
        String status = "status_" + randomAlphanumeric(5, 10);
        String contextDefinition = randomPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(businessTerm);
        topLevelASBIEPPanel.setRemark(remark);
        topLevelASBIEPPanel.setVersion(version);
        topLevelASBIEPPanel.setStatus(status);
        topLevelASBIEPPanel.setContextDefinition(contextDefinition);

        WebElement securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Security Classification");
        EditBIEPage.ASBIEPanel securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        int securityClassificationCardinalityMin = nextInt(2, 5);
        int securityClassificationCardinalityMax = nextInt(5, 10);
        String securityClassificationRemark = "remark_" + randomAlphanumeric(5, 10);
        String securityClassificationContextDefinition = randomPrint(50, 100).trim();

        securityClassificationAsbiePanel.toggleUsed();
        securityClassificationAsbiePanel.setCardinalityMin(securityClassificationCardinalityMin);
        securityClassificationAsbiePanel.setCardinalityMax(securityClassificationCardinalityMax);
        securityClassificationAsbiePanel.setRemark(securityClassificationRemark);
        securityClassificationAsbiePanel.setContextDefinition(securityClassificationContextDefinition);

        WebElement statusAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Status");
        EditBIEPage.ASBIEPanel statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        int statusCardinalityMin = nextInt(2, 5);
        int statusCardinalityMax = nextInt(5, 10);
        String statusRemark = "remark_" + randomAlphanumeric(5, 10);
        String statusContextDefinition = randomPrint(50, 100).trim();

        statusAsbiePanel.toggleUsed();
        statusAsbiePanel.setCardinalityMin(statusCardinalityMin);
        statusAsbiePanel.setCardinalityMax(statusCardinalityMax);
        statusAsbiePanel.setRemark(statusRemark);
        statusAsbiePanel.setContextDefinition(statusContextDefinition);

        WebElement effectivityAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Effectivity");
        EditBIEPage.ASBIEPanel effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        int effectivityCardinalityMin = nextInt(2, 5);
        int effectivityCardinalityMax = nextInt(5, 10);
        String effectivityRemark = "remark_" + randomAlphanumeric(5, 10);
        String effectivityContextDefinition = randomPrint(50, 100).trim();

        effectivityAsbiePanel.toggleUsed();
        effectivityAsbiePanel.setCardinalityMin(effectivityCardinalityMin);
        effectivityAsbiePanel.setCardinalityMax(effectivityCardinalityMax);
        effectivityAsbiePanel.setRemark(effectivityRemark);
        effectivityAsbiePanel.setContextDefinition(effectivityContextDefinition);

        editBIEPage.hitUpdateButton();

        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setBusinessContext(randomBusinessContext.getName());
        viewEditBIEPage.setOwner(endUser.getLoginId());
        viewEditBIEPage.setDEN(asccp.getDen());
        viewEditBIEPage.hitSearchButton();

        WebElement tr = viewEditBIEPage.getTableRecordByValue(asccp.getDen());
        viewEditBIEPage.hitCreateInheritedBIE(tr);
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

        assertEquals(businessTerm, getText(topLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(remark, getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals(version, getText(topLevelASBIEPPanel.getVersionField()));
        assertEquals(status, getText(topLevelASBIEPPanel.getStatusField()));
        assertEquals(contextDefinition, getText(topLevelASBIEPPanel.getContextDefinitionField()));

        EditBIEPage.TopLevelASBIEPPanel baseTopLevelASBIEPPanel = topLevelASBIEPPanel.getBaseTopLevelASBIEPPanel();
        assertDisabled(baseTopLevelASBIEPPanel.getBusinessTermField());
        assertDisabled(baseTopLevelASBIEPPanel.getRemarkField());
        assertDisabled(baseTopLevelASBIEPPanel.getVersionField());
        assertDisabled(baseTopLevelASBIEPPanel.getStatusField());
        assertDisabled(baseTopLevelASBIEPPanel.getContextDefinitionField());

        assertEquals(businessTerm, getText(baseTopLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(remark, getText(baseTopLevelASBIEPPanel.getRemarkField()));
        assertEquals(version, getText(baseTopLevelASBIEPPanel.getVersionField()));
        assertEquals(status, getText(baseTopLevelASBIEPPanel.getStatusField()));
        assertEquals(contextDefinition, getText(baseTopLevelASBIEPPanel.getContextDefinitionField()));

        securityClassificationAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Security Classification");
        securityClassificationAsbiePanel = editBIEPage.getASBIEPanel(securityClassificationAsbieNode);

        assertEnabled(securityClassificationAsbiePanel.getUsedCheckbox());
        assertEnabled(securityClassificationAsbiePanel.getCardinalityMinField());
        assertEnabled(securityClassificationAsbiePanel.getCardinalityMaxField());
        assertEnabled(securityClassificationAsbiePanel.getRemarkField());
        assertEnabled(securityClassificationAsbiePanel.getContextDefinitionField());

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

        statusAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Status");
        statusAsbiePanel = editBIEPage.getASBIEPanel(statusAsbieNode);

        assertEnabled(statusAsbiePanel.getUsedCheckbox());
        assertEnabled(statusAsbiePanel.getCardinalityMinField());
        assertEnabled(statusAsbiePanel.getCardinalityMaxField());
        assertEnabled(statusAsbiePanel.getRemarkField());
        assertEnabled(statusAsbiePanel.getContextDefinitionField());

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

        effectivityAsbieNode = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Effectivity");
        effectivityAsbiePanel = editBIEPage.getASBIEPanel(effectivityAsbieNode);

        assertEnabled(effectivityAsbiePanel.getUsedCheckbox());
        assertEnabled(effectivityAsbiePanel.getCardinalityMinField());
        assertEnabled(effectivityAsbiePanel.getCardinalityMaxField());
        assertEnabled(effectivityAsbiePanel.getRemarkField());
        assertEnabled(effectivityAsbiePanel.getContextDefinitionField());

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

}
