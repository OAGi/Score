package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "release")
public class Release implements Serializable {

    @Id
    @GeneratedValue(generator = "RELEASE_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "RELEASE_ID_SEQ", sequenceName = "RELEASE_ID_SEQ", allocationSize = 1)
    private int releaseId;

    @Column(nullable = false)
    private String releaseNum;

    @Column
    private String releaseNote;

    @Column(nullable = false)
    private int namespaceId;

    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
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

    public int getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
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
