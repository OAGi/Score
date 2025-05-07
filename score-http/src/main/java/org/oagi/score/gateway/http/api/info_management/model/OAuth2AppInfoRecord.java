package org.oagi.score.gateway.http.api.info_management.model;

public record OAuth2AppInfoRecord(
        String loginUrl,
        String providerName,
        String displayProviderName,
        String backgroundColor,
        String fontColor) {
}