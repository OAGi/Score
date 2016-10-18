package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.model.bod.impl.BaseTopLevelNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.beans.bod.CreateProfileBODBean;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Component
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
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbiescRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private int batchSize = 25;

    private class SubmitNodeVisitor implements NodeVisitor {

        private User user;
        private CreateProfileBODBean.ProgressListener progressListener;

        private TopLevelAbie topLevelAbie;
        private List<AggregateBusinessInformationEntity> abieList = new ArrayList();
        private List<AssociationBusinessInformationEntity> asbieList = new ArrayList();
        private List<AssociationBusinessInformationEntityProperty> asbiepList = new ArrayList();
        private List<BasicBusinessInformationEntity> bbieList = new ArrayList();
        private List<BasicBusinessInformationEntityProperty> bbiepList = new ArrayList();
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList = new ArrayList();

        public SubmitNodeVisitor(User user) {
            this.user = user;
        }

        public void setProgressListener(CreateProfileBODBean.ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            topLevelAbie = new TopLevelAbie();
            topLevelAbie.setAbie(topLevelNode.getAbie());
            asbiepList.add(topLevelNode.getAsbiep());
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            abieList.add(asbieNode.getAbie());
            asbieList.add(asbieNode.getAsbie());
            asbiepList.add(asbieNode.getAsbiep());
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            bbieList.add(bbieNode.getBbie());
            bbiepList.add(bbieNode.getBbiep());
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbiescNode) {
            bbiescList.add(bbiescNode.getBbiesc());
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            AggregateBusinessInformationEntity tAbie = topLevelAbie.getAbie();
            tAbie.setCreatedBy(user.getAppUserId());
            tAbie.setLastUpdatedBy(user.getAppUserId());
            tAbie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
            tAbie.setOwnerTopLevelAbie(topLevelAbie);
            tAbie.addPersistEventListener(progressListener);

            abieList.stream().forEach(abie -> {
                abie.setCreatedBy(user.getAppUserId());
                abie.setLastUpdatedBy(user.getAppUserId());
                abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
                abie.setOwnerTopLevelAbie(topLevelAbie);
                abie.addPersistEventListener(progressListener);
            });
            asbieList.stream().forEach(asbie -> {
                asbie.setCreatedBy(user.getAppUserId());
                asbie.setLastUpdatedBy(user.getAppUserId());
                asbie.setOwnerTopLevelAbie(topLevelAbie);
                asbie.addPersistEventListener(progressListener);
            });
            asbiepList.stream().forEach(asbiep -> {
                asbiep.setCreatedBy(user.getAppUserId());
                asbiep.setLastUpdatedBy(user.getAppUserId());
                asbiep.setOwnerTopLevelAbie(topLevelAbie);
                asbiep.addPersistEventListener(progressListener);
            });
            bbieList.stream().forEach(bbie -> {
                bbie.setCreatedBy(user.getAppUserId());
                bbie.setLastUpdatedBy(user.getAppUserId());
                bbie.setOwnerTopLevelAbie(topLevelAbie);
                bbie.addPersistEventListener(progressListener);
            });
            bbiepList.stream().forEach(bbiep -> {
                bbiep.setCreatedBy(user.getAppUserId());
                bbiep.setLastUpdatedBy(user.getAppUserId());
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
            saveTopLevelAbie();
            saveAbieList();
            saveBbiepList();
            saveBbieList();
            saveBbieScList();
            saveAsbiepList();
            saveAsbieList();

//            EntityManager entityManager = null;
//            EntityTransaction txn = null;
//            try {
//                entityManager = entityManagerFactory.createEntityManager();
//                txn = entityManager.getTransaction();
//                txn.begin();
//
//                saveTopLevelAbie(entityManager);
//                saveBatch(entityManager, abieList);
//                saveBatch(entityManager, bbiepList);
//                saveBatch(entityManager, bbieList);
//                saveBatch(entityManager, bbiescList);
//                saveBatch(entityManager, asbiepList);
//                saveBatch(entityManager, asbieList);
//
//                txn.commit();
//            } catch (RuntimeException e) {
//                if (txn != null && txn.isActive()) txn.rollback();
//                throw e;
//            } finally {
//                entityManager.close();
//            }
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

        private void saveTopLevelAbie(EntityManager entityManager) {
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
            topLevelAbie.setAbie(null);

            entityManager.persist(topLevelAbie);
            entityManager.flush();
            abie.setOwnerTopLevelAbie(topLevelAbie);

            entityManager.persist(abie);
            entityManager.flush();

            topLevelAbie.setAbie(abie);
            entityManager.persist(topLevelAbie);
            entityManager.flush();
        }

        private void saveBatch(EntityManager entityManager, List list) {
            for (int i = 0, len = list.size(); i < len; ++i) {
                entityManager.persist(list.get(i));

                if (i % batchSize == 0) {
                    // flush a batch of inserts and release memory
                    entityManager.flush();
                    entityManager.clear();
                }
            }
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

    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bizCtx) {
        Node node = nodeService.createNode(asccp, bizCtx);

        long s = System.currentTimeMillis();
        TreeNodeVisitor treeNodeVisitor = new TreeNodeVisitor();
        node.accept(treeNodeVisitor);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNodeVisitor.getRoot();
    }

    public TreeNode createTreeNode(TopLevelAbie topLevelAbie) {
        Node node = nodeService.createLazyNode(topLevelAbie);

        LazyTreeNodeVisitor lazyTreeNodeVisitor = new LazyTreeNodeVisitor();
        node.accept(lazyTreeNodeVisitor);
        return lazyTreeNodeVisitor.getParent();
    }

    public void expandLazyTreeNode(DefaultTreeNode treeNode) {
        LazyNode lazyNode = (LazyNode) treeNode.getData();
        if (!lazyNode.isFetched()) {
            lazyNode.fetch();

            LazyTreeNodeVisitor lazyTreeNodeVisitor = new LazyTreeNodeVisitor(treeNode);
            treeNode.setChildren(new ArrayList()); // clear children

            for (Node child : lazyNode.getChildren()) {
                child.accept(lazyTreeNodeVisitor);
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void submit(BaseTopLevelNode node, CreateProfileBODBean.ProgressListener progressListener) {
        SubmitNodeVisitor submitNodeVisitor = new SubmitNodeVisitor(loadAuthentication());
        submitNodeVisitor.setProgressListener(progressListener);
        node.accept(submitNodeVisitor);
    }


    private class UpdateNodeVisitor implements NodeVisitor {

        private User user;
        private List<AggregateBusinessInformationEntity> abieList = new ArrayList();
        private List<AssociationBusinessInformationEntity> asbieList = new ArrayList();
        private List<AssociationBusinessInformationEntityProperty> asbiepList = new ArrayList();
        private List<BasicBusinessInformationEntity> bbieList = new ArrayList();
        private List<BasicBusinessInformationEntityProperty> bbiepList = new ArrayList();
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbiescList = new ArrayList();

        public UpdateNodeVisitor(User user) {
            this.user = user;
        }

        @Override
        public void startNode(TopLevelNode topLevelNode) {
            AggregateBusinessInformationEntity abie = topLevelNode.getAbie();
            if (abie.isDirty()) {
                abieList.add(abie);
            }
            AssociationBusinessInformationEntityProperty asbiep = topLevelNode.getAsbiep();
            if (asbiep.isDirty()) {
                asbiepList.add(asbiep);
            }
        }

        @Override
        public void visitASBIENode(ASBIENode asbieNode) {
            AggregateBusinessInformationEntity abie = asbieNode.getAbie();
            if (abie.isDirty()) {
                abieList.add(abie);
            }
            AssociationBusinessInformationEntity asbie = asbieNode.getAsbie();
            if (asbie.isDirty()) {
                asbieList.add(asbie);
            }
            AssociationBusinessInformationEntityProperty asbiep = asbieNode.getAsbiep();
            if (asbiep.isDirty()) {
                asbiepList.add(asbiep);
            }
        }

        @Override
        public void visitBBIENode(BBIENode bbieNode) {
            BasicBusinessInformationEntity bbie = bbieNode.getBbie();
            if (bbie.isDirty()) {
                bbieList.add(bbie);
            }
            BasicBusinessInformationEntityProperty bbiep = bbieNode.getBbiep();
            if (bbiep.isDirty()) {
                bbiepList.add(bbiep);
            }
        }

        @Override
        public void visitBBIESCNode(BBIESCNode bbiescNode) {
            BasicBusinessInformationEntitySupplementaryComponent bbiesc = bbiescNode.getBbiesc();
            if (bbiesc.isDirty()) {
                bbiescList.add(bbiesc);
            }
        }

        @Override
        public void endNode() {
            adjust();
            save();
        }

        private void adjust() {
            abieList.stream().forEach(abie -> {
                abie.setLastUpdatedBy(user.getAppUserId());
            });
            asbieList.stream().forEach(asbie -> {
                asbie.setLastUpdatedBy(user.getAppUserId());
            });
            asbiepList.stream().forEach(asbiep -> {
                asbiep.setLastUpdatedBy(user.getAppUserId());
            });
            bbieList.stream().forEach(bbie -> {
                bbie.setLastUpdatedBy(user.getAppUserId());
            });
            bbiepList.stream().forEach(bbiep -> {
                bbiep.setLastUpdatedBy(user.getAppUserId());
            });
        }

        private void save() {
            abieRepository.save(abieList);
            asbieRepository.save(asbieList);
            asbiepRepository.save(asbiepList);
            bbieRepository.save(bbieList);
            bbiepRepository.save(bbiepList);
            bbiescRepository.save(bbiescList);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(org.oagi.srt.model.bod.TopLevelNode node) {
        UpdateNodeVisitor updateNodeVisitor = new UpdateNodeVisitor(loadAuthentication());
        node.accept(updateNodeVisitor);
    }

}
