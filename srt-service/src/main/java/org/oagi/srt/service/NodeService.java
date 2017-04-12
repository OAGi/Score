package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Editing;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Service
@Transactional
public class NodeService {

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

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
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbieScRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private BusinessContextRepository businessContextRepository;

    public ACCNode createCoreComponentTreeNode(
            AggregateCoreComponent aggregateCoreComponent, boolean enableShowingGroup) {
        if (aggregateCoreComponent == null) {
            throw new IllegalArgumentException("'aggregateCoreComponent' argument must not be null.");
        }
        return new AggregateCoreComponentNodeImpl(aggregateCoreComponent, enableShowingGroup);
    }

    public BCCPNode createCoreComponentTreeNode(
            ACCNode parent,
            BasicCoreComponent basicCoreComponent, boolean enableShowingGroup) {
        if (basicCoreComponent == null) {
            throw new IllegalArgumentException("'basicCoreComponent' argument must not be null.");
        }
        return new BasicCoreComponentPropertyNodeImpl(parent, basicCoreComponent, enableShowingGroup);
    }

    public BCCPNode createCoreComponentTreeNode(
            BasicCoreComponent basicCoreComponent, boolean enableShowingGroup) {
        if (basicCoreComponent == null) {
            throw new IllegalArgumentException("'basicCoreComponent' argument must not be null.");
        }
        return new BasicCoreComponentPropertyNodeImpl(basicCoreComponent, enableShowingGroup);
    }

