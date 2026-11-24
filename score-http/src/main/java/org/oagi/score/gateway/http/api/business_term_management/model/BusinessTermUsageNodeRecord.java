package org.oagi.score.gateway.http.api.business_term_management.model;

import java.math.BigInteger;
import java.util.List;

/**
 * A BIE node (ASBIE or BBIE) that the business term is assigned to. The path and its hash tell where the node is in the
 * BIE tree, which the client draws.
 */
public record BusinessTermUsageNodeRecord(
        String bieType,
        BigInteger bieId,
        String path,
        String hashPath,
        List<Assignment> assignments) {

    public record Assignment(
            BusinessTermId assignedBizTermId,
            String typeCode,
            boolean primaryIndicator) {
    }
}
