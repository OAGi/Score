package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListRecordTypes;
import org.oagi.score.gateway.http.api.account_management.model.AccountRecordTypes;
import org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListRecordTypes;
import org.oagi.score.gateway.http.api.library_management.model.LibraryRecordTypes;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseRecordTypes;
import org.oagi.score.gateway.http.api.tag_management.model.TagRecordTypes;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtRecordTypes;
import org.oagi.score.gateway.http.cache.RecordKeyProvider;
import org.oagi.score.gateway.http.change.RecordRef;
import org.oagi.score.gateway.http.change.RecordType;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The cache keys of the core components. {@link CachedCcDocument} stores under the keys made here, and the same keys are what a
 * change forgets.
 */
@Component
public class CcRecordKeys implements RecordKeyProvider {

    private static final Set<RecordType> TYPES = Set.of(
            CcRecordTypes.ACC, CcRecordTypes.ASCC, CcRecordTypes.BCC, CcRecordTypes.ASCCP, CcRecordTypes.BCCP, CcRecordTypes.DT,
            CcRecordTypes.DT_SC);

    private static final Set<String> TABLES = Set.of(
            "acc", "acc_manifest", "ascc", "ascc_manifest", "bcc", "bcc_manifest",
            "asccp", "asccp_manifest", "bccp", "bccp_manifest",
            "dt", "dt_manifest", "dt_sc", "dt_sc_manifest",
            "seq_key", "tag", "acc_manifest_tag", "asccp_manifest_tag", "bccp_manifest_tag", "dt_manifest_tag",
            "log", "release", "release_dep", "library", "app_user");

    private final AffectedRecordsRepository affected;

    @Autowired
    public CcRecordKeys(RepositoryFactory repositoryFactory) {
        this(repositoryFactory.affectedRecordsRepository());
    }

    public CcRecordKeys(AffectedRecordsRepository affected) {
        this.affected = affected;
    }

    public static String acc(BigInteger id) {
        return "acc:" + id;
    }

    public static String ascc(BigInteger id) {
        return "ascc:" + id;
    }

    public static String bcc(BigInteger id) {
        return "bcc:" + id;
    }

    public static String asccp(BigInteger id) {
        return "asccp:" + id;
    }

    public static String bccp(BigInteger id) {
        return "bccp:" + id;
    }

    public static String dt(BigInteger id) {
        return "dt:" + id;
    }

    public static String dtSc(BigInteger id) {
        return "dt_sc:" + id;
    }

    /** The ASCCs of an ACC, which are stored as one list. */
    public static String asccFromAcc(BigInteger accManifestId) {
        return "ascc-from-acc:" + accManifestId;
    }

    /** The BCCs of an ACC, which are stored as one list. */
    public static String bccFromAcc(BigInteger accManifestId) {
        return "bcc-from-acc:" + accManifestId;
    }

    /** The order of the associations of an ACC. */
    public static String seqKeyFromAcc(BigInteger accManifestId) {
        return "seq-key-from-acc:" + accManifestId;
    }

    /** The supplementary components of a DT, which are stored as one list. */
    public static String dtScOfDt(BigInteger dtManifestId) {
        return "dt_sc-of-dt:" + dtManifestId;
    }

    public static String dtAwdPri(BigInteger dtManifestId) {
        return "dt-awd-pri:" + dtManifestId;
    }

    public static String dtScAwdPri(BigInteger dtScManifestId) {
        return "dt_sc-awd-pri:" + dtScManifestId;
    }

    /** The DTs of a release, which are stored as one list. */
    public static String dts(BigInteger releaseId) {
        return "dts:" + releaseId;
    }

    /** The tags of a component. */
    public static String tags(CcType type, BigInteger manifestId) {
        return "tags:" + type.name().toLowerCase(Locale.ROOT) + ":" + manifestId;
    }

