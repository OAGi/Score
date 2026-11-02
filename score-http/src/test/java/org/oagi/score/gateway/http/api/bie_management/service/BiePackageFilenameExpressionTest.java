package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationProperties;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.xsd.BieXMLGenerateExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The BIE Package export calls generateSchemaForEach directly, so that method must resolve the
 * configured filename expressions itself.
 */
class BiePackageFilenameExpressionTest {

    @Test
    void generateSchemaForEachResolvesTheConfiguredPackageFilenameExpression() {
        ApplicationConfigurationQueryService config = mock(ApplicationConfigurationQueryService.class);
        when(config.getConfigurationValueByName(any(), anyString())).thenAnswer(inv -> "cfg:" + inv.getArgument(1));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(BieXMLGenerateExpression.class)).thenReturn(mock(BieXMLGenerateExpression.class));

        BieGenerateService service = new BieGenerateService();
        ReflectionTestUtils.setField(service, "applicationConfigurationService", config);
        ReflectionTestUtils.setField(service, "applicationContext", applicationContext);

        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setExpressionOption("XML");

        // Generation itself is not under test; it fails on the mocked expression after the options are resolved.
        assertThrows(RuntimeException.class,
                () -> service.generateSchemaForEach(null, List.<TopLevelAsbiepSummaryRecord>of(), option));

        assertEquals("cfg:" + ApplicationConfigurationProperties.BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION_CONFIG_PARAM_NAME,
                option.getBiePackageSchemaFilenameExpression());
    }
}
