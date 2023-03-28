package org.oagi.score.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.redis.event.Event;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSetReleaseValidationRequestEvent implements Event {

    private BigInteger userId;
    private BigInteger moduleSetReleaseId;
    private String requestId;
    private File baseDirectory;
    private List<File> schemaFiles;

}
