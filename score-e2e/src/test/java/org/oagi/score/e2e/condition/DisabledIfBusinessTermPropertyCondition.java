package org.oagi.score.e2e.condition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.oagi.score.e2e.BaseTest;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class DisabledIfBusinessTermPropertyCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "No @DisabledIfBusinessTermProperty conditions resulting in 'disabled' execution encountered");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<Object> optionalTestInstance = context.getTestInstance();
        Optional<AnnotatedElement> optionalElement = context.getElement();

        if (optionalTestInstance.isPresent() && optionalElement.isPresent()) {
            Object testInstance = optionalTestInstance.get();
            AnnotatedElement annotatedElement = optionalElement.get();
            if (testInstance instanceof BaseTest) {
                return findAnnotation(annotatedElement, DisabledIfBusinessTermProperty.class).stream()
                        .map(annotation -> {
                            ConditionEvaluationResult result = evaluate((BaseTest) testInstance, annotation);
                            logResult(annotation, annotatedElement, result);
                            return result;
                        })
                        .filter(ConditionEvaluationResult::isDisabled)
                        .findFirst()
                        .orElse(getNoDisabledConditionsEncounteredResult());
            }
        }
        return getNoDisabledConditionsEncounteredResult();
    }

    private ConditionEvaluationResult getNoDisabledConditionsEncounteredResult() {
        return ENABLED;
    }

    private void logResult(DisabledIfBusinessTermProperty annotation, AnnotatedElement annotatedElement, ConditionEvaluationResult result) {
        logger.trace(() -> format("Evaluation of %s on [%s] resulted in: %s", annotation, annotatedElement, result));
    }

    private ConditionEvaluationResult evaluate(BaseTest testInstance, DisabledIfBusinessTermProperty annotation) {
        boolean actualBusinessTermEnabled = testInstance.getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled();
        boolean value = annotation.value();
        if (value != actualBusinessTermEnabled) {
            return enabled(format("Current business term property sets to [%s]", actualBusinessTermEnabled));
        }
        return disabled(format("Current business term property sets to [%s]", actualBusinessTermEnabled));
    }

}
