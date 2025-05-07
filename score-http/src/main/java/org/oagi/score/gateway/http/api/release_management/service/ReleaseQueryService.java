package org.oagi.score.gateway.http.api.release_management.service;

import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.impl.StandaloneExportContextBuilder;
import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.model.SchemaModule;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.release_management.controller.payload.GenerateMigrationScriptResponse;
import org.oagi.score.gateway.http.api.release_management.model.*;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.criteria.ReleaseListFilterCriteria;
import org.oagi.score.gateway.http.common.model.ExportStandaloneSchemaResponse;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.Zip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;

/**
 * Service class for querying release-related data.
 */
@Service
@Transactional(readOnly = true)
public class ReleaseQueryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ReleaseQueryRepository query(ScoreUser requester) {
        return repositoryFactory.releaseQueryRepository(requester);
    }

    private LibraryQueryRepository libraryQuery(ScoreUser requester) {
        return repositoryFactory.libraryQueryRepository(requester);
    }

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ResourceLoader resourceLoader;

    public List<ReleaseSummaryRecord> getReleaseSummaryList(
            ScoreUser requester, LibraryId libraryId, Collection<ReleaseState> releaseStateSet) {
        boolean isReadOnly = libraryQuery(requester).isReadOnly(libraryId);
        List<ReleaseSummaryRecord> releases = query(requester).getReleaseSummaryList(libraryId, releaseStateSet);

        ReleaseSummaryRecord workingRelease = releases.stream()
                .filter(ReleaseSummaryRecord::isWorkingRelease)
                .findAny()
                .orElse(null);
        releases.remove(workingRelease);

        if (!isReadOnly) {
            if (requester.hasRole(DEVELOPER)) {
                releases.add(0, workingRelease);
            } else {
                releases.add(workingRelease);
            }
        }

        return releases;
    }

    /**
     * Retrieves a paginated list of releases based on filter criteria.
     *
     * @param requester      The user making the request.
     * @param filterCriteria The criteria to filter the releases.
     * @param pageRequest    The pagination information.
     * @return A {@link ResultAndCount} object containing the list of releases and the total count.
     */
    public ResultAndCount<ReleaseListEntryRecord> getReleaseList(ScoreUser requester,
                                                                 ReleaseListFilterCriteria filterCriteria,
                                                                 PageRequest pageRequest) {
        return query(requester).getReleaseList(filterCriteria, pageRequest);
    }

    /**
     * Retrieves the details of a specific release by its ID.
     *
     * @param requester The user making the request.
     * @param releaseId The ID of the release to fetch details for.
     * @return The {@link ReleaseDetailsRecord} containing the release details.
     */
    public ReleaseDetailsRecord getReleaseDetails(ScoreUser requester, ReleaseId releaseId) {
        return query(requester).getReleaseDetails(releaseId);
    }

    public AssignComponents getAssignComponents(ScoreUser requester, ReleaseId releaseId) {
        return query(requester).getAssignComponents(releaseId);
    }

    public GenerateMigrationScriptResponse generateMigrationScript(ScoreUser requester, ReleaseId releaseId) throws IOException {
        ReleaseSummaryRecord release = query(requester).getReleaseSummary(releaseId);

        MigrationScriptGenerator generator = new MigrationScriptGenerator(dslContext, resourceLoader,
                BigInteger.valueOf(100000000L));
        File file = generator.generate(requester, release);

        String fileName = release.releaseNum().replace(".", "_") + ".zip";
        return new GenerateMigrationScriptResponse(fileName, file);
    }

    public ExportStandaloneSchemaResponse exportStandaloneSchema(
            ScoreUser requester, Collection<AsccpManifestId> asccpManifestIdList) throws Exception {
        if (asccpManifestIdList == null || asccpManifestIdList.isEmpty()) {
            throw new IllegalArgumentException();
        }

        File baseDir = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(baseDir);

        try {
            List<File> files = new ArrayList<>();

            Map<AsccpManifestId, ReleaseId> releaseIdMap = getReleaseIdMapByAsccpManifestIdList(requester, asccpManifestIdList);
            CcDocument ccDocument = new CcDocumentImpl(requester, repositoryFactory, releaseIdMap.values());
            Map<String, Integer> pathCounter = new ConcurrentHashMap<>();
            List<Exception> exceptions = new ArrayList<>();
            asccpManifestIdList.parallelStream().forEach(asccpManifestId -> {
                try {
                    XMLExportSchemaModuleVisitor visitor = new XMLExportSchemaModuleVisitor(ccDocument);
                    visitor.setBaseDirectory(baseDir);

                    StandaloneExportContextBuilder builder =
                            new StandaloneExportContextBuilder(ccDocument, pathCounter);
                    ExportContext exportContext = builder.build(asccpManifestId);

                    for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
                        schemaModule.visit(visitor);
                        File file = schemaModule.getModuleFile();
                        if (file != null) {
                            files.add(file);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unexpected error occurs while it generates a stand-alone schema for 'asccp_manifest_id' [" + asccpManifestId + "]", e);
                    exceptions.add(e);
                }
            });

            if (!exceptions.isEmpty()) {
                throw new IllegalStateException(exceptions.stream().map(e -> e.getMessage()).collect(Collectors.joining("\n")));
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

    public Map<AsccpManifestId, ReleaseId> getReleaseIdMapByAsccpManifestIdList(
            ScoreUser requester, Collection<AsccpManifestId> asccpManifestIdList) {
        return query(requester).getReleaseIdMapByAsccpManifestIdList(asccpManifestIdList);
    }

    public String generatePlantUmlText(
            ScoreUser requester, ReleaseId releaseId, String releaseLinkTemplate, String libraryLinkTemplate) {

        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("!pragma layout smetana\n");
        sb.append("skinparam svgLinkTarget _blank\n");
        sb.append("set namespaceSeparator none\n");
        String styleName = "link_style";
        sb.append("<style>\n")
                .append("\t").append("classDiagram {\n")
                .append("\t\t").append("class {\n")
                .append("\t\t\t").append("header {\n")
                .append("\t\t\t\t").append(".").append(styleName).append(" {\n")
                .append("\t\t\t\t\t").append("FontColor blue\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t}\n")
                .append("\t\t}\n")
                .append("\t}\n")
                .append("</style>\n");
        sb.append("\n");

        var query = query(requester);
        ReleaseSummaryRecord release = query.getReleaseSummary(releaseId);
        List<ReleaseSummaryRecord> dependentList = query.getDependentReleaseSummaryList(releaseId);

        sb.append(
                toClassDiagram(requester, release, styleName, releaseLinkTemplate, libraryLinkTemplate)
        ).append("\n");

        for (ReleaseSummaryRecord dependent : dependentList) {
            sb.append(
                    toClassDiagram(requester, dependent, styleName, releaseLinkTemplate, libraryLinkTemplate)
            ).append("\n");

            sb.append("\"Release " + release.releaseNum() + "\" o-- \"Release " + dependent.releaseNum() + "\"\n");
        }

        sb.append("\n");
        sb.append("hide circle\n");
        sb.append("hide empty members\n");
        sb.append("hide <<").append(styleName).append(">> stereotype\n");
        sb.append("@enduml");

        return sb.toString();
    }

    private String toClassDiagram(ScoreUser requester,
                                  ReleaseSummaryRecord release,
                                  String styleName,
                                  String releaseLinkTemplate,
                                  String libraryLinkTemplate) {

        var libraryQuery = repositoryFactory.libraryQueryRepository(requester);
        LibraryDetailsRecord library = libraryQuery.getLibraryDetails(release.libraryId());

        StringBuilder sb = new StringBuilder();

        sb.append("class \"Release " + release.releaseNum() + "\" ")
                .append("<<").append(styleName).append(">> [[")
                .append(releaseLinkTemplate.replaceAll("\\{releaseId\\}", release.releaseId().toString()))
                .append("]] {\n");

        sb.append("\t +library: \"" + library.name() + "\" [[[")
                .append(libraryLinkTemplate.replaceAll("\\{libraryId\\}", library.libraryId().toString()))
                .append("]]]\n");

        sb.append("}").append("\n");

        return sb.toString();
    }
}
