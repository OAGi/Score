package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class MessageList {

    private BigInteger messageId;
    private ScoreUser sender;
    private String subject;
    private boolean read;
    private Date timestamp;

    public BigInteger getMessageId() {
        return messageId;
    }

    public void setMessageId(BigInteger messageId) {
        this.messageId = messageId;
    }

    public ScoreUser getSender() {
        return sender;
    }

    public void setSender(ScoreUser sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
