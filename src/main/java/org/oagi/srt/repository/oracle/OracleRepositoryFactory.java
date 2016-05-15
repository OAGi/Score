package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.BaseBusinessContextRepository;
import org.oagi.srt.repository.impl.BaseBusinessContextValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OracleRepositoryFactory implements RepositoryFactory {

    @Autowired
    private OracleBaseCodeListRepository oracleCodeListRepository;

    @Autowired
    private OracleBaseCodeListValueRepository oracleCodeListValueRepository;

    @Autowired
    private OracleContextSchemeRepository oracleContextSchemeRepository;

    @Autowired
    private OracleContextSchemeValueRepository oracleContextSchemeValueRepository;

    @Autowired
    private BaseBusinessContextRepository baseBusinessContextRepository;

    @Autowired
    private BaseBusinessContextValueRepository baseBusinessContextValueRepository;

    @Autowired
    private OracleContextCategoryRepository oracleContextCategoryRepository;

    @Override
    public CodeListRepository codeListRepository() {
        return oracleCodeListRepository;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return oracleCodeListValueRepository;
    }

    @Override
    public ContextSchemeRepository contextSchemeRepository() {
        return oracleContextSchemeRepository;
    }

    @Override
    public ContextSchemeValueRepository contextSchemeValueRepository() {
        return oracleContextSchemeValueRepository;
    }

    @Override
    public BusinessContextRepository businessContextRepository() {
        return baseBusinessContextRepository;
    }

    @Override
    public BusinessContextValueRepository businessContextValueRepository() {
        return baseBusinessContextValueRepository;
    }

    @Override
    public ContextCategoryRepository contextCategoryRepository() {
        return oracleContextCategoryRepository;
    }
}
