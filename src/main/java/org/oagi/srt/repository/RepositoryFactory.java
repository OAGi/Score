package org.oagi.srt.repository;

public interface RepositoryFactory {

    public CodeListRepository codeListRepository();

    public CodeListValueRepository codeListValueRepository();

    public ContextSchemeRepository contextSchemeRepository();

    public ContextSchemeValueRepository contextSchemeValueRepository();

    public BusinessContextRepository businessContextRepository();

    public BusinessContextValueRepository businessContextValueRepository();

    public ContextCategoryRepository contextCategoryRepository();

}
