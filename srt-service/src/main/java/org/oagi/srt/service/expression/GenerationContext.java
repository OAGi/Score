package org.oagi.srt.service.expression;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class GenerationContext {

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
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbieScRepository;

    @Autowired
    private ContextCategoryRepository contextCategoryRepository;

    @Autowired
    private ContextSchemeRepository contextSchemeRepository;

    @Autowired
    private ContextSchemeValueRepository contextSchemeValueRepository;

    @Autowired
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    public void init(TopLevelAbie topLevelAbie) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findAll();
        findBdtPriRestriByBdtIdAndDefaultIsTrueMap = bdtPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtId(), Function.identity()));
        findBdtPriRestriMap = bdtPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
        findBdtScPriRestriByBdtIdAndDefaultIsTrueMap = bdtScPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtScId(), Function.identity()));
        findBdtScPriRestriMap = bdtScPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtScPriRestriId(), Function.identity()));

        List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList = cdtAwdPriXpsTypeMapRepository.findAll();
        findCdtAwdPriXpsTypeMapMap = cdtAwdPriXpsTypeMapList.stream()
                .collect(Collectors.toMap(e -> e.getCdtAwdPriXpsTypeMapId(), Function.identity()));

        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList = cdtScAwdPriXpsTypeMapRepository.findAll();
        findCdtScAwdPriXpsTypeMapMap = cdtScAwdPriXpsTypeMapList.stream()
                .collect(Collectors.toMap(e -> e.getCdtScAwdPriXpsTypeMapId(), Function.identity()));

        List<XSDBuiltInType> xbtList = xbtRepository.findAll();
        findXSDBuiltInTypeMap = xbtList.stream()
                .collect(Collectors.toMap(e -> e.getXbtId(), Function.identity()));

        List<CodeList> codeLists = codeListRepository.findAll();
        findCodeListMap = codeLists.stream()
                .collect(Collectors.toMap(e -> e.getCodeListId(), Function.identity()));

        List<CodeListValue> codeListValues = codeListValueRepository.findAll();
        findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap = codeListValues.stream()
                .filter(e -> e.isUsedIndicator())
                .collect(Collectors.groupingBy(e -> e.getCodeListId()));

        List<AggregateCoreComponent> accList = accRepository.findAll();
        findACCMap = accList.stream()
                .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));

        List<BasicCoreComponent> bccList = bccRepository.findAll();
        findBCCMap = bccList.stream()
                .collect(Collectors.toMap(e -> e.getBccId(), Function.identity()));

        List<BasicCoreComponentProperty> bccpList = bccpRepository.findAll();
        findBCCPMap = bccpList.stream()
                .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));

        List<AssociationCoreComponent> asccList = asccRepository.findAll();
        findASCCMap = asccList.stream()
                .collect(Collectors.toMap(e -> e.getAsccId(), Function.identity()));

        List<AssociationCoreComponentProperty> asccpList = asccpRepository.findAll();
        findASCCPMap = asccpList.stream()
                .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));

        List<DataType> dataTypeList = dataTypeRepository.findAll();
        findDTMap = dataTypeList.stream()
                .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));

        List<DataTypeSupplementaryComponent> dtScList = dtScRepository.findAll();
        findDtScMap = dtScList.stream()
                .collect(Collectors.toMap(e -> e.getDtScId(), Function.identity()));

        List<AgencyIdList> agencyIdLists = agencyIdListRepository.findAll();
        findAgencyIdListMap = agencyIdLists.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListId(), Function.identity()));

        List<AgencyIdListValue> agencyIdListValues = agencyIdListValueRepository.findAll();
        findAgencyIdListValueMap = agencyIdListValues.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListValueId(), Function.identity()));
        findAgencyIdListValueByOwnerListIdMap = agencyIdListValues.stream()
                .collect(Collectors.groupingBy(e -> e.getOwnerListId()));

        List<AggregateBusinessInformationEntity> abieList = abieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
        findAbieMap = abieList.stream()
                .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));

        List<BasicBusinessInformationEntity> bbieList =
                bbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
        findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                .filter(e -> e.isUsed())
                .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

        List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList =
                bbieScRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
        findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                .filter(e -> e.isUsed())
                .collect(Collectors.groupingBy(e -> e.getBbieId()));

        List<AssociationBusinessInformationEntity> asbieList =
                asbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
        findAsbieByFromAbieIdAndUsedIsTrueMap = asbieList.stream()
                .filter(e -> e.isUsed())
                .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

        List<AssociationBusinessInformationEntityProperty> asbiepList =
                asbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
        findASBIEPMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
        findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));

        List<BasicBusinessInformationEntityProperty> bbiepList =
                bbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
        findBBIEPMap = bbiepList.stream()
                .collect(Collectors.toMap(e -> e.getBbiepId(), Function.identity()));

        findUserNameMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getAppUserId(), e -> e.getLoginId()));
        findReleaseNumberMap = releaseRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getReleaseId(), e -> e.getReleaseNum()));
    }

    // Prepared Datas
    private Map<Long, BusinessDataTypePrimitiveRestriction> findBdtPriRestriByBdtIdAndDefaultIsTrueMap;

    public BusinessDataTypePrimitiveRestriction findBdtPriRestriByBdtIdAndDefaultIsTrue(long bdtId) {
        return findBdtPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtId);
    }

    private Map<Long, BusinessDataTypePrimitiveRestriction> findBdtPriRestriMap;

    public BusinessDataTypePrimitiveRestriction findBdtPriRestri(long bdtPriRestriId) {
        return findBdtPriRestriMap.get(bdtPriRestriId);
    }

    private Map<Long, CoreDataTypeAllowedPrimitiveExpressionTypeMap> findCdtAwdPriXpsTypeMapMap;

    public CoreDataTypeAllowedPrimitiveExpressionTypeMap findCdtAwdPriXpsTypeMap(long cdtAwdPriXpsTypeMapId) {
        return findCdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId);
    }

    private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriByBdtIdAndDefaultIsTrueMap;

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestriByBdtScIdAndDefaultIsTrue(long bdtScId) {
        return findBdtScPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtScId);
    }

    private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriMap;

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestri(long bdtScPriRestriId) {
        return findBdtScPriRestriMap.get(bdtScPriRestriId);
    }

    private Map<Long, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findCdtScAwdPriXpsTypeMapMap;

    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(long cdtScAwdPriXpsTypeMapId) {
        return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
    }

    private Map<Long, XSDBuiltInType> findXSDBuiltInTypeMap;

    public XSDBuiltInType findXSDBuiltInType(long xbtId) {
        return findXSDBuiltInTypeMap.get(xbtId);
    }

    private Map<Long, CodeList> findCodeListMap;

    public CodeList findCodeList(long codeListId) {
        return findCodeListMap.get(codeListId);
    }

    private Map<Long, List<CodeListValue>> findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap;

    public List<CodeListValue> findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(long codeListId) {
        return findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.containsKey(codeListId) ?
                findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.get(codeListId) :
                Collections.emptyList();
    }

    private Map<Long, AggregateCoreComponent> findACCMap;

    public AggregateCoreComponent findACC(long accId) {
        return findACCMap.get(accId);
    }

    private Map<Long, BasicCoreComponent> findBCCMap;

    public BasicCoreComponent findBCC(long bccId) {
        return findBCCMap.get(bccId);
    }

    private Map<Long, BasicCoreComponentProperty> findBCCPMap;

    public BasicCoreComponentProperty findBCCP(long bccpId) {
        return findBCCPMap.get(bccpId);
    }

    private Map<Long, AssociationCoreComponent> findASCCMap;

    public AssociationCoreComponent findASCC(long asccId) {
        return findASCCMap.get(asccId);
    }

    private Map<Long, AssociationCoreComponentProperty> findASCCPMap;

    public AssociationCoreComponentProperty findASCCP(long asccpId) {
        return findASCCPMap.get(asccpId);
    }

    private Map<Long, DataType> findDTMap;

    public DataType findDT(long dtId) {
        return findDTMap.get(dtId);
    }

    private Map<Long, DataTypeSupplementaryComponent> findDtScMap;

    public DataTypeSupplementaryComponent findDtSc(long dtScId) {
        return findDtScMap.get(dtScId);
    }

    private Map<Long, AgencyIdList> findAgencyIdListMap;

    public AgencyIdList findAgencyIdList(long agencyIdListId) {
        return findAgencyIdListMap.get(agencyIdListId);
    }

    private Map<Long, AgencyIdListValue> findAgencyIdListValueMap;
    private Map<Long, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;

    public AgencyIdListValue findAgencyIdListValue(long agencyIdListValueId) {
        return findAgencyIdListValueMap.get(agencyIdListValueId);
    }

    public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(long ownerListId) {
        return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ?
                findAgencyIdListValueByOwnerListIdMap.get(ownerListId) :
                Collections.emptyList();
    }

    private Map<Long, AggregateBusinessInformationEntity> findAbieMap;

    public AggregateBusinessInformationEntity findAbie(long abieId) {
        return findAbieMap.get(abieId);
    }

    private Map<Long, List<BasicBusinessInformationEntity>> findBbieByFromAbieIdAndUsedIsTrueMap;

    public List<BasicBusinessInformationEntity> findBbieByFromAbieIdAndUsedIsTrue(long fromAbieId) {
        return findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                Collections.emptyList();
    }

    private Map<Long, List<BasicBusinessInformationEntitySupplementaryComponent>>
            findBbieScByBbieIdAndUsedIsTrueMap;

    public List<BasicBusinessInformationEntitySupplementaryComponent> findBbieScByBbieIdAndUsedIsTrue(long bbieId) {
        return findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                Collections.emptyList();
    }

    private Map<Long, List<AssociationBusinessInformationEntity>> findAsbieByFromAbieIdAndUsedIsTrueMap;

    public List<AssociationBusinessInformationEntity> findAsbieByFromAbieIdAndUsedIsTrue(long fromAbieId) {
        return findAsbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                findAsbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                Collections.emptyList();
    }

    private Map<Long, AssociationBusinessInformationEntityProperty> findASBIEPMap;

    public AssociationBusinessInformationEntityProperty findASBIEP(long asbiepId) {
        return findASBIEPMap.get(asbiepId);
    }

    private Map<Long, AssociationBusinessInformationEntityProperty> findAsbiepByRoleOfAbieIdMap;

    public AssociationBusinessInformationEntityProperty findAsbiepByRoleOfAbieId(long roleOfAbieId) {
        return findAsbiepByRoleOfAbieIdMap.get(roleOfAbieId);
    }

    private Map<Long, BasicBusinessInformationEntityProperty> findBBIEPMap;

    public BasicBusinessInformationEntityProperty findBBIEP(long bbiepId) {
        return findBBIEPMap.get(bbiepId);
    }

    private Map<Long, String> findUserNameMap;

    public String findUserName(long userId) {
        return findUserNameMap.get(userId);
    }

    private Map<Long, String> findReleaseNumberMap;

    public String findReleaseNumber(long releaseId) {
        return findReleaseNumberMap.get(releaseId);
    }

    public AggregateCoreComponent queryBasedACC(AggregateBusinessInformationEntity abie) {
        long basedAccId = abie.getBasedAccId();
        return findACC(basedAccId);
    }

    // Get only Child BIEs whose is_used flag is true
    public List<BusinessInformationEntity> queryChildBIEs(AggregateBusinessInformationEntity abie) {
        List<BusinessInformationEntity> result;
        Map<BusinessInformationEntity, Double> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        Map<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

        List<AssociationBusinessInformationEntity> asbievo = findAsbieByFromAbieIdAndUsedIsTrue(abie.getAbieId());
        List<BasicBusinessInformationEntity> bbievo = findBbieByFromAbieIdAndUsedIsTrue(abie.getAbieId());

        for (BasicBusinessInformationEntity aBasicBusinessInformationEntity : bbievo) {
            if (aBasicBusinessInformationEntity.getCardinalityMax() != 0) //modify
                sequence.put(aBasicBusinessInformationEntity, aBasicBusinessInformationEntity.getSeqKey());
        }

        for (AssociationBusinessInformationEntity aAssociationBusinessInformationEntity : asbievo) {
            if (aAssociationBusinessInformationEntity.getCardinalityMax() != 0)
                sequence.put(aAssociationBusinessInformationEntity, aAssociationBusinessInformationEntity.getSeqKey());
        }

        ordered_sequence.putAll(sequence);
        Set set = ordered_sequence.entrySet();
        Iterator i = set.iterator();
        result = new ArrayList();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            result.add((BusinessInformationEntity) me.getKey());
        }

        return result;
    }

    class ValueComparator implements Comparator<BusinessInformationEntity> {

        Map<BusinessInformationEntity, Double> base;

        public ValueComparator(Map<BusinessInformationEntity, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(BusinessInformationEntity a, BusinessInformationEntity b) {
            if (base.get(a) <= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    // Get only SCs whose is_used is true.
    public List<BasicBusinessInformationEntitySupplementaryComponent> queryBBIESCs(BasicBusinessInformationEntity bbie) {
        long bbieId = bbie.getBbieId();
        return findBbieScByBbieIdAndUsedIsTrue(bbieId);
    }

    public AssociationBusinessInformationEntityProperty receiveASBIEP(AggregateBusinessInformationEntity abie) {
        return findAsbiepByRoleOfAbieId(abie.getAbieId());
    }

    public DataType queryBDT(BasicBusinessInformationEntity bbie) {
        BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
        BasicCoreComponentProperty bccp = findBCCP(bcc.getToBccpId());
        return queryBDT(bccp);
    }

    public DataType queryBDT(BasicCoreComponentProperty bccp) {
        DataType bdt = findDT(bccp.getBdtId());
        return bdt;
    }

    public AssociationCoreComponentProperty queryBasedASCCP(AssociationBusinessInformationEntityProperty asbiep) {
        AssociationCoreComponentProperty asccp = findASCCP(asbiep.getBasedAsccpId());
        return asccp;
    }

    public AssociationCoreComponent queryBasedASCC(AssociationBusinessInformationEntity asbie) {
        AssociationCoreComponent ascc = findASCC(asbie.getBasedAsccId());
        return ascc;
    }

    public AggregateBusinessInformationEntity queryTargetABIE(AssociationBusinessInformationEntityProperty asbiep) {
        AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());
        return abie;
    }

    public AggregateCoreComponent queryTargetACC(AssociationBusinessInformationEntityProperty asbiep) {
        AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());

        AggregateCoreComponent acc = findACC(abie.getBasedAccId());
        return acc;
    }

    public AggregateBusinessInformationEntity queryTargetABIE2(AssociationBusinessInformationEntityProperty asbiep) {
        AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());
        return abie;
    }

    public BasicCoreComponent queryBasedBCC(BasicBusinessInformationEntity bbie) {
        BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
        return bcc;
    }

    public BasicCoreComponentProperty queryToBCCP(BasicCoreComponent bcc) {
        return findBCCP(bcc.getToBccpId());
    }

    public CodeList getCodeList(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        CodeList codeList = findCodeList(bbieSc.getCodeListId());
        if (codeList != null) {
            return codeList;
        }

        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            return findCodeList(bdtScPriRestri.getCodeListId());
        } else {
            DataTypeSupplementaryComponent gDTSC = findDtSc(bbieSc.getDtScId());
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bBDTSCPrimitiveRestriction =
                    findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
            if (bBDTSCPrimitiveRestriction != null) {
                codeList = findCodeList(bBDTSCPrimitiveRestriction.getCodeListId());
            }
        }

        return codeList;
    }

    public AgencyIdList getAgencyIdList(BasicBusinessInformationEntity bbie) {
        AgencyIdList agencyIdList = findAgencyIdList(bbie.getAgencyIdListId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                findBdtPriRestri(bbie.getBdtPriRestriId());
        if (bdtPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
        }

        if (agencyIdList == null) {
            DataType bdt = queryAssocBDT(bbie);
            bdtPriRestri = findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
            if (bdtPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
            }
        }
        return agencyIdList;
    }

    public AgencyIdList getAgencyIdList(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        AgencyIdList agencyIdList = findAgencyIdList(bbieSc.getAgencyIdListId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
        }

        if (agencyIdList == null) {
            DataTypeSupplementaryComponent gDTSC = findDtSc(bbieSc.getDtScId());
            bdtScPriRestri = findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
            }
        }
        return agencyIdList;
    }

    public List<CodeListValue> getCodeListValues(CodeList codeList) {
        return findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(codeList.getCodeListId());
    }

    public AssociationBusinessInformationEntityProperty queryAssocToASBIEP(AssociationBusinessInformationEntity asbie) {
        AssociationBusinessInformationEntityProperty asbiepVO = findASBIEP(asbie.getToAsbiepId());
        return asbiepVO;
    }

    public DataType queryAssocBDT(BasicBusinessInformationEntity bbie) {
        BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
        BasicCoreComponentProperty bccp = findBCCP(bcc.getToBccpId());
        return queryBDT(bccp);
    }

    public BusinessContext findBusinessContext(TopLevelAbie topLevelAbie) {
        long bizCtxId = topLevelAbie.getAbie().getBizCtxId();
        return businessContextRepository.findById(bizCtxId).orElse(null);
    }

    public List<ContextSchemeValue> findContextSchemeValue(BusinessContext businessContext) {
        List<BusinessContextValue> businessContextValues =
                businessContextValueRepository.findByBizCtxId(businessContext.getBizCtxId());

        return businessContextValues.stream().map(e -> e.getContextSchemeValue()).collect(Collectors.toList());
    }

    public AgencyIdList findAgencyIdList(ContextScheme contextScheme) {
        String schemeAgencyId = contextScheme.getSchemeAgencyId();
        if (StringUtils.isEmpty(schemeAgencyId)) {
            return null;
        }

        for (AgencyIdList agencyIdList : findAgencyIdListMap.values()) {
            if (schemeAgencyId.equals(agencyIdList.getListId())) {
                return agencyIdList;
            }
        }

        return null;
    }
}
