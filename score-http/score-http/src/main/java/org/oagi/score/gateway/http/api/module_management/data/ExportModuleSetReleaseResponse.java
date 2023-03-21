package org.oagi.score.gateway.http.api.module_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class ExportModuleSetReleaseResponse {

    private String filename;

    private File file;

}
