package org.oagi.score.service.bie;

import org.jooq.DSLContext;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.jooq.impl.DSL.and;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

class EnabledIfBiePresentCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<EnabledIfBiePresent> optional = findAnnotation(context.getElement(), EnabledIfBiePresent.class);
        if (optional.isPresent()) {
            String propertyTerm = optional.get().propertyTerm();
            String release = optional.get().release();

            DSLContext dslContext = SpringExtension.getApplicationContext(context).getBean(DSLContext.class);
            long topLevelAsbiepId = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                    .from(TOP_LEVEL_ASBIEP)
                    .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .join(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                    .join(ASCCP).on(and(
                            ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID),
                            ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID)
                    ))
                    .where(and(
                            ASCCP.PROPERTY_TERM.eq(propertyTerm),
                            RELEASE.RELEASE_NUM.eq(release)
                    ))
                    .limit(1)
                    .fetchOptionalInto(Long.class).orElse(0L);

            return (topLevelAsbiepId > 0L) ? enabled("Enabled with `top_level_asbiep_id`: " + topLevelAsbiepId) :
                    disabled("Disabled by null `top_level_asbiep_id`");
        }

        return disabled("Disabled by not presenting `top_level_asbiep_id`");
    }

}
