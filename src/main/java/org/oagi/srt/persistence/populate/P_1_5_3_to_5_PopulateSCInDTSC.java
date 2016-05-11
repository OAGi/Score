package org.oagi.srt.persistence.populate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.persistence.db.DBAgent;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.startup.SRTInitializer;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
*
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_5_3_to_5_PopulateSCInDTSC {
	
	private static Connection conn = null;
	
	private void populateDTSCforDefaultBDT(XPathHandler xh, XPathHandler xh2, Connection conn) throws SRTDAOException, XPathExpressionException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoDTSC = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 1);
		List<SRTObject> srtObjects = dao.findObjects(qc, conn);
		for(SRTObject srtObject :  srtObjects) {
			DTVO dtVO = (DTVO)srtObject;
			qc = new QueryCondition();
			qc.add("dt_id", dtVO.getBasedDTID());
			DTVO dtVO2 = (DTVO)dao.findObject(qc, conn);

			// default BDT
			if(dtVO2.getDTType() == 0) {
				System.out.println("Popuating SCs for default BDT with type = "+Utility.denToTypeName(dtVO.getDEN()));
				qc = new QueryCondition();
				qc.add("owner_dt_id", dtVO2.getDTID());
				ArrayList<SRTObject> cdtSCList = daoDTSC.findObjects(qc, conn);
				
				NodeList attributeNodeList = xh2.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				if(attributeNodeList==null || attributeNodeList.getLength()<1){
					attributeNodeList = xh.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				}				
				for(int i = 0 ; i < attributeNodeList.getLength(); i++) {
					DTSCVO vo = new DTSCVO();
					int min_cardinality=-1;
					int max_cardinality=-1;
					Element attrElement = (Element)attributeNodeList.item(i);
					String attribute_name = attrElement.getAttribute("name");
					String attribute_id = attrElement.getAttribute("id");
						
					vo.setDTSCGUID(attribute_id);
					vo.setOwnerDTID(dtVO.getDTID());
//					Node propertyTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
//					if(propertyTermNode==null){
//						propertyTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
//					}
//					if(propertyTermNode!=null){
//						vo.setPropertyTerm(propertyTermNode.getTextContent());
//					}
//					
//					Node repTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
//					if(repTermNode==null){
//						repTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
//					}
//					if(repTermNode!=null){
//						vo.setRepresentationTerm(repTermNode.getTextContent());
//					}
//					Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
//					if(defNode==null){
//						defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
//					}
//					if(defNode!=null){
//						vo.setDefinition(defNode.getTextContent());
//					}
					
					if(attrElement.getAttribute("use")==null){
						min_cardinality = 0;
						max_cardinality = 1;
					}
					else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
						min_cardinality = 0;
						max_cardinality = 1;
					}
					else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
						min_cardinality = 1;
						max_cardinality = 1;
					}
					else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
						min_cardinality = 0;
						max_cardinality = 0;
					}
					
					vo.setMinCardinality(min_cardinality);
					vo.setMaxCardinality(max_cardinality);
					
					int baseInd = -1;
					for(int j=0; j<cdtSCList.size(); j++){
						
						DTSCVO baseCDTSC = (DTSCVO) cdtSCList.get(j);
						String basePropertyTerm = baseCDTSC.getPropertyTerm();
						String baseRepresentationTerm = baseCDTSC.getRepresentationTerm();
						String baseStr = basePropertyTerm + " "+baseRepresentationTerm;
						String thisStr = Utility.spaceSeparator(attribute_name);
						
						System.out.println(baseStr+" vs "+thisStr);
						if(baseStr.equals(thisStr)){
							baseInd = j;
							vo.setPropertyTerm(baseCDTSC.getPropertyTerm());
							vo.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
							vo.setDefinition(baseCDTSC.getDefinition());
							vo.setBasedDTSCID(baseCDTSC.getDTSCID());
							break;
						}
					}
					if(baseInd>-1){
						cdtSCList.remove(baseInd);
						System.out.println("This sc has corresponding base!");
						System.out.println(min_cardinality + " " + max_cardinality);
						daoDTSC.insertObject(vo);
					}
					else{
						System.out.println("This SC owned by default BDT is new from Attribute!");
						
						String propertyTerm = "";
						String representationTerm = "";
						String definition = "";
						
						propertyTerm = Utility.spaceSeparator(attribute_name);
						representationTerm = Utility.getRepresentationTerm(attribute_name);
						
						Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
						if(defNode==null){
							defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation");
						}
						if(defNode==null){
							defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
						}
						if(defNode!=null){
							definition = defNode.getTextContent();
						}
						
						vo.setPropertyTerm(propertyTerm);
						vo.setRepresentationTerm(representationTerm);
						vo.setDefinition(definition);					
						daoDTSC.insertObject(vo);
					}
				}

				//Inherit From the remain SCs based on CDT because they don't have corresponding attributes
				//Just copy and get the values from remain cdtSCList
				for(int i=0; i<cdtSCList.size(); i++){
					DTSCVO baseCDTSC = (DTSCVO) cdtSCList.get(i);
					DTSCVO vo = new DTSCVO();

					vo.setOwnerDTID(dtVO.getDTID());
					vo.setDTSCGUID(Utility.generateGUID());
					vo.setPropertyTerm(baseCDTSC.getPropertyTerm());
					vo.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
					vo.setDefinition(baseCDTSC.getDefinition());
					
					//we already know it doesn't have attributes
					//so according to design doc, 
					//min_cardinality = 0, max_cardinality = 0
					vo.setMinCardinality(0);
					vo.setMaxCardinality(0);
					vo.setBasedDTSCID(baseCDTSC.getDTSCID());
					System.out.println("This SC owned by default BDT is inherited from Base!");
					daoDTSC.insertObject(vo);
				}
			}
		}
	}
	
	private void validatePopulateDTSCforDefaultBDT(XPathHandler xh, XPathHandler xh2, Connection conn) throws SRTDAOException, XPathExpressionException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoDTSC = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 1);
		List<SRTObject> srtObjects = dao.findObjects(qc, conn);
		for(SRTObject srtObject :  srtObjects) {
			DTVO dtVO = (DTVO)srtObject;
			qc = new QueryCondition();
			qc.add("dt_id", dtVO.getBasedDTID());
			DTVO dtVO2 = (DTVO)dao.findObject(qc, conn);
			
			
			
			// default BDT
			if(dtVO2.getDTType() == 0) {
				System.out.println("Popuating SCs for default BDT with type = "+Utility.denToTypeName(dtVO.getDEN()));
				//Inherit from based CDT
				ArrayList<String> fromXSDwAttrs =new ArrayList<String>();
				ArrayList<String> fromDBwAttrs= new ArrayList<String>();
				
				//Let's check BDT has attribute or not
				NodeList attributeNodeList = xh2.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				if(attributeNodeList==null){
					attributeNodeList = xh.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				}
				if(attributeNodeList!=null && attributeNodeList.getLength() > 0){//Get it from Attributes
					
					for(int i=0; i<attributeNodeList.getLength(); i++){
						String aStrFromXSDwAttr  = "";
						
						Element attrNode = (Element) attributeNodeList.item(i);
						aStrFromXSDwAttr = aStrFromXSDwAttr+attrNode.getAttribute("id");
						String attrPropertyTerm = "";
						String attrRepTerm = "";
						String attrDefinition = "";
						
						Node propertyTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
						if(propertyTermNode==null){
							propertyTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
						}
						if(propertyTermNode!=null){
							attrPropertyTerm = propertyTermNode.getTextContent();
						}
						else {
							attrPropertyTerm = "null";
						}
						
						Node repTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
						if(repTermNode==null){
							repTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
						}
						if(repTermNode!=null){
							attrRepTerm = repTermNode.getTextContent();
						}
						else {
							attrRepTerm = "null";
						}
						
						Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_Definition");
						if(defNode==null){
							defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attrNode.getAttribute("id")+"']/xsd:annotation/xsd:documentation/ccts_Definition");
						}
						if(defNode!=null){
							attrDefinition = defNode.getTextContent();
						}
						else {
							attrDefinition = "null";
						}
						aStrFromXSDwAttr = aStrFromXSDwAttr+attrPropertyTerm;
						aStrFromXSDwAttr = aStrFromXSDwAttr+attrRepTerm;
						aStrFromXSDwAttr = aStrFromXSDwAttr+attrDefinition;
						
						String minCar="";
						String maxCar="";
						if(attrNode.getAttribute("use")!=null){
							if(attrNode.getAttribute("use").equalsIgnoreCase("required")){
								minCar = "1";
								maxCar = "1";
							}
							else if (attrNode.getAttribute("use").equalsIgnoreCase("optional")){
								minCar = "0";
								maxCar = "1";
							}
							else if (attrNode.getAttribute("use").equalsIgnoreCase("prohibited")){
								minCar = "0";
								maxCar = "0";
							}
						}
						else {
							minCar = "0";
							maxCar = "1";
						}
						aStrFromXSDwAttr = aStrFromXSDwAttr+minCar;
						aStrFromXSDwAttr = aStrFromXSDwAttr+maxCar;
						
						
						String fromDBStr = "";
						QueryCondition qc4DTSC = new QueryCondition();
						qc4DTSC.add("guid", attrNode.getAttribute("id"));
						DTSCVO adtsc  = (DTSCVO) daoDTSC.findObject(qc4DTSC);
						
						if(adtsc==null){
							System.out.println("@@@@ DTSC from Attributes is not imported! Check DTSC (guid="+attrNode.getAttribute("id")+")");
						}
						else{
							fromDBStr = fromDBStr + adtsc.getDTSCGUID();
							fromDBStr = fromDBStr + adtsc.getPropertyTerm();
							fromDBStr = fromDBStr + adtsc.getRepresentationTerm();
							fromDBStr = fromDBStr + adtsc.getDefinition();
							fromDBStr = fromDBStr + adtsc.getMinCardinality();
							fromDBStr = fromDBStr + adtsc.getMaxCardinality();
						}
						
						if(!fromDBStr.equals(aStrFromXSDwAttr)){
							System.out.println("@@@@ DTSC from Attributes has different values! Check DTSC (guid="+attrNode.getAttribute("id")+")");
							System.out.println("     FromXSD: "+aStrFromXSDwAttr);
							System.out.println("      FromDB: "+fromDBStr);
						}
					}
				}
				else {//Copy the CDT's SC
					//Check two are same
				}
				
				ArrayList<String> cdtSCs = new ArrayList<String>();
				ArrayList<String> bdtSCs = new ArrayList<String>();
				
				qc = new QueryCondition();
				qc.add("owner_dt_id", dtVO2.getDTID());
				ArrayList<SRTObject> cdtSCList = daoDTSC.findObjects(qc, conn);
				ArrayList<String> CDTSCPropertyTermList = new ArrayList<String>();
				for(SRTObject dt : cdtSCList) {
					DTSCVO cdtscvo = (DTSCVO) dt;
					DTSCVO vo = new DTSCVO();
					System.out.println("Inheriting from based CDTs from "+cdtscvo.getPropertyTerm());
					
					String cdtsc = "";
					cdtsc = cdtsc + cdtscvo.getPropertyTerm();
					cdtsc = cdtsc + cdtscvo.getRepresentationTerm();
					cdtsc = cdtsc + cdtscvo.getDefinition();
					cdtsc = cdtsc + cdtscvo.getMinCardinality();
					cdtsc = cdtsc + cdtscvo.getMaxCardinality();
					
					
					vo.setBasedDTSCID(cdtscvo.getDTSCID());
					vo.setPropertyTerm(cdtscvo.getPropertyTerm());
					vo.setRepresentationTerm(cdtscvo.getRepresentationTerm());
					vo.setDefinition(cdtscvo.getDefinition());
					vo.setOwnerDTID(dtVO.getDTID());
					vo.setBasedDTSCID(cdtscvo.getDTSCID());
					CDTSCPropertyTermList.add(cdtscvo.getPropertyTerm().replaceAll(" ", "").toLowerCase());
					int min_cardinality = 0, max_cardinality = 0;

				
					boolean checkOfAttr = false;
					for(int i = 0 ; i < attributeNodeList.getLength(); i++) {
						Element attrElement = (Element)attributeNodeList.item(i);
						String attribute_name = attrElement.getAttribute("name");
						if(attribute_name.toLowerCase().startsWith(cdtscvo.getPropertyTerm().replaceAll(" ", "").toLowerCase())){
							if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
								min_cardinality = 0;
							else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
								min_cardinality = 1;
							vo.setMinCardinality(min_cardinality);
							
							if(attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("required"))
								max_cardinality = 1;
							else if(attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
								max_cardinality = 0;
							vo.setMaxCardinality(max_cardinality);
							vo.setDTSCGUID(attrElement.getAttribute("id"));
							System.out.println("There is corresponing xsd:attribute");
							daoDTSC.insertObject(vo);
							checkOfAttr = true;
							break;
						}
					}

					if(!checkOfAttr){
						vo.setMinCardinality(min_cardinality);
						vo.setMaxCardinality(max_cardinality);
						vo.setDTSCGUID(Utility.generateGUID());
						System.out.println("There is no corresponing xsd:attribute for SC whose property term = "+ vo.getPropertyTerm());
						daoDTSC.insertObject(vo);
					}
				}
			}
		}
	}
	
	public void populateDTSCforUnqualifiedBDT(XPathHandler xh, XPathHandler xh2, Connection conn, boolean is_fields_xsd) throws SRTDAOException, XPathExpressionException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoDTSC = df.getDAO("DTSC");
		//SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 1);
		List<SRTObject> srtObjects = new ArrayList<SRTObject>();
		if(is_fields_xsd){
			srtObjects = dao.findObjects(qc, conn);
		} 
		else {
			String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"} ;
			for(int k = 0 ; k < metalist.length; k++){
				qc = new QueryCondition();
				qc.add("type", 1);
				qc.add("den", Utility.typeToDen(metalist[k]));
				srtObjects.add(k, dao.findObject(qc, conn));
			}
		}
		
		for(SRTObject srtObject :  srtObjects) {
			DTVO dtVO = (DTVO)srtObject;
			qc = new QueryCondition();
			qc.add("dt_id", dtVO.getBasedDTID());
			DTVO dtVO2 = (DTVO)dao.findObject(qc, conn);
			// unqualified BDT
			if(dtVO2.getDTType() != 0) {
				String denType = Utility.DenToName(dtVO.getDEN());
				System.out.println("Popuating SCs for unqualified bdt with type = "+denType);
				Node extensionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
				if(extensionNode == null)
					extensionNode = xh2.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
				String base = ((Element)extensionNode).getAttribute("base");
				Node baseNode = xh2.getNode("//xsd:complexType[@name = '" + base + "']");
				if(baseNode == null)
					baseNode = xh.getNode("//xsd:complexType[@name = '" + base + "']");
				if(baseNode == null) 
					baseNode = xh2.getNode("//xsd:simpleType[@name = '" + base + "']");
				if(baseNode == null) 
					baseNode = xh.getNode("//xsd:simpleType[@name = '" + base + "']");
				
				qc = new QueryCondition();
				qc.add("guid", ((Element)baseNode).getAttribute("id"));
				DTVO basedDtVO = (DTVO)dao.findObject(qc, conn);
				int based_dt_id = basedDtVO.getDTID();
				qc = new QueryCondition();
				qc.add("owner_dt_id", based_dt_id);
				List<SRTObject> baseDefaultDTSCs = daoDTSC.findObjects(qc, conn);
				
				//adding additional SCs for attributes
				NodeList attributeList = xh2.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				if(attributeList==null){
					attributeList = xh.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				}
				int min_cardinality = 0;
				int max_cardinality = 1;
				
				for(int i = 0; i < attributeList.getLength(); i++) {
					Element attrElement = (Element)attributeList.item(i);
					String attribute_name = attrElement.getAttribute("name");
					String attribute_id = attrElement.getAttribute("id");
					
					DTSCVO vo = new DTSCVO();
					vo.setOwnerDTID(dtVO.getDTID());
					vo.setDTSCGUID(attribute_id);
					if(attrElement.getAttribute("use")==null){
						min_cardinality=0;
						max_cardinality=1;
					}
					else if(attrElement.getAttribute("use").equalsIgnoreCase("optional")){
						min_cardinality=0;
						max_cardinality=1;
					}
					else if(attrElement.getAttribute("use").equalsIgnoreCase("required")){
						min_cardinality=1;
						max_cardinality=1;
					}
					else if(attrElement.getAttribute("use").equalsIgnoreCase("prohibited")){
						min_cardinality=0;
						max_cardinality=0;
					}
					
					vo.setMinCardinality(min_cardinality);
					vo.setMaxCardinality(max_cardinality);
					
					
					int baseInd = -1;
					
					for(int j=0; j<baseDefaultDTSCs.size(); j++){
						DTSCVO baseDefaultBDTSC = (DTSCVO)baseDefaultDTSCs.get(j);
						String basePropertyTerm = baseDefaultBDTSC.getPropertyTerm();
						String baseRepresentationTerm = baseDefaultBDTSC.getRepresentationTerm();
						String baseStr = basePropertyTerm + " "+baseRepresentationTerm;
						String thisStr = Utility.spaceSeparator(attribute_name);
						
						System.out.println(baseStr+" vs "+thisStr);
						if(baseStr.equals(thisStr)){
							baseInd = j;
							vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
							vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
							vo.setDefinition(baseDefaultBDTSC.getDefinition());
							vo.setBasedDTSCID(baseDefaultBDTSC.getDTSCID());
							break;
						}
					}
					
					if(baseInd>-1){
						baseDefaultDTSCs.remove(baseInd);
						System.out.println("This SC owned by unqualified BDT has corresponding base!");
						daoDTSC.insertObject(vo);
					}
					else{
						System.out.println("This SC owned by unqualified BDT is new from Attribute!");
						
						String propertyTerm = "";
						String representationTerm = "";
						String definition = "";
						
						propertyTerm = Utility.spaceSeparator(attribute_name);
						representationTerm = Utility.getRepresentationTerm(attribute_name);
						
						Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
						if(defNode==null){
							defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation");
						}
						if(defNode!=null){
							definition = defNode.getTextContent();
						}
						
						vo.setPropertyTerm(propertyTerm);
						vo.setRepresentationTerm(representationTerm);
						vo.setDefinition(definition);					
						daoDTSC.insertObject(vo);
						
						//if it has new attribute extension, 
						//it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map
						//And, there is only one case for this: "NameType"						
						DTSCVO insertedSCVO = new DTSCVO();
						QueryCondition qc4SC = new QueryCondition();
						qc4SC.add("guid", vo.getDTSCGUID());
						insertedSCVO = (DTSCVO) daoDTSC.findObject(qc4SC);
						
						ArrayList<SRTObject> DTVOwDataTypeTerm = new ArrayList<SRTObject>();
						QueryCondition qc4DTwDataTypeTerm = new QueryCondition();
						qc4DTwDataTypeTerm.add("data_type_term", vo.getRepresentationTerm());
						DTVOwDataTypeTerm = dao.findObjects(qc4DTwDataTypeTerm);
						
						for(int j=0; j<DTVOwDataTypeTerm.size(); j++){						
							DTVO aDTVO = (DTVO) DTVOwDataTypeTerm.get(j);
							ArrayList<SRTObject> CDTAwdPriVOs = new ArrayList<SRTObject>();
							QueryCondition qc4CDTAwdPris = new QueryCondition();
							qc4CDTAwdPris.add("CDT_ID", aDTVO.getDTID());
							CDTAwdPriVOs = aCDTAllowedPrimitiveDAO.findObjects(qc4CDTAwdPris);
							
							if(CDTAwdPriVOs.size() > 0 && !CDTAwdPriVOs.isEmpty()){
								for(int k=0;k<CDTAwdPriVOs.size(); k++){
									CDTSCAllowedPrimitiveVO cdtSCAP = new CDTSCAllowedPrimitiveVO();
									CDTAllowedPrimitiveVO cdtAP = (CDTAllowedPrimitiveVO) CDTAwdPriVOs.get(k);
									cdtSCAP.setCDTSCID(insertedSCVO.getDTSCID());
									cdtSCAP.setCDTPrimitiveID(cdtAP.getCDTPrimitiveID());
									cdtSCAP.setisDefault(cdtAP.getisDefault());
									aCDTSCAllowedPrimitiveDAO.insertObject(cdtSCAP);		

									CDTSCAllowedPrimitiveVO insertedCDTSCAP = new CDTSCAllowedPrimitiveVO();
									QueryCondition qc4SCAP = new QueryCondition();
									qc4SCAP.add("CDT_SC_ID", cdtSCAP.getCDTSCID());
									qc4SCAP.add("CDT_Pri_id", cdtSCAP.getCDTPrimitiveID());
									insertedCDTSCAP = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc4SCAP);
									
									ArrayList<CDTSCAllowedPrimitiveExpressionTypeMapVO> CDTSCAPXTMapVO = new ArrayList<CDTSCAllowedPrimitiveExpressionTypeMapVO>();
									QueryCondition qc4XTMap = new QueryCondition();
									qc4XTMap.add("CDT_awd_pri_id", cdtAP.getCDTAllowedPrimitiveID());
									ArrayList<SRTObject> cdtAPXTMs = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4XTMap);
									for(int m=0; m<cdtAPXTMs.size(); m++){
										CDTAllowedPrimitiveExpressionTypeMapVO thisAPXTmap= (CDTAllowedPrimitiveExpressionTypeMapVO) cdtAPXTMs.get(m);
										CDTSCAllowedPrimitiveExpressionTypeMapVO tmp = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
										tmp.setXSDBuiltInTypeID(thisAPXTmap.getXSDBuiltInTypeID());
										tmp.setCDTSCAllowedPrimitive(insertedCDTSCAP.getCDTSCAllowedPrimitiveID());
										
									}
								}
								break;
							}
						}
					}
				}

				//Inherit From the remain SCs based on default bdt because they don't have corresponding attributes
				//Just copy and get the values from remain baseDefaultDTSCs
				for(int i=0; i<baseDefaultDTSCs.size(); i++){
					DTSCVO baseDefaultBDTSC = (DTSCVO) baseDefaultDTSCs.get(i);
					DTSCVO vo = new DTSCVO();
					vo.setOwnerDTID(dtVO.getDTID());
					vo.setDTSCGUID(Utility.generateGUID());
					vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
					vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
					vo.setDefinition(baseDefaultBDTSC.getDefinition());
					
					//we already know it doesn't have attributes
					//so according to design doc, 
					//min_cardinality = 0, max_cardinality = 0
					vo.setMinCardinality(0);
					vo.setMaxCardinality(0);
					vo.setBasedDTSCID(baseDefaultBDTSC.getDTSCID());
					System.out.println("This SC owned by unqualified BDT is inherited from Base!");
					daoDTSC.insertObject(vo);
				}
			}
		}
	}
	
	public void validatePopulateDTSCforUnqualifiedBDT(XPathHandler xh, XPathHandler xh2, Connection conn, boolean is_fields_xsd) throws SRTDAOException, XPathExpressionException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoDTSC = df.getDAO("DTSC");
		//SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 1);
		List<SRTObject> srtObjects = new ArrayList<SRTObject>();
		if(is_fields_xsd){
			srtObjects = dao.findObjects(qc, conn);
		} 
		else {
			String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"} ;
			for(int k = 0 ; k < metalist.length; k++){
				qc = new QueryCondition();
				qc.add("type", 1);
				qc.add("den", Utility.typeToDen(metalist[k]));
				srtObjects.add(k, dao.findObject(qc, conn));
			}
		}
		
		for(SRTObject srtObject :  srtObjects) {
			DTVO dtVO = (DTVO)srtObject;
			qc = new QueryCondition();
			qc.add("dt_id", dtVO.getBasedDTID());
			DTVO dtVO2 = (DTVO)dao.findObject(qc, conn);
			// unqualified BDT
			if(dtVO2.getDTType() != 0) {
				//inheritance
				String denType = Utility.DenToName(dtVO.getDEN());
				System.out.println("Popuating SCs for unqualified bdt with type = "+denType);
				Node extensionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
				if(extensionNode == null)
					extensionNode = xh2.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
				String base = ((Element)extensionNode).getAttribute("base");
				Node baseNode = xh2.getNode("//xsd:complexType[@name = '" + base + "']");
				if(baseNode == null)
					baseNode = xh.getNode("//xsd:complexType[@name = '" + base + "']");
				if(baseNode == null) 
					baseNode = xh2.getNode("//xsd:simpleType[@name = '" + base + "']");
				if(baseNode == null) 
					baseNode = xh.getNode("//xsd:simpleType[@name = '" + base + "']");
				
				qc = new QueryCondition();
				qc.add("guid", ((Element)baseNode).getAttribute("id"));
				DTVO basedDtVO = (DTVO)dao.findObject(qc, conn);
				int based_dt_id = basedDtVO.getDTID();
				qc = new QueryCondition();
				qc.add("owner_dt_id", based_dt_id);
				List<SRTObject> dtscVOs = daoDTSC.findObjects(qc, conn);
				for(SRTObject dtscObject: dtscVOs) { // copy all SCs from its based default BDT  
					DTSCVO dtscVO = (DTSCVO)dtscObject;
					DTSCVO vo = new DTSCVO();
					System.out.println("Inheriting from based default BDTs from "+dtscVO.getPropertyTerm());
					vo.setDTSCGUID(Utility.generateGUID());  // set new GUID
					vo.setPropertyTerm(dtscVO.getPropertyTerm());
					vo.setRepresentationTerm(dtscVO.getRepresentationTerm());
					vo.setDefinition((dtscVO.getDefinition()));
					vo.setOwnerDTID(dtVO.getDTID());
					vo.setMinCardinality(dtscVO.getMinCardinality());
					vo.setMaxCardinality(dtscVO.getMaxCardinality());
					vo.setBasedDTSCID(dtscVO.getDTSCID());
					daoDTSC.insertObject(vo);
				}
				
				
				//adding additional SCs for attributes
				NodeList attributeList = xh2.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				if(attributeList==null){
					attributeList = xh.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				}
				int min_cardinality = 0;
				int max_cardinality = 1;
				for(int i = 0; i < attributeList.getLength(); i++) {
					Node attribute = attributeList.item(i);
					Element attrElement = (Element)attribute;
					System.out.println("Populating SCs from attributes in unqualified BDTs.. attribute name = "+attrElement.getAttribute("name"));
					DTSCVO vo = new DTSCVO();
					vo.setDTSCGUID(attrElement.getAttribute("id"));
					
					String attribute_name = attrElement.getAttribute("name");
					Node definitionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@name = '" + attribute_name + "']/xsd:annotation/xsd:documentation");
					String definition = definitionNode == null? "": definitionNode.getTextContent();
					vo.setPropertyTerm(Utility.spaceSeparator(attribute_name));
					vo.setRepresentationTerm(Utility.getRepresentationTerm(attribute_name));
					vo.setDefinition(definition);
					vo.setOwnerDTID(dtVO.getDTID());
					
					if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
						min_cardinality = 0;
					else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
						min_cardinality = 1;
					vo.setMinCardinality(min_cardinality);
					
					if(attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("required"))
						max_cardinality = 1;
					else if(attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
						min_cardinality = 0;
					vo.setMaxCardinality(max_cardinality);
					daoDTSC.insertObject(vo);
					if(attrElement.getAttribute("type").endsWith("CodeContentType")){
						
						QueryCondition qc_04 = new QueryCondition();
						qc_04.add("Data_Type_Term", vo.getRepresentationTerm());
						qc_04.add("Type", 0);
						DTVO cdtVO = (DTVO) aDTDAO.findObject(qc_04, conn);
						QueryCondition qc_05 = new QueryCondition();
						qc_05.add("CDT_ID", cdtVO.getDTID()); 
						qc_05.add("is_default", 1);
						ArrayList<SRTObject> cdtawdprilist = aCDTAllowedPrimitiveDAO.findObjects(qc_05);
						for(SRTObject acdtawdprilist : cdtawdprilist) {
							CDTAllowedPrimitiveVO aCDTAPVO = (CDTAllowedPrimitiveVO) acdtawdprilist;
							if(aCDTAPVO.getCDTPrimitiveID() != 0){
								CDTSCAllowedPrimitiveVO aVO = new CDTSCAllowedPrimitiveVO();
								QueryCondition qc_055 = new QueryCondition();
								qc_055.add("guid", vo.getDTSCGUID());
								int cdt_sc_id = ((DTSCVO)daoDTSC.findObject(qc_055, conn)).getDTSCID();
								
								aVO.setCDTSCID(cdt_sc_id);
								aVO.setCDTPrimitiveID(aCDTAPVO.getCDTPrimitiveID());
								aVO.setisDefault(aCDTAPVO.getisDefault());
								aCDTSCAllowedPrimitiveDAO.insertObject(aVO);
								QueryCondition qc_051 = new QueryCondition();
								qc_051.add("CDT_SC_ID", aVO.getCDTSCID());
								qc_051.add("cdt_pri_id", aVO.getCDTPrimitiveID());
								CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc_051);
								int cdtscallowedprimitiveid = aCDTSCAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID();

								String xsd_builtin_type = "xsd:token";
								CDTSCAllowedPrimitiveExpressionTypeMapVO bVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
								bVO.setCDTSCAllowedPrimitive(cdtscallowedprimitiveid);
								QueryCondition qc_07 = new QueryCondition();
								qc_07.add("BuiltIn_Type", xsd_builtin_type);
								XSDBuiltInTypeVO cVO = (XSDBuiltInTypeVO) aXBTDAO.findObject(qc_07);
								bVO.setXSDBuiltInTypeID(cVO.getXSDBuiltInTypeID());
								System.out.println("Inserting CDT SC Allowed Primitive Expresssion Typemap for new SC whose property term = "+vo.getPropertyTerm()+", expression type map = "+xsd_builtin_type);
								aCDTSCAllowedPrimitiveExpressionTypeMapDAO.insertObject(bVO);
							}
						}
					}
					else {
						QueryCondition qc_04 = new QueryCondition();
						qc_04.add("Data_Type_Term", vo.getRepresentationTerm());
						qc_04.add("Type", 0);
						DTVO cdtVO = (DTVO) aDTDAO.findObject(qc_04, conn);
						QueryCondition qc_05 = new QueryCondition();
						qc_05.add("CDT_ID", cdtVO.getDTID()); 
						ArrayList<SRTObject> cdtawdprilist = aCDTAllowedPrimitiveDAO.findObjects(qc_05);
						for(SRTObject acdtawdprilist : cdtawdprilist) {
							CDTAllowedPrimitiveVO aCDTAPVO = (CDTAllowedPrimitiveVO) acdtawdprilist;
							if(aCDTAPVO.getCDTPrimitiveID() != 0){
								CDTSCAllowedPrimitiveVO aVO = new CDTSCAllowedPrimitiveVO();
								QueryCondition qc_055 = new QueryCondition();
								qc_055.add("guid", vo.getDTSCGUID());
								int cdt_sc_id = ((DTSCVO)daoDTSC.findObject(qc_055, conn)).getDTSCID();
								
								aVO.setCDTSCID(cdt_sc_id);
								aVO.setCDTPrimitiveID(aCDTAPVO.getCDTPrimitiveID());
								aVO.setisDefault(aCDTAPVO.getisDefault());
								aCDTSCAllowedPrimitiveDAO.insertObject(aVO);
								
								QueryCondition qc_051 = new QueryCondition();
								qc_051.add("CDT_SC_ID", aVO.getCDTSCID());
								qc_051.add("cdt_pri_id", aVO.getCDTPrimitiveID());
								CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc_051);
								int cdtscallowedprimitiveid = aCDTSCAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID();
								
								QueryCondition qc_06 = new QueryCondition();
								qc_06.add("cdt_pri_id", aVO.getCDTPrimitiveID());
								CDTPrimitiveVO aCDTPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitiveDAO.findObject(qc_06);
								String cdt_primitive_name = aCDTPrimitiveVO.getName();
								ArrayList<String> xsd_builtin_type = new ArrayList<String>();
	
								if(cdt_primitive_name.equals("Binary")){
									xsd_builtin_type.add("xsd:base64Binary");
									xsd_builtin_type.add("xsd:hexBinary");
								}
								else if(cdt_primitive_name.equals("Boolean")){
									xsd_builtin_type.add("xsd:Boolean");
								}
								else if(cdt_primitive_name.equals("Decimal")){
									xsd_builtin_type.add("xsd:decimal");
								}
								else if(cdt_primitive_name.equals("Double")){
									xsd_builtin_type.add("xsd:double");
									xsd_builtin_type.add("xsd:float");
								}
								else if(cdt_primitive_name.equals("Float")){
									xsd_builtin_type.add("xsd:float");
								}
								else if(cdt_primitive_name.equals("Integer")){
									xsd_builtin_type.add("xsd:integer");
									xsd_builtin_type.add("xsd:nonNegativeInteger");
									xsd_builtin_type.add("xsd:positiveInteger");
								}
								else if(cdt_primitive_name.equals("NormalizedString")){
									xsd_builtin_type.add("xsd:normalizedString");
								}
								else if(cdt_primitive_name.equals("String")){
									xsd_builtin_type.add("xsd:string");
								}
								else if(cdt_primitive_name.equals("TimeDuration")){
									xsd_builtin_type.add("xsd:token");
									xsd_builtin_type.add("xsd:duration");
								}
								else if(cdt_primitive_name.equals("TimePoint")){
									xsd_builtin_type.add("xsd:token");
									xsd_builtin_type.add("xsd:dateTime");
									xsd_builtin_type.add("xsd:date");
									xsd_builtin_type.add("xsd:time");
									xsd_builtin_type.add("xsd:gYearMonth");
									xsd_builtin_type.add("xsd:gYear");
									xsd_builtin_type.add("xsd:gMonthDay");
									xsd_builtin_type.add("xsd:gDay");
									xsd_builtin_type.add("xsd:gMonth");
								}
								else if(cdt_primitive_name.equals("Token")){
									xsd_builtin_type.add("xsd:token");
								}
									
								for(int l = 0 ; l < xsd_builtin_type.size(); l++){
									CDTSCAllowedPrimitiveExpressionTypeMapVO bVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
									bVO.setCDTSCAllowedPrimitive(cdtscallowedprimitiveid);
									QueryCondition qc_07 = new QueryCondition();
									qc_07.add("BuiltIn_Type", xsd_builtin_type.get(l));
									XSDBuiltInTypeVO cVO = (XSDBuiltInTypeVO) aXBTDAO.findObject(qc_07);
									bVO.setXSDBuiltInTypeID(cVO.getXSDBuiltInTypeID());
									System.out.println("Inserting CDT SC Allowed Primitive Expresssion Typemap for new SC whose property term = "+vo.getPropertyTerm()+", expression type map = "+xsd_builtin_type.get(l));
									aCDTSCAllowedPrimitiveExpressionTypeMapDAO.insertObject(bVO);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void populateDTSC() throws SRTDAOException, XPathExpressionException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		populateDTSCforDefaultBDT(businessDataType_xsd, fields_xsd, conn);
		populateDTSCforUnqualifiedBDT(businessDataType_xsd, fields_xsd, conn, true);
	}
	
	public void run() throws Exception {
		System.out.println("### 1.5.3-5 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		populateDTSC();
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.3-5 End");
		
	}
	
	public void validate_P_1_5_3_to_5_PopulateSCInDTSC() throws Exception {
		System.out.println("### 1.5.3-5 Start Validation");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		validatePopulateDTSCforDefaultBDT(businessDataType_xsd, fields_xsd, conn);
		validatePopulateDTSCforUnqualifiedBDT(businessDataType_xsd, fields_xsd, conn, true);
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.3-5 Validation End");
		
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		P_1_5_3_to_5_PopulateSCInDTSC p = new P_1_5_3_to_5_PopulateSCInDTSC();
		p.run();
	}
}
