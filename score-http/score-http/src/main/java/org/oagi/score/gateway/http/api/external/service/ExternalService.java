package org.oagi.score.gateway.http.api.external.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.impl.StandaloneExportContextBuilder;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.export.model.SchemaModule;
import org.oagi.score.export.service.CoreComponentService;
import org.oagi.score.gateway.http.api.external.data.BieList;
import org.oagi.score.gateway.http.api.bie_management.data.BieListRequest;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieGenerateFailureException;
import org.oagi.score.gateway.http.api.module_management.data.ExportStandaloneSchemaResponse;
import org.oagi.score.gateway.http.api.release_management.provider.ReleaseDataProvider;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.repo.BusinessInformationEntityRepository;
import org.oagi.score.repo.PaginationResponse;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.repository.CoreComponentRepositoryForRelease;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import static org.jooq.impl.DSL.*;
import org.jooq.*;
import org.jooq.types.ULong;
import java.util.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;


@Service
@Transactional(readOnly = true)
public class ExternalService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private CoreComponentRepositoryForRelease coreComponentRepositoryForRelease;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private BusinessInformationEntityRepository bieRepository;
    
    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

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
            List<BigInteger> asccpManifestIdList) throws Exception {
        if (asccpManifestIdList == null || asccpManifestIdList.isEmpty()) {
            throw new IllegalArgumentException();
        }

        File baseDir = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(baseDir);

        try {
            List<File> files = new ArrayList<>();

            Map<BigInteger, BigInteger> releaseIdMap = releaseRepository.getReleaseIdMapByAsccpManifestIdList(asccpManifestIdList);
            Map<BigInteger, ReleaseDataProvider> dataProviderMap = releaseIdMap.values().stream().distinct()
                    .collect(Collectors.toMap(Function.identity(), e -> new ReleaseDataProvider(coreComponentRepositoryForRelease, e)));
            Map<String, Integer> pathCounter = new ConcurrentHashMap<>();
            List<Exception> exceptions = new ArrayList<>();
            asccpManifestIdList.parallelStream().forEach(asccpManifestId -> {
                try {
                    ReleaseDataProvider dataProvider = dataProviderMap.get(releaseIdMap.get(asccpManifestId));

                    XMLExportSchemaModuleVisitor visitor = new XMLExportSchemaModuleVisitor(coreComponentService, dataProvider);
                    visitor.setBaseDirectory(baseDir);

                    StandaloneExportContextBuilder builder =
                            new StandaloneExportContextBuilder(dataProvider, coreComponentService, pathCounter);
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

     public PageResponse<BieList> getBieList( BieListRequest request) {
        PageRequest pageRequest = request.getPageRequest();


        List<BieState> bieStates = request.getStates().isEmpty()
        ?Arrays.asList(BieState.values()).stream().filter(e -> e.getLevel()>=2).collect(Collectors.toList())
        :request.getStates().stream().filter(e -> e.getLevel()>=2).collect(Collectors.toList());

        PaginationResponse<BieList> result = bieRepository.selectBieLists()
                .setDen(request.getDen())
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setTopLevelAsbiepIds(request.getTopLevelAsbiepIds())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(bieStates)
                .setReleaseIds(request.getReleaseIds())
                .setOwnerLoginIds(request.getOwnerLoginIds())
                .setUpdaterLoginIds(request.getUpdaterLoginIds())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort(pageRequest.getSortActive(), pageRequest.getSortDirection())
                .setOffset(pageRequest.getOffset(), pageRequest.getPageSize())
                .fetchInto(BieList.class);


        List<BieList> bieLists = result.getResult();
        
        bieLists.forEach(bieList -> {
            ULong topLevelAsbpieId = ULong.valueOf(bieList.getTopLevelAsbiepId());
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
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }



    public BieGenerateExpressionResult generate(
            BigInteger topLevelAsbiepId,
            GenerateExpressionOption option) throws BieGenerateFailureException {

        TopLevelAsbiep topLevelAsbiep = topLevelAsbiepRepository.findById(topLevelAsbiepId);
        List<TopLevelAsbiep> topLevelAsbieps = new ArrayList<TopLevelAsbiep>();
        topLevelAsbieps.add(topLevelAsbiep);
        File file = bieGenerateService.generateSchema(topLevelAsbieps, option);
        return bieGenerateService.toResult(file);
    }


}
