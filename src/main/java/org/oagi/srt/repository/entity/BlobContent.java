package org.oagi.srt.repository.entity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

@Entity
@Table(name = "blob_content")
public class BlobContent implements Serializable {

    @Id
    @GeneratedValue(generator = "BLOB_CONTENT_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "BLOB_CONTENT_ID_SEQ", sequenceName = "BLOB_CONTENT_ID_SEQ", allocationSize = 1)
    private int blobContentId;

    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private int releaseId;

    @Column(nullable = false)
    private String module;

    public BlobContent() {
    }

    public BlobContent(File file) throws IOException {
        setModule(extractModuleFromFile(file));
        setContent(FileUtils.readFileToByteArray(file));
    }

    private String extractModuleFromFile(File file) {
        String path = file.getAbsolutePath();
        path = path.substring(path.indexOf("Model"));
        return FilenameUtils.separatorsToWindows(path);
    }

    public int getBlobContentId() {
        return blobContentId;
    }

    public void setBlobContentId(int blobContentId) {
        this.blobContentId = blobContentId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public String toString() {
        return "BlobContent{" +
                "blobContentId=" + blobContentId +
                ", content=" + ((content != null) ? "<byte[] " + content.length + ">" : null) +
                ", releaseId=" + releaseId +
                ", module='" + module + '\'' +
                '}';
    }
}
