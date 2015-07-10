<<<<<<< HEAD
package org.oagi.srt.generate.standalone;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.AgencyIDListValueVO;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.BBIE_SCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StandaloneXMLSchema {

	private static Connection conn = null;
	public static int acc_ids[] = {22202};
	public static boolean schema_package_flag = false;

	
	public void writeXSDFile(Document doc, String filename) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("/Temp/test/"+filename+".xsd"));
		transformer.transform(source, result);
		System.out.println(filename + ".xsd is generated");
	}
	
	public Element generateSchema(Document doc) {
		Element schemaNode = doc.createElement("xsd:schema");
		schemaNode.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		schemaNode.setAttribute("xmlns","http://www.openapplications.org/oagis/10");
		schemaNode.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
		schemaNode.setAttribute("targetNamespace", "http://www.openapplications.org/oagis/10");
		schemaNode.setAttribute("elementFormDefault", "qualified");
		schemaNode.setAttribute("attributeFormDefault", "unqualified");
		doc.appendChild(schemaNode);
		return schemaNode;
	}
	
	public Document generateTopLevelABIE(ASBIEPVO tlASBIEP, Document tlABIEDOM, Element schemaNode) throws Exception {		
		Element rootEleNode = generateTopLevelASBIEP(tlASBIEP, schemaNode);
		ABIEVO aABIE = queryTargetABIE(tlASBIEP);
		Element rootSeqNode = generateABIE(aABIE, rootEleNode);
		schemaNode = generateBIEs(aABIE, rootSeqNode, schemaNode); 
		return tlABIEDOM;
	}
	
	public Element generateTopLevelASBIEP(ASBIEPVO gTlASBIEP, Element gSchemaNode) throws SRTDAOException {
		Element rootEleNode = gSchemaNode.getOwnerDocument().createElement("xsd:element"); 
		gSchemaNode.appendChild(rootEleNode);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gTlASBIEP.getBasedASCCPID());
		ArrayList<SRTObject> asccpvo = dao.findObjects(qc, conn);
		for(SRTObject aSRTObject : asccpvo){
			ASCCPVO asccpVO = (ASCCPVO)aSRTObject;
			rootEleNode.setAttribute("name", toCamelCase(Utility.first(asccpVO.getDEN().replaceAll("Type", ""))));
		}
		return rootEleNode;
	}
	
	public Element generateABIE(ABIEVO gABIE, Element gElementNode) throws SRTDAOException {
		Element complexType = gElementNode.getOwnerDocument().createElement("xsd:complexType");
		Element PNode = gElementNode.getOwnerDocument().createElement("xsd:sequence");
		Element origin_ElementNode = (Element) gElementNode.getOwnerDocument().getFirstChild();
		origin_ElementNode.appendChild(complexType);
		complexType.appendChild(PNode);
		return PNode;
	}

	public Element generateBIEs(ABIEVO gABIE, Element gPNode, Element gSchemaNode) throws Exception {
		ArrayList<SRTObject> childBIEs = queryChildBIEs(gABIE);
		Element node = null;
		for(SRTObject aSRTObject : childBIEs){
			if(aSRTObject.getClass().getCanonicalName().endsWith("ASBIEVO")){
				ASBIEVO childBIE = (ASBIEVO)aSRTObject;
				node = generateASBIE(childBIE, gPNode);
				ASBIEPVO anASBIEP = queryAssocToASBIEP(childBIE);
				node = generateASBIEP(anASBIEP, node);
				ABIEVO anABIE = queryTargetABIE2(anASBIEP);
				
				ArrayList<SRTObject> next_childBIEs = queryChildBIEs(anABIE);
				if(next_childBIEs.size() > 0) {
				node = generateABIE(anABIE, node);
				node = generateBIEs(anABIE, node, gSchemaNode);
				}
			}
			else {
				BBIEVO childBIE = (BBIEVO)aSRTObject;
				DTVO aBDT = queryAssocBDT(childBIE);
				node = generateBBIE(childBIE, aBDT, gPNode, gSchemaNode);
			}
		}
		return gSchemaNode;
	}
	
	public ABIEVO queryTargetABIE(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abievo = (ABIEVO)dao.findObject(qc, conn);
		return abievo;
	}
	
	public ABIEVO queryTargetABIE2(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
    	QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abieVO = (ABIEVO)dao.findObject(qc, conn);		
		return abieVO;
	}
	
	public ArrayList<SRTObject> queryChildBIEs(ABIEVO gABIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIE");
    	QueryCondition qc = new QueryCondition();
		qc.add("Assoc_From_ABIE_ID", gABIE.getABIEID());
		ArrayList<SRTObject> asbievo = dao.findObjects(qc, conn);

		SRTDAO dao2 = df.getDAO("BBIE");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("Assoc_From_ABIE_ID", gABIE.getABIEID());
		ArrayList<SRTObject> bbievo = dao2.findObjects(qc2, conn);
		ArrayList<SRTObject> result = new ArrayList<SRTObject>();

		for(SRTObject aSRTObject : asbievo){
			ASBIEVO aASBIEVO = (ASBIEVO)aSRTObject;
			if(aASBIEVO.getCardinalityMax() != 0){
				result.add(aASBIEVO);
			}
		}
		
		for(SRTObject aSRTObject : bbievo){
			BBIEVO aBBIEVO = (BBIEVO)aSRTObject;
			if(aBBIEVO.getCardinalityMax() != 0){
				result.add(aBBIEVO);		
			}
		}//sorting 
		
		return result;
	}
	
	public Element generateASBIE(ASBIEVO gASBIE, Element gPNode){
		Element element = gPNode.getOwnerDocument().createElement("xsd:element");
		
		element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
		if(gASBIE.getCardinalityMax() == 0)
			element.setAttribute("maxOccurs" ,"unbounded");
		else
			element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
		if(gASBIE.getNillable() !=0)
			element.setAttribute("nillable", String.valueOf(gASBIE.getNillable()));
		
		gPNode.appendChild(element);
		return element;
	}
	
	public ASBIEPVO queryAssocToASBIEP(ASBIEVO gASBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASBIEP_ID", gASBIE.getAssocToASBIEPID());
		ASBIEPVO asbiepVO = (ASBIEPVO)dao.findObject(qc, conn);
		return asbiepVO;
	}
	
	public Element generateASBIEP(ASBIEPVO gASBIEP, Element gElementNode) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gASBIEP.getBasedASCCPID());
		ASCCPVO asccpVO = (ASCCPVO)dao.findObject(qc, conn);
		gElementNode.setAttribute("name", toCamelCase(asccpVO.getDEN().replaceAll("Type", "")));	
		return gElementNode;
	}
	

	
	public DTVO queryAssocBDT(BBIEVO gBBIE) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BCC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO bccVO = (BCCVO)dao.findObject(qc, conn);
		
		SRTDAO dao2 = df.getDAO("BCCP");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("BCCP_ID", bccVO.getAssocToBCCPID());
		BCCPVO bccpVO = (BCCPVO)dao2.findObject(qc2, conn);
		
		SRTDAO dao3 = df.getDAO("DT");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("DT_ID", bccpVO.getBDTID());
		DTVO aBDT = (DTVO)dao3.findObject(qc3, conn);
		
		return aBDT;
	}
	
	public Element generateBBIE(BBIEVO gBBIE, DTVO gBDT, Element gPNode, Element gSchemaNode) throws Exception{
		Element eNode = gPNode.getOwnerDocument().createElement("xsd:element"); 
		Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
	
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BCC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO bccVO = (BCCVO)dao.findObject(qc, conn);
		nameANode.setValue(toCamelCase(Utility.first(bccVO.getDEN())));
		eNode.setAttributeNode(nameANode);

		if(gBBIE.getDefaultText() != null && gBBIE.getFixedValue() != null) { 
			System.out.println("Error");
			return null;
		}
		
		if(gBBIE.getNillable() == 1) {
			Attr nillable = eNode.getOwnerDocument().createAttribute("nillable");
			nillable.setValue("true");
			eNode.setAttributeNode(nillable);
		}

		if(gBBIE.getDefaultText() != null) {
			Attr defaulta = eNode.getOwnerDocument().createAttribute("default");
			defaulta.setValue(gBBIE.getDefaultText());
			eNode.setAttributeNode(defaulta);
		}
		
		if(gBBIE.getFixedValue() != null) {
			Attr fixedvalue = eNode.getOwnerDocument().createAttribute("fixed");
			fixedvalue.setValue(gBBIE.getFixedValue());
			eNode.setAttributeNode(fixedvalue);
		}

		gPNode.appendChild(eNode);

		ArrayList<SRTObject> SCs = queryBBIESCs(gBBIE); 

		CodeListVO aCL = new CodeListVO();

		if(gBBIE.getCodeListId() != 0){
			SRTDAO dao1 = df.getDAO("CodeList");
	    	QueryCondition qc1 = new QueryCondition();
			qc1.add("Code_List_ID", gBBIE.getCodeListId());
			aCL = (CodeListVO)dao1.findObject(qc1, conn);
		}
		
		if(aCL.getCodeListID() == 0) {
	    	SRTDAO dao2 = df.getDAO("BDTPrimitiveRestriction");
	    	QueryCondition qc2_2 = new QueryCondition();
	    	qc2_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
	    	BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao2.findObject(qc2_2, conn);
	    	SRTDAO dao1 = df.getDAO("CodeList");		
	    	QueryCondition qc1_2 = new QueryCondition();
	    	qc1_2.add("Code_List_ID", aBDTPrimitiveRestriction.getCodeListID());	    	
			aCL = (CodeListVO)dao1.findObject(qc1_2, conn);
		}
		
		if(aCL.getCodeListID() == 0) {
			SRTDAO dao2 = df.getDAO("BDTPrimitiveRestriction");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("BDT_ID", gBDT.getDTID());
			qc2.add("isDefault", 1);
			BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao2.findObject(qc2, conn);
			if(aBDTPrimitiveRestriction.getBDTPrimitiveRestrictionID() == 0)
				aCL = null;
			else {
				SRTDAO dao1 = df.getDAO("CodeList");
		    	QueryCondition qc1_3 = new QueryCondition();
				qc1_3.add("Code_List_ID", aBDTPrimitiveRestriction.getCodeListID());
				if(aBDTPrimitiveRestriction.getCodeListID() !=0 )
					aCL = (CodeListVO)dao1.findObject(qc1_3, conn);
				else
					aCL = null;
			}
		}

		if(aCL == null) {
			if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");
					
					SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
			    	QueryCondition qc3 = new QueryCondition();
					qc3.add("BDT_ID", gBDT.getDTID());
					qc3.add("isDefault", 1);
					BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
					
					SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			    	QueryCondition qc4 = new QueryCondition();
					qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
					CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
					
					SRTDAO dao5 = df.getDAO("XSDBuiltInType");
			    	QueryCondition qc5 = new QueryCondition();
					qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
					
					tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
					Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
					Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");

					if(gPNode.getParentNode().getNextSibling() != null)
						gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
					else
						gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
					
					complextType.appendChild(simpleContent);
					simpleContent.appendChild(extNode);
					
					Attr base = extNode.getOwnerDocument().createAttribute("base");
					if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
						
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3 = new QueryCondition();
						qc3.add("BDT_ID", gBDT.getDTID());
						qc3.add("isDefault", 1);
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
						
						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					else {
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					extNode.setAttributeNode(base);
					eNode = generateSCs(gBBIE,eNode, SCs, gSchemaNode);
					return eNode;
				}
			}
			else {
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");

					SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
			    	QueryCondition qc3_2 = new QueryCondition();
					qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
					BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
					
					SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			    	QueryCondition qc4 = new QueryCondition();
					qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
					CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
					
					SRTDAO dao5 = df.getDAO("XSDBuiltInType");
			    	QueryCondition qc5 = new QueryCondition();
					qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

					tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
					Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
					Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
					if(gPNode.getParentNode().getNextSibling() != null)
						gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
					else
						gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
					complextType.appendChild(simpleContent);
					simpleContent.appendChild(extNode);
					
					Attr base = extNode.getOwnerDocument().createAttribute("base");
					if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
						
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3 = new QueryCondition();
						qc3.add("BDT_ID", gBDT.getDTID());
						qc3.add("isDefault", 1);
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
						
						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					else {
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					extNode.setAttributeNode(base);
					eNode = generateSCs(gBBIE,eNode, SCs, gSchemaNode);
					return eNode;
				}
			}
		}
		
		else { //is aCL null?
			if(!isCodeListGenerated(aCL)) 
				generateCodeList(aCL, gBDT, gSchemaNode); 
			
			if(SCs.size() == 0) {
				Attr tNode = eNode.getOwnerDocument().createAttribute("type");
				
				SRTDAO dao6 = df.getDAO("BDTPrimitiveRestriction");
		    	QueryCondition qc6 = new QueryCondition();
				qc6.add("BDT_ID", gBDT.getDTID());
				qc6.add("isDefault", 1);
				BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao6.findObject(qc6, conn);
				
				SRTDAO dao7 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		    	QueryCondition qc7 = new QueryCondition();
				qc7.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
				CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao7.findObject(qc7, conn);
				
				SRTDAO dao8 = df.getDAO("XSDBuiltInType");
		    	QueryCondition qc8 = new QueryCondition();
				qc8.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
				XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao8.findObject(qc8, conn);
				
				tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
				eNode.setAttributeNode(tNode);
				return eNode;
			}
			
			else {
				Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
				Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
				Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
				if(gPNode.getParentNode().getNextSibling() != null)
					gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
				else
					gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
				
				extNode.setNodeValue(getCodeListTypeName(aCL));
				complextType.appendChild(simpleContent);
				simpleContent.appendChild(extNode);
				
				Attr base = extNode.getOwnerDocument().createAttribute("base");
				base.setNodeValue(getCodeListTypeName(aCL));
				extNode.setAttributeNode(base);
				eNode = generateSCs(gBBIE, eNode, SCs, gSchemaNode);
				return eNode;
			}
		}
	}
	

	public ArrayList<SRTObject> queryBBIESCs(BBIEVO gBBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BBIE_SC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BBIE_ID", gBBIE.getBBIEID());
		ArrayList<SRTObject> bbiescVO = dao.findObjects(qc);
		return bbiescVO;
	}
	
	public boolean isCodeListGenerated(CodeListVO gCL) throws Exception {
		Vector<String> GuidVector = getGUIDVector();
		for(int i = 0; i < GuidVector.size(); i++) {
			if(gCL.getCodeListGUID().equals(GuidVector.get(i)))
				return true;
		}
		return false;
	}
	
	public String getCodeListTypeName(CodeListVO gCL) throws Exception {
		String CodeListTypeName = gCL.getName() + (gCL.getName().endsWith("Code") == true ? "" : "Code") + "ContentType" + "_" + gCL.getAgencyID() + "_" + gCL.getListID() + "_" + gCL.getVersionID();
		return CodeListTypeName;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTVO gBDT, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		gSchemaNode.appendChild(stNode);
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		stNode.setAttributeNode(stNameNode);
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());
		stNode.setAttributeNode(stIdNode);
		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTPrimitiveRestriction");
    	QueryCondition qc = new QueryCondition();
		qc.add("BDT_ID", gBDT.getDTID());
		qc.add("isDefault", 1);
		BDTPrimitiveRestrictionVO dPrim = (BDTPrimitiveRestrictionVO) dao.findObject(qc, conn);
		if(dPrim.getCodeListID() != 0) {
			base.setNodeValue("xsd:token");
		}
		else {
			SRTDAO dao2 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("CDT_Primitive_Expression_Type_Map_ID", dPrim.getCDTPrimitiveExpressionTypeMapID());
			CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO) dao2.findObject(qc2, conn);
	
			SRTDAO dao3 = df.getDAO("XSDBuiltInType");
	    	QueryCondition qc3 = new QueryCondition();
			qc3.add("XSD_BuiltIn_Type_ID", aCDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
			XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao3.findObject(qc3, conn);
			base.setNodeValue(aXSDBuiltInType.getBuiltInType());
		}
		rtNode.setAttributeNode(base);
		stNode.appendChild(rtNode);

		SRTDAO dao4 = df.getDAO("CodeListValue");
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("Code_List_ID", gCL.getCodeListID());
		ArrayList<SRTObject> codelistid = dao4.findObjects(qc4);
		ArrayList<CodeListValueVO> gCLVs = new ArrayList<CodeListValueVO>();

		for(int i = 0; i < codelistid.size(); i++){
			CodeListValueVO aCodeListValue = (CodeListValueVO)codelistid.get(i);
			if(aCodeListValue.getUsedIndicator())
				gCLVs.add(aCodeListValue);
		}
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);

		}		
		return stNode;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTSCVO gSC, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		gSchemaNode.appendChild(stNode);
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());

		stNode.setAttributeNode(stNameNode);
		stNode.setAttributeNode(stIdNode);

		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTSCPrimitiveRestriction");
    	QueryCondition qc = new QueryCondition();
		qc.add("BDT_SC_ID", gSC.getDTSCID());
		qc.add("isDefault", 1);
		BDTSCPrimitiveRestrictionVO dPrim = (BDTSCPrimitiveRestrictionVO) dao.findObject(qc, conn);
		if(dPrim.getCodeListID() != 0) {
			base.setNodeValue("xsd:token");
		}
		else {
			SRTDAO dao2 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", dPrim.getCDTSCAllowedPrimitiveExpressionTypeMapID());
			CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao2.findObject(qc2, conn);
	
			SRTDAO dao3 = df.getDAO("XSDBuiltInType");
	    	QueryCondition qc3 = new QueryCondition();
			qc3.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
			XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao3.findObject(qc3, conn);
			base.setNodeValue(aXSDBuiltInType.getBuiltInType());
		}

		stNode.appendChild(rtNode);
		rtNode.setAttributeNode(base);

		SRTDAO dao4 = df.getDAO("CodeListValue");
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("Code_List_ID", gCL.getCodeListID());
		ArrayList<SRTObject> codelistid = dao4.findObjects(qc4, conn);
		ArrayList<CodeListValueVO> gCLVs = new ArrayList<CodeListValueVO>();

		for(int i = 0; i < codelistid.size(); i++){
			CodeListValueVO aCodeListValue = (CodeListValueVO)codelistid.get(i);
			if(aCodeListValue.getUsedIndicator())
				gCLVs.add(aCodeListValue);				
		}
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);			
		}		
		return stNode;
	}
	
	public Element generateSCs(BBIEVO gBBIE, Element gBBIENode, ArrayList<SRTObject> gSCs, Element gSchemaNode) throws DOMException, Exception{
		
		Element tNode = (Element) gBBIENode.getParentNode().getParentNode().getNextSibling().getFirstChild().getFirstChild();
		for(int i = 0; i < gSCs.size(); i++) {//For each gSC[i]
			BBIE_SCVO aBBIESC = (BBIE_SCVO)gSCs.get(i);
			//Generate a DOM Element Node
			Element aNode = tNode.getOwnerDocument().createElement("xsd:attribute");
			//Handle gSC[i]
			if(aBBIESC.getDefaultText() != null && aBBIESC.getFixedValue() != null){
				System.out.println("default and fixed value options handling error");
				return null;
			}
			else if(aBBIESC.getDefaultText() != null){
				Attr default_att = aNode.getOwnerDocument().createAttribute("default");
				default_att.setNodeValue(aBBIESC.getDefaultText());
				aNode.setAttributeNode(default_att);
			}
			else if(aBBIESC.getFixedValue() != null){
				Attr fixed_att = aNode.getOwnerDocument().createAttribute("fixed");
				fixed_att.setNodeValue(aBBIESC.getFixedValue());
				aNode.setAttributeNode(fixed_att);
			}	
			// Generate a DOM Attribute node
			Attr aNameNode = aNode.getOwnerDocument().createAttribute("name");
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO dao = df.getDAO("DTSC");
	    	QueryCondition qc = new QueryCondition();
			qc.add("DT_SC_ID", aBBIESC.getDTSCID());
			DTSCVO aDTSC = (DTSCVO) dao.findObject(qc, conn);
			aNameNode.setNodeValue(toLowerCamelCase(aDTSC.getPropertyTerm()).concat(toCamelCase(aDTSC.getRepresentationTerm())));
			tNode.appendChild(aNode);
			aNode.setAttributeNode(aNameNode);

			//Get a code list object
			SRTDAO dao2 = df.getDAO("CodeList");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("Code_List_ID", aBBIESC.getCodeListId());
			SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("BDT_SC_Primitive_Restriction_ID", aBBIESC.getDTSCPrimitiveRestrictionID());
			CodeListVO aCL = null;
			BDTSCPrimitiveRestrictionVO aBDTSCPrimitiveRestriction = null;
			if(((CodeListVO)dao2.findObject(qc2, conn)).getCodeListID() != 0){ 
				aCL = (CodeListVO) dao2.findObject(qc2, conn);
			}
			
			else if (((BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn)).getBDTSCPrimitiveRestrictionID() != 0){
				aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
				QueryCondition qc2_2 = new QueryCondition();
				qc2_2.add("Code_List_ID", aBDTSCPrimitiveRestriction.getCodeListID());		
				aCL = (CodeListVO) dao2.findObject(qc2_2, conn);
			}
			
			else if(((CodeListVO)dao2.findObject(qc2, conn)).getCodeListID() == 0 && ((BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn)).getBDTSCPrimitiveRestrictionID() == 0) {
				QueryCondition qc3_2 = new QueryCondition();
				qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
				qc3_2.add("isDefault", 1);
				BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
				QueryCondition qc2_3 = new QueryCondition();
				qc2_3.add("Code_List_ID", bBDTSCPrimitiveRestriction.getCodeListID());		
				if(((CodeListVO)dao2.findObject(qc2_3, conn)).getCodeListID() == 0){
					aCL = null;
				}
				else {
					aCL = (CodeListVO) dao2.findObject(qc2_3, conn);
				}
			}

			if(aCL != null) {
				Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
				stIdNode.setNodeValue(aCL.getCodeListGUID());
				aNode.setAttributeNode(stIdNode);
			}
			
			AgencyIDListVO aAL = null;
			BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = null;
			
			if(aCL == null) { //aCL = null?
				SRTDAO dao4 = df.getDAO("AgencyIDList"); //Get an agency id list
		    	QueryCondition qc4 = new QueryCondition();
				qc4.add("Agency_ID_List_ID", aBBIESC.getAgencyIdListId());
				aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
				QueryCondition qc2_4 = new QueryCondition();
				qc2_4.add("Agency_ID_List_ID", aBDTSCPrimitiveRestriction.getAgencyIDListID());
				
				if(((AgencyIDListVO)dao4.findObject(qc4)).getAgencyIDListID() != 0)
					aAL = (AgencyIDListVO) dao4.findObject(qc4);
				
				else if(((AgencyIDListVO)dao4.findObject(qc2_4)).getAgencyIDListID() != 0){
					aAL = (AgencyIDListVO) dao4.findObject(qc2_4);
				}
				else if(((AgencyIDListVO)dao4.findObject(qc4)).getAgencyIDListID() == 0 && ((AgencyIDListVO) dao4.findObject(qc2_4)).getAgencyIDListID() == 0) {
					QueryCondition qc3_2 = new QueryCondition();
					qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
					qc3_2.add("isDefault", 1);
					bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
					QueryCondition qc2_5 = new QueryCondition();
					qc2_5.add("Agency_ID_List_ID", bBDTSCPrimitiveRestriction.getAgencyIDListID());		
					if(((AgencyIDListVO) dao4.findObject(qc2_5)).getAgencyIDListID() == 0){
						aAL = null;
					}
					else {
						aAL = (AgencyIDListVO) dao4.findObject(qc2_5);
					}
				}
				
				if(aAL != null) {
					Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
					stIdNode.setNodeValue(aAL.getAgencyIDListGUID());
					aNode.setAttributeNode(stIdNode);
				}

				if(aAL == null) { //aAL = null?
					int primRestriction = aBBIESC.getDTSCPrimitiveRestrictionID(); //primRestriction = gSCs[i]. DT_SC_Primitive_Rescrition_ID
					Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
					if(primRestriction == 0){ //primRestriction = null?
						QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
						qc3_2.add("isDefault", 1);
						bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2);
						SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc5 = new QueryCondition();
				    	qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", bBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
						CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5);
						SRTDAO dao6 = df.getDAO("XSDBuiltInType");
						QueryCondition qc6 = new QueryCondition();
						qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
						aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
					}
					else { //primRestriction = null?
						aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3);
						
						SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", aBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
						CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5);
						SRTDAO dao6 = df.getDAO("XSDBuiltInType");
						QueryCondition qc6 = new QueryCondition();
						qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
						aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
					}
					aNode.setAttributeNode(aTypeNode);
				}
				else { //aAL = null?
					if(!isAgencyListGenerated(aAL))  //isAgencyListGenerated(aAL)?
						generateAgencyList(aAL, aDTSC, gSchemaNode);
					Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
					aTypeNode.setNodeValue(getAgencyListTypeName(aAL));
					aNode.setAttributeNode(aTypeNode);
				}	
			}
			
			else { //aCL = null?
				if(!isCodeListGenerated(aCL)) //isCodeListGenerated(aCL)?
					generateCodeList(aCL, aDTSC, gSchemaNode);
				Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
				aTypeNode.setNodeValue(getCodeListTypeName(aCL));
				aNode.setAttributeNode(aTypeNode);
			}
		}
		return gBBIENode;
	}
	
	public boolean isAgencyListGenerated(AgencyIDListVO gAL) throws Exception {
		Vector<String> GuidVector = getGUIDVector();
		for(int i = 0; i < GuidVector.size(); i++) {
			if(gAL.getAgencyIDListGUID().equals(GuidVector.get(i)))
				return true;
		}
		return false;
	}

	public String getAgencyListTypeName(AgencyIDListVO gAL) throws Exception {
		String AgencyListTypeName = "clm" + gAL.getAgencyID() + gAL.getListID() + gAL.getVersionID() + "_" + toCamelCase(gAL.getName()) + "ContentType";
		return AgencyListTypeName;
	}
	
	public Element generateAgencyList(AgencyIDListVO gAL, DTSCVO gSC, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setNodeValue(getAgencyListTypeName(gAL));
		stNode.setAttributeNode(stNameNode);
				
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setNodeValue(gAL.getAgencyIDListGUID());
		stNode.setAttributeNode(stIdNode);
		
		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		base.setNodeValue("xsd:token");
		stNode.appendChild(rtNode);
		rtNode.setAttributeNode(base);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDListValue");
    	QueryCondition qc = new QueryCondition();
		qc.add("Agency_ID_List_ID", gAL.getAgencyIDListID());
		ArrayList<SRTObject> gALVs = dao.findObjects(qc);

		for(int i = 0; i< gALVs.size(); i++){
			AgencyIDListValueVO aAgencyIDListValue = (AgencyIDListValueVO)gALVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			stNode.appendChild(enumeration);
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(aAgencyIDListValue.getValue());
			stNode.setAttributeNode(value);
		}	
		return stNode;
	}
	
	public Vector<String> getGUIDVector() throws Exception{
		Vector<String> result = new Vector<String>();// Store code list and agency ID lists 
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		
		ArrayList<SRTObject> codelist = dao.findObjects();
		for(int i = 0; i < codelist.size(); i++){
			CodeListVO codelistVO = (CodeListVO)codelist.get(i);	
			result.add(codelistVO.getCodeListGUID());
		}
		dao = df.getDAO("AgencyIDList");
		ArrayList<SRTObject> agencyidlist = dao.findObjects();
		for(int i = 0; i < agencyidlist.size(); i++){
			AgencyIDListVO agencyIDListVO = (AgencyIDListVO)agencyidlist.get(i);	
			result.add(agencyIDListVO.getAgencyIDListGUID());
		}

		return result;
	}

	
	public static String toCamelCase(final String init) {
	    if (init==null)
	        return null;

	    final StringBuilder ret = new StringBuilder(init.length());

	    for (String word : init.split(" ")) {
	    	if(word.startsWith("_"))
	    		word = word.substring(0, 1);
	    	
	    	if(word.equalsIgnoreCase("identifier"))
	        	ret.append("ID");      	

	        else if (!word.isEmpty()) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    }
	    return ret.toString();
	}
	
	public static String toLowerCamelCase(final String init) {
	    if (init==null)
	        return null;

	    final StringBuilder ret = new StringBuilder(init.length());
	    
	    int cnt = 0;
	    
	    for (String word : init.split(" ")) {
	    	if(word.startsWith("_"))
	    		word = word.substring(0, 1);
	    	
	    	if(word.equalsIgnoreCase("identifier"))
	        	ret.append("ID");      	

	        else if (!word.isEmpty() && cnt != 0 ) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    	
	        else if (!word.isEmpty() && cnt == 0 ) {
	            ret.append(word.substring(0, 1).toLowerCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    	
	    	cnt++;
	    }
	    return ret.toString();
	}
	
	public ASBIEPVO receiveASBIEP(int ABIE_ID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
		QueryCondition qc = new QueryCondition();
		qc.add("Role_Of_ABIE_ID", ABIE_ID);
		ASBIEPVO aASBIEPVO = (ASBIEPVO) dao.findObject(qc);
		return aASBIEPVO;
	}
	
	public ArrayList<SRTObject> receiveABIE(int based_acc_id[]) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		
		ArrayList<SRTObject> aABIEVO = new ArrayList<SRTObject>();
		for(int i = 0; i < based_acc_id.length ; i++) {
			QueryCondition qc = new QueryCondition();
			qc.add("Based_ACC_ID", based_acc_id[i]);
			aABIEVO.addAll(dao.findObjects(qc, conn));
		}
		return aABIEVO;
	}
	
	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		DBAgent tx = new DBAgent();
		conn = tx.open();

		StandaloneXMLSchema aa = new StandaloneXMLSchema();
		ArrayList<SRTObject> gABIE = aa.receiveABIE(acc_ids);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element schemaNode = aa.generateSchema(doc);
		Calendar aCalendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));

		if(schema_package_flag == false) {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = aa.receiveASBIEP(aABIEVO.getABIEID());
				System.out.println("Generating Top Level ABIE w/ given ASBIEPVO ID: "+ aASBIEPVO.getASBIEPID());
				doc = aa.generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
			}
			aa.writeXSDFile(doc, "packaged_file_"+(aCalendar.get(Calendar.MONTH)+1)+"_"+aCalendar.get(Calendar.DAY_OF_MONTH)+"_"+
					aCalendar.get(Calendar.YEAR)+"_"+(aCalendar.get(Calendar.HOUR_OF_DAY)+1)+"_"+aCalendar.get(Calendar.MINUTE)+"_"+aCalendar.get(Calendar.SECOND));
		}
		else {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = aa.receiveASBIEP(aABIEVO.getABIEID());
				doc = docBuilder.newDocument();
				schemaNode = aa.generateSchema(doc);
				doc = aa.generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
				aa.writeXSDFile(doc, "Package/" + aABIEVO.getAbieGUID());
			}
			Zip.main("packaged_zip_file_"+(aCalendar.get(Calendar.MONTH)+1)+"_"+aCalendar.get(Calendar.DAY_OF_MONTH)+"_"+
					aCalendar.get(Calendar.YEAR)+"_"+(aCalendar.get(Calendar.HOUR_OF_DAY)+1)+"_"+aCalendar.get(Calendar.MINUTE)+"_"+aCalendar.get(Calendar.SECOND));
		}
		System.out.println("###END###");
	}
