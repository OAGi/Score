package org.oagi.score.gateway.http.api.library_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Library {

    private BigInteger libraryId;

    private String name;

}
