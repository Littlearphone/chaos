package com.littlearphone.gmail;

import lombok.SneakyThrows;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.internet.InternetAddress.parse;

public class Startup {
    @SneakyThrows
    public static void main(String[] args) {
        final Properties prop = new Properties();
        prop.put("mail.smtp.username", "ggg@gmail.com");
        prop.put("mail.smtp.password", "xxxxxxxxx");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        Session mailSession = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prop.getProperty("mail.smtp.username"),
                    prop.getProperty("mail.smtp.password"));
            }
        });
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress("yaojiamin.qy@gmail.com"));
        message.setSubject("Sending Mail with pure Java Mail API ");
        /* Mail body with plain Text */
        message.setText("Hello User,\n\n If you read this, means mail sent with Java Mail API is successful");
        // /* Step 1: Create MimeBodyPart and set content and its Mime Type */
        // BodyPart mimeBody = new MimeBodyPart();
        // mimeBody.setContent("<h1> This is HTML content </h1><br> Hello <b> User </b>", "text/html");
        // /* Step 2: Create MimeMultipart and  wrap the mimebody to it */
        // Multipart multiPart = new MimeMultipart();
        // multiPart.addBodyPart(mimeBody);
        // MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        // attachmentBodyPart.attachFile(new File("<<Path to file>>"));
        // multiPart.addBodyPart(attachmentBodyPart);
        // /* Step 3: set the multipart content to Message in caller method*/
        // message.setContent(multiPart);
        message.setRecipients(TO, parse("aaa@outlook.com"));
        message.setRecipients(CC, parse("bbb@outlook.com"));
        message.setRecipients(BCC, parse("ccc@outlook.com"));
        Transport.send(message);
    }
}
