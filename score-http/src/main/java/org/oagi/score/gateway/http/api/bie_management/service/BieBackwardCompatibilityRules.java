package org.oagi.score.gateway.http.api.bie_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BackwardCompatibility;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Issue #1733: pure decision logic for the schema-level (Type-A) backward-compatibility indicator of a BIE.
 *
 * <p>Each {@code *Break*} method answers, for one kind of profiled change, whether it makes a previously valid
 * instance invalid. The XSD vs JSON distinction is encoded explicitly: a change can break both syntaxes
 * (syntax-independent), only the XML Schema rendering, or only the JSON Schema rendering. The {@link Accumulator}
 * OR-combines per-change verdicts into the final {@link BackwardCompatibility}.
 *
 * <p>This class is intentionally free of any document/repository dependency so it can be unit tested exhaustively;
 * {@code BiePackageManifestService} resolves the BIE structure and delegates every decision here.
 */
public final class BieBackwardCompatibilityRules {

    private BieBackwardCompatibilityRules() {
    }

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * XSD built-in types whose only constraining facet is whitespace (no pattern / enumeration): they accept ANY
     * character-data lexical, so a cross-branch value-domain change TO one of them (e.g. {@code xsd:integer ->
     * xsd:string}) is backward-compatible at the XML Schema level even though the derivation lattice has no
     * super-type link.
     */
    static final Set<String> UNIVERSAL_TEXT_BUILTIN_TYPES =
            Set.of("xsd:string", "xsd:normalizedString", "xsd:token", "xsd:anySimpleType", "xsd:anyType");

    // ----- cardinality -----

    /** True when the new occurrence constraint is strictly more restrictive (min raised or max lowered). */
    public static boolean cardinalityMoreRestrictive(int newMin, int newMax, int oldMin, int oldMax) {
        if (newMin > oldMin) {
            return true;
        }
        return effectiveMax(newMax) < effectiveMax(oldMax);
    }

    static int effectiveMax(int cardinalityMax) {
        return (cardinalityMax < 0) ? Integer.MAX_VALUE : cardinalityMax; // -1 (or any negative) == unbounded
    }

    /** JSON renders {@code max < 0 || max > 1} as an array, otherwise a bare value. */
    public static boolean isJsonArray(int cardinalityMax) {
        return cardinalityMax < 0 || cardinalityMax > 1;
    }

    /**
     * True when the cardinality change crosses the JSON array/scalar boundary (array vs bare value). XSD keeps the
     * same element shape, so this is a JSON-only concern.
     */
    public static boolean jsonArrayFlip(int newMax, int oldMax) {
        return isJsonArray(oldMax) != isJsonArray(newMax);
    }

    // ----- nillable / value constraint / facet -----

    /** True when nillable was turned off (the null representation is dropped). Scope is decided by the caller. */
    public static boolean nillableRemoved(boolean newNillable, boolean oldNillable) {
        return oldNillable && !newNillable;
    }

    /** Adding a fixed value where none existed, or changing it, rejects previously valid instances (both syntaxes). */
    public static boolean fixedValueBreaks(String newFixed, String oldFixed) {
        return hasLength(newFixed) && !Objects.equals(newFixed, oldFixed);
    }

    /** Tightening a length/pattern facet (min raised, max lowered, pattern added/changed). */
    public static boolean facetTightened(BigInteger newMinLength, BigInteger newMaxLength, String newPattern,
                                         BigInteger oldMinLength, BigInteger oldMaxLength, String oldPattern) {
        if (newMinLength != null && (oldMinLength == null || newMinLength.compareTo(oldMinLength) > 0)) {
            return true; // minLength added or raised
        }
        if (newMaxLength != null && (oldMaxLength == null || newMaxLength.compareTo(oldMaxLength) < 0)) {
            return true; // maxLength added or lowered
        }
        return hasLength(newPattern) && !Objects.equals(newPattern, oldPattern); // pattern added or changed
    }

    // ----- value domain (primitive XBT) -----

    public static boolean isUniversalTextType(String builtInType) {
        return builtInType != null && UNIVERSAL_TEXT_BUILTIN_TYPES.contains(builtInType);
    }

    /**
     * XSD verdict for a primitive XBT change: a widening (new is a super-type of old) is compatible; a restriction
     * (new is a sub-type of old) breaks; a cross-branch change breaks unless the new type accepts any character
     * data (a universal text type).
     */
    public static boolean xbtBreaksXml(boolean newIsSuperTypeOfOld, boolean newIsSubTypeOfOld, String newBuiltInType) {
        if (newIsSuperTypeOfOld) {
            return false; // widening
        }
        if (newIsSubTypeOfOld) {
            return true; // restriction
        }
        return !isUniversalTextType(newBuiltInType); // cross-branch
    }

    /** Walks a {@code childBuiltInType -> parentBuiltInType} lattice; true if {@code ancestor} is start or an ancestor. */
    public static boolean isAncestorOrSame(String ancestorBuiltInType, String startBuiltInType,
                                           Map<String, String> parentOf) {
        String node = startBuiltInType;
        int guard = 0;
        while (node != null && guard++ < 64) {
            if (node.equals(ancestorBuiltInType)) {
                return true;
            }
            node = parentOf.get(node);
        }
        return false;
    }

