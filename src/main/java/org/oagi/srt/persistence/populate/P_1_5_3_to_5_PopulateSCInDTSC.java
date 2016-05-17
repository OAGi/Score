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
						System.out.println("~~~ "+vo.getPropertyTerm()+" "+vo.getRepresentationTerm()+". This sc has corresponding base!");
						daoDTSC.insertObject(vo);
					}
					else{
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
						System.out.println("~~~ "+vo.getPropertyTerm()+" "+vo.getRepresentationTerm()+". This SC owned by default BDT is new from Attribute!");
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
					System.out.println("~~~ "+baseCDTSC.getPropertyTerm()+" "+baseCDTSC.getRepresentationTerm()+". This SC owned by default BDT is inherited from Base!");
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
				System.out.println("Validating SCs for default BDT with type = "+Utility.denToTypeName(dtVO.getDEN()));
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
				//Copy the CDT's SC
				//Check BDT SC >= CDT SC (it could be if it's from attr)
					
				QueryCondition qc4CDTSC = new QueryCondition();
				qc4CDTSC.add("owner_dt_id", dtVO.getBasedDTID());
				ArrayList<SRTObject> CDTSCs = daoDTSC.findObjects(qc4CDTSC);
				
				QueryCondition qc4BDTSC = new QueryCondition();
				qc4BDTSC.add("owner_dt_id", dtVO.getDTID());
				ArrayList<SRTObject> BDTSCs = daoDTSC.findObjects(qc4BDTSC);
					
				for(int i=CDTSCs.size()-1; i>-1; i--){
					DTSCVO cdtscVO = (DTSCVO) CDTSCs.get(i);
					System.out.print("    Default SC:"+cdtscVO.getPropertyTerm() +" "+ cdtscVO.getRepresentationTerm());
					for(int j=0; j<BDTSCs.size(); j++){
						
						DTSCVO bdtscVO = (DTSCVO) BDTSCs.get(j);

						if(cdtscVO.getPropertyTerm().equals(bdtscVO.getPropertyTerm())
						&& cdtscVO.getRepresentationTerm().equals(bdtscVO.getRepresentationTerm())
						&& cdtscVO.getDefinition().equals(bdtscVO.getDefinition())
						&& cdtscVO.getDTSCID()==bdtscVO.getBasedDTSCID()){
							System.out.print(" has corresponding Attr= "+cdtscVO.getPropertyTerm() +" "+ cdtscVO.getRepresentationTerm());
							Node attrCheckNode = xh2.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id = '" + bdtscVO.getDTSCGUID() + "']");
							if(attrCheckNode==null){
								attrCheckNode = xh.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id = '" + bdtscVO.getDTSCGUID() + "']");
							}
							
							if(attrCheckNode!=null){
								Element ele = (Element) attrCheckNode;
								if(ele.getAttribute("use")==null){
									if(bdtscVO.getMinCardinality()==0 && bdtscVO.getMaxCardinality() == 1){
										CDTSCs.remove(i);
										break;
									}
								}
								else if(ele.getAttribute("use").equalsIgnoreCase("optional")) {
									if(bdtscVO.getMinCardinality()==0 && bdtscVO.getMaxCardinality() == 1){
										CDTSCs.remove(i);
										break;
									}
								}
								else if(ele.getAttribute("use").equalsIgnoreCase("required")) {
									if(bdtscVO.getMinCardinality()==1 && bdtscVO.getMaxCardinality() == 1){
										CDTSCs.remove(i);
										break;
									}
								}
								else if(ele.getAttribute("use").equalsIgnoreCase("prohibited")) {
									if(bdtscVO.getMinCardinality()==0 && bdtscVO.getMaxCardinality() == 0){
										CDTSCs.remove(i);
										break;
									}
								}
							}
							else {
								if(bdtscVO.getMinCardinality()==0 && bdtscVO.getMaxCardinality() == 0){
									CDTSCs.remove(i);
									break;
								}
							}
						}
					}
					System.out.println("");
					
				}
				
				for(int i=CDTSCs.size()-1; i>-1; i--){
					DTSCVO cdtscVO = (DTSCVO) CDTSCs.get(i);
					System.out.println("@@@@ "+cdtscVO.getPropertyTerm() + " " + cdtscVO.getRepresentationTerm() + " is not imported! Check Default BDT: "+dtVO.getDTGUID());
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
						System.out.println("~~~"+vo.getPropertyTerm()+" "+vo.getRepresentationTerm()+". This SC owned by unqualified BDT has corresponding base!");
						daoDTSC.insertObject(vo);
					}
					else{
						
						String propertyTerm = "";
						String representationTerm = "";
						String definition = "";
						
						propertyTerm = Utility.spaceSeparator(attribute_name);
						if(!is_fields_xsd){
							propertyTerm = Utility.spaceSeparatorBeforeStr(attribute_name, "Code");
						}
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
						System.out.println("~~~"+vo.getPropertyTerm()+" "+vo.getRepresentationTerm()+". This SC owned by unqualified BDT is new from Attribute!");

						daoDTSC.insertObject(vo);
						
						//if it has new attribute extension, 
						//it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map					
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
									
									QueryCondition qc4pri = new QueryCondition();
									qc4pri.add("cdt_pri_id", cdtAP.getCDTPrimitiveID());
									CDTPrimitiveVO tmpPri = (CDTPrimitiveVO) aCDTPrimitiveDAO.findObject(qc4pri);

									
									String expressionLanguageOrActionCode = "";
									expressionLanguageOrActionCode= insertedSCVO.getPropertyTerm();
									
									System.out.print("   ~~~"+insertedSCVO.getPropertyTerm()+" "+insertedSCVO.getRepresentationTerm()+" is "+tmpPri.getName());
									if(cdtSCAP.getisDefault()){
										System.out.println(" and it's Default!");
									}
									else{
										System.out.println("");
									}
									
									aCDTSCAllowedPrimitiveDAO.insertObject(cdtSCAP);		
										
									CDTSCAllowedPrimitiveVO insertedCDTSCAP = new CDTSCAllowedPrimitiveVO();
									QueryCondition qc4SCAP = new QueryCondition();
									qc4SCAP.add("CDT_SC_ID", cdtSCAP.getCDTSCID());
									qc4SCAP.add("CDT_Pri_id", cdtSCAP.getCDTPrimitiveID());
									insertedCDTSCAP = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc4SCAP);
									
									QueryCondition qc4XTMap = new QueryCondition();
									qc4XTMap.add("CDT_awd_pri_id", cdtAP.getCDTAllowedPrimitiveID());
									ArrayList<SRTObject> cdtAPXTMs = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4XTMap);
									for(int m=0; m<cdtAPXTMs.size(); m++){
										CDTAllowedPrimitiveExpressionTypeMapVO thisAPXTmap= (CDTAllowedPrimitiveExpressionTypeMapVO) cdtAPXTMs.get(m);
										CDTSCAllowedPrimitiveExpressionTypeMapVO tmp = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
										tmp.setXSDBuiltInTypeID(thisAPXTmap.getXSDBuiltInTypeID());
										tmp.setCDTSCAllowedPrimitive(insertedCDTSCAP.getCDTSCAllowedPrimitiveID());
										aCDTSCAllowedPrimitiveExpressionTypeMapDAO.insertObject(tmp);
									}
								}
								break;//if this is hit, that means dt sc is mapped to cdt sc
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
					//inherit the values of default BDT sc's min_cardinality, max_cardinality
					vo.setMinCardinality(baseDefaultBDTSC.getMinCardinality());
					vo.setMaxCardinality(baseDefaultBDTSC.getMaxCardinality());
					vo.setBasedDTSCID(baseDefaultBDTSC.getDTSCID());
					System.out.println("~~~"+vo.getPropertyTerm()+" "+vo.getRepresentationTerm()+". This SC owned by unqualified BDT is inherited from Base!");
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
				String denType = Utility.DenToName(dtVO.getDEN());
				System.out.println("Validating SCs for unqualified bdt with type = "+denType);
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
					
					String fromXSD="";
					String fromDB="";
					
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
					
					String propertyTerm = "null";
					String representationTerm = "null";
					String definition = "null";
					
					propertyTerm = Utility.spaceSeparator(attribute_name);
					if(!is_fields_xsd) {
						propertyTerm = Utility.spaceSeparatorBeforeStr(attribute_name, "Code");
					}

					representationTerm = Utility.getRepresentationTerm(attribute_name);
					
					Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
					if(defNode==null){
						defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dtVO.getDEN()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation");
					}
					if(defNode!=null){
						definition = defNode.getTextContent();
					}
					
					fromXSD = fromXSD + attribute_id;
					fromXSD = fromXSD + propertyTerm;
					fromXSD = fromXSD + representationTerm;
					fromXSD = fromXSD + definition;
					fromXSD = fromXSD + min_cardinality;
					fromXSD = fromXSD + max_cardinality;
					
					QueryCondition qc4DTSC = new QueryCondition();
					qc4DTSC.add("guid", attribute_id);
					DTSCVO dtscVO = (DTSCVO) daoDTSC.findObject(qc4DTSC);
					
					if(dtscVO==null){
						System.out.println("@@@@ The Attribute is not imported! Check DT: "+ dtVO.getDTGUID());
					}
					else {
						fromDB = fromDB+dtscVO.getDTSCGUID();
						fromDB = fromDB+dtscVO.getPropertyTerm();
						fromDB = fromDB+dtscVO.getRepresentationTerm();
						fromDB = fromDB+dtscVO.getDefinition();
						fromDB = fromDB+dtscVO.getMinCardinality();
						fromDB = fromDB+dtscVO.getMaxCardinality();
						
						if(!fromXSD.equals(fromDB)){
							System.out.println("@@@@ DTSC from Attributes has different values! Check DTSC (guid="+dtscVO.getDTSCGUID()+")");
							System.out.println("     FromXSD: "+fromXSD);
							System.out.println("      FromDB: "+fromDB);
						}
					}
					
					
					//if it has new attribute extension, 
					//it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map					
					DTSCVO insertedSCVO = new DTSCVO();
					QueryCondition qc4SC = new QueryCondition();
					qc4SC.add("guid", vo.getDTSCGUID());
					insertedSCVO = (DTSCVO) daoDTSC.findObject(qc4SC);
					
					ArrayList<SRTObject> DTVOwDataTypeTerm = new ArrayList<SRTObject>();
					QueryCondition qc4DTwDataTypeTerm = new QueryCondition();
					qc4DTwDataTypeTerm.add("data_type_term", insertedSCVO.getRepresentationTerm());
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
								
								QueryCondition qc4pri = new QueryCondition();
								qc4pri.add("cdt_pri_id", cdtAP.getCDTPrimitiveID());
								CDTPrimitiveVO tmpPri = (CDTPrimitiveVO) aCDTPrimitiveDAO.findObject(qc4pri);
								
								String expressionLanguageOrActionCode = "";
								expressionLanguageOrActionCode= insertedSCVO.getPropertyTerm();
								
