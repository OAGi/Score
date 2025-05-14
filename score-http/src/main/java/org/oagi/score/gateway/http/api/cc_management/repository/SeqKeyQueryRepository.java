package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface SeqKeyQueryRepository {

    SeqKeySummaryRecord getSeqKeySummary(SeqKeyId seqKeyId);

    List<SeqKeySummaryRecord> getSeqKeySummaryList(Collection<ReleaseId> releaseIdList);

    List<SeqKeySummaryRecord> getSeqKeySummaryList(AccManifestId fromAccManifestId);

}
