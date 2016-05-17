package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;

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
import org.oagi.srt.persistence.dto.AgencyIDListValueVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/

public class P_1_4_PopulateCodeList {
	
	private static Connection conn = null;

	public void codeList(String fileinput, int agencyid) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		CodeListVO codelistVO = new CodeListVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
    	
		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
    	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    if(tmp.getAttribute("name").endsWith("CodeContentType")){
		    	codelistVO.setCodeListGUID(tmp.getAttribute("id"));
		    	
//		    	for(int j = 0; j < result.getLength(); j++) {
//		    		Element tmp2 = (Element)result.item(j);
//
//	    			if(tmp2.getAttribute("name").endsWith("EnumerationType")) {
//	    				if(tmp2.getAttribute("name").substring(0, tmp2.getAttribute("name").lastIndexOf("EnumerationType")).equals(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")))) {
//		    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
//		    				break;
//		    			}
//		    		} else {
//	    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
//	    			}
//		    			
//		    	}
		    	//added by TKim according to design Doc v2.4
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
		    		//System.out.print("  "+tmp.getAttribute("name") + " with ");
		    		Node enumTypeNode = xh.getNode("//xsd:simpleType[@name='"+xyz+"CodeEnumerationType']/@id");
		    		String enumerationTypeGUID = null;
		    		if(enumTypeNode!=null){
		    			enumerationTypeGUID = enumTypeNode.getTextContent();
			    		//System.out.println(xyz+"CodeEnumerationTypeGUID: "+enumerationTypeGUID);
		    		}
		    		codelistVO.setEnumerationTypeGUID(enumerationTypeGUID);
		    	}
		    	else{
		    		//System.out.print("  "+tmp.getAttribute("name") + " with its own enumeration ");
		    		codelistVO.setEnumerationTypeGUID(null);
		    	}
		    	
		    	codelistVO.setName(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")));
		    	codelistVO.setListID(tmp.getAttribute("id"));
		    	codelistVO.setAgencyID(getAgencyID(agencyid));
		    	
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		codelistVO.setVersionID("1");
		    	} else if(tmp.getAttribute("name").equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
		    		codelistVO.setVersionID("5");
		    	} else {
			    	for(int j = 0; j < tmp.getAttribute("name").length(); j++) {
				    	if(tmp.getAttribute("name").charAt(j)>47 && tmp.getAttribute("name").charAt(j)<58) {
				    		for(int k = j+1 ; k < tmp.getAttribute("name").length(); k++) {
				    			if(tmp.getAttribute("name").charAt(k) == '_') {
						    		String complicated_version = tmp.getAttribute("name").substring(j, k);
						    		for(int l = complicated_version.length()-1 ; l >=0 ; l-- ) {
						    			if(!(complicated_version.charAt(l) > 47 && complicated_version.charAt(l) < 58)){
						    				complicated_version = complicated_version.substring(l+1);
						    				break;
						    			}
						    		}
				    				codelistVO.setVersionID(complicated_version);
						    		break;
				    			}
				    		}
				    	break;
				    	}
			    	}		    		
		    	}
		    	
    			Node definition_node = xh.getNode("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']/xsd:annotation/xsd:documentation");	
    			if(definition_node!=null){
	    			Element definition_element2 = (Element)definition_node;
	    			codelistVO.setDefinition(definition_element2.getTextContent());
			    	codelistVO.setDefinitionSource(definition_element2.getAttribute("source"));
    			}
    			else{
    				codelistVO.setDefinition(null);
			    	codelistVO.setDefinitionSource(null);
    			} 		    	
		    	codelistVO.setExtensibleIndicator(true);  //logic changed. extensible indicator is always TRUE.
		    	
//		    	for(int j = 0; j < union.getLength(); j++) {
//				    Element tmp2 = (Element)union.item(j);
//				    if(tmp2.getAttribute("name").equals(tmp.getAttribute("name"))) {
//				    	codelistVO.setExtensibleIndicator(true);
//				    	break;
//				    }
//				}

