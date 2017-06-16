package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Element;
import static org.oagi.srt.repository.entity.CoreComponentState.Editing;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;
import static org.oagi.srt.repository.entity.RevisionAction.Insert;

@Service
@Transactional(readOnly = true)
public class ExtensionService {

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private JpaRepositoryDefinitionHelper jpaRepositoryDefinitionHelper;

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent appendUserExtension(AggregateCoreComponent eAcc, AggregateCoreComponent ueAcc,
                                                      AssociationCoreComponentProperty asccp, User user)
            throws PermissionDeniedDataAccessException {
        if (!"Extension".equals(asccp.getPropertyTerm())) {
            throw new IllegalArgumentException("Can't append user extension on this ASCCP: " + asccp);
        }

        if (ueAcc != null) {
            updateRevisionNumberOfUserExtensionGroupACC(eAcc, ueAcc, user);
        } else {
            createNewUserExtensionGroupACC(eAcc, user);
        }

        return eAcc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent appendUserExtension(
            AssociationCoreComponentProperty asccp, User user, boolean isLocally) throws PermissionDeniedDataAccessException {
        if (!"Extension".equals(asccp.getPropertyTerm())) {
            throw new IllegalArgumentException("Can't append user extension on this ASCCP: " + asccp);
        }

        AggregateCoreComponent eAcc = getExtensionAcc(asccp, isLocally);
        AggregateCoreComponent ueAcc = getExistsUserExtension(eAcc);

        return appendUserExtension(eAcc, ueAcc, asccp, user);
    }

    public AggregateCoreComponent getExtensionAcc(AssociationCoreComponentProperty asccp, boolean isLocally) {
        AggregateCoreComponent eAcc = accRepository.findOne(asccp.getRoleOfAccId());
        if (!isLocally) { // isGlobally
            eAcc = getAllExtensionAcc(eAcc);
        }
        return eAcc;
    }

    private AggregateCoreComponent getAllExtensionAcc(AggregateCoreComponent eAcc) {
        while (!"All Extension".equals(eAcc.getObjectClassTerm())) {
            long basedAccId = eAcc.getBasedAccId();
            if (basedAccId > 0L) {
                eAcc = accRepository.findOne(basedAccId);
            } else {
                throw new IllegalStateException();
            }
        }
        return eAcc;
    }

    public AggregateCoreComponent getExistsUserExtension(AggregateCoreComponent eAcc) {
        for (AssociationCoreComponent ascc : asccRepository.findByFromAccIdAndRevisionNum(eAcc.getAccId(), 0)) {
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
            AggregateCoreComponent acc = accRepository.findOne(asccp.getRoleOfAccId());
            if (acc.getOagisComponentType() == UserExtensionGroup) {
                return acc;
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent createNewUserExtensionGroupACC(
            AggregateCoreComponent eAcc, User currentLoginUser) {
        AggregateCoreComponent ueAcc = createACCForExtension(eAcc, currentLoginUser);
        createACCHistoryForExtension(ueAcc, 1);

        AssociationCoreComponentProperty ueAsccp = createASCCPForExtension(eAcc, currentLoginUser, ueAcc);
        createASCCPHistoryForExtension(ueAsccp, 1);

        AssociationCoreComponent ueAscc = createASCCForExtension(eAcc, currentLoginUser, ueAcc, ueAsccp);
        createASCCHistoryForExtension(ueAscc, 1);

        return ueAcc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent updateRevisionNumberOfUserExtensionGroupACC(
            AggregateCoreComponent eAcc, AggregateCoreComponent ueAcc, User currentLoginUser) {
        updateStateACCForException(ueAcc, currentLoginUser);
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(ueAcc.getAccId());
        int latestRevisionNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        createACCHistoryForExtension(ueAcc, latestRevisionNum + 1);

        AssociationCoreComponentProperty ueAsccp = updateASCCPForExtension(ueAcc, currentLoginUser);
        List<AssociationCoreComponentProperty> latestHistoryAsccpList =
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(ueAsccp.getAsccpId());
        latestRevisionNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        createASCCPHistoryForExtension(ueAsccp, latestRevisionNum + 1);

        AssociationCoreComponent ueAscc = updateASCCForException(eAcc, ueAsccp, currentLoginUser);
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(ueAscc.getAsccId());
        latestRevisionNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        createASCCHistoryForExtension(ueAscc, latestRevisionNum + 1);

        return ueAcc;
    }

    private AggregateCoreComponent createACCForExtension(AggregateCoreComponent eAcc, User currentLoginUser) {
        long userId = currentLoginUser.getAppUserId();
        AggregateCoreComponent ueAcc = new AggregateCoreComponent();
        ueAcc.setGuid(Utility.generateGUID());
        ueAcc.setObjectClassTerm(Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm()));
        ueAcc.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAcc.setOagisComponentType(UserExtensionGroup);
        ueAcc.setCreatedBy(userId);
        ueAcc.setLastUpdatedBy(userId);
        ueAcc.setOwnerUserId(userId);
        ueAcc.setState(Editing);
        ueAcc.setRevisionNum(0);
        ueAcc.setRevisionTrackingNum(0);
        ueAcc.setNamespaceId(namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10"));
        return jpaRepositoryDefinitionHelper.saveAndFlush(ueAcc);
    }

    private AggregateCoreComponent updateStateACCForException(AggregateCoreComponent ueAcc, User currentLoginUser) {
        ueAcc.setState(Editing);
        ueAcc.addUpdateEventListener(new CreatorModifierAwareEventListener(currentLoginUser));
        ueAcc.setOwnerUserId(currentLoginUser.getAppUserId());
        jpaRepositoryDefinitionHelper.save(ueAcc);
        return ueAcc;
    }

    private void createACCHistoryForExtension(AggregateCoreComponent ueAcc, int revisionNum) {
        AggregateCoreComponent accHistory = ueAcc.clone();
        accHistory.setRevisionNum(revisionNum);
        accHistory.setRevisionTrackingNum(1);
        accHistory.setRevisionAction(Insert);
        jpaRepositoryDefinitionHelper.saveAndFlush(accHistory);
    }

    private AssociationCoreComponentProperty createASCCPForExtension(AggregateCoreComponent eAcc,
                                                                     User currentLoginUser,
                                                                     AggregateCoreComponent ueAcc) {
        AssociationCoreComponentProperty ueAsccp = createASCCP(ueAcc, currentLoginUser);
        ueAsccp.setPropertyTerm(ueAsccp.getPropertyTerm(), ueAcc);
        ueAsccp.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAsccp.setState(Published);
        return jpaRepositoryDefinitionHelper.saveAndFlush(ueAsccp);
    }

    private AssociationCoreComponentProperty updateASCCPForExtension(AggregateCoreComponent ueAcc, User currentLoginUser) {
        long roleOfAccId = ueAcc.getAccId();
        List<AssociationCoreComponentProperty> asccpList = asccpRepository.findByRoleOfAccId(roleOfAccId);
        if (asccpList.size() != 1) {
            throw new IllegalStateException();
        }
        AssociationCoreComponentProperty asccp = asccpList.get(0);
        asccp.addUpdateEventListener(new CreatorModifierAwareEventListener(currentLoginUser));
        asccp.setOwnerUserId(currentLoginUser.getAppUserId());
        return jpaRepositoryDefinitionHelper.save(asccp);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponentProperty ueAsccp, int revisionNum) {
        AssociationCoreComponentProperty asccpHistory = createASCCPHistory(ueAsccp, revisionNum);
        jpaRepositoryDefinitionHelper.saveAndFlush(asccpHistory);
    }

    private AssociationCoreComponent createASCCForExtension(AggregateCoreComponent eAcc,
                                                            User currentLoginUser,
                                                            AggregateCoreComponent ueAcc,
                                                            AssociationCoreComponentProperty ueAsccp) {
        AssociationCoreComponent ueAscc = createASCC(eAcc, ueAsccp, currentLoginUser, 1);
        ueAscc.setCardinalityMin(1);
        ueAscc.setDefinition("System created association to the system created user extension group component - " + ueAcc.getObjectClassTerm() + ".");
        ueAscc.setState(Published);
        return jpaRepositoryDefinitionHelper.saveAndFlush(ueAscc);
    }

    private AssociationCoreComponent updateASCCForException(AggregateCoreComponent eAcc,
                                                            AssociationCoreComponentProperty ueAsccp,
                                                            User currentLoginUser) {
        long fromAccId = eAcc.getAccId();
        long toAsccpId = ueAsccp.getAsccpId();
        AssociationCoreComponent ascc = asccRepository.findByFromAccIdAndToAsccpIdAndRevisionNumAndState(
                fromAccId, toAsccpId, 0, Published);
        ascc.addUpdateEventListener(new CreatorModifierAwareEventListener(currentLoginUser));
        ascc.setOwnerUserId(currentLoginUser.getAppUserId());
        return jpaRepositoryDefinitionHelper.save(ascc);
    }

    private void createASCCHistoryForExtension(AssociationCoreComponent ueAscc, int revisionNum) {
        AssociationCoreComponent asccHistory = createASCCHistory(ueAscc, revisionNum);
        jpaRepositoryDefinitionHelper.saveAndFlush(asccHistory);
    }

    private AssociationCoreComponentProperty createASCCP(AggregateCoreComponent ueAcc, User owner) {
        long userId = owner.getAppUserId();
        AssociationCoreComponentProperty ueAsccp = new AssociationCoreComponentProperty();
        ueAsccp.setGuid(Utility.generateGUID());
        ueAsccp.setPropertyTerm("A new ASCCP property");
        ueAsccp.setRoleOfAccId(ueAcc.getAccId());
        ueAsccp.setDen(ueAsccp.getPropertyTerm() + ". " + ueAcc.getObjectClassTerm());
        ueAsccp.setCreatedBy(userId);
        ueAsccp.setLastUpdatedBy(userId);
        ueAsccp.setOwnerUserId(userId);
        ueAsccp.setReusableIndicator(false);
        ueAsccp.setRevisionNum(0);
        ueAsccp.setRevisionTrackingNum(0);
        ueAsccp.setNamespaceId(ueAcc.getNamespaceId());
        return ueAsccp;
    }

    private AssociationCoreComponentProperty createASCCPHistory(AssociationCoreComponentProperty tAsccp, int revisionNum) {
        AssociationCoreComponentProperty asccpHistory = tAsccp.clone();
        asccpHistory.setRevisionNum(revisionNum);
        asccpHistory.setRevisionTrackingNum(1);
        asccpHistory.setRevisionAction(Insert);
        asccpHistory.setCurrentAsccpId(tAsccp.getAsccpId());
        return asccpHistory;
    }

    private AssociationCoreComponent createASCC(AggregateCoreComponent pAcc,
                                                AssociationCoreComponentProperty tAsccp,
                                                User owner, int seqKey) {
        long userId = owner.getAppUserId();
        AssociationCoreComponent ascc = new AssociationCoreComponent();
        ascc.setGuid(Utility.generateGUID());
        ascc.setCardinalityMin(0);
        ascc.setCardinalityMax(1);
        ascc.setSeqKey(seqKey);
        ascc.setFromAccId(pAcc.getAccId());
        ascc.setToAsccpId(tAsccp.getAsccpId());
        ascc.setDen(pAcc, tAsccp);
        ascc.setDefinition(null);
        ascc.setDeprecated(false);
        ascc.setCreatedBy(userId);
        ascc.setLastUpdatedBy(userId);
        ascc.setOwnerUserId(userId);
        ascc.setState(Editing);
        ascc.setRevisionNum(0);
        ascc.setRevisionTrackingNum(0);
        return ascc;
    }

    private AssociationCoreComponent createASCCHistory(AssociationCoreComponent tAscc, int revisionNum) {
        AssociationCoreComponent asccHistory = tAscc.clone();
        asccHistory.setRevisionNum(revisionNum);
        asccHistory.setRevisionTrackingNum(1);
        asccHistory.setRevisionAction(Insert);
        asccHistory.setCurrentAsccId(tAscc.getAsccId());
        return asccHistory;
    }


    private BasicCoreComponentProperty createBCCP(AggregateCoreComponent tAcc, User owner, DataType tBdt) {
        long userId = owner.getAppUserId();
        BasicCoreComponentProperty bccp = new BasicCoreComponentProperty();
        bccp.setGuid(Utility.generateGUID());
        bccp.setPropertyTerm("A new BCCP property");
        bccp.setRepresentationTerm(tBdt.getDataTypeTerm());
        bccp.setBdtId(tBdt.getDtId());
        bccp.setDen(bccp.getPropertyTerm() + ". " + tAcc.getObjectClassTerm());
        bccp.setCreatedBy(userId);
        bccp.setLastUpdatedBy(userId);
        bccp.setOwnerUserId(userId);
        bccp.setState(Editing);
        bccp.setRevisionNum(0);
        bccp.setRevisionTrackingNum(0);
        bccp.setNamespaceId(tAcc.getNamespaceId());
        return bccp;
    }

    private BasicCoreComponentProperty createBCCPHistory(BasicCoreComponentProperty tBccp) {
        BasicCoreComponentProperty bccpHistory = tBccp.clone();
        bccpHistory.setRevisionNum(1);
        bccpHistory.setRevisionTrackingNum(1);
        bccpHistory.setRevisionAction(Insert);
        bccpHistory.setCurrentBccpId(tBccp.getBccpId());
        return bccpHistory;
    }

    private BasicCoreComponent createBCC(AggregateCoreComponent pAcc,
                                         BasicCoreComponentProperty tBccp,
                                         User owner, int seqKey) {
        long userId = owner.getAppUserId();
        BasicCoreComponent bcc = new BasicCoreComponent();
        bcc.setGuid(Utility.generateGUID());
        bcc.setCardinalityMin(0);
        bcc.setCardinalityMax(1);
        bcc.setSeqKey(seqKey);
        bcc.setEntityType(Element);
        bcc.setFromAccId(pAcc.getAccId());
        bcc.setToBccpId(tBccp.getBccpId());
        bcc.setDen(pAcc, tBccp);
        bcc.setDefinition(null);
        bcc.setDeprecated(false);
        bcc.setCreatedBy(userId);
        bcc.setLastUpdatedBy(userId);
        bcc.setOwnerUserId(userId);
        bcc.setState(Editing);
        bcc.setRevisionNum(0);
        bcc.setRevisionTrackingNum(0);
        return bcc;
    }

    private BasicCoreComponent createBCCHistory(BasicCoreComponent tBcc) {
        BasicCoreComponent bccHistory = tBcc.clone();
        bccHistory.setRevisionNum(1);
        bccHistory.setRevisionTrackingNum(1);
        bccHistory.setRevisionAction(Insert);
        bccHistory.setCurrentBccId(tBcc.getBccId());
        return bccHistory;
    }

    public AggregateCoreComponent findUserExtensionAcc(AggregateCoreComponent acc) {
        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(acc.getAccId());
        String term = Utility.getUserExtensionGroupObjectClassTerm(acc.getObjectClassTerm());

        asccList = asccList.stream()
                .filter(e -> e.getRevisionNum() == 0 && e.getDen().contains(term))
                .collect(Collectors.toList());
        if (asccList.isEmpty()) {
            return null;
        } else if (asccList.size() > 1) {
            throw new IllegalStateException();
        }

        AssociationCoreComponent ascc = asccList.get(0);
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
        if (asccp == null) {
            throw new IllegalStateException();
        }
        return accRepository.findOne(asccp.getRoleOfAccId());
    }

    public static class AppendAsccResult {

        private final AssociationCoreComponent ascc;
        private final AssociationCoreComponent asccHistory;

        public AppendAsccResult(AssociationCoreComponent ascc,
                                AssociationCoreComponent asccHistory) {
            this.ascc = ascc;
            this.ascc.afterLoaded();

            this.asccHistory = asccHistory;
        }

        public AssociationCoreComponent getAscc() {
            return ascc;
        }

        public AssociationCoreComponent getAsccHistory() {
            return asccHistory;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public AppendAsccResult appendAsccTo(AggregateCoreComponent pAcc,
                                         AssociationCoreComponentProperty tAsccp,
                                         User user) {

        int seqKey = nextSeqKey(pAcc);
        AssociationCoreComponent tAscc = createASCC(pAcc, tAsccp, user, seqKey);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAscc);

        AssociationCoreComponent tAsccHistory = createASCCHistory(tAscc, tAscc.getRevisionNum() + 1);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAsccHistory);

        return new AppendAsccResult(tAscc, tAsccHistory);
    }

    public boolean exists(AggregateCoreComponent pAcc, AssociationCoreComponentProperty tAsccp) {
        return !asccRepository.findByFromAccIdAndToAsccpId(pAcc.getAccId(), tAsccp.getAsccpId()).isEmpty();
    }

    public static class CreateAsccResult {
        private final AssociationCoreComponentProperty asccp;
        private final AssociationCoreComponentProperty asccpHistory;

        private final AssociationCoreComponent ascc;
        private final AssociationCoreComponent asccHistory;

        private CreateAsccResult(AssociationCoreComponentProperty asccp,
                                 AssociationCoreComponentProperty asccpHistory,
                                 AssociationCoreComponent ascc,
                                 AssociationCoreComponent asccHistory) {
            this.asccp = asccp;
            this.asccpHistory = asccpHistory;
            this.ascc = ascc;
            this.asccHistory = asccHistory;
        }

        public AssociationCoreComponentProperty getAsccp() {
            return asccp;
        }

        public AssociationCoreComponentProperty getAsccpHistory() {
            return asccpHistory;
        }

        public AssociationCoreComponent getAscc() {
            return ascc;
        }

        public AssociationCoreComponent getAsccHistory() {
            return asccHistory;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public CreateAsccResult createAsccTo(AggregateCoreComponent pAcc, User user) {
        AssociationCoreComponentProperty tAsccp = createASCCP(pAcc, user);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAsccp);

        AssociationCoreComponentProperty tAsccpHistory = createASCCPHistory(tAsccp, tAsccp.getRevisionNum() + 1);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAsccpHistory);

        int seqKey = nextSeqKey(pAcc);
        AssociationCoreComponent tAscc = createASCC(pAcc, tAsccp, user, seqKey);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAscc);

        AssociationCoreComponent tAsccHistory = createASCCHistory(tAscc, tAscc.getRevisionNum() + 1);
        jpaRepositoryDefinitionHelper.saveAndFlush(tAsccHistory);

        return new CreateAsccResult(tAsccp, tAsccpHistory, tAscc, tAsccHistory);
    }

    public static class AppendBccResult {

        private final BasicCoreComponent bcc;
        private final BasicCoreComponent bccHistory;

        public AppendBccResult(BasicCoreComponent bcc, BasicCoreComponent bccHistory) {
            this.bcc = bcc;
            this.bcc.afterLoaded();

            this.bccHistory = bccHistory;
        }

        public BasicCoreComponent getBcc() {
            return bcc;
        }

        public BasicCoreComponent getBccHistory() {
            return bccHistory;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public AppendBccResult appendBccTo(AggregateCoreComponent pAcc,
                                       BasicCoreComponentProperty tBccp,
                                       User user) {
        int seqKey = nextSeqKey(pAcc);
        BasicCoreComponent tBcc = createBCC(pAcc, tBccp, user, seqKey);
        jpaRepositoryDefinitionHelper.saveAndFlush(tBcc);

        BasicCoreComponent tBccHistory = createBCCHistory(tBcc);
        jpaRepositoryDefinitionHelper.saveAndFlush(tBccHistory);

        return new AppendBccResult(tBcc, tBccHistory);
    }

    public boolean exists(AggregateCoreComponent pAcc, BasicCoreComponentProperty tBccp) {
        return !bccRepository.findByFromAccIdAndToBccpId(pAcc.getAccId(), tBccp.getBccpId()).isEmpty();
    }

    public static class CreateBccResult {
        private final BasicCoreComponentProperty bccp;
        private final BasicCoreComponentProperty bccpHistory;

        private final BasicCoreComponent bcc;
        private final BasicCoreComponent bccHistory;

        private CreateBccResult(BasicCoreComponentProperty bccp,
                                BasicCoreComponentProperty bccpHistory,
                                BasicCoreComponent bcc,
                                BasicCoreComponent bccHistory) {
            this.bccp = bccp;
            this.bccpHistory = bccpHistory;
            this.bcc = bcc;
            this.bccHistory = bccHistory;
        }

        public BasicCoreComponentProperty getBccp() {
            return bccp;
        }

        public BasicCoreComponentProperty getBccpHistory() {
            return bccpHistory;
        }

        public BasicCoreComponent getBcc() {
            return bcc;
        }

        public BasicCoreComponent getBccHistory() {
            return bccHistory;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public CreateBccResult createBccTo(AggregateCoreComponent pAcc, User user, DataType tBdt) {
        BasicCoreComponentProperty tBccp = createBCCP(pAcc, user, tBdt);
        BasicCoreComponentProperty tBccpHistory = createBCCPHistory(tBccp);
        jpaRepositoryDefinitionHelper.save(Arrays.asList(tBccp, tBccpHistory));

        int seqKey = nextSeqKey(pAcc);
        BasicCoreComponent tBcc = createBCC(pAcc, tBccp, user, seqKey);
        BasicCoreComponent tBccHistory = createBCCHistory(tBcc);
        jpaRepositoryDefinitionHelper.save(Arrays.asList(tBcc, tBccHistory));

        return new CreateBccResult(tBccp, tBccpHistory, tBcc, tBccHistory);
    }

    private int nextSeqKey(AggregateCoreComponent acc) {
        int nextSeqKey = coreComponentService.getMaxSeqKeyOfChildren(acc) + 1;
        ensureNextSeqKey(nextSeqKey, acc);
        return nextSeqKey;
    }

    private void ensureNextSeqKey(int nextSeqKey, AggregateCoreComponent acc) {
        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccIdAndRevisionNum(acc.getAccId(), 0);
        List<BasicCoreComponent> bccList = bccRepository.findByFromAccIdAndRevisionNum(acc.getAccId(), 0);

        int maxSeqKey = Math.max(asccList.stream().mapToInt(e -> e.getSeqKey()).max().orElse(0),
                bccList.stream().mapToInt(e -> e.getSeqKey()).max().orElse(0));
        if (nextSeqKey != (maxSeqKey + 1)) {
            throw new IllegalStateException();
        }
    }
}
