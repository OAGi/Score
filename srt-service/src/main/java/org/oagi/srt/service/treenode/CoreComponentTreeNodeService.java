package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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
        private int childrenCount = -1;
        private Collection<CoreComponentPropertyTreeNode> children = null;

        private AggregateCoreComponentTreeNodeImpl(AggregateCoreComponent aggregateCoreComponent) {
            super(aggregateCoreComponent);
        }

        @Override
        public AggregateCoreComponentTreeNode getBase() {
            if (base == null) {
                AggregateCoreComponent acc = getRaw();
                long basedAccId = acc.getBasedAccId();
                AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
                base = new AggregateCoreComponentTreeNodeImpl(basedAcc);
            }
            return base;
        }

        @Override
        public int getChildrenCount() {
            if (childrenCount < 0) {
                AggregateCoreComponent acc = getRaw();
                long accId = acc.getAccId();

                int asccCount = asccRepository.countByFromAccId(accId);
                int bccCount = bccRepository.countByFromAccId(accId);

                childrenCount = asccCount + bccCount;
            }
            return childrenCount;
        }

        @Override
        public Collection<? extends CoreComponentPropertyTreeNode> getChildren() {
            if (children == null) {
                List<CoreComponentRelation> ccList = getAssociationList();

                if (ccList.isEmpty()) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList();

                    Collections.sort(ccList, Comparator.comparingInt(CoreComponentRelation::getSeqKey));
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

        private List<CoreComponentRelation> getAssociationList() {
            AggregateCoreComponent acc = getRaw();
            long accId = acc.getAccId();

            List<CoreComponentRelation> ccList = new ArrayList();
            List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(accId);
            ccList.addAll(asccList);
            List<BasicCoreComponent> bccList = bccRepository.findByFromAccId(accId);
            ccList.addAll(bccList);

            return ccList;
        }
    }

    private class AssociationCoreComponentPropertyTreeNodeImpl
            extends AbstractCoreComponentPropertyTreeNode<AssociationCoreComponentProperty, AssociationCoreComponent>
            implements AssociationCoreComponentPropertyTreeNode {

        private final AssociationCoreComponent ascc;
        private AggregateCoreComponentTreeNode type;

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
                AssociationCoreComponentProperty asccp = getRaw();
                long roleOfAccId = asccp.getRoleOfAccId();
                if (roleOfAccId > 0L) {
                    AggregateCoreComponent roleOfAcc = accRepository.findOne(roleOfAccId);
                    type = new AggregateCoreComponentTreeNodeImpl(roleOfAcc);
                }
            }
            return type;
        }
    }

    private class BasicCoreComponentPropertyTreeNodeImpl
            extends AbstractCoreComponentPropertyTreeNode<BasicCoreComponentProperty, BasicCoreComponent>
            implements BasicCoreComponentPropertyTreeNode {

        private final BasicCoreComponent bcc;
        private DataType dataType;

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
    }


}
