package org.oagi.score.e2e.impl.api.jooq.entity;

import org.jooq.DSLContext;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.AssignedBusinessTermAPI;
import org.oagi.score.e2e.obj.ABIEObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.AssignedBusinessTermObject;
import org.oagi.score.e2e.obj.BusinessTermObject;

public class DSLContextAssignedBusinessTermAPIImpl implements AssignedBusinessTermAPI {
    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextAssignedBusinessTermAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public AssignedBusinessTermObject getAssignedBusinessTermByName(String businessTermName){
        return null;

    }

    @Override
    public AssignedBusinessTermObject[] getAssignedBusinessTermByBIE(String bieName){
        return null;
    }

    @Override
    public AssignedBusinessTermObject createRandomAssignedBusinessTerm(BusinessTermObject businessTerm, ABIEObject aBIE,
                                                                       AppUserObject creator){
        return null;
    }

}
