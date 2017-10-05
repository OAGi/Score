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
            undoInsertHistoryRecord();
        } else if (ra.getRevisionAction().getValue() == 2) {
            undoUpdateHistoryRecord(ra);
        } else if (ra.getRevisionAction().getValue() == 3) {
            undoDeleteHistoryRecord();
        }
    }

    private void undoInsertHistoryRecord() {
        System.out.println();// TODO: implement for ASCCs and BCCs
    }

    private void undoUpdateHistoryRecord(RevisionAware ra) {
        if (ra instanceof BasicCoreComponentProperty) {
            BasicCoreComponentProperty historyRecord = (BasicCoreComponentProperty) ra;
            BasicCoreComponentProperty previousRecord = bccpRepository.findOneByCurrentBccpIdAndRevisions(historyRecord.getCurrentBccpId(), historyRecord.getRevisionNum(), historyRecord.getRevisionTrackingNum() - 1);
            BasicCoreComponentProperty currentRecord = (BasicCoreComponentProperty) currentCC;

            currentRecord.setPropertyTerm(previousRecord.getPropertyTerm());
            currentRecord.setRepresentationTerm(previousRecord.getRepresentationTerm());
            currentRecord.setDeprecated(previousRecord.isDeprecated());
            currentRecord.setState(previousRecord.getState());
            currentRecord.setNillable(previousRecord.isNillable());
            currentRecord.setDefaultValue(previousRecord.getDefaultValue());
            currentRecord.setDefinition(previousRecord.getDefinition());

            bccpRepository.save(currentRecord);
            bccpRepository.delete(historyRecord);
            bccpRepository.flush();
        }

        if (ra instanceof AssociationCoreComponentProperty) {
            AssociationCoreComponentProperty historyRecord = (AssociationCoreComponentProperty) ra;
            AssociationCoreComponentProperty previousRecord = asccpRepository.findOneByCurrentAsccpIdAndRevisions(historyRecord.getCurrentAsccpId(), historyRecord.getRevisionNum(), historyRecord.getRevisionTrackingNum() - 1);
            AssociationCoreComponentProperty currentRecord = (AssociationCoreComponentProperty) currentCC;

            currentRecord.setPropertyTerm(previousRecord.getPropertyTerm());
            currentRecord.setDeprecated(previousRecord.isDeprecated());
            currentRecord.setState(previousRecord.getState());
            currentRecord.setNillable(previousRecord.isNillable());
            currentRecord.setReusableIndicator(previousRecord.isReusableIndicator());
            currentRecord.setDefinition(previousRecord.getDefinition());

            asccpRepository.save(currentRecord);
            asccpRepository.delete(historyRecord);
            asccpRepository.flush();
        }

        if (ra instanceof AggregateCoreComponent) {
            AggregateCoreComponent historyRecord = (AggregateCoreComponent) ra;
            AggregateCoreComponent previousRecord = accRepository.findOneByCurrentAccIdAndRevisions(historyRecord.getCurrentAccId(), historyRecord.getRevisionNum(), historyRecord.getRevisionTrackingNum() - 1);
            AggregateCoreComponent currentRecord = (AggregateCoreComponent) currentCC;

            currentRecord.setDeprecated(previousRecord.isDeprecated());
            currentRecord.setState(previousRecord.getState());
            currentRecord.setDefinition(previousRecord.getDefinition());
            currentRecord.setObjectClassTerm(previousRecord.getObjectClassTerm());
            currentRecord.setOagisComponentType(previousRecord.getOagisComponentType());
            currentRecord.setAbstract(previousRecord.isAbstract());

            accRepository.save(currentRecord);
            accRepository.delete(historyRecord);
            accRepository.flush();
        }

        if (ra instanceof AssociationCoreComponent) {
            AssociationCoreComponent historyRecord = (AssociationCoreComponent) ra;
            AssociationCoreComponent previousRecord = asccRepository.findOneByCurrentAsccIdAndRevisions(historyRecord.getCurrentAsccId(), historyRecord.getRevisionNum(), historyRecord.getRevisionTrackingNum() - 1);
            AssociationCoreComponent currentRecord = asccRepository.findOne(historyRecord.getCurrentAsccId());

            currentRecord.setDeprecated(previousRecord.isDeprecated());
            currentRecord.setState(previousRecord.getState());
            currentRecord.setDefinition(previousRecord.getDefinition());
            currentRecord.setCardinalityMax(previousRecord.getCardinalityMax());
            currentRecord.setCardinalityMin(previousRecord.getCardinalityMin());

            asccRepository.save(currentRecord);
            asccRepository.delete(historyRecord);
            asccRepository.flush();
        }

        if (ra instanceof BasicCoreComponent) {
            BasicCoreComponent historyRecord = (BasicCoreComponent) ra;
            BasicCoreComponent previousRecord = bccRepository.findOneByCurrentBccIdAndRevisions(historyRecord.getCurrentBccId(), historyRecord.getRevisionNum(), historyRecord.getRevisionTrackingNum() - 1);
            BasicCoreComponent currentRecord = bccRepository.findOne(historyRecord.getCurrentBccId());

            currentRecord.setDeprecated(previousRecord.isDeprecated());
            currentRecord.setState(previousRecord.getState());
            currentRecord.setDefinition(previousRecord.getDefinition());
            currentRecord.setCardinalityMax(previousRecord.getCardinalityMax());
            currentRecord.setCardinalityMin(previousRecord.getCardinalityMin());
            currentRecord.setNillable(previousRecord.isNillable());
            currentRecord.setDefaultValue(previousRecord.getDefaultValue());

            bccRepository.save(currentRecord);
            bccRepository.delete(historyRecord);
            bccRepository.flush();
        }

        currentCC = null;
    }

    private void undoDeleteHistoryRecord() {
        System.out.println();// TODO: implement for ASCCs and BCCs
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
