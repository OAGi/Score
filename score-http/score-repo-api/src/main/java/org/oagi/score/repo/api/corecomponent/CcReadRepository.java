package org.oagi.score.repo.api.corecomponent;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.FindNextAsccpManifestRequest;
import org.oagi.score.repo.api.corecomponent.model.FindNextAsccpManifestResponse;
import org.oagi.score.repo.api.corecomponent.model.GetCcPackageRequest;
import org.oagi.score.repo.api.corecomponent.model.GetCcPackageResponse;

public interface CcReadRepository {

    GetCcPackageResponse getCcPackage(
            GetCcPackageRequest request) throws ScoreDataAccessException;

    FindNextAsccpManifestResponse findNextAsccpManifest(
            FindNextAsccpManifestRequest request) throws ScoreDataAccessException;
}
