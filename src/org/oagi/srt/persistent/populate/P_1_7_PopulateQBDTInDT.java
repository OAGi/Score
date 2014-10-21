package org.oagi.srt.persistent.populate;

import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
*
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_7_PopulateQBDTInDT {
	
	private XPathHandler fields_xsd;
	private XPathHandler meta_xsd;
	
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
		return (DTVO)dtDao.findObject(qc);
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
		
		fields_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/Components/Fields_modified.xsd");
		meta_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/Components/Meta.xsd");
	}
	
	private void populate() throws XPathExpressionException, SRTDAOException {
		NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
		NodeList elementsFromMetaXSD = meta_xsd.getNodeList("/xsd:schema/xsd:element");
		
		insertDTAndBCCP(elementsFromFieldsXSD, fields_xsd);
		insertDTAndBCCP(elementsFromMetaXSD, meta_xsd);
		
		System.out.println("### elementsFromFieldsXSD.getLength() " + elementsFromFieldsXSD.getLength());
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
						
						System.out.println("##################################################### Code List: " + base.substring(0, base.indexOf("CodeContentType")));
						
						CodeListVO aCode_ListVO = (CodeListVO)aCodeListDAO.findObject(qc);
						
						insertBDTPrimitiveRestriction(aDTVO, true, aCode_ListVO.getCodeListID());
					} else {
						System.out.println("##################################################### Code Special Case: " + base);
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
		DTVO parentDT = (DTVO)dtDao.findObject(qc1);
		if(parentDT.getDTType() == 1) {
			QueryCondition qc11 = new QueryCondition();
			qc11.add("dt_id", parentDT.getBasedDTID());
			DTVO aDTVO11 = (DTVO)dtDao.findObject(qc11);
			if(aDTVO11.getDTType() == 1) {
				QueryCondition qc12 = new QueryCondition();
				qc12.add("dt_id", parentDT.getBasedDTID());
				DTVO aDTVO12 = (DTVO)dtDao.findObject(qc12);
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
			
			Node simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
			Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
			if(simpleContent != null || simpleType != null) {
			
				Node documentationFromXSD = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation");
				
				System.out.println("### bccp: " + bccp);
				
				String definition = "";
				if(documentationFromXSD != null) {
					definition = ((Element)documentationFromXSD).getTextContent();
				}
				
				QueryCondition qc = new QueryCondition();
				qc.add("den", den);
				qc.add("dt_type", 1);
				
				DTVO dtVO = (DTVO)dtDao.findObject(qc);
				if(dtVO == null) {
					System.out.println("### NULL " + bccp + " - " + den);
					
					// TODO check these exceptions
					if(bccp.equals("FinalAgentInstructionCode") || bccp.equals("FirstAgentPaymentMethodCode") || bccp.equals("EndTime") || bccp.equals("StartTime") || bccp.equals("CompositeID"))
						continue;
					
					Node typeNode = xHandler.getNode("//xsd:complexType[@name = '" + type + "']");
					if(typeNode == null) {
						typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
					}
					
					DTVO dVO = addToDT(((Element)typeNode).getAttribute("id"), type);
					if(dVO == null) 
						continue;
					
					addToDTSC(xHandler, type, dVO);
					
					// add to BDTPrimitiveRestriction
					insertBDTPrimitiveRestriction(dVO, type);
					
					// add to BCCP
					addToBCCP(guid, bccp, dVO, definition);
				} else {
					// add to BCCP
					addToBCCP(guid, bccp, dtVO, definition);
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("### i " + i);
			}
		}
	}
	
	private DTVO addToDT(String guid, String type) throws XPathExpressionException, SRTDAOException {
		
		
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", guid);
		
		DTVO dVO = (DTVO)dtDao.findObject(qc);
		if(dVO == null) {
		
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(guid);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			dtVO.setRevisionType(0);
			
			Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent/xsd:extension");
			if(typeNameNode != null) {
				String base = Utility.createDENFormat(((Element)typeNameNode).getAttribute("base"));
				
				if(base.endsWith("CodeContentType")) {
					dVO = getDTVO("Code. Type");
				} else {
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
			
			dtDao.insertObject(dtVO);
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_guid", guid);
			
			return (DTVO)dtDao.findObject(qc1);
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
		
		QueryCondition qc = new QueryCondition();
		qc.add("dt_guid", tN.getAttribute("id"));
		
		DTVO dVO1 = (DTVO)dtDao.findObject(qc);
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
				System.out.println("####################### " + type);
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
			
			dtDao.insertObject(dtVO);
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_guid", tN.getAttribute("id"));
			
			return (DTVO)dtDao.findObject(qc1);
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
		bccpVO.setDEN(propertyTerm + ". " + dtVO.getDataTypeTerm());
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
			
			aDTSCDAO.insertObject(vo);	
		}
		
		// new SC
		NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		if(attributeList == null || attributeList.getLength() == 0) {
			//System.out.println("##### " + "//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		} else {
			
			for(int i = 0; i < attributeList.getLength(); i++) {
				Node attribute = attributeList.item(i);
				Element attrElement = (Element)attribute;
				dt_sc_guid = attrElement.getAttribute("id");
				
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
				vo.setPropertyTerm(property_term);
				vo.setRepresentationTerm(representation_term);
				vo.setDefinition(definition);
				vo.setOwnerDTID(owner_dT_iD);
				
				vo.setMinCardinality(min_cardinality);
				vo.setMaxCardinality(max_cardinality);
				
				aDTSCDAO.insertObject(vo);		
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		
		P_1_7_PopulateQBDTInDT q = new P_1_7_PopulateQBDTInDT();
		//System.out.println(q.spaceSeparator("TotalAmountType"));
		q.populate();
		
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
