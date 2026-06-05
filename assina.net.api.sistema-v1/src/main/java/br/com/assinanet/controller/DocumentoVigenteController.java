package br.com.assinanet.controller;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.models.ArquivoDownloadModel;
import br.com.assinanet.models.ContratoListaAssinaModel;
import br.com.assinanet.request.ContratoCancelamentoRequest;
import br.com.assinanet.request.ContratoDocumentoRequest;
import br.com.assinanet.request.ContratoFiltroRequest;
import br.com.assinanet.response.Response;
import br.com.assinanet.service.ContratoService;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/vigente")
@CrossOrigin(origins = "*")
public class DocumentoVigenteController {

    @Autowired
    private final ContratoService contratoService;

    @Autowired
    private final HttpServletRequest request;

    public DocumentoVigenteController(ContratoService contratoService,
                                      HttpServletRequest request) {
        this.contratoService = contratoService;
        this.request = request;
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<ContratoListaAssinaModel>>> findAll(@RequestBody ContratoFiltroRequest filtro, Pageable pageable) {
        Response<Page<ContratoListaAssinaModel>> response = new Response<>();

        if (!CommonsUtil.semValor(filtro.getContrato().getCustodiante())) {
            Page<ContratoListaAssinaModel> contratos = contratoService.findAllVigentes(filtro, pageable);
            response.setData(contratos);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "download")
    public ResponseEntity<Response<ArquivoDownloadModel>> Download(@RequestBody Contrato contrato) throws IOException {

        Response<ArquivoDownloadModel> response = new Response<>();
        try {
            ArquivoDownloadModel arquivoDownloadModel = contratoService.getDownload(contrato);
            response.setData(arquivoDownloadModel);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "cancelarDocumento")
    public ResponseEntity<Response<Boolean>> cancelarContrato(@RequestBody ContratoCancelamentoRequest contratoCancelamentoRequest) throws IOException {

        Response<Boolean> response = new Response<>();
        try {
            contratoService.cancelarContrato(contratoCancelamentoRequest);
            response.setData(Boolean.TRUE);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "gerarAssinaturaNovamente")
    public ResponseEntity<Response<String>> gerarAssinaturaNovamente(@RequestBody Contrato contrato){
        Response<String> response = new Response<>();
        try {
            String documentoGeradoNovamente = contratoService.gerarAssinaturaNovamente(contrato);
            response.setData(documentoGeradoNovamente);
        }catch (Exception e){
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "recusarDocumento")
    public ResponseEntity<Response<Boolean>> recusarContrato(@RequestBody ContratoCancelamentoRequest contratoCancelamentoRequest) throws IOException {

        Response<Boolean> response = new Response<>();
        try {
            contratoService.recusarContrato(contratoCancelamentoRequest);
            response.setData(Boolean.TRUE);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}

