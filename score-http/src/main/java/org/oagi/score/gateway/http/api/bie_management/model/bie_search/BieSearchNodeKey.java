package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

/**
 * The place of a BIE that a predicate holds for.
 *
 * @param table    the table of the row that holds the text
 * @param hashPath the hash path of the row, unique in the BIE
 * @param path     the path of the row (what the hash path is made from), which lets a client find the place without
 *                 having loaded it; null for a row that has none
 */
public record BieSearchNodeKey(BieSearchTable table, String hashPath, String path) {
}
