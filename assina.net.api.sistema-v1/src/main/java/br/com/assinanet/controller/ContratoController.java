package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.assinanet.models.ContratoListaCadastroModel;
import br.com.assinanet.request.*;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.response.ContratoResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.service.*;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.Util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/contrato")
@CrossOrigin(origins = "*")
public class ContratoController {

    private final ContratoService contratoService;
    private final PessoaService pessoaService;
    private final ContratoLogService contratoLogService;
    private final TipoDocumentoService tipoDocumentoService;
    private final PapelService papelService;
    private final ClienteService clienteService;
    private final ContratoDocumentoService contratoDocumentoService;



    public ContratoController(ContratoService contratoService, PessoaService pessoaService, ContratoLogService contratoLogService,
                              TipoDocumentoService tipoDocumentoService, PapelService papelService, ClienteService clienteService, ContratoDocumentoService contratoDocumentoService) {
        this.contratoService = contratoService;
        this.pessoaService = pessoaService;
        this.contratoLogService = contratoLogService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.papelService = papelService;
        this.clienteService = clienteService;
        this.contratoDocumentoService = contratoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<Response<ContratoRequest>> create(@RequestBody ContratoRequest contratoRequest) {
        Response<ContratoRequest> response = new Response<>();
        try {
            contratoRequest = contratoService.Salva(contratoRequest);
            response.setData(contratoRequest);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<ContratoRequest>> update(@RequestBody ContratoRequest contratoRequest) {
        Response<ContratoRequest> response = new Response<>();
        try {
            contratoRequest = contratoService.Salva(contratoRequest);
            response.setData(contratoRequest);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<ContratoRequest>> findById(@RequestBody IdRequest idRequest) {
        Response<ContratoRequest> response = new Response<>();
        ContratoRequest contratoRequest = new ContratoRequest();
        if (idRequest.getId() != null) {
            Contrato contrato = contratoService.findById(idRequest.getId());
            if (!Util.verifica(contrato).isPresent()) {
                response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
                return ResponseEntity.badRequest().body(response);
            }
            contratoRequest.setContrato(new ContratoResponse(contrato));
        }

        BuscaCombosContrato(contratoRequest);
        contratoRequest.setId(contratoRequest.getContrato().getId());

        response.setData(contratoRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "new")
    public ResponseEntity<Response<ContratoRequest>> carregaCombos(@RequestBody IdRequest idRequest) {
        Response<ContratoRequest> response = new Response<>();
        ContratoRequest contratoRequest = new ContratoRequest();
        contratoRequest.setContrato(new ContratoResponse());

        contratoRequest.getContrato().setStatus(StatusEnum.ATIVO);
        Cliente cliente = clienteService.findById(idRequest.getIdCliente());
        contratoRequest.getContrato().setCustodiante(cliente);

        contratoRequest.getContrato().setStatus(StatusEnum.ATIVO);
        BuscaCombosContrato(contratoRequest);
        response.setData(contratoRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        Contrato contrato = contratoService.findById(idRequest.getId());
        if (!Util.verifica(contrato).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        contrato.setStatus(StatusEnum.INATIVO);
        contratoService.Salva(contrato);
        return ResponseEntity.ok(new Response<>());
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<ContratoListaCadastroModel>>> findAll(@RequestBody Contrato filtro, Pageable pageable) {
        Response<Page<ContratoListaCadastroModel>> response = new Response<>();
        if (!CommonsUtil.semValor(filtro.getCustodiante())) {
            Page<ContratoListaCadastroModel> contratos = contratoService.findAllCadastro(filtro, pageable);
            response.setData(contratos);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "documentoPdf")
    public ResponseEntity<Response<ContratoDocumentoVisualizaResponse>> getDocumentoPdf(@RequestBody ContratoDocumentoRequest documento) {
        Response<ContratoDocumentoVisualizaResponse> response = new Response<>();
        try {
            ContratoDocumentoVisualizaResponse documentoPDF;
            if (StatusDocumentoEnum.ASSINADO.equals(documento.getStatusDocumento())  && CommonsUtil.booleanValue(documento.getTipoDocumento().getAssina())) {
                documentoPDF = contratoService.getDocumentoAssiandoPDF(documento);
            } else {
                documentoPDF = contratoService.getDocumentoPDF(documento);
            }
            response.setData(documentoPDF);
        } catch (Exception e) {
            System.out.println("[CONTRATO_DOCUMENTO_PDF] Falha ao visualizar documento: " + e.getMessage());
            response.getErrors().add("Não foi possível visualizar este documento agora. Verifique se o arquivo está disponível no armazenamento.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "documentosPdf")
    public ResponseEntity<Response<ContratoDocumentoVisualizaResponse>> getDocumentosPdf(@RequestBody List<ContratoDocumentoRequest> documentos) {
        Response<ContratoDocumentoVisualizaResponse> response = new Response<>();
        ContratoDocumentoVisualizaResponse documentoPDF;

        List<byte[]> pdfsToMerge = new ArrayList<>();
        for(ContratoDocumentoRequest documento : documentos) {
            if (StatusDocumentoEnum.ASSINADO.equals(documento.getStatusDocumento()) && CommonsUtil.booleanValue(documento.getTipoDocumento().getAssina())) {
                pdfsToMerge.add(contratoService.getDocumentoAssiandoPDF(documento).getDocumento());
            } else {
                pdfsToMerge.add(contratoService.getDocumentoPDF(documento).getDocumento());
            }
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PDDocument destination = null;

            for (byte[] pdf : pdfsToMerge) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(pdf);
                PDDocument source = null;
                try {
                    source = PDDocument.load(inputStream);
                } catch (Exception e){
                    continue;
                }

                if (destination == null && source != null) {
                    destination = new PDDocument(source.getDocument());
                } else {
                    destination.addPage(source.getPage(0));
                }
            }
            destination.save(outputStream);
            destination.close();
            documentoPDF = new ContratoDocumentoVisualizaResponse(null, outputStream.toByteArray());
            response.setData(documentoPDF);
        } catch (IOException e){
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "liberarAssinaturaID")
    public ResponseEntity<Response<ContratoRequest>> liberarAssinaturaID(@RequestBody UUID idContrato) {
        Response<ContratoRequest> response = new Response<>();
        try {

            Contrato contrato = contratoService.findById(idContrato);
            ContratoRequest contratoRequest = new ContratoRequest();
            contratoRequest.setContrato( new ContratoResponse(contrato));
            contratoRequest = contratoService.LiberarAssinatura(contratoRequest);
            response.setData(contratoRequest);

        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "liberarAssinatura")
    public ResponseEntity<Response<ContratoRequest>> liberarAssinatura(@RequestBody ContratoRequest contratoRequest) {
        Response<ContratoRequest> response = new Response<>();
        try {
            contratoRequest = contratoService.LiberarAssinatura(contratoRequest);
            response.setData(contratoRequest);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "liberarAssinaturaLote")
    public ResponseEntity<Response<String>> liberarAssinaturaLote(@RequestBody ContratoLiberacaoLoteRequest contratoLiberacaoLoteRequest) {
        Response<String> response = new Response<>();
        try {
            String result = contratoService.LiberarAssinaturaLote(contratoLiberacaoLoteRequest);
            response.setData(result);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "reenviaSolicitacaoAssintura")
    public ResponseEntity<Response<String>> ReenviaSolicitacaoAssintura(@RequestBody ContratoParteResponse contratoParteResponse) {
        Response<String> response = new Response<>();
        try {
            String result = contratoService.ReenviaSolicitacaoAssintura(contratoParteResponse);
            response.setData(result);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "dadosPessoa")
    public ResponseEntity<Response<Pessoa>> DadosPessoa(@RequestBody ContratoParteRequest contratoParteRequest) {
        Response<Pessoa> response = new Response<>();
        Pessoa pessoa = pessoaService.findByCpfCnpjAndCliente(contratoParteRequest.getContratoParte(), contratoParteRequest.getClienteSelecionado());

        //  Pessoa contratoParteResponse = new Pessoa(pessoa);

        response.setData(pessoa);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "partesPadrao")
    public ResponseEntity<Response<List<ContratoParteResponse>>> PartesPadraoPorTipoDocumento(@RequestBody PartesPadraoRequest partesPadraoRequest) {

        Response<List<ContratoParteResponse>> response = new Response<>();
        List<ContratoParteResponse> partes = tipoDocumentoService.findPartes( partesPadraoRequest.getCliente(),  partesPadraoRequest.getTipoDocumento());
        response.setData(partes);
        return ResponseEntity.ok(response);
    }

    private void BuscaCombosContrato(ContratoRequest contratoRequest) {
        contratoRequest.getListCombos().put("TipoDocumento", tipoDocumentoService.getListCombo(contratoRequest.getContrato().getCustodiante()));
        contratoRequest.getListCombos().put("ContratoPapel", papelService.getListCombo(contratoRequest.getContrato().getCustodiante()));
        contratoRequest.getListCombos().put("TipoPessoa", TipoPessoaEnum.getListCombo());
    }

}



