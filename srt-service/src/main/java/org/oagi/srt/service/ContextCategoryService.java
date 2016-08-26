package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.ContextCategoryRepository;
import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.ContextSchemeValueRepository;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ContextCategoryService {

    @Autowired
    private ContextCategoryRepository contextCategoryRepository;

    @Autowired
    private ContextSchemeRepository contextSchemeRepository;

    @Autowired
    private ContextSchemeValueRepository contextSchemeValueRepository;

    public List<ContextCategory> findAll(Sort.Direction direction, String property) {
        return Collections.unmodifiableList(
                contextCategoryRepository.findAll(new Sort(new Sort.Order(direction, property)))
        );
    }

    public List<ContextCategory> findByNameContaining(String name) {
        name = (name != null) ? name.trim() : null;
        if (StringUtils.isEmpty(name)) {
            return Collections.emptyList();
        }
        return contextCategoryRepository.findByNameContaining(name);
    }

    public List<ContextScheme> findByCtxCategoryId(long ctxCategoryId) {
        return contextSchemeRepository.findByCtxCategoryId(ctxCategoryId);
    }

    public List<ContextSchemeValue> findByOwnerCtxSchemeId(long ownerCtxSchemeId) {
        return contextSchemeValueRepository.findByOwnerCtxSchemeId(ownerCtxSchemeId);
    }

    public ContextCategory findContextCategoryById(long ctxCategoryId) {
        return contextCategoryRepository.findOne(ctxCategoryId);
    }

    public ContextScheme findContextSchemeById(long ctxSchemeId) {
        return contextSchemeRepository.findOne(ctxSchemeId);
    }

    public ContextSchemeValue findContextSchemeValueById(long ctxSchemeValueId) {
        return contextSchemeValueRepository.findOne(ctxSchemeValueId);
    }

    public ContextCategoryBuilder newContextCategoryBuilder() {
        return new ContextCategoryBuilder();
    }

    public void update(ContextCategory contextCategory) {
        contextCategoryRepository.save(contextCategory);
    }

    public void deleteById(long ctxCategoryId) {
        contextCategoryRepository.delete(ctxCategoryId);
    }

    public class ContextCategoryBuilder {

        private String name;
        private String description;

        private ContextCategoryBuilder() {}

        public ContextCategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ContextCategoryBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ContextCategory build() {
            ContextCategory contextCategory = new ContextCategory();
            contextCategory.setName(this.name);
            contextCategory.setDescription(this.description);
            contextCategory.setGuid(Utility.generateGUID());
            contextCategoryRepository.saveAndFlush(contextCategory);

            return contextCategory;
        }
    }

}
