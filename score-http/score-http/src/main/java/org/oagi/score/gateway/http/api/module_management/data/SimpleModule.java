package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class SimpleModule {

    private BigInteger moduleId;
    private String name;

}
