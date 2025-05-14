package org.oagi.score.gateway.http.api.cc_management.model.seq_key;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;

public record SeqKeySummaryRecord(
        SeqKeyId seqKeyId,

        AccManifestId fromAccManifestId,
        @Nullable AsccManifestId asccManifestId,
        @Nullable BccManifestId bccManifestId,
        @Nullable EntityType entityType,

        @Nullable SeqKeyId prevSeqKeyId,
        @Nullable SeqKeyId nextSeqKeyId) implements SeqKeySupportable {

}
