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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

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
            AuthenticatedPrincipal user, List<BigInteger> topLevelAsbiepIds,
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
                Map<BigInteger, File> files = generateSchemaForEach(topLevelAsbieps, option);
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
            filename = getFilenameByTopLevelAsbiep(topLevelAsbiepList.get(0), option);
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

    public Map<BigInteger, File> generateSchemaForEach(List<TopLevelAsbiep> topLevelAsbieps,
                                                       GenerateExpressionOption option) throws BieGenerateFailureException {
        Map<BigInteger, File> targetFiles = new HashMap();
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(topLevelAsbieps, option);

        Map<String, Integer> filenames = new HashMap();
        for (TopLevelAsbiep topLevelAsbiep : topLevelAsbieps) {
            try {
                generateExpression.reset();
            } catch (Exception e) {
                throw new BieGenerateFailureException("Unexpected error occurs during initialization of the expression processor.");
            }

            generateExpression.generate(topLevelAsbiep, generationContext, option);
            String filename = getFilenameByTopLevelAsbiep(topLevelAsbiep, option);
            int numOfFilenames = filenames.getOrDefault(filename, 0);
            filenames.put(filename, numOfFilenames + 1);

            if (numOfFilenames > 0) {
                filename = filename + " (" + numOfFilenames + ")";
            }

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

    private String getFilenameByTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep, GenerateExpressionOption option) {
        Map<BigInteger, String> filenames = option.getFilenames();
        if (filenames != null && filenames.containsKey(topLevelAsbiep.getTopLevelAsbiepId())) {
            return filenames.get(topLevelAsbiep.getTopLevelAsbiepId());
        }

        /*
         * Issue 566
         */
        BigInteger rootAsbiepId = topLevelAsbiep.getAsbiepId();
        Record2<String, ULong> result = dslContext.select(ASBIEP.GUID, ASBIEP.BASED_ASCCP_MANIFEST_ID)
                .from(ASBIEP)
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(and(ASBIEP.ASBIEP_ID
                                .eq(ULong.valueOf(rootAsbiepId)),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID
                                .eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))))
                .fetchOne();

        String propertyTerm = dslContext.select(ASCCP.PROPERTY_TERM)
                .from(ASCCP_MANIFEST)
                .join(ASCCP)
                .on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(result.get(ASBIEP.BASED_ASCCP_MANIFEST_ID)))
                .fetchOneInto(String.class);

        /*
         * Issue 1267
         */
        StringBuilder sb = new StringBuilder();
        sb.append(propertyTerm.replaceAll(" ", "-"));

        if (option.isIncludeBusinessContextInFilename()) {
            BizCtxAssignmentRecord bizCtxAssignmentRecord = dslContext.selectFrom(BIZ_CTX_ASSIGNMENT)
                    .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                    .fetchAny();
            BizCtxRecord bizCtxRecord = dslContext.selectFrom(BIZ_CTX)
                    .where(BIZ_CTX.BIZ_CTX_ID.eq(bizCtxAssignmentRecord.getBizCtxId()))
                    .fetchOne();

            if (bizCtxRecord != null) {
                sb.append('-').append(bizCtxRecord.getName().replaceAll(" ", "-"));
            }
        }
        
        if (option.isIncludeVersionInFilename()) {
            String version = StringUtils.trim(topLevelAsbiep.getVersion());
            if (StringUtils.hasLength(version)) {
                sb.append('-').append(version.replaceAll(".", "_"));
            }
        }

        return sb.toString();
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
            case "ODF":
                generateExpression = applicationContext.getBean(BieODFSpreadsheetGenerationExpression.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown expression option: " + expressionOption);
        }

        return generateExpression;
    }
}
