package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.*;

public class ABIEView implements Serializable, Comparable<ABIEView> {

	private ASCCVO asccVO;
	private ASCCPVO asccpVO;
	private ACCVO accVO;
	private ABIEVO abieVO;
	private ASBIEPVO asbiepVO;
	private ASBIEVO asbieVO;
	private BCCVO bccVO;
	private BBIEPVO bbiepVO;
	private BBIEVO bbieVO;
	private BCCPVO bccpVO;
	private DTSCVO dtscVO;
	private BBIE_SCVO bbiescVO;
	private Map<String, Integer> bdtPrimitiveRestrictions = new HashMap<String, Integer>();
	private Map<String, Integer> bdtscPrimitiveRestrictions = new HashMap<String, Integer>();
	private String bdtName;
	private String bdtscName;
	private BDTPrimitiveRestrictionVO bdtPrimitiveRestrictionVO;
    private String name;
    private int id;
    private String color;
    private String type;
    private String primitiveType;
    private int bdtPrimitiveRestrictionId;
	private int bdtscPrimitiveRestrictionId;
    private int codeListId;
    private String restrictionType;

	public ABIEView() {
		
	}
	
	public int getCodeListId() {
		return codeListId;
	}

	public void setCodeListId(int codeListId) {
		this.codeListId = codeListId;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public void onBDTPrimitiveChange() {
		System.out.println(bdtPrimitiveRestrictionId);
	}
	
	public int getBdtPrimitiveRestrictionId() {
		return bdtPrimitiveRestrictionId;
	}

	public void setBdtPrimitiveRestrictionId(int bdtPrimitiveRestrictionId) {
		this.bdtPrimitiveRestrictionId = bdtPrimitiveRestrictionId;
	}

	public void onBDTSCPrimitiveChange() {
		System.out.println(bdtscPrimitiveRestrictionId);
	}

	public int getBdtscPrimitiveRestrictionId() {
		return bdtscPrimitiveRestrictionId;
	}

	public void setBdtscPrimitiveRestrictionId(int bdtscPrimitiveRestrictionId) {
		this.bdtscPrimitiveRestrictionId = bdtscPrimitiveRestrictionId;
	}

	public BDTPrimitiveRestrictionVO getBdtPrimitiveRestrictionVO() {
		return bdtPrimitiveRestrictionVO;
	}

	public void setBdtPrimitiveRestrictionVO(
			BDTPrimitiveRestrictionVO bdtPrimitiveRestrictionVO) {
		this.bdtPrimitiveRestrictionVO = bdtPrimitiveRestrictionVO;
	}

	public String getPrimitiveType() {
		return primitiveType;
	}

	public void setPrimitiveType(String primitiveType) {
		this.primitiveType = primitiveType;
	}

	public Map<String, Integer> getBdtPrimitiveRestrictions() {
		try {
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO bdtPrimitiveRestrictionDao = df.getDAO("BDTPrimitiveRestriction");
			
			QueryCondition qc_02 = new QueryCondition();
			qc_02.add("bdt_id", bccpVO.getBDTID());
			
			List<SRTObject> ccs = bdtPrimitiveRestrictionDao.findObjects(qc_02);
			bdtPrimitiveRestrictionId = ((BDTPrimitiveRestrictionVO)ccs.get(0)).getBDTPrimitiveRestrictionID();
			for(SRTObject obj : ccs) {
				BDTPrimitiveRestrictionVO cc = (BDTPrimitiveRestrictionVO)obj;
				
				if(cc.getCDTPrimitiveExpressionTypeMapID() > 0) {
					primitiveType = "XSD Builtin Type";
					
					SRTDAO cdtAllowedPrimitiveExpressionTypeMapDao = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
					QueryCondition qc_03 = new QueryCondition();
					qc_03.add("cdt_awd_pri_xps_type_map_id", cc.getCDTPrimitiveExpressionTypeMapID());
					CDTAllowedPrimitiveExpressionTypeMapVO vo = (CDTAllowedPrimitiveExpressionTypeMapVO)cdtAllowedPrimitiveExpressionTypeMapDao.findObject(qc_03);
					
					SRTDAO xsdBuiltInTypeDao = df.getDAO("XSDBuiltInType");
					QueryCondition qc_04 = new QueryCondition();
					qc_04.add("xbt_id", vo.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO xbt = (XSDBuiltInTypeVO)xsdBuiltInTypeDao.findObject(qc_04);
					bdtPrimitiveRestrictions.put(xbt.getName(), cc.getBDTPrimitiveRestrictionID());
				} else {
					primitiveType = "Code List";
					
					SRTDAO codeListDao = df.getDAO("CodeList");
					QueryCondition qc_04 = new QueryCondition();
					qc_04.add("code_list_id", cc.getCodeListID());
					CodeListVO code = (CodeListVO)codeListDao.findObject(qc_04);
					bdtPrimitiveRestrictions.put(code.getName(), cc.getBDTPrimitiveRestrictionID());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bdtPrimitiveRestrictions;
	}
	
	public void setBdtPrimitiveRestrictions(Map<String, Integer> bdtPrimitiveRestrictions) {
		this.bdtPrimitiveRestrictions = bdtPrimitiveRestrictions;
	}

	public Map<String, Integer> getBdtscPrimitiveRestrictions() {
		try {
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO bdtscPrimitiveRestrictionDao = df.getDAO("BDTSCPrimitiveRestriction");

			QueryCondition qc_02 = new QueryCondition();
			qc_02.add("bdt_sc_id", dtscVO.getDTSCID());

			List<SRTObject> ccs = bdtscPrimitiveRestrictionDao.findObjects(qc_02);
			bdtscPrimitiveRestrictionId = ((BDTSCPrimitiveRestrictionVO)ccs.get(0)).getBDTSCPrimitiveRestrictionID();
			for(SRTObject obj : ccs) {
				BDTSCPrimitiveRestrictionVO cc = (BDTSCPrimitiveRestrictionVO)obj;

				if(cc.getCDTSCAllowedPrimitiveExpressionTypeMapID() > 0) {
					primitiveType = "XSD Builtin Type";

					SRTDAO cdtscAllowedPrimitiveExpressionTypeMapDao = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
					QueryCondition qc_03 = new QueryCondition();
					qc_03.add("cdt_sc_awd_pri_xps_type_map_id", cc.getCDTSCAllowedPrimitiveExpressionTypeMapID());
					CDTSCAllowedPrimitiveExpressionTypeMapVO vo = (CDTSCAllowedPrimitiveExpressionTypeMapVO)cdtscAllowedPrimitiveExpressionTypeMapDao.findObject(qc_03);

					SRTDAO xsdBuiltInTypeDao = df.getDAO("XSDBuiltInType");
					QueryCondition qc_04 = new QueryCondition();
					qc_04.add("xbt_id", vo.getXSDBuiltInTypeID());
					XSDBuiltInTypeVO xbt = (XSDBuiltInTypeVO)xsdBuiltInTypeDao.findObject(qc_04);
					bdtscPrimitiveRestrictions.put(xbt.getName(), cc.getBDTSCPrimitiveRestrictionID());
				} else {
					primitiveType = "Code List";

					SRTDAO codeListDao = df.getDAO("CodeList");
					QueryCondition qc_04 = new QueryCondition();
					qc_04.add("code_list_id", cc.getCodeListID());
					CodeListVO code = (CodeListVO)codeListDao.findObject(qc_04);
					bdtscPrimitiveRestrictions.put(code.getName(), cc.getBDTSCPrimitiveRestrictionID());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bdtscPrimitiveRestrictions;
	}

	public void setBdtscPrimitiveRestrictions(Map<String, Integer> bdtscPrimitiveRestrictions) {
		this.bdtscPrimitiveRestrictions = bdtscPrimitiveRestrictions;
	}

	public String getBdtName() {
		return bdtName;
	}

	public void setBdtName(String bdtName) {
		this.bdtName = bdtName;
	}

	public String getBdtscName() {
		return bdtscName;
	}

	public void setBdtscName(String bdtscName) {
		this.bdtscName = bdtscName;
	}

	public ABIEView(String name, int id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

	public ASBIEVO getAsbieVO() {
		return asbieVO;
	}

	public void setAsbieVO(ASBIEVO asbieVO) {
		this.asbieVO = asbieVO;
	}

	public ASCCVO getAsccVO() {
		return asccVO;
	}

	public void setAsccVO(ASCCVO asccVO) {
		this.asccVO = asccVO;
	}

	public ASCCPVO getAsccpVO() {
		return asccpVO;
	}

	public void setAsccpVO(ASCCPVO asccpVO) {
		this.asccpVO = asccpVO;
	}

	public ACCVO getAccVO() {
		return accVO;
	}

	public void setAccVO(ACCVO accVO) {
		this.accVO = accVO;
	}

	public ABIEVO getAbieVO() {
		return abieVO;
	}

	public void setAbieVO(ABIEVO abieVO) {
		this.abieVO = abieVO;
	}

	public ASBIEPVO getAsbiepVO() {
		return asbiepVO;
	}

	public void setAsbiepVO(ASBIEPVO asbiepVO) {
		this.asbiepVO = asbiepVO;
	}

	public BCCVO getBccVO() {
		return bccVO;
	}

	public void setBccVO(BCCVO bccVO) {
		this.bccVO = bccVO;
	}

	public BBIEPVO getBbiepVO() {
		return bbiepVO;
	}

	public void setBbiepVO(BBIEPVO bbiepVO) {
		this.bbiepVO = bbiepVO;
	}

	public BBIEVO getBbieVO() {
		return bbieVO;
	}

	public void setBbieVO(BBIEVO bbieVO) {
		this.bbieVO = bbieVO;
	}

	public BCCPVO getBccpVO() {
		return bccpVO;
	}

	public void setBccpVO(BCCPVO bccpVO) {
		this.bccpVO = bccpVO;
	}

	public DTSCVO getDtscVO() {
		return dtscVO;
	}

	public void setDtscVO(DTSCVO dtscVO) {
		this.dtscVO = dtscVO;
	}

	public BBIE_SCVO getBbiescVO() {
		return bbiescVO;
	}

	public void setBbiescVO(BBIE_SCVO bbiescVO) {
		this.bbiescVO = bbiescVO;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getType() {
        return type;
    }
 
    public void setType(String type) {
        this.type = type;
    }
 
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	//Eclipse Generated hashCode and equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((String.valueOf(id) == null) ? 0 : String.valueOf(id).hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ABIEView other = (ABIEView) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (id <= 0) {
            if (other.id > 0)
                return false;
        } else if (id != other.id)
            return false;
        return true;
    }
 
    @Override
    public String toString() {
        return name;
    }
 
    public int compareTo(ABIEView document) {
        return this.getName().compareTo(document.getName());
    }
} 
