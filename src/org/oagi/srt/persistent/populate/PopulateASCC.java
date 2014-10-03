package org.oagi.srt.persistent.populate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASCCVO;
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

public class PopulateASCC {
    int seq = 0;
    
	public void ascc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("BOD")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory acc = DAOFactory.getDAOFactory();
		SRTDAO dao = acc.getDAO("ASCC");
		ASCCVO asccVO = new ASCCVO();

		NodeList result = xh.getNodeList("//xsd:element");
		Node tmpParent = null;
		

		for(int i = 0; i < result.getLength(); i++) {
		    Element tmp = (Element)result.item(i);
		    asccVO.setASCCGUID(tmp.getAttribute("id"));
		    if(tmp.getAttribute("minOccurs").isEmpty()){
		    	asccVO.setCardinalityMin(1);
		    }
		    else {
		    	asccVO.setCardinalityMin(Integer.parseInt(tmp.getAttribute("minOccurs")));
		    }
		    
		    if(tmp.getAttribute("maxOccurs").isEmpty()){
		    	asccVO.setCardinalityMax(1);
		    }
		    else if(tmp.getAttribute("maxOccurs").equals("unbounded")){
		    	
		    }
		    else {
		    	asccVO.setCardinalityMax(Integer.parseInt(tmp.getAttribute("maxOccurs")));
		    }
		    
		    if(tmp.getParentNode() != tmpParent){
		    	seq++;
			    tmpParent = tmp.getParentNode();
		    }
	    	asccVO.setSequencingKey(seq);

	    	asccVO.setAssocFromACCID(0);
	    	asccVO.setAssocToASCCPID(0);
		    Node definition = xh.getNode("//xsd:element[@id = '" + asccVO.getASCCGUID() + "']//xsd:annotation//xsd:documentation");
		    Element definition_element = (Element)definition;
		    if(definition_element != null) {
		    	asccVO.setDefinition(definition.getTextContent());
		    }
		    else {
		    	asccVO.setDefinition("");
		    }
	    	
	    //dao.insertObject(asccVO);
	    }
		System.out.println("###"+fileinput+" is ended#####");
	}
	
	private String first(String den) {
		return den.substring(0, den.indexOf(".")).replace("_", " ");
	}
	

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		PopulateASCC ascc = new PopulateASCC();
		
		String path1 = SRTConstants.filepath("BOD");
		File path = new File(path1);
		String files[] = path.list();
		for(int i = 0; i < 1/*files.length*/; i++){
			System.out.println(files[i]+" is proceeded...");
			ascc.ascc(files[i]);
		}
	}
}
