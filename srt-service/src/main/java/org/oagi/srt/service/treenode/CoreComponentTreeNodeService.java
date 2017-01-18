package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;

import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Service
@Transactional
public class CoreComponentTreeNodeService {

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
    private NamespaceRepository namespaceRepository;

    public AggregateCoreComponentTreeNode createCoreComponentTreeNode(
            AggregateCoreComponent aggregateCoreComponent) {
        if (aggregateCoreComponent == null) {
            throw new IllegalArgumentException("'aggregateCoreComponent' argument must not be null.");
        }
        return new AggregateCoreComponentTreeNodeImpl(aggregateCoreComponent);
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

    private abstract class AbstractCoreComponentTreeNode<R extends CoreComponent>
            implements CoreComponentTreeNode<R> {

        private final R raw;
        private final Namespace namespace;
        private Map<String, Object> attributes = new HashMap();

        private AbstractCoreComponentTreeNode(R raw) {
            this.raw = raw;

            if (raw instanceof NamespaceAware) {
                long namespaceId = ((NamespaceAware) raw).getNamespaceId();
                this.namespace = (namespaceId > 0L) ? namespaceRepository.findOne(namespaceId) : null;
            } else {
                this.namespace = null;
            }
        }

        @Override
        public String getId() {
            return raw.getGuid();
        }

        @Override
        public Namespace getNamespace() {
            return namespace;
        }

        @Override
        public R getRaw() {
            return raw;
        }

        @Override
        public void setAttribute(String key, Object attr) {
            attributes.put(key, attr);
        }

        @Override
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
    }

    private abstract class AbstractCoreComponentPropertyTreeNode
            <R extends CoreComponentProperty, A extends CoreComponentRelation>
            extends AbstractCoreComponentTreeNode<R>
            implements CoreComponentPropertyTreeNode<R, A> {

        private AggregateCoreComponentTreeNode parent;

        private AbstractCoreComponentPropertyTreeNode(R raw) {
            super(raw);
        }

        @Override
        public AggregateCoreComponentTreeNode getParent() {
            if (parent == null) {
                CoreComponentRelation coreComponentRelation = getRawRelation();
                long fromAccId = coreComponentRelation.getFromAccId();
                AggregateCoreComponent fromAcc = accRepository.findOne(fromAccId);
                parent = new AggregateCoreComponentTreeNodeImpl(fromAcc);
            }
            return parent;
        }

    }

    private class AggregateCoreComponentTreeNodeImpl
            extends AbstractCoreComponentTreeNode<AggregateCoreComponent>
            implements AggregateCoreComponentTreeNode {

        private AggregateCoreComponentTreeNode base;
        private Boolean hasChild = null;
        private Collection<CoreComponentTreeNode> children = null;

        private AggregateCoreComponentTreeNodeImpl(AggregateCoreComponent aggregateCoreComponent) {
            super(aggregateCoreComponent);
        }

        @Override
        public AggregateCoreComponentTreeNode getBase() {
            if (base == null) {
                AggregateCoreComponent acc = getRaw();
                long basedAccId = acc.getBasedAccId();
                if (basedAccId > 0L) {
                    AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
                    base = new AggregateCoreComponentTreeNodeImpl(basedAcc);
                }
            }
            return base;
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                AggregateCoreComponent acc = getRaw();
                long accId = acc.getAccId();

                int asccCount = asccRepository.countByFromAccIdAndRevisionNumAndState(
                        accId, 0, CoreComponentState.Published);
                if (asccCount > 0) {
                    hasChild = true;
                } else {
                    int bccCount = bccRepository.countByFromAccIdAndRevisionNumAndState(
                            accId, 0, CoreComponentState.Published);
                    if (bccCount > 0) {
                        hasChild = true;
                    } else {
                        hasChild = false;
                    }
                }
            }
            return hasChild;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                AggregateCoreComponent acc = getRaw();
                List<CoreComponentRelation> ccList = getAssociations(acc);

                if (ccList.isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();

                    for (CoreComponentRelation cc : ccList) {
                        if (cc instanceof AssociationCoreComponent) {
                            AssociationCoreComponentPropertyTreeNode asccpNode =
                                    createCoreComponentTreeNode((AssociationCoreComponent) cc);
                            children.add(asccpNode);
                        } else if (cc instanceof BasicCoreComponent) {
                            BasicCoreComponentPropertyTreeNode bccpNode =
                                    createCoreComponentTreeNode((BasicCoreComponent) cc);
                            children.add(bccpNode);
                        }
                    }
                }
            }

            return children;
        }

        private List<CoreComponentRelation> getAssociations(AggregateCoreComponent acc) {
            long accId = acc.getAccId();

            List<CoreComponentRelation> coreComponentRelations = new ArrayList();
            List<AssociationCoreComponent> asccList =
                    asccRepository.findByFromAccIdAndRevisionNum(accId, 0);

            for (int i = 0, len = asccList.size(); i < len; ++i) {
                AssociationCoreComponent ascc = asccList.get(i);
                AggregateCoreComponent roleOfAcc = getRoleOfAcc(ascc);
                if (isGroup(roleOfAcc)) {
                    coreComponentRelations.addAll(getAssociations(roleOfAcc));
                } else {
                    coreComponentRelations.add(ascc);
                }
            }
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
    }

    private class AssociationCoreComponentPropertyTreeNodeImpl
            extends AbstractCoreComponentPropertyTreeNode<AssociationCoreComponentProperty, AssociationCoreComponent>
            implements AssociationCoreComponentPropertyTreeNode {

        private final AssociationCoreComponent ascc;
        private AggregateCoreComponentTreeNode type;
        private Boolean hasChild = null;
        private Collection<CoreComponentTreeNode> children = null;

        private AssociationCoreComponentPropertyTreeNodeImpl(AssociationCoreComponent ascc) {
            super(asccpRepository.findOne(ascc.getToAsccpId()));
            this.ascc = ascc;
        }

        @Override
        public AssociationCoreComponent getRawRelation() {
            return ascc;
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
            AssociationCoreComponentProperty asccp = getRaw();
            long roleOfAccId = asccp.getRoleOfAccId();
            return roleOfAccId;
        }
    }

    private class BasicCoreComponentPropertyTreeNodeImpl
            extends AbstractCoreComponentPropertyTreeNode<BasicCoreComponentProperty, BasicCoreComponent>
            implements BasicCoreComponentPropertyTreeNode {

        private final BasicCoreComponent bcc;
        private DataType dataType;
        private Boolean hasChild = null;
        private Collection<CoreComponentPropertyTreeNode> children = null;

        private BasicCoreComponentPropertyTreeNodeImpl(BasicCoreComponent bcc) {
            super(bccpRepository.findOne(bcc.getToBccpId()));
            this.bcc = bcc;
        }

        @Override
        public BasicCoreComponent getRawRelation() {
            return bcc;
        }

        @Override
        public DataType getDataType() {
            if (dataType == null) {
                BasicCoreComponentProperty bccp = getRaw();
                long bdtId = bccp.getBdtId();
                dataType = dtRepository.findOne(bdtId);
            }
            return dataType;
        }

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
                hasChild = false;
            }
            return hasChild;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                children = Collections.emptyList();
            }
            return children;
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

}
