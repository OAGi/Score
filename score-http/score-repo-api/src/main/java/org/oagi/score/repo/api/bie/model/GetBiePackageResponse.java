package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

public class GetBiePackageResponse extends Response {

    private final BiePackage biePackage;

    public GetBiePackageResponse(BiePackage biePackage) {
        this.biePackage = biePackage;
    }

    public BiePackage getBiePackage() {
        return biePackage;
    }
}
