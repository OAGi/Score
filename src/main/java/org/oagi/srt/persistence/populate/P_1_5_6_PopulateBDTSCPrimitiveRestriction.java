package org.oagi.srt.persistence.populate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class P_1_5_6_PopulateBDTSCPrimitiveRestriction {
	
	private static Connection conn = null;
	
	public int getAgencyListID() throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
    	QueryCondition qc = new QueryCondition();
		qc.add("name", "Agency Identification");
		AgencyIDListVO agencyidlistVO = (AgencyIDListVO)dao.findObject(qc);
		return agencyidlistVO.getAgencyIDListID();
	}

	public void run() throws Exception {
		XPathHandler xh = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xh2 = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		System.out.println("### 1.5.6 Start");
		populateBDTSCPrimitiveRestriction(xh, xh2, true);
		System.out.println("### 1.5.6 End");
	}
	
	public void populateBDTSCPrimitiveRestriction(XPathHandler xh, XPathHandler xh2, boolean is_fields_xsd) throws BfPersistenceException, SRTDAOException, XPathExpressionException, SQLException {
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
		SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aCodeListDAO = df.getDAO("CodeList");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");
		
		ArrayList<SRTObject> al = dao.findObjects(conn);
		ArrayList<SRTObject> al_meta = new ArrayList<SRTObject>();
		if(is_fields_xsd){
			
		}
		else {
			for(SRTObject aSRTObject : al){
				DTSCVO aDTSCVO = (DTSCVO)aSRTObject;
				QueryCondition qc_01 = new QueryCondition();
				qc_01.add("dt_id", aDTSCVO.getOwnerDTID());
				DTVO dtVO = (DTVO) aDTDAO.findObject(qc_01, conn);
				
				String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"} ;
				for(int k = 0 ; k < metalist.length; k++){
					if(dtVO.getDEN().equalsIgnoreCase(Utility.typeToDen(metalist[k])))
						al_meta.add(k, aDTSCVO);
				}
			}
			al = new ArrayList<SRTObject>();
			al = (ArrayList<SRTObject>) al_meta.clone();
		}
			
		for(SRTObject aSRTObject : al) {
			DTSCVO aDTSCVO = (DTSCVO)aSRTObject;
			String tmp_guid = null;
			if(isBDTSC(aDTSCVO.getOwnerDTID())) {
				
				Node result = xh.getNode("//xsd:attribute[@id='" + aDTSCVO.getDTSCGUID() + "']");
				tmp_guid = aDTSCVO.getDTSCGUID();
				if(result == null) { // if result is null, then look up its based default BDT and get guid
					result = xh2.getNode("//xsd:attribute[@id='" + aDTSCVO.getDTSCGUID() + "']");
					tmp_guid = aDTSCVO.getDTSCGUID();

					if(aDTSCVO.getBasedDTSCID() != 0 && result == null){
						DTSCVO dtscVO = getDTSC(aDTSCVO.getBasedDTSCID());
						result = xh.getNode("//xsd:attribute[@id='" + dtscVO.getDTSCGUID() + "']");
						tmp_guid = dtscVO.getDTSCGUID();
						if(result == null){
							result = xh2.getNode("//xsd:attribute[@id='" + dtscVO.getDTSCGUID() + "']");
							tmp_guid = dtscVO.getDTSCGUID();
						}
					}
				}

				Element ele = (Element)result;
				int codeListId = 0;
				QueryCondition qc_00 = new QueryCondition();
				String ele_name = "";
				
				if(ele != null && ele.getAttribute("type") != null) {
					qc_00.add("name", ele.getAttribute("type").replaceAll("ContentType", ""));
					codeListId = ((CodeListVO)aCodeListDAO.findObject(qc_00, conn)).getCodeListID();
					ele_name = ele.getAttribute("name");
				}
				
				if((aDTSCVO.getRepresentationTerm().contains("Code") && codeListId > 0) || ele_name.contains("AgencyID")) { 
					
					BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
					bVO.setBDTSCID(aDTSCVO.getDTSCID());
					bVO.setisDefault(false);
					
					if(ele.getAttribute("name").contains("AgencyID")) {
						bVO.setAgencyIDListID(getAgencyListID());
					} else {
						bVO.setCodeListID(codeListId);
					}
					System.out.println("Populating bdt sc primitive restriction for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" code list id = "+bVO.getCodeListID()+" agency id list id = "+bVO.getAgencyIDListID()+ " is default = "+bVO.getisDefault());
					aBDTSCPrimitiveRestrictionDAO.insertObject(bVO);
					
					QueryCondition qc01 = new QueryCondition();
					qc01.add("name", "Token");
					int CDT_Primitive_id = ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc01, conn)).getCDTPrimitiveID();
					
					QueryCondition qc02 = new QueryCondition();
					qc02.add("builtin_type", "xsd:token");
					int xbt_id = ((XSDBuiltInTypeVO)aXBTDAO.findObject(qc02, conn)).getXSDBuiltInTypeID();
					
					
					QueryCondition qc021 = new QueryCondition();
					qc021.add("dt_sc_id", aDTSCVO.getBasedDTSCID());
					DTSCVO stscVO = (DTSCVO)dao.findObject(qc021, conn);
					int cdt_id = 0;
					if(stscVO == null){ //New
						QueryCondition qc031 = new QueryCondition();
						qc031.add("name", ele.getAttribute("type").substring(0, ele.getAttribute("type").lastIndexOf("ContentType")));
						CodeListVO codelistVO = (CodeListVO)aCodeListDAO.findObject(qc031, conn);
						BDTSCPrimitiveRestrictionVO bVO1 = new BDTSCPrimitiveRestrictionVO();
						bVO1.setBDTSCID(aDTSCVO.getDTSCID());
						bVO1.setCodeListID(codelistVO.getCodeListID());
						bVO1.setisDefault(false);
						System.out.println("Populating bdt sc primitive restriction for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" code list id = "+bVO1.getCodeListID()+" code list name = "+codelistVO.getName()+"  is default = "+bVO1.getisDefault());
						aBDTSCPrimitiveRestrictionDAO.insertObject(bVO1);
					}
					else {
						if(stscVO.getBasedDTSCID() < 1)
							cdt_id = aDTSCVO.getBasedDTSCID();
						else
							cdt_id = stscVO.getBasedDTSCID();
						
						QueryCondition qc03 = new QueryCondition();
						qc03.add("cdt_sc_id", cdt_id); 
						qc03.add("CDT_Pri_id", CDT_Primitive_id);
						int cdt_sc_awd_pri_id = ((CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitiveDAO.findObject(qc03, conn)).getCDTSCAllowedPrimitiveID();
						
						QueryCondition qc04 = new QueryCondition();
						qc04.add("CDT_SC_awd_pri", cdt_sc_awd_pri_id);
						qc04.add("xbt_id", xbt_id);
						int CDTSCAllowedPrimitiveExpressionTypeMapID = ((CDTSCAllowedPrimitiveExpressionTypeMapVO)aCDTSCAllowedPrimitiveExpressionTypeMapDAO.findObject(qc04, conn)).getCTSCAllowedPrimitiveExpressionTypeMapID();
						
						BDTSCPrimitiveRestrictionVO bVO1 = new BDTSCPrimitiveRestrictionVO();
						bVO1.setBDTSCID(aDTSCVO.getDTSCID());
						
						bVO1.setCDTSCAllowedPrimitiveExpressionTypeMapID(CDTSCAllowedPrimitiveExpressionTypeMapID);
						bVO1.setisDefault(true); 
						System.out.println("Populating bdt sc primitive restriction for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" cdt_sc_allowed_pri_xps_type_map_id = "+bVO1.getCDTSCAllowedPrimitiveExpressionTypeMapID()+"  is default = "+bVO1.getisDefault());
						aBDTSCPrimitiveRestrictionDAO.insertObject(bVO1);
					}
				} else {
					ArrayList<SRTObject> al3 = new ArrayList<SRTObject>();
					if(aDTSCVO.getBasedDTSCID() < 1) { //new attributes SC
						QueryCondition qc = new QueryCondition();
						qc.add("cdt_sc_id", aDTSCVO.getDTSCID());
						al3 = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
					}
					else { // DTSC is based on other dt_sc
						QueryCondition qc = new QueryCondition();
						qc.add("cdt_sc_id", aDTSCVO.getBasedDTSCID());
						
						al3 = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
						if(al3.size() < 1) {
							QueryCondition qc4 = new QueryCondition();
							qc4.add("dt_sc_id", aDTSCVO.getBasedDTSCID());
							
							qc = new QueryCondition();
							qc.add("cdt_sc_id", ((DTSCVO)dao.findObject(qc4, conn)).getBasedDTSCID());
							
							al3 = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
						}
					}
					for(SRTObject aSRTObject3 : al3) {//Loop retrieved cdt_sc_awd_pri
						CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aSRTObject3;
						
						QueryCondition qc1 = new QueryCondition();
						qc1.add("cdt_sc_awd_pri", aCDTSCAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						ArrayList<SRTObject> al4 = aCDTSCAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc1, conn);
						for(SRTObject aSRTObject4 : al4) {
							CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
							
							BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
							bVO.setBDTSCID(aDTSCVO.getDTSCID());
							
							bVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(aCDTSCAllowedPrimitiveExVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
							
							
							XSDBuiltInTypeVO xbtVO = getXSDBuiltInTypeID(aCDTSCAllowedPrimitiveExVO.getXSDBuiltInTypeID());
							String xdtName = xbtVO.getBuiltInType();
							
							int cdtPrimitiveId = aCDTSCAllowedPrimitiveVO.getCDTPrimitiveID();
							
							String representationTerm = aDTSCVO.getRepresentationTerm();
							if(representationTerm.equalsIgnoreCase("Code") && xdtName.equalsIgnoreCase("xsd:token") && "Token".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Identifier") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Name") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Indicator") && xdtName.equalsIgnoreCase("xsd:boolean") && "Boolean".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Value") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Text") && xdtName.equalsIgnoreCase("xsd:normalizedString") && "NormalizedString".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Date Time") && xdtName.equalsIgnoreCase("xsd:token") && "Timepoint".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else {
								bVO.setisDefault(false); 
							}
							
							//exceptional case
							if(ele != null && ele.getAttribute("type") != null && ele.getAttribute("type").equalsIgnoreCase("NumberType_B98233") ){
								if(representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:integer") && "Integer".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))){
									bVO.setisDefault(true); 
									System.out.println("Populating bdt sc primitive restriction(NumberType_B98233) for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" cdt_sc_allowed_pri_xps_type_map_id = "+bVO.getCDTSCAllowedPrimitiveExpressionTypeMapID()+"  is default = "+bVO.getisDefault());
								}
								else if(representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))){
									bVO.setisDefault(false); 
									System.out.println("Populating bdt sc primitive restriction(NumberType_B98233) for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" cdt_sc_allowed_pri_xps_type_map_id = "+bVO.getCDTSCAllowedPrimitiveExpressionTypeMapID()+"  is default = "+bVO.getisDefault());
								}
							}
							//System.out.println("representation term = "+representationTerm+" xbt name = "+xdtName+" cdt primitive name = "+getCDTPrimitiveName(cdtPrimitiveId));
							System.out.println("Populating bdt sc primitive restriction for bdt sc = "+aDTSCVO.getPropertyTerm()+aDTSCVO.getRepresentationTerm()+" owner dt den = "+getDEN(aDTSCVO.getOwnerDTID())+" cdt_sc_allowed_pri_xps_type_map_id = "+bVO.getCDTSCAllowedPrimitiveExpressionTypeMapID()+"  is default = "+bVO.getisDefault());
							aBDTSCPrimitiveRestrictionDAO.insertObject(bVO);
						}
					}
				}
			}
		}
		
		tx.close();
		conn.close();
	}
	public DTSCVO getDTSC(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("dt_sc_id",  id);
		return (DTSCVO)aDTDAO.findObject(qc, conn);
	}
	
	public String getDEN (int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("dt_id",  id);
		return ((DTVO)aDTDAO.findObject(qc, conn)).getDEN();
	}
	
	public boolean isBDTSC (int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
    	QueryCondition qc = new QueryCondition();
		qc.add("dt_id",  id);
		
		DTVO tmp = (DTVO)aDTDAO.findObject(qc, conn);
		
		if(tmp!=null && tmp.getDTType() == 1)
			return true;
		return false;
	}
	
	public String getCDTPrimitiveName(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("cdt_pri_id",  id);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getName();
	}
	
	public XSDBuiltInTypeVO getXSDBuiltInTypeID(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("xbt_id", id);
		return (XSDBuiltInTypeVO)aXSDBuiltInTypeDAO.findObject(qc);
	}

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		P_1_5_6_PopulateBDTSCPrimitiveRestriction p = new P_1_5_6_PopulateBDTSCPrimitiveRestriction();
		p.run();
	}
}