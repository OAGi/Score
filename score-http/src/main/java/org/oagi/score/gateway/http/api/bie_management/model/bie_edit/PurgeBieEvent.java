package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurgeBieEvent {

    private TopLevelAsbiepId topLevelAsbiepId;

}