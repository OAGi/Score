package org.oagi.score.gateway.http.api.library_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class LibraryList {

    private BigInteger libraryId = BigInteger.ZERO;
    private String name;
    private String organization;
    private String description;
    private String link;
    private String domain;
    private String state;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;

}
