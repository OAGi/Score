package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameTokenUtils.sanitizeFileName;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.StringUtils.trim;

/**
 * Filename strategy that renders a filename from a placeholder expression.
 */
public class ExpressionBasedFilenameStrategy implements BieSchemaFilenameStrategy {

    public static final String DEFAULT_BIE_SCHEMA_FILENAME_EXPRESSION =
            "{BIE Property Term:separator('-')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace('\\\\.', '_')})";
    public static final String DEFAULT_BIE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION = "-{Incremental}";
    public static final String DEFAULT_BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION =
            "{BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace('\\\\s+', ''):separator('+')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})";
    public static final String DEFAULT_BIE_PACKAGE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION = "~{BIE ID}";

    protected static final String PLACEHOLDER_BIE_PACKAGE_NAME = "BIE Package Name";
    protected static final String PLACEHOLDER_BIE_PACKAGE_VERSION_ID = "BIE Package Version ID";
    protected static final String PLACEHOLDER_BIE_PACKAGE_VERSION_NAME = "BIE Package Version Name";
    protected static final String PLACEHOLDER_BUSINESS_CONTEXT_NAMES = "Business Context Names";
    protected static final String PLACEHOLDER_BUSINESS_CONTEXT_NAME_INDEXED_PATTERN = "Business Context Name[%d]";
    protected static final String PLACEHOLDER_BIE_ID = "BIE ID";
    protected static final String PLACEHOLDER_INCREMENTAL = "Incremental";
    protected static final String PLACEHOLDER_BIE_PROPERTY_TERM = "BIE Property Term";
    protected static final String PLACEHOLDER_BIE_DISPLAY_NAME = "BIE Display Name";
    protected static final String PLACEHOLDER_BIE_VERSION = "BIE Version";
    protected static final String PLACEHOLDER_BIE_DEN = "BIE DEN";
    protected static final String PLACEHOLDER_BIE_STATUS = "BIE Status";
    protected static final String PLACEHOLDER_BIE_REMARK = "BIE Remark";

    protected static final String FLAG_INCLUDE_BUSINESS_CONTEXT = "includeBusinessContext";
    protected static final String FLAG_INCLUDE_VERSION = "includeVersion";

    private static final Pattern BUSINESS_CONTEXT_NAME_INDEXED_NAME_PATTERN =
            compileIndexedBusinessContextNamePattern();

    private final RepositoryFactory repositoryFactory;
    private final BiePackageSummaryRecord biePackage;
    private final Map<TopLevelAsbiepId, BusinessContextId> businessContextByTopLevelAsbiepId;
    private final boolean includeBusinessContextInFilename;
    private final boolean includeVersionInFilename;

    private final String filenameExpression;
    private final String duplicateHandlerExpression;
    private final String defaultFilenameExpression;
    private final String defaultDuplicateHandlerExpression;
    private final FilenameExpressionParser.ParsedExpression parsedDefaultFilenameExpression;
    private final FilenameExpressionParser.ParsedExpression parsedDefaultDuplicateHandlerExpression;
    private final ConcurrentMap<String, FilenameExpressionParser.ParsedExpression> parsedFilenameExpressionCache =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FilenameExpressionParser.ParsedExpression> parsedDuplicateHandlerExpressionCache =
            new ConcurrentHashMap<>();

    public static ExpressionBasedFilenameStrategy from(RepositoryFactory repositoryFactory,
                                                       GenerateExpressionOption option) {
        boolean packageExpression = option.getBiePackage() != null;

        String defaultFilenameExpression = packageExpression
                ? DEFAULT_BIE_PACKAGE_SCHEMA_FILENAME_EXPRESSION
                : DEFAULT_BIE_SCHEMA_FILENAME_EXPRESSION;
        String defaultDuplicateHandlerExpression = packageExpression
                ? DEFAULT_BIE_PACKAGE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION
                : DEFAULT_BIE_SCHEMA_DUPLICATE_HANDLER_EXPRESSION;

        String filenameExpression = packageExpression
                ? option.getBiePackageSchemaFilenameExpression()
                : option.getBieSchemaFilenameExpression();
        String duplicateHandlerExpression = packageExpression
                ? option.getBiePackageSchemaFilenameDuplicateHandlerExpression()
                : option.getBieSchemaFilenameDuplicateHandlerExpression();

        return new ExpressionBasedFilenameStrategy(
                repositoryFactory,
                filenameExpression,
                duplicateHandlerExpression,
                defaultFilenameExpression,
                defaultDuplicateHandlerExpression,
                option.getBiePackage(),
                option.getBizCtxIds(),
                option.isIncludeBusinessContextInFilename(),
                option.isIncludeVersionInFilename());
    }

