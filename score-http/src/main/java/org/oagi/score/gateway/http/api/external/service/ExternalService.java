package org.oagi.score.gateway.http.api.external.service;

import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieListRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.BusinessInformationEntityRepository;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieGenerateFailureException;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.impl.StandaloneExportContextBuilder;
import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.gateway.http.api.export.model.SchemaModule;
import org.oagi.score.gateway.http.api.external.data.BieList;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ExportStandaloneSchemaResponse;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.max;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;


@Service
@Transactional(readOnly = true)
public class ExternalService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private BieGenerateService bieGenerateService;


    public String getReleases() {

        return dslContext.select(
                RELEASE.RELEASE_ID,
                RELEASE.GUID,
                RELEASE.RELEASE_NUM,
                RELEASE.RELEASE_NOTE,
                RELEASE.RELEASE_LICENSE,
                RELEASE.STATE)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.notEqual("WORKING"))
                .orderBy(RELEASE.RELEASE_NUM.desc())
                .fetch().formatJSON();
    }

    public String getLatestRelease() {

        return dslContext.select(
                max(RELEASE.RELEASE_ID).as("latest_release"))
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.notEqual("WORKING"))
                .fetch().formatJSON();
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
                    //logger.warn("Unexpected error occurs while it generates a stand-alone schema for 'asccp_manifest_id' [" + asccpManifestId + "]", e);
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

    public PageResponse<BieList> getBieList(BieListRequest request) {
        PageRequest pageRequest = request.getPageRequest();

        List<BieState> bieStates = request.getStates().isEmpty()
        ?Arrays.asList(BieState.values()).stream().filter(e -> e.getLevel()>=2).collect(Collectors.toList())
        :request.getStates().stream().filter(e -> e.getLevel()>=2).collect(Collectors.toList());

        PageResponse<BieList> result = bieRepository.selectBieLists()
                .setDen(request.getDen())
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setTopLevelAsbiepIds(request.getTopLevelAsbiepIds())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(bieStates)
                .setReleaseIds(request.getReleaseIds())
                .setOwnerLoginIdList(request.getOwnerLoginIdList())
                .setUpdaterLoginIdList(request.getUpdaterLoginIdList())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort(pageRequest.sorts())
                .setOffset(pageRequest.pageOffset(), pageRequest.pageSize())
                .fetch();


        List<BieList> bieLists = result.getList();
        
        bieLists.forEach(bieList -> {
            ULong topLevelAsbpieId = ULong.valueOf(bieList.getTopLevelAsbiepId().value());
            List<String> businessContextNames = dslContext
                .select(BIZ_CTX.NAME)
                .from(BIZ_CTX)
                .join(BIZ_CTX_ASSIGNMENT)
                    .on(BIZ_CTX.BIZ_CTX_ID.eq(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID))
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbpieId))
                .fetch(BIZ_CTX.NAME)
            ;

            bieList.setBusinessContextNames(businessContextNames);           
        });
        

        PageResponse<BieList> response = new PageResponse<BieList>();
        response.setList(bieLists);
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(result.getLength());
        return response;
    }



    public BieGenerateExpressionResult generate(
            ScoreUser requester,
            TopLevelAsbiepId topLevelAsbiepId,
            GenerateExpressionOption option) throws BieGenerateFailureException {

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);
        List<TopLevelAsbiepSummaryRecord> topLevelAsbieps = new ArrayList<>();
        topLevelAsbieps.add(topLevelAsbiep);
        File file = bieGenerateService.generateSchema(requester, topLevelAsbieps, option);
        return bieGenerateService.toResult(file);
    }


}
