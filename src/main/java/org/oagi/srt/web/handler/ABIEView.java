package org.oagi.srt.web.handler;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.repository.entity.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ABIEView implements Serializable, Comparable<ABIEView> {

    private AssociationCoreComponent ascc;
    private AssociationCoreComponentProperty asccp;
    private AggregateCoreComponent acc;
    private AggregateBusinessInformationEntity abie;
    private AssociationBusinessInformationEntityProperty asbiep;
    private AssociationBusinessInformationEntity asbie;
    private BasicCoreComponent bcc;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicBusinessInformationEntity bbie;
    private BasicCoreComponentProperty bccp;
    private DataTypeSupplementaryComponent dtsc;
    private BasicBusinessInformationEntitySupplementaryComponent bbiesc;

    private Map<String, Integer> bdtPrimitiveRestrictions = new HashMap<String, Integer>();
    private String bdtName;
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
            qc_02.add("bdt_id", bccp.getBdtId());

            List<SRTObject> ccs = bdtPrimitiveRestrictionDao.findObjects(qc_02);
            // Implicitly declaration. Why does it need to be here?
            // Because of this code, Add 'setBccpVO_BbieVO' method.
            // TODO: Fix me.
            bdtPrimitiveRestrictionId = bbie.getBdtPriRestriId();
            for (SRTObject obj : ccs) {
                BDTPrimitiveRestrictionVO cc = (BDTPrimitiveRestrictionVO) obj;

                if (cc.getCDTPrimitiveExpressionTypeMapID() > 0) {
                    primitiveType = "XSD Builtin Type";

                    SRTDAO cdtAllowedPrimitiveExpressionTypeMapDao = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
                    QueryCondition qc_03 = new QueryCondition();
                    qc_03.add("cdt_awd_pri_xps_type_map_id", cc.getCDTPrimitiveExpressionTypeMapID());
                    CDTAllowedPrimitiveExpressionTypeMapVO vo = (CDTAllowedPrimitiveExpressionTypeMapVO) cdtAllowedPrimitiveExpressionTypeMapDao.findObject(qc_03);

                    SRTDAO xsdBuiltInTypeDao = df.getDAO("XSDBuiltInType");
                    QueryCondition qc_04 = new QueryCondition();
                    qc_04.add("xbt_id", vo.getXSDBuiltInTypeID());
                    XSDBuiltInTypeVO xbt = (XSDBuiltInTypeVO) xsdBuiltInTypeDao.findObject(qc_04);
                    bdtPrimitiveRestrictions.put(xbt.getName(), cc.getBDTPrimitiveRestrictionID());
                } else {
                    primitiveType = "Code List";

                    SRTDAO codeListDao = df.getDAO("CodeList");
                    QueryCondition qc_04 = new QueryCondition();
                    qc_04.add("code_list_id", cc.getCodeListID());
                    CodeListVO code = (CodeListVO) codeListDao.findObject(qc_04);
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

    public AssociationBusinessInformationEntity getAsbie() {
        return asbie;
    }

    public void setAsbie(AssociationBusinessInformationEntity asbie) {
        this.asbie = asbie;
    }

    public AssociationCoreComponent getAscc() {
        return ascc;
    }

    public void setAscc(AssociationCoreComponent ascc) {
        this.ascc = ascc;
    }

    public AssociationCoreComponentProperty getAsccp() {
        return asccp;
    }

    public void setAsccp(AssociationCoreComponentProperty asccp) {
        this.asccp = asccp;
    }

    public AggregateCoreComponent getAcc() {
        return acc;
    }

    public void setAcc(AggregateCoreComponent acc) {
        this.acc = acc;
    }

    public AggregateBusinessInformationEntity getAbie() {
        return abie;
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        this.abie = abie;
    }

    public AssociationBusinessInformationEntityProperty getAsbiep() {
        return asbiep;
    }

    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep) {
        this.asbiep = asbiep;
    }

    public BasicCoreComponent getBcc() {
        return bcc;
    }

    public void setBcc(BasicCoreComponent bcc) {
        this.bcc = bcc;
    }

    public BasicBusinessInformationEntityProperty getBbiep() {
        return bbiep;
    }

    public void setBbiep(BasicBusinessInformationEntityProperty bbiep) {
        this.bbiep = bbiep;
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public BasicCoreComponentProperty getBccp() {
        return bccp;
    }

    private void setBccp(BasicCoreComponentProperty bccp) {
        this.bccp = bccp;
    }

    public void setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(
            BasicCoreComponentProperty basicCoreComponentProperty,
            BasicBusinessInformationEntity basicBusinessInformationEntity) {
        setBccp(basicCoreComponentProperty);
        setBbie(basicBusinessInformationEntity);
    }

    public DataTypeSupplementaryComponent getDtsc() {
        return dtsc;
    }

    public void setDtsc(DataTypeSupplementaryComponent dtsc) {
        this.dtsc = dtsc;
    }

    public BasicBusinessInformationEntitySupplementaryComponent getBbiesc() {
        return bbiesc;
    }

    public void setBbiesc(BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        this.bbiesc = bbiesc;
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
