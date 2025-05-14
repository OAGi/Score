package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcRefactorValidationResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface AccQueryRepository {

    AccDetailsRecord getAccDetails(AccManifestId accManifestId);

    AccSummaryRecord getAccSummary(AccManifestId accManifestId);

    List<AccSummaryRecord> getAccSummaryList(Collection<ReleaseId> releaseIdList);

    List<AccSummaryRecord> getAccSummaryList(Collection<ReleaseId> releaseIdList, String objectClassTerm);

    List<AccSummaryRecord> getAccSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    List<AccSummaryRecord> getInheritedAccSummaryList(AccManifestId accManifestId);

    List<CcAssociation> getAssociationSummaryList(AccManifestId accManifestId);

    AccDetailsRecord getPrevAccDetails(AccManifestId accManifestId);

    // ASCC
    AsccDetailsRecord getAsccDetails(AsccManifestId asccManifestId);

    List<AsccDetailsRecord> getAsccDetailsList();

    List<AsccDetailsRecord> getAsccDetailsList(ReleaseId releaseId);

    List<AsccDetailsRecord> getAsccDetailsList(AccManifestId accManifestId);

    AsccSummaryRecord getAsccSummary(AsccManifestId asccManifestId);

    AsccSummaryRecord getAsccSummary(AccManifestId fromAccManifestId, AsccpManifestId toAsccpManifestId);

    List<AsccSummaryRecord> getAsccSummaryList(Collection<ReleaseId> releaseIdList);

    List<AsccSummaryRecord> getAsccSummaryList(AccManifestId fromAccManifestId);

    List<AsccSummaryRecord> getAsccSummaryList(AsccpManifestId toAsccpManifestId);

    List<AsccSummaryRecord> getAsccSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    // BCC
    BccDetailsRecord getBccDetails(BccManifestId bccManifestId);

    List<BccDetailsRecord> getBccDetailsList();

    List<BccDetailsRecord> getBccDetailsList(ReleaseId releaseId);

    List<BccDetailsRecord> getBccDetailsList(AccManifestId accManifestId);

    BccSummaryRecord getBccSummary(BccManifestId bccManifestId);

    BccSummaryRecord getBccSummary(AccManifestId fromAccManifestId, BccpManifestId toBccpManifestId);

    List<BccSummaryRecord> getBccSummaryList(Collection<ReleaseId> releaseIdList);

    List<BccSummaryRecord> getBccSummaryList(AccManifestId fromAccManifestId);

    List<BccSummaryRecord> getBccSummaryList(BccpManifestId bccpManifestId);

    List<BccSummaryRecord> getBccSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    boolean hasSamePropertyTerm(AccManifestId accManifestId, String propertyTerm);

    AccSummaryRecord getAllExtensionAccManifest(ReleaseId releaseId);

    AccSummaryRecord getExistsUserExtension(AccManifestId roleOfAccManifestId);

    CcRefactorValidationResponse validateAsccRefactoring(AsccManifestId targetManifestId, AccManifestId destinationManifestId);

    CcRefactorValidationResponse validateBccRefactoring(BccManifestId targetManifestId, AccManifestId destinationManifestId);

    List<AsccSummaryRecord> getRefactorTargetAsccManifestList(AsccManifestId asccManifestId, AccManifestId accManifestId);

    List<BccSummaryRecord> getRefactorTargetBccManifestList(BccManifestId bccManifestId, AccManifestId accManifestId);

}
