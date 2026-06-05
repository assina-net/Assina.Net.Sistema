package br.com.assinanet.controller;

import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.request.ContratoDocumentoRequest;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.service.ContratoService;
import br.com.assinanet.service.SistemaAtributoService;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/validar")
@CrossOrigin(origins = "*")
public class ValidarDocumentoController {


    private final ContratoService contratoService;
    private final SistemaAtributoService sistemaAtributoService;

    public ValidarDocumentoController(ContratoService contratoService, SistemaAtributoService sistemaAtributoService) {
        this.contratoService = contratoService;
        this.sistemaAtributoService = sistemaAtributoService;
    }

    @GetMapping("/{id}")
    public  ResponseEntity<Response<ContratoDocumentoVisualizaResponse>>  validarDocumento(@PathVariable String id) {
        UUID idDocumento;
        try{
            idDocumento = UUID.fromString(id);
        }catch(Exception e) {
            throw new IllegalArgumentException("Código de documento inválido");
        }
        Response<ContratoDocumentoVisualizaResponse> response = new Response<>();
        ContratoDocumentoVisualizaResponse documentoPDF;
        ContratoDocumentoRequest documento =  new ContratoDocumentoRequest();
        documento.setId(idDocumento);
        documentoPDF = contratoService.getDocumentoAssiandoPDF(documento);
        if ( !CommonsUtil.booleanValue(documentoPDF.getValidacaoOnLine()) &&
              !sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.EXIBE_MENSAGEM_VALIDACAO_DE_DOCUMENTO, documentoPDF.getCliente())){
            return ResponseEntity.notFound().build();
        }
        response.setData(documentoPDF);
        return ResponseEntity.ok(response);
    }
}
