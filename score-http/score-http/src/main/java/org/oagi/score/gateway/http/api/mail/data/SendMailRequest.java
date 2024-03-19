package org.oagi.score.gateway.http.api.mail.data;

import lombok.Data;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.HashMap;
import java.util.Map;

@Data
public class SendMailRequest {

    private String templateName;

    private ScoreUser recipient;

    private Map<String, Object> parameters = new HashMap<>();

}