		    	int id = getUserID("oagis");
				codelistVO.setCreatedByUserID(id);
				codelistVO.setLastUpdatedByUserID(id);
				codelistVO.setLastUpdateTimestamp(current_stamp);
				codelistVO.setState(SRTConstants.CODE_LIST_STATE_PUBLISHED);
//	    		Node union_check = xh.getNode("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:union");
//	    		if(union_check != null){
//		    		codelistVO.setExtensibleIndicator(true);
//	    		}
//	    		else
//	    			codelistVO.setExtensibleIndicator(false);
	    		
		    	dao.insertObject(codelistVO);
		    }
		    else
		    	if(!tmp.getAttribute("name").endsWith("EnumerationType"))
		    		System.out.println("Check !!  "+tmp.getAttribute("name"));
		}
	}
	
	public void validateImportCodeList(String fileinput, int agencyid) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		CodeListVO codelistVO = new CodeListVO();
		CodeListVO codelistFromDBVO = new CodeListVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
    	NodeList union = xh.getNodeList("//xsd:simpleType//xsd:union");
    	
		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
    	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    if(tmp.getAttribute("name").endsWith("CodeContentType")){
		    	
		    	String fromXSD = "";
		    	String fromDB = "";
		    	
		    	fromXSD = fromXSD + tmp.getAttribute("id");
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
		    		Node enumTypeNode = xh.getNode("//xsd:simpleType[@name='"+xyz+"CodeEnumerationType']/@id");
		    		if(enumTypeNode!=null){
		    			fromXSD = fromXSD+ enumTypeNode.getTextContent();
		    			
		    		}
		    		else {
		    		fromXSD = fromXSD+"null";
		    		}
		    	}
		    	else {
		    		fromXSD = fromXSD+"null";
		    	}
		    	
		    	fromXSD = fromXSD + tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType"));
		    	fromXSD = fromXSD + tmp.getAttribute("id");
		    	fromXSD = fromXSD + getAgencyID(agencyid);
		    	
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		fromXSD = fromXSD + "1";
		    	} else if(tmp.getAttribute("name").equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
		    		fromXSD = fromXSD + "5";
		    	} else {
			    	for(int j = 0; j < tmp.getAttribute("name").length(); j++) {
				    	if(tmp.getAttribute("name").charAt(j)>47 && tmp.getAttribute("name").charAt(j)<58) {
				    		for(int k = j+1 ; k < tmp.getAttribute("name").length(); k++) {
				    			if(tmp.getAttribute("name").charAt(k) == '_') {
						    		String complicated_version = tmp.getAttribute("name").substring(j, k);
						    		for(int l = complicated_version.length()-1 ; l >=0 ; l-- ) {
						    			if(!(complicated_version.charAt(l) > 47 && complicated_version.charAt(l) < 58)){
						    				complicated_version = complicated_version.substring(l+1);
						    				break;
						    			}
						    		}
						    		fromXSD = fromXSD + complicated_version;
						    		break;
				    			}
				    		}
				    	break;
				    	}
			    	}		    		
		    	}
		    	
			    Node definition = xh.getNode("//xsd:simpleType[@name='"+tmp.getAttribute("name")+"']/xsd:annotation/xsd:documentation");	
		    	
	    		if(definition!=null){
	    			Element definition_element = (Element)definition;
	    			fromXSD = fromXSD + definition_element.getTextContent();
	    			fromXSD = fromXSD + definition_element.getAttribute("source");
	    		}
	    		else {
	    			fromXSD = fromXSD + "null" + "null";
	    		}
	    		fromXSD = fromXSD + "null"; // remark
		    	
		    	String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
				Node unionNode = xh.getNode("//xsd:simpleType[@name='"+xyz+"CodeContentType']//xsd:union/@memberTypes");
				
				if(unionNode!=null){
					String unionMemberStr = null;
					unionMemberStr = unionNode.getTextContent();
					unionMemberStr = unionMemberStr.replace("xsd:token", "");
					unionMemberStr = unionMemberStr.trim();
				
					if(!unionMemberStr.equals(xyz+"CodeEnumerationType")){
						unionMemberStr = unionMemberStr.replace("CodeContentType","Code");
						fromXSD = fromXSD+unionMemberStr;
					}
					else {
						fromXSD = fromXSD+"null";
					}
				}
				else {
					fromXSD = fromXSD + "null" ;
				}
		    	
		    	fromXSD = fromXSD + "true"; //for extensible indicator True
		    	int id = getUserID("oagis");
		    	fromXSD = fromXSD + id + id + "Published";
				
		    	QueryCondition qc = new QueryCondition();
		    	qc.add("guid", tmp.getAttribute("id"));
		    	codelistFromDBVO = (CodeListVO) dao.findObject(qc);
		    	
		    	if(codelistFromDBVO==null){
		    		System.out.println("@@@@@@@Check: "+tmp.getAttribute("id") + " is not imported!");
		    	}
		    	else {
			    	fromDB = fromDB + codelistFromDBVO.getCodeListGUID() 
			    	+ codelistFromDBVO.getEnumerationTypeGUID()
			    	+ codelistFromDBVO.getName()
			    	+ codelistFromDBVO.getListID()
			    	+ codelistFromDBVO.getAgencyID() 
			    	+ codelistFromDBVO.getVersionID()
			    	+ codelistFromDBVO.getDefinition()
			    	+ codelistFromDBVO.getDefinitionSource()
			    	+ codelistFromDBVO.getRemark();
			    	
			    	QueryCondition qc4Base = new QueryCondition();
			    	qc4Base.add("code_list_id", codelistFromDBVO.getBasedCodeListID());
			    	CodeListVO baseCodeListVO = new CodeListVO();
			    	baseCodeListVO = (CodeListVO) dao.findObject(qc4Base);
			    	fromDB = fromDB + baseCodeListVO.getName();  //base code list's Name is used instead of its ID
			    	
			    	fromDB = fromDB+ codelistFromDBVO.getExtensibleIndicator()
			    	+ codelistFromDBVO.getCreatedByUserID()
			    	+ codelistFromDBVO.getLastUpdatedByUserID()
			    	+ codelistFromDBVO.getState();

			    	if(!fromDB.equals(fromXSD)){
			    		System.out.println("@@@@@@@Check: "+tmp.getAttribute("id") + " has different values!");
			    		System.out.println("            FromXSD: "+fromXSD);
			    		System.out.println("             FromDB: "+fromDB);
			    	}
		    	}
		    	
		    }
		    else
		    	if(!tmp.getAttribute("name").endsWith("EnumerationType"))
		    		System.out.println("Check !!  "+tmp.getAttribute("name"));
		}
	}
	
	public int getUserID(String userName) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("User");
    	QueryCondition qc = new QueryCondition();
		qc.add("login_id", userName);
		UserVO userVO = (UserVO)dao.findObject(qc, conn);
		int id = userVO.getUserID();
		return id;
	}
	
	public int getAgencyID(int valueid) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDListValue");
    	QueryCondition qc = new QueryCondition();
    	qc.add("value", String.valueOf(valueid));
		AgencyIDListValueVO agencyIDListValueVO = (AgencyIDListValueVO)dao.findObject(qc);
		int id = agencyIDListValueVO.getAgencyIDListValueID();
		return id;
	}
	
	
	public void codeListValue(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeListValue");
		NodeList result = xh.getNodeList("//xsd:simpleType");
		NodeList enumeration = null;
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);

		    enumeration = null;

	    	if(tmp.getAttribute("name").endsWith("CodeContentType")) {
	    		//System.out.println("Import CodeListValue for: "+tmp.getAttribute("name"));
		    	SRTDAO dao2 = df2.getDAO("CodeList");
			   	QueryCondition qc = new QueryCondition();
				qc.add("GUID", new String(tmp.getAttribute("id")));
				CodeListVO codelistVO = (CodeListVO)dao2.findObject(qc, conn);
				
				if(codelistVO.getEnumerationTypeGUID()!=null){
					//Get code_list_value from enumerationType 
					enumeration = xh.getNodeList("//xsd:simpleType[@id='"+codelistVO.getEnumerationTypeGUID()+"']//xsd:enumeration");
					if(enumeration.getLength() < 1) {
						System.out.println("   EnumerationType "+codelistVO.getEnumerationTypeGUID() + " has no enumerations");
					}
					else {
						System.out.println("   Start import from EnumerationType: "+codelistVO.getEnumerationTypeGUID());
					}
				}
				else {//if enum_type_guid is null
					if(codelistVO.getBasedCodeListID()>0){
						//Inherit from based code list
						QueryCondition qcBase = new QueryCondition();
						qcBase.add("code_list_id", codelistVO.getBasedCodeListID());
						ArrayList<SRTObject> codeListValuesFromBase = dao.findObjects(qcBase);
						
						if(codeListValuesFromBase.size() > 0){
							System.out.println("   Start inherit from BasedCodeList: "+codelistVO.getName());
							for(int j=0; j<codeListValuesFromBase.size(); j++){
								CodeListValueVO codelistvalVO = (CodeListValueVO) codeListValuesFromBase.get(j);
								codelistvalVO.setOwnerCodeListID(codelistVO.getCodeListID());
								//System.out.print("       "+codelistvalVO.getName()+" (value=");
								//System.out.println(codelistvalVO.getValue()+")");
								dao.insertObject(codelistvalVO);
							}
						}
					}
					else {//if based code list is null
						//Get the values from local xsd:enumeration
						enumeration = xh.getNodeList("//xsd:simpleType[@name='"+tmp.getAttribute("name")+"']//xsd:enumeration");
						if(enumeration.getLength() < 1) {
							System.out.println("   "+codelistVO.getEnumerationTypeGUID() + " has no enumerations");
						}
						else {
							System.out.println("   Start import from LocalEnumeration: ");
						}
					}
				}
				
				if(enumeration!=null) {
					for(int j=0; j<enumeration.getLength(); j++){
						Element aEnum = (Element) enumeration.item(j);
						CodeListValueVO codelistvalueVO = new CodeListValueVO();
						codelistvalueVO.setOwnerCodeListID(codelistVO.getCodeListID());
						codelistvalueVO.setValue(aEnum.getAttribute("value"));
						codelistvalueVO.setName(aEnum.getAttribute("value"));
						codelistvalueVO.setUsedIndicator(true);
						codelistvalueVO.setLockedIndicator(false);
						codelistvalueVO.setExtensionIndicator(false);
						
						//System.out.print("       "+codelistvalueVO.getName()+" (value=");
						//System.out.println(codelistvalueVO.getValue()+")");
		
						Node definitionNode = xh.getNode("//xsd:simpleType[@name='"+tmp.getAttribute("name")+"']//xsd:enumeration[@value='"+codelistvalueVO.getValue()+"']//xsd:documentation");
						if(definitionNode == null){
							definitionNode = xh.getNode("//xsd:simpleType[@id='"+codelistVO.getEnumerationTypeGUID()+"']//xsd:enumeration[@value='"+codelistvalueVO.getValue()+"']//xsd:documentation");
						}
						if(definitionNode!=null){
							Element definition = (Element) definitionNode;
							codelistvalueVO.setDefinition(definition.getTextContent());
							codelistvalueVO.setDefinitionSource(definition.getAttribute("source"));
						}
						else {
							codelistvalueVO.setDefinition(null);
							codelistvalueVO.setDefinitionSource(null);
						}
						dao.insertObject(codelistvalueVO);
					}
				}
			}
		}
	}
	
	public void validateImportCodeListValue(String fileinput, String[][] reference) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeListValue");
		NodeList result = xh.getNodeList("//xsd:simpleType");
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
	    	if(tmp.getAttribute("name").endsWith("CodeContentType")) {
	    		SRTDAO dao2 = df2.getDAO("CodeList");
			   	QueryCondition qc = new QueryCondition();
				qc.add("GUID", new String(tmp.getAttribute("id")));
				CodeListVO codelistVO = (CodeListVO)dao2.findObject(qc, conn);
				
				QueryCondition qc4CodeListVal = new QueryCondition();
				qc4CodeListVal.add("code_list_id", codelistVO.getCodeListID());
				ArrayList<SRTObject> clvalListVOFromDB = dao.findObjects(qc4CodeListVal);
				ArrayList<String> clvalListFromXSD = new ArrayList<String>();
				ArrayList<String> clvalListFromDB = new ArrayList<String>();
				
				if(tmp.getAttribute("name").startsWith("oacl")){
					
					//check its enum type is corresponding to it 
					// corresponding means that //xsd:simpleType[@name = 'xyzCodeEnumerationType'] exists
					String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
					Node unionNode = xh.getNode("//xsd:simpleType[@name='"+xyz+"CodeContentType']//xsd:union/@memberTypes");
					
					if(unionNode!=null){
						String unionMemberStr = null;
						unionMemberStr = unionNode.getTextContent();
						unionMemberStr = unionMemberStr.replace("xsd:token", "");
						unionMemberStr = unionMemberStr.trim();
						String checkEnumType = xyz+"CodeEnumerationType";
						
						if(unionMemberStr.equals(checkEnumType)){
							//When corresponding Enum Type Exists, codelistvalues are from that enumType
							
							NodeList enumerations = xh.getNodeList("//xsd:simpleType[@name='"+checkEnumType+"']//xsd:enumeration");
							
							for(int j=0;j<enumerations.getLength(); j++){
								Element itsEnum = (Element) enumerations.item(j);
								
								String codeListVal = "";
								codeListVal = codeListVal+itsEnum.getAttribute("value");
								Node enumNode = xh.getNode("//xsd:simpleType[@name='"+checkEnumType+"']//xsd:enumeration[@value='"+codeListVal+"']//xsd:documentation");
								
								if(enumNode!=null){
									
									Element definition = (Element) enumNode;
									
									codeListVal = codeListVal+definition.getTextContent();
									
									codeListVal = codeListVal + definition.getAttribute("source");
								}
								else {
									codeListVal = codeListVal + "null"+"null";
								}
								
								codeListVal = codeListVal + "true" + "false"+"false";
								
										
								clvalListFromXSD.add(codeListVal);
							}
							
						}
						else if(unionMemberStr.endsWith("CodeContentType")){
							//When the enumeration exists but not corresponding (except xsd:token), copy the union member type's local enumerations
							
							XPathHandler xh4Base = new XPathHandler(path1);
							//At first, find the Type referring the codeList files
							NodeList enumerations = null;
							for(int j=0; j<reference.length; j++){
								
								String searchFileName = SRTConstants.filepath("CodeList") + reference[j][0]+".xsd";
								xh4Base = new XPathHandler(searchFileName);
								
								enumerations = xh4Base.getNodeList("//xsd:simpleType[@name='"+unionMemberStr+"']//xsd:enumeration");
								if(enumerations!=null && enumerations.getLength()>0){
									System.out.println("     "+unionMemberStr+": BaseType is In File--> "+reference[j][0]+".xsd");
									break;
								}
							}
							
							
							if(enumerations!=null) {
								for(int j=0;j<enumerations.getLength(); j++){
									Element itsEnum = (Element) enumerations.item(j);
									
									String codeListVal = "";
									codeListVal = codeListVal+itsEnum.getAttribute("value");
									Node enumNode = xh4Base.getNode("//xsd:simpleType[@name='"+unionMemberStr+"']//xsd:enumeration[@value='"+codeListVal+"']//xsd:documentation");
									
									if(enumNode!=null){
										
										Element definition = (Element) enumNode;
										
										codeListVal = codeListVal+definition.getTextContent();
										
										codeListVal = codeListVal + definition.getAttribute("source");
									}
									else {
										codeListVal = codeListVal + "null"+"null";
									}
									
									codeListVal = codeListVal + "true"+"false"+"false";

									clvalListFromXSD.add(codeListVal);
								}
							}
							
						}
					}
				}
				else {//has no union (Not oacl_~~), get them from its local enumeration 
					NodeList enumerations = xh.getNodeList("//xsd:simpleType[@name='"+tmp.getAttribute("name")+"']//xsd:enumeration");
					
					for(int j=0;j<enumerations.getLength(); j++){
						Element itsEnum = (Element) enumerations.item(j);
						
						String codeListVal = "";
						codeListVal = codeListVal+itsEnum.getAttribute("value");
						Node enumNode = xh.getNode("//xsd:simpleType[@name='"+tmp.getAttribute("name")+"']//xsd:enumeration[@value='"+codeListVal+"']//xsd:documentation");
						
						if(enumNode!=null){
							
							Element definition = (Element) enumNode;
							
							codeListVal = codeListVal+definition.getTextContent();
							
							codeListVal = codeListVal + definition.getAttribute("source");
						}
						else {
							codeListVal = codeListVal + "null"+"null";
						}
						codeListVal = codeListVal + "true"+"false"+"false";
						
								
						clvalListFromXSD.add(codeListVal);
					}
				}
				
				for (int j=0; j<clvalListVOFromDB.size(); j++){
					String clval = "";
					CodeListValueVO thisClvalVO = (CodeListValueVO) clvalListVOFromDB.get(j);
					clval = clval+thisClvalVO.getValue();
					clval = clval+thisClvalVO.getDefinition();
					clval = clval+thisClvalVO.getDefinitionSource();
					clval = clval+thisClvalVO.getUsedIndicator();
					clval = clval+thisClvalVO.getLockedIndicator();
					clval = clval+thisClvalVO.isExtensionIndicator();
					clvalListFromDB.add(clval);
				}
				
				clvalListFromDB.sort(null);
				clvalListFromXSD.sort(null);

				if(clvalListFromDB.size() != clvalListFromXSD.size() ){
					System.out.print("@@@@ Some CodeListValues are not imported! Check code list: "+codelistVO.getCodeListID());
					System.out.println("      CountfromXSD: "+clvalListFromXSD.size() +" CountfromDB: "+clvalListFromDB.size());
				}
				else {
					for(int j=0; j<clvalListFromXSD.size(); j++){
						if(!clvalListFromXSD.get(j).equals(clvalListFromDB.get(j))){
							System.out.println("@@@@CodeListValue has different values! Check code list: "+codelistVO.getCodeListID());
							System.out.println(clvalListFromXSD.get(j));
							System.out.println(clvalListFromDB.get(j));
						}
					}
				}
			}
		}
	}
	public void updateBasedCodeListID(String fileinput, int agencyid) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, SRTDAOException, XPathExpressionException{
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		
		NodeList result = xh.getNodeList("//xsd:simpleType");
		
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    if(tmp.getAttribute("name").endsWith("CodeContentType")){
		    	//added by T.Kim according to design doc v2.4
		    	//based_code_list_id
		    	String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
				Node unionNode = xh.getNode("//xsd:simpleType[@name='"+xyz+"CodeContentType']//xsd:union/@memberTypes");
				
				if(unionNode!=null){
					String unionMemberStr = null;
					unionMemberStr = unionNode.getTextContent();
					unionMemberStr = unionMemberStr.replace("xsd:token", "");
					unionMemberStr = unionMemberStr.trim();
					String checkEnumType = xyz+"CodeEnumerationType";
					
					if(!unionMemberStr.equals(checkEnumType)){//find base code list!
						unionMemberStr = unionMemberStr.replace("ContentType", "");
						
						QueryCondition qc = new QueryCondition();
						qc.addLikeClause("name", unionMemberStr);
						CodeListVO baseCodelistVO = (CodeListVO) dao.findObject(qc);
						
						QueryCondition qc2 = new QueryCondition();
						qc2.add("guid", tmp.getAttribute("id"));
						CodeListVO codelistVO = (CodeListVO) dao.findObject(qc2);
						
						if(baseCodelistVO!=null && codelistVO!=null){					
							codelistVO.setBasedCodeListID(baseCodelistVO.getCodeListID());
							dao.updateObject(codelistVO);
							System.out.println(" Update Based Code List ID: "+tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType"))+ " is based on "+baseCodelistVO.getName());
						}
						else {
							System.out.println(" Update Based Code List ID Is Failed! Check CodeListID: "+tmp.getAttribute("name"));
							return;
						}
					}
				}
		    }
		    else
		    	if(!tmp.getAttribute("name").endsWith("EnumerationType"))
		    		System.out.println("Check !!  "+tmp.getAttribute("name"));
		}
		
	}
	
	public void run() throws Exception {
		System.out.println("### 1.4 Start");	
		
		DBAgent tx = new DBAgent();
		conn = tx.open();

		String tt[][] = {{"CodeList_ConditionTypeCode_1","314"}, {"CodeList_ConstraintTypeCode_1","314"}, 
				{"CodeList_DateFormatCode_1","314"}, {"CodeList_DateTimeFormatCode_1","314"}, {"CodeList_TimeFormatCode_1","314"},
				{"CodeList_CharacterSetCode_IANA_20070514", "379"}, {"CodeList_MIMEMediaTypeCode_IANA_7_04","379"}, 
				{"CodeList_CurrencyCode_ISO_7_04","5"}, {"CodeList_LanguageCode_ISO_7_04", "5"}, {"CodeList_TimeZoneCode_1", "5"},
				{"CodeList_UnitCode_UNECE_7_04", "6"}, {"CodeLists_1","314"}};
		
		for(int i = 0; i< tt.length; i++) {
			System.out.println("## Importing Code List from "+tt[i][0]+"..");
			String filename = tt[i][0] + ".xsd";
			codeList(filename, Integer.parseInt(tt[i][1]));
		}
		
		for(int i = 0; i< tt.length; i++) {
			System.out.println("## Updating Code List referring "+tt[i][0]+"..");

			String filename = tt[i][0] + ".xsd";
			updateBasedCodeListID(filename, Integer.parseInt(tt[i][1]));
		}
		
		for(int i = 0; i< tt.length; i++) {
			System.out.println("## Impoting Code List Value from "+tt[i][0]+"..");

			String filename = tt[i][0] + ".xsd";
			codeListValue(filename);
		}
		
		tx.close();
		conn.close();
		System.out.println("### 1.4 End");		
	}
	
	public void validate_P_1_4_PopulateCodeList() throws Exception{
		System.out.println("### 1.4 Start Validation");	
		
		DBAgent tx = new DBAgent();
		conn = tx.open();

		String tt[][] = {{"CodeList_ConditionTypeCode_1","314"}, {"CodeList_ConstraintTypeCode_1","314"}, 
				{"CodeList_DateFormatCode_1","314"}, {"CodeList_DateTimeFormatCode_1","314"}, {"CodeList_TimeFormatCode_1","314"},
				{"CodeList_CharacterSetCode_IANA_20070514", "379"}, {"CodeList_MIMEMediaTypeCode_IANA_7_04","379"}, 
				{"CodeList_CurrencyCode_ISO_7_04","5"}, {"CodeList_LanguageCode_ISO_7_04", "5"}, {"CodeList_TimeZoneCode_1", "5"},
				{"CodeList_UnitCode_UNECE_7_04", "6"}, {"CodeLists_1","314"}};
		
		for(int i = 0; i< tt.length; i++) {
			System.out.println("@@ Validating Import Code List from "+tt[i][0]+"..");
			String filename = tt[i][0] + ".xsd";
			validateImportCodeList(filename, Integer.parseInt(tt[i][1]));
		}
		
		for(int i = 0; i< tt.length; i++) {
			System.out.println("@@ Validating Import Code List Value from "+tt[i][0]+"..");
			String filename = tt[i][0] + ".xsd";
			validateImportCodeListValue(filename, tt);
		}
		

		
		tx.close();
		conn.close();
		System.out.println("### 1.4 Validation End");		
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_4_PopulateCodeList codelist = new P_1_4_PopulateCodeList();
		codelist.run();

	}
}
