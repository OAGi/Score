package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.treenode.*;
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

import static org.oagi.srt.model.treenode.BasicBusinessInformationEntityRestrictionType.*;
import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Editing;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Service
@Transactional
public class TreeNodeService {

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

    public AggregateCoreComponentTreeNode createCoreComponentTreeNode(
            AggregateCoreComponent aggregateCoreComponent) {
        if (aggregateCoreComponent == null) {
            throw new IllegalArgumentException("'aggregateCoreComponent' argument must not be null.");
        }
        return new AggregateCoreComponentTreeNodeImpl(aggregateCoreComponent);
    }

    public BasicCoreComponentPropertyTreeNode createCoreComponentTreeNode(
            AggregateCoreComponentTreeNode parent,
            BasicCoreComponent basicCoreComponent) {
        if (basicCoreComponent == null) {
            throw new IllegalArgumentException("'basicCoreComponent' argument must not be null.");
        }
        return new BasicCoreComponentPropertyTreeNodeImpl(parent, basicCoreComponent);
    }

    public BasicCoreComponentPropertyTreeNode createCoreComponentTreeNode(
            BasicCoreComponent basicCoreComponent) {
        if (basicCoreComponent == null) {
            throw new IllegalArgumentException("'basicCoreComponent' argument must not be null.");
        }
        return new BasicCoreComponentPropertyTreeNodeImpl(basicCoreComponent);
    }

