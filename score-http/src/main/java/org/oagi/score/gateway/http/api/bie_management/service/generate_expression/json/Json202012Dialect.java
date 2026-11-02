package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.json;

import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/** JSON Schema 2020-12. */
public class Json202012Dialect implements BieJsonSchemaDialect {

    public static final Json202012Dialect INSTANCE = new Json202012Dialect();

    @Override
    public String schemaUri() {
        return "https://json-schema.org/draft/2020-12/schema";
    }

    @Override
    public String definitionsKeyword() {
        return "$defs";
    }

    @Override
    public String idKeyword() {
        return "$id";
    }

    @Override
    public boolean supportsSeparateSchemaFiles() {
        return true;
    }

    @Override
    public String xbtJsonSchemaMap(XbtSummaryRecord xbt) {
        return xbt.jbt202012Map();
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

            // Fallback for schemas without explicit 'type' (e.g. $ref with siblings).
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("anyOf", Arrays.asList(schema, Map.of("type", "null")));
            return prop;
        }

        return schema;
    }

    @Override
    public void fixedValue(Map<String, Object> schema, String fixedValue) {
        schema.put("const", fixedValue);
    }

    @Override
    public Map<String, Object> withReference(Map<String, Object> schema, String ref) {
        LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
        ordered.put("$ref", ref);
        for (Map.Entry<String, Object> entry : schema.entrySet()) {
            if ("$ref".equals(entry.getKey())) {
                continue;
            }
            ordered.put(entry.getKey(), entry.getValue());
        }
        return ordered;
    }

    @Override
    public void adaptToReference(Map<String, Object> schema, String ref, Map<String, Object> definitions) {
        String prefix = definitionRef("");
        if (schema == null || ref == null || !ref.startsWith(prefix)) {
            return;
        }

        Object examplesObject = schema.get("examples");
        if (!(examplesObject instanceof List<?>)) {
            return;
        }

        String definitionName = ref.substring(prefix.length());
        Object definitionObject = definitions.get(definitionName);
        if (!(definitionObject instanceof Map<?, ?>)) {
            return;
        }

        Object typeObject = ((Map<?, ?>) definitionObject).get("type");
        if (!(typeObject instanceof String)) {
            return;
        }
        String type = (String) typeObject;
        if (!"boolean".equals(type) && !"number".equals(type) && !"integer".equals(type)) {
            return;
        }

        List<Object> normalizedExamples = new ArrayList<>();
        boolean changed = false;
        for (Object example : (List<?>) examplesObject) {
            Object normalized = normalizeExampleByType(example, type);
            normalizedExamples.add(normalized);
            if (!Objects.equals(example, normalized)) {
                changed = true;
            }
        }

        if (changed) {
            schema.put("examples", normalizedExamples);
        }
    }

    private Object normalizeExampleByType(Object example, String type) {
        if (!(example instanceof String)) {
            return example;
        }

        String trimmed = ((String) example).trim();
        if (!StringUtils.hasLength(trimmed)) {
            return example;
        }

        if ("boolean".equals(type)) {
            if ("true".equalsIgnoreCase(trimmed)) {
                return true;
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return false;
            }
            return example;
        }

        try {
            if ("integer".equals(type)) {
                return new BigInteger(trimmed);
            }
            if ("number".equals(type)) {
                return new BigDecimal(trimmed);
            }
        } catch (NumberFormatException ignored) {
            return example;
        }

        return example;
    }

    @Override
    public List<String> scContentCopyKeys() {
        return Arrays.asList("const", "default", "examples");
    }
}
