package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

@Data
public class CreateExtensionResponse {
    public boolean canEdit;
    public boolean canView;
    public AccManifestId extensionId;
}
