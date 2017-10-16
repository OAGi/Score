package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.ReleaseStateConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "release")
public class Release implements NamespaceAware, Serializable {

    public static final String SEQUENCE_NAME = "RELEASE_ID_SEQ";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private long releaseId;

    @Column(nullable = false, length = 45)
    private String releaseNum;

    @Lob
    @Column(length = 10 * 1024)
    private String releaseNote;

    @Column(nullable = false)
    private Long namespaceId;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = ReleaseStateConverter.class)
    private ReleaseState state;

    public long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public void setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
    }

    public long getNamespaceId() {
        return (namespaceId != null) ? 0L : namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public ReleaseState getState() {
        return state;
    }

    public void setState(ReleaseState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Release that = (Release) o;

        if (releaseId != 0L && releaseId == that.releaseId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (releaseId ^ (releaseId >>> 32));
        result = 31 * result + (releaseNum != null ? releaseNum.hashCode() : 0);
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0);
        result = 31 * result + (namespaceId != null ? namespaceId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Release{" +
                "releaseId=" + releaseId +
                ", releaseNum='" + releaseNum + '\'' +
                ", releaseNote='" + releaseNote + '\'' +
                ", namespaceId=" + namespaceId +
                '}';
    }
}
