package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.openapidoc.BieForOasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.OasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocListRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocListResponse;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocResponse;

public class JooqBieForOasDocReadRepository extends JooqScoreRepository
        implements BieForOasDocReadRepository {
    public JooqBieForOasDocReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public GetBieForOasDocListResponse getBieForOasDocList(GetBieForOasDocListRequest request) throws ScoreDataAccessException {
        return null;
    }
}
