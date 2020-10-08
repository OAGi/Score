package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.*;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oagi.score.entity.jooq.Tables.ASBIEP;
import static org.oagi.score.entity.jooq.Tables.ASCCP;

@Service
@Transactional(readOnly = true)
public class BieGenerateService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private DSLContext dslContext;

    public BieGenerateExpressionResult generate(
            AuthenticatedPrincipal user, List<Long> topLevelAsbiepIds,
            GenerateExpressionOption option) throws BieGenerateFailureException {

        List<TopLevelAsbiep> topLevelAsbieps = topLevelAsbiepRepository.findByIdIn(topLevelAsbiepIds);
        File file = generateSchema(topLevelAsbieps, option);
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

    public File generateSchema(List<TopLevelAsbiep> topLevelAsbieps,
                               GenerateExpressionOption option) throws BieGenerateFailureException {
        if (topLevelAsbieps == null || topLevelAsbieps.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (option == null) {
            throw new IllegalArgumentException();
        }

        String packageOption = option.getPackageOption();
        if (packageOption != null) {
            packageOption = packageOption.trim();
        }

        switch (packageOption.toUpperCase()) {
            case "ALL":
                return generateSchemaForAll(topLevelAsbieps, option);

            case "EACH":
                Map<Long, File> files = generateSchemaForEach(topLevelAsbieps, option);
                if (files.size() == 1) {
                    return files.values().iterator().next();
                }

                try {
                    return Zip.compression(files.values(), ScoreGuid.randomGuid());
                } catch (IOException e) {
                    throw new BieGenerateFailureException("Compression failure.", e);
                }

            default:
                throw new IllegalStateException();
        }
    }

    public File generateSchemaForAll(List<TopLevelAsbiep> topLevelAsbiepList,
                                     GenerateExpressionOption option) throws BieGenerateFailureException {
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(topLevelAsbiepList, option);

        for (TopLevelAsbiep topLevelAsbiep : topLevelAsbiepList) {
            generateExpression.generate(topLevelAsbiep, generationContext, option);
        }

        String filename;
        if (topLevelAsbiepList.size() == 1) {
            filename = getFilenameByTopLevelAsbiep(topLevelAsbiepList.get(0));
        } else {
            filename = ScoreGuid.randomGuid();
        }

        File schemaExpressionFile;
        try {
            schemaExpressionFile = generateExpression.asFile(filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("I/O operation failure.", e);
        }
        return schemaExpressionFile;
    }

    public Map<Long, File> generateSchemaForEach(List<TopLevelAsbiep> topLevelAsbieps,
                                                 GenerateExpressionOption option) throws BieGenerateFailureException {
        Map<Long, File> targetFiles = new HashMap();
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(topLevelAsbieps, option);

        for (TopLevelAsbiep topLevelAsbiep : topLevelAsbieps) {
            try {
                generateExpression.reset();
            } catch (Exception e) {
                throw new BieGenerateFailureException("Unexpected error occurs during initialization of the expression processor.");
            }

            generateExpression.generate(topLevelAsbiep, generationContext, option);
            String filename = getFilenameByTopLevelAsbiep(topLevelAsbiep);

            File schemaExpressionFile;
            try {
                schemaExpressionFile = generateExpression.asFile(filename);
            } catch (IOException e) {
                throw new BieGenerateFailureException("I/O operation failure.", e);
            }
            targetFiles.put(topLevelAsbiep.getTopLevelAsbiepId(), schemaExpressionFile);
        }
        return targetFiles;
    }

    private String getFilenameByTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep) {
        /*
         * Issue 566
         */
        long rootAsbiepId = topLevelAsbiep.getAsbiepId();

        Record2<String, ULong> result = dslContext.select(ASBIEP.GUID, ASBIEP.BASED_ASCCP_ID)
                .from(ASBIEP)
                .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(rootAsbiepId)))
                .fetchOne();

        String propertyTerm = dslContext.select(ASCCP.PROPERTY_TERM)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(result.get(ASBIEP.BASED_ASCCP_ID)))
                .fetchOneInto(String.class);

        String asbiepGuid = result.get(ASBIEP.GUID);

        return propertyTerm.replaceAll(" ", "-") + "-" + asbiepGuid;
    }

    private BieGenerateExpression createBieGenerateExpression(GenerateExpressionOption option) {
        String expressionOption = option.getExpressionOption();
        if (expressionOption != null) {
            expressionOption = expressionOption.trim();
        }

        BieGenerateExpression generateExpression;
        switch (expressionOption.toUpperCase()) {
            case "XML":
                generateExpression = applicationContext.getBean(BieXMLGenerateExpression.class);
                break;
            case "JSON":
                generateExpression = applicationContext.getBean(BieJSONGenerateExpression.class);
                break;
            case "OPENAPI30":
                generateExpression = applicationContext.getBean(BieOpenAPIGenerateExpression.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown expression option: " + expressionOption);
        }

        return generateExpression;
    }
}
