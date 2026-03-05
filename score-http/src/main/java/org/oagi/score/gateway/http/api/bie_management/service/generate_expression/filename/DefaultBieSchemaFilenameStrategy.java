package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameTokenUtils.*;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

/**
 * Default filename strategy for non-package exports.
 * <p>
 * Pattern:
 * {@code {Property-Term[-BusinessContext][-Version]}}
 */
@Component
public class DefaultBieSchemaFilenameStrategy implements BieSchemaFilenameStrategy {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private IncrementalSuffixDuplicateHandler duplicateHandler;

    @Override
    public String buildBaseFilename(ScoreUser requester,
                                    TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                    GenerateExpressionOption option) {
        String propertyTerm = normalizeTokenWithHyphen(topLevelAsbiep.propertyTerm());
        StringBuilder sb = new StringBuilder(propertyTerm);

        if (option.isIncludeBusinessContextInFilename()) {
            String businessContextName = resolveBusinessContextName(requester, topLevelAsbiep.topLevelAsbiepId(), option);
            if (hasLength(businessContextName)) {
                sb.append('-').append(normalizeTokenWithoutSpaces(businessContextName));
            }
        }

        if (option.isIncludeVersionInFilename()) {
            String version = topLevelAsbiep.version();
            if (hasLength(version)) {
                sb.append('-').append(version.trim().replaceAll("\\.", "_"));
            }
        }

        return sanitizeFileName(sb.toString());
    }

    @Override
    public DuplicateHandler duplicateHandler() {
        return duplicateHandler;
    }

    private String resolveBusinessContextName(ScoreUser requester,
                                              TopLevelAsbiepId topLevelAsbiepId,
                                              GenerateExpressionOption option) {
        BusinessContextQueryRepository businessContextQuery =
                repositoryFactory.businessContextQueryRepository(requester);

        String selectedBusinessContextName =
                resolveSelectedBusinessContextName(topLevelAsbiepId, option, businessContextQuery);
        if (hasLength(selectedBusinessContextName)) {
            return selectedBusinessContextName;
        }

        // Fallback to the first assigned business context to preserve legacy default behavior.
        List<BusinessContextId> assignedBusinessContextIds =
                repositoryFactory.topLevelAsbiepQueryRepository(requester)
                        .getAssignedBusinessContextList(topLevelAsbiepId);
        if (assignedBusinessContextIds == null || assignedBusinessContextIds.isEmpty()) {
            return null;
        }

        BusinessContextDetailsRecord details =
                businessContextQuery.getBusinessContextDetails(assignedBusinessContextIds.get(0));
        return (details != null) ? details.name() : null;
    }

    private String resolveSelectedBusinessContextName(
            TopLevelAsbiepId topLevelAsbiepId,
            GenerateExpressionOption option,
            BusinessContextQueryRepository businessContextQuery) {
        Map<TopLevelAsbiepId, BusinessContextId> businessContextByTopLevelAsbiepId = option.getBizCtxIds();
        if (businessContextByTopLevelAsbiepId == null) {
            return null;
        }

        BusinessContextId selectedBusinessContextId =
                businessContextByTopLevelAsbiepId.get(topLevelAsbiepId);
        if (selectedBusinessContextId == null) {
            return null;
        }

        BusinessContextDetailsRecord details =
                businessContextQuery.getBusinessContextDetails(selectedBusinessContextId);
        return (details != null) ? details.name() : null;
    }

}
