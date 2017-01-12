package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.model.bie.BBIERestrictionType.*;
import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Editing;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Element;
import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Service
@Transactional(readOnly = true)
public class BusinessInformationEntityService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbiescRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    public class CreateBIEsResult {

        private int abieCount = 0;
        private int bbiescCount = 0;
        private int asbiepCount = 0;
        private int asbieCount = 0;
        private int bbiepCount = 0;
        private int bbieCount = 0;

        private long userId;
        private AssociationCoreComponentProperty asccp;
        private AggregateCoreComponent acc;
        private TopLevelAbie topLevelAbie;

        private CreateBIEsResult(long userId, AssociationCoreComponentProperty asccp,
                                 AggregateCoreComponent acc, TopLevelAbie topLevelAbie) {
            this.userId = userId;
            this.asccp = asccp;
            this.acc = acc;
            this.topLevelAbie = topLevelAbie;
        }

        public AggregateCoreComponent getAcc() {
            return acc;
        }

        public TopLevelAbie getTopLevelAbie() {
            return topLevelAbie;
        }

        public int getAbieCount() {
            return abieCount;
        }

        public int getBbiescCount() {
            return bbiescCount;
        }

        public int getAsbiepCount() {
            return asbiepCount;
        }

        public int getAsbieCount() {
            return asbieCount;
        }

        public int getBbiepCount() {
            return bbiepCount;
        }

