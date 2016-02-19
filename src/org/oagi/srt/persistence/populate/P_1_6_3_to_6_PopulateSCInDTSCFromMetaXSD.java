package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
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
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @author Yunsu Lee
* @version 1.0
*
*/
public class P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD {
	
	private static Connection conn = null;
	
	public void importDTSCFromMeta() throws SRTDAOException, FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dtscDao = df.getDAO("DTSC");
		SRTDAO dtDao = df.getDAO("DT");
		SRTDAO cdtSCAPDao = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO cdtSCAPMapDao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO xbtDao = df.getDAO("XSDBuiltInType");
		SRTDAO bdtSCPRDao = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO codeListDao = df.getDAO("CodeList");
		
		QueryCondition qc_01 = new QueryCondition();
		qc_01.add("den", "Text_62S0B4. Type");
		DTVO dtVO_01 = (DTVO)dtDao.findObject(qc_01, conn);
		
		QueryCondition qc_011 = new QueryCondition();
		qc_011.add("DT_ID", dtVO_01.getBasedDTID());
		DTVO dtVO_012 = (DTVO)dtDao.findObject(qc_011, conn);
		
		QueryCondition qc_02 = new QueryCondition();
		qc_02.add("owner_dt_id", dtVO_012.getDTID());
		DTSCVO dtscVO_01 = (DTSCVO)dtscDao.findObject(qc_02, conn);
		
