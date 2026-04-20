package by.ladyka.poputka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Configuration
@Getter
@Setter
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String mailHost;
    @Value("${spring.mail.port}")
    private Integer mailPort;
    @Value("${spring.mail.protocol:smtp}")
    private String mailProtocol;
    @Value("${spring.mail.default-encoding:UTF-8}")
    private String mailEncoding;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;


    @Bean
    @Qualifier("ladyka")
    @Primary
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl() {

            /**
             * Spring's {@code send(SimpleMailMessage...)} calls {@link #doSend} directly and never
             * {@code send(MimeMessage...)}, so we normalize From here for all send paths (Mail.ru
             * rejects envelope MAIL FROM if it is the default {@code user@hostname}).
             */
            @Override
            protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
                for (MimeMessage mimeMessage : mimeMessages) {
                    try {
                        mimeMessage.setFrom(new InternetAddress(mailUsername, "poputka.by"));
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        throw new MailPreparationException("Failed to set SMTP From address", e);
                    }
                }
                super.doSend(mimeMessages, originalMessages);
            }
        };
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.from", mailUsername);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);
        props.put("mail.transport.protocol", mailProtocol);
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.enable", true);
        props.setProperty("mail.smtp.allow8bitmime", "true");
        props.setProperty("mail.smtps.allow8bitmime", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });
        mailSender.setSession(session);
        return mailSender;
    }
}