    public ASCCPNode createCoreComponentTreeNode(
            AssociationCoreComponent associationCoreComponent, boolean enableShowingGroup) {
        if (associationCoreComponent == null) {
            throw new IllegalArgumentException("'associationCoreComponent' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyNodeImpl(associationCoreComponent, enableShowingGroup);
    }

    public ASCCPNode createCoreComponentTreeNode(
            ACCNode parent,
            AssociationCoreComponent associationCoreComponent, boolean enableShowingGroup) {
        if (associationCoreComponent == null) {
            throw new IllegalArgumentException("'associationCoreComponent' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyNodeImpl(parent, associationCoreComponent, enableShowingGroup);
    }

    public ASCCPNode createCoreComponentTreeNode(
            AssociationCoreComponentProperty associationCoreComponentProperty, boolean enableShowingGroup) {
        if (associationCoreComponentProperty == null) {
            throw new IllegalArgumentException("'associationCoreComponentProperty' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyNodeImpl(associationCoreComponentProperty, enableShowingGroup);
    }

    public ASCCPNode createCoreComponentTreeNode(
            ACCNode parent,
            AssociationCoreComponentProperty associationCoreComponentProperty, boolean enableShowingGroup) {
        if (associationCoreComponentProperty == null) {
            throw new IllegalArgumentException("'associationCoreComponentProperty' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyNodeImpl(parent, associationCoreComponentProperty, enableShowingGroup);
    }

    public BCCPNode createCoreComponentTreeNode(
            BasicCoreComponentProperty basicCoreComponentProperty, boolean enableShowingGroup) {
        if (basicCoreComponentProperty == null) {
            throw new IllegalArgumentException("'basicCoreComponentProperty' argument must not be null.");
        }
        return new BasicCoreComponentPropertyNodeImpl(basicCoreComponentProperty, enableShowingGroup);
    }

    public BCCPNode createCoreComponentTreeNode(
            ACCNode parent,
            BasicCoreComponentProperty basicCoreComponentProperty, boolean enableShowingGroup) {
        if (basicCoreComponentProperty == null) {
            throw new IllegalArgumentException("'basicCoreComponentProperty' argument must not be null.");
        }
        return new BasicCoreComponentPropertyNodeImpl(parent, basicCoreComponentProperty, enableShowingGroup);
    }

    private abstract class AbstractSRTNode implements SRTNode {

        private Map<String, Object> attributes = new HashMap();

        @Override
        public void setAttribute(String key, Object attr) {
            attributes.put(key, attr);
        }

        @Override
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
    }

    private class AggregateCoreComponentNodeImpl
            extends AbstractSRTNode
            implements ACCNode {

        private final AggregateCoreComponent acc;
        private boolean enableShowingGroup;
        private ACCNode base;

        private List<CoreComponentRelation> associations = null;
        private Collection<CCNode> children = null;

        private AggregateCoreComponentNodeImpl(AggregateCoreComponent aggregateCoreComponent, boolean enableShowingGroup) {
            this.acc = aggregateCoreComponent;
            this.enableShowingGroup = enableShowingGroup;
        }

        @Override
        public AggregateCoreComponent getAcc() {
            return acc;
        }

        @Override
        public ACCNode getBase() {
            if (base == null) {
                AggregateCoreComponent acc = getAcc();
                long basedAccId = acc.getBasedAccId();
                if (basedAccId > 0L) {
                    AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
                    base = new AggregateCoreComponentNodeImpl(basedAcc, enableShowingGroup);
                }
            }
            return base;
        }

        @Override
        public String getId() {
            return getAcc().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = getAcc().getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
        }

        @Override
        public void setNamespace(Namespace namespace) {
            if (namespace != null) {
                getAcc().setNamespaceId(namespace.getNamespaceId());
            }
        }

        @Override
        public void validate() {
        }

        private List<CoreComponentRelation> associations() {
            if (associations == null) {
                associations = getAssociations(acc, enableShowingGroup);
            }
            return associations;
        }

        @Override
        public boolean hasChild() {
            return associations().isEmpty() ? false : true;
        }

        @Override
        public Collection<? extends CCNode> getChildren() {
            if (children == null) {
                if (associations().isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();

                    for (CoreComponentRelation association : associations()) {
                        if (association instanceof AssociationCoreComponent) {
                            ASCCPNode asccpNode =
                                    createCoreComponentTreeNode(this, (AssociationCoreComponent) association, enableShowingGroup);
                            children.add(asccpNode);
                        } else if (association instanceof BasicCoreComponent) {
                            BCCPNode bccpNode =
                                    createCoreComponentTreeNode(this, (BasicCoreComponent) association, enableShowingGroup);
                            children.add(bccpNode);
                        }
                    }
                }
            }

            return children;
        }

        @Override
        public void reload() {
            associations = null;
            children = null;
        }
    }

    private List<CoreComponentRelation> getAssociations(AggregateCoreComponent acc, boolean enableShowingGroup) {
        return getAssociations(acc, null, enableShowingGroup);
    }

    private List<CoreComponentRelation> getAssociations(AggregateCoreComponent acc, CoreComponentState state) {
        return getAssociations(acc, state, false);
    }

    private List<CoreComponentRelation> getAssociations(AggregateCoreComponent acc, CoreComponentState state, boolean enableShowingGroup) {
        List<CoreComponentRelation> associationsWithoutRecursive = getAssociationsWithoutRecursive(acc, state);
        List<CoreComponentRelation> associations = new ArrayList();

        for (int i = 0, len = associationsWithoutRecursive.size(); i < len; ++i) {
            CoreComponentRelation relation = associationsWithoutRecursive.get(i);
            if (relation instanceof AssociationCoreComponent) {
                AssociationCoreComponent ascc = (AssociationCoreComponent) relation;
                AggregateCoreComponent roleOfAcc = getRoleOfAcc(ascc);
                if (isGroup(roleOfAcc)) {
                    if (enableShowingGroup) {
                        associations.add(ascc);
                    } else {
                        associations.addAll(getAssociations(roleOfAcc, state));
                    }
                } else {
                    associations.add(ascc);
                }
            } else {
                associations.add(relation);
            }
        }

        return associations;
    }

    private List<CoreComponentRelation> getAssociationsWithoutRecursive(AggregateCoreComponent acc, CoreComponentState state) {
        long accId = acc.getAccId();

        List<CoreComponentRelation> coreComponentRelations = new ArrayList();
        if (state != null) {
            List<AssociationCoreComponent> asccList =
                    asccRepository.findByFromAccIdAndRevisionNumAndState(accId, 0, state);
            coreComponentRelations.addAll(asccList);
            List<BasicCoreComponent> bccList =
                    bccRepository.findByFromAccIdAndRevisionNumAndState(accId, 0, state);
            coreComponentRelations.addAll(bccList);
            Collections.sort(coreComponentRelations, comparingCoreComponentRelation());

            return coreComponentRelations;
        } else {
            List<AssociationCoreComponent> asccList =
                    asccRepository.findByFromAccIdAndRevisionNum(accId, 0);
            coreComponentRelations.addAll(asccList);
            List<BasicCoreComponent> bccList =
                    bccRepository.findByFromAccIdAndRevisionNum(accId, 0);
            coreComponentRelations.addAll(bccList);
            Collections.sort(coreComponentRelations, comparingCoreComponentRelation());

            return coreComponentRelations;
        }
    }

    private AggregateCoreComponent getRoleOfAcc(AssociationCoreComponent associationCoreComponent) {
        long toAsccpId = associationCoreComponent.getToAsccpId();
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(toAsccpId);
        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
        return acc;
    }

    public boolean isGroup(AggregateCoreComponent acc) {
        OagisComponentType oagisComponentType = acc.getOagisComponentType();
        return (oagisComponentType == SemanticGroup || oagisComponentType == UserExtensionGroup) ? true : false;
    }

    private class AssociationCoreComponentPropertyNodeImpl
            extends AbstractSRTNode
            implements ASCCPNode {

        private ACCNode parent = null;
        private AssociationCoreComponent ascc;
        private boolean enableShowingGroup;

        private final AssociationCoreComponentProperty asccp;
        private ACCNode type;

        private Boolean hasChild = null;
        private Collection<CCNode> children = null;

        private AssociationCoreComponentPropertyNodeImpl(AssociationCoreComponent ascc, boolean enableShowingGroup) {
            this(null, ascc, enableShowingGroup);
        }

        private AssociationCoreComponentPropertyNodeImpl(AssociationCoreComponentProperty asccp, boolean enableShowingGroup) {
            this(null, asccp, enableShowingGroup);
        }

        private AssociationCoreComponentPropertyNodeImpl(ACCNode parent,
                                                         AssociationCoreComponent ascc,
                                                         boolean enableShowingGroup) {
            this.parent = parent;
            this.ascc = ascc;
            this.enableShowingGroup = enableShowingGroup;

            long asccpId = ascc.getToAsccpId();
            this.asccp = asccpRepository.findOne(asccpId);
        }

        private AssociationCoreComponentPropertyNodeImpl(ACCNode parent,
                                                         AssociationCoreComponentProperty asccp,
                                                         boolean enableShowingGroup) {
            this.parent = parent;
            this.asccp = asccp;
            this.enableShowingGroup = enableShowingGroup;
        }

        @Override
        public AssociationCoreComponent getAscc() {
            return ascc;
        }

        @Override
        public AssociationCoreComponentProperty getAsccp() {
            return asccp;
        }

        @Override
        public ACCNode getType() {
            if (type == null) {
                long roleOfAccId = getRoleOfAccId();
                if (roleOfAccId > 0L) {
                    AggregateCoreComponent roleOfAcc = accRepository.findOne(roleOfAccId);
                    type = new AggregateCoreComponentNodeImpl(roleOfAcc, enableShowingGroup);
                }
            }
            return type;
        }

        @Override
        public String getId() {
            return getAsccp().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = getAsccp().getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
        }

        @Override
        public void setNamespace(Namespace namespace) {
            if (namespace != null) {
                getAsccp().setNamespaceId(namespace.getNamespaceId());
            }
        }

        @Override
        public void validate() {
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                long roleOfAccId = getRoleOfAccId();
                hasChild = (roleOfAccId > 0L);
            }
            return hasChild;
        }

        @Override
        public Collection<? extends CCNode> getChildren() {
            if (children == null) {
                ACCNode type = getType();
                if (type != null) {
                    children = new ArrayList();
                    children.add(type);
                } else {
                    children = Collections.emptyList();
                }
            }
            return children;
        }

        private long getRoleOfAccId() {
            long roleOfAccId = getAsccp().getRoleOfAccId();
            return roleOfAccId;
        }

        @Override
        public ACCNode getParent() {
            if (parent == null) {
                long fromAccId = getAscc().getFromAccId();
                AggregateCoreComponent fromAcc = accRepository.findOne(fromAccId);
                parent = new AggregateCoreComponentNodeImpl(fromAcc, enableShowingGroup);
            }
            return parent;
        }

        @Override
        public void reload() {
            parent = null;
            hasChild = null;
            children = null;
        }
    }

    private class BasicCoreComponentPropertyNodeImpl
            extends AbstractSRTNode
            implements BCCPNode {

        private ACCNode parent = null;
        private BasicCoreComponent bcc;
        private boolean enableShowingGroup;

        private final BasicCoreComponentProperty bccp;
        private DataType dataType;

        private Collection<BDTSCNode> children = null;

        private BasicCoreComponentPropertyNodeImpl(BasicCoreComponent bcc, boolean enableShowingGroup) {
            this(null, bcc, enableShowingGroup);
        }

        private BasicCoreComponentPropertyNodeImpl(BasicCoreComponentProperty bccp, boolean enableShowingGroup) {
            this(null, bccp, enableShowingGroup);
        }

        private BasicCoreComponentPropertyNodeImpl(ACCNode parent,
                                                   BasicCoreComponent bcc, boolean enableShowingGroup) {
            this.parent = parent;
            this.bcc = bcc;
            this.enableShowingGroup = enableShowingGroup;

            long bccpId = bcc.getToBccpId();
            this.bccp = bccpRepository.findOne(bccpId);
        }

        private BasicCoreComponentPropertyNodeImpl(ACCNode parent,
                                                   BasicCoreComponentProperty bccp, boolean enableShowingGroup) {
            this.parent = parent;
            this.bccp = bccp;
            this.enableShowingGroup = enableShowingGroup;
        }

        @Override
        public BasicCoreComponent getBcc() {
            return bcc;
        }

        @Override
        public BasicCoreComponentProperty getBccp() {
            return bccp;
        }

        @Override
        public DataType getBdt() {
            if (dataType == null) {
                long bdtId = getBccp().getBdtId();
                dataType = dtRepository.findOne(bdtId);
            }
            return dataType;
        }

        @Override
        public String getId() {
            return getBccp().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = getBccp().getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
        }

        @Override
        public void setNamespace(Namespace namespace) {
            if (namespace != null) {
                getBccp().setNamespaceId(namespace.getNamespaceId());
            }
        }

        @Override
        public void validate() {
        }

        @Override
        public boolean hasChild() {
            return !getChildren().isEmpty();
        }

        @Override
        public Collection<? extends CCNode> getChildren() {
            if (children == null) {
                long bdtId = getBdt().getDtId();
                List<DataTypeSupplementaryComponent> bdtScList = dtScRepository.findByOwnerDtId(bdtId).stream()
                        .filter(e -> e.getCardinalityMin() != 0 || e.getCardinalityMax() != 0)
                        .collect(Collectors.toList());

                if (bdtScList.isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();
                    for (DataTypeSupplementaryComponent bdtSc : bdtScList) {
                        BDTSCNode child = new BusinessDataTypeSupplementaryComponentNodeImpl(this, bdtSc);
                        children.add(child);
                    }
                }
            }
            return children;
        }

        @Override
        public ACCNode getParent() {
            if (parent == null) {
                long fromAccId = getBcc().getFromAccId();
                AggregateCoreComponent fromAcc = accRepository.findOne(fromAccId);
                parent = new AggregateCoreComponentNodeImpl(fromAcc, enableShowingGroup);
            }
            return parent;
        }

        @Override
        public void reload() {
            parent = null;
            children = null;
        }
    }

    private class BusinessDataTypeSupplementaryComponentNodeImpl
            extends AbstractSRTNode
            implements BDTSCNode {

        private BCCPNode parent;
        private DataTypeSupplementaryComponent dtSc;

        public BusinessDataTypeSupplementaryComponentNodeImpl(BCCPNode parent,
                                                              DataTypeSupplementaryComponent dtSc) {
            this.parent = parent;
            this.dtSc = dtSc;
        }

        @Override
        public BCCPNode getParent() {
            return parent;
        }

        @Override
        public DataTypeSupplementaryComponent getBdtSc() {
            return dtSc;
        }

        @Override
        public String getId() {
            return getBdtSc().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            return (parent != null) ? parent.getNamespace() : null;
        }

        @Override
        public void setNamespace(Namespace namespace) {
            if (parent != null) {
                parent.setNamespace(namespace);
            }
        }

        @Override
        public void validate() {
        }

        @Override
        public boolean hasChild() {
            return false;
        }

        @Override
        public Collection<? extends CCNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void reload() {
        }
    }

    public static <T extends CoreComponentRelation> Comparator<T> comparingCoreComponentRelation() {
        return (Comparator<T> & Serializable)
                (a, b) -> {
                    int ask = a.getSeqKey();
                    int bsk = b.getSeqKey();
                    if (ask == 0 && bsk == 0) {
                        return a.getCreationTimestamp().compareTo(b.getCreationTimestamp());
                    } else {
                        return ask - bsk;
                    }
                };
    }

    public ASBIEPNode createBusinessInformationEntityTreeNode(AssociationCoreComponentProperty asccp,
                                                              BusinessContext bizCtx) {
        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }
        if (bizCtx == null) {
            throw new IllegalArgumentException("'bizCtx' argument must not be null.");
        }

        return new AssociationBIEPropertyNodeImpl(asccp, bizCtx);
    }

    public ASBIEPNode createBusinessInformationEntityTreeNode(
            TopLevelAbie topLevelAbie) {

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        BusinessContext bizCtx = businessContextRepository.findOne(abie.getBizCtxId());
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
        AggregateCoreComponent acc = accRepository.findOne(abie.getBasedAccId());

        ABIENodeImpl abieNode =
                new ABIENodeImpl(abie, acc, bizCtx);

        return new AssociationBIEPropertyNodeImpl(asbiep, asccp, abieNode);
    }

    private class ABIENodeImpl
            extends AbstractSRTNode
            implements ABIENode {

        private ASBIEPNode parent;

        private AggregateBusinessInformationEntity abie;
        private AggregateCoreComponent basedAcc;
        private BusinessContext bizCtx;

        private Boolean hasChild = null;
        private List<BIENode> children = null;

        public ABIENodeImpl(AggregateCoreComponent acc, BusinessContext bizCtx) {
            this.abie = createABIE(acc, bizCtx);
            this.basedAcc = acc;
            this.bizCtx = bizCtx;
        }

        public ABIENodeImpl(AggregateBusinessInformationEntity abie,
                            AggregateCoreComponent acc, BusinessContext bizCtx) {
            this.abie = abie;
            this.basedAcc = acc;
            this.bizCtx = bizCtx;
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
            abie.afterLoaded();

            return abie;
        }

        public ASBIEPNode getParent() {
            return parent;
        }

        private void setParent(ASBIEPNode parent) {
            this.parent = parent;
        }

        @Override
        public String getId() {
            return abie.getGuid();
        }

        @Override
        public AggregateBusinessInformationEntity getAbie() {
            return abie;
        }

        @Override
        public AggregateCoreComponent getAcc() {
            return basedAcc;
        }

        @Override
        public BusinessContext getBusinessContext() {
            return bizCtx;
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                AggregateCoreComponent acc = this.basedAcc;
                while (acc != null) {
                    long accId = acc.getAccId();
                    int asccCount = asccRepository.countByFromAccIdAndRevisionNumAndState(
                            accId, 0, CoreComponentState.Published);
                    if (asccCount > 0) {
                        hasChild = true;
                        break;
                    } else {
                        int bccCount = bccRepository.countByFromAccIdAndRevisionNumAndState(
                                accId, 0, CoreComponentState.Published);
                        if (bccCount > 0) {
                            hasChild = true;
                            break;
                        } else {
                            long basedAccId = acc.getBasedAccId();
                            acc = accRepository.findOneByAccIdAndRevisionNumAndState(basedAccId, 0, Published);
                        }
                    }
                }
                if (hasChild == null) {
                    hasChild = false;
                }
            }
            return hasChild;
        }

        @Override
        public Collection<? extends BIENode> getChildren() {
            if (children == null) {
                children = new ArrayList();

                LinkedList<AggregateCoreComponent> accList = new LinkedList();
                AggregateCoreComponent acc = this.basedAcc;
                while (acc != null) {
                    accList.add(acc);
                    acc = accRepository.findOneByAccIdAndRevisionNumAndState(acc.getBasedAccId(), 0, Published);
                }

                AggregateBusinessInformationEntity abie = getAbie();
                int seqKey = 0;
                while (!accList.isEmpty()) {
                    acc = accList.pollLast();

                    List<CoreComponentRelation> associations = getAssociations(acc, CoreComponentState.Published);
                    for (CoreComponentRelation relation : associations) {
                        if (relation instanceof AssociationCoreComponent) {
                            AssociationCoreComponent ascc = (AssociationCoreComponent) relation;
                            AssociationBIEPropertyNodeImpl asbieChild =
                                    new AssociationBIEPropertyNodeImpl(abie, ascc, bizCtx, ++seqKey);
                            asbieChild.setParent(parent);

                            children.add(asbieChild);
                        } else if (relation instanceof BasicCoreComponent) {
                            BasicCoreComponent bcc = (BasicCoreComponent) relation;

                            BasicBIEPropertyNodeImpl bbieChild =
                                    new BasicBIEPropertyNodeImpl(
                                            abie, bcc, (bcc.getEntityType() == Attribute ? 0 : ++seqKey));
                            bbieChild.setParent(parent);

                            children.add(bbieChild);
                        }
                    }
                }

                if (children.isEmpty()) {
                    children = Collections.emptyList();
                }
            }

            return children;
        }

        @Override
        public void accept(BIENodeVisitor visitor) {
            visitor.visit(this);
            if (children != null) {
                for (BIENode child : children) {
                    child.accept(visitor);
                }
            }
        }

        @Override
        public void validate() {
        }

        @Override
        public void reload() {
            hasChild = null;
            children = null;
        }

        @Override
        public boolean isUsed() {
            return true;
        }

        @Override
        public void setUsed(boolean used) {
        }
    }

    private class AssociationBIEPropertyNodeImpl
            extends AbstractSRTNode
            implements ASBIEPNode {

        private ASBIEPNode parent;

        private AssociationBusinessInformationEntity asbie;
        private AssociationCoreComponent ascc;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationCoreComponentProperty asccp;

        private ABIENodeImpl type;

        public AssociationBIEPropertyNodeImpl(AssociationCoreComponentProperty asccp,
                                              BusinessContext bizCtx) {
            this.asccp = asccp;

            long roleOfAccId = asccp.getRoleOfAccId();
            AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
            type = new ABIENodeImpl(acc, bizCtx);
            type.setParent(this);

            AggregateBusinessInformationEntity roleOfAbie = type.getAbie();
            asbiep = createASBIEP(asccp, roleOfAbie);
        }

        public AssociationBIEPropertyNodeImpl(AssociationBusinessInformationEntityProperty asbiep,
                                              AssociationCoreComponentProperty asccp,
                                              ABIENodeImpl type) {
            this.asbiep = asbiep;
            this.asccp = asccp;
            this.type = type;

            type.setParent(this);
        }

        public AssociationBIEPropertyNodeImpl(AggregateBusinessInformationEntity fromAbie,
                                              AssociationCoreComponent ascc,
                                              BusinessContext bizCtx,
                                              int seqKey) {
            this.ascc = ascc;

            long abieId = fromAbie.getAbieId();
            if (abieId > 0L) {
                long topLevelAbieId = fromAbie.getOwnerTopLevelAbieId();
                long basedAsccId = ascc.getAsccId();
                asbie = asbieRepository.findOneByBasedAsccIdAndFromAbieIdAndOwnerTopLevelAbieId(basedAsccId, abieId, topLevelAbieId);
            }

            if (asbie != null) {
                asbiep = asbiepRepository.findOne(asbie.getToAsbiepId());
                asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());

                AggregateBusinessInformationEntity roleOfAbie = abieRepository.findOne(asbiep.getRoleOfAbieId());
                long roleOfAccId = asccp.getRoleOfAccId();
                AggregateCoreComponent basedAcc = accRepository.findOneByAccIdAndRevisionNumAndState(roleOfAccId, 0, Published);

                type = new ABIENodeImpl(roleOfAbie, basedAcc, bizCtx);

            } else {
                long toAsccpId = ascc.getToAsccpId();
                asccp = asccpRepository.findOneByAsccpIdAndRevisionNumAndState(toAsccpId, 0, Published);
                long roleOfAccId = asccp.getRoleOfAccId();
                AggregateCoreComponent basedAcc = accRepository.findOneByAccIdAndRevisionNumAndState(roleOfAccId, 0, Published);

                type = new ABIENodeImpl(basedAcc, bizCtx);
                AggregateBusinessInformationEntity roleOfAbie = type.getAbie();
                asbiep = createASBIEP(asccp, roleOfAbie);
                asbie = createASBIE(fromAbie, asbiep, ascc, seqKey);
            }

            type.setParent(this);
        }

        @Override
        public ASBIEPNode getParent() {
            return parent;
        }

        private void setParent(ASBIEPNode parent) {
            this.parent = parent;
        }

        @Override
        public String getId() {
            return asbiep.getGuid();
        }

        @Override
        public AssociationBusinessInformationEntity getAsbie() {
            return asbie;
        }

        @Override
        public AssociationCoreComponent getAscc() {
            return ascc;
        }

        @Override
        public AssociationBusinessInformationEntityProperty getAsbiep() {
            return asbiep;
        }

        @Override
        public AssociationCoreComponentProperty getAsccp() {
            return asccp;
        }

        @Override
        public ABIENode getType() {
            return type;
        }

        @Override
        public boolean hasChild() {
            return type.hasChild();
        }

        @Override
        public Collection<? extends BIENode> getChildren() {
            return type.getChildren();
        }

        @Override
        public void accept(BIENodeVisitor visitor) {
            visitor.visit(this);
            type.accept(visitor);
        }

        @Override
        public void validate() {
            AssociationBusinessInformationEntity asbie = getAsbie();
            if (asbie == null || !asbie.isDirty()) {
                return;
            }

            int originalCardinalityMin = ascc.getCardinalityMin();
            int originalCardinalityMax = ascc.getCardinalityMax();

            int cardinalityMin = asbie.getCardinalityMin();
            int cardinalityMax = asbie.getCardinalityMax();

            ensureCardinalityMin(originalCardinalityMin, cardinalityMin, cardinalityMax);
            ensureCardinalityMax(originalCardinalityMax, cardinalityMin, cardinalityMax);
        }

        @Override
        public void reload() {
            type.reload();
        }

        public AssociationBusinessInformationEntity createASBIE(AggregateBusinessInformationEntity fromAbie,
                                                                AssociationBusinessInformationEntityProperty asbiep,
                                                                AssociationCoreComponent ascc, int seqKey) {
            AssociationBusinessInformationEntity asbie = new AssociationBusinessInformationEntity();
            asbie.setGuid(Utility.generateGUID());
            asbie.setFromAbie(fromAbie);
            asbie.setToAsbiep(asbiep);
            asbie.setBasedAsccId(ascc.getAsccId());
            asbie.setCardinalityMax(ascc.getCardinalityMax());
            asbie.setCardinalityMin(ascc.getCardinalityMin());
            asbie.setDefinition(ascc.getDefinition());
            asbie.setSeqKey(seqKey);
            asbie.afterLoaded();
            return asbie;
        }

        public AssociationBusinessInformationEntityProperty createASBIEP(AssociationCoreComponentProperty asccp,
                                                                         AggregateBusinessInformationEntity roleOfAbie) {
            AssociationBusinessInformationEntityProperty asbiep = new AssociationBusinessInformationEntityProperty();
            asbiep.setGuid(Utility.generateGUID());
            asbiep.setBasedAsccp(asccp);
            asbiep.setRoleOfAbie(roleOfAbie);
            asbiep.setDefinition(asccp.getDefinition());
            asbiep.afterLoaded();
            return asbiep;
        }

        @Override
        public boolean isUsed() {
            AssociationBusinessInformationEntity asbie = getAsbie();
            return (asbie != null) ? asbie.isUsed() : true;
        }

        @Override
        public void setUsed(boolean used) {
            AssociationBusinessInformationEntity asbie = getAsbie();
            if (asbie != null) {
                asbie.setUsed(used);
            }

            if (used) {
                if (parent != null && !parent.isUsed()) {
                    parent.setUsed(used);
                }
            } else {
                if (type.children != null) {
                    for (BIENode node : getChildren()) {
                        node.setUsed(used);
                    }
                }
            }
        }
    }

    private class BasicBIEPropertyNodeImpl
            extends AbstractSRTNode
            implements BBIEPNode {

        private ASBIEPNode parent;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicCoreComponentProperty bccp;

        private BasicBusinessInformationEntity bbie;
        private BasicCoreComponent bcc;

        private DataType bdt;

        private Boolean hasChild = null;
        private List<BIENode> children = null;

        public BasicBIEPropertyNodeImpl(AggregateBusinessInformationEntity fromAbie,
                                        BasicCoreComponent bcc,
                                        int seqKey) {
            this.bcc = bcc;

            long abieId = fromAbie.getAbieId();
            if (abieId > 0L) {
                long bccId = bcc.getBccId();
                long topLevelAbieId = fromAbie.getOwnerTopLevelAbieId();

                bbie = bbieRepository.findOneByBasedBccIdAndFromAbieIdAndOwnerTopLevelAbieId(bccId, abieId, topLevelAbieId);
                if (bbie != null) {
                    long toBbiepId = bbie.getToBbiepId();
                    bbiep = bbiepRepository.findOne(toBbiepId);
                    bccp = bccpRepository.findOne(bbiep.getBasedBccpId());
                }
            }

            if (bbie == null) {
                long toBccpId = bcc.getToBccpId();
                bccp = bccpRepository.findOneByBccpIdAndRevisionNumAndState(toBccpId, 0, Published);
                bbiep = createBBIEP(bccp);
            }

            long bdtId = bccp.getBdtId();
            bdt = dtRepository.findOne(bdtId);
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    bdtPriRestriRepository.findOneByBdtIdAndCodeListIdIsNotZero(bdtId);
            long codeListId = (bdtPriRestri != null) ? bdtPriRestri.getCodeListId() : 0L;
            bdtPriRestri = bdtPriRestriRepository.findOneByBdtIdAndDefault(bdtId, true);

            if (bbie == null) {
                bbie = createBBIE(bcc, fromAbie, bbiep, bdtPriRestri, codeListId, seqKey);
            }
        }

        private void setParent(ASBIEPNode parent) {
            this.parent = parent;
        }

        @Override
        public ASBIEPNode getParent() {
            return parent;
        }

        @Override
        public String getId() {
            return bbie.getGuid();
        }

        @Override
        public BasicBusinessInformationEntity getBbie() {
            return bbie;
        }

        @Override
        public BasicCoreComponent getBcc() {
            return bcc;
        }

        @Override
        public BasicBusinessInformationEntityProperty getBbiep() {
            return bbiep;
        }

        @Override
        public BasicCoreComponentProperty getBccp() {
            return bccp;
        }

        @Override
        public DataType getBdt() {
            return bdt;
        }

        @Override
        public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
            return getBbie().getRestrictionType();
        }

        @Override
        public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
            getBbie().setRestrictionType(restrictionType);
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                long bdtId = bdt.getDtId();
                hasChild = dtScRepository.findByOwnerDtId(bdtId).stream().filter(e -> e.getCardinalityMax() != 0).count() > 0;
            }
            return hasChild;
        }

        @Override
        public Collection<? extends BIENode> getChildren() {
            if (children == null) {

                long bdtId = bdt.getDtId();

                List<DataTypeSupplementaryComponent> dtScList =
                        dtScRepository.findByOwnerDtId(bdtId)
                                .stream()
                                .filter(dtSc -> dtSc.getCardinalityMax() != 0)
                                .collect(Collectors.toList());

                long bbieId = bbie.getBbieId();
                long ownerTopLevelAbieId = bbie.getOwnerTopLevelAbieId();

                children = new ArrayList();
                for (DataTypeSupplementaryComponent dtSc : dtScList) {
                    BasicBusinessInformationEntitySupplementaryComponent bbieSc = null;
                    if (bbieId > 0L) {
                        long dtScId = dtSc.getDtScId();
                        bbieSc = bbieScRepository.findOneByBbieIdAndDtScIdAndOwnerTopLevelAbieId(bbieId, dtScId, ownerTopLevelAbieId);
                    }

                    if (bbieSc == null) {
                        bbieSc = new BasicBusinessInformationEntitySupplementaryComponent();
                        long bdtScId = dtSc.getDtScId();
                        bbieSc.setBbie(bbie);
                        bbieSc.setDtSc(dtSc);
                        bbieSc.setGuid(Utility.generateGUID());

                        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                                bdtScPriRestriRepository.findOneByBdtScIdAndDefault(bdtScId, true);
                        if (bdtScPriRestri != null) {
                            bbieSc.setDtScPriRestri(bdtScPriRestri);
                        }

//                    long codeListId = dataContainer.getCodeListIdOfBdtScPriRestriId(bdtScId);
//                    if (codeListId > 0) {
//                        bbieSc.setCodeListId(codeListId);
//                    }

                        bbieSc.setCardinalityMax(dtSc.getCardinalityMax());
                        bbieSc.setCardinalityMin(dtSc.getCardinalityMin());
                        bbieSc.setDefinition(dtSc.getDefinition());
                        bbieSc.afterLoaded();
                    }

                    long dtScId = bbieSc.getDtScId();
                    DataTypeSupplementaryComponent bdtSc = dtScRepository.findOne(dtScId);

                    BBIESCNode bbieScNode =
                            new BasicBIESupplementaryComponentNodeImpl(this, bbieSc, bdtSc);
                    children.add(bbieScNode);
                }
            }
            return children;
        }

        @Override
        public void accept(BIENodeVisitor visitor) {
            visitor.visit(this);
            if (children != null) {
                for (BIENode child : children) {
                    child.accept(visitor);
                }
            }
        }

        @Override
        public void validate() {
            BasicBusinessInformationEntity bbie = getBbie();
            if (bbie == null || !bbie.isDirty()) {
                return;
            }

            int originalCardinalityMin = bcc.getCardinalityMin();
            int originalCardinalityMax = bcc.getCardinalityMax();

            int cardinalityMin = bbie.getCardinalityMin();
            int cardinalityMax = bbie.getCardinalityMax();

            ensureCardinalityMin(originalCardinalityMin, cardinalityMin, cardinalityMax);
            ensureCardinalityMax(originalCardinalityMax, cardinalityMin, cardinalityMax);
        }

        @Override
        public void reload() {
        }

        private BasicBusinessInformationEntityProperty createBBIEP(BasicCoreComponentProperty bccp) {
            BasicBusinessInformationEntityProperty bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccp(bccp);
            bbiep.setDefinition(bccp.getDefinition());
            bbiep.afterLoaded();
            return bbiep;
        }

        private BasicBusinessInformationEntity createBBIE(BasicCoreComponent bcc,
                                                          AggregateBusinessInformationEntity abie,
                                                          BasicBusinessInformationEntityProperty bbiep,
                                                          BusinessDataTypePrimitiveRestriction bdtPriRestri,
                                                          long codeListId,
                                                          int seqKey) {
            BasicBusinessInformationEntity bbie = new BasicBusinessInformationEntity();
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
            bbie.setDefinition(bcc.getDefinition());
            bbie.afterLoaded();
            return bbie;
        }

        @Override
        public boolean isUsed() {
            return bbie.isUsed();
        }

        @Override
        public void setUsed(boolean used) {
            bbie.setUsed(used);

            if (used) {
                if (parent != null && !parent.isUsed()) {
                    parent.setUsed(used);
                }
            } else {
                if (children != null) {
                    for (BIENode node : getChildren()) {
                        node.setUsed(used);
                    }
                }
            }
        }
    }

    private class BasicBIESupplementaryComponentNodeImpl
            extends AbstractSRTNode
            implements BBIESCNode {

        private BBIEPNode parent;
        private BasicBusinessInformationEntitySupplementaryComponent bbieSc;
        private DataTypeSupplementaryComponent bdtSc;

        public BasicBIESupplementaryComponentNodeImpl(BBIEPNode parent,
                                                      BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                                      DataTypeSupplementaryComponent bdtSc) {
            this.parent = parent;
            this.bbieSc = bbieSc;
            this.bdtSc = bdtSc;
        }

        @Override
        public BBIEPNode getParent() {
            return parent;
        }

        @Override
        public BasicBusinessInformationEntitySupplementaryComponent getBbieSc() {
            return bbieSc;
        }

        @Override
        public DataTypeSupplementaryComponent getBdtSc() {
            return bdtSc;
        }

        @Override
        public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
            return getBbieSc().getRestrictionType();
        }

        @Override
        public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
            getBbieSc().setRestrictionType(restrictionType);
        }

