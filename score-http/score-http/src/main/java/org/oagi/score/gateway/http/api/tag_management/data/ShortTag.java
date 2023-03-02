package org.oagi.score.gateway.http.api.tag_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ShortTag {

    private BigInteger tagId;

    private String name;

    private String textColor;

    private String backgroundColor;

}
