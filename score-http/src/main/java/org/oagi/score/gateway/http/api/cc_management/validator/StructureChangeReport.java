package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.validation.ValidationReport;

/**
 * What a change of structure would break: the problems that the change would make (the ones the ACCs it affects do
 * not have already), each about the ACC that would have it, and how many ACCs were checked to find them.
 */
public record StructureChangeReport(ValidationReport report, int checkedCount) {
}
