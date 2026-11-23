package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DtQueryRepository {

    DtDetailsRecord getDtDetails(DtManifestId dtManifestId);

    DtDetailsRecord getPrevDtDetails(DtManifestId dtManifestId);

    DtSummaryRecord getDtSummary(DtManifestId dtManifestId);

    List<DtSummaryRecord> getDtSummaryListByManifestIds(Collection<DtManifestId> dtManifestIdList);

    List<DtSummaryRecord> getDtSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtSummaryRecord> getDtSummaryList(ReleaseId releaseId);

    List<DtSummaryRecord> getDtSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    List<DtSummaryRecord> getInheritedDtSummaryList(DtManifestId basedDtManifestId);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(ReleaseId releaseId);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(DtManifestId dtManifestId);

    /** The allowed primitives of these data types in every release that has them; the caller picks the release. */
    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryListByDtIds(Collection<DtId> dtIds);

    DtAwdPriSummaryRecord getDefaultDtAwdPriSummary(DtManifestId dtManifestId);

    /**
     * The allowed primitive each data type of the release starts with (see {@link DefaultAwdPriRule}), by data type. A
     * data type that has no allowed primitive to start with has no entry.
     */
    Map<DtId, DtAwdPriSummaryRecord> getDefaultDtAwdPriSummaryMap(ReleaseId releaseId);

    List<DtAwdPriDetailsRecord> getDefaultPrimitiveValues(String representationTerm);


    // DT_SC
    DtScDetailsRecord getDtScDetails(DtScManifestId dtScManifestId);

    List<DtScDetailsRecord> getDtScDetailsList(DtManifestId dtManifestId);

    DtScSummaryRecord getDtScSummary(DtScManifestId dtScManifestId);

    List<DtScSummaryRecord> getInheritedDtScSummaryList(DtScManifestId basedDtScManifestId);

    List<DtScSummaryRecord> getDtScSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtScSummaryRecord> getDtScSummaryList(DtManifestId ownerDtManifestId);

    List<DtScSummaryRecord> getDtScSummaryListByOwnerDtManifestIds(Collection<DtManifestId> ownerDtManifestIdList);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(ReleaseId releaseId);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(DtScManifestId dtScManifestId);

    /** The allowed primitives of these supplementary components in every release that has them; the caller picks the release. */
    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryListByDtScIds(Collection<DtScId> dtScIds);

    DtScAwdPriSummaryRecord getDefaultDtScAwdPriSummary(DtScManifestId dtScManifestId);

    /** Like {@link #getDefaultDtAwdPriSummaryMap}, for the supplementary components of the release. */
    Map<DtScId, DtScAwdPriSummaryRecord> getDefaultDtScAwdPriSummaryMap(ReleaseId releaseId);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    boolean hasDuplicateSixDigitId(DtManifestId dtManifestId, String sixDigitId);

    boolean hasSamePropertyTerm(DtScManifestId dtScManifestId, String propertyTerm);

    boolean hasSameDen(DtScManifestId dtScManifestId, String objectClassTerm, String propertyTerm, String representationTerm);

}