    /**
     * JSON verdict: true when the stored JSON Schema (2020-12) type/bounds are strictly narrowed. A type that no
     * longer covers a previously allowed type (treating {@code integer} as a subset of {@code number}) or a tighter
     * numeric minimum/maximum narrows; {@code format} / {@code pattern} / {@code contentEncoding} are ignored
     * (length/pattern facets are handled by {@link #facetTightened}).
     */
    public static boolean jsonTypeNarrows(String oldMap, String newMap) {
        try {
            JsonNode o = JSON_MAPPER.readTree(hasLength(oldMap) ? oldMap : "{}");
            JsonNode n = JSON_MAPPER.readTree(hasLength(newMap) ? newMap : "{}");
            if (!jsonTypeCovers(jsonTypes(n), jsonTypes(o))) {
                return true;
            }
            Double oMin = jsonNumber(o, "minimum"), nMin = jsonNumber(n, "minimum");
            Double oMax = jsonNumber(o, "maximum"), nMax = jsonNumber(n, "maximum");
            if (nMin != null && (oMin == null || nMin > oMin)) {
                return true; // new lower bound is tighter
            }
            if (nMax != null && (oMax == null || nMax < oMax)) {
                return true; // new upper bound is tighter
            }
            return false;
        } catch (Exception ignored) {
            return hasLength(newMap) && !Objects.equals(oldMap, newMap);
        }
    }

    private static Set<String> jsonTypes(JsonNode node) {
        Set<String> types = new HashSet<>();
        JsonNode t = node.get("type");
        if (t == null) {
            return types; // no constraint -> accepts anything
        }
        if (t.isArray()) {
            t.forEach(e -> types.add(e.asText()));
        } else {
            types.add(t.asText());
        }
        types.remove("null"); // nullability is handled by nillableRemoved
        return types;
    }

    private static boolean jsonTypeCovers(Set<String> newTypes, Set<String> oldTypes) {
        if (newTypes.isEmpty()) {
            return true; // new accepts anything
        }
        if (oldTypes.isEmpty()) {
            return false; // old accepted anything, new restricts
        }
        for (String t : oldTypes) {
            if (newTypes.contains(t)) {
                continue;
            }
            if ("integer".equals(t) && newTypes.contains("number")) {
                continue; // number is a super-set of integer
            }
            return false;
        }
        return true;
    }

    private static Double jsonNumber(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v != null && v.isNumber()) ? v.asDouble() : null;
    }

    private static boolean hasLength(String s) {
        return s != null && !s.isEmpty();
    }

    // ----- aggregation -----

    /**
     * OR-combines per-change verdicts into the final {@link BackwardCompatibility}.
     *
     * <p>{@code syntaxIndependent} answers whether the change is backward compatible <em>regardless of the target
     * rendering</em>. A single change that invalidates a previously valid instance in EVERY syntax is a
     * syntax-independent break: this covers structural element-set changes (a required element added, or an element
     * removed / unused — {@link #recordStructuralBreak()}) AND per-element constraint tightenings that bind in both
     * renderings (cardinality tightening, fixed value added/changed, element nillable removal, facet tightening,
     * value-domain / enum narrowing — recorded via {@link #recordBreak(boolean, boolean)} with both flags set).
     *
     * <p>A change that breaks only ONE rendering (XML-only: entity-type flip, XSD primitive restriction; JSON-only:
     * array/scalar flip, SC-wrapper flip, attribute / supplementary-component nillable removal) is syntax-DEPENDENT:
     * it leaves {@code syntaxIndependent} intact and flips only the affected {@code xmlSchema} / {@code jsonSchema}
     * column. Two <em>distinct</em> single-syntax breaks (one XML-only and one JSON-only) therefore do not, between
     * them, constitute a syntax-independent break — only a single change that breaks both renderings does.
     */
    public static final class Accumulator {
        private boolean breaksXmlSchema = false;
        private boolean breaksJsonSchema = false;
        private boolean breaksSyntaxIndependent = false;

        /**
         * A per-element constraint change. When it breaks a SINGLE rendering it is syntax-dependent and only that
         * column flips; when one change breaks BOTH renderings it is, by definition, rendering-independent and also
         * flips {@code syntaxIndependent}.
         */
        public void recordBreak(boolean xmlSchema, boolean jsonSchema) {
            this.breaksXmlSchema |= xmlSchema;
            this.breaksJsonSchema |= jsonSchema;
            if (xmlSchema && jsonSchema) {
                this.breaksSyntaxIndependent = true;
            }
        }

        /** A change to the element set (required element added, or element removed / unused): breaks every level. */
        public void recordStructuralBreak() {
            this.breaksSyntaxIndependent = true;
            this.breaksXmlSchema = true;
            this.breaksJsonSchema = true;
        }

        public BackwardCompatibility toBackwardCompatibility() {
            return new BackwardCompatibility(!breaksSyntaxIndependent, !breaksXmlSchema, !breaksJsonSchema);
        }
    }
}
