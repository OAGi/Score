package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BiePackageQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BieGenerateService bieGenerateService;

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
            Collection<TopLevelAsbiepId> topLevelAsbiepIdList, String schemaExpression) throws IOException {

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

        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setExpressionOption(schemaExpression);
        option.setBiePackage(biePackage);

        Map<TopLevelAsbiepId, File> result = bieGenerateService.generateSchemaForEach(requester, topLevelAsbiepList, option);
        return makeGenerateBiePackageResponse(biePackage, result);
    }

    private BieGenerateExpressionResult makeGenerateBiePackageResponse(
            BiePackageSummaryRecord biePackage, Map<TopLevelAsbiepId, File> result) throws IOException {
        File file;
        if (result.size() == 1) {
            file = result.values().iterator().next();
        } else {
            String filename = biePackage.versionName() + "-" + biePackage.versionId() + "-" + System.currentTimeMillis();
            file = Zip.compression(result.values(), filename);
        }

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

}
