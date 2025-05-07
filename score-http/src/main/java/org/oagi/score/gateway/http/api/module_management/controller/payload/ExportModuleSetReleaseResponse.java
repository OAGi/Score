package org.oagi.score.gateway.http.api.module_management.controller.payload;

import java.io.File;

public record ExportModuleSetReleaseResponse(String filename, File file) {

}
