package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurgeBieEvent {

    private BigInteger topLevelAsbiepId;

}
