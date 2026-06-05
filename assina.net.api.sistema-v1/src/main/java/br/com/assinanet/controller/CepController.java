package br.com.assinanet.controller;

import br.com.assinanet.response.Response;
import br.com.assinanet.service.CepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/cep")
@CrossOrigin(origins = "*")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping(value = "{cep}")
    public ResponseEntity<Response<CepService.ViaCep>> buscaEndereco(@PathVariable("cep") String cep) {
        Response<CepService.ViaCep> response = new Response<>();

        try {
            CepService.ViaCep viaCep = cepService.buscaEndereco(cep);
            response.setData(viaCep);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


}

