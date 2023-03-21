package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.oagi.score.e2e.api.AgencyIDListAPI;

public class DSLContextAgencyIDListAPIImpl implements AgencyIDListAPI {

    private final DSLContext dslContext;

    public DSLContextAgencyIDListAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

}
