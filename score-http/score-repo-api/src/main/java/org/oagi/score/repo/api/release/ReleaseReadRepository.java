package org.oagi.score.repo.api.release;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.release.model.GetReleaseRequest;
import org.oagi.score.repo.api.release.model.GetReleaseResponse;

public interface ReleaseReadRepository {

    GetReleaseResponse getRelease(GetReleaseRequest request) throws ScoreDataAccessException;

}
