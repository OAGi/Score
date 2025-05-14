package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;

import java.math.BigInteger;
import java.util.Date;

public record AssignNodeRecord<T extends ManifestId>(T manifestId,
                                                     CcType type,
                                                     CcState state,
                                                     String den,
                                                     String ownerUsername,
                                                     Date timestamp,
                                                     BigInteger revision) {
}
