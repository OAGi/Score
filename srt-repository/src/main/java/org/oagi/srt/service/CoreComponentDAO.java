package org.oagi.srt.service;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class CoreComponentDAO {

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

    @Transactional
    public AggregateCoreComponent save(AggregateCoreComponent acc) {
        return accRepository.saveAndFlush(acc);
    }

    @Transactional
    public AssociationCoreComponent save(AssociationCoreComponent ascc) {
        return asccRepository.saveAndFlush(ascc);
    }

    @Transactional
    public AssociationCoreComponentProperty save(AssociationCoreComponentProperty asccp) {
        return asccpRepository.saveAndFlush(asccp);
    }

    @Transactional
    public BasicCoreComponent save(BasicCoreComponent bcc) {
        return bccRepository.saveAndFlush(bcc);
    }

    @Transactional
    public BasicCoreComponentProperty save(BasicCoreComponentProperty bccp) {
        return bccpRepository.saveAndFlush(bccp);
    }

    @Transactional
    public List<AggregateCoreComponent> saveAccList(Collection<AggregateCoreComponent> accList) {
        return accRepository.saveAll(accList);
    }

    @Transactional
    public List<AssociationCoreComponent> saveAsccList(Collection<AssociationCoreComponent> asccList) {
        return asccRepository.saveAll(asccList);
    }

    @Transactional
    public List<AssociationCoreComponentProperty> saveAsccpList(Collection<AssociationCoreComponentProperty> asccpList) {
        return asccpRepository.saveAll(asccpList);
    }

    @Transactional
    public List<BasicCoreComponent> saveBccList(Collection<BasicCoreComponent> bccList) {
        return bccRepository.saveAll(bccList);
    }

    @Transactional
    public List<BasicCoreComponentProperty> saveBccpList(Collection<BasicCoreComponentProperty> bccpList) {
        return bccpRepository.saveAll(bccpList);
    }

    @Transactional
    public void delete(AggregateCoreComponent acc) {
        if (acc == null) {
            return;
        }

        long accId = acc.getAccId();
        if (accId == 0L) {
            return;
        }

        accRepository.deleteByCurrentAccId(accId); // To remove history
        asccRepository.deleteByFromAccId(accId);
        bccRepository.deleteByFromAccId(accId);
        accRepository.deleteByAccId(accId);
    }

    @Transactional
    public void deleteByCurrentAsccId(Long currentAsccId) {
        asccRepository.deleteByCurrentAsccId(currentAsccId);
    }

    @Transactional
    public void delete(AssociationCoreComponent ascc) {
        if (ascc == null) {
            return;
        }
        asccRepository.delete(ascc);
    }

    @Transactional
    public void deleteByCurrentAsccpId(Long currentAsccpId) {
        asccpRepository.deleteByCurrentAsccpId(currentAsccpId);
    }

    @Transactional
    public void delete(AssociationCoreComponentProperty asccp) {
        if (asccp == null) {
            return;
        }
        asccpRepository.delete(asccp);
    }

    @Transactional
    public void deleteByCurrentBccId(Long currentBccId) {
        bccRepository.deleteByCurrentBccId(currentBccId);
    }

    @Transactional
    public void delete(BasicCoreComponent bcc) {
        if (bcc == null) {
            return;
        }
        bccRepository.delete(bcc);
    }

    @Transactional
    public void deleteByCurrentBccpId(Long currentBccpId) {
        bccpRepository.deleteByCurrentBccpId(currentBccpId);
    }

    @Transactional
    public void delete(BasicCoreComponentProperty bccp) {
        if (bccp == null) {
            return;
        }
        bccpRepository.delete(bccp);
    }

}
