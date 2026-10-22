package org.oagi.score.gateway.http.api.external.service;

import org.apache.commons.io.FileUtils;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.impl.StandaloneExportContextBuilder;
import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.model.SchemaModule;
import org.oagi.score.gateway.http.api.external.model.ExternalChildComponentRecord;
import org.oagi.score.gateway.http.api.external.repository.ExternalComponentsRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ExportStandaloneSchemaResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExternalComponentsService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ExternalComponentsRepository externalComponentsRepository;

    public LibraryId getLibraryId(String libraryName) {
        return externalComponentsRepository.getLibraryId(libraryName);
    }

    public ReleaseId getReleaseId(String libraryName, String releaseVersion) {
        return externalComponentsRepository.getReleaseId(libraryName, releaseVersion);
    }

    public AsccpManifestId getAsccpManifestId(ReleaseId releaseId, String guid) {

        return externalComponentsRepository.getAsccpManifestId(releaseId, guid);
    }

    public ExportStandaloneSchemaResponse exportStandaloneSchema(
            ScoreUser requester, List<AsccpManifestId> asccpManifestIdList) throws Exception {
        if (asccpManifestIdList == null || asccpManifestIdList.isEmpty()) {
            throw new IllegalArgumentException();
        }

        File baseDir = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(baseDir);

        try {
            List<File> files = new ArrayList<>();

            Map<AsccpManifestId, ReleaseId> releaseIdMap = repositoryFactory.releaseQueryRepository(requester)
                    .getReleaseIdMapByAsccpManifestIdList(asccpManifestIdList);
            CcDocument ccDocument = repositoryFactory.ccDocumentFactory().create(requester, releaseIdMap.values());
            Map<String, Integer> pathCounter = new ConcurrentHashMap<>();
            List<Exception> exceptions = new ArrayList<>();
            asccpManifestIdList.parallelStream().forEach(asccpManifestId -> {
                try {
                    XMLExportSchemaModuleVisitor visitor = new XMLExportSchemaModuleVisitor(ccDocument);
                    visitor.setBaseDirectory(baseDir);

                    StandaloneExportContextBuilder builder = new StandaloneExportContextBuilder(ccDocument,
                            pathCounter);
                    ExportContext exportContext = builder.build(asccpManifestId);

                    for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
                        schemaModule.visit(visitor);
                        File file = schemaModule.getModuleFile();
                        if (file != null) {
                            files.add(file);
                        }
                    }
                } catch (Exception e) {
                    // logger.warn("Unexpected error occurs while it generates a stand-alone schema
                    // for 'asccp_manifest_id' [" + asccpManifestId + "]", e);
                    exceptions.add(e);
                }
            });

            if (!exceptions.isEmpty()) {
                throw new IllegalStateException(
                        exceptions.stream().map(e -> e.getMessage()).collect(Collectors.joining("\n")));
            }

            if (files.size() == 1) {
                File srcFile = files.get(0);
                File destFile = File.createTempFile("oagis-", null);
                if (!srcFile.renameTo(destFile)) {
                    FileUtils.copyFile(srcFile, destFile);
                }
                String filename = srcFile.getName();
                return new ExportStandaloneSchemaResponse(filename, destFile);
            } else {
                return new ExportStandaloneSchemaResponse(UUID.randomUUID() + ".zip",
                        Zip.compressionHierarchy(baseDir, files));
            }
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    public List<ExternalChildComponentRecord> getChildComponents(ReleaseId releaseId) {
        return externalComponentsRepository.getChildComponents(releaseId);
    }

}
