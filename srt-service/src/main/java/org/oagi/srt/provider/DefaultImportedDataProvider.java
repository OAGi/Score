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
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

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
        Sort moduleSort = new Sort(Sort.Direction.ASC, "module");

        findAgencyIdListMap = agencyIdListRepository.findAll().stream()
                .collect(Collectors.toMap(AgencyIdList::getAgencyIdListId, Function.identity()));

        findCodeListList = codeListRepository.findAll();
        findCodeListMap = findCodeListList.stream()
                .collect(Collectors.toMap(CodeList::getCodeListId, Function.identity()));

        findCodeListValueByCodeListIdMap = codeListValueRepository.findAll().stream()
                .collect(Collectors.groupingBy(CodeListValue::getCodeListId));

        findDtList = dataTypeRepository.findAll(moduleSort);
        findDtMap = findDtList.stream()
                .collect(Collectors.toMap(DataType::getDtId, Function.identity()));

        findDtScByOwnerDtIdMap = dtScRepository.findAll().stream()
                .collect(Collectors.groupingBy(DataTypeSupplementaryComponent::getOwnerDtId));

        findBdtPriRestriListByDtIdMap = bdtPriRestriRepository.findAll().stream()
                .collect(Collectors.groupingBy(BusinessDataTypePrimitiveRestriction::getBdtId));

        findBdtScPriRestriListByDtScIdMap = bdtScPriRestriRepository.findAll().stream()
                .collect(Collectors.groupingBy(BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId));

        findCdtScAwdPriXpsTypeMapMap = cdtScAwdPriXpsTypeMapRepository.findAll().stream()
                .collect(Collectors.toMap(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap::getCdtScAwdPriXpsTypeMapId, Function.identity()));

        findXbtMap = xbtRepository.findAll().stream()
                .collect(Collectors.toMap(XSDBuiltInType::getXbtId, Function.identity()));

        findACCList = accRepository.findAll(moduleSort);
        findAccMap = findACCList.stream()
                .collect(Collectors.toMap(AggregateCoreComponent::getAccId, Function.identity()));

        findASCCPList = asccpRepository.findAll(moduleSort);
        findAsccpMap = findASCCPList.stream()
                .collect(Collectors.toMap(AssociationCoreComponentProperty::getAsccpId, Function.identity()));
        findAsccpByGuidMap = findASCCPList.stream()
                .collect(Collectors.toMap(AssociationCoreComponentProperty::getGuid, Function.identity()));

        findBCCPList = bccpRepository.findAll(moduleSort);
        findBccpMap = findBCCPList.stream()
                .collect(Collectors.toMap(BasicCoreComponentProperty::getBccpId, Function.identity()));

        List<BasicCoreComponent> bccList = bccRepository.findAll();
        findBCCByToBccpIdAndEntityTypeIs1Map = bccList.stream()
                .filter(e -> e.getEntityType() == 1)
                .collect(Collectors.groupingBy(BasicCoreComponent::getToBccpId));
        findBccByFromAccIdMap = bccList.stream()
                .collect(Collectors.groupingBy(BasicCoreComponent::getFromAccId));

        findAsccByFromAccIdMap = asccRepository.findAll().stream()
                .collect(Collectors.groupingBy(AssociationCoreComponent::getFromAccId));

        logger.info("Ready for " + getClass().getSimpleName() + " in " + (System.currentTimeMillis() - s) / 1000d + " seconds");
    }

    private Map<Integer, AgencyIdList> findAgencyIdListMap;

    @Override
    public AgencyIdList findAgencyIdList(int agencyIdListId) {
        return findAgencyIdListMap.get(agencyIdListId);
    }

    private List<CodeList> findCodeListList;

    @Override
    public List<CodeList> findCodeList() {
        return Collections.unmodifiableList(findCodeListList);
    }

    private Map<Integer, CodeList> findCodeListMap;

    @Override
    public CodeList findCodeList(int codeListId) {
        return findCodeListMap.get(codeListId);
    }

    private Map<Integer, List<CodeListValue>> findCodeListValueByCodeListIdMap;

    @Override
    public List<CodeListValue> findCodeListValueByCodeListId(int codeListId) {
        return (findCodeListValueByCodeListIdMap.containsKey(codeListId)) ? findCodeListValueByCodeListIdMap.get(codeListId) : Collections.emptyList();
    }

    private List<DataType> findDtList;

    @Override
    public List<DataType> findDT() {
        return Collections.unmodifiableList(findDtList);
    }

    private Map<Integer, DataType> findDtMap;

    @Override
    public DataType findDT(int dtId) {
        return findDtMap.get(dtId);
    }

    private Map<Integer, List<DataTypeSupplementaryComponent>> findDtScByOwnerDtIdMap;

    @Override
    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(int ownerDtId) {
        return (findDtScByOwnerDtIdMap.containsKey(ownerDtId)) ? findDtScByOwnerDtIdMap.get(ownerDtId) : Collections.emptyList();
    }

    private Map<Integer, List<BusinessDataTypePrimitiveRestriction>> findBdtPriRestriListByDtIdMap;

    @Override
    public List<BusinessDataTypePrimitiveRestriction> findBdtPriRestriListByDtId(int dtId) {
        return (findBdtPriRestriListByDtIdMap.containsKey(dtId)) ? findBdtPriRestriListByDtIdMap.get(dtId) : Collections.emptyList();
    }

    private Map<Integer, List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction>> findBdtScPriRestriListByDtScIdMap;

    @Override
    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriListByDtScId(int dtScId) {
        return (findBdtScPriRestriListByDtScIdMap.containsKey(dtScId)) ? findBdtScPriRestriListByDtScIdMap.get(dtScId) : Collections.emptyList();
    }

    private Map<Integer, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findCdtScAwdPriXpsTypeMapMap;

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(int cdtScAwdPriXpsTypeMapId) {
        return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
    }

    private Map<Integer, XSDBuiltInType> findXbtMap;

    @Override
    public XSDBuiltInType findXbt(int xbtId) {
        return findXbtMap.get(xbtId);
    }

    private List<AggregateCoreComponent> findACCList;

    @Override
    public List<AggregateCoreComponent> findACC() {
        return Collections.unmodifiableList(findACCList);
    }

    private Map<Integer, AggregateCoreComponent> findAccMap;

    @Override
    public AggregateCoreComponent findACC(int accId) {
        return findAccMap.get(accId);
    }

    private List<AssociationCoreComponentProperty> findASCCPList;

    @Override
    public List<AssociationCoreComponentProperty> findASCCP() {
        return Collections.unmodifiableList(findASCCPList);
    }

    private Map<Integer, AssociationCoreComponentProperty> findAsccpMap;

    @Override
    public AssociationCoreComponentProperty findASCCP(int asccpId) {
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

    private Map<Integer, BasicCoreComponentProperty> findBccpMap;

    @Override
    public BasicCoreComponentProperty findBCCP(int bccpId) {
        return findBccpMap.get(bccpId);
    }

    private Map<Integer, List<BasicCoreComponent>> findBCCByToBccpIdAndEntityTypeIs1Map;
    @Override
    public List<BasicCoreComponent> findBCCByToBccpIdAndEntityTypeIs1(int toBccpId) {
        return (findBCCByToBccpIdAndEntityTypeIs1Map.containsKey(toBccpId)) ? findBCCByToBccpIdAndEntityTypeIs1Map.get(toBccpId) : Collections.emptyList();
    }

    private Map<Integer, List<BasicCoreComponent>> findBccByFromAccIdMap;

    @Override
    public List<BasicCoreComponent> findBCCByFromAccId(int fromAccId) {
        return (findBccByFromAccIdMap.containsKey(fromAccId)) ? findBccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }

    private Map<Integer, List<AssociationCoreComponent>> findAsccByFromAccIdMap;

    @Override
    public List<AssociationCoreComponent> findASCCByFromAccId(int fromAccId) {
        return (findAsccByFromAccIdMap.containsKey(fromAccId)) ? findAsccByFromAccIdMap.get(fromAccId) : Collections.emptyList();
    }
}
