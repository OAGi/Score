package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.DataTypeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

@Component
public class P_1_2_PopulateCDTandCDTSC {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private DataTypeDAO dtDAO;

    private Map<String, XSDBuiltInType> xbtMap;
    private Map<String, List<XSDBuiltInType>> cdtAwdPriXpsTypeMapppingMap = new HashMap();

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_2_PopulateCDTandCDTSC populateCDTandCDTSC = ctx.getBean(P_1_2_PopulateCDTandCDTSC.class);
            populateCDTandCDTSC.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.2 Start");

        init();
        populateCdtAwdPri();
        populateCdtAwdPriXpsTypeMap();
        populateDtSc();
        populateCdtScAwdPri();
        populateCdtScAwdPriXpsTypeMap();

        logger.info("### 1.2 End");
    }

    public void init() {
        List<XSDBuiltInType> xbtList = xbtRepository.findAll();
        Collections.sort(xbtList, Comparator.comparing(XSDBuiltInType::getXbtId));

        xbtMap = xbtList.stream().collect(Collectors.toMap(XSDBuiltInType::getBuiltInType, Function.identity()));

        cdtAwdPriXpsTypeMapppingMap.put("Binary",
                Arrays.asList(xbtMap.get("xsd:base64Binary"), xbtMap.get("xsd:hexBinary")));
        cdtAwdPriXpsTypeMapppingMap.put("Boolean",
                Arrays.asList(xbtMap.get("xbt_BooleanType")));
        cdtAwdPriXpsTypeMapppingMap.put("Decimal",
                Arrays.asList(xbtMap.get("xsd:decimal")));
        cdtAwdPriXpsTypeMapppingMap.put("Double",
                Arrays.asList(xbtMap.get("xsd:double"), xbtMap.get("xsd:float")));
        cdtAwdPriXpsTypeMapppingMap.put("Float",
                Arrays.asList(xbtMap.get("xsd:float")));
        cdtAwdPriXpsTypeMapppingMap.put("Integer",
                Arrays.asList(xbtMap.get("xsd:integer"), xbtMap.get("xsd:positiveInteger"), xbtMap.get("xsd:nonNegativeInteger")));
        cdtAwdPriXpsTypeMapppingMap.put("NormalizedString",
                Arrays.asList(xbtMap.get("xsd:normalizedString")));
        cdtAwdPriXpsTypeMapppingMap.put("String",
                Arrays.asList(xbtMap.get("xsd:string")));
        cdtAwdPriXpsTypeMapppingMap.put("TimeDuration",
                Arrays.asList(xbtMap.get("xsd:token"), xbtMap.get("xsd:duration"), xbtMap.get("xbt_WeekDurationType")));
        cdtAwdPriXpsTypeMapppingMap.put("TimePoint",
                xbtList.stream().filter(e -> {
                    String builtInType = e.getBuiltInType();
                    if (Arrays.asList("xsd:token", "xsd:dateTime", "xsd:time").contains(builtInType)) {
                        return true;
                    }
                    if (builtInType.startsWith("xsd:g")) {
                        return true;
                    }
                    if (builtInType.startsWith("xbt_")) {
                        if (!builtInType.equals("xbt_BooleanType") && !builtInType.equals("xbt_WeekDurationType")) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList()));
        cdtAwdPriXpsTypeMapppingMap.put("Token",
                Arrays.asList(xbtMap.get("xsd:token")));
    }

    public void populateCdtAwdPri() {
        printTitle("Populate CDT Allowed Primitive");

        cdtAwdPri("Amount", "Decimal", true);
        cdtAwdPri("Amount", "Double", false);
        cdtAwdPri("Amount", "Float", false);
        cdtAwdPri("Amount", "Integer", false);
        cdtAwdPri("Binary Object", "Binary", true);
        cdtAwdPri("Code", "NormalizedString", false);
        cdtAwdPri("Code", "String", false);
        cdtAwdPri("Code", "Token", true);
        cdtAwdPri("Date", "TimePoint", true);
        cdtAwdPri("Date Time", "TimePoint", true);
        cdtAwdPri("Duration", "TimeDuration", true);
        cdtAwdPri("Graphic", "Binary", true);
        cdtAwdPri("Identifier", "NormalizedString", false);
        cdtAwdPri("Identifier", "String", false);
        cdtAwdPri("Identifier", "Token", true);
        cdtAwdPri("Indicator", "Boolean", true);
        cdtAwdPri("Measure", "Decimal", true);
        cdtAwdPri("Measure", "Double", false);
        cdtAwdPri("Measure", "Float", false);
        cdtAwdPri("Measure", "Integer", false);
        cdtAwdPri("Name", "NormalizedString", false);
        cdtAwdPri("Name", "String", false);
        cdtAwdPri("Name", "Token", true);
        cdtAwdPri("Number", "Decimal", true);
        cdtAwdPri("Number", "Double", false);
        cdtAwdPri("Number", "Float", false);
        cdtAwdPri("Number", "Integer", false);
        cdtAwdPri("Ordinal", "Integer", true);
        cdtAwdPri("Percent", "Decimal", true);
        cdtAwdPri("Percent", "Double", false);
        cdtAwdPri("Percent", "Float", false);
        cdtAwdPri("Percent", "Integer", false);
        cdtAwdPri("Picture", "Binary", true);
        cdtAwdPri("Quantity", "Decimal", true);
        cdtAwdPri("Quantity", "Double", false);
        cdtAwdPri("Quantity", "Float", false);
        cdtAwdPri("Quantity", "Integer", false);
        cdtAwdPri("Rate", "Decimal", true);
        cdtAwdPri("Rate", "Double", false);
        cdtAwdPri("Rate", "Float", false);
        cdtAwdPri("Rate", "Integer", false);
        cdtAwdPri("Ratio", "Decimal", true);
        cdtAwdPri("Ratio", "Double", false);
        cdtAwdPri("Ratio", "Float", false);
        cdtAwdPri("Ratio", "Integer", false);
        cdtAwdPri("Ratio", "String", false);
        cdtAwdPri("Sound", "Binary", true);
        cdtAwdPri("Text", "NormalizedString", false);
        cdtAwdPri("Text", "String", true);
        cdtAwdPri("Text", "Token", false);
        cdtAwdPri("Time", "TimePoint", true);
        cdtAwdPri("Value", "Decimal", true);
        cdtAwdPri("Value", "Double", false);
        cdtAwdPri("Value", "Float", false);
        cdtAwdPri("Value", "Integer", false);
        cdtAwdPri("Value", "NormalizedString", false);
        cdtAwdPri("Value", "String", false);
        cdtAwdPri("Value", "Token", false);
        cdtAwdPri("Video", "Binary", true);
    }

    private CoreDataTypeAllowedPrimitive cdtAwdPri(String cdtTerm, String cdtPriName, boolean isDefault) {
        CoreDataTypeAllowedPrimitive cdtAwdPri = new CoreDataTypeAllowedPrimitive();

        cdtAwdPri.setCdtId(dataTypeRepository.findOneByDataTypeTermAndType(cdtTerm, DataTypeType.CoreDataType).getDtId());
        cdtAwdPri.setCdtPriId(cdtPriRepository.findOneByName(cdtPriName).getCdtPriId());
        cdtAwdPri.setDefault(isDefault);

        return cdtAwdPriRepository.saveAndFlush(cdtAwdPri);
    }

    public void populateCdtAwdPriXpsTypeMap() {
        printTitle("Populate CDT Allowed Primitive Expression Type Map");

        cdtAwdPriXpsTypeMap("Amount", "Decimal");
        cdtAwdPriXpsTypeMap("Amount", "Double");
        cdtAwdPriXpsTypeMap("Amount", "Float");
        cdtAwdPriXpsTypeMap("Amount", "Integer");
        cdtAwdPriXpsTypeMap("Binary Object", "Binary");
        cdtAwdPriXpsTypeMap("Code", "NormalizedString");
        cdtAwdPriXpsTypeMap("Code", "String");
        cdtAwdPriXpsTypeMap("Code", "Token");
        cdtAwdPriXpsTypeMap("Date", "TimePoint",
                cdtAwdPriXpsTypeMapppingMap.get("TimePoint").stream()
                        .filter(e -> !"xsd:dateTime".equals(e.getBuiltInType()))
                        .collect(Collectors.toList())
        );
        cdtAwdPriXpsTypeMap("Date Time", "TimePoint");
        cdtAwdPriXpsTypeMap("Duration", "TimeDuration");
        cdtAwdPriXpsTypeMap("Graphic", "Binary");
        cdtAwdPriXpsTypeMap("Identifier", "NormalizedString");
        cdtAwdPriXpsTypeMap("Identifier", "String");
        cdtAwdPriXpsTypeMap("Identifier", "Token");
        cdtAwdPriXpsTypeMap("Indicator", "Boolean");
        cdtAwdPriXpsTypeMap("Measure", "Decimal");
        cdtAwdPriXpsTypeMap("Measure", "Double");
        cdtAwdPriXpsTypeMap("Measure", "Float");
        cdtAwdPriXpsTypeMap("Measure", "Integer");
        cdtAwdPriXpsTypeMap("Name", "NormalizedString");
        cdtAwdPriXpsTypeMap("Name", "String");
        cdtAwdPriXpsTypeMap("Name", "Token");
        cdtAwdPriXpsTypeMap("Number", "Decimal");
        cdtAwdPriXpsTypeMap("Number", "Double");
        cdtAwdPriXpsTypeMap("Number", "Float");
        cdtAwdPriXpsTypeMap("Number", "Integer");
        cdtAwdPriXpsTypeMap("Ordinal", "Integer");
        cdtAwdPriXpsTypeMap("Percent", "Decimal");
        cdtAwdPriXpsTypeMap("Percent", "Double");
        cdtAwdPriXpsTypeMap("Percent", "Float");
        cdtAwdPriXpsTypeMap("Percent", "Integer");
        cdtAwdPriXpsTypeMap("Picture", "Binary");
        cdtAwdPriXpsTypeMap("Quantity", "Decimal");
        cdtAwdPriXpsTypeMap("Quantity", "Double");
        cdtAwdPriXpsTypeMap("Quantity", "Float");
        cdtAwdPriXpsTypeMap("Quantity", "Integer");
        cdtAwdPriXpsTypeMap("Rate", "Decimal");
        cdtAwdPriXpsTypeMap("Rate", "Double");
        cdtAwdPriXpsTypeMap("Rate", "Float");
        cdtAwdPriXpsTypeMap("Rate", "Integer");
        cdtAwdPriXpsTypeMap("Ratio", "Decimal");
        cdtAwdPriXpsTypeMap("Ratio", "Double");
        cdtAwdPriXpsTypeMap("Ratio", "Float");
        cdtAwdPriXpsTypeMap("Ratio", "Integer");
        cdtAwdPriXpsTypeMap("Ratio", "String");
        cdtAwdPriXpsTypeMap("Sound", "Binary");
        cdtAwdPriXpsTypeMap("Text", "NormalizedString");
        cdtAwdPriXpsTypeMap("Text", "String");
        cdtAwdPriXpsTypeMap("Text", "Token");
        cdtAwdPriXpsTypeMap("Time", "TimePoint",
                Arrays.asList(xbtMap.get("xsd:token"), xbtMap.get("xsd:time"))
        );
        cdtAwdPriXpsTypeMap("Value", "Decimal");
        cdtAwdPriXpsTypeMap("Value", "Double");
        cdtAwdPriXpsTypeMap("Value", "Float");
        cdtAwdPriXpsTypeMap("Value", "Integer");
        cdtAwdPriXpsTypeMap("Value", "NormalizedString");
        cdtAwdPriXpsTypeMap("Value", "String");
        cdtAwdPriXpsTypeMap("Value", "Token");
        cdtAwdPriXpsTypeMap("Video", "Binary");
    }

    private void cdtAwdPriXpsTypeMap(
            String cdtTerm, String cdtPriName) {
        List<XSDBuiltInType> xsdBuiltInTypes = cdtAwdPriXpsTypeMapppingMap.get(cdtPriName);
        cdtAwdPriXpsTypeMap(cdtTerm, cdtPriName, xsdBuiltInTypes);
    }

    private void cdtAwdPriXpsTypeMap(
            String cdtTerm, String cdtPriName, List<XSDBuiltInType> xbtBuiltInTypes) {
        for (XSDBuiltInType xsdBuiltInType : xbtBuiltInTypes) {
            cdtAwdPriXpsTypeMap(cdtTerm, cdtPriName, xsdBuiltInType);
        }
    }

    private CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap(
            String cdtTerm, String cdtPriName, XSDBuiltInType xbtBuiltInType) {
        if (xbtBuiltInType == null) {
            throw new IllegalArgumentException();
        }

        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                new CoreDataTypeAllowedPrimitiveExpressionTypeMap();

        cdtAwdPriXpsTypeMap.setCdtAwdPriId(
                cdtAwdPriRepository.findOneByCdtIdAndCdtPriId(
                        dataTypeRepository.findOneByDataTypeTermAndType(cdtTerm, DataTypeType.CoreDataType).getDtId(),
                        cdtPriRepository.findOneByName(cdtPriName).getCdtPriId()).getCdtAwdPriId()
        );
        cdtAwdPriXpsTypeMap.setXbtId(xbtBuiltInType.getXbtId());

        return cdtAwdPriXpsTypeMapRepository.saveAndFlush(cdtAwdPriXpsTypeMap);
    }

    public void populateDtSc() {
        printTitle("Populate CDT Supplementary Components");

        dtSc("Currency", "Code", "The currency of the amount", "Amount");
        dtSc("MIME", "Code", "The Multipurpose Internet Mail Extensions(MIME) media type of the binary object", "Binary Object");
        dtSc("Character Set", "Code", "The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text", "Binary Object");
        dtSc("Filename", "Name", "The filename of the binary object", "Binary Object");
        dtSc("List", "Identifier", "The identification of a list of codes", "Code");
        dtSc("List Agency", "Identifier", "The identification of the agency that manages the code list.", "Code");
        dtSc("List Version", "Identifier", "The identification of the version of the list of codes.", "Code");
        dtSc("Time Zone", "Code", "The time zone to which the date time refers", "Date Time");
        dtSc("Daylight Saving", "Indicator", "The indication of whether or not this Date Time is in daylight saving", "Date Time");
        dtSc("MIME", "Code", "The Multipurpose Internet Mail Extensions (MIME) media type of the graphic.", "Graphic");
        dtSc("Character Set", "Code", "The character set of the graphic if the Multipurpose Internet Mail Extensions (MIME) type is text.", "Graphic");
        dtSc("Filename", "Name", "The filename of the graphic", "Graphic");
        dtSc("Scheme", "Identifier", "The identification of the identifier scheme.", "Identifier");
        dtSc("Scheme Version", "Identifier", "The identification of the version of the identifier scheme", "Identifier");
        dtSc("Scheme Agency", "Identifier", "The identification of the agency that manages the identifier scheme", "Identifier");
        dtSc("Unit", "Code", "The unit of measure", "Measure");
        dtSc("Language", "Code", "The language used in the corresponding text string", "Name");
        dtSc("MIME", "Code", "The Multipurpose Internet Mail Extensions(MIME) media type of the picture", "Picture");
        dtSc("Character Set", "Code", "The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text", "Picture");
        dtSc("Filename", "Name", "The filename of the picture", "Picture");
        dtSc("Unit", "Code", "The unit of measure in which the quantity is expressed", "Quantity");
        dtSc("Multiplier", "Value", "The multiplier of the Rate. Unit. Code or Rate. Currency. Code", "Rate");
        dtSc("Unit", "Code", "The unit of measure of the numerator", "Rate");
        dtSc("Currency", "Code", "The currency of the numerator", "Rate");
        dtSc("Base Multiplier", "Value", "The multiplier of the Rate. Base Unit. Code or Rate. Base Currency. Code", "Rate");
        dtSc("Base Unit", "Code", "The unit of measure of the denominator", "Rate");
        dtSc("Base Currency", "Code", "The currency of the denominator", "Rate");
        dtSc("MIME", "Code", "The Multipurpose Internet Mail Extensions(MIME) media type of the sound", "Sound");
        dtSc("Character Set", "Code", "The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text", "Sound");
        dtSc("Filename", "Name", "The filename of the sound", "Sound");
        dtSc("Language", "Code", "The language used in the corresponding text string", "Text");
        dtSc("MIME", "Code", "The Multipurpose Internet Mail Extensions(MIME) media type of the video", "Video");
        dtSc("Character Set", "Code", "The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text", "Video");
        dtSc("Filename", "Name", "The filename of the video", "Video");
    }

    private DataTypeSupplementaryComponent dtSc(String propertyTerm,
                                                String representationTerm,
                                                String definition,
                                                String cdtTerm) {
        DataTypeSupplementaryComponent dtSc = new DataTypeSupplementaryComponent();

        dtSc.setGuid(Utility.generateGUID());
        dtSc.setPropertyTerm(propertyTerm);
        dtSc.setRepresentationTerm(representationTerm);
        dtSc.setDefinition(definition);
        dtSc.setCardinalityMin(0);
        dtSc.setCardinalityMax(1);
        dtSc.setOwnerDtId(
                dataTypeRepository.findOneByDataTypeTermAndType(cdtTerm, DataTypeType.CoreDataType).getDtId()
        );

        return dtDAO.save(dtSc);
    }

    public void populateCdtScAwdPri() {
        printTitle("Populate CDT Supplementary Component Allowed Primitive");

        cdtScAwdPri("Amount", "Currency", "NormalizedString", false);
        cdtScAwdPri("Amount", "Currency", "String", false);
        cdtScAwdPri("Amount", "Currency", "Token", true);
        cdtScAwdPri("Binary Object", "MIME", "NormalizedString", false);
        cdtScAwdPri("Binary Object", "MIME", "String", false);
        cdtScAwdPri("Binary Object", "MIME", "Token", true);
        cdtScAwdPri("Binary Object", "Character Set", "NormalizedString", false);
        cdtScAwdPri("Binary Object", "Character Set", "String", false);
        cdtScAwdPri("Binary Object", "Character Set", "Token", true);
        cdtScAwdPri("Binary Object", "Filename", "NormalizedString", false);
        cdtScAwdPri("Binary Object", "Filename", "String", false);
        cdtScAwdPri("Binary Object", "Filename", "Token", true);
        cdtScAwdPri("Code", "List", "NormalizedString", false);
        cdtScAwdPri("Code", "List", "String", false);
        cdtScAwdPri("Code", "List", "Token", true);
        cdtScAwdPri("Code", "List Agency", "NormalizedString", false);
        cdtScAwdPri("Code", "List Agency", "String", false);
        cdtScAwdPri("Code", "List Agency", "Token", true);
        cdtScAwdPri("Code", "List Version", "NormalizedString", false);
        cdtScAwdPri("Code", "List Version", "String", false);
        cdtScAwdPri("Code", "List Version", "Token", true);
        cdtScAwdPri("Date Time", "Time Zone", "NormalizedString", false);
        cdtScAwdPri("Date Time", "Time Zone", "String", false);
        cdtScAwdPri("Date Time", "Time Zone", "Token", true);
        cdtScAwdPri("Date Time", "Daylight Saving", "Boolean", true);
        cdtScAwdPri("Graphic", "MIME", "NormalizedString", false);
        cdtScAwdPri("Graphic", "MIME", "String", false);
        cdtScAwdPri("Graphic", "MIME", "Token", true);
        cdtScAwdPri("Graphic", "Character Set", "NormalizedString", false);
        cdtScAwdPri("Graphic", "Character Set", "String", false);
        cdtScAwdPri("Graphic", "Character Set", "Token", true);
        cdtScAwdPri("Graphic", "Filename", "NormalizedString", false);
        cdtScAwdPri("Graphic", "Filename", "String", false);
        cdtScAwdPri("Graphic", "Filename", "Token", true);
        cdtScAwdPri("Identifier", "Scheme", "NormalizedString", false);
        cdtScAwdPri("Identifier", "Scheme", "String", false);
        cdtScAwdPri("Identifier", "Scheme", "Token", true);
        cdtScAwdPri("Identifier", "Scheme Version", "NormalizedString", false);
        cdtScAwdPri("Identifier", "Scheme Version", "String", false);
        cdtScAwdPri("Identifier", "Scheme Version", "Token", true);
        cdtScAwdPri("Identifier", "Scheme Agency", "NormalizedString", false);
        cdtScAwdPri("Identifier", "Scheme Agency", "String", false);
        cdtScAwdPri("Identifier", "Scheme Agency", "Token", true);
        cdtScAwdPri("Measure", "Unit", "NormalizedString", false);
        cdtScAwdPri("Measure", "Unit", "String", false);
        cdtScAwdPri("Measure", "Unit", "Token", true);
        cdtScAwdPri("Name", "Language", "NormalizedString", false);
        cdtScAwdPri("Name", "Language", "String", false);
        cdtScAwdPri("Name", "Language", "Token", true);
        cdtScAwdPri("Picture", "MIME", "NormalizedString", false);
        cdtScAwdPri("Picture", "MIME", "String", false);
        cdtScAwdPri("Picture", "MIME", "Token", true);
        cdtScAwdPri("Picture", "Character Set", "NormalizedString", false);
        cdtScAwdPri("Picture", "Character Set", "String", false);
        cdtScAwdPri("Picture", "Character Set", "Token", true);
        cdtScAwdPri("Picture", "Filename", "NormalizedString", false);
        cdtScAwdPri("Picture", "Filename", "String", false);
        cdtScAwdPri("Picture", "Filename", "Token", true);
        cdtScAwdPri("Quantity", "Unit", "NormalizedString", false);
        cdtScAwdPri("Quantity", "Unit", "String", false);
        cdtScAwdPri("Quantity", "Unit", "Token", true);
        cdtScAwdPri("Rate", "Multiplier", "Decimal", true);
        cdtScAwdPri("Rate", "Multiplier", "Double", false);
        cdtScAwdPri("Rate", "Multiplier", "Float", false);
        cdtScAwdPri("Rate", "Multiplier", "Integer", false);
        cdtScAwdPri("Rate", "Unit", "NormalizedString", false);
        cdtScAwdPri("Rate", "Unit", "String", false);
        cdtScAwdPri("Rate", "Unit", "Token", true);
        cdtScAwdPri("Rate", "Currency", "NormalizedString", false);
        cdtScAwdPri("Rate", "Currency", "String", false);
        cdtScAwdPri("Rate", "Currency", "Token", true);
        cdtScAwdPri("Rate", "Base Multiplier", "Decimal", true);
        cdtScAwdPri("Rate", "Base Multiplier", "Double", false);
        cdtScAwdPri("Rate", "Base Multiplier", "Float", false);
        cdtScAwdPri("Rate", "Base Multiplier", "Integer", false);
        cdtScAwdPri("Rate", "Base Unit", "NormalizedString", false);
        cdtScAwdPri("Rate", "Base Unit", "String", false);
        cdtScAwdPri("Rate", "Base Unit", "Token", true);
        cdtScAwdPri("Rate", "Base Currency", "NormalizedString", false);
        cdtScAwdPri("Rate", "Base Currency", "String", false);
        cdtScAwdPri("Rate", "Base Currency", "Token", true);
        cdtScAwdPri("Sound", "MIME", "NormalizedString", false);
        cdtScAwdPri("Sound", "MIME", "String", false);
        cdtScAwdPri("Sound", "MIME", "Token", true);
        cdtScAwdPri("Sound", "Character Set", "NormalizedString", false);
        cdtScAwdPri("Sound", "Character Set", "String", false);
        cdtScAwdPri("Sound", "Character Set", "Token", true);
        cdtScAwdPri("Sound", "Filename", "NormalizedString", false);
        cdtScAwdPri("Sound", "Filename", "String", false);
        cdtScAwdPri("Sound", "Filename", "Token", true);
        cdtScAwdPri("Text", "Language", "NormalizedString", false);
        cdtScAwdPri("Text", "Language", "String", false);
        cdtScAwdPri("Text", "Language", "Token", true);
        cdtScAwdPri("Video", "MIME", "NormalizedString", false);
        cdtScAwdPri("Video", "MIME", "String", false);
        cdtScAwdPri("Video", "MIME", "Token", true);
        cdtScAwdPri("Video", "Character Set", "NormalizedString", false);
        cdtScAwdPri("Video", "Character Set", "String", false);
        cdtScAwdPri("Video", "Character Set", "Token", true);
        cdtScAwdPri("Video", "Filename", "NormalizedString", false);
        cdtScAwdPri("Video", "Filename", "String", false);
        cdtScAwdPri("Video", "Filename", "Token", true);
    }

    private CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri(
            String cdtTerm, String cdtScPropertyTerm, String cdtPriName, boolean isDefault) {
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri =
                new CoreDataTypeSupplementaryComponentAllowedPrimitive();

        cdtScAwdPri.setCdtScId(
                dtScRepository.findOneByOwnerDataTypeTermAndPropertyTerm(
                        cdtTerm, cdtScPropertyTerm).getDtScId()
        );
        cdtScAwdPri.setCdtPriId(cdtPriRepository.findOneByName(cdtPriName).getCdtPriId());
        cdtScAwdPri.setDefault(isDefault);

        return cdtScAwdPriRepository.save(cdtScAwdPri);
    }

    public void populateCdtScAwdPriXpsTypeMap() {
        printTitle("Populate CDT Supplementary Component Allowed Primitive Expression Type Map");

        cdtScAwdPriXpsTypeMap("Amount", "Currency", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Amount", "Currency", "String", "string");
        cdtScAwdPriXpsTypeMap("Amount", "Currency", "Token", "token");
        cdtScAwdPriXpsTypeMap("Binary Object", "MIME", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Binary Object", "MIME", "String", "string");
        cdtScAwdPriXpsTypeMap("Binary Object", "MIME", "Token", "token");
        cdtScAwdPriXpsTypeMap("Binary Object", "Character Set", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Binary Object", "Character Set", "String", "string");
        cdtScAwdPriXpsTypeMap("Binary Object", "Character Set", "Token", "token");
        cdtScAwdPriXpsTypeMap("Binary Object", "Filename", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Binary Object", "Filename", "String", "string");
        cdtScAwdPriXpsTypeMap("Binary Object", "Filename", "Token", "token");
        cdtScAwdPriXpsTypeMap("Code", "List", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Code", "List", "String", "string");
        cdtScAwdPriXpsTypeMap("Code", "List", "Token", "token");
        cdtScAwdPriXpsTypeMap("Code", "List Agency", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Code", "List Agency", "String", "string");
        cdtScAwdPriXpsTypeMap("Code", "List Agency", "Token", "token");
        cdtScAwdPriXpsTypeMap("Code", "List Version", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Code", "List Version", "String", "string");
        cdtScAwdPriXpsTypeMap("Code", "List Version", "Token", "token");
        cdtScAwdPriXpsTypeMap("Date Time", "Time Zone", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Date Time", "Time Zone", "String", "string");
        cdtScAwdPriXpsTypeMap("Date Time", "Time Zone", "Token", "token");
        cdtScAwdPriXpsTypeMap("Date Time", "Daylight Saving", "Boolean", "xbt boolean");
        cdtScAwdPriXpsTypeMap("Graphic", "MIME", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Graphic", "MIME", "String", "string");
        cdtScAwdPriXpsTypeMap("Graphic", "MIME", "Token", "token");
        cdtScAwdPriXpsTypeMap("Graphic", "Character Set", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Graphic", "Character Set", "String", "string");
        cdtScAwdPriXpsTypeMap("Graphic", "Character Set", "Token", "token");
        cdtScAwdPriXpsTypeMap("Graphic", "Filename", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Graphic", "Filename", "String", "string");
        cdtScAwdPriXpsTypeMap("Graphic", "Filename", "Token", "token");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme", "String", "string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme", "Token", "token");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Version", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Version", "String", "string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Version", "Token", "token");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Agency", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Agency", "String", "string");
        cdtScAwdPriXpsTypeMap("Identifier", "Scheme Agency", "Token", "token");
        cdtScAwdPriXpsTypeMap("Measure", "Unit", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Measure", "Unit", "String", "string");
        cdtScAwdPriXpsTypeMap("Measure", "Unit", "Token", "token");
        cdtScAwdPriXpsTypeMap("Name", "Language", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Name", "Language", "String", "string");
        cdtScAwdPriXpsTypeMap("Name", "Language", "Token", "token");
        cdtScAwdPriXpsTypeMap("Picture", "MIME", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Picture", "MIME", "String", "string");
        cdtScAwdPriXpsTypeMap("Picture", "MIME", "Token", "token");
        cdtScAwdPriXpsTypeMap("Picture", "Character Set", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Picture", "Character Set", "String", "string");
        cdtScAwdPriXpsTypeMap("Picture", "Character Set", "Token", "token");
        cdtScAwdPriXpsTypeMap("Picture", "Filename", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Picture", "Filename", "String", "string");
        cdtScAwdPriXpsTypeMap("Picture", "Filename", "Token", "token");
        cdtScAwdPriXpsTypeMap("Quantity", "Unit", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Quantity", "Unit", "String", "string");
        cdtScAwdPriXpsTypeMap("Quantity", "Unit", "Token", "token");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Decimal", "decimal");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Double", "double");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Double", "float");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Float", "float");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Integer", "integer");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Integer", "non negative integer");
        cdtScAwdPriXpsTypeMap("Rate", "Multiplier", "Integer", "positive integer");
        cdtScAwdPriXpsTypeMap("Rate", "Unit", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Rate", "Unit", "String", "string");
        cdtScAwdPriXpsTypeMap("Rate", "Unit", "Token", "token");
        cdtScAwdPriXpsTypeMap("Rate", "Currency", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Rate", "Currency", "String", "string");
        cdtScAwdPriXpsTypeMap("Rate", "Currency", "Token", "token");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Decimal", "decimal");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Double", "double");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Double", "float");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Float", "float");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Integer", "integer");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Integer", "non negative integer");
        cdtScAwdPriXpsTypeMap("Rate", "Base Multiplier", "Integer", "positive integer");
        cdtScAwdPriXpsTypeMap("Rate", "Base Unit", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Rate", "Base Unit", "String", "string");
        cdtScAwdPriXpsTypeMap("Rate", "Base Unit", "Token", "token");
        cdtScAwdPriXpsTypeMap("Rate", "Base Currency", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Rate", "Base Currency", "String", "string");
        cdtScAwdPriXpsTypeMap("Rate", "Base Currency", "Token", "token");
        cdtScAwdPriXpsTypeMap("Sound", "MIME", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Sound", "MIME", "String", "string");
        cdtScAwdPriXpsTypeMap("Sound", "MIME", "Token", "token");
        cdtScAwdPriXpsTypeMap("Sound", "Character Set", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Sound", "Character Set", "String", "string");
        cdtScAwdPriXpsTypeMap("Sound", "Character Set", "Token", "token");
        cdtScAwdPriXpsTypeMap("Sound", "Filename", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Sound", "Filename", "String", "string");
        cdtScAwdPriXpsTypeMap("Sound", "Filename", "Token", "token");
        cdtScAwdPriXpsTypeMap("Text", "Language", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Text", "Language", "String", "string");
        cdtScAwdPriXpsTypeMap("Text", "Language", "Token", "token");
        cdtScAwdPriXpsTypeMap("Video", "MIME", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Video", "MIME", "String", "string");
        cdtScAwdPriXpsTypeMap("Video", "MIME", "Token", "token");
        cdtScAwdPriXpsTypeMap("Video", "Character Set", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Video", "Character Set", "String", "string");
        cdtScAwdPriXpsTypeMap("Video", "Character Set", "Token", "token");
        cdtScAwdPriXpsTypeMap("Video", "Filename", "NormalizedString", "normalized string");
        cdtScAwdPriXpsTypeMap("Video", "Filename", "String", "string");
        cdtScAwdPriXpsTypeMap("Video", "Filename", "Token", "token");
    }

    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap(
            String cdtTerm, String dtScPropertyTerm, String cdtPriName, String xbtName) {
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();

        cdtScAwdPriXpsTypeMap.setCdtScAwdPriId(
                cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(
                        dtScRepository.findOneByOwnerDataTypeTermAndPropertyTerm(cdtTerm, dtScPropertyTerm).getDtScId(),
                        cdtPriRepository.findOneByName(cdtPriName).getCdtPriId()
                ).getCdtScAwdPriId()
        );
        cdtScAwdPriXpsTypeMap.setXbtId(
                xbtRepository.findOneByName(xbtName).getXbtId()
        );

        return cdtScAwdPriXpsTypeMapRepository.saveAndFlush(cdtScAwdPriXpsTypeMap);
    }
}
