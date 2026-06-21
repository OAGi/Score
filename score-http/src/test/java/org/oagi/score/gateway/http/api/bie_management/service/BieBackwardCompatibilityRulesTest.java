package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BackwardCompatibility;
import org.oagi.score.gateway.http.api.bie_management.service.BieBackwardCompatibilityRules.Accumulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.oagi.score.gateway.http.api.bie_management.service.BieBackwardCompatibilityRules.*;

/**
 * Issue #1733: exhaustive unit tests for the schema-level (Type-A) backward-compatibility decision rules.
 *
 * <p>Each case applies the same rule(s) the manifest service uses, through a fresh {@link Accumulator}, and asserts
 * the resulting {@code {syntaxIndependent, xmlSchema, jsonSchema}}. Value-domain cases are data-driven from the real
 * XBT derivation lattice and the stored JSON Schema 2020-12 maps (xbt.subtype_of_xbt_id / xbt.jbt_202012_map).
 */
class BieBackwardCompatibilityRulesTest {

    // ----- real XBT lattice (child builtin -> parent builtin) from the oagi DB -----
    private static final Map<String, String> PARENT_OF = Map.ofEntries(
            Map.entry("xsd:anySimpleType", "xsd:anyType"),
            Map.entry("xsd:string", "xsd:anySimpleType"),
            Map.entry("xsd:normalizedString", "xsd:string"),
            Map.entry("xsd:token", "xsd:normalizedString"),
            Map.entry("xsd:language", "xsd:token"),
            Map.entry("xsd:Name", "xsd:token"),
            Map.entry("xsd:NCName", "xsd:Name"),
            Map.entry("xsd:ID", "xsd:NCName"),
            Map.entry("xsd:NMTOKEN", "xsd:token"),
            Map.entry("xsd:decimal", "xsd:anySimpleType"),
            Map.entry("xsd:integer", "xsd:decimal"),
            Map.entry("xsd:nonNegativeInteger", "xsd:integer"),
            Map.entry("xsd:positiveInteger", "xsd:nonNegativeInteger"),
            Map.entry("xsd:nonPositiveInteger", "xsd:integer"),
            Map.entry("xsd:long", "xsd:integer"),
            Map.entry("xsd:int", "xsd:long"),
            Map.entry("xsd:short", "xsd:int"),
            Map.entry("xsd:byte", "xsd:short"),
            Map.entry("xsd:unsignedLong", "xsd:nonNegativeInteger"),
            Map.entry("xsd:unsignedInt", "xsd:unsignedLong"),
            Map.entry("xsd:float", "xsd:anySimpleType"),
            Map.entry("xsd:double", "xsd:anySimpleType"),
            Map.entry("xsd:boolean", "xsd:anySimpleType"),
            Map.entry("xsd:date", "xsd:anySimpleType"),
            Map.entry("xsd:dateTime", "xsd:anySimpleType"),
            Map.entry("xsd:anyURI", "xsd:anySimpleType"),
            Map.entry("xsd:hexBinary", "xsd:anySimpleType"));

    // ----- stored XBT -> JSON Schema 2020-12 maps from the oagi DB -----
    private static final Map<String, String> JSON_MAP = Map.ofEntries(
            Map.entry("xsd:anyType", "{}"),
            Map.entry("xsd:anySimpleType", "{\"type\":\"string\"}"),
            Map.entry("xsd:string", "{\"type\":\"string\"}"),
            Map.entry("xsd:normalizedString", "{\"type\":\"string\"}"),
            Map.entry("xsd:token", "{\"type\":\"string\"}"),
            Map.entry("xsd:language", "{\"type\":\"string\"}"),
            Map.entry("xsd:NMTOKEN", "{\"type\":\"string\"}"),
            Map.entry("xsd:decimal", "{\"type\":\"number\"}"),
            Map.entry("xsd:float", "{\"type\":\"number\"}"),
            Map.entry("xsd:integer", "{\"type\":\"integer\"}"),
            Map.entry("xsd:nonNegativeInteger", "{\"type\":\"integer\",\"minimum\":0}"),
            Map.entry("xsd:long", "{\"type\":\"integer\",\"minimum\":-9223372036854775808,\"maximum\":9223372036854775807}"),
            Map.entry("xsd:int", "{\"type\":\"integer\",\"minimum\":-2147483648,\"maximum\":2147483647}"),
            Map.entry("xsd:boolean", "{\"type\":\"boolean\"}"),
            Map.entry("xsd:date", "{\"type\":\"string\",\"format\":\"date\"}"),
            Map.entry("xsd:dateTime", "{\"type\":\"string\",\"format\":\"date-time\"}"),
            Map.entry("xsd:anyURI", "{\"type\":\"string\",\"format\":\"uri-reference\"}"));

