package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;

import java.util.Collection;

public interface OpenApiDocumentCommandRepository {

    void deleteMessageBodyList(Collection<OasMessageBodyId> oasMessageBodyIdList);

}
