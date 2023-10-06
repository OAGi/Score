package org.oagi.score.gateway.http.api.module_management.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.impl.DefaultExportContextBuilder;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.export.model.SchemaModule;
import org.oagi.score.export.service.CoreComponentService;
import org.oagi.score.gateway.http.api.module_management.data.AssignCCToModule;
import org.oagi.score.gateway.http.api.module_management.data.ExportModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.data.ModuleAssignComponents;
import org.oagi.score.gateway.http.api.module_management.provider.ModuleSetReleaseDataProvider;
import org.oagi.score.gateway.http.api.release_management.data.ReleaseState;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.event.ModuleSetReleaseValidationRequestEvent;
import org.oagi.score.gateway.http.event.ReleaseCreateRequestEvent;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.redis.event.EventListenerContainer;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.corecomponent.model.CcType;
import org.oagi.score.repo.api.module.ModuleSetReleaseReadRepository;
import org.oagi.score.repo.api.module.ModuleSetReleaseWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repository.CoreComponentRepositoryForModuleSetRelease;
import org.oagi.score.repository.ModuleRepository;
import org.redisson.api.*;
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
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleSetReleaseService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private CoreComponentRepositoryForModuleSetRelease coreComponentRepositoryForModuleSetRelease;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private SessionService sessionService;

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

    public GetModuleSetReleaseListResponse getModuleSetReleaseList(GetModuleSetReleaseListRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetReleaseList(request);
    }

    public GetModuleSetReleaseResponse getModuleSetRelease(GetModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetRelease(request);
    }

    @Transactional
    public CreateModuleSetReleaseResponse createModuleSetRelease(CreateModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().createModuleSetRelease(request);
    }

    @Transactional
    public UpdateModuleSetReleaseResponse updateModuleSetRelease(UpdateModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().updateModuleSetRelease(request);
    }

    @Transactional
    public DeleteModuleSetReleaseResponse discardModuleSetRelease(DeleteModuleSetReleaseRequest request) {
        return scoreRepositoryFactory.createModuleSetReleaseWriteRepository().deleteModuleSetRelease(request);
    }

    public ValidateModuleSetReleaseResponse validateModuleSetRelease(ValidateModuleSetReleaseRequest request) throws Exception {
        String requestId = UUID.randomUUID().toString();
        File baseDirectory = new File(FileUtils.getTempDirectory(), requestId);
        FileUtils.forceMkdir(baseDirectory);

        List<File> schemaFiles = exportModuleSetReleaseWithoutCompression(
                request.getRequester(), request.getModuleSetReleaseId(), baseDirectory);

        ModuleSetReleaseValidationRequestEvent event = new ModuleSetReleaseValidationRequestEvent(
                request.getRequester().getUserId(),
                request.getModuleSetReleaseId(),
                requestId, baseDirectory, schemaFiles);

        /*
         * Message Publishing
         */
        redisTemplate.convertAndSend(MODULE_SET_RELEASE_VALIDATION_REQUEST_EVENT, event);

        ValidateModuleSetReleaseResponse response = new ValidateModuleSetReleaseResponse();
        response.setRequestId(event.getRequestId());
        response.setLength(schemaFiles.size());
        return response;
    }

    public ValidateModuleSetReleaseResponse progressValidationModuleSetRelease(ValidateModuleSetReleaseRequest request) {
        RAtomicLong counter = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + request.getRequestId() + ":Counter");
        RAtomicLong length = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + request.getRequestId() + ":Length");
        RAtomicLong done = redissonClient.getAtomicLong("ModuleSetReleaseValidationRequestEvent:" + request.getRequestId() + ":Done");
        RMap<String, String> resultMap = redissonClient.getMap("ModuleSetReleaseValidationRequestEvent:" + request.getRequestId() + ":Result");

        ValidateModuleSetReleaseResponse response = new ValidateModuleSetReleaseResponse();
        response.setRequestId(request.getRequestId());
        response.setProgress(counter.get());
        response.setLength(length.get());
        response.setDone(done.get() == 1);
        response.setResults(resultMap.readAllMap());
        return response;
    }

    /**
     * This method is invoked by 'moduleSetReleaseValidationRequestEvent' channel subscriber.
     *
     * @param event
     */
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

    public ExportModuleSetReleaseResponse exportModuleSetRelease(ScoreUser user, BigInteger moduleSetReleaseId) throws Exception {
        GetModuleSetReleaseRequest request = new GetModuleSetReleaseRequest(user);
        request.setModuleSetReleaseId(moduleSetReleaseId);
        ModuleSetRelease moduleSetRelease = scoreRepositoryFactory.createModuleSetReleaseReadRepository()
                .getModuleSetRelease(request).getModuleSetRelease();
        String fileName = moduleSetRelease.getModuleSetName().replace(" ", "");
        File baseDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        File rootDirectory = new File(baseDirectory, fileName);
        FileUtils.forceMkdir(rootDirectory);

        List<File> files = exportModuleSetReleaseWithoutCompression(user, moduleSetReleaseId, rootDirectory);
        File zipFile = Zip.compressionHierarchy(baseDirectory, files);
        FileUtils.deleteDirectory(baseDirectory);
        return new ExportModuleSetReleaseResponse(fileName + ".zip", zipFile);
    }

    private List<File> exportModuleSetReleaseWithoutCompression(ScoreUser user, BigInteger moduleSetReleaseId,
                                                                File baseDirectory) throws Exception {
        ModuleSetReleaseDataProvider dataProvider = new ModuleSetReleaseDataProvider(coreComponentRepositoryForModuleSetRelease, moduleSetReleaseId);
        DefaultExportContextBuilder builder = new DefaultExportContextBuilder(moduleRepository, dataProvider, moduleSetReleaseId);
        XMLExportSchemaModuleVisitor visitor = new XMLExportSchemaModuleVisitor(coreComponentService, dataProvider);
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

    public ModuleAssignComponents getAssignableCCs(GetAssignableCCListRequest request) {
        ModuleAssignComponents assignComponents = new ModuleAssignComponents();

        GetModuleSetReleaseRequest moduleSetReleaseRequest = new GetModuleSetReleaseRequest(request.getRequester());
        moduleSetReleaseRequest.setModuleSetReleaseId(request.getModuleSetReleaseId());
        GetModuleSetReleaseResponse moduleSetReleaseResponse = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetRelease(moduleSetReleaseRequest);
        request.setReleaseId(moduleSetReleaseResponse.getModuleSetRelease().getReleaseId());

        List<AssignableNode> accList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableACCByModuleSetReleaseId(request);
        List<AssignableNode> asccpList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableASCCPByModuleSetReleaseId(request);
        List<AssignableNode> bccpList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableBCCPByModuleSetReleaseId(request);
        List<AssignableNode> dtList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableDTByModuleSetReleaseId(request);
        List<AssignableNode> codeListList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableCodeListByModuleSetReleaseId(request);
        List<AssignableNode> agencyIdListList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableAgencyIdListByModuleSetReleaseId(request);
        List<AssignableNode> xbtList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignableXBTByModuleSetReleaseId(request);

        assignComponents.setAssignableAccManifestMap(accList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableAsccpManifestMap(asccpList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableBccpManifestMap(bccpList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableDtManifestMap(dtList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableCodeListManifestMap(codeListList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableAgencyIdListManifestMap(agencyIdListList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignableXbtManifestMap(xbtList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));

        return assignComponents;
    }

    public ModuleAssignComponents getAssignedCCs(ScoreUser user, BigInteger moduleSetReleaseId, BigInteger moduleId) {
        ModuleAssignComponents assignComponents = new ModuleAssignComponents();
        GetAssignedCCListRequest request = new GetAssignedCCListRequest(user);
        request.setModuleSetReleaseId(moduleSetReleaseId);
        request.setModuleId(moduleId);

        ModuleSetReleaseReadRepository moduleSetReleaseReadRepository = scoreRepositoryFactory.createModuleSetReleaseReadRepository();

        List<AssignableNode> accList = moduleSetReleaseReadRepository.getAssignedACCByModuleSetReleaseId(request);
        List<AssignableNode> asccpList = moduleSetReleaseReadRepository.getAssignedASCCPByModuleSetReleaseId(request);
        List<AssignableNode> bccpList = moduleSetReleaseReadRepository.getAssignedBCCPByModuleSetReleaseId(request);
        List<AssignableNode> dtList = moduleSetReleaseReadRepository.getAssignedDTByModuleSetReleaseId(request);
        List<AssignableNode> codeListList = moduleSetReleaseReadRepository.getAssignedCodeListByModuleSetReleaseId(request);
        List<AssignableNode> agencyIdListList = moduleSetReleaseReadRepository.getAssignedAgencyIdListByModuleSetReleaseId(request);
        List<AssignableNode> xbtList = moduleSetReleaseReadRepository.getAssignedXBTByModuleSetReleaseId(request);

        assignComponents.setAssignedAccManifestMap(accList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedAsccpManifestMap(asccpList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedBccpManifestMap(bccpList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedDtManifestMap(dtList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedCodeListManifestMap(codeListList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedAgencyIdListManifestMap(agencyIdListList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));
        assignComponents.setAssignedXbtManifestMap(xbtList.stream().collect(Collectors.toMap(AssignableNode::getManifestId, Function.identity())));

        return assignComponents;
    }

    @Transactional
    public void setAssignCc(ScoreUser user, AssignCCToModule assignCCToModule) {
        ModuleSetReleaseWriteRepository repo = scoreRepositoryFactory.createModuleSetReleaseWriteRepository();
        LocalDateTime timestamp = LocalDateTime.now();
        assignCCToModule.getNodes().forEach(node -> {
            CreateModuleManifestRequest request = new CreateModuleManifestRequest(user);
            request.setType(CcType.valueOf(node.getType()));
            request.setManifestId(node.getManifestId());
            request.setModuleId(assignCCToModule.getModuleId());
            request.setModuleSetReleaseId(assignCCToModule.getModuleSetReleaseId());
            request.setTimestamp(timestamp);
            repo.createModuleManifest(request);
        });
    }

    @Transactional
    public void unAssignCc(ScoreUser user, AssignCCToModule assignCCToModule) {
        ModuleSetReleaseWriteRepository repo = scoreRepositoryFactory.createModuleSetReleaseWriteRepository();
        LocalDateTime timestamp = LocalDateTime.now();
        assignCCToModule.getNodes().forEach(node -> {
            DeleteModuleManifestRequest request = new DeleteModuleManifestRequest(user);
            request.setType(CcType.valueOf(node.getType()));
            request.setManifestId(node.getManifestId());
            request.setModuleId(assignCCToModule.getModuleId());
            request.setModuleSetReleaseId(assignCCToModule.getModuleSetReleaseId());
            repo.deleteModuleManifest(request);
        });
    }
}