		QueryCondition qc_12 = new QueryCondition();
		qc_12.add("owner_dt_id", dtVO_01.getDTID());
		DTSCVO textBDT_dtscVO = (DTSCVO)dtscDao.findObject(qc_12, conn);
		
		
		XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		NodeList complexTypes = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
		for(int i = 0; i < complexTypes.getLength(); i++) {
			Element ele = (Element)complexTypes.item(i);
		    String eleGuid = ele.getAttribute("id");
		    
			// inherit all values from default Text BDT with two exceptions (max cardinality and based DTSC Id)
			QueryCondition qc_021 = new QueryCondition();
			qc_021.add("guid", eleGuid);
			DTVO dtVO_011 = (DTVO)dtDao.findObject(qc_021, conn);
			
			DTSCVO dtscVO_02 = new DTSCVO();
			dtscVO_02.setBasedDTSCID(textBDT_dtscVO.getDTSCID());
			dtscVO_02.setDefinition(dtscVO_01.getDefinition());
			dtscVO_02.setDTSCGUID(dtscVO_01.getDTSCGUID());
			dtscVO_02.setMaxCardinality(0);
			dtscVO_02.setMinCardinality(dtscVO_01.getMinCardinality());
			dtscVO_02.setOwnerDTID(dtVO_011.getDTID());
			dtscVO_02.setPropertyTerm(dtscVO_01.getPropertyTerm());
			dtscVO_02.setRepresentationTerm(dtscVO_01.getRepresentationTerm());
			
			dtscDao.insertObject(dtscVO_02);
			
			QueryCondition qc_0211 = new QueryCondition();
			qc_0211.add("guid", dtscVO_01.getDTSCGUID());
			qc_0211.add("owner_dt_id", dtVO_011.getDTID());
			int bdtSCId = ((DTSCVO)dtscDao.findObject(qc_0211, conn)).getDTSCID();
			
			// populate BDT_SC_Primitive_Restriction table for language code
			QueryCondition qc_022 = new QueryCondition();
			qc_022.add("bdt_sc_id", dtscVO_01.getDTSCID());
			List<SRTObject> bdtscs = bdtSCPRDao.findObjects(qc_022, conn);
			for(SRTObject obj : bdtscs) {
				BDTSCPrimitiveRestrictionVO parent = (BDTSCPrimitiveRestrictionVO)obj;
				
				BDTSCPrimitiveRestrictionVO bdtSCPRVO = new BDTSCPrimitiveRestrictionVO();
				bdtSCPRVO.setBDTSCID(bdtSCId);
				bdtSCPRVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(parent.getCDTSCAllowedPrimitiveExpressionTypeMapID());
				bdtSCPRVO.setCodeListID(parent.getCodeListID());
				bdtSCPRVO.setisDefault(parent.getisDefault());
				bdtSCPRDao.insertObject(bdtSCPRVO);
			}
			
			// populate using attributes
		    QueryCondition qc_03 = new QueryCondition();
		    qc_03.add("guid", eleGuid);
			DTVO dtVO_02 = (DTVO)dtDao.findObject(qc_03, conn);
			
		    NodeList attributes = ele.getElementsByTagName("xsd:attribute");
		    for(int j = 0; j < attributes.getLength(); j++) {
		    	Element attr = (Element)attributes.item(j);
		    	String attrName = attr.getAttribute("name");
		    	
		    	DTSCVO dtscVO_03 = new DTSCVO();
		    	dtscVO_03.setDTSCGUID(attr.getAttribute("id"));
		    	
		    	String use = attr.getAttribute("use");
		    	int minCardinality = 0;
		    	int maxCardinality = 1;
		    	minCardinality = (use == null) ? 0 : (use.equalsIgnoreCase("required")) ? 1: 0;
		    	maxCardinality = (use == null) ? 1 : (use.equalsIgnoreCase("prohibited")) ? 0: 1;
		    	dtscVO_03.setMaxCardinality(maxCardinality);
		    	dtscVO_03.setMinCardinality(minCardinality);
		    	
		    	dtscVO_03.setOwnerDTID(dtVO_02.getDTID());
		    	dtscVO_03.setPropertyTerm(Utility.spaceSeparator(attrName));
		    	dtscVO_03.setRepresentationTerm((attrName.equalsIgnoreCase("expressionLanguage")) ? "Text" : "Code");
				
				dtscDao.insertObject(dtscVO_03);
				
				// populate CDT_SC for this new dt_sc
				QueryCondition qc_04 = new QueryCondition();
			    qc_04.add("guid", attr.getAttribute("id"));
			    String[] name = {"NormalizedString", "String", "Token"};
			    int dt_sc_id = 0;
			    for(int k = 0; k < name.length; k++) {
					DTSCVO dtscVO_04 = (DTSCVO)dtscDao.findObject(qc_04, conn);
					dt_sc_id = dtscVO_04.getDTSCID();
					CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = new CDTSCAllowedPrimitiveVO();
					aCDTSCAllowedPrimitiveVO.setCDTSCID(dt_sc_id);
					
					int cdtPrimitiveID = getCDTPrimitiveID(name[k]);
					aCDTSCAllowedPrimitiveVO.setCDTPrimitiveID(cdtPrimitiveID);
					aCDTSCAllowedPrimitiveVO.setisDefault((name[k].equalsIgnoreCase("Token")) ? true : false);
					cdtSCAPDao.insertObject(aCDTSCAllowedPrimitiveVO);
					
					QueryCondition qc_05 = new QueryCondition();
					qc_05.add("cdt_sc_id", dt_sc_id);
					qc_05.add("cdt_pri_id", cdtPrimitiveID);
					int cdtSCAllowedPrimitiveId = ((CDTSCAllowedPrimitiveVO)cdtSCAPDao.findObject(qc_05, conn)).getCDTSCAllowedPrimitiveID();

					// populate CDT_SC_Allowed_Primitive_Expression_Type_Map 
					List<String> xsdbs = Types.getCorrespondingXSDBuiltType(name[k]);
					for(String xbt : xsdbs) {
						CDTSCAllowedPrimitiveExpressionTypeMapVO mapVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
						mapVO.setCDTSCAllowedPrimitive(cdtSCAllowedPrimitiveId);
						QueryCondition qc_06 = new QueryCondition();
						qc_06.add("builtin_type", xbt);
						int xdtBuiltTypeId = ((XSDBuiltInTypeVO)xbtDao.findObject(qc_06, conn)).getXSDBuiltInTypeID();
						mapVO.setXSDBuiltInTypeID(xdtBuiltTypeId);
						cdtSCAPMapDao.insertObject(mapVO);
						
						QueryCondition qc_07 = new QueryCondition();
						qc_07.add("cdt_sc_awd_pri", cdtSCAllowedPrimitiveId);
						qc_07.add("xbt_id", xdtBuiltTypeId);
						int mapId = ((CDTSCAllowedPrimitiveExpressionTypeMapVO)cdtSCAPMapDao.findObject(qc_07, conn)).getCTSCAllowedPrimitiveExpressionTypeMapID();
						
						// populate BDT_SC_Primitive_Restriction table for expressionLanguage and actionCode
						BDTSCPrimitiveRestrictionVO bdtSCPRVO = new BDTSCPrimitiveRestrictionVO();
						bdtSCPRVO.setBDTSCID(dt_sc_id);
						bdtSCPRVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(mapId);
						if(attrName.equalsIgnoreCase("expressionLanguage")) {
							bdtSCPRVO.setisDefault((name[k].equalsIgnoreCase("Token")) ? true : false);
						} else if(attrName.equalsIgnoreCase("actionCode")) {
							bdtSCPRVO.setisDefault(false);
						}
						bdtSCPRDao.insertObject(bdtSCPRVO);
					}
			    }
			    
			 // populate BDT_SC_Primitive_Restriction table for actionCode (add codeList field)
			    if(attrName.equalsIgnoreCase("actionCode")) {
			    	BDTSCPrimitiveRestrictionVO bdtSCPRVO = new BDTSCPrimitiveRestrictionVO();
					bdtSCPRVO.setBDTSCID(dt_sc_id);
					bdtSCPRVO.setisDefault(true);
					QueryCondition qc = new QueryCondition();
					qc.add("name", "oacl_ActionCode");
					bdtSCPRVO.setCodeListID(((CodeListVO)codeListDao.findObject(qc, conn)).getCodeListID());
					
					bdtSCPRDao.insertObject(bdtSCPRVO);
			    }
		    }
		}
	}
	
	public int getCDTPrimitiveID(String name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", name);
		return ((CDTPrimitiveVO)dao.findObject(qc, conn)).getCDTPrimitiveID();
	}
	
	public void run() throws Exception {
		System.out.println("### 1.6.3-6 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		importDTSCFromMeta();
		
		tx.close();
		conn.close();
		System.out.println("### 1.6.3-6 End");
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD scindt_sc = new P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD();
		scindt_sc.run();
	}
}
