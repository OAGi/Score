package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_1_BusinessContextsOfTheTenantsOfAUser;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_2_TenantAdministration;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_3_VisibilityFollowsTenantAssociations;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_4_BIEVisibilityByTenant;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_5_BIEOwnershipTransferByTenant;
import org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility.TC_49_6_TenantManagement;

@Suite
@SuiteDisplayName("Test Suite 49")
@SelectClasses({
        TC_49_1_BusinessContextsOfTheTenantsOfAUser.class,
        TC_49_2_TenantAdministration.class,
        TC_49_3_VisibilityFollowsTenantAssociations.class,
        TC_49_4_BIEVisibilityByTenant.class,
        TC_49_5_BIEOwnershipTransferByTenant.class,
        TC_49_6_TenantManagement.class
})
public class TS_49 {
}
