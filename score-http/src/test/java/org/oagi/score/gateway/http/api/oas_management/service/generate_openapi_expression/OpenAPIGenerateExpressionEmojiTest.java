package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApi30Dialect;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApi31Dialect;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApiDialect;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1892: the text of an OpenAPI Document may hold any Unicode character, emoji (outside the Basic Multilingual
 * Plane, so two UTF-16 units and four UTF-8 bytes) included. The generated JSON and YAML must carry them unchanged,
 * and the file must be UTF-8, whatever the platform's default charset is. Documents of operations without a BIE are
 * generated here, which needs no database.
 */
class OpenAPIGenerateExpressionEmojiTest {

    private static final String SMILE = "😀";            // U+1F600
    private static final String FAMILY = "👨‍👩‍👧"; // ZWJ sequence
    private static final String FLAG = "🇰🇷";  // regional indicators
    private static final String MIXED = "Café 한글 " + SMILE + " " + FAMILY + " " + FLAG;

    private static final String SCHEME = "Auth.v1-b_2"; // ASCII only: the Components Object limits the key
    private static final String SCOPE = "read:pets"; // ASCII only: an OAuth 2.0 scope token
    private static final String PATH = "/pets" + SMILE + "/{id}";
    private static final String OPERATION_ID = "get" + SMILE;
    private static final String TAG = "tag " + FAMILY;

    private static OasSecurityScheme scheme() {
        OasOAuthScope scope = new OasOAuthScope();
        scope.setScopeName(SCOPE);
        scope.setDescription("Read " + MIXED);
        OasOAuthFlow flow = new OasOAuthFlow();
        flow.setFlowType("authorizationCode");
        flow.setAuthorizationUrl("https://example.com/authorize");
        flow.setTokenUrl("https://example.com/token");
        flow.setScopes(List.of(scope));
        OasSecurityScheme scheme = new OasSecurityScheme();
        scheme.setType("oauth2");
        scheme.setSchemeName(SCHEME);
        scheme.setDescription("Scheme " + MIXED);
        scheme.setFlows(List.of(flow));
        return scheme;
    }

    private static OpenAPITemplateForVerbOption template() {
        OpenAPITemplateForVerbOption template = new OpenAPITemplateForVerbOption(Operation.GET);
        template.setResourceName(PATH);
        template.setOperationId(OPERATION_ID);
        template.setOasOperationId(new OasOperationId(BigInteger.ONE));
        template.setTagName(TAG);
        template.setErrorResponseBodyType("NONE");
        return template;
    }

    private static File generate(BieOpenApiDialect dialect, OpenAPIExpressionFormat format) throws Exception {
        OasDoc oasDoc = new OasDoc();
        oasDoc.setTitle("Title " + MIXED);
        oasDoc.setDescription("Description " + MIXED);
        oasDoc.setVersion("1.0");
        oasDoc.setContactName("Contact " + MIXED);
        oasDoc.setLicenseName("License " + MIXED);
        oasDoc.setOpenAPIVersion(dialect.openApiVersion());
        oasDoc.setSecuritySchemes(List.of(scheme()));

        OpenAPIGenerateExpressionOption option = new OpenAPIGenerateExpressionOption();
        option.setOasDoc(oasDoc);
        option.setOpenAPIExpressionFormat(format);
        option.addTemplate(template());

        OpenAPIGenerateExpression expression = new OpenAPIGenerateExpression(null, option, dialect);
        expression.afterPropertiesSet();
        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            expression.generate(template);
        }
        expression.generateErrorResponses();
        expression.generateHttpHeaders();
        return expression.asFile("emoji-test");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> at(Object node, String... keys) {
        Object current = node;
        for (String key : keys) {
            current = ((Map<String, Object>) current).get(key);
        }
        return (Map<String, Object>) current;
    }

    private void assertDocument(BieOpenApiDialect dialect, OpenAPIExpressionFormat format, ObjectMapper reader)
            throws Exception {
        File file = generate(dialect, format);
        try {
            String label = dialect.openApiVersion() + " " + format;
            // The file is read as UTF-8 on purpose: the generated file must not depend on the default charset.
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Map<String, Object> doc = reader.readValue(text, Map.class);

            assertEquals("Title " + MIXED, at(doc, "info").get("title"), label);
            assertEquals("Description " + MIXED, at(doc, "info").get("description"), label);
            assertEquals("Contact " + MIXED, at(doc, "info", "contact").get("name"), label);
            assertEquals("License " + MIXED, at(doc, "info", "license").get("name"), label);

            assertTrue(at(doc, "paths").containsKey(PATH), label + " path " + at(doc, "paths").keySet());
            Map<String, Object> operation = at(doc, "paths", PATH, "get");
            assertEquals(OPERATION_ID, operation.get("operationId"), label);
            assertEquals(List.of(TAG), operation.get("tags"), label);

            Map<String, Object> schemes = at(doc, "components", "securitySchemes");
            assertTrue(schemes.containsKey(SCHEME), label + " scheme " + schemes.keySet());
            assertEquals("Scheme " + MIXED, at(schemes, SCHEME).get("description"), label);
            Map<String, Object> scopes = at(schemes, SCHEME, "flows", "authorizationCode", "scopes");
            assertEquals("Read " + MIXED, scopes.get(SCOPE), label);

            // The emoji are written as they are, not as escape sequences (😀, \U0001F600).
            assertTrue(text.contains(MIXED), label + ": the characters are escaped in the file");
        } finally {
            file.delete();
        }
    }

    @Test
    void jsonCarriesEmojiInBothVersions() throws Exception {
        for (BieOpenApiDialect dialect : List.of(BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE)) {
            assertDocument(dialect, OpenAPIExpressionFormat.JSON, JsonMapper.builder().build());
        }
    }

    @Test
    void yamlCarriesEmojiInBothVersions() throws Exception {
        for (BieOpenApiDialect dialect : List.of(BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE)) {
            assertDocument(dialect, OpenAPIExpressionFormat.YAML, YAMLMapper.builder().build());
        }
    }
}
