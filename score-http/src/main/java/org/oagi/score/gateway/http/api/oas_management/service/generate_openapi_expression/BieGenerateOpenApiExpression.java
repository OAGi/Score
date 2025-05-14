package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface BieGenerateOpenApiExpression {

    void reset() throws Exception;

    Map<String, Object> getSchemas();

    void generate(TopLevelAsbiepSummaryRecord topLevelAsbiep);

    void generate(OpenAPITemplateForVerbOption template);

    File asFile(String filename) throws IOException;

}
