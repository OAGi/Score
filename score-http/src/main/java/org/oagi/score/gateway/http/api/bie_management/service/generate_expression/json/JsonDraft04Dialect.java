package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.json;

import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JSON Schema Draft-04: no sibling keywords next to a $ref, no {@code const}, no type union with null. */
public class JsonDraft04Dialect implements BieJsonSchemaDialect {

    public static final JsonDraft04Dialect INSTANCE = new JsonDraft04Dialect();

    @Override
    public String schemaUri() {
        return "http://json-schema.org/draft-04/schema#";
    }

    @Override
    public String definitionsKeyword() {
        return "definitions";
    }

    @Override
    public String xbtJsonSchemaMap(XbtSummaryRecord xbt) {
        return xbt.jbtDraft05Map();
    }

    /** The {@code allOf} wrapping is applied even when the schema is not nillable. */
    @Override
    public Map<String, Object> nullable(Map<String, Object> schema, boolean isNillable) {
        Map<String, Object> allOf = allOf(schema);
        if (isNillable) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("oneOf", Arrays.asList(Map.of("type", "null"), allOf));
            return prop;
        }

        return allOf;
    }

    /** Siblings of a {@code $ref} are ignored, so the reference moves into an {@code allOf} of its own. */
    private Map<String, Object> allOf(Map<String, Object> schema) {
        if (schema.containsKey("$ref") && schema.size() > 1) {
            Map<String, Object> prop = new LinkedHashMap<>();
            Map<String, Object> refMap = Map.of("$ref", schema.remove("$ref"));
            prop.put("allOf", (schema.isEmpty()) ? Arrays.asList(refMap) : Arrays.asList(refMap, schema));

            return prop;
        }

        return schema;
    }

    @Override
    public void fixedValue(Map<String, Object> schema, String fixedValue) {
        schema.put("enum", Arrays.asList(fixedValue));
    }

    @Override
    public Map<String, Object> withReference(Map<String, Object> schema, String ref) {
        schema.put("$ref", ref);
        return schema;
    }

    @Override
    public Map<String, Object> bbieSc(Map<String, Object> schema, String ref, BbieScSummaryRecord bbieSc) {
        return allOf(withReference(schema, ref));
    }

    @Override
    public List<String> scContentCopyKeys() {
        return Arrays.asList("enum", "default", "examples");
    }
}
