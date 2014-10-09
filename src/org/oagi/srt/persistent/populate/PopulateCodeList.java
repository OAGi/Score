package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.startup.SRTInitializerException;
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

public class PopulateCodeList {

	public void codeList(String fileinput, int agencyid) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
		CodeListVO codelistVO = new CodeListVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
	    NodeList definition = xh.getNodeList("//xsd:simpleType[xsd:annotation[xsd:documentation]]");	
    	NodeList union = xh.getNodeList("//xsd:simpleType[xsd:union]");
    	
		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
    	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    if(tmp.getAttribute("name").endsWith("CodeContentType")){
		    	codelistVO.setCodeListGUID(tmp.getAttribute("id"));
		    	
		    	for(int j = 0; j < result.getLength(); j++) {
		    		Element tmp2 = (Element)result.item(j);
		    		

	    			if(tmp2.getAttribute("name").endsWith("EnumerationType")) {
	    				if(tmp2.getAttribute("name").substring(0, tmp2.getAttribute("name").lastIndexOf("EnumerationType")).equals(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")))
		    				) {
		    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
		    				break;
		    			}
		    		}
	    			else {
	    				codelistVO.setEnumerationTypeGUID(tmp2.getAttribute("id"));
	    			}
		    			
		    	}
		   
		    	codelistVO.setName(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")));
		    	codelistVO.setListID(tmp.getAttribute("id"));
		    	codelistVO.setAgencyID(agencyid);
		    	
		    	if(tmp.getAttribute("name").startsWith("oacl")) {
		    		codelistVO.setVersionID("1");
		    	}
		    	else if(tmp.getAttribute("name").equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
		    		codelistVO.setVersionID("5");
		    	}
		    	else {
			    	for(int j = 0; j < tmp.getAttribute("name").length(); j++) {
				    	if(tmp.getAttribute("name").charAt(j)>47 && tmp.getAttribute("name").charAt(j)<58) {
				    		for(int k = j+1 ; k < tmp.getAttribute("name").length(); k++){
				    			if(tmp.getAttribute("name").charAt(k) == '_'){
						    		codelistVO.setVersionID(tmp.getAttribute("name").substring(j, k));
						    		break;
				    			}
				    		}
				    	break;
				    	}
			    	}		    		
		    	}
		    	
		    	for(int j = 0 ; j < definition.getLength(); j++) {
		    		Element definition_element = (Element)definition.item(j);
		    		if(definition_element.getAttribute("id") == tmp.getAttribute("id")) {
		    			Node definition_node = xh.getNode("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:annotation//xsd:documentation");	
		    			Element definition_element2 = (Element)definition_node;
		    			codelistVO.setDefinition(definition_element2.getTextContent());
				    	codelistVO.setDefinitionSource(definition_element2.getAttribute("source"));
				    	break;
		    		}
		    		
	    			codelistVO.setDefinition("NONE");
			    	codelistVO.setDefinitionSource("NONE");				    	

		    	}
		    	
		    	codelistVO.setBasedCodeListID(1);//Empty
		    	codelistVO.setExtensibleIndicator(false); 
		    	for(int j = 0; j < union.getLength(); j++) {
				    Element tmp2 = (Element)union.item(j);
				    if(tmp2.getAttribute("name").equals(tmp.getAttribute("name"))) {
				    	codelistVO.setExtensibleIndicator(true);
				    	break;
				    }
				}

		    	int id = getUserID("oagis");
				codelistVO.setCreatedByUserID(id);
				codelistVO.setLastUpdatedByUserID(id);
				codelistVO.setLastUpdateTimestamp(current_stamp);
		    	dao.insertObject(codelistVO);
		    }
		}
		System.out.println("###END#####");
	}
	
