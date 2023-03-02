package org.oagi.score.gateway.http.api.tag_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Tag {

    private BigInteger tagId;

    private String name;

    private String description;

    private String textColor;

    private String backgroundColor;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

}
