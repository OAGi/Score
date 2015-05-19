package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class P_1_6_6_PopulateBDTSCPrimitiveRestriction {
	public void scindt_sc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BDT_Primitive_Restriction")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTSCPrimitiveRestriction");
		BDTSCPrimitiveRestrictionVO bdt_sc_primitive_restrictionVO = new BDTSCPrimitiveRestrictionVO();

		NodeList ref = xh.getNodeList("//xsd:complexType");
		for (int j = 0; j < ref.getLength(); j++) {
			Element ref_tmp = (Element) ref.item(j);
			NodeList result = xh.getNodeList("//xsd:complexType[@id = '"+ ref_tmp.getAttribute("id") + "']//xsd:attribute");				
			for (int i = 0; i < result.getLength(); i++) {
				Element tmp = (Element) result.item(i);
				bdt_sc_primitive_restrictionVO.setBDTSCID(getDTSCID(tmp.getAttribute("id")));

				if(tmp.getAttribute("name").equals("languageCode")){
					ArrayList<SRTObject> cdtallowedprimitivelist = new ArrayList<SRTObject>();
					
					cdtallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getOwnerDTID("Expression.Type"));
					for(SRTObject dvo2 : cdtallowedprimitivelist){
						CDTSCAllowedPrimitiveVO svo2 = (CDTSCAllowedPrimitiveVO) dvo2;
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(svo2.getCDTSCAllowedPrimitiveID()));
						bdt_sc_primitive_restrictionVO.setCodeListID(getCodeListID("clm56392A20081107_LanguageCode"));
						bdt_sc_primitive_restrictionVO.setisDefault(svo2.getisDefault());
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					
					cdtallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getOwnerDTID("ActionExpression.Type"));
					for(SRTObject dvo2 : cdtallowedprimitivelist){
						CDTSCAllowedPrimitiveVO svo2 = (CDTSCAllowedPrimitiveVO) dvo2;
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(svo2.getCDTSCAllowedPrimitiveID()));
						bdt_sc_primitive_restrictionVO.setCodeListID(getCodeListID("clm56392A20081107_LanguageCode"));
						bdt_sc_primitive_restrictionVO.setisDefault(svo2.getisDefault());
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					
					cdtallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getOwnerDTID("ResponseExpression.Type"));
					for(SRTObject dvo2 : cdtallowedprimitivelist){
						CDTSCAllowedPrimitiveVO svo2 = (CDTSCAllowedPrimitiveVO) dvo2;
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(svo2.getCDTSCAllowedPrimitiveID()));
						bdt_sc_primitive_restrictionVO.setCodeListID(getCodeListID("clm56392A20081107_LanguageCode"));
						bdt_sc_primitive_restrictionVO.setisDefault(svo2.getisDefault());
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}																													
				}
				
				else if(tmp.getAttribute("name").equals("expressionLanguage")){
					if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString")),getXSDBuiltInTypeID("normalized string")));
						bdt_sc_primitive_restrictionVO.setisDefault(false);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("String"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("String")),getXSDBuiltInTypeID("string")));
						bdt_sc_primitive_restrictionVO.setisDefault(false);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("Token"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("Token")),getXSDBuiltInTypeID("token")));
						bdt_sc_primitive_restrictionVO.setisDefault(true);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
				}
				else if(tmp.getAttribute("name").equals("actionCode")){
					if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString")),getXSDBuiltInTypeID("normalized string")));
						bdt_sc_primitive_restrictionVO.setisDefault(false);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("String"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("String")),getXSDBuiltInTypeID("string")));
						bdt_sc_primitive_restrictionVO.setisDefault(false);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}
					else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("Token"))!=0) {
						bdt_sc_primitive_restrictionVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveExpressionTypeMapID(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("Token")),getXSDBuiltInTypeID("token")));
						bdt_sc_primitive_restrictionVO.setisDefault(false);
						dao.insertObject(bdt_sc_primitive_restrictionVO);
					}	
				}
				
				ArrayList<Integer> bdtid = new ArrayList<Integer>();
				bdtid = getBDTSCPrimitiveRestrictionID(getCodeListID("oacl_ActionCode"));				
				if(bdtid.size() != 0){
					bdt_sc_primitive_restrictionVO.setCodeListID(getCodeListID("oacl_ActionCode"));
					bdt_sc_primitive_restrictionVO.setisDefault(true);
					dao.insertObject(bdt_sc_primitive_restrictionVO);
				}
			}
		}


		System.out.println("###END#####");
	}
	
	public int getDTSCID(String DTSCGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("DT_SC_GUID", new String(DTSCGUID));
		DTSCVO dtscVO = (DTSCVO)dao.findObject(qc);
		int id = dtscVO.getDTSCID();
		return id;
	}
	
	public int getDTSCID(int OwnerDTID, String Property) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("Owner_DT_ID", OwnerDTID);
		qc.add("Property_Term", new String(Property));
		DTSCVO dtscVO = (DTSCVO)dao.findObject(qc);
		int id = dtscVO.getDTSCID();
		return id;
	}
	
	public ArrayList<SRTObject> getCdt_SC_Allowed_Primitive_ID(int cdt_sc_id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", cdt_sc_id);
		ArrayList<SRTObject> cdtscallowedprimitiveidlist = new ArrayList<SRTObject>();
		cdtscallowedprimitiveidlist = dao.findObjects(qc);
		return cdtscallowedprimitiveidlist;
	}
		
	public int getOwnerDTID(String DEN) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("DEN", new String(DEN));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
	
	public int getCDTSCID(int DTSCID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", DTSCID);
		CDTSCAllowedPrimitiveVO cdtVO = (CDTSCAllowedPrimitiveVO)dao.findObject(qc);
		int id = cdtVO.getCDTSCID();
		return id;
	}
	
	public int getCDTPrimitiveID(String Name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(Name));
		CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO)dao.findObject(qc);
		int id = cdtPrimitiveVO.getCDTPrimitiveID();
		return id;
	}
	
	public int getCDTSCAllowedPrimitiveID(int CDTSCID, int CDTPrimitiveID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", CDTSCID);
		qc.add("CDT_Primitive_ID", CDTPrimitiveID);
		CDTSCAllowedPrimitiveVO cdtVO = (CDTSCAllowedPrimitiveVO)dao.findObject(qc);
		int id = cdtVO.getCDTSCAllowedPrimitiveID();
		return id;
	}
	
	public int getCDTSCAllowedPrimitiveExpressionTypeMapID(int CDTSCAllowedPrimitive) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_Allowed_Primitive", CDTSCAllowedPrimitive);
		CDTSCAllowedPrimitiveExpressionTypeMapVO cdtVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)dao.findObject(qc);
		int id = cdtVO.getCTSCAllowedPrimitiveExpressionTypeMapID();
		return id;
	}
	
	public int getCDTSCAllowedPrimitiveExpressionTypeMapID(int CDTSCAllowedPrimitive, int XSD_BuiltIn_Type_ID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_Allowed_Primitive", CDTSCAllowedPrimitive);
		qc.add("XSD_BuiltIn_Type_ID", XSD_BuiltIn_Type_ID);
		CDTSCAllowedPrimitiveExpressionTypeMapVO cdtVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)dao.findObject(qc);
		int id = cdtVO.getCTSCAllowedPrimitiveExpressionTypeMapID();
		return id;
	}
	
	public int getXSDBuiltInTypeID(String Name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(Name));
		XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO)dao.findObject(qc);
		int id = xsdBuiltInTypeVO.getXSDBuiltInTypeID();
		return id;
	}
	
	public int getCodeListID(String Name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(Name));
		CodeListVO codelistVO = (CodeListVO)dao.findObject(qc);
		int id = codelistVO.getCodeListID();
		return id;
	}
	
	public ArrayList<Integer> getBDTSCPrimitiveRestrictionID(int CodeListID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTSCPrimitiveRestriction");
    	QueryCondition qc = new QueryCondition();
		qc.add("Code_List_ID", CodeListID);
		ArrayList<SRTObject> al = dao.findObjects(qc);
		ArrayList<Integer> id = new ArrayList<Integer>();
		for(SRTObject aSRTObject : al) {
			BDTSCPrimitiveRestrictionVO bdtscprimitiverestrictionVO = (BDTSCPrimitiveRestrictionVO)aSRTObject;
			id.add(bdtscprimitiverestrictionVO.getBDTSCPrimitiveRestrictionID());
		}	
		return id;
	}
	
	public void run() throws Exception {
		String filename = "Meta" + ".xsd";
		scindt_sc(filename);
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_6_PopulateBDTSCPrimitiveRestriction scindt_sc = new P_1_6_6_PopulateBDTSCPrimitiveRestriction();
		String filename = "Meta" + ".xsd";
		scindt_sc.scindt_sc(filename);
	}
}
