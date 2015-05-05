package org.oagi.srt.persistent.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @version 1.0
*
*/
public class P_1_6_2_PopulateBDTPrimitiveRestrictionFromMetaXSD {
	public void bdt_primitive_restriction(String fileinput) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("BDTPrimitiveRestriction");
		BDTPrimitiveRestrictionVO bdt_primitive_restrictionVO = new BDTPrimitiveRestrictionVO();
		
	    ArrayList<SRTObject> bdtidlist = new ArrayList<SRTObject>();
	    bdtidlist = getBDTID("TEXT", getTokenID("Text_0F0ZL2. Type"));
	    ArrayList<SRTObject> cdtallowedprimitiveexpressiontypemapidlist = new ArrayList<SRTObject>();
	    int builtintypeid = getBuiltInTypeID("token");
	    cdtallowedprimitiveexpressiontypemapidlist = getCdt_Allowed_Primitive_Expression_Type_MapID(builtintypeid);

	    for(SRTObject dvo : bdtidlist) {
			DTVO svo = (DTVO) dvo;
			for(SRTObject dvo2 : cdtallowedprimitiveexpressiontypemapidlist){
				CDTAllowedPrimitiveExpressionTypeMapVO svo2 = (CDTAllowedPrimitiveExpressionTypeMapVO) dvo2;
				bdt_primitive_restrictionVO.setBDTID(svo.getDTID());
				bdt_primitive_restrictionVO.setCDTPrimitiveExpressionTypeMapID(svo2.getCDTPrimitiveExpressionTypeMapID());
				//bdt_primitive_restrictionVO.setCodeListID(0); BLANK
				if(svo2.getXSDBuiltInTypeID() == builtintypeid) {
					bdt_primitive_restrictionVO.setisDefault(true);
				}
				else {
					bdt_primitive_restrictionVO.setisDefault(false);
				}
			}
		}
	    dao.insertObject(bdt_primitive_restrictionVO);
		System.out.println("###END#####");
	}
	
	public ArrayList<SRTObject> getBDTID(String dataType, int basedDTID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(dataType));
		qc.add("Based_DT_ID", basedDTID);
		ArrayList<SRTObject> bdtidlist = new ArrayList<SRTObject>();
		bdtidlist = dao.findObjects(qc);
		return bdtidlist;
	}
	
	public int getTokenID(String DEN) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("DEN", new String(DEN));
		DTVO dtVO = (DTVO)dao.findObject(qc);
		int id = dtVO.getDTID();
		return id;
	}
		
	public int getBuiltInTypeID(String name) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("Name", new String(name));
		XSDBuiltInTypeVO XSD_BuiltIn_TypeVO = (XSDBuiltInTypeVO)dao.findObject(qc);
		int id = XSD_BuiltIn_TypeVO.getXSDBuiltInTypeID();
		return id;
	}
	
	public ArrayList<SRTObject> getCdt_Allowed_Primitive_Expression_Type_MapID(int xsd_builtin_type_id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
    	QueryCondition qc = new QueryCondition();
		qc.add("XSD_BuiltIn_Type_ID", xsd_builtin_type_id);
		ArrayList<SRTObject> cdtallowedprimitiveexpressiontypemapidlist = new ArrayList<SRTObject>();
		cdtallowedprimitiveexpressiontypemapidlist = dao.findObjects(qc);
		return cdtallowedprimitiveexpressiontypemapidlist;
		
	}
	
	public void run() throws Exception {
		String filename = "Meta" + ".xsd";
		bdt_primitive_restriction(filename);
	}

	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_2_PopulateBDTPrimitiveRestrictionFromMetaXSD bdt_primitive_restriction = new P_1_6_2_PopulateBDTPrimitiveRestrictionFromMetaXSD();
		String filename = "Meta" + ".xsd";
		bdt_primitive_restriction.bdt_primitive_restriction(filename);
	}
}
