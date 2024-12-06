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
import org.oagi.score.e2e.page.bie.SelectBaseProfileBIEDialog;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_44_2_UseBaseBIE extends BaseTest {

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
    @DisplayName("TC_44_2_1")
    public void enduser_use_base_BIE() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

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
        TopLevelASBIEPObject anotherBIE = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(randomBusinessContext),
                        getAPIFactory().getCoreComponentAPI()
                                .getASCCPByDENAndReleaseNum(asccp.getDen(), release.getReleaseNumber()),
                        anotherEndUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage =
                homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBIE);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();

        String baseBusinessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        String baseRemark = "remark_" + randomAlphanumeric(5, 10);
        String baseVersion = "version_" + randomAlphanumeric(5, 10);
        String baseStatus = "status_" + randomAlphanumeric(5, 10);
        String baseContextDefinition = randomPrint(50, 100).trim();

        topLevelASBIEPPanel.setBusinessTerm(baseBusinessTerm);
        topLevelASBIEPPanel.setRemark(baseRemark);
        topLevelASBIEPPanel.setVersion(baseVersion);
        topLevelASBIEPPanel.setStatus(baseStatus);
        topLevelASBIEPPanel.setContextDefinition(baseContextDefinition);

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

        homePage.logout();
        waitFor(ofMillis(1000L));

        homePage = loginPage().signIn(anotherEndUser.getLoginId(), anotherEndUser.getPassword());
        editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(anotherBIE);
        SelectBaseProfileBIEDialog selectBaseProfileBIEDialog = editBIEPage.openUseBaseBIEDialog();
        selectBaseProfileBIEDialog.selectBaseBIE(baseBIE);

        topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEnabled(topLevelASBIEPPanel.getBusinessTermField());
        assertEnabled(topLevelASBIEPPanel.getRemarkField());
        assertEnabled(topLevelASBIEPPanel.getVersionField());
        assertEnabled(topLevelASBIEPPanel.getStatusField());
        assertEnabled(topLevelASBIEPPanel.getContextDefinitionField());

        // Existing attributes are not propagated from the base BIE.
        // assertEquals(anotherBIE.getBusinessTerm(), getText(topLevelASBIEPPanel.getBusinessTermField()));
        // assertEquals(anotherBIE.getRemark(), getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals(anotherBIE.getVersion(), getText(topLevelASBIEPPanel.getVersionField()));
        assertEquals(anotherBIE.getStatus(), getText(topLevelASBIEPPanel.getStatusField()));
        // assertEquals(anotherBIE.getContextDefinition(), getText(topLevelASBIEPPanel.getContextDefinitionField()));

        EditBIEPage.TopLevelASBIEPPanel baseTopLevelASBIEPPanel = topLevelASBIEPPanel.getBaseTopLevelASBIEPPanel();
        assertDisabled(baseTopLevelASBIEPPanel.getBusinessTermField());
        assertDisabled(baseTopLevelASBIEPPanel.getRemarkField());
        assertDisabled(baseTopLevelASBIEPPanel.getVersionField());
        assertDisabled(baseTopLevelASBIEPPanel.getStatusField());
        assertDisabled(baseTopLevelASBIEPPanel.getContextDefinitionField());

        assertEquals(baseBusinessTerm, getText(baseTopLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(baseRemark, getText(baseTopLevelASBIEPPanel.getRemarkField()));
        assertEquals(baseVersion, getText(baseTopLevelASBIEPPanel.getVersionField()));
        assertEquals(baseStatus, getText(baseTopLevelASBIEPPanel.getStatusField()));
        assertEquals(baseContextDefinition, getText(baseTopLevelASBIEPPanel.getContextDefinitionField()));

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
