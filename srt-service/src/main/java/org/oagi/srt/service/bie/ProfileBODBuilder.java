package org.oagi.srt.service.bie;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.impl.BaseASBIENode;
import org.oagi.srt.model.bie.impl.BaseBBIENode;
import org.oagi.srt.model.bie.impl.BaseBBIESCNode;
import org.oagi.srt.model.bie.impl.BaseTopLevelNode;
import org.oagi.srt.model.bie.visitor.BIENodeSortVisitor;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Element;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ProfileBODBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private BusinessContextRepository bizCtxRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    private DataContainerForProfileBODBuilder dataContainer;

    public void setDataContainer(DataContainerForProfileBODBuilder dataContainer) {
        this.dataContainer = dataContainer;
    }

    public BIENode createBIENode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        long s = System.currentTimeMillis();

        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = dataContainer.getACC(roleOfAccId);
        AggregateBusinessInformationEntity abie = createABIE(acc, bizCtx);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(asccp, abie);

        BaseTopLevelNode topLevelNode = new BaseTopLevelNode(asbiep, asccp, abie, bizCtx);
        appendChildren(acc, abie, topLevelNode, bizCtx);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        s = System.currentTimeMillis();
        topLevelNode.accept(new BIENodeSortVisitor());
        logger.info("Node sorted - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return topLevelNode;
    }

    public BIENode createBIENode(BusinessInformationEntityUserExtensionRevision bieUserExtRevision) {
        AggregateBusinessInformationEntity eAbie = bieUserExtRevision.getExtAbie();
        long bizCtxId = eAbie.getBizCtxId();
        BusinessContext bizCtx = bizCtxRepository.findOne(bizCtxId);

        ASBIENode asbieNode = new BaseASBIENode(0, null, null, null, null, eAbie);

        AggregateCoreComponent ueAcc = bieUserExtRevision.getUserExtAcc();
        appendChildren(ueAcc, eAbie, asbieNode, bizCtx);

        double nextSeqKey = getNextSeqKey(eAbie);
        for (Node child : asbieNode.getChildren().stream()
                .filter(e -> {
                    if (e instanceof BBIENode) {
                        BasicBusinessInformationEntity bbie = ((BBIENode) e).getBbie();
                        long bccId = bbie.getBasedBccId();
                        BasicCoreComponent bcc = bccRepository.findOne(bccId);
                        return bcc.getEntityType() == Element;
                    }
                    return true;
                }).collect(Collectors.toList())) {
            if (child instanceof ASBIENode) {
                AssociationBusinessInformationEntity asbie = ((ASBIENode) child).getAsbie();
                asbie.setSeqKey(nextSeqKey);
            } else if (child instanceof BBIENode) {
                BasicBusinessInformationEntity bbie = ((BBIENode) child).getBbie();
                bbie.setSeqKey(nextSeqKey);
            }
            nextSeqKey += 1.0;
        }

        return asbieNode;
    }

    private double getNextSeqKey(AggregateBusinessInformationEntity abie) {
        long fromAbieId = abie.getAbieId();
        List<AssociationBusinessInformationEntity> asbieList = asbieRepository.findByFromAbieId(fromAbieId);
        List<BasicBusinessInformationEntity> bbieList = bbieRepository.findByFromAbieId(fromAbieId);

        double maxSeqKey = Math.max(asbieList.stream().mapToDouble(e -> e.getSeqKey()).max().orElse(0),
                bbieList.stream().mapToDouble(e -> e.getSeqKey()).max().orElse(0));
        return maxSeqKey + 1;
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

    private AssociationBusinessInformationEntity createASBIE(AggregateBusinessInformationEntity fromAbie,
                                                             AssociationBusinessInformationEntityProperty asbiep,
                                                             AssociationCoreComponent ascc, double seqKey) {
        AssociationBusinessInformationEntity asbie = new AssociationBusinessInformationEntity();
        asbie.setGuid(Utility.generateGUID());
        asbie.setFromAbie(fromAbie);
        asbie.setToAsbiep(asbiep);
        asbie.setBasedAscc(ascc);
        asbie.setCardinalityMax(ascc.getCardinalityMax());
        asbie.setCardinalityMin(ascc.getCardinalityMin());
        asbie.setDefinition(ascc.getDefinition());
        asbie.setSeqKey(seqKey);

        return asbie;
    }

    private void appendChildren(AggregateCoreComponent acc, AggregateBusinessInformationEntity abie,
                                Node parent, BusinessContext bizCtx) {
        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        accList.add(acc);
        while (acc.getBasedAccId() > 0) {
            acc = dataContainer.getACC(acc.getBasedAccId());
            accList.add(acc);
        }

        int seqKey = 1;
        while (!accList.isEmpty()) {
            acc = accList.pollLast();

            List<CoreComponent> childAssoc = queryNestedChildAssoc(acc);
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (Attribute == bcc.getEntityType()) {
                        new BBIENodeBuilder(parent, bcc, abie, 0).build();
                    }
                }
            }

            for (CoreComponent assoc : childAssoc) {
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (Element == bcc.getEntityType()) {
                        new BBIENodeBuilder(parent, bcc, abie, seqKey++).build();
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    new ASBIENodeBuilder(parent, bizCtx, ascc, abie, seqKey++).build();
                }
            }
        }
    }


    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(
                aggregateCoreComponent, dataContainer);
        return getAssocList(assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc(AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, dataContainer);
        return getAssocList(assoc);
    }

    private List<CoreComponent> getAssocList(List<CoreComponent> list) {
        Map<Integer, Boolean> hashCodes = new HashMap();

        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && dataContainer.groupcheck((AssociationCoreComponent) srt)) {
                AssociationCoreComponent ascc = (AssociationCoreComponent) srt;
                long toAsccpId = ascc.getToAsccpId();
                AssociationCoreComponentProperty toAsccp = dataContainer.getASCCP(toAsccpId);
                long roleOfAccId = toAsccp.getRoleOfAccId();
                AggregateCoreComponent roleOfAcc = dataContainer.getACC(roleOfAccId);
                list = handleNestedGroup(roleOfAcc, list, i, hashCodes);
            }
        }
        return list;
    }

    private boolean ensureCircularReference(AggregateCoreComponent acc, List<CoreComponent> coreComponents,
                                            int gPosition, Map<Integer, Boolean> hashCodes) {
        int hashCode = acc.hashCode() + coreComponents.hashCode() + gPosition;
        Boolean check = hashCodes.get(hashCode);
        if (check == null) {
            check = false;
            hashCodes.put(hashCode, true);

        }
        return check;
    }

    private List<CoreComponent> handleNestedGroup(AggregateCoreComponent acc,
                                                  List<CoreComponent> coreComponents, int gPosition,
                                                  Map<Integer, Boolean> hashCodes) {
        /*
         * TODO: FIX ME
         * As of Nov 15th, 2016, When the User Extension is in Editing state
         * Circular Reference Problem occurred in this code.
         */
        if (ensureCircularReference(acc, coreComponents, gPosition, hashCodes)) {
            coreComponents.remove(gPosition);
            return coreComponents;
        }

        List<CoreComponent> bList = queryChildAssoc(acc);
        if (!bList.isEmpty()) {
            coreComponents.addAll(gPosition, bList);
            coreComponents.remove(gPosition + bList.size());
        }

        for (int i = 0; i < coreComponents.size(); i++) {
            CoreComponent coreComponent = coreComponents.get(i);
            if (coreComponent instanceof AssociationCoreComponent &&
                    dataContainer.groupcheck((AssociationCoreComponent) coreComponent)) {

                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                long toAsccpId = ascc.getToAsccpId();
                AssociationCoreComponentProperty asccp = dataContainer.getASCCP(toAsccpId);
                long roleOfAccId = asccp.getRoleOfAccId();
                AggregateCoreComponent roleOfAcc = dataContainer.getACC(roleOfAccId);

                coreComponents = handleNestedGroup(roleOfAcc, coreComponents, i, hashCodes);
            }
        }

        return coreComponents;
    }

    private List<CoreComponent> queryChildAssoc(AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, dataContainer);
        return assoc;
    }


    private class BBIENodeBuilder {

        private Node parent;
        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public BBIENodeBuilder(Node parent,
                               BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.parent = parent;
            this.bcc = bcc;
            this.abie = abie;
            this.seqKey = seqKey;
            if (Element == bcc.getEntityType() && seqKey <= 0) {
                throw new IllegalStateException();
            }
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
        private Node parent;
        private BusinessContext bizCtx;

        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public ASBIENodeBuilder(Node parent, BusinessContext bizCtx,
                                AssociationCoreComponent ascc,
                                AggregateBusinessInformationEntity fromAbie,
                                int seqKey) {
            this.parent = parent;
            this.bizCtx = bizCtx;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;
            if (seqKey <= 0) {
                throw new IllegalStateException();
            }

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
            this.roleOfAbie = createABIE(acc, bizCtx);

            createASBIEP();
            createASBIE();

            BaseASBIENode asbieNode = new BaseASBIENode(seqKey, parent, asbie, asbiep, asccp, roleOfAbie);
            appendChildren(acc, roleOfAbie, asbieNode, bizCtx);
            return asbieNode;
        }
    }
}
