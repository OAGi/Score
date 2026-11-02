package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
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
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper;
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
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Builds the {@code #/components/schemas} of an OpenAPI document from BIEs: the schema of a top-level BIE and of the
 * code lists, agency ID lists and built-in types it refers to. What the OpenAPI 3.0 and 3.1 schemas express differently
 * is decided by the {@link BieOpenApiDialect}; what a user of the builder expresses differently (the BIE generator and
 * the OpenAPI Document generator) by the {@link BieOpenApiSchemaPolicy}.
 * <p>
 * The builder writes into the {@code schemas} it is given, so the schemas of the BIEs of one document accumulate.
 */
public class BieOpenApiSchemaBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GenerationContext generationContext;
    private final BieOpenApiDialect dialect;
    private final BieOpenApiSchemaPolicy policy;
    private final boolean bieDefinition;
    private final String scheme;
    private final String host;
    private final Map<String, Object> schemas;
    private final Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap;
    private final ObjectMapper mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    /**
     * @param bieDefinition                whether the definition of a BIE is put as its description
     * @param scheme                       the scheme and the host of the URI of a BIE
     * @param schemas                      the {@code components.schemas} that the schemas are put into
     * @param reusedTopLevelAsbiepNameMap  the names of the schemas of the reused top-level BIEs, by the top-level BIE
     */
    public BieOpenApiSchemaBuilder(GenerationContext generationContext, BieOpenApiDialect dialect,
                                   BieOpenApiSchemaPolicy policy, boolean bieDefinition, String scheme, String host,
                                   Map<String, Object> schemas,
                                   Map<TopLevelAsbiepId, String> reusedTopLevelAsbiepNameMap) {
        this.generationContext = generationContext;
        this.dialect = dialect;
        this.policy = policy;
        this.bieDefinition = bieDefinition;
        this.scheme = scheme;
        this.host = host;
        this.schemas = schemas;
        this.reusedTopLevelAsbiepNameMap = reusedTopLevelAsbiepNameMap;
    }

    public Map<String, Object> makeProperties(AbieSummaryRecord typeAbie, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        Map<String, Object> properties = new LinkedHashMap();
        // Issue #1148
        properties.put("x-oagis-bie-guid", asbiep.getGuid());
        properties.put("x-oagis-bie-date-time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(typeAbie.lastUpdated().when()));
        properties.put("x-oagis-bie-version", StringUtils.hasLength(topLevelAsbiep.version()) ? topLevelAsbiep.version() : "");
        policy.putRootExtensions(properties, generationContext.getAsccp(asbiep.basedAsccpManifestId()));
        // Issue #1574
        ReleaseDetailsRecord release = generationContext.findRelease(topLevelAsbiep.release().releaseId());
        properties.put("x-oagis-bie-uri", scheme + "://" + host + "/profile_bie/" + topLevelAsbiep.topLevelAsbiepId().toString());
        properties.put("x-oagis-release", release.releaseNum());
        properties.put("x-oagis-release-date", new SimpleDateFormat("yyyy-MM-dd").format(release.lastUpdated().when()));
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        return properties;
    }

    /**
     * Promotes the first child under {@code properties} to the current root object.
     * Used by template options that suppress the wrapper root property.
     */
    public void suppressRootProperty(Map<String, Object> parent) {
        Map<String, Object> properties = (Map<String, Object>) parent.get("properties");
        if (properties == null || properties.isEmpty()) {
            return;
        }
        Map<String, Object> rootProperties =
                (Map<String, Object>) properties.get(properties.keySet().iterator().next());
        if (rootProperties == null) {
            return;
        }
        parent.put("type", "object");
        parent.remove("required");
        parent.remove("additionalProperties");
        parent.remove("properties");
        parent.putAll(rootProperties);
    }

    public void fillProperties(Map<String, Object> parent, TopLevelAsbiepSummaryRecord topLevelAsbiep) {

        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);

        fillProperties(parent, asbiep, typeAbie);
    }

    void fillProperties(Map<String, Object> parent,
                        AsbieSummaryRecord asbie) {

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
        boolean isArray = (maxVal < 0 || maxVal > 1);
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
            String ref = reference(asbiep);
            properties.put("$ref", ref);
            // A nillable ASBIE that reuses another BIE is nillable like any other ASBIE.
            properties = dialect.asbie(allOf(properties), asbie, isNillable);
        } else {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

            if (bieDefinition) {
                String definition = asbie.definition();
                if (StringUtils.hasLength(definition)) {
                    properties.put("description", definition);
                }
            }

            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            fillProperties(properties, typeAbie);

            if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
                properties.remove("required");
            }

            properties = dialect.asbie(allOf(properties), asbie, isNillable);
        }

        // Issue #1298
        if (asbie.deprecated() != null && asbie.deprecated()) {
            properties.put("deprecated", true);
        }

        if (isArray) {
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }

            properties.put("type", "array");

            if (policy.moveDeprecatedToAssociationArray()) {
                Boolean deprecated = (Boolean) items.remove("deprecated");
                if (deprecated != null) {
                    properties.put("deprecated", deprecated);
                }
            }

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
                properties = makeGlobalPropertyIfArray(resolveReusedSchemaName(asbiep), properties);
            }
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> makeGlobalPropertyIfArray(String name,
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
        if (!schemas.containsKey(nameForList)) {
            schemas.put(nameForList, properties);
        }

        Map<String, Object> refProperties = new HashMap<>();
        refProperties.put("$ref", "#/components/schemas/" + nameForList);
        return refProperties;
    }

    public void fillProperties(Map<String, Object> parent,
                               AsbiepSummaryRecord asbiep, AbieSummaryRecord abie) {

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.propertyTerm()));

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (bieDefinition) {
            String definition = asbiep.definition();
            if (!StringUtils.hasLength(definition)) {
                definition = abie.definition();
            }
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        properties.put("type", "object");
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);

        fillProperties(properties, abie);

        if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        properties = dialect.asbiep(properties, asbiep);

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    Map<String, Object> toProperties(XbtSummaryRecord xbt) {
        String openApiMap = dialect.xbtOpenApiMap(xbt);
        try {
            return mapper.readValue(openApiMap, LinkedHashMap.class);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillSchemas(BbieSummaryRecord bbie, CodeListSummaryRecord codeList) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);

        Map<String, Object> properties;
        if (dtAwdPri.codeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            XbtSummaryRecord xbt = Helper.getXbt(generationContext, dtAwdPri);
            properties = toProperties(xbt);
        }

        return fillSchemas(properties, codeList);
    }

    private String fillSchemas(BbieScSummaryRecord bbieSc, CodeListSummaryRecord codeList) {
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

        return fillSchemas(properties, codeList);
    }

    private String fillSchemas(Map<String, Object> properties,
                               CodeListSummaryRecord codeList) {

        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
        String codeListName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        /*
         * Issue #589
         */
        codeListName = Stream.of(codeListName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));

        if (!schemas.containsKey(codeListName)) {
            List<CodeListValueSummaryRecord> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(codeListName, properties);
        }

        return "#/components/schemas/" + codeListName;
    }

    private String fillSchemas(AgencyIdListSummaryRecord agencyIdList) {
        AgencyIdListValueSummaryRecord agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
        String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);
        /*
         * Issue #589
         */
        agencyListTypeName = Stream.of(agencyListTypeName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));
        if (!schemas.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValueSummaryRecord> agencyIdListValues =
                    generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.agencyIdListManifestId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.value()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            schemas.put(agencyListTypeName, properties);
        }

        return "#/components/schemas/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                AbieSummaryRecord abie) {

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

                fillProperties(item, bie);

                if (item.containsKey("required") && ((List) item.get("required")).isEmpty()) {
                    item.remove("required");
                }
                oneOf.add(item);
            }

            dialect.abieChoice(parent, oneOf, abie);
        } else {
            for (BIE bie : children) {
                fillProperties(parent, bie);
            }
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                BIE bie) {
        if (bie instanceof BbieSummaryRecord) {
            BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
            fillProperties(parent, bbie);
        } else {
            AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;
            if (Helper.isAnyProperty(asbie, generationContext)) {
                parent.put("additionalProperties", true);
            } else {
                AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);

                generationContext.referenceCounter().increase(asbiep)
                        .ifNotCircularReference(asbiep,
                                () -> fillProperties(parent, asbie))
                        .decrease(asbiep);
            }
        }
    }

    private void fillProperties(Map<String, Object> parent,
                                BbieSummaryRecord bbie) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        BccpSummaryRecord bccp = generationContext.queryToBCCP(bcc);
        DtSummaryRecord bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.cardinality().min();
        int maxVal = bbie.cardinality().max();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = (bbie.nillable() != null) ? bbie.nillable() : false;

        String name = convertIdentifierToId(camelCase(bccp.propertyTerm()));

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (bieDefinition) {
            String definition = bbie.definition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        // Issue #700, #692
        XbtSummaryRecord xbt = getXbt(bbie, bdt);
        dialect.bbieKeywords(properties, bbie, exampleValue(xbt, bbie.example()));

        // Issue #564
        String ref = getReference(bbie, bdt);
        List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.cardinality().max() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            if (ref == null) {
                Map<String, Object> content = applyFacet(toProperties(xbt), bbie.facet());
                properties.putAll(content);
            } else {
                properties.put("$ref", ref);
            }
            properties = dialect.bbie(allOf(properties), bbie, isNillable);
        } else {
            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            Map<String, Object> contentProperties = new LinkedHashMap();
            if (ref == null) {
                Map<String, Object> content = applyFacet(toProperties(xbt), bbie.facet());
                contentProperties.putAll(content);
            } else {
                contentProperties.put("$ref", ref);
            }
            for (String key : dialect.scContentCopyKeys()) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", dialect.bbieContent(allOf(contentProperties), bbie, isNillable));

            for (BbieScSummaryRecord bbieSc : bbieScList) {
                fillProperties(properties, bbieSc);
            }
        }

        // Issue #1298
        if (bbie.deprecated() != null && bbie.deprecated()) {
            properties.put("deprecated", true);
        }

        if (isArray) {
            String description = policy.moveDescriptionToLeafArray() ? (String) properties.remove("description") : null;
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }

            properties.put("type", "array");

            Boolean deprecated = (Boolean) items.remove("deprecated");
            if (deprecated != null) {
                properties.put("deprecated", deprecated);
            }

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

    private Object exampleValue(XbtSummaryRecord xbt, String exampleText) {
        // Issue #1405
        return StringUtils.hasLength(exampleText) ? exampleText : emptyExample(xbt);
    }

    Object emptyExample(XbtSummaryRecord xbt) {
        Map<String, Object> properties = toProperties(xbt);
        Object type = properties.getOrDefault("type", "string");
        if ("boolean".equals(type)) {
            return false; // false for boolean (example: false)
        } else if ("integer".equals(type) || "number".equals(type)) {
            return null; // null value if integer or numeric (example: null)
        } else {
            return ""; // string or date, then empty string (example: "")
        }
    }

    private Map<String, Object> allOf(Map<String, Object> properties) {
        if (properties.containsKey("$ref") && properties.size() > 1) {
            Map<String, Object> prop = new LinkedHashMap();
            Map<String, Object> refMap = ImmutableMap.<String, Object>builder()
                    .put("$ref", properties.remove("$ref"))
                    .build();
            prop.put("allOf", (properties.isEmpty()) ? Arrays.asList(refMap) : Arrays.asList(refMap, properties));

            return prop;
        }

        return properties;
    }

    private XbtSummaryRecord getXbt(BbieSummaryRecord bbie, DtSummaryRecord bdt) {
        if (bbie.primitiveRestriction().xbtManifestId() == null) {
            DtAwdPriSummaryRecord dtAwdPri =
                    generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);
            return Helper.getXbt(generationContext, dtAwdPri);
        } else {
            return generationContext.getXbt(bbie.primitiveRestriction().xbtManifestId());
        }
    }

    /**
     * The reference to the schema of the top-level BIE that {@code asbiep} reuses, named after its ASCCP.
     */
    public String reference(AsbiepSummaryRecord asbiep) {
        return reference(asbiep, null, true);
    }

    /**
     * The reference to the schema of the top-level BIE that {@code asbiep} belongs to; the schema is made if it is not
     * there yet.
     *
     * @param schemaName         the name of the schema, or null for the name after its ASCCP
     * @param suppressRootProperty whether the schema holds the properties of the root property instead of the property
     */
    public String reference(AsbiepSummaryRecord asbiep, String schemaName, boolean suppressRootProperty) {
        if (schemaName == null) {
            schemaName = resolveReusedSchemaName(asbiep);
        }

        TopLevelAsbiepSummaryRecord refTopLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.ownerTopLevelAsbiepId());
        if (!schemas.containsKey(schemaName)) {
            schemas.put(schemaName, makeSchema(asbiep, refTopLevelAsbiep, suppressRootProperty));
        } else if (policy.separateSchemaForOtherBieOfSameName()
                && !asbiep.getGuid().equals(((Map<String, Object>) schemas.get(schemaName)).get("x-oagis-bie-guid"))) {
            schemaName = schemaName + refTopLevelAsbiep.topLevelAsbiepId();
            schemas.put(schemaName, makeSchema(asbiep, refTopLevelAsbiep, suppressRootProperty));
        }

        return "#/components/schemas/" + schemaName;
    }

    private Map<String, Object> makeSchema(AsbiepSummaryRecord asbiep, TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                           boolean suppressRootProperty) {
        AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
        Map<String, Object> properties = makeProperties(typeAbie, topLevelAsbiep);
        fillProperties(properties, asbiep, typeAbie);
        if (suppressRootProperty) {
            suppressRootProperty(properties);
        }
        return properties;
    }

    public String resolveReusedSchemaName(AsbiepSummaryRecord asbiep) {
        String baseName = resolveReusedSchemaBaseName(asbiep);
        TopLevelAsbiepId ownerTopLevelAsbiepId = asbiep.ownerTopLevelAsbiepId();
        String existing = reusedTopLevelAsbiepNameMap.get(ownerTopLevelAsbiepId);
        if (StringUtils.hasLength(existing)) {
            return existing;
        }

        String candidate = baseName;
        int suffix = 1;
        while (schemas.containsKey(candidate)) {
            candidate = baseName + suffix++;
        }

        reusedTopLevelAsbiepNameMap.put(ownerTopLevelAsbiepId, candidate);
        return candidate;
    }

    private String resolveReusedSchemaBaseName(AsbiepSummaryRecord asbiep) {
        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        return convertIdentifierToId(camelCase(asccp.propertyTerm()));
    }

    private String getReference(BbieSummaryRecord bbie, DtSummaryRecord bdt) {
        CodeListSummaryRecord codeList = generationContext.getCodeList(bbie.primitiveRestriction().codeListManifestId());
        String ref;
        if (codeList != null) {
            ref = fillSchemas(bbie, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbie.primitiveRestriction().agencyIdListManifestId());
            if (agencyIdList != null) {
                ref = fillSchemas(agencyIdList);
            } else {
                // Issue #1633
                // Primitive types shouldn't be expressed in the 'schema' content.
                return null;
            }
        }

        return ref;
    }

    private XbtSummaryRecord getXbt(BbieScSummaryRecord bbieSc, DtScSummaryRecord dtSc) {
        if (bbieSc.primitiveRestriction().xbtManifestId() == null) {
            DtScAwdPriSummaryRecord dtScAwdPri =
                    generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
            return generationContext.getXbt(dtScAwdPri.xbtManifestId());
        } else {
            return generationContext.getXbt(bbieSc.primitiveRestriction().xbtManifestId());
        }
    }

    private Map<String, Object> applyFacet(Map<String, Object> content, Facet facet) {
        if (facet != null) {
            String type = (String) content.get("type");
            boolean isTypeString = "string".equals(type);

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
        return content;
    }

    private void fillProperties(Map<String, Object> parent,
                                BbieScSummaryRecord bbieSc) {
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

        if (bieDefinition) {
            String definition = bbieSc.definition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596, #692
        XbtSummaryRecord xbt = getXbt(bbieSc, dtSc);
        dialect.bbieScKeywords(properties, bbieSc, exampleValue(xbt, bbieSc.example()));

        CodeListSummaryRecord codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillSchemas(bbieSc, codeList);
        } else {
            AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillSchemas(agencyIdList);
            } else {
                // Issue #1633
                // Primitive types shouldn't be expressed in the 'schema' content.
                ref = null;
            }
        }

        if (ref == null) {
            Map<String, Object> content = applyFacet(toProperties(xbt), bbieSc.facet());
            properties.putAll(content);
        } else {
            properties.put("$ref", ref);
        }
        properties = dialect.bbieSc(allOf(properties), bbieSc);

        // Issue #1298
        if (bbieSc.deprecated() != null && bbieSc.deprecated()) {
            properties.put("deprecated", true);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }
}
