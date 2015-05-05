package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.DTSCVO;
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
public class P_1_6_5_PopulateCDTSCAllowedPrimitiveExpressionTypeMapFromXSD {
	public void scindt_sc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BDT_Primitive_Restriction")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();

		NodeList ref = xh.getNodeList("//xsd:complexType");
		for (int j = 0; j < ref.getLength(); j++) {
			Element ref_tmp = (Element) ref.item(j);
		    if(ref_tmp.getAttribute("name").equals("ExpressionType") || ref_tmp.getAttribute("name").equals("ActionExpressionType") || ref_tmp.getAttribute("name").equals("ResponseExpressionType") ) {

				NodeList result = xh.getNodeList("//xsd:complexType[@id = '"+ ref_tmp.getAttribute("id") + "']//xsd:attribute");
				for (int i = 0; i < result.getLength(); i++) {
					Element tmp = (Element) result.item(i);
					if (tmp.getAttribute("name").equals("actionCode") || tmp.getAttribute("name").equals("expressionLanguage")) {
						if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString"))!=0) {
							cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString")));
							cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("normalized string"));
						}
						else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("String"))!=0) {
							cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString")));
							cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("string"));
						}
						else if(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("Token"))!=0) {
							cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(getCDTSCAllowedPrimitiveID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))),getCDTPrimitiveID("NormalizedString")));
							cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("token"));
						}
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
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
	
	public int getXSDBuiltInTypeID(String Name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(Name));
		XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO)dao.findObject(qc);
		int id = xsdBuiltInTypeVO.getXSDBuiltInTypeID();
		return id;
	}
	
	public void run() throws Exception {
		String filename = "Meta" + ".xsd";
		scindt_sc(filename);
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_5_PopulateCDTSCAllowedPrimitiveExpressionTypeMapFromXSD scindt_sc = new P_1_6_5_PopulateCDTSCAllowedPrimitiveExpressionTypeMapFromXSD();
		String filename = "Meta" + ".xsd";
		scindt_sc.scindt_sc(filename);
	}
}
