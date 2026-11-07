package org.oagi.score.gateway.http.api.oas_management.service;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.dialect.Dialects;
import org.oagi.score.gateway.http.api.oas_management.model.OasJson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Issue #1791: checks an OpenAPI document against the JSON Schema that the OpenAPI Initiative publishes for its version
 * (3.0.4 or 3.1.2), which is kept in the resources and never loaded from the network. The schema of 3.1 is the one that
 * validates the Schema Objects with the OpenAPI dialect. What a JSON Schema cannot say (a path parameter has to occur in
 * its path, for one) is not checked here.
 */
public final class OasSpecValidator {

    /** The versions of OpenAPI a document can be of. */
    public enum Version {
        V3_0_4("3.0.4"), V3_1_2("3.1.2");

        private final String number;

        Version(String number) {
            this.number = number;
        }

        public String number() {
            return number;
        }

        /** The version of the {@code openapi} field or of the minor version a document is set to ("3.0", "3.1", "3.1.2"...). */
        public static Version of(String openApiVersion) {
            return openApiVersion != null && openApiVersion.trim().startsWith("3.0") ? V3_0_4 : V3_1_2;
        }
    }

    private static final String DIALECT_ID = "https://spec.openapis.org/oas/3.1/dialect/2024-11-10";
    private static final String META_ID = "https://spec.openapis.org/oas/3.1/meta/2024-11-10";
    private static final String SCHEMA_ID = "https://spec.openapis.org/oas/3.1/schema/2025-09-15";
    private static final String SCHEMA_BASE_ID = "https://spec.openapis.org/oas/3.1/schema-base/2025-09-15";

    private OasSpecValidator() {
    }

    private static final class Holder {
        static final Schema V3_0 = load30();
        static final Schema V3_1 = load31();

        private static Schema load30() {
            SchemaRegistry registry = SchemaRegistry.withDialect(Dialects.getDraft4());
            return registry.getSchema(read("oas-schemas/oas-3.0.4-schema.json"), InputFormat.JSON);
        }

        private static Schema load31() {
            Map<String, String> sources = Map.of(
                    DIALECT_ID, read("oas-schemas/oas-3.1-dialect.json"),
                    META_ID, read("oas-schemas/oas-3.1-meta.json"),
                    SCHEMA_ID, read("oas-schemas/oas-3.1.2-schema.json"),
                    SCHEMA_BASE_ID, read("oas-schemas/oas-3.1.2-schema-base.json"));
            SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                    builder -> builder.schemas(sources));
            return registry.getSchema(sources.get(SCHEMA_BASE_ID), InputFormat.JSON);
        }
    }

    /**
     * The places where the document breaks the specification, each as "location: message"; empty when the document is
     * valid. {@code document} is the document as maps and lists.
     */
    public static List<String> validate(Version version, Map<String, Object> document) {
        Schema schema = version == Version.V3_0_4 ? Holder.V3_0 : Holder.V3_1;
        List<String> problems = new ArrayList<>();
        for (Error error : schema.validate(OasJson.toJson(document), InputFormat.JSON)) {
            problems.add(error.getInstanceLocation() + ": " + error.getMessage());
        }
        return problems;
    }

    private static String read(String resource) {
        try (InputStream in = OasSpecValidator.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
