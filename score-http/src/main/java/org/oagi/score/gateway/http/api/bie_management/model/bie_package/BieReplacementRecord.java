package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * A BIE of a BIE package that replaces another BIE of the package.
 *
 * @param topLevelAsbiepId     the BIE that replaces
 * @param prevTopLevelAsbiepId the BIE that is replaced ({@code prev_top_level_asbiep_id})
 */
public record BieReplacementRecord(TopLevelAsbiepId topLevelAsbiepId,
                                   TopLevelAsbiepId prevTopLevelAsbiepId) {
}
