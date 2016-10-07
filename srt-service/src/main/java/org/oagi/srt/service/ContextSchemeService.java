package org.oagi.srt.service;

import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.ContextSchemeValueRepository;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContextSchemeService {

    @Autowired
    private ContextSchemeRepository contextSchemeRepository;

    @Autowired
    private ContextSchemeValueRepository contextSchemeValueRepository;

    public List<ContextScheme> findAll(Sort.Direction direction, String property) {
        return Collections.unmodifiableList(
                contextSchemeRepository.findAll(new Sort(new Sort.Order(direction, property)))
        );
    }

    public List<ContextScheme> findByCtxCategoryId(long ctxCategoryId) {
        return contextSchemeRepository.findByCtxCategoryId(ctxCategoryId);
    }

    public List<ContextScheme> findBySchemeIdAndSchemeAgencyId(String schemeId, String schemeAgencyId) {
        return contextSchemeRepository.findBySchemeIdAndSchemeAgencyId(schemeId, schemeAgencyId);
    }

    public List<ContextScheme> findBySchemeNameAndSchemeAgencyId(String schemeName, String schemeAgencyId) {
        return contextSchemeRepository.findBySchemeNameAndSchemeAgencyId(schemeName, schemeAgencyId);
    }

    public List<ContextSchemeValue> findByOwnerCtxSchemeId(long ownerCtxSchemeId) {
        return contextSchemeValueRepository.findByOwnerCtxSchemeId(ownerCtxSchemeId);
    }

    public ContextScheme findContextSchemeById(long ctxSchemeId) {
        return contextSchemeRepository.findOne(ctxSchemeId);
    }

    public ContextSchemeValue findContextSchemeValueById(long ctxSchemeValueId) {
        return contextSchemeValueRepository.findOne(ctxSchemeValueId);
    }

    @Transactional(readOnly = false)
    public void update(ContextScheme contextScheme, List<ContextSchemeValue> contextSchemeValues) {
        contextSchemeRepository.saveAndFlush(contextScheme);
        contextSchemeValues.stream().forEach(e -> e.setOwnerCtxSchemeId(contextScheme.getCtxSchemeId()));
        contextSchemeValueRepository.save(contextSchemeValues);
    }

    @Transactional(readOnly = false)
    public void delete(List<ContextSchemeValue> contextSchemeValues) {
        contextSchemeValueRepository.delete(contextSchemeValues);
    }

    @Transactional(readOnly = false)
    public void delete(ContextScheme contextScheme) {
        contextSchemeValueRepository.deleteByOwnerCtxSchemeId(contextScheme.getCtxSchemeId());
        contextSchemeRepository.delete(contextScheme);
    }

}
