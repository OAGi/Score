package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.springframework.stereotype.Component;

/**
 * Filename strategy for BIE package exports based on a placeholder expression.
 * <p>
 * Default expression:
 * {@code {BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace('\\s+', ''):separator('_')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})}
 */
@Component
public class BiePackageExpressionFilenameStrategy extends ExpressionBasedFilenameStrategy {

    static final String DEFAULT_FILENAME_EXPRESSION =
            "{BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace('\\\\s+', ''):separator('_')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})";
    static final String DEFAULT_DUPLICATE_HANDLER_EXPRESSION = "~{BIE ID}";

    public BiePackageExpressionFilenameStrategy() {
        super(DEFAULT_FILENAME_EXPRESSION, DEFAULT_DUPLICATE_HANDLER_EXPRESSION);
    }
}
