package com.user.email.controller;

import com.user.dto.AuthenticationResponse;
import com.user.email.dto.EmailRequest;
import com.user.email.service.EmailServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailServiceImpl emailService;

    public EmailController ( EmailServiceImpl emailService ) {
        this.emailService = emailService;
    }


    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail( @Valid @RequestBody  EmailRequest emailRequest ){
       AuthenticationResponse response = emailService.sendEmail ( emailRequest.to (), emailRequest.subject (), emailRequest.message () );
       return  new ResponseEntity<> ( response, HttpStatus.OK );
    }

    @PostMapping("/send-email-html")
    public ResponseEntity<?> sendEmailWithHtml( @Valid @RequestBody  EmailRequest emailRequest ){
        AuthenticationResponse response = emailService.sendEmailWitHTML ( emailRequest );
        return  new ResponseEntity<> ( response, HttpStatus.OK );
    }
}
