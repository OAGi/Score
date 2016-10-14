package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreateBIETreeNode {

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

    private class NodeSortVisitor implements NodeVisitor {
        @Override
        public void visit(Node node) {
            Collections.sort(node.getChildren(), (a, b) -> a.getSeqKey() - b.getSeqKey());
        }
    }

    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        Node node = createNode(asccp, bizCtx);
        NodeSortVisitor nodeSortVisitor = new NodeSortVisitor();
        node.accept(nodeSortVisitor);

        TreeNodeVisitor treeNodeVisitor = new TreeNodeVisitor();
        node.accept(treeNodeVisitor);
        return treeNodeVisitor.getRoot();
    }

    private Node createNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        DataContainer dataContainer = new DataContainer(bizCtx);
        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = dataContainer.getACC(roleOfAccId);
        AggregateBusinessInformationEntity abie = createABIE(acc, bizCtx);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(asccp, abie);

        TopLevelNode topLevelNode = new TopLevelNode(asbiep, asccp, abie, bizCtx);
        appendChildren(dataContainer, acc, abie, topLevelNode);
        return topLevelNode;
    }

    private AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc, BusinessContext bizCtx) {

        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAcc(acc);
        abie.setBizCtx(bizCtx);
        abie.setDefinition(acc.getDefinition());
        // abie.setCreatedBy(userId);
        // abie.setLastUpdatedBy(userId);
        // abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
        // abie.setOwnerTopLevelAbie(topLevelAbie);

        return abie;
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(AssociationCoreComponentProperty asccp,
                                                                      AggregateBusinessInformationEntity abie) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccp(asccp);
        asbiep.setRoleOfAbie(abie);
        // asbiep.setCreatedBy(userId);
        // asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(asccp.getDefinition());
        // asbiep.setOwnerTopLevelAbieId(topLevelAbie);

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
                        new BBIENodeBuilder(dataContainer, bcc, abie, skb).build(parent);
                    }
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0) {
                        new BBIENodeBuilder(dataContainer, bcc, abie, skb + i - attr_cnt).build(parent);
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    new ASBIENodeBuilder(dataContainer, ascc, abie, skb + i - attr_cnt).build(parent);
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
            if (srt instanceof AssociationCoreComponent && groupcheck(dataContainer, (AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty = dataContainer.getASCCP(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent = dataContainer.getACC(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(dataContainer, aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private boolean groupcheck(DataContainer dataContainer, AssociationCoreComponent associationCoreComponent) {
        boolean check = false;
        AssociationCoreComponentProperty asccp = dataContainer.getASCCP(associationCoreComponent.getToAsccpId());
        AggregateCoreComponent acc = dataContainer.getACC(asccp.getRoleOfAccId());
        if (acc.getOagisComponentType() == 3) {
            check = true;
        }
        return check;
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
                    groupcheck(dataContainer, (AssociationCoreComponent) coreComponent)) {

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
        private Map<Long, AggregateCoreComponent> accMap;
        private Map<Long, AssociationCoreComponentProperty> asccpMap;
        private Map<Long, BasicCoreComponentProperty> bccpMap;
        private Map<Long, DataType> dtMap;
        private Map<Long, DataTypeSupplementaryComponent> dtScMap;

        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
        private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;

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
            associationCoreComponents = asccRepository.findAll();
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
        }

        public BusinessContext getBusinessContext() {
            return businessContext;
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
            return dataTypeSupplementaryComponents.stream()
                    .filter(dtSc -> dtSc.getOwnerDtId() == ownerDtId)
                    .collect(Collectors.toList());
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
            return bdtPriRestriList.stream()
                    .filter(e -> e.getBdtId() == bdtId)
                    .collect(Collectors.toList());
        }

        @Override
        public List<BasicCoreComponent> getBCCs(long accId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == accId)
                    .collect(Collectors.toList());
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == accId)
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.toList());
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(long accId) {
            return associationCoreComponents.stream()
                    .filter(acc -> acc.getFromAccId() == accId)
                    .collect(Collectors.toList());
        }
    }

    private class BBIENodeBuilder {
        private DataContainer dataContainer;
        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public BBIENodeBuilder(DataContainer dataContainer,
                               BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.dataContainer = dataContainer;
            this.bcc = bcc;
            this.abie = abie;
            this.seqKey = seqKey;
        }

        private void createBBIEP(BasicCoreComponentProperty bccp) {
            bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccp(bccp);
            bbiep.setDefinition(bccp.getDefinition());
//            bbiep.setCreatedBy(userId);
//            bbiep.setLastUpdatedBy(userId);
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
//            bbie.setCreatedBy(userId);
//            bbie.setLastUpdatedBy(userId);
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

        public BBIENode build(Node parent) {
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
        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public ASBIENodeBuilder(DataContainer dataContainer,
                                AssociationCoreComponent ascc,
                                AggregateBusinessInformationEntity fromAbie,
                                int seqKey) {
            this.dataContainer = dataContainer;
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
            // asbiep.setCreatedBy(userId);
            // asbiep.setLastUpdatedBy(userId);
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
            // asbie.setCreatedBy(userId);
            // asbie.setLastUpdatedBy(userId);
            asbie.setSeqKey(seqKey);
        }

        public ASBIENode build(Node parent) {
            createASBIEP();
            createASBIE();

            AggregateCoreComponent acc = dataContainer.getACC(asccp.getRoleOfAccId());
            this.roleOfAbie = createABIE(acc, dataContainer.getBusinessContext());

            ASBIENode asbieNode = new ASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
            appendChildren(dataContainer, acc, roleOfAbie, asbieNode);
            return asbieNode;
        }
    }

}
