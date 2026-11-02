package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BieSchemaFilenameExpressionTest {

    private static TopLevelAsbiepSummaryRecord purchaseOrder() {
        return new TopLevelAsbiepSummaryRecord(null, null, new TopLevelAsbiepId(BigInteger.valueOf(965)),
                null, null, null, null, null, "Purchase Order", null, "1.0", null, null,
                false, false, null, null, null);
    }

    private static String baseFilename(String expression) {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.businessContextQueryRepository(any()))
                .thenReturn(mock(BusinessContextQueryRepository.class));

        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setBieSchemaFilenameExpression(expression);
        return ExpressionBasedFilenameStrategy.from(repositoryFactory, option)
                .buildBaseFilename(null, purchaseOrder());
    }

    @Test
    void theDefaultExpressionJoinsThePropertyTermWithHyphens() {
        assertEquals("Purchase-Order", baseFilename(null));
    }

    @Test
    void theConfiguredSeparatorIsUsed() {
        assertEquals("Purchase Order", baseFilename("{BIE Property Term:separator(' ')}"));
    }
}