        @Override
        public String getId() {
            return bbieSc.getGuid();
        }

        @Override
        public boolean hasChild() {
            return false;
        }

        @Override
        public Collection<? extends BIENode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void accept(BIENodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public void validate() {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                    getBbieSc();
            if (bbieSc == null || !bbieSc.isDirty()) {
                return;
            }

            int originalCardinalityMin = bdtSc.getCardinalityMin();
            int originalCardinalityMax = bdtSc.getCardinalityMax();

            int cardinalityMin = bbieSc.getCardinalityMin();
            int cardinalityMax = bbieSc.getCardinalityMax();

            ensureCardinalityMin(originalCardinalityMin, cardinalityMin, cardinalityMax);
            ensureCardinalityMax(originalCardinalityMax, cardinalityMin, cardinalityMax);
        }

        @Override
        public void reload() {
        }

        @Override
        public boolean isUsed() {
            return bbieSc.isUsed();
        }

        @Override
        public void setUsed(boolean used) {
            bbieSc.setUsed(used);

            if (used) {
                if (parent != null && !parent.isUsed()) {
                    parent.setUsed(used);
                }
            }
        }
    }

    private void ensureCardinalityMin(int originalCardinalityMin, int cardinalityMin, int cardinalityMax) {
        if (cardinalityMax == -1) {
            cardinalityMax = Integer.MAX_VALUE;
        }

        if (cardinalityMin < originalCardinalityMin) {
            throw new IllegalStateException("'Min' must be greater than or equals to " + originalCardinalityMin + ".");
        }
        if (cardinalityMin > cardinalityMax) {
            throw new IllegalStateException("'Min' must be less than or equals to " + cardinalityMax + ".");
        }
    }

