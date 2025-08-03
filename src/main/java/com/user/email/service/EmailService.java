package com.user.email.service;

import com.user.dto.AuthenticationResponse;
import com.user.email.dto.EmailRequest;
import jakarta.validation.Valid;

public interface EmailService {

   AuthenticationResponse sendEmail( String to, String subject, String text);

   AuthenticationResponse sendEmailWitHTML ( @Valid EmailRequest emailRequest );

}
