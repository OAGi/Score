package org.oagi.srt.service;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class CoreComponentDAO extends AbstractDefinitionDAO {

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
        return save(acc, accRepository);
    }

    @Transactional
    public AssociationCoreComponent save(AssociationCoreComponent ascc) {
        return save(ascc, asccRepository);
    }

    @Transactional
    public AssociationCoreComponentProperty save(AssociationCoreComponentProperty asccp) {
        return save(asccp, asccpRepository);
    }

    @Transactional
    public BasicCoreComponent save(BasicCoreComponent bcc) {
        return save(bcc, bccRepository);
    }

    @Transactional
    public BasicCoreComponentProperty save(BasicCoreComponentProperty bccp) {
        return save(bccp, bccpRepository);
    }

    @Transactional
    public List<AggregateCoreComponent> saveAccList(Collection<AggregateCoreComponent> accList) {
        return save(accList, accRepository);
    }

    @Transactional
    public List<AssociationCoreComponent> saveAsccList(Collection<AssociationCoreComponent> asccList) {
        return save(asccList, asccRepository);
    }

    @Transactional
    public List<AssociationCoreComponentProperty> saveAsccpList(Collection<AssociationCoreComponentProperty> asccpList) {
        return save(asccpList, asccpRepository);
    }

    @Transactional
    public List<BasicCoreComponent> saveBccList(Collection<BasicCoreComponent> bccList) {
        return save(bccList, bccRepository);
    }

    @Transactional
    public List<BasicCoreComponentProperty> saveBccpList(Collection<BasicCoreComponentProperty> bccpList) {
        return save(bccpList, bccpRepository);
    }

    @Transactional
    public void deleteByCurrentAccId(Long currentAccId) {
        List<Long> definitionIds = accRepository.findDefinitionIdByCurrentAccId(currentAccId);
        deleteDefinitions(definitionIds);
        accRepository.deleteByCurrentAccId(currentAccId);
    }

    @Transactional
    public void delete(AggregateCoreComponent acc) {
        if (acc == null) {
            return;
        }
        deleteDefinition(acc.getDefinitionId());
        accRepository.delete(acc);
    }

    @Transactional
    public void deleteByCurrentAsccId(Long currentAsccId) {
        List<Long> definitionIds = asccRepository.findDefinitionIdByCurrentAsccId(currentAsccId);
        deleteDefinitions(definitionIds);
        asccRepository.deleteByCurrentAsccId(currentAsccId);
    }

    @Transactional
    public void delete(AssociationCoreComponent ascc) {
        if (ascc == null) {
            return;
        }
        deleteDefinition(ascc.getDefinitionId());
        asccRepository.delete(ascc);
    }

    @Transactional
    public void deleteAsccByFromAccId(Long accId) {
        if (accId == null || accId == 0L) {
            return;
        }

        List<Long> definitionIds = asccRepository.findDefinitionIdByFromAccId(accId);
        deleteDefinitions(definitionIds);
        asccRepository.deleteByFromAccId(accId);
    }

    @Transactional
    public void deleteByCurrentAsccpId(Long currentAsccpId) {
        List<Long> definitionIds = asccpRepository.findDefinitionIdByCurrentAsccpId(currentAsccpId);
        deleteDefinitions(definitionIds);
        asccpRepository.deleteByCurrentAsccpId(currentAsccpId);
    }

    @Transactional
    public void delete(AssociationCoreComponentProperty asccp) {
        if (asccp == null) {
            return;
        }
        deleteDefinition(asccp.getDefinitionId());
        asccpRepository.delete(asccp);
    }

    @Transactional
    public void deleteByCurrentBccId(Long currentBccId) {
        List<Long> definitionIds = bccRepository.findDefinitionIdByCurrentBccId(currentBccId);
        deleteDefinitions(definitionIds);
        bccRepository.deleteByCurrentBccId(currentBccId);
    }

    @Transactional
    public void delete(BasicCoreComponent bcc) {
        if (bcc == null) {
            return;
        }
        deleteDefinition(bcc.getDefinitionId());
        bccRepository.delete(bcc);
    }

    @Transactional
    public void deleteBccByFromAccId(Long accId) {
        if (accId == null || accId == 0L) {
            return;
        }

        List<Long> definitionIds = bccRepository.findDefinitionIdByFromAccId(accId);
        deleteDefinitions(definitionIds);
        bccRepository.deleteByFromAccId(accId);
    }

    @Transactional
    public void deleteByCurrentBccpId(Long currentBccpId) {
        List<Long> definitionIds = bccpRepository.findDefinitionIdByCurrentBccpId(currentBccpId);
        deleteDefinitions(definitionIds);
        bccpRepository.deleteByCurrentBccpId(currentBccpId);
    }

    @Transactional
    public void delete(BasicCoreComponentProperty bccp) {
        if (bccp == null) {
            return;
        }
        deleteDefinition(bccp.getDefinitionId());
        bccpRepository.delete(bccp);
    }

}
