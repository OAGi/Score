package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
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
import org.oagi.score.gateway.http.api.export.model.ModuleCCID;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleCcDocumentImpl implements ModuleCcDocument {

    private ScoreUser requester;

    private RepositoryFactory repositoryFactory;

    private ModuleSetReleaseId moduleSetReleaseId;

    private CcDocument delegate;

    private Map<AccManifestId, ModuleAccRecord> moduleAccMap;
    private Map<AsccpManifestId, ModuleAsccpRecord> moduleAsccpMap;
    private Map<BccpManifestId, ModuleBccpRecord> moduleBccpMap;
    private Map<DtManifestId, ModuleDtRecord> moduleDtMap;
    private Map<CodeListManifestId, ModuleCodeListRecord> moduleCodeListMap;
    private Map<AgencyIdListManifestId, ModuleAgencyIdListRecord> moduleAgencyIdListMap;
    private Map<XbtManifestId, ModuleXbtRecord> moduleXbtMap;
    private Map<BlobContentManifestId, ModuleBlobContentRecord> moduleBlobContentMap;

    public ModuleCcDocumentImpl(ScoreUser requester, RepositoryFactory repositoryFactory, ModuleSetReleaseId moduleSetReleaseId) {
        ModuleSetReleaseQueryRepository query = repositoryFactory.moduleSetReleaseQueryRepository(requester);
        ModuleSetReleaseDetailsRecord moduleSetReleaseDetails = query.getModuleSetReleaseDetails(moduleSetReleaseId);
        delegate = new CcDocumentImpl(requester, repositoryFactory, moduleSetReleaseDetails.release().releaseId());

        this.requester = requester;
        this.repositoryFactory = repositoryFactory;
        this.moduleSetReleaseId = moduleSetReleaseId;

        List<ModuleAccRecord> moduleAccRecordList = query.getModuleAccList(moduleSetReleaseId);
        moduleAccMap = moduleAccRecordList.stream()
                .collect(Collectors.toMap(ModuleAccRecord::accManifestId, Function.identity()));

        List<ModuleAsccpRecord> moduleAsccpRecordList = query.getModuleAsccpList(moduleSetReleaseId);
        moduleAsccpMap = moduleAsccpRecordList.stream()
                .collect(Collectors.toMap(ModuleAsccpRecord::asccpManifestId, Function.identity()));

        List<ModuleBccpRecord> moduleBccpRecordList = query.getModuleBccpList(moduleSetReleaseId);
        moduleBccpMap = moduleBccpRecordList.stream()
                .collect(Collectors.toMap(ModuleBccpRecord::bccpManifestId, Function.identity()));

        List<ModuleDtRecord> moduleDtRecordList = query.getModuleDtList(moduleSetReleaseId);
        moduleDtMap = moduleDtRecordList.stream()
                .collect(Collectors.toMap(ModuleDtRecord::dtManifestId, Function.identity()));

        List<ModuleCodeListRecord> moduleCodeListRecordList = query.getModuleCodeListList(moduleSetReleaseId);
        moduleCodeListMap = moduleCodeListRecordList.stream()
                .collect(Collectors.toMap(ModuleCodeListRecord::codeListManifestId, Function.identity()));

        List<ModuleAgencyIdListRecord> moduleAgencyIdListRecordList = query.getModuleAgencyIdListList(moduleSetReleaseId);
        moduleAgencyIdListMap = moduleAgencyIdListRecordList.stream()
                .collect(Collectors.toMap(ModuleAgencyIdListRecord::agencyIdListManifestId, Function.identity()));

        List<ModuleXbtRecord> moduleXbtRecordList = query.getModuleXbtList(moduleSetReleaseId);
        moduleXbtMap = moduleXbtRecordList.stream()
                .collect(Collectors.toMap(ModuleXbtRecord::xbtManifestId, Function.identity()));

        List<ModuleBlobContentRecord> moduleBlobContentRecordList = query.getModuleBlobContentList(moduleSetReleaseId);
        moduleBlobContentMap = moduleBlobContentRecordList.stream()
                .collect(Collectors.toMap(ModuleBlobContentRecord::blobContentManifestId, Function.identity()));
    }

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        return delegate.getAcc(accManifestId);
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord accManifest) {
        return delegate.getAssociations(accManifest);
    }

    @Override
    public AsccSummaryRecord getAscc(AsccManifestId asccManifestId) {
        return delegate.getAscc(asccManifestId);
    }

    @Override
    public BccSummaryRecord getBcc(BccManifestId bccManifestId) {
        return delegate.getBcc(bccManifestId);
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        return delegate.getAsccp(asccpManifestId);
    }

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        return delegate.getAsccpList();
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        return delegate.getBccp(bccpManifestId);
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        return delegate.getDt(dtManifestId);
    }

    @Override
    public List<DtSummaryRecord> getDtList() {
        return delegate.getDtList();
    }

    @Override
    public DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId) {
        return delegate.getDtSc(dtScManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return delegate.getDtScList(dtManifestId);
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        return delegate.getDtAwdPriList(dtManifestId);
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        return delegate.getDtScAwdPriList(dtScManifestId);
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return delegate.getXbt(xbtManifestId);
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return delegate.getXbtListByName(name);
    }

    @Override
    public List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return delegate.getAccListByBasedAccManifestId(basedAccManifestId);
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId) {
        return delegate.getAsccpListByRoleOfAccManifestId(roleOfAccManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId) {
        return delegate.getAccListByToAsccpManifestId(toAsccpManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return delegate.getAccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId) {
        return delegate.getBccpListByDtManifestId(dtManifestId);
    }

    @Override
    public List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return delegate.getAsccListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return delegate.getBccListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return delegate.getBccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return delegate.getAssociationListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId) {
        return delegate.getDtScListByDtManifestId(dtManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId) {
        return delegate.getTagListByAccManifestId(accManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId) {
        return delegate.getTagListByAsccpManifestId(asccpManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId) {
        return delegate.getTagListByBccpManifestId(bccpManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId) {
        return delegate.getTagListByDtManifestId(dtManifestId);
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        return delegate.getNamespace(namespaceId);
    }

    @Override
    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return delegate.getCodeList(codeListManifestId);
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return delegate.getAgencyIdList(agencyIdListManifestId);
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        return delegate.getBlobContent(blobContentManifestId);
    }

    @Override
    public List<ModuleCCID<AgencyIdListManifestId>> getModuleAgencyIdList() {
        return moduleAgencyIdListMap.values().stream()
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<AgencyIdListManifestId> getModuleAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return toModuleCCID(moduleAgencyIdListMap.get(agencyIdListManifestId));
    }

    private ModuleCCID<AgencyIdListManifestId> toModuleCCID(ModuleAgencyIdListRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.agencyIdListManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<CodeListManifestId>> getModuleCodeList() {
        return moduleCodeListMap.values().stream()
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<CodeListManifestId> getModuleCodeList(CodeListManifestId codeListManifestId) {
        return toModuleCCID(moduleCodeListMap.get(codeListManifestId));
    }

    private ModuleCCID<CodeListManifestId> toModuleCCID(ModuleCodeListRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.codeListManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<AccManifestId>> getModuleAcc() {
        return moduleAccMap.values().stream()
                .sorted(Comparator.comparing(ModuleAccRecord::accManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<AccManifestId> getModuleAcc(AccManifestId accManifestId) {
        return toModuleCCID(moduleAccMap.get(accManifestId));
    }

    private ModuleCCID<AccManifestId> toModuleCCID(ModuleAccRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.accManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<AsccpManifestId>> getModuleAsccp() {
        return moduleAsccpMap.values().stream()
                .sorted(Comparator.comparing(ModuleAsccpRecord::asccpManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<AsccpManifestId> getModuleAsccp(AsccpManifestId asccpManifestId) {
        return toModuleCCID(moduleAsccpMap.get(asccpManifestId));
    }

    private ModuleCCID<AsccpManifestId> toModuleCCID(ModuleAsccpRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.asccpManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<BccpManifestId>> getModuleBccp() {
        return moduleBccpMap.values().stream()
                .sorted(Comparator.comparing(ModuleBccpRecord::bccpManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<BccpManifestId> getModuleBccp(BccpManifestId bccpManifestId) {
        return toModuleCCID(moduleBccpMap.get(bccpManifestId));
    }

    private ModuleCCID<BccpManifestId> toModuleCCID(ModuleBccpRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.bccpManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<DtManifestId>> getModuleDt() {
        return moduleDtMap.values().stream()
                .sorted(Comparator.comparing(ModuleDtRecord::dtManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<DtManifestId> getModuleDt(DtManifestId dtManifestId) {
        return toModuleCCID(moduleDtMap.get(dtManifestId));
    }

    private ModuleCCID<DtManifestId> toModuleCCID(ModuleDtRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.dtManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<XbtManifestId>> getModuleXbt() {
        return moduleXbtMap.values().stream()
                .sorted(Comparator.comparing(ModuleXbtRecord::xbtManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleCCID<XbtManifestId> getModuleXbt(XbtManifestId xbtManifestId) {
        return toModuleCCID(moduleXbtMap.get(xbtManifestId));
    }

    private ModuleCCID<XbtManifestId> toModuleCCID(ModuleXbtRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.xbtManifestId(),
                record.modulePath()) : null;
    }

    @Override
    public List<ModuleCCID<BlobContentManifestId>> getModuleBlobContent() {
        return moduleBlobContentMap.values().stream()
                .sorted(Comparator.comparing(ModuleBlobContentRecord::blobContentManifestId))
                .map(record -> toModuleCCID(record))
                .collect(Collectors.toList());
    }

    private ModuleCCID<BlobContentManifestId> toModuleCCID(ModuleBlobContentRecord record) {
        return (record != null) ? new ModuleCCID<>(
                record.moduleId(),
                record.blobContentManifestId(),
                record.modulePath()) : null;
    }
}
