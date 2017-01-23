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

    private abstract class AbstractCoreComponentTreeNode implements CoreComponentTreeNode {

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
            extends AbstractCoreComponentTreeNode
            implements AggregateCoreComponentTreeNode {

        private final AggregateCoreComponent acc;
        private AggregateCoreComponentTreeNode base;
        private Boolean hasChild = null;
        private Collection<CoreComponentTreeNode> children = null;

        private AggregateCoreComponentTreeNodeImpl(AggregateCoreComponent aggregateCoreComponent) {
            this.acc = aggregateCoreComponent;
        }

        @Override
        public AggregateCoreComponent getAggregateCoreComponent() {
            return acc;
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

        @Override
        public boolean hasChild() {
            if (hasChild == null) {
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
                List<CoreComponentRelation> ccList = getAssociations(acc);

                if (ccList.isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();

                    for (CoreComponentRelation cc : ccList) {
                        if (cc instanceof AssociationCoreComponent) {
                            AssociationCoreComponentPropertyTreeNode asccpNode =
                                    createCoreComponentTreeNode(this, (AssociationCoreComponent) cc);
                            children.add(asccpNode);
                        } else if (cc instanceof BasicCoreComponent) {
                            BasicCoreComponentPropertyTreeNode bccpNode =
                                    createCoreComponentTreeNode(this, (BasicCoreComponent) cc);
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

        @Override
        public void reload() {
            hasChild = null;
            children = null;
        }
    }

    private class AssociationCoreComponentPropertyTreeNodeImpl
            extends AbstractCoreComponentTreeNode
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
        public AssociationCoreComponentProperty getAssociationCoreComponentProperty() {
            return asccp;
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
            extends AbstractCoreComponentTreeNode
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
        public BasicCoreComponentProperty getBasicCoreComponentProperty() {
            return bccp;
        }

        @Override
        public DataType getDataType() {
            if (dataType == null) {
                long bdtId = getBasicCoreComponentProperty().getBdtId();
                dataType = dtRepository.findOne(bdtId);
            }
            return dataType;
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
                long bdtId = getDataType().getDtId();
                hasChild = dtScRepository.countByOwnerDtId(bdtId) > 0;
            }
            return hasChild;
        }

        @Override
        public Collection<? extends CoreComponentTreeNode> getChildren() {
            if (children == null) {
                long bdtId = getDataType().getDtId();
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
            extends AbstractCoreComponentTreeNode
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
        public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent() {
            return dtSc;
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

}
