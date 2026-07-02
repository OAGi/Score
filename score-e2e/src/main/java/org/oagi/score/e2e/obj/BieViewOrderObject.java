package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

/**
 * A single sibling view-order weight row (issue #1638). Keyed by the view-parent ACC
 * ({@code fromAccManifestId}); exactly one of {@code asccManifestId} / {@code bccManifestId}
 * identifies the reordered child.
 */
@Data
public class BieViewOrderObject {

    private BigInteger bieViewOrderId;

    private BigInteger fromAccManifestId;

    private BigInteger asccManifestId;

    private BigInteger bccManifestId;

    private int weight;

}
