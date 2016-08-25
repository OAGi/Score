package org.oagi.srt.web.handler;

import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;
import org.oagi.srt.repository.XSDBuiltInTypeRepository;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ABIEView implements Serializable, Comparable<ABIEView> {

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    private AssociationCoreComponent ascc;
    private AssociationCoreComponentProperty asccp;
    private AggregateCoreComponent acc;
    private TopLevelAbie topLevelAbie;
    private AggregateBusinessInformationEntity abie;
    private AssociationBusinessInformationEntityProperty asbiep;
    private AssociationBusinessInformationEntity asbie;
    private BasicCoreComponent bcc;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicBusinessInformationEntity bbie;
    private BasicCoreComponentProperty bccp;
    private DataTypeSupplementaryComponent dtsc;
    private BasicBusinessInformationEntitySupplementaryComponent bbiesc;

    private Map<String, Long> bdtPrimitiveRestrictions = new HashMap();
    private String bdtName;
    private String name;
    private long id;
    private String color;
    private String type;
    private String primitiveType;
    private long bdtPrimitiveRestrictionId;
    private long codeListId;
    private String restrictionType;

    public ABIEView() {
    }

    public ABIEView(String name, long id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public long getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(long codeListId) {
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

    public long getBdtPrimitiveRestrictionId() {
        return bdtPrimitiveRestrictionId;
    }

    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId) {
        this.bdtPrimitiveRestrictionId = bdtPrimitiveRestrictionId;
    }

    public String getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(String primitiveType) {
        this.primitiveType = primitiveType;
    }

    public Map<String, Long> getBdtPrimitiveRestrictions() {
        List<BusinessDataTypePrimitiveRestriction> ccs = bdtPriRestriRepository.findByBdtId(bccp.getBdtId());
        // Implicitly declaration. Why does it need to be here?
        // Because of this code, Add 'setBccpVO_BbieVO' method.
        // TODO: Fix me.
        bdtPrimitiveRestrictionId = bbie.getBdtPriRestriId();
        for (BusinessDataTypePrimitiveRestriction cc : ccs) {
            if (cc.getCdtAwdPriXpsTypeMapId() > 0L) {
                primitiveType = "XSD Builtin Type";

                CoreDataTypeAllowedPrimitiveExpressionTypeMap vo =
                        cdtAwdPriXpsTypeMapRepository.findOne(cc.getCdtAwdPriXpsTypeMapId());

                XSDBuiltInType xbt = xbtRepository.findOne(vo.getXbtId());
                bdtPrimitiveRestrictions.put(xbt.getName(), cc.getBdtPriRestriId());
            } else {
                primitiveType = "Code List";

                CodeList code = codeListRepository.findOne(cc.getCodeListId());
                bdtPrimitiveRestrictions.put(code.getName(), cc.getBdtPriRestriId());
            }
        }

        return bdtPrimitiveRestrictions;
    }

    public void setBdtPrimitiveRestrictions(Map<String, Long> bdtPrimitiveRestrictions) {
        this.bdtPrimitiveRestrictions = bdtPrimitiveRestrictions;
    }

    public String getBdtName() {
        return bdtName;
    }

    public void setBdtName(String bdtName) {
        this.bdtName = bdtName;
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

    public TopLevelAbie getTopLevelAbie() {
        return topLevelAbie;
    }

    public void setTopLevelAbie(TopLevelAbie topLevelAbie) {
        this.topLevelAbie = topLevelAbie;
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

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
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
