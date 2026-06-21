package org.oagi.score.gateway.http.api.log_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueDetailsRecord;
import org.oagi.score.gateway.http.api.log_management.model.ChangeSummaryType;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChangeSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentFieldChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentSummaryField;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #1533 sub-task 3: unit tests for the per-component change-summary builders.
 *
 * <p>Each case constructs minimal current/prev {@code *DetailsRecord} fixtures (unused components
 * left {@code null}) and asserts the produced {@link ComponentChangeSummary}: NEW vs REVISED
 * branching, blank-vs-null normalization, GUID-keyed child matching, sequence detection, the
 * type-specific rules (BCCP DT-swap suppression, DT value-domain identity, the same-manifest
 * fallbacks), and the Markdown rendering.</p>
 */
class ComponentChangeSummaryBuilderTest {

    private static Guid guid(char c) {
        return new Guid(String.valueOf(c).repeat(32));
    }

    private static LogSummaryRecord log(int revisionNum) {
        return new LogSummaryRecord(null, revisionNum, 1);
    }

    private static Definition definition(String content) {
        return new Definition(content, null);
    }

    // ----- fixtures -----

    private static AsccpDetailsRecord asccp(int revision, String propertyTerm, boolean nillable, String definition) {
        return new AsccpDetailsRecord(null, null, null, null, guid('a'), null, null, null, null, null,
                propertyTerm + ". " + propertyTerm, propertyTerm, true, false, nillable,
                null, null, definition(definition), null, log(revision), null, null, null,
                null, null, null, null);
    }

    private static BccpDetailsRecord bccp(int revision, DtSummaryRecord dt, String representationTerm,
                                          ValueConstraint valueConstraint) {
        return new BccpDetailsRecord(null, null, null, null, guid('b'), dt, null, null, null,
                "Total. Amount", "Total", representationTerm, false, false, false,
                null, null, valueConstraint, null, null, log(revision), null, null, null,
                null, null, null, null);
    }

    private static DtSummaryRecord dtSummary(String den) {
        return new DtSummaryRecord(null, null, null, null, guid('d'), null,
                den, "Amount", null, "Amount", null, false, false, null, null, null, null,
                1, null, null, null);
    }

    private static AccDetailsRecord acc(int revision, String objectClassTerm, List<CcAssociation> associations) {
        return new AccDetailsRecord(null, null, null, null, guid('c'), null, null, null, null, null,
                objectClassTerm + ". Details", objectClassTerm, null, OagisComponentType.Semantics,
                false, false, false, false, false,
                null, null, null, null, associations, log(revision), null, null, null,
                null, null, null, null);
    }

    private static AsccSummaryRecord ascc(char guidChar, String den, int cardinalityMin, int cardinalityMax) {
        return new AsccSummaryRecord(null, null, null, null, guid(guidChar), null, null, null, null, null,
                den, new Cardinality(cardinalityMin, cardinalityMax), false, null, null, 1, null, null, null);
    }

    private static BccSummaryRecord bcc(char guidChar, String den, int cardinalityMin, int cardinalityMax,
                                        EntityType entityType, ValueConstraint valueConstraint) {
        return new BccSummaryRecord(null, null, null, null, guid(guidChar), null, null, null, null, null,
                entityType, den, new Cardinality(cardinalityMin, cardinalityMax), false, false, null,
                valueConstraint, null, 1, null, null, null);
    }

    private static CodeListDetailsRecord codeList(int revision, String versionId,
                                                  List<CodeListValueDetailsRecord> values) {
        return new CodeListDetailsRecord(null, null, null, null, guid('e'), null, null, null,
                "oacl_RiskCode", "oacl_RiskCode", versionId, null, null, null,
                false, false, false, null, null, log(revision), null, null, null, null,
                null, null, values);
    }

    private static CodeListValueDetailsRecord codeListValue(char guidChar, String value, String meaning) {
        return codeListValue(guidChar, value, meaning, null);
    }

    private static CodeListValueDetailsRecord codeListValue(char guidChar, String value, String meaning,
                                                            String definition) {
        return new CodeListValueDetailsRecord(null, null, null, guid(guidChar),
                value, meaning, definition(definition), false, true, null, null, null, null, null, null);
    }

    private static AgencyIdListDetailsRecord agencyIdList(int revision, List<AgencyIdListValueDetailsRecord> values) {
        return new AgencyIdListDetailsRecord(null, null, null, null, guid('f'), null, null,
                "clm6AgencyIdentification", "6", "5", null, null, null, null,
                false, false, null, null, log(revision), null, null, null, null,
                null, null, values);
    }

