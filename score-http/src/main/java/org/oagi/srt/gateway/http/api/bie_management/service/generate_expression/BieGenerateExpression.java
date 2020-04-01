package org.oagi.srt.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.srt.gateway.http.helper.Utility;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BieGenerateExpression {

    void generate(TopLevelAbie topLevelAbie, GenerateExpressionOption option);

    File asFile(String filename) throws IOException;

    default String toName(String propertyTerm, String representationTerm,
                          Function<String, String> representationTermMapper,
                          boolean includedAbbr) {
        if (StringUtils.isEmpty(propertyTerm) || StringUtils.isEmpty(representationTerm)) {
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
