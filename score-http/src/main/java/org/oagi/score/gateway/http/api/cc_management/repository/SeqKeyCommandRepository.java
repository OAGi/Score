package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.AsccpOrBccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public interface SeqKeyCommandRepository {

    SeqKeyId create(AccManifestId fromAccManifestId, AsccManifestId asccManifestId);

    SeqKeyId create(AccManifestId fromAccManifestId, BccManifestId bccManifestId);

    void move(AccManifestId accManifestId, AsccpOrBccpManifestId item, AsccpOrBccpManifestId after);

    void moveAfter(SeqKeySummaryRecord current, SeqKeySummaryRecord after);

    boolean updatePrev(SeqKeyId current, SeqKeyId prev);

    boolean updateNext(SeqKeyId current, SeqKeyId next);

    boolean delete(SeqKeyId seqKeyId);

    void delete(ReleaseId releaseId);

}
