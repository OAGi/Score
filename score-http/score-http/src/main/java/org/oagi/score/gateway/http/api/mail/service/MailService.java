package org.oagi.score.gateway.http.api.mail.service;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.angus.mail.util.MailConnectException;
import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.mail.data.SendMailRequest;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TextTemplateRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.TEXT_TEMPLATE;

@Service
@Transactional(readOnly = true)
public class MailService {

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Autowired
    private DSLContext dslContext;

    private Session createSession() {
        Properties props = new Properties();

        boolean smtpAuth = applicationConfigurationService.getBooleanProperty(
                "score.mail.smtp.auth", true);
        boolean smtpSslEnable = applicationConfigurationService.getBooleanProperty(
                "score.mail.smtp.ssl.enable", false);
        boolean smtpStartTlsEnable = applicationConfigurationService.getBooleanProperty(
                "score.mail.smtp.starttls.enable", true);
        String smtpHost = applicationConfigurationService.getProperty("score.mail.smtp.host");
        int smtpPort = applicationConfigurationService.getIntProperty(
                "score.mail.smtp.port", (smtpStartTlsEnable) ? 587 : ((smtpSslEnable) ? 465 : 25));

        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.ssl.enable", smtpSslEnable);
        props.put("mail.smtp.starttls.enable", smtpStartTlsEnable);

        String authenticationMethod = applicationConfigurationService.getProperty("score.mail.smtp.auth.method");
        if ("Password".equals(authenticationMethod)) {
            String smtpUserName = applicationConfigurationService.getProperty("score.mail.smtp.auth.username");
            String smtpUserPassword = applicationConfigurationService.getProperty("score.mail.smtp.auth.password");

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
        Session session = createSession();

        String senderAddress = requester.getEmailAddress();
        if (senderAddress == null) {
            throw new IllegalStateException("The user '" + requester.getUsername() + "' does not have a registered email address.");
        }
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(senderAddress, requester.getName(), "UTF-8"));
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Invalid sender address: " + senderAddress, e);
        }

        ScoreUser recipient = request.getRecipient();
        String recipientEmail = recipient.getEmailAddress();
        if (recipientEmail == null) {
            throw new IllegalStateException("The user '" + recipient.getUsername() + "' does not have a registered email address.");
        }
        try {
            msg.setRecipients(Message.RecipientType.TO,
                    new Address[]{
                            new InternetAddress(recipientEmail, recipient.getName(), "UTF-8")
                    });
        } catch (MessagingException | UnsupportedEncodingException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid recipient address: " + recipientEmail, e);
        }
        try {
            Map<String, Object> parameters = request.getParameters();
            parameters.put("sender", requester.getUsername());
            parameters.put("senderName", requester.getName());
            parameters.put("senderEmail", requester.getEmailAddress());
            parameters.put("recipient", recipient.getUsername());
            parameters.put("recipientName", recipient.getName());
            parameters.put("recipientEmail", recipient.getEmailAddress());
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
