package org.oagi.srt.persistence.validate;

import org.oagi.srt.Application;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CDTTest {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    public int getCodeListId(String codeName) throws Exception {
        CodeList codelist = codeListRepository.findByNameContaining(codeName.trim()).get(0);
        return codelist.getCodeListId();
    }

    public int getAgencyListId() throws Exception {
        AgencyIdList agencyidlist = agencyIdListRepository.findOneByName("Agency Identification");
        return agencyidlist.getAgencyIdListId();
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid) throws Exception {
        return dtScRepository.findOneByGuid(guid);
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid, int ownerId) throws Exception {
        return dtScRepository.findOneByGuidAndOwnerDtId(guid, ownerId);
    }

    public int getDataTypeId(String DataTypeTerm) throws Exception {
        DataType dt = dataTypeRepository.findOneByDataTypeTermAndType(DataTypeTerm, 0);
        return dt.getDtId();
    }

    public int getCdtScId(int DataTypeSupplementaryComponentId) throws Exception {
        CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository aCoreDataTypeSupplementaryComponentAllowedPrimitiveDAO =
                repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveRepository();
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdt = aCoreDataTypeSupplementaryComponentAllowedPrimitiveDAO.findByCdtScId(DataTypeSupplementaryComponentId).get(0);
        int id = cdt.getCdtScId();
        return id;
    }

    public String getRepresentationTerm(String DataTypeSupplementaryComponentGUId) throws Exception {
        DataTypeSupplementaryComponent dtsc = dtScRepository.findOneByGuid(DataTypeSupplementaryComponentGUId);
        String term = dtsc.getRepresentationTerm();
        return term;
    }

    public String getPrimitiveName(int CdtPriId) throws Exception {
        return cdtPriRepository.findOne(CdtPriId).getName();
    }


    public int getCdtPriId(String name) throws Exception {
        return cdtPriRepository.findOneByName(name).getCdtPriId();
    }

    public List<CoreDataTypeAllowedPrimitive> getCdtAllowedPrimitiveIds(int cdt_id) throws Exception {
        return cdtAwdPriRepository.findByCdtId(cdt_id);
    }

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> getCdtSCAllowedPrimitiveId(int dt_sc_id) throws Exception {
        CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository aCoreDataTypeSupplementaryComponentAllowedPrimitiveDAO =
                repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveRepository();
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> res = aCoreDataTypeSupplementaryComponentAllowedPrimitiveDAO.findByCdtScId(dt_sc_id);
        if (res.isEmpty()) {
            DataTypeSupplementaryComponent dtsc = dtScRepository.findOne(dt_sc_id);
            res = getCdtSCAllowedPrimitiveId(dtsc.getBasedDtScId());
        }
        return res;
    }

    private void check_number_of_cdt() throws Exception {
        List<DataType> cdt = dataTypeRepository.findByType(0);
        System.out.println("# of cdts in catalog : " + cdt.size());
        if (cdt.size() == 23)
            System.out.println("Validated");
    }

    private void check_number_of_XBT() throws Exception {
        List<XSDBuiltInType> xsd = xbtRepository.findAll();
        System.out.println("# of XBTs in catalog : " + xsd.size());
        for (int i = 0; i < xsd.size(); i++) {
            for (int j = i + 1; j < xsd.size(); j++) {
                if (xsd.get(i).getName().equalsIgnoreCase(xsd.get(j).getName()))
                    System.out.println("Strange data; see the " + i + "th data and " + j + " th data");
            }
        }
        if (xsd.size() == 24)
            System.out.println("Validated");
    }

    private void validate_cdt_sc() throws Exception {
        List<DataType> cdtlist = dataTypeRepository.findByType(0);
        List<String> cdt_sc_list = new ArrayList();

        cdt_sc_list.add("AmountCurrencyCode01");
        cdt_sc_list.add("Binary ObjectMIMECode01");
        cdt_sc_list.add("Binary ObjectCharacter SetCode01");
        cdt_sc_list.add("Binary ObjectFilenameName01");
        cdt_sc_list.add("CodeListIdentifier01");
        cdt_sc_list.add("CodeList AgencyIdentifier01");
        cdt_sc_list.add("CodeList VersionIdentifier01");
        cdt_sc_list.add("Date TimeTime ZoneCode01");
        cdt_sc_list.add("Date TimeDaylight SavingIndicator01");
        cdt_sc_list.add("GraphicMIMECode01");
        cdt_sc_list.add("GraphicCharacter SetCode01");
        cdt_sc_list.add("GraphicFilenameName01");
        cdt_sc_list.add("IdentifierSchemeIdentifier01");
        cdt_sc_list.add("IdentifierScheme VersionIdentifier01");
        cdt_sc_list.add("IdentifierScheme AgencyIdentifier01");
        cdt_sc_list.add("MeasureUnitCode01");
        cdt_sc_list.add("NameLanguageCode01");
        cdt_sc_list.add("PictureMIMECode01");
        cdt_sc_list.add("PictureCharacter SetCode01");
        cdt_sc_list.add("PictureFilenameName01");
        cdt_sc_list.add("QuantityUnitCode01");
        cdt_sc_list.add("RateMultiplierValue01");
        cdt_sc_list.add("RateUnitCode01");
        cdt_sc_list.add("RateCurrencyCode01");
        cdt_sc_list.add("RateBase MultiplierValue01");
        cdt_sc_list.add("RateBase UnitCode01");
        cdt_sc_list.add("RateBase CurrencyCode01");

        cdt_sc_list.add("SoundMIMECode01");
        cdt_sc_list.add("SoundCharacter SetCode01");
        cdt_sc_list.add("SoundFilenameName01");
        cdt_sc_list.add("TextLanguageCode01");
        cdt_sc_list.add("VideoMIMECode01");
        cdt_sc_list.add("VideoCharacter SetCode01");
        cdt_sc_list.add("VideoFilenameName01");


        List<String> cdtscFromDB = new ArrayList();
        List<DataTypeSupplementaryComponent> cdtsclistfromDB = new ArrayList();
        for (DataType cdt : cdtlist) {
            cdtsclistfromDB.addAll(dtScRepository.findByOwnerDtId(cdt.getDtId()));
        }

        for (DataTypeSupplementaryComponent cdtsc : cdtsclistfromDB) {
            Cdt aCdt = new Cdt(cdtsc.getDtScId());
            if (cdt_sc_list.indexOf(aCdt.getCdtData()) == -1)
                System.out.println("Data error in dt. Data is " + aCdt.getCdtData());
            cdtscFromDB.add(aCdt.getCdtData());
        }

        for (String cdtsc : cdt_sc_list) {
            if (cdtscFromDB.indexOf(cdtsc) == -1)
                System.out.println("Data may be missing in the dt_sc table. Data is " + cdtsc);
        }

    }

    private void validate_cdt_sc_awd_pri_xps_type_map() throws Exception {
        List<String> cdtscxpsMapData = new ArrayList();

        cdtscxpsMapData.add("AmountCurrencyCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("AmountCurrencyCodeStringxsd:string");
        cdtscxpsMapData.add("AmountCurrencyCodeTokenxsd:token");
        cdtscxpsMapData.add("Binary ObjectMIMECodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("Binary ObjectMIMECodeStringxsd:string");
        cdtscxpsMapData.add("Binary ObjectMIMECodeTokenxsd:token");
        cdtscxpsMapData.add("Binary ObjectCharacter SetCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("Binary ObjectCharacter SetCodeStringxsd:string");
        cdtscxpsMapData.add("Binary ObjectCharacter SetCodeTokenxsd:token");
        cdtscxpsMapData.add("Binary ObjectFilenameNameNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("Binary ObjectFilenameNameStringxsd:string");
        cdtscxpsMapData.add("Binary ObjectFilenameNameTokenxsd:token");
        cdtscxpsMapData.add("CodeListIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("CodeListIdentifierStringxsd:string");
        cdtscxpsMapData.add("CodeListIdentifierTokenxsd:token");
        cdtscxpsMapData.add("CodeList AgencyIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("CodeList AgencyIdentifierStringxsd:string");
        cdtscxpsMapData.add("CodeList AgencyIdentifierTokenxsd:token");
        cdtscxpsMapData.add("CodeList VersionIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("CodeList VersionIdentifierStringxsd:string");
        cdtscxpsMapData.add("CodeList VersionIdentifierTokenxsd:token");

        cdtscxpsMapData.add("Date TimeTime ZoneCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("Date TimeTime ZoneCodeStringxsd:string");
        cdtscxpsMapData.add("Date TimeTime ZoneCodeTokenxsd:token");

        cdtscxpsMapData.add("Date TimeDaylight SavingIndicatorBooleanxsd:boolean");

        cdtscxpsMapData.add("GraphicMIMECodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("GraphicMIMECodeStringxsd:string");
        cdtscxpsMapData.add("GraphicMIMECodeTokenxsd:token");
        cdtscxpsMapData.add("GraphicCharacter SetCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("GraphicCharacter SetCodeStringxsd:string");
        cdtscxpsMapData.add("GraphicCharacter SetCodeTokenxsd:token");
        cdtscxpsMapData.add("GraphicFilenameNameNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("GraphicFilenameNameStringxsd:string");
        cdtscxpsMapData.add("GraphicFilenameNameTokenxsd:token");

        cdtscxpsMapData.add("IdentifierSchemeIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("IdentifierSchemeIdentifierStringxsd:string");
        cdtscxpsMapData.add("IdentifierSchemeIdentifierTokenxsd:token");
        cdtscxpsMapData.add("IdentifierScheme VersionIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("IdentifierScheme VersionIdentifierStringxsd:string");
        cdtscxpsMapData.add("IdentifierScheme VersionIdentifierTokenxsd:token");
        cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierStringxsd:string");
        cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierTokenxsd:token");

        cdtscxpsMapData.add("MeasureUnitCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("MeasureUnitCodeStringxsd:string");
        cdtscxpsMapData.add("MeasureUnitCodeTokenxsd:token");

        cdtscxpsMapData.add("NameLanguageCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("NameLanguageCodeStringxsd:string");
        cdtscxpsMapData.add("NameLanguageCodeTokenxsd:token");

        cdtscxpsMapData.add("PictureMIMECodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("PictureMIMECodeStringxsd:string");
        cdtscxpsMapData.add("PictureMIMECodeTokenxsd:token");
        cdtscxpsMapData.add("PictureCharacter SetCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("PictureCharacter SetCodeStringxsd:string");
        cdtscxpsMapData.add("PictureCharacter SetCodeTokenxsd:token");
        cdtscxpsMapData.add("PictureFilenameNameNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("PictureFilenameNameStringxsd:string");
        cdtscxpsMapData.add("PictureFilenameNameTokenxsd:token");

        cdtscxpsMapData.add("QuantityUnitCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("QuantityUnitCodeStringxsd:string");
        cdtscxpsMapData.add("QuantityUnitCodeTokenxsd:token");

        cdtscxpsMapData.add("RateMultiplierValueDecimalxsd:decimal");
        cdtscxpsMapData.add("RateMultiplierValueDoublexsd:double");
        cdtscxpsMapData.add("RateMultiplierValueDoublexsd:float");
        cdtscxpsMapData.add("RateMultiplierValueFloatxsd:float");
        cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:integer");
        cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:nonNegativeInteger");
        cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:positiveInteger");

        cdtscxpsMapData.add("RateUnitCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("RateUnitCodeStringxsd:string");
        cdtscxpsMapData.add("RateUnitCodeTokenxsd:token");

        cdtscxpsMapData.add("RateCurrencyCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("RateCurrencyCodeStringxsd:string");
        cdtscxpsMapData.add("RateCurrencyCodeTokenxsd:token");


        cdtscxpsMapData.add("RateBase MultiplierValueDecimalxsd:decimal");
        cdtscxpsMapData.add("RateBase MultiplierValueDoublexsd:double");
        cdtscxpsMapData.add("RateBase MultiplierValueDoublexsd:float");
        cdtscxpsMapData.add("RateBase MultiplierValueFloatxsd:float");
        cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:integer");
        cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:nonNegativeInteger");
        cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:positiveInteger");

        cdtscxpsMapData.add("RateBase UnitCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("RateBase UnitCodeStringxsd:string");
        cdtscxpsMapData.add("RateBase UnitCodeTokenxsd:token");

        cdtscxpsMapData.add("RateBase CurrencyCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("RateBase CurrencyCodeStringxsd:string");
        cdtscxpsMapData.add("RateBase CurrencyCodeTokenxsd:token");

        cdtscxpsMapData.add("SoundMIMECodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("SoundMIMECodeStringxsd:string");
        cdtscxpsMapData.add("SoundMIMECodeTokenxsd:token");
        cdtscxpsMapData.add("SoundCharacter SetCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("SoundCharacter SetCodeStringxsd:string");
        cdtscxpsMapData.add("SoundCharacter SetCodeTokenxsd:token");
        cdtscxpsMapData.add("SoundFilenameNameNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("SoundFilenameNameStringxsd:string");
        cdtscxpsMapData.add("SoundFilenameNameTokenxsd:token");

        cdtscxpsMapData.add("TextLanguageCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("TextLanguageCodeStringxsd:string");
        cdtscxpsMapData.add("TextLanguageCodeTokenxsd:token");

        cdtscxpsMapData.add("VideoMIMECodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("VideoMIMECodeStringxsd:string");
        cdtscxpsMapData.add("VideoMIMECodeTokenxsd:token");
        cdtscxpsMapData.add("VideoCharacter SetCodeNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("VideoCharacter SetCodeStringxsd:string");
        cdtscxpsMapData.add("VideoCharacter SetCodeTokenxsd:token");
        cdtscxpsMapData.add("VideoFilenameNameNormalizedStringxsd:normalizedString");
        cdtscxpsMapData.add("VideoFilenameNameStringxsd:string");
        cdtscxpsMapData.add("VideoFilenameNameTokenxsd:token");

        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository aCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap =
                repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository();
        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapList =
                aCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap.findAll();
        ArrayList<String> cdtscxpsMapDataFromDB = new ArrayList<String>();
        for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdt_sc_allowed_primitive_expression_type_map : CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapList) {
            CdtSupplementaryComponentExpressionTypeMap xpsMap = new CdtSupplementaryComponentExpressionTypeMap(cdt_sc_allowed_primitive_expression_type_map.getCdtScAwdPriXpsTypeMapId());
            if (cdtscxpsMapData.indexOf(xpsMap.getXpsMapData()) == -1)
                System.out.println("Data error in cdt_sc_awd_pri_xps_map. Data is " + xpsMap.getXpsMapData());
            cdtscxpsMapDataFromDB.add(xpsMap.getXpsMapData());
        }

        for (String xpsMap : cdtscxpsMapData) {
            if (cdtscxpsMapDataFromDB.indexOf(xpsMap) == -1)
                System.out.println("Data may be missing in the cdt_sc_awd_pri_xps_map table. Data is " + xpsMap);
        }
    }

    private void validate_cdt_awd_pri_xps_type_map() throws Exception {
        List<String> xpsMapData = new ArrayList();
        xpsMapData.add("AmountDecimalxsd:decimal");
        xpsMapData.add("AmountDoublexsd:double");
        xpsMapData.add("AmountDoublexsd:float");
        xpsMapData.add("AmountFloatxsd:float");
        xpsMapData.add("AmountIntegerxsd:integer");
        xpsMapData.add("AmountIntegerxsd:nonNegativeInteger");
        xpsMapData.add("AmountIntegerxsd:positiveInteger");

        xpsMapData.add("Binary ObjectBinaryxsd:base64Binary");
        xpsMapData.add("Binary ObjectBinaryxsd:hexBinary");

        xpsMapData.add("CodeNormalizedStringxsd:normalizedString");
        xpsMapData.add("CodeStringxsd:string");
        xpsMapData.add("CodeTokenxsd:token");
        xpsMapData.add("DateTimePointxsd:token");
        xpsMapData.add("DateTimePointxsd:date");
        xpsMapData.add("DateTimePointxsd:time");
        xpsMapData.add("DateTimePointxsd:gYearMonth");
        xpsMapData.add("DateTimePointxsd:gYear");
        xpsMapData.add("DateTimePointxsd:gMonthDay");
        xpsMapData.add("DateTimePointxsd:gDay");
        xpsMapData.add("DateTimePointxsd:gMonth");

        xpsMapData.add("Date TimeTimePointxsd:token");
        xpsMapData.add("Date TimeTimePointxsd:dateTime");
        xpsMapData.add("Date TimeTimePointxsd:date");
        xpsMapData.add("Date TimeTimePointxsd:time");
        xpsMapData.add("Date TimeTimePointxsd:gYearMonth");
        xpsMapData.add("Date TimeTimePointxsd:gYear");
        xpsMapData.add("Date TimeTimePointxsd:gMonthDay");
        xpsMapData.add("Date TimeTimePointxsd:gDay");
        xpsMapData.add("Date TimeTimePointxsd:gMonth");

        xpsMapData.add("DurationTimeDurationxsd:duration");
        xpsMapData.add("DurationTimeDurationxsd:token");

        xpsMapData.add("GraphicBinaryxsd:base64Binary");
        xpsMapData.add("GraphicBinaryxsd:hexBinary");

        xpsMapData.add("IdentifierNormalizedStringxsd:normalizedString");
        xpsMapData.add("IdentifierStringxsd:string");
        xpsMapData.add("IdentifierTokenxsd:token");

        xpsMapData.add("IndicatorBooleanxsd:boolean");

        xpsMapData.add("MeasureDecimalxsd:decimal");
        xpsMapData.add("MeasureDoublexsd:double");
        xpsMapData.add("MeasureDoublexsd:float");
        xpsMapData.add("MeasureFloatxsd:float");
        xpsMapData.add("MeasureIntegerxsd:integer");
        xpsMapData.add("MeasureIntegerxsd:nonNegativeInteger");
        xpsMapData.add("MeasureIntegerxsd:positiveInteger");

        xpsMapData.add("NameNormalizedStringxsd:normalizedString");
        xpsMapData.add("NameStringxsd:string");
        xpsMapData.add("NameTokenxsd:token");

        xpsMapData.add("NumberDecimalxsd:decimal");
        xpsMapData.add("NumberDoublexsd:double");
        xpsMapData.add("NumberDoublexsd:float");
        xpsMapData.add("NumberFloatxsd:float");
        xpsMapData.add("NumberIntegerxsd:integer");
        xpsMapData.add("NumberIntegerxsd:nonNegativeInteger");
        xpsMapData.add("NumberIntegerxsd:positiveInteger");

        xpsMapData.add("OrdinalIntegerxsd:integer");
        xpsMapData.add("OrdinalIntegerxsd:nonNegativeInteger");
        xpsMapData.add("OrdinalIntegerxsd:positiveInteger");

        xpsMapData.add("PercentDecimalxsd:decimal");
        xpsMapData.add("PercentDoublexsd:double");
        xpsMapData.add("PercentDoublexsd:float");
        xpsMapData.add("PercentFloatxsd:float");
        xpsMapData.add("PercentIntegerxsd:integer");
        xpsMapData.add("PercentIntegerxsd:nonNegativeInteger");
        xpsMapData.add("PercentIntegerxsd:positiveInteger");

        xpsMapData.add("PictureBinaryxsd:base64Binary");
        xpsMapData.add("PictureBinaryxsd:hexBinary");

        xpsMapData.add("QuantityDecimalxsd:decimal");
        xpsMapData.add("QuantityDoublexsd:double");
        xpsMapData.add("QuantityDoublexsd:float");
        xpsMapData.add("QuantityFloatxsd:float");
        xpsMapData.add("QuantityIntegerxsd:integer");
        xpsMapData.add("QuantityIntegerxsd:nonNegativeInteger");
        xpsMapData.add("QuantityIntegerxsd:positiveInteger");

        xpsMapData.add("RateDecimalxsd:decimal");
        xpsMapData.add("RateDoublexsd:double");
        xpsMapData.add("RateDoublexsd:float");
        xpsMapData.add("RateFloatxsd:float");
        xpsMapData.add("RateIntegerxsd:integer");
        xpsMapData.add("RateIntegerxsd:nonNegativeInteger");
        xpsMapData.add("RateIntegerxsd:positiveInteger");

        xpsMapData.add("RatioDecimalxsd:decimal");
        xpsMapData.add("RatioDoublexsd:double");
        xpsMapData.add("RatioDoublexsd:float");
        xpsMapData.add("RatioFloatxsd:float");
        xpsMapData.add("RatioIntegerxsd:integer");
        xpsMapData.add("RatioIntegerxsd:nonNegativeInteger");
        xpsMapData.add("RatioIntegerxsd:positiveInteger");
        xpsMapData.add("RatioStringxsd:string");

        xpsMapData.add("SoundBinaryxsd:base64Binary");
        xpsMapData.add("SoundBinaryxsd:hexBinary");

        xpsMapData.add("TextNormalizedStringxsd:normalizedString");
        xpsMapData.add("TextStringxsd:string");
        xpsMapData.add("TextTokenxsd:token");

        xpsMapData.add("TimeTimePointxsd:token");
        xpsMapData.add("TimeTimePointxsd:time");

        xpsMapData.add("ValueDecimalxsd:decimal");
        xpsMapData.add("ValueDoublexsd:double");
        xpsMapData.add("ValueDoublexsd:float");
        xpsMapData.add("ValueFloatxsd:float");
        xpsMapData.add("ValueIntegerxsd:integer");
        xpsMapData.add("ValueIntegerxsd:nonNegativeInteger");
        xpsMapData.add("ValueIntegerxsd:positiveInteger");
        xpsMapData.add("ValueNormalizedStringxsd:normalizedString");
        xpsMapData.add("ValueTokenxsd:token");
        xpsMapData.add("ValueStringxsd:string");

        xpsMapData.add("VideoBinaryxsd:base64Binary");
        xpsMapData.add("VideoBinaryxsd:hexBinary");

        List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAllowedPrimitiveExpressionTypeMapList =
                cdtAwdPriXpsTypeMapRepository.findAll();
        List<String> xpsMapDataFromDB = new ArrayList();
        for (CoreDataTypeAllowedPrimitiveExpressionTypeMap xpsMap : cdtAllowedPrimitiveExpressionTypeMapList) {
            CdtPrimitiveExpressionTypeMap cdtPrimitiveExpressionTypeMap = new CdtPrimitiveExpressionTypeMap(xpsMap.getCdtAwdPriXpsTypeMapId());
            if (xpsMapData.indexOf(cdtPrimitiveExpressionTypeMap.getXpsMapData()) == -1)
                System.out.println("Data error in cdt_awd_pri_xps_map. Data is " + cdtPrimitiveExpressionTypeMap.getXpsMapData());
            xpsMapDataFromDB.add(cdtPrimitiveExpressionTypeMap.getXpsMapData());
        }

        for (String xpsMap : xpsMapData) {
            if (xpsMapDataFromDB.indexOf(xpsMap) == -1)
                System.out.println("Data may be missing in the cdt_awd_pri_xps_map table. Data is " + xpsMap);
        }
    }

    private void validate_agency_id_list() throws Exception {
        if (agencyIdListValueRepository.count() == 395)
            System.out.println("# of Agency Id List Values is correct");
    }

    class Cdt {
        int id;
        String cdtDataTypeTerm;
        String scPropertyTerm;
        String scRepresentationTerm;
        int minCardinality;
        int maxCardinality;

        public Cdt(int id) throws Exception {
            DataTypeSupplementaryComponent dataTypeSupplementaryComponent = dtScRepository.findOne(id);
            int dt_id = dataTypeSupplementaryComponent.getOwnerDtId();

            DataType aDataType = dataTypeRepository.findOne(dt_id);

            cdtDataTypeTerm = aDataType.getDataTypeTerm();

            scPropertyTerm = dataTypeSupplementaryComponent.getPropertyTerm();
            scRepresentationTerm = dataTypeSupplementaryComponent.getRepresentationTerm();
            minCardinality = dataTypeSupplementaryComponent.getMinCardinality();
            maxCardinality = dataTypeSupplementaryComponent.getMaxCardinality();
        }

        public String getCdtData() {
            return this.cdtDataTypeTerm + this.scPropertyTerm + this.scRepresentationTerm + this.minCardinality + this.maxCardinality;
        }

    }

    class CdtPrimitiveExpressionTypeMap {
        int id;
        String cdtDataTypeTerm;
        String primitiveName;
        String builtInType;

        public CdtPrimitiveExpressionTypeMap(int id) throws Exception {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAllowedPrimitiveExpressionTypeMap =
                    cdtAwdPriXpsTypeMapRepository.findOne(id);
            int awdPriId = cdtAllowedPrimitiveExpressionTypeMap.getCdtAwdPriId();
            int xbtId = cdtAllowedPrimitiveExpressionTypeMap.getXbtId();

            CoreDataTypeAllowedPrimitive cdtAllowedPrimitive = cdtAwdPriRepository.findOne(awdPriId);

            CoreDataTypePrimitive cdtPrimitive = cdtPriRepository.findOne(cdtAllowedPrimitive.getCdtPriId());
            primitiveName = cdtPrimitive.getName();

            DataType cdt = dataTypeRepository.findOne(cdtAllowedPrimitive.getCdtId());
            cdtDataTypeTerm = cdt.getDataTypeTerm();

            XSDBuiltInType xsdBuiltInType = xbtRepository.findOne(xbtId);
            builtInType = xsdBuiltInType.getBuiltInType();

        }

        public String getXpsMapData() {
            return this.cdtDataTypeTerm + this.primitiveName + this.builtInType;
        }
    }

    class CdtSupplementaryComponentExpressionTypeMap {
        int id;
        String cdtDataTypeTerm;
        String primitiveName;
        String builtInType;
        String scPropertyTerm;
        String scRepresentationTerm;

        public CdtSupplementaryComponentExpressionTypeMap(int id) throws Exception {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository =
                    repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository();
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                    cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriXpsTypeMapId(id);
            int cdtScAwdPriId = cdtScAwdPriXpsTypeMap.getCdtScAwdPri();
            int xbtId = cdtScAwdPriXpsTypeMap.getXbtId();

            CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository =
                    repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveRepository();
            CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri = cdtScAwdPriRepository.findOneByCdtScAwdPriId(cdtScAwdPriId);

            CoreDataTypePrimitive cdtPrimitive = cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId());
            primitiveName = cdtPrimitive.getName();

            DataTypeSupplementaryComponent cdtsc = dtScRepository.findOne(cdtScAwdPri.getCdtScId());
            scPropertyTerm = cdtsc.getPropertyTerm();
            scRepresentationTerm = cdtsc.getRepresentationTerm();

            DataType cdt = dataTypeRepository.findOne(cdtsc.getOwnerDtId());
            cdtDataTypeTerm = cdt.getDataTypeTerm();

            XSDBuiltInType xsdBuiltInType = xbtRepository.findOne(xbtId);
            builtInType = xsdBuiltInType.getBuiltInType();

        }

        public String getXpsMapData() {
            return this.cdtDataTypeTerm + this.scPropertyTerm + this.scRepresentationTerm + this.primitiveName + this.builtInType;
        }

    }

    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### cdt Validation Start");

        check_number_of_cdt();
        check_number_of_XBT();
        validate_cdt_awd_pri_xps_type_map();
        validate_cdt_sc();
        validate_cdt_sc_awd_pri_xps_type_map();
        validate_agency_id_list();

        System.out.println("### cdt Validation End");
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            CDTTest cdtTest = ctx.getBean(CDTTest.class);
            cdtTest.run(ctx);
        }
    }
}
