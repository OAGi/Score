package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.*;

public interface OasDocWriteRepository {
    CreateOasDocResponse createOasDoc(CreateOasDocRequest request) throws ScoreDataAccessException;

    UpdateOasDocResponse updateOasDoc(UpdateOasDocRequest request) throws ScoreDataAccessException;

    DeleteOasDocResponse deleteOasDoc(DeleteOasDocRequest request) throws ScoreDataAccessException;
}
