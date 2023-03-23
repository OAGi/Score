package org.oagi.score.gateway.http.api.release_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class GenerateMigrationScriptResponse {

    private String filename;

    private File file;

}
