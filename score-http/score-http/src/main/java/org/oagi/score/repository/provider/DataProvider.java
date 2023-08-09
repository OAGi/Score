package org.oagi.score.repository.provider;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;

import java.util.List;

public interface DataProvider {

    List<SeqKeyRecord> getSeqKeys(ULong accManifestId);

    AgencyIdListManifestRecord findAgencyIdListManifest(ULong agencyIdListManifestId);

    List<AgencyIdListRecord> findAgencyIdList();

    AgencyIdListRecord findAgencyIdList(ULong agencyIdListId);

    List<AgencyIdListValueManifestRecord> findAgencyIdListValueManifestByAgencyIdListManifestId(ULong agencyIdListManifestId);

    AgencyIdListValueRecord findAgencyIdListValue(ULong agencyIdListValueId);

    CodeListManifestRecord findCodeListManifest(ULong codeListManifestId);

    List<CodeListRecord> findCodeList();

    CodeListRecord findCodeList(ULong codeListId);

    CodeListValueRecord findCodeListValue(ULong codeListValueId);

    List<CodeListValueManifestRecord> findCodeListValueManifestByCodeListManifestId(ULong codeListManifestId);

    List<DtManifestRecord> findDtManifest();

    DtManifestRecord findDtManifestByDtManifestId(ULong dtManifestId);

    List<DtRecord> findDT();

    DtRecord findDT(ULong dtId);

    List<DtScManifestRecord> findDtScManifestByOwnerDtManifestId(ULong ownerDtManifestId);

    DtScRecord findDtSc(ULong dtScId);

    List<BdtPriRestriRecord> findBdtPriRestriListByDtManifestId(ULong dtManifestId);

    CdtAwdPriXpsTypeMapRecord findCdtAwdPriXpsTypeMapById(ULong cdtAwdPriXpsTypeMapId);

    List<CdtAwdPriXpsTypeMapRecord> findCdtAwdPriXpsTypeMapListByDtManifestId(ULong dtManifestId);

    List<BdtScPriRestriRecord> findBdtScPriRestriListByDtScManifestId(ULong dtScManifestId);

    CdtScAwdPriXpsTypeMapRecord findCdtScAwdPriXpsTypeMap(ULong cdtScAwdPriXpsTypeMapId);

    CdtScAwdPriRecord findCdtScAwdPri(ULong cdtScAwdPriId);

    List<XbtRecord> findXbt();

    CdtAwdPriRecord findCdtAwdPri(ULong cdtAwdPriId);

    CdtPriRecord findCdtPri(ULong cdtPriId);

    XbtRecord findXbt(ULong xbtId);

    List<AccRecord> findACC();

    List<AccManifestRecord> findACCManifest();

    AccRecord findACC(ULong accId);

    AccManifestRecord findACCManifest(ULong accManifestId);

    List<AsccpManifestRecord> findASCCPManifest();

    AsccpManifestRecord findASCCPManifest(ULong asccpManifestId);

    List<BccpManifestRecord> findBCCPManifest();

    BccpManifestRecord findBCCPManifest(ULong bccpManifestId);

    List<AsccpRecord> findASCCP();

    AsccpRecord findASCCP(ULong asccpId);

    AsccpRecord findASCCPByGuid(String guid);

    List<BccpRecord> findBCCP();

    BccpRecord findBCCP(ULong bccpId);

    List<BccRecord> findBCCByToBccpId(ULong toBccpId);

    List<BccRecord> findBCCByFromAccId(ULong fromAccId);

    AsccRecord findASCC(ULong asccId);

    BccRecord findBCC(ULong bccId);

    AsccManifestRecord findASCCManifest(ULong asccManifestId);

    List<AsccManifestRecord> findASCCManifestByFromAccManifestId(ULong fromAccManifestId);

    BccManifestRecord findBCCManifest(ULong bccManifestId);

    List<BccManifestRecord> findBCCManifestByFromAccManifestId(ULong fromAccManifestId);

    List<AsccRecord> findASCCByFromAccId(ULong fromAccId);

    List<BlobContentRecord> findBlobContent();

    List<ReleaseRecord> findRelease();

    ReleaseRecord findRelease(ULong releaseId);

    List<NamespaceRecord> findNamespace();

    NamespaceRecord findNamespace(ULong namespaceId);

}