        public int getBbieCount() {
            return bbieCount;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public CreateBIEsResult createBIEs(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        long userId = userRepository.findAppUserIdByLoginId("oagis");
        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
        TopLevelAbie topLevelAbie = createTopLevelAbie(bizCtx);
        CreateBIEsResult createBIEsResult = new CreateBIEsResult(userId, asccp, acc, topLevelAbie);

        AggregateBusinessInformationEntity abie = createABIE(userId, acc, topLevelAbie, bizCtx, createBIEsResult);
        updateTopLevelAbie(topLevelAbie, abie);

        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(userId, asccp, topLevelAbie, createBIEsResult);
        CreateBIEContext createBIEContext = new CreateBIEContext(userId, topLevelAbie, createBIEsResult);
        createBIEs(createBIEContext, roleOfAccId, abie);
        createBIEContext.save();

        return createBIEsResult;
    }

    private TopLevelAbie createTopLevelAbie(BusinessContext bizCtx) {
        TopLevelAbie topLevelAbie = new TopLevelAbie();
        return topLevelAbieRepository.saveAndFlush(topLevelAbie);
    }

    private AggregateBusinessInformationEntity createABIE(long userId, AggregateCoreComponent acc,
                                                          TopLevelAbie topLevelAbie, BusinessContext bizCtx,
                                                          CreateBIEsResult createBIEsResult) {

        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAcc(acc);
        abie.setBizCtx(bizCtx);
        abie.setDefinition(acc.getDefinition());
        abie.setCreatedBy(userId);
        abie.setLastUpdatedBy(userId);
        abie.setState(Editing);
        abie.setOwnerTopLevelAbie(topLevelAbie);

        abieRepository.saveAndFlush(abie);
        createBIEsResult.abieCount++;

        return abie;
    }

    private void updateTopLevelAbie(TopLevelAbie topLevelAbie, AggregateBusinessInformationEntity abie) {
        topLevelAbie.setAbie(abie);
        topLevelAbieRepository.save(topLevelAbie);
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(long userId,
                                                                      AssociationCoreComponentProperty asccp,
                                                                      TopLevelAbie topLevelAbie,
                                                                      CreateBIEsResult createBIEsResult) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccp(asccp);
        asbiep.setRoleOfAbieId(topLevelAbie.getAbie().getAbieId());
        asbiep.setCreatedBy(userId);
        asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(asccp.getDefinition());
        asbiep.setOwnerTopLevelAbie(topLevelAbie);

        asbiepRepository.saveAndFlush(asbiep);
        createBIEsResult.asbiepCount++;

        return asbiep;
    }

    public void createBIEs(CreateBIEContext createBIEContext, long accId, AggregateBusinessInformationEntity abie) {
        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        AggregateCoreComponent aggregateCoreComponent = createBIEContext.getACC(accId);
        accList.add(aggregateCoreComponent);
        while (aggregateCoreComponent.getBasedAccId() > 0) {
            aggregateCoreComponent = createBIEContext.getACC(aggregateCoreComponent.getBasedAccId());
            accList.add(aggregateCoreComponent);
        }

        int seqKey = 1;
        while (!accList.isEmpty()) {
            aggregateCoreComponent = accList.pollLast();

            List<CoreComponentRelation> childAssoc = queryNestedChildAssoc(createBIEContext, aggregateCoreComponent);
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (Attribute == bcc.getEntityType()) {
                        createBIEContext.createBBIETree(bcc, abie, 0);
                    }
                }
            }

            for (CoreComponent assoc : childAssoc) {
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (Element == bcc.getEntityType()) {
                        createBIEContext.createBBIETree(bcc, abie, seqKey++);
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    createBIEContext.createASBIETree(ascc, abie, seqKey++);
                }
            }
        }
    }

    private List<CoreComponentRelation> queryNestedChildAssoc(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponentRelation> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponentRelation> queryNestedChildAssoc_wo_attribute(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponentRelation> assoc = coreComponentService.getCoreComponentsWithoutAttributes(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponentRelation> getAssocList(CreateBIEContext createBIEContext, List<CoreComponentRelation> list) {
        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && groupcheck(createBIEContext, (AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent = createBIEContext.getACC(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(createBIEContext, aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private boolean groupcheck(CreateBIEContext createBIEContext, AssociationCoreComponent associationCoreComponent) {
        AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
        AggregateCoreComponent acc = createBIEContext.getACC(asccp.getRoleOfAccId());
        OagisComponentType oagisComponentType = acc.getOagisComponentType();
        return (oagisComponentType == SemanticGroup || oagisComponentType == UserExtensionGroup) ? true : false;
    }

    private List<CoreComponentRelation> handleNestedGroup(CreateBIEContext createBIEContext,
                                                          AggregateCoreComponent acc,
                                                          List<CoreComponentRelation> coreComponents, int gPosition) {

        List<CoreComponentRelation> bList = queryChildAssoc(createBIEContext, acc);
        if (!bList.isEmpty()) {
            coreComponents.addAll(gPosition, bList);
            coreComponents.remove(gPosition + bList.size());
        }

        for (int i = 0; i < coreComponents.size(); i++) {
            CoreComponent coreComponent = coreComponents.get(i);
            if (coreComponent instanceof AssociationCoreComponent &&
                    groupcheck(createBIEContext, (AssociationCoreComponent) coreComponent)) {

                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(ascc.getToAsccpId());
                coreComponents = handleNestedGroup(
                        createBIEContext, createBIEContext.getACC(asccp.getRoleOfAccId()), coreComponents, i);
            }
        }

        return coreComponents;
    }

    private List<CoreComponentRelation> queryChildAssoc(CreateBIEContext createBIEContext,
                                                        AggregateCoreComponent acc) {
        List<CoreComponentRelation> assoc = coreComponentService.getCoreComponents(acc, createBIEContext);
        return assoc;
    }

    private class CreateBIEContext implements CoreComponentProvider {
        private ABIETaskHolder abieTaskHolder;
        private BBIETreeTaskHolder bbieTreeTaskHolder;
        private ASBIETreeTaskHolder asbieTreeTaskHolder;

        private Map<Long, AggregateCoreComponent> aggregateCoreComponentMap;
        private Map<Long, AssociationCoreComponentProperty> associationCoreComponentPropertyMap;
        private Map<Long, BasicCoreComponentProperty> basicCoreComponentPropertyMap;

        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;

        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriMap;
        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriDefaultMap;
        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriCodeListMap;

        private long userId;
        private TopLevelAbie topLevelAbie;
        private CreateBIEsResult createBIEsResult;

        private List<BasicCoreComponent> basicCoreComponents;
        private List<AssociationCoreComponent> associationCoreComponents;
        private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;
        private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;
        private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList;

        public CreateBIEContext(long userId, TopLevelAbie topLevelAbie, CreateBIEsResult createBIEsResult) {
            abieTaskHolder = new ABIETaskHolder();
            bbieTreeTaskHolder = new BBIETreeTaskHolder();
            asbieTreeTaskHolder = new ASBIETreeTaskHolder();

            this.userId = userId;
            this.topLevelAbie = topLevelAbie;
            this.createBIEsResult = createBIEsResult;

            aggregateCoreComponentMap =
                    accRepository.findAllWithRevisionNum(0).stream()
                            .collect(Collectors.toMap(acc -> acc.getAccId(), Function.identity()));
            associationCoreComponentPropertyMap =
                    asccpRepository.findAllWithRevisionNum(0).stream()
                            .collect(Collectors.toMap(asccp -> asccp.getAsccpId(), Function.identity()));
            basicCoreComponents = bccRepository.findAllWithRevisionNum(0);
            associationCoreComponents = asccRepository.findAllWithRevisionNum(0);
            dataTypeSupplementaryComponents = dtScRepository.findAll();

            basicCoreComponentPropertyMap = bccpRepository.findAllWithRevisionNum(0).stream()
                    .collect(Collectors.toMap(bccp -> bccp.getBccpId(), Function.identity()));

            bdtPriRestriList = bdtPriRestriRepository.findAll();
            bdtPriRestriMap = bdtPriRestriList.stream()
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtPriRestriId(), Function.identity()));
            bdtPriRestriDefaultMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
            bdtPriRestriCodeListMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));

            bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            bdtScPriRestriMap = bdtScPriRestriList.stream()
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScPriRestriId(), Function.identity()));
            bdtScPriRestriDefaultMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
            bdtScPriRestriCodeListMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
        }

        public long getUserId() {
            return userId;
        }

        public TopLevelAbie getBod() {
            return topLevelAbie;
        }

        public AggregateCoreComponent getACC(long accId) {
            return aggregateCoreComponentMap.get(accId);
        }

        public AssociationCoreComponentProperty getASCCP(long asccpId) {
            return associationCoreComponentPropertyMap.get(asccpId);
        }

        public AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc) {
            return abieTaskHolder.createABIE(userId, acc, topLevelAbie.getAbie().getBizCtxId());
        }

        public void createBBIETree(BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            bbieTreeTaskHolder.createBBIETree(this, bcc, abie, seqKey);
        }

        public void createASBIETree(AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            asbieTreeTaskHolder.createASBIETree(this, asccVO, abie, seqKey);
        }

        @Override
        public List<BasicCoreComponent> getBCCs(long fromAccId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
            return getBCCs(accId).stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.toList());
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(long fromAccId) {
            return associationCoreComponents.stream()
                    .filter(acc -> acc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        public BasicCoreComponentProperty getBCCP(long toBccpId) {
            return basicCoreComponentPropertyMap.get(toBccpId);
        }

        public BusinessDataTypePrimitiveRestriction getBdtPriRestri(long bdtPriRestriId) {
            return bdtPriRestriMap.get(bdtPriRestriId);
        }

        public long getDefaultBdtPriRestriId(long bdtId) {
            return bdtPriRestriDefaultMap.get(bdtId).getBdtPriRestriId();
        }

        public long getCodeListIdOfBdtPriRestriId(long bdtId) {
            BusinessDataTypePrimitiveRestriction e = bdtPriRestriCodeListMap.get(bdtId);
            return (e != null) ? e.getCodeListId() : 0L;
        }

        public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri(long bdtScPriRestriId) {
            return bdtScPriRestriMap.get(bdtScPriRestriId);
        }

        public long getDefaultBdtScPriRestriId(long bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriDefaultMap.get(bdtScId);
            return (e != null) ? e.getBdtScPriRestriId() : 0L;
        }

        public long getCodeListIdOfBdtScPriRestriId(long bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriCodeListMap.get(bdtScId);
            return (e != null) ? e.getCodeListId() : 0L;
        }

        public List<DataTypeSupplementaryComponent> findByOwnerDtId(long ownerDtId) {
            return dataTypeSupplementaryComponents.stream()
                    .filter(dtSc -> dtSc.getOwnerDtId() == ownerDtId)
                    .collect(Collectors.toList());
        }

        public void save() {
            abieTaskHolder.save(topLevelAbie, createBIEsResult);
            bbieTreeTaskHolder.save(topLevelAbie, createBIEsResult);
            asbieTreeTaskHolder.save(topLevelAbie, createBIEsResult);
        }
    }

    private class ABIETaskHolder {

        private List<AggregateBusinessInformationEntity> aggregateBusinessInformationEntitys = new ArrayList();

        public AggregateBusinessInformationEntity createABIE(long userId, AggregateCoreComponent acc, long bizCtxId) {
            AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
            String abieGuid = Utility.generateGUID();
            abie.setGuid(abieGuid);
            abie.setBasedAcc(acc);
            abie.setBizCtxId(bizCtxId);
            abie.setDefinition(acc.getDefinition());
            abie.setCreatedBy(userId);
            abie.setLastUpdatedBy(userId);
            abie.setState(Editing);

            aggregateBusinessInformationEntitys.add(abie);

            return abie;
        }

        public void save(TopLevelAbie topLevelAbie, CreateBIEsResult createBIEsResult) {
            aggregateBusinessInformationEntitys.stream()
                    .forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            abieRepository.save(aggregateBusinessInformationEntitys);
            createBIEsResult.abieCount += aggregateBusinessInformationEntitys.size();
        }

    }

    private class BBIETreeTaskHolder {
        private List<CreateBBIETreeTask> createBBIETreeTasks = new ArrayList();

        public void createBBIETree(CreateBIEContext createBIEContext, BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            createBBIETreeTasks.add(new CreateBBIETreeTask(createBIEContext, bcc, abie, seqKey));
        }

        public void save(TopLevelAbie topLevelAbie, CreateBIEsResult createBIEsResult) {
            List<BasicBusinessInformationEntityProperty> bbiepList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbiep())
                            .collect(Collectors.toList());
            bbiepList.stream().forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            bbiepRepository.save(bbiepList);
            createBIEsResult.bbiepCount += createBBIETreeTasks.size();

            List<BasicBusinessInformationEntity> bbieList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbie())
                            .collect(Collectors.toList());
            bbieList.stream().forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            bbieRepository.save(bbieList);
            createBIEsResult.bbieCount += createBBIETreeTasks.size();

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new ArrayList();
            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        bbieScList.addAll(task.getBbieScList());
                    });
            bbieScList.stream().forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            bbiescRepository.save(bbieScList);
            createBIEsResult.bbiescCount += bbieScList.size();
        }
    }

    private class CreateBBIETreeTask {

        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public CreateBBIETreeTask(CreateBIEContext createBIEContext, BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.bcc = bcc;
            this.abie = abie;
            this.seqKey = seqKey;

            BasicCoreComponentProperty bccp = createBIEContext.getBCCP(bcc.getToBccpId());
            long bdtId = bccp.getBdtId();
            long bdtPrimitiveRestrictionId = createBIEContext.getDefaultBdtPriRestriId(bdtId);
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    createBIEContext.getBdtPriRestri(bdtPrimitiveRestrictionId);
            long codeListId = createBIEContext.getCodeListIdOfBdtPriRestriId(bdtId);

            createBBIEP(createBIEContext.getUserId(), bccp);
            createBBIE(createBIEContext.getUserId(), bdtPriRestri, codeListId);
            createBBIESC(createBIEContext, bdtId);
        }

        private void createBBIEP(long userId, BasicCoreComponentProperty bccp) {
            bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccp(bccp);
            bbiep.setCreatedBy(userId);
            bbiep.setLastUpdatedBy(userId);
            bbiep.setDefinition(bccp.getDefinition());
        }

        private void createBBIE(long userId, BusinessDataTypePrimitiveRestriction bdtPriRestri, long codeListId) {
            bbie = new BasicBusinessInformationEntity();
            bbie.setGuid(Utility.generateGUID());
            bbie.setBasedBccId(bcc.getBccId());
            bbie.setFromAbie(abie);
            bbie.setToBbiep(bbiep);
            bbie.setNillable(false);
            bbie.setCardinalityMax(bcc.getCardinalityMax());
            bbie.setCardinalityMin(bcc.getCardinalityMin());
            bbie.setBdtPriRestri(bdtPriRestri);
//            if (codeListId > 0) {
//                bbie.setCodeListId(codeListId);
//            }
            bbie.setCreatedBy(userId);
            bbie.setLastUpdatedBy(userId);
            bbie.setSeqKey(seqKey);
        }

        private void createBBIESC(CreateBIEContext createBIEContext, long bdtId) {
            bbieScList = createBIEContext.findByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getCardinalityMax() != 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        bbieSc.setBbie(bbie);
                        long bdtScId = dtSc.getDtScId();
                        bbieSc.setDtSc(dtSc);
                        bbieSc.setGuid(Utility.generateGUID());
                        long bdtScPriRestriId = createBIEContext.getDefaultBdtScPriRestriId(bdtScId);
                        if (bdtScPriRestriId > 0L) {
                            bbieSc.setDtScPriRestri(createBIEContext.getBdtScPriRestri(bdtScPriRestriId));
                        }
                        long codeListId = createBIEContext.getCodeListIdOfBdtScPriRestriId(bdtScId);
//                        if (codeListId > 0) {
//                            bbieSc.setCodeListId(codeListId);
//                        }
                        bbieSc.setCardinalityMax(dtSc.getCardinalityMax());
                        bbieSc.setCardinalityMin(dtSc.getCardinalityMin());
                        bbieSc.setDefinition(dtSc.getDefinition());
                        return bbieSc;
                    })
                    .collect(Collectors.toList());
        }

        public AggregateBusinessInformationEntity getAbie() {
            return abie;
        }

        public BasicBusinessInformationEntityProperty getBbiep() {
            return bbiep;
        }

        public BasicBusinessInformationEntity getBbie() {
            return bbie;
        }

        public List<BasicBusinessInformationEntitySupplementaryComponent> getBbieScList() {
            return bbieScList;
        }
    }

    private class CreateASBIETreeTask {

        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public CreateASBIETreeTask(long userId, AssociationCoreComponentProperty asccp, AggregateBusinessInformationEntity roleOfAbie,
                                   AssociationCoreComponent ascc, AggregateBusinessInformationEntity fromAbie, int seqKey) {
            this.asccp = asccp;
            this.roleOfAbie = roleOfAbie;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;

            createASBIEP(userId);
            createASBIE(userId);
        }

        public void createASBIEP(long userId) {
            asbiep = new AssociationBusinessInformationEntityProperty();
            asbiep.setGuid(Utility.generateGUID());
            asbiep.setBasedAsccp(asccp);
            asbiep.setRoleOfAbie(roleOfAbie);
            asbiep.setCreatedBy(userId);
            asbiep.setLastUpdatedBy(userId);
            asbiep.setDefinition(asccp.getDefinition());
        }

        public void createASBIE(long userId) {
            asbie = new AssociationBusinessInformationEntity();
            asbie.setGuid(Utility.generateGUID());
            asbie.setFromAbie(fromAbie);
            asbie.setToAsbiep(asbiep);
            asbie.setBasedAscc(ascc);
            asbie.setCardinalityMax(ascc.getCardinalityMax());
            asbie.setCardinalityMin(ascc.getCardinalityMin());
            asbie.setDefinition(ascc.getDefinition());
            asbie.setCreatedBy(userId);
            asbie.setLastUpdatedBy(userId);
            asbie.setSeqKey(seqKey);
        }

        public AggregateBusinessInformationEntity getRoleOfAbie() {
            return roleOfAbie;
        }

        public AggregateBusinessInformationEntity getFromAbie() {
            return fromAbie;
        }

        public AssociationBusinessInformationEntityProperty getAsbiep() {
            return asbiep;
        }

        public AssociationBusinessInformationEntity getAsbie() {
            return asbie;
        }
    }

    private class ASBIETreeTaskHolder {
        private List<CreateASBIETreeTask> createASBIETreeTasks = new ArrayList();

        public void createASBIETree(CreateBIEContext createBIEContext, AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(asccVO.getToAsccpId());
            AggregateCoreComponent acc = createBIEContext.getACC(asccp.getRoleOfAccId());

            AggregateBusinessInformationEntity newAbie = createBIEContext.createABIE(acc);

            createASBIETreeTasks.add(new CreateASBIETreeTask(createBIEContext.getUserId(), asccp, newAbie, asccVO, abie, seqKey));
            createBIEs(createBIEContext, acc.getAccId(), newAbie);
        }

        public void save(TopLevelAbie topLevelAbie, CreateBIEsResult createBIEsResult) {
            List<AssociationBusinessInformationEntityProperty> asbiepList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbiep())
                            .collect(Collectors.toList());
            asbiepList.stream().forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            asbiepRepository.save(asbiepList);
            createBIEsResult.asbiepCount += createASBIETreeTasks.size();

            List<AssociationBusinessInformationEntity> asbieList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbie())
                            .collect(Collectors.toList());
            asbieList.stream().forEach(e -> e.setOwnerTopLevelAbie(topLevelAbie));
            asbieRepository.save(asbieList);
            createBIEsResult.asbieCount += createASBIETreeTasks.size();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteProfileBOD(long topLevelAbieId) {
        asbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        asbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbiescRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.updateAbieToNull(topLevelAbieId);
        abieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.delete(topLevelAbieId);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(long toplevelAbieId, AggregateBusinessInformationEntityState state) {
        topLevelAbieRepository.updateState(toplevelAbieId, state);
        abieRepository.updateState(toplevelAbieId, state);
    }


    /*
     * for BBIE Primitive Restriction
     */
    public Map<BBIERestrictionType, BBIERestrictionType> getAvailablePrimitiveRestrictions(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = getBdtPriRestriList(node);
        Map<BBIERestrictionType, BBIERestrictionType> availablePrimitiveRestrictions = new LinkedHashMap();

        List<BBIERestrictionType> restrictionTypes = bdtPriRestriList.stream().map(e -> {
            if (e.getCdtAwdPriXpsTypeMapId() > 0L) {
                return Primitive;
            } else if (e.getCodeListId() > 0L) {
                return Code;
            } else {
                return Agency;
            }
        }).collect(Collectors.toList());

        if (hasOnlyCdtAwdPriXpsTypeMap(bdtPriRestriList)) {
            availablePrimitiveRestrictions.put(Primitive, Primitive);
            availablePrimitiveRestrictions.put(Code, Code);
            availablePrimitiveRestrictions.put(Agency, Agency);
        } else {
            if (restrictionTypes.contains(Primitive)) {
                availablePrimitiveRestrictions.put(Primitive, Primitive);
            }
            if (restrictionTypes.contains(Code)) {
                availablePrimitiveRestrictions.put(Code, Code);
            }
            if (restrictionTypes.contains(Agency)) {
                availablePrimitiveRestrictions.put(Agency, Agency);
            }
        }

        return availablePrimitiveRestrictions;
    }

    private boolean hasOnlyCdtAwdPriXpsTypeMap(List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList) {
        long sum = bdtPriRestriList.stream().mapToLong(e -> e.getCodeListId() + e.getAgencyIdListId()).sum();
        return (sum == 0L);
    }

    public String getBdtPrimitiveRestrictionName(BBIENode node) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri = node.getBdtPriRestri();
        if (bdtPriRestri == null) {
            return null;
        }

        Map<String, BusinessDataTypePrimitiveRestriction> bdtPrimitiveRestrictions =
                getBdtPrimitiveRestrictions(node);
        for (Map.Entry<String, BusinessDataTypePrimitiveRestriction> e : bdtPrimitiveRestrictions.entrySet()) {
            if (e.getValue().getCdtAwdPriXpsTypeMapId() == bdtPriRestri.getCdtAwdPriXpsTypeMapId()) {
                return e.getKey();
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void setBdtPrimitiveRestriction(BBIENode node, String name) {
        Map<String, BusinessDataTypePrimitiveRestriction> bdtPrimitiveRestrictions =
                getBdtPrimitiveRestrictions(node);
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                bdtPrimitiveRestrictions.get(name);
        node.setBdtPriRestri(bdtPriRestri);
    }

    public Map<String, BusinessDataTypePrimitiveRestriction> getBdtPrimitiveRestrictions(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = getBdtPriRestriList(node).stream()
                .filter(e -> e.getCdtAwdPriXpsTypeMapId() > 0L)
                .collect(Collectors.toList());

        Map<String, BusinessDataTypePrimitiveRestriction> bdtPrimitiveRestrictions = new LinkedHashMap();
        for (BusinessDataTypePrimitiveRestriction e : bdtPriRestriList) {
            long cdtAwdPriXpsTypeMapId = e.getCdtAwdPriXpsTypeMapId();
            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                    cdtAwdPriXpsTypeMapRepository.findOne(cdtAwdPriXpsTypeMapId);
            long xbtId = cdtAwdPriXpsTypeMap.getXbtId();
            XSDBuiltInType xbt = xbtRepository.findOne(xbtId);
            bdtPrimitiveRestrictions.put(xbt.getName(), e);
        }

        return bdtPrimitiveRestrictions;
    }

    public String getCodeListName(BBIENode node) {
        Map<String, CodeList> codeListMap = getCodeLists(node);
        for (CodeList codeList : codeListMap.values()) {
            if (codeList.getCodeListId() == node.getCodeListId()) {
                return codeList.getName();
            }
        }
        return null;
    }

    public Map<String, CodeList> getCodeLists(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = getBdtPriRestriList(node).stream()
                .filter(e -> e.getCodeListId() > 0L)
                .collect(Collectors.toList());
        BusinessDataTypePrimitiveRestriction bdtPriRestri = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);

        Map<String, CodeList> codeListMap;
        if (bdtPriRestri != null) {
            codeListMap = new LinkedHashMap();
            CodeList codeList = codeListRepository.findOne(bdtPriRestri.getCodeListId());
            while (codeList != null) {
                codeListMap.put(codeList.getName(), codeList);
                long basedCodeListId = codeList.getBasedCodeListId();
                if (basedCodeListId > 0L) {
                    codeList = codeListRepository.findOne(basedCodeListId);
                } else {
                    codeList = null;
                }
            }
        } else {
            codeListMap = codeListRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getName(), Function.identity()));
        }

        return codeListMap;
    }

    public String getBbieAgencyIdListName(BBIENode node) {
        Map<String, AgencyIdList> agencyIdListMap = getAgencyIdListIds(node);
        for (AgencyIdList agencyIdList : agencyIdListMap.values()) {
            if (agencyIdList.getAgencyIdListId() == node.getAgencyIdListId()) {
                return agencyIdList.getName();
            }
        }
        return null;
    }

    public Map<String, AgencyIdList> getAgencyIdListIds(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = getBdtPriRestriList(node).stream()
                .filter(e -> e.getAgencyIdListId() > 0L)
                .collect(Collectors.toList());
        BusinessDataTypePrimitiveRestriction bdtPriRestri = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
        Map<String, AgencyIdList> agencyIdListIdMap;
        if (bdtPriRestri != null) {
            AgencyIdList agencyIdList = agencyIdListRepository.findOne(bdtPriRestri.getAgencyIdListId());
            agencyIdListIdMap = new LinkedHashMap();
            agencyIdListIdMap.put(agencyIdList.getName(), agencyIdList);
        } else {
            agencyIdListIdMap = agencyIdListRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getName(), Function.identity()));
        }

        return agencyIdListIdMap;
    }

    private List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriList(BBIENode node) {
        BasicBusinessInformationEntity bbie = node.getBbie();
        BasicCoreComponent bcc = bccRepository.findOne(bbie.getBasedBccId());
        BasicCoreComponentProperty bccp = bccpRepository.findOne(bcc.getToBccpId());
        long bdtId = bccp.getBdtId();
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findByBdtId(bdtId);
        return bdtPriRestriList;
    }

    /*
     * for BBIE_SC Primitive Restriction
     */
    public Map<BBIERestrictionType, BBIERestrictionType> getAvailablePrimitiveRestrictions(BBIESCNode node) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = getBdtScPriRestriList(node);
        Map<BBIERestrictionType, BBIERestrictionType> availablePrimitiveRestrictions = new LinkedHashMap();

        List<BBIERestrictionType> restrictionTypes = bdtScPriRestriList.stream().map(e -> {
            if (e.getCdtScAwdPriXpsTypeMapId() > 0L) {
                return Primitive;
            } else if (e.getCodeListId() > 0L) {
                return Code;
            } else {
                return Agency;
            }
        }).collect(Collectors.toList());

        if (hasOnlyCdtScAwdPriXpsTypeMap(bdtScPriRestriList)) {
            availablePrimitiveRestrictions.put(Primitive, Primitive);
            availablePrimitiveRestrictions.put(Code, Code);
            availablePrimitiveRestrictions.put(Agency, Agency);
        } else {
            if (restrictionTypes.contains(Primitive)) {
                availablePrimitiveRestrictions.put(Primitive, Primitive);
            }
            if (restrictionTypes.contains(Code)) {
                availablePrimitiveRestrictions.put(Code, Code);
            }
            if (restrictionTypes.contains(Agency)) {
                availablePrimitiveRestrictions.put(Agency, Agency);
            }
        }

        return availablePrimitiveRestrictions;
    }

    private boolean hasOnlyCdtScAwdPriXpsTypeMap(List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList) {
        long sum = bdtScPriRestriList.stream().mapToLong(e -> e.getCodeListId() + e.getAgencyIdListId()).sum();
        return (sum == 0L);
    }

    public String getBdtScPrimitiveRestrictionName(BBIESCNode node) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri = node.getBdtScPriRestri();
        if (bdtScPriRestri == null) {
            return null;
        }

        Map<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPrimitiveRestrictions =
                getBdtScPrimitiveRestrictions(node);
        for (Map.Entry<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> e : bdtScPrimitiveRestrictions.entrySet()) {
            if (e.getValue().getCdtScAwdPriXpsTypeMapId() == bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()) {
                return e.getKey();
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void setBdtScPrimitiveRestriction(BBIESCNode node, String name) {
        Map<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtPrimitiveRestrictions =
                getBdtScPrimitiveRestrictions(node);
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                bdtPrimitiveRestrictions.get(name);
        if (bdtScPriRestri == null) {
            return;
        }
        node.setBdtScPriRestri(bdtScPriRestri);
    }

    public Map<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> getBdtScPrimitiveRestrictions(BBIESCNode node) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = getBdtScPriRestriList(node).stream()
                .filter(e -> e.getCdtScAwdPriXpsTypeMapId() > 0L)
                .collect(Collectors.toList());

        Map<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtPrimitiveRestrictions = new LinkedHashMap();
        for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction e : bdtScPriRestriList) {
            long cdtScAwdPriXpsTypeMapId = e.getCdtScAwdPriXpsTypeMapId();
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriTypeMap =
                    cdtScAwdPriXpsTypeMapRepository.findOne(cdtScAwdPriXpsTypeMapId);
            long xbtId = cdtScAwdPriTypeMap.getXbtId();
            XSDBuiltInType xbt = xbtRepository.findOne(xbtId);
            bdtPrimitiveRestrictions.put(xbt.getName(), e);
        }

        return bdtPrimitiveRestrictions;
    }

    public String getCodeListName(BBIESCNode node) {
        Map<String, CodeList> codeListMap = getCodeLists(node);
        for (CodeList codeList : codeListMap.values()) {
            if (codeList.getCodeListId() == node.getCodeListId()) {
                return codeList.getName();
            }
        }
        return null;
    }

    public Map<String, CodeList> getCodeLists(BBIESCNode node) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = getBdtScPriRestriList(node).stream()
                .filter(e -> e.getCodeListId() > 0L)
                .collect(Collectors.toList());
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri = (bdtScPriRestriList.isEmpty()) ? null : bdtScPriRestriList.get(0);

        Map<String, CodeList> codeListMap;
        if (bdtScPriRestri != null) {
            codeListMap = new LinkedHashMap();
            CodeList codeList = codeListRepository.findOne(bdtScPriRestri.getCodeListId());
            while (codeList != null) {
                codeListMap.put(codeList.getName(), codeList);
                long basedCodeListId = codeList.getBasedCodeListId();
                if (basedCodeListId > 0L) {
                    codeList = codeListRepository.findOne(basedCodeListId);
                } else {
                    codeList = null;
                }
            }
        } else {
            codeListMap = codeListRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getName(), Function.identity()));
        }

        return codeListMap;
    }

    public String getBbieAgencyIdListName(BBIESCNode node) {
        Map<String, AgencyIdList> agencyIdListMap = getAgencyIdListIds(node);
        for (AgencyIdList agencyIdList : agencyIdListMap.values()) {
            if (agencyIdList.getAgencyIdListId() == node.getAgencyIdListId()) {
                return agencyIdList.getName();
            }
        }
        return null;
    }

    public Map<String, AgencyIdList> getAgencyIdListIds(BBIESCNode node) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = getBdtScPriRestriList(node).stream()
                .filter(e -> e.getAgencyIdListId() > 0L)
                .collect(Collectors.toList());
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri = (bdtScPriRestriList.isEmpty()) ? null : bdtScPriRestriList.get(0);
        Map<String, AgencyIdList> agencyIdListIdMap;
        if (bdtScPriRestri != null) {
            AgencyIdList agencyIdList = agencyIdListRepository.findOne(bdtScPriRestri.getAgencyIdListId());
            agencyIdListIdMap = new LinkedHashMap();
            agencyIdListIdMap.put(agencyIdList.getName(), agencyIdList);
        } else {
            agencyIdListIdMap = agencyIdListRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getName(), Function.identity()));
        }

        return agencyIdListIdMap;
    }

    private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> getBdtScPriRestriList(BBIESCNode node) {
        BasicBusinessInformationEntitySupplementaryComponent bbieSc = node.getBbieSc();
        long bdtScId = bbieSc.getDtScId();
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList =
                bdtScPriRestriRepository.findByBdtScId(bdtScId);
        return bdtScPriRestriList;
    }

    public void transferOwner(TopLevelAbie topLevelAbie, User newOwner) {
        long oldOwnerId = topLevelAbie.getOwnerUserId();
        long newOwnerId = newOwner.getAppUserId();

        if (oldOwnerId == newOwnerId) {
            return;
        }

        topLevelAbie.setOwnerUserId(newOwnerId);
        topLevelAbieRepository.save(topLevelAbie);
    }
}
