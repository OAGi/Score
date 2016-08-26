package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "release")
public class Release implements Serializable {

    public static final String SEQUENCE_NAME = "RELEASE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private long releaseId;

    @Column(nullable = false, length = 45)
    private String releaseNum;

    @Lob
    @Column(length = 10 * 1024)
    private String releaseNote;

    @Column(nullable = false)
    private long namespaceId;

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
        return namespaceId;
    }

    public void setNamespaceId(long namespaceId) {
        this.namespaceId = namespaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Release release = (Release) o;

        if (releaseId != release.releaseId) return false;
        if (namespaceId != release.namespaceId) return false;
        if (releaseNum != null ? !releaseNum.equals(release.releaseNum) : release.releaseNum != null) return false;
        return releaseNote != null ? releaseNote.equals(release.releaseNote) : release.releaseNote == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (releaseId ^ (releaseId >>> 32));
        result = 31 * result + (releaseNum != null ? releaseNum.hashCode() : 0);
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0);
        result = 31 * result + (int) (namespaceId ^ (namespaceId >>> 32));
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
