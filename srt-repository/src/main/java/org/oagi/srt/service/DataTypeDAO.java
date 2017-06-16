package org.oagi.srt.service;

import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.DataTypeSupplementaryComponentRepository;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class DataTypeDAO extends AbstractDefinitionDAO {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Transactional
    public DataType save(DataType dt) {
        return save(dt, dtRepository);
    }

    @Transactional
    public DataTypeSupplementaryComponent save(DataTypeSupplementaryComponent dtSc) {
        return save(dtSc, dtScRepository);
    }

    public DataType findDt(Long dtId) {
        DataType dt = dtRepository.findOne(dtId);
        return loadDefinition(dt);
    }

    public DataTypeSupplementaryComponent findDtSc(Long dtScId) {
        DataTypeSupplementaryComponent dtSc = dtScRepository.findOne(dtScId);
        return loadDefinition(dtSc);
    }
}
