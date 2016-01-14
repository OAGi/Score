package org.oagi.srt.persistence.validate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CodeListTest {
	
	private static Connection conn = null;
	
	public void validate(String path1) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, SRTDAOException, XPathExpressionException{
		
		XPathHandler xh = new XPathHandler(path1);
		NodeList codelist = xh.getNodeList("//xsd:simpleType");
		
		for(int i = 0; i < codelist.getLength(); i++) {
			Element content = (Element)codelist.item(i);
			if(content.getAttribute("name").endsWith("CodeContentType")){
				String contentTypeGuid = content.getAttribute("id");
				String name = content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("ContentType"));
				String enumTypeGuid = null;
				NodeList enumList = null;
				for(int j = 0 ; j < codelist.getLength(); j++) {
					Element enumType = (Element)codelist.item(j);
					if((content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType"))+"EnumerationType").equalsIgnoreCase(enumType.getAttribute("name")) || 
							(content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType"))+"CodeEnumerationType").equalsIgnoreCase(enumType.getAttribute("name"))
							){
						enumTypeGuid = enumType.getAttribute("id");
						enumList = xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType"))+"EnumerationType or CodeEnumerationType" + "']/xsd:restriction/xsd:enumeration");
						break;
					}
					else if(xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name") +"']/xsd:restriction/xsd:enumeration") != null) {
						enumTypeGuid = enumType.getAttribute("id");
						enumList = xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name") +"']/xsd:restriction/xsd:enumeration");
						break;
					}
						
				}
				
				if(contentTypeGuid == null || enumTypeGuid == null || name == null) {
					System.out.println("### Codelist Error : contentTypeGuid = "+contentTypeGuid+"  enum_type_guid = "+enumTypeGuid+"  name = "+name);
					continue;
				}
				
				int codelist_id = getCodeListID(contentTypeGuid, enumTypeGuid, name);
				for(int j = 0 ; j < enumList.getLength(); j ++) {
					Element codelistvalue = (Element)enumList.item(j);
					int codelistvalue_id = getCodeListValueID(codelist_id, codelistvalue.getAttribute("value"));
					if(codelist_id == 0 || codelistvalue_id == 0)
						System.out.println("### Codelist value Error : code_list_id = "+codelist_id+"  enum_type_guid = "+enumTypeGuid+"  name = "+name+"  enum_value = "+codelistvalue.getAttribute("value"));
				}
			}
		}
	}
	
	public int getCodeListID(String codelistGuid, String enumTypeguid, String name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeList");
    	QueryCondition qc = new QueryCondition();
		qc.add("guid", codelistGuid);
		qc.add("enum_type_guid", enumTypeguid);
		qc.add("name", name);
		int id = 0;
		if((CodeListVO) dao.findObject(qc, conn) != null)
			id = ((CodeListVO) dao.findObject(qc, conn)).getCodeListID();
		return id;
	}
	
	public int getCodeListValueID(int codelist_id, String value) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CodeListValue");
    	QueryCondition qc = new QueryCondition();
		qc.add("code_list_id", codelist_id);
		qc.add("value", value);
		CodeListValueVO codelistvalueVO = (CodeListValueVO) dao.findObject(qc);
		int id = 0;
		if(codelistvalueVO != null)
			id = codelistvalueVO.getCodeListValueID();
		return id;
	}
	
	public void run() throws Exception {
		System.out.println("### Codelist validation Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		String tt[][] = {{"CodeLists_1","314"}, {"CodeList_ConditionTypeCode_1","314"}, {"CodeList_ConstraintTypeCode_1","314"}, 
				{"CodeList_DateFormatCode_1","314"}, {"CodeList_DateTimeFormatCode_1","314"}, {"CodeList_TimeFormatCode_1","314"},
				{"CodeList_CharacterSetCode_IANA_20070514", "379"}, {"CodeList_MIMEMediaTypeCode_IANA_7_04","379"}, 
				{"CodeList_CurrencyCode_ISO_7_04","5"}, {"CodeList_LanguageCode_ISO_7_04", "5"}, {"CodeList_TimeZoneCode_1", "5"},
				{"CodeList_UnitCode_UNECE_7_04", "6"}};
		for(int i = 0; i< tt.length; i++) {
			String path = SRTConstants.filepath("CodeList")+tt[i][0]+".xsd";
			System.out.println("#### "+tt[i][0]+" ing..");
			validate(path);
		}
		
		tx.close();
		conn.close();
		System.out.println("### Codelist validation End");
		
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		CodeListTest p = new CodeListTest();
		p.run();
	}
	
}
