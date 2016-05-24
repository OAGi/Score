package org.oagi.srt.repository.entity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class BlobContent implements Serializable {

    private int blobContentId;
    private byte[] content;
    private int releaseId;
    private String module;

    public BlobContent() {}

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
