package org.oagi.score.gateway.http.api.integration_management.github.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The configurable CcState -> board action (fieldOption / remove) mapping, key validation, and the
 * maintainer gate-fieldOption guard (issue #1533, Feature 2).
 */
class ProjectFieldOptionsTest {

    private static final ProjectFieldOptions DEFAULTS = new ProjectFieldOptions(Map.of(
            "WIP", "Implementing", "Draft", "Implemented", "Candidate", "Candidate",
            "ReleaseDraft", "Ready for release"), "New");

    @Test
    void mapsTheWorkingStatesToTheirFieldOptions() {
        assertThat(DEFAULTS.fieldOptionFor(CcState.WIP)).isEqualTo("Implementing");
        assertThat(DEFAULTS.fieldOptionFor(CcState.Draft)).isEqualTo("Implemented");
        assertThat(DEFAULTS.fieldOptionFor(CcState.Candidate)).isEqualTo("Candidate");
        assertThat(DEFAULTS.fieldOptionFor(CcState.ReleaseDraft)).isEqualTo("Ready for release");
    }

    @Test
    void givesNoFieldOptionForStatesThatDoNotMapToAFieldOption() {
        assertThat(DEFAULTS.fieldOptionFor(CcState.QA)).isNull();
        assertThat(DEFAULTS.fieldOptionFor(CcState.Production)).isNull();
        assertThat(DEFAULTS.fieldOptionFor(CcState.Deleted)).isNull();
        // Published removes the card rather than assigning a fieldOption.
        assertThat(DEFAULTS.fieldOptionFor(CcState.Published)).isNull();
        assertThat(DEFAULTS.fieldOptionFor(null)).isNull();
    }

    @Test
    void removesTheCardOnlyWhenPublished() {
        assertThat(DEFAULTS.isRemoveState(CcState.Published)).isTrue();

        assertThat(DEFAULTS.isRemoveState(CcState.ReleaseDraft)).isFalse();
        assertThat(DEFAULTS.isRemoveState(CcState.WIP)).isFalse();
        assertThat(DEFAULTS.isRemoveState(CcState.Production)).isFalse();
        assertThat(DEFAULTS.isRemoveState(null)).isFalse();
    }

    @Test
    void treatsOnlyMemberReviewAsAGateFieldOption() {
        assertThat(DEFAULTS.isGateFieldOption("Member review")).isTrue();

        // 'Ready for release' is now state-driven (ReleaseDraft), so it is no longer a gate fieldOption.
        assertThat(DEFAULTS.isGateFieldOption("Ready for release")).isFalse();
        assertThat(DEFAULTS.isGateFieldOption("Implementing")).isFalse();
        assertThat(DEFAULTS.isGateFieldOption("Candidate")).isFalse();
        assertThat(DEFAULTS.isGateFieldOption("New")).isFalse();
        assertThat(DEFAULTS.isGateFieldOption(null)).isFalse();
    }

    @Test
    void exposesTheDistinctFieldOptionNamesIncludingTheInitialFieldOptionForFieldAutoDiscovery() {
        assertThat(DEFAULTS.fieldOptionNames())
                .containsExactlyInAnyOrder("Implementing", "Implemented", "Candidate", "Ready for release", "New");
    }

    @Test
    void exposesTheConfiguredInitialFieldOptionOrNullWhenUnset() {
        assertThat(DEFAULTS.getDefaultFieldOption()).isEqualTo("New");
        assertThat(new ProjectFieldOptions(Map.of(), "Backlog").getDefaultFieldOption()).isEqualTo("Backlog");
        // Optional: blank/unset -> no initial fieldOption (GitHub's API can't expose the field's Default).
        assertThat(new ProjectFieldOptions(Map.of(), null).getDefaultFieldOption()).isNull();
        assertThat(new ProjectFieldOptions(Map.of(), "  ").getDefaultFieldOption()).isNull();
    }

    @Test
    void treatsTheCancelRevisionRevertsAsResetsToTheInitialFieldOption() {
        // WIP -> Published (developer cancel) and WIP -> Production (end-user cancel) reset to New.
        assertThat(DEFAULTS.isRevertToInitial(CcState.WIP, CcState.Published)).isTrue();
        assertThat(DEFAULTS.isRevertToInitial(CcState.WIP, CcState.Production)).isTrue();

        // The release path (ReleaseDraft -> Published) is NOT a revert — it removes.
        assertThat(DEFAULTS.isRevertToInitial(CcState.ReleaseDraft, CcState.Published)).isFalse();
        assertThat(DEFAULTS.isRevertToInitial(CcState.Draft, CcState.Candidate)).isFalse();
        assertThat(DEFAULTS.isRevertToInitial(null, CcState.Published)).isFalse();
    }

    @Test
    void exposesTheStateToFieldOptionMapForTheFrontend() {
        assertThat(DEFAULTS.stateFieldOptionNames())
                .containsEntry("WIP", "Implementing")
                .containsEntry("Draft", "Implemented")
                .containsEntry("Candidate", "Candidate")
                .containsEntry("ReleaseDraft", "Ready for release")
                .hasSize(4);
    }

    @Test
    void usesTheBuiltInDefaultsWhenNothingIsConfigured() {
        ProjectFieldOptions empty = new ProjectFieldOptions(Map.of(), null);
        assertThat(empty.fieldOptionFor(CcState.WIP)).isEqualTo("Implementing");
        assertThat(empty.fieldOptionFor(CcState.ReleaseDraft)).isEqualTo("Ready for release");
        assertThat(empty.isRemoveState(CcState.Published)).isTrue();
    }

    @Test
    void honoursConfiguredFieldOptionsOverTheDefaults() {
        ProjectFieldOptions custom = new ProjectFieldOptions(
                Map.of("WIP", "In Progress", "Candidate", "Review"), "Backlog");
        assertThat(custom.fieldOptionFor(CcState.WIP)).isEqualTo("In Progress");
        assertThat(custom.fieldOptionFor(CcState.Candidate)).isEqualTo("Review");
        // Draft was not configured, so it has no fieldOption (a non-empty configured map is authoritative).
        assertThat(custom.fieldOptionFor(CcState.Draft)).isNull();
        assertThat(custom.getDefaultFieldOption()).isEqualTo("Backlog");
        // Removal is fixed to Published regardless of the fieldOption configuration.
        assertThat(custom.isRemoveState(CcState.Published)).isTrue();
        assertThat(custom.isRemoveState(CcState.Production)).isFalse();
    }

    @Test
    void ignoresUndefinedStatesAndBlankFieldOptionsAndMatchesStateNamesCaseInsensitively() {
        Map<String, String> fieldOptionByState = new HashMap<>();
        fieldOptionByState.put("wip", "Implementing");   // lower-case state name still resolves to CcState.WIP
        fieldOptionByState.put("NotAState", "Whatever"); // not a CcState -> ignored
        fieldOptionByState.put("Draft", "   ");          // blank fieldOption -> ignored

        ProjectFieldOptions p = new ProjectFieldOptions(fieldOptionByState, "New");

        assertThat(p.fieldOptionFor(CcState.WIP)).isEqualTo("Implementing");
        assertThat(p.fieldOptionFor(CcState.Draft)).isNull();
        assertThat(p.stateFieldOptionNames()).containsOnlyKeys("WIP");
        // Removal is fixed to Published (not configured).
        assertThat(p.isRemoveState(CcState.Published)).isTrue();
    }
}