    private static AgencyIdListValueDetailsRecord agencyIdListValue(char guidChar, String value, String name,
                                                                    boolean userDefault) {
        return new AgencyIdListValueDetailsRecord(null, null, null, guid(guidChar),
                value, name, null, false, false, userDefault, true, null, null, null, null, null);
    }

    private static DtDetailsRecord dt(int revision, String sixDigitId, List<DtAwdPriDetailsRecord> valueDomains) {
        return new DtDetailsRecord(null, null, null, null, guid('1'), null, null, null, null,
                "Amount_ Currency. Type", "Amount", "Currency", "Amount", sixDigitId,
                true, false, false, null, null, null, definition("A currency amount."), valueDomains,
                null, log(revision), null, null, null, null, null, null, null);
    }

    /** A primitive value-domain row under the fixed CDT primitive "Decimal". */
    private static DtAwdPriDetailsRecord primitiveDomain(String xbtName, boolean isDefault) {
        return primitiveDomain("Decimal", xbtName, isDefault);
    }

    private static DtAwdPriDetailsRecord primitiveDomain(String cdtPriName, String xbtName, boolean isDefault) {
        XbtSummaryRecord xbt = new XbtSummaryRecord(null, null, null, guid('2'), xbtName, "xsd:" + xbtName,
                null, null, null, null, null, null);
        return new DtAwdPriDetailsRecord(null, null, null, cdtPriName, xbt, null, null, isDefault, false);
    }

    private static DtScAwdPriDetailsRecord scPrimitiveDomain(String xbtName, boolean isDefault) {
        XbtSummaryRecord xbt = new XbtSummaryRecord(null, null, null, guid('3'), xbtName, "xsd:" + xbtName,
                null, null, null, null, null, null);
        return new DtScAwdPriDetailsRecord(null, null, null, xbtName, xbt, null, null, isDefault, false);
    }

    private static DtScDetailsRecord dtSc(char guidChar, String den, Cardinality cardinality,
                                          Cardinality prevCardinality, ValueConstraint valueConstraint) {
        return dtSc(guidChar, den, cardinality, prevCardinality, valueConstraint, List.of());
    }

    private static DtScDetailsRecord dtSc(char guidChar, String den, Cardinality cardinality,
                                          Cardinality prevCardinality, ValueConstraint valueConstraint,
                                          List<DtScAwdPriDetailsRecord> valueDomains) {
        return new DtScDetailsRecord(null, null, null, null, guid(guidChar), null, null, null, null, null,
                den, "Amount", "Currency", "Code", cardinality, prevCardinality, false, null,
                valueConstraint, null, valueDomains, log(1), null, null, null, null, null);
    }

    private static List<String> labels(List<ComponentFieldChange> changes) {
        return changes.stream().map(ComponentFieldChange::label).toList();
    }

    // ----- ASCCP -----

    @Test
    void newAsccpListsOnlyPopulatedFields() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.asccp(
                asccp(1, "Purchase Order", false, null), null, false);

