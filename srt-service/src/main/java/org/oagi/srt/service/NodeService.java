package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.model.bie.TopLevelNode;
import org.oagi.srt.model.bie.impl.*;
import org.oagi.srt.model.bie.visitor.BIENodeSortVisitor;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.model.cc.impl.*;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;

@Service
@Transactional(readOnly = true)
public class NodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

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
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

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
    private TopLevelAbieRepository topLevelAbieRepository;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

    public BIENode createBIENode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        long s = System.currentTimeMillis();
        DataContainerForProfileBODBuilder dataContainer = new DataContainerForProfileBODBuilder(bizCtx);

        logger.info("DataContainerForProfileBODBuilder instantiated - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = dataContainer.getACC(roleOfAccId);
        AggregateBusinessInformationEntity abie = createABIE(acc, bizCtx);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(asccp, abie);

        BaseTopLevelNode topLevelNode = new BaseTopLevelNode(asbiep, asccp, abie, bizCtx);
        appendChildren(dataContainer, acc, abie, topLevelNode);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        s = System.currentTimeMillis();
        topLevelNode.accept(new BIENodeSortVisitor());
        logger.info("Node sorted - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return topLevelNode;
    }

    private AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc, BusinessContext bizCtx) {
        if (acc == null) {
            throw new IllegalArgumentException("'acc' argument must not be null.");
        }
        if (bizCtx == null) {
            throw new IllegalArgumentException("'bizCtx' argument must not be null.");
        }

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
        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }
        if (abie == null) {
            throw new IllegalArgumentException("'abie' argument must not be null.");
        }

        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccp(asccp);
        asbiep.setRoleOfAbie(abie);
        asbiep.setDefinition(asccp.getDefinition());

        return asbiep;
    }

    private void appendChildren(DataContainerForProfileBODBuilder dataContainer,
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

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(
            DataContainerForProfileBODBuilder dataContainer,
            AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(
                aggregateCoreComponent, dataContainer);
        return getAssocList(dataContainer, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc(
            DataContainerForProfileBODBuilder dataContainer,
            AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, dataContainer);
        return getAssocList(dataContainer, assoc);
    }

    private List<CoreComponent> getAssocList(
            DataContainerForProfileBODBuilder dataContainer, List<CoreComponent> list) {
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

    private List<CoreComponent> handleNestedGroup(DataContainerForProfileBODBuilder dataContainer,
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

    private List<CoreComponent> queryChildAssoc(DataContainerForProfileBODBuilder dataContainer,
                                                AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, dataContainer);
        return assoc;
    }

    private class DataContainerForProfileBODBuilder implements CoreComponentProvider {
        private BusinessContext businessContext;
        private BaseTopLevelNode topLevelNode;

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

        public DataContainerForProfileBODBuilder(BusinessContext businessContext) {
            this.businessContext = businessContext;

            basicCoreComponents = bccRepository.findAllWithRevisionNum(0);
            fromAccIdToBccMap = basicCoreComponents.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));
            fromAccIdToBccWithoutAttributesMap = basicCoreComponents.stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));

            associationCoreComponents = asccRepository.findAllWithRevisionNum(0);
            fromAccIdToAsccMap = associationCoreComponents.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));

            bdtPriRestriList = bdtPriRestriRepository.findAll();
            bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            dataTypes = dataTypeRepository.findAll();
            dataTypeSupplementaryComponents = dtScRepository.findAll();

            accMap = accRepository.findAllWithRevisionNum(0).stream()
                    .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));
            asccpMap = asccpRepository.findAllWithRevisionNum(0).stream()
                    .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));
            bccpMap = bccpRepository.findAllWithRevisionNum(0).stream()
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
                        return (acc.getOagisComponentType() == SemanticGroup) ? true : false;
                    }));
        }

        public BusinessContext getBusinessContext() {
            return businessContext;
        }

        public BaseTopLevelNode getTopLevelNode() {
            return topLevelNode;
        }

        public void setTopLevelNode(BaseTopLevelNode topLevelNode) {
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
        private DataContainerForProfileBODBuilder dataContainer;
        private Node parent;
        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public BBIENodeBuilder(DataContainerForProfileBODBuilder dataContainer, Node parent,
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

        private void appendBBIESC(BasicBusinessInformationEntity bbie, BaseBBIENode parent) {
            for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : bbieScList) {
                long dtScId = bbiesc.getDtScId();
                DataTypeSupplementaryComponent dtsc = dataContainer.getDtSc(dtScId);
                if (dtsc == null) {
                    throw new IllegalArgumentException("Can't find 'dtSc'");
                }

                long bdtScPriRestriId = dataContainer.getDefaultBdtScPriRestriId(dtScId);
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri
                        = dataContainer.getBdtScPriRestri(bdtScPriRestriId);

                new BaseBBIESCNode(parent, bbiesc, bdtScPriRestri, dtsc);
            }
        }

        public BaseBBIENode build() {
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

            BaseBBIENode bbieNode = new BaseBBIENode(seqKey, parent, bbie, bdtPriRestri, bbiep, bccp, bdt);
            appendBBIESC(bbie, bbieNode);
            return bbieNode;
        }
    }

    private class ASBIENodeBuilder {
        private DataContainerForProfileBODBuilder dataContainer;
        private Node parent;

        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public ASBIENodeBuilder(DataContainerForProfileBODBuilder dataContainer, Node parent,
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

        public BaseASBIENode build() {
            AggregateCoreComponent acc = dataContainer.getACC(asccp.getRoleOfAccId());
            this.roleOfAbie = createABIE(acc, dataContainer.getBusinessContext());

            createASBIEP();
            createASBIE();

            BaseASBIENode asbieNode = new BaseASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
            appendChildren(dataContainer, acc, roleOfAbie, asbieNode);
            return asbieNode;
        }
    }


    private class DataContainerForProfileBODLoader {
        private TopLevelAbie topLevelAbie;
        private List<BusinessContext> businessContextList;
        private List<BasicCoreComponent> bccList;
        private List<AssociationCoreComponentProperty> asccpList;
        private List<BasicCoreComponentProperty> bccpList;
        private List<DataType> dtList;
        private List<DataTypeSupplementaryComponent> dtScList;
        private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;

        private List<AggregateBusinessInformationEntity> abieList;
        private List<AssociationBusinessInformationEntity> asbieList;
        private List<AssociationBusinessInformationEntityProperty> asbiepList;
        private List<BasicBusinessInformationEntity> bbieList;
        private List<BasicBusinessInformationEntityProperty> bbiepList;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList;

        private Map<Long, BusinessContext> bizCtxIdMap;
        private Map<Long, BasicCoreComponent> bccIdMap;
        private Map<Long, AssociationCoreComponentProperty> asccpIdMap;
        private Map<Long, BasicCoreComponentProperty> bccpIdMap;
        private Map<Long, DataType> dtIdMap;
        private Map<Long, DataTypeSupplementaryComponent> dtScIdMap;
        private Map<Long, List<BusinessDataTypePrimitiveRestriction>> bdtPriRestriListByBdtIdMap;

        private Map<Long, AggregateBusinessInformationEntity> abieIdMap;
        private Map<Long, List<AssociationBusinessInformationEntity>> asbieByFromAbieIdMap;
        private Map<Long, AssociationBusinessInformationEntityProperty> asbiepIdMap;
        private Map<Long, AssociationBusinessInformationEntityProperty> asbiepByRoleOfAbieIdMap;
        private Map<Long, List<BasicBusinessInformationEntity>> bbieByFromAbieIdMap;
        private Map<Long, BasicBusinessInformationEntityProperty> bbiepIdMap;
        private Map<Long, List<BasicBusinessInformationEntitySupplementaryComponent>> bbieScListByBbieIdMap;


        public DataContainerForProfileBODLoader(TopLevelAbie topLevelAbie) {
            this.topLevelAbie = topLevelAbie;

            ExecutorCompletionService executorCompletionService = new ExecutorCompletionService(executorService);
            executorCompletionService.submit(() -> {
                businessContextList = businessContextRepository.findAll();
                bizCtxIdMap = businessContextList.stream()
                        .collect(Collectors.toMap(e -> e.getBizCtxId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                bccList = bccRepository.findAllWithRevisionNum(0);
                bccIdMap = bccList.stream()
                        .collect(Collectors.toMap(e -> e.getBccId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                asccpList = asccpRepository.findAllWithRevisionNum(0);
                asccpIdMap = asccpList.stream()
                        .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                bccpList = bccpRepository.findAllWithRevisionNum(0);
                bccpIdMap = bccpList.stream()
                        .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                dtList = dataTypeRepository.findAll();
                dtIdMap = dtList.stream()
                        .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                dtScList = dtScRepository.findAll();
                dtScIdMap = dtScList.stream()
                        .collect(Collectors.toMap(e -> e.getDtScId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                bdtPriRestriList = bdtPriRestriRepository.findAll();
                bdtPriRestriListByBdtIdMap = bdtPriRestriList.stream()
                        .collect(Collectors.groupingBy(e -> e.getBdtId()));
            }, null);

            executorCompletionService.submit(() -> {
                abieList = abieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                abieIdMap = abieList.stream()
                        .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                asbieList = asbieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                asbieByFromAbieIdMap = asbieList.stream()
                        .collect(Collectors.groupingBy(e -> e.getFromAbieId()));
            }, null);

            executorCompletionService.submit(() -> {
                asbiepList = asbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                asbiepIdMap = asbiepList.stream()
                        .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
                asbiepByRoleOfAbieIdMap = asbiepList.stream()
                        .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                bbieList = bbieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                bbieByFromAbieIdMap = bbieList.stream()
                        .collect(Collectors.groupingBy(e -> e.getFromAbieId()));
            }, null);

            executorCompletionService.submit(() -> {
                bbiepList = bbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                bbiepIdMap = bbiepList.stream()
                        .collect(Collectors.toMap(e -> e.getBbiepId(), Function.identity()));
            }, null);

            executorCompletionService.submit(() -> {
                bbiescList = bbiescRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
                bbieScListByBbieIdMap = bbiescList.stream()
                        .collect(Collectors.groupingBy(e -> e.getBbieId()));
            }, null);

            for (int i = 0, taskSize = 13; i < taskSize; ++i) {
                try {
                    executorCompletionService.take().get();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e.getCause());
                }
            }
        }

        public BusinessContext findBusinessContext(long bizCtxId) {
            return bizCtxIdMap.get(bizCtxId);
        }

        public AggregateBusinessInformationEntity findAbie(long abieId) {
            return abieIdMap.get(abieId);
        }

        public BasicCoreComponent findBcc(long bccId) {
            return bccIdMap.get(bccId);
        }

        public BasicCoreComponentProperty findBccp(long bccpId) {
            return bccpIdMap.get(bccpId);
        }

        public AssociationCoreComponentProperty findAsccp(long asccpId) {
            return asccpIdMap.get(asccpId);
        }

        public DataType findDt(long dtId) {
            return dtIdMap.get(dtId);
        }

        public DataTypeSupplementaryComponent findDtSc(long dtScId) {
            return dtScIdMap.get(dtScId);
        }

        public List<BusinessDataTypePrimitiveRestriction> findBdtPriRestriByBdtId(long bdtId) {
            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriListByBdtIdMap.get(bdtId);
            return (bdtPriRestriList != null) ? bdtPriRestriList : Collections.emptyList();
        }

        public AssociationBusinessInformationEntityProperty findAsbiep(long asbiepId) {
            return asbiepIdMap.get(asbiepId);
        }

        public BasicBusinessInformationEntityProperty findBbiep(long bbiepId) {
            return bbiepIdMap.get(bbiepId);
        }

        public AssociationBusinessInformationEntityProperty findAsbiepByRoleOfAbie(AggregateBusinessInformationEntity abie) {
            return asbiepByRoleOfAbieIdMap.get(abie.getAbieId());
        }

        public List<BasicBusinessInformationEntity> findBbieByFromAbieId(AggregateBusinessInformationEntity abie) {
            List<BasicBusinessInformationEntity> bbieList = bbieByFromAbieIdMap.get(abie.getAbieId());
            return (bbieList != null) ? bbieList : Collections.emptyList();
        }

        public List<AssociationBusinessInformationEntity> findAsbieByFromAbieId(AggregateBusinessInformationEntity abie) {
            List<AssociationBusinessInformationEntity> asbieList = asbieByFromAbieIdMap.get(abie.getAbieId());
            return (asbieList != null) ? asbieList : Collections.emptyList();
        }

        public List<BasicBusinessInformationEntitySupplementaryComponent> findBbieScByBbieId(long bbieId) {
            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = bbieScListByBbieIdMap.get(bbieId);
            return (bbieScList != null) ? bbieScList : Collections.emptyList();
        }

    }

    public BIENode createBIENode(TopLevelAbie topLevelAbie) {
        long s = System.currentTimeMillis();
        DataContainerForProfileBODLoader dataContainer = new DataContainerForProfileBODLoader(topLevelAbie);
        logger.info("DataContainerForProfileBODLoader instantiated - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        BusinessContext bizCtx = dataContainer.findBusinessContext(abie.getBizCtxId());
        AssociationBusinessInformationEntityProperty asbiep = dataContainer.findAsbiepByRoleOfAbie(abie);
        AssociationCoreComponentProperty asccp = dataContainer.findAsccp(asbiep.getBasedAsccpId());
        TopLevelNode topLevelNode = new BaseTopLevelNode(asbiep, asccp, abie, bizCtx);
        createBIEChildren(dataContainer, abie, topLevelNode);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        s = System.currentTimeMillis();
        topLevelNode.accept(new BIENodeSortVisitor());
        logger.info("Node sorted - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return topLevelNode;
    }

    private void createBIEChildren(DataContainerForProfileBODLoader dataContainer,
                                   AggregateBusinessInformationEntity abie, Node parent) {
        List<BasicBusinessInformationEntity> bbieList = dataContainer.findBbieByFromAbieId(abie);
        List<AssociationBusinessInformationEntity> asbieList = dataContainer.findAsbieByFromAbieId(abie);

        Map<BusinessInformationEntity, Double> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        TreeMap<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

        for (BasicBusinessInformationEntity bbie : bbieList) {
            double sk = bbie.getSeqKey();
            if (getEntityType(dataContainer, bbie.getBasedBccId()) == 0L)
                appendBBIENode(dataContainer, bbie, parent);
            else
                sequence.put(bbie, sk);
        }

        for (AssociationBusinessInformationEntity asbie : asbieList) {
            double sk = asbie.getSeqKey();
            sequence.put(asbie, sk);
        }

        ordered_sequence.putAll(sequence);
        Set set = ordered_sequence.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getKey() instanceof BasicBusinessInformationEntity)
                appendBBIENode(dataContainer, (BasicBusinessInformationEntity) me.getKey(), parent);
            else
                appendASBIENode(dataContainer, (AssociationBusinessInformationEntity) me.getKey(), parent);
        }
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

    public int getEntityType(DataContainerForProfileBODLoader dataContainer, long bccId) {
        BasicCoreComponent basicCoreComponent = dataContainer.findBcc(bccId);
        return basicCoreComponent.getEntityType();
    }

    private void appendBBIENode(DataContainerForProfileBODLoader dataContainer,
                                BasicBusinessInformationEntity bbie, Node parent) {
        BasicBusinessInformationEntityProperty bbiep = dataContainer.findBbiep(bbie.getToBbiepId());
        BasicCoreComponentProperty bccp = dataContainer.findBccp(bbiep.getBasedBccpId());

        long bdtId = bccp.getBdtId();
        DataType bdt = dataContainer.findDt(bdtId);
        BusinessDataTypePrimitiveRestriction bdtPriRestri = bdtPriRestriRepository.findOne(bbie.getBdtPriRestriId());

        int seqKey = (int) bbie.getSeqKey();
        BBIENode bbieNode = new BaseBBIENode(seqKey, parent, bbie, bdtPriRestri, bbiep, bccp, bdt);
        appendBBIESC(dataContainer, bbie, bbieNode);
    }

    private void appendBBIESC(DataContainerForProfileBODLoader dataContainer,
                              BasicBusinessInformationEntity bbie, BBIENode parent) {
        List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList =
                dataContainer.findBbieScByBbieId(bbie.getBbieId());
        for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : bbiescList) {
            long dtScId = bbiesc.getDtScId();
            DataTypeSupplementaryComponent dtsc = dataContainer.findDtSc(dtScId);
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtPriRestri =
                    bdtScPriRestriRepository.findOne(bbiesc.getDtScPriRestriId());

            new BaseBBIESCNode(parent, bbiesc, bdtPriRestri, dtsc);
        }
    }

    private void appendASBIENode(DataContainerForProfileBODLoader dataContainer,
                                 AssociationBusinessInformationEntity asbie, Node parent) {
        AssociationBusinessInformationEntityProperty asbiep = dataContainer.findAsbiep(asbie.getToAsbiepId());
        AssociationCoreComponentProperty asccp = dataContainer.findAsccp(asbiep.getBasedAsccpId());
        AggregateBusinessInformationEntity roleOfAbie = dataContainer.findAbie(asbiep.getRoleOfAbieId());

        int seqKey = (int) asbie.getSeqKey();
        ASBIENode asbieNode = new BaseASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
        createBIEChildren(dataContainer, roleOfAbie, asbieNode);
    }

    /*
     * Lazy Node
     */
    public LazyBIENode createLazyBIENode(TopLevelAbie topLevelAbie) {
        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        BusinessContext bizCtx = businessContextRepository.findOne(abie.getBizCtxId());
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
        TopLevelNode topLevelNode = new BaseTopLevelNode(asbiep, asccp, abie, bizCtx);
        BIEFetcher fetcher = new BIEFetcher(abie);
        return new LazyTopLevelNode(topLevelNode, fetcher, fetcher.getChildrenCount());
    }

    private class BIEFetcher implements Fetcher {
        private AggregateBusinessInformationEntity abie;

        public BIEFetcher(AggregateBusinessInformationEntity abie) {
            this.abie = abie;
        }

        public int getChildrenCount() {
            long abieId = abie.getAbieId();
            return bbieRepository.countByFromAbieId(abieId) + asbieRepository.countByFromAbieId(abieId);
        }

        @Override
        public void fetch(Node parent) {
            long abieId = abie.getAbieId();
            List<BasicBusinessInformationEntity> bbieList = bbieRepository.findByFromAbieId(abieId);
            List<AssociationBusinessInformationEntity> asbieList = asbieRepository.findByFromAbieId(abieId);

            Map<BusinessInformationEntity, Double> sequence = new HashMap();
            ValueComparator bvc = new ValueComparator(sequence);
            TreeMap<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

            for (BasicBusinessInformationEntity bbie : bbieList) {
                double sk = bbie.getSeqKey();
                if (getEntityType(bbie.getBasedBccId()) == 0L)
                    appendBBIELazyNode(bbie, parent);
                else
                    sequence.put(bbie, sk);
            }

            for (AssociationBusinessInformationEntity asbie : asbieList) {
                double sk = asbie.getSeqKey();
                sequence.put(asbie, sk);
            }

            ordered_sequence.putAll(sequence);
            Set set = ordered_sequence.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                if (me.getKey() instanceof BasicBusinessInformationEntity)
                    appendBBIELazyNode((BasicBusinessInformationEntity) me.getKey(), parent);
                else
                    appendASBIELazyNode((AssociationBusinessInformationEntity) me.getKey(), parent);
            }
        }

        private int getEntityType(long bccId) {
            BasicCoreComponent basicCoreComponent = bccRepository.findOne(bccId);
            return basicCoreComponent.getEntityType();
        }

        private void appendBBIELazyNode(BasicBusinessInformationEntity bbie, Node parent) {
            BasicBusinessInformationEntityProperty bbiep = bbiepRepository.findOne(bbie.getToBbiepId());
            BasicCoreComponentProperty bccp = bccpRepository.findOne(bbiep.getBasedBccpId());

            long bdtId = bccp.getBdtId();
            DataType bdt = dataTypeRepository.findOne(bdtId);
            BusinessDataTypePrimitiveRestriction bdtPriRestri = bdtPriRestriRepository.findOne(bbie.getBdtPriRestriId());

            int seqKey = (int) bbie.getSeqKey();
            BBIENode bbieNode = new BaseBBIENode(seqKey, null, bbie, bdtPriRestri, bbiep, bccp, bdt);
            BBIESCFetcher fetcher = new BBIESCFetcher(bbie);
            new LazyBBIENode(bbieNode, fetcher, fetcher.getChildrenCount(), parent);
        }

        private void appendASBIELazyNode(AssociationBusinessInformationEntity asbie, Node parent) {
            AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOne(asbie.getToAsbiepId());
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
            AggregateBusinessInformationEntity roleOfAbie = abieRepository.findOne(asbiep.getRoleOfAbieId());

            int seqKey = (int) asbie.getSeqKey();
            ASBIENode asbieNode = new BaseASBIENode(seqKey, null, asbie, asbiep, asccp, roleOfAbie);
            BIEFetcher fetcher = new BIEFetcher(roleOfAbie);
            new LazyASBIENode(asbieNode, fetcher, fetcher.getChildrenCount(), parent);
        }
    }

    private class BBIESCFetcher implements Fetcher {

        private BasicBusinessInformationEntity bbie;

        public BBIESCFetcher(BasicBusinessInformationEntity bbie) {
            this.bbie = bbie;
        }

        public int getChildrenCount() {
            return bbiescRepository.countByBbieId(bbie.getBbieId());
        }

        @Override
        public void fetch(Node parent) {
            List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList =
                    bbiescRepository.findByBbieId(bbie.getBbieId());
            for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : bbiescList) {
                long dtScId = bbiesc.getDtScId();
                DataTypeSupplementaryComponent dtsc = dtScRepository.findOne(dtScId);
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtPriRestri =
                        bdtScPriRestriRepository.findOne(bbiesc.getDtScPriRestriId());

                new BaseBBIESCNode(parent, bbiesc, bdtPriRestri, dtsc);
            }
        }
    }

    private class DataContainerForCC implements CoreComponentProvider {

        private List<AggregateCoreComponent> accList;
        private List<AssociationCoreComponent> asccList;
        private List<AssociationCoreComponentProperty> asccpList;
        private List<BasicCoreComponent> bccList;
        private List<BasicCoreComponentProperty> bccpList;
        private List<DataType> dataTypes;

        private Map<Long, AggregateCoreComponent> accMap;
        private Map<Long, AssociationCoreComponent> asccMap;
        private Map<Long, AssociationCoreComponentProperty> asccpMap;
        private Map<Long, BasicCoreComponent> bccMap;
        private Map<Long, BasicCoreComponentProperty> bccpMap;
        private Map<Long, DataType> bdtMap;

        private Map<Long, List<BasicCoreComponent>> fromAccIdToBccMap;
        private Map<Long, List<BasicCoreComponent>> fromAccIdToBccWithoutAttributesMap;
        private Map<Long, List<AssociationCoreComponent>> fromAccIdToAsccMap;

        public DataContainerForCC() {
            accList = accRepository.findAllWithRevisionNum(0);
            asccList = asccRepository.findAllWithRevisionNum(0);
            asccpList = asccpRepository.findAllWithRevisionNum(0);
            bccList = bccRepository.findAllWithRevisionNum(0);
            bccpList = bccpRepository.findAllWithRevisionNum(0);
            dataTypes = dataTypeRepository.findAll();

            accMap = accList.stream()
                    .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));
            asccMap = asccList.stream()
                    .collect(Collectors.toMap(e -> e.getAsccId(), Function.identity()));
            asccpMap = asccpList.stream()
                    .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));
            bccMap = bccList.stream()
                    .collect(Collectors.toMap(e -> e.getBccId(), Function.identity()));
            bccpMap = bccpList.stream()
                    .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));
            bdtMap = dataTypes.stream()
                    .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));

            fromAccIdToBccMap = bccList.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));
            fromAccIdToBccWithoutAttributesMap = bccList.stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));
            fromAccIdToAsccMap = asccList.stream()
                    .collect(Collectors.groupingBy(e -> e.getFromAccId()));
        }

        public AggregateCoreComponent getACC(long accId) {
            return accMap.get(accId);
        }

        public AssociationCoreComponent getASCC(long asccId) {
            return asccMap.get(asccId);
        }

        public AssociationCoreComponentProperty getASCCP(long asccpId) {
            return asccpMap.get(asccpId);
        }

        public BasicCoreComponent getBCC(long bccId) {
            return bccMap.get(bccId);
        }

        public BasicCoreComponentProperty getBCCP(long bccpId) {
            return bccpMap.get(bccpId);
        }

        public DataType getBdt(long bdtId) {
            return bdtMap.get(bdtId);
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

    public CCNode createCCNode(long asccpId) {
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asccpId);
        return createCCNode(asccp);
    }

    public CCNode createCCNode(AssociationCoreComponentProperty asccp) {
        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }

        DataContainerForCC dataContainer = new DataContainerForCC();
        return createASCCPNode(dataContainer, asccp);
    }

    private ACCNode createACCNode(DataContainerForCC dataContainer, Node parent, AggregateCoreComponent acc) {
        long basedAccId = acc.getBasedAccId();
        ACCNode accNode = new BaseACCNode(parent, acc);

        List<CoreComponent> coreComponentList = coreComponentService.getCoreComponents(acc, dataContainer);
        for (CoreComponent coreComponent : coreComponentList) {
            if (coreComponent instanceof BasicCoreComponent) {
                createBCCPNode(dataContainer, accNode, (BasicCoreComponent) coreComponent);
            } else if (coreComponent instanceof AssociationCoreComponent) {
                createASCCPNode(dataContainer, accNode, (AssociationCoreComponent) coreComponent);
            }
        }

        if (basedAccId > 0L) {
            ACCNode basedAccNode = createACCNode(dataContainer, accNode, dataContainer.getACC(basedAccId));
            accNode.setBasedAcc(basedAccNode);
        }

        return accNode;
    }

    private BCCPNode createBCCPNode(DataContainerForCC dataContainer,
                                    ACCNode fromAccNode, BasicCoreComponent bcc) {
        if (fromAccNode.getAcc().getAccId() != bcc.getFromAccId()) {
            throw new IllegalArgumentException("ACC ID doesn't match between relative and itself.");
        }

        BasicCoreComponentProperty bccp = dataContainer.getBCCP(bcc.getToBccpId());
        DataType bdt = dataContainer.getBdt(bccp.getBdtId());

        BCCPNode bccNode = new BaseBCCPNode(fromAccNode, bcc, bccp, bdt);
        return bccNode;
    }

    private ASCCPNode createASCCPNode(DataContainerForCC dataContainer, AssociationCoreComponentProperty asccp) {
        ASCCPNode asccpNode = new BaseASCCPNode(asccp);
        return setRoleOfACC(dataContainer, asccpNode);
    }

    private ASCCPNode createASCCPNode(DataContainerForCC dataContainer,
                                      ACCNode fromAccNode, AssociationCoreComponent ascc) {
        if (fromAccNode.getAcc().getAccId() != ascc.getFromAccId()) {
            throw new IllegalArgumentException("ACC ID doesn't match between relative and itself.");
        }
        ASCCPNode asccpNode = new BaseASCCPNode(fromAccNode, ascc, dataContainer.getASCCP(ascc.getToAsccpId()));
        return setRoleOfACC(dataContainer, asccpNode);
    }

    private ASCCPNode setRoleOfACC(DataContainerForCC dataContainer, ASCCPNode asccpNode) {
        AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
        AggregateCoreComponent acc = dataContainer.getACC(asccp.getRoleOfAccId());
        ACCNode roleOfAcc = createACCNode(dataContainer, asccpNode, acc);
        asccpNode.setRoleOfAcc(roleOfAcc);
        return asccpNode;
    }

    public LazyCCNode createLazyCCNodeByAsccpId(long asccpId) {
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asccpId);
        return createLazyCCNode(asccp);
    }

    public LazyCCNode createLazyCCNode(AssociationCoreComponentProperty asccp) {
        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }

        ASCCPNode asccpNode = new BaseASCCPNode(asccp);
        ASCCPFetcher fetcher = new ASCCPFetcher(asccp);
        return new LazyASCCPNode(asccpNode, fetcher, fetcher.getChildrenCount());
    }

    public LazyCCNode createLazyCCNodeByAccId(long accId) {
        AggregateCoreComponent acc = accRepository.findOne(accId);
        return createLazyCCNode(acc);
    }

    public LazyCCNode createLazyCCNode(AggregateCoreComponent acc) {
        if (acc == null) {
            throw new IllegalArgumentException("'acc' argument must not be null.");
        }
        ACCNode accNode = new BaseACCNode(null, acc);
        ACCFetcher fetcher = new ACCFetcher(acc);
        return new LazyACCNode(accNode, fetcher, fetcher.getChildrenCount(), null);
    }

    private class ASCCPFetcher implements Fetcher {

        private AssociationCoreComponentProperty asccp;

        public ASCCPFetcher(AssociationCoreComponentProperty asccp) {
            this.asccp = asccp;
        }

        @Override
        public void fetch(Node parent) {
            ASCCPNode asccpNode = (ASCCPNode) parent;
            AggregateCoreComponent acc = accRepository.findOne(asccp.getRoleOfAccId());
            ACCNode accNode = createLazyACCNode(parent, acc);
            asccpNode.setRoleOfAcc(accNode);
        }

        public int getChildrenCount() {
            // ASCCP only have one ACC child
            return 1;
        }
    }

    private LazyACCNode createLazyACCNode(Node parent, AggregateCoreComponent acc) {
        ACCNode accNode = new BaseACCNode(parent, acc);
        ACCFetcher fetcher = new ACCFetcher(acc);
        LazyACCNode lazyACCNode = new LazyACCNode(accNode, fetcher, fetcher.getChildrenCount(), parent);
        return lazyACCNode;
    }

    private class ACCFetcher implements Fetcher {

        private AggregateCoreComponent acc;

        public ACCFetcher(AggregateCoreComponent acc) {
            this.acc = acc;
        }

        @Override
        public void fetch(Node parent) {
            ACCNode accNode = (ACCNode) parent;

            List<CoreComponent> coreComponentList = coreComponentService.getCoreComponents(
                    acc, new CoreComponentProviderImpl());
            for (CoreComponent coreComponent : coreComponentList) {
                if (coreComponent instanceof BasicCoreComponent) {
                    createBCCPNode(accNode, (BasicCoreComponent) coreComponent);
                } else if (coreComponent instanceof AssociationCoreComponent) {
                    createLazyASCCPNode(accNode, (AssociationCoreComponent) coreComponent);
                }
            }

            long basedAccId = acc.getBasedAccId();
            if (basedAccId > 0L) {
                AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
                ACCNode basedAccNode = createLazyACCNode(accNode, basedAcc);
                accNode.setBasedAcc(basedAccNode);
            }
        }

        private void createBCCPNode(ACCNode fromAccNode, BasicCoreComponent bcc) {
            if (fromAccNode.getAcc().getAccId() != bcc.getFromAccId()) {
                throw new IllegalArgumentException("ACC ID doesn't match between relative and itself.");
            }

            BasicCoreComponentProperty bccp = bccpRepository.findOne(bcc.getToBccpId());
            DataType bdt = dataTypeRepository.findOne(bccp.getBdtId());

            new BaseBCCPNode(fromAccNode, bcc, bccp, bdt);
        }

        private void createLazyASCCPNode(ACCNode fromAccNode, AssociationCoreComponent ascc) {
            if (fromAccNode.getAcc().getAccId() != ascc.getFromAccId()) {
                throw new IllegalArgumentException("ACC ID doesn't match between relative and itself.");
            }

            AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
            ASCCPNode asccpNode = new BaseASCCPNode(null, ascc, asccp);
            ASCCPFetcher fetcher = new ASCCPFetcher(asccp);
            new LazyASCCPNode(asccpNode, fetcher, fetcher.getChildrenCount(), fromAccNode);
        }

        public int getChildrenCount() {
            long basedAccId = acc.getBasedAccId();
            int asccCount = asccRepository.countByFromAccId(acc.getAccId());
            int bccCount = bccRepository.countByFromAccId(acc.getAccId());

            int childrenCount = ((basedAccId > 0L) ? 1 : 0) + asccCount + bccCount;
            return childrenCount;
        }
    }

    private class CoreComponentProviderImpl implements CoreComponentProvider {

        @Override
        public List<BasicCoreComponent> getBCCs(long accId) {
            return bccRepository.findByFromAccId(accId);
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
            return bccRepository.findByFromAccIdAndSeqKeyIsNotZero(accId);
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(long accId) {
            return asccRepository.findByFromAccId(accId);
        }
    }
}
