package org.oagi.score.gateway.http.api.bie_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.*;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BiePackageQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BieGenerateService bieGenerateService;
    @Autowired
    private BiePackageManifestService biePackageManifestService;

    private BiePackageQueryRepository query(ScoreUser requester) {
        return repositoryFactory.biePackageQueryRepository(requester);
    }

    public List<BiePackageSummaryRecord> getBiePackageSummaryList(
            ScoreUser requester, Collection<BiePackageId> biePackageIdList) {

        return query(requester).getBiePackageSummaryList(biePackageIdList);
    }

    public ResultAndCount<BiePackageListEntryRecord> getBiePackageList(
            ScoreUser requester, BiePackageListFilterCriteria filterCriteria, PageRequest pageRequest) {

        return query(requester).getBiePackageList(filterCriteria, pageRequest);
    }

    public BiePackageDetailsRecord getBiePackageDetails(ScoreUser requester, BiePackageId biePackageId) {

        return query(requester).getBiePackageDetails(biePackageId);
    }

    public BieGenerateExpressionResult generate(
            ScoreUser requester, BiePackageId biePackageId,
            Collection<TopLevelAsbiepId> topLevelAsbiepIdList, GenerateExpressionOption option,
            String pathDelimiter, String manifestVersion) throws IOException {

        var query = query(requester);
        BiePackageSummaryRecord biePackage = query.getBiePackageSummary(biePackageId);
        List<TopLevelAsbiepId> topLevelAsbiepIdListInBiePackage = query.getTopLevelAsbiepIdListInBiePackage(biePackageId);

        if (topLevelAsbiepIdList.isEmpty()) {
            topLevelAsbiepIdList = topLevelAsbiepIdListInBiePackage;
        } else {
            for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIdList) {
                if (!topLevelAsbiepIdListInBiePackage.contains(topLevelAsbiepId)) {
                    throw new IllegalArgumentException("Invalid request for generating a BIE that is not included in the BIE Package.");
                }
            }
        }

        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        List<TopLevelAsbiepSummaryRecord> topLevelAsbiepList =
                topLevelAsbiepIdList.stream().map(e -> topLevelAsbiepQuery.getTopLevelAsbiepSummary(e))
                        .collect(Collectors.toList());

        if (option == null) {
            option = new GenerateExpressionOption();
        }
        if (!hasLength(option.getExpressionOption())) {
            option.setExpressionOption("XML");
        }
        // BIE package export always generates each schema file with split reused-schema references.
        option.setPackageOption("EACH");
        option.setSeparateFileReferencesForReusedSchemas(true);
        // #1711: Package export filenames always use package pattern metadata.
        option.setIncludeBusinessContextInFilename(true);
        option.setIncludeVersionInFilename(true);
        option.setFilenames(Collections.emptyMap());
        option.setBiePackage(biePackage);

        Map<TopLevelAsbiepId, File> result =
                bieGenerateService.generateSchemaForEach(requester, topLevelAsbiepList, option);
        Map<TopLevelAsbiepId, String> generatedFilesByTopLevelAsbiepId = result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getName()));
        BiePackageManifestResponse biePackageManifestResponse =
                biePackageManifestService.getBiePackageManifest(
                        requester, biePackageId, pathDelimiter, generatedFilesByTopLevelAsbiepId, manifestVersion);

        return makeGenerateBiePackageResponse(biePackage, result, biePackageManifestResponse);
    }

    public File writeBiePackageManifestResponseToTempFile(BiePackageManifestResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create temp file
        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), "manifest.json");
        tempFile.deleteOnExit();

        // Write JSON to file using BufferedOutputStream for efficiency
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            objectMapper.writeValue(bos, response);
            bos.flush(); // ensure all data is written
        }

        return tempFile;
    }

    private BieGenerateExpressionResult makeGenerateBiePackageResponse(
            BiePackageSummaryRecord biePackage, Map<TopLevelAsbiepId, File> result, BiePackageManifestResponse biePackageManifestResponse) throws IOException {

        File manifestFile = writeBiePackageManifestResponseToTempFile(biePackageManifestResponse);
        List<File> files = new ArrayList<>(result.values());
        files.add(manifestFile);

        String filename = String.join("-", Arrays.asList(
                        biePackage.name(),
                        biePackage.versionName(),
                        biePackage.versionId(),
                        Long.toString(System.currentTimeMillis()))
                .stream()
                .map(e -> e.replaceAll("\\s+", ""))                      // remove whitespace
                .map(e -> e.replaceAll("[\\\\/:*?\"<>|]", ""))           // remove invalid filename char
                .collect(Collectors.toList()));

        File file = Zip.compression(files, filename);

        String contentType = "application/zip";
        return new BieGenerateExpressionResult(file.getName(), contentType, file);
    }

    public ResultAndCount<BieListEntryRecord> getBieListInBiePackage(
            ScoreUser requester, BieListInBiePackageFilterCriteria filterCriteria, PageRequest pageRequest) {

        var biePackageQuery = repositoryFactory.biePackageQueryRepository(requester);
        return biePackageQuery.getBieListInBiePackage(filterCriteria, pageRequest);
    }

    public boolean exists(ScoreUser requester, BiePackageId biePackageId, TopLevelAsbiepId topLevelAsbiepId) {
        var biePackageQuery = repositoryFactory.biePackageQueryRepository(requester);
        return biePackageQuery.exists(biePackageId, topLevelAsbiepId);
    }
}
