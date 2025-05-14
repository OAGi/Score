package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BieGenerateExpression {

    GenerationContext generateContext(
            ScoreUser requester,
            List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option);

    void reset() throws Exception;

    void generate(
            ScoreUser requester,
            TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option);

    File asFile(String filename) throws IOException;

}
