package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.helper.Utility;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BieGenerateOpenApiExpression {

    GenerationContext generateContext(List<TopLevelAsbiep> topLevelAsbieps);

    void reset() throws Exception;

    void generate(TopLevelAsbiep topLevelAsbiep, GenerationContext generationContext, OpenAPIGenerateExpressionOption option);

    File asFile(String filename) throws IOException;

    default String toName(String propertyTerm, String representationTerm,
                          Function<String, String> representationTermMapper,
                          boolean includedAbbr) {
        if (!StringUtils.hasLength(propertyTerm) || !StringUtils.hasLength(representationTerm)) {
            throw new IllegalArgumentException();
        }

        representationTerm = representationTermMapper.apply(representationTerm);

        List<String> s = Stream.concat(
                Stream.of(propertyTerm.split(" ")),
                Stream.of(representationTerm.split(" "))
        ).distinct().collect(Collectors.toList());
        s.set(0, s.get(0).toLowerCase());
        if (s.size() > 1) {
            for (int i = 1, len = s.size(); i < len; ++i) {
                s.set(i, Utility.camelCase(s.get(i), includedAbbr));
            }
        }
        return String.join("", s);
    }
}
