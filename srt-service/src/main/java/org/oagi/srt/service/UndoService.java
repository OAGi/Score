package org.oagi.srt.service;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UndoService {

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    private CoreComponent currentCC;

    public void undoLastAction(CoreComponent cc) {
        currentCC = cc;
        if (currentCC instanceof BasicCoreComponentProperty) {
            List<BasicCoreComponentProperty> bccps = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(((BasicCoreComponentProperty) cc).getBccpId());
            if (!bccps.isEmpty()) {
                undoHistoryRecord(bccps.get(0));
            }
        }

        if (currentCC instanceof AssociationCoreComponentProperty) {
            List<AssociationCoreComponentProperty> asccps = asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(((AssociationCoreComponentProperty) cc).getAsccpId());
            if (!asccps.isEmpty()) {
                undoHistoryRecord(asccps.get(0));
            }
        }

        if (currentCC instanceof AggregateCoreComponent) {
            List<RevisionAware> ras = findACCWithLatestRevisionNumWithAssociatedComponents();
            if (!ras.isEmpty()) {
                undoHistoryRecord(ras.get(0));
            }
        }
    }

    public void undoHistoryRecord(RevisionAware ra) {

        if (ra.getRevisionAction().getValue() == 1) {
            undoInsertHistoryRecord(ra);
        } else if (ra.getRevisionAction().getValue() == 2) {
            undoUpdateHistoryRecord(ra);
        } else if (ra.getRevisionAction().getValue() == 3) {
            undoDeleteHistoryRecord();
        }
    }

    private void undoInsertHistoryRecord(RevisionAware ra) {// only for ASCCs and BCCs

        if (ra instanceof AssociationCoreComponent) {
            undoInsertHistoryRecord((AssociationCoreComponent) ra);
        }

        if (ra instanceof BasicCoreComponent) {
            undoInsertHistoryRecord((BasicCoreComponent) ra);
        }

        currentCC = null;
    }

    private void undoInsertHistoryRecord (AssociationCoreComponent ascc) {
        long currentAsccId = ascc.getCurrentAsccId();
        asccRepository.delete(ascc.getAsccId()); // remove history record

        if (asccRepository.countByCurrentAsccId(currentAsccId) - 1 == 0) { // no other history records,
                                                                           // i.e., current record should be removed as well
            asccRepository.delete(currentAsccId);
        }

        asccRepository.flush();
    }

    private void undoInsertHistoryRecord (BasicCoreComponent bcc) {
        long currentBccId = bcc.getCurrentBccId();
        bccRepository.delete(bcc.getBccId()); // remove history record

        if (bccRepository.countByCurrentBccId(currentBccId) - 1 == 0) { // no other history records,
                                                                        // i.e., current record should be removed as well
            bccRepository.delete(currentBccId);
        }

        bccRepository.flush();
    }

    private void undoUpdateHistoryRecord(RevisionAware ra) {
        if (ra instanceof BasicCoreComponentProperty) {
            undoUpdateHistoryRecord((BasicCoreComponentProperty) ra);
        }

        if (ra instanceof AssociationCoreComponentProperty) {
            undoUpdateHistoryRecord((AssociationCoreComponentProperty) ra);
        }

        if (ra instanceof AggregateCoreComponent) {
            undoUpdateHistoryRecord((AggregateCoreComponent) ra);
        }

        if (ra instanceof AssociationCoreComponent) {
            undoUpdateHistoryRecord((AssociationCoreComponent) ra);
        }

        if (ra instanceof BasicCoreComponent) {
            undoUpdateHistoryRecord((BasicCoreComponent) ra);
        }

        currentCC = null;
    }

    private void undoUpdateHistoryRecord(BasicCoreComponent bcc) {
        BasicCoreComponent previousRecord = bccRepository.findOneByCurrentBccIdAndRevisions(bcc.getCurrentBccId(), bcc.getRevisionNum(), bcc.getRevisionTrackingNum() - 1);
        BasicCoreComponent currentRecord = bccRepository.findOne(bcc.getCurrentBccId());

        currentRecord.setDeprecated(previousRecord.isDeprecated());
        currentRecord.setState(previousRecord.getState());
        currentRecord.setDefinition(previousRecord.getDefinition());
        currentRecord.setCardinalityMax(previousRecord.getCardinalityMax());
        currentRecord.setCardinalityMin(previousRecord.getCardinalityMin());
        currentRecord.setNillable(previousRecord.isNillable());
        currentRecord.setDefaultValue(previousRecord.getDefaultValue());

        bccRepository.save(currentRecord);
        bccRepository.delete(bcc);
        bccRepository.flush();
    }

    private void undoUpdateHistoryRecord(AssociationCoreComponent ascc) {
        AssociationCoreComponent previousRecord = asccRepository.findOneByCurrentAsccIdAndRevisions(ascc.getCurrentAsccId(), ascc.getRevisionNum(), ascc.getRevisionTrackingNum() - 1);
        AssociationCoreComponent currentRecord = asccRepository.findOne(ascc.getCurrentAsccId());

        currentRecord.setDeprecated(previousRecord.isDeprecated());
        currentRecord.setState(previousRecord.getState());
        currentRecord.setDefinition(previousRecord.getDefinition());
        currentRecord.setCardinalityMax(previousRecord.getCardinalityMax());
        currentRecord.setCardinalityMin(previousRecord.getCardinalityMin());

        asccRepository.save(currentRecord);
        asccRepository.delete(ascc);
        asccRepository.flush();
    }

    private void undoUpdateHistoryRecord(AggregateCoreComponent acc) {
        AggregateCoreComponent previousRecord = accRepository.findOneByCurrentAccIdAndRevisions(acc.getCurrentAccId(), acc.getRevisionNum(), acc.getRevisionTrackingNum() - 1);
        AggregateCoreComponent currentRecord = (AggregateCoreComponent) currentCC;

        currentRecord.setDeprecated(previousRecord.isDeprecated());
        currentRecord.setState(previousRecord.getState());
        currentRecord.setDefinition(previousRecord.getDefinition());
        currentRecord.setObjectClassTerm(previousRecord.getObjectClassTerm());
        currentRecord.setOagisComponentType(previousRecord.getOagisComponentType());
        currentRecord.setAbstract(previousRecord.isAbstract());
        currentRecord.setBasedAccId((previousRecord.getBasedAccId() == 0L) ? null : previousRecord.getBasedAccId());

        accRepository.save(currentRecord);
        accRepository.delete(acc);
        accRepository.flush();
    }

    private void undoUpdateHistoryRecord(AssociationCoreComponentProperty asccp) {
        AssociationCoreComponentProperty previousRecord = asccpRepository.findOneByCurrentAsccpIdAndRevisions(asccp.getCurrentAsccpId(), asccp.getRevisionNum(), asccp.getRevisionTrackingNum() - 1);
        AssociationCoreComponentProperty currentRecord = (AssociationCoreComponentProperty) currentCC;

        currentRecord.setPropertyTerm(previousRecord.getPropertyTerm());
        currentRecord.setDeprecated(previousRecord.isDeprecated());
        currentRecord.setState(previousRecord.getState());
        currentRecord.setNillable(previousRecord.isNillable());
        currentRecord.setReusableIndicator(previousRecord.isReusableIndicator());
        currentRecord.setDefinition(previousRecord.getDefinition());

        asccpRepository.save(currentRecord);
        asccpRepository.delete(asccp);
        asccpRepository.flush();
    }

    private void undoUpdateHistoryRecord(BasicCoreComponentProperty bcc) {
        BasicCoreComponentProperty previousRecord = bccpRepository.findOneByCurrentBccpIdAndRevisions(bcc.getCurrentBccpId(), bcc.getRevisionNum(), bcc.getRevisionTrackingNum() - 1);
        BasicCoreComponentProperty currentRecord = (BasicCoreComponentProperty) currentCC;

        currentRecord.setPropertyTerm(previousRecord.getPropertyTerm());
        currentRecord.setRepresentationTerm(previousRecord.getRepresentationTerm());
        currentRecord.setDeprecated(previousRecord.isDeprecated());
        currentRecord.setState(previousRecord.getState());
        currentRecord.setNillable(previousRecord.isNillable());
        currentRecord.setDefaultValue(previousRecord.getDefaultValue());
        currentRecord.setDefinition(previousRecord.getDefinition());

        bccpRepository.save(currentRecord);
        bccpRepository.delete(bcc);
        bccpRepository.flush();
    }

    private void undoDeleteHistoryRecord() {
        System.out.println();// TODO: implement when nad if needed
    }

    public boolean isPreviousRevisionNotInsert(CoreComponent cc) {
        if (cc instanceof BasicCoreComponentProperty) {
            currentCC = cc;
            List<BasicCoreComponentProperty> bccps = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(((BasicCoreComponentProperty) cc).getBccpId());
            if (bccps.isEmpty() || bccps.get(0).getRevisionAction().getValue() == 1) {
                return false;
            } else {
                return true;
            }
        }

        if (cc instanceof AssociationCoreComponentProperty) {
            currentCC = cc;
            List<AssociationCoreComponentProperty> asccps = asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(((AssociationCoreComponentProperty) cc).getAsccpId());
            if (asccps.isEmpty() || asccps.get(0).getRevisionAction().getValue() == 1) {
                return false;
            } else {
                return true;
            }
        }

        if (cc instanceof AggregateCoreComponent) {
            currentCC = cc;
            List<RevisionAware> ras = findACCWithLatestRevisionNumWithAssociatedComponents();
            if (ras.isEmpty() || (ras.get(0) instanceof AggregateCoreComponent && ras.get(0).getRevisionAction().getValue() == 1)) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private List<RevisionAware> findACCWithLatestRevisionNumWithAssociatedComponents() {
        List<AggregateCoreComponent> accs = accRepository.findAllWithLatestRevisionNumByCurrentAccId(((AggregateCoreComponent) currentCC).getAccId());
        List<BasicCoreComponent> bccs = bccRepository.findAllWithLatestRevisionNumByFromAccId(((AggregateCoreComponent) currentCC).getAccId());
        List<AssociationCoreComponent> asccs = asccRepository.findAllWithLatestRevisionNumByFromAccId(((AggregateCoreComponent) currentCC).getAccId());

        List<RevisionAware> ras = new ArrayList<>();
        ras.addAll(accs);
        ras.addAll(bccs);
        ras.addAll(asccs);

        List<RevisionAware> sortedRas = ras.stream().sorted(Comparator.comparing(RevisionAware::getCreationTimestamp).reversed()).collect(Collectors.toList());

        return sortedRas;
    }
}
