package org.oagi.score.gateway.http.api.log_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcType;

import java.util.List;

/**
 * A child element of a component in a {@link ComponentChangeSummary} (issue #1533): an ACC
 * association, or a DT supplementary
 * component; {@code type} is {@link CcType#ASCC}, {@link CcType#BCC} or {@link CcType#DT_SC}. Used both for the {@code NEW} state listing and for the added/removed
 * buckets of a {@code REVISED} summary.
 */
public record ComponentChildItem(CcType type, String name, List<ComponentSummaryField> fields) {
}
