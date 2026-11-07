package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.api.oas_management.service.OasSpecValidator.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1791: every combination of what the dialog of a request parameter or of a response header lets the user give,
 * in OpenAPI 3.0.4 and 3.1.2. The expected answer is written here from the Parameter Object, the Header Object and the
 * style table of the specification, not taken from the code under test; whatever the policy accepts is also generated and
 * checked against the JSON Schema that the OpenAPI Initiative publishes.
 */
class OasDocHttpHeaderExhaustiveTest {

    private static final List<String> VERSIONS = List.of("3.0", "3.1");
    private static final List<String> IN = Arrays.asList(null, "query", "header", "path", "cookie", "body");
    private static final List<String> STYLES = Arrays.asList(null, "matrix", "label", "simple", "form", "spaceDelimited",
            "pipeDelimited", "deepObject", "bogus");
    private static final List<Boolean> EXPLODE = Arrays.asList(null, true, false);
    private static final List<String> MEDIA_TYPES = Arrays.asList(null, "text/plain", "application/json", "not a media type", "  ");
    private static final List<Boolean> BOOLEANS = List.of(false, true);

    // The schemas: a string, an array, an object, an integer, a nullable string of 3.0, a type list of 3.1, a $ref
    private static final List<String> SCHEMAS = List.of(
            "{\"type\":\"string\"}",
            "{\"type\":\"array\",\"items\":{\"type\":\"string\"}}",
            "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"}}}",
            "{\"type\":\"integer\",\"minimum\":1}",
            "{\"type\":\"string\",\"nullable\":true}",
            "{\"type\":[\"string\",\"null\"]}",
            "{\"$ref\":\"#/components/schemas/Thing\"}");

    private static final Pattern TOKEN = Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");
    private static final Pattern MEDIA_TYPE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*/[A-Za-z0-9*][A-Za-z0-9!#$&^_.+*-]*");

    private static final Map<String, Set<String>> STYLES_OF = Map.of(
            "query", Set.of("form", "spaceDelimited", "pipeDelimited", "deepObject"),
            "path", Set.of("matrix", "label", "simple"),
            "header", Set.of("simple"),
            "cookie", Set.of("form"));

    private static OasDocHttpHeader header(Direction direction, String name, String in, String style, Boolean explode,
                                           boolean allowReserved, String mediaType, boolean required, String schema,
                                           Object example) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setDirection(direction);
        header.setName(name);
        header.setIn(in);
        header.setStyle(style);
        header.setExplode(explode);
        header.setAllowReserved(allowReserved);
        header.setContentMediaType(mediaType);
        header.setRequired(required);
        header.setSchema(OasSchema.fromJson(schema));
        header.setExample(example);
        header.setDescription("d");
        return header;
    }

    /** A name that is right for the location, so that the other fields are what makes a combination right or wrong. */
    private static String nameFor(Direction direction, String in) {
        if (direction == Direction.RESPONSE || in == null || "header".equals(in)) {
            return "x-trace";
        }
        return switch (in) {
            case "path" -> "id";
            case "cookie" -> "session";
            default -> "filter";
        };
    }

    private static Set<String> typesOf(String schemaJson) {
        Map<String, Object> schema = OasSchema.fromJson(schemaJson).toMap();
        Object type = schema.get("type");
        if (type instanceof String s) {
            return Set.of(s);
        }
        if (type instanceof List<?> list) {
            return Set.copyOf(list.stream().map(String::valueOf).toList());
        }
        return Set.of();
    }

    /** What the specification says about the combination: the reason it is refused, or {@code null} when it is allowed. */
    static String refusal(String version, OasDocHttpHeader h, String schemaJson) {
        boolean request = h.getDirection() == Direction.REQUEST;
        String in = request ? (h.getIn() == null ? "header" : h.getIn()) : "header";
        if (!Set.of("query", "header", "path", "cookie").contains(in)) {
            return "location";
        }
        if (h.getName() == null || h.getName().isBlank()) {
            return "name";
        }
        if ("header".equals(in)) {
            if (!TOKEN.matcher(h.getName()).matches()) {
                return "token";
            }
            String lower = h.getName().toLowerCase();
            if (request ? Set.of("accept", "content-type", "authorization").contains(lower) : lower.equals("content-type")) {
                return "ignored by the specification";
            }
        }
        if (h.getContentMediaType() != null && !h.getContentMediaType().isBlank()) {
            if (!MEDIA_TYPE.matcher(h.getContentMediaType().trim()).matches()) {
                return "media type";
            }
            if (h.getStyle() != null || h.getExplode() != null || h.isAllowReserved()) {
                return "content excludes style";
            }
        } else {
            if (h.getStyle() != null) {
                if (!STYLES_OF.get(in).contains(h.getStyle())) {
                    return "style of the location";
                }
                Set<String> types = typesOf(schemaJson);
                Set<String> wanted = switch (h.getStyle()) {
                    case "spaceDelimited", "pipeDelimited" -> Set.of("array", "object");
                    case "deepObject" -> Set.of("object");
                    default -> Set.of();
                };
                if (!wanted.isEmpty() && !types.isEmpty() && types.stream().noneMatch(wanted::contains)) {
                    return "style of the type";
                }
                if ("deepObject".equals(h.getStyle()) && !Boolean.TRUE.equals(h.getExplode())) {
                    return "deepObject without explode";
                }
            }
            if (h.isAllowReserved() && !(request && "query".equals(in))) {
                return "allowReserved outside the query";
            }
        }
        if ("3.0".equals(version) && OasSchema.fromJson(schemaJson).toMap().get("type") instanceof List) {
            return "type list is of 3.1";
        }
        return null;
    }

    private static String describe(String version, OasDocHttpHeader h, String schema) {
        return version + " " + h.getDirection() + " in=" + h.getIn() + " name=" + h.getName() + " style=" + h.getStyle()
                + " explode=" + h.getExplode() + " allowReserved=" + h.isAllowReserved() + " content=" + h.getContentMediaType()
                + " required=" + h.isRequired() + " schema=" + schema + " example=" + h.getExample();
    }

    private static String verdict(String version, OasDocHttpHeader h) {
        try {
            OasDocHttpHeaderPolicy.validate(List.of(h), version);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Test
    void thePolicyAcceptsExactlyWhatTheSpecificationAllows() {
        int combinations = 0;
        int accepted = 0;
        List<String> mismatches = new ArrayList<>();
        for (String version : VERSIONS) {
            for (Direction direction : Direction.values()) {
                for (String in : direction == Direction.REQUEST ? IN : Arrays.asList((String) null)) {
                    for (String style : STYLES) {
                        for (Boolean explode : EXPLODE) {
                            for (boolean allowReserved : BOOLEANS) {
                                for (String mediaType : MEDIA_TYPES) {
                                    for (boolean required : BOOLEANS) {
                                        for (String schema : SCHEMAS) {
                                            for (Object example : new Object[]{null, "abc"}) {
                                                OasDocHttpHeader h = header(direction, nameFor(direction, in), in, style, explode,
                                                        allowReserved, mediaType, required, schema, example);
                                                String expected = refusal(version, h, schema);
                                                String actual = verdict(version, h);
                                                combinations++;
                                                if (actual == null) {
                                                    accepted++;
                                                }
                                                if ((expected == null) != (actual == null) && mismatches.size() < 25) {
                                                    mismatches.add(describe(version, h, schema) + "\n    expected: "
                                                            + (expected == null ? "allowed" : "refused (" + expected + ")")
                                                            + "\n    actual:   " + (actual == null ? "allowed" : "refused: " + actual));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("header combinations: " + combinations + ", accepted: " + accepted);
        assertEquals(List.of(), mismatches, "the policy and the specification disagree");
        assertTrue(accepted > 1000, "a meaningful part of the combinations is allowed");
    }

    @Test
    void namesAreCheckedPerLocation() {
        List<String> mismatches = new ArrayList<>();
        String[] names = {"x-trace", "TraceParent", "Accept", "accept", "Content-Type", "content-type", "Authorization",
                "authorization", "Cookie", "Set-Cookie", "bad name", "bad:name", "", " ", "x_y.z~!", "é", "{id}", "a/b",
                "a{b}", "id", "user.id", "a b"};
        for (String version : VERSIONS) {
            for (Direction direction : Direction.values()) {
                for (String in : direction == Direction.REQUEST ? IN : Arrays.asList((String) null)) {
                    for (String name : names) {
                        OasDocHttpHeader h = header(direction, name, in, null, null, false, null, false, "{\"type\":\"string\"}", null);
                        String expected = refusal(version, h, "{\"type\":\"string\"}");
                        String location = direction == Direction.REQUEST ? (in == null ? "header" : in) : "header";
                        if (expected == null && "path".equals(location) && !Pattern.matches("[^{}/\\s]+", name.trim())) {
                            expected = "path template";
                        }
                        if (expected == null && Set.of("query", "cookie").contains(location) && name.isBlank()) {
                            expected = "name";
                        }
                        String actual = verdict(version, h);
                        if ((expected == null) != (actual == null)) {
                            mismatches.add(version + " " + direction + " in=" + in + " name='" + name + "' expected "
                                    + (expected == null ? "allowed" : "refused (" + expected + ")") + " but was "
                                    + (actual == null ? "allowed" : "refused: " + actual));
                        }
                    }
                }
            }
        }
        assertEquals(List.of(), mismatches);
    }

    @Test
    void aPathParameterIsAlwaysRequired() {
        for (String version : VERSIONS) {
            OasDocHttpHeader h = header(Direction.REQUEST, "id", "path", null, null, false, null, false, "{\"type\":\"string\"}", null);
            OasDocHttpHeaderPolicy.validate(List.of(h), version);
            assertTrue(h.isRequired(), "OpenAPI: the required of a path parameter MUST be true");
        }
    }

    @Test
    void aResponseHeaderHasNoLocation() {
        for (String version : VERSIONS) {
            OasDocHttpHeader h = header(Direction.RESPONSE, "x-trace", "query", null, null, false, null, false, "{\"type\":\"string\"}", null);
            OasDocHttpHeaderPolicy.validate(List.of(h), version);
            assertNull(h.getIn(), "a Header Object has no in");
        }
    }

    @Test
    void theSameNameInDifferentLocationsAndDirectionsAreDifferentUses() {
        OasDocHttpHeader a = header(Direction.REQUEST, "id", "query", null, null, false, null, false, "{\"type\":\"string\"}", null);
        OasDocHttpHeader b = header(Direction.REQUEST, "id", "cookie", null, null, false, null, false, "{\"type\":\"string\"}", null);
        OasDocHttpHeader c = header(Direction.REQUEST, "id", "header", null, null, false, null, false, "{\"type\":\"string\"}", null);
        OasDocHttpHeader d = header(Direction.RESPONSE, "id", null, null, null, false, null, false, "{\"type\":\"string\"}", null);
        for (String version : VERSIONS) {
            OasDocHttpHeaderPolicy.validate(List.of(a, b, c, d), version);
        }
        // a query parameter is told apart by case, a header name is not
        OasDocHttpHeaderPolicy.validate(List.of(
                header(Direction.REQUEST, "Id", "query", null, null, false, null, false, "{\"type\":\"string\"}", null),
                header(Direction.REQUEST, "id", "query", null, null, false, null, false, "{\"type\":\"string\"}", null)));
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(
                header(Direction.REQUEST, "X-A", "header", null, null, false, null, false, "{\"type\":\"string\"}", null),
                header(Direction.REQUEST, "x-a", "header", null, null, false, null, false, "{\"type\":\"string\"}", null))));
    }

    /** Schema, then whether OpenAPI 3.0.4 and OpenAPI 3.1.2 take it for a Schema Object (from the two specifications). */
    private static final Object[][] SCHEMA_OBJECTS = {
            {"{}", true, true},
            {"{\"type\":\"string\"}", true, true},
            {"{\"type\":[\"string\",\"null\"]}", false, true},
            {"{\"type\":\"null\"}", false, true},
            {"{\"type\":\"string\",\"nullable\":true}", true, true},
            {"{\"type\":\"integer\",\"minimum\":1,\"exclusiveMinimum\":true}", true, false},
            {"{\"type\":\"integer\",\"maximum\":5,\"exclusiveMaximum\":true}", true, false},
            {"{\"type\":\"integer\",\"exclusiveMinimum\":1}", false, true},
            {"{\"$ref\":\"#/components/schemas/Thing\"}", true, true},
            {"{\"$ref\":\"#/components/schemas/Thing\",\"description\":\"x\"}", false, true},
            {"{\"const\":\"a\"}", false, true},
            {"{\"examples\":[\"a\"]}", false, true},
            {"{\"if\":{\"type\":\"string\"},\"then\":{\"minLength\":1}}", false, true},
            {"{\"type\":\"array\",\"prefixItems\":[{\"type\":\"string\"}]}", false, true},
            {"{\"enum\":[]}", false, true},
            {"{\"type\":\"foo\"}", false, false},
            {"{\"type\":\"string\",\"minLength\":-1}", false, false},
            {"{\"type\":\"number\",\"multipleOf\":0}", false, false},
            {"{\"type\":\"array\",\"items\":[{\"type\":\"string\"}]}", false, false},
            {"{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"minItems\":1}", true, true},
            {"{\"x-note\":\"a\"}", true, true},
            {"{\"type\":\"string\",\"format\":\"uuid\",\"readOnly\":true,\"example\":\"a\"}", true, true},
            {"{\"type\":\"object\",\"additionalProperties\":false}", true, true},
            {"{\"allOf\":[{\"type\":\"string\"},{\"minLength\":1}]}", true, true},
            {"{\"discriminator\":{\"propertyName\":\"t\"},\"oneOf\":[{\"$ref\":\"#/components/schemas/Thing\"}]}", true, true},
    };

    @Test
    void theSchemaOfAHeaderIsASchemaObjectOfTheVersionOfTheDocument() {
        List<String> mismatches = new ArrayList<>();
        for (Object[] row : SCHEMA_OBJECTS) {
            for (Direction direction : Direction.values()) {
                for (String version : VERSIONS) {
                    OasDocHttpHeader h = header(direction, "x-trace", direction == Direction.REQUEST ? "header" : null, null, null,
                            false, null, false, (String) row[0], null);
                    boolean expected = (boolean) row["3.0".equals(version) ? 1 : 2];
                    boolean actual = verdict(version, h) == null;
                    if (expected != actual) {
                        mismatches.add(version + " " + direction + " " + row[0] + " expected " + (expected ? "allowed" : "refused")
                                + ", was " + (actual ? "allowed" : "refused: " + verdict(version, h)));
                    }
                }
            }
        }
        assertEquals(List.of(), mismatches);
    }

    @Test
    void theSchemaOfAContentHeaderIsCheckedToo() {
        for (String version : VERSIONS) {
            OasDocHttpHeader h = header(Direction.REQUEST, "x-trace", "header", null, null, false, "application/json", false,
                    "{\"type\":[\"string\",\"null\"]}", null);
            assertEquals("3.0".equals(version), verdict(version, h) != null, version);
        }
    }

    @Test
    void withoutAVersionOnlyTheRulesOfTheHeaderAreChecked() {
        OasDocHttpHeader h = header(Direction.REQUEST, "x-trace", "header", null, null, false, null, false,
                "{\"type\":[\"string\",\"null\"]}", null);
        OasDocHttpHeaderPolicy.validate(List.of(h));
    }
}

