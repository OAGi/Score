package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class CreateExtensionResponse {
    public boolean canEdit;
    public boolean canView;
    public long extensionId;
}
