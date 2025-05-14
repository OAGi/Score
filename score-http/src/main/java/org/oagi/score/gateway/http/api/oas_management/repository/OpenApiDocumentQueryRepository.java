package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;
import org.oagi.score.gateway.http.api.oas_management.model.OasRequestId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResponseId;

import java.util.Collection;
import java.util.List;

public interface OpenApiDocumentQueryRepository {

    List<OasMessageBodyId> getOasMessageBodyIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    List<OasRequestId> getOasRequestIdList(Collection<OasMessageBodyId> oasMessageBodyIdList);

    List<OasResponseId> getOasResponseIdList(Collection<OasMessageBodyId> oasMessageBodyIdList);

    boolean hasTopLevelAsbiepReference(TopLevelAsbiepId topLevelAsbiepId);

}
