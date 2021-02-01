package org.oagi.score.provider;

import org.jooq.types.ULong;
import org.oagi.score.export.model.BlobContent;
import org.oagi.score.export.model.ModuleCCID;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;

import java.util.List;

public interface ImportedDataProvider {

    public List<AgencyIdListRecord> findAgencyIdList();

    public AgencyIdListRecord findAgencyIdList(ULong agencyIdListId);

    public List<AgencyIdListValueRecord> findAgencyIdListValueByOwnerListId(ULong ownerListId);

    public List<CodeListRecord> findCodeList();

    public CodeListRecord findCodeList(ULong codeListId);

    public List<CodeListValueRecord> findCodeListValueByCodeListId(ULong codeListId);

    public List<DtRecord> findDT();

    public DtRecord findDT(ULong dtId);

    public List<DtScRecord> findDtScByOwnerDtId(ULong ownerDtId);

    public List<BdtPriRestriRecord> findBdtPriRestriListByDtId(ULong dtId);

    public CdtAwdPriXpsTypeMapRecord findCdtAwdPriXpsTypeMapById(ULong cdtAwdPriXpsTypeMapId);

    public List<CdtAwdPriXpsTypeMapRecord> findCdtAwdPriXpsTypeMapListByDtId(ULong dtId);

    public List<BdtScPriRestriRecord> findBdtScPriRestriListByDtScId(ULong dtScId);

    public CdtScAwdPriXpsTypeMapRecord findCdtScAwdPriXpsTypeMap(ULong cdtScAwdPriXpsTypeMapId);

    public CdtScAwdPriRecord findCdtScAwdPri(ULong cdtScAwdPriId);

    public List<XbtRecord> findXbt();

    public CdtAwdPriRecord findCdtAwdPri(ULong cdtAwdPriId);

    public CdtPriRecord findCdtPri(ULong cdtPriId);

    public XbtRecord findXbt(ULong xbtId);

    public List<AccRecord> findACC();

    public AccRecord findACC(ULong accId);

    public List<AccManifestRecord> findACCManifest();

    public AccManifestRecord findACCManifest(ULong accManifestId);

    public List<AsccpRecord> findASCCP();

    public AsccpRecord findASCCP(ULong asccpId);

    public List<AsccpManifestRecord> findASCCPManifest();

    public AsccpManifestRecord findASCCPManifest(ULong asccpManifestId);

    public AsccpRecord findASCCPByGuid(String guid);

    public List<BccpRecord> findBCCP();

    public BccpRecord findBCCP(ULong bccpId);

    public List<BccpManifestRecord> findBCCPManifest();

    public BccpManifestRecord findBCCPManifest(ULong bccpManifestId);

    public List<BccRecord> findBCCByToBccpId(ULong toBccpId);

    public List<BccRecord> findBCCByFromAccId(ULong fromAccId);

    public List<AsccRecord> findASCCByFromAccId(ULong fromAccId);

    public BccRecord findBCC(ULong bccId);

    public BccManifestRecord findBCCManifest(ULong bccManifestId);

    public AsccRecord findASCC(ULong asccId);

    public AsccManifestRecord findASCCManifest(ULong asccManifestId);

    public ModuleCCID findModuleAgencyIdList(ULong agencyIdListId);

    public ModuleCCID findModuleCodeList(ULong codeListId);

    public ModuleCCID findModuleAcc(ULong accId);

    public ModuleCCID findModuleAsccp(ULong asccpId);

    public ModuleCCID findModuleBccp(ULong bccpId);

    public ModuleCCID findModuleDt(ULong dtId);

    public ModuleCCID findModuleXbt(ULong xbtId);

    public ModuleCCID findModuleBlobContent(ULong blobContentId);

    public List<BlobContentRecord> findBlobContent();
}
