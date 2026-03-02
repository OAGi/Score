package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.BieQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.*;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BizCtxAssignmentRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BizCtxRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class BieGenerateService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BieQueryRepository query(ScoreUser requester) {
        return repositoryFactory.bieQueryRepository(requester);
    };

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DSLContext dslContext;

    public BieGenerateExpressionResult generate(
            ScoreUser requester, List<TopLevelAsbiepId> topLevelAsbiepIds,
            GenerateExpressionOption option) throws BieGenerateFailureException {

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        List<TopLevelAsbiepSummaryRecord> topLevelAsbieps = topLevelAsbiepIds.stream()
                .map(e -> topLevelAsbiepQuery.getTopLevelAsbiepSummary(e))
                .collect(Collectors.toList());
        File file = generateSchema(requester, topLevelAsbieps, option);
        return toResult(file);
    }

    public BieGenerateExpressionResult toResult(File file) {
        String filename = file.getName();
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

        return new BieGenerateExpressionResult(filename, contentType, file);
    }

    public File generateSchema(
            ScoreUser requester,
            List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) throws BieGenerateFailureException {
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
                return generateSchemaForAll(requester, topLevelAsbieps, option);

            case "EACH":
                Map<TopLevelAsbiepId, File> files = generateSchemaForEach(requester, topLevelAsbieps, option);
                if (files.size() == 1) {
                    return files.values().iterator().next();
                }

                try {
                    return Zip.compression(files.values(), ScoreGuidUtils.randomGuid());
                } catch (IOException e) {
                    throw new BieGenerateFailureException("Compression failure.", e);
                }

            default:
                throw new IllegalStateException();
        }
    }

    public File generateSchemaForAll(ScoreUser requester,
                                     List<TopLevelAsbiepSummaryRecord> topLevelAsbiepList,
                                     GenerateExpressionOption option) throws BieGenerateFailureException {
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(requester, topLevelAsbiepList, option);
        ensureExpressionFilenames(collectTopLevelAsbieps(topLevelAsbiepList, generationContext), option);

        Map<TopLevelAsbiepId, File> referencedSchemaFiles = new LinkedHashMap<>();
        if (isSeparateFileReferencesForReusedSchemasEnabled(option) &&
                !generationContext.getRefTopLevelAsbiepSet().isEmpty()) {
            // #1711: Generate reused schemas as standalone files first.
            referencedSchemaFiles.putAll(generateReferencedSchemasRecursively(
                    requester, generationContext.getRefTopLevelAsbiepSet(), option, null));
        }

        for (TopLevelAsbiepSummaryRecord topLevelAsbiep : topLevelAsbiepList) {
            generateExpression.generate(requester, topLevelAsbiep, generationContext, option);
        }

        String filename;
        if (topLevelAsbiepList.size() == 1) {
            filename = getFilenameByTopLevelAsbiep(topLevelAsbiepList.get(0), option);
        } else {
            filename = ScoreGuidUtils.randomGuid();
        }

        File schemaExpressionFile;
        try {
            schemaExpressionFile = generateExpression.asFile(filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("I/O operation failure.", e);
        }

        if (referencedSchemaFiles.isEmpty()) {
            return schemaExpressionFile;
        }

        List<File> files = new ArrayList<>();
        files.add(schemaExpressionFile);
        files.addAll(referencedSchemaFiles.values());

        try {
            return Zip.compression(files, filename);
        } catch (IOException e) {
            throw new BieGenerateFailureException("Compression failure.", e);
        }
    }

    public Map<TopLevelAsbiepId, File> generateSchemaForEach(
            ScoreUser requester,
            List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) throws BieGenerateFailureException {
        Map<TopLevelAsbiepId, File> targetFiles = new HashMap();
        BieGenerateExpression generateExpression = createBieGenerateExpression(option);
        GenerationContext generationContext = generateExpression.generateContext(requester, topLevelAsbieps, option);
        ensureExpressionFilenames(collectTopLevelAsbieps(topLevelAsbieps, generationContext), option);

        Set<TopLevelAsbiepId> selectedTopLevelAsbiepIdSet = new HashSet<>();
        for (TopLevelAsbiepSummaryRecord topLevelAsbiep : topLevelAsbieps) {
            selectedTopLevelAsbiepIdSet.add(topLevelAsbiep.topLevelAsbiepId());
        }

        if (isSeparateFileReferencesForReusedSchemasEnabled(option) &&
                !generationContext.getRefTopLevelAsbiepSet().isEmpty()) {
            // #1711: Include recursively discovered reused schemas in EACH packaging.
            Map<TopLevelAsbiepId, File> referencedFiles = generateReferencedSchemasRecursively(
                    requester, generationContext.getRefTopLevelAsbiepSet(), option, selectedTopLevelAsbiepIdSet);
            targetFiles.putAll(referencedFiles);
        }

        for (TopLevelAsbiepSummaryRecord topLevelAsbiep : topLevelAsbieps) {
            try {
                generateExpression.reset();
            } catch (Exception e) {
                throw new BieGenerateFailureException("Unexpected error occurs during initialization of the expression processor.");
            }

            generateExpression.generate(requester, topLevelAsbiep, generationContext, option);
            String filename = getFilenameByTopLevelAsbiep(topLevelAsbiep, option);

            File schemaExpressionFile;
            try {
                schemaExpressionFile = generateExpression.asFile(filename);
            } catch (IOException e) {
                throw new BieGenerateFailureException("I/O operation failure.", e);
            }
            targetFiles.put(topLevelAsbiep.topLevelAsbiepId(), schemaExpressionFile);
        }

        return targetFiles;
    }

    /**
     * Recursively generates standalone schema files for reused top-level ASBIEPs.
     *
     * This method is used for JSON generation when "Separate file references for reused schemas"
     * is enabled (#1711). The goal is to produce external reference targets that are independently
     * valid schema documents (with their own root and local {@code #/$defs} closure), instead of
     * partially extracted fragments.
     *
     * Generation flow:
     * 1) Seed a pending queue with initial referenced top-level ASBIEPs.
     * 2) Pop one pending item at a time and skip if it was already processed.
     * 3) Optionally skip items that are in {@code excludedTopLevelAsbiepIds}.
     * 4) Create a copied option with split-reference mode disabled so each referenced file is
     *    generated as a self-contained schema.
     * 5) Build a dedicated {@link GenerationContext} for that referenced top-level ASBIEP and
     *    generate its file using the same filename mapping strategy as the target schema.
     * 6) Discover nested reused top-level ASBIEPs from that dedicated context and enqueue them.
     *
     * Notes:
     * - A fresh expression/context per referenced top-level ASBIEP avoids cross-file state bleed.
     * - Filename resolution is synchronized between the original option and copied option so
     *   external {@code $ref} values and emitted filenames always align.
     * - The returned map preserves traversal order (LinkedHashMap) and contains one file per
     *   processed top-level ASBIEP ID.
     *
     * @param requester user requesting generation
     * @param initialReferences initially discovered reused top-level ASBIEPs
     * @param option original generation option (used for expression selection and filename policy)
     * @param excludedTopLevelAsbiepIds optional set of top-level ASBIEP IDs to skip (for example,
     *                                  IDs already selected as primary generation targets)
     * @return map of referenced top-level ASBIEP IDs to generated schema files
     * @throws BieGenerateFailureException when expression initialization or file generation fails
     */
    private Map<TopLevelAsbiepId, File> generateReferencedSchemasRecursively(
            ScoreUser requester,
            Set<TopLevelAsbiepSummaryRecord> initialReferences,
            GenerateExpressionOption option,
            Set<TopLevelAsbiepId> excludedTopLevelAsbiepIds) throws BieGenerateFailureException {

        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> pending = new LinkedHashMap<>();
        for (TopLevelAsbiepSummaryRecord refTopLevelAsbiep : initialReferences) {
            pending.put(refTopLevelAsbiep.topLevelAsbiepId(), refTopLevelAsbiep);
        }

        Set<TopLevelAsbiepId> processed = new HashSet<>();
        Map<TopLevelAsbiepId, File> files = new LinkedHashMap<>();
        while (!pending.isEmpty()) {
            TopLevelAsbiepSummaryRecord refTopLevelAsbiep = pending.values().iterator().next();
            pending.remove(refTopLevelAsbiep.topLevelAsbiepId());

            TopLevelAsbiepId topLevelAsbiepId = refTopLevelAsbiep.topLevelAsbiepId();
            if (processed.contains(topLevelAsbiepId)) {
                continue;
            }
            if (excludedTopLevelAsbiepIds != null && excludedTopLevelAsbiepIds.contains(topLevelAsbiepId)) {
                processed.add(topLevelAsbiepId);
                continue;
            }

            GenerateExpressionOption referencedOption = cloneOptionForReferencedSchema(option);
            BieGenerateExpression referencedGenerateExpression = createBieGenerateExpression(referencedOption);
            GenerationContext referencedGenerationContext = referencedGenerateExpression.generateContext(
                    requester, List.of(refTopLevelAsbiep), referencedOption);
            // #1711: Keep filename resolution consistent between referenced and target schemas.
            ensureExpressionFilenames(collectTopLevelAsbieps(List.of(refTopLevelAsbiep), referencedGenerationContext), option);
            ensureExpressionFilenames(collectTopLevelAsbieps(List.of(refTopLevelAsbiep), referencedGenerationContext), referencedOption);
            try {
                referencedGenerateExpression.reset();
            } catch (Exception e) {
                throw new BieGenerateFailureException("Unexpected error occurs during initialization of the expression processor.");
            }

            referencedGenerateExpression.generate(
                    requester, refTopLevelAsbiep, referencedGenerationContext, referencedOption);
            String filename = getFilenameByTopLevelAsbiep(refTopLevelAsbiep, option);
            try {
                files.put(topLevelAsbiepId, referencedGenerateExpression.asFile(filename));
            } catch (IOException e) {
                throw new BieGenerateFailureException("I/O operation failure.", e);
            }

            processed.add(topLevelAsbiepId);
            for (TopLevelAsbiepSummaryRecord nestedRefTopLevelAsbiep : referencedGenerationContext.getRefTopLevelAsbiepSet()) {
                if (!processed.contains(nestedRefTopLevelAsbiep.topLevelAsbiepId())) {
                    pending.put(nestedRefTopLevelAsbiep.topLevelAsbiepId(), nestedRefTopLevelAsbiep);
                }
            }
        }

        return files;
    }

    private List<TopLevelAsbiepSummaryRecord> collectTopLevelAsbieps(
            List<TopLevelAsbiepSummaryRecord> topLevelAsbieps,
            GenerationContext generationContext) {
        List<TopLevelAsbiepSummaryRecord> all = new java.util.ArrayList<>(topLevelAsbieps);
        all.addAll(generationContext.getRefTopLevelAsbiepSet());
        return all;
    }

    private void ensureExpressionFilenames(List<TopLevelAsbiepSummaryRecord> topLevelAsbieps,
                                           GenerateExpressionOption option) {
        Map<TopLevelAsbiepId, String> existing = option.getFilenames();
        Map<TopLevelAsbiepId, String> resolved = new LinkedHashMap<>();
        Map<String, Integer> filenameCount = new HashMap<>();
        if (existing != null && !existing.isEmpty()) {
            // #1711: Normalize pre-resolved names as well to avoid duplicate filenames.
            List<Map.Entry<TopLevelAsbiepId, String>> existingEntries = new ArrayList<>(existing.entrySet());
            existingEntries.sort(Comparator.comparing(e -> e.getKey().value()));
            for (Map.Entry<TopLevelAsbiepId, String> entry : existingEntries) {
                String baseFilename = StringUtils.trim(entry.getValue());
                if (!StringUtils.hasLength(baseFilename)) {
                    continue;
                }

                int count = filenameCount.getOrDefault(baseFilename, 0);
                String resolvedFilename = withDuplicateSuffix(baseFilename, count);
                filenameCount.put(baseFilename, count + 1);
                resolved.put(entry.getKey(), resolvedFilename);
            }
        }

        for (TopLevelAsbiepSummaryRecord topLevelAsbiep : topLevelAsbieps) {
            if (resolved.containsKey(topLevelAsbiep.topLevelAsbiepId())) {
                continue;
            }

            String baseFilename = getFilenameByTopLevelAsbiep(topLevelAsbiep, option);
            int count = filenameCount.getOrDefault(baseFilename, 0);
            String filename = withDuplicateSuffix(baseFilename, count);
            filenameCount.put(baseFilename, count + 1);
            resolved.put(topLevelAsbiep.topLevelAsbiepId(), filename);
        }

        option.setFilenames(resolved);
    }

    private String withDuplicateSuffix(String baseFilename, int count) {
        return (count == 0) ? baseFilename : (baseFilename + "-" + count);
    }

    private boolean isSeparateFileReferencesForReusedSchemasEnabled(GenerateExpressionOption option) {
        String expressionOption = option.getExpressionOption();

        return "JSON".equalsIgnoreCase(expressionOption)
                && !isJsonDraft04Version(option.getExpressionVersion())
                && option.isSeparateFileReferencesForReusedSchemas();
    }

    private boolean isJsonDraft04Version(String expressionVersion) {
        if (!StringUtils.hasLength(expressionVersion)) {
            return false;
        }

        String normalizedExpressionVersion = expressionVersion.trim().toUpperCase();
        return "DRAFT-04".equals(normalizedExpressionVersion)
                || "DRAFT04".equals(normalizedExpressionVersion);
    }

    private GenerateExpressionOption cloneOptionForReferencedSchema(GenerateExpressionOption option) {
        GenerateExpressionOption copied = new GenerateExpressionOption();
        BeanUtils.copyProperties(option, copied);
        copied.setSeparateFileReferencesForReusedSchemas(false);
        copied.setFilenames(new LinkedHashMap<>(option.getFilenames()));
        return copied;
    }

    private String getFilenameByTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerateExpressionOption option) {
        Map<TopLevelAsbiepId, String> filenames = option.getFilenames();
        if (filenames != null && filenames.containsKey(topLevelAsbiep.topLevelAsbiepId())) {
            return filenames.get(topLevelAsbiep.topLevelAsbiepId());
        }

        /*
         * Issue 566
         */
        AsbiepId rootAsbiepId = topLevelAsbiep.asbiepId();
        Record2<String, ULong> result = dslContext.select(ASBIEP.GUID, ASBIEP.BASED_ASCCP_MANIFEST_ID)
                .from(ASBIEP)
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(and(ASBIEP.ASBIEP_ID
                                .eq(ULong.valueOf(rootAsbiepId.value())),
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID
                                .eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value()))))
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
                    .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.topLevelAsbiepId().value())))
                    .fetchAny();
            BizCtxRecord bizCtxRecord = dslContext.selectFrom(BIZ_CTX)
                    .where(BIZ_CTX.BIZ_CTX_ID.eq(bizCtxAssignmentRecord.getBizCtxId()))
                    .fetchOne();

            if (bizCtxRecord != null) {
                sb.append('-').append(bizCtxRecord.getName().replaceAll(" ", "-"));
            }
        }

        if (option.isIncludeVersionInFilename()) {
            String version = StringUtils.trim(topLevelAsbiep.version());
            if (StringUtils.hasLength(version)) {
                sb.append('-').append(version.replaceAll("\\.", "_"));
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
                String expressionVersion = option.getExpressionVersion();
                if (expressionVersion != null) {
                    expressionVersion = expressionVersion.trim();
                }
                if (!StringUtils.hasLength(expressionVersion)) {
                    expressionVersion = "2020-12";
                }

                switch (expressionVersion.toUpperCase()) {
                    case "DRAFT-04":
                    case "DRAFT04":
                    case "DRAFT-05":
                    case "DRAFT05":
                        generateExpression = applicationContext.getBean(BieJSONDraft04GenerateExpression.class);
                        break;
                    case "2020-12":
                    case "202012":
                        generateExpression = applicationContext.getBean(BieJSON202012GenerateExpression.class);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown JSON expression version: " + expressionVersion);
                }
                break;
            case "OPENAPI30":
                generateExpression = applicationContext.getBean(BieOpenAPIGenerateExpression.class);
                break;
            case "ODF":
                generateExpression = applicationContext.getBean(BieODFSpreadsheetGenerationExpression.class);
                break;
            case "AVRO":
                generateExpression = applicationContext.getBean(BieAvroGenerateExpression.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown expression option: " + expressionOption);
        }

        return generateExpression;
    }
}
