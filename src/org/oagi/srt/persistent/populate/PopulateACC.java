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
import org.oagi.srt.persistence.dto.ACCVO;
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

public class PopulateACC {
	public void acc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BOD")+ fileinput;
				
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory acc = DAOFactory.getDAOFactory();
		SRTDAO dao = acc.getDAO("ACC");
		ACCVO accVO = new ACCVO();

		NodeList result = xh.getNodeList("//xsd:complexType");

		Timestamp current_stamp = new Timestamp (System.currentTimeMillis());
	
		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    
			Node xsd_element = xh.getNode("//xsd:complexType[@id = '" + accVO.getACCGUID() + "']//xsd:element");
			Element element_element = (Element) xsd_element;
		    
			if(element_element != null) {
		    	System.out.println("1");
		    }
		    else {
		    	System.out.println("0");
		    }
			
			
//			if(!element_element.getAttribute("ref").equals("")){
//		    	System.out.println(i+"="+element_element.getAttribute("ref"));
//		    }
//		    else {
//		    	System.out.println("null");
//		    }
		    	
		    
		    accVO.setACCGUID(tmp.getAttribute("id"));
		    accVO.setObjectClassTerm(spaceSeparator(tmp.getAttribute("name").substring(0, tmp.getAttribute("name").indexOf("Type"))));
		    accVO.setDEN(accVO.getObjectClassTerm()+".Details");
		    Node definition = xh.getNode("//xsd:complexType[@id = '" + accVO.getACCGUID() + "']//xsd:annotation//xsd:documentation");
		    Element definition_element = (Element)definition;
		    if(definition_element != null) {
		    	accVO.setDefinition(definition.getTextContent());
		    }
		    else {
		    	accVO.setDefinition("");
		    }
		    
		    
		    Node count = xh.getNode("//xsd:complexType[@id = '" + accVO.getACCGUID() + "']//xsd:extension");
		    Element count_element = (Element)count;
		    int based_acc_id = 0;  
		    if(count_element == null) {
		    	based_acc_id = 0;
		    }
		    else {
		    	
		    	QueryCondition qc = new QueryCondition();
				qc.add("DEN", new String(spaceSeparator(count_element.getAttribute("base").substring(0, count_element.getAttribute("base").indexOf("Type")))));//first?
				ACCVO accVO2 = (ACCVO)dao.findObject(qc);
				if(accVO2 != null) {
					based_acc_id = accVO2.getACCID();
				}
				else {
					based_acc_id = accVO.getACCID();
				}
		    }

		    accVO.setBasedACCID(based_acc_id);
		    accVO.setObjectClassQualifier(null);
		    accVO.setOAGISComponentType(getOAGISComponentType(first(accVO.getDEN())));

		    int id = getUserID("oagis");
		    accVO.setCreatedByUserId(id);
		    accVO.setLastUpdatedByUserId(id);
		    accVO.setLastUpdateTimestamp(current_stamp);
		    accVO.setState(4);
		    accVO.setModule(fileinput.substring(0,fileinput.lastIndexOf(".")));
		    //dao.insertObject(accVO);
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
	
	public int getOAGISComponentType(String DEN) {
		if(DEN.endsWith("Base")){
			return 0;
		}
		else if(DEN.equals("Open User Area") || DEN.equals("Any User Area") || DEN.equals("All Extension") || DEN.endsWith("Extension") ) {
			return 2;
		}
		else if(DEN.endsWith("Group")) {
			return 3;
		}
		else {
			return 1;
		}		
	}
	

	private String spaceSeparator(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i)) && i != 0) {
				if (i > 0 && i < str.length()
						&& Character.isUpperCase(str.charAt(i - 1)))
					if (i < str.length() - 1
							&& Character.isLowerCase(str.charAt(i + 1)))
						sb.append(" " + str.charAt(i));
					else
						sb.append(str.charAt(i));
				else
					sb.append(" " + str.charAt(i));
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}
	
	private String first(String den) {
		return den.substring(0, den.indexOf(".")).replace("_", " ");
	}
	

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateACC acc = new PopulateACC();
		
		String path1 = SRTConstants.filepath("BOD");
		File path = new File(path1);
		String files[] = path.list();
		for(int i = 0; i < 1/*files.length*/; i++){
			System.out.println(files[i]+" is proceeded...");
			acc.acc(files[i]);
		}
	}
}
