package org.oagi.score.gateway.http.api.module_management.service;

import org.apache.commons.io.FileUtils;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.impl.DefaultExportContextBuilder;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.export.model.SchemaModule;
import org.oagi.score.export.service.CoreComponentService;
import org.oagi.score.gateway.http.api.module_management.data.AssignCCToModule;
import org.oagi.score.gateway.http.api.module_management.data.ExportModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.data.ModuleAssignComponents;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.corecomponent.model.CcType;
import org.oagi.score.repo.api.module.ModuleSetReleaseWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repository.CcRepository;
import org.oagi.score.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleSetReleaseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private CcRepository ccRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoreComponentService coreComponentService;

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
        File baseDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(baseDirectory);

        List<File> schemaFiles = exportModuleSetReleaseWithoutCompression(request.getRequester(), request.getModuleSetReleaseId(), baseDirectory);
        ValidateModuleSetReleaseResponse response = new ValidateModuleSetReleaseResponse();
        try {
            schemaFiles.parallelStream().forEach(schemaFile -> {
                logger.debug("Attempt to validate the schema file: " + schemaFile);
                try {
                    SchemaFactory schemaFactory = SchemaFactory.newDefaultInstance();
                    schemaFactory.newSchema(schemaFile);
                } catch (Exception e) {
                    try {
                        String moduleName = schemaFile.getCanonicalPath();
                        moduleName = moduleName.substring(baseDirectory.getCanonicalPath().length() + 1);
                        moduleName = moduleName.replaceAll("\\\\", "/");
                        response.addResult(moduleName, e.getMessage());
                    } catch (IOException ignore) {
                        logger.debug("I/O exception occurs", ignore);
                    }
                }
            });
        } finally {
            try {
                FileUtils.deleteDirectory(baseDirectory);
                logger.debug("After the validation, the base directory has been removed: " + baseDirectory);
            } catch (IOException ignore) {
                logger.debug("I/O exception occurs", ignore);
            }
        }

        return response;
    }

    public ExportModuleSetReleaseResponse exportModuleSetRelease(ScoreUser user, BigInteger moduleSetReleaseId) throws Exception {
        GetModuleSetReleaseRequest request = new GetModuleSetReleaseRequest(user);
        request.setModuleSetReleaseId(moduleSetReleaseId);
        ModuleSetRelease moduleSetRelease = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getModuleSetRelease(request).getModuleSetRelease();
        String fileName = moduleSetRelease.getModuleSetName().replace(" ", "");
        File baseDirectory = new File(new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString()), fileName);
        FileUtils.forceMkdir(baseDirectory);

        List<File> files = exportModuleSetReleaseWithoutCompression(user, moduleSetReleaseId, baseDirectory);
        File zipFile = Zip.compressionHierarchy(baseDirectory, files);
        FileUtils.deleteDirectory(baseDirectory);
        return new ExportModuleSetReleaseResponse(fileName + ".zip", zipFile);
    }

    private List<File> exportModuleSetReleaseWithoutCompression(ScoreUser user, BigInteger moduleSetReleaseId,
                                                                File baseDirectory) throws Exception {
        ImportedDataProvider dataProvider = new ImportedDataProvider(ccRepository, moduleSetReleaseId);
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
        List<AssignableNode> accList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedACCByModuleSetReleaseId(request);
        List<AssignableNode> asccpList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedASCCPByModuleSetReleaseId(request);
        List<AssignableNode> bccpList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedBCCPByModuleSetReleaseId(request);
        List<AssignableNode> dtList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedDTByModuleSetReleaseId(request);
        List<AssignableNode> codeListList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedCodeListByModuleSetReleaseId(request);
        List<AssignableNode> agencyIdListList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedAgencyIdListByModuleSetReleaseId(request);
        List<AssignableNode> xbtList = scoreRepositoryFactory.createModuleSetReleaseReadRepository().getAssignedXBTByModuleSetReleaseId(request);
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
