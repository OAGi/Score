package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.TenantUserPage;

public class TenantUserPageImpl extends AbstractTenantAssociationPageImpl implements TenantUserPage {

    public TenantUserPageImpl(BasePage parent) {
        super(parent, "/tenant/users/", "Add User");
    }

    @Override
    protected String getKeyColumnClass() {
        return "mat-column-loginId";
    }

}
