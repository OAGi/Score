package org.oagi.score.gateway.http.common.model;

import java.io.File;

public record ExportStandaloneSchemaResponse(String filename, File file) {

}
