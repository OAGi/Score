package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;

public interface CcAssociation extends SeqKeySupportable {

    boolean isManifest();

    boolean isAscc();

    boolean isBcc();

}
