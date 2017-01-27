package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.*;
import org.oagi.srt.model.bie.impl.BaseTopLevelNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.*;

import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Editing;

@Component
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class BIETreeNodeHandler extends UIHandler {

    private static final Logger logger = LoggerFactory.getLogger(BIETreeNodeHandler.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbiescRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    private BasicBusinessInformationEntity handleBBIEBdtPriRestri(BBIENode bbieNode) {
        BasicBusinessInformationEntity bbie = bbieNode.getBbie();
        BBIERestrictionType restrictionType = bbieNode.getRestrictionType();
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

    private BasicBusinessInformationEntitySupplementaryComponent handleBBIEScBdtScPriRestri(BBIESCNode bbiescNode) {
        BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbiescNode.getBbieSc();
        BBIERestrictionType restrictionType = bbiescNode.getRestrictionType();
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

    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        BIENode node = nodeService.createBIENode(asccp, bizCtx);

        long s = System.currentTimeMillis();
        TreeBIENodeVisitor treeNodeVisitor = new TreeBIENodeVisitor();
        node.accept(treeNodeVisitor);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNodeVisitor.getRoot();
    }

    public TreeNode createTreeNode(TopLevelAbie topLevelAbie) {
        BIENode node = nodeService.createBIENode(topLevelAbie);

        long s = System.currentTimeMillis();
        TreeBIENodeVisitor treeNodeVisitor = new TreeBIENodeVisitor();
        node.accept(treeNodeVisitor);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNodeVisitor.getRoot();
    }

    public TreeNode createLazyTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        BIENode node = nodeService.createLazyBIENode(asccp, bizCtx);

        LazyTreeBIENodeVisitor lazyTreeNodeVisitor = new LazyTreeBIENodeVisitor();
        node.accept(lazyTreeNodeVisitor);
        return lazyTreeNodeVisitor.getParent();
    }

    public TreeNode createLazyTreeNode(TopLevelAbie topLevelAbie) {
        BIENode node = nodeService.createLazyBIENode(topLevelAbie);

        LazyTreeBIENodeVisitor lazyTreeNodeVisitor = new LazyTreeBIENodeVisitor();
        node.accept(lazyTreeNodeVisitor);
        return lazyTreeNodeVisitor.getParent();
    }

    public void expandLazyTreeNode(DefaultTreeNode treeNode) {
        LazyNode lazyNode = (LazyNode) treeNode.getData();
        if (!lazyNode.isFetched()) {
            lazyNode.fetch();

            LazyTreeBIENodeVisitor lazyTreeNodeVisitor = new LazyTreeBIENodeVisitor(treeNode);
            treeNode.setChildren(new ArrayList()); // clear children

            for (Node child : lazyNode.getChildren()) {
                ((BIENode) child).accept(lazyTreeNodeVisitor);
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void submit(BIENode node, ProgressListener progressListener) {
        User currentUser = getCurrentUser();
        UpdateBIENodeVisitor updateBIENodeVisitor = new UpdateBIENodeVisitor(currentUser);
        updateBIENodeVisitor.setProgressListener(progressListener);
        node.accept(updateBIENodeVisitor);
    }


    private class UpdateBIENodeVisitor implements BIENodeVisitor {

        private User user;
        private ProgressListener progressListener;

        private TopLevelAbie topLevelAbie;
        private Set<AggregateBusinessInformationEntity> abieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntity> asbieList = new LinkedHashSet();
        private Set<AssociationBusinessInformationEntityProperty> asbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntity> bbieList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntityProperty> bbiepList = new LinkedHashSet();
        private Set<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new LinkedHashSet();

        public UpdateBIENodeVisitor(User user) {
            this.user = user;
        }

        public void setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        private boolean isCreationProcess() {
            return (topLevelAbie == null || topLevelAbie.getTopLevelAbieId() == 0L);
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            topLevelAbie = topLevelNode.getTopLevelAbie();
            AggregateBusinessInformationEntity abie = topLevelNode.getAbie();
            AssociationBusinessInformationEntityProperty asbiep = topLevelNode.getAsbiep();

            if (isCreationProcess()) {
                topLevelAbie = new TopLevelAbie();
                topLevelAbie.setAbie(abie);
                asbiepList.add(asbiep);
            } else {
                if (abie.isDirty()) {
                    abieList.add(abie);
                }
                if (asbiep.isDirty()) {
                    asbiepList.add(asbiep);
                }
            }
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            AggregateBusinessInformationEntity abie = asbieNode.getAbie();
            AssociationBusinessInformationEntity asbie = asbieNode.getAsbie();
            AssociationBusinessInformationEntityProperty asbiep = asbieNode.getAsbiep();

            if (abie.getAbieId() > 0L) {
                if (asbie.isDirty()) {
                    asbieList.add(asbie);
                }
                if (abie.isDirty()) {
                    abieList.add(abie);
                }
                if (asbiep.isDirty()) {
                    asbiepList.add(asbiep);
                }
            } else {
                if (asbie.isDirty() || abie.isDirty() || asbiep.isDirty()) {
                    asbieList.add(asbie);
                    abieList.add(abie);
                    asbiepList.add(asbiep);
                }
            }
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbieNode);
            BasicBusinessInformationEntityProperty bbiep = bbieNode.getBbiep();

            if (bbie.getBbieId() > 0L) {
                if (bbie.isDirty()) {
                    bbieList.add(bbie);
                }
                if (bbiep.isDirty()) {
                    bbiepList.add(bbiep);
                }
            } else {
                if (bbie.isDirty() || bbiep.isDirty()) {
                    bbieList.add(bbie);
                    bbiepList.add(bbiep);
                }
            }
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbieScNode) {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbieScNode);
            if (bbieSc.isDirty()) {
                bbieScList.add(bbieSc);

                BBIENode bbieNode = (BBIENode) bbieScNode.getParent();

                BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbieNode);
                BasicBusinessInformationEntityProperty bbiep = bbieNode.getBbiep();

                bbieList.add(bbie);
                bbiepList.add(bbiep);
            }
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            long owner = user.getAppUserId();

            if (isCreationProcess()) {
                topLevelAbie.setOwnerUserId(owner);
                topLevelAbie.setState(Editing);

                AggregateBusinessInformationEntity tAbie = topLevelAbie.getAbie();
                tAbie.setCreatedBy(owner);
                tAbie.setLastUpdatedBy(owner);
                tAbie.setState(Editing);
                tAbie.setOwnerTopLevelAbie(topLevelAbie);
                tAbie.addPersistEventListener(progressListener);
            }

            abieList.stream().forEach(abie -> {
                if (abie.getAbieId() == 0L) {
                    abie.setCreatedBy(owner);
                    abie.setLastUpdatedBy(owner);
                    abie.setState(Editing);
                    abie.setOwnerTopLevelAbie(topLevelAbie);
                } else {
                    abie.setLastUpdatedBy(owner);
                }
                abie.addPersistEventListener(progressListener);
            });
            asbieList.stream().forEach(asbie -> {
                if (asbie.getAsbieId() == 0L) {
                    asbie.setCreatedBy(owner);
                    asbie.setLastUpdatedBy(owner);
                    asbie.setOwnerTopLevelAbie(topLevelAbie);
                } else {
                    asbie.setLastUpdatedBy(owner);
                }
                asbie.addPersistEventListener(progressListener);
            });
            asbiepList.stream().forEach(asbiep -> {
                if (asbiep.getAsbiepId() == 0L) {
                    asbiep.setCreatedBy(owner);
                    asbiep.setLastUpdatedBy(owner);
                    asbiep.setOwnerTopLevelAbie(topLevelAbie);
                } else {
                    asbiep.setLastUpdatedBy(owner);
                }
                asbiep.addPersistEventListener(progressListener);
            });
            bbieList.stream().forEach(bbie -> {
                if (bbie.getBbieId() == 0L) {
                    bbie.setCreatedBy(owner);
                    bbie.setLastUpdatedBy(owner);
                    bbie.setOwnerTopLevelAbie(topLevelAbie);
                } else {
                    bbie.setLastUpdatedBy(owner);
                }
                bbie.addPersistEventListener(progressListener);
            });
            bbiepList.stream().forEach(bbiep -> {
                if (bbiep.getBbiepId() == 0L) {
                    bbiep.setCreatedBy(owner);
                    bbiep.setLastUpdatedBy(owner);
                    bbiep.setOwnerTopLevelAbie(topLevelAbie);
                } else {
                    bbiep.setLastUpdatedBy(owner);
                }
                bbiep.addPersistEventListener(progressListener);
            });
            bbieScList.stream().forEach(bbiesc -> {
                if (bbiesc.getBbieScId() == 0L) {
                    bbiesc.setOwnerTopLevelAbie(topLevelAbie);
                }
                bbiesc.addPersistEventListener(progressListener);
            });

            if (progressListener != null) {
                int maxCount = abieList.size() + asbieList.size() + asbiepList.size() + bbieList.size() + bbiepList.size() + bbieScList.size();
                progressListener.setMaxCount(maxCount);
            }
        }

        private void save() {
            if (isCreationProcess()) {
                saveTopLevelAbie();
            }
            saveAbieList();
            saveBbiepList();
            saveBbieList();
            saveBbieScList();
            saveAsbiepList();
            saveAsbieList();
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
            bbiescRepository.save(bbieScList);
        }

        private void saveAsbiepList() {
            asbiepRepository.save(asbiepList);
        }

        private void saveAsbieList() {
            asbieRepository.save(asbieList);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(TopLevelNode node) {
        UpdateBIENodeVisitor updateNodeVisitor = new UpdateBIENodeVisitor(getCurrentUser());
        node.accept(updateNodeVisitor);
    }

    private class CopyBIENodeVisitor implements BIENodeVisitor {

        private User user;
        private BusinessContext bizCtx;
        private ProgressListener progressListener;

        private TopLevelAbie topLevelAbie;
        private Map<Long, AggregateBusinessInformationEntity> abieMap = new HashMap();
        private Map<Long, AssociationBusinessInformationEntity> asbieMap = new HashMap();
        private Map<Long, AssociationBusinessInformationEntityProperty> asbiepMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntity> bbieMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntityProperty> bbiepMap = new HashMap();
        private Map<Long, BasicBusinessInformationEntitySupplementaryComponent> bbiescMap = new HashMap();

        public CopyBIENodeVisitor(User user, BusinessContext bizCtx) {
            this.user = user;
            this.bizCtx = bizCtx;
        }

        public void setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            topLevelAbie = new TopLevelAbie();
            AggregateBusinessInformationEntity abie = topLevelNode.getAbie();
            abieMap.put(abie.getAbieId(), abie);
            topLevelAbie.setAbie(abie);
            AssociationBusinessInformationEntityProperty asbiep = topLevelNode.getAsbiep();
            asbiepMap.put(asbiep.getAsbiepId(), asbiep);
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            AggregateBusinessInformationEntity abie = asbieNode.getAbie();
            abieMap.put(abie.getAbieId(), abie);
            AssociationBusinessInformationEntity asbie = asbieNode.getAsbie();
            asbieMap.put(asbie.getAsbieId(), asbie);
            AssociationBusinessInformationEntityProperty asbiep = asbieNode.getAsbiep();
            asbiepMap.put(asbiep.getAsbiepId(), asbiep);
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbieNode);
            bbieMap.put(bbie.getBbieId(), bbie);
            BasicBusinessInformationEntityProperty bbiep = bbieNode.getBbiep();
            bbiepMap.put(bbiep.getBbiepId(), bbiep);
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbiescNode) {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbiescNode);
            bbiescMap.put(bbieSc.getBbieScId(), bbieSc);
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            long owner = user.getAppUserId();
            topLevelAbie.setOwnerUserId(owner);
            topLevelAbie.setState(Editing);

            asbieMap.values().stream().forEach(asbie -> {
                AggregateBusinessInformationEntity fromAbie = abieMap.get(asbie.getFromAbieId());
                asbie.setFromAbie(fromAbie);
                AssociationBusinessInformationEntityProperty toAsbiep = asbiepMap.get(asbie.getToAsbiepId());
                asbie.setToAsbiep(toAsbiep);
            });
            asbiepMap.values().stream().forEach(asbiep -> {
                AggregateBusinessInformationEntity roleOfAbie = abieMap.get(asbiep.getRoleOfAbieId());
                asbiep.setRoleOfAbie(roleOfAbie);
            });
            bbieMap.values().stream().forEach(bbie -> {
                AggregateBusinessInformationEntity fromAbie = abieMap.get(bbie.getFromAbieId());
                bbie.setFromAbie(fromAbie);
                BasicBusinessInformationEntityProperty toBbiep = bbiepMap.get(bbie.getToBbiepId());
                bbie.setToBbiep(toBbiep);
            });
            bbiescMap.values().stream().forEach(bbiesc -> {
                BasicBusinessInformationEntity bbie = bbieMap.get(bbiesc.getBbieId());
                bbiesc.setBbie(bbie);
            });

            AggregateBusinessInformationEntity tAbie = topLevelAbie.getAbie();
            tAbie.setAbieId(0L);
            tAbie.setGuid(Utility.generateGUID());
            tAbie.setCreatedBy(owner);
            tAbie.setLastUpdatedBy(owner);
            tAbie.setState(Editing);
            tAbie.setOwnerTopLevelAbie(topLevelAbie);
            tAbie.setBizCtxId(bizCtx.getBizCtxId());
            tAbie.addPersistEventListener(progressListener);

            abieMap.values().stream().forEach(abie -> {
                abie.setAbieId(0L);
                abie.setGuid(Utility.generateGUID());
                abie.setCreatedBy(owner);
                abie.setLastUpdatedBy(owner);
                abie.setState(Editing);
                abie.setOwnerTopLevelAbie(topLevelAbie);
                abie.setBizCtxId(bizCtx.getBizCtxId());
                abie.addPersistEventListener(progressListener);
            });
            asbieMap.values().stream().forEach(asbie -> {
                asbie.setAsbieId(0L);
                asbie.setFromAbieId(0L);
                asbie.setToAsbiepId(0L);
                asbie.setGuid(Utility.generateGUID());
                asbie.setCreatedBy(owner);
                asbie.setLastUpdatedBy(owner);
                asbie.setOwnerTopLevelAbie(topLevelAbie);
                asbie.addPersistEventListener(progressListener);
            });
            asbiepMap.values().stream().forEach(asbiep -> {
                asbiep.setAsbiepId(0L);
                asbiep.setRoleOfAbieId(0L);
                asbiep.setGuid(Utility.generateGUID());
                asbiep.setCreatedBy(owner);
                asbiep.setLastUpdatedBy(owner);
                asbiep.setOwnerTopLevelAbie(topLevelAbie);
                asbiep.addPersistEventListener(progressListener);
            });
            bbieMap.values().stream().forEach(bbie -> {
                bbie.setBbieId(0L);
                bbie.setFromAbieId(0L);
                bbie.setToBbiepId(0L);
                bbie.setGuid(Utility.generateGUID());
                bbie.setCreatedBy(owner);
                bbie.setLastUpdatedBy(owner);
                bbie.setOwnerTopLevelAbie(topLevelAbie);
                bbie.addPersistEventListener(progressListener);
            });
            bbiepMap.values().stream().forEach(bbiep -> {
                bbiep.setBbiepId(0L);
                bbiep.setGuid(Utility.generateGUID());
                bbiep.setCreatedBy(owner);
                bbiep.setLastUpdatedBy(owner);
                bbiep.setOwnerTopLevelAbie(topLevelAbie);
                bbiep.addPersistEventListener(progressListener);
            });
            bbiescMap.values().stream().forEach(bbiesc -> {
                bbiesc.setBbieScId(0L);
                bbiesc.setBbieId(0L);
                bbiesc.setGuid(Utility.generateGUID());
                bbiesc.setOwnerTopLevelAbie(topLevelAbie);
                bbiesc.addPersistEventListener(progressListener);
            });

            if (progressListener != null) {
                int maxCount = abieMap.size() + asbieMap.size() + asbiepMap.size() + bbieMap.size() + bbiepMap.size() + bbiescMap.size();
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

        private void saveAbieList() {
            abieRepository.save(abieMap.values());
        }

        private void saveBbiepList() {
            bbiepRepository.save(bbiepMap.values());
        }

        private void saveBbieList() {
            bbieRepository.save(bbieMap.values());
        }

        private void saveBbieScList() {
            bbiescRepository.save(bbiescMap.values());
        }

        private void saveAsbiepList() {
            asbiepRepository.save(asbiepMap.values());
        }

        private void saveAsbieList() {
            asbieRepository.save(asbieMap.values());
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void copy(BaseTopLevelNode node, BusinessContext bizCtx, ProgressListener progressListener) {
        CopyBIENodeVisitor copyNodeVisitor = new CopyBIENodeVisitor(getCurrentUser(), bizCtx);
        copyNodeVisitor.setProgressListener(progressListener);
        node.accept(copyNodeVisitor);
    }


    private class AppendBIENodeVisitor implements BIENodeVisitor {

        private User user;
        private ProgressListener progressListener;

        private TopLevelAbie topLevelAbie;
        private List<AggregateBusinessInformationEntity> abieList = new ArrayList();
        private List<AssociationBusinessInformationEntity> asbieList = new ArrayList();
        private List<AssociationBusinessInformationEntityProperty> asbiepList = new ArrayList();
        private List<BasicBusinessInformationEntity> bbieList = new ArrayList();
        private List<BasicBusinessInformationEntityProperty> bbiepList = new ArrayList();
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList = new ArrayList();

        public AppendBIENodeVisitor(TopLevelAbie topLevelAbie, User user) {
            this.topLevelAbie = topLevelAbie;
            this.user = user;
        }

        public void setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            AggregateBusinessInformationEntity abie = topLevelNode.getAbie();
            if (abie != null) {
                abieList.add(abie);
            }
            AssociationBusinessInformationEntityProperty asbiep = topLevelNode.getAsbiep();
            if (asbiep != null) {
                asbiepList.add(asbiep);
            }
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            AggregateBusinessInformationEntity abie = asbieNode.getAbie();
            if (abie != null) {
                abieList.add(abie);
            }
            AssociationBusinessInformationEntity asbie = asbieNode.getAsbie();
            if (asbie != null) {
                asbieList.add(asbie);
            }
            AssociationBusinessInformationEntityProperty asbiep = asbieNode.getAsbiep();
            if (asbiep != null) {
                asbiepList.add(asbiep);
            }
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            BasicBusinessInformationEntity bbie = handleBBIEBdtPriRestri(bbieNode);
            if (bbie != null) {
                bbieList.add(bbie);
            }
            BasicBusinessInformationEntityProperty bbiep = bbieNode.getBbiep();
            if (bbiep != null) {
                bbiepList.add(bbiep);
            }
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbiescNode) {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = handleBBIEScBdtScPriRestri(bbiescNode);
            if (bbieSc != null) {
                bbiescList.add(bbieSc);
            }
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            long owner = user.getAppUserId();

            abieList.stream().forEach(abie -> {
                abie.setCreatedBy(owner);
                abie.setLastUpdatedBy(owner);
                abie.setState(Editing);
                abie.setOwnerTopLevelAbie(topLevelAbie);
                abie.addPersistEventListener(progressListener);
            });
            asbieList.stream().forEach(asbie -> {
                asbie.setCreatedBy(owner);
                asbie.setLastUpdatedBy(owner);
                asbie.setOwnerTopLevelAbie(topLevelAbie);
                asbie.addPersistEventListener(progressListener);
            });
            asbiepList.stream().forEach(asbiep -> {
                asbiep.setCreatedBy(owner);
                asbiep.setLastUpdatedBy(owner);
                asbiep.setOwnerTopLevelAbie(topLevelAbie);
                asbiep.addPersistEventListener(progressListener);
            });
            bbieList.stream().forEach(bbie -> {
                bbie.setCreatedBy(owner);
                bbie.setLastUpdatedBy(owner);
                bbie.setOwnerTopLevelAbie(topLevelAbie);
                bbie.addPersistEventListener(progressListener);
            });
            bbiepList.stream().forEach(bbiep -> {
                bbiep.setCreatedBy(owner);
                bbiep.setLastUpdatedBy(owner);
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
            saveAbieList();
            saveBbiepList();
            saveBbieList();
            saveBbieScList();
            saveAsbiepList();
            saveAsbieList();
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
    public void append(BIENode node, TopLevelAbie topLevelAbie) {
        User currentUser = getCurrentUser();
        AppendBIENodeVisitor appendNodeVisitor = new AppendBIENodeVisitor(topLevelAbie, currentUser);
        node.accept(appendNodeVisitor);
    }

}
