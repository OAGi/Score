package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.*;

/** OpenAPI 3.1: the Schema Object is JSON Schema 2020-12. */
public class BieOpenApi31Dialect implements BieOpenApiDialect {

    public static final BieOpenApi31Dialect INSTANCE = new BieOpenApi31Dialect();

    @Override
    public String openApiVersion() {
        // Issue #1760: pin the canonical patch release (errata-only) of the minor family.
        return "3.1.2";
    }

    @Override
    public boolean allowsDeleteRequestBody() {
        return true;
    }

    @Override
    public String xbtOpenApiMap(XbtSummaryRecord xbt) {
        return xbt.openApi31Map();
    }

    @Override
    public Map<String, Object> nullable(Map<String, Object> schema, boolean isNillable) {
        if (isNillable) {
            Object type = schema.get("type");
            if (type instanceof String) {
                if (!"null".equals(type)) {
                    schema.put("type", Arrays.asList(type, "null"));
                }
                return schema;
            }
            if (type instanceof Collection) {
                List<Object> typeList = new ArrayList<>((Collection) type);
                if (!typeList.contains("null")) {
                    typeList.add("null");
                }
                schema.put("type", typeList);
                return schema;
            }

            // Fallback for schemas without explicit 'type' (e.g. allOf/$ref).
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("anyOf", Arrays.asList(
                    schema,
                    ImmutableMap.<String, Object>builder()
                            .put("type", "null")
                            .build()
            ));
            return prop;
        }
        return schema;
    }

    @Override
    public void fixedValue(Map<String, Object> schema, String fixedValue) {
        schema.put("const", fixedValue);
    }

    @Override
    public void example(Map<String, Object> schema, Object example) {
        schema.put("examples", Arrays.asList(example));
    }

    @Override
    public List<String> scContentCopyKeys() {
        return Arrays.asList("description", "const", "default", "examples");
    }
}