=======
package org.oagi.srt.generate.standalone;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.AgencyIDListValueVO;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.BBIE_SCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StandaloneXMLSchema {

	private static Connection conn = null;
	public static int acc_ids[] = {22202};
	public static boolean schema_package_flag = false;

	
	public void writeXSDFile(Document doc, String filename) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("/Temp/test/"+filename+".xsd"));
		transformer.transform(source, result);
		System.out.println(filename + ".xsd is generated");
	}
	
	public Element generateSchema(Document doc) {
		Element schemaNode = doc.createElement("xsd:schema");
		schemaNode.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		schemaNode.setAttribute("xmlns","http://www.openapplications.org/oagis/10");
		schemaNode.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
		schemaNode.setAttribute("targetNamespace", "http://www.openapplications.org/oagis/10");
		schemaNode.setAttribute("elementFormDefault", "qualified");
		schemaNode.setAttribute("attributeFormDefault", "unqualified");
		doc.appendChild(schemaNode);
		return schemaNode;
	}
	
	public Document generateTopLevelABIE(ASBIEPVO tlASBIEP, Document tlABIEDOM, Element schemaNode) throws Exception {		
		Element rootEleNode = generateTopLevelASBIEP(tlASBIEP, schemaNode);
		ABIEVO aABIE = queryTargetABIE(tlASBIEP);
		Element rootSeqNode = generateABIE(aABIE, rootEleNode);
		schemaNode = generateBIEs(aABIE, rootSeqNode, schemaNode); 
		return tlABIEDOM;
	}
	
	public Element generateTopLevelASBIEP(ASBIEPVO gTlASBIEP, Element gSchemaNode) throws SRTDAOException {
		Element rootEleNode = gSchemaNode.getOwnerDocument().createElement("xsd:element"); 
		gSchemaNode.appendChild(rootEleNode);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gTlASBIEP.getBasedASCCPID());
		ArrayList<SRTObject> asccpvo = dao.findObjects(qc, conn);
		for(SRTObject aSRTObject : asccpvo){
			ASCCPVO asccpVO = (ASCCPVO)aSRTObject;
			rootEleNode.setAttribute("name", toCamelCase(Utility.first(asccpVO.getDEN().replaceAll("Type", ""))));
		}
		return rootEleNode;
	}
	
	public Element generateABIE(ABIEVO gABIE, Element gElementNode) throws SRTDAOException {
		Element complexType = gElementNode.getOwnerDocument().createElement("xsd:complexType");
		Element PNode = gElementNode.getOwnerDocument().createElement("xsd:sequence");
		Element origin_ElementNode = (Element) gElementNode.getOwnerDocument().getFirstChild();
		origin_ElementNode.appendChild(complexType);
		complexType.appendChild(PNode);
		return PNode;
	}

	public Element generateBIEs(ABIEVO gABIE, Element gPNode, Element gSchemaNode) throws Exception {
		ArrayList<SRTObject> childBIEs = queryChildBIEs(gABIE);
		Element node = null;
		for(SRTObject aSRTObject : childBIEs){
			if(aSRTObject.getClass().getCanonicalName().endsWith("ASBIEVO")){
				ASBIEVO childBIE = (ASBIEVO)aSRTObject;
				node = generateASBIE(childBIE, gPNode);
				ASBIEPVO anASBIEP = queryAssocToASBIEP(childBIE);
				node = generateASBIEP(anASBIEP, node);
				ABIEVO anABIE = queryTargetABIE2(anASBIEP);
				
				ArrayList<SRTObject> next_childBIEs = queryChildBIEs(anABIE);
				if(next_childBIEs.size() > 0) {
				node = generateABIE(anABIE, node);
				node = generateBIEs(anABIE, node, gSchemaNode);
				}
			}
			else {
				BBIEVO childBIE = (BBIEVO)aSRTObject;
				DTVO aBDT = queryAssocBDT(childBIE);
				node = generateBBIE(childBIE, aBDT, gPNode, gSchemaNode);
			}
		}
		return gSchemaNode;
	}
	
	public ABIEVO queryTargetABIE(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abievo = (ABIEVO)dao.findObject(qc, conn);
		return abievo;
	}
	
	public ABIEVO queryTargetABIE2(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
    	QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abieVO = (ABIEVO)dao.findObject(qc, conn);		
		return abieVO;
	}
	
	public ArrayList<SRTObject> queryChildBIEs(ABIEVO gABIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIE");
    	QueryCondition qc = new QueryCondition();
		qc.add("Assoc_From_ABIE_ID", gABIE.getABIEID());
		ArrayList<SRTObject> asbievo = dao.findObjects(qc, conn);

		SRTDAO dao2 = df.getDAO("BBIE");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("Assoc_From_ABIE_ID", gABIE.getABIEID());
		ArrayList<SRTObject> bbievo = dao2.findObjects(qc2, conn);
		ArrayList<SRTObject> result = new ArrayList<SRTObject>();

		for(SRTObject aSRTObject : asbievo){
			ASBIEVO aASBIEVO = (ASBIEVO)aSRTObject;
			if(aASBIEVO.getCardinalityMax() != 0){
				result.add(aASBIEVO);
			}
		}
		
		for(SRTObject aSRTObject : bbievo){
			BBIEVO aBBIEVO = (BBIEVO)aSRTObject;
			if(aBBIEVO.getCardinalityMax() != 0){
				result.add(aBBIEVO);		
			}
		}//sorting 
		
		return result;
	}
	
	public Element generateASBIE(ASBIEVO gASBIE, Element gPNode){
		Element element = gPNode.getOwnerDocument().createElement("xsd:element");
		
		element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
		if(gASBIE.getCardinalityMax() == 0)
			element.setAttribute("maxOccurs" ,"unbounded");
		else
			element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
		if(gASBIE.getNillable() !=0)
			element.setAttribute("nillable", String.valueOf(gASBIE.getNillable()));
		
		gPNode.appendChild(element);
		return element;
	}
	
	public ASBIEPVO queryAssocToASBIEP(ASBIEVO gASBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASBIEP_ID", gASBIE.getAssocToASBIEPID());
		ASBIEPVO asbiepVO = (ASBIEPVO)dao.findObject(qc, conn);
		return asbiepVO;
	}
	
	public Element generateASBIEP(ASBIEPVO gASBIEP, Element gElementNode) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gASBIEP.getBasedASCCPID());
		ASCCPVO asccpVO = (ASCCPVO)dao.findObject(qc, conn);
		gElementNode.setAttribute("name", toCamelCase(asccpVO.getDEN().replaceAll("Type", "")));	
		return gElementNode;
	}
	

	
	public DTVO queryAssocBDT(BBIEVO gBBIE) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BCC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO bccVO = (BCCVO)dao.findObject(qc, conn);
		
		SRTDAO dao2 = df.getDAO("BCCP");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("BCCP_ID", bccVO.getAssocToBCCPID());
		BCCPVO bccpVO = (BCCPVO)dao2.findObject(qc2, conn);
		
		SRTDAO dao3 = df.getDAO("DT");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("DT_ID", bccpVO.getBDTID());
		DTVO aBDT = (DTVO)dao3.findObject(qc3, conn);
		
		return aBDT;
	}
	
	public Element generateBBIE(BBIEVO gBBIE, DTVO gBDT, Element gPNode, Element gSchemaNode) throws Exception{
		Element eNode = gPNode.getOwnerDocument().createElement("xsd:element"); 
		Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
	
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BCC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO bccVO = (BCCVO)dao.findObject(qc, conn);
		nameANode.setValue(toCamelCase(Utility.first(bccVO.getDEN())));
		eNode.setAttributeNode(nameANode);

		if(gBBIE.getDefaultText() != null && gBBIE.getFixedValue() != null) { 
			System.out.println("Error");
			return null;
		}
		
		if(gBBIE.getNillable() == 1) {
			Attr nillable = eNode.getOwnerDocument().createAttribute("nillable");
			nillable.setValue("true");
			eNode.setAttributeNode(nillable);
		}

		if(gBBIE.getDefaultText() != null) {
			Attr defaulta = eNode.getOwnerDocument().createAttribute("default");
			defaulta.setValue(gBBIE.getDefaultText());
			eNode.setAttributeNode(defaulta);
		}
		
		if(gBBIE.getFixedValue() != null) {
			Attr fixedvalue = eNode.getOwnerDocument().createAttribute("fixed");
			fixedvalue.setValue(gBBIE.getFixedValue());
			eNode.setAttributeNode(fixedvalue);
		}

		gPNode.appendChild(eNode);

		ArrayList<SRTObject> SCs = queryBBIESCs(gBBIE); 

		CodeListVO aCL = new CodeListVO();

		if(gBBIE.getCodeListId() != 0){
			SRTDAO dao1 = df.getDAO("CodeList");
	    	QueryCondition qc1 = new QueryCondition();
			qc1.add("Code_List_ID", gBBIE.getCodeListId());
			aCL = (CodeListVO)dao1.findObject(qc1, conn);
		}
		
		if(aCL.getCodeListID() == 0) {
	    	SRTDAO dao2 = df.getDAO("BDTPrimitiveRestriction");
	    	QueryCondition qc2_2 = new QueryCondition();
	    	qc2_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
	    	BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao2.findObject(qc2_2, conn);
	    	SRTDAO dao1 = df.getDAO("CodeList");		
	    	QueryCondition qc1_2 = new QueryCondition();
	    	qc1_2.add("Code_List_ID", aBDTPrimitiveRestriction.getCodeListID());	    	
			aCL = (CodeListVO)dao1.findObject(qc1_2, conn);
		}
		
		if(aCL.getCodeListID() == 0) {
			SRTDAO dao2 = df.getDAO("BDTPrimitiveRestriction");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("BDT_ID", gBDT.getDTID());
			qc2.add("isDefault", 1);
			BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao2.findObject(qc2, conn);
			if(aBDTPrimitiveRestriction.getBDTPrimitiveRestrictionID() == 0)
				aCL = null;
			else {
				SRTDAO dao1 = df.getDAO("CodeList");
		    	QueryCondition qc1_3 = new QueryCondition();
				qc1_3.add("Code_List_ID", aBDTPrimitiveRestriction.getCodeListID());
				if(aBDTPrimitiveRestriction.getCodeListID() !=0 )
					aCL = (CodeListVO)dao1.findObject(qc1_3, conn);
				else
					aCL = null;
			}
		}

		if(aCL == null) {
			if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");
					
					SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
			    	QueryCondition qc3 = new QueryCondition();
					qc3.add("BDT_ID", gBDT.getDTID());
					qc3.add("isDefault", 1);
					BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
					
					SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			    	QueryCondition qc4 = new QueryCondition();
					qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
					CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
					
					SRTDAO dao5 = df.getDAO("XSDBuiltInType");
			    	QueryCondition qc5 = new QueryCondition();
					qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
					
					tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
					Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
					Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");

					if(gPNode.getParentNode().getNextSibling() != null)
						gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
					else
						gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
					
					complextType.appendChild(simpleContent);
					simpleContent.appendChild(extNode);
					
					Attr base = extNode.getOwnerDocument().createAttribute("base");
					if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
						
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3 = new QueryCondition();
						qc3.add("BDT_ID", gBDT.getDTID());
						qc3.add("isDefault", 1);
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
						
						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					else {
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					extNode.setAttributeNode(base);
					eNode = generateSCs(gBBIE,eNode, SCs, gSchemaNode);
					return eNode;
				}
			}
			else {
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");

					SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
			    	QueryCondition qc3_2 = new QueryCondition();
					qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
					BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
					
					SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			    	QueryCondition qc4 = new QueryCondition();
					qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
					CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
					
					SRTDAO dao5 = df.getDAO("XSDBuiltInType");
			    	QueryCondition qc5 = new QueryCondition();
					qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

					tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
					Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
					Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
					if(gPNode.getParentNode().getNextSibling() != null)
						gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
					else
						gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
					complextType.appendChild(simpleContent);
					simpleContent.appendChild(extNode);
					
					Attr base = extNode.getOwnerDocument().createAttribute("base");
					if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
						
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3 = new QueryCondition();
						qc3.add("BDT_ID", gBDT.getDTID());
						qc3.add("isDefault", 1);
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);
						
						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					else {
						SRTDAO dao3 = df.getDAO("BDTPrimitiveRestriction");
				    	QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_Primitive_Restriction_ID", gBBIE.getBdtPrimitiveRestrictionId());
						BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao3.findObject(qc3_2, conn);
						
						SRTDAO dao4 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc4 = new QueryCondition();
						qc4.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao4.findObject(qc4, conn);
						
						SRTDAO dao5 = df.getDAO("XSDBuiltInType");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao5.findObject(qc5, conn);

						base.setValue(aXSDBuiltInTypeVO.getBuiltInType());
					}
					extNode.setAttributeNode(base);
					eNode = generateSCs(gBBIE,eNode, SCs, gSchemaNode);
					return eNode;
				}
			}
		}
		
		else { //is aCL null?
			if(!isCodeListGenerated(aCL)) 
				generateCodeList(aCL, gBDT, gSchemaNode); 
			
			if(SCs.size() == 0) {
				Attr tNode = eNode.getOwnerDocument().createAttribute("type");
				
				SRTDAO dao6 = df.getDAO("BDTPrimitiveRestriction");
		    	QueryCondition qc6 = new QueryCondition();
				qc6.add("BDT_ID", gBDT.getDTID());
				qc6.add("isDefault", 1);
				BDTPrimitiveRestrictionVO aBDTPrimitiveRestriction = (BDTPrimitiveRestrictionVO)dao6.findObject(qc6, conn);
				
				SRTDAO dao7 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		    	QueryCondition qc7 = new QueryCondition();
				qc7.add("CDT_Primitive_Expression_Type_Map_ID", aBDTPrimitiveRestriction.getCDTPrimitiveExpressionTypeMapID());
				CDTAllowedPrimitiveExpressionTypeMapVO aDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO)dao7.findObject(qc7, conn);
				
				SRTDAO dao8 = df.getDAO("XSDBuiltInType");
		    	QueryCondition qc8 = new QueryCondition();
				qc8.add("XSD_BuiltIn_Type_ID", aDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
				XSDBuiltInTypeVO aXSDBuiltInTypeVO = (XSDBuiltInTypeVO)dao8.findObject(qc8, conn);
				
				tNode.setValue(aXSDBuiltInTypeVO.getBuiltInType());
				eNode.setAttributeNode(tNode);
				return eNode;
			}
			
			else {
				Element complextType = eNode.getOwnerDocument().createElement("xsd:complexType");
				Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
				Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
				if(gPNode.getParentNode().getNextSibling() != null)
					gSchemaNode.insertBefore(complextType, gPNode.getParentNode().getNextSibling());
				else
					gSchemaNode.insertBefore(complextType, gSchemaNode.getNextSibling());
				
				extNode.setNodeValue(getCodeListTypeName(aCL));
				complextType.appendChild(simpleContent);
				simpleContent.appendChild(extNode);
				
				Attr base = extNode.getOwnerDocument().createAttribute("base");
				base.setNodeValue(getCodeListTypeName(aCL));
				extNode.setAttributeNode(base);
				eNode = generateSCs(gBBIE, eNode, SCs, gSchemaNode);
				return eNode;
			}
		}
	}
	

	public ArrayList<SRTObject> queryBBIESCs(BBIEVO gBBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BBIE_SC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BBIE_ID", gBBIE.getBBIEID());
		ArrayList<SRTObject> bbiescVO = dao.findObjects(qc);
		return bbiescVO;
	}
	
	public boolean isCodeListGenerated(CodeListVO gCL) throws Exception {
		Vector<String> GuidVector = getGUIDVector();
		for(int i = 0; i < GuidVector.size(); i++) {
			if(gCL.getCodeListGUID().equals(GuidVector.get(i)))
				return true;
		}
		return false;
	}
	
	public String getCodeListTypeName(CodeListVO gCL) throws Exception {
		String CodeListTypeName = gCL.getName() + (gCL.getName().endsWith("Code") == true ? "" : "Code") + "ContentType" + "_" + gCL.getAgencyID() + "_" + gCL.getListID() + "_" + gCL.getVersionID();
		return CodeListTypeName;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTVO gBDT, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		gSchemaNode.appendChild(stNode);
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		stNode.setAttributeNode(stNameNode);
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());
		stNode.setAttributeNode(stIdNode);
		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTPrimitiveRestriction");
    	QueryCondition qc = new QueryCondition();
		qc.add("BDT_ID", gBDT.getDTID());
		qc.add("isDefault", 1);
		BDTPrimitiveRestrictionVO dPrim = (BDTPrimitiveRestrictionVO) dao.findObject(qc, conn);
		if(dPrim.getCodeListID() != 0) {
			base.setNodeValue("xsd:token");
		}
		else {
			SRTDAO dao2 = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("CDT_Primitive_Expression_Type_Map_ID", dPrim.getCDTPrimitiveExpressionTypeMapID());
			CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMap = (CDTAllowedPrimitiveExpressionTypeMapVO) dao2.findObject(qc2, conn);
	
			SRTDAO dao3 = df.getDAO("XSDBuiltInType");
	    	QueryCondition qc3 = new QueryCondition();
			qc3.add("XSD_BuiltIn_Type_ID", aCDTAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
			XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao3.findObject(qc3, conn);
			base.setNodeValue(aXSDBuiltInType.getBuiltInType());
		}
		rtNode.setAttributeNode(base);
		stNode.appendChild(rtNode);

		SRTDAO dao4 = df.getDAO("CodeListValue");
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("Code_List_ID", gCL.getCodeListID());
		ArrayList<SRTObject> codelistid = dao4.findObjects(qc4);
		ArrayList<CodeListValueVO> gCLVs = new ArrayList<CodeListValueVO>();

		for(int i = 0; i < codelistid.size(); i++){
			CodeListValueVO aCodeListValue = (CodeListValueVO)codelistid.get(i);
			if(aCodeListValue.getUsedIndicator())
				gCLVs.add(aCodeListValue);
		}
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);

		}		
		return stNode;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTSCVO gSC, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		gSchemaNode.appendChild(stNode);
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());

		stNode.setAttributeNode(stNameNode);
		stNode.setAttributeNode(stIdNode);

		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTSCPrimitiveRestriction");
    	QueryCondition qc = new QueryCondition();
		qc.add("BDT_SC_ID", gSC.getDTSCID());
		qc.add("isDefault", 1);
		BDTSCPrimitiveRestrictionVO dPrim = (BDTSCPrimitiveRestrictionVO) dao.findObject(qc, conn);
		if(dPrim.getCodeListID() != 0) {
			base.setNodeValue("xsd:token");
		}
		else {
			SRTDAO dao2 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", dPrim.getCDTSCAllowedPrimitiveExpressionTypeMapID());
			CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao2.findObject(qc2, conn);
	
			SRTDAO dao3 = df.getDAO("XSDBuiltInType");
	    	QueryCondition qc3 = new QueryCondition();
			qc3.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
			XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao3.findObject(qc3, conn);
			base.setNodeValue(aXSDBuiltInType.getBuiltInType());
		}

		stNode.appendChild(rtNode);
		rtNode.setAttributeNode(base);

		SRTDAO dao4 = df.getDAO("CodeListValue");
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("Code_List_ID", gCL.getCodeListID());
		ArrayList<SRTObject> codelistid = dao4.findObjects(qc4, conn);
		ArrayList<CodeListValueVO> gCLVs = new ArrayList<CodeListValueVO>();

		for(int i = 0; i < codelistid.size(); i++){
			CodeListValueVO aCodeListValue = (CodeListValueVO)codelistid.get(i);
			if(aCodeListValue.getUsedIndicator())
				gCLVs.add(aCodeListValue);				
		}
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);			
		}		
		return stNode;
	}
	
	public Element generateSCs(BBIEVO gBBIE, Element gBBIENode, ArrayList<SRTObject> gSCs, Element gSchemaNode) throws DOMException, Exception{
		
		Element tNode = (Element) gBBIENode.getParentNode().getParentNode().getNextSibling().getFirstChild().getFirstChild();
		for(int i = 0; i < gSCs.size(); i++) {//For each gSC[i]
			BBIE_SCVO aBBIESC = (BBIE_SCVO)gSCs.get(i);
			//Generate a DOM Element Node
			Element aNode = tNode.getOwnerDocument().createElement("xsd:attribute");
			//Handle gSC[i]
			if(aBBIESC.getDefault() != null && aBBIESC.getFixedValue() != null){
				System.out.println("default and fixed value options handling error");
				return null;
			}
			else if(aBBIESC.getDefault() != null){
				Attr default_att = aNode.getOwnerDocument().createAttribute("default");
				default_att.setNodeValue(aBBIESC.getDefault());
				aNode.setAttributeNode(default_att);
			}
			else if(aBBIESC.getFixedValue() != null){
				Attr fixed_att = aNode.getOwnerDocument().createAttribute("fixed");
				fixed_att.setNodeValue(aBBIESC.getFixedValue());
				aNode.setAttributeNode(fixed_att);
			}	
			// Generate a DOM Attribute node
			Attr aNameNode = aNode.getOwnerDocument().createAttribute("name");
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO dao = df.getDAO("DTSC");
	    	QueryCondition qc = new QueryCondition();
			qc.add("DT_SC_ID", aBBIESC.getDTSCID());
			DTSCVO aDTSC = (DTSCVO) dao.findObject(qc, conn);
			aNameNode.setNodeValue(toLowerCamelCase(aDTSC.getPropertyTerm()).concat(toCamelCase(aDTSC.getRepresentationTerm())));
			tNode.appendChild(aNode);
			aNode.setAttributeNode(aNameNode);

			//Get a code list object
			SRTDAO dao2 = df.getDAO("CodeList");
	    	QueryCondition qc2 = new QueryCondition();
			qc2.add("Code_List_ID", aBBIESC.getCodeListId());
			SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("BDT_SC_Primitive_Restriction_ID", aBBIESC.getDTSCPrimitiveRestrictionID());
			CodeListVO aCL = null;
			BDTSCPrimitiveRestrictionVO aBDTSCPrimitiveRestriction = null;
			if(((CodeListVO)dao2.findObject(qc2, conn)).getCodeListID() != 0){ 
				aCL = (CodeListVO) dao2.findObject(qc2, conn);
			}
			
			else if (((BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn)).getBDTSCPrimitiveRestrictionID() != 0){
				aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
				QueryCondition qc2_2 = new QueryCondition();
				qc2_2.add("Code_List_ID", aBDTSCPrimitiveRestriction.getCodeListID());		
				aCL = (CodeListVO) dao2.findObject(qc2_2, conn);
			}
			
			else if(((CodeListVO)dao2.findObject(qc2, conn)).getCodeListID() == 0 && ((BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn)).getBDTSCPrimitiveRestrictionID() == 0) {
				QueryCondition qc3_2 = new QueryCondition();
				qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
				qc3_2.add("isDefault", 1);
				BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
				QueryCondition qc2_3 = new QueryCondition();
				qc2_3.add("Code_List_ID", bBDTSCPrimitiveRestriction.getCodeListID());		
				if(((CodeListVO)dao2.findObject(qc2_3, conn)).getCodeListID() == 0){
					aCL = null;
				}
				else {
					aCL = (CodeListVO) dao2.findObject(qc2_3, conn);
				}
			}

			if(aCL != null) {
				Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
				stIdNode.setNodeValue(aCL.getCodeListGUID());
				aNode.setAttributeNode(stIdNode);
			}
			
			AgencyIDListVO aAL = null;
			BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = null;
			
			if(aCL == null) { //aCL = null?
				SRTDAO dao4 = df.getDAO("AgencyIDList"); //Get an agency id list
		    	QueryCondition qc4 = new QueryCondition();
				qc4.add("Agency_ID_List_ID", aBBIESC.getAgencyIdListId());
				aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
				QueryCondition qc2_4 = new QueryCondition();
				qc2_4.add("Agency_ID_List_ID", aBDTSCPrimitiveRestriction.getAgencyIDListID());
				
				if(((AgencyIDListVO)dao4.findObject(qc4)).getAgencyIDListID() != 0)
					aAL = (AgencyIDListVO) dao4.findObject(qc4);
				
				else if(((AgencyIDListVO)dao4.findObject(qc2_4)).getAgencyIDListID() != 0){
					aAL = (AgencyIDListVO) dao4.findObject(qc2_4);
				}
				else if(((AgencyIDListVO)dao4.findObject(qc4)).getAgencyIDListID() == 0 && ((AgencyIDListVO) dao4.findObject(qc2_4)).getAgencyIDListID() == 0) {
					QueryCondition qc3_2 = new QueryCondition();
					qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
					qc3_2.add("isDefault", 1);
					bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
					QueryCondition qc2_5 = new QueryCondition();
					qc2_5.add("Agency_ID_List_ID", bBDTSCPrimitiveRestriction.getAgencyIDListID());		
					if(((AgencyIDListVO) dao4.findObject(qc2_5)).getAgencyIDListID() == 0){
						aAL = null;
					}
					else {
						aAL = (AgencyIDListVO) dao4.findObject(qc2_5);
					}
				}
				
				if(aAL != null) {
					Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
					stIdNode.setNodeValue(aAL.getAgencyIDListGUID());
					aNode.setAttributeNode(stIdNode);
				}

				if(aAL == null) { //aAL = null?
					int primRestriction = aBBIESC.getDTSCPrimitiveRestrictionID(); //primRestriction = gSCs[i]. DT_SC_Primitive_Rescrition_ID
					Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
					if(primRestriction == 0){ //primRestriction = null?
						QueryCondition qc3_2 = new QueryCondition();
						qc3_2.add("BDT_SC_ID", aDTSC.getDTSCID());
						qc3_2.add("isDefault", 1);
						bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2);
						SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc5 = new QueryCondition();
				    	qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", bBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
						CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5);
						SRTDAO dao6 = df.getDAO("XSDBuiltInType");
						QueryCondition qc6 = new QueryCondition();
						qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
						aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
					}
					else { //primRestriction = null?
						aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3);
						
						SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
				    	QueryCondition qc5 = new QueryCondition();
						qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", aBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
						CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5);
						SRTDAO dao6 = df.getDAO("XSDBuiltInType");
						QueryCondition qc6 = new QueryCondition();
						qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
						aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
					}
					aNode.setAttributeNode(aTypeNode);
				}
				else { //aAL = null?
					if(!isAgencyListGenerated(aAL))  //isAgencyListGenerated(aAL)?
						generateAgencyList(aAL, aDTSC, gSchemaNode);
					Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
					aTypeNode.setNodeValue(getAgencyListTypeName(aAL));
					aNode.setAttributeNode(aTypeNode);
				}	
			}
			
			else { //aCL = null?
				if(!isCodeListGenerated(aCL)) //isCodeListGenerated(aCL)?
					generateCodeList(aCL, aDTSC, gSchemaNode);
				Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
				aTypeNode.setNodeValue(getCodeListTypeName(aCL));
				aNode.setAttributeNode(aTypeNode);
			}
		}
		return gBBIENode;
	}
	
	public boolean isAgencyListGenerated(AgencyIDListVO gAL) throws Exception {
		Vector<String> GuidVector = getGUIDVector();
		for(int i = 0; i < GuidVector.size(); i++) {
			if(gAL.getAgencyIDListGUID().equals(GuidVector.get(i)))
				return true;
		}
		return false;
	}

	public String getAgencyListTypeName(AgencyIDListVO gAL) throws Exception {
		String AgencyListTypeName = "clm" + gAL.getAgencyID() + gAL.getListID() + gAL.getVersionID() + "_" + toCamelCase(gAL.getName()) + "ContentType";
		return AgencyListTypeName;
	}
	
	public Element generateAgencyList(AgencyIDListVO gAL, DTSCVO gSC, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setNodeValue(getAgencyListTypeName(gAL));
		stNode.setAttributeNode(stNameNode);
				
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setNodeValue(gAL.getAgencyIDListGUID());
		stNode.setAttributeNode(stIdNode);
		
		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		base.setNodeValue("xsd:token");
		stNode.appendChild(rtNode);
		rtNode.setAttributeNode(base);

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDListValue");
    	QueryCondition qc = new QueryCondition();
		qc.add("Agency_ID_List_ID", gAL.getAgencyIDListID());
		ArrayList<SRTObject> gALVs = dao.findObjects(qc);

		for(int i = 0; i< gALVs.size(); i++){
			AgencyIDListValueVO aAgencyIDListValue = (AgencyIDListValueVO)gALVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			stNode.appendChild(enumeration);
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(aAgencyIDListValue.getValue());
			stNode.setAttributeNode(value);
		}	
		return stNode;
	}
	
	public Vector<String> getGUIDVector() throws Exception{
		Vector<String> result = new Vector<String>();// Store code list and agency ID lists 
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		
		ArrayList<SRTObject> codelist = dao.findObjects();
		for(int i = 0; i < codelist.size(); i++){
			CodeListVO codelistVO = (CodeListVO)codelist.get(i);	
			result.add(codelistVO.getCodeListGUID());
		}
		dao = df.getDAO("AgencyIDList");
		ArrayList<SRTObject> agencyidlist = dao.findObjects();
		for(int i = 0; i < agencyidlist.size(); i++){
			AgencyIDListVO agencyIDListVO = (AgencyIDListVO)agencyidlist.get(i);	
			result.add(agencyIDListVO.getAgencyIDListGUID());
		}

		return result;
	}

	
	public static String toCamelCase(final String init) {
	    if (init==null)
	        return null;

	    final StringBuilder ret = new StringBuilder(init.length());

	    for (String word : init.split(" ")) {
	    	if(word.startsWith("_"))
	    		word = word.substring(0, 1);
	    	
	    	if(word.equalsIgnoreCase("identifier"))
	        	ret.append("ID");      	

	        else if (!word.isEmpty()) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    }
	    return ret.toString();
	}
	
	public static String toLowerCamelCase(final String init) {
	    if (init==null)
	        return null;

	    final StringBuilder ret = new StringBuilder(init.length());
	    
	    int cnt = 0;
	    
	    for (String word : init.split(" ")) {
	    	if(word.startsWith("_"))
	    		word = word.substring(0, 1);
	    	
	    	if(word.equalsIgnoreCase("identifier"))
	        	ret.append("ID");      	

	        else if (!word.isEmpty() && cnt != 0 ) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    	
	        else if (!word.isEmpty() && cnt == 0 ) {
	            ret.append(word.substring(0, 1).toLowerCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	    	
	    	cnt++;
	    }
	    return ret.toString();
	}
	
	public ASBIEPVO receiveASBIEP(int ABIE_ID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
		QueryCondition qc = new QueryCondition();
		qc.add("Role_Of_ABIE_ID", ABIE_ID);
		ASBIEPVO aASBIEPVO = (ASBIEPVO) dao.findObject(qc);
		return aASBIEPVO;
	}
	
	public ArrayList<SRTObject> receiveABIE(int based_acc_id[]) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		
		ArrayList<SRTObject> aABIEVO = new ArrayList<SRTObject>();
		for(int i = 0; i < based_acc_id.length ; i++) {
			QueryCondition qc = new QueryCondition();
			qc.add("Based_ACC_ID", based_acc_id[i]);
			aABIEVO.addAll(dao.findObjects(qc, conn));
		}
		return aABIEVO;
	}
	
	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		DBAgent tx = new DBAgent();
		conn = tx.open();

		StandaloneXMLSchema aa = new StandaloneXMLSchema();
		ArrayList<SRTObject> gABIE = aa.receiveABIE(acc_ids);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element schemaNode = aa.generateSchema(doc);
		Calendar aCalendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));

		if(schema_package_flag == false) {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = aa.receiveASBIEP(aABIEVO.getABIEID());
				System.out.println("Generating Top Level ABIE w/ given ASBIEPVO ID: "+ aASBIEPVO.getASBIEPID());
				doc = aa.generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
			}
			aa.writeXSDFile(doc, "packaged_file_"+(aCalendar.get(Calendar.MONTH)+1)+"_"+aCalendar.get(Calendar.DAY_OF_MONTH)+"_"+
					aCalendar.get(Calendar.YEAR)+"_"+(aCalendar.get(Calendar.HOUR_OF_DAY)+1)+"_"+aCalendar.get(Calendar.MINUTE)+"_"+aCalendar.get(Calendar.SECOND));
		}
		else {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = aa.receiveASBIEP(aABIEVO.getABIEID());
				doc = docBuilder.newDocument();
				schemaNode = aa.generateSchema(doc);
				doc = aa.generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
				aa.writeXSDFile(doc, "Package/" + aABIEVO.getAbieGUID());
			}
			Zip.main("packaged_zip_file_"+(aCalendar.get(Calendar.MONTH)+1)+"_"+aCalendar.get(Calendar.DAY_OF_MONTH)+"_"+
					aCalendar.get(Calendar.YEAR)+"_"+(aCalendar.get(Calendar.HOUR_OF_DAY)+1)+"_"+aCalendar.get(Calendar.MINUTE)+"_"+aCalendar.get(Calendar.SECOND));
		}
		System.out.println("###END###");
	}
>>>>>>> refs/remotes/origin/develop
}
