package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcMatchingScore;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

@Service
@Transactional(readOnly = true)
public class CcMatchingService {

    public <T extends Object, U extends CoreComponent> CcMatchingScore<T> score(
            CcDocument sourceDoc, T source,
            CcDocument targetDoc, T target,
            BiFunction<CcDocument, T, U> mapper) {
        assert mapper != null;

        U sourceCc = mapper.apply(sourceDoc, source);
        U targetCc = mapper.apply(targetDoc, target);

        double score = score(sourceDoc, sourceCc, targetDoc, targetCc);
        return new CcMatchingScore(score, source, target);
    }

    public <T extends CoreComponent> double score(CcDocument sourceDoc, T source,
                                                  CcDocument targetDoc, T target) {
        assert source != null;
        assert target != null;

        double score = 0.0d;
        if (source.guid().equals(target.guid())) {
            score = 1.0d;
        }

        return score;
    }

}
