package org.oagi.score.service.corecomponent;

import org.oagi.score.repo.api.corecomponent.model.CoreComponent;
import org.oagi.score.service.corecomponent.model.CcMatchingScore;
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
        if (source.getGuid().equals(target.getGuid())) {
            score = 1.0d;
        }

        return score;
    }

}
