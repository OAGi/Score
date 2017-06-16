package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.ContextCategoryRepository;
import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.ContextSchemeValueRepository;
import org.oagi.srt.repository.entity.ContextCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContextCategoryService {

    @Autowired
    private ContextCategoryRepository contextCategoryRepository;

    @Autowired
    private ContextSchemeRepository contextSchemeRepository;

    @Autowired
    private ContextSchemeValueRepository contextSchemeValueRepository;

    public List<ContextCategory> findAll(Sort.Direction direction, String property) {
        return contextCategoryRepository.findAll(new Sort(new Sort.Order(direction, property)));
    }

    public List<ContextCategory> findAll() {
        return contextCategoryRepository.findAll();
    }

    public List<ContextCategory> findByName(String name) {
        name = (name != null) ? name.trim() : null;
        if (StringUtils.isEmpty(name)) {
            return Collections.emptyList();
        }
        return contextCategoryRepository.findByName(name);
    }

    public List<ContextCategory> findByNameContaining(String name) {
        name = (name != null) ? name.trim() : null;
        if (StringUtils.isEmpty(name)) {
            return Collections.emptyList();
        }
        return contextCategoryRepository.findByNameContaining(name);
    }

    public ContextCategory findById(long ctxCategoryId) {
        return contextCategoryRepository.findOne(ctxCategoryId);
    }

    public ContextCategory findOneByGuid(String ctxCategoryGuid) {
        return contextCategoryRepository.findOneByGuid(ctxCategoryGuid);
    }

    public ContextCategoryBuilder newContextCategoryBuilder() {
        return new ContextCategoryBuilder();
    }

    @Transactional(readOnly = false)
    public void update(ContextCategory contextCategory) {
        if (StringUtils.isEmpty(contextCategory.getGuid())) {
            contextCategory.setGuid(Utility.generateGUID());
        }
        contextCategoryRepository.save(contextCategory);
    }

    @Transactional(readOnly = false)
    public void deleteById(long ctxCategoryId) {
        contextCategoryRepository.delete(ctxCategoryId);
    }

    public class ContextCategoryBuilder {

        private String name;
        private String description;

        private ContextCategoryBuilder() {
        }

        public ContextCategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ContextCategoryBuilder description(String description) {
            this.description = description;
            return this;
        }

        @Transactional(readOnly = false)
        public ContextCategory build() {
            if (StringUtils.isEmpty(this.name)) {
                throw new IllegalArgumentException("'name' property must not be null.");
            }
            if (StringUtils.isEmpty(this.description)) {
                throw new IllegalArgumentException("'description' property must not be null.");
            }

            ContextCategory contextCategory = new ContextCategory();
            contextCategory.setName(this.name);
            contextCategory.setDescription(this.description);
            contextCategory.setGuid(Utility.generateGUID());
            contextCategoryRepository.saveAndFlush(contextCategory);

            return contextCategory;
        }
    }

}
