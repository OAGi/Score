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
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BBIEPVO;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.BBIE_SCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;

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
	private String bdtName;
	private BDTPrimitiveRestrictionVO bdtPrimitiveRestrictionVO;
    private String name;
    private int id;
    private String color;
    private String type;
    private String primitiveType;
    private int bdtPrimitiveRestrictionId;
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
			// Implicitly declaration. Why does it need to be here?
			// Because of this code, Add 'setBccpVO_BbieVO' method.
			// TODO: Fix me.
			bdtPrimitiveRestrictionId = bbieVO.getBdtPrimitiveRestrictionId();
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

	public String getBdtName() {
		return bdtName;
	}

	public void setBdtName(String bdtName) {
		this.bdtName = bdtName;
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

	private void setBccpVO(BCCPVO bccpVO) {
		this.bccpVO = bccpVO;
	}

	public void setBccpVO_BbieVO(BCCPVO bccpVO, BBIEVO bbieVO) {
		setBccpVO(bccpVO);
		setBbieVO(bbieVO);
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