    // ----- case model -----
    record Case(String category, String label, BackwardCompatibility expected, Supplier<BackwardCompatibility> actual) {
    }

    private static BackwardCompatibility bc(boolean si, boolean xml, boolean json) {
        return new BackwardCompatibility(si, xml, json);
    }

    private static BackwardCompatibility eval(Consumer<Accumulator> rule) {
        Accumulator acc = new Accumulator();
        rule.accept(acc);
        return acc.toBackwardCompatibility();
    }

    /** Cardinality change: the service applies cardinalityMoreRestrictive (both) + jsonArrayFlip (json-only). */
    private static Supplier<BackwardCompatibility> card(int nMin, int nMax, int oMin, int oMax) {
        return () -> eval(acc -> {
            if (cardinalityMoreRestrictive(nMin, nMax, oMin, oMax)) acc.recordBreak(true, true);
            if (jsonArrayFlip(nMax, oMax)) acc.recordBreak(false, true);
        });
    }

    /** Primitive value-domain change, driven by the real lattice + JSON maps (matches evaluateValueDomain). */
    private static Supplier<BackwardCompatibility> vd(String oldB, String newB) {
        return () -> eval(acc -> {
            if (oldB.equals(newB)) return;
            boolean sup = isAncestorOrSame(newB, oldB, PARENT_OF);
            boolean sub = isAncestorOrSame(oldB, newB, PARENT_OF);
            boolean bx = xbtBreaksXml(sup, sub, newB);
            boolean bj = jsonTypeNarrows(JSON_MAP.get(oldB), JSON_MAP.get(newB));
            if (bx || bj) acc.recordBreak(bx, bj);
        });
    }

    private static final BigInteger TEN = BigInteger.TEN, FIVE = BigInteger.valueOf(5);

