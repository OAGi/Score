package org.oagi.score.gateway.http.api.log_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.log_management.model.ChangeSummaryType;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChangeSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentFieldChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentSummaryField;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #1533: unit tests for the LOG snapshot-pair differ backing the compare view's summary.
 *
 * <p>Snapshot fixtures mirror the {@code LogSerializer} output shape: scalar keys, {@code {}} for
 * unset references, GUID-keyed child arrays, and absent keys for null values.</p>
 */
class SnapshotChangeSummaryBuilderTest {

    private static String guid(char c) {
        return String.valueOf(c).repeat(32);
    }

    // ----- ACC: mirrors the real Item Master Base case (an ASCC added between two commits) -----

    private static String accSnapshot(String objectClassTerm, String... associations) {
        return """
                {
                  "component": "acc",
                  "guid": "%s",
                  "objectClassTerm": "%s",
                  "componentType": "Base",
                  "state": "Published",
                  "deprecated": false,
                  "abstract": true,
                  "basedAcc": {},
                  "ownerUser": {"username": "oagis", "roles": ["developer"]},
                  "namespace": {"uri": "http://www.openapplications.org/oagis/10", "standard": true},
                  "associations": [%s],
                  "_metadata": {"accManifest": {"den": "%s. Details"}}
                }
                """.formatted(guid('c'), objectClassTerm, String.join(",", associations), objectClassTerm);
    }

    private static String ascc(char guidChar, String propertyTerm, int min, int max, String definition) {
        return """
                {
                  "component": "ascc",
                  "guid": "%s",
                  "cardinalityMin": %d,
                  "cardinalityMax": %d,
                  %s
                  "deprecated": false,
                  "toAsccp": {"guid": "%s", "propertyTerm": "%s", "den": "%s. %s"}
                }
                """.formatted(guid(guidChar), min, max,
                (definition != null) ? "\"definition\": \"" + definition + "\"," : "",
                guid('9'), propertyTerm, propertyTerm, propertyTerm);
    }

    private static String bcc(char guidChar, String propertyTerm, int min, int max, String fixedValue) {
        return """
                {
                  "component": "bcc",
                  "guid": "%s",
                  "cardinalityMin": %d,
                  "cardinalityMax": %d,
                  "entityType": "Element",
                  "nillable": false,
                  "deprecated": false,
                  %s
                  "toBccp": {"guid": "%s", "propertyTerm": "%s", "den": "%s. Text"}
                }
                """.formatted(guid(guidChar), min, max,
                (fixedValue != null) ? "\"fixedValue\": \"" + fixedValue + "\"," : "",
                guid('8'), propertyTerm, propertyTerm);
    }

