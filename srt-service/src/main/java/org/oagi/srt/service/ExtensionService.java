package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = Throwable.class)
    public void appendUserExtensionIfAbsent(AssociationCoreComponentProperty asccp,
                                            User user, boolean isLocally) {
        if (!"Extension".equals(asccp.getPropertyTerm())) {
            throw new IllegalArgumentException("Can't append user extension on this ASCCP: " + asccp);
        }

        AggregateCoreComponent eAcc = accRepository.findOne(asccp.getRoleOfAccId());
        if (!isLocally) { // isGlobally
            eAcc = getAllExtensionAcc(eAcc);
        }

        if (!existsUserExtension(eAcc)) {
            createNewUserExtensionGroupACC(eAcc, user);
        }
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

    private boolean existsUserExtension(AggregateCoreComponent eAcc) {
        for (AssociationCoreComponent ascc : asccRepository.findByFromAccId(eAcc.getAccId())) {
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
            AggregateCoreComponent acc = accRepository.findOne(asccp.getRoleOfAccId());
            if (acc.getOagisComponentType() == UserExtensionGroup) {
                return true;
            }
        }
        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent createNewUserExtensionGroupACC(
            AggregateCoreComponent eAcc, User currentLoginUser) {
        AggregateCoreComponent ueAcc = createACCForExtension(eAcc, currentLoginUser);
        createACCHistoryForExtension(ueAcc);

        AssociationCoreComponentProperty ueAsccp = createASCCPForExtension(eAcc, currentLoginUser, ueAcc);
        createASCCPHistoryForExtension(ueAsccp);

        AssociationCoreComponent ueAscc = createASCCForExtension(eAcc, currentLoginUser, ueAcc, ueAsccp);
        createASCCHistoryForExtension(ueAscc);

        return ueAcc;
    }

    private AggregateCoreComponent createACCForExtension(AggregateCoreComponent eAcc, User currentLoginUser) {
        long userId = currentLoginUser.getAppUserId();
        AggregateCoreComponent ueAcc = new AggregateCoreComponent();
        ueAcc.setGuid(Utility.generateGUID());
        ueAcc.setObjectClassTerm(Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm()));
        ueAcc.setDen((ueAcc.getObjectClassTerm() + ". Details"));
        ueAcc.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAcc.setOagisComponentType(UserExtensionGroup);
        ueAcc.setCreatedBy(userId);
        ueAcc.setLastUpdatedBy(userId);
        ueAcc.setOwnerUserId(userId);
        ueAcc.setState(Editing);
        ueAcc.setRevisionNum(0);
        ueAcc.setRevisionTrackingNum(0);
        ueAcc.setNamespaceId(namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10"));
        return accRepository.saveAndFlush(ueAcc);
    }

    private void createACCHistoryForExtension(AggregateCoreComponent ueAcc) {
        AggregateCoreComponent accHistory = new AggregateCoreComponent();
        accHistory.setGuid(Utility.generateGUID());
        accHistory.setObjectClassTerm(ueAcc.getObjectClassTerm());
        accHistory.setDen(ueAcc.getDen());
        accHistory.setDefinition(ueAcc.getDefinition());
        accHistory.setOagisComponentType(ueAcc.getOagisComponentType());
        accHistory.setCreatedBy(ueAcc.getCreatedBy());
        accHistory.setLastUpdatedBy(ueAcc.getLastUpdatedBy());
        accHistory.setOwnerUserId(ueAcc.getOwnerUserId());
        accHistory.setState(ueAcc.getState());
        accHistory.setRevisionNum(1);
        accHistory.setRevisionTrackingNum(1);
        accHistory.setRevisionAction(Insert);
        accHistory.setCurrentAccId(ueAcc.getAccId());
        accHistory.setNamespaceId(ueAcc.getNamespaceId());
        accRepository.saveAndFlush(accHistory);
    }

    private AssociationCoreComponentProperty createASCCPForExtension(AggregateCoreComponent eAcc,
                                                                     User currentLoginUser,
                                                                     AggregateCoreComponent ueAcc) {
        AssociationCoreComponentProperty ueAsccp = createASCCP(ueAcc, currentLoginUser);
        ueAsccp.setPropertyTerm(ueAcc.getObjectClassTerm());
        ueAsccp.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAsccp.setState(Published);
        return asccpRepository.saveAndFlush(ueAsccp);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponentProperty ueAsccp) {
        AssociationCoreComponentProperty asccpHistory = createASCCPHistory(ueAsccp);
        asccpRepository.saveAndFlush(asccpHistory);
    }

    private AssociationCoreComponent createASCCForExtension(AggregateCoreComponent eAcc,
                                                            User currentLoginUser,
                                                            AggregateCoreComponent ueAcc,
                                                            AssociationCoreComponentProperty ueAsccp) {
        AssociationCoreComponent ueAscc = createASCC(eAcc, ueAsccp, currentLoginUser, 1);
        ueAscc.setCardinalityMin(1);
        ueAscc.setDefinition("System created association to the system created user extension group component - " + ueAcc.getObjectClassTerm() + ".");
        ueAscc.setState(Published);
        return asccRepository.saveAndFlush(ueAscc);
    }

    private void createASCCHistoryForExtension(AssociationCoreComponent ueAscc) {
        AssociationCoreComponent asccHistory = createASCCHistory(ueAscc);
        asccRepository.saveAndFlush(asccHistory);
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

    private AssociationCoreComponentProperty createASCCPHistory(AssociationCoreComponentProperty tAsccp) {
        AssociationCoreComponentProperty asccpHistory = tAsccp.clone();
        asccpHistory.setRevisionNum(1);
        asccpHistory.setRevisionTrackingNum(1);
        asccpHistory.setRevisionAction(Insert);
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
        ascc.setDen(pAcc.getObjectClassTerm() + ". " + tAsccp.getDen());
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

    private AssociationCoreComponent createASCCHistory(AssociationCoreComponent tAscc) {
        AssociationCoreComponent asccHistory = tAscc.clone();
        asccHistory.setRevisionNum(1);
        asccHistory.setRevisionTrackingNum(1);
        asccHistory.setRevisionAction(Insert);
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
        bcc.setEntityType(1);
        bcc.setFromAccId(pAcc.getAccId());
        bcc.setToBccpId(tBccp.getBccpId());
        bcc.setDen(pAcc.getObjectClassTerm() + ". " + tBccp.getDen());
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

    @Transactional(rollbackFor = Throwable.class)
    public AssociationCoreComponent appendAsccTo(AggregateCoreComponent pAcc, User user) {
        AssociationCoreComponentProperty tAsccp = createASCCP(pAcc, user);
        AssociationCoreComponentProperty tAsccpHistory = createASCCPHistory(tAsccp);
        asccpRepository.save(Arrays.asList(tAsccp, tAsccpHistory));

        int seqKey = nextSeqKey(pAcc);
        AssociationCoreComponent tAscc = createASCC(pAcc, tAsccp, user, seqKey);
        AssociationCoreComponent tAsccHistory = createASCCHistory(tAscc);
        asccRepository.save(Arrays.asList(tAscc, tAsccHistory));

        return tAscc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public BasicCoreComponent appendBccTo(AggregateCoreComponent pAcc, User user, DataType tBdt) {
        BasicCoreComponentProperty tBccp = createBCCP(pAcc, user, tBdt);
        BasicCoreComponentProperty tBccpHistory = createBCCPHistory(tBccp);
        bccpRepository.save(Arrays.asList(tBccp, tBccpHistory));

        int seqKey = nextSeqKey(pAcc);
        BasicCoreComponent tBcc = createBCC(pAcc, tBccp, user, seqKey);
        BasicCoreComponent tBccHistory = createBCCHistory(tBcc);
        bccRepository.save(Arrays.asList(tBcc, tBccHistory));

        return tBcc;
    }

    private int nextSeqKey(AggregateCoreComponent acc) {
        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(acc.getAccId());
        List<BasicCoreComponent> bccList = bccRepository.findByFromAccId(acc.getAccId());

        int nextSeqKey = asccList.size() + bccList.size() + 1;
        ensureNextSeqKey(nextSeqKey, asccList, bccList);

        return nextSeqKey;
    }

    private void ensureNextSeqKey(int nextSeqKey,
                                  List<AssociationCoreComponent> asccList,
                                  List<BasicCoreComponent> bccList) {
        int maxSeqKey = Math.max(asccList.stream().mapToInt(e -> e.getSeqKey()).max().getAsInt(),
                bccList.stream().mapToInt(e -> e.getSeqKey()).max().getAsInt());
        if (nextSeqKey != (maxSeqKey + 1)) {
            throw new IllegalStateException();
        }
    }
}
