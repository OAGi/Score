package org.oagi.score.gateway.http.api.agency_id_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class UpdateAgencyIdListListRequest {
    public List<BigInteger> agencyIdListManifestIds;
}