	public int getUserID(String userName) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("User");
    	QueryCondition qc = new QueryCondition();
		qc.add("User_Name", new String(userName));
		UserVO userVO = (UserVO)dao.findObject(qc);
		int id = userVO.getUserID();
		return id;
	}
	
	public void codeListValue(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("CodeList") + fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeListValue");
		CodeListValueVO codelistvalueVO = new CodeListValueVO();

		NodeList result = xh.getNodeList("//xsd:simpleType");
		NodeList enumeration, typenode;
		
		DAOFactory df2 = DAOFactory.getDAOFactory();
		
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
	    	if(tmp.getAttribute("name").endsWith("CodeContentType")) {
				
	    		typenode = xh.getNodeList("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:enumeration");
		    	SRTDAO dao2 = df2.getDAO("CodeList");
			   	QueryCondition qc = new QueryCondition();
				qc.add("Code_List_GUID", new String(tmp.getAttribute("id")));
				CodeListVO codelistVO = (CodeListVO)dao2.findObject(qc);
	    		
	    		for(int j = 0; j < result.getLength(); j++) {
		    		Element tmp2 = (Element)result.item(j);
	    			if(tmp2.getAttribute("name").endsWith("EnumerationType")) {
	    				if(tmp2.getAttribute("name").substring(0, tmp2.getAttribute("name").lastIndexOf("EnumerationType")).equals(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")))) {
	    			    	
	    		    		if(typenode.getLength()==0) {
		    					enumeration = xh.getNodeList("//xsd:simpleType[@name = '" + tmp2.getAttribute("name") + "']//xsd:enumeration");
	    		    		}
	    		    		else {
	    		    			enumeration = typenode;	    		    			
	    		    		}

	    			    	for(int k = 0; k < enumeration.getLength(); k++) {
	    			    		Element tmp3 = (Element)enumeration.item(k);
	    						codelistvalueVO.setOwnerCodeListID(codelistVO.getCodeListID());
	    			    		codelistvalueVO.setValue(tmp3.getAttribute("value"));
	    			    		codelistvalueVO.setName(tmp3.getAttribute("value"));//It will be edited
	    			    		codelistvalueVO.setUsedIndicator(true);
	    			    		codelistvalueVO.setLockedIndicator(false);
		    			    	Node name_node = xh.getNode("//xsd:simpleType[@name = '" + tmp2.getAttribute("name") + "']//xsd:enumeration[@value = '" + tmp3.getAttribute("value") + "']//xsd:documentation");	
				    			if(name_node != null) {
			    			    	Element name_element = (Element)name_node;
			    			    	codelistvalueVO.setDefinition(name_element.getTextContent());
			    			    	codelistvalueVO.setDefinitionSource(name_element.getAttribute("source"));
					    		}
				    			
				    			else {
				    				codelistvalueVO.setDefinition("");
				    				codelistvalueVO.setDefinitionSource("");
				    			}
				    		dao.insertObject(codelistvalueVO);
	    			    	}
		    				break;
		    			}
		    		}	
		    	}
	    	}
		}
		System.out.println(codelistvalueVO.getValue());
		System.out.println("###END#####");
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateCodeList codelist = new PopulateCodeList();
		String tt[][] = {{"CodeLists_1","314"}, {"CodeList_ConditionTypeCode_1","314"}, {"CodeList_ConstraintTypeCode_1","314"}, 
				{"CodeList_DateFormatCode_1","314"}, {"CodeList_DateTimeFormatCode_1","314"}, {"CodeList_TimeFormatCode_1","314"},
				{"CodeList_CharacterSetCode_IANA_20070514", "379"}, {"CodeList_MIMEMediaTypeCode_IANA_7_04","379"}, 
				{"CodeList_CurrencyCode_ISO_7_04","5"}, {"CodeList_LanguageCode_ISO_7_04", "5"}, {"CodeList_TimeZoneCode_1", "5"},
				{"CodeList_UnitCode_UNECE_7_04", "6"}};
		for(int i = 0; i< tt.length; i++) {
			String filename = tt[i][0] + ".xsd";
			codelist.codeList(filename, Integer.parseInt(tt[i][1]));
			codelist.codeListValue(filename);
			System.out.println(filename+" upload is completed..");			
		}

	}
}