    private void ensureCardinalityMax(int originalCardinalityMax, int cardinalityMin, int cardinalityMax) {
        if (cardinalityMax == -1) {
            cardinalityMax = Integer.MAX_VALUE;
        }
        if (originalCardinalityMax == -1) {
            originalCardinalityMax = Integer.MAX_VALUE;
        }

        if (cardinalityMax > originalCardinalityMax) {
            throw new IllegalStateException("'Max' must be less than or equals to " + originalCardinalityMax + ".");
        }
        if (cardinalityMax < cardinalityMin) {
            throw new IllegalStateException("'Max' must be greater than or equals to " + cardinalityMin + ".");
        }
    }

    @Transactional
    public TopLevelAbie submit(ASBIEPNode bieNode,
                               User user, ProgressListener progressListener) {
        Boolean isTopLevel = (Boolean) bieNode.getAttribute("isTopLevel");
        if (isTopLevel == null || isTopLevel == false) {
            throw new IllegalArgumentException();
        }

        BusinessInformationEntityTreeNodeSubmitHandler submitHandler =
                new BusinessInformationEntityTreeNodeSubmitHandler(bieNode, user);
        submitHandler.setProgressListener(progressListener);
        TopLevelAbie topLevelAbie = submitHandler.submit();
        return topLevelAbie;
    }

