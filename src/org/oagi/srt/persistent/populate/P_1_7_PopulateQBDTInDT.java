package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
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
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_7_PopulateQBDTInDT {
	
	private XPathHandler fields_xsd;
	private XPathHandler meta_xsd;
	
	private static Connection conn = null;
	
	DAOFactory df;
	SRTDAO dtDao;
	SRTDAO bccpDao;
	SRTDAO aBDTPrimitiveRestrictionDAO;
	SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO;
	SRTDAO aCDTAllowedPrimitiveDAO;
	SRTDAO aCodeListDAO;
	SRTDAO aDTSCDAO;
	
	
	private DTVO getDTVO(String den) throws SRTDAOException {
		QueryCondition qc = new QueryCondition();
		qc.add("den", den);
		qc.add("dt_type", 1);
		return (DTVO)dtDao.findObject(qc, conn);
	}
	
	public P_1_7_PopulateQBDTInDT() throws Exception {
		df = DAOFactory.getDAOFactory();
		dtDao = df.getDAO("DT");
		bccpDao = df.getDAO("BCCP");
		aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		aCodeListDAO = df.getDAO("CodeList");
		aDTSCDAO = df.getDAO("DTSC");
		
		fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
	}
	
	private void populate() throws XPathExpressionException, SRTDAOException {
		NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
		NodeList elementsFromMetaXSD = meta_xsd.getNodeList("/xsd:schema/xsd:element");
		insertDTAndBCCP(elementsFromFieldsXSD, fields_xsd);
		insertDTAndBCCP(elementsFromMetaXSD, meta_xsd);
		insertCDTSCAllowedPrimitive(elementsFromFieldsXSD, fields_xsd);
		insertCDTSCAllowedPrimitive(elementsFromMetaXSD, meta_xsd);
		insertCDTSCAllowedPrimitiveExpressionTypeMap(elementsFromFieldsXSD, fields_xsd);
		insertCDTSCAllowedPrimitiveExpressionTypeMap(elementsFromMetaXSD, meta_xsd);
		
		//System.out.println("### elementsFromFieldsXSD.getLength() " + elementsFromFieldsXSD.getLength());
	}
	
	
	private void insertCDTSCAllowedPrimitive(NodeList elementsFromXSD, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitive");
		CDTSCAllowedPrimitiveVO cdt_sc_allowedVO = new CDTSCAllowedPrimitiveVO();

		for (int j = 0; j < elementsFromXSD.getLength(); j++) {
			Element ref_tmp = (Element) elementsFromXSD.item(j);
			NodeList result = xHandler.getNodeList("//xsd:complexType[@id = '"+ ref_tmp.getAttribute("id") + "']//xsd:attribute");
			for (int i = 0; i < result.getLength(); i++) {
				Element tmp = (Element) result.item(i);
				cdt_sc_allowedVO.setCDTSCID(getDTSCID(tmp.getAttribute("id")));
				ArrayList<SRTObject> cdtallowedprimitivelist = new ArrayList<SRTObject>();
				cdtallowedprimitivelist = getCdt_Allowed_Primitive_ID(getDTID(getRepresentationTerm(tmp.getAttribute("id"))));
				for(SRTObject dvo : cdtallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					cdt_sc_allowedVO.setCDTPrimitiveID(svo.getCDTPrimitiveID());
					cdt_sc_allowedVO.setisDefault(svo.getisDefault());
					dao.insertObject(cdt_sc_allowedVO);
				}
			}
		}
		//System.out.println("###END#####");
	}
	
	private void insertCDTSCAllowedPrimitiveExpressionTypeMap(NodeList elementsFromXSD, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		CDTSCAllowedPrimitiveExpressionTypeMapVO cdt_sc_allowed_primitive_expression_type_mapVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();

		NodeList ref = xHandler.getNodeList("//xsd:complexType");
		for (int j = 0; j < ref.getLength(); j++) {
			Element ref_tmp = (Element) ref.item(j);
			NodeList result = xHandler.getNodeList("//xsd:complexType[@id = '"+ ref_tmp.getAttribute("id") + "']//xsd:attribute");
			for (int i = 0; i < result.getLength(); i++) {
				Element tmp = (Element) result.item(i);
				
				ArrayList<SRTObject> cdtscallowedprimitivelist = new ArrayList<SRTObject>();
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getCDTSCID(getDTSCID(tmp.getAttribute("id"))));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					cdt_sc_allowed_primitive_expression_type_mapVO.setCDTSCAllowedPrimitive(svo.getCDTSCAllowedPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Binary")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:base64Binary"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:hexBinary"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Boolean")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:boolean"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Decimal")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:decimal"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Double")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:double"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:float"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Float")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:float"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Integer")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:integer"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("NormalizedString")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:normalizedString"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("String")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:string"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("TimeDuration")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:token"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:duration"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("TimePoint")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:token"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:dateTime"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:date"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:time"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:gYearMonth"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:gYear"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:gMonthDay"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:gDay"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:gMonth"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
					else if(getPrimitiveName(svo.getCDTPrimitiveID()).equals("Token")){
						cdt_sc_allowed_primitive_expression_type_mapVO.setXSDBuiltInTypeID(getXSDBuiltInTypeID("xsd:token"));
						dao.insertObject(cdt_sc_allowed_primitive_expression_type_mapVO);
					}
				}
			}
		}

		//System.out.println("###END#####");
	}
	
	
	private void insertBDTPrimitiveRestriction(DTVO aDTVO, String type) throws SRTDAOException, XPathExpressionException {
		if(aDTVO.getDataTypeTerm().equals("Code")) {
			Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent/xsd:extension");
			if(typeNameNode != null) {
				String base = ((Element)typeNameNode).getAttribute("base");
				
				if(base.equals("CodeType")) {
					insertBDTPrimitiveRestriction(aDTVO, false, 0);
				} else {
					if(base.endsWith("CodeContentType")) {
						QueryCondition qc = new QueryCondition();
						qc.addLikeClause("name", "%" + base.substring(0, base.indexOf("CodeContentType")) + "%");
						
						//System.out.println("##################################################### Code List: " + base.substring(0, base.indexOf("CodeContentType")));
						
						CodeListVO aCode_ListVO = (CodeListVO)aCodeListDAO.findObject(qc);
						
						insertBDTPrimitiveRestriction(aDTVO, true, aCode_ListVO.getCodeListID());
					} else {
						//System.out.println("##################################################### Code Special Case: " + base);
					}
				}
			} 
		} else {
			insertBDTPrimitiveRestriction(aDTVO, false, 0);
		}
	}
	
	private void insertBDTPrimitiveRestriction(DTVO aDTVO, boolean isMapBlank, int codeListId) throws SRTDAOException {
			
		QueryCondition qc1 = new QueryCondition();
		qc1.add("dt_id", aDTVO.getBasedDTID());
		DTVO parentDT = (DTVO)dtDao.findObject(qc1, conn);
		if(parentDT.getDTType() == 1) {
			QueryCondition qc11 = new QueryCondition();
			qc11.add("dt_id", parentDT.getBasedDTID());
			DTVO aDTVO11 = (DTVO)dtDao.findObject(qc11, conn);
			if(aDTVO11.getDTType() == 1) {
				QueryCondition qc12 = new QueryCondition();
				qc12.add("dt_id", parentDT.getBasedDTID());
				DTVO aDTVO12 = (DTVO)dtDao.findObject(qc12, conn);
				aDTVO.setBasedDTID(aDTVO12.getBasedDTID());
			} else {
				aDTVO.setBasedDTID(aDTVO11.getBasedDTID());
			}
			
		}
			
		QueryCondition qc3 = new QueryCondition();
		qc3.add("cdt_id", aDTVO.getBasedDTID());
		ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc3);
		
		for(SRTObject aSRTObject3 : al3) {
			
			CDTAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aSRTObject3;
			
			QueryCondition qc4 = new QueryCondition();
			qc4.add("cdt_allowed_primitive_id", aCDTAllowedPrimitiveVO.getCDTAllowedPrimitiveID());
			ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4);
			
			for(SRTObject aSRTObject4 : al4) {
				CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMapVO = (CDTAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
				// create insert statement
				BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
				aBDT_Primitive_RestrictionVO.setBDTID(aDTVO.getDTID());
				if(!isMapBlank)
					aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
				else
					aBDT_Primitive_RestrictionVO.setCodeListID(codeListId);
				
				aBDT_Primitive_RestrictionVO.setisDefault(true);;
				
				aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
			}
		}
	}
	
	private void insertDTAndBCCP(NodeList elementsFromXSD, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {
		for(int i = 0; i < elementsFromXSD.getLength(); i++) {
			String den = Utility.createDENFormat(((Element)elementsFromXSD.item(i)).getAttribute("type"));
			String bccp = ((Element)elementsFromXSD.item(i)).getAttribute("name");
			String guid = ((Element)elementsFromXSD.item(i)).getAttribute("id");
			String type = ((Element)elementsFromXSD.item(i)).getAttribute("type");
			//System.out.println("$$$$$DEN:  "+den+"   name = "+"    id = "+guid+"   type="+type);
			Node simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
			Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
			if(simpleContent != null || simpleType != null) {
			
				Node documentationFromXSD = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation");
				
				//System.out.println("### bccp: " + bccp);
				
				String definition = "";
				if(documentationFromXSD != null) {
					definition = ((Element)documentationFromXSD).getTextContent();
				}
				
				QueryCondition qc = new QueryCondition();
				qc.add("den", den);
				qc.add("dt_type", 1);
				
				DTVO dtVO = (DTVO)dtDao.findObject(qc, conn);
				if(dtVO == null) {
					//System.out.println("### NULL " + bccp + " - " + den);
					
					// TODO check these exceptions
					//if(bccp.equals("FinalAgentInstructionCode") || bccp.equals("FirstAgentPaymentMethodCode") || bccp.equals("EndTime") || bccp.equals("StartTime") || bccp.equals("CompositeID"))
					//	continue;
					
					Node typeNode = xHandler.getNode("//xsd:complexType[@name = '" + type + "']");
					if(typeNode == null) {
						typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
					}
					
					DTVO dVO = addToDT(((Element)typeNode).getAttribute("id"), type);
					if(dVO == null) 
						continue;
					
					//System.out.println("typeNode id = "+((Element)typeNode).getAttribute("id")+"   type = "+type+ "     dVO ="+dVO);
					
//					boolean check_duplication = false;
//					for(int j = 0; j < i-1; j++) {
//						if(type.equals(((Element)elementsFromXSD.item(j)).getAttribute("type"))) {
//							check_duplication = true;
//							break;
//						}
//					}
//					
//					if(check_duplication == false)
//						addToDTSC(xHandler, type, dVO);
					
					
					addToDTSC(xHandler, type, dVO);

					// add to BDTPrimitiveRestriction
					insertBDTPrimitiveRestriction(dVO, type);
					
					// add to BCCP
					addToBCCP(guid, bccp, dVO, definition);
				} else {
					// add to BCCP
					addToBCCP(guid, bccp, dtVO, definition);
				}
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				//System.out.println("### i " + i);
			}
		}
	}
	private void insertBDT_SC_Primitive_Restriction(DTSCVO dtscvo, int dtsc, int mode, String name, String type)  throws XPathExpressionException, SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTSCPrimitiveRestriction");
		
		BDTSCPrimitiveRestrictionVO bdtscprimitiverestionvo = new BDTSCPrimitiveRestrictionVO();
		bdtscprimitiverestionvo.setBDTSCID(dtscvo.getDTSCID());
		
		ArrayList<SRTObject> cdtscallowedprimitivelist = new ArrayList<SRTObject>();
		// if (SC = inherit from the base BDT)
		if(mode == 1) {
			cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(dtsc);
			for(SRTObject dvo : cdtscallowedprimitivelist){
				CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
				bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTSCAllowedPrimitiveID());
				bdtscprimitiverestionvo.setisDefault(svo.getisDefault());
				dao.insertObject(bdtscprimitiverestionvo);
			}			
		}
			
		else if (mode == 0) { // else if (new SC)
			if(type.equalsIgnoreCase("Number_B98233")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Number"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("Integer"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}		
			}
			
			else if(type.equalsIgnoreCase("CodeType_1E7368") || type.equalsIgnoreCase("CodeContentType") || name.equalsIgnoreCase("countryCode")) {
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Code"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("Token"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(type.equalsIgnoreCase("StringType")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Text"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("String"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(type.equalsIgnoreCase("NormalizedStringType")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Text"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("String"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(name.equalsIgnoreCase("listID") || name.equalsIgnoreCase("listVersionID") || name.equalsIgnoreCase("unitCodeListVersionID")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Identifier"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("NormalizedString"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(type.equalsIgnoreCase("DateTimeType")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Date Time"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("Token"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(type.equalsIgnoreCase("IndicatorType")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Indicator"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("Token"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}	
			else if(type.equalsIgnoreCase("ValueType_E7171E")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Value"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("NormalizedString"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(name.equalsIgnoreCase("name")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Name"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					if(getPrimitiveName(svo.getCDTPrimitiveID()).equalsIgnoreCase("NormalizedString"))
						bdtscprimitiverestionvo.setisDefault(true);
					else 
						bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}	
			}
			else if(type.contains("CodeContentType") || name.equalsIgnoreCase("listAgencyID")){
				cdtscallowedprimitivelist = getCdt_SC_Allowed_Primitive_ID(getDTID("Code"));
				for(SRTObject dvo : cdtscallowedprimitivelist){
					CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
					bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(svo.getCDTPrimitiveID());
					bdtscprimitiverestionvo.setisDefault(false);
					dao.insertObject(bdtscprimitiverestionvo);
				}
			}
			else if(type.contains("CodeContentType")) {//need to check
				bdtscprimitiverestionvo.setCDTSCAllowedPrimitiveExpressionTypeMapID(getCodeListID(type.substring(0, type.indexOf("CodeContentType"))));
				bdtscprimitiverestionvo.setisDefault(true);
				dao.insertObject(bdtscprimitiverestionvo);
			}
			else if (name.contains("listAgencyID")){
				bdtscprimitiverestionvo.setAgencyIDListID(getAgencyListID("oagis-id-f1df540ef0db48318f3a423b3057955f"));
				bdtscprimitiverestionvo.setisDefault(true);
				dao.insertObject(bdtscprimitiverestionvo);
			}
		}		
	}
		
	private DTVO addToDT(String guid, String type) throws XPathExpressionException, SRTDAOException {
		
		
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", guid);
		
		DTVO dVO = (DTVO)dtDao.findObject(qc, conn);
		if(dVO == null) {
		
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(guid);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			dtVO.setRevisionType(0);
			
			Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent/xsd:extension");
			if(typeNameNode != null) {
				
				String base = ((Element)typeNameNode).getAttribute("base");
				if(base.endsWith("CodeContentType")) {
					dVO = getDTVO("Code. Type");
				} else {
					base = Utility.createDENFormat(((Element)typeNameNode).getAttribute("base"));
					dVO = getDTVO(base);
				}
				
				if(dVO == null) {
					dVO = addToDT2(((Element)typeNameNode).getAttribute("base"));
				}
			} else {
				return null;
			}
			
			dtVO.setBasedDTID(dVO.getDTID());
			dtVO.setDataTypeTerm(dVO.getDataTypeTerm());
			
			String tmp = Utility.spaceSeparator(type);
			String qualifier = tmp.substring(0, tmp.indexOf(" "));
			dtVO.setQualifier(qualifier);
			
			String den = qualifier + "_ " + dVO.getDEN();
			dtVO.setDEN(den);
			dtVO.setContentComponentDEN(den.substring(0, den.indexOf(".")) + ". Content");
			dtVO.setDefinition(null);
			dtVO.setContentComponentDefinition(null);
			dtVO.setRevisionState(1);
			dtVO.setCreatedByUserId(1);
			dtVO.setLastUpdatedByUserId(1);
			dtVO.setRevisionDocumentation("");
			
			dtDao.insertObject(dtVO);
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_guid", guid);
			
			return (DTVO)dtDao.findObject(qc1, conn);
		} else {
			return dVO;
		}
	}
	
	private DTVO addToDT2(String type) throws XPathExpressionException, SRTDAOException {
		
		Node typeNode = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']");
		if(typeNode == null) {
			typeNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + type + "']");
		}
		Element tN = (Element)typeNode;
		
		System.out.println("######## type" + type);
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", tN.getAttribute("id"));
		
		DTVO dVO1 = (DTVO)dtDao.findObject(qc, conn);
		if(dVO1 == null) {
			
			DTVO dVO = new DTVO();
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(tN.getAttribute("id"));
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			dtVO.setRevisionType(0);
			
			if(type.endsWith("CodeContentType")) {
				dVO = getDTVO("Code. Type");
			} else {
				// TODO fix this
				dVO = getDTVO("Text. Type");
				//System.out.println("####################### " + type);
			}
			
			dtVO.setBasedDTID(dVO.getDTID());
			dtVO.setDataTypeTerm(dVO.getDataTypeTerm());
			
			String tmp = Utility.spaceSeparator(type);
			String qualifier = tmp.substring(0, tmp.indexOf(" "));
			dtVO.setQualifier(qualifier);
			
			String den = qualifier + "_ " + dVO.getDEN();
			dtVO.setDEN(den);
			dtVO.setContentComponentDEN(den.substring(0, den.indexOf(".")) + ". Content");
			dtVO.setDefinition(null);
			dtVO.setContentComponentDefinition(null);
			dtVO.setRevisionState(1);
			dtVO.setCreatedByUserId(1);
			dtVO.setLastUpdatedByUserId(1);
			dtVO.setRevisionDocumentation("");

			dtDao.insertObject(dtVO);
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_guid", tN.getAttribute("id"));
			
			return (DTVO)dtDao.findObject(qc1, conn);
		} else {
			return dVO1;
		}
	}
	
	private void addToBCCP(String guid, String bccp, DTVO dtVO, String definition) throws SRTDAOException {
		
		BCCPVO bccpVO = new BCCPVO();
		bccpVO.setBCCPGUID(guid);
		
		String propertyTerm = Utility.spaceSeparator(bccp.replaceAll("ID", "Identifier"));
		bccpVO.setPropertyTerm(propertyTerm);
		bccpVO.setRepresentationTerm(dtVO.getDataTypeTerm());
		bccpVO.setBDTID(dtVO.getDTID());
		bccpVO.setDEN(Utility.firstToUpperCase(propertyTerm) + ". " + dtVO.getDataTypeTerm());
		bccpVO.setDefinition(definition);
		bccpVO.setCreatedByUserId(1);
		bccpVO.setLastUpdatedByUserId(1);
		
		bccpDao.insertObject(bccpVO);
		
	}
	
	private void addToDTSC(XPathHandler xHandler, String typeName, DTVO qbdtVO) throws SRTDAOException, XPathExpressionException {
		
		String dt_sc_guid = "";
		String property_term = "";
		String representation_term = "";
		int owner_dT_iD = 0;
		String definition;
		int min_cardinality = 0;
		int max_cardinality = 0;
		
		int based_dT_sc_id; 
		
		// inherit from the base BDT
		QueryCondition qc = new QueryCondition();
		qc.add("owner_dt_iD", qbdtVO.getBasedDTID());
		
		ArrayList<SRTObject> dtsc_vos = aDTSCDAO.findObjects(qc);
		for(SRTObject sObj : dtsc_vos) {
			DTSCVO dtsc_vo = (DTSCVO)sObj;
			
			DTSCVO vo = new DTSCVO();
			vo.setDTSCGUID(dtsc_vo.getDTSCGUID());
			vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
			vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
			vo.setOwnerDTID(qbdtVO.getDTID());
			
			vo.setMinCardinality(dtsc_vo.getMinCardinality());
			vo.setMaxCardinality(dtsc_vo.getMaxCardinality());
			vo.setBasedDTSCID(dtsc_vo.getDTSCID());
			
			
			if(!checkDuplicate(vo)) {
				aDTSCDAO.insertObject(vo);	
				insertBDT_SC_Primitive_Restriction(vo, vo.getOwnerDTID(), 1, "", "");
			}
		}
		
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("dt_id", qbdtVO.getBasedDTID());
		DTVO dtVO = (DTVO)dtDao.findObject(qc1);
		if(dtVO.getBasedDTID() > 0) {
			QueryCondition qc2 = new QueryCondition();
			qc2.add("owner_dt_iD", dtVO.getBasedDTID());
			
			ArrayList<SRTObject> dtsc_vos2 = aDTSCDAO.findObjects(qc2);
			for(SRTObject sObj : dtsc_vos2) {
				DTSCVO dtsc_vo = (DTSCVO)sObj;
				
				DTSCVO vo = new DTSCVO();
				vo.setDTSCGUID(dtsc_vo.getDTSCGUID());
				vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
				vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
				vo.setOwnerDTID(qbdtVO.getDTID());
				
				vo.setMinCardinality(dtsc_vo.getMinCardinality());
				vo.setMaxCardinality(dtsc_vo.getMaxCardinality());
				vo.setBasedDTSCID(dtsc_vo.getDTSCID());
				
				if(!checkDuplicate(vo)) {
					aDTSCDAO.insertObject(vo);	
					insertBDT_SC_Primitive_Restriction(vo, vo.getOwnerDTID(), 1, "", "");
				}
			}
		}
		
		// new SC
		NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		
		if(attributeList == null || attributeList.getLength() == 0) {
			//System.out.println("##### " + "//xsd:"+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		} else {
			
			for(int i = 0; i < attributeList.getLength(); i++) {
				Node attribute = attributeList.item(i);
				Element attrElement = (Element)attribute;
				dt_sc_guid = attrElement.getAttribute("id");
				//System.out.println(typeName+"   %%%%%%  "+i+"+######"+dt_sc_guid+"!!!!!");
				if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
					min_cardinality = 0;
				else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
					min_cardinality = 1;
				
				owner_dT_iD = qbdtVO.getDTID();
				
				String attrName = attrElement.getAttribute("name");
				
				// Property Term
				if(attrName.endsWith("Code"))  
					property_term = attrName.substring(0, attrName.indexOf("Code"));
				else if(attrName.endsWith("ID")) 
					property_term = attrName.substring(0, attrName.indexOf("ID"));
				else if(attrName.endsWith("Value"))	
					property_term = attrName.substring(0, attrName.indexOf("Value"));
				else
					property_term = Utility.spaceSeparator(attrName);
				
				if(property_term.trim().length() == 0)
					property_term = attrName;
				
				property_term = Utility.firstToUpperCase(property_term);
				
				
				// Representation Term
				if(attrName.endsWith("Code") || attrName.endsWith("code")) {
					representation_term = "Code";
				} else if(attrName.endsWith("Number")) {
					representation_term = "Number";
				} else if(attrName.endsWith("ID")) {
					representation_term = "Identifier";
				} else if(attrName.endsWith("DateTime")) {
					representation_term = "Date Time";
				} else if(attrName.endsWith("Value")) {
					representation_term = "Value";
				} else if(attrName.endsWith("Name") || attrName.endsWith("name")) {
					representation_term = "Name";
				} else {
					String attrType = attrElement.getAttribute("type");
					if(attrType.equals("StringType") || attrType.equals("NormalizedStringType"))
						representation_term = "Text";
					else if(attrType.equals("IndicatorType"))
						representation_term = "Indicator";
				}
					
					
				Node documentationNode = xHandler.getNode("//xsd:complexType[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation");
				if(documentationNode != null) {
					definition = ((Element)documentationNode).getTextContent();
				} else {
					definition = "N/A";
				}
					
				DTSCVO vo = new DTSCVO();
				vo.setDTSCGUID(dt_sc_guid);
				vo.setPropertyTerm(Utility.spaceSeparator(property_term));
				vo.setRepresentationTerm(representation_term);
				vo.setDefinition(definition);
				vo.setOwnerDTID(owner_dT_iD);
				
				vo.setMinCardinality(min_cardinality);
				vo.setMaxCardinality(max_cardinality);
				
				//System.out.println("######"+vo.getDTSCGUID()+"!!!!!"+vo.getPropertyTerm()+"@@@@@"+vo.getRepresentationTerm());
				
				if(!checkDuplicate(vo)) {
					aDTSCDAO.insertObject(vo);	
					insertBDT_SC_Primitive_Restriction(vo, vo.getOwnerDTID(), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
				}
			}
		}
	}
	
	private boolean checkDuplicate(DTSCVO dtVO) throws SRTDAOException {
		QueryCondition qc = new QueryCondition();
		qc.add("dt_sc_guid", dtVO.getDTSCGUID());
		qc.add("property_term", dtVO.getPropertyTerm());
		qc.add("owner_dt_id", dtVO.getOwnerDTID());
		return (((DTSCVO)aDTSCDAO.findObject(qc)).getDTSCID() > 0) ? true : false;
	}
	

	public int getCodeListID(String CodeName) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(CodeName));
		CodeListVO codelistVO = (CodeListVO)dao.findObject(qc);
		int id = codelistVO.getCodeListID();
		return id;
	}
	
	public int getAgencyListID(String GUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
    	QueryCondition qc = new QueryCondition();
		qc.add("Agency_ID_List_GUID", new String(GUID));
		AgencyIDListVO agencyidlistVO = (AgencyIDListVO)dao.findObject(qc);
		int id = agencyidlistVO.getAgencyIDListID();
		return id;
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
	
	public int getDTID(String DataTypeTerm) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(DataTypeTerm));
		qc.add("DT_Type", 0);
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
	
	public String getRepresentationTerm(String DTSCGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("DT_SC_GUID", new String(DTSCGUID));
		DTSCVO dtscVO = (DTSCVO)dao.findObject(qc);
		String term = dtscVO.getRepresentationTerm();
		return term;
	}
	
	public String getPrimitiveName(int CDTPrimitiveID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_Primitive_ID", CDTPrimitiveID);
		CDTPrimitiveVO cdtprimitiveVO = (CDTPrimitiveVO)dao.findObject(qc);
		String name = cdtprimitiveVO.getName();
		return name;
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
	
	public ArrayList<SRTObject> getCdt_Allowed_Primitive_ID(int cdt_id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTAllowedPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("CDT_ID", cdt_id);
		ArrayList<SRTObject> cdtallowedprimitiveidlist = new ArrayList<SRTObject>();
		cdtallowedprimitiveidlist = dao.findObjects(qc);
		return cdtallowedprimitiveidlist;
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
	
	public int getXSDBuiltInTypeID(String BuiltIntype) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("BuiltIn_Type", new String(BuiltIntype));
		XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO)dao.findObject(qc);
		int id = xsdBuiltInTypeVO.getXSDBuiltInTypeID();
		return id;
	}
	
	
	public void run() throws Exception {
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		populate();
		
		tx.close();
		conn.close();
		System.out.println("###END###");
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		P_1_7_PopulateQBDTInDT q = new P_1_7_PopulateQBDTInDT();
		//System.out.println(q.spaceSeparator("TotalAmountType"));
		q.populate();
		
		tx.close();
		conn.close();
		
		System.out.println("###END###");

		//System.out.println(q.spaceSeparator("ISBN"));
		
		
		//String c = "aaCode";
		//System.out.println(c.substring(0, c.indexOf("Code")));
		
//		PopulateQBDTInDT p = new PopulateQBDTInDT();
//		for (int i = 0; i < Types.dataTypeList.length; i++){
//			p.importDataTypeList(Types.dataTypeList[i]);
//		}
//		for (int i = 0; i < Types.simpleTypeList.length; i++){
//			p.importDataTypeList(Types.simpleTypeList[i]);
//		}
	}
}
