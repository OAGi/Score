package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirement;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirementScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Issue #1729: shared helpers for persisting Security Requirement entries (document-level and
 * operation-level) as a diff rather than delete-then-reinsert. A requirement entry's identity is
 * (requirement_group, oas_security_scheme_id) — group = the OR index, schemes sharing a group are ANDed,
 * a null scheme id is an anonymous {} requirement. The only mutable content is the free-text scope name
 * list (reconciled per entry by each repository), so there is nothing else to "update".
 */
final class OasSecurityRequirementDiff {

    private OasSecurityRequirementDiff() {
    }

    /** A desired requirement entry: the OR-group index, the referenced scheme id (null = anonymous), scopes. */
    static final class Entry {
        final int group;
        final ULong schemeId;
        final List<String> scopes;

        Entry(int group, ULong schemeId, List<String> scopes) {
            this.group = group;
            this.schemeId = schemeId;
            this.scopes = scopes;
        }
    }

    /** Stable map key for a (requirement_group, scheme id) pair; a null scheme id is anonymous. */
    static String key(int requirementGroup, ULong oasSecuritySchemeId) {
        return requirementGroup + ":" + (oasSecuritySchemeId == null ? "" : oasSecuritySchemeId.toString());
    }

    /** Trim, drop blanks and de-duplicate scope names, preserving order. */
    static List<String> normalizeScopes(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String scope : scopes) {
            if (scope == null || scope.isBlank()) {
                continue;
            }
            String trimmed = scope.trim();
            if (seen.add(trimmed)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    /**
     * Build the desired requirement entries keyed by {@link #key}. The requirement index is the OR group;
     * within a group each scheme is ANDed. A scheme reference is resolved by name to its id; a name that
     * does not resolve to a declared scheme is skipped. Anonymous requirements map to a null scheme id.
     */
    static LinkedHashMap<String, Entry> build(List<OasSecurityRequirement> requirements,
                                              Map<String, ULong> schemeIdByName) {
        LinkedHashMap<String, Entry> desired = new LinkedHashMap<>();
        if (requirements == null) {
            return desired;
        }
        for (int i = 0; i < requirements.size(); i++) {
            OasSecurityRequirement requirement = requirements.get(i);
            if (requirement == null) {
                continue;
            }
            if (requirement.isAnonymous()) {
                desired.putIfAbsent(key(i, null), new Entry(i, null, Collections.emptyList()));
                continue;
            }
            if (requirement.getSchemes() == null) {
                continue;
            }
            for (OasSecurityRequirementScheme scheme : requirement.getSchemes()) {
                if (scheme == null || scheme.getSchemeName() == null || scheme.getSchemeName().isBlank()) {
                    continue;
                }
                ULong oasSecuritySchemeId = (schemeIdByName == null)
                        ? null : schemeIdByName.get(scheme.getSchemeName().trim());
                if (oasSecuritySchemeId == null) {
                    continue;
                }
                String entryKey = key(i, oasSecuritySchemeId);
                List<String> scopes = normalizeScopes(scheme.getScopes());
                Entry existing = desired.get(entryKey);
                if (existing == null) {
                    desired.put(entryKey, new Entry(i, oasSecuritySchemeId, scopes));
                } else {
                    // The same scheme referenced more than once within one requirement object (its map key
                    // is unique) -> union the scopes instead of silently dropping the later occurrence's.
                    List<String> merged = new ArrayList<>(existing.scopes);
                    for (String scope : scopes) {
                        if (!merged.contains(scope)) {
                            merged.add(scope);
                        }
                    }
                    desired.put(entryKey, new Entry(i, oasSecuritySchemeId, merged));
                }
            }
        }
        return desired;
    }
}
