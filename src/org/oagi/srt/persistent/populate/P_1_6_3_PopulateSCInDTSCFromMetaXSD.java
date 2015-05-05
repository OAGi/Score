package org.oagi.srt.persistent.populate;

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
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
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
public class P_1_6_3_PopulateSCInDTSCFromMetaXSD {
	public void scindt_sc(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		String path1 = SRTConstants.filepath("DT_SC")+ fileinput;
		XPathHandler xh = new XPathHandler(path1);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
		DTSCVO dt_scVO = new DTSCVO();

		NodeList ref = xh.getNodeList("//xsd:complexType[@name = 'ExpressionType' or @name = 'ActionExpressionType' or @name = 'ResponseExpressionType']");
		for(int j = 0; j < ref.getLength(); j++) {
			Element ref_tmp = (Element)ref.item(j);
		    //if(ref_tmp.getAttribute("name").equals("ExpressionType") || ref_tmp.getAttribute("name").equals("ActionExpressionType") || ref_tmp.getAttribute("name").equals("ResponseExpressionType") ) {
				int ownerDTID = getBasedDTID(ref_tmp.getAttribute("id"));
				NodeList result = xh.getNodeList("//xsd:complexType[@id = '" + ref_tmp.getAttribute("id") + "']//xsd:attribute");
				for(int i = 0; i < result.getLength(); i++) {
				    Element tmp = (Element)result.item(i);
				   
				    dt_scVO.setDTSCGUID(tmp.getAttribute("id"));
				    String PropertyTerm = tmp.getAttribute("name").substring(0,1).toUpperCase()+tmp.getAttribute("name").substring(1);

				    if(tmp.getAttribute("name").indexOf("Code") > 0) {
				    	PropertyTerm = PropertyTerm.substring(0, tmp.getAttribute("name").indexOf("Code"));
				    }
				    dt_scVO.setPropertyTerm(Utility.spaceSeparator(PropertyTerm));
				    
				    if(tmp.getAttribute("name").equals("expressionLanguage")){
				    	dt_scVO.setRepresentationTerm("Text");
				    }
				    else if(tmp.getAttribute("name").equals("actionCode")){
				    	dt_scVO.setRepresentationTerm("Code");
				    }
				    else {
				    	
				    }
				    Node definition = xh.getNode("//xsd:complexType//xsd:attribute[@id = '" + dt_scVO.getDTSCGUID() + "']//xsd:annotation//xsd:documentation") ;
				    Element definition_element = (Element)definition;
				    if(definition_element != null) {
				    	dt_scVO.setDefinition(definition.getTextContent());
				    }
				    else {
				    	dt_scVO.setDefinition(null);
				    }
				    dt_scVO.setMinCardinality((getMinCardinality(tmp.getAttribute("use"))));
				    dt_scVO.setMaxCardinality((getMaxCardinality(tmp.getAttribute("use"))));
				    dt_scVO.setBasedDTSCID(0); //Blank 
				    dt_scVO.setOwnerDTID(ownerDTID); 
		
				    if(tmp.getAttribute("name").equals("languageCode")){
				    	dt_scVO.setMaxCardinality(0);
				    	dt_scVO.setBasedDTSCID(getBasedDTSCID("Language"));
				    }
				    dao.insertObject(dt_scVO);
				    }
		    //}
		}
		System.out.println("###END#####");
	}
	
	public int getBasedDTID(String DTGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("DT_GUID", new String(DTGUID));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
	
	public int getBasedDTSCID(String PropertyTerm) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("Property_Term", new String(PropertyTerm));
		DTSCVO dt_SCVO = (DTSCVO)dao.findObject(qc);
		int id = dt_SCVO.getDTSCID();
		return id;
	}
	
	public int getMinCardinality(String use) {
		if (use.equals("optional")) {
			return 0;
		} 
		else if (use.equals("required")) {
			return 1;
		} 
		else if (use.equals("prohibited")) {
			return 0;
		} 
		else {
			return 0;
		}
	}
	public int getMaxCardinality(String use) {
		if (use.equals("optional")) {
			return 1;
		} 
		else if (use.equals("required")) {
			return 1;
		} 
		else if (use.equals("prohibited")) {
			return 0;
		} 
		else {
			return 1;
		}
	}
	
	public void run() throws Exception {
		String filename = "Meta" + ".xsd";
		scindt_sc(filename);
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_3_PopulateSCInDTSCFromMetaXSD scindt_sc = new P_1_6_3_PopulateSCInDTSCFromMetaXSD();
		String filename = "Meta" + ".xsd";
		scindt_sc.scindt_sc(filename);

	}
}
