package org.oagi.srt.repository.mysql;

import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.impl.CodeListRepositoryImpl;
import org.oagi.srt.repository.impl.CodeListValueRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MysqlRepositoryFactory implements RepositoryFactory {

    @Autowired
    private CodeListRepositoryImpl codeListRepositoryImpl;

    @Autowired
    private CodeListValueRepositoryImpl codeListValueRepositoryImpl;

    @Override
    public CodeListRepository codeListRepository() {
        return codeListRepositoryImpl;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return codeListValueRepositoryImpl;
    }
}