    private ExpressionBasedFilenameStrategy(RepositoryFactory repositoryFactory,
                                            String filenameExpression,
                                            String duplicateHandlerExpression,
                                            String defaultFilenameExpression,
                                            String defaultDuplicateHandlerExpression,
                                            BiePackageSummaryRecord biePackage,
                                            Map<TopLevelAsbiepId, BusinessContextId> businessContextByTopLevelAsbiepId,
                                            boolean includeBusinessContextInFilename,
                                            boolean includeVersionInFilename) {
        this.repositoryFactory = repositoryFactory;
        this.biePackage = biePackage;
        this.businessContextByTopLevelAsbiepId =
                (businessContextByTopLevelAsbiepId != null) ? businessContextByTopLevelAsbiepId : Collections.emptyMap();
        this.includeBusinessContextInFilename = includeBusinessContextInFilename;
        this.includeVersionInFilename = includeVersionInFilename;

        this.defaultFilenameExpression = defaultFilenameExpression;
        this.defaultDuplicateHandlerExpression = defaultDuplicateHandlerExpression;
        this.filenameExpression = hasLength(trim(filenameExpression)) ? trim(filenameExpression) : defaultFilenameExpression;
        this.duplicateHandlerExpression = hasLength(trim(duplicateHandlerExpression))
                ? trim(duplicateHandlerExpression)
                : defaultDuplicateHandlerExpression;

        this.parsedDefaultFilenameExpression = FilenameExpressionParser.parse(defaultFilenameExpression);
        this.parsedDefaultDuplicateHandlerExpression =
                FilenameExpressionParser.parse(defaultDuplicateHandlerExpression);
        this.parsedFilenameExpressionCache.put(defaultFilenameExpression, this.parsedDefaultFilenameExpression);
        this.parsedDuplicateHandlerExpressionCache.put(
                defaultDuplicateHandlerExpression, this.parsedDefaultDuplicateHandlerExpression);
    }

    @Override
    public String buildBaseFilename(ScoreUser requester,
                                    TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        FilenameExpressionParser.ParsedExpression parsedExpression =
                parseFilenameExpressionOrDefault(filenameExpression);
        try {
            return evaluateFilenameExpression(requester, topLevelAsbiep,
                    filenameExpression, parsedExpression);
        } catch (RuntimeException e) {
            if (!defaultFilenameExpression.equals(filenameExpression)) {
                return evaluateFilenameExpression(requester, topLevelAsbiep,
                        defaultFilenameExpression, parsedDefaultFilenameExpression);
            }
            throw e;
        }
    }

    private FilenameExpressionParser.ParsedExpression parseFilenameExpressionOrDefault(String expression) {
        if (!hasLength(expression) || defaultFilenameExpression.equals(expression)) {
            return parsedDefaultFilenameExpression;
        }
        try {
            return parsedFilenameExpressionCache.computeIfAbsent(expression, FilenameExpressionParser::parse);
        } catch (RuntimeException ignore) {
            return parsedDefaultFilenameExpression;
        }
    }

    private FilenameExpressionParser.ParsedExpression parseDuplicateHandlerExpressionOrDefault(String expression) {
        if (!hasLength(expression) || defaultDuplicateHandlerExpression.equals(expression)) {
            return parsedDefaultDuplicateHandlerExpression;
        }
        try {
            return parsedDuplicateHandlerExpressionCache.computeIfAbsent(expression, FilenameExpressionParser::parse);
        } catch (RuntimeException ignore) {
            return parsedDefaultDuplicateHandlerExpression;
        }
    }

