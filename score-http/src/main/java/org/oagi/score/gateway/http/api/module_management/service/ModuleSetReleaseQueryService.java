package org.oagi.score.gateway.http.api.module_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import org.apache.commons.io.FileUtils;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.impl.DefaultExportContextBuilder;
import org.oagi.score.gateway.http.api.export.impl.ExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.impl.JSONExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.model.JsonSchemaNamingStrategy;
import org.oagi.score.gateway.http.api.export.model.SchemaNamingStrategy;
import org.oagi.score.gateway.http.api.export.model.SchemaModule;
import org.oagi.score.gateway.http.api.export.model.XmlSchemaNamingStrategy;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ExportModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ModuleAssignableComponentsRecord;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ModuleAssignedComponentsRecord;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ValidateModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.model.event.ModuleSetReleaseValidationRequestEvent;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleManifestQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetReleaseQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.Zip;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleSetReleaseQueryService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ModuleSetReleaseQueryRepository moduleSetReleaseQuery(ScoreUser requester) {
        return repositoryFactory.moduleSetReleaseQueryRepository(requester);
    }

    private ModuleSetQueryRepository moduleSetQuery(ScoreUser requester) {
        return repositoryFactory.moduleSetQueryRepository(requester);
    }

    private ModuleQueryRepository moduleQuery(ScoreUser requester) {
        return repositoryFactory.moduleQueryRepository(requester);
    }

    private ModuleManifestQueryRepository moduleManifestQuery(ScoreUser requester) {
        return repositoryFactory.moduleManifestQueryRepository(requester);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private final String MODULE_SET_RELEASE_VALIDATION_REQUEST_EVENT = "moduleSetReleaseValidationRequestEvent";

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this, "onModuleSetReleaseValidationRequestEventReceived",
                new ChannelTopic(MODULE_SET_RELEASE_VALIDATION_REQUEST_EVENT));
    }

    public List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(
            ScoreUser requester, LibraryId libraryId) {

        return moduleSetReleaseQuery(requester).getModuleSetReleaseSummaryList(libraryId);
    }

    public ModuleSetReleaseDetailsRecord getModuleSetReleaseDetails(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) {

        return moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(moduleSetReleaseId);
    }

    public ResultAndCount<ModuleSetReleaseListEntryRecord> getModuleSetReleaseList(
            ScoreUser requester, ModuleSetReleaseListFilterCriteria filterCriteria, PageRequest pageRequest) {

        return moduleSetReleaseQuery(requester).getModuleSetReleaseList(filterCriteria, pageRequest);
    }

    public ValidateModuleSetReleaseResponse validateModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) throws Exception {
        return validateModuleSetRelease(requester, moduleSetReleaseId, "XML", null);
    }

    public ValidateModuleSetReleaseResponse validateModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId,
            String expressionOption, String expressionVersion) throws Exception {

        String requestId = UUID.randomUUID().toString();
        File baseDirectory = new File(FileUtils.getTempDirectory(), requestId);
        FileUtils.forceMkdir(baseDirectory);

        List<File> schemaFiles = exportModuleSetReleaseWithoutCompression(
                requester, moduleSetReleaseId, baseDirectory, expressionOption, expressionVersion);

        ModuleSetReleaseValidationRequestEvent event = new ModuleSetReleaseValidationRequestEvent(
                requester.userId(),
                moduleSetReleaseId,
                requestId, baseDirectory, schemaFiles);

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(MODULE_SET_RELEASE_VALIDATION_REQUEST_EVENT, event);

        return new ValidateModuleSetReleaseResponse(
                Collections.emptyMap(),
                event.getRequestId(),
                0,
                schemaFiles.size(),
                false
        );
    }

    private List<File> exportModuleSetReleaseWithoutCompression(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId, File baseDirectory) throws Exception {
        return exportModuleSetReleaseWithoutCompression(requester, moduleSetReleaseId, baseDirectory, "XML", null);
    }

    private List<File> exportModuleSetReleaseWithoutCompression(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId, File baseDirectory,
            String expressionOption, String expressionVersion) throws Exception {

        String normalizedExpressionOption = normalizeExpressionOption(expressionOption);
        validateExpressionVersion(normalizedExpressionOption, expressionVersion);

        ModuleCcDocument moduleCcDocument = new ModuleCcDocumentImpl(requester, repositoryFactory, moduleSetReleaseId);
        SchemaNamingStrategy namingStrategy = newSchemaNamingStrategy(normalizedExpressionOption);
        DefaultExportContextBuilder builder = new DefaultExportContextBuilder(
                moduleQuery(requester), moduleCcDocument, moduleSetReleaseId, namingStrategy);
        ExportSchemaModuleVisitor visitor = newSchemaModuleVisitor(moduleCcDocument, normalizedExpressionOption);
        ExportContext exportContext = builder.build(moduleSetReleaseId);

        List<File> files = new ArrayList<>();

        visitor.setBaseDirectory(baseDirectory);
        for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
            schemaModule.visit(visitor);
            File file = schemaModule.getModuleFile();
            if (file != null) {
                files.add(file);
            }
        }

        return files;
    }

    public ValidateModuleSetReleaseResponse progressValidationModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId, String requestId) {

        RAtomicLong counter = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + requestId + ":Counter");
        RAtomicLong length = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + requestId + ":Length");
        RAtomicLong done = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + requestId + ":Done");
        RMap<String, String> resultMap = redissonClient.getMap("ModuleSetReleaseValidationRequestEvent:" + requestId + ":Result");

        return new ValidateModuleSetReleaseResponse(
                resultMap.readAllMap(), requestId, counter.get(), length.get(), done.get() == 1);
    }

    public void onModuleSetReleaseValidationRequestEventReceived(ModuleSetReleaseValidationRequestEvent event) {
        RLock lock = redissonClient.getLock("ModuleSetReleaseValidationRequestEvent:" + event.getRequestId());
        if (!lock.tryLock()) {
            return;
        }

        logger.debug("Received ModuleSetReleaseValidationRequestEvent: " + event);
        RAtomicLong counter = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + event.getRequestId() + ":Counter");
        RMap<String, String> resultMap = redissonClient.getMap("ModuleSetReleaseValidationRequestEvent:" + event.getRequestId() + ":Result");

        File baseDirectory = event.getBaseDirectory();
        List<File> schemaFiles = event.getSchemaFiles();
        Map<String, String> jsonSchemaSources = buildJsonSchemaSources(baseDirectory, schemaFiles);
        RAtomicLong length = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + event.getRequestId() + ":Length");
        length.set(schemaFiles.size());
        RAtomicLong done = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + event.getRequestId() + ":Done");
        try {
            schemaFiles.parallelStream().forEach(schemaFile -> {
                logger.debug("Attempt to validate the schema file: " + schemaFile);
                String moduleName = null;
                try {
                    moduleName = schemaFile.getCanonicalPath();
                    moduleName = moduleName.substring(baseDirectory.getCanonicalPath().length() + 1);
                    moduleName = moduleName.replaceAll("\\\\", "/");

                    SchemaModuleValidator validator = newSchemaModuleValidator(baseDirectory, schemaFile, jsonSchemaSources);
                    validator.validate(schemaFile);
                    resultMap.put(moduleName, "Valid");
                } catch (Exception e) {
                    logger.error("Unexpected error occurs during the module set release validation", e);
                    if (moduleName != null) {
                        resultMap.putAsync(moduleName, e.getMessage());
                    } else {
                        resultMap.putAsync(schemaFile.getName(), e.getMessage());
                    }
                } finally {
                    counter.incrementAndGet();
                }
            });
        } catch (Exception e) {
            logger.error("Unexpected error occurs during the module set release validation", e);
            resultMap.put("" + event.getRequestId(), e.getMessage());
        } finally {
            done.incrementAndGet();

            Duration duration = Duration.ofMinutes(1);
            counter.expire(duration);
            length.expire(duration);
            done.expire(duration);
            resultMap.expire(duration);

            lock.unlock();

            try {
                FileUtils.deleteDirectory(baseDirectory);
                logger.debug("After the validation, the base directory has been removed: " + baseDirectory);
            } catch (IOException ignore) {
                logger.debug("I/O exception occurs", ignore);
            }
        }
    }

    private SchemaModuleValidator newSchemaModuleValidator(File baseDirectory,
                                                           File schemaFile,
                                                           Map<String, String> jsonSchemaSources) {
        if (schemaFile.getName().endsWith(".json")) {
            return new JSONSchemaValidator(baseDirectory, jsonSchemaSources);
        }
        if (schemaFile.getName().endsWith(".xsd")) {
            return new XMLSchemaValidator();
        }
        throw new IllegalArgumentException("Unsupported schema file type: " + schemaFile.getName());
    }

    private Map<String, String> buildJsonSchemaSources(File baseDirectory, List<File> schemaFiles) {
        Map<String, String> schemaSources = new LinkedHashMap<>();
        for (File schemaFile : schemaFiles) {
            if (!schemaFile.getName().endsWith(".json")) {
                continue;
            }

            try {
                String content = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
                String relativePath = baseDirectory.toPath().relativize(schemaFile.toPath()).toString().replace("\\", "/");
                String fileUri = schemaFile.toURI().toString();

                schemaSources.put(relativePath, content);
                schemaSources.put("/" + relativePath, content);
                schemaSources.put(fileUri, content);

                var schemaNode = objectMapper.readTree(content);
                var idNode = schemaNode.get("$id");
                if (idNode != null && idNode.isTextual() && !idNode.asText().isBlank()) {
                    String schemaId = idNode.asText();
                    schemaSources.put(schemaId, content);
                    schemaSources.put("/" + schemaId, content);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.warn("Skipping JSON schema id registration for invalid JSON source: {}", schemaFile, e);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read JSON schema source: " + schemaFile, e);
            }
        }
        return schemaSources;
    }

    private String resolveJsonSchemaSource(File baseDirectory,
                                           Map<String, String> jsonSchemaSources,
                                           String schemaLocation) {
        for (String normalizedLocation : candidateJsonSchemaLocations(baseDirectory, schemaLocation)) {
            String schemaSource = jsonSchemaSources.get(normalizedLocation);
            if (schemaSource != null) {
                return schemaSource;
            }

            if (!normalizedLocation.startsWith("/") && jsonSchemaSources.containsKey("/" + normalizedLocation)) {
                return jsonSchemaSources.get("/" + normalizedLocation);
            }

            if (normalizedLocation.startsWith("file:")) {
                try {
                    File schemaFile = new File(java.net.URI.create(normalizedLocation));
                    if (schemaFile.isFile()) {
                        return FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
                    }
                } catch (Exception ignore) {
                    logger.debug("Unable to resolve JSON schema location from file URI: {}", normalizedLocation, ignore);
                }
            }

            File schemaFile = new File(baseDirectory, normalizedLocation);
            if (schemaFile.isFile()) {
                try {
                    return FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read JSON schema source: " + normalizedLocation, e);
                }
            }
        }

        return null;
    }

    private List<String> candidateJsonSchemaLocations(File baseDirectory, String schemaLocation) {
        String normalizedLocation = stripJsonSchemaFragment(schemaLocation);
        List<String> candidates = new ArrayList<>();
        candidates.add(normalizedLocation);

        if (normalizedLocation.startsWith("file:")) {
            try {
                File schemaFile = new File(java.net.URI.create(normalizedLocation));
                String absolutePath = schemaFile.getCanonicalPath().replace("\\", "/");
                candidates.add(absolutePath);

                String basePath = baseDirectory.getCanonicalPath().replace("\\", "/");
                if (absolutePath.startsWith(basePath + "/")) {
                    candidates.add(absolutePath.substring(basePath.length() + 1));
                }
                addModelPathCandidates(candidates, absolutePath);
            } catch (Exception ignore) {
                logger.debug("Unable to normalize JSON schema location: {}", normalizedLocation, ignore);
            }
        } else {
            addModelPathCandidates(candidates, normalizedLocation);
        }

        return candidates.stream().distinct().toList();
    }

    private void addModelPathCandidates(List<String> candidates, String location) {
        String normalized = location.replace("\\", "/");
        int modelIndex = normalized.indexOf("/Model/");
        if (modelIndex >= 0) {
            candidates.add(normalized.substring(modelIndex + 1));
        }

        int lastModelIndex = normalized.lastIndexOf("/Model/");
        if (lastModelIndex >= 0) {
            candidates.add(normalized.substring(lastModelIndex + 1));
        }

        if (normalized.startsWith("Model/")) {
            candidates.add(normalized);
        }
    }

    private String stripJsonSchemaFragment(String schemaLocation) {
        int fragmentIndex = schemaLocation.indexOf('#');
        return (fragmentIndex >= 0) ? schemaLocation.substring(0, fragmentIndex) : schemaLocation;
    }

    private interface SchemaModuleValidator {
        void validate(File schemaFile) throws Exception;
    }

    private static final class XMLSchemaValidator implements SchemaModuleValidator {
        @Override
        public void validate(File schemaFile) throws Exception {
            SchemaFactory schemaFactory = SchemaFactory.newDefaultInstance();
            schemaFactory.newSchema(schemaFile);
        }
    }

    private final class JSONSchemaValidator implements SchemaModuleValidator {
        private final File baseDirectory;
        private final Map<String, String> jsonSchemaSources;

        private JSONSchemaValidator(File baseDirectory, Map<String, String> jsonSchemaSources) {
            this.baseDirectory = baseDirectory;
            this.jsonSchemaSources = jsonSchemaSources;
        }

        @Override
        public void validate(File schemaFile) throws Exception {
            String schemaSource = FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
            SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(), builder ->
                    builder.schemas(uri -> resolveJsonSchemaSource(baseDirectory, jsonSchemaSources, uri)));

            Schema schema = schemaRegistry.getSchema(SchemaLocation.of(schemaFile.toURI().toString()), schemaSource, InputFormat.JSON);
            schema.initializeValidators();

            Schema metaSchema = schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().getId()));
            List<Error> errors = metaSchema.validate(schemaSource, InputFormat.JSON);
            if (!errors.isEmpty()) {
                throw new IllegalStateException(errors.get(0).getMessage());
            }
        }
    }

    public ExportModuleSetReleaseResponse exportModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) throws Exception {
        return exportModuleSetRelease(requester, moduleSetReleaseId, "XML", null);
    }

    public ExportModuleSetReleaseResponse exportModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId,
            String expressionOption, String expressionVersion) throws Exception {

        String normalizedExpressionOption = normalizeExpressionOption(expressionOption);
        validateExpressionVersion(normalizedExpressionOption, expressionVersion);

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(moduleSetReleaseId);

        ModuleSetDetailsRecord moduleSet =
                moduleSetQuery(requester).getModuleSetDetails(moduleSetRelease.moduleSet().moduleSetId());

        String fileName = moduleSet.name().replace(" ", "");
        File baseDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        File rootDirectory = new File(baseDirectory, fileName);
        FileUtils.forceMkdir(rootDirectory);

        try {
            List<File> files = exportModuleSetReleaseWithoutCompression(
                    requester, moduleSetReleaseId, rootDirectory, normalizedExpressionOption, expressionVersion);
            File zipFile = Zip.compressionHierarchy(baseDirectory, files);
            return new ExportModuleSetReleaseResponse(fileName + ".zip", zipFile);
        } finally {
            FileUtils.deleteDirectory(baseDirectory);
        }
    }

    private ExportSchemaModuleVisitor newSchemaModuleVisitor(ModuleCcDocument moduleCcDocument, String expressionOption) {
        if ("JSON".equals(expressionOption)) {
            return new JSONExportSchemaModuleVisitor(moduleCcDocument, new JsonSchemaNamingStrategy());
        }
        return new XMLExportSchemaModuleVisitor(moduleCcDocument, new XmlSchemaNamingStrategy());
    }

    private SchemaNamingStrategy newSchemaNamingStrategy(String expressionOption) {
        if ("JSON".equals(expressionOption)) {
            return new JsonSchemaNamingStrategy();
        }
        return new XmlSchemaNamingStrategy();
    }

    private String normalizeExpressionOption(String expressionOption) {
        if (!org.springframework.util.StringUtils.hasLength(expressionOption)) {
            return "XML";
        }
        String normalized = expressionOption.trim().toUpperCase();
        if ("XML".equals(normalized) || "JSON".equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("Unsupported expression option: " + expressionOption);
    }

    private void validateExpressionVersion(String expressionOption, String expressionVersion) {
        if (!"JSON".equals(expressionOption)) {
            return;
        }
        if (!org.springframework.util.StringUtils.hasLength(expressionVersion)) {
            return;
        }
        String normalizedVersion = expressionVersion.trim().toUpperCase();
        if (!"2020-12".equals(normalizedVersion) && !"202012".equals(normalizedVersion)) {
            throw new IllegalArgumentException("Unsupported JSON expression version: " + expressionVersion);
        }
    }

    public ModuleAssignableComponentsRecord getAssignableCCs(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) {

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(moduleSetReleaseId);

        var query = moduleManifestQuery(requester);
        List<AssignNodeRecord> accList = query.getAssignableACCByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> asccpList = query.getAssignableASCCPByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> bccpList = query.getAssignableBCCPByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> dtList = query.getAssignableDTByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> codeListList = query.getAssignableCodeListByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> agencyIdListList = query.getAssignableAgencyIdListByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());
        List<AssignNodeRecord> xbtList = query.getAssignableXBTByModuleSetReleaseId(
                moduleSetReleaseId, moduleSetRelease.release().releaseId());

        return new ModuleAssignableComponentsRecord(
                accList.stream().collect(Collectors.toMap(AssignNodeRecord<AccManifestId>::manifestId, Function.identity())),
                asccpList.stream().collect(Collectors.toMap(AssignNodeRecord<AsccpManifestId>::manifestId, Function.identity())),
                bccpList.stream().collect(Collectors.toMap(AssignNodeRecord<BccpManifestId>::manifestId, Function.identity())),
                dtList.stream().collect(Collectors.toMap(AssignNodeRecord<DtManifestId>::manifestId, Function.identity())),
                codeListList.stream().collect(Collectors.toMap(AssignNodeRecord<CodeListManifestId>::manifestId, Function.identity())),
                agencyIdListList.stream().collect(Collectors.toMap(AssignNodeRecord<AgencyIdListManifestId>::manifestId, Function.identity())),
                xbtList.stream().collect(Collectors.toMap(AssignNodeRecord<XbtManifestId>::manifestId, Function.identity()))
        );
    }

    public ModuleAssignedComponentsRecord getAssignedCCs(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId) {

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(moduleSetReleaseId);

        var query = moduleManifestQuery(requester);
        List<AssignNodeRecord> accList = query.getAssignedACCByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> asccpList = query.getAssignedASCCPByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> bccpList = query.getAssignedBCCPByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> dtList = query.getAssignedDTByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> codeListList = query.getAssignedCodeListByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> agencyIdListList = query.getAssignedAgencyIdListByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);
        List<AssignNodeRecord> xbtList = query.getAssignedXBTByModuleSetReleaseId(
                moduleSetReleaseId, moduleId);

        return new ModuleAssignedComponentsRecord(
                accList.stream().collect(Collectors.toMap(AssignNodeRecord<AccManifestId>::manifestId, Function.identity())),
                asccpList.stream().collect(Collectors.toMap(AssignNodeRecord<AsccpManifestId>::manifestId, Function.identity())),
                bccpList.stream().collect(Collectors.toMap(AssignNodeRecord<BccpManifestId>::manifestId, Function.identity())),
                dtList.stream().collect(Collectors.toMap(AssignNodeRecord<DtManifestId>::manifestId, Function.identity())),
                codeListList.stream().collect(Collectors.toMap(AssignNodeRecord<CodeListManifestId>::manifestId, Function.identity())),
                agencyIdListList.stream().collect(Collectors.toMap(AssignNodeRecord<AgencyIdListManifestId>::manifestId, Function.identity())),
                xbtList.stream().collect(Collectors.toMap(AssignNodeRecord<XbtManifestId>::manifestId, Function.identity()))
        );
    }
}
