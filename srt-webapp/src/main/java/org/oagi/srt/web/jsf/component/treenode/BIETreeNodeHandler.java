package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.beans.bod.CreateProfileBODBean;
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

    @Transactional(rollbackFor = Throwable.class)
    public void submit(TopLevelNode node, CreateProfileBODBean.ProgressListener progressListener) {
        SubmitNodeVisitor submitNodeVisitor = new SubmitNodeVisitor(loadAuthentication());
        submitNodeVisitor.setProgressListener(progressListener);
        node.accept(submitNodeVisitor);
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
        Node node = nodeService.createNode(topLevelAbie);

        long s = System.currentTimeMillis();
        TreeNodeVisitor treeNodeVisitor = new TreeNodeVisitor();
        node.accept(treeNodeVisitor);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNodeVisitor.getRoot();
    }

}
