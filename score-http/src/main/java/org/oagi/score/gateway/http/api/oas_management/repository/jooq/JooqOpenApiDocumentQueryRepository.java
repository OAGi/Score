package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;
import org.oagi.score.gateway.http.api.oas_management.model.OasRequestId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResponseId;
import org.oagi.score.gateway.http.api.oas_management.repository.OpenApiDocumentQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqOpenApiDocumentQueryRepository extends JooqBaseRepository implements OpenApiDocumentQueryRepository {

    public JooqOpenApiDocumentQueryRepository(DSLContext dslContext, ScoreUser requester,
                                              RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<OasMessageBodyId> getOasMessageBodyIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        return dslContext().selectDistinct(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .from(OAS_MESSAGE_BODY)
                .where(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .fetch(record -> new OasMessageBodyId(record.get(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID).toBigInteger()));
    }

    @Override
    public List<OasRequestId> getOasRequestIdList(Collection<OasMessageBodyId> oasMessageBodyIdList) {
        return dslContext().selectDistinct(OAS_REQUEST.OAS_REQUEST_ID)
                .from(OAS_REQUEST)
                .where(OAS_REQUEST.OAS_MESSAGE_BODY_ID.in(valueOf(oasMessageBodyIdList)))
                .fetch(record -> new OasRequestId(record.get(OAS_REQUEST.OAS_REQUEST_ID).toBigInteger()));
    }

    @Override
    public List<OasResponseId> getOasResponseIdList(Collection<OasMessageBodyId> oasMessageBodyIdList) {
        return dslContext().selectDistinct(OAS_RESPONSE.OAS_RESPONSE_ID)
                .from(OAS_RESPONSE)
                .where(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.in(valueOf(oasMessageBodyIdList)))
                .fetch(record -> new OasResponseId(record.get(OAS_RESPONSE.OAS_RESPONSE_ID).toBigInteger()));
    }

    @Override
    public boolean hasTopLevelAsbiepReference(TopLevelAsbiepId topLevelAsbiepId) {
        return hasOasRequest(topLevelAsbiepId) || hasOasResponse(topLevelAsbiepId);
    }

    private boolean hasOasRequest(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().selectCount()
                .from(OAS_REQUEST)
                .join(OAS_MESSAGE_BODY).on(OAS_REQUEST.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .where(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    private boolean hasOasResponse(TopLevelAsbiepId topLevelAsbiepId) {
        return dslContext().selectCount()
                .from(OAS_RESPONSE)
                .join(OAS_MESSAGE_BODY).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .where(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

}
