package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieGenerateFailureException;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.BieGenerateOpenApiExpression;
import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.OpenAPIGenerateExpression;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BizCtxAssignmentRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BizCtxRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class OpenAPIGenerateService {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private DSLContext dslContext;

    public BieGenerateExpressionResult generate(
            AuthenticatedPrincipal user, Map<BigInteger, OpenAPIGenerateExpressionOption> params) throws BieGenerateFailureException {
        File file = generateSchemaForAll(params);
        return toResult(file);
    }

    public BieGenerateExpressionResult toResult(File file) {
        BieGenerateExpressionResult result = new BieGenerateExpressionResult();
        result.setFile(file);

        String filename = file.getName();
        result.setFilename(filename);

        String contentType;
        if (filename.endsWith(".xsd")) {
            contentType = "text/xml";
        } else if (filename.endsWith(".json")) {
            contentType = "application/json";
        } else if (filename.endsWith(".zip")) {
            contentType = "application/zip";
        } else if (filename.endsWith(".yml")) {
            contentType = "text/x-yaml";
        } else {
            contentType = "application/octet-stream";
        }

        result.setContentType(contentType);

        return result;
    }
    public File generateSchemaForAll(Map<BigInteger, OpenAPIGenerateExpressionOption> params) throws BieGenerateFailureException {
        List<BigInteger> topLevelAsbiepIds = null;
        for (BigInteger key: params.keySet()){
            topLevelAsbiepIds.add(key);
        }
        List<TopLevelAsbiep> topLevelAsbiepList = topLevelAsbiepRepository.findByIdIn(topLevelAsbiepIds);
        OpenAPIGenerateExpressionOption option = new OpenAPIGenerateExpressionOption();
        BieGenerateOpenApiExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(topLevelAsbiepList, option);

        for (TopLevelAsbiep topLevelAsbiep : topLevelAsbiepList) {
            generateExpression.generate(topLevelAsbiep, generationContext, option);
        }

        String filename = option.getFilename();

        File schemaExpressionFile;
        try {
            schemaExpressionFile = generateExpression.asFile(filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("I/O operation failure.", e);
        }
        return schemaExpressionFile;
    }

    private BieGenerateOpenApiExpression createBieGenerateExpression(OpenAPIGenerateExpressionOption option) {

        BieGenerateOpenApiExpression generateExpression;
        generateExpression = applicationContext.getBean(OpenAPIGenerateExpression.class);
        return generateExpression;
    }
}