    /**
     * What a document reads ahead for a component. It is only a hint, read again when it is stale, so no change needs to forget it.
     */
    public static String dependencies(boolean withDtRelated, CcType type, BigInteger rootManifestId) {
        return (withDtRelated ? "dependencies-dt:" : "dependencies:") + type.name().toLowerCase(Locale.ROOT) + ":" + rootManifestId;
    }

    /** The releases that a document includes. */
    public static String scope(BigInteger releaseId) {
        return "scope:" + releaseId;
    }

    @Override
    public Set<RecordType> types() {
        return TYPES;
    }

    @Override
    public void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        if (type.equals(CcRecordTypes.ACC)) {
            keys.add(acc(id));
            keys.add(tags(CcType.ACC, id));
            addAssociationsOf(keys, id);
        } else if (type.equals(CcRecordTypes.ASCC)) {
            keys.add(ascc(id));
            addAssociationsOf(keys, parentId);
        } else if (type.equals(CcRecordTypes.BCC)) {
            keys.add(bcc(id));
            addAssociationsOf(keys, parentId);
        } else if (type.equals(CcRecordTypes.ASCCP)) {
            keys.add(asccp(id));
            keys.add(tags(CcType.ASCCP, id));
        } else if (type.equals(CcRecordTypes.BCCP)) {
            keys.add(bccp(id));
            keys.add(tags(CcType.BCCP, id));
        } else if (type.equals(CcRecordTypes.DT)) {
            keys.add(dt(id));
            keys.add(tags(CcType.DT, id));
            keys.add(dtScOfDt(id));
            keys.add(dtAwdPri(id));
            if (releaseId != null) {
                keys.add(dts(releaseId));
            }
        } else if (type.equals(CcRecordTypes.DT_SC)) {
            keys.add(dtSc(id));
            keys.add(dtScAwdPri(id));
            if (parentId != null) {
                keys.add(dtScOfDt(parentId));
            }
        }
    }

    /** The associations of an ACC (and their order) are stored per ACC, so a change to one of them reaches all of them. */
    private static void addAssociationsOf(Set<String> keys, BigInteger accManifestId) {
        if (accManifestId != null) {
            keys.add(asccFromAcc(accManifestId));
            keys.add(bccFromAcc(accManifestId));
            keys.add(seqKeyFromAcc(accManifestId));
        }
    }

    @Override
    public List<RecordRef> recordsReachedBy(RecordType kind, Collection<BigInteger> ids) {
        if (kind.equals(ReleaseRecordTypes.RELEASE)) {
            return flatten(ids, affected::recordsOfRelease);
        } else if (kind.equals(LibraryRecordTypes.LIBRARY)) {
            return flatten(ids, affected::recordsOfLibrary);
        } else if (kind.equals(AccountRecordTypes.USER)) {
            return flatten(ids, affected::recordsOfUser);
        } else if (kind.equals(TagRecordTypes.TAG)) {
            return flatten(ids, affected::recordsTaggedWith);
        } else if (kind.equals(XbtRecordTypes.XBT) || kind.equals(CodeListRecordTypes.CODE_LIST)
                || kind.equals(AgencyIdListRecordTypes.AGENCY_ID_LIST)) {
            // the data types show the names of what their allowed primitives are
            return affected.recordsShowing(kind, ids);
        }
        return List.of();
    }

    private static List<RecordRef> flatten(Collection<BigInteger> ids, Function<BigInteger, List<RecordRef>> lookup) {
        return ids.stream().flatMap(id -> lookup.apply(id).stream()).toList();
    }

    @Override
    public Set<String> keysReachedBy(RecordType kind, Collection<BigInteger> ids) {
        if (!kind.equals(ReleaseRecordTypes.RELEASE_DEP)) {
            return Set.of();
        }
        return affected.allReleaseIds().stream().map(CcRecordKeys::scope).collect(Collectors.toSet());
    }

    @Override
    public Set<String> tables() {
        return TABLES;
    }
}
