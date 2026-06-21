package org.oagi.score.gateway.http.api.integration_management.github.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;

/**
 * The ownership and lifecycle state of a component (ACC/ASCCP/BCCP/DT/code list/agency ID list),
 * resolved by manifest id. Used to enforce that only the owner may change GitHub issue links and
 * only while the component is in WIP.
 *
 * @param ownerUserId the component owner, or {@code null} if unset
 * @param state       the component lifecycle state (e.g. {@code WIP})
 */
public record ComponentOwnerState(UserId ownerUserId, String state) {
}
