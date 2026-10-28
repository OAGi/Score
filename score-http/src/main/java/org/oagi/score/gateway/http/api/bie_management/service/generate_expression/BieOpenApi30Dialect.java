package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** OpenAPI 3.0: the OpenAPI Schema Object (a JSON Schema subset with {@code nullable}). */
public class BieOpenApi30Dialect implements BieOpenApiDialect {

    public static final BieOpenApi30Dialect INSTANCE = new BieOpenApi30Dialect();

    @Override
    public String openApiVersion() {
        // Issue #1760: pin the canonical patch release (errata-only) of the minor family.
        return "3.0.4";
    }

    @Override
    public boolean allowsDeleteRequestBody() {
        return false;
    }

    @Override
    public String xbtOpenApiMap(XbtSummaryRecord xbt) {
        return xbt.openApi30Map();
    }

    @Override
    public Map<String, Object> nullable(Map<String, Object> schema, boolean isNillable) {
        if (isNillable) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("oneOf", Arrays.asList(
                    ImmutableMap.builder()
                            .put("nullable", true)
                            .build(),
                    schema
            ));
            return prop;
        }
        return schema;
    }

    @Override
    public void fixedValue(Map<String, Object> schema, String fixedValue) {
        schema.put("enum", Arrays.asList(fixedValue));
    }

    @Override
    public void example(Map<String, Object> schema, Object example) {
        schema.put("example", example);
    }

    @Override
    public List<String> scContentCopyKeys() {
        return Arrays.asList("description", "enum", "default", "example");
    }
}
