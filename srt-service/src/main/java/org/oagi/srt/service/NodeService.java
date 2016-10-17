package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.model.bod.visitor.NodeSortVisitor;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public Node createNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        long s = System.currentTimeMillis();
        DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder = new DataContainerForProfileBODBuilder(bizCtx);

        logger.info("DataContainerForProfileBODBuilder instantiated - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = dataContainerForProfileBODBuilder.getACC(roleOfAccId);
        AggregateBusinessInformationEntity abie = createABIE(acc, bizCtx);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(asccp, abie);

        TopLevelNode topLevelNode = new TopLevelNode(asbiep, asccp, abie, bizCtx);
        appendChildren(dataContainerForProfileBODBuilder, acc, abie, topLevelNode);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        s = System.currentTimeMillis();
        topLevelNode.accept(new NodeSortVisitor());
        logger.info("Node sorted - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

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

    private void appendChildren(DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder,
                                AggregateCoreComponent acc, AggregateBusinessInformationEntity abie, Node parent) {
        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        accList.add(acc);
        while (acc.getBasedAccId() > 0) {
            acc = dataContainerForProfileBODBuilder.getACC(acc.getBasedAccId());
            accList.add(acc);
        }

        while (!accList.isEmpty()) {
            acc = accList.pollFirst();
            int skb = 0;
            for (AggregateCoreComponent cnt_acc : accList) {
                skb += queryNestedChildAssoc_wo_attribute(dataContainerForProfileBODBuilder, cnt_acc).size(); //here
            }

            List<CoreComponent> childAssoc = queryNestedChildAssoc(dataContainerForProfileBODBuilder, acc);
            int attr_cnt = childAssoc.size() - queryNestedChildAssoc_wo_attribute(dataContainerForProfileBODBuilder, acc).size();
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() == 0) {
                        new BBIENodeBuilder(dataContainerForProfileBODBuilder, parent, bcc, abie, skb).build();
                    }
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0) {
                        new BBIENodeBuilder(dataContainerForProfileBODBuilder, parent, bcc, abie, skb + i - attr_cnt).build();
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    new ASBIENodeBuilder(dataContainerForProfileBODBuilder, parent, ascc, abie, skb + i - attr_cnt).build();
                }
            }
        }
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(
            DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder,
            AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(
                aggregateCoreComponent, dataContainerForProfileBODBuilder);
        return getAssocList(dataContainerForProfileBODBuilder, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc(
            DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder,
            AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, dataContainerForProfileBODBuilder);
        return getAssocList(dataContainerForProfileBODBuilder, assoc);
    }

    private List<CoreComponent> getAssocList(
            DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder, List<CoreComponent> list) {
        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && dataContainerForProfileBODBuilder.groupcheck((AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty = dataContainerForProfileBODBuilder.getASCCP(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent = dataContainerForProfileBODBuilder.getACC(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(dataContainerForProfileBODBuilder, aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private List<CoreComponent> handleNestedGroup(DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder,
                                                  AggregateCoreComponent acc,
                                                  List<CoreComponent> coreComponents, int gPosition) {

        List<CoreComponent> bList = queryChildAssoc(dataContainerForProfileBODBuilder, acc);
        if (!bList.isEmpty()) {
            coreComponents.addAll(gPosition, bList);
            coreComponents.remove(gPosition + bList.size());
        }

        for (int i = 0; i < coreComponents.size(); i++) {
            CoreComponent coreComponent = coreComponents.get(i);
            if (coreComponent instanceof AssociationCoreComponent &&
                    dataContainerForProfileBODBuilder.groupcheck((AssociationCoreComponent) coreComponent)) {

                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty asccp = dataContainerForProfileBODBuilder.getASCCP(ascc.getToAsccpId());
                coreComponents = handleNestedGroup(dataContainerForProfileBODBuilder,
                        dataContainerForProfileBODBuilder.getACC(asccp.getRoleOfAccId()), coreComponents, i);
            }
        }

        return coreComponents;
    }

    private List<CoreComponent> queryChildAssoc(DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder,
                                                AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, dataContainerForProfileBODBuilder);
        return assoc;
    }

    private class DataContainerForProfileBODBuilder implements CoreComponentProvider {
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

        public DataContainerForProfileBODBuilder(BusinessContext businessContext) {
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
        private DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder;
        private Node parent;
        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public BBIENodeBuilder(DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder, Node parent,
                               BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.dataContainerForProfileBODBuilder = dataContainerForProfileBODBuilder;
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
            bbieScList = dataContainerForProfileBODBuilder.findDtScByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getCardinalityMax() != 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        long bdtScId = dtSc.getDtScId();
                        bbieSc.setBbie(bbie);
                        bbieSc.setDtSc(dtSc);
                        bbieSc.setGuid(Utility.generateGUID());
                        long bdtScPriRestriId = dataContainerForProfileBODBuilder.getDefaultBdtScPriRestriId(bdtScId);
                        if (bdtScPriRestriId > 0L) {
                            bbieSc.setDtScPriRestri(dataContainerForProfileBODBuilder.getBdtScPriRestri(bdtScPriRestriId));
                        }
                        long codeListId = dataContainerForProfileBODBuilder.getCodeListIdOfBdtScPriRestriId(bdtScId);
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
            BasicCoreComponentProperty bccp = dataContainerForProfileBODBuilder.getBCCP(bcc.getToBccpId());
            long bdtId = bccp.getBdtId();
            long bdtPrimitiveRestrictionId = dataContainerForProfileBODBuilder.getDefaultBdtPriRestriId(bdtId);
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    dataContainerForProfileBODBuilder.getBdtPriRestri(bdtPrimitiveRestrictionId);
            long codeListId = dataContainerForProfileBODBuilder.getCodeListIdOfBdtPriRestriId(bdtId);
            DataType bdt = dataContainerForProfileBODBuilder.getDt(bdtId);

            createBBIEP(bccp);
            createBBIE(bdtPriRestri, codeListId);
            createBBIESC(bdtId);

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = dataContainerForProfileBODBuilder.getBdtPriRestriByBdtId(bdtId);

            BBIENode bbieNode = new BBIENode(seqKey, parent, bbie, bbiep, bccp, bdt, bdtPriRestriList);
            appendBBIESC(bbie, bbieNode);
            return bbieNode;
        }
    }

    private class ASBIENodeBuilder {
        private DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder;
        private Node parent;

        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public ASBIENodeBuilder(DataContainerForProfileBODBuilder dataContainerForProfileBODBuilder, Node parent,
                                AssociationCoreComponent ascc,
                                AggregateBusinessInformationEntity fromAbie,
                                int seqKey) {
            this.dataContainerForProfileBODBuilder = dataContainerForProfileBODBuilder;
            this.parent = parent;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;

            this.asccp = dataContainerForProfileBODBuilder.getASCCP(ascc.getToAsccpId());
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
            AggregateCoreComponent acc = dataContainerForProfileBODBuilder.getACC(asccp.getRoleOfAccId());
            this.roleOfAbie = createABIE(acc, dataContainerForProfileBODBuilder.getBusinessContext());

            createASBIEP();
            createASBIE();

            ASBIENode asbieNode = new ASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
            appendChildren(dataContainerForProfileBODBuilder, acc, roleOfAbie, asbieNode);
            return asbieNode;
        }
    }



    private class DataContainerForProfileBODLoader {
        private TopLevelAbie topLevelAbie;
        private List<AggregateBusinessInformationEntity> abieList;
        private List<AssociationBusinessInformationEntity> asbieList;
        private List<AssociationBusinessInformationEntityProperty> asbiepList;
        private List<BasicBusinessInformationEntity> bbieList;
        private List<BasicBusinessInformationEntityProperty> bbiepList;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList;

        private Map<Long, AggregateBusinessInformationEntity> abieIdMap;
        private Map<Long, AssociationBusinessInformationEntity> asbieIdMap;
        private Map<Long, AssociationBusinessInformationEntityProperty> asbiepIdMap;
        private Map<Long, AssociationBusinessInformationEntityProperty> asbiepByRoleOfAbieIdMap;
        private Map<Long, BasicBusinessInformationEntity> bbieIdMap;
        private Map<Long, BasicBusinessInformationEntityProperty> bbiepIdMap;
        private Map<Long, BasicBusinessInformationEntitySupplementaryComponent> bbiescIdMap;

        public DataContainerForProfileBODLoader(TopLevelAbie topLevelAbie) {
            this.topLevelAbie = topLevelAbie;

            abieList = abieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            asbieList = asbieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            asbiepList = asbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            bbieList = bbieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            bbiepList = bbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            bbiescList = bbiescRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

            abieIdMap = abieList.stream()
                    .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));
            asbieIdMap = asbieList.stream()
                    .collect(Collectors.toMap(e -> e.getAsbieId(), Function.identity()));
            asbiepIdMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
            asbiepByRoleOfAbieIdMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));
            bbieIdMap = bbieList.stream()
                    .collect(Collectors.toMap(e -> e.getBbieId(), Function.identity()));
            bbiepIdMap = bbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getBbiepId(), Function.identity()));
            bbiescIdMap = bbiescList.stream()
                    .collect(Collectors.toMap(e -> e.getBbieScId(), Function.identity()));
        }

        public AggregateBusinessInformationEntity findAbie(long abieId) {
            return abieIdMap.get(abieId);
        }
    }




    public Node createNode(TopLevelAbie topLevelAbie) {
        DataContainerForProfileBODLoader dataContainer = new DataContainerForProfileBODLoader(topLevelAbie);

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        BusinessContext bizCtx = abie.getBizCtx();
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
        AssociationCoreComponentProperty asccp = asbiep.getBasedAsccp();
        TopLevelNode topLevelNode = new TopLevelNode(asbiep, asccp, abie, bizCtx);

        createBIEChildren(abie, topLevelNode);
        return topLevelNode;
    }

    private void createBIEChildren(AggregateBusinessInformationEntity abie, Node parent) {
        long abieId = abie.getAbieId();
        List<BasicBusinessInformationEntity> bbieList = bbieRepository.findByFromAbieId(abieId);
        List<AssociationBusinessInformationEntity> asbieList = asbieRepository.findByFromAbieId(abieId);

        Map<BusinessInformationEntity, Double> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        TreeMap<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

        for (BasicBusinessInformationEntity bbie : bbieList) {
            double sk = bbie.getSeqKey();
            if (getEntityType(bbie.getBasedBccId()) == 0L)
                showBBIETree(bbie, parent);
            else
                sequence.put(bbie, sk);
        }

        for (AssociationBusinessInformationEntity asbieVO : asbieList) {
            double sk = asbieVO.getSeqKey();
            sequence.put(asbieVO, sk);
        }

        ordered_sequence.putAll(sequence);
        Set set = ordered_sequence.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getKey() instanceof BasicBusinessInformationEntity)
                showBBIETree((BasicBusinessInformationEntity) me.getKey(), parent);
            else
                showASBIETree((AssociationBusinessInformationEntity) me.getKey(), parent);
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

    public int getEntityType(long bccId) {
        BasicCoreComponent basicCoreComponent = bccRepository.findOne(bccId);
        return basicCoreComponent.getEntityType();
    }

    private void showBBIETree(BasicBusinessInformationEntity bbie, Node parent) {
        BasicBusinessInformationEntityProperty bbiep = bbie.getToBbiep();
        BasicCoreComponentProperty bccp = bbiep.getBasedBccp();

        long bdtId = bccp.getBdtId();
        DataType bdt = dataTypeRepository.findOne(bdtId);
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findByBdtId(bdtId);

        int seqKey = (int) bbie.getSeqKey();
        BBIENode bbieNode = new BBIENode(seqKey, parent, bbie, bbiep, bccp, bdt, bdtPriRestriList);
        appendBBIESC(bbie, bbieNode);
    }

    private void appendBBIESC(BasicBusinessInformationEntity bbie, BBIENode parent) {
        List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList =
                bbiescRepository.findByBbieId(bbie.getBbieId());
        for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : bbiescList) {
            DataTypeSupplementaryComponent dtsc = bbiesc.getDtSc();
            new BBIESCNode(parent, bbiesc, dtsc);
        }
    }

    private void showASBIETree(AssociationBusinessInformationEntity asbie, Node parent) {
        AssociationBusinessInformationEntityProperty asbiep = asbie.getToAsbiep();
        AssociationCoreComponentProperty asccp = asbiep.getBasedAsccp();
        AggregateBusinessInformationEntity roleOfAbie = asbiep.getRoleOfAbie();

        int seqKey = (int) asbie.getSeqKey();
        ASBIENode asbieNode = new ASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
        createBIEChildren(roleOfAbie, asbieNode);
    }
}
