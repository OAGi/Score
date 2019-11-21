package org.oagi.srt.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;

import java.io.File;
import java.io.IOException;

public interface BieGenerateExpression {

    void generate(TopLevelAbie topLevelAbie, GenerateExpressionOption option);

    File asFile(String filename) throws IOException;

}