    @Test
    void accAddedAssociationIsReported() {
        // The real 51183 vs 51265 shape: same scalars, one ASCC added in the newer snapshot.
        String before = accSnapshot("Item Master Base",
                ascc('a', "Classification", 0, -1, null));
        String after = accSnapshot("Item Master Base",
                ascc('a', "Classification", 0, -1, null),
                ascc('b', "Sales Party Reference", 0, -1, "One or more sales parties."));

        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(before, after, 9, 8);

        assertThat(summary.ccType()).isEqualTo(CcType.ACC);
        assertThat(summary.name()).isEqualTo("Item Master Base. Details");
        assertThat(summary.summaryType()).isEqualTo(ChangeSummaryType.REVISED);
        assertThat(summary.revisionNum()).isEqualTo(9);
        assertThat(summary.prevRevisionNum()).isEqualTo(8);
        assertThat(summary.fieldChanges()).isEmpty();
        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).kind()).isEqualTo("ASCC");
        assertThat(summary.childrenAdded().get(0).name())
                .isEqualTo("Sales Party Reference. Sales Party Reference");
        assertThat(summary.childrenAdded().get(0).fields()).contains(
                new ComponentSummaryField("Cardinality", "0..unbounded"),
                new ComponentSummaryField("Definition", "One or more sales parties."));
        assertThat(summary.childrenRemoved()).isEmpty();
        assertThat(summary.childrenChanged()).isEmpty();
    }

    @Test
    void accChangedAndRemovedAssociationsAndScalars() {
        String before = accSnapshot("Item Master Base",
                bcc('a', "Identifier", 0, 1, null),
                ascc('b', "Note", 0, 1, null));
        String after = accSnapshot("Item Master",
                bcc('a', "Identifier", 1, 1, "ID"));

        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(before, after, 3, 2);

        assertThat(summary.fieldChanges()).contains(
                new ComponentFieldChange("Object Class Term", "Item Master Base", "Item Master"));
        assertThat(summary.childrenRemoved()).hasSize(1);
        assertThat(summary.childrenRemoved().get(0).name()).isEqualTo("Note. Note");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("BCC", "Identifier. Text", List.of(
                        new ComponentFieldChange("Cardinality", "0..1", "1..1"),
                        new ComponentFieldChange("Value Constraint", null, "fixed \"ID\""))));
    }

    @Test
    void accSequenceReorderIsDetected() {
        String before = accSnapshot("Order",
                ascc('a', "Header", 1, 1, null), bcc('b', "Identifier", 0, 1, null));
        String after = accSnapshot("Order",
                bcc('b', "Identifier", 0, 1, null), ascc('a', "Header", 1, 1, null));

        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(before, after, 2, 1);

        assertThat(summary.fieldChanges()).containsExactly(new ComponentFieldChange("Association Sequence",
                "Header. Header | Identifier. Text", "Identifier. Text | Header. Header"));
    }

    // ----- BCCP -----

    private static String bccpSnapshot(String dtDen, String representationTerm, String defaultValue) {
        return """
                {
                  "component": "bccp",
                  "guid": "%s",
                  "propertyTerm": "Total",
                  "representationTerm": "%s",
                  %s
                  "nillable": false,
                  "deprecated": false,
                  "state": "Published",
                  "bdt": {"guid": "%s", "dataTypeTerm": "Amount", "den": "%s"},
                  "namespace": {"uri": "http://x", "standard": true},
                  "_metadata": {"bccpManifest": {"den": "Total. Amount"}}
                }
                """.formatted(guid('b'), representationTerm,
                (defaultValue != null) ? "\"defaultValue\": \"" + defaultValue + "\"," : "",
                guid('d'), dtDen);
    }

    @Test
    void bccpDtSwapSuppressesMirroredRepresentationTerm() {
        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(
                bccpSnapshot("Amount. Type", "Amount", null),
                bccpSnapshot("Amount_ Currency. Type", "Amount Currency", null), 2, 1);

        assertThat(summary.fieldChanges()).extracting(ComponentFieldChange::label)
                .containsExactly("Data Type");
    }

    @Test
    void bccpValueConstraintChange() {
        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(
                bccpSnapshot("Amount. Type", "Amount", "EUR"),
                bccpSnapshot("Amount. Type", "Amount", "USD"), 2, 1);

        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Value Constraint", "default \"EUR\"", "default \"USD\""));
    }

    // ----- Code list -----

    private static String codeListSnapshot(String versionId, String... values) {
        return """
                {
                  "component": "codeList",
                  "guid": "%s",
                  "name": "oacl_RiskCode",
                  "listId": "oacl_RiskCode",
                  "versionId": "%s",
                  "state": "Published",
                  "deprecated": false,
                  "extensible": false,
                  "agencyId": {"guid": "%s", "value": "314", "name": "OAGi"},
                  "basedCodeList": {},
                  "namespace": {"uri": "http://x", "standard": true},
                  "values": [%s]
                }
                """.formatted(guid('e'), versionId, guid('7'), String.join(",", values));
    }

    private static String codeListValue(char guidChar, String value, String meaning) {
        return """
                {"component": "codeListValue", "guid": "%s", "value": "%s", "meaning": "%s", "deprecated": false}
                """.formatted(guid(guidChar), value, meaning);
    }

    @Test
    void codeListValuesAndHeaderDiff() {
        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(
                codeListSnapshot("1",
                        codeListValue('a', "High", "High risk"),
                        codeListValue('b', "None", "No risk")),
                codeListSnapshot("2",
                        codeListValue('a', "High", "High risk"),
                        codeListValue('b', "None", "Risk not assessed"),
                        codeListValue('f', "Critical", "Highest risk")), 2, 1);

        assertThat(summary.ccType()).isEqualTo(CcType.CODE_LIST);
        assertThat(summary.name()).isEqualTo("oacl_RiskCode");
        assertThat(summary.fieldChanges()).containsExactly(new ComponentFieldChange("Version", "1", "2"));
        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Critical");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Value", "None", List.of(
                        new ComponentFieldChange("Meaning", "No risk", "Risk not assessed"))));
    }

    // ----- DT -----

    private static String dtSnapshot(String sixDigitId, String... supplementaryComponents) {
        return """
                {
                  "component": "dt",
                  "guid": "%s",
                  "dataTypeTerm": "Amount",
                  "qualifier": "Currency",
                  "representationTerm": "Amount",
                  %s
                  "state": "Published",
                  "deprecated": false,
                  "basedDt": {"guid": "%s", "dataTypeTerm": "Amount", "den": "Amount. Type"},
                  "namespace": {"uri": "http://x", "standard": true},
                  "supplementaryComponents": [%s],
                  "_metadata": {"dtManifest": {"den": "Amount_ Currency. Type"}}
                }
                """.formatted(guid('1'),
                (sixDigitId != null) ? "\"sixDigitId\": \"" + sixDigitId + "\"," : "",
                guid('0'), String.join(",", supplementaryComponents));
    }

    private static String dtSc(char guidChar, String propertyTerm, int min, int max) {
        return """
                {"component": "dtSc", "guid": "%s", "propertyTerm": "%s", "representationTerm": "Code",
                 "cardinalityMin": %d, "cardinalityMax": %d}
                """.formatted(guid(guidChar), propertyTerm, min, max);
    }

    @Test
    void dtSixHexIdAndSupplementaryComponentDiff() {
        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(
                dtSnapshot("1f2e3d", dtSc('a', "Currency", 1, 1)),
                dtSnapshot("3a5b7c", dtSc('a', "Currency", 0, 1), dtSc('b', "Unit", 0, 1)), 2, 1);

        assertThat(summary.name()).isEqualTo("Amount_ Currency. Type");
        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Six Hexadecimal Identifier", "1f2e3d", "3a5b7c"));
        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Unit. Code");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Supplementary Component", "Currency. Code", List.of(
                        new ComponentFieldChange("Cardinality", "1..1", "0..1"))));
    }

    // ----- Agency ID list -----

    @Test
    void agencyIdListValueMeaningDiff() {
        String template = """
                {
                  "component": "agencyIdList",
                  "guid": "%s",
                  "name": "clm6AgencyIdentification",
                  "listId": "6",
                  "versionId": "5",
                  "state": "Published",
                  "deprecated": false,
                  "agencyIdListValue": {"guid": "%s", "value": "6", "name": "United Nations"},
                  "values": [{"component": "agencyIdListValue", "guid": "%s", "value": "6", "name": "%s", "deprecated": false}]
                }
                """;
        ComponentChangeSummary summary = SnapshotChangeSummaryBuilder.diff(
                template.formatted(guid('f'), guid('5'), guid('a'), "UN"),
                template.formatted(guid('f'), guid('5'), guid('a'), "United Nations"), 2, 1);

        assertThat(summary.ccType()).isEqualTo(CcType.AGENCY_ID_LIST);
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Value", "6", List.of(
                        new ComponentFieldChange("Meaning", "UN", "United Nations"))));
    }

    // ----- guards & rendering -----

    @Test
    void differentComponentTypesAreRejected() {
        assertThatThrownBy(() -> SnapshotChangeSummaryBuilder.diff(
                accSnapshot("Order"), bccpSnapshot("Amount. Type", "Amount", null), 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revisionNumbersCarryThroughForBothWithinAndAcrossRevisionPairs() {
        String before = accSnapshot("Order", ascc('a', "Header", 1, 1, null));
        String after = accSnapshot("Order", ascc('a', "Header", 0, 1, null));

        // Two commits within the same revision.
        ComponentChangeSummary within = SnapshotChangeSummaryBuilder.diff(before, after, 9, 9);
        assertThat(within.revisionNum()).isEqualTo(9);
        assertThat(within.prevRevisionNum()).isEqualTo(9);

        ComponentChangeSummary across = SnapshotChangeSummaryBuilder.diff(before, after, 9, 8);
        assertThat(across.revisionNum()).isEqualTo(9);
        assertThat(across.prevRevisionNum()).isEqualTo(8);
        assertThat(across.childrenChanged()).containsExactly(
                new ComponentChildChange("ASCC", "Header. Header", List.of(
                        new ComponentFieldChange("Cardinality", "1..1", "0..1"))));
    }
}
