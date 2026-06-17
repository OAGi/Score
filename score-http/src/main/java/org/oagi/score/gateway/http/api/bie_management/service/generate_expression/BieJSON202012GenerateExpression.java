package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Generates JSON Schema 2020-12 expressions for selected top-level BIEs.
 * <p>
 * This generator supports two reuse modes:
 * 1) Inline reusable definitions under {@code #/$defs}
 * 2) External-file references when split-reference mode is enabled
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class BieJSON202012GenerateExpression implements BieGenerateExpression, InitializingBean {

    private static final String ID_KEYWORD = "$id";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper mapper;

    private Map<String, Object> root;
    private Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap;
    private GenerateExpressionOption option;
    private ScoreUser requester;

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationContext applicationContext;

    private GenerationContext generationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = null;
        reusedTopLevelAsbiepNameMap = null;
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    /**
     * Builds a generation context for the requested top-level BIEs and optional JSON wrappers
     * (meta header / pagination response). Optional wrappers must belong to the same release.
     */
    @Override
    public GenerationContext generateContext(
            ScoreUser requester,
            List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) {
        List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps = new ArrayList<>(topLevelAsbieps);

        if (mergedTopLevelAsbieps.isEmpty()) {
            throw new IllegalArgumentException("Cannot found BIEs.");
        }
        ReleaseId releaseId = mergedTopLevelAsbieps.get(0).release().releaseId();

        TopLevelAsbiepQueryRepository topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
        appendOptionalTopLevelAsbiep(
                mergedTopLevelAsbieps,
                topLevelAsbiepQuery,
                option.isIncludeMetaHeaderForJson(),
                option.getMetaHeaderTopLevelAsbiepId(),
                releaseId,
                "Meta Header");
        appendOptionalTopLevelAsbiep(
                mergedTopLevelAsbieps,
                topLevelAsbiepQuery,
                option.isIncludePaginationResponseForJson(),
                option.getPaginationResponseTopLevelAsbiepId(),
                releaseId,
                "Pagination Response");

        return applicationContext.getBean(GenerationContext.class, requester, mergedTopLevelAsbieps);
    }

    @Override
    public void generate(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;
        this.requester = requester;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);

            Map<String, Object> definitions;
            if (root == null) {
                root = new LinkedHashMap();
                root.put("$schema", getJsonSchemaUri());
                root.put(ID_KEYWORD, "");
                root.put("type", "object");

                root.put("required", new ArrayList());
                root.put("additionalProperties", false);

                Map<String, Object> properties = new LinkedHashMap();
                root.put("properties", properties);
                definitions = new LinkedHashMap();
                root.put("$defs", definitions);

                reusedTopLevelAsbiepNameMap = new LinkedHashMap<>();
            } else {
                definitions = (Map<String, Object>) root.get("$defs");
            }

            /*
             * Issue #587
             */
            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            if (option.isIncludeMetaHeaderForJson()) {
                TopLevelAsbiepSummaryRecord metaHeaderTopLevelAsbiep =
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(option.getMetaHeaderTopLevelAsbiepId());
                fillProperties(root, definitions, metaHeaderTopLevelAsbiep, false, generationContext);
            }
            if (option.isIncludePaginationResponseForJson()) {
                TopLevelAsbiepSummaryRecord paginationResponseTopLevelAsbiep =
                        topLevelAsbiepQuery.getTopLevelAsbiepSummary(option.getPaginationResponseTopLevelAsbiepId());
                fillProperties(root, definitions, paginationResponseTopLevelAsbiep, false, generationContext);
            }

            fillProperties(root, definitions, topLevelAsbiep, asbiep, typeAbie,
                    option.isArrayForJsonExpression(), this.option.getBiePackage(), generationContext);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                TopLevelAsbiepSummaryRecord topLevelAsbiep, boolean isArray,
                                GenerationContext generationContext) {

        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);

        fillProperties(parent, definitions, topLevelAsbiep, asbiep, typeAbie, isArray, null, generationContext);
    }

    /**
     * Promotes the first child under {@code properties} to the current root object.
     * Used when a wrapped top-level property should be suppressed.
     */
    private void suppressRootProperty(Map<String, Object> parent, boolean isArray) {
        Map<String, Object> properties = (Map<String, Object>) parent.get("properties");
        if (properties == null || properties.isEmpty()) {
            return;
        }
        Map<String, Object> rootProperties =
                (Map<String, Object>) properties.get(properties.keySet().iterator().next());
        if (rootProperties == null) {
            return;
        }
        if (!isArray) {
            parent.put("type", "object");
        }
        parent.remove("required");
        parent.remove("additionalProperties");
        parent.remove("properties");
        parent.putAll(rootProperties);
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                AsbieSummaryRecord asbie,
                                GenerationContext generationContext) {

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        int minVal = asbie.cardinality().min();
        int maxVal = asbie.cardinality().max();
        // Issue #562
        // Issue #1733: the array vs. object decision follows the based ASCC's max
        // cardinality, not the (possibly narrowed) ASBIE max. The ASBIE cardinality
        // still governs the array bounds (minItems/maxItems).
        int basedMaxVal = generationContext.queryBasedASCC(asbie).cardinality().max();
        boolean isArray = (basedMaxVal < 0 || basedMaxVal > 1);
        boolean isNillable = (asbie.nillable() != null) ? asbie.nillable() : false;

        boolean reused = !asbie.ownerTopLevelAsbiepId().equals(asbiep.ownerTopLevelAsbiepId());
        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        if (reused) {
            String ref = getReference(definitions, asbiep, generationContext);
            properties.put("$ref", ref);
        } else {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

            String resolvedDescription = resolveDescription(option,
                    new String[]{asbie.definition()},
                    ascc.definition());
            if (StringUtils.hasLength(resolvedDescription)) {
                properties.put("description", resolvedDescription);
            }

            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            fillProperties(properties, definitions, typeAbie, generationContext);

            if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
                properties.remove("required");
            }

            properties = applyNillableTypeUnion(properties, isNillable);
        }

        if (isArray) {
            Map<String, Object> items = new LinkedHashMap();
            items.putAll(properties);
            if (reused) {
                items = applyNillableTypeUnion(items, isNillable);
            }

            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);

            // Issue #1483
            // make a global property for an array
            if (reused) {
                properties = makeGlobalPropertyIfArray(definitions, resolveReusedSchemaName(definitions, asbiep), properties);
            }
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> makeGlobalPropertyIfArray(Map<String, Object> definitions, String name,
                                                          Map<String, Object> properties) {
        if (properties == null || !"array".equals(properties.get("type"))) {
            return properties;
        }

        Set<String> keySet = properties.keySet();
        boolean customized = keySet.contains("minItems") || keySet.contains("maxItems");
        if (customized) {
            return properties;
        }

        String nameForList = name + "List";
        if (!definitions.containsKey(nameForList)) {
            definitions.put(nameForList, properties);
        }

        Map<String, Object> refProperties = new HashMap<>();
        refProperties.put("$ref", "#/$defs/" + nameForList);
        return refProperties;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                AsbiepSummaryRecord asbiep, AbieSummaryRecord abie, boolean isArray,
                                BiePackageSummaryRecord biePackage,
                                GenerationContext generationContext) {

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        AccSummaryRecord acc = generationContext.queryBasedACC(abie);
        String name = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        String resolvedDescription = resolveDescription(option,
                new String[]{asbiep.definition(), abie.definition()},
                asccp.definition(), acc.definition());
        if (StringUtils.hasLength(resolvedDescription)) {
            properties.put("description", resolvedDescription);
        }

        /*
         * Issue #550
         */
        if (!isArray) {
            properties.put("type", "object");
        }
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        fillProperties(properties, definitions, abie, generationContext);
        // The following comment will be unpacked after
        // the architecture committee has reached a conclusion on how to express BIE Package Metadata.
        /*
        if (biePackage != null) { // Issue #1615
            attachBiePackageAttributes(properties, definitions, topLevelAsbiep, biePackage);
        }
        */

        if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        /*
         * Issue #575
         */
        if (isArray) {
            Map<String, Object> items = new LinkedHashMap();
            items.put("type", "object");
            items.putAll(properties);
            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            properties.put("items", items);
        }

        Map<String, Object> parentProperties = (Map<String, Object>) parent.get("properties");
        if (isReferencedSchemaFile(topLevelAsbiep)) {
            // Mirror XML "global named type" style for referenced schema files.
            // The root property points to a named $defs type, and external refs can target it.
            String topLevelTypeName = resolveTopLevelExternalTypeName(topLevelAsbiep, asccp, definitions);
            definitions.putIfAbsent(topLevelTypeName, properties);
            Map<String, Object> refProperty = new LinkedHashMap<>();
            refProperty.put("$ref", "#/$defs/" + topLevelTypeName);
            parentProperties.put(name, refProperty);
            return;
        }

        parentProperties.put(name, properties);
    }

    private void attachBiePackageAttributes(Map<String, Object> properties, Map<String, Object> definitions,
                                            TopLevelAsbiepSummaryRecord topLevelAsbiep, BiePackageSummaryRecord biePackage) {

        Map<String, Object> newProps = new LinkedHashMap<>();
        List<String> requiredAttrs = new ArrayList<>();
        if (addAttribute(newProps, definitions, "packageName", biePackage.name(), "string")) {
            requiredAttrs.add("packageName");
        }
        if (addAttribute(newProps, definitions, "packageVersionId", biePackage.versionId(), "string")) {
            requiredAttrs.add("packageVersionId");
        }
        if (addAttribute(newProps, definitions, "packageVersionName", biePackage.versionName(), "string")) {
            requiredAttrs.add("packageVersionName");
        }
        if (addAttribute(newProps, definitions, "packageDescription", biePackage.description(), "string")) {
            requiredAttrs.add("packageDescription");
        }
        if (addAttribute(newProps, definitions, "versionId", topLevelAsbiep.version(), "normalized string")) {
            requiredAttrs.add("versionId");
        }

        if (properties.get("required") != null) {
            requiredAttrs.addAll((List<String>) properties.get("required"));
        }
        properties.put("required", requiredAttrs);

        Map<String, Object> objProps = (Map<String, Object>) properties.get("properties");
        if (objProps != null) {
            newProps.putAll(objProps);
        }
        properties.put("properties", newProps);
    }

    private boolean addAttribute(Map<String, Object> properties, Map<String, Object> definitions,
                                 String name, String value, String type) {
        if (properties.get(name) != null) {
            return false;
        }

        XbtSummaryRecord xbt = generationContext.findXbtByName(type);
        String ref = fillDefinitions(definitions, xbt);

        Map<String, Object> attrProps = new LinkedHashMap<>();
        attrProps.put("$ref", ref);
        attrProps.put("const", hasLength(value) ? value : "");

        properties.put(name, attrProps);

        return true;
    }

    protected String getJsonSchemaUri() {
        return "https://json-schema.org/draft/2020-12/schema";
    }

    protected String getXbtJsonSchemaMap(XbtSummaryRecord xbt) {
        return xbt.jbt202012Map();
    }

    protected Map<String, Object> toProperties(XbtSummaryRecord xbt) {
        String jbtDraft05Map = getXbtJsonSchemaMap(xbt);
        try {
            return mapper.readValue(jbtDraft05Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   XbtSummaryRecord xbt, Facet facet, String componentName) {
        if (!definitions.containsKey(componentName)) {
            Map<String, Object> content = toProperties(xbt);

            String type;
            if (content.containsKey("type")) {
                type = (String) content.get("type");
            } else {
                type = "string";
                content.put("type", type);
            }

            if (facet != null) {
                boolean isTypeString = "string".equals(type);
                boolean isTypeNumeric = "integer".equals(type) || "number".equals(type);

                if (isTypeString && facet.minLength() != null) {
                    content.put("minLength", facet.minLength().longValue());
                }
                if (isTypeString && facet.maxLength() != null) {
                    content.put("maxLength", facet.maxLength().longValue());
                }
                if (isTypeString && StringUtils.hasLength(facet.pattern())) {
                    // Override 'pattern' and 'format' properties
                    content.remove("pattern");
                    content.remove("format");
                    content.put("pattern", facet.pattern());
                }
            }

            definitions.put(componentName, content);
        }

        return "#/$defs/" + componentName;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   XbtSummaryRecord xbt) {
        String builtInType = xbt.builtInType();
        if (builtInType.startsWith("xsd:")) {
            builtInType = builtInType.substring(4);
        }
        if (!definitions.containsKey(builtInType)) {
            Map<String, Object> content = toProperties(xbt);
            definitions.put(builtInType, content);
        }

        return "#/$defs/" + builtInType;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BbieSummaryRecord bbie, CodeListSummaryRecord codeList) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);

        Map<String, Object> properties;
        if (dtAwdPri.codeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtAwdPri.xbtManifestId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BbieScSummaryRecord bbieSc, CodeListSummaryRecord codeList) {
        DtScAwdPriSummaryRecord dtScAwdPri =
                generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);

        Map<String, Object> properties;
        if (dtScAwdPri.codeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> properties,
                                   Map<String, Object> definitions,
                                   CodeListSummaryRecord codeList) {

        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
        String codeListName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        if (!definitions.containsKey(codeListName)) {
            List<CodeListValueSummaryRecord> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(codeListName, properties);
        }

        return "#/$defs/" + codeListName;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   AgencyIdListSummaryRecord agencyIdList) {
        AgencyIdListValueSummaryRecord agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
        String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);
        if (!definitions.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValueSummaryRecord> agencyIdListValues =
                    generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.agencyIdListManifestId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(agencyListTypeName, properties);
        }

        return "#/$defs/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                AbieSummaryRecord abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        AccSummaryRecord acc = generationContext.getAcc(abie.basedAccManifestId());
        if (OagisComponentType.Choice == acc.componentType()) {
            List<Object> oneOf = new ArrayList();

            for (BIE bie : children) {
                Map<String, Object> item = new LinkedHashMap();
                item.put("type", "object");
                item.put("required", new ArrayList());
                item.put("additionalProperties", false);
                item.put("properties", new LinkedHashMap<String, Object>());

                fillProperties(item, definitions, bie, generationContext);

                if (item.containsKey("required") && ((List) item.get("required")).isEmpty()) {
                    item.remove("required");
                }
                oneOf.add(item);
            }

            parent.clear();
            parent.put("oneOf", oneOf);
        } else {
            for (BIE bie : children) {
                fillProperties(parent, definitions, bie, generationContext);
            }
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BIE bie,
                                GenerationContext generationContext) {
        if (bie instanceof BbieSummaryRecord) {
            BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
            fillProperties(parent, definitions, bbie, generationContext);
        } else {
            AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;

            if (Helper.isAnyProperty(asbie, generationContext)) {
                parent.put("additionalProperties", true);
            } else {
                AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);

                generationContext.referenceCounter().increase(asbiep)
                        .ifNotCircularReference(asbiep,
                                () -> fillProperties(parent, definitions, asbie, generationContext))
                        .decrease(asbiep);
            }
        }
    }

    private Object readJsonValue(String textContent) {
        try {
            return mapper.readValue(textContent, Object.class);
        } catch (Exception e) {
            logger.warn("Can't read JSON value from given text: " + textContent, e);
        }
        return null;
    }

    private void normalizeExamplesByRef(Map<String, Object> properties,
                                        String ref,
                                        Map<String, Object> definitions) {
        if (properties == null || ref == null || !ref.startsWith("#/$defs/")) {
            return;
        }

        Object examplesObject = properties.get("examples");
        if (!(examplesObject instanceof List<?>)) {
            return;
        }

        String definitionName = ref.substring("#/$defs/".length());
        Object definitionObject = definitions.get(definitionName);
        if (!(definitionObject instanceof Map<?, ?>)) {
            return;
        }

        Object typeObject = ((Map<?, ?>) definitionObject).get("type");
        if (!(typeObject instanceof String)) {
            return;
        }
        String type = (String) typeObject;
        if (!"boolean".equals(type) && !"number".equals(type) && !"integer".equals(type)) {
            return;
        }

        List<Object> normalizedExamples = new ArrayList<>();
        boolean changed = false;
        for (Object example : (List<?>) examplesObject) {
            Object normalized = normalizeExampleByType(example, type);
            normalizedExamples.add(normalized);
            if (!Objects.equals(example, normalized)) {
                changed = true;
            }
        }

        if (changed) {
            properties.put("examples", normalizedExamples);
        }
    }

    private Object normalizeExampleByType(Object example, String type) {
        if (!(example instanceof String)) {
            return example;
        }

        String trimmed = ((String) example).trim();
        if (!StringUtils.hasLength(trimmed)) {
            return example;
        }

        if ("boolean".equals(type)) {
            if ("true".equalsIgnoreCase(trimmed)) {
                return true;
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return false;
            }
            return example;
        }

        try {
            if ("integer".equals(type)) {
                return new BigInteger(trimmed);
            }
            if ("number".equals(type)) {
                return new BigDecimal(trimmed);
            }
        } catch (NumberFormatException ignored) {
            return example;
        }

        return example;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BbieSummaryRecord bbie,
                                GenerationContext generationContext) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        BccpSummaryRecord bccp = generationContext.queryToBCCP(bcc);
        if (bccp == null) {
            throw new IllegalStateException();
        }
        DtSummaryRecord bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.cardinality().min();
        int maxVal = bbie.cardinality().max();
        // Issue #562
        // Issue #1733: the array vs. object decision follows the based BCC's max
        // cardinality, not the (possibly narrowed) BBIE max. The BBIE cardinality
        // still governs the array bounds (minItems/maxItems).
        int basedMaxVal = bcc.cardinality().max();
        boolean isArray = (basedMaxVal < 0 || basedMaxVal > 1);
        boolean isNillable = (bbie.nillable() != null) ? bbie.nillable() : false;

        String name = convertIdentifierToId(camelCase(bccp.propertyTerm()));

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        String resolvedDescription = resolveDescription(option,
                new String[]{bbie.definition()},
                bcc.definition());
        if (StringUtils.hasLength(resolvedDescription)) {
            properties.put("description", resolvedDescription);
        }

        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        // Issue #596
        if (bbie.valueConstraint() != null) {
            if (StringUtils.hasLength(bbie.valueConstraint().fixedValue())) {
                properties.put("const", bbie.valueConstraint().fixedValue());
            } else if (StringUtils.hasLength(bbie.valueConstraint().defaultValue())) {
                properties.put("default", bbie.valueConstraint().defaultValue());
            }
        }

        // Issue #692
        String exampleText = bbie.example();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        // Issue #564
        String ref = getReference(definitions, bbie, bdt, generationContext);
        normalizeExamplesByRef(properties, ref, definitions);
        List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.cardinality().max() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            properties = withRefFirst(properties, ref);
            properties = applyNillableTypeUnion(properties, isNillable);
        } else {
            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            Map<String, Object> contentProperties = new LinkedHashMap();
            contentProperties.put("$ref", ref);
            for (String key : Arrays.asList("const", "default", "examples")) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", applyNillableTypeUnion(contentProperties, isNillable));

            for (BbieScSummaryRecord bbieSc : bbieScList) {
                fillProperties(properties, definitions, bbieSc, generationContext);
            }
        }

        if (isArray) {
            String description = (String) properties.remove("description");
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            if (minVal > 0) {
                properties.put("minItems", minVal);
            }
            if (maxVal > 0) {
                properties.put("maxItems", maxVal);
            }
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> applyNillableTypeUnion(Map<String, Object> properties,
                                                       boolean isNillable) {
        if (isNillable) {
            Object type = properties.get("type");
            if (type instanceof String) {
                if (!"null".equals(type)) {
                    properties.put("type", Arrays.asList(type, "null"));
                }
                return properties;
            }
            if (type instanceof Collection) {
                List<Object> typeList = new ArrayList<>((Collection) type);
                if (!typeList.contains("null")) {
                    typeList.add("null");
                }
                properties.put("type", typeList);
                return properties;
            }

            // Fallback for schemas without explicit 'type' (e.g. $ref with siblings).
            Map<String, Object> prop = new LinkedHashMap();
            prop.put("anyOf", Arrays.asList(
                    properties,
                    ImmutableMap.<String, Object>builder()
                            .put("type", "null")
                            .build()
            ));
            return prop;
        }

        return properties;
    }

    /**
     * Resolves a reference for a reused ABIE schema.
     * <p>
     * Depending on split-reference mode, this points either to local {@code #/$defs}
     * or to an external file with a JSON Pointer into that file.
     */
    private String getReference(Map<String, Object> definitions,
                                AsbiepSummaryRecord asbiep,
                                GenerationContext generationContext) {
        String refNameOfTopLevelAsbiep = resolveReusedSchemaName(definitions, asbiep);
        TopLevelAsbiepSummaryRecord refTopLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.ownerTopLevelAsbiepId());
        if (!option.isSeparateFileReferencesForReusedSchemas()) {
            if (!definitions.containsKey(refNameOfTopLevelAsbiep)) {
                Map<String, Object> properties = new LinkedHashMap<>();
                properties.put("required", new ArrayList());
                fillProperties(properties, definitions, refTopLevelAsbiep, false, generationContext);
                suppressRootProperty(properties, false);
                definitions.put(refNameOfTopLevelAsbiep, properties);
            }
            return "#/$defs/" + refNameOfTopLevelAsbiep;
        } else {
            String refFileName = resolveExternalRefFileName(refTopLevelAsbiep);
            if (!StringUtils.hasLength(refFileName)) {
                refFileName = defaultExternalRefFileName(refNameOfTopLevelAsbiep);
            }
            String refTypeName = resolveTopLevelExternalTypeName(
                    refTopLevelAsbiep,
                    generationContext.queryBasedASCCP(asbiep),
                    null);
            return refFileName + ".json#/$defs/" + escapeJsonPointerToken(refTypeName);
        }
    }

    private String resolveExternalRefFileName(TopLevelAsbiepSummaryRecord refTopLevelAsbiep) {
        Map<TopLevelAsbiepId, String> filenames = option.getFilenames();
        if (filenames == null || filenames.isEmpty()) {
            return null;
        }

        String filename = filenames.get(refTopLevelAsbiep.topLevelAsbiepId());
        if (!StringUtils.hasLength(filename)) {
            return null;
        }
        // Keep the filename bound to the reused schema's owner top-level ASBIEP.
        // This guarantees that $ref targets the exact file generated for that reused schema.
        return filename;
    }

    private String resolveReusedSchemaName(Map<String, Object> definitions,
                                           AsbiepSummaryRecord asbiep) {
        String baseName = resolveReusedSchemaBaseName(asbiep);
        TopLevelAsbiepId ownerTopLevelAsbiepId = asbiep.ownerTopLevelAsbiepId();
        String existing = reusedTopLevelAsbiepNameMap.get(ownerTopLevelAsbiepId);
        if (StringUtils.hasLength(existing)) {
            return existing;
        }

        String candidate = baseName;
        int suffix = 1;
        while (definitions.containsKey(candidate) || reusedTopLevelAsbiepNameMap.containsValue(candidate)) {
            candidate = baseName + suffix++;
        }

        reusedTopLevelAsbiepNameMap.put(ownerTopLevelAsbiepId, candidate);
        return candidate;
    }

    private String resolveReusedSchemaBaseName(AsbiepSummaryRecord asbiep) {
        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        return convertIdentifierToId(camelCase(asccp.propertyTerm()));
    }

    private String resolveTopLevelExternalTypeName(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                                   AsccpSummaryRecord asccp,
                                                   Map<String, Object> definitions) {
        String baseName = convertIdentifierToId(camelCase(asccp.propertyTerm()));
        String topLevelAsbiepIdValue = (topLevelAsbiep != null && topLevelAsbiep.topLevelAsbiepId() != null)
                ? topLevelAsbiep.topLevelAsbiepId().value().toString()
                : "0";
        String candidate = baseName + "_" + topLevelAsbiepIdValue + "Type";
        if (definitions == null) {
            return candidate;
        }
        if (!definitions.containsKey(candidate)) {
            return candidate;
        }
        int suffix = 1;
        String uniqueCandidate = candidate + "_" + suffix;
        while (definitions.containsKey(uniqueCandidate)) {
            suffix++;
            uniqueCandidate = candidate + "_" + suffix;
        }
        return uniqueCandidate;
    }

    private boolean isReferencedSchemaFile(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (topLevelAsbiep == null || topLevelAsbiep.topLevelAsbiepId() == null || generationContext == null) {
            return false;
        }
        return generationContext.getSchemaReferenceInfo(topLevelAsbiep.topLevelAsbiepId()).isReferencedSchemaFile();
    }

    private String escapeJsonPointerToken(String token) {
        if (token == null) {
            return "";
        }
        return token.replace("~", "~0").replace("/", "~1");
    }

    private String defaultExternalRefFileName(String reusedSchemaName) {
        if (!StringUtils.hasLength(reusedSchemaName)) {
            return "Schema";
        }
        return Character.toUpperCase(reusedSchemaName.charAt(0)) + reusedSchemaName.substring(1);
    }

    private void appendOptionalTopLevelAsbiep(
            List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps,
            TopLevelAsbiepQueryRepository topLevelAsbiepQuery,
            boolean enabled,
            TopLevelAsbiepId optionalTopLevelAsbiepId,
            ReleaseId expectedReleaseId,
            String label) {
        if (!enabled || optionalTopLevelAsbiepId == null) {
            return;
        }
        TopLevelAsbiepSummaryRecord optionalTopLevelAsbiep =
                topLevelAsbiepQuery.getTopLevelAsbiepSummary(optionalTopLevelAsbiepId);
        if (!expectedReleaseId.equals(optionalTopLevelAsbiep.release().releaseId())) {
            throw new IllegalArgumentException(label + " release does not match.");
        }
        mergedTopLevelAsbieps.add(optionalTopLevelAsbiep);
    }

    private String getReference(Map<String, Object> definitions, BbieSummaryRecord bbie, DtSummaryRecord bdt,
                                GenerationContext generationContext) {
        CodeListSummaryRecord codeList = generationContext.getCodeList(bbie.primitiveRestriction().codeListManifestId());
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbie, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbie.primitiveRestriction().agencyIdListManifestId());
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                XbtSummaryRecord xbt;
                if (bbie.primitiveRestriction().xbtManifestId() == null) {
                    DtAwdPriSummaryRecord dtAwdPri =
                            generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);
                    xbt = Helper.getXbt(generationContext, dtAwdPri);
                } else {
                    xbt = generationContext.getXbt(bbie.primitiveRestriction().xbtManifestId());
                }

                if (hasAnyValuesInFacets(bbie.facet())) {
                    ref = fillDefinitions(definitions, xbt, bbie.facet(), "type_" + bbie.getGuid());
                } else {
                    ref = fillDefinitions(definitions, xbt);
                }
            }
        }

        return ref;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BbieScSummaryRecord bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.cardinality().min();
        int maxVal = bbieSc.cardinality().max();
        if (maxVal == 0) {
            return;
        }

        DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
        String name = convertIdentifierToId(toName(dtSc.propertyTerm(), dtSc.representationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            return rt;
        }, true));
        Map<String, Object> properties = new LinkedHashMap();

        String resolvedDescription = resolveDescription(option,
                new String[]{bbieSc.definition()},
                dtSc.definition());
        if (StringUtils.hasLength(resolvedDescription)) {
            properties.put("description", resolvedDescription);
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596
        if (bbieSc.valueConstraint() != null) {
            if (StringUtils.hasLength(bbieSc.valueConstraint().fixedValue())) {
                properties.put("const", bbieSc.valueConstraint().fixedValue());
            } else if (StringUtils.hasLength(bbieSc.valueConstraint().defaultValue())) {
                properties.put("default", bbieSc.valueConstraint().defaultValue());
            }
        }

        // Issue #692
        String exampleText = bbieSc.example();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        CodeListSummaryRecord codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbieSc, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                XbtSummaryRecord xbt;
                if (bbieSc.primitiveRestriction().xbtManifestId() == null) {
                    DtScAwdPriSummaryRecord dtScAwdPri =
                            generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
                    xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());
                } else {
                    xbt = generationContext.getXbt(bbieSc.primitiveRestriction().xbtManifestId());
                }

                if (hasAnyValuesInFacets(bbieSc.facet())) {
                    ref = fillDefinitions(definitions, xbt, bbieSc.facet(), "type_" + bbieSc.getGuid());
                } else {
                    ref = fillDefinitions(definitions, xbt);
                }
            }
        }

        normalizeExamplesByRef(properties, ref, definitions);
        properties = withRefFirst(properties, ref);

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> withRefFirst(Map<String, Object> properties, String ref) {
        LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
        ordered.put("$ref", ref);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if ("$ref".equals(entry.getKey())) {
                continue;
            }
            ordered.put(entry.getKey(), entry.getValue());
        }
        return ordered;
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }

        // The "required" property for the root element of schema should has only one child.
        List<String> required = (List<String>) root.get("required");
        if (required != null && required.size() > 1) {
            root.remove("required");
        }

        Map<String, Object> properties = (Map<String, Object>) root.get("properties");
        if (properties == null || properties.isEmpty()) {
            return;
        }

        for (String key : properties.keySet()) {
            Map<String, Object> copied = new LinkedHashMap();
            copied.putAll(((Map<String, Object>) properties.get(key)));
            properties.put(key, copied);
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();
        root.put(ID_KEYWORD, filename + ".json");

        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".json");

        mapper.writeValue(tempFile, root);
        logger.info("JSON Schema is generated: " + tempFile);

        return tempFile;
    }
}
