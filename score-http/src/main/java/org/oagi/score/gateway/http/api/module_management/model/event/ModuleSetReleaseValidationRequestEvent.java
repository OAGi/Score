package org.oagi.score.gateway.http.api.module_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.common.model.event.Event;

import java.io.File;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSetReleaseValidationRequestEvent implements Event {

    private UserId userId;
    private ModuleSetReleaseId moduleSetReleaseId;
    private String requestId;
    private File baseDirectory;
    private List<File> schemaFiles;

}
