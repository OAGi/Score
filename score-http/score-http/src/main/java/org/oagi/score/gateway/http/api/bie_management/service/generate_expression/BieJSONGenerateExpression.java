package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.oagi.score.common.util.OagisComponentType;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repository.TopLevelAsbiepRepository;
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
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.camelCase;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.convertIdentifierToId;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieJSONGenerateExpression implements BieGenerateExpression, InitializingBean {

    private static final String ID_KEYWORD = "$id";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper mapper;

    private Map<String, Object> root;
    private GenerateExpressionOption option;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    private GenerationContext generationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = null;
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    @Override
    public GenerationContext generateContext(List<TopLevelAsbiep> topLevelAsbieps, GenerateExpressionOption option) {
        List<TopLevelAsbiep> mergedTopLevelAsbieps = new ArrayList(topLevelAsbieps);

        if (mergedTopLevelAsbieps.size() == 0) {
            throw new IllegalArgumentException("Can not found BIEs.");
        }
        BigInteger releaseId = mergedTopLevelAsbieps.get(0).getReleaseId();

        /* Issue 587 */
        if (option.isIncludeMetaHeaderForJson()) {
            TopLevelAsbiep metaHeaderTopLevelAsbiep =
                    topLevelAsbiepRepository.findById(option.getMetaHeaderTopLevelAsbiepId());
            if (!releaseId.equals(metaHeaderTopLevelAsbiep.getReleaseId())) {
                throw new IllegalArgumentException("Meta Header release does not match.");
            }
            mergedTopLevelAsbieps.add(metaHeaderTopLevelAsbiep);
        }
        if (option.isIncludePaginationResponseForJson()) {
            TopLevelAsbiep paginationResponseTopLevelAsbiep =
                    topLevelAsbiepRepository.findById(option.getPaginationResponseTopLevelAsbiepId());
            if (!releaseId.equals(paginationResponseTopLevelAsbiep.getReleaseId())) {
                throw new IllegalArgumentException("Pagination Response release does not match.");
            }
            mergedTopLevelAsbieps.add(paginationResponseTopLevelAsbiep);
        }

        return applicationContext.getBean(GenerationContext.class, mergedTopLevelAsbieps);
    }

    @Override
    public void generate(TopLevelAsbiep topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private void generateTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            ABIE typeAbie = generationContext.queryTargetABIE(asbiep);

            Map<String, Object> definitions;
            if (root == null) {
                root = new LinkedHashMap();
                root.put("$schema", "http://json-schema.org/draft-04/schema#");

                root.put("required", new ArrayList());
                root.put("additionalProperties", false);

                Map<String, Object> properties = new LinkedHashMap();
                root.put("properties", properties);
                definitions = new LinkedHashMap();
                root.put("definitions", definitions);
            } else {
                definitions = (Map<String, Object>) root.get("definitions");
            }

            /*
             * Issue #587
             */
            if (option.isIncludeMetaHeaderForJson()) {
                TopLevelAsbiep metaHeaderTopLevelAsbiep =
                        topLevelAsbiepRepository.findById(option.getMetaHeaderTopLevelAsbiepId());
                fillProperties(root, definitions, metaHeaderTopLevelAsbiep, generationContext, false);
            }
            if (option.isIncludePaginationResponseForJson()) {
                TopLevelAsbiep paginationResponseTopLevelAsbiep =
                        topLevelAsbiepRepository.findById(option.getPaginationResponseTopLevelAsbiepId());
                fillProperties(root, definitions, paginationResponseTopLevelAsbiep, generationContext, false);
            }

            fillProperties(root, definitions, asbiep, typeAbie, generationContext);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                TopLevelAsbiep topLevelAsbiep,
                                GenerationContext generationContext,
                                boolean isArray) {

        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        ABIE typeAbie = generationContext.queryTargetABIE(asbiep);

        fillProperties(parent, definitions, asbiep, typeAbie, generationContext, isArray);
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ASBIE asbie,
                                GenerationContext generationContext) {
        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        ABIE typeAbie = generationContext.queryTargetABIE(asbiep);

        ASCC ascc = generationContext.queryBasedASCC(asbie);

        int minVal = asbie.getCardinalityMin();
        int maxVal = asbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = asbie.isNillable();

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.getPropertyTerm()));
        if (minVal > 0) {
            List<String> parentRequired = (List<String>) parent.get("required");
            if (parentRequired == null) {
                throw new IllegalStateException();
            }
            parentRequired.add(name);
        }

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = asbie.getDefinition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        properties.put("type", "object");
        properties.put("required", new ArrayList());
        properties.put("additionalProperties", false);
        properties.put("properties", new LinkedHashMap<String, Object>());

        fillProperties(properties, definitions, typeAbie, generationContext);

        if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        properties = oneOf(allOf(properties), isNillable);

        if (isArray) {
            Map<String, Object> items = new LinkedHashMap();
            items.putAll(properties);

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
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ASBIEP asbiep, ABIE abie,
                                GenerationContext generationContext) {
        fillProperties(parent, definitions, asbiep, abie, generationContext, option.isArrayForJsonExpression());
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ASBIEP asbiep, ABIE abie,
                                GenerationContext generationContext,
                                boolean isArray) {

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        String name = convertIdentifierToId(camelCase(asccp.getPropertyTerm()));

        List<String> parentRequired = (List<String>) parent.get("required");
        parentRequired.add(name);

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = abie.getDefinition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
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

        if (properties.containsKey("required") && ((List) properties.get("required")).isEmpty()) {
            properties.remove("required");
        }

        /*
         * Issue #575
         */
        if (isArray) {
            Map<String, Object> items = new LinkedHashMap(properties);
            properties = new LinkedHashMap();

            String description = (String) items.remove("description");
            if (StringUtils.hasLength(description)) {
                properties.put("description", description);
            }
            properties.put("type", "array");
            properties.put("items", items);
        }

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private Map<String, Object> toProperties(Xbt xbt) {
        String jbtDraft05Map = xbt.getJbtDraft05Map();
        try {
            return mapper.readValue(jbtDraft05Map, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   Xbt xbt, FacetRestrictionsAware facetRestri) {
        String guid = (facetRestri instanceof BIE) ? ((BIE) facetRestri).getGuid() : ScoreGuid.randomGuid();
        String name = "type_" + guid;

        Map<String, Object> content = toProperties(xbt);
        if (facetRestri.getFacetMinLength() != null) {
            content.put("minLength", facetRestri.getFacetMinLength().longValue());
        }
        if (facetRestri.getFacetMaxLength() != null) {
            content.put("maxLength", facetRestri.getFacetMaxLength().longValue());
        }
        if (StringUtils.hasLength(facetRestri.getFacetPattern())) {
            // Override 'pattern' and 'format' properties
            content.remove("pattern");
            content.remove("format");
            content.put("pattern", facetRestri.getFacetPattern());
        }

        definitions.put(name, content);

        return "#/definitions/" + name;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   Xbt xbt) {
        String builtInType = xbt.getBuiltinType();
        if (builtInType.startsWith("xsd:")) {
            builtInType = builtInType.substring(4);
        }
        if (!definitions.containsKey(builtInType)) {
            Map<String, Object> content = toProperties(xbt);
            definitions.put(builtInType, content);
        }

        return "#/definitions/" + builtInType;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BBIE bbie, CodeList codeList) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);

        Map<String, Object> properties;
        if (bdtPriRestri.getCodeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   BBIESC bbieSc, CodeList codeList) {
        BdtScPriRestri bdtScPriRestri =
                generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);

        Map<String, Object> properties;
        if (bdtScPriRestri.getCodeListManifestId() != null) {
            properties = new LinkedHashMap();
            properties.put("type", "string");
        } else {
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
            properties = toProperties(xbt);
        }

        return fillDefinitions(properties, definitions, codeList);
    }

    private String fillDefinitions(Map<String, Object> properties,
                                   Map<String, Object> definitions,
                                   CodeList codeList) {

        AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
        String codeListName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        /*
         * Issue #589
         */
        codeListName = Stream.of(codeListName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));

        if (!definitions.containsKey(codeListName)) {
            List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
            List<String> enumerations = codeListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(codeListName, properties);
        }

        return "#/definitions/" + codeListName;
    }

    private String fillDefinitions(Map<String, Object> definitions,
                                   AgencyIdList agencyIdList) {
        AgencyIdListValue agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueManifestId());
        String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);
        /*
         * Issue #589
         */
        agencyListTypeName = Stream.of(agencyListTypeName.split("_"))
                .map(e -> convertIdentifierToId(camelCase(e))).collect(Collectors.joining("_"));
        if (!definitions.containsKey(agencyListTypeName)) {
            Map<String, Object> properties = new LinkedHashMap();
            properties.put("type", "string");

            List<AgencyIdListValue> agencyIdListValues =
                    generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.getAgencyIdListManifestId());
            List<String> enumerations = agencyIdListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
            if (!enumerations.isEmpty()) {
                properties.put("enum", enumerations);
            }

            definitions.put(agencyListTypeName, properties);
        }

        return "#/definitions/" + agencyListTypeName;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                ABIE abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        ACC acc = generationContext.findACC(abie.getBasedAccManifestId());
        if (OagisComponentType.Choice.getValue() == acc.getOagisComponentType()) {
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
        if (bie instanceof BBIE) {
            BBIE bbie = (BBIE) bie;
            fillProperties(parent, definitions, bbie, generationContext);
        } else {
            ASBIE asbie = (ASBIE) bie;
            if (Helper.isAnyProperty(asbie, generationContext)) {
                parent.put("additionalProperties", true);
            } else {
                ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);

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

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BBIE bbie,
                                GenerationContext generationContext) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        BCCP bccp = generationContext.queryToBCCP(bcc);
        if (bccp == null) {
            throw new IllegalStateException();
        }
        DT bdt = generationContext.queryBDT(bccp);

        int minVal = bbie.getCardinalityMin();
        int maxVal = bbie.getCardinalityMax();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = bbie.isNillable();

        String name = convertIdentifierToId(camelCase(bccp.getPropertyTerm()));

        Map<String, Object> properties = new LinkedHashMap();
        if (!parent.containsKey("properties")) {
            parent.put("properties", new LinkedHashMap<String, Object>());
        }

        if (option.isBieDefinition()) {
            String definition = bbie.getDefinition();
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

        // Issue #596
        if (StringUtils.hasLength(bbie.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbie.getFixedValue()));
        } else if (StringUtils.hasLength(bbie.getDefaultValue())) {
            properties.put("default", bbie.getDefaultValue());
        }

        // Issue #692
        String exampleText = bbie.getExample();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        // Issue #564
        String ref = getReference(definitions, bbie, bdt, generationContext);
        List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.getCardinalityMax() != 0).collect(Collectors.toList());
        if (bbieScList.isEmpty()) {
            properties.put("$ref", ref);
            properties = oneOf(allOf(properties), isNillable);
        } else {
            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            Map<String, Object> contentProperties = new LinkedHashMap();
            contentProperties.put("$ref", ref);
            for (String key : Arrays.asList("enum", "default", "examples")) {
                if (properties.containsKey(key)) {
                    contentProperties.put(key, properties.remove(key));
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", oneOf(allOf(contentProperties), isNillable));

            for (BBIESC bbieSc : bbieScList) {
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

    private Map<String, Object> oneOf(Map<String, Object> properties,
                                      boolean isNillable) {
        if (isNillable) {
            Map<String, Object> prop = new LinkedHashMap();
            prop.put("oneOf", Arrays.asList(
                    ImmutableMap.builder()
                            .put("type", "null")
                            .build(),
                    properties
            ));

            return prop;
        }

        return properties;
    }

    private String getReference(Map<String, Object> definitions, BBIE bbie, DT bdt,
                                GenerationContext generationContext) {
        CodeList codeList = Helper.getCodeList(generationContext, bbie, bdt);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbie, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                Xbt xbt;
                if (bbie.getBdtPriRestriId() == null) {
                    BdtPriRestri bdtPriRestri =
                            generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);
                    xbt = Helper.getXbt(generationContext, bdtPriRestri);
                } else {
                    BdtPriRestri bdtPriRestri =
                            generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
                    xbt = Helper.getXbt(generationContext, bdtPriRestri);
                }

                if (bbie.getFacetMinLength() != null || bbie.getFacetMaxLength() != null || StringUtils.hasLength(bbie.getFacetPattern())) {
                    ref = fillDefinitions(definitions, xbt, bbie);
                } else {
                    ref = fillDefinitions(definitions, xbt);
                }
            }
        }

        return ref;
    }

    private void fillProperties(Map<String, Object> parent,
                                Map<String, Object> definitions,
                                BBIESC bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.getCardinalityMin();
        int maxVal = bbieSc.getCardinalityMax();
        if (maxVal == 0) {
            return;
        }

        DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        String name = convertIdentifierToId(toName(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            return rt;
        }, true));
        Map<String, Object> properties = new LinkedHashMap();

        if (option.isBieDefinition()) {
            String definition = bbieSc.getDefinition();
            if (StringUtils.hasLength(definition)) {
                properties.put("description", definition);
            }
        }

        if (minVal > 0) {
            ((List<String>) parent.get("required")).add(name);
        }

        // Issue #596
        if (StringUtils.hasLength(bbieSc.getFixedValue())) {
            properties.put("enum", Arrays.asList(bbieSc.getFixedValue()));
        } else if (StringUtils.hasLength(bbieSc.getDefaultValue())) {
            properties.put("default", bbieSc.getDefaultValue());
        }

        // Issue #692
        String exampleText = bbieSc.getExample();
        if (StringUtils.hasLength(exampleText)) {
            properties.put("examples", Arrays.asList(exampleText));
        }

        CodeList codeList = generationContext.getCodeList(bbieSc);
        String ref;
        if (codeList != null) {
            ref = fillDefinitions(definitions, bbieSc, codeList);
        } else {
            AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);
            if (agencyIdList != null) {
                ref = fillDefinitions(definitions, agencyIdList);
            } else {
                Xbt xbt;
                if (bbieSc.getDtScPriRestriId() == null) {
                    BdtScPriRestri bdtScPriRestri =
                            generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);
                    CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                            generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                } else {
                    BdtScPriRestri bdtScPriRestri =
                            generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
                    CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                            generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                }

                if (bbieSc.getFacetMinLength() != null || bbieSc.getFacetMaxLength() != null || StringUtils.hasLength(bbieSc.getFacetPattern())) {
                    ref = fillDefinitions(definitions, xbt, bbieSc);
                } else {
                    ref = fillDefinitions(definitions, xbt);
                }
            }
        }

        properties.put("$ref", ref);
        properties = allOf(properties);

        ((Map<String, Object>) parent.get("properties")).put(name, properties);
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }

        // The "required" property for the root element of schema should has only one child.
        if (((List<String>) root.get("required")).size() > 1) {
            root.remove("required");
        }

        //
        Map<String, Object> properties = (Map<String, Object>) root.get("properties");
        for (String key : properties.keySet()) {
            Map<String, Object> copied = new LinkedHashMap();
            copied.putAll(((Map<String, Object>) properties.get(key)));
            properties.put(key, copied);
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(ScoreGuid.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".json");

        mapper.writeValue(tempFile, root);
        logger.info("JSON Schema is generated: " + tempFile);

        return tempFile;
    }
}
