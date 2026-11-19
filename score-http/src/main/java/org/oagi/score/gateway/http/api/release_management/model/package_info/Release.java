package org.oagi.score.gateway.http.api.release_management.model.package_info;

public class Release {

    private long id;
    private String releaseNum;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

}
