package org.oagi.score.gateway.http.api.oas_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.util.*;

@Data
public class OpenAPIGenerateExpressionOption {

    private OasDoc oasDoc;
    private OpenAPIExpressionFormat openAPIExpressionFormat = OpenAPIExpressionFormat.YAML;
    private boolean bieDefinition;
    private String scheme;
    private String host;

    private Set<TopLevelAsbiepId> topLevelAsbiepIdSet = new LinkedHashSet<>();
    private List<OpenAPITemplateForVerbOption> openAPI30TemplateList = new ArrayList<>();

    public void addTemplate(OpenAPITemplateForVerbOption openAPITemplate) {
        topLevelAsbiepIdSet.add(openAPITemplate.getTopLevelAsbiepId());
        openAPI30TemplateList.add(openAPITemplate);
    }

    public Collection<TopLevelAsbiepId> getTopLevelAsbiepIdSet() {
        return topLevelAsbiepIdSet;
    }

    public Collection<OpenAPITemplateForVerbOption> getTemplates() {
        return openAPI30TemplateList;
    }

    public boolean hasSchemaName(String schemaName) {
        return this.openAPI30TemplateList.stream().filter(e -> StringUtils.equals(e.getSchemaName(), schemaName)).count() > 0;
    }

}