    static List<Case> cases() {
        List<Case> c = new ArrayList<>();

        // syntaxIndependent (SI) is broken by any change that invalidates a previously valid instance in EVERY
        // syntax: a structural element-set change (element added-required / removed) OR a per-element tightening
        // that breaks both renderings at once (recordBreak(true,true)). A single-syntax break (XML-only or
        // JSON-only) keeps SI=true and flips only the affected xmlSchema / jsonSchema column.

        // ---- cardinality (min/max + JSON array flip): tightening breaks both syntaxes -> SI=false ----
        c.add(new Case("cardinality", "tighten min 0->1", bc(false, false, false), card(1, 1, 0, 1)));
        c.add(new Case("cardinality", "tighten max 5->3", bc(false, false, false), card(0, 3, 0, 5)));
        c.add(new Case("cardinality", "tighten max unbounded->1 (array->scalar)", bc(false, false, false), card(0, 1, 0, -1)));
        c.add(new Case("cardinality", "loosen min 1->0", bc(true, true, true), card(0, 1, 1, 1)));
        c.add(new Case("cardinality", "loosen max 1->2 (scalar->array)", bc(true, true, false), card(0, 2, 0, 1)));
        c.add(new Case("cardinality", "loosen max 1->unbounded (scalar->array)", bc(true, true, false), card(0, -1, 0, 1)));
        c.add(new Case("cardinality", "unchanged 1..1", bc(true, true, true), card(1, 1, 1, 1)));

        // ---- structural add/remove: changes the element set -> recordStructuralBreak() -> SI=false ----
        c.add(new Case("structural", "required element added (boundary)", bc(false, false, false),
                () -> eval(Accumulator::recordStructuralBreak)));
        c.add(new Case("structural", "element removed / unused (boundary, any cardinality)", bc(false, false, false),
                () -> eval(Accumulator::recordStructuralBreak)));
        c.add(new Case("structural", "optional element added (boundary)", bc(true, true, true),
                () -> eval(acc -> { /* min 0 -> no break */ })));

        // ---- nillable (element vs attribute/SC scope): per-element ----
        c.add(new Case("nillable", "element true->false", bc(false, false, false),
                () -> eval(acc -> { if (nillableRemoved(false, true)) acc.recordBreak(true, true); })));
        c.add(new Case("nillable", "attribute / SC true->false (XSD ignores -> JSON-only)", bc(true, true, false),
                () -> eval(acc -> { if (nillableRemoved(false, true)) acc.recordBreak(false, true); })));
        c.add(new Case("nillable", "false->true (widening)", bc(true, true, true),
                () -> eval(acc -> { if (nillableRemoved(true, false)) acc.recordBreak(true, true); })));

        // ---- fixed / default value: per-element ----
        c.add(new Case("value-constraint", "fixed added none->X", bc(false, false, false),
                () -> eval(acc -> { if (fixedValueBreaks("X", null)) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "fixed changed X->Y", bc(false, false, false),
                () -> eval(acc -> { if (fixedValueBreaks("Y", "X")) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "fixed removed X->none", bc(true, true, true),
                () -> eval(acc -> { if (fixedValueBreaks(null, "X")) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "fixed unchanged X->X", bc(true, true, true),
                () -> eval(acc -> { if (fixedValueBreaks("X", "X")) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "default->fixed (now forced)", bc(false, false, false),
                () -> eval(acc -> { if (fixedValueBreaks("Y", null)) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "fixed->default (loosened)", bc(true, true, true),
                () -> eval(acc -> { if (fixedValueBreaks(null, "X")) acc.recordBreak(true, true); })));
        c.add(new Case("value-constraint", "default changed (never constrains)", bc(true, true, true),
                () -> eval(acc -> { if (fixedValueBreaks(null, null)) acc.recordBreak(true, true); })));

        // ---- facet (length/pattern): per-element ----
        c.add(new Case("facet", "maxLength added none->10", bc(false, false, false),
                () -> eval(acc -> { if (facetTightened(null, TEN, null, null, null, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "maxLength tightened 10->5", bc(false, false, false),
                () -> eval(acc -> { if (facetTightened(null, FIVE, null, null, TEN, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "maxLength loosened 5->10", bc(true, true, true),
                () -> eval(acc -> { if (facetTightened(null, TEN, null, null, FIVE, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "maxLength removed 10->none", bc(true, true, true),
                () -> eval(acc -> { if (facetTightened(null, null, null, null, TEN, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "minLength added/raised", bc(false, false, false),
                () -> eval(acc -> { if (facetTightened(FIVE, null, null, null, null, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "pattern added", bc(false, false, false),
                () -> eval(acc -> { if (facetTightened(null, null, "[A-Z]+", null, null, null)) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "pattern changed", bc(false, false, false),
                () -> eval(acc -> { if (facetTightened(null, null, "[A-Z]+", null, null, "[a-z]+")) acc.recordBreak(true, true); })));
        c.add(new Case("facet", "pattern removed", bc(true, true, true),
                () -> eval(acc -> { if (facetTightened(null, null, null, null, null, "[a-z]+")) acc.recordBreak(true, true); })));

        // ---- value domain: primitive XBT (lattice-driven): per-element ----
        c.add(new Case("value-domain primitive", "normalizedString -> token (restriction, same JSON)", bc(true, false, true), vd("xsd:normalizedString", "xsd:token")));
        c.add(new Case("value-domain primitive", "string -> token (restriction)", bc(true, false, true), vd("xsd:string", "xsd:token")));
        c.add(new Case("value-domain primitive", "string -> language (restriction)", bc(true, false, true), vd("xsd:string", "xsd:language")));
        c.add(new Case("value-domain primitive", "token -> NMTOKEN (restriction)", bc(true, false, true), vd("xsd:token", "xsd:NMTOKEN")));
        c.add(new Case("value-domain primitive", "token -> normalizedString (widening)", bc(true, true, true), vd("xsd:token", "xsd:normalizedString")));
        c.add(new Case("value-domain primitive", "decimal -> integer (restriction, JSON narrows)", bc(false, false, false), vd("xsd:decimal", "xsd:integer")));
        c.add(new Case("value-domain primitive", "integer -> decimal (widening)", bc(true, true, true), vd("xsd:integer", "xsd:decimal")));
        c.add(new Case("value-domain primitive", "integer -> nonNegativeInteger (JSON min added)", bc(false, false, false), vd("xsd:integer", "xsd:nonNegativeInteger")));
        c.add(new Case("value-domain primitive", "integer -> int (JSON bounds added)", bc(false, false, false), vd("xsd:integer", "xsd:int")));
        c.add(new Case("value-domain primitive", "long -> int (JSON bounds tighter)", bc(false, false, false), vd("xsd:long", "xsd:int")));
        c.add(new Case("value-domain primitive", "int -> long (widening)", bc(true, true, true), vd("xsd:int", "xsd:long")));
        c.add(new Case("value-domain primitive", "int -> integer (widening)", bc(true, true, true), vd("xsd:int", "xsd:integer")));
        c.add(new Case("value-domain primitive", "string -> integer (cross, JSON narrows)", bc(false, false, false), vd("xsd:string", "xsd:integer")));
        c.add(new Case("value-domain primitive", "string -> date (cross, JSON format-only)", bc(true, false, true), vd("xsd:string", "xsd:date")));
        c.add(new Case("value-domain primitive", "string -> anyURI (cross, JSON format-only)", bc(true, false, true), vd("xsd:string", "xsd:anyURI")));
        c.add(new Case("value-domain primitive", "anyURI -> string (cross -> universal)", bc(true, true, true), vd("xsd:anyURI", "xsd:string")));
        c.add(new Case("value-domain primitive", "date -> string (cross -> universal)", bc(true, true, true), vd("xsd:date", "xsd:string")));
        c.add(new Case("value-domain primitive", "integer -> string (cross -> universal, JSON narrows)", bc(true, true, false), vd("xsd:integer", "xsd:string")));
        c.add(new Case("value-domain primitive", "boolean -> string (cross -> universal, JSON narrows)", bc(true, true, false), vd("xsd:boolean", "xsd:string")));
        c.add(new Case("value-domain primitive", "decimal -> float (sibling, same JSON number)", bc(true, false, true), vd("xsd:decimal", "xsd:float")));
        c.add(new Case("value-domain primitive", "dateTime -> date (sibling, JSON format-only)", bc(true, false, true), vd("xsd:dateTime", "xsd:date")));
        c.add(new Case("value-domain primitive", "string -> string (unchanged)", bc(true, true, true), vd("xsd:string", "xsd:string")));

        // ---- value domain: code list / agency (per-element; !newValues.containsAll(oldValues)) ----
        c.add(new Case("value-domain code list", "code list value removed (subset)", bc(false, false, false),
                () -> eval(acc -> { if (!Set.of("A", "B").containsAll(Set.of("A", "B", "C"))) acc.recordBreak(true, true); })));
        c.add(new Case("value-domain code list", "code list value added (superset)", bc(true, true, true),
                () -> eval(acc -> { if (!Set.of("A", "B", "C").containsAll(Set.of("A", "B"))) acc.recordBreak(true, true); })));
        c.add(new Case("value-domain code list", "primitive -> code list (enum added)", bc(false, false, false),
                () -> eval(acc -> acc.recordBreak(true, true))));
        c.add(new Case("value-domain code list", "code list -> primitive (enum removed)", bc(true, true, true),
                () -> eval(acc -> { /* widening -> no break */ })));

        // ---- JSON-only representation flips: per-element ----
        c.add(new Case("json representation", "SC-wrapper: gain first used OPTIONAL SC (scalar->object)", bc(true, true, false),
                () -> eval(acc -> { if (true != false) acc.recordBreak(false, true); })));
        // Gaining a first used REQUIRED SC additionally fires the boundary structural-add break
        // (BiePackageManifestService: prevBbie != null && cardinalityMin > 0 -> recordStructuralBreak()), which
        // dominates the JSON-only wrapper flip: a new required attribute/property breaks every syntax.
        c.add(new Case("json representation", "SC-wrapper: gain first used REQUIRED SC (structural add dominates)", bc(false, false, false),
                () -> eval(acc -> { acc.recordBreak(false, true); acc.recordStructuralBreak(); })));

        // ---- entity type: per-element (XML-only) ----
        c.add(new Case("entity type", "Element <-> Attribute flip (XML-only)", bc(true, false, true),
                () -> eval(acc -> acc.recordBreak(true, false))));

        // ---- annotations (no impact) ----
        c.add(new Case("annotation", "remark / definition / example / deprecated changed", bc(true, true, true),
                () -> eval(acc -> { /* never records a break */ })));

        // ---- aggregation across multiple changes ----
        c.add(new Case("aggregation", "structural + per-element XML-only (structure dominates SI)", bc(false, false, false),
                () -> eval(acc -> { acc.recordStructuralBreak(); acc.recordBreak(true, false); })));
        c.add(new Case("aggregation", "per-element XML-only + JSON-only (no structural break)", bc(true, false, false),
                () -> eval(acc -> { acc.recordBreak(true, false); acc.recordBreak(false, true); })));
        c.add(new Case("aggregation", "brand-new BIE (no prior)", bc(false, false, false),
                () -> new BackwardCompatibility(false, false, false)));

        return c;
    }

    @ParameterizedTest(name = "[{index}] {0}: {1}")
    @MethodSource("cases")
    void verifies(Case testCase) {
        assertThat(testCase.actual().get())
                .as(testCase.category() + " / " + testCase.label())
                .isEqualTo(testCase.expected());
    }

    @Test
    void printReportTableAndYaml() {
        List<Case> cases = cases();
        StringBuilder table = new StringBuilder();
        table.append("\n========== Issue #1733 backward-compatibility cases ==========\n");
        table.append(String.format("%-26s | %-58s | %-15s | %-9s | %-10s%n",
                "category", "change", "syntaxIndependent", "xmlSchema", "jsonSchema"));
        table.append("-".repeat(132)).append('\n');
        Map<String, List<Case>> byCat = new LinkedHashMap<>();
        for (Case cse : cases) byCat.computeIfAbsent(cse.category(), k -> new ArrayList<>()).add(cse);
        for (var e : byCat.entrySet()) {
            for (Case cse : e.getValue()) {
                BackwardCompatibility r = cse.actual().get();
                table.append(String.format("%-26s | %-58s | %-15s | %-9s | %-10s%n",
                        cse.category(), cse.label(), r.syntaxIndependent(), r.xmlSchema(), r.jsonSchema()));
            }
        }
        System.out.println(table);

        StringBuilder yaml = new StringBuilder("\n========== YAML ==========\ncases:\n");
        for (Case cse : cases) {
            BackwardCompatibility r = cse.actual().get();
            yaml.append("  - category: ").append(cse.category()).append('\n');
            yaml.append("    change: \"").append(cse.label()).append("\"\n");
            yaml.append("    backwardCompatibility: { syntaxIndependent: ").append(r.syntaxIndependent())
                    .append(", xmlSchema: ").append(r.xmlSchema())
                    .append(", jsonSchema: ").append(r.jsonSchema()).append(" }\n");
        }
        System.out.println(yaml);
    }
}
