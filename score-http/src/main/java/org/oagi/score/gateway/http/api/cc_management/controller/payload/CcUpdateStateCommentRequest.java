package org.oagi.score.gateway.http.api.cc_management.controller.payload;

/**
 * The optional JSON body of the single-component state-change endpoints (issue #1533, sub-task 5):
 * the GitHub status comment to post verbatim on the component's linked issues. The frontend
 * pre-fills it with the rendered change summary and the acting user edits it freely before
 * confirming. The body — and the comment within it — may be absent; both mean "post nothing"
 * (user opt-out) and behave exactly like a body-less request.
 */
public record CcUpdateStateCommentRequest(String comment) {
}
