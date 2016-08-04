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

import java.util.*;
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
    private BusinessObjectDocumentRepository bodRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    public class CreateBIEsResult {

        private int abieCount = 0;
        private int bbiescCount = 0;
        private int asbiepCount = 0;
        private int asbieCount = 0;
        private int bbiepCount = 0;
        private int bbieCount = 0;

        private int userId;
        private AssociationCoreComponentProperty asccp;
        private AggregateCoreComponent acc;
        private BusinessObjectDocument bod;

        private AggregateBusinessInformationEntity topLevelAbie;

        private CreateBIEsResult(int userId, AssociationCoreComponentProperty asccp,
                                 AggregateCoreComponent acc, BusinessObjectDocument bod) {
            this.userId = userId;
            this.asccp = asccp;
            this.acc = acc;
            this.bod = bod;
        }

        public void setTopLevelAbie(AggregateBusinessInformationEntity topLevelAbie) {
            this.topLevelAbie = topLevelAbie;
        }

        public AggregateCoreComponent getAcc() {
            return acc;
        }

        public BusinessObjectDocument getBod() {
            return bod;
        }

        public AggregateBusinessInformationEntity getTopLevelAbie() {
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
        int userId = userRepository.findAppUserIdByLoginId("oagis");
        int roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
        BusinessObjectDocument bod = createBOD(bizCtx);
        CreateBIEsResult createBIEsResult = new CreateBIEsResult(userId, asccp, acc, bod);

        AggregateBusinessInformationEntity topLevelAbie = createABIE(userId, acc, bod, createBIEsResult);
        updateBOD(bod, topLevelAbie);

        int abieId = topLevelAbie.getAbieId();

        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(userId, asccp, abieId, bod, createBIEsResult);
        CreateBIEContext createBIEContext = new CreateBIEContext(userId, bod, createBIEsResult);
        createBIEs(createBIEContext, roleOfAccId, topLevelAbie);
        createBIEContext.save();

        return createBIEsResult;
    }

    private BusinessObjectDocument createBOD(BusinessContext bizCtx) {
        BusinessObjectDocument bod = new BusinessObjectDocument();
        bod.setBizCtxId(bizCtx.getBizCtxId());
        bod.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
        return bodRepository.saveAndFlush(bod);
    }

    private AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc,
                                                          BusinessObjectDocument bod,
                                                          CreateBIEsResult createBIEsResult) {

        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAccId(acc.getAccId());
        abie.setDefinition(acc.getDefinition());
        abie.setCreatedBy(userId);
        abie.setLastUpdatedBy(userId);
        abie.setBodId(bod.getBodId());

        abieRepository.saveAndFlush(abie);
        createBIEsResult.abieCount++;
        createBIEsResult.setTopLevelAbie(abie);

        return abie;
    }

    private void updateBOD(BusinessObjectDocument bod, AggregateBusinessInformationEntity topLevelAbie) {
        bod.setTopLevelAbieId(topLevelAbie.getAbieId());
        bodRepository.save(bod);
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(int userId,
                                                                      AssociationCoreComponentProperty asccp,
                                                                      int abieId,
                                                                      BusinessObjectDocument bod,
                                                                      CreateBIEsResult createBIEsResult) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccpId(asccp.getAsccpId());
        asbiep.setRoleOfAbieId(abieId);
        asbiep.setCreatedBy(userId);
        asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(asccp.getDefinition());
        asbiep.setBodId(bod.getBodId());

        asbiepRepository.saveAndFlush(asbiep);
        createBIEsResult.asbiepCount++;

        return asbiep;
    }

    public void createBIEs(CreateBIEContext createBIEContext, int accId, AggregateBusinessInformationEntity abie) {
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

        private Map<Integer, AggregateCoreComponent> aggregateCoreComponentMap;
        private Map<Integer, AssociationCoreComponentProperty> associationCoreComponentPropertyMap;
        private Map<Integer, BasicCoreComponentProperty> basicCoreComponentPropertyMap;

        private Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
        private Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;

        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriDefaultMap;
        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriCodeListMap;

        private int userId;
        private BusinessObjectDocument bod;
        private CreateBIEsResult createBIEsResult;

        private List<BasicCoreComponent> basicCoreComponents;
        private List<AssociationCoreComponent> associationCoreComponents;
        private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;

        public CreateBIEContext(int userId, BusinessObjectDocument bod, CreateBIEsResult createBIEsResult) {
            abieTaskHolder = new ABIETaskHolder();
            bbieTreeTaskHolder = new BBIETreeTaskHolder();
            asbieTreeTaskHolder = new ASBIETreeTaskHolder();

            this.userId = userId;
            this.bod = bod;
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

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findAll();
            bdtPriRestriDefaultMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
            bdtPriRestriCodeListMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));

            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            bdtScPriRestriDefaultMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
            bdtScPriRestriCodeListMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
        }

        public int getUserId() {
            return userId;
        }

        public BusinessObjectDocument getBod() {
            return bod;
        }

        public AggregateCoreComponent getACC(int accId) {
            return aggregateCoreComponentMap.get(accId);
        }

        public AssociationCoreComponentProperty getASCCP(int asccpId) {
            return associationCoreComponentPropertyMap.get(asccpId);
        }

        public AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc) {
            return abieTaskHolder.createABIE(userId, acc);
        }

        public void createBBIETree(BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            bbieTreeTaskHolder.createBBIETree(this, bcc, abie, seqKey);
        }

        public void createASBIETree(AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            asbieTreeTaskHolder.createASBIETree(this, asccVO, abie, seqKey);
        }

        @Override
        public List<BasicCoreComponent> getBCCs(int fromAccId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(int accId) {
            return getBCCs(accId).stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.toList());
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(int fromAccId) {
            return associationCoreComponents.stream()
                    .filter(acc -> acc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        public BasicCoreComponentProperty getBCCP(int toBccpId) {
            return basicCoreComponentPropertyMap.get(toBccpId);
        }

        public int getDefaultBdtPriRestriId(int bdtId) {
            return bdtPriRestriDefaultMap.get(bdtId).getBdtPriRestriId();
        }

        public int getCodeListIdOfBdtPriRestriId(int bdtId) {
            BusinessDataTypePrimitiveRestriction e = bdtPriRestriCodeListMap.get(bdtId);
            return (e != null) ? e.getCodeListId() : 0;
        }

        public int getDefaultBdtScPriRestriId(int bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriDefaultMap.get(bdtScId);
            return (e != null) ? e.getBdtScPriRestriId() : 0;
        }

        public int getCodeListIdOfBdtScPriRestriId(int bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriCodeListMap.get(bdtScId);
            return (e != null) ? e.getCodeListId() : 0;
        }

        public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId) {
            return dataTypeSupplementaryComponents.stream()
                    .filter(dtSc -> dtSc.getOwnerDtId() == ownerDtId)
                    .collect(Collectors.toList());
        }

        public void save() {
            abieTaskHolder.save(bod, createBIEsResult);
            bbieTreeTaskHolder.save(bod, createBIEsResult);
            asbieTreeTaskHolder.save(bod, createBIEsResult);
        }
    }

    private class ABIETaskHolder {

        private List<AggregateBusinessInformationEntity> aggregateBusinessInformationEntitys = new ArrayList();

        public AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc) {
            AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
            String abieGuid = Utility.generateGUID();
            abie.setGuid(abieGuid);
            abie.setBasedAccId(acc.getAccId());
            abie.setDefinition(acc.getDefinition());
            abie.setCreatedBy(userId);
            abie.setLastUpdatedBy(userId);

            aggregateBusinessInformationEntitys.add(abie);

            return abie;
        }

        public void save(BusinessObjectDocument bod, CreateBIEsResult createBIEsResult) {
            aggregateBusinessInformationEntitys.stream()
                    .forEach(e -> e.setBodId(bod.getBodId()));
            abieRepository.save(aggregateBusinessInformationEntitys);
            createBIEsResult.abieCount += aggregateBusinessInformationEntitys.size();
        }

    }

    private class BBIETreeTaskHolder {
        private List<CreateBBIETreeTask> createBBIETreeTasks = new ArrayList();

        public void createBBIETree(CreateBIEContext createBIEContext, BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            createBBIETreeTasks.add(new CreateBBIETreeTask(createBIEContext, bcc, abie, seqKey));
        }

        public void save(BusinessObjectDocument bod, CreateBIEsResult createBIEsResult) {
            List<BasicBusinessInformationEntityProperty> bbiepList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbiep())
                            .collect(Collectors.toList());
            bbiepList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            bbiepRepository.save(bbiepList);
            createBIEsResult.bbiepCount += createBBIETreeTasks.size();

            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getBbie().setFromAbieId(task.getAbie().getAbieId());
                        task.getBbie().setToBbiepId(task.getBbiep().getBbiepId());
                    });

            List<BasicBusinessInformationEntity> bbieList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbie())
                            .collect(Collectors.toList());
            bbieList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            bbieRepository.save(bbieList);
            createBIEsResult.bbieCount += createBBIETreeTasks.size();

            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getBbieScList().forEach(bbieSc -> {
                            bbieSc.setBbieId(task.getBbie().getBbieId());
                        });
                    });

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new ArrayList();
            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        bbieScList.addAll(task.getBbieScList());
                    });
            bbieScList.stream().forEach(e -> e.setBodId(bod.getBodId()));
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
            int bdtId = bccp.getBdtId();
            int bdtPrimitiveRestrictionId = createBIEContext.getDefaultBdtPriRestriId(bdtId);
            int codeListId = createBIEContext.getCodeListIdOfBdtPriRestriId(bdtId);

            createBBIEP(createBIEContext.getUserId(), bccp);
            createBBIE(createBIEContext.getUserId(), bdtPrimitiveRestrictionId, codeListId);
            createBBIESC(createBIEContext, bdtId);
        }

        private void createBBIEP(int userId, BasicCoreComponentProperty bccp) {
            bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccpId(bccp.getBccpId());
            bbiep.setCreatedBy(userId);
            bbiep.setLastUpdatedBy(userId);
            bbiep.setDefinition(bccp.getDefinition());
        }

        private void createBBIE(int userId, int bdtPrimitiveRestrictionId, int codeListId) {
            bbie = new BasicBusinessInformationEntity();
            bbie.setGuid(Utility.generateGUID());
            bbie.setBasedBccId(bcc.getBccId());
            // bbie.setFromAbieId(abie);
            // bbie.setToBbiepId(bbiepId);
            bbie.setNillable(false);
            bbie.setCardinalityMax(bcc.getCardinalityMax());
            bbie.setCardinalityMin(bcc.getCardinalityMin());
            bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
//            if (codeListId > 0) {
//                bbie.setCodeListId(codeListId);
//            }
            bbie.setCreatedBy(userId);
            bbie.setLastUpdatedBy(userId);
            bbie.setSeqKey(seqKey);
        }

        private void createBBIESC(CreateBIEContext createBIEContext, int bdtId) {
            bbieScList = createBIEContext.findByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getMaxCardinality() != 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        // bbieSc.setBbieId(bbieId);
                        int bdtScId = dtSc.getDtScId();
                        bbieSc.setDtScId(bdtScId);
                        int bdtScPriRestriId = createBIEContext.getDefaultBdtScPriRestriId(bdtScId);
                        if (bdtScPriRestriId > 0) {
                            bbieSc.setDtScPriRestriId(bdtScPriRestriId);
                        }
                        int codeListId = createBIEContext.getCodeListIdOfBdtScPriRestriId(bdtScId);
//                        if (codeListId > 0) {
//                            bbieSc.setCodeListId(codeListId);
//                        }
                        bbieSc.setMaxCardinality(dtSc.getMaxCardinality());
                        bbieSc.setMinCardinality(dtSc.getMinCardinality());
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

        public CreateASBIETreeTask(int userId, AssociationCoreComponentProperty asccp, AggregateBusinessInformationEntity roleOfAbie,
                                   AssociationCoreComponent ascc, AggregateBusinessInformationEntity fromAbie, int seqKey) {
            this.asccp = asccp;
            this.roleOfAbie = roleOfAbie;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;

            createASBIEP(userId);
            createASBIE(userId);
        }

        public void createASBIEP(int userId) {
            asbiep = new AssociationBusinessInformationEntityProperty();
            asbiep.setGuid(Utility.generateGUID());
            asbiep.setBasedAsccpId(asccp.getAsccpId());
            // asbiep.setRoleOfAbieId(roleOfAbieId);
            asbiep.setCreatedBy(userId);
            asbiep.setLastUpdatedBy(userId);
            asbiep.setDefinition(asccp.getDefinition());
        }

        public void createASBIE(int userId) {
            asbie = new AssociationBusinessInformationEntity();
            asbie.setGuid(Utility.generateGUID());
            // asbie.setFromAbieId(fromAbieId);
            // asbie.setToAsbiepId(asbiep);
            asbie.setBasedAscc(ascc.getAsccId());
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

        public void save(BusinessObjectDocument bod, CreateBIEsResult createBIEsResult) {
            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbiep().setRoleOfAbieId(task.getRoleOfAbie().getAbieId());
                    });
            List<AssociationBusinessInformationEntityProperty> asbiepList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbiep())
                            .collect(Collectors.toList());
            asbiepList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            asbiepRepository.save(asbiepList);
            createBIEsResult.asbiepCount += createASBIETreeTasks.size();

            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbie().setFromAbieId(task.getFromAbie().getAbieId());
                        task.getAsbie().setToAsbiepId(task.getAsbiep().getAsbiepId());
                    });

            List<AssociationBusinessInformationEntity> asbieList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbie())
                            .collect(Collectors.toList());
            asbieList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            asbieRepository.save(asbieList);
            createBIEsResult.asbieCount += createASBIETreeTasks.size();
        }
    }

}
