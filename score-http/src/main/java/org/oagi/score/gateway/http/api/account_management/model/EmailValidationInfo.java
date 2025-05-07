package org.oagi.score.gateway.http.api.account_management.model;

import java.util.Date;

public record EmailValidationInfo(UserId userId, String email, Date timestamp) {
}
