package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface CcDocument {

    AccSummaryRecord getAcc(AccManifestId accManifestId);

    List<CcAssociation> getAssociations(AccSummaryRecord accManifest);

    AsccSummaryRecord getAscc(AsccManifestId asccManifestId);

    BccSummaryRecord getBcc(BccManifestId bccManifestId);

    AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId);

    Collection<AsccpSummaryRecord> getAsccpList();

    BccpSummaryRecord getBccp(BccpManifestId bccpManifestId);

    DtSummaryRecord getDt(DtManifestId dtManifestId);

    List<DtSummaryRecord> getDtList();

    DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId);

    List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId);

    List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId);

    XbtSummaryRecord getXbt(XbtManifestId xbtManifestId);

    List<XbtSummaryRecord> getXbtListByName(String name);

    List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId);

    /** Whether any ACC has the given ACC as its base; a document that knows it without the ACCs overrides this. */
    default boolean hasAccByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return !getAccListByBasedAccManifestId(basedAccManifestId).isEmpty();
    }

    /**
     * The ACCs that have each of the given ACCs as their base, by base. A document that can answer many at once
     * (one read for all of them) overrides this; the default asks one by one.
     */
    default Map<AccManifestId, List<AccSummaryRecord>> getAccListByBasedAccManifestIds(Collection<AccManifestId> basedAccManifestIds) {
        Map<AccManifestId, List<AccSummaryRecord>> result = new LinkedHashMap<>();
        for (AccManifestId basedAccManifestId : basedAccManifestIds) {
            result.put(basedAccManifestId, getAccListByBasedAccManifestId(basedAccManifestId));
        }
        return result;
    }

    List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId);

    List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId);

    List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId);

    List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId);

    List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId);

    List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId);

    List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId);

    List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId);

    List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId);

    List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId);

    List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId);

    List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId);

    List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId);

    NamespaceSummaryRecord getNamespace(NamespaceId namespaceId);

    CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId);

    AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId);

    BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId);

    /**
     * A hint that the children of these components are about to be looked up, one by one.
     * A document that loads on demand can fetch them all at once instead; the default does nothing, and the
     * answers of the lookups never depend on it.
     */
    default void prefetchChildren(CcType type, Collection<? extends ManifestId> manifestIds) {
    }

    /**
     * A hint that the dependencies of this component are about to be walked, so that a document that loads on demand
     * can fetch everything it depends on at once. Like {@link #prefetchChildren} it changes no answer.
     */
    default void prefetchDependencies(CcType type, ManifestId root) {
    }

}
