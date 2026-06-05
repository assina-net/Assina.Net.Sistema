package br.com.assinanet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/recuperarsenha")
@CrossOrigin(origins = "*")
public class RecuperarSenha {

    @PostMapping()
    ResponseEntity<?> ValidarChaveAcesso(@RequestBody String chaveAcesso) {

        return null;
    }


}
