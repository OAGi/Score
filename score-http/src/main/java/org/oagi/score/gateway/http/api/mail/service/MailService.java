package org.oagi.score.gateway.http.api.mail.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.PreencodedMimeBodyPart;
import org.apache.commons.text.StringSubstitutor;
import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.mail.controller.payload.SendMailRequest;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.TextTemplateRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TEXT_TEMPLATE;

@Service
@Transactional(readOnly = true)
public class MailService {

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Autowired
    private DSLContext dslContext;

    private Session createSession(ScoreUser requester) {
        Properties props = new Properties();

        boolean smtpAuth = applicationConfigurationService.getBooleanProperty(
                requester, "score.mail.smtp.auth", true);
        boolean smtpSslEnable = applicationConfigurationService.getBooleanProperty(
                requester, "score.mail.smtp.ssl.enable", false);
        boolean smtpStartTlsEnable = applicationConfigurationService.getBooleanProperty(
                requester, "score.mail.smtp.starttls.enable", true);
        String smtpHost = applicationConfigurationService.getProperty(requester, "score.mail.smtp.host");
        int smtpPort = applicationConfigurationService.getIntProperty(
                requester, "score.mail.smtp.port", (smtpStartTlsEnable) ? 587 : ((smtpSslEnable) ? 465 : 25));

        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.ssl.enable", smtpSslEnable);
        props.put("mail.smtp.starttls.enable", smtpStartTlsEnable);

        String authenticationMethod = applicationConfigurationService.getProperty(
                requester, "score.mail.smtp.auth.method");
        if ("Password".equals(authenticationMethod)) {
            String smtpUserName = applicationConfigurationService.getProperty(
                    requester, "score.mail.smtp.auth.username");
            String smtpUserPassword = applicationConfigurationService.getProperty(
                    requester, "score.mail.smtp.auth.password");

            return Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUserName, smtpUserPassword);
                }
            });
        } else {
            throw new IllegalStateException("Unsupported Mail Authentication Method: " + authenticationMethod);
        }
    }

    public void sendMail(ScoreUser requester, SendMailRequest request) {
        Session session = createSession(requester);

        String senderAddress = requester.emailAddress();
        if (senderAddress == null) {
            throw new IllegalStateException("The user '" + requester.username() + "' does not have a registered email address.");
        }
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(senderAddress, requester.name(), "UTF-8"));
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Invalid sender address: " + senderAddress, e);
        }

        ScoreUser recipient = request.getRecipient();
        String recipientEmail = recipient.emailAddress();
        if (recipientEmail == null) {
            throw new IllegalStateException("The user '" + recipient.username() + "' does not have a registered email address.");
        }
        try {
            msg.setRecipients(Message.RecipientType.TO,
                    new Address[]{
                            new InternetAddress(recipientEmail, recipient.name(), "UTF-8")
                    });
        } catch (MessagingException | UnsupportedEncodingException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid recipient address: " + recipientEmail, e);
        }
        try {
            Map<String, Object> parameters = request.getParameters();
            parameters.put("sender", requester.username());
            parameters.put("senderName", requester.name());
            parameters.put("senderEmail", requester.emailAddress());
            parameters.put("recipient", recipient.username());
            parameters.put("recipientName", recipient.name());
            parameters.put("recipientEmail", recipient.emailAddress());
            setSubjectAndContent(msg, request.getTemplateName(), parameters);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Invalid content: " + e.getMessage(), e);
        }
        try {
            msg.setSentDate(new Date());
        } catch (MessagingException e) {
            throw new IllegalStateException("Unexpected error during mail message creation for the following reason: " + e.getMessage(), e);
        }
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new IllegalStateException("The email cannot be sent for the following reason: " + e.getMessage(), e);
        }
    }

    private void setSubjectAndContent(MimeMessage message, String templateName, Map<String, Object> parameters) throws MessagingException {
        StringSubstitutor sub = new StringSubstitutor(parameters);

        TextTemplateRecord textTemplateRecord = dslContext.selectFrom(TEXT_TEMPLATE)
                .where(TEXT_TEMPLATE.NAME.eq(templateName))
                .fetchOptional().orElse(null);

        if (textTemplateRecord == null) {
            throw new ScoreDataAccessException("No template name: " + templateName);
        }

        message.setSubject(textTemplateRecord.getSubject());

        String contentType = textTemplateRecord.getContentType();
        String subtype = contentType.substring(contentType.indexOf("/") + 1);

        PreencodedMimeBodyPart bodyPart = new PreencodedMimeBodyPart("base64");
        bodyPart.setText(sub.replace(textTemplateRecord.getTemplate()), "utf-8", subtype);
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);
        message.setContent(multipart);
        message.saveChanges();
    }

}
