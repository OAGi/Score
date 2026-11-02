package org.oagi.score.gateway.http.change;


import java.math.BigInteger;

/** A record named by its type and manifest id; {@code parentId} is the ACC or DT it belongs to and {@code releaseId} its release, when it has them. */
public record RecordRef(RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {

    public RecordRef(RecordType type, BigInteger id, BigInteger parentId) {
        this(type, id, parentId, null);
    }
}
