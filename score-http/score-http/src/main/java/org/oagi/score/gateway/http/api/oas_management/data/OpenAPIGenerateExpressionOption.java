package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.model.OasDoc;

import java.math.BigInteger;
import java.util.*;

@Data
public class OpenAPIGenerateExpressionOption {

    private OasDoc oasDoc;
    private OpenAPIExpressionFormat openAPIExpressionFormat = OpenAPIExpressionFormat.YAML;
    private boolean bieDefinition;
    private String scheme;
    private String host;

    private Set<BigInteger> topLevelAsbiepIdSet = new LinkedHashSet<>();
    private List<OpenAPITemplateForVerbOption> openAPI30TemplateList = new ArrayList<>();

    public void addTemplate(OpenAPITemplateForVerbOption openAPITemplate) {
        topLevelAsbiepIdSet.add(openAPITemplate.getTopLevelAsbiepId());
        openAPI30TemplateList.add(openAPITemplate);
    }

    public Collection<BigInteger> getTopLevelAsbiepIdSet() {
        return topLevelAsbiepIdSet;
    }

    public Collection<OpenAPITemplateForVerbOption> getTemplates() {
        return openAPI30TemplateList;
    }

    public boolean hasSchemaName(String schemaName) {
        return this.openAPI30TemplateList.stream().filter(e -> StringUtils.equals(e.getSchemaName(), schemaName)).count() > 0;
    }

}

