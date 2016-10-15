package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.model.bod.visitor.NodeSortVisitor;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.beans.bod.CreateProfileBODBean;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreateBIETreeNode extends UIHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateBIETreeNode.class);

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;

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
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private int batchSize = 25;

    private class SubmitNodeVisitor implements NodeVisitor {

        private User user;
        private CreateProfileBODBean.ProgressListener progressListener;

        private TopLevelAbie topLevelAbie;
        private List<AggregateBusinessInformationEntity> abieList = new ArrayList();
        private List<AssociationBusinessInformationEntity> asbieList = new ArrayList();
        private List<AssociationBusinessInformationEntityProperty> asbiepList = new ArrayList();
        private List<BasicBusinessInformationEntity> bbieList = new ArrayList();
        private List<BasicBusinessInformationEntityProperty> bbiepList = new ArrayList();
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList = new ArrayList();

        public SubmitNodeVisitor(User user) {
            this.user = user;
        }

        public void setProgressListener(CreateProfileBODBean.ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            topLevelAbie = new TopLevelAbie();
            topLevelAbie.setAbie(topLevelNode.getAbie());
            asbiepList.add(topLevelNode.getAsbiep());
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            abieList.add(asbieNode.getAbie());
            asbieList.add(asbieNode.getAsbie());
            asbiepList.add(asbieNode.getAsbiep());
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            bbieList.add(bbieNode.getBbie());
            bbiepList.add(bbieNode.getBbiep());
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbiescNode) {
            bbiescList.add(bbiescNode.getBbiesc());
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            AggregateBusinessInformationEntity tAbie = topLevelAbie.getAbie();
            tAbie.setCreatedBy(user.getAppUserId());
            tAbie.setLastUpdatedBy(user.getAppUserId());
            tAbie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
            tAbie.setOwnerTopLevelAbie(topLevelAbie);
            tAbie.addPersistEventListener(progressListener);

            abieList.stream().forEach(abie -> {
                abie.setCreatedBy(user.getAppUserId());
                abie.setLastUpdatedBy(user.getAppUserId());
                abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
                abie.setOwnerTopLevelAbie(topLevelAbie);
                abie.addPersistEventListener(progressListener);
            });
            asbieList.stream().forEach(asbie -> {
                asbie.setCreatedBy(user.getAppUserId());
                asbie.setLastUpdatedBy(user.getAppUserId());
                asbie.setOwnerTopLevelAbie(topLevelAbie);
                asbie.addPersistEventListener(progressListener);
            });
            asbiepList.stream().forEach(asbiep -> {
                asbiep.setCreatedBy(user.getAppUserId());
                asbiep.setLastUpdatedBy(user.getAppUserId());
                asbiep.setOwnerTopLevelAbie(topLevelAbie);
                asbiep.addPersistEventListener(progressListener);
            });
            bbieList.stream().forEach(bbie -> {
                bbie.setCreatedBy(user.getAppUserId());
                bbie.setLastUpdatedBy(user.getAppUserId());
                bbie.setOwnerTopLevelAbie(topLevelAbie);
                bbie.addPersistEventListener(progressListener);
            });
            bbiepList.stream().forEach(bbiep -> {
                bbiep.setCreatedBy(user.getAppUserId());
                bbiep.setLastUpdatedBy(user.getAppUserId());
                bbiep.setOwnerTopLevelAbie(topLevelAbie);
                bbiep.addPersistEventListener(progressListener);
            });
            bbiescList.stream().forEach(bbiesc -> {
                bbiesc.setOwnerTopLevelAbie(topLevelAbie);
                bbiesc.addPersistEventListener(progressListener);
            });

            if (progressListener != null) {
                int maxCount = abieList.size() + asbieList.size() + asbiepList.size() + bbieList.size() + bbiepList.size() + bbiescList.size();
                progressListener.setMaxCount(maxCount);
            }
        }

        private void save() {
            saveTopLevelAbie();
            saveAbieList();
            saveBbiepList();
            saveBbieList();
            saveBbieScList();
            saveAsbiepList();
            saveAsbieList();

//            EntityManager entityManager = null;
//            EntityTransaction txn = null;
//            try {
//                entityManager = entityManagerFactory.createEntityManager();
//                txn = entityManager.getTransaction();
//                txn.begin();
//
//                saveTopLevelAbie(entityManager);
//                saveBatch(entityManager, abieList);
//                saveBatch(entityManager, bbiepList);
//                saveBatch(entityManager, bbieList);
//                saveBatch(entityManager, bbiescList);
//                saveBatch(entityManager, asbiepList);
//                saveBatch(entityManager, asbieList);
//
//                txn.commit();
//            } catch (RuntimeException e) {
//                if (txn != null && txn.isActive()) txn.rollback();
//                throw e;
//            } finally {
//                entityManager.close();
//            }
        }

        private void saveTopLevelAbie() {
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
            topLevelAbie.setAbie(null);

            topLevelAbieRepository.saveAndFlush(topLevelAbie);
            abie.setOwnerTopLevelAbie(topLevelAbie);

            abieRepository.saveAndFlush(abie);

            topLevelAbie.setAbie(abie);
            topLevelAbieRepository.save(topLevelAbie);
        }

        private void saveTopLevelAbie(EntityManager entityManager) {
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
            topLevelAbie.setAbie(null);

            entityManager.persist(topLevelAbie);
            entityManager.flush();
            abie.setOwnerTopLevelAbie(topLevelAbie);

            entityManager.persist(abie);
            entityManager.flush();

            topLevelAbie.setAbie(abie);
            entityManager.persist(topLevelAbie);
            entityManager.flush();
        }

        private void saveBatch(EntityManager entityManager, List list) {
            for (int i = 0, len = list.size(); i < len; ++i) {
                entityManager.persist(list.get(i));

                if (i % batchSize == 0) {
                    // flush a batch of inserts and release memory
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }

        private void saveAbieList() {
            abieRepository.save(abieList);
        }

        private void saveBbiepList() {
            bbiepRepository.save(bbiepList);
        }

        private void saveBbieList() {
            bbieRepository.save(bbieList);
        }

        private void saveBbieScList() {
            bbiescRepository.save(bbiescList);
        }

        private void saveAsbiepList() {
            asbiepRepository.save(asbiepList);
        }

        private void saveAsbieList() {
            asbieRepository.save(asbieList);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void submit(TopLevelNode node, CreateProfileBODBean.ProgressListener progressListener) {
        SubmitNodeVisitor submitNodeVisitor = new SubmitNodeVisitor(loadAuthentication());
        submitNodeVisitor.setProgressListener(progressListener);
        node.accept(submitNodeVisitor);
    }

    @Transactional(readOnly = true)
    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        Node node = createNode(asccp, bizCtx);

        long s = System.currentTimeMillis();
        node.accept(new NodeSortVisitor());
        logger.info("Node sorted - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        TreeNodeVisitor treeNodeVisitor = new TreeNodeVisitor();
        node.accept(treeNodeVisitor);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNodeVisitor.getRoot();
    }

    private Node createNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        long s = System.currentTimeMillis();
        DataContainer dataContainer = new DataContainer(bizCtx);

        logger.info("DataContainer instantiated - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = dataContainer.getACC(roleOfAccId);
        AggregateBusinessInformationEntity abie = createABIE(acc, bizCtx);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(asccp, abie);

        TopLevelNode topLevelNode = new TopLevelNode(asbiep, asccp, abie, bizCtx);
        appendChildren(dataContainer, acc, abie, topLevelNode);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return topLevelNode;
    }

    private AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc, BusinessContext bizCtx) {

        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAcc(acc);
        abie.setBizCtx(bizCtx);
        abie.setDefinition(acc.getDefinition());

        return abie;
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(AssociationCoreComponentProperty asccp,
                                                                      AggregateBusinessInformationEntity abie) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccp(asccp);
        asbiep.setRoleOfAbie(abie);
        asbiep.setDefinition(asccp.getDefinition());

        return asbiep;
    }

    private void appendChildren(DataContainer dataContainer,
                                AggregateCoreComponent acc, AggregateBusinessInformationEntity abie, Node parent) {
        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        accList.add(acc);
        while (acc.getBasedAccId() > 0) {
            acc = dataContainer.getACC(acc.getBasedAccId());
            accList.add(acc);
        }

        while (!accList.isEmpty()) {
            acc = accList.pollFirst();
            int skb = 0;
            for (AggregateCoreComponent cnt_acc : accList) {
                skb += queryNestedChildAssoc_wo_attribute(dataContainer, cnt_acc).size(); //here
            }

            List<CoreComponent> childAssoc = queryNestedChildAssoc(dataContainer, acc);
            int attr_cnt = childAssoc.size() - queryNestedChildAssoc_wo_attribute(dataContainer, acc).size();
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() == 0) {
                        new BBIENodeBuilder(dataContainer, parent, bcc, abie, skb).build();
                    }
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0) {
                        new BBIENodeBuilder(dataContainer, parent, bcc, abie, skb + i - attr_cnt).build();
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    new ASBIENodeBuilder(dataContainer, parent, ascc, abie, skb + i - attr_cnt).build();
                }
            }
        }
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(DataContainer dataContainer,
                                                                   AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(
                aggregateCoreComponent, dataContainer);
        return getAssocList(dataContainer, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc(DataContainer dataContainer,
                                                      AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, dataContainer);
        return getAssocList(dataContainer, assoc);
    }

    private List<CoreComponent> getAssocList(DataContainer dataContainer, List<CoreComponent> list) {
        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && dataContainer.groupcheck((AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty = dataContainer.getASCCP(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent = dataContainer.getACC(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(dataContainer, aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private List<CoreComponent> handleNestedGroup(DataContainer dataContainer,
                                                  AggregateCoreComponent acc,
                                                  List<CoreComponent> coreComponents, int gPosition) {

        List<CoreComponent> bList = queryChildAssoc(dataContainer, acc);
        if (!bList.isEmpty()) {
            coreComponents.addAll(gPosition, bList);
            coreComponents.remove(gPosition + bList.size());
        }

        for (int i = 0; i < coreComponents.size(); i++) {
            CoreComponent coreComponent = coreComponents.get(i);
            if (coreComponent instanceof AssociationCoreComponent &&
                    dataContainer.groupcheck((AssociationCoreComponent) coreComponent)) {

                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty asccp = dataContainer.getASCCP(ascc.getToAsccpId());
                coreComponents = handleNestedGroup(dataContainer,
                        dataContainer.getACC(asccp.getRoleOfAccId()), coreComponents, i);
            }
        }

        return coreComponents;
    }

    private List<CoreComponent> queryChildAssoc(DataContainer dataContainer, AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, dataContainer);
        return assoc;
    }

    private class DataContainer implements CoreComponentProvider {
        private BusinessContext businessContext;
        private TopLevelNode topLevelNode;

        private Map<Long, AggregateCoreComponent> accMap;
        private Map<Long, AssociationCoreComponentProperty> asccpMap;
        private Map<Long, Boolean> groupcheckMap;

        private Map<Long, List<BasicCoreComponent>> fromAccIdToBccMap;
        private Map<Long, List<BasicCoreComponent>> fromAccIdToBccWithoutAttributesMap;
        private Map<Long, List<AssociationCoreComponent>> fromAccIdToAsccMap;
        private Map<Long, BasicCoreComponentProperty> bccpMap;

        private Map<Long, DataType> dtMap;
        private Map<Long, DataTypeSupplementaryComponent> dtScMap;
        private Map<Long, List<DataTypeSupplementaryComponent>> ownerDtScIdToDtScMap;

        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;
        private Map<Long, List<BusinessDataTypePrimitiveRestriction>> bdtIdTobdtPriRestriMap;

        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriMap;
        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriDefaultMap;
        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriCodeListMap;

        private List<BasicCoreComponent> basicCoreComponents;
        private List<AssociationCoreComponent> associationCoreComponents;
        private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;
        private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList;
        private List<DataType> dataTypes;
        private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;

        public DataContainer(BusinessContext businessContext) {
            this.businessContext = businessContext;

            basicCoreComponents = bccRepository.findAll();
            fromAccIdToBccMap = basicCoreComponents.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));
            fromAccIdToBccWithoutAttributesMap = basicCoreComponents.stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));

            associationCoreComponents = asccRepository.findAll();
            fromAccIdToAsccMap = associationCoreComponents.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));

            bdtPriRestriList = bdtPriRestriRepository.findAll();
            bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            dataTypes = dataTypeRepository.findAll();
            dataTypeSupplementaryComponents = dtScRepository.findAll();

            accMap = accRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));
            asccpMap = asccpRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));
            bccpMap = bccpRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));

            bdtPriRestriMap = bdtPriRestriList.stream()
                    .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));
            bdtPriRestriDefaultMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
            bdtPriRestriCodeListMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
            bdtIdTobdtPriRestriMap = bdtPriRestriList.stream()
                    .collect(Collectors.groupingBy(e -> e.getBdtId()));

            bdtScPriRestriMap = bdtScPriRestriList.stream()
                    .collect(Collectors.toMap(e -> e.getBdtScPriRestriId(), Function.identity()));
            bdtScPriRestriDefaultMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
            bdtScPriRestriCodeListMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));

            dtMap = dataTypes.stream()
                    .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));
            dtScMap = dataTypeSupplementaryComponents.stream()
                    .collect(Collectors.toMap(e -> e.getDtScId(), Function.identity()));
            ownerDtScIdToDtScMap = dataTypeSupplementaryComponents.stream()
                    .collect(Collectors.groupingBy(e -> e.getOwnerDtId()));

            groupcheckMap = asccpMap.values().stream()
                    .collect(Collectors.toMap(e -> e.getAsccpId(), e -> {
                        AggregateCoreComponent acc = getACC(e.getRoleOfAccId());
                        return (acc.getOagisComponentType() == 3) ? true : false;
                    }));
        }

        public BusinessContext getBusinessContext() {
            return businessContext;
        }

        public TopLevelNode getTopLevelNode() {
            return topLevelNode;
        }

        public void setTopLevelNode(TopLevelNode topLevelNode) {
            this.topLevelNode = topLevelNode;
        }

        private AggregateCoreComponent getACC(long accId) {
            return accMap.get(accId);
        }

        private AssociationCoreComponentProperty getASCCP(long asccpId) {
            return asccpMap.get(asccpId);
        }

        public BasicCoreComponentProperty getBCCP(long bccpId) {
            return bccpMap.get(bccpId);
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

        public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId) {
            List<DataTypeSupplementaryComponent> dtScList = ownerDtScIdToDtScMap.get(ownerDtId);
            return (dtScList != null) ? dtScList : Collections.emptyList();
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

        public DataType getDt(long dtId) {
            return dtMap.get(dtId);
        }

        public DataTypeSupplementaryComponent getDtSc(long dtScId) {
            return dtScMap.get(dtScId);
        }

        public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriByBdtId(long bdtId) {
            return bdtIdTobdtPriRestriMap.get(bdtId);
        }

        public boolean groupcheck(AssociationCoreComponent ascc) {
            return groupcheckMap.get(ascc.getToAsccpId());
        }

        @Override
        public List<BasicCoreComponent> getBCCs(long accId) {
            List<BasicCoreComponent> bccList = fromAccIdToBccMap.get(accId);
            return (bccList != null) ? bccList : Collections.emptyList();
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
            List<BasicCoreComponent> bccList = fromAccIdToBccWithoutAttributesMap.get(accId);
            return (bccList != null) ? bccList : Collections.emptyList();
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(long accId) {
            List<AssociationCoreComponent> asccList = fromAccIdToAsccMap.get(accId);
            return (asccList != null) ? asccList : Collections.emptyList();
        }
    }

    private class BBIENodeBuilder {
        private DataContainer dataContainer;
        private Node parent;
        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public BBIENodeBuilder(DataContainer dataContainer, Node parent,
                               BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.dataContainer = dataContainer;
            this.parent = parent;
            this.bcc = bcc;
            this.abie = abie;
            this.seqKey = seqKey;
        }

        private void createBBIEP(BasicCoreComponentProperty bccp) {
            bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccp(bccp);
            bbiep.setDefinition(bccp.getDefinition());
        }

        private void createBBIE(BusinessDataTypePrimitiveRestriction bdtPriRestri, long codeListId) {
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
            bbie.setSeqKey(seqKey);
        }

        private void createBBIESC(long bdtId) {
            bbieScList = dataContainer.findDtScByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getCardinalityMax() != 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        long bdtScId = dtSc.getDtScId();
                        bbieSc.setBbie(bbie);
                        bbieSc.setDtSc(dtSc);
                        bbieSc.setGuid(Utility.generateGUID());
                        long bdtScPriRestriId = dataContainer.getDefaultBdtScPriRestriId(bdtScId);
                        if (bdtScPriRestriId > 0L) {
                            bbieSc.setDtScPriRestri(dataContainer.getBdtScPriRestri(bdtScPriRestriId));
                        }
                        long codeListId = dataContainer.getCodeListIdOfBdtScPriRestriId(bdtScId);
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

        private void appendBBIESC(BasicBusinessInformationEntity bbie, BBIENode parent) {
            for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : bbieScList) {
                DataTypeSupplementaryComponent dtsc = bbiesc.getDtSc();
                new BBIESCNode(parent, bbiesc, dtsc);
            }
        }

        public BBIENode build() {
            BasicCoreComponentProperty bccp = dataContainer.getBCCP(bcc.getToBccpId());
            long bdtId = bccp.getBdtId();
            long bdtPrimitiveRestrictionId = dataContainer.getDefaultBdtPriRestriId(bdtId);
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    dataContainer.getBdtPriRestri(bdtPrimitiveRestrictionId);
            long codeListId = dataContainer.getCodeListIdOfBdtPriRestriId(bdtId);
            DataType bdt = dataContainer.getDt(bdtId);

            createBBIEP(bccp);
            createBBIE(bdtPriRestri, codeListId);
            createBBIESC(bdtId);

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = dataContainer.getBdtPriRestriByBdtId(bdtId);

            BBIENode bbieNode = new BBIENode(seqKey, parent, bbie, bbiep, bccp, bdt, bdtPriRestriList);
            appendBBIESC(bbie, bbieNode);
            return bbieNode;
        }
    }

    private class ASBIENodeBuilder {
        private DataContainer dataContainer;
        private Node parent;

        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public ASBIENodeBuilder(DataContainer dataContainer, Node parent,
                                AssociationCoreComponent ascc,
                                AggregateBusinessInformationEntity fromAbie,
                                int seqKey) {
            this.dataContainer = dataContainer;
            this.parent = parent;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;

            this.asccp = dataContainer.getASCCP(ascc.getToAsccpId());
        }

        public void createASBIEP() {
            asbiep = new AssociationBusinessInformationEntityProperty();
            asbiep.setGuid(Utility.generateGUID());
            asbiep.setBasedAsccp(asccp);
            asbiep.setRoleOfAbie(roleOfAbie);
            asbiep.setDefinition(asccp.getDefinition());
        }

        public void createASBIE() {
            asbie = new AssociationBusinessInformationEntity();
            asbie.setGuid(Utility.generateGUID());
            asbie.setFromAbie(fromAbie);
            asbie.setToAsbiep(asbiep);
            asbie.setBasedAscc(ascc);
            asbie.setCardinalityMax(ascc.getCardinalityMax());
            asbie.setCardinalityMin(ascc.getCardinalityMin());
            asbie.setDefinition(ascc.getDefinition());
            asbie.setSeqKey(seqKey);
        }

        public ASBIENode build() {
            AggregateCoreComponent acc = dataContainer.getACC(asccp.getRoleOfAccId());
            this.roleOfAbie = createABIE(acc, dataContainer.getBusinessContext());

            createASBIEP();
            createASBIE();

            ASBIENode asbieNode = new ASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
            appendChildren(dataContainer, acc, roleOfAbie, asbieNode);
            return asbieNode;
        }
    }

}
