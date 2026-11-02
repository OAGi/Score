package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.TransferBIEOwnershipDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the multi-tenant mode the owner of a BIE can transfer the BIE only to a user who belongs to a tenant that one of the
 * business contexts of the BIE is connected to, and not to the owner.
 */
public class TC_49_5_BIEOwnershipTransferByTenant extends AbstractTenantTest {

    private AppUserObject owner;
    private AppUserObject otherUserOfTenantA;
    private AppUserObject userOfTenantB;
    private AppUserObject userOfBothTenants;
    private AppUserObject userWithoutTenant;

    private BusinessContextObject contextOfTenantA;
    private BusinessContextObject contextOfTenantB;

    @BeforeAll
    void createTheTenants() {
        owner = endUser();
        otherUserOfTenantA = endUser();
        userOfTenantB = endUser();
        userOfBothTenants = endUser();
        userWithoutTenant = endUser();

        TenantObject tenantA = tenant();
        TenantObject tenantB = tenant();
        var tenantAPI = getAPIFactory().getTenantAPI();
        tenantAPI.addUserToTenant(owner, tenantA);
        tenantAPI.addUserToTenant(otherUserOfTenantA, tenantA);
        tenantAPI.addUserToTenant(userOfTenantB, tenantB);
        tenantAPI.addUserToTenant(userOfBothTenants, tenantA);
        tenantAPI.addUserToTenant(userOfBothTenants, tenantB);

        contextOfTenantA = businessContext(owner);
        contextOfTenantB = businessContext(owner);
        tenantAPI.addBusinessContextToTenant(contextOfTenantA, tenantA);
        tenantAPI.addBusinessContextToTenant(contextOfTenantB, tenantB);
    }

    /** The login IDs that the owner is offered to transfer the BIE to. Signs out. */
    private List<String> offeredToTransfer(TopLevelASBIEPObject bie) {
        HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        ViewEditBIEPage page = openBieListOfTheTests(homePage);
        TransferBIEOwnershipDialog dialog = page.openTransferBIEOwnershipDialog(page.getTableRecordByValue(bie.getPropertyTerm()));
        List<String> listed = dialog.getListedLoginIds();
        dialog.close();
        homePage.logout();
        return listed;
    }

    private static void assertOnceEach(Set<String> expected, List<String> listed) {
        assertEquals(expected, Set.copyOf(listed));
        assertEquals(expected.size(), listed.size(), "Each user is offered once: " + listed);
    }

    @Test
    @DisplayName("TC_49_5_TA_1: a BIE can be transferred only to the users of the tenants of its business context")
    public void bie_can_be_transferred_to_users_of_the_tenants_of_its_business_context() {
        TopLevelASBIEPObject bie = bie(owner, "WIP", contextOfTenantA);

        assertOnceEach(Set.of(otherUserOfTenantA.getLoginId(), userOfBothTenants.getLoginId()), offeredToTransfer(bie));
    }

    @Test
    @DisplayName("TC_49_5_TA_2: a BIE with business contexts of several tenants can be transferred to the users of all of them, each offered once")
    public void bie_with_business_contexts_of_two_tenants_can_be_transferred_to_users_of_both() {
        TopLevelASBIEPObject bie = bie(owner, "WIP", contextOfTenantA, contextOfTenantB);

        assertOnceEach(Set.of(otherUserOfTenantA.getLoginId(), userOfTenantB.getLoginId(), userOfBothTenants.getLoginId()),
                offeredToTransfer(bie));
    }

    @Test
    @DisplayName("TC_49_5_TA_3: the owner and a user of no tenant are not offered")
    public void owner_and_user_without_tenant_are_not_offered() {
        TopLevelASBIEPObject bie = bie(owner, "WIP", contextOfTenantA, contextOfTenantB);

        List<String> listed = offeredToTransfer(bie);

        assertEquals(false, listed.contains(owner.getLoginId()), "The owner is not offered");
        assertEquals(false, listed.contains(userWithoutTenant.getLoginId()), "A user of no tenant is not offered");
    }

    @Test
    @DisplayName("TC_49_5_TA_4: a transferred BIE belongs to the new owner")
    public void transferred_bie_belongs_to_the_new_owner() {
        TopLevelASBIEPObject bie = bie(owner, "WIP", contextOfTenantA);

        HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        ViewEditBIEPage page = openBieListOfTheTests(homePage);
        page.openTransferBIEOwnershipDialog(page.getTableRecordByValue(bie.getPropertyTerm()))
                .transfer(otherUserOfTenantA.getLoginId());
        homePage.logout();

        homePage = loginPage().signIn(otherUserOfTenantA.getLoginId(), otherUserOfTenantA.getPassword());
        page = openBieListOfTheTests(homePage);
        page.setOwner(otherUserOfTenantA.getLoginId());
        page.hitSearchButton();
        List<String> listed = page.getListedDENs();
        homePage.logout();

        assertEquals(1, listed.stream().filter(den -> den.contains(bie.getPropertyTerm())).count(),
                "The new owner has the BIE: " + listed);
    }

}