//								System.out.print("   ~~~"+insertedSCVO.getPropertyTerm()+" "+insertedSCVO.getRepresentationTerm()+" is "+tmpPri.getName());
//								if(cdtSCAP.getisDefault()){
//									System.out.println(" and it's Default!");
//								}
//								else{
//									System.out.println("");
//								}
								
								//aCDTSCAllowedPrimitiveDAO.insertObject(cdtSCAP);	
								
								
								System.out.println("        ** Pri:" + tmpPri.getName() + " Default:" + cdtSCAP.getisDefault());
									
								CDTSCAllowedPrimitiveVO insertedCDTSCAP = new CDTSCAllowedPrimitiveVO();
								QueryCondition qc4SCAP = new QueryCondition();
								qc4SCAP.add("CDT_SC_ID", cdtSCAP.getCDTSCID());
								qc4SCAP.add("CDT_Pri_id", cdtSCAP.getCDTPrimitiveID());
								insertedCDTSCAP = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc4SCAP);
								
								QueryCondition qc4XTMap = new QueryCondition();
								qc4XTMap.add("CDT_awd_pri_id", cdtAP.getCDTAllowedPrimitiveID());
								ArrayList<SRTObject> cdtAPXTMs = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4XTMap);
								for(int m=0; m<cdtAPXTMs.size(); m++){
									CDTAllowedPrimitiveExpressionTypeMapVO thisAPXTmap= (CDTAllowedPrimitiveExpressionTypeMapVO) cdtAPXTMs.get(m);
									CDTSCAllowedPrimitiveExpressionTypeMapVO tmp = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
									tmp.setXSDBuiltInTypeID(thisAPXTmap.getXSDBuiltInTypeID());
									tmp.setCDTSCAllowedPrimitive(insertedCDTSCAP.getCDTSCAllowedPrimitiveID());
									
									QueryCondition qc4xbt = new QueryCondition();
									qc4xbt.add("xbt_id", tmp.getXSDBuiltInTypeID());
									XSDBuiltInTypeVO xbt = (XSDBuiltInTypeVO) aXBTDAO.findObject(qc4xbt);
											
									//aCDTSCAllowedPrimitiveExpressionTypeMapDAO.insertObject(tmp);
									System.out.println("          ** XBT:" + xbt.getBuiltInType());

								}
							}
							break;//if this is hit, that means dt sc is mapped to cdt sc
						}
					}
				}

				//Inherit From the remain SCs based on default bdt because they don't have corresponding attributes
				//Just copy and get the values from remain baseDefaultDTSCs
				
				QueryCondition qc4UnqualifiedDTSC = new QueryCondition();
				qc4UnqualifiedDTSC.add("owner_dt_id", dtVO.getDTID());
				ArrayList<SRTObject> unqaulifiedDTSCs = daoDTSC.findObjects(qc4UnqualifiedDTSC);
				
				for(int i=baseDefaultDTSCs.size()-1; i>-1; i--){
					DTSCVO baseDefaultDTSC = (DTSCVO) baseDefaultDTSCs.get(i);
					System.out.print("    Unqualified SC:"+baseDefaultDTSC.getPropertyTerm() +" "+ baseDefaultDTSC.getRepresentationTerm());

					for(int j=0; j<unqaulifiedDTSCs.size(); j++){
						
						DTSCVO unqualifiedDTSC = (DTSCVO) unqaulifiedDTSCs.get(j);

						if(baseDefaultDTSC.getPropertyTerm().equals(unqualifiedDTSC.getPropertyTerm())
						&& baseDefaultDTSC.getRepresentationTerm().equals(unqualifiedDTSC.getRepresentationTerm())
						&& baseDefaultDTSC.getDefinition().equals(unqualifiedDTSC.getDefinition())
						&& baseDefaultDTSC.getDTSCID()==unqualifiedDTSC.getBasedDTSCID()){
							System.out.print(" has corresponding attr="+baseDefaultDTSC.getPropertyTerm() +" "+ baseDefaultDTSC.getRepresentationTerm());
							
							Node attribute = xh2.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+unqualifiedDTSC.getDTSCGUID()+"']");
							if(attribute==null){
								attribute = xh.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+unqualifiedDTSC.getDTSCGUID()+"']");
							}
							
							if(attribute!=null){
								Element ele = (Element) attribute;
								
								if(ele.getAttribute("use").equalsIgnoreCase("required")){
									if(unqualifiedDTSC.getMinCardinality()==1 && unqualifiedDTSC.getMaxCardinality()==1){
										baseDefaultDTSCs.remove(i);
										break;
									}
								}
								else if (ele.getAttribute("use").equalsIgnoreCase("optional")){
									if(unqualifiedDTSC.getMinCardinality()==0 && unqualifiedDTSC.getMaxCardinality()==1){
										baseDefaultDTSCs.remove(i);
										break;
									}
								}
								else if (ele.getAttribute("use").equalsIgnoreCase("prohibited")){
									if(unqualifiedDTSC.getMinCardinality()==0 && unqualifiedDTSC.getMaxCardinality()==0){
										baseDefaultDTSCs.remove(i);
										break;
									}
								}
							}
							else {
								if(baseDefaultDTSC.getMinCardinality() == unqualifiedDTSC.getMinCardinality()
								&& baseDefaultDTSC.getMaxCardinality() == unqualifiedDTSC.getMaxCardinality()){
									baseDefaultDTSCs.remove(i);
									break;
								}
							}
						}
					}
					System.out.println("");
				}
				for(int i=baseDefaultDTSCs.size()-1; i>-1; i--){
					DTSCVO bdtscVO = (DTSCVO) baseDefaultDTSCs.get(i);
					System.out.println("@@@@ "+bdtscVO.getPropertyTerm() + " " + bdtscVO.getRepresentationTerm() + " is not inherited! Check Unqualified BDT: "+dtVO.getDTGUID());
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
