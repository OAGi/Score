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

    public <T, U extends CoreComponent<?>> CcMatchingScore<T> score(
            CcDocument sourceDoc, T source,
            CcDocument targetDoc, T target,
            BiFunction<CcDocument, T, U> mapper) {
        assert mapper != null;

        U sourceCc = mapper.apply(sourceDoc, source);
        U targetCc = mapper.apply(targetDoc, target);

        double score = score(sourceDoc, sourceCc, targetDoc, targetCc);
        return new CcMatchingScore(score, source, target);
    }

    public <T extends CoreComponent<?>> double score(CcDocument sourceDoc, T source,
                                                  CcDocument targetDoc, T target) {
        if (source == null || target == null || source.guid() == null || target.guid() == null) {
            return 0.0d;
        }

        return source.guid().equals(target.guid()) ? 1.0d : 0.0d;
    }

    public <T extends CoreComponent<?>> double score(T source, T target) {
        return score(null, source, null, target);
    }

}
