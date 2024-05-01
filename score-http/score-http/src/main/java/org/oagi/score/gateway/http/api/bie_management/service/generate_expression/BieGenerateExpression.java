package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BieGenerateExpression {

    GenerationContext generateContext(List<TopLevelAsbiep> topLevelAsbieps, GenerateExpressionOption option);

    void reset() throws Exception;

    void generate(TopLevelAsbiep topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option);

    File asFile(String filename) throws IOException;

}
