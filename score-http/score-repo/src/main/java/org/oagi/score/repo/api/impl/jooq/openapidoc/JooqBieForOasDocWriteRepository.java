package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateRequest;
import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateResponse;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.springframework.security.core.AuthenticatedPrincipal;

public class JooqBieForOasDocWriteRepository extends JooqScoreRepository
        implements BieForOasDocWriteRepository {
    public JooqBieForOasDocWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }


    @Override
    public AddBieForOasDocResponse assignBieForOasDoc(AuthenticatedPrincipal user, AddBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public UpdateBieForOasDocResponse updateBieForOasDoc(UpdateBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }
}
