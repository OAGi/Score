package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.springframework.stereotype.Component;

/**
 * Default filename strategy for non-package exports based on expression placeholders.
 * <p>
 * Expression:
 * {@code {BIE Property Term:separator('-')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace('\\.', '_')})}
 */
@Component
public class DefaultBieSchemaFilenameStrategy extends ExpressionBasedFilenameStrategy {

    static final String DEFAULT_FILENAME_EXPRESSION =
            "{BIE Property Term:separator('-')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace('\\\\.', '_')})";
    static final String DEFAULT_DUPLICATE_HANDLER_EXPRESSION = "-{Incremental}";

    public DefaultBieSchemaFilenameStrategy() {
        super(DEFAULT_FILENAME_EXPRESSION, DEFAULT_DUPLICATE_HANDLER_EXPRESSION);
    }

    @Override
    protected BusinessContextResolutionOrder businessContextResolutionOrder() {
        return BusinessContextResolutionOrder.SELECTED_THEN_ASSIGNED;
    }

}
