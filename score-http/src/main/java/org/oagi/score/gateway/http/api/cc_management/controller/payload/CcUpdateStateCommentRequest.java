package org.oagi.score.gateway.http.api.cc_management.controller.payload;

/**
 * The optional JSON body of the single-component state-change endpoints (issue #1533): the GitHub
 * status comment to post verbatim on the component's linked issues (sub-task 5), and an optional
 * Projects v2 board fieldOption the acting user picked to override the configured destination fieldOption for this
 * transition (Feature 2). The frontend pre-fills the comment with the rendered change summary and
 * the acting user edits it freely before confirming. The body and both fields may be absent: an
 * absent comment means "post nothing" (opt-out), an absent {@code projectFieldOptionOverride} means "use
 * the configured fieldOption". A body-less request behaves exactly like both being absent.
 */
public record CcUpdateStateCommentRequest(String comment, String projectFieldOptionOverride) {
}
