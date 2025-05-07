package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;
import org.oagi.score.gateway.http.api.oas_management.model.OasRequestId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResponseId;
import org.oagi.score.gateway.http.api.oas_management.repository.OpenApiDocumentCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OpenApiDocumentQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqOpenApiDocumentCommandRepository extends JooqBaseRepository implements OpenApiDocumentCommandRepository {

    private final OpenApiDocumentQueryRepository openApiDocumentQueryRepository;

    public JooqOpenApiDocumentCommandRepository(DSLContext dslContext, ScoreUser requester,
                                                RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);

        this.openApiDocumentQueryRepository = repositoryFactory.openApiDocumentQueryRepository(requester);
    }

    @Override
    public void deleteMessageBodyList(Collection<OasMessageBodyId> oasMessageBodyIdList) {
        if (oasMessageBodyIdList == null || oasMessageBodyIdList.isEmpty()) {
            return;
        }

        List<OasRequestId> oasRequestIdList = openApiDocumentQueryRepository.getOasRequestIdList(oasMessageBodyIdList);
        deleteOasRequestList(oasRequestIdList);

        List<OasResponseId> oasResponseIdList = openApiDocumentQueryRepository.getOasResponseIdList(oasMessageBodyIdList);
        deleteOasResponseList(oasResponseIdList);

        dslContext().deleteFrom(OAS_MESSAGE_BODY)
                .where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.in(valueOf(oasMessageBodyIdList)))
                .execute();
    }

    private void deleteOasRequestList(List<OasRequestId> oasRequestIdList) {
        if (oasRequestIdList == null || oasRequestIdList.isEmpty()) {
            return;
        }
        dslContext().deleteFrom(OAS_REQUEST_PARAMETER)
                .where(OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(valueOf(oasRequestIdList)))
                .execute();
        dslContext().deleteFrom(OAS_REQUEST)
                .where(OAS_REQUEST.OAS_REQUEST_ID.in(valueOf(oasRequestIdList)))
                .execute();
    }

    private void deleteOasResponseList(List<OasResponseId> oasResponseIdList) {
        if (oasResponseIdList == null || oasResponseIdList.isEmpty()) {
            return;
        }
        dslContext().deleteFrom(OAS_RESPONSE_HEADERS)
                .where(OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(valueOf(oasResponseIdList)))
                .execute();
        dslContext().deleteFrom(OAS_RESPONSE)
                .where(OAS_RESPONSE.OAS_RESPONSE_ID.in(valueOf(oasResponseIdList)))
                .execute();
    }

}
