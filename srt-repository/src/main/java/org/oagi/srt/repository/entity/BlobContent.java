package org.oagi.srt.repository.entity;

import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

@Entity
@Table(name = "blob_content")
public class BlobContent implements Serializable {

    public static final String SEQUENCE_NAME = "BLOB_CONTENT_ID_SEQ";

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
    private long blobContentId;

    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private long releaseId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Transient
    private File file;

    public BlobContent() {
    }

    public BlobContent(File file) throws IOException {
        this.file = file;
        setContent(FileUtils.readFileToByteArray(file));
    }

    public long getBlobContentId() {
        return blobContentId;
    }

    public void setBlobContentId(long blobContentId) {
        this.blobContentId = blobContentId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlobContent that = (BlobContent) o;

        if (blobContentId != that.blobContentId) return false;
        if (releaseId != that.releaseId) return false;
        if (!Arrays.equals(content, that.content)) return false;
        return module != null ? module.equals(that.module) : that.module == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (blobContentId ^ (blobContentId >>> 32));
        result = 31 * result + Arrays.hashCode(content);
        result = 31 * result + (int) (releaseId ^ (releaseId >>> 32));
        result = 31 * result + (module != null ? module.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BlobContent{" +
                "blobContentId=" + blobContentId +
                ", content=" + Arrays.toString(content) +
                ", releaseId=" + releaseId +
                ", module=" + module +
                '}';
    }
}
