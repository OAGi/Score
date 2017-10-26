package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.converter.ReleaseStateConverter;

import javax.persistence.*;
import java.util.Date;

@SqlResultSetMapping(
        name="releases",
        entities = {
                @EntityResult(
                        entityClass = Release.class,
                        fields = {
                                @FieldResult(name = "releaseId", column = "release_id"),
                                @FieldResult(name = "releaseNum", column = "release_num"),
                                @FieldResult(name = "releaseNote", column = "release_note"),
                                @FieldResult(name = "state", column = "state"),
                                @FieldResult(name = "lastUpdateTimestamp", column = "last_update_timestamp"),
                        }
                ),
                @EntityResult(
                        entityClass = Namespace.class,
                        fields = {
                                @FieldResult(name = "uri", column = "uri"),
                        }
                ),
                @EntityResult(
                        entityClass = User.class,
                        fields = {
                                @FieldResult(name = "loginId", column = "login_id"),
                        }
                )
        }
)

@Entity
public class Releases {

    @Id
    private long releaseId;

    @Column(nullable = false, length = 45)
    private String releaseNum;

    @Lob
    @Column(length = 10 * 1024)
    private String releaseNote;

    @Column(nullable = false)
    @Convert(attributeName = "state", converter = ReleaseStateConverter.class)
    private ReleaseState state;

    @Column(nullable = false, length = 100)
    private String uri;

    @Column(length = 100)
    private String loginId;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

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

    public ReleaseState getState() {
        return state;
    }

    public void setState(ReleaseState state) {
        this.state = state;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Releases releases = (Releases) o;

        if (releaseId != releases.releaseId) return false;
        if (releaseNum != null ? !releaseNum.equals(releases.releaseNum) : releases.releaseNum != null) return false;
        if (releaseNote != null ? !releaseNote.equals(releases.releaseNote) : releases.releaseNote != null)
            return false;
        if (state != releases.state) return false;
        if (uri != null ? !uri.equals(releases.uri) : releases.uri != null) return false;
        if (loginId != null ? !loginId.equals(releases.loginId) : releases.loginId != null) return false;
        return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(releases.lastUpdateTimestamp) : releases.lastUpdateTimestamp == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (releaseId ^ (releaseId >>> 32));
        result = 31 * result + (releaseNum != null ? releaseNum.hashCode() : 0);
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (loginId != null ? loginId.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Releases{" +
                "releaseId=" + releaseId +
                ", releaseNum='" + releaseNum + '\'' +
                ", releaseNote='" + releaseNote + '\'' +
                ", state=" + state +
                ", uri='" + uri + '\'' +
                ", loginId='" + loginId + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
