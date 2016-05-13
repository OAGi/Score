package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OracleRepositoryFactory implements RepositoryFactory {

    @Autowired
    private OracleCodeListRepository oracleCodeListRepository;

    @Autowired
    private OracleCodeListValueRepository oracleCodeListValueRepository;

    @Override
    public CodeListRepository codeListRepository() {
        return oracleCodeListRepository;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return oracleCodeListValueRepository;
    }
}
