package org.oagi.srt.service;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BusinessInformationEntityService {

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Value("${spring.datasource.platform}")
    private String platform;

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
        abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
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

        while (!accList.isEmpty()) {
            aggregateCoreComponent = accList.pollFirst();
            int skb = 0;
            for (AggregateCoreComponent cnt_acc : accList) {
                skb += queryNestedChildAssoc_wo_attribute(createBIEContext, cnt_acc).size(); //here
            }

            List<CoreComponent> childAssoc = queryNestedChildAssoc(createBIEContext, aggregateCoreComponent);
            int attr_cnt = childAssoc.size() - queryNestedChildAssoc_wo_attribute(createBIEContext, aggregateCoreComponent).size();
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() == 0) {
                        createBIEContext.createBBIETree(bcc, abie, skb);
                    }
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0) {
                        createBIEContext.createBBIETree(bcc, abie, skb + i - attr_cnt);
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    createBIEContext.createASBIETree(ascc, abie, skb + i - attr_cnt);
                }
            }
        }
    }

    private List<CoreComponent> queryNestedChildAssoc(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> getAssocList(CreateBIEContext createBIEContext, List<CoreComponent> list) {
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
        boolean check = false;
        AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
        AggregateCoreComponent acc = createBIEContext.getACC(asccp.getRoleOfAccId());
        if (acc.getOagisComponentType() == 3) {
            check = true;
        }
        return check;
    }

    private List<CoreComponent> handleNestedGroup(CreateBIEContext createBIEContext,
                                                  AggregateCoreComponent acc,
                                                  List<CoreComponent> coreComponents, int gPosition) {

        List<CoreComponent> bList = queryChildAssoc(createBIEContext, acc);
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

    private List<CoreComponent> queryChildAssoc(CreateBIEContext createBIEContext,
                                                AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, createBIEContext);
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
                    accRepository.findAll().stream()
                            .filter(acc -> acc.getRevisionNum() == 0)
                            .collect(Collectors.toMap(acc -> acc.getAccId(), Function.identity()));
            associationCoreComponentPropertyMap =
                    asccpRepository.findAll().stream()
                            .filter(asccp -> asccp.getRevisionNum() == 0)
                            .collect(Collectors.toMap(asccp -> asccp.getAsccpId(), Function.identity()));
            basicCoreComponents = bccRepository.findAll();
            associationCoreComponents = asccRepository.findAll();
            dataTypeSupplementaryComponents = dtScRepository.findAll();

            basicCoreComponentPropertyMap = bccpRepository.findAll().stream()
                    .filter(bccp -> bccp.getRevisionNum() == 0)
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
            abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

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
    public AggregateCoreComponent createNewUserExtensionGroupACC(
            AggregateCoreComponent eAcc, User currentLoginUser, boolean isGlobally) {
        AggregateCoreComponent ueAcc = createACCForExtension(eAcc, currentLoginUser);
        createACCHistoryForExtension(ueAcc);

        AssociationCoreComponentProperty ueAsccp = createASCCPForExtension(eAcc, currentLoginUser, ueAcc);
        createASCCPHistoryForExtension(ueAsccp);

        AssociationCoreComponent ueAscc = createASCCForExtension(eAcc, currentLoginUser, ueAcc, ueAsccp);
        createASCCPHistoryForExtension(ueAscc);

        return ueAcc;
    }

    private AggregateCoreComponent createACCForExtension(AggregateCoreComponent eAcc, User currentLoginUser) {
        long userId = currentLoginUser.getAppUserId();
        AggregateCoreComponent ueAcc = new AggregateCoreComponent();
        ueAcc.setGuid(Utility.generateGUID());
        ueAcc.setObjectClassTerm(Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm()));
        ueAcc.setDen((ueAcc.getObjectClassTerm() + ". Details"));
        ueAcc.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAcc.setOagisComponentType(4);
        ueAcc.setCreatedBy(userId);
        ueAcc.setLastUpdatedBy(userId);
        ueAcc.setOwnerUserId(userId);
        ueAcc.setState(1);
        ueAcc.setRevisionNum(0);
        ueAcc.setRevisionTrackingNum(0);
        ueAcc.setNamespaceId(namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10"));
        return accRepository.saveAndFlush(ueAcc);
    }

    private void createACCHistoryForExtension(AggregateCoreComponent ueAcc) {
        AggregateCoreComponent accHistory = new AggregateCoreComponent();
        accHistory.setGuid(Utility.generateGUID());
        accHistory.setObjectClassTerm(ueAcc.getObjectClassTerm());
        accHistory.setDen(ueAcc.getDen());
        accHistory.setDefinition(ueAcc.getDefinition());
        accHistory.setOagisComponentType(ueAcc.getOagisComponentType());
        accHistory.setCreatedBy(ueAcc.getCreatedBy());
        accHistory.setLastUpdatedBy(ueAcc.getLastUpdatedBy());
        accHistory.setOwnerUserId(ueAcc.getOwnerUserId());
        accHistory.setState(ueAcc.getState());
        accHistory.setRevisionNum(1);
        accHistory.setRevisionTrackingNum(1);
        accHistory.setRevisionAction(1);
        accHistory.setCurrentAccId(ueAcc.getAccId());
        accHistory.setNamespaceId(ueAcc.getNamespaceId());
        accRepository.saveAndFlush(accHistory);
    }

    private AssociationCoreComponentProperty createASCCPForExtension(AggregateCoreComponent eAcc,
                                                                     User currentLoginUser,
                                                                     AggregateCoreComponent ueAcc) {
        long userId = currentLoginUser.getAppUserId();
        AssociationCoreComponentProperty ueAsccp = new AssociationCoreComponentProperty();
        ueAsccp.setGuid(Utility.generateGUID());
        ueAsccp.setPropertyTerm(ueAcc.getObjectClassTerm());
        ueAsccp.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAsccp.setRoleOfAccId(ueAcc.getAccId());
        ueAsccp.setDen(ueAsccp.getPropertyTerm() + ". " + ueAcc.getObjectClassTerm());
        ueAsccp.setCreatedBy(userId);
        ueAsccp.setLastUpdatedBy(userId);
        ueAsccp.setOwnerUserId(userId);
        ueAsccp.setState(4);
        ueAsccp.setReusableIndicator(false);
        ueAsccp.setRevisionNum(0);
        ueAsccp.setRevisionTrackingNum(0);
        ueAsccp.setNamespaceId(ueAcc.getNamespaceId());
        return asccpRepository.saveAndFlush(ueAsccp);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponentProperty ueAsccp) {
        AssociationCoreComponentProperty asccpHistory = new AssociationCoreComponentProperty();
        asccpHistory.setGuid(Utility.generateGUID());
        asccpHistory.setPropertyTerm(ueAsccp.getPropertyTerm());
        asccpHistory.setDefinition(ueAsccp.getDefinition());
        asccpHistory.setRoleOfAccId(ueAsccp.getRoleOfAccId());
        asccpHistory.setDen(ueAsccp.getDen());
        asccpHistory.setCreatedBy(ueAsccp.getCreatedBy());
        asccpHistory.setLastUpdatedBy(ueAsccp.getLastUpdatedBy());
        asccpHistory.setOwnerUserId(ueAsccp.getOwnerUserId());
        asccpHistory.setState(ueAsccp.getState());
        asccpHistory.setReusableIndicator(ueAsccp.isReusableIndicator());
        asccpHistory.setRevisionNum(1);
        asccpHistory.setRevisionTrackingNum(1);
        asccpHistory.setRevisionAction(1);
        asccpHistory.setCurrentAsccpId(ueAsccp.getAsccpId());
        asccpHistory.setNamespaceId(ueAsccp.getNamespaceId());
        asccpRepository.saveAndFlush(asccpHistory);
    }

    private AssociationCoreComponent createASCCForExtension(AggregateCoreComponent eAcc,
                                                            User currentLoginUser,
                                                            AggregateCoreComponent ueAcc,
                                                            AssociationCoreComponentProperty ueAsccp) {
        long userId = currentLoginUser.getAppUserId();
        AssociationCoreComponent ueAscc = new AssociationCoreComponent();
        ueAscc.setGuid(Utility.generateGUID());
        ueAscc.setCardinalityMin(1);
        ueAscc.setCardinalityMax(1);
        ueAscc.setSeqKey(1);
        ueAscc.setFromAccId(eAcc.getAccId());
        ueAscc.setToAsccpId(ueAsccp.getAsccpId());
        ueAscc.setDen(eAcc.getObjectClassTerm() + ". " + ueAsccp.getDen());
        ueAscc.setDefinition("System created association to the system created user extension group component - " + ueAcc.getObjectClassTerm() + ".");
        ueAscc.setCreatedBy(userId);
        ueAscc.setLastUpdatedBy(userId);
        ueAscc.setOwnerUserId(userId);
        ueAscc.setState(4);
        ueAscc.setRevisionNum(0);
        ueAscc.setRevisionTrackingNum(0);
        return asccRepository.saveAndFlush(ueAscc);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponent ueAscc) {
        AssociationCoreComponent asccHistory = new AssociationCoreComponent();
        asccHistory.setGuid(Utility.generateGUID());
        asccHistory.setCardinalityMin(ueAscc.getCardinalityMin());
        asccHistory.setCardinalityMax(ueAscc.getCardinalityMax());
        asccHistory.setSeqKey(ueAscc.getSeqKey());
        asccHistory.setFromAccId(ueAscc.getFromAccId());
        asccHistory.setToAsccpId(ueAscc.getToAsccpId());
        asccHistory.setDen(ueAscc.getDen());
        asccHistory.setDefinition(ueAscc.getDefinition());
        asccHistory.setCreatedBy(ueAscc.getCreatedBy());
        asccHistory.setLastUpdatedBy(ueAscc.getLastUpdatedBy());
        asccHistory.setOwnerUserId(ueAscc.getOwnerUserId());
        asccHistory.setState(ueAscc.getState());
        asccHistory.setRevisionNum(1);
        asccHistory.setRevisionTrackingNum(1);
        asccHistory.setRevisionAction(1);
        asccRepository.saveAndFlush(asccHistory);
    }

}
