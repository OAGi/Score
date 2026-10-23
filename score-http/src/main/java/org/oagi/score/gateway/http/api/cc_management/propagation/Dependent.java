package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.Id;

/**
 * A record found by a reverse lookup: {@code id} depends on {@code source} (for example the ASCC that points to an ASCCP).
 *
 * @param parent  the ACC that owns the ASCC or BCC; otherwise null
 * @param release the release of the record, when the lookup knows it
 */
public record Dependent<S extends Id, D extends Id>(S source, D id, AccManifestId parent, ReleaseId release) {

    public Dependent(S source, D id) {
        this(source, id, null, null);
    }

    public Dependent(S source, D id, AccManifestId parent) {
        this(source, id, parent, null);
    }
}