    public static class ProgressListener implements PersistEventListener {
        private int maxCount = 0;
        private AtomicInteger currentCount = new AtomicInteger();
        private String status = "Initializing";

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void onPrePersist(Object object) {
        }

        @Override
        public void onPostPersist(Object object) {
//            if (object instanceof AggregateBusinessInformationEntity) {
//                setProgressStatus("Updating ABIE");
//            } else if (object instanceof AssociationBusinessInformationEntity) {
//                setProgressStatus("Updating ASBIE");
//            } else if (object instanceof AssociationBusinessInformationEntityProperty) {
//                setProgressStatus("Updating ASBIEP");
//            } else if (object instanceof BasicBusinessInformationEntity) {
//                setProgressStatus("Updating BBIE");
//            } else if (object instanceof BasicBusinessInformationEntityProperty) {
//                setProgressStatus("Updating BBIEP");
//            } else if (object instanceof BasicBusinessInformationEntitySupplementaryComponent) {
//                setProgressStatus("Updating BBIESC");
//            }

            if (currentCount.incrementAndGet() == maxCount) {
                setProgressStatus("Completed");
            }
        }

        public int getProgress() {
            long progress = Math.round((currentCount.get() / (double) maxCount) * 100);
            return (int) progress;
        }

        public synchronized void setProgressStatus(String status) {
            this.status = status;
        }

        public synchronized String getProgressStatus() {
            return status;
        }
    }

    private class BusinessInformationEntityTreeNodeSubmitHandler {
        private ASBIEPNode root;
        private User user;
        private ProgressListener progressListener;

        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public BusinessInformationEntityTreeNodeSubmitHandler(
                ASBIEPNode root,
                User user) {
            this.root = root;
            this.user = user;
        }

        public void setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        public TopLevelAbie submit() {
            TopLevelAbie topLevelAbie = prepareForTopLevelAbieEntity();
            gatheringBusinessInformationEntities(root);
            if (progressListener != null) {
                progressListener.setMaxCount(
                        abieList.size() + asbiepList.size() + asbieList.size() +
                        bbiepList.size() + bbieList.size() + bbieScList.size()
                );
            }

            abieList.stream().forEach(e -> preset(e, topLevelAbie));
            abieRepository.save(abieList);

            bbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            bbiepRepository.save(bbiepList);

            bbieList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieRepository.save(bbieList);

            bbieScList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieScRepository.save(bbieScList);

            asbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            asbiepRepository.save(asbiepList);

            asbieList.stream().forEach(e -> preset(e, topLevelAbie));
            asbieRepository.save(asbieList);

            return topLevelAbie;
        }

