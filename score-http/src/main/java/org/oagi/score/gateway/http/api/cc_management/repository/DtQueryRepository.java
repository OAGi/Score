package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface DtQueryRepository {

    DtDetailsRecord getDtDetails(DtManifestId dtManifestId);

    DtDetailsRecord getPrevDtDetails(DtManifestId dtManifestId);

    DtSummaryRecord getDtSummary(DtManifestId dtManifestId);

    List<DtSummaryRecord> getDtSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtSummaryRecord> getDtSummaryList(ReleaseId releaseId);

    List<DtSummaryRecord> getDtSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    List<DtSummaryRecord> getInheritedDtSummaryList(DtManifestId basedDtManifestId);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(ReleaseId releaseId);

    List<DtAwdPriSummaryRecord> getDtAwdPriSummaryList(DtManifestId dtManifestId);

    DtAwdPriSummaryRecord getDefaultDtAwdPriSummary(DtManifestId dtManifestId);

    List<DtAwdPriDetailsRecord> getDefaultPrimitiveValues(String representationTerm);


    // DT_SC
    DtScDetailsRecord getDtScDetails(DtScManifestId dtScManifestId);

    List<DtScDetailsRecord> getDtScDetailsList(DtManifestId dtManifestId);

    DtScSummaryRecord getDtScSummary(DtScManifestId dtScManifestId);

    List<DtScSummaryRecord> getInheritedDtScSummaryList(DtScManifestId basedDtScManifestId);

    List<DtScSummaryRecord> getDtScSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtScSummaryRecord> getDtScSummaryList(DtManifestId ownerDtManifestId);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(Collection<ReleaseId> releaseIdList);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(ReleaseId releaseId);

    List<DtScAwdPriSummaryRecord> getDtScAwdPriSummaryList(DtScManifestId dtScManifestId);

    DtScAwdPriSummaryRecord getDefaultDtScAwdPriSummary(DtScManifestId dtScManifestId);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    boolean hasDuplicateSixDigitId(DtManifestId dtManifestId);

    boolean hasSamePropertyTerm(DtScManifestId dtScManifestId, String propertyTerm);

    boolean hasSameDen(DtScManifestId dtScManifestId, String objectClassTerm, String propertyTerm, String representationTerm);

}
