package br.com.assinanet.controller;

import br.com.assinanet.service.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/integracao")
@CrossOrigin(origins = "*")
public class IntegracaoController {

    @Autowired
    private final ContratoService contratoService;

    public IntegracaoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public String getStatus() {
        return "API Assina.Net - Running";
    }

    @PostMapping(value = "envioContrato1")
    public @ResponseBody
    String EnvioContrato(@RequestBody String xml) {

        String response = contratoService.EnvioDocumentoXML(xml);
        return response;
    }

    @PostMapping(value = "envioContrato")
    public @ResponseBody
    String EnvioContrato(@RequestParam("xml") MultipartFile arqXML) {

        try {
            String xml = new String(arqXML.getBytes());


            String response = contratoService.EnvioDocumentoXML(xml);
            return response;
        } catch (Exception e) {
        }
        return null;
    }

    @PostMapping(value = "exclusaoProcurador")
    public @ResponseBody
    String ExclusaoProcurador(@RequestBody String xml) {
        String response = contratoService.ExclusaoProcurador(xml);
        return response;
    }

}