    public AssociationCoreComponentPropertyTreeNode createCoreComponentTreeNode(
            AssociationCoreComponent associationCoreComponent) {
        if (associationCoreComponent == null) {
            throw new IllegalArgumentException("'associationCoreComponent' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyTreeNodeImpl(associationCoreComponent);
    }

    public AssociationCoreComponentPropertyTreeNode createCoreComponentTreeNode(
            AggregateCoreComponentTreeNode parent,
            AssociationCoreComponent associationCoreComponent) {
        if (associationCoreComponent == null) {
            throw new IllegalArgumentException("'associationCoreComponent' argument must not be null.");
        }
        return new AssociationCoreComponentPropertyTreeNodeImpl(parent, associationCoreComponent);
    }

    private abstract class AbstractSRTTreeNode implements SRTTreeNode {

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

    private class AggregateCoreComponentTreeNodeImpl
            extends AbstractSRTTreeNode
            implements AggregateCoreComponentTreeNode {

        private final AggregateCoreComponent acc;
        private AggregateCoreComponentTreeNode base;

        private List<CoreComponentRelation> associations = null;
        private Collection<CoreComponentTreeNode> children = null;

        private AggregateCoreComponentTreeNodeImpl(AggregateCoreComponent aggregateCoreComponent) {
            this.acc = aggregateCoreComponent;
        }

        @Override
        public AggregateCoreComponent getAggregateCoreComponent() {
            return acc;
        }

        @Override
        public AggregateCoreComponent getAcc() {
            return getAggregateCoreComponent();
        }

        @Override
        public AggregateCoreComponentTreeNode getBase() {
            if (base == null) {
                AggregateCoreComponent acc = getAggregateCoreComponent();
                long basedAccId = acc.getBasedAccId();
                if (basedAccId > 0L) {
                    AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
                    base = new AggregateCoreComponentTreeNodeImpl(basedAcc);
                }
            }
            return base;
        }

        @Override
        public String getId() {
            return acc.getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = acc.getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
        }

        private List<CoreComponentRelation> associations() {
            if (associations == null) {
                associations = getAssociations(acc);
            }
            return associations;
        }

        @Override
        public boolean hasChild() {
            return associations().isEmpty() ? false : true;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                if (associations().isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();

                    for (CoreComponentRelation association : associations()) {
                        if (association instanceof AssociationCoreComponent) {
                            AssociationCoreComponentPropertyTreeNode asccpNode =
                                    createCoreComponentTreeNode(this, (AssociationCoreComponent) association);
                            children.add(asccpNode);
                        } else if (association instanceof BasicCoreComponent) {
                            BasicCoreComponentPropertyTreeNode bccpNode =
                                    createCoreComponentTreeNode(this, (BasicCoreComponent) association);
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

    private List<CoreComponentRelation> getAssociations(AggregateCoreComponent acc) {
        List<CoreComponentRelation> associationsWithoutRecursive = getAssociationsWithoutRecursive(acc);
        List<CoreComponentRelation> associations = new ArrayList();

        for (int i = 0, len = associationsWithoutRecursive.size(); i < len; ++i) {
            CoreComponentRelation relation = associationsWithoutRecursive.get(i);
            if (relation instanceof AssociationCoreComponent) {
                AssociationCoreComponent ascc = (AssociationCoreComponent) relation;
                AggregateCoreComponent roleOfAcc = getRoleOfAcc(ascc);
                if (isGroup(roleOfAcc)) {
                    associations.addAll(getAssociations(roleOfAcc));
                } else {
                    associations.add(ascc);
                }
            } else {
                associations.add(relation);
            }
        }

        return associations;
    }

    private List<CoreComponentRelation> getAssociationsWithoutRecursive(AggregateCoreComponent acc) {
        long accId = acc.getAccId();

        List<CoreComponentRelation> coreComponentRelations = new ArrayList();
        List<AssociationCoreComponent> asccList =
                asccRepository.findByFromAccIdAndRevisionNum(accId, 0);
        coreComponentRelations.addAll(asccList);
        List<BasicCoreComponent> bccList =
                bccRepository.findByFromAccIdAndRevisionNum(accId, 0);
        coreComponentRelations.addAll(bccList);
        Collections.sort(coreComponentRelations, comparingCoreComponentRelation());

        return coreComponentRelations;
    }

    private AggregateCoreComponent getRoleOfAcc(AssociationCoreComponent associationCoreComponent) {
        long toAsccpId = associationCoreComponent.getToAsccpId();
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(toAsccpId);
        long roleOfAccId = asccp.getRoleOfAccId();
        AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
        return acc;
    }

    private boolean isGroup(AggregateCoreComponent acc) {
        OagisComponentType oagisComponentType = acc.getOagisComponentType();
        return (oagisComponentType == SemanticGroup || oagisComponentType == UserExtensionGroup) ? true : false;
    }

    private class AssociationCoreComponentPropertyTreeNodeImpl
            extends AbstractSRTTreeNode
            implements AssociationCoreComponentPropertyTreeNode {

        private AggregateCoreComponentTreeNode parent = null;
        private final AssociationCoreComponent ascc;
        private final AssociationCoreComponentProperty asccp;
        private AggregateCoreComponentTreeNode type;

        private Boolean hasChild = null;
        private Collection<CoreComponentTreeNode> children = null;

        private AssociationCoreComponentPropertyTreeNodeImpl(AssociationCoreComponent ascc) {
            this(null, ascc);
        }

        private AssociationCoreComponentPropertyTreeNodeImpl(AggregateCoreComponentTreeNode parent,
                                                             AssociationCoreComponent ascc) {
            this.parent = parent;
            this.ascc = ascc;

            long asccpId = ascc.getToAsccpId();
            this.asccp = asccpRepository.findOne(asccpId);
        }

        @Override
        public AssociationCoreComponent getAssociationCoreComponent() {
            return ascc;
        }

        @Override
        public AssociationCoreComponent getAscc() {
            return getAssociationCoreComponent();
        }

        @Override
        public AssociationCoreComponentProperty getAssociationCoreComponentProperty() {
            return asccp;
        }

        @Override
        public AssociationCoreComponentProperty getAsccp() {
            return getAssociationCoreComponentProperty();
        }

        @Override
        public AggregateCoreComponentTreeNode getType() {
            if (type == null) {
                long roleOfAccId = getRoleOfAccId();
                if (roleOfAccId > 0L) {
                    AggregateCoreComponent roleOfAcc = accRepository.findOne(roleOfAccId);
                    type = new AggregateCoreComponentTreeNodeImpl(roleOfAcc);
                }
            }
            return type;
        }

        @Override
        public String getId() {
            return getAssociationCoreComponentProperty().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = getAssociationCoreComponentProperty().getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
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
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                AggregateCoreComponentTreeNode type = getType();
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
            long roleOfAccId = getAssociationCoreComponentProperty().getRoleOfAccId();
            return roleOfAccId;
        }

        @Override
        public AggregateCoreComponentTreeNode getParent() {
            if (parent == null) {
                long fromAccId = getAssociationCoreComponent().getFromAccId();
                AggregateCoreComponent fromAcc = accRepository.findOne(fromAccId);
                parent = new AggregateCoreComponentTreeNodeImpl(fromAcc);
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

    private class BasicCoreComponentPropertyTreeNodeImpl
            extends AbstractSRTTreeNode
            implements BasicCoreComponentPropertyTreeNode {

        private AggregateCoreComponentTreeNode parent = null;
        private final BasicCoreComponent bcc;
        private final BasicCoreComponentProperty bccp;
        private DataType dataType;

        private Boolean hasChild = null;
        private Collection<BusinessDataTypeSupplementaryComponentTreeNode> children = null;

        private BasicCoreComponentPropertyTreeNodeImpl(BasicCoreComponent bcc) {
            this(null, bcc);
        }

        private BasicCoreComponentPropertyTreeNodeImpl(AggregateCoreComponentTreeNode parent,
                                                       BasicCoreComponent bcc) {
            this.parent = parent;
            this.bcc = bcc;

            long bccpId = bcc.getToBccpId();
            this.bccp = bccpRepository.findOne(bccpId);
        }

        @Override
        public BasicCoreComponent getBasicCoreComponent() {
            return bcc;
        }

        @Override
        public BasicCoreComponent getBcc() {
            return getBasicCoreComponent();
        }

        @Override
        public BasicCoreComponentProperty getBasicCoreComponentProperty() {
            return bccp;
        }

        @Override
        public BasicCoreComponentProperty getBccp() {
            return getBasicCoreComponentProperty();
        }

        @Override
        public DataType getBusinessDataType() {
            if (dataType == null) {
                long bdtId = getBasicCoreComponentProperty().getBdtId();
                dataType = dtRepository.findOne(bdtId);
            }
            return dataType;
        }

        @Override
        public DataType getBdt() {
            return getBusinessDataType();
        }

        @Override
        public String getId() {
            return getBasicCoreComponentProperty().getGuid();
        }

        @Override
        public Namespace getNamespace() {
            long namespaceId = getBasicCoreComponentProperty().getNamespaceId();
            return namespaceRepository.findOne(namespaceId);
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                long bdtId = getBusinessDataType().getDtId();
                hasChild = dtScRepository.countByOwnerDtId(bdtId) > 0;
            }
            return hasChild;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                long bdtId = getBusinessDataType().getDtId();
                List<DataTypeSupplementaryComponent> bdtScList = dtScRepository.findByOwnerDtId(bdtId);
                if (bdtScList.isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();
                    for (DataTypeSupplementaryComponent bdtSc : bdtScList) {
                        BusinessDataTypeSupplementaryComponentTreeNode child =
                                new BusinessDataTypeSupplementaryComponentTreeNodeImpl(this, bdtSc);
                        children.add(child);
                    }
                }
            }
            return children;
        }

        @Override
        public AggregateCoreComponentTreeNode getParent() {
            if (parent == null) {
                long fromAccId = getBasicCoreComponent().getFromAccId();
                AggregateCoreComponent fromAcc = accRepository.findOne(fromAccId);
                parent = new AggregateCoreComponentTreeNodeImpl(fromAcc);
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

    private class BusinessDataTypeSupplementaryComponentTreeNodeImpl
            extends AbstractSRTTreeNode
            implements BusinessDataTypeSupplementaryComponentTreeNode {

        private BasicCoreComponentPropertyTreeNode parent;
        private DataTypeSupplementaryComponent dtSc;

        public BusinessDataTypeSupplementaryComponentTreeNodeImpl(BasicCoreComponentPropertyTreeNode parent,
                                                                  DataTypeSupplementaryComponent dtSc) {
            this.parent = parent;
            this.dtSc = dtSc;
        }

        @Override
        public BasicCoreComponentPropertyTreeNode getParent() {
            return parent;
        }

        @Override
        public DataTypeSupplementaryComponent getBusinessDataTypeSupplementaryComponent() {
            return dtSc;
        }

        @Override
        public DataTypeSupplementaryComponent getBdtSc() {
            return getBusinessDataTypeSupplementaryComponent();
        }

        @Override
        public String getId() {
            return dtSc.getGuid();
        }

        @Override
        public Namespace getNamespace() {
            return (parent != null) ? parent.getNamespace() : null;
        }

        @Override
        public boolean hasChild() {
            return false;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
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

    public AssociationBusinessInformationEntityPropertyTreeNode createBusinessInformationEntityTreeNode(AssociationCoreComponentProperty asccp,
                                                                                                        BusinessContext bizCtx) {
        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }
        if (bizCtx == null) {
            throw new IllegalArgumentException("'bizCtx' argument must not be null.");
        }

        return new AssociationBusinessInformationEntityPropertyTreeNodeImpl(asccp, bizCtx);
    }

    public AssociationBusinessInformationEntityPropertyTreeNode createBusinessInformationEntityTreeNode(
            TopLevelAbie topLevelAbie) {

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        BusinessContext bizCtx = businessContextRepository.findOne(abie.getBizCtxId());
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
        AggregateCoreComponent acc = accRepository.findOne(abie.getBasedAccId());

        AggregateBusinessInformationEntityTreeNodeImpl abieNode =
                new AggregateBusinessInformationEntityTreeNodeImpl(abie, acc, bizCtx);

        return new AssociationBusinessInformationEntityPropertyTreeNodeImpl(asbiep, asccp, abieNode);
    }

    private class AggregateBusinessInformationEntityTreeNodeImpl
            extends AbstractSRTTreeNode
            implements AggregateBusinessInformationEntityTreeNode {

        private AssociationBusinessInformationEntityPropertyTreeNode parent;

        private AggregateBusinessInformationEntity abie;
        private AggregateCoreComponent basedAcc;
        private BusinessContext bizCtx;

        private Boolean hasChild = null;
        private List<BusinessInformationEntityTreeNode> children = null;

        public AggregateBusinessInformationEntityTreeNodeImpl(AggregateCoreComponent acc, BusinessContext bizCtx) {
            this.abie = createABIE(acc, bizCtx);
            this.basedAcc = acc;
            this.bizCtx = bizCtx;
        }

        public AggregateBusinessInformationEntityTreeNodeImpl(AggregateBusinessInformationEntity abie,
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

        public AssociationBusinessInformationEntityPropertyTreeNode getParent() {
            return parent;
        }

        private void setParent(AssociationBusinessInformationEntityPropertyTreeNode parent) {
            this.parent = parent;
        }

        @Override
        public String getId() {
            return abie.getGuid();
        }

        @Override
        public AggregateBusinessInformationEntity getAggregateBusinessInformationEntity() {
            return abie;
        }

        @Override
        public AggregateBusinessInformationEntity getAbie() {
            return getAggregateBusinessInformationEntity();
        }

        @Override
        public AggregateCoreComponent getAggregateCoreComponent() {
            return basedAcc;
        }

        @Override
        public AggregateCoreComponent getAcc() {
            return getAggregateCoreComponent();
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
        public Collection<? extends BusinessInformationEntityTreeNode> getChildren() {
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

                    List<CoreComponentRelation> associations = getAssociations(acc);
                    for (CoreComponentRelation relation : associations) {
                        if (relation instanceof AssociationCoreComponent) {
                            AssociationCoreComponent ascc = (AssociationCoreComponent) relation;
                            AssociationBusinessInformationEntityPropertyTreeNodeImpl asbieChild =
                                    new AssociationBusinessInformationEntityPropertyTreeNodeImpl(abie, ascc, bizCtx, ++seqKey);
                            asbieChild.setParent(parent);

                            children.add(asbieChild);
                        } else if (relation instanceof BasicCoreComponent) {
                            BasicCoreComponent bcc = (BasicCoreComponent) relation;

                            BasicBusinessInformationEntityPropertyTreeNodeImpl bbieChild =
                                    new BasicBusinessInformationEntityPropertyTreeNodeImpl(
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

    private class AssociationBusinessInformationEntityPropertyTreeNodeImpl
            extends AbstractSRTTreeNode
            implements AssociationBusinessInformationEntityPropertyTreeNode {

        private AssociationBusinessInformationEntityPropertyTreeNode parent;

        private AssociationBusinessInformationEntity asbie;
        private AssociationCoreComponent ascc;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationCoreComponentProperty asccp;

        private AggregateBusinessInformationEntityTreeNodeImpl type;

        public AssociationBusinessInformationEntityPropertyTreeNodeImpl(AssociationCoreComponentProperty asccp,
                                                                        BusinessContext bizCtx) {
            this.asccp = asccp;

            long roleOfAccId = asccp.getRoleOfAccId();
            AggregateCoreComponent acc = accRepository.findOne(roleOfAccId);
            type = new AggregateBusinessInformationEntityTreeNodeImpl(acc, bizCtx);
            type.setParent(this);

            AggregateBusinessInformationEntity roleOfAbie = type.getAggregateBusinessInformationEntity();
            asbiep = createASBIEP(asccp, roleOfAbie);
        }

        public AssociationBusinessInformationEntityPropertyTreeNodeImpl(AssociationBusinessInformationEntityProperty asbiep,
                                                                        AssociationCoreComponentProperty asccp,
                                                                        AggregateBusinessInformationEntityTreeNodeImpl type) {
            this.asbiep = asbiep;
            this.asccp = asccp;
            this.type = type;

            type.setParent(this);
        }

        public AssociationBusinessInformationEntityPropertyTreeNodeImpl(AggregateBusinessInformationEntity fromAbie,
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

                type = new AggregateBusinessInformationEntityTreeNodeImpl(roleOfAbie, basedAcc, bizCtx);

            } else {
                long toAsccpId = ascc.getToAsccpId();
                asccp = asccpRepository.findOneByAsccpIdAndRevisionNumAndState(toAsccpId, 0, Published);
                long roleOfAccId = asccp.getRoleOfAccId();
                AggregateCoreComponent basedAcc = accRepository.findOneByAccIdAndRevisionNumAndState(roleOfAccId, 0, Published);

                type = new AggregateBusinessInformationEntityTreeNodeImpl(basedAcc, bizCtx);
                AggregateBusinessInformationEntity roleOfAbie = type.getAggregateBusinessInformationEntity();
                asbiep = createASBIEP(asccp, roleOfAbie);
                asbie = createASBIE(fromAbie, asbiep, ascc, seqKey);
            }

            type.setParent(this);
        }

        @Override
        public AssociationBusinessInformationEntityPropertyTreeNode getParent() {
            return parent;
        }

        private void setParent(AssociationBusinessInformationEntityPropertyTreeNode parent) {
            this.parent = parent;
        }

        @Override
        public String getId() {
            return asbiep.getGuid();
        }

        @Override
        public AssociationBusinessInformationEntity getAssociationBusinessInformationEntity() {
            return asbie;
        }

        @Override
        public AssociationBusinessInformationEntity getAsbie() {
            return getAssociationBusinessInformationEntity();
        }

        @Override
        public AssociationCoreComponent getAssociationCoreComponent() {
            return ascc;
        }

        @Override
        public AssociationCoreComponent getAscc() {
            return getAssociationCoreComponent();
        }

        @Override
        public AssociationBusinessInformationEntityProperty getAssociationBusinessInformationEntityProperty() {
            return asbiep;
        }

        @Override
        public AssociationBusinessInformationEntityProperty getAsbiep() {
            return getAssociationBusinessInformationEntityProperty();
        }

        @Override
        public AssociationCoreComponentProperty getAssociationCoreComponentProperty() {
            return asccp;
        }

        @Override
        public AssociationCoreComponentProperty getAsccp() {
            return getAssociationCoreComponentProperty();
        }

        @Override
        public AggregateBusinessInformationEntityTreeNode getType() {
            return type;
        }

        @Override
        public boolean hasChild() {
            return type.hasChild();
        }

        @Override
        public Collection<? extends BusinessInformationEntityTreeNode> getChildren() {
            return type.getChildren();
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
            asbie.setBasedAscc(ascc);
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
            return (asbie != null) ? asbie.isUsed() : true;
        }

        @Override
        public void setUsed(boolean used) {
            if (asbie != null) {
                asbie.setUsed(used);
            }

            if (used) {
                if (parent != null && !parent.isUsed()) {
                    parent.setUsed(used);
                }
            } else {
                if (type.children != null) {
                    for (BusinessInformationEntityTreeNode node : getChildren()) {
                        node.setUsed(used);
                    }
                }
            }
        }
    }

    private class BasicBusinessInformationEntityPropertyTreeNodeImpl
            extends AbstractSRTTreeNode
            implements BasicBusinessInformationEntityPropertyTreeNode {

        private AssociationBusinessInformationEntityPropertyTreeNode parent;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicCoreComponentProperty bccp;

        private BasicBusinessInformationEntity bbie;
        private BasicCoreComponent bcc;

        private DataType bdt;
        private BasicBusinessInformationEntityRestrictionType restrictionType;

        private Boolean hasChild = null;
        private List<BusinessInformationEntityTreeNode> children = null;

        public BasicBusinessInformationEntityPropertyTreeNodeImpl(AggregateBusinessInformationEntity fromAbie,
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

            setRestrictionType((bbie.getBdtPriRestriId() > 0L) ? Primitive : (bbie.getCodeListId() > 0L) ? Code : Agency);
        }

        private void setParent(AssociationBusinessInformationEntityPropertyTreeNode parent) {
            this.parent = parent;
        }

        @Override
        public AssociationBusinessInformationEntityPropertyTreeNode getParent() {
            return parent;
        }

        @Override
        public String getId() {
            return bbie.getGuid();
        }

        @Override
        public BasicBusinessInformationEntity getBasicBusinessInformationEntity() {
            return bbie;
        }

        @Override
        public BasicBusinessInformationEntity getBbie() {
            return getBasicBusinessInformationEntity();
        }

        @Override
        public BasicCoreComponent getBasicCoreComponent() {
            return bcc;
        }

        @Override
        public BasicCoreComponent getBcc() {
            return getBasicCoreComponent();
        }

        @Override
        public BasicBusinessInformationEntityProperty getBasicBusinessInformationEntityProperty() {
            return bbiep;
        }

        @Override
        public BasicBusinessInformationEntityProperty getBbiep() {
            return getBasicBusinessInformationEntityProperty();
        }

        @Override
        public BasicCoreComponentProperty getBasicCoreComponentProperty() {
            return bccp;
        }

        @Override
        public BasicCoreComponentProperty getBccp() {
            return getBasicCoreComponentProperty();
        }

        @Override
        public DataType getBusinessDataType() {
            return bdt;
        }

        @Override
        public DataType getBdt() {
            return getBusinessDataType();
        }

        @Override
        public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
            return restrictionType;
        }

        @Override
        public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
            this.restrictionType = restrictionType;
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
        public Collection<? extends BusinessInformationEntityTreeNode> getChildren() {
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

                    BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                            new BasicBusinessInformationEntitySupplementaryComponentTreeNodeImpl(this, bbieSc, bdtSc);
                    children.add(bbieScNode);
                }
            }
            return children;
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
                    for (BusinessInformationEntityTreeNode node : getChildren()) {
                        node.setUsed(used);
                    }
                }
            }
        }
    }

    private class BasicBusinessInformationEntitySupplementaryComponentTreeNodeImpl
            extends AbstractSRTTreeNode
            implements BasicBusinessInformationEntitySupplementaryComponentTreeNode {

        private BasicBusinessInformationEntityPropertyTreeNode parent;
        private BasicBusinessInformationEntitySupplementaryComponent bbieSc;
        private DataTypeSupplementaryComponent bdtSc;

        private BasicBusinessInformationEntityRestrictionType restrictionType;

        public BasicBusinessInformationEntitySupplementaryComponentTreeNodeImpl(BasicBusinessInformationEntityPropertyTreeNode parent,
                                                                                BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                                                                DataTypeSupplementaryComponent bdtSc) {
            this.parent = parent;
            this.bbieSc = bbieSc;
            this.bdtSc = bdtSc;

            setRestrictionType((bbieSc.getDtScPriRestriId() > 0L) ? Primitive : (bbieSc.getCodeListId() > 0L) ? Code : Agency);
        }

        @Override
        public BasicBusinessInformationEntityPropertyTreeNode getParent() {
            return parent;
        }

        @Override
        public BasicBusinessInformationEntitySupplementaryComponent getBasicBusinessInformationEntitySupplementaryComponent() {
            return bbieSc;
        }

        @Override
        public BasicBusinessInformationEntitySupplementaryComponent getBbieSc() {
            return getBasicBusinessInformationEntitySupplementaryComponent();
        }

        @Override
        public DataTypeSupplementaryComponent getBusinessDataTypeSupplementaryComponent() {
            return bdtSc;
        }

        @Override
        public DataTypeSupplementaryComponent getBdtSc() {
            return getBusinessDataTypeSupplementaryComponent();
        }

        @Override
        public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
            return restrictionType;
        }

        @Override
        public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
            this.restrictionType = restrictionType;
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
        public Collection<? extends BusinessInformationEntityTreeNode> getChildren() {
            return Collections.emptyList();
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

    @Transactional
    public TopLevelAbie submit(AssociationBusinessInformationEntityPropertyTreeNode bieNode,
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
        private AssociationBusinessInformationEntityPropertyTreeNode root;
        private User user;
        private ProgressListener progressListener;

        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public BusinessInformationEntityTreeNodeSubmitHandler(
                AssociationBusinessInformationEntityPropertyTreeNode root,
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

            AggregateBusinessInformationEntity abie = root.getType().getAggregateBusinessInformationEntity();
            preset(abie, topLevelAbie);
            abie = abieRepository.saveAndFlush(abie);
            abie.afterLoaded();

            topLevelAbie.setAbie(abie);
            topLevelAbieRepository.save(topLevelAbie);

            // It has to be added whether it is dirty or not.
            asbiepList.add(root.getAssociationBusinessInformationEntityProperty());

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
                BusinessInformationEntityTreeNode node) {
            Collection<? extends BusinessInformationEntityTreeNode> children;

            if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                        (AssociationBusinessInformationEntityPropertyTreeNode) node;

                AssociationBusinessInformationEntity asbie =
                        asbiepNode.getAssociationBusinessInformationEntity();

                AssociationBusinessInformationEntityProperty asbiep =
                        asbiepNode.getAssociationBusinessInformationEntityProperty();

                AggregateBusinessInformationEntityTreeNode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAggregateBusinessInformationEntity();

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

                if (abieNode instanceof AggregateBusinessInformationEntityTreeNodeImpl) {
                    children = ((AggregateBusinessInformationEntityTreeNodeImpl) abieNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                        (BasicBusinessInformationEntityPropertyTreeNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBasicBusinessInformationEntityProperty();

                if ((bbie != null && bbie.isDirty()) ||
                    (bbiep != null && bbiep.isDirty())) {
                    if (bbie != null) {
                        bbieList.add(bbie);
                    }
                    if (bbiep != null) {
                        bbiepList.add(bbiep);
                    }
                }

                if (bbiepNode instanceof BasicBusinessInformationEntityPropertyTreeNodeImpl) {
                    children = ((BasicBusinessInformationEntityPropertyTreeNodeImpl) bbiepNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
                BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                        (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        handleBBIEScBdtScPriRestri(bbieScNode);
                if (bbieSc != null && bbieSc.isDirty()) {
                    bbieScList.add(bbieSc);
                }

                children = node.getChildren();
            } else {
                throw new IllegalStateException();
            }

            for (BusinessInformationEntityTreeNode child : children) {
                gatheringBusinessInformationEntities(child);
            }
        }
    }

    private BasicBusinessInformationEntity handleBBIEBdtPriRestri(
            BasicBusinessInformationEntityPropertyTreeNode bbiepNode) {
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
            BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode) {
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

    @Transactional
    public void update(AssociationBusinessInformationEntityPropertyTreeNode bieNode, User user) {
        BusinessInformationEntityTreeNodeUpdateHandler updateHandler =
                new BusinessInformationEntityTreeNodeUpdateHandler(bieNode, user);
        updateHandler.update();
    }

    private class BusinessInformationEntityTreeNodeUpdateHandler {
        private AssociationBusinessInformationEntityPropertyTreeNode root;
        private User user;

        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public BusinessInformationEntityTreeNodeUpdateHandler(
                AssociationBusinessInformationEntityPropertyTreeNode root,
                User user) {
            this.root = root;
            this.user = user;
        }

        public void update() {
            TopLevelAbie topLevelAbie = prepareForTopLevelAbieEntity();
            gatheringBusinessInformationEntities(root);

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
            long topLevelAbieId = root.getType().getAggregateBusinessInformationEntity().getOwnerTopLevelAbieId();
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

        private void gatheringBusinessInformationEntities(
                BusinessInformationEntityTreeNode node) {
            Collection<? extends BusinessInformationEntityTreeNode> children;

            if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                        (AssociationBusinessInformationEntityPropertyTreeNode) node;

                AssociationBusinessInformationEntity asbie =
                        asbiepNode.getAssociationBusinessInformationEntity();

                AssociationBusinessInformationEntityProperty asbiep =
                        asbiepNode.getAssociationBusinessInformationEntityProperty();

                AggregateBusinessInformationEntityTreeNode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAggregateBusinessInformationEntity();

                if (abie != null && abie.getAbieId() > 0L && abie.isDirty()) {
                    abieList.add(abie);
                } else if (asbie != null && asbie.getAsbieId() > 0L && asbie.isDirty()) {
                    asbieList.add(asbie);
                } else if (asbiep != null && asbiep.getAsbiepId() > 0L && asbiep.isDirty()) {
                    asbiepList.add(asbiep);
                } else {
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
                }

                if (abieNode instanceof AggregateBusinessInformationEntityTreeNodeImpl) {
                    children = ((AggregateBusinessInformationEntityTreeNodeImpl) abieNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                        (BasicBusinessInformationEntityPropertyTreeNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBasicBusinessInformationEntityProperty();

                if (bbie != null && bbie.getBbieId() > 0L && bbie.isDirty()) {
                    bbieList.add(bbie);
                } else if (bbiep != null && bbiep.getBbiepId() > 0L && bbiep.isDirty()) {
                    bbiepList.add(bbiep);
                } else if ((bbie != null && bbie.isDirty()) || (bbiep != null && bbiep.isDirty())) {
                    if (bbie != null) {
                        bbieList.add(bbie);
                    }
                    if (bbiep != null) {
                        bbiepList.add(bbiep);
                    }
                }

                if (bbiepNode instanceof BasicBusinessInformationEntityPropertyTreeNodeImpl) {
                    children = ((BasicBusinessInformationEntityPropertyTreeNodeImpl) bbiepNode).children;
                    if (children == null) {
                        children = Collections.emptyList();
                    }
                } else {
                    children = node.getChildren();
                }
            } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
                BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                        (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        handleBBIEScBdtScPriRestri(bbieScNode);
                if (bbieSc != null && bbieSc.isDirty()) {
                    bbieScList.add(bbieSc);
                }

                children = node.getChildren();
            } else {
                throw new IllegalStateException();
            }

            for (BusinessInformationEntityTreeNode child : children) {
                gatheringBusinessInformationEntities(child);
            }
        }
    }

    @Transactional
    public TopLevelAbie copy(AssociationBusinessInformationEntityPropertyTreeNode bieNode, User user,
                             BusinessContext bizCtx, ProgressListener progressListener) {
        BusinessInformationEntityTreeNodeCopyHandler copyHandler =
                new BusinessInformationEntityTreeNodeCopyHandler(bieNode, user, bizCtx);
        copyHandler.setProgressListener(progressListener);

        return copyHandler.copy();
    }

    private class BusinessInformationEntityTreeNodeCopyHandler {
        private AssociationBusinessInformationEntityPropertyTreeNode root;
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
                AssociationBusinessInformationEntityPropertyTreeNode root,
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
            for (BusinessInformationEntityTreeNode child : root.getChildren()) {
                removeIdentifierOfBusinessInformationEntities(child);
            }
            for (BusinessInformationEntityTreeNode child : root.getChildren()) {
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

            AggregateBusinessInformationEntity abie = root.getType().getAggregateBusinessInformationEntity();
            long abieId = abie.getAbieId();
            abie = abie.clone();
            preset(abie, topLevelAbie);

            prevAbieIdMap.put(abieId, abie);
            abie = abieRepository.saveAndFlush(abie);

            topLevelAbie.setAbie(abie);
            topLevelAbieRepository.save(topLevelAbie);

            // It has to be added whether it is dirty or not.
            AssociationBusinessInformationEntityProperty asbiep = root.getAssociationBusinessInformationEntityProperty();
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
                BusinessInformationEntityTreeNode node) {

            Collection<? extends BusinessInformationEntityTreeNode> children = null;

            if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                        (AssociationBusinessInformationEntityPropertyTreeNode) node;

                AssociationBusinessInformationEntity asbie =
                        asbiepNode.getAssociationBusinessInformationEntity();
                if (asbie != null && asbie.getAsbieId() > 0L) {
                    prevAsbieIdMap.put(asbie.getAsbieId(), asbie.clone());
                }

                AssociationBusinessInformationEntityProperty asbiep =
                        asbiepNode.getAssociationBusinessInformationEntityProperty();
                if (asbiep != null && asbiep.getAsbiepId() > 0L) {
                    prevAsbiepIdMap.put(asbiep.getAsbiepId(), asbiep.clone());
                }

                AggregateBusinessInformationEntityTreeNode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAggregateBusinessInformationEntity();
                if (abie != null && abie.getAbieId() > 0L) {
                    prevAbieIdMap.put(abie.getAbieId(), abie.clone());
                }

                if (abieNode instanceof AggregateBusinessInformationEntityTreeNodeImpl) {
                    children = ((AggregateBusinessInformationEntityTreeNodeImpl) abieNode).children;
                }
            } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                        (BasicBusinessInformationEntityPropertyTreeNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);
                if (bbie != null && bbie.getBbieId() > 0L) {
                    prevBbieIdMap.put(bbie.getBbieId(), bbie.clone());
                }

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBasicBusinessInformationEntityProperty();
                if (bbiep != null && bbiep.getBbiepId() > 0L) {
                    prevBbiepIdMap.put(bbiep.getBbiepId(), bbiep.clone());
                }

                if (bbiepNode instanceof BasicBusinessInformationEntityPropertyTreeNodeImpl) {
                    children = ((BasicBusinessInformationEntityPropertyTreeNodeImpl) bbiepNode).children;
                }
            } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
                BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                        (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        handleBBIEScBdtScPriRestri(bbieScNode);
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
                for (BusinessInformationEntityTreeNode child : children) {
                    removeIdentifierOfBusinessInformationEntities(child);
                }
            }
        }

        private void gatheringBusinessInformationEntities(
                BusinessInformationEntityTreeNode node) {

            Collection<? extends BusinessInformationEntityTreeNode> children = null;

            if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                        (AssociationBusinessInformationEntityPropertyTreeNode) node;

                AssociationBusinessInformationEntity asbie =
                        asbiepNode.getAssociationBusinessInformationEntity();

                AssociationBusinessInformationEntityProperty asbiep =
                        asbiepNode.getAssociationBusinessInformationEntityProperty();

                AggregateBusinessInformationEntityTreeNode abieNode = asbiepNode.getType();
                AggregateBusinessInformationEntity abie =
                        abieNode.getAggregateBusinessInformationEntity();

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

                if (abieNode instanceof AggregateBusinessInformationEntityTreeNodeImpl) {
                    children = ((AggregateBusinessInformationEntityTreeNodeImpl) abieNode).children;
                }
            } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                        (BasicBusinessInformationEntityPropertyTreeNode) node;

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                BasicBusinessInformationEntityProperty bbiep =
                        bbiepNode.getBasicBusinessInformationEntityProperty();

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

                if (bbiepNode instanceof BasicBusinessInformationEntityPropertyTreeNodeImpl) {
                    children = ((BasicBusinessInformationEntityPropertyTreeNodeImpl) bbiepNode).children;
                }
            } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
                BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                        (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node;

                BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                        handleBBIEScBdtScPriRestri(bbieScNode);

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
                for (BusinessInformationEntityTreeNode child : children) {
                    gatheringBusinessInformationEntities(child);
                }
            }
        }

        private boolean isDirtyOfAnyChild(Collection<? extends BusinessInformationEntityTreeNode> children) {
            for (BusinessInformationEntityTreeNode node : children) {
                if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
                    AssociationBusinessInformationEntityPropertyTreeNode asbiepNode =
                            (AssociationBusinessInformationEntityPropertyTreeNode) node;

                    AssociationBusinessInformationEntity asbie =
                            asbiepNode.getAssociationBusinessInformationEntity();

                    AssociationBusinessInformationEntityProperty asbiep =
                            asbiepNode.getAssociationBusinessInformationEntityProperty();

                    AggregateBusinessInformationEntityTreeNode abieNode = asbiepNode.getType();
                    AggregateBusinessInformationEntity abie =
                            abieNode.getAggregateBusinessInformationEntity();

                    if ((asbie != null && (asbie.isDirty() || asbie.getAsbieId() > 0L)) ||
                        (asbiep != null && (asbiep.isDirty() || asbiep.getAsbiepId() > 0L)) ||
                        (abie != null && (abie.isDirty()) || abie.getAbieId() > 0L)) {
                        return true;
                    }
                } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
                    BasicBusinessInformationEntityPropertyTreeNode bbiepNode =
                            (BasicBusinessInformationEntityPropertyTreeNode) node;

                    BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbiepNode);

                    BasicBusinessInformationEntityProperty bbiep =
                            bbiepNode.getBasicBusinessInformationEntityProperty();

                    if ((bbie != null && (bbie.isDirty() || bbie.getBbieId() > 0L)) ||
                        (bbiep != null && (bbiep.isDirty() || bbiep.getBbiepId() > 0L))) {
                        return true;
                    }
                } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
                    BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode =
                            (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node;

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
