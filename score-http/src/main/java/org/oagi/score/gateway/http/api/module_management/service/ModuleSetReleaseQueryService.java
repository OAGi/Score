package org.oagi.score.gateway.http.api.module_management.service;

import org.apache.commons.io.FileUtils;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.impl.DefaultExportContextBuilder;
import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.model.SchemaModule;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleSetReleaseQueryService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

        String requestId = UUID.randomUUID().toString();
        File baseDirectory = new File(FileUtils.getTempDirectory(), requestId);
        FileUtils.forceMkdir(baseDirectory);

        List<File> schemaFiles = exportModuleSetReleaseWithoutCompression(
                requester, moduleSetReleaseId, baseDirectory);

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

        ModuleCcDocument moduleCcDocument = new ModuleCcDocumentImpl(requester, repositoryFactory, moduleSetReleaseId);
        DefaultExportContextBuilder builder = new DefaultExportContextBuilder(
                moduleQuery(requester), moduleCcDocument, moduleSetReleaseId);
        XMLExportSchemaModuleVisitor visitor = new XMLExportSchemaModuleVisitor(moduleCcDocument);
        ExportContext exportContext = builder.build(moduleSetReleaseId);

        List<File> files = new ArrayList<>();

        for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
            visitor.setBaseDirectory(baseDirectory);
            schemaModule.visit(visitor);
            File file = visitor.endSchemaModule(schemaModule);
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

                    SchemaFactory schemaFactory = SchemaFactory.newDefaultInstance();
                    schemaFactory.newSchema(schemaFile);
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

    public ExportModuleSetReleaseResponse exportModuleSetRelease(
            ScoreUser requester, ModuleSetReleaseId moduleSetReleaseId) throws Exception {

        ModuleSetReleaseDetailsRecord moduleSetRelease =
                moduleSetReleaseQuery(requester).getModuleSetReleaseDetails(moduleSetReleaseId);

        ModuleSetDetailsRecord moduleSet =
                moduleSetQuery(requester).getModuleSetDetails(moduleSetRelease.moduleSet().moduleSetId());

        String fileName = moduleSet.name().replace(" ", "");
        File baseDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        File rootDirectory = new File(baseDirectory, fileName);
        FileUtils.forceMkdir(rootDirectory);

        List<File> files = exportModuleSetReleaseWithoutCompression(requester, moduleSetReleaseId, rootDirectory);
        File zipFile = Zip.compressionHierarchy(baseDirectory, files);
        FileUtils.deleteDirectory(baseDirectory);
        return new ExportModuleSetReleaseResponse(fileName + ".zip", zipFile);
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
