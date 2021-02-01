package org.oagi.score.repo.api.release.model;

import org.oagi.score.repo.api.base.Response;

public class GetReleaseResponse extends Response {

    private final Release release;

    public GetReleaseResponse(Release release) {
        this.release = release;
    }

    public Release getRelease() {
        return release;
    }

}
