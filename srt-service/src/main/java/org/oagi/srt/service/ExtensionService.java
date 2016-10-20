package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.AssociationCoreComponentRepository;
import org.oagi.srt.repository.NamespaceRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        createASCCPHistoryForExtension(ueAscc);

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
        long userId = currentLoginUser.getAppUserId();
        AssociationCoreComponentProperty ueAsccp = new AssociationCoreComponentProperty();
        ueAsccp.setGuid(Utility.generateGUID());
        ueAsccp.setPropertyTerm(ueAcc.getObjectClassTerm());
        ueAsccp.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        ueAsccp.setRoleOfAccId(ueAcc.getAccId());
        ueAsccp.setDen(ueAsccp.getPropertyTerm() + ". " + ueAcc.getObjectClassTerm());
        ueAsccp.setCreatedBy(userId);
        ueAsccp.setLastUpdatedBy(userId);
        ueAsccp.setOwnerUserId(userId);
        ueAsccp.setState(Published);
        ueAsccp.setReusableIndicator(false);
        ueAsccp.setRevisionNum(0);
        ueAsccp.setRevisionTrackingNum(0);
        ueAsccp.setNamespaceId(ueAcc.getNamespaceId());
        return asccpRepository.saveAndFlush(ueAsccp);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponentProperty ueAsccp) {
        AssociationCoreComponentProperty asccpHistory = new AssociationCoreComponentProperty();
        asccpHistory.setGuid(Utility.generateGUID());
        asccpHistory.setPropertyTerm(ueAsccp.getPropertyTerm());
        asccpHistory.setDefinition(ueAsccp.getDefinition());
        asccpHistory.setRoleOfAccId(ueAsccp.getRoleOfAccId());
        asccpHistory.setDen(ueAsccp.getDen());
        asccpHistory.setCreatedBy(ueAsccp.getCreatedBy());
        asccpHistory.setLastUpdatedBy(ueAsccp.getLastUpdatedBy());
        asccpHistory.setOwnerUserId(ueAsccp.getOwnerUserId());
        asccpHistory.setState(ueAsccp.getState());
        asccpHistory.setReusableIndicator(ueAsccp.isReusableIndicator());
        asccpHistory.setRevisionNum(1);
        asccpHistory.setRevisionTrackingNum(1);
        asccpHistory.setRevisionAction(Insert);
        asccpHistory.setCurrentAsccpId(ueAsccp.getAsccpId());
        asccpHistory.setNamespaceId(ueAsccp.getNamespaceId());
        asccpRepository.saveAndFlush(asccpHistory);
    }

    private AssociationCoreComponent createASCCForExtension(AggregateCoreComponent eAcc,
                                                            User currentLoginUser,
                                                            AggregateCoreComponent ueAcc,
                                                            AssociationCoreComponentProperty ueAsccp) {
        long userId = currentLoginUser.getAppUserId();
        AssociationCoreComponent ueAscc = new AssociationCoreComponent();
        ueAscc.setGuid(Utility.generateGUID());
        ueAscc.setCardinalityMin(1);
        ueAscc.setCardinalityMax(1);
        ueAscc.setSeqKey(1);
        ueAscc.setFromAccId(eAcc.getAccId());
        ueAscc.setToAsccpId(ueAsccp.getAsccpId());
        ueAscc.setDen(eAcc.getObjectClassTerm() + ". " + ueAsccp.getDen());
        ueAscc.setDefinition("System created association to the system created user extension group component - " + ueAcc.getObjectClassTerm() + ".");
        ueAscc.setCreatedBy(userId);
        ueAscc.setLastUpdatedBy(userId);
        ueAscc.setOwnerUserId(userId);
        ueAscc.setState(Published);
        ueAscc.setRevisionNum(0);
        ueAscc.setRevisionTrackingNum(0);
        return asccRepository.saveAndFlush(ueAscc);
    }

    private void createASCCPHistoryForExtension(AssociationCoreComponent ueAscc) {
        AssociationCoreComponent asccHistory = new AssociationCoreComponent();
        asccHistory.setGuid(Utility.generateGUID());
        asccHistory.setCardinalityMin(ueAscc.getCardinalityMin());
        asccHistory.setCardinalityMax(ueAscc.getCardinalityMax());
        asccHistory.setSeqKey(ueAscc.getSeqKey());
        asccHistory.setFromAccId(ueAscc.getFromAccId());
        asccHistory.setToAsccpId(ueAscc.getToAsccpId());
        asccHistory.setDen(ueAscc.getDen());
        asccHistory.setDefinition(ueAscc.getDefinition());
        asccHistory.setCreatedBy(ueAscc.getCreatedBy());
        asccHistory.setLastUpdatedBy(ueAscc.getLastUpdatedBy());
        asccHistory.setOwnerUserId(ueAscc.getOwnerUserId());
        asccHistory.setState(ueAscc.getState());
        asccHistory.setRevisionNum(1);
        asccHistory.setRevisionTrackingNum(1);
        asccHistory.setRevisionAction(Insert);
        asccRepository.saveAndFlush(asccHistory);
    }
}
