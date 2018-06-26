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
public class BusinessInformationEntityDAO {

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

    @Autowired
    private BusinessInformationEntityUserExtensionRevisionRepository bieUserExtRevRepository;

    @Transactional
    public AggregateBusinessInformationEntity save(AggregateBusinessInformationEntity abie) {
        return abieRepository.saveAndFlush(abie);
    }

    @Transactional
    public AssociationBusinessInformationEntity save(AssociationBusinessInformationEntity asbie) {
        return asbieRepository.saveAndFlush(asbie);
    }

    @Transactional
    public AssociationBusinessInformationEntityProperty save(AssociationBusinessInformationEntityProperty asbiep) {
        return asbiepRepository.saveAndFlush(asbiep);
    }

    @Transactional
    public BasicBusinessInformationEntity save(BasicBusinessInformationEntity bbie) {
        return bbieRepository.saveAndFlush(bbie);
    }

    @Transactional
    public BasicBusinessInformationEntityProperty save(BasicBusinessInformationEntityProperty bbiep) {
        return bbiepRepository.saveAndFlush(bbiep);
    }

    @Transactional
    public BasicBusinessInformationEntitySupplementaryComponent save(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        return bbieScRepository.saveAndFlush(bbieSc);
    }

    @Transactional
    public List<AggregateBusinessInformationEntity> saveAbieList(
            Collection<AggregateBusinessInformationEntity> abieList) {
        return abieRepository.saveAll(abieList);
    }

    @Transactional
    public List<AssociationBusinessInformationEntity> saveAsbieList(
            Collection<AssociationBusinessInformationEntity> asbieList) {
        return asbieRepository.saveAll(asbieList);
    }

    @Transactional
    public List<AssociationBusinessInformationEntityProperty> saveAsbiepList(
            Collection<AssociationBusinessInformationEntityProperty> asbiepList) {
        return asbiepRepository.saveAll(asbiepList);
    }

    @Transactional
    public List<BasicBusinessInformationEntity> saveBbieList(
            Collection<BasicBusinessInformationEntity> bbieList) {
        return bbieRepository.saveAll(bbieList);
    }

    @Transactional
    public List<BasicBusinessInformationEntityProperty> saveBbiepList(
            Collection<BasicBusinessInformationEntityProperty> bbiepList) {
        return bbiepRepository.saveAll(bbiepList);
    }

    @Transactional
    public List<BasicBusinessInformationEntitySupplementaryComponent> saveBbieScList(
            Collection<BasicBusinessInformationEntitySupplementaryComponent> bbieScList) {
        return bbieScRepository.saveAll(bbieScList);
    }

    @Transactional
    public void deleteProfileBOD(long topLevelAbieId) {
        asbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        asbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbieScRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);
        bbiepRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.updateAbieToNull(topLevelAbieId);
        abieRepository.deleteByOwnerTopLevelAbieId(topLevelAbieId);

        bieUserExtRevRepository.deleteByTopLevelAbieId(topLevelAbieId);

        topLevelAbieRepository.deleteById(topLevelAbieId);
    }

}
