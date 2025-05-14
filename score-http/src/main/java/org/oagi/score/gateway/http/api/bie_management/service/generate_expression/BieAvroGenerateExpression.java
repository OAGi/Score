package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.common.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.toName;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieAvroGenerateExpression implements BieGenerateExpression, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper mapper;

    private AvroObject root;
    private GenerateExpressionOption option;

    @Autowired
    private ApplicationContext applicationContext;

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
    public GenerationContext generateContext(
            ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) {
        List<TopLevelAsbiepSummaryRecord> mergedTopLevelAsbieps = new ArrayList(topLevelAsbieps);

        if (mergedTopLevelAsbieps.size() == 0) {
            throw new IllegalArgumentException("Cannot found BIEs.");
        }

        return applicationContext.getBean(GenerationContext.class, requester, mergedTopLevelAsbieps);
    }

    @Override
    public void generate(
            ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
            fillProperties(asbiep, typeAbie, generationContext);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void fillProperties(AsbiepSummaryRecord asbiep, AbieSummaryRecord abie,
                                GenerationContext generationContext) {

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = Utility.first(asccp.den(), true);
        NamespaceSummaryRecord namespaceSummary = generationContext.findNamespace(asccp.namespaceId());
        String namespace = null;
        if (namespaceSummary != null) {
            URI uri;
            try {
                uri = new URI(namespaceSummary.uri());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid URI in the namespace: " + namespaceSummary.uri(), e);
            }

            List<String> tokensInHostname = Arrays.asList(uri.getHost().replace("www.", "").split("[\\.]"));
            Collections.reverse(tokensInHostname);
            namespace = String.join(".", tokensInHostname);
        }

        root = new AvroObject();
        root.setNamespace(namespace);
        root.setType("record");
        root.setName(name);
        root.setDocumentation(asbiep.definition());

        fillProperties(root, abie, generationContext);
    }

    private void fillProperties(AvroObject parent,
                                AbieSummaryRecord abie,
                                GenerationContext generationContext) {

        List<BIE> children = generationContext.queryChildBIEs(abie);
        for (BIE bie : children) {
            if (bie instanceof BbieSummaryRecord) {
                BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
                fillProperties(parent, bbie, generationContext);
            } else {
                AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;

                if (Helper.isAnyProperty(asbie, generationContext)) {
                    AvroObject anyPropertyObj = new AvroObject(parent);
                    anyPropertyObj.setName("anyProperty");
                    anyPropertyObj.setType("bytes");
                } else {
                    AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);

                    generationContext.referenceCounter().increase(asbiep)
                            .ifNotCircularReference(asbiep,
                                    () -> fillProperties(parent, asbie, generationContext))
                            .decrease(asbiep);
                }
            }
        }
    }

    private void fillProperties(AvroObject parent,
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
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = bbie.nillable();

        boolean isAttribute = bcc.entityType() == EntityType.Attribute;
        String name = Utility.second(bcc.den(), !isAttribute);
        List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie)
                .stream().filter(e -> e.cardinality().max() != 0).collect(Collectors.toList());

        AvroObject avroObj = new AvroObject(parent);
        String type = getType(bbie, generationContext);
        if (!bbieScList.isEmpty()) {
            AvroObject contentObj = new AvroObject(avroObj);
            contentObj.setType(type);
            contentObj.setName("content");

            for (BbieScSummaryRecord bbieSc : bbieScList) {
                fillProperties(avroObj, bbieSc, generationContext);
            }

            type = "record";
        }

        avroObj.setType(type);
        avroObj.setName(name);
        avroObj.setDocumentation(bbie.definition());
        avroObj.setArray(isArray);
        avroObj.setNullable(isNillable || minVal == 0);
    }

    private String getType(BbieSummaryRecord bbie, GenerationContext generationContext) {
        XbtSummaryRecord xbt;
        if (bbie.primitiveRestriction().xbtManifestId() == null) {
            DtAwdPriSummaryRecord dtAwdPri =
                    generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);
            xbt = Helper.getXbt(generationContext, dtAwdPri);
        } else {
            xbt = generationContext.getXbt(bbie.primitiveRestriction().xbtManifestId());
        }

        return (String) toProperties(xbt).get("type");
    }

    private Map<String, Object> toProperties(XbtSummaryRecord xbt) {
        String avroMap = xbt.avroMap();
        try {
            return mapper.readValue(avroMap, LinkedHashMap.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fillProperties(AvroObject parent,
                                BbieScSummaryRecord bbieSc,
                                GenerationContext generationContext) {
        int minVal = bbieSc.cardinality().min();
        int maxVal = bbieSc.cardinality().max();
        if (maxVal == 0) {
            return;
        }

        DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
        String name = toName(dtSc.propertyTerm(), dtSc.representationTerm(), rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            if ("Identifier".equals(rt)) {
                return "ID";
            }
            return rt;
        }, false);

        String type = getType(bbieSc, generationContext);

        AvroObject avroObj = new AvroObject(parent);
        avroObj.setName(name);
        avroObj.setDocumentation(bbieSc.definition());
        avroObj.setType(type);
        avroObj.setNullable(minVal == 0);
    }

    private String getType(BbieScSummaryRecord bbieSc, GenerationContext generationContext) {
        XbtSummaryRecord xbt;
        if (bbieSc.primitiveRestriction().xbtManifestId() == null) {
            DtScAwdPriSummaryRecord dtScAwdPri =
                    generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
            xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());
        } else {
            xbt = generationContext.getXbt(bbieSc.primitiveRestriction().xbtManifestId());
        }

        return (String) toProperties(xbt).get("type");
    }

    private void fillProperties(AvroObject parent,
                                AsbieSummaryRecord asbie,
                                GenerationContext generationContext) {

        AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        String name = Utility.first(asccp.den(), true);

        int minVal = asbie.cardinality().min();
        int maxVal = asbie.cardinality().max();
        // Issue #562
        boolean isArray = (maxVal < 0 || maxVal > 1);
        boolean isNillable = asbie.nillable();

        boolean reused = !asbie.ownerTopLevelAsbiepId().equals(asbiep.ownerTopLevelAsbiepId());
        AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);
        AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

        AvroObject avroObj = new AvroObject(parent);
        String type = "record";
        fillProperties(avroObj, typeAbie, generationContext);

        avroObj.setType(type);
        avroObj.setName(name);
        avroObj.setDocumentation(asbie.definition());
        avroObj.setArray(isArray);
        avroObj.setNullable(isNillable || minVal == 0);
    }

    private void ensureRoot() {
        if (root == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        ensureRoot();

        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".avsc");

        mapper.writeValue(tempFile, root.toProperties());
        logger.info("Avro Schema is generated: " + tempFile);

        return tempFile;
    }

    private class AvroObject {

        private AvroObject parent;

        private String type;

        private String name;

        private String recordName;

        private String namespace;

        private String documentation;

        private boolean array;

        private boolean nullable;

        private List<AvroObject> children = new ArrayList<>();

        public AvroObject() {
            this(null);
        }

        public AvroObject(AvroObject parent) {
            setParent(parent);
            if (parent != null) {
                parent.addChild(this);
            }
        }

        public AvroObject getParent() {
            return parent;
        }

        public void setParent(AvroObject parent) {
            this.parent = parent;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRecordName() {
            if (recordName == null) {
                if (this.getParent() != null) {
                    recordName = this.getParent().getRecordName() + getName();
                } else {
                    recordName = getName();
                }
            }
            return recordName;
        }

        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            this.documentation = documentation;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public boolean isRecord() {
            return "record".equals(this.getType());
        }

        public List<AvroObject> getChildren() {
            return children;
        }

        public void addChild(AvroObject child) {
            this.children.add(child);
        }

        public Map<String, Object> toProperties() {
            Map<String, Object> properties = new LinkedHashMap<>();
            if (getNamespace() != null) {
                properties.put("namespace", getNamespace());
            }
            properties.put("type", getType());

            boolean isRecord = isRecord();
            boolean isArray = isArray();
            boolean isNullable = isNullable();
            if (isRecord) {
                properties.put("name", getRecordName());
            } else {
                properties.put("name", getName());
            }
            if (option.isBieDefinition() && StringUtils.hasLength(getDocumentation())) {
                properties.put("doc", getDocumentation());
            }
            if (isRecord) {
                properties.put("fields", getChildren().stream()
                        .map(e -> e.toProperties()).collect(Collectors.toList()));
            }

            if (isArray) {
                Map<String, Object> arrayProperties = new LinkedHashMap<>();
                arrayProperties.put("type", "array");
                arrayProperties.put("name", getName());
                arrayProperties.put("items", properties);
                properties = arrayProperties;
            }

            boolean isRoot = this.parent == null;
            if (!isRoot) {
                if (isRecord || isArray) {
                    Map<String, Object> wrappedProperties = new LinkedHashMap<>();
                    wrappedProperties.put("name", getName());
                    wrappedProperties.put("type", (isNullable) ? Arrays.asList("null", properties) : properties);
                    properties = wrappedProperties;
                } else if (isNullable) {
                    properties.put("type", Arrays.asList("null", properties.get("type")));
                }
            }

            return properties;
        }
    }
}
