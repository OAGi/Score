package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.io.File;

@Data
public class GenerateBiePackageResponse {

    private String filename;
    private String contentType;
    private File file;

}