    private String evaluateFilenameExpression(ScoreUser requester,
                                              TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                              String filenameExpression,
                                              FilenameExpressionParser.ParsedExpression parsedExpression) {
        List<String> businessContextNames =
                resolveBusinessContextNames(requester, topLevelAsbiep.topLevelAsbiepId());
        ensurePackageMetadataAvailableIfUsed(biePackage, filenameExpression);
        Map<String, FilenameExpressionEvaluator.PlaceholderValues> placeholders =
                placeholders(requester, topLevelAsbiep, businessContextNames, filenameExpression);
        Map<String, Boolean> flags = flags();

        return sanitizeFileName(FilenameExpressionEvaluator.evaluate(
                parsedExpression,
                (placeholderName, flagName) -> {
                    FilenameExpressionEvaluator.PlaceholderValues placeholderValue =
                            resolvePlaceholderValue(placeholderName, placeholders, businessContextNames);
                    if (hasLength(flagName) && !isFlagEnabled(flagName, flags)) {
                        return FilenameExpressionEvaluator.PlaceholderValues.single("");
                    }
                    return placeholderValue;
                }));
    }

    protected Map<String, FilenameExpressionEvaluator.PlaceholderValues> placeholders(
            ScoreUser requester,
            TopLevelAsbiepSummaryRecord topLevelAsbiep,
            List<String> businessContextNames,
            String resolvedFilenameExpression) {
        Map<String, FilenameExpressionEvaluator.PlaceholderValues> placeholders = new LinkedHashMap<>();
        if (biePackage != null) {
            placeholders.put(PLACEHOLDER_BIE_PACKAGE_NAME,
                    FilenameExpressionEvaluator.PlaceholderValues.single(biePackage.name()));
            placeholders.put(PLACEHOLDER_BIE_PACKAGE_VERSION_ID,
                    FilenameExpressionEvaluator.PlaceholderValues.single(biePackage.versionId()));
            placeholders.put(PLACEHOLDER_BIE_PACKAGE_VERSION_NAME,
                    FilenameExpressionEvaluator.PlaceholderValues.single(biePackage.versionName()));
        }
        placeholders.put(PLACEHOLDER_BUSINESS_CONTEXT_NAMES,
                FilenameExpressionEvaluator.PlaceholderValues.multiple(businessContextNames));
        placeholders.put(PLACEHOLDER_BIE_ID, FilenameExpressionEvaluator.PlaceholderValues.single(
                (topLevelAsbiep.topLevelAsbiepId() != null) ? topLevelAsbiep.topLevelAsbiepId().value().toString() : ""));
        placeholders.put(PLACEHOLDER_BIE_PROPERTY_TERM,
                FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiep.propertyTerm()));
        placeholders.put(PLACEHOLDER_BIE_DISPLAY_NAME,
                FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiep.displayName()));
        placeholders.put(PLACEHOLDER_BIE_VERSION,
                FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiep.version()));
        placeholders.put(PLACEHOLDER_BIE_DEN,
                FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiep.den()));
        placeholders.put(PLACEHOLDER_BIE_STATUS,
                FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiep.status()));
        placeholders.put(PLACEHOLDER_BIE_REMARK,
                FilenameExpressionEvaluator.PlaceholderValues.single(
                        resolveBieRemark(requester, topLevelAsbiep, resolvedFilenameExpression)));
        return placeholders;
    }

    protected Map<String, Boolean> flags() {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put(FLAG_INCLUDE_BUSINESS_CONTEXT, includeBusinessContextInFilename);
        flags.put(FLAG_INCLUDE_VERSION, includeVersionInFilename);
        return flags;
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolvePlaceholderValue(
            String placeholderName,
            Map<String, FilenameExpressionEvaluator.PlaceholderValues> placeholders,
            List<String> businessContextNames) {
        FilenameExpressionEvaluator.PlaceholderValues placeholderValue = placeholders.get(placeholderName);
        if (placeholderValue != null) {
            return placeholderValue;
        }

        FilenameExpressionEvaluator.PlaceholderValues indexedBusinessContext =
                resolveIndexedBusinessContextPlaceholder(placeholderName, businessContextNames);
        if (indexedBusinessContext != null) {
            return indexedBusinessContext;
        }

        throw new IllegalArgumentException("Unknown filename placeholder: {" + placeholderName + "}");
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolveIndexedBusinessContextPlaceholder(
            String placeholderName,
            List<String> businessContextNames) {
        Matcher matcher = BUSINESS_CONTEXT_NAME_INDEXED_NAME_PATTERN.matcher(placeholderName);
        if (!matcher.matches()) {
            return null;
        }
        int index = Integer.parseInt(matcher.group(1));
        String value = (index < businessContextNames.size()) ? businessContextNames.get(index) : "";
        return FilenameExpressionEvaluator.PlaceholderValues.single(value);
    }

    private boolean isFlagEnabled(String flagName, Map<String, Boolean> flags) {
        if (!flags.containsKey(flagName)) {
            throw new IllegalArgumentException("Unknown filename flag: ?" + flagName);
        }
        return Boolean.TRUE.equals(flags.get(flagName));
    }

    private void ensurePackageMetadataAvailableIfUsed(BiePackageSummaryRecord biePackage, String expression) {
        if (biePackage != null) {
            return;
        }
        if (usesPlaceholder(expression, PLACEHOLDER_BIE_PACKAGE_NAME)
                || usesPlaceholder(expression, PLACEHOLDER_BIE_PACKAGE_VERSION_ID)
                || usesPlaceholder(expression, PLACEHOLDER_BIE_PACKAGE_VERSION_NAME)) {
            throw new IllegalArgumentException("BIE package metadata is required for package placeholders.");
        }
    }

    private String resolveBieRemark(ScoreUser requester,
                                    TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                    String expression) {
        if (!usesPlaceholder(expression, PLACEHOLDER_BIE_REMARK)
                || topLevelAsbiep == null
                || topLevelAsbiep.asbiepId() == null) {
            return "";
        }
        AsbiepSummaryRecord asbiep = repositoryFactory.asbiepQueryRepository(requester)
                .getAsbiepSummary(topLevelAsbiep.asbiepId());
        if (asbiep == null) {
            return "";
        }
        return asbiep.remark();
    }

    private boolean usesPlaceholder(String expression, String placeholderName) {
        if (!hasLength(expression)) {
            return false;
        }
        return Pattern.compile("\\{" + Pattern.quote(placeholderName) + "(?:\\?[^}:]+)?(?:\\:[^}]*)?}")
                .matcher(expression)
                .find();
    }

    private List<String> resolveBusinessContextNames(ScoreUser requester,
                                                     TopLevelAsbiepId topLevelAsbiepId) {
        BusinessContextQueryRepository businessContextQuery =
                repositoryFactory.businessContextQueryRepository(requester);
        Set<String> businessContextNames = new LinkedHashSet<>();

        addSelectedBusinessContextName(topLevelAsbiepId, businessContextQuery, businessContextNames);
        if (businessContextNames.isEmpty()) {
            addAssignedBusinessContextNames(topLevelAsbiepId, businessContextQuery, businessContextNames);
        }

        return new ArrayList<>(businessContextNames);
    }

    private void addAssignedBusinessContextNames(
            TopLevelAsbiepId topLevelAsbiepId,
            BusinessContextQueryRepository businessContextQuery,
            Set<String> businessContextNames) {
        List<BusinessContextSummaryRecord> assignedBusinessContextSummaries =
                businessContextQuery.getBusinessContextSummaryList(topLevelAsbiepId);
        if (assignedBusinessContextSummaries == null || assignedBusinessContextSummaries.isEmpty()) {
            return;
        }
        for (BusinessContextSummaryRecord assignedBusinessContextSummary : assignedBusinessContextSummaries) {
            String businessContextName =
                    (assignedBusinessContextSummary != null) ? assignedBusinessContextSummary.name() : null;
            if (hasLength(businessContextName)) {
                businessContextNames.add(businessContextName);
            }
        }
    }

    private void addSelectedBusinessContextName(
            TopLevelAsbiepId topLevelAsbiepId,
            BusinessContextQueryRepository businessContextQuery,
            Set<String> businessContextNames) {
        BusinessContextId selectedBusinessContextId = businessContextByTopLevelAsbiepId.get(topLevelAsbiepId);
        if (selectedBusinessContextId == null) {
            return;
        }
        BusinessContextDetailsRecord details = businessContextQuery.getBusinessContextDetails(selectedBusinessContextId);
        String selectedBusinessContextName = (details != null) ? details.name() : null;
        if (hasLength(selectedBusinessContextName)) {
            businessContextNames.add(selectedBusinessContextName);
        }
    }

    @Override
    public String resolveDuplicateFilename(String baseFilename,
                                           TopLevelAsbiepId topLevelAsbiepId,
                                           int occurrence,
                                           int totalOccurrences) {
        FilenameExpressionParser.ParsedExpression parsedExpression =
                parseDuplicateHandlerExpressionOrDefault(duplicateHandlerExpression);
        try {
            return resolveDuplicateFilename(baseFilename, topLevelAsbiepId, occurrence, totalOccurrences,
                    duplicateHandlerExpression, parsedExpression);
        } catch (RuntimeException e) {
            if (!defaultDuplicateHandlerExpression.equals(duplicateHandlerExpression)) {
                return resolveDuplicateFilename(baseFilename, topLevelAsbiepId, occurrence, totalOccurrences,
                        defaultDuplicateHandlerExpression, parsedDefaultDuplicateHandlerExpression);
            }
            throw e;
        }
    }

    private String resolveDuplicateFilename(String baseFilename,
                                            TopLevelAsbiepId topLevelAsbiepId,
                                            int occurrence,
                                            int totalOccurrences,
                                            String duplicateHandlerExpression,
                                            FilenameExpressionParser.ParsedExpression parsedExpression) {
        if (totalOccurrences <= 1 || !hasLength(duplicateHandlerExpression)) {
            return baseFilename;
        }

        boolean usesIncremental = usesPlaceholder(duplicateHandlerExpression, PLACEHOLDER_INCREMENTAL);
        if (usesIncremental && occurrence == 0) {
            return baseFilename;
        }

        String topLevelAsbiepIdValue =
                (topLevelAsbiepId != null) ? topLevelAsbiepId.value().toString() : "";
        Map<String, FilenameExpressionEvaluator.PlaceholderValues> placeholders = new LinkedHashMap<>();
        placeholders.put(PLACEHOLDER_BIE_ID, FilenameExpressionEvaluator.PlaceholderValues.single(topLevelAsbiepIdValue));
        placeholders.put(PLACEHOLDER_INCREMENTAL,
                FilenameExpressionEvaluator.PlaceholderValues.single(String.valueOf(occurrence)));

        String suffix = FilenameExpressionEvaluator.evaluate(
                parsedExpression,
                (placeholderName, flagName) -> {
                    if (hasLength(flagName)) {
                        throw new IllegalArgumentException(
                                "Duplicate handler expression does not support flags: ?" + flagName);
                    }
                    FilenameExpressionEvaluator.PlaceholderValues placeholderValue = placeholders.get(placeholderName);
                    if (placeholderValue == null) {
                        throw new IllegalArgumentException(
                                "Unknown duplicate placeholder: {" + placeholderName + "}");
                    }
                    return placeholderValue;
                });
        if (!hasLength(suffix)) {
            return baseFilename;
        }
        return baseFilename + sanitizeFileName(suffix);
    }

    private static Pattern compileIndexedBusinessContextNamePattern() {
        String template = PLACEHOLDER_BUSINESS_CONTEXT_NAME_INDEXED_PATTERN;
        if (!template.contains("%d")) {
            throw new IllegalStateException("Indexed business context placeholder pattern must contain %d.");
        }

        String regex = template
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("%d", "(\\d+)");
        return Pattern.compile("^" + regex + "$");
    }
}
