package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPCreateDialog;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_2_CreatingBrandNewDeveloperACC extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_10_2_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCViewEditPage accCreatePage = viewEditCoreComponentPage.createACC(branch);
        String url = getDriver().getCurrentUrl();
        BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
        WebElement accNode = accCreatePage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accCreatePage.getACCPanel(accNode);
        assertEquals("ACC", getText(accPanel.getCoreComponentField()));
        assertEquals(branch, getText(accPanel.getReleaseField()));
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("WIP", getText(accPanel.getStateField()));
        assertDisabled(accPanel.getGUIDField());
        assertDisabled(accPanel.getDENField());
        String objectClassTermText = getText(accPanel.getObjectClassTermField());
        assertEquals(acc.getObjectClassTerm(), objectClassTermText);
        assertEquals("Semantics", getText(accPanel.getComponentTypeSelectField()));
        assertNotChecked(accPanel.getAbstractCheckbox());
        assertDisabled(accPanel.getDeprecatedCheckbox());

        String namespaceText = getText(accPanel.getNamespaceSelectField());
        assertEquals("Namespace", namespaceText);

        String definitionText = getText(accPanel.getDefinitionField());
        assertTrue(isEmpty(definitionText));

        String definitionSourceText = getText(accPanel.getDefinitionSourceField());
        assertTrue(isEmpty(definitionSourceText));
    }

    @Test
    public void test_TA_10_2_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCViewEditPage accCreatePage = viewEditCoreComponentPage.createACC(branch);
        String url = getDriver().getCurrentUrl();
        BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ACCObject acc = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
        WebElement accNode = accCreatePage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accCreatePage.getACCPanel(accNode);
        assertEquals("ACC", getText(accPanel.getCoreComponentField()));
        assertEquals(branch, getText(accPanel.getReleaseField()));
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("WIP", getText(accPanel.getStateField()));
        assertDisabled(accPanel.getGUIDField());
        assertDisabled(accPanel.getDENField());
        String objectClassTermText = getText(accPanel.getObjectClassTermField());
        assertEquals(acc.getObjectClassTerm(), objectClassTermText);
        assertEquals("Semantics", getText(accPanel.getComponentTypeSelectField()));
        assertNotChecked(accPanel.getAbstractCheckbox());
        assertDisabled(accPanel.getDeprecatedCheckbox());

        String namespaceText = getText(accPanel.getNamespaceSelectField());
        assertEquals("Namespace", namespaceText);

        String definitionText = getText(accPanel.getDefinitionField());
        assertTrue(isEmpty(definitionText));

        String definitionSourceText = getText(accPanel.getDefinitionSourceField());
        assertTrue(isEmpty(definitionSourceText));
    }

}
