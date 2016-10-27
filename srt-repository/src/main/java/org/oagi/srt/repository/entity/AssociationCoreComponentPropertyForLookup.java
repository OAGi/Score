package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.oagi.srt.repository.entity.converter.CoreComponentStateConverter;

import javax.persistence.*;

@Entity
@Table(name = "asccp")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssociationCoreComponentPropertyForLookup {

    public static final String SEQUENCE_NAME = "ASCCP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long asccpId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private String propertyTerm;

    @Column
    private Long roleOfAccId;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = CoreComponentStateConverter.class)
    private CoreComponentState state;

    @Column(nullable = false)
    private boolean reusableIndicator;

    @Column(nullable = false)
    private int revisionNum;

    public long getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(long asccpId) {
        this.asccpId = asccpId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public long getRoleOfAccId() {
        return (roleOfAccId != null) ? roleOfAccId : 0L;
    }

    public void setRoleOfAccId(Long roleOfAccId) {
        this.roleOfAccId = roleOfAccId;
    }

    public CoreComponentState getState() {
        return state;
    }

    public void setState(CoreComponentState state) {
        this.state = state;
    }

    public boolean isReusableIndicator() {
        return reusableIndicator;
    }

    public void setReusableIndicator(boolean reusableIndicator) {
        this.reusableIndicator = reusableIndicator;
    }

    public int getRevisionNum() {
        return revisionNum;
    }

    public void setRevisionNum(int revisionNum) {
        this.revisionNum = revisionNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociationCoreComponentPropertyForLookup that = (AssociationCoreComponentPropertyForLookup) o;

        if (asccpId != that.asccpId) return false;
        if (reusableIndicator != that.reusableIndicator) return false;
        if (revisionNum != that.revisionNum) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (propertyTerm != null ? !propertyTerm.equals(that.propertyTerm) : that.propertyTerm != null) return false;
        if (roleOfAccId != null ? !roleOfAccId.equals(that.roleOfAccId) : that.roleOfAccId != null) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (roleOfAccId != null ? roleOfAccId.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (reusableIndicator ? 1 : 0);
        result = 31 * result + revisionNum;
        return result;
    }

    @Override
    public String toString() {
        return "AssociationCoreComponentPropertyForLookup{" +
                "asccpId=" + asccpId +
                ", guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", roleOfAccId=" + roleOfAccId +
                ", state=" + state +
                ", reusableIndicator=" + reusableIndicator +
                ", revisionNum=" + revisionNum +
                '}';
    }
}