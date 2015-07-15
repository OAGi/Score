package org.oagi.srt.generate.standalone;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.chanchan.common.persistence.db.DBAgent;
import org.chanchan.common.persistence.file.BfFileSaveStrategy;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
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
	public static ArrayList<Integer> abie_ids = new ArrayList<Integer>();
	public static boolean schema_package_flag = false;
	private ArrayList<String> StoredCC = new ArrayList<String>();
	private ArrayList<String> GUID_array_list = new ArrayList<String>();
	
	public String writeXSDFile(Document doc, String filename) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		
		String filepath = SRTConstants.BOD_FILE_PATH + filename + ".xsd";
		StreamResult result = new StreamResult(new File(filepath));
		transformer.transform(source, result);
		System.out.println(filepath + " is generated");
		return filepath;
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
		Element rootSeqNode = generateABIE(aABIE, rootEleNode, schemaNode);
		schemaNode = generateBIEs(aABIE, rootSeqNode, schemaNode); 
		return tlABIEDOM;
	}
	
	public Element generateTopLevelASBIEP(ASBIEPVO tlASBIEP, Element gSchemaNode) throws Exception {
		
		ASCCPVO asccpVO = queryBasedASCCP(tlASBIEP);
		if(isCCStored(tlASBIEP.getASBIEPGUID()))
			return gSchemaNode;
		
		Element rootEleNode = gSchemaNode.getOwnerDocument().createElement("xsd:element"); 
		gSchemaNode.appendChild(rootEleNode);
		rootEleNode.setAttribute("name", asccpVO.getPropertyTerm().replaceAll(" ", ""));
		rootEleNode.setAttribute("id", tlASBIEP.getASBIEPGUID()); //rootEleNode.setAttribute("id", asccpVO.getASCCPGUID());
		//rootEleNode.setAttribute("type", Utility.second(asccpVO.getDEN()).replaceAll(" ", "")+"Type");
		Element annotation = gSchemaNode.getOwnerDocument().createElement("xsd:annotation"); 
		Element documentation = gSchemaNode.getOwnerDocument().createElement("xsd:documentation");
		documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
		documentation.setTextContent(asccpVO.getDefinition());
		rootEleNode.appendChild(annotation);
		annotation.appendChild(documentation);
		
		StoredCC.add(tlASBIEP.getASBIEPGUID());
		
		return rootEleNode;
	}
		
	public Element generateABIE (ABIEVO gABIE, Element gElementNode, Element gSchemaNode) throws Exception {
		//ACCVO gACC = queryBasedACC(gABIE);
		
		if(isCCStored(gABIE.getAbieGUID()))
			return gElementNode;
		Element complexType = gElementNode.getOwnerDocument().createElement("xsd:complexType");
		gElementNode.appendChild(complexType);
		Element PNode = generateACC(gABIE, complexType, gElementNode);
		return PNode;
	}
		
	public Element generateACC(ABIEVO gABIE, Element complexType, Element gElementNode) throws Exception{

		ACCVO gACC = queryBasedACC(gABIE);
		Element PNode = complexType.getOwnerDocument().createElement("xsd:sequence");
		//complexType.setAttribute("name", gACC.getObjectClassTerm().replaceAll(" ", "")+"Type"); 
		complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", gACC.getACCGUID());
		StoredCC.add(gACC.getACCGUID());
		Element annotation = gElementNode.getOwnerDocument().createElement("xsd:annotation"); 
		Element documentation = gElementNode.getOwnerDocument().createElement("xsd:documentation");
		documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
		documentation.setTextContent(gACC.getDefinition());
		complexType.appendChild(annotation);
		annotation.appendChild(documentation);
		complexType.appendChild(PNode);
		
		return PNode;
	}
	
	public Element generateBIEs(ABIEVO gABIE, Element gPNode, Element gSchemaNode) throws Exception {
		ArrayList<SRTObject> childBIEs = queryChildBIEs(gABIE);
		Element node = null;
		
		for(SRTObject aSRTObject : childBIEs){
			if(aSRTObject.getClass().getCanonicalName().endsWith("ASBIEVO")){
				ASBIEVO childBIE = (ASBIEVO)aSRTObject;
//				ASCCVO gASCC = queryBasedASCC(childBIE);
//				if(isCCStored(gASCC.getASCCGUID()))
//					continue;
				
				node = generateASBIE(childBIE, gPNode);
				ASBIEPVO anASBIEP = queryAssocToASBIEP(childBIE);
				node = generateASBIEP(anASBIEP, node);
				ABIEVO anABIE = queryTargetABIE2(anASBIEP);
				node = generateABIE(anABIE, node, gSchemaNode);
				node = generateBIEs(anABIE, node, gSchemaNode);
			}
			else {
				BBIEVO childBIE = (BBIEVO)aSRTObject;
//				BCCVO bccVO = queryBasedBCC(childBIE);
//				if(isCCStored(bccVO.getBCCGUID()))
//					continue;
				DTVO aBDT = queryAssocBDT(childBIE);
				generateBBIE(childBIE, aBDT, gPNode, gSchemaNode);				
			}
		}
		return gSchemaNode;
	}	

	public Element generateBDT(BBIEVO gBBIE, Element eNode, Element gSchemaNode, CodeListVO gCL) throws Exception{

		DTVO bDT = queryBDT(gBBIE);
//		if(isCCStored(bDT.getDTGUID()))
//			return eNode;

		Element complexType = eNode.getOwnerDocument().createElement("xsd:complexType");
		Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
		Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
		//complexType.setAttribute("name", Utility.DenToName(bDT.getDEN())); 
		complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getDTGUID());
		if(bDT.getDefinition() != null) {
			Element annotation = eNode.getOwnerDocument().createElement("xsd:annotation"); 
			Element documentation = eNode.getOwnerDocument().createElement("xsd:documentation");
			documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
			documentation.setTextContent(bDT.getDefinition());
			complexType.appendChild(annotation);
		}		
		StoredCC.add(bDT.getDTGUID());
		
		complexType.appendChild(simpleContent);
		simpleContent.appendChild(extNode);
		
		Attr base = extNode.getOwnerDocument().createAttribute("base");
		base.setNodeValue(getCodeListTypeName(gCL));
		extNode.setAttributeNode(base);
		eNode.appendChild(complexType);
		return eNode;
	}
	
	
	public Attr setBDTBase(DTVO gBDT, Attr base) throws Exception{
		DAOFactory df = DAOFactory.getDAOFactory();
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
		
		return base;
	}
	
	public Attr setBDTBase(BBIEVO gBBIE, Attr base) throws Exception{
		DAOFactory df = DAOFactory.getDAOFactory();
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
		
		return base;
	}

	public Element setBBIE_Attr_Type(DTVO gBDT, Element gNode) throws Exception{
		Attr type = gNode.getOwnerDocument().createAttribute("type");
		DAOFactory df = DAOFactory.getDAOFactory();
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
		type.setValue(aXSDBuiltInTypeVO.getBuiltInType()); //type.setValue(Utility.toCamelCase(aXSDBuiltInTypeVO.getName())+"Type");
		gNode.setAttributeNode(type);
		return gNode;
	}
	
	public Element setBBIE_Attr_Type(BBIEVO gBBIE, Element gNode) throws Exception{
		Attr type = gNode.getOwnerDocument().createAttribute("type");
		DAOFactory df = DAOFactory.getDAOFactory();
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
		type.setValue(aXSDBuiltInTypeVO.getBuiltInType()); //type.setValue(Utility.toCamelCase(aXSDBuiltInTypeVO.getName())+"Type");
		gNode.setAttributeNode(type);
		return gNode;
	}
	
	public Element generateBDT(BBIEVO gBBIE, Element eNode) throws Exception{
		
		DTVO bDT = queryBDT(gBBIE);
//		if(isCCStored(bDT.getDTGUID()))
//			return eNode;

		Element complexType = eNode.getOwnerDocument().createElement("xsd:complexType");
		Element simpleContent = eNode.getOwnerDocument().createElement("xsd:simpleContent");
		Element extNode = eNode.getOwnerDocument().createElement("xsd:extension");
		//complexType.setAttribute("name", Utility.DenToName(bDT.getDEN())); 
		complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getDTGUID());
		if(bDT.getDefinition() != null) {
			Element annotation = eNode.getOwnerDocument().createElement("xsd:annotation"); 
			Element documentation = eNode.getOwnerDocument().createElement("xsd:documentation");
			documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
			documentation.setTextContent(bDT.getDefinition());
			complexType.appendChild(annotation);
		}		
		StoredCC.add(bDT.getDTGUID());
		
		complexType.appendChild(simpleContent);
		simpleContent.appendChild(extNode);
		
		DTVO gBDT = queryAssocBDT(gBBIE);
		
		Attr base = extNode.getOwnerDocument().createAttribute("base");
		
		if(gBBIE.getBdtPrimitiveRestrictionId() == 0)			
			base = setBDTBase(gBDT, base);
		else
			base = setBDTBase(gBBIE, base);
		extNode.setAttributeNode(base);
		eNode.appendChild(complexType);
		return eNode;
	}
	
	public Element generateASBIE(ASBIEVO gASBIE, Element gPNode) throws Exception{
		
		ASCCVO gASCC = queryBasedASCC(gASBIE);
		
		Element element = gPNode.getOwnerDocument().createElement("xsd:element");
		element.setAttribute("id", gASBIE.getAsbieGuid()); //element.setAttribute("id", gASCC.getASCCGUID()); 
		element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
		if(gASBIE.getCardinalityMax() == -1)
			element.setAttribute("maxOccurs" ,"unbounded");
		else
			element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
		if(gASBIE.getNillable() !=0)
			element.setAttribute("nillable", String.valueOf(gASBIE.getNillable()));
		
		while(!gPNode.getNodeName().equals("xsd:sequence")) {
			gPNode = (Element) gPNode.getParentNode();
		}
		
		gPNode.appendChild(element);
		StoredCC.add(gASCC.getASCCGUID());//check
		return element;
	}
		
	public Element generateASBIEP(ASBIEPVO gASBIEP, Element gElementNode) throws SRTDAOException{
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gASBIEP.getBasedASCCPID());
		ASCCPVO asccp = (ASCCPVO)dao.findObject(qc, conn);
		gElementNode.setAttribute("name", Utility.first(asccp.getDEN()));	
		//gElementNode.setAttribute("type", Utility.second(asccp.getDEN())+"Type");
		return gElementNode;
		
	}

	public BCCVO queryBasedBCC(BBIEVO gBBIE) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BCC");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO bccVO = (BCCVO)dao.findObject(qc, conn);
		return bccVO;
	}
	
	public Element handleBBIE_Elementvalue(BBIEVO gBBIE, Element eNode) throws Exception {
		
		BCCVO bccVO = queryBasedBCC(gBBIE);
		Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
		nameANode.setValue(Utility.second(bccVO.getDEN())); 
		eNode.setAttributeNode(nameANode);
		eNode.setAttribute("id", gBBIE.getBbieGuid()); //eNode.setAttribute("id", bccVO.getBCCGUID());
		StoredCC.add(bccVO.getBCCGUID());
		if(gBBIE.getDefaultText() != null && gBBIE.getFixedValue() != null) { 
			System.out.println("Error");
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
		
		eNode.setAttribute("minOccurs", String.valueOf(gBBIE.getCardinalityMin()));
		if(gBBIE.getCardinalityMax() == -1)
			eNode.setAttribute("maxOccurs" ,"unbounded");
		else
			eNode.setAttribute("maxOccurs", String.valueOf(gBBIE.getCardinalityMax()));
		if(gBBIE.getNillable() !=0)
			eNode.setAttribute("nillable", String.valueOf(gBBIE.getNillable()));


		return eNode;
	}
	
	public Element handleBBIE_Attributevalue(BBIEVO gBBIE, Element eNode) throws Exception {
		
		BCCVO bccVO = queryBasedBCC(gBBIE);
		Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
		nameANode.setValue(Utility.second(bccVO.getDEN())); 
		eNode.setAttributeNode(nameANode);
		eNode.setAttribute("id", gBBIE.getBbieGuid()); //eNode.setAttribute("id", bccVO.getBCCGUID());
		StoredCC.add(bccVO.getBCCGUID());
		if(gBBIE.getDefaultText() != null && gBBIE.getFixedValue() != null) { 
			System.out.println("Error");
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
		if(gBBIE.getCardinalityMin() >= 1)
			eNode.setAttribute("use", "required");
		else
			eNode.setAttribute("use" ,"optional");
		return eNode;
	}
	
	public CodeListVO getCodeList(BBIEVO gBBIE, DTVO gBDT) throws Exception{
		CodeListVO aCL = new CodeListVO();
		
		DAOFactory df = DAOFactory.getDAOFactory();
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
		
		return aCL;
	}

	public Element setBBIEType(DTVO gBDT, Element gNode) throws Exception {
		Attr tNode = gNode.getOwnerDocument().createAttribute("type");
		
		DAOFactory df = DAOFactory.getDAOFactory();
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
		gNode.setAttributeNode(tNode);
		return gNode;
	}
	
	public Element setBBIEType(BBIEVO gBBIE, Element gNode) throws Exception {
		Attr tNode = gNode.getOwnerDocument().createAttribute("type");
		
		DAOFactory df = DAOFactory.getDAOFactory();
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
		gNode.setAttributeNode(tNode);

		return gNode;
	}
	
	public Element generateBBIE(BBIEVO gBBIE, DTVO gBDT, Element gPNode, Element gSchemaNode) throws Exception{
		
		BCCVO gBCC = queryBasedBCC(gBBIE);
		Element eNode = null;
		if(gBCC.getEntityType() == 1) {
			eNode = gPNode.getOwnerDocument().createElement("xsd:element"); 
			eNode = handleBBIE_Elementvalue(gBBIE, eNode);
			
			while(!gPNode.getNodeName().equals("xsd:sequence")) {
				gPNode = (Element) gPNode.getParentNode();
			}
			
			gPNode.appendChild(eNode);
			
			ArrayList<SRTObject> SCs = queryBBIESCs(gBBIE); 
			
			CodeListVO aCL = getCodeList(gBBIE, gBDT);
			
			if(aCL == null) {
				if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
					if(SCs.size() == 0) {
						eNode = setBBIEType(gBDT, eNode);
						return eNode;
					}
					else {
						eNode = generateBDT(gBBIE, eNode);
						eNode = generateSCs(gBBIE, eNode, SCs, gSchemaNode);
						return eNode;
					}
				}
				else {
					if(SCs.size() == 0) {
						eNode = setBBIEType(gBBIE, eNode);
						return eNode;
					}
					else {
						eNode = generateBDT(gBBIE, eNode);
						eNode = generateSCs(gBBIE, eNode, SCs, gSchemaNode);
						return eNode;
					}
				}
			}
			
			else { //is aCL null?
				if(!isCodeListGenerated(aCL)) {
					generateCodeList(aCL, gBDT, gSchemaNode); 
				}
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");
					tNode.setNodeValue(getCodeListTypeName(aCL));
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					eNode = generateBDT(gBBIE, eNode, gSchemaNode, aCL);
					eNode = generateSCs(gBBIE, eNode, SCs, gSchemaNode);
					return eNode;
				}
			}
			

		}
		else {
			eNode = gPNode.getOwnerDocument().createElement("xsd:attribute"); 
			eNode = handleBBIE_Attributevalue(gBBIE, eNode);
			
			while(!gPNode.getNodeName().equals("xsd:complexType")) {
				gPNode = (Element) gPNode.getParentNode();
			}
			
			gPNode.appendChild(eNode);
			
			ArrayList<SRTObject> SCs = queryBBIESCs(gBBIE); 
			
			CodeListVO aCL = getCodeList(gBBIE, gBDT);

			if(aCL == null) {
				if(gBBIE.getBdtPrimitiveRestrictionId() == 0) {
					if(SCs.size() == 0) {
						eNode = setBBIE_Attr_Type(gBDT, eNode);
						return eNode;
					}
					else {
						eNode = setBBIE_Attr_Type(gBBIE, eNode);
						return eNode;
					}
				}
				else {
					if(SCs.size() == 0) {
						eNode = setBBIE_Attr_Type(gBBIE, eNode);
						return eNode;
					}
					else {
						eNode = setBBIE_Attr_Type(gBBIE, eNode);
						return eNode;
					}
				}
			}
			
			else { //is aCL null?
				if(!isCodeListGenerated(aCL)) {
					generateCodeList(aCL, gBDT, gSchemaNode); 
				}
				if(SCs.size() == 0) {
					Attr tNode = eNode.getOwnerDocument().createAttribute("type");
					tNode.setNodeValue(getCodeListTypeName(aCL));
					eNode.setAttributeNode(tNode);
					return eNode;
				}
				else {
					if(gBBIE.getBdtPrimitiveRestrictionId() == 0){			
						eNode = setBBIE_Attr_Type(gBDT, eNode);
						return eNode;
					}
					else {
						eNode.setAttribute("type", getCodeListTypeName(aCL)); 
						return eNode;
					}
				}
			}
		}
	}
	
	public String getCodeListTypeName(CodeListVO gCL) throws Exception { //confirm
		//String CodeListTypeName ="xsd:string";
		String CodeListTypeName = gCL.getName() + (gCL.getName().endsWith("Code") == true ? "" : "Code") + "ContentType" + "_" + gCL.getAgencyID() + "_" + gCL.getListID() + "_" + gCL.getVersionID();
		return CodeListTypeName;
	}
	
	public Attr setCodeListRestrictionAttr(DTVO gBDT, Attr base) throws Exception {
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
		return base;
	}

	public Attr setCodeListRestrictionAttr(DTSCVO gSC, Attr base) throws Exception {
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
		return base;
	}
	
	public ArrayList<CodeListValueVO> getCodeListValues(CodeListVO gCL) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeListValue");
    	QueryCondition qc = new QueryCondition();
		qc.add("Code_List_ID", gCL.getCodeListID());
		ArrayList<SRTObject> codelistid = dao.findObjects(qc);
		ArrayList<CodeListValueVO> gCLVs = new ArrayList<CodeListValueVO>();

		for(int i = 0; i < codelistid.size(); i++){
			CodeListValueVO aCodeListValue = (CodeListValueVO)codelistid.get(i);
			if(aCodeListValue.getUsedIndicator())
				gCLVs.add(aCodeListValue);
		}
		
		return gCLVs;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTVO gBDT, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		stNode.setAttributeNode(stNameNode);
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());
		stNode.setAttributeNode(stIdNode);
		
		Element rtNode = stNode.getOwnerDocument().createElement("xsd:restriction");
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		base = setCodeListRestrictionAttr(gBDT, base);
		rtNode.setAttributeNode(base);
		stNode.appendChild(rtNode);

		ArrayList<CodeListValueVO> gCLVs = getCodeListValues(gCL);
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);
		}		
		GUID_array_list.add(gCL.getCodeListGUID());
		gSchemaNode.appendChild(stNode);
		return stNode;
	}
	
	public Element generateCodeList(CodeListVO gCL, DTSCVO gSC, Element gSchemaNode) throws DOMException, Exception {
		Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
		Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
		stNameNode.setValue(getCodeListTypeName(gCL));
		stNode.setAttributeNode(stNameNode);
		
		Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
		stIdNode.setValue(gCL.getCodeListGUID());
		stNode.setAttributeNode(stIdNode);
		
		Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");
		
		Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
		base = setCodeListRestrictionAttr(gSC, base);
		rtNode.setAttributeNode(base);
		stNode.appendChild(rtNode);
		
		ArrayList<CodeListValueVO> gCLVs = getCodeListValues(gCL);
		for(int i = 0; i< gCLVs.size(); i++){
			CodeListValueVO bCodeListValue = (CodeListValueVO)gCLVs.get(i);
			Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
			Attr value = stNode.getOwnerDocument().createAttribute("value");
			value.setNodeValue(bCodeListValue.getValue());
			enumeration.setAttributeNode(value);
			rtNode.appendChild(enumeration);			
		}		
		GUID_array_list.add(gCL.getCodeListGUID());
		gSchemaNode.appendChild(stNode);
		return stNode;
	}
	
	public Element handleBBIESCvalue(BBIE_SCVO aBBIESC, Element aNode) throws Exception {
		//Handle gSC[i]
		if(aBBIESC.getDefaultText() != null && aBBIESC.getFixedValue() != null){
			System.out.println("default and fixed value options handling error");
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
		aNameNode.setNodeValue(aDTSC.getPropertyTerm().replaceAll(" ", "").concat(aDTSC.getRepresentationTerm().replaceAll(" ", "")));
		aNode.setAttributeNode(aNameNode);
		return aNode;
	}
	
	public CodeListVO getCodeList(BBIE_SCVO gBBIESC) throws Exception{

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao2 = df.getDAO("CodeList");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("Code_List_ID", gBBIESC.getCodeListId());
		SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("BDT_SC_Primitive_Restriction_ID", gBBIESC.getDTSCPrimitiveRestrictionID());
		CodeListVO aCL = new CodeListVO();
		SRTDAO dao4 = df.getDAO("DTSC");
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("DT_SC_ID", gBBIESC.getDTSCID());
		DTSCVO gDTSC = (DTSCVO) dao4.findObject(qc4, conn);

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
			qc3_2.add("BDT_SC_ID", gDTSC.getDTSCID());
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
		return aCL;
	}
	
	public AgencyIDListVO getAgencyIDList(BBIE_SCVO gBBIESC) throws Exception {
		
		AgencyIDListVO aAL = new AgencyIDListVO();
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao4 = df.getDAO("AgencyIDList"); 
    	QueryCondition qc4 = new QueryCondition();
		qc4.add("Agency_ID_List_ID", gBBIESC.getAgencyIdListId());
		SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("BDT_SC_Primitive_Restriction_ID", gBBIESC.getDTSCPrimitiveRestrictionID());
		BDTSCPrimitiveRestrictionVO aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
		QueryCondition qc2_4 = new QueryCondition();
		qc2_4.add("Agency_ID_List_ID", aBDTSCPrimitiveRestriction.getAgencyIDListID());
		SRTDAO dao5 = df.getDAO("DTSC");
    	QueryCondition qc5 = new QueryCondition();
		qc5.add("DT_SC_ID", gBBIESC.getDTSCID());
		DTSCVO gDTSC = (DTSCVO) dao5.findObject(qc5, conn);
		
		if(((AgencyIDListVO)dao4.findObject(qc4, conn)) != null)
			aAL = (AgencyIDListVO) dao4.findObject(qc4, conn);
		
		if(((AgencyIDListVO)dao4.findObject(qc2_4, conn)) != null){
			aAL = (AgencyIDListVO) dao4.findObject(qc2_4, conn);
		}
		if(((AgencyIDListVO)dao4.findObject(qc4, conn)) == null && ((AgencyIDListVO) dao4.findObject(qc2_4, conn)) == null) {
			QueryCondition qc3_2 = new QueryCondition();
			qc3_2.add("BDT_SC_ID", gDTSC.getDTSCID());
			qc3_2.add("isDefault", 1);
			BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
			QueryCondition qc2_5 = new QueryCondition();
			qc2_5.add("Agency_ID_List_ID", bBDTSCPrimitiveRestriction.getAgencyIDListID());		
			if(((AgencyIDListVO) dao4.findObject(qc2_5, conn)) == null){
				aAL = null;
			}
			else {
				aAL = (AgencyIDListVO) dao4.findObject(qc2_5, conn);
			}
		}
		return aAL;
	}
	
	public Element setBBIESCType(BBIE_SCVO gBBIESC, Element gNode) throws Exception {

		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao2 = df.getDAO("DTSC");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("DT_SC_ID", gBBIESC.getDTSCID());
		DTSCVO gDTSC = (DTSCVO) dao2.findObject(qc2, conn);
		SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
		QueryCondition qc3_2 = new QueryCondition();
		qc3_2.add("BDT_SC_ID", gDTSC.getDTSCID());
		qc3_2.add("isDefault", 1);
		BDTSCPrimitiveRestrictionVO bBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3_2, conn);
		SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
    	QueryCondition qc5 = new QueryCondition();
    	qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", bBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
		CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5, conn);
		SRTDAO dao6 = df.getDAO("XSDBuiltInType");
		QueryCondition qc6 = new QueryCondition();
		qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
		XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
		Attr aTypeNode = gNode.getOwnerDocument().createAttribute("type");
		aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
		gNode.setAttributeNode(aTypeNode);
		return gNode;

	}
	
	public Element setBBIESCType2(BBIE_SCVO gBBIESC, Element gNode) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao3 = df.getDAO("BDTSCPrimitiveRestriction");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("BDT_SC_Primitive_Restriction_ID", gBBIESC.getDTSCPrimitiveRestrictionID());
		BDTSCPrimitiveRestrictionVO aBDTSCPrimitiveRestriction = (BDTSCPrimitiveRestrictionVO) dao3.findObject(qc3, conn);
		SRTDAO dao5 = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
    	QueryCondition qc5 = new QueryCondition();
		qc5.add("CDT_SC_Allowed_Primitive_Expression_Type_Map_ID", aBDTSCPrimitiveRestriction.getCDTSCAllowedPrimitiveExpressionTypeMapID());
		CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExpressionTypeMap = (CDTSCAllowedPrimitiveExpressionTypeMapVO) dao5.findObject(qc5, conn);
		SRTDAO dao6 = df.getDAO("XSDBuiltInType");
		QueryCondition qc6 = new QueryCondition();
		qc6.add("XSD_BuiltIn_Type_ID", aCDTSCAllowedPrimitiveExpressionTypeMap.getXSDBuiltInTypeID());
		XSDBuiltInTypeVO aXSDBuiltInType = (XSDBuiltInTypeVO) dao6.findObject(qc6, conn);
		Attr aTypeNode = gNode.getOwnerDocument().createAttribute("type");
		aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
		gNode.setAttributeNode(aTypeNode);
		return gNode;

	}
	
	public Element generateSCs(BBIEVO gBBIE, Element gBBIENode, ArrayList<SRTObject> gSCs, Element gSchemaNode) throws DOMException, Exception{
		Element tNode = (Element) gBBIENode;
		while(true){
			if(tNode.getNodeName().equals("xsd:simpleType") || tNode.getNodeName().equals("xsd:complexType"))
				break;
			tNode = (Element) tNode.getParentNode();
		}

		for(int i = 0; i < gSCs.size(); i++) {
			BBIE_SCVO aBBIESC = (BBIE_SCVO)gSCs.get(i);
			
			Element aNode = tNode.getOwnerDocument().createElement("xsd:attribute");
			aNode = handleBBIESCvalue(aBBIESC, aNode); //Generate a DOM Element Node, handle values

			//Get a code list object
			CodeListVO aCL = getCodeList(aBBIESC);
			if(aCL != null) {
				Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
				stIdNode.setNodeValue(Utility.generateGUID()); 
				aNode.setAttributeNode(stIdNode);
			}
			
			AgencyIDListVO aAL = new AgencyIDListVO();

			if(aCL == null) { //aCL = null?
				aAL = getAgencyIDList(aBBIESC);
				
				if(aAL != null) {
					Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
					stIdNode.setNodeValue(aAL.getAgencyIDListGUID());
					aNode.setAttributeNode(stIdNode);
				}

				if(aAL == null) { //aAL = null?
					int primRestriction = aBBIESC.getDTSCPrimitiveRestrictionID(); 
					if(primRestriction == 0) 
						aNode = setBBIESCType(aBBIESC, aNode);
					else 
						aNode = setBBIESCType2(aBBIESC, aNode);
				}
				else { //aAL = null?
					if(!isAgencyListGenerated(aAL))  //isAgencyListGenerated(aAL)?
						generateAgencyList(aAL, aBBIESC, gSchemaNode);
					Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
					aTypeNode.setNodeValue(getAgencyListTypeName(aAL));
					aNode.setAttributeNode(aTypeNode);
				}	
			}
			
			else { //aCL = null?
				if(!isCodeListGenerated(aCL)) {
					DAOFactory df = DAOFactory.getDAOFactory();
					SRTDAO dao = df.getDAO("DTSC");
			    	QueryCondition qc = new QueryCondition();
					qc.add("DT_SC_ID", aBBIESC.getDTSCID());
					DTSCVO aDTSC = (DTSCVO)dao.findObject(qc, conn);
					generateCodeList(aCL, aDTSC, gSchemaNode);
				}
				Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
				aTypeNode.setNodeValue(getCodeListTypeName(aCL));
				aNode.setAttributeNode(aTypeNode);
			}
			if(isCCStored(aNode.getAttribute("id")))
				continue;
			StoredCC.add(aNode.getAttribute("id"));
			tNode.appendChild(aNode);
		}
		return tNode; 
	}
	
	public boolean isAgencyListGenerated(AgencyIDListVO gAL) throws Exception {
		for(int i = 0; i < GUID_array_list.size(); i++) {
			if(gAL.getAgencyIDListGUID().equals(GUID_array_list.get(i)))
				return true;
		}
		return false;
	}

	public String getAgencyListTypeName(AgencyIDListVO gAL) throws Exception {
		String AgencyListTypeName = "clm" + gAL.getAgencyID() + gAL.getListID() + gAL.getVersionID() + "_" + Utility.toCamelCase(gAL.getName()) + "ContentType";
		return AgencyListTypeName;
	}
	
	public Element generateAgencyList(AgencyIDListVO gAL, BBIE_SCVO gSC, Element gSchemaNode) throws DOMException, Exception {
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
		gSchemaNode.appendChild(stNode);
		GUID_array_list.add(gAL.getAgencyIDListGUID());
		return stNode;
	}
	
	public ASBIEPVO receiveASBIEP(int ABIE_ID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
		QueryCondition qc = new QueryCondition();
		qc.add("Role_Of_ABIE_ID", ABIE_ID);
		ASBIEPVO aASBIEPVO = (ASBIEPVO) dao.findObject(qc, conn);
		return aASBIEPVO;
	}
	
	public ArrayList<SRTObject> receiveABIE(ArrayList<Integer> abie_id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		
		ArrayList<SRTObject> aABIEVO = new ArrayList<SRTObject>();
		for(int i = 0; i < abie_id.size() ; i++) {
			QueryCondition qc = new QueryCondition();
			qc.add("ABIE_ID", abie_id.get(i));
			aABIEVO.addAll(dao.findObjects(qc, conn));
		}
		return aABIEVO;
	}
	
	public DTVO queryBDT(BBIEVO gBBIE) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao3 = df.getDAO("BCC");
		QueryCondition qc3 = new QueryCondition();
		qc3.add("BCC_ID", gBBIE.getBasedBCCID());
		BCCVO gBCC = (BCCVO) dao3.findObject(qc3, conn);
		SRTDAO dao = df.getDAO("BCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("BCCP_ID", gBCC.getAssocToBCCPID());
		BCCPVO aBCCPVO = (BCCPVO) dao.findObject(qc, conn);
		SRTDAO dao2 = df.getDAO("DT");
		QueryCondition qc2 = new QueryCondition();
		qc2.add("DT_ID", aBCCPVO.getBDTID());
		DTVO bDT = (DTVO) dao2.findObject(qc2, conn);
		return bDT;
	}
	
	public ASCCPVO queryBasedASCCP(ASBIEPVO gASBIEP) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCCP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASCCP_ID", gASBIEP.getBasedASCCPID());
		ASCCPVO asccpVO = (ASCCPVO) dao.findObject(qc, conn);
		return asccpVO;
	}
	
	public ACCVO queryBasedACC(ABIEVO gABIE) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ACC");
    	QueryCondition qc = new QueryCondition();
		qc.add("ACC_ID", gABIE.getBasedACCID());
		ACCVO gACC = (ACCVO) dao.findObject(qc, conn);
		return gACC;
	}
	
	public ASCCVO queryBasedASCC(ASBIEVO gASBIE) throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASCC");
		QueryCondition qc = new QueryCondition();
		qc.add("ASCC_ID", gASBIE.getBasedASCC());
		ASCCVO gASCC = (ASCCVO) dao.findObject(qc, conn);
		return gASCC;
	}
	
	public ABIEVO queryTargetABIE(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abievo = (ABIEVO)dao.findObject(qc, conn);
		return abievo;
	}
	
	public ACCVO queryTargetACC(ASBIEPVO gASBIEP) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ABIE");
		QueryCondition qc = new QueryCondition();
		qc.add("ABIE_ID", gASBIEP.getRoleOfABIEID());
		ABIEVO abievo = (ABIEVO)dao.findObject(qc, conn);
		
		SRTDAO dao2 = df.getDAO("ACC");
    	QueryCondition qc2 = new QueryCondition();
		qc2.add("ACC_ID", abievo.getBasedACCID());
		ACCVO aACCVO = (ACCVO) dao2.findObject(qc2, conn);
		return aACCVO;
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
			if(aASBIEVO.getCardinalityMax() != 0)
				result.add(aASBIEVO);
		}
		
		for(SRTObject aSRTObject : bbievo){
			BBIEVO aBBIEVO = (BBIEVO)aSRTObject;
			if(aBBIEVO.getCardinalityMax() != 0)
				result.add(aBBIEVO);
		}//sorting 
		return result;
	}
	
	public ASBIEPVO queryAssocToASBIEP(ASBIEVO gASBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("ASBIEP");
    	QueryCondition qc = new QueryCondition();
		qc.add("ASBIEP_ID", gASBIE.getAssocToASBIEPID());
		ASBIEPVO asbiepVO = (ASBIEPVO)dao.findObject(qc, conn);
		return asbiepVO;
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
	
	public ArrayList<SRTObject> queryBBIESCs(BBIEVO gBBIE) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BBIE_SC");
    	QueryCondition qc = new QueryCondition();
    	qc.add("BBIE_ID", gBBIE.getBBIEID());
 		ArrayList<SRTObject> bbiescVO = dao.findObjects(qc, conn);
		return bbiescVO;
	}
	
	public boolean isCodeListGenerated(CodeListVO gCL) throws Exception {
		for(int i = 0; i < GUID_array_list.size(); i++) {
			if(gCL.getCodeListGUID().equals(GUID_array_list.get(i)))
				return true;
		}
		return false;
	}
	
	public boolean isCCStored(String id) throws Exception {
		for(int i = 0; i < StoredCC.size(); i++) {
			if(StoredCC.get(i).equals(id)){
				return true;
			}
		}
		return false;
	}

	public String generateXMLSchema (ArrayList<Integer> abie_id, boolean schema_package_flag) throws Exception {
		Utility.dbSetup();
		DBAgent tx = new DBAgent();
		conn = tx.open();

		ArrayList<SRTObject> gABIE = receiveABIE(abie_id);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element schemaNode = generateSchema(doc);
		String filepath = null;
		if(schema_package_flag == true) {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = receiveASBIEP(aABIEVO.getABIEID());
				System.out.println("Generating Top Level ABIE w/ given ASBIEPVO ID: "+ aASBIEPVO.getASBIEPID());
				doc = generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
			}
			filepath = writeXSDFile(doc, Utility.generateGUID());
		}
		else {
			for(SRTObject aSRTObject : gABIE){
				ABIEVO aABIEVO = (ABIEVO)aSRTObject;
				ASBIEPVO aASBIEPVO = receiveASBIEP(aABIEVO.getABIEID());
				doc = docBuilder.newDocument();
				schemaNode = generateSchema(doc);
				doc = generateTopLevelABIE(aASBIEPVO, doc, schemaNode);
				writeXSDFile(doc, "Package/" + aABIEVO.getAbieGUID());
			}
			filepath = Zip.compression(Utility.generateGUID());
		}
		
		return filepath;
	}
	
	public static void main(String args[]) throws Exception {
		StandaloneXMLSchema aa = new StandaloneXMLSchema();
		abie_ids.add(0);
		aa.generateXMLSchema(abie_ids, true);
		System.out.println("###END###");
	}
}
