package org.oagi.srt.provider;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Lazy
public class DefaultImportedDataProvider implements ImportedDataProvider, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        long s = System.currentTimeMillis();

        findAgencyIdListList = agencyIdListRepository.findAll(new Sort(Sort.Direction.ASC, "module"));
        findAgencyIdListMap = findAgencyIdListList.stream()
                .collect(Collectors.toMap(AgencyIdList::getAgencyIdListId, Function.identity()));

        findAgencyIdListValueByOwnerListIdMap = agencyIdListValueRepository.findAll().stream()
                .collect(Collectors.groupingBy(AgencyIdListValue::getOwnerListId));

        findCodeListList = codeListRepository.findAll();
        findCodeListMap = findCodeListList.stream()
                .collect(Collectors.toMap(CodeList::getCodeListId, Function.identity()));

        findCodeListValueByCodeListIdMap = codeListValueRepository.findAll().stream()
                .collect(Collectors.groupingBy(CodeListValue::getCodeListId));

        findDtList = dataTypeRepository.findAll(new Sort(Sort.Direction.ASC, "module"));
        findDtMap = findDtList.stream()
                .collect(Collectors.toMap(DataType::getDtId, Function.identity()));

        findDtScByOwnerDtIdMap = dtScRepository.findAll().stream()
                .collect(Collectors.groupingBy(DataTypeSupplementaryComponent::getOwnerDtId));

        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findAll();
        findBdtPriRestriListByDtIdMap = bdtPriRestriList.stream()
                .collect(Collectors.groupingBy(BusinessDataTypePrimitiveRestriction::getBdtId));

        cdtAwdPriXpsTypeMapMap = cdtAwdPriXpsTypeMapRepository.findAll().stream()
                .collect(Collectors.toMap(CoreDataTypeAllowedPrimitiveExpressionTypeMap::getCdtAwdPriXpsTypeMapId, Function.identity()));

        findBdtScPriRestriListByDtScIdMap = bdtScPriRestriRepository.findAll().stream()
                .collect(Collectors.groupingBy(BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId));

        findCdtScAwdPriXpsTypeMapMap = cdtScAwdPriXpsTypeMapRepository.findAll().stream()
                .collect(Collectors.toMap(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap::getCdtScAwdPriXpsTypeMapId, Function.identity()));

        findCdtScAwdPriMap = cdtScAwdPriRepository.findAll().stream()
                .collect(Collectors.toMap(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId, Function.identity()));

        List<CoreDataTypeAllowedPrimitive> cdtAwdPriList = cdtAwdPriRepository.findAll();
        findCdtAwdPriMap = cdtAwdPriList.stream()
                .collect(Collectors.toMap(CoreDataTypeAllowedPrimitive::getCdtAwdPriId, Function.identity()));

        List<CoreDataTypePrimitive> cdtPriList = cdtPriRepository.findAll();
        findCdtPriMap = cdtPriList.stream()
                .collect(Collectors.toMap(CoreDataTypePrimitive::getCdtPriId, Function.identity()));

        findXbtList = xbtRepository.findAll();
        findXbtMap = findXbtList.stream()
                .collect(Collectors.toMap(XSDBuiltInType::getXbtId, Function.identity()));

        findACCList = accRepository.findAllByRevisionNum(0);
        findAccMap = findACCList.stream()
                .collect(Collectors.toMap(AggregateCoreComponent::getAccId, Function.identity()));

        findASCCPList = asccpRepository.findAllByRevisionNum(0);
        findAsccpMap = findASCCPList.stream()
                .collect(Collectors.toMap(AssociationCoreComponentProperty::getAsccpId, Function.identity()));
        findAsccpByGuidMap = findASCCPList.stream()
                .collect(Collectors.toMap(AssociationCoreComponentProperty::getGuid, Function.identity()));

        findBCCPList = bccpRepository.findAllByRevisionNum(0);
        findBccpMap = findBCCPList.stream()
                .collect(Collectors.toMap(BasicCoreComponentProperty::getBccpId, Function.identity()));

        List<BasicCoreComponent> bccList = bccRepository.findAllWithRevisionNum(0);
        findBCCByToBccpIdMap = bccList.stream()
                .collect(Collectors.groupingBy(BasicCoreComponent::getToBccpId));
        findBccByFromAccIdMap = bccList.stream()
                .collect(Collectors.groupingBy(BasicCoreComponent::getFromAccId));

        findAsccByFromAccIdMap = asccRepository.findAllWithRevisionNum(0).stream()
                .collect(Collectors.groupingBy(AssociationCoreComponent::getFromAccId));

        logger.info("Ready for " + getClass().getSimpleName() + " in " + (System.currentTimeMillis() - s) / 1000d + " seconds");
    }

    private List<AgencyIdList> findAgencyIdListList;

    @Override
    public List<AgencyIdList> findAgencyIdList() {
        return Collections.unmodifiableList(findAgencyIdListList);
    }

    private Map<Long, AgencyIdList> findAgencyIdListMap;

    @Override
    public AgencyIdList findAgencyIdList(long agencyIdListId) {
        return findAgencyIdListMap.get(agencyIdListId);
    }

    private Map<Long, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;

    @Override
    public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(long ownerListId) {
        return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ? findAgencyIdListValueByOwnerListIdMap.get(ownerListId) : Collections.emptyList();
    }

    private List<CodeList> findCodeListList;

    @Override
    public List<CodeList> findCodeList() {
        return Collections.unmodifiableList(findCodeListList);
    }

    private Map<Long, CodeList> findCodeListMap;

    @Override
    public CodeList findCodeList(long codeListId) {
        return findCodeListMap.get(codeListId);
    }

    private Map<Long, List<CodeListValue>> findCodeListValueByCodeListIdMap;

    @Override
    public List<CodeListValue> findCodeListValueByCodeListId(long codeListId) {
        return (findCodeListValueByCodeListIdMap.containsKey(codeListId)) ? findCodeListValueByCodeListIdMap.get(codeListId) : Collections.emptyList();
    }

    private List<DataType> findDtList;

    @Override
    public List<DataType> findDT() {
        return Collections.unmodifiableList(findDtList);
    }

    private Map<Long, DataType> findDtMap;

    @Override
    public DataType findDT(long dtId) {
        return findDtMap.get(dtId);
    }

    private Map<Long, List<DataTypeSupplementaryComponent>> findDtScByOwnerDtIdMap;

    @Override
    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId) {
        return (findDtScByOwnerDtIdMap.containsKey(ownerDtId)) ? findDtScByOwnerDtIdMap.get(ownerDtId) : Collections.emptyList();
    }

    private Map<Long, List<BusinessDataTypePrimitiveRestriction>> findBdtPriRestriListByDtIdMap;

    @Override
    public List<BusinessDataTypePrimitiveRestriction> findBdtPriRestriListByDtId(long dtId) {
        return (findBdtPriRestriListByDtIdMap.containsKey(dtId)) ? findBdtPriRestriListByDtIdMap.get(dtId) : Collections.emptyList();
    }

    private Map<Long, CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapMap;

    @Override
    public CoreDataTypeAllowedPrimitiveExpressionTypeMap findCdtAwdPriXpsTypeMapById(long cdtAwdPriXpsTypeMapId) {
        return cdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId);
    }

    @Override
    public List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> findCdtAwdPriXpsTypeMapListByDtId(long dtId) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = findBdtPriRestriListByDtId(dtId);
        List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList = bdtPriRestriList.stream()
                .filter(e -> e.getCdtAwdPriXpsTypeMapId() > 0L)
                .map(e -> cdtAwdPriXpsTypeMapMap.get(e.getCdtAwdPriXpsTypeMapId()))
                .collect(Collectors.toList());
        return (cdtAwdPriXpsTypeMapList != null) ? cdtAwdPriXpsTypeMapList : Collections.emptyList();
    }

    private Map<Long, List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction>> findBdtScPriRestriListByDtScIdMap;

    @Override
    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriListByDtScId(long dtScId) {
        return (findBdtScPriRestriListByDtScIdMap.containsKey(dtScId)) ? findBdtScPriRestriListByDtScIdMap.get(dtScId) : Collections.emptyList();
    }

    private Map<Long, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findCdtScAwdPriXpsTypeMapMap;

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(long cdtScAwdPriXpsTypeMapId) {
        return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
    }

    private Map<Long, CoreDataTypeSupplementaryComponentAllowedPrimitive> findCdtScAwdPriMap;

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitive findCdtScAwdPri(long cdtScAwdPriId) {
        return findCdtScAwdPriMap.get(cdtScAwdPriId);
    }

    private List<XSDBuiltInType> findXbtList;

    @Override
    public List<XSDBuiltInType> findXbt() {
        return findXbtList;
    }

    private Map<Long, CoreDataTypeAllowedPrimitive> findCdtAwdPriMap;

    @Override
    public CoreDataTypeAllowedPrimitive findCdtAwdPri(long cdtAwdPriId) {
        return findCdtAwdPriMap.get(cdtAwdPriId);
    }

    private Map<Long, CoreDataTypePrimitive> findCdtPriMap;

    public CoreDataTypePrimitive findCdtPri(long cdtPriId) {
        return findCdtPriMap.get(cdtPriId);
    }

    private Map<Long, XSDBuiltInType> findXbtMap;

    @Override
    public XSDBuiltInType findXbt(long xbtId) {
        return findXbtMap.get(xbtId);
    }

    private List<AggregateCoreComponent> findACCList;

    @Override
    public List<AggregateCoreComponent> findACC() {
        return Collections.unmodifiableList(findACCList);
    }

    private Map<Long, AggregateCoreComponent> findAccMap;

    @Override
    public AggregateCoreComponent findACC(long accId) {
        return findAccMap.get(accId);
    }

    private List<AssociationCoreComponentProperty> findASCCPList;

    @Override
    public List<AssociationCoreComponentProperty> findASCCP() {
        return Collections.unmodifiableList(findASCCPList);
    }

    private Map<Long, AssociationCoreComponentProperty> findAsccpMap;

    @Override
    public AssociationCoreComponentProperty findASCCP(long asccpId) {
        return findAsccpMap.get(asccpId);
    }

    private Map<String, AssociationCoreComponentProperty> findAsccpByGuidMap;

    @Override
    public AssociationCoreComponentProperty findASCCPByGuid(String guid) {
        return findAsccpByGuidMap.get(guid);
    }

    private List<BasicCoreComponentProperty> findBCCPList;

    @Override
    public List<BasicCoreComponentProperty> findBCCP() {
        return Collections.unmodifiableList(findBCCPList);
    }

    private Map<Long, BasicCoreComponentProperty> findBccpMap;

    @Override
    public BasicCoreComponentProperty findBCCP(long bccpId) {
        return findBccpMap.get(bccpId);
    }

    private Map<Long, List<BasicCoreComponent>> findBCCByToBccpIdMap;

    @Override
    public List<BasicCoreComponent> findBCCByToBccpId(long toBccpId) {
        return (findBCCByToBccpIdMap.containsKey(toBccpId)) ? findBCCByToBccpIdMap.get(toBccpId) : Collections.emptyList();
    }

    private Map<Long, List<BasicCoreComponent>> findBccByFromAccIdMap;

    @Override
    public List<BasicCoreComponent> findBCCByFromAccId(long fromAccId) {
        return (findBccByFromAccIdMap.containsKey(fromAccId)) ? findBccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }

    private Map<Long, List<AssociationCoreComponent>> findAsccByFromAccIdMap;

    @Override
    public List<AssociationCoreComponent> findASCCByFromAccId(long fromAccId) {
        return (findAsccByFromAccIdMap.containsKey(fromAccId)) ? findAsccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }
}
