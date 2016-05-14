package org.oagi.srt.repository.mysql;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MysqlRepositoryFactory implements RepositoryFactory {

    @Autowired
    private BaseCodeListRepository baseCodeListRepository;

    @Autowired
    private BaseCodeListValueRepository baseCodeListValueRepository;

    @Autowired
    private BaseContextSchemeRepository baseContextSchemeRepository;

    @Autowired
    private BaseContextSchemeValueRepository baseContextSchemeValueRepository;

    @Autowired
    private BaseBusinessContextRepository baseBusinessContextRepository;

    @Autowired
    private BaseBusinessContextValueRepository baseBusinessContextValueRepository;

    @Autowired
    private BaseContextCategoryRepository baseContextCategoryRepository;

    @Override
    public CodeListRepository codeListRepository() {
        return baseCodeListRepository;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return baseCodeListValueRepository;
    }

    @Override
    public ContextSchemeRepository contextSchemeRepository() {
        return baseContextSchemeRepository;
    }

    @Override
    public ContextSchemeValueRepository contextSchemeValueRepository() {
        return baseContextSchemeValueRepository;
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
        return baseContextCategoryRepository;
    }
}