        assertThat(summary.summaryType()).isEqualTo(ChangeSummaryType.NEW);
        assertThat(summary.ccType()).isEqualTo(CcType.ASCCP);
        assertThat(summary.revisionNum()).isEqualTo(1);
        assertThat(summary.fields()).extracting(ComponentSummaryField::label)
                .containsExactly("Property Term", "Reusable"); // null definition / false flags omitted
        assertThat(summary.fieldChanges()).isEmpty();
    }

    @Test
    void revisedAsccpReportsFieldChanges() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.asccp(
                asccp(2, "Order", true, "Updated."),
                asccp(1, "Purchase Order", false, null), false);

        assertThat(summary.summaryType()).isEqualTo(ChangeSummaryType.REVISED);
        assertThat(labels(summary.fieldChanges()))
                .containsExactly("Property Term", "Nillable", "Definition");
        ComponentFieldChange nillable = summary.fieldChanges().get(1);
        assertThat(nillable.before()).isEqualTo("No");
        assertThat(nillable.after()).isEqualTo("Yes");
        ComponentFieldChange definition = summary.fieldChanges().get(2);
        assertThat(definition.before()).isNull();
        assertThat(definition.after()).isEqualTo("Updated.");
    }

    @Test
    void blankAndNullAreTheSameValue() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.asccp(
                asccp(2, "Order", false, "  "),
                asccp(1, "Order", false, null), false);

        assertThat(summary.isEmpty()).isTrue();
    }

    // ----- BCCP -----

    @Test
    void revisedBccpDtSwapSuppressesMirroredRepresentationTerm() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.bccp(
                bccp(2, dtSummary("Amount_ Currency. Type"), "Amount Currency", null),
                bccp(1, dtSummary("Amount. Type"), "Amount", null), false);

        assertThat(labels(summary.fieldChanges())).containsExactly("Data Type");
    }

    @Test
    void revisedBccpReportsOwnRepresentationTermWhenDtUnchanged() {
        DtSummaryRecord dt = dtSummary("Amount. Type");
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.bccp(
                bccp(2, dt, "Amount Currency", null),
                bccp(1, dt, "Amount", null), false);

        assertThat(labels(summary.fieldChanges())).containsExactly("Representation Term");
    }

    @Test
    void revisedBccpValueConstraintChange() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.bccp(
                bccp(2, null, "Amount", new ValueConstraint(null, "USD")),
                bccp(1, null, "Amount", new ValueConstraint("EUR", null)), false);

        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Value Constraint", "default \"EUR\"", "fixed \"USD\""));
    }

    @Test
    void sameManifestBccpSkipsUnresolvableDtButReportsFrozenRepresentationTerm() {
        // End-user in-place amendment: the prev mapper mirrors the CURRENT manifest's DT, so the
        // DT compare is skipped; the frozen representation term still surfaces the swap.
        DtSummaryRecord mirroredDt = dtSummary("Amount_ Currency. Type");
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.bccp(
                bccp(2, mirroredDt, "Amount Currency", null),
                bccp(1, mirroredDt, "Amount", null), true);

        assertThat(labels(summary.fieldChanges())).containsExactly("Representation Term");
    }

    // ----- ACC associations -----

    @Test
    void newAccListsAssociationsInSequenceOrder() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(1, "Purchase Order", List.of(
                        ascc('a', "Header. Header", 1, 1),
                        bcc('b', "Identifier. Identifier", 0, -1, EntityType.Element, null))),
                null, false);

        assertThat(summary.children()).hasSize(2);
        assertThat(summary.children().get(0).kind()).isEqualTo("ASCC");
        assertThat(summary.children().get(0).name()).isEqualTo("Header. Header");
        assertThat(summary.children().get(1).kind()).isEqualTo("BCC");
        assertThat(summary.children().get(1).fields()).contains(
                new ComponentSummaryField("Cardinality", "0..unbounded"),
                new ComponentSummaryField("Entity Type", "Element"));
    }

    @Test
    void revisedAccMatchesAssociationsByGuid() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(2, "Purchase Order", List.of(
                        ascc('a', "Header. Header", 1, 1),
                        bcc('b', "Identifier. Identifier", 1, 1, EntityType.Element, null),
                        ascc('e', "Note. Note", 0, -1))),
                acc(1, "Purchase Order", List.of(
                        ascc('a', "Header. Header", 1, 1),
                        bcc('b', "Identifier. Identifier", 0, 1, EntityType.Element, null),
                        bcc('f', "Status. Code", 0, 1, EntityType.Element, null))), false);

        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Note. Note");
        assertThat(summary.childrenRemoved()).hasSize(1);
        assertThat(summary.childrenRemoved().get(0).name()).isEqualTo("Status. Code");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("BCC", "Identifier. Identifier",
                        List.of(new ComponentFieldChange("Cardinality", "0..1", "1..1"))));
    }

    @Test
    void revisedAccDetectsSequenceReorder() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(2, "Purchase Order", List.of(
                        bcc('b', "Identifier. Identifier", 0, 1, EntityType.Element, null),
                        ascc('a', "Header. Header", 1, 1))),
                acc(1, "Purchase Order", List.of(
                        ascc('a', "Header. Header", 1, 1),
                        bcc('b', "Identifier. Identifier", 0, 1, EntityType.Element, null))), false);

        assertThat(labels(summary.fieldChanges())).containsExactly("Association Sequence");
        ComponentFieldChange sequence = summary.fieldChanges().get(0);
        assertThat(sequence.before()).isEqualTo("Header. Header | Identifier. Identifier");
        assertThat(sequence.after()).isEqualTo("Identifier. Identifier | Header. Header");
        assertThat(summary.childrenChanged()).isEmpty();
    }

    @Test
    void revisedAccWithNoChangesIsEmpty() {
        List<CcAssociation> associations = List.of(ascc('a', "Header. Header", 1, 1));
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(2, "Purchase Order", associations),
                acc(1, "Purchase Order", associations), false);

        assertThat(summary.isEmpty()).isTrue();
    }

    @Test
    void sameManifestAccSkipsAssociationDiffButStillDiffsScalars() {
        // End-user in-place amendment: prev.associations() mirror the CURRENT manifest, so the
        // association diff (and sequence compare) is skipped instead of reporting "no change".
        List<CcAssociation> mirrored = List.of(ascc('a', "Header. Header", 1, 1));
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(2, "Order", mirrored),
                acc(1, "Purchase Order", mirrored), true);

        assertThat(labels(summary.fieldChanges())).containsExactly("Object Class Term");
        assertThat(summary.childrenAdded()).isEmpty();
        assertThat(summary.childrenRemoved()).isEmpty();
        assertThat(summary.childrenChanged()).isEmpty();
    }

    @Test
    void addedAssociationSummaryIncludesDefinition() {
        AsccSummaryRecord added = new AsccSummaryRecord(null, null, null, null, guid('e'), null, null,
                null, null, null, "Note. Note", new Cardinality(0, 1), false, null,
                definition("A free-form note."), 1, null, null, null);
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.acc(
                acc(2, "Purchase Order", List.of(ascc('a', "Header. Header", 1, 1), added)),
                acc(1, "Purchase Order", List.of(ascc('a', "Header. Header", 1, 1))), false);

        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).fields()).contains(
                new ComponentSummaryField("Definition", "A free-form note."));
    }

    // ----- Code list -----

    @Test
    void revisedCodeListDiffsHeaderAndValues() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.codeList(
                codeList(2, "2", List.of(
                        codeListValue('a', "High", "High risk"),
                        codeListValue('b', "None", "Risk not assessed"),
                        codeListValue('e', "Critical", "Highest risk"))),
                codeList(1, "1", List.of(
                        codeListValue('a', "High", "High risk"),
                        codeListValue('b', "None", "No risk"),
                        codeListValue('f', "Low", "Low risk"))));

        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Version", "1", "2"));
        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Critical");
        assertThat(summary.childrenRemoved()).hasSize(1);
        assertThat(summary.childrenRemoved().get(0).name()).isEqualTo("Low");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Value", "None",
                        List.of(new ComponentFieldChange("Meaning", "No risk", "Risk not assessed"))));
    }

    @Test
    void newCodeListValueIncludesDefinition() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.codeList(
                codeList(1, "1", List.of(codeListValue('a', "High", "High risk", "Highest tier."))), null);

        assertThat(summary.children()).hasSize(1);
        assertThat(summary.children().get(0).fields()).containsExactly(
                new ComponentSummaryField("Meaning", "High risk"),
                new ComponentSummaryField("Definition", "Highest tier."));
    }

    // ----- Agency ID list -----

    @Test
    void revisedAgencyIdListReportsDefaultFlagChanges() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.agencyIdList(
                agencyIdList(2, List.of(agencyIdListValue('a', "6", "United Nations", true))),
                agencyIdList(1, List.of(agencyIdListValue('a', "6", "United Nations", false))));

        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Value", "6",
                        List.of(new ComponentFieldChange("User Default", "No", "Yes"))));
    }

    // ----- DT -----

    @Test
    void newDtListsValueDomainsAndSupplementaryComponents() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(1, "3a5b7c", List.of(primitiveDomain("Decimal", "Decimal", true))),
                List.of(dtSc('a', "Currency. Code", new Cardinality(0, 1), null, null)),
                null, null);

        assertThat(summary.fields()).contains(
                new ComponentSummaryField("Data Type Term", "Amount"),
                new ComponentSummaryField("Six Hexadecimal Identifier", "3a5b7c"));
        assertThat(summary.children()).hasSize(2);
        assertThat(summary.children().get(0).kind()).isEqualTo("Value Domain");
        assertThat(summary.children().get(0).name()).isEqualTo("Primitive \"Decimal\"");
        assertThat(summary.children().get(0).fields()).containsExactly(
                new ComponentSummaryField("Default", "Yes"));
        assertThat(summary.children().get(1).kind()).isEqualTo("Supplementary Component");
    }

    @Test
    void revisedDtComparesSixHexId() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, "3a5b7c", List.of()), List.of(),
                dt(1, "1f2e3d", List.of()), List.of());

        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Six Hexadecimal Identifier", "1f2e3d", "3a5b7c"));
    }

    @Test
    void revisedDtReportsValueDomainAddAndDefaultChange() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, null, List.of(primitiveDomain("Token", false), primitiveDomain("Decimal", "Decimal", true))),
                List.of(),
                dt(1, null, List.of(primitiveDomain("Token", true))), List.of());

        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Primitive \"Decimal\"");
        assertThat(summary.fieldChanges()).containsExactly(
                new ComponentFieldChange("Default Value Domain",
                        "Primitive \"Token\" (Decimal)", "Primitive \"Decimal\""));
    }

    @Test
    void valueDomainIdentityIncludesCdtPrimitive() {
        // The same XBT under two different CDT primitives must be two distinct domains,
        // not silently merged by a shared display name.
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, null, List.of(
                        primitiveDomain("Decimal", "Token", true),
                        primitiveDomain("Integer", "Token", false))),
                List.of(),
                dt(1, null, List.of(primitiveDomain("Decimal", "Token", true))), List.of());

        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Primitive \"Token\" (Integer)");
        assertThat(summary.childrenRemoved()).isEmpty();
        assertThat(summary.fieldChanges()).isEmpty();
    }

    @Test
    void revisedDtDiffsSupplementaryComponentsWhenPrevListAvailable() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, null, List.of()), List.of(
                        dtSc('a', "Currency. Code", new Cardinality(0, 1), null, null),
                        dtSc('b', "Unit. Code", new Cardinality(1, 1), null, null)),
                dt(1, null, List.of()), List.of(
                        dtSc('a', "Currency. Code", new Cardinality(1, 1), null, null),
                        dtSc('c', "Scheme. Identifier", new Cardinality(0, 1), null, null)));

        assertThat(summary.childrenAdded()).hasSize(1);
        assertThat(summary.childrenAdded().get(0).name()).isEqualTo("Unit. Code");
        assertThat(summary.childrenRemoved()).hasSize(1);
        assertThat(summary.childrenRemoved().get(0).name()).isEqualTo("Scheme. Identifier");
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Supplementary Component", "Currency. Code",
                        List.of(new ComponentFieldChange("Cardinality", "1..1", "0..1"))));
    }

    @Test
    void revisedDtScDetectsDefaultValueDomainFlip() {
        Cardinality cardinality = new Cardinality(0, 1);
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, null, List.of()), List.of(dtSc('a', "Currency. Code", cardinality, null, null,
                        List.of(scPrimitiveDomain("Token", false), scPrimitiveDomain("Decimal", true)))),
                dt(1, null, List.of()), List.of(dtSc('a', "Currency. Code", cardinality, null, null,
                        List.of(scPrimitiveDomain("Token", true), scPrimitiveDomain("Decimal", false)))));

        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Supplementary Component", "Currency. Code",
                        List.of(new ComponentFieldChange("Default Value Domain",
                                "Primitive \"Token\"", "Primitive \"Decimal\""))));
    }

    @Test
    void revisedDtFallsBackToPrevCardinalityWithoutPrevScList() {
        ComponentChangeSummary summary = ComponentChangeSummaryBuilder.dt(
                dt(2, null, List.of()), List.of(
                        dtSc('a', "Currency. Code", new Cardinality(0, 1), new Cardinality(1, 1), null),
                        dtSc('b', "Unit. Code", new Cardinality(1, 1), new Cardinality(1, 1), null)),
                dt(1, null, List.of()), null);

        assertThat(summary.childrenAdded()).isEmpty();
        assertThat(summary.childrenRemoved()).isEmpty();
        assertThat(summary.childrenChanged()).containsExactly(
                new ComponentChildChange("Supplementary Component", "Currency. Code",
                        List.of(new ComponentFieldChange("Cardinality", "1..1", "0..1"))));
    }

    // ----- helpers -----

    @Test
    void componentTypeNameIsHumanized() {
        assertThat(ComponentChangeSummaryBuilder.componentTypeName(OagisComponentType.UserExtensionGroup))
                .isEqualTo("User Extension Group");
        assertThat(ComponentChangeSummaryBuilder.componentTypeName(OagisComponentType.OAGIS10Nouns))
                .isEqualTo("OAGIS10 Nouns");
        assertThat(ComponentChangeSummaryBuilder.componentTypeName(OagisComponentType.Semantics))
                .isEqualTo("Semantics");
        assertThat(ComponentChangeSummaryBuilder.componentTypeName(OagisComponentType.Base))
                .isEqualTo("Base (Abstract)");
    }

}