        private TopLevelAbie prepareForTopLevelAbieEntity() {
            TopLevelAbie topLevelAbie = new TopLevelAbie();
            topLevelAbie.setOwnerUserId(user.getAppUserId());
            topLevelAbie.setState(Editing);
            topLevelAbie = topLevelAbieRepository.saveAndFlush(topLevelAbie);

            AggregateBusinessInformationEntity abie = root.getType().getAbie();
            preset(abie, topLevelAbie);
            abie = abieRepository.saveAndFlush(abie);
            abie.afterLoaded();

            topLevelAbie.setAbie(abie);
            topLevelAbieRepository.save(topLevelAbie);

            // It has to be added whether it is dirty or not.
            asbiepList.add(root.getAsbiep());

            return topLevelAbie;
        }

        private void preset(BusinessInformationEntity bie, TopLevelAbie topLevelAbie) {
            if (bie instanceof AggregateBusinessInformationEntity) {
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) bie;
                abie.setState(Editing);
            }

            bie.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            bie.addPersistEventListener(new CreatorModifierAwareEventListener(user));
            bie.addPersistEventListener(progressListener);
        }

        private void gatheringBusinessInformationEntities(
                BIENode node) {
            Collection<? extends BIENode> children;

            if (node instanceof ASBIEPNode) {
                ASBIEPNode asbiepNode = (ASBIEPNode) node;

                AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();

                ABIENode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAbie();

                if ((asbie != null && asbie.isDirty()) ||
                    (asbiep != null && asbiep.isDirty()) ||
                    (abie != null && abie.isDirty())) {
                    if (abie != null) {
                        abieList.add(abie);
                    }
                    if (asbie != null) {
                        asbieList.add(asbie);
                    }
                    if (asbiep != null) {
                        asbiepList.add(asbiep);
                    }
                }

                if (abieNode instanceof ABIENodeImpl) {
                    children = ((ABIENodeImpl) abieNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BBIEPNode) {
                BBIEPNode bbiepNode = (BBIEPNode) node;
                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);
                BasicBusinessInformationEntityProperty bbiep = bbiepNode.getBbiep();

                if ((bbie != null && bbie.isDirty()) ||
                    (bbiep != null && bbiep.isDirty())) {
                    if (bbie != null) {
                        bbieList.add(bbie);
                    }
                    if (bbiep != null) {
                        bbiepList.add(bbiep);
                    }
                }

                if (bbiepNode instanceof BasicBIEPropertyNodeImpl) {
                    children = ((BasicBIEPropertyNodeImpl) bbiepNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BBIESCNode) {
                BBIESCNode bbieScNode = (BBIESCNode) node;
                BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbieScNode);
                if (bbieSc != null && bbieSc.isDirty()) {
                    bbieScList.add(bbieSc);
                }

                children = node.getChildren();
            } else {
                throw new IllegalStateException();
            }

            for (BIENode child : children) {
                gatheringBusinessInformationEntities(child);
            }
        }
    }

    private BasicBusinessInformationEntity handleBBIEBdtPriRestri(
            BBIEPNode bbiepNode) {
        BasicBusinessInformationEntity bbie = bbiepNode.getBbie();
        BasicBusinessInformationEntityRestrictionType restrictionType = bbiepNode.getRestrictionType();
        switch (restrictionType) {
            case Primitive:
                if (bbie.getBdtPriRestriId() > 0L) {
                    bbie.setCodeListId(null);
                    bbie.setAgencyIdListId(null);
                }
                break;
            case Code:
                if (bbie.getCodeListId() > 0L) {
                    bbie.setBdtPriRestriId(null);
                    bbie.setAgencyIdListId(null);
                }
                break;
            case Agency:
                if (bbie.getAgencyIdListId() > 0L) {
                    bbie.setBdtPriRestriId(null);
                    bbie.setCodeListId(null);
                }
                break;
        }
        return bbie;
    }

    private BasicBusinessInformationEntitySupplementaryComponent handleBBIEScBdtScPriRestri(
            BBIESCNode bbieScNode) {
        BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbieScNode.getBbieSc();
        BasicBusinessInformationEntityRestrictionType restrictionType = bbieScNode.getRestrictionType();
        switch (restrictionType) {
            case Primitive:
                if (bbieSc.getDtScPriRestriId() > 0L) {
                    bbieSc.setCodeListId(null);
                    bbieSc.setAgencyIdListId(null);
                }
                break;
            case Code:
                if (bbieSc.getCodeListId() > 0L) {
                    bbieSc.setDtScPriRestriId(null);
                    bbieSc.setAgencyIdListId(null);
                }
                break;
            case Agency:
                if (bbieSc.getAgencyIdListId() > 0L) {
                    bbieSc.setDtScPriRestriId(null);
                    bbieSc.setCodeListId(null);
                }
                break;
        }
        return bbieSc;
    }

    public void validate(BIENode bieNode) {
        bieNode.validate();

        Collection<? extends BIENode> children = null;
        if (bieNode instanceof ABIENodeImpl) {
            children = ((ABIENodeImpl) bieNode).children;
        } else  if (bieNode instanceof AssociationBIEPropertyNodeImpl) {
            children = ((AssociationBIEPropertyNodeImpl) bieNode).type.children;
        } else if (bieNode instanceof BasicBIEPropertyNodeImpl) {
            children = ((BasicBIEPropertyNodeImpl) bieNode).children;
        }

        if (children != null) {
            for (BIENode child : children) {
                validate(child);
            }
        }
    }

    @Transactional
    public void update(ASBIEPNode bieNode, User user) {
        BusinessInformationEntityNodeUpdateHandler updateHandler =
                new BusinessInformationEntityNodeUpdateHandler(bieNode, user);
        updateHandler.update();
    }

    public void afterUpdate(ASBIEPNode bieNode) {
        bieNode.accept(new BIENodeVisitor() {
            @Override
            public void visit(ABIENode abieNode) {
                AggregateBusinessInformationEntity abie = abieNode.getAbie();
                if (abie != null) {
                    abie.afterLoaded();
                }
            }

            @Override
            public void visit(ASBIEPNode asbiepNode) {
                AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                if (asbie != null) {
                    asbie.afterLoaded();
                }

                AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();
                if (asbiep != null) {
                    asbiep.afterLoaded();
                }
            }

            @Override
            public void visit(BBIEPNode bbiepNode) {
                BasicBusinessInformationEntity bbie = bbiepNode.getBbie();
                if (bbie != null) {
                    bbie.afterLoaded();
                }

                BasicBusinessInformationEntityProperty bbiep = bbiepNode.getBbiep();
                if (bbiep != null) {
                    bbiep.afterLoaded();
                }
            }

            @Override
            public void visit(BBIESCNode bbieScNode) {
                BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbieScNode.getBbieSc();
                if (bbieSc != null) {
                    bbieSc.afterLoaded();
                }
            }
        });
    }

    private class BusinessInformationEntityNodeUpdateHandler implements BIENodeVisitor {
        private ASBIEPNode root;
        private User user;

        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public BusinessInformationEntityNodeUpdateHandler(
                ASBIEPNode root,
                User user) {
            this.root = root;
            this.user = user;
        }

        public void update() {
            TopLevelAbie topLevelAbie = prepareForTopLevelAbieEntity();
            root.accept(this);

            abieList.stream().forEach(e -> preset(e, topLevelAbie));
            abieRepository.save(abieList);

            bbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            bbiepRepository.save(bbiepList);

            bbieList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieRepository.save(bbieList);

            bbieScList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieScRepository.save(bbieScList);

            asbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            asbiepRepository.save(asbiepList);

            asbieList.stream().forEach(e -> preset(e, topLevelAbie));
            asbieRepository.save(asbieList);
        }

        private TopLevelAbie prepareForTopLevelAbieEntity() {
            long topLevelAbieId = root.getType().getAbie().getOwnerTopLevelAbieId();
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);

            return topLevelAbie;
        }

        private void preset(BusinessInformationEntity bie, TopLevelAbie topLevelAbie) {
            if (bie instanceof AggregateBusinessInformationEntity) {
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) bie;
                if (abie.getState() == null) {
                    abie.setState(Editing);
                }
            }

            bie.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
            bie.addPersistEventListener(eventListener);
            bie.addUpdateEventListener(eventListener);
        }

