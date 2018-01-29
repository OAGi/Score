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
public class DataTypeDAO {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Transactional
    public DataType save(DataType dt) {
        return dtRepository.saveAndFlush(dt);
    }

    @Transactional
    public DataTypeSupplementaryComponent save(DataTypeSupplementaryComponent dtSc) {
        return dtScRepository.saveAndFlush(dtSc);
    }
}
