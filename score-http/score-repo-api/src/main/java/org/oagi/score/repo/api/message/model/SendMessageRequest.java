package org.oagi.score.repo.api.message.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SendMessageRequest extends Request {

    public static String DEFAULT_BODY_CONTENT_TYPE = "text/plain";
    public static String MARKDOWN_CONTENT_TYPE = "text/markdown";

    private Set<ScoreUser> recipients = new HashSet();
    private String subject;
    private String body;
    private String bodyContentType = DEFAULT_BODY_CONTENT_TYPE;

    public SendMessageRequest(ScoreUser requester) {
        super(requester);
    }

    public Collection<ScoreUser> getRecipients() {
        return Collections.unmodifiableCollection(this.recipients);
    }

    public void addRecipient(ScoreUser recipient) {
        recipients.add(recipient);
    }

    public SendMessageRequest withRecipient(ScoreUser recipient) {
        addRecipient(recipient);
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public SendMessageRequest withSubject(String subject) {
        setSubject(subject);
        return this;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public SendMessageRequest withBody(String body) {
        setBody(body);
        return this;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public SendMessageRequest withBodyContentType(String bodyContentType) {
        setBodyContentType(bodyContentType);
        return this;
    }
}
