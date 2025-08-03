package com.user.email.service;

import com.user.dto.AuthenticationResponse;
import com.user.email.dto.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailServiceImpl implements EmailService {

    private  Logger logger = LoggerFactory.getLogger ( EmailServiceImpl.class );

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl ( JavaMailSender mailSender ) {
        this.mailSender = mailSender;
    }

    @Override
    public AuthenticationResponse sendEmail( String to, String subject, String message) {

        AuthenticationResponse response = new AuthenticationResponse ();
       try {
           SimpleMailMessage simpleMailMessage = new SimpleMailMessage ();
           simpleMailMessage.setTo ( to );
           simpleMailMessage.setSubject ( subject );
           simpleMailMessage.setText ( message );
           mailSender.send ( simpleMailMessage );
           logger.info ( "Email sent successfully to {}",to);
           response.setMessage ("email sent to " + to + " successfully.");

       }catch (Exception e){
           logger.error("Failed to send email to {}: {}", to, e.getMessage());
           response.setMessage("Failed to send email.");
       }
        response.setTimestamp ( Instant.now () );
        return response;
    }

    @Override
    public AuthenticationResponse sendEmailWitHTML ( EmailRequest emailRequest ) {
        MimeMessage mimeMessage = mailSender.createMimeMessage ();
        AuthenticationResponse response = new AuthenticationResponse ();

        try {

            MimeMessageHelper messageHelper = new MimeMessageHelper ( mimeMessage, true, "UTF-8" );
            messageHelper.setTo (emailRequest.to () );
            messageHelper.setSubject (emailRequest.subject ());
            messageHelper.setText (emailRequest.message (), true);
            // make it dynamic later
            messageHelper.setFrom ("mursalim.codknox@gmail.com");
            mailSender.send(mimeMessage);
            logger.info ( "Email sent successfully with html content to {}",emailRequest.to());
            response.setMessage ("Email sent successfully with html content to "+emailRequest.to());

        }catch (Exception e){
            logger.error ( "Error sending email with html content to {}",emailRequest.to());
            response.setMessage ("Error in sending email with html content to "+emailRequest.to());
        }
    return  response;
    }
}
