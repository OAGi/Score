package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_1_PopulateCommonData populateCommonData = ctx.getBean(P_1_1_PopulateCommonData.class);
            populateCommonData.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.1 Start");

        User user = populateUser();
        Namespace namespace = populateNamespace(user);
        Release release = populateRelease(namespace);
        populateXbt();
        populateCdtPri();
        populateCdt(user, release);

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

        userRepository.saveAndFlush(user);

        return user;
    }

    private Namespace populateNamespace(User user) throws ParseException {
        printTitle("Populate OAGIS release 10 namespace");

        Namespace namespace = new Namespace();
        namespace.setUri("http://www.openapplications.org/oagis/10");
        namespace.setPrefix("");
        namespace.setDescription("OAGIS release 10 namespace");
        namespace.setStdNmsp(true);
        namespace.setOwnerUserId(user.getAppUserId());
        namespace.setCreatedBy(user.getAppUserId());
        namespace.setLastUpdatedBy(user.getAppUserId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
        namespace.setCreationTimestamp(simpleDateFormat.parse("2014-06-27 00:00:00 -05:00"));
        namespace.setLastUpdateTimestamp(simpleDateFormat.parse("2014-06-27 00:00:00 -05:00"));

        namespaceRepository.saveAndFlush(namespace);
        return namespace;
    }

    private Release populateRelease(Namespace namespace) {
        printTitle("Populate OAGIS release " + OAGIS_VERSION);

        Release release = new Release();
        release.setReleaseNum(OAGIS_VERSION);
        release.setNamespaceId(namespace.getNamespaceId());
        release.setReleaseNote(OAGIS_RELEASE_NOTE);

        releaseRepository.saveAndFlush(release);
        return release;
    }

    private class XBTBuilder {
        private String name;
        private String builtInType;
        private XSDBuiltInType subTypeXbt;

        public XBTBuilder name(String name) {
            this.name = name;
            return this;
        }

        public XBTBuilder builtInType(String builtInType) {
            this.builtInType = builtInType;
            return this;
        }

        public XBTBuilder subTypeOfXbt(XSDBuiltInType subTypeXbt) {
            this.subTypeXbt = subTypeXbt;
            return this;
        }

        public XSDBuiltInType build() {
            XSDBuiltInType xbt = new XSDBuiltInType();
            xbt.setName(name);
            xbt.setBuiltInType(builtInType);
            if (subTypeXbt != null) {
                xbt.setSubtypeOfXbtId(subTypeXbt.getXbtId());
            }
            xbtRepository.saveAndFlush(xbt);
            return xbt;
        }
    }

    private void populateXbt() {
        printTitle("Populate XSD Built-In Types");

        XSDBuiltInType anyType =
                xbtName("any type").builtInType("xsd:anyType").build();
        XSDBuiltInType anySimpleType =
                xbtName("any simple type").builtInType("xsd:anySimpleType").subTypeOfXbt(anyType).build();
        xbtName("duration").builtInType("xsd:duration").subTypeOfXbt(anySimpleType).build();
        xbtName("date time").builtInType("xsd:dateTime").subTypeOfXbt(anySimpleType).build();
        xbtName("time").builtInType("xsd:time").subTypeOfXbt(anySimpleType).build();
        xbtName("date").builtInType("xsd:date").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian year month").builtInType("xsd:gYearMonth").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian year").builtInType("xsd:gYear").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian month day").builtInType("xsd:gMonthDay").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian day").builtInType("xsd:gDay").subTypeOfXbt(anySimpleType).build();
        xbtName("gregorian month").builtInType("xsd:gMonth").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType stringType =
                xbtName("string").builtInType("xsd:string").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType normalizedStringType =
                xbtName("normalized string").builtInType("xsd:normalizedString").subTypeOfXbt(stringType).build();
        xbtName("token").builtInType("xsd:token").subTypeOfXbt(normalizedStringType).build();
        XSDBuiltInType booleanType =
                xbtName("boolean").builtInType("xsd:boolean").subTypeOfXbt(anySimpleType).build();
        xbtName("base64 binary").builtInType("xsd:base64Binary").subTypeOfXbt(anySimpleType).build();
        xbtName("hex binary").builtInType("xsd:hexBinary").subTypeOfXbt(anySimpleType).build();
        xbtName("float").builtInType("xsd:float").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType decimalType =
                xbtName("decimal").builtInType("xsd:decimal").subTypeOfXbt(anySimpleType).build();
        XSDBuiltInType integerType =
                xbtName("integer").builtInType("xsd:integer").subTypeOfXbt(decimalType).build();
        XSDBuiltInType nonNegativeIntegerType =
                xbtName("non negative integer").builtInType("xsd:nonNegativeInteger").subTypeOfXbt(integerType).build();
        xbtName("positive integer").builtInType("xsd:positiveInteger").subTypeOfXbt(nonNegativeIntegerType).build();
        xbtName("double").builtInType("xsd:double").subTypeOfXbt(anySimpleType).build();
        xbtName("any uri").builtInType("xsd:anyURI").subTypeOfXbt(anySimpleType).build();
        xbtName("xbt boolean true or false").builtInType("xbt_BooleanTrueFalseType").subTypeOfXbt(booleanType).build();
    }

    public XBTBuilder xbtName(String name) {
        XBTBuilder xbtBuilder = new XBTBuilder();
        xbtBuilder.name(name);
        return xbtBuilder;
    }

    public void populateCdtPri() {
        printTitle("Populate CDT Primitive");

        cdtPriRepository.save(cdtPri("Binary"));
        cdtPriRepository.save(cdtPri("Boolean"));
        cdtPriRepository.save(cdtPri("Decimal"));
        cdtPriRepository.save(cdtPri("Double"));
        cdtPriRepository.save(cdtPri("Float"));
        cdtPriRepository.save(cdtPri("Integer"));
        cdtPriRepository.save(cdtPri("NormalizedString"));
        cdtPriRepository.save(cdtPri("String"));
        cdtPriRepository.save(cdtPri("TimeDuration"));
        cdtPriRepository.save(cdtPri("TimePoint"));
        cdtPriRepository.save(cdtPri("Token"));
    }

    private CoreDataTypePrimitive cdtPri(String name) {
        CoreDataTypePrimitive cdtPri = new CoreDataTypePrimitive();
        cdtPri.setName(name);
        return cdtPri;
    }

    public void populateCdt(User user, Release release) {
        printTitle("Populate CDT");

        new CDTBuilder(user, release).dataTypeTerm("Amount").den("Amount. Type")
                .contentComponentDen("Amount. Content")
                .definition("CDT V3.1. An amount is a number of monetary units specified in a currency.")
                .contentComponentDefinition("A number of monetary units.").build();

        new CDTBuilder(user, release).dataTypeTerm("Binary Object").den("Binary Object. Type")
                .contentComponentDen("Binary Object. Content")
                .definition("CDT V3.1. A binary object is a sequence of binary digits (bits).")
                .contentComponentDefinition("A finite sequence of binary digits (bits).").build();

        new CDTBuilder(user, release).dataTypeTerm("Code").den("Code. Type")
                .contentComponentDen("Code. Content")
                .definition("CDT V3.1. A code is a character string of letters, numbers, special characters (except escape sequences); and symbols. It represents a definitive value,\na method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.")
                .contentComponentDefinition("A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute.").build();

        new CDTBuilder(user, release).dataTypeTerm("Date").den("Date. Type")
                .contentComponentDen("Date. Content")
                .definition("CDT V3.1. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.")
                .contentComponentDefinition("The particular point in the progression of date.").build();

        new CDTBuilder(user, release).dataTypeTerm("Date Time").den("Date Time. Type")
                .contentComponentDen("Date Time. Content")
                .definition("CDT V3.1. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of\nsecond.")
                .contentComponentDefinition("The particular date and time point in the progression of time.").build();

        new CDTBuilder(user, release).dataTypeTerm("Duration").den("Duration. Type")
                .contentComponentDen("Duration. Content")
                .definition("CDT V3.1. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,\nWeek, Day) and Hours, Minutes or Seconds.")
                .contentComponentDefinition("The particular representation of duration.").build();

        new CDTBuilder(user, release).dataTypeTerm("Graphic").den("Graphic. Type")
                .contentComponentDen("Graphic. Content")
                .definition("CDT V3.1. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for graphics.").build();

        new CDTBuilder(user, release).dataTypeTerm("Identifier").den("Identifier. Type")
                .contentComponentDen("Identifier. Content")
                .definition("CDT V3.1. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an\nagency.")
                .contentComponentDefinition("A character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency.").build();

        new CDTBuilder(user, release).dataTypeTerm("Indicator").den("Indicator. Type")
                .contentComponentDen("Indicator. Content")
                .definition("CDT V3.1. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.")
                .contentComponentDefinition("The value of the Indicator.").build();

        new CDTBuilder(user, release).dataTypeTerm("Measure").den("Measure. Type")
                .contentComponentDen("Measure. Content")
                .definition("CDT V3.1. A measure is a numeric value determined by measuring an object along with the specified unit of measure.")
                .contentComponentDefinition("The numeric value determined by measuring an object.").build();

        new CDTBuilder(user, release).dataTypeTerm("Name").den("Name. Type")
                .contentComponentDen("Name. Content")
                .definition("CDT V3.1. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.")
                .contentComponentDefinition("A word or phrase that represents a designation of a person, place, thing or concept.").build();

        new CDTBuilder(user, release).dataTypeTerm("Number").den("Number. Type")
                .contentComponentDen("Number. Content")
                .definition("CDT V3.1. A mathematical number that is assigned or is determined by calculation.")
                .contentComponentDefinition("Mathematical number that is assigned or is determined by calculation.").build();

        new CDTBuilder(user, release).dataTypeTerm("Ordinal").den("Ordinal. Type")
                .contentComponentDen("Ordinal. Content")
                .definition("CDT V3.1. An ordinal number is an assigned mathematical number that represents order or sequence.")
                .contentComponentDefinition("An assigned mathematical number that represents order or sequence").build();

        new CDTBuilder(user, release).dataTypeTerm("Percent").den("Percent. Type")
                .contentComponentDen("Percent. Content")
                .definition("CDT V3.1. A percent is a value representing a fraction of one hundred, expressed as a quotient.")
                .contentComponentDefinition("Numeric information that is assigned or is determined by percent.").build();

        new CDTBuilder(user, release).dataTypeTerm("Picture").den("Picture. Type")
                .contentComponentDen("Picture. Content")
                .definition("CDT V3.1. A picture is a visual representation of a person, object, or scene in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for pictures.").build();

        new CDTBuilder(user, release).dataTypeTerm("Quantity").den("Quantity. Type")
                .contentComponentDen("Quantity. Content")
                .definition("CDT V3.1. A quantity is a counted number of non-monetary units, possibly including fractions.")
                .contentComponentDefinition("A counted number of non-monetary units possibly including fractions.").build();

        new CDTBuilder(user, release).dataTypeTerm("Rate").den("Rate. Type")
                .contentComponentDen("Rate. Content")
                .definition("CDT V3.1. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.")
                .contentComponentDefinition("The numerical value of the rate.").build();

        new CDTBuilder(user, release).dataTypeTerm("Ratio").den("Ratio. Type")
                .contentComponentDen("Ratio. Content")
                .definition("CDT V3.1. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a\nquotient showing the number of times one value contains or is contained within the other, or as a proportion.")
                .contentComponentDefinition("The quotient or proportion between two independent quantities of the same unit of measure or currency.").build();

        new CDTBuilder(user, release).dataTypeTerm("Sound").den("Sound. Type")
                .contentComponentDen("Sound. Content")
                .definition("CDT V3.1. A sound is any form of an audio file such as audio recordings in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for sounds.").build();

        new CDTBuilder(user, release).dataTypeTerm("Text").den("Text. Type")
                .contentComponentDen("Text. Content")
                .definition("CDT V3.1. Text is a character string such as a finite set of characters generally in the form of words of a language.")
                .contentComponentDefinition("A character string generally in the form of words of a language.").build();

        new CDTBuilder(user, release).dataTypeTerm("Time").den("Time. Type")
                .contentComponentDen("Time. Content")
                .definition("CDT V3.1. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.")
                .contentComponentDefinition("The particular point in the progression of time.").build();

        new CDTBuilder(user, release).dataTypeTerm("Value").den("Value. Type")
                .contentComponentDen("Value. Content")
                .definition("CDT V3.1. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.")
                .contentComponentDefinition("Numeric information that is assigned or is determined by value.").build();

        new CDTBuilder(user, release).dataTypeTerm("Video").den("Video. Type")
                .contentComponentDen("Video. Content")
                .definition("CDT V3.1. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).")
                .contentComponentDefinition("A finite sequence of binary digits (bits) for videos.").build();
    }

    private class CDTBuilder {

        private DataType cdt;

        public CDTBuilder(User user, Release release) {
            cdt = new DataType();
            cdt.setGuid(Utility.generateGUID());
            cdt.setType(0);
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
            dataTypeRepository.save(cdt);
            return cdt;
        }
    }
}
