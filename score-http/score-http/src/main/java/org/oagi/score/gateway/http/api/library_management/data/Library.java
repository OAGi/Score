package org.oagi.score.gateway.http.api.library_management.data;

import lombok.Data;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

@Data
public class Library {

    private BigInteger libraryId;

    private String name;

    private String organization;

    private String link;

    private String domain;

    private String description;

    private String state;

    private ScoreUser createdBy;

    private ScoreUser lastUpdatedBy;

    private Date creationTimestamp;

    private Date lastUpdateTimestamp;

}
