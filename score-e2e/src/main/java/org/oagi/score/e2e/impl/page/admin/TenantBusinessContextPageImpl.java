package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.TenantBusinessContextPage;

public class TenantBusinessContextPageImpl extends AbstractTenantAssociationPageImpl implements TenantBusinessContextPage {

    public TenantBusinessContextPageImpl(BasePage parent) {
        super(parent, "/tenant/bis-ctx/", "Add Business Context");
    }

    @Override
    protected String getKeyColumnClass() {
        return "mat-column-name";
    }

}
