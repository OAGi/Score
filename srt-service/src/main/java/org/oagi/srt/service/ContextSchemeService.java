package org.oagi.srt.service;

import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.ContextSchemeValueRepository;
import org.oagi.srt.repository.entity.ContextScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
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

}
