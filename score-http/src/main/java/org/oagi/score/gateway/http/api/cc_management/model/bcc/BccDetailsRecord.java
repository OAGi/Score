package org.oagi.score.gateway.http.api.cc_management.model.bcc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.Objects;

public record BccDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        BccManifestId bccManifestId,
        BccId bccId,
        Guid guid,

        AccSummaryRecord fromAcc,
        BccpSummaryRecord toBccp,
        SeqKeyId seqKeyId,
        SeqKeyId prevSeqKeyId,
        SeqKeyId nextSeqKeyId,
        BccSummaryRecord replacement,
        BccSummaryRecord since,
        BccSummaryRecord lastChanged,

        EntityType entityType,
        String den,
        Cardinality cardinality,
        boolean deprecated,
        boolean nillable,
        CcState state,
        ValueConstraint valueConstraint,
        Definition definition,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        BccManifestId prevBccManifestId,
        BccManifestId nextBccManifestId) implements CoreComponent<BccId>, CcAssociation {

    @Override
    public BccId getId() {
        return bccId;
    }

    @Override
    public boolean isManifest() {
        return false;
    }

    @Override
    public boolean isAscc() {
        return false;
    }

    @Override
    public boolean isBcc() {
        return true;
    }

    @Override
    public SeqKeyId seqKeyId() {
        return seqKeyId;
    }

    @Override
    public SeqKeyId prevSeqKeyId() {
        return prevSeqKeyId;
    }

    @Override
    public SeqKeyId nextSeqKeyId() {
        return nextSeqKeyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BccDetailsRecord that = (BccDetailsRecord) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(bccManifestId, that.bccManifestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bccManifestId, isManifest(), isAscc(), isBcc());
    }

}
