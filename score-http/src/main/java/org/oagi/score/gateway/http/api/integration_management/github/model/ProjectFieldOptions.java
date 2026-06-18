package org.oagi.score.gateway.http.api.integration_management.github.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The action a component's {@link CcState} drives on the GitHub Projects v2 board (issue #1533,
 * Feature 2; OAGi wiki {@code connectSpec-issue-state-management}), keyed on the <em>destination</em>
 * state of a transition: a state maps either to a fieldOption (move the card there), to "remove" (take the
 * card off the board), or to nothing.
 *
 * <p>The {@code <CcState> -> <fieldOption name>} mapping is configurable via
 * {@code score.integration.github.project-field-option-by-state} (e.g. {@code WIP: Implementing}; default:
 * WIP=Implementing, Draft=Implemented, Candidate=Candidate, ReleaseDraft="Ready for release"). Keys
 * that are not defined {@link CcState} values are ignored (case-insensitively), as are blank fieldOption
 * names. When nothing is configured the built-in defaults apply. The frontend reads the resolved fieldOption
 * map via {@code GET /status} so the dialog fieldOption preview matches the backend.</p>
 *
 * <p>Removing a card from the board is fixed rather than configured: it happens in exactly two places
 * — when a component reaches {@link CcState#Published} (released) and when an issue is unlinked (the
 * latter handled at the link service) — so {@link #isRemoveState} is hard-wired to {@code Published}.</p>
 */
@Component
public class ProjectFieldOptions {

    public static final String FIELD_OPTION_CONFIG_KEY = "score.integration.github.project-field-option-by-state";
    public static final String DEFAULT_FIELD_OPTION_CONFIG_KEY = "score.integration.github.project-default-field-option";

    /** Applied when no fieldOption map is configured. */
    private static final Map<CcState, String> DEFAULT_FIELD_OPTION_BY_STATE = Map.of(
            CcState.WIP, "Implementing",
            CcState.Draft, "Implemented",
            CcState.Candidate, "Candidate",
            CcState.ReleaseDraft, "Ready for release");

    /**
     * FieldOptions the maintainers own on the board — the sync never overwrites a card already sitting here, so
     * a state-driven move cannot undo a maintainer's "this is in member review" decision. {@code Ready
     * for release} is NOT a gate fieldOption: it is now driven by the {@code ReleaseDraft} state. Not configurable.
     */
    private static final Set<String> GATE_FIELD_OPTIONS = Set.of("Member review");

    private final Map<CcState, String> fieldOptionByState;
    private final String defaultFieldOption;

    @Autowired
    public ProjectFieldOptions(Environment environment) {
        this(Binder.get(environment).bind(FIELD_OPTION_CONFIG_KEY, Bindable.mapOf(String.class, String.class))
                        .orElseGet(Map::of),
                Binder.get(environment).bind(DEFAULT_FIELD_OPTION_CONFIG_KEY, Bindable.of(String.class))
                        .orElse(null));
    }

    /** Builds the mapping from raw config (also used by tests). */
    public ProjectFieldOptions(Map<String, String> configuredFieldOptionByState, String configuredInitialFieldOption) {
        this.fieldOptionByState = buildFieldOptionByState(configuredFieldOptionByState);
        // The initial/reset fieldOption is OPTIONAL: GitHub's API does not expose a single-select field's
        // "Default" value, so it cannot be read from the board; an operator sets it to match the board's
        // Status default. Blank/unset -> no initial fieldOption: a freshly linked issue is not placed, and a
        // cancelled revision is not reset (the move simply no-ops). ("If there is no default, don't set it.")
        this.defaultFieldOption = (configuredInitialFieldOption == null || configuredInitialFieldOption.isBlank())
                ? null : configuredInitialFieldOption.trim();
    }

    private static Map<CcState, String> buildFieldOptionByState(Map<String, String> configured) {
        Map<CcState, String> result = new EnumMap<>(CcState.class);
        if (configured != null) {
            configured.forEach((stateName, fieldOption) -> {
                if (stateName == null || fieldOption == null || fieldOption.isBlank()) {
                    return;
                }
                CcState state = parseState(stateName.trim());
                if (state != null) {
                    result.put(state, fieldOption.trim());
                }
            });
        }
        return result.isEmpty() ? new EnumMap<>(DEFAULT_FIELD_OPTION_BY_STATE) : result;
    }

    /** Matches a configured key to a {@link CcState} by name, case-insensitively; null if undefined. */
    private static CcState parseState(String stateName) {
        for (CcState state : CcState.values()) {
            if (state.name().equalsIgnoreCase(stateName)) {
                return state;
            }
        }
        return null;
    }

    /** The board fieldOption a component lands in when it enters {@code state}, or {@code null} for no fieldOption. */
    public String fieldOptionFor(CcState state) {
        return (state == null) ? null : fieldOptionByState.get(state);
    }

    /**
     * Whether entering {@code state} removes the component's card from the board. Fixed to
     * {@link CcState#Published} (released): a card leaves the board only on release or when its issue
     * is unlinked — there is nothing to configure.
     */
    public boolean isRemoveState(CcState state) {
        return state == CcState.Published;
    }

    /**
     * The initial/reset fieldOption — where a freshly linked issue is added and a cancelled revision is reset —
     * or {@code null} when none is configured (then those placements no-op).
     */
    public String getDefaultFieldOption() {
        return defaultFieldOption;
    }

    /**
     * Whether the transition is a cancel-revision revert — WIP back to the previously released state
     * (Published for developers, Production for end-users) — which resets the card to the initial fieldOption.
     * This takes precedence over the remove action (a revert is not a release).
     */
    public boolean isRevertToInitial(CcState prevState, CcState nextState) {
        return prevState == CcState.WIP && (nextState == CcState.Published || nextState == CcState.Production);
    }

    /** Whether {@code fieldOption} is a maintainer-owned gate fieldOption the sync must not overwrite. */
    public boolean isGateFieldOption(String fieldOption) {
        return fieldOption != null && GATE_FIELD_OPTIONS.contains(fieldOption);
    }

    /** The distinct fieldOption names this mapping writes (incl. the initial fieldOption) — used to auto-discover the
     * board's fieldOption field. */
    public Set<String> fieldOptionNames() {
        Set<String> names = new HashSet<>(fieldOptionByState.values());
        if (defaultFieldOption != null && !defaultFieldOption.isBlank()) {
            names.add(defaultFieldOption);
        }
        return names;
    }

    /**
     * The fieldOption mapping as {@code CcState name -> fieldOption name} (e.g. {@code "WIP" -> "Implementing"}), the
     * form the frontend reads so the dialog fieldOption preview has a single source of truth (this bean) and
     * never drifts. Remove states are not fieldOptionByState and are not included.
     */
    public Map<String, String> stateFieldOptionNames() {
        Map<String, String> byState = new LinkedHashMap<>();
        fieldOptionByState.forEach((state, fieldOption) -> byState.put(state.name(), fieldOption));
        return byState;
    }
}
