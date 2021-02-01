package org.oagi.score.provider;

import org.jooq.types.ULong;
import org.oagi.score.export.model.BlobContent;
import org.oagi.score.export.model.ModuleCCID;
import org.oagi.score.export.repository.CcRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.BccManifest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.MODULE_SET_RELEASE_ID;

@Component
@Lazy
public class DefaultImportedDataProvider implements ImportedDataProvider, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CcRepository ccRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        long s = System.currentTimeMillis();

        findAgencyIdListList = ccRepository.findAllAgencyIdList(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findModuleAgencyIdListManifestMap = ccRepository.findAllModuleAgencyIdListManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));
        findAgencyIdListMap = findAgencyIdListList.stream()
                .collect(Collectors.toMap(AgencyIdListRecord::getAgencyIdListId, Function.identity()));

        findAgencyIdListValueByOwnerListIdMap = ccRepository.findAllAgencyIdListValue(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.groupingBy(AgencyIdListValueRecord::getOwnerListId));

        findCodeListList = ccRepository.findAllCodeList(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findCodeListMap = findCodeListList.stream()
                .collect(Collectors.toMap(CodeListRecord::getCodeListId, Function.identity()));

        findCodeListValueByCodeListIdMap = ccRepository.findAllCodeListValue(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.groupingBy(CodeListValueRecord::getCodeListId));

        findDtList = ccRepository.findAllDt(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findDtMap = findDtList.stream()
                .collect(Collectors.toMap(DtRecord::getDtId, Function.identity()));

        findDtScByOwnerDtIdMap = ccRepository.findAllDtSc(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.groupingBy(DtScRecord::getOwnerDtId));

        List<BdtPriRestriRecord> bdtPriRestriList = ccRepository.findAllBdtPriRestri(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findBdtPriRestriListByDtIdMap = bdtPriRestriList.stream()
                .collect(Collectors.groupingBy(BdtPriRestriRecord::getBdtId));

        cdtAwdPriXpsTypeMapMap = ccRepository.findAllCdtAwdPriXpsTypeMap(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.toMap(CdtAwdPriXpsTypeMapRecord::getCdtAwdPriXpsTypeMapId, Function.identity()));

        findBdtScPriRestriListByDtScIdMap = ccRepository.findAllBdtScPriRestri(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.groupingBy(BdtScPriRestriRecord::getBdtScId));

        findCdtScAwdPriXpsTypeMapMap = ccRepository.findAllCdtScAwdPriXpsTypeMap(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.toMap(CdtScAwdPriXpsTypeMapRecord::getCdtScAwdPriXpsTypeMapId, Function.identity()));

        findCdtScAwdPriMap = ccRepository.findAllCdtScAwdPri(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.toMap(CdtScAwdPriRecord::getCdtScAwdPriId, Function.identity()));

        List<CdtAwdPriRecord> cdtAwdPriList = ccRepository.findAllCdtAwdPri(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findCdtAwdPriMap = cdtAwdPriList.stream()
                .collect(Collectors.toMap(CdtAwdPriRecord::getCdtAwdPriId, Function.identity()));

        List<CdtPriRecord> cdtPriList = ccRepository.findAllCdtPri();
        findCdtPriMap = cdtPriList.stream()
                .collect(Collectors.toMap(CdtPriRecord::getCdtPriId, Function.identity()));

        findXbtList = ccRepository.findAllXbt(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findXbtMap = findXbtList.stream()
                .collect(Collectors.toMap(XbtRecord::getXbtId, Function.identity()));

        findACCList = ccRepository.findAllAcc(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAccMap = findACCList.stream()
                .collect(Collectors.toMap(AccRecord::getAccId, Function.identity()));

        findACCManifestList = ccRepository.findAllAccManifest(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAccManifestMap = findACCManifestList.stream()
                .collect(Collectors.toMap(AccManifestRecord::getAccManifestId, Function.identity()));

        findASCCPList = ccRepository.findAllAsccp(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAsccpMap = findASCCPList.stream()
                .collect(Collectors.toMap(AsccpRecord::getAsccpId, Function.identity()));
        findAsccpByGuidMap = findASCCPList.stream()
                .collect(Collectors.toMap(AsccpRecord::getGuid, Function.identity()));

        findASCCPManifestList = ccRepository.findAllAsccpManifest(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAsccpManifestMap = findASCCPManifestList.stream()
                .collect(Collectors.toMap(AsccpManifestRecord::getAsccpManifestId, Function.identity()));

        findBCCPManifestList = ccRepository.findAllBccpManifest(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findBccpManifestMap = findBCCPManifestList.stream()
                .collect(Collectors.toMap(BccpManifestRecord::getBccpManifestId, Function.identity()));
        
        findBCCPList = ccRepository.findAllBccp(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findBccpMap = findBCCPList.stream()
                .collect(Collectors.toMap(BccpRecord::getBccpId, Function.identity()));

        findACCManifestList = ccRepository.findAllAccManifest(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAccManifestMap = findACCManifestList.stream()
                .collect(Collectors.toMap(AccManifestRecord::getAccManifestId, Function.identity()));

        List<BccRecord> bccList = ccRepository.findAllBcc(ULong.valueOf(MODULE_SET_RELEASE_ID));

        findBCCByToBccpIdMap = bccList.stream()
                .collect(Collectors.groupingBy(BccRecord::getToBccpId));
        findBccByFromAccIdMap = bccList.stream()
                .collect(Collectors.groupingBy(BccRecord::getFromAccId));
        findBccMap = bccList.stream()
                .collect(Collectors.toMap(BccRecord::getBccId, Function.identity()));

        findBccManifestMap = ccRepository.findAllBccManifest(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.toMap(BccManifestRecord::getBccId, Function.identity()));

        findAsccManifestMap = ccRepository.findAllAsccManifest(ULong.valueOf(MODULE_SET_RELEASE_ID)).stream()
                .collect(Collectors.toMap(AsccManifestRecord::getAsccId, Function.identity()));

        List<AsccRecord> asccList = ccRepository.findAllAscc(ULong.valueOf(MODULE_SET_RELEASE_ID));
        findAsccByFromAccIdMap = asccList.stream()
                .collect(Collectors.groupingBy(AsccRecord::getFromAccId));

        findAsccMap = asccList.stream()
                .collect(Collectors.toMap(AsccRecord::getAsccId, Function.identity()));

        findModuleCodeListManifestMap = ccRepository.findAllModuleCodeListManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findModuleAccManifestMap = ccRepository.findAllModuleAccManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findModuleAsccpManifestMap = ccRepository.findAllModuleAsccpManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findModuleBccpManifestMap = ccRepository.findAllModuleBccpManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findModuleDtManifestMap = ccRepository.findAllModuleDtManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findModuleXbtManifestMap = ccRepository.findAllModuleXbtManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        findBlobContentList = ccRepository.findAllBlobContent(ULong.valueOf(MODULE_SET_RELEASE_ID));

        findModuleBlobContentManifestMap = ccRepository.findAllModuleBlobContentManifest(ULong.valueOf(MODULE_SET_RELEASE_ID))
                .stream().collect(Collectors.toMap(ModuleCCID::getCcId, Function.identity()));

        logger.info("Ready for " + getClass().getSimpleName() + " in " + (System.currentTimeMillis() - s) / 1000d + " seconds");
    }

    private List<AgencyIdListRecord> findAgencyIdListList;
    private Map<ULong, ModuleCCID> findModuleAgencyIdListManifestMap;
    private Map<ULong, ModuleCCID> findModuleCodeListManifestMap;
    private Map<ULong, ModuleCCID> findModuleAccManifestMap;
    private Map<ULong, ModuleCCID> findModuleDtManifestMap;
    private Map<ULong, ModuleCCID> findModuleAsccpManifestMap;
    private Map<ULong, ModuleCCID> findModuleBccpManifestMap;
    private Map<ULong, ModuleCCID> findModuleXbtManifestMap;
    private Map<ULong, ModuleCCID> findModuleBlobContentManifestMap;

    @Override
    public List<AgencyIdListRecord> findAgencyIdList() {
        return Collections.unmodifiableList(findAgencyIdListList);
    }

    private Map<ULong, AgencyIdListRecord> findAgencyIdListMap;

    @Override
    public AgencyIdListRecord findAgencyIdList(ULong agencyIdListId) {
        AgencyIdListRecord a = findAgencyIdListMap.get(agencyIdListId);
        if (a == null) {
            throw new IllegalStateException();
        }
        return findAgencyIdListMap.get(agencyIdListId);
    }

    private Map<ULong, List<AgencyIdListValueRecord>> findAgencyIdListValueByOwnerListIdMap;

    @Override
    public List<AgencyIdListValueRecord> findAgencyIdListValueByOwnerListId(ULong ownerListId) {
        return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ? findAgencyIdListValueByOwnerListIdMap.get(ownerListId) : Collections.emptyList();
    }

    private List<CodeListRecord> findCodeListList;

    @Override
    public List<CodeListRecord> findCodeList() {
        return Collections.unmodifiableList(findCodeListList);
    }

    private Map<ULong, CodeListRecord> findCodeListMap;

    @Override
    public CodeListRecord findCodeList(ULong codeListId) {
        return findCodeListMap.get(codeListId);
    }

    private Map<ULong, List<CodeListValueRecord>> findCodeListValueByCodeListIdMap;

    @Override
    public List<CodeListValueRecord> findCodeListValueByCodeListId(ULong codeListId) {
        return (findCodeListValueByCodeListIdMap.containsKey(codeListId)) ? findCodeListValueByCodeListIdMap.get(codeListId) : Collections.emptyList();
    }

    private List<DtRecord> findDtList;

    @Override
    public List<DtRecord> findDT() {
        return Collections.unmodifiableList(findDtList);
    }

    private Map<ULong, DtRecord> findDtMap;

    @Override
    public DtRecord findDT(ULong dtId) {
        return findDtMap.get(dtId);
    }

    private Map<ULong, List<DtScRecord>> findDtScByOwnerDtIdMap;

    @Override
    public List<DtScRecord> findDtScByOwnerDtId(ULong ownerDtId) {
        return (findDtScByOwnerDtIdMap.containsKey(ownerDtId)) ? findDtScByOwnerDtIdMap.get(ownerDtId) : Collections.emptyList();
    }

    private Map<ULong, List<BdtPriRestriRecord>> findBdtPriRestriListByDtIdMap;

    @Override
    public List<BdtPriRestriRecord> findBdtPriRestriListByDtId(ULong dtId) {
        return (findBdtPriRestriListByDtIdMap.containsKey(dtId)) ? findBdtPriRestriListByDtIdMap.get(dtId) : Collections.emptyList();
    }

    private Map<ULong, CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapMap;

    @Override
    public CdtAwdPriXpsTypeMapRecord findCdtAwdPriXpsTypeMapById(ULong cdtAwdPriXpsTypeMapId) {
        return cdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId);
    }

    @Override
    public List<CdtAwdPriXpsTypeMapRecord> findCdtAwdPriXpsTypeMapListByDtId(ULong dtId) {
        List<BdtPriRestriRecord> bdtPriRestriList = findBdtPriRestriListByDtId(dtId);
        List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapList = bdtPriRestriList.stream()
                .filter(e -> e.getCdtAwdPriXpsTypeMapId() != null)
                .map(e -> cdtAwdPriXpsTypeMapMap.get(e.getCdtAwdPriXpsTypeMapId()))
                .collect(Collectors.toList());
        return (cdtAwdPriXpsTypeMapList != null) ? cdtAwdPriXpsTypeMapList : Collections.emptyList();
    }

    private Map<ULong, List<BdtScPriRestriRecord>> findBdtScPriRestriListByDtScIdMap;

    @Override
    public List<BdtScPriRestriRecord> findBdtScPriRestriListByDtScId(ULong dtScId) {
        return (findBdtScPriRestriListByDtScIdMap.containsKey(dtScId)) ? findBdtScPriRestriListByDtScIdMap.get(dtScId) : Collections.emptyList();
    }

    private Map<ULong, CdtScAwdPriXpsTypeMapRecord> findCdtScAwdPriXpsTypeMapMap;

    @Override
    public CdtScAwdPriXpsTypeMapRecord findCdtScAwdPriXpsTypeMap(ULong cdtScAwdPriXpsTypeMapId) {
        return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
    }

    private Map<ULong, CdtScAwdPriRecord> findCdtScAwdPriMap;

    @Override
    public CdtScAwdPriRecord findCdtScAwdPri(ULong cdtScAwdPriId) {
        return findCdtScAwdPriMap.get(cdtScAwdPriId);
    }

    private List<XbtRecord> findXbtList;

    @Override
    public List<XbtRecord> findXbt() {
        return findXbtList;
    }

    private Map<ULong, CdtAwdPriRecord> findCdtAwdPriMap;

    @Override
    public CdtAwdPriRecord findCdtAwdPri(ULong cdtAwdPriId) {
        return findCdtAwdPriMap.get(cdtAwdPriId);
    }

    private Map<ULong, CdtPriRecord> findCdtPriMap;

    public CdtPriRecord findCdtPri(ULong cdtPriId) {
        return findCdtPriMap.get(cdtPriId);
    }

    private Map<ULong, XbtRecord> findXbtMap;

    @Override
    public XbtRecord findXbt(ULong xbtId) {
        XbtRecord xbt = findXbtMap.get(xbtId);
        return xbt;
    }

    private List<AccRecord> findACCList;

    @Override
    public List<AccRecord> findACC() {
        return Collections.unmodifiableList(findACCList);
    }

    @Override
    public List<AccManifestRecord> findACCManifest() {
        return Collections.unmodifiableList(findACCManifestList);
    }

    private Map<ULong, AccRecord> findAccMap;

    @Override
    public AccRecord findACC(ULong accId) {
        return findAccMap.get(accId);
    }

    private List<AccManifestRecord> findACCManifestList;

    private Map<ULong, AccManifestRecord> findAccManifestMap;

    @Override
    public AccManifestRecord findACCManifest(ULong accManifestId) {
        return findAccManifestMap.get(accManifestId);
    }

    @Override
    public List<AsccpManifestRecord> findASCCPManifest() {
        return Collections.unmodifiableList(findASCCPManifestList);
    }

    private List<AsccpManifestRecord> findASCCPManifestList;

    private Map<ULong, AsccpManifestRecord> findAsccpManifestMap;

    @Override
    public AsccpManifestRecord findASCCPManifest(ULong asccpManifestId) {
        return findAsccpManifestMap.get(asccpManifestId);
    }

    @Override
    public List<BccpManifestRecord> findBCCPManifest() {
        return Collections.unmodifiableList(findBCCPManifestList);
    }

    private List<BccpManifestRecord> findBCCPManifestList;

    private Map<ULong, BccpManifestRecord> findBccpManifestMap;

    @Override
    public BccpManifestRecord findBCCPManifest(ULong bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        return findBccpManifestMap.get(bccpManifestId);
    }

    private List<AsccpRecord> findASCCPList;

    @Override
    public List<AsccpRecord> findASCCP() {
        return Collections.unmodifiableList(findASCCPList);
    }

    private Map<ULong, AsccpRecord> findAsccpMap;

    @Override
    public AsccpRecord findASCCP(ULong asccpId) {
        return findAsccpMap.get(asccpId);
    }

    private Map<String, AsccpRecord> findAsccpByGuidMap;

    @Override
    public AsccpRecord findASCCPByGuid(String guid) {
        return findAsccpByGuidMap.get(guid);
    }

    private List<BccpRecord> findBCCPList;

    @Override
    public List<BccpRecord> findBCCP() {
        return Collections.unmodifiableList(findBCCPList);
    }

    private Map<ULong, BccpRecord> findBccpMap;

    @Override
    public BccpRecord findBCCP(ULong bccpId) {
        return findBccpMap.get(bccpId);
    }

    private Map<ULong, List<BccRecord>> findBCCByToBccpIdMap;

    @Override
    public List<BccRecord> findBCCByToBccpId(ULong toBccpId) {
        return (findBCCByToBccpIdMap.containsKey(toBccpId)) ? findBCCByToBccpIdMap.get(toBccpId) : Collections.emptyList();
    }

    private Map<ULong, List<BccRecord>> findBccByFromAccIdMap;

    @Override
    public List<BccRecord> findBCCByFromAccId(ULong fromAccId) {
        return (findBccByFromAccIdMap.containsKey(fromAccId)) ? findBccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }

    private Map<ULong, AsccRecord> findAsccMap;

    @Override
    public AsccRecord findASCC(ULong asccId) {
        return findAsccMap.get(asccId);
    }

    private Map<ULong, BccRecord> findBccMap;

    @Override
    public BccRecord findBCC(ULong bccId) {
        return findBccMap.get(bccId);
    }

    private Map<ULong, AsccManifestRecord> findAsccManifestMap;

    @Override
    public AsccManifestRecord findASCCManifest(ULong asccId) {
        return findAsccManifestMap.get(asccId);
    }

    private Map<ULong, BccManifestRecord> findBccManifestMap;

    @Override
    public BccManifestRecord findBCCManifest(ULong bccId) {
        return findBccManifestMap.get(bccId);
    }

    private Map<ULong, List<AsccRecord>> findAsccByFromAccIdMap;

    @Override
    public List<AsccRecord> findASCCByFromAccId(ULong fromAccId) {
        return (findAsccByFromAccIdMap.containsKey(fromAccId)) ? findAsccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }

    @Override
    public ModuleCCID findModuleAgencyIdList(ULong agencyIdListId) {
        return findModuleAgencyIdListManifestMap.get(agencyIdListId);
    }

    @Override
    public ModuleCCID findModuleCodeList(ULong codeListId) {
        return findModuleCodeListManifestMap.get(codeListId);
    }

    @Override
    public ModuleCCID findModuleAcc(ULong accId) {
        return findModuleAccManifestMap.get(accId);
    }

    @Override
    public ModuleCCID findModuleAsccp(ULong asccpId) {
        return findModuleAsccpManifestMap.get(asccpId);
    }

    @Override
    public ModuleCCID findModuleBccp(ULong bccpId) {
        return findModuleBccpManifestMap.get(bccpId);
    }

    @Override
    public ModuleCCID findModuleDt(ULong dtId) {
        return findModuleDtManifestMap.get(dtId);
    }

    @Override
    public ModuleCCID findModuleXbt(ULong xbtId) {
        return findModuleXbtManifestMap.get(xbtId);
    }

    private List<BlobContentRecord> findBlobContentList;

    @Override
    public List<BlobContentRecord> findBlobContent() {
        return findBlobContentList;
    }

    @Override
    public ModuleCCID findModuleBlobContent(ULong blobContentId) {
        return findModuleBlobContentManifestMap.get(blobContentId);
    }
}
