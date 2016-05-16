package org.oagi.srt.persistence.validate;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class CDTTest {
	
	
	public int getCodeListID(String codeName) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCodeListDAO = df.getDAO("CodeList");
		QueryCondition qc = new QueryCondition();
		qc.addLikeClause("Name", "%" + codeName.trim() + "%");
		CodeListVO codelistVO = (CodeListVO)aCodeListDAO.findObject(qc, conn);
		return codelistVO.getCodeListID();
	}
	
	public int getAgencyListID() throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
    	QueryCondition qc = new QueryCondition();
		qc.add("name", "Agency Identification");
		AgencyIDListVO agencyidlistVO = (AgencyIDListVO)dao.findObject(qc);
		return agencyidlistVO.getAgencyIDListID();
	}
	
	public DTSCVO getDTSCVO(String guid) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", guid);
		return (DTSCVO)aDTSCDAO.findObject(qc);
	}
	
	public DTSCVO getDTSCVO(String guid, int ownerId) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", guid);
		qc.add("owner_dt_id", ownerId);
		return (DTSCVO)aDTSCDAO.findObject(qc);
	}
	
	public int getDTID(String DataTypeTerm) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(DataTypeTerm));
		qc.add("Type", 0);
		DTVO dtVO = (DTVO)aDTDAO.findObject(qc);		
		int id = dtVO.getDTID();
		return id;
	}
	
	public int getCDTSCID(int DTSCID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", DTSCID);
		CDTSCAllowedPrimitiveVO cdtVO = (CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitiveDAO.findObject(qc);
		int id = cdtVO.getCDTSCID();
		return id;
	}
	
	public String getRepresentationTerm(String DTSCGUID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", new String(DTSCGUID));
		DTSCVO dtscVO = (DTSCVO)aDTSCDAO.findObject(qc);
		String term = dtscVO.getRepresentationTerm();
		return term;
	}
	
	public String getPrimitiveName(int CDTPrimitiveID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_Pri_ID", CDTPrimitiveID);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getName();
	}
	
	
	public int getCDTPrimitiveID(String name) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("Name",  name);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getCDTPrimitiveID();
	}
	
	public ArrayList<SRTObject> getCDTAllowedPrimitiveIDs(int cdt_id) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_ID", cdt_id);
		return aCDTAllowedPrimitiveDAO.findObjects(qc, conn);
	}
	
	public List<SRTObject> getCdtSCAllowedPrimitiveID(int dt_sc_id) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", dt_sc_id);
		List<SRTObject> res = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
		if(res.size() < 1) {
			QueryCondition qc_01 = new QueryCondition();
			qc_01.add("DT_SC_ID", dt_sc_id);
			DTSCVO dtscVO = (DTSCVO)aDTSCDAO.findObject(qc_01);
			res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDTSCID());
		}
		return res;
	}
	
	private void check_number_of_CDT() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 0);
		List<SRTObject> cdt = aDTDAO.findObjects(qc, conn);
		System.out.println("# of CDTs in catalog : "+cdt.size());
		if(cdt.size() == 23)
			System.out.println("Validated");
	}
	
	private void check_number_of_XBT() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
		List<SRTObject> xsd = aXSDBuiltInTypeDAO.findObjects();
		System.out.println("# of XBTs in catalog : "+xsd.size());
		for(int i = 0 ; i < xsd.size(); i++){
			for(int j = i+1 ; j < xsd.size(); j++){
				if(((XSDBuiltInTypeVO)(xsd.get(i))).getName().equalsIgnoreCase((((XSDBuiltInTypeVO)(xsd.get(j))).getName())))
						System.out.println("Strange data; see the "+i+"th data and "+j+" th data");
			}
		}
		if(xsd.size() == 24)
			System.out.println("Validated");
	}
	
	private void validate_cdt_sc() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 0);
		List<SRTObject> cdtlist = aDTDAO.findObjects(qc, conn);
		ArrayList<String> cdt_sc_list = new ArrayList<String>();
		
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
		
		
		ArrayList<String> cdtscFromDB = new ArrayList<String>();
		ArrayList<SRTObject> cdtsclistfromDB = new ArrayList<SRTObject>();
		for(SRTObject cdt : cdtlist){
			SRTDAO aDTSCDAO = df.getDAO("DTSC");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("owner_dt_id", ((DTVO)cdt).getDTID() );
			cdtsclistfromDB.addAll(aDTSCDAO.findObjects(qc2, conn));
		}
		
		for(SRTObject cdtsc : cdtsclistfromDB) {
			CDT aCDT = new CDT(((DTSCVO)cdtsc).getDTSCID());
			if(cdt_sc_list.indexOf(aCDT.getCDTData()) == -1) 
				System.out.println("Data error in dt. Data is " + aCDT.getCDTData());
			cdtscFromDB.add(aCDT.getCDTData());
		}
		
		for(String cdtsc : cdt_sc_list){
				if(cdtscFromDB.indexOf(cdtsc) == -1 ) System.out.println("Data may be missing in the dt_sc table. Data is " + cdtsc);
		}
		
	}
		
	private void validate_cdt_sc_awd_pri_xps_type_map() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		ArrayList<String> cdtscxpsMapData = new ArrayList<String>(); 
		
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
		
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		List<SRTObject> CDTSCAllowedPrimitiveExpressionTypeMapList = aCDTSCAllowedPrimitiveExpressionTypeMap.findObjects();
		ArrayList<String> cdtscxpsMapDataFromDB = new ArrayList<String>();
		for(SRTObject cdt_sc_allowed_primitive_expression_type_map : CDTSCAllowedPrimitiveExpressionTypeMapList){
				CDTSCExpressionTypeMap xpsMap = new CDTSCExpressionTypeMap(((CDTSCAllowedPrimitiveExpressionTypeMapVO)cdt_sc_allowed_primitive_expression_type_map).getCTSCAllowedPrimitiveExpressionTypeMapID());
				if (cdtscxpsMapData.indexOf(xpsMap.getXpsMapData()) == -1) System.out.println("Data error in cdt_sc_awd_pri_xps_map. Data is " + xpsMap.getXpsMapData());
				cdtscxpsMapDataFromDB.add(xpsMap.getXpsMapData());
		}
		
		for (String xpsMap : cdtscxpsMapData){
			if(cdtscxpsMapDataFromDB.indexOf(xpsMap) == -1) System.out.println("Data may be missing in the cdt_sc_awd_pri_xps_map table. Data is " + xpsMap);
		}	
	}
	
	private void validate_cdt_awd_pri_xps_type_map() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		ArrayList<String> xpsMapData = new ArrayList<String>(); 
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
		
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		List<SRTObject> CDTAllowedPrimitiveExpressionTypeMapList = aCDTAllowedPrimitiveExpressionTypeMap.findObjects();
		ArrayList<String> xpsMapDataFromDB = new ArrayList<String>();
		for(SRTObject cdt_allowed_primitive_expression_type_map : CDTAllowedPrimitiveExpressionTypeMapList){
				CDTPrimitiveExpressionTypeMap xpsMap = new CDTPrimitiveExpressionTypeMap(((CDTAllowedPrimitiveExpressionTypeMapVO)cdt_allowed_primitive_expression_type_map).getCDTPrimitiveExpressionTypeMapID());
				if (xpsMapData.indexOf(xpsMap.getXpsMapData()) == -1) System.out.println("Data error in cdt_awd_pri_xps_map. Data is " + xpsMap.getXpsMapData());
				xpsMapDataFromDB.add(xpsMap.getXpsMapData());
		}
		
		for (String xpsMap : xpsMapData){
			if(xpsMapDataFromDB.indexOf(xpsMap) == -1) System.out.println("Data may be missing in the cdt_awd_pri_xps_map table. Data is " + xpsMap);
		}		
			
	}
	
	private void validate_agency_id_list() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aAgencyIDListValue = df.getDAO("AgencyIDListValue");
		List<SRTObject> AgencyIDListValueList = aAgencyIDListValue.findObjects();
		if(AgencyIDListValueList.size() == 395)
			System.out.println("# of Agency ID List Values is correct");
	}
	
	class CDT {
		int id;
		String cdtDTTerm;
		String scPropertyTerm;
		String scRepresentationTerm;
		int minCardinality;
		int maxCardinality;
		
		public CDT(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();

			SRTDAO aDT = df.getDAO("DT");
			SRTDAO aDTSC = df.getDAO("DTSC");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("dt_sc_id", id);
			DTSCVO aDTSCVO = (DTSCVO) aDTSC.findObject(qc2, conn);
			int dt_id = aDTSCVO.getOwnerDTID();
			
			QueryCondition qc = new QueryCondition();
			qc.add("dt_id", dt_id);
			DTVO aDTVO= (DTVO)aDT.findObject(qc, conn);
			
			cdtDTTerm = aDTVO.getDataTypeTerm();
			
			scPropertyTerm = aDTSCVO.getPropertyTerm();
			scRepresentationTerm = aDTSCVO.getRepresentationTerm();
			minCardinality = aDTSCVO.getMinCardinality();
			maxCardinality = aDTSCVO.getMaxCardinality();
		}
		
		public String getCDTData() {
			return this.cdtDTTerm + this.scPropertyTerm + this.scRepresentationTerm + this.minCardinality + this.maxCardinality;
		}
		
	}
	class CDTPrimitiveExpressionTypeMap {
		int id;
		String cdtDTTerm;
		String primitiveName;
		String xsdBuiltInType;
		
		public CDTPrimitiveExpressionTypeMap(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();
			SRTDAO aCDTAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			QueryCondition qc = new QueryCondition();
			qc.add("cdt_awd_pri_xps_type_map_id", id);
			CDTAllowedPrimitiveExpressionTypeMapVO cdtAllowedPrimitiveExpressionTypeMapVO = (CDTAllowedPrimitiveExpressionTypeMapVO) aCDTAllowedPrimitiveExpressionTypeMap.findObject(qc, conn);
			int awdPriId = cdtAllowedPrimitiveExpressionTypeMapVO.getCDTAllowedPrimitiveID();
			int xbtId = cdtAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID();
			
			SRTDAO aCDTAllowedPrimitive = df.getDAO("CDTAllowedPrimitive");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("cdt_awd_pri_id", awdPriId);
			CDTAllowedPrimitiveVO cdtAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aCDTAllowedPrimitive.findObject(qc2, conn);
			
			SRTDAO aCDTPrimitive = df.getDAO("CDTPrimitive");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_pri_id", cdtAllowedPrimitiveVO.getCDTPrimitiveID());
			CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitive.findObject(qc3, conn);
			primitiveName = cdtPrimitiveVO.getName();
			
			SRTDAO aDT = df.getDAO("DT");
			QueryCondition qc4 = new QueryCondition();
			qc4.add("dt_id", cdtAllowedPrimitiveVO.getCDTID());
			DTVO cdtVO = (DTVO) aDT.findObject(qc4, conn);
			cdtDTTerm = cdtVO.getDataTypeTerm();
			
			SRTDAO aXbt = df.getDAO("XSDBuiltInType");
			QueryCondition qc5 = new QueryCondition();
			qc5.add("xbt_id", xbtId);
			XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO) aXbt.findObject(qc5, conn);
			xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();
			
		}
		
		public String getXpsMapData() {
			return this.cdtDTTerm + this.primitiveName + this.xsdBuiltInType;
		}
	}
	
	class CDTSCExpressionTypeMap {
		int id;
		String cdtDTTerm;
		String primitiveName;
		String xsdBuiltInType;
		String scPropertyTerm;
		String scRepresentationTerm;
		
		public CDTSCExpressionTypeMap(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();
			SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
			QueryCondition qc = new QueryCondition();
			qc.add("cdt_sc_awd_pri_xps_type_map_id", id);
			CDTSCAllowedPrimitiveExpressionTypeMapVO cdtscAllowedPrimitiveExpressionTypeMapVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO) aCDTSCAllowedPrimitiveExpressionTypeMap.findObject(qc, conn);
			int cdtScAwdPriId = cdtscAllowedPrimitiveExpressionTypeMapVO.getCDTSCAllowedPrimitive();
			int xbtId = cdtscAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID();
			
			SRTDAO aCDTSCAllowedPrimitive = df.getDAO("CDTSCAllowedPrimitive");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("cdt_sc_awd_pri_id", cdtScAwdPriId);
			CDTSCAllowedPrimitiveVO cdtscAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitive.findObject(qc2, conn);
			
			SRTDAO aCDTPrimitive = df.getDAO("CDTPrimitive");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_pri_id", cdtscAllowedPrimitiveVO.getCDTPrimitiveID());
			CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitive.findObject(qc3, conn);
			primitiveName = cdtPrimitiveVO.getName();
			
			SRTDAO aDTSC = df.getDAO("DTSC");
			QueryCondition qc4 = new QueryCondition();
			qc4.add("dt_sc_id", cdtscAllowedPrimitiveVO.getCDTSCID());
			DTSCVO cdtscVO = (DTSCVO) aDTSC.findObject(qc4, conn);
			scPropertyTerm = cdtscVO.getPropertyTerm();
			scRepresentationTerm = cdtscVO.getRepresentationTerm();
			
			SRTDAO aDT = df.getDAO("DT");
			QueryCondition qc5 = new QueryCondition();
			qc5.add("dt_id", cdtscVO.getOwnerDTID());
			DTVO cdtVO = (DTVO) aDT.findObject(qc5, conn);
			cdtDTTerm = cdtVO.getDataTypeTerm();
			
			SRTDAO aXbt = df.getDAO("XSDBuiltInType");
			QueryCondition qc6 = new QueryCondition();
			qc6.add("xbt_id", xbtId);
			XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO) aXbt.findObject(qc6, conn);
			xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();
			
		}

		public String getXpsMapData() {
			return this.cdtDTTerm + this.scPropertyTerm + this.scRepresentationTerm + this.primitiveName + this.xsdBuiltInType;
		}
		
	}
	
	private static Connection conn = null;
	
	public void run() throws Exception {
		System.out.println("### CDT Validation Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		check_number_of_CDT();
		check_number_of_XBT();
		validate_cdt_awd_pri_xps_type_map();
		validate_cdt_sc();
		validate_cdt_sc_awd_pri_xps_type_map();
		validate_agency_id_list();
		System.out.println("### CDT Validation End");
	}
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		CDTTest p = new CDTTest();
		p.run();
	}
}
