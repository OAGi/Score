package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.base.Response;

public class GetCcPackageResponse extends Response {

    private final CcPackage ccPackage;

    public GetCcPackageResponse(CcPackage ccPackage) {
        this.ccPackage = ccPackage;
    }

    public CcPackage getCcPackage() {
        return ccPackage;
    }
}
