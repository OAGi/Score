package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Response;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class GetMessageResponse extends Response {

    private final BigInteger messageId;
    private final ScoreUser sender;
    private final String subject;
    private final String body;
    private final String bodyContentType;
    private final LocalDateTime timestamp;

    public GetMessageResponse(BigInteger messageId,
                              ScoreUser sender,
                              String subject,
                              String body, String bodyContentType,
                              LocalDateTime timestamp) {
        this.messageId = messageId;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.bodyContentType = bodyContentType;
        this.timestamp = timestamp;
    }

    public BigInteger getMessageId() {
        return messageId;
    }

    public ScoreUser getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