        @Override
        public void visit(ABIENode abieNode) {
            AggregateBusinessInformationEntity abie = abieNode.getAbie();
            if (abie != null && (abie.isDirty() || abie.getAbieId() == 0L)) {
                abieList.add(abie);
            }
        }

        @Override
        public void visit(ASBIEPNode asbiepNode) {
            AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
            AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();

            if (asbie != null && asbie.getAsbieId() > 0L && asbie.isDirty()) {
                asbieList.add(asbie);
            } else if (asbiep != null && asbiep.getAsbiepId() > 0L && asbiep.isDirty()) {
                asbiepList.add(asbiep);
            } else {
                if ((asbie != null && (asbie.isDirty() || asbie.getAsbieId() == 0L)) ||
                    (asbiep != null && (asbiep.isDirty() || asbiep.getAsbiepId() == 0L))) {
                    if (asbie != null) {
                        asbieList.add(asbie);
                    }
                    if (asbiep != null) {
                        asbiepList.add(asbiep);
                    }
                }
            }
        }

        @Override
        public void visit(BBIEPNode bbiepNode) {
            BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);
            BasicBusinessInformationEntityProperty bbiep = bbiepNode.getBbiep();

            if (bbie != null && bbie.getBbieId() > 0L && bbie.isDirty()) {
                bbieList.add(bbie);
            } else if (bbiep != null && bbiep.getBbiepId() > 0L && bbiep.isDirty()) {
                bbiepList.add(bbiep);
            } else if ((bbie != null && (bbie.isDirty() || bbie.getBbieId() == 0L)) ||
                       (bbiep != null && (bbiep.isDirty() || bbiep.getBbiepId() == 0L))) {
                if (bbie != null) {
                    bbieList.add(bbie);
                }
                if (bbiep != null) {
                    bbiepList.add(bbiep);
                }
            }
        }

        @Override
        public void visit(BBIESCNode bbieScNode) {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbieScNode);
            if (bbieSc != null && (bbieSc.isDirty() || bbieSc.getBbieScId() == 0L)) {
                bbieScList.add(bbieSc);
            }
        }
    }

    @Transactional
    public TopLevelAbie copy(ASBIEPNode bieNode, User user,
                             BusinessContext bizCtx, ProgressListener progressListener) {
        BusinessInformationEntityTreeNodeCopyHandler copyHandler =
                new BusinessInformationEntityTreeNodeCopyHandler(bieNode, user, bizCtx);
        copyHandler.setProgressListener(progressListener);

        return copyHandler.copy();
    }

    private class BusinessInformationEntityTreeNodeCopyHandler {
        private ASBIEPNode root;
        private User user;
        private BusinessContext bizCtx;

        private ProgressListener progressListener;

        private Map<Long, AggregateBusinessInformationEntity> prevAbieIdMap = new HashMap();
        private Map<Long, AssociationBusinessInformationEntity> prevAsbieIdMap = new HashMap();
        private Map<Long, AssociationBusinessInformationEntityProperty> prevAsbiepIdMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntity> prevBbieIdMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntityProperty> prevBbiepIdMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntitySupplementaryComponent> prevBbieScIdMap = new HashMap();

        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public BusinessInformationEntityTreeNodeCopyHandler(
                ASBIEPNode root,
                User user, BusinessContext bizCtx) {
            this.root = root;
            this.user = user;
            this.bizCtx = bizCtx;
        }

        public void setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        public TopLevelAbie copy() {
            TopLevelAbie topLevelAbie = prepareForTopLevelAbieEntity();
            for (BIENode child : root.getChildren()) {
                removeIdentifierOfBusinessInformationEntities(child);
            }
            for (BIENode child : root.getChildren()) {
                gatheringBusinessInformationEntities(child);
            }

            if (progressListener != null) {
                progressListener.setMaxCount(
                        abieList.size() + asbiepList.size() + asbieList.size() +
                        bbiepList.size() + bbieList.size() + bbieScList.size()
                );
            }

            abieList.stream().forEach(e -> preset(e, topLevelAbie));
            abieRepository.save(abieList);

            bbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            bbiepRepository.save(bbiepList);

            bbieList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieRepository.save(bbieList);

            bbieScList.stream().forEach(e -> preset(e, topLevelAbie));
            bbieScRepository.save(bbieScList);

            asbiepList.stream().forEach(e -> preset(e, topLevelAbie));
            asbiepRepository.save(asbiepList);

            asbieList.stream().forEach(e -> preset(e, topLevelAbie));
            asbieRepository.save(asbieList);

            return topLevelAbie;
        }

        private TopLevelAbie prepareForTopLevelAbieEntity() {
            TopLevelAbie topLevelAbie = new TopLevelAbie();
            topLevelAbie.setOwnerUserId(user.getAppUserId());
            topLevelAbie.setState(Editing);
            topLevelAbie = topLevelAbieRepository.saveAndFlush(topLevelAbie);

            AggregateBusinessInformationEntity abie = root.getType().getAbie();
            long abieId = abie.getAbieId();
            abie = abie.clone();
            preset(abie, topLevelAbie);

            prevAbieIdMap.put(abieId, abie);
            abie = abieRepository.saveAndFlush(abie);

            topLevelAbie.setAbie(abie);
            topLevelAbieRepository.save(topLevelAbie);

            // It has to be added whether it is dirty or not.
            AssociationBusinessInformationEntityProperty asbiep = root.getAsbiep();
            long asbiepId = asbiep.getAsbiepId();
            asbiep = asbiep.clone();
            asbiep.setRoleOfAbie(abie);
            preset(asbiep, topLevelAbie);

            prevAsbiepIdMap.put(asbiepId, asbiep);
            asbiepList.add(asbiep);

            return topLevelAbie;
        }

        private BusinessInformationEntity preset(BusinessInformationEntity bie, TopLevelAbie topLevelAbie) {
            if (bie == null) {
                throw new IllegalArgumentException("'businessInformationEntity' argument must not be null.");
            }

            if (bie instanceof AggregateBusinessInformationEntity) {
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) bie;
                abie.setState(Editing);
                abie.setBizCtx(bizCtx);
            }

            bie.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            bie.addPersistEventListener(new CreatorModifierAwareEventListener(user));
            bie.addPersistEventListener(progressListener);

            return bie;
        }

        private void removeIdentifierOfBusinessInformationEntities(
                BIENode node) {

            Collection<? extends BIENode> children = null;

            if (node instanceof ASBIEPNode) {
                ASBIEPNode asbiepNode = (ASBIEPNode) node;

                AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                if (asbie != null && asbie.getAsbieId() > 0L) {
                    prevAsbieIdMap.put(asbie.getAsbieId(), asbie.clone());
                }

                AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();
                if (asbiep != null && asbiep.getAsbiepId() > 0L) {
                    prevAsbiepIdMap.put(asbiep.getAsbiepId(), asbiep.clone());
                }

                ABIENode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAbie();
                if (abie != null && abie.getAbieId() > 0L) {
                    prevAbieIdMap.put(abie.getAbieId(), abie.clone());
                }

                if (abieNode instanceof ABIENodeImpl) {
                    children = ((ABIENodeImpl) abieNode).children;
                }
            } else if (node instanceof BBIEPNode) {
                BBIEPNode bbiepNode = (BBIEPNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);
                if (bbie != null && bbie.getBbieId() > 0L) {
                    prevBbieIdMap.put(bbie.getBbieId(), bbie.clone());
                }

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBbiep();
                if (bbiep != null && bbiep.getBbiepId() > 0L) {
                    prevBbiepIdMap.put(bbiep.getBbiepId(), bbiep.clone());
                }

                if (bbiepNode instanceof BasicBIEPropertyNodeImpl) {
                    children = ((BasicBIEPropertyNodeImpl) bbiepNode).children;
                }
            } else if (node instanceof BBIESCNode) {
                BBIESCNode bbieScNode = (BBIESCNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbieScNode);
                if (bbieSc != null && bbieSc.getBbieScId() > 0L) {
                    prevBbieScIdMap.put(bbieSc.getBbieScId(), bbieSc.clone());
                }
            } else {
                throw new IllegalStateException();
            }

            if (children != null || isDirtyOfAnyChild(node.getChildren())) {
                if (children == null) {
                    children = node.getChildren();
                }
                for (BIENode child : children) {
                    removeIdentifierOfBusinessInformationEntities(child);
                }
            }
        }

        private void gatheringBusinessInformationEntities(
                BIENode node) {

            Collection<? extends BIENode> children = null;

            if (node instanceof ASBIEPNode) {
                ASBIEPNode asbiepNode =
                        (ASBIEPNode) node;

                AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();

                ABIENode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie = abieNode.getAbie();

                if ((asbie != null && (asbie.isDirty() || asbie.getAsbieId() > 0L)) ||
                    (asbiep != null && (asbiep.isDirty() || asbiep.getAsbiepId() > 0L)) ||
                    (abie != null && (abie.isDirty()) || abie.getAbieId() > 0L)) {
                    if (abie != null) {
                        if (abie.getAbieId() > 0L) {
                            abie = prevAbieIdMap.get(abie.getAbieId());
                        }
                        abieList.add(abie);
                    }
                    if (asbie != null) {
                        if (asbie.getAsbieId() > 0L) {
                            asbie = prevAsbieIdMap.get(asbie.getAsbieId());
                        }
                        long fromAbieId = asbie.getFromAbieId();
                        if (fromAbieId == 0L) {
                            AggregateBusinessInformationEntity fromAbie = asbie.getFromAbie();
                            if (fromAbie != null) {
                                fromAbieId = fromAbie.getAbieId();
                            }
                        }
                        if (fromAbieId > 0L) {
                            AggregateBusinessInformationEntity fromAbie = prevAbieIdMap.get(fromAbieId);
                            if (fromAbie != null) {
                                asbie.setFromAbie(fromAbie);
                            }
                        }

                        long toAsbiepId = asbie.getToAsbiepId();
                        if (toAsbiepId == 0L) {
                            AssociationBusinessInformationEntityProperty toAsbiep = asbie.getToAsbiep();
                            if (toAsbiep != null) {
                                toAsbiepId = toAsbiep.getAsbiepId();
                            }
                        }
                        if (toAsbiepId > 0L) {
                            AssociationBusinessInformationEntityProperty toAsbiep = prevAsbiepIdMap.get(toAsbiepId);
                            if (toAsbiep != null) {
                                asbie.setToAsbiep(toAsbiep);
                            }
                        }

                        if (asbie.getFromAbieId() == 0L && asbie.getFromAbie() == null) {
                            throw new IllegalStateException();
                        }
                        if (asbie.getToAsbiepId() == 0L && asbie.getToAsbiep() == null) {
                            throw new IllegalStateException();
                        }
                        asbieList.add(asbie);
                    }
                    if (asbiep != null) {
                        if (asbiep.getAsbiepId() > 0L) {
                            asbiep = prevAsbiepIdMap.get(asbiep.getAsbiepId());
                        }
                        long roleOfAbieId = asbiep.getRoleOfAbieId();
                        if (roleOfAbieId == 0L) {
                            AggregateBusinessInformationEntity roleOfAbie = asbiep.getRoleOfAbie();
                            if (roleOfAbie != null) {
                                roleOfAbieId = roleOfAbie.getAbieId();
                            }
                        }
                        if (roleOfAbieId > 0L) {
                            AggregateBusinessInformationEntity roleOfAbie = prevAbieIdMap.get(roleOfAbieId);
                            if (roleOfAbie != null) {
                                asbiep.setRoleOfAbie(roleOfAbie);
                            }
                        }

                        if (asbiep.getRoleOfAbieId() == 0L && asbiep.getRoleOfAbie() == null) {
                            throw new IllegalStateException();
                        }
                        asbiepList.add(asbiep);
                    }
                }

                if (abieNode instanceof ABIENodeImpl) {
                    children = ((ABIENodeImpl) abieNode).children;
                }
            } else if (node instanceof BBIEPNode) {
                BBIEPNode bbiepNode = (BBIEPNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBbiep();

                if ((bbie != null && (bbie.isDirty() || bbie.getBbieId() > 0L)) ||
                    (bbiep != null && (bbiep.isDirty() || bbiep.getBbiepId() > 0L))) {
                    if (bbie != null) {
                        long bbieId = bbie.getBbieId();
                        if (bbieId > 0L) {
                            bbie = prevBbieIdMap.get(bbieId);
                        }

                        long fromAbieId = bbie.getFromAbieId();
                        if (fromAbieId == 0L) {
                            AggregateBusinessInformationEntity fromAbie = bbie.getFromAbie();
                            if (fromAbie != null) {
                                fromAbieId = fromAbie.getAbieId();
                            }
                        }
                        if (fromAbieId > 0L) {
                            AggregateBusinessInformationEntity fromAbie = prevAbieIdMap.get(fromAbieId);
                            if (fromAbie != null) {
                                bbie.setFromAbie(fromAbie);
                            }
                        }

                        long toBbiepId = bbie.getToBbiepId();
                        if (toBbiepId == 0L) {
                            BasicBusinessInformationEntityProperty toBbiep = bbie.getToBbiep();
                            if (toBbiep != null) {
                                toBbiepId = toBbiep.getBbiepId();
                            }
                        }
                        if (toBbiepId > 0L) {
                            BasicBusinessInformationEntityProperty toBbiep = prevBbiepIdMap.get(toBbiepId);
                            if (toBbiep != null) {
                                bbie.setToBbiep(toBbiep);
                            }
                        }

                        if (bbie.getFromAbieId() == 0L && bbie.getFromAbie() == null) {
                            throw new IllegalStateException();
                        }
                        bbieList.add(bbie);
                    }
                    if (bbiep != null) {
                        long bbiepId = bbiep.getBbiepId();
                        if (bbiepId > 0L) {
                            bbiep = prevBbiepIdMap.get(bbiepId);
                        }
                        bbiepList.add(bbiep);
                    }
                }

                if (bbiepNode instanceof BasicBIEPropertyNodeImpl) {
                    children = ((BasicBIEPropertyNodeImpl) bbiepNode).children;
                }
            } else if (node instanceof BBIESCNode) {
                BBIESCNode bbieScNode = (BBIESCNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbieScNode);

                if (bbieSc != null && (bbieSc.isDirty() || bbieSc.getBbieScId() > 0L)) {
                    if (bbieSc.getBbieScId() > 0L) {
                        bbieSc = prevBbieScIdMap.get(bbieSc.getBbieScId());
                    }

                    long bbieId = bbieSc.getBbieId();
                    if (bbieId == 0L) {
                        BasicBusinessInformationEntity bbie = bbieSc.getBbie();
                        if (bbie != null) {
                            bbieId = bbie.getBbieId();
                        }
                    }
                    if (bbieId > 0L) {
                        BasicBusinessInformationEntity bbie = prevBbieIdMap.get(bbieId);
                        if (bbie != null) {
                            bbieSc.setBbie(bbie);
                        }
                    }
                    bbieScList.add(bbieSc);
                }
            } else {
                throw new IllegalStateException();
            }

            if (children != null || isDirtyOfAnyChild(node.getChildren())) {
                if (children == null) {
                    children = node.getChildren();
                }
                for (BIENode child : children) {
                    gatheringBusinessInformationEntities(child);
                }
            }
        }

        private boolean isDirtyOfAnyChild(Collection<? extends BIENode> children) {
            for (BIENode node : children) {
                if (node instanceof ASBIEPNode) {
                    ASBIEPNode asbiepNode = (ASBIEPNode) node;

                    AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                    AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();

                    ABIENode abieNode = asbiepNode.getType();
                    AggregateBusinessInformationEntity abie = abieNode.getAbie();

                    if ((asbie != null && (asbie.isDirty() || asbie.getAsbieId() > 0L)) ||
                        (asbiep != null && (asbiep.isDirty() || asbiep.getAsbiepId() > 0L)) ||
                        (abie != null && (abie.isDirty()) || abie.getAbieId() > 0L)) {
                        return true;
                    }
                } else if (node instanceof BBIEPNode) {
                    BBIEPNode bbiepNode = (BBIEPNode) node;

                    BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                    BasicBusinessInformationEntityProperty bbiep =
                            bbiepNode.getBbiep();

                    if ((bbie != null && (bbie.isDirty() || bbie.getBbieId() > 0L)) ||
                        (bbiep != null && (bbiep.isDirty() || bbiep.getBbiepId() > 0L))) {
                        return true;
                    }
                } else if (node instanceof BBIESCNode) {
                    BBIESCNode bbieScNode = (BBIESCNode) node;

                    BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                            handleBBIEScBdtScPriRestri(bbieScNode);

                    if (bbieSc != null && (bbieSc.isDirty() || bbieSc.getBbieScId() > 0L)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
