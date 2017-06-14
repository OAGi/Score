package org.oagi.srt.persistence.populate;

import org.apache.commons.lang.StringUtils;
import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.oagi.srt.common.ImportConstants.DATA_TYPES_PATH;
import static org.oagi.srt.common.ImportConstants.OAGIS_RELEASE_NOTE;
import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;
import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

@Component
public class P_1_1_PopulateCommonData {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    private User oagisUser;

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_1_PopulateCommonData populateCommonData = ctx.getBean(P_1_1_PopulateCommonData.class);
            populateCommonData.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.1 Start");

        oagisUser = populateUser();
        Namespace namespace = populateNamespace();
        Release release = populateRelease(namespace);
        populateXbtFromXMLSchemaBuiltInTypes();
        populateXbtFromOAGISDataTypes(
                new File(DATA_TYPES_PATH, "XMLSchemaBuiltinType_1_patterns.xsd"),
                new File(DATA_TYPES_PATH, "XMLSchemaBuiltinType_1.xsd"));
        populateCdtPri();
        populateCdt(release);

        logger.info("### 1.1 End");
    }

    private User populateUser() {
        printTitle("Populate OAGIS user");

        User user = new User();
        user.setLoginId("oagis");
        user.setPassword("$2a$10$pp.KvtaKOMjsKtH407o9o.csMj.JXsysm.VBuxC6bYQzq20OFerdS");
        user.setName("Open Applications Group Developer");
        user.setOrganization("Open Applications Group");
        user.setOagisDeveloperIndicator(true);

        return userRepository.saveAndFlush(user);
    }

    private Namespace populateNamespace() throws ParseException {
        printTitle("Populate OAGIS release 10 namespace");

        Namespace namespace = new Namespace();
        namespace.setUri("http://www.openapplications.org/oagis/10");
        namespace.setPrefix("");
        namespace.setDescription("OAGIS release 10 namespace");
        namespace.setStdNmsp(true);
        namespace.setOwnerUserId(oagisUser.getAppUserId());
        namespace.setCreatedBy(oagisUser.getAppUserId());
        namespace.setLastUpdatedBy(oagisUser.getAppUserId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
        namespace.setCreationTimestamp(simpleDateFormat.parse("2014-06-27 00:00:00 -05:00"));
        namespace.setLastUpdateTimestamp(simpleDateFormat.parse("2014-06-27 00:00:00 -05:00"));

        return namespaceRepository.saveAndFlush(namespace);
    }

    private Release populateRelease(Namespace namespace) {
        printTitle("Populate OAGIS release " + OAGIS_VERSION);

        Release release = new Release();
        release.setReleaseNum(Double.toString(OAGIS_VERSION));
        release.setNamespaceId(namespace.getNamespaceId());
        release.setReleaseNote(OAGIS_RELEASE_NOTE);

        return releaseRepository.saveAndFlush(release);
    }

    private class XBTBuilder {
        private String name;
        private String builtInType;
        private String schemaDefinition;
        private XSDBuiltInType subTypeXbt;

        private Element element;
        private XSDBuiltInType xbt;

        public XBTBuilder name(String name) {
            this.name = name;
            return this;
        }

        public XBTBuilder builtInType(String builtInType) {
            this.builtInType = builtInType;
            return this;
        }

        public XBTBuilder schemaDefinition(String schemaDefinition) {
            this.schemaDefinition = schemaDefinition;
            return this;
        }

        public XBTBuilder subTypeOfXbt(XSDBuiltInType subTypeXbt) {
            this.subTypeXbt = subTypeXbt;
            return this;
        }

        public XBTBuilder element(Element element) {
            this.element = element;
            return this;
        }

        public XSDBuiltInType build() {
            xbt = new XSDBuiltInType();
            xbt.setName(name);
            xbt.setBuiltInType(builtInType);
            xbt.setSchemaDefinition(schemaDefinition);
            if (subTypeXbt != null) {
                xbt.setSubtypeOfXbtId(subTypeXbt.getXbtId());
            }
            xbt.setState(CoreComponentState.Published);
            xbt.addPersistEventListener(new CreatorModifierAwareEventListener(oagisUser));
            xbt.setOwnerUserId(oagisUser.getAppUserId());
            xbt = xbtRepository.saveAndFlush(xbt);
            return xbt;
        }
    }

    private void populateXbtFromXMLSchemaBuiltInTypes() {
        printTitle("Populate XSD Built-In Types");

        XSDBuiltInType anyType = xbtName("any type").builtInType("xsd:anyType").build();
        XSDBuiltInType anySimpleType = xbtName("any simple type").builtInType("xsd:anySimpleType").subTypeOfXbt(anyType).build();
        xbtName("duration").builtInType("xsd:duration").subTypeOfXbt(anySimpleType).build();
        xbtName("date time").builtInType("xsd:dateTime").subTypeOfXbt(anySimpleType).build();
        xbtName("time").builtInType("xsd:time").subTypeOfXbt(anySimpleType).build();
        xbtName("date").builtInType("xsd:date").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian year month").builtInType("xsd:gYearMonth").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian year").builtInType("xsd:gYear").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian month day").builtInType("xsd:gMonthDay").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian day").builtInType("xsd:gDay").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian month").builtInType("xsd:gMonth").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType stringType = xbtName("string").builtInType("xsd:string").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType normalizedStringType = xbtName("normalized string").builtInType("xsd:normalizedString").subTypeOfXbt(stringType).build();
        xbtName("token").builtInType("xsd:token").subTypeOfXbt(normalizedStringType).build();
        XSDBuiltInType booleanType = xbtName("boolean").builtInType("xsd:boolean").subTypeOfXbt(anySimpleType).build();
        xbtName("base64 binary").builtInType("xsd:base64Binary").subTypeOfXbt(anySimpleType).build();
        xbtName("hex binary").builtInType("xsd:hexBinary").subTypeOfXbt(anySimpleType).build();
        xbtName("float").builtInType("xsd:float").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType decimalType = xbtName("decimal").builtInType("xsd:decimal").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType integerType = xbtName("integer").builtInType("xsd:integer").subTypeOfXbt(decimalType).build();
        XSDBuiltInType nonNegativeIntegerType = xbtName("non negative integer").builtInType("xsd:nonNegativeInteger").subTypeOfXbt(integerType).build();
        xbtName("positive integer").builtInType("xsd:positiveInteger").subTypeOfXbt(nonNegativeIntegerType).build();
        xbtName("double").builtInType("xsd:double").subTypeOfXbt(anySimpleType).build();
        xbtName("any uri").builtInType("xsd:anyURI").subTypeOfXbt(anySimpleType).build();
    }

    public XBTBuilder xbtName(String name) {
        XBTBuilder xbtBuilder = new XBTBuilder();
        xbtBuilder.name(name);
        return xbtBuilder;
    }

    private void populateXbtFromOAGISDataTypes(File ... typeFiles) {
        printTitle("Populate XSD Built-In Types from '" + Arrays.stream(typeFiles).map(e -> e.getName()) + "'");

        Map<String, XBTBuilder> xbtMap = new HashMap();

        for (File typeFile : typeFiles) {
            Document document = Context.loadDocument(typeFile);
            NodeList nodeList;
            try {
                nodeList = (NodeList) Context.xPath.evaluate("//xsd:simpleType", document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }

            for (int i = 0, len = nodeList.getLength(); i < len; ++i) {
                Element simpleTypeElement = (Element) nodeList.item(i);
                String builtInType = simpleTypeElement.getAttribute("name");
                if (xbtMap.containsKey(builtInType)) {
                    continue;
                }
                String name = normalize(builtInType);
                String schemaDefinition = toString(simpleTypeElement.getChildNodes());

                XBTBuilder xbtBuilder =
                        xbtName(name).builtInType(builtInType)
                                .schemaDefinition(schemaDefinition)
                                .element(simpleTypeElement);
                xbtMap.put(builtInType, xbtBuilder);
                xbtBuilder.build();
            }
        }

        for (Map.Entry<String, XBTBuilder> entry : xbtMap.entrySet()) {
            XBTBuilder xbtBuilder = entry.getValue();

            String base;
            try {
                base = (String) Context.xPath.evaluate(".//xsd:restriction/@base", xbtBuilder.element, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }

            XSDBuiltInType xbt = xbtBuilder.xbt;
            xbt.afterLoaded();

            if (!StringUtils.isEmpty(base)) {
                if (base.equals("xsd:boolean") || base.equals("xsd:date") || base.equals("xsd:time") || base.equals("xsd:dateTime")) {
                    XSDBuiltInType baseXbt = xbtRepository.findOneByBuiltInType(base);
                    if (baseXbt == null) {
                        throw new IllegalStateException();
                    }
                    xbt.setSubtypeOfXbtId(baseXbt.getXbtId());
                }
            }

            if (xbt.getSubtypeOfXbtId() == 0L) {
                String builtinType = entry.getKey();
                if (builtinType.contains("Duration")) {
                    XSDBuiltInType baseXbt = xbtRepository.findOneByBuiltInType("xsd:duration");
                    xbt.setSubtypeOfXbtId(baseXbt.getXbtId());
                } else if (builtinType.contains("Hour") || builtinType.contains("Minute") || builtinType.contains("Second")) {
                    if (builtinType.contains("Date") || builtinType.contains("Day")) {
                        XSDBuiltInType baseXbt = xbtRepository.findOneByBuiltInType("xsd:time");
                        xbt.setSubtypeOfXbtId(baseXbt.getXbtId());
                    }
                } else if (builtinType.contains("Hour") || builtinType.contains("Time")) {
                    XSDBuiltInType baseXbt = xbtRepository.findOneByBuiltInType("xsd:dateTime");
                    xbt.setSubtypeOfXbtId(baseXbt.getXbtId());
                }
            }

            if (xbt.getSubtypeOfXbtId() == 0L) {
                XSDBuiltInType baseXbt = xbtRepository.findOneByBuiltInType("xsd:date");
                xbt.setSubtypeOfXbtId(baseXbt.getXbtId());
            }

            if (xbt.isDirty()) {
                xbtRepository.save(xbt);
            }
        }
    }

    public String normalize(String xbtName) {
        xbtName = StringUtils.trim(xbtName);
        if (StringUtils.isEmpty(xbtName)) {
            return null;
        }

        if (xbtName.endsWith("Type")) {
            xbtName = xbtName.substring(0, xbtName.length() - "Type".length());
        }

        xbtName = xbtName.replaceAll("_", " ");
        xbtName = xbtName.replaceAll("([A-Z]+)", " $1");
        xbtName = xbtName.replaceAll("([A-Z][a-z])", " $1");

        StringTokenizer tokenizer = new StringTokenizer(xbtName, " ");
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (StringUtils.isAllUpperCase(token)) {
                sb.append(token);
            } else {
                sb.append(token.toLowerCase());
            }
            sb.append(" ");
        }

        return StringUtils.trim(sb.toString());
    }

    private String toString(NodeList nodeList) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = nodeList.getLength(); i < len; ++i) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            DOMSource domSource = new DOMSource(node);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            try {
                transformer.transform(domSource, result);
            } catch (TransformerException e) {
                throw new IllegalStateException(e);
            }

            writer.flush();

            String str = StringUtils.trim(writer.toString());
            // Eliminate definitions of the namespace
            str = str.replaceAll("[\\W]+xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", "")
                     .replaceAll("[\\W]+xmlns=\"http://www.openapplications.org/oagis/10\"", "")
                     .replaceAll("([\t]+)", "\t")
                     .replaceAll("[\t]</", "</");
            sb.append(str);

            if ((i + 1) != len) {
                sb.append("\n");
            }
        }

        return StringUtils.trim(sb.toString());
    }

    public void populateCdtPri() {
        printTitle("Populate CDT Primitive");

        cdtPriRepository.saveAndFlush(cdtPri("Binary"));
        cdtPriRepository.saveAndFlush(cdtPri("Boolean"));
        cdtPriRepository.saveAndFlush(cdtPri("Decimal"));
        cdtPriRepository.saveAndFlush(cdtPri("Double"));
        cdtPriRepository.saveAndFlush(cdtPri("Float"));
        cdtPriRepository.saveAndFlush(cdtPri("Integer"));
        cdtPriRepository.saveAndFlush(cdtPri("NormalizedString"));
        cdtPriRepository.saveAndFlush(cdtPri("String"));
        cdtPriRepository.saveAndFlush(cdtPri("TimeDuration"));
        cdtPriRepository.saveAndFlush(cdtPri("TimePoint"));
        cdtPriRepository.saveAndFlush(cdtPri("Token"));
    }

    private CoreDataTypePrimitive cdtPri(String name) {
        CoreDataTypePrimitive cdtPri = new CoreDataTypePrimitive();
        cdtPri.setName(name);
        return cdtPri;
    }

    public void populateCdt(Release release) {
        printTitle("Populate CDT");

        new CDTBuilder(oagisUser, release).dataTypeTerm("Amount").den("Amount. Type")
                .contentComponentDen("Amount. Content")
                .definition("CDT V3.1. An amount is a number of monetary units specified in a currency.")
                .contentComponentDefinition("A number of monetary units.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Binary Object").den("Binary Object. Type")
                .contentComponentDen("Binary Object. Content")
                .definition("CDT V3.1. A binary object is a sequence of binary digits (bits).")
                .contentComponentDefinition("A finite sequence of binary digits (bits).").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Code").den("Code. Type")
                .contentComponentDen("Code. Content")
                .definition("CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences); and symbols. It represents a definitive value,\na method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.")
                .contentComponentDefinition("A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Date").den("Date. Type")
                .contentComponentDen("Date. Content")
                .definition("CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.")
                .contentComponentDefinition("The particular point in the progression of date.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Date Time").den("Date Time. Type")
                .contentComponentDen("Date Time. Content")
                .definition("CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of\nsecond.")
                .contentComponentDefinition("The particular date and time point in the progression of time.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Duration").den("Duration. Type")
                .contentComponentDen("Duration. Content")
                .definition("CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,\nWeek, Day) and Hours, Minutes or Seconds.")
                .contentComponentDefinition("The particular representation of duration.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Graphic").den("Graphic. Type")
                .contentComponentDen("Graphic. Content")
                .definition("CDT V3.1. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for graphics.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Identifier").den("Identifier. Type")
                .contentComponentDen("Identifier. Content")
                .definition("CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an\nagency.")
                .contentComponentDefinition("A character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Indicator").den("Indicator. Type")
                .contentComponentDen("Indicator. Content")
                .definition("CDT V3.1. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.")
                .contentComponentDefinition("The value of the Indicator.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Measure").den("Measure. Type")
                .contentComponentDen("Measure. Content")
                .definition("CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.")
                .contentComponentDefinition("The numeric value determined by measuring an object.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Name").den("Name. Type")
                .contentComponentDen("Name. Content")
                .definition("CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.")
                .contentComponentDefinition("A word or phrase that represents a designation of a person, place, thing or concept.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Number").den("Number. Type")
                .contentComponentDen("Number. Content")
                .definition("CDT V3.1. A mathematical number that is assigned or is determined by calculation.")
                .contentComponentDefinition("Mathematical number that is assigned or is determined by calculation.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Ordinal").den("Ordinal. Type")
                .contentComponentDen("Ordinal. Content")
                .definition("CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.")
                .contentComponentDefinition("An assigned mathematical number that represents order or sequence").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Percent").den("Percent. Type")
                .contentComponentDen("Percent. Content")
                .definition("CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.")
                .contentComponentDefinition("Numeric information that is assigned or is determined by percent.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Picture").den("Picture. Type")
                .contentComponentDen("Picture. Content")
                .definition("CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for pictures.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Quantity").den("Quantity. Type")
                .contentComponentDen("Quantity. Content")
                .definition("CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.")
                .contentComponentDefinition("A counted number of non-monetary units possibly including fractions.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Rate").den("Rate. Type")
                .contentComponentDen("Rate. Content")
                .definition("CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.")
                .contentComponentDefinition("The numerical value of the rate.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Ratio").den("Ratio. Type")
                .contentComponentDen("Ratio. Content")
                .definition("CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a\nquotient showing the number of times one value contains or is contained within the other, or as a proportion.")
                .contentComponentDefinition("The quotient or proportion between two independent quantities of the same unit of measure or currency.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Sound").den("Sound. Type")
                .contentComponentDen("Sound. Content")
                .definition("CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for sounds.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Text").den("Text. Type")
                .contentComponentDen("Text. Content")
                .definition("CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.")
                .contentComponentDefinition("A character string generally in the form of words of a language.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Time").den("Time. Type")
                .contentComponentDen("Time. Content")
                .definition("CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.")
                .contentComponentDefinition("The particular point in the progression of time.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Value").den("Value. Type")
                .contentComponentDen("Value. Content")
                .definition("CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.")
                .contentComponentDefinition("Numeric information that is assigned or is determined by value.").build();

        new CDTBuilder(oagisUser, release).dataTypeTerm("Video").den("Video. Type")
                .contentComponentDen("Video. Content")
                .definition("CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for videos.").build();
    }

    private class CDTBuilder {

        private DataType cdt;

        public CDTBuilder(User user, Release release) {
            cdt = new DataType();
            cdt.setGuid(Utility.generateGUID());
            cdt.setType(DataTypeType.CoreDataType);
            cdt.setVersionNum("1.0");
            cdt.setState(CoreComponentState.Published);
            cdt.setOwnerUserId(user.getAppUserId());
            cdt.setCreatedBy(user.getAppUserId());
            cdt.setLastUpdatedBy(user.getAppUserId());
            cdt.setReleaseId(release.getReleaseId());
            cdt.setRevisionNum(0);
            cdt.setRevisionTrackingNum(0);
            cdt.setDeprecated(false);
        }

        public CDTBuilder dataTypeTerm(String dataTypeTerm) {
            cdt.setDataTypeTerm(dataTypeTerm);
            return this;
        }

        public CDTBuilder den(String den) {
            cdt.setDen(den);
            return this;
        }

        public CDTBuilder contentComponentDen(String contentComponentDen) {
            cdt.setContentComponentDen(contentComponentDen);
            return this;
        }

        public CDTBuilder definition(String definition) {
            cdt.setDefinition(definition);
            return this;
        }

        public CDTBuilder contentComponentDefinition(String contentComponentDefinition) {
            cdt.setContentComponentDefinition(contentComponentDefinition);
            return this;
        }

        public DataType build() {
            return dataTypeRepository.saveAndFlush(cdt);
        }
    }
}
