package br.com.assinanet.controller;

import br.com.assinanet.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/mail")
@CrossOrigin(origins = "*")
public class MailController {

    //private final MailService mailService;

//    public MailController(MailService mailService) {
//        this.mailService = mailService;
//    }

    public MailController() {
    }

    @GetMapping(value="config")
    public String config() {

        String configMail = "Retornando a configuração de envio de email do assina.net...";
        return configMail;
    }

}

