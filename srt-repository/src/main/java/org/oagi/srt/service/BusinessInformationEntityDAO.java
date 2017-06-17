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
public class BusinessInformationEntityDAO extends AbstractDefinitionDAO {

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
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbieScRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Transactional
    public AggregateBusinessInformationEntity save(AggregateBusinessInformationEntity abie) {
        return save(abie, abieRepository);
    }

    @Transactional
    public AssociationBusinessInformationEntity save(AssociationBusinessInformationEntity asbie) {
        return save(asbie, asbieRepository);
    }

    @Transactional
    public AssociationBusinessInformationEntityProperty save(AssociationBusinessInformationEntityProperty asbiep) {
        return save(asbiep, asbiepRepository);
    }

    @Transactional
    public BasicBusinessInformationEntity save(BasicBusinessInformationEntity bbie) {
        return save(bbie, bbieRepository);
    }

    @Transactional
    public BasicBusinessInformationEntityProperty save(BasicBusinessInformationEntityProperty bbiep) {
        return save(bbiep, bbiepRepository);
    }

    @Transactional
    public BasicBusinessInformationEntitySupplementaryComponent save(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        return save(bbieSc, bbieScRepository);
    }

    @Transactional
    public List<AggregateBusinessInformationEntity> saveAbieList(
            Collection<AggregateBusinessInformationEntity> abieList) {
        return save(abieList, abieRepository);
    }

    @Transactional
    public List<AssociationBusinessInformationEntity> saveAsbieList(
            Collection<AssociationBusinessInformationEntity> asbieList) {
        return save(asbieList, asbieRepository);
    }

    @Transactional
    public List<AssociationBusinessInformationEntityProperty> saveAsbiepList(
            Collection<AssociationBusinessInformationEntityProperty> asbiepList) {
        return save(asbiepList, asbiepRepository);
    }

    @Transactional
    public List<BasicBusinessInformationEntity> saveBbieList(
            Collection<BasicBusinessInformationEntity> bbieList) {
        return save(bbieList, bbieRepository);
    }

    @Transactional
    public List<BasicBusinessInformationEntityProperty> saveBbiepList(
            Collection<BasicBusinessInformationEntityProperty> bbiepList) {
        return save(bbiepList, bbiepRepository);
    }

    @Transactional
    public List<BasicBusinessInformationEntitySupplementaryComponent> saveBbieScList(
            Collection<BasicBusinessInformationEntitySupplementaryComponent> bbieScList) {
        return save(bbieScList, bbieScRepository);
    }

    public List<AggregateBusinessInformationEntity> findAbieByBasedAccId(long basedAccId) {
        List<AggregateBusinessInformationEntity> results = abieRepository.findByBasedAccId(basedAccId);
        return loadDefinition(results);
    }

    @Transactional
    public void deleteProfileBOD(long topLevelAbieId) {
        List<Long> definitionIds;

        definitionIds = asbieRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        asbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        definitionIds = asbiepRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        asbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        definitionIds = bbieScRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        bbieScRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        definitionIds = bbieRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        bbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        definitionIds = bbiepRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        bbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.updateAbieToNull(topLevelAbieId);
        definitionIds = abieRepository.findDefinitionIdByOwnerTopLevelAbieId(topLevelAbieId);
        deleteDefinitions(definitionIds);
        abieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.delete(topLevelAbieId);
    }

}
