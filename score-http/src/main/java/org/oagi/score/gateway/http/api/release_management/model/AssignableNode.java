package org.oagi.score.gateway.http.api.release_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;

import java.math.BigInteger;
import java.util.Date;

@Data
public class AssignableNode {
    private ManifestId manifestId;
    private CcType type;
    private CcState state;
    private String den;
    private String ownerUsername;
    private Date timestamp;
    private BigInteger revision;
}
