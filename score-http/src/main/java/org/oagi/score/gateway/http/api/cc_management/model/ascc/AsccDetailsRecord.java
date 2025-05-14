package org.oagi.score.gateway.http.api.cc_management.model.ascc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.Objects;

public record AsccDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AsccManifestId asccManifestId,
        AsccId asccId,
        Guid guid,

        AccSummaryRecord fromAcc,
        AsccpSummaryRecord toAsccp,
        SeqKeyId seqKeyId,
        SeqKeyId prevSeqKeyId,
        SeqKeyId nextSeqKeyId,
        AsccSummaryRecord replacement,
        AsccSummaryRecord since,
        AsccSummaryRecord lastChanged,

        String den,
        Cardinality cardinality,
        boolean deprecated,
        CcState state,
        Definition definition,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        AsccManifestId prevAsccManifestId,
        AsccManifestId nextAsccManifestId) implements CoreComponent<AsccId>, CcAssociation {

    @Override
    public AsccId getId() {
        return asccId;
    }

    @Override
    public boolean isManifest() {
        return false;
    }

    @Override
    public boolean isAscc() {
        return true;
    }

    @Override
    public boolean isBcc() {
        return false;
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
        AsccDetailsRecord that = (AsccDetailsRecord) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(asccManifestId, that.asccManifestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asccManifestId, isManifest(), isAscc(), isBcc());
    }

}
