package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.List;

@Data
public class BieTransferOwnershipListRequest {
    private String targetLoginId;
    private List<TopLevelAsbiepId> topLevelAsbiepIds;
}
