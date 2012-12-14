package cz.cesnet.shongo.controller.notification;

import cz.cesnet.shongo.controller.Configuration;
import cz.cesnet.shongo.controller.common.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@link NotificationExecutor} for sending mails.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class EmailNotificationExecutor extends NotificationExecutor
{
    private static Logger logger = LoggerFactory.getLogger(EmailNotificationExecutor.class);

    private static final String EMAIL_HEADER = ""
            + "==========================================================+\n"
            + " Automatic notification from the Shongo reservation system \n"
            + "===========================================================\n\n";
    /**
     * Sender email address.
     */
    private String emailSender = null;

    /**
     * Session for sending emails.
     */
    private Session session;

    @Override
    public void init(Configuration configuration)
    {
        super.init(configuration);

        // Skip configuration without host
        if (!configuration.containsKey(Configuration.SMTP_HOST)) {
            logger.warn("Cannot initialize email notifications because SMTP configuration is empty.");
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", configuration.getString(Configuration.SMTP_HOST));
        properties.setProperty("mail.smtp.port", configuration.getString(Configuration.SMTP_PORT));
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");

        emailSender = configuration.getString(Configuration.SMTP_SENDER);

        Authenticator authenticator = null;
        if (configuration.containsKey(Configuration.SMTP_USERNAME)) {
            properties.setProperty("mail.smtp.auth", "true");
            authenticator = new PasswordAuthenticator(
                    configuration.getString(Configuration.SMTP_USERNAME),
                    configuration.getString(Configuration.SMTP_PASSWORD));
        }

        session = Session.getDefaultInstance(properties, authenticator);
    }

    @Override
    public void executeNotification(Notification notification)
    {
        if (session == null) {
            return;
        }

        List<String> recipients = new ArrayList<String>();
        for (Person person : notification.getRecipients()) {
            String email = person.getInformation().getPrimaryEmail();
            if (email != null) {
                recipients.add(email);
            }
        }
        if (recipients.size() == 0) {
            logger.warn("Notification '{}' doesn't have any recipients with email address.", notification.getName());
            return;
        }

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailSender));

            StringBuilder recipientString = new StringBuilder();
            for (String recipient : recipients) {
                if (recipientString.length() > 0) {
                    recipientString.append(", ");
                }
                recipientString.append(recipient);
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            String subject = notification.getName();
            StringBuilder text = new StringBuilder();
            text.append(EMAIL_HEADER);
            text.append(notification.getContent());
            message.setSubject(subject);
            message.setText(text.toString());
            logger.debug("Sending email '{}' to '{}'...\n{}", new Object[]{subject, recipientString, text});
            sendMail(message);
        }
        catch (Exception exception) {
            logger.error("Failed to send email.", exception);
        }
    }

    /**
     * Send email.
     *
     * @param message
     * @throws MessagingException
     */
    protected void sendMail(Message message) throws MessagingException
    {
        Transport.send(message);
    }

    /**
     * {@link Authenticator} for username and password.
     */
    private static class PasswordAuthenticator extends Authenticator
    {
        /**
         * Username.
         */
        private String userName;

        /**
         * Password for {@link #userName}.
         */
        private String password;

        /**
         * Constructor.
         *
         * @param userName sets the {@link #userName}
         * @param password sets the {@link #password}
         */
        public PasswordAuthenticator(String userName, String password)
        {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(userName, password);
        }
    }
}
