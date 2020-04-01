package org.oagi.srt.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.TopLevelAbie;
import org.oagi.srt.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.srt.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.srt.gateway.http.api.bie_management.service.generate_expression.*;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.oagi.srt.gateway.http.helper.Zip;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.and;
import static org.oagi.srt.entity.jooq.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieGenerateService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private DSLContext dslContext;

    public BieGenerateExpressionResult generate(
            User user, List<Long> topLevelAbieIds, GenerateExpressionOption option) throws BieGenerateFailureException {

        List<TopLevelAbie> topLevelAbies = topLevelAbieRepository.findByIdIn(topLevelAbieIds);
        File file = generateSchema(topLevelAbies, option);
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

    public File generateSchema(List<TopLevelAbie> topLevelAbies, GenerateExpressionOption option) throws BieGenerateFailureException {
        if (topLevelAbies == null || topLevelAbies.isEmpty()) {
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
                return generateSchemaForAll(topLevelAbies, option);

            case "EACH":
                Map<Long, File> files = generateSchemaForEach(topLevelAbies, option);
                if (files.size() == 1) {
                    return files.values().iterator().next();
                }

                try {
                    return Zip.compression(files.values(), SrtGuid.randomGuid());
                } catch (IOException e) {
                    throw new BieGenerateFailureException("Compression failure.", e);
                }

            default:
                throw new IllegalStateException();
        }
    }

    public File generateSchemaForAll(List<TopLevelAbie> topLevelAbieList,
                                     GenerateExpressionOption option) throws BieGenerateFailureException {
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);

        for (TopLevelAbie topLevelAbie : topLevelAbieList) {
            generateExpression.generate(topLevelAbie, option);
        }

        String filename;
        if (topLevelAbieList.size() == 1) {
            filename = getFilenameByTopLevelAbie(topLevelAbieList.get(0));
        } else {
            filename = SrtGuid.randomGuid();
        }

        File schemaExpressionFile;
        try {
            schemaExpressionFile = generateExpression.asFile(filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("I/O operation failure.", e);
        }
        return schemaExpressionFile;
    }

    public Map<Long, File> generateSchemaForEach(List<TopLevelAbie> topLevelAbies,
                                                 GenerateExpressionOption option) throws BieGenerateFailureException {
        Map<Long, File> targetFiles = new HashMap();
        for (TopLevelAbie topLevelAbie : topLevelAbies) {
            BieGenerateExpression generateExpression = createBieGenerateExpression(option);

            generateExpression.generate(topLevelAbie, option);
            String filename = getFilenameByTopLevelAbie(topLevelAbie);

            File schemaExpressionFile;
            try {
                schemaExpressionFile = generateExpression.asFile(filename);
            } catch (IOException e) {
                throw new BieGenerateFailureException("I/O operation failure.", e);
            }
            targetFiles.put(topLevelAbie.getTopLevelAbieId(), schemaExpressionFile);
        }
        return targetFiles;
    }

    private String getFilenameByTopLevelAbie(TopLevelAbie topLevelAbie) {
        /*
         * Issue 566
         */
        long rootAbieId = topLevelAbie.getAbieId();
        long asccpId = dslContext.select(ASBIEP.BASED_ASCCP_ID)
                .from(ASBIEP)
                .where(and(ASBIEP.ROLE_OF_ABIE_ID
                                .eq(ULong.valueOf(rootAbieId)),
                        ASBIEP.OWNER_TOP_LEVEL_ABIE_ID
                                .eq(ULong.valueOf(topLevelAbie.getTopLevelAbieId()))))
                .fetchOneInto(Long.class);

        String propertyTerm = dslContext.select(ASCCP.PROPERTY_TERM)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(String.class);

        String abieGuid = dslContext.select(ABIE.GUID)
                .from(ABIE)
                .where(ABIE.ABIE_ID.eq(ULong.valueOf(rootAbieId)))
                .fetchOneInto(String.class);

        return propertyTerm.replaceAll(" ", "-") + "-" + abieGuid;
    }

    private BieGenerateExpression createBieGenerateExpression(GenerateExpressionOption option) {
        String expressionOption = option.getExpressionOption();
        if (expressionOption != null) {
            expressionOption = expressionOption.trim();
        }

        BieGenerateExpression generateExpression = null;
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
