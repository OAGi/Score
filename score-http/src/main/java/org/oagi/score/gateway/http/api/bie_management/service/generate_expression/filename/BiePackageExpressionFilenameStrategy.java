package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameTokenUtils.normalizeTokenWithoutSpaces;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameTokenUtils.sanitizeFileName;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

/**
 * Filename strategy for BIE package exports.
 * <p>
 * Pattern:
 * {@code {PackageName-PackageVersionId}_{BizCtx1[_BizCtx2...]}_{PropertyTerm[DisplayName]-Version}}
 */
@Component
public class BiePackageExpressionFilenameStrategy implements BieSchemaFilenameStrategy {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private TopLevelAsbiepIdSuffixDuplicateHandler duplicateHandler;

    @Override
    public String buildBaseFilename(ScoreUser requester,
                                    TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                    GenerateExpressionOption option) {
        BiePackageSummaryRecord biePackage = option.getBiePackage();
        if (biePackage == null) {
            throw new IllegalArgumentException("BIE package metadata is required for package filename pattern.");
        }

        List<String> tokens = new ArrayList<>();
        String packageToken = normalizeTokenWithoutSpaces(biePackage.name())
                + "-" + normalizeTokenWithoutSpaces(biePackage.versionId());
        tokens.add(packageToken);

        List<String> businessContextTokens = resolveBusinessContextTokens(requester, topLevelAsbiep.topLevelAsbiepId(), option);
        if (!businessContextTokens.isEmpty()) {
            tokens.add(String.join("_", businessContextTokens));
        }

        String propertyTerm = normalizeTokenWithoutSpaces(topLevelAsbiep.propertyTerm());
        StringBuilder bieToken = new StringBuilder(propertyTerm);

        if (hasLength(topLevelAsbiep.displayName())) {
            bieToken.append('[')
                    .append(normalizeTokenWithoutSpaces(topLevelAsbiep.displayName()))
                    .append(']');
        }

        if (hasLength(topLevelAsbiep.version())) {
            bieToken.append('-')
                    .append(normalizeTokenWithoutSpaces(topLevelAsbiep.version()));
        }

        tokens.add(bieToken.toString());

        return sanitizeFileName(String.join("_", tokens));
    }

    @Override
    public DuplicateHandler duplicateHandler() {
        return duplicateHandler;
    }

    private List<String> resolveBusinessContextTokens(ScoreUser requester,
                                                      TopLevelAsbiepId topLevelAsbiepId,
                                                      GenerateExpressionOption option) {
        BusinessContextQueryRepository businessContextQuery =
                repositoryFactory.businessContextQueryRepository(requester);
        Set<String> businessContextTokens = new LinkedHashSet<>();

        addAssignedBusinessContextTokens(topLevelAsbiepId, businessContextQuery, businessContextTokens);

        if (!businessContextTokens.isEmpty()) {
            return new ArrayList<>(businessContextTokens);
        }

        // Fallback to explicitly selected context when there is no assignment.
        addSelectedBusinessContextToken(topLevelAsbiepId, option, businessContextQuery, businessContextTokens);

        return new ArrayList<>(businessContextTokens);
    }

    private void addAssignedBusinessContextTokens(
            TopLevelAsbiepId topLevelAsbiepId,
            BusinessContextQueryRepository businessContextQuery,
            Set<String> businessContextTokens) {
        List<BusinessContextSummaryRecord> assignedBusinessContextSummaries =
                businessContextQuery.getBusinessContextSummaryList(topLevelAsbiepId);
        if (assignedBusinessContextSummaries == null || assignedBusinessContextSummaries.isEmpty()) {
            return;
        }
        for (BusinessContextSummaryRecord assignedBusinessContextSummary : assignedBusinessContextSummaries) {
            String businessContextName =
                    (assignedBusinessContextSummary != null) ? assignedBusinessContextSummary.name() : null;
            if (hasLength(businessContextName)) {
                businessContextTokens.add(normalizeTokenWithoutSpaces(businessContextName));
            }
        }
    }

    private void addSelectedBusinessContextToken(
            TopLevelAsbiepId topLevelAsbiepId,
            GenerateExpressionOption option,
            BusinessContextQueryRepository businessContextQuery,
            Set<String> businessContextTokens) {
        Map<TopLevelAsbiepId, BusinessContextId> businessContextByTopLevelAsbiepId = option.getBizCtxIds();
        if (businessContextByTopLevelAsbiepId == null) {
            return;
        }
        BusinessContextId selectedBusinessContextId = businessContextByTopLevelAsbiepId.get(topLevelAsbiepId);
        if (selectedBusinessContextId == null) {
            return;
        }
        BusinessContextDetailsRecord details = businessContextQuery.getBusinessContextDetails(selectedBusinessContextId);
        String selectedBusinessContextName = (details != null) ? details.name() : null;
        if (hasLength(selectedBusinessContextName)) {
            businessContextTokens.add(normalizeTokenWithoutSpaces(selectedBusinessContextName));
        }
    }

}
