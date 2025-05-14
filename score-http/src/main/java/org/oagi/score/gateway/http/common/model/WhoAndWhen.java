package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;

import java.util.Date;

/**
 * Represents audit information for an entity.
 * Contains details about the individual who performed the action and the timestamp
 * when the action was performed.
 */
public record WhoAndWhen(UserSummaryRecord who, Date when) {
}
