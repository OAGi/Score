package org.oagi.srt.persistent.populate;

import java.io.File;
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
import org.oagi.srt.persistence.dto.ASCCPVO;
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

public class PopulateASCCP {
	public void asccp(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BOD")+ fileinput;
				
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory asccp = DAOFactory.getDAOFactory();
		SRTDAO dao = asccp.getDAO("ASCCP");
		ASCCPVO asccpVO = new ASCCPVO();

		NodeList result = xh.getNodeList("//xsd:element");
		NodeList complexType = xh.getNodeList("//xsd:complexType");

		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
		    int go = 0;
		    for (int j = 0; j < complexType.getLength(); j++) {
		    	Element tmp_complex = (Element)complexType.item(j);
	    		Node simpleContent = xh.getNode("//xsd:complexType[@name = '" + tmp_complex.getAttribute("name") + "']//xsd:simpleContent");
		    	if(tmp.getAttribute("type").equals(tmp_complex.getAttribute("name")) && simpleContent==null ) {
		    		go = 1;
		    		break;
		    	}			    
		    }
		    
		    if(go == 0) { 
		    	break;
		    }
		    
		    asccpVO.setASCCPGUID(tmp.getAttribute("id"));
		    asccpVO.setPropertyTerm(spaceSeparator(tmp.getAttribute("name")));
		    Node definition = xh.getNode("//xsd:element[@id = '" + asccpVO.getASCCPGUID() + "']//xsd:annotation//xsd:documentation");
		    Element definition_element = (Element)definition;
		    if(definition_element != null) {
		    	asccpVO.setDefinition(definition.getTextContent());
		    }
		    else {
		    	asccpVO.setDefinition("");
		    }
		    
		    asccpVO.setRoleOfACCID(0);
		    asccpVO.setDEN(asccpVO.getPropertyTerm()+".");
		    
		    int id = getUserID("oagis");
		    asccpVO.setCreatedByUserId(id);
		    asccpVO.setLastUpdatedByUserId(id);
		    asccpVO.setLastUpdateTimestamp(current_stamp);
		    asccpVO.setState(4);
		    asccpVO.setModule(fileinput.substring(0,fileinput.lastIndexOf(".")));

		    if(tmp.getAttribute("ref").isEmpty()){
		    	asccpVO.setReusableIndicator(true);
		    }
		    else {
		    	asccpVO.setReusableIndicator(false);
		    }

		    dao.insertObject(asccpVO);
		    }
		System.out.println("###"+fileinput+" is ended#####");
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
	
	private String spaceSeparator(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i)) && i != 0) {
				sb.append(" " + str.charAt(i));
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateASCCP asccp = new PopulateASCCP();
		
		String path1 = SRTConstants.filepath("BOD");
		File path = new File(path1);
		String files[] = path.list();
		for(int i = 0; i < 1/*files.length*/; i++){
			System.out.println(files[i]+" is proceeded...");
			asccp.asccp(files[i]);
		}
	}
}
