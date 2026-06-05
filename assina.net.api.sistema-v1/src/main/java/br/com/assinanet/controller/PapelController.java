package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.enums.PapelEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.request.NovoPapelTipoDocumentoRequest;
import br.com.assinanet.response.PapelResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.service.PapelService;
import br.com.assinanet.service.SegmentoService;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/config/papel")
@CrossOrigin(origins = "*")
public class PapelController {

    private final ClienteService clienteService;
    private final PapelService papelService;

    private final SegmentoService segmentoService;

    public PapelController(ClienteService clienteService, PapelService papelService, SegmentoService segmentoService)
    {
        this.clienteService = clienteService;
        this.papelService = papelService;
        this.segmentoService = segmentoService;
    }

    @PostMapping
    public ResponseEntity<Response<Papel>> create(@RequestBody @Valid Papel papel) {
        Response<Papel> response = new Response<>();
        try {
            response.setData(papelService.save(papel));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Papel>> update(@RequestBody @Valid Papel papel) {
        Response<Papel> response = new Response<>();
        try {
            response.setData(papelService.save(papel));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<PapelResponse>> findById(@RequestBody IdRequest idRequest) {
        Response<PapelResponse> response = new Response<>();
        PapelResponse papelResponse = new PapelResponse();

        Papel papel = papelService.findById(idRequest.getId());
        if (!Util.verifica(papel).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        papelResponse.setPapel(papel);

        BuscaCombosContrato(papelResponse, idRequest);

        response.setData(papelResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "new")
    public ResponseEntity<Response<PapelResponse>> carregaCombos(@RequestBody IdRequest idRequest) {
        Response<PapelResponse> response = new Response<>();

        PapelResponse papelResponse = new PapelResponse();

        Cliente cliente = new Cliente();
        cliente.setId(idRequest.getIdCliente());

        Papel papel = new Papel();
        papel.setStatus(StatusEnum.ATIVO);
        papel.setCliente(cliente);
        papelResponse.setPapel(papel);

        BuscaCombosContrato(papelResponse, idRequest);

        response.setData(papelResponse);
        return ResponseEntity.ok(response);

    }


    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<Papel>>> findAll(@RequestBody Papel filtro, Pageable pageable) {
        Response<Page<Papel>> response = new Response<>();
        Page<Papel> papeis = papelService.findAll(filtro, pageable);

        response.setData(papeis);
        return ResponseEntity.ok(response);
    }
    @PostMapping(value = "novoPapelTipoDocumento")
    public ResponseEntity<Response<Papel>> novoPapelTipoDocumento(@RequestBody NovoPapelTipoDocumentoRequest novoPapelTipoDocumentoRequest) {
        Response<Papel> response = new Response<>();
        Papel Papel = papelService.novoPapelTipoDocumento(novoPapelTipoDocumentoRequest);
        response.setData(Papel);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping()
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        Papel papel = papelService.findById(idRequest.getId());
        if (!Util.verifica(papel).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }

        Cliente clienteSistema = clienteService.retornaClienteSistema();
        Papel papelObesrvador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), clienteSistema);
        if (papelObesrvador != null && CommonsUtil.mesmoValor(papel.getNome(), papelObesrvador.getNome())) {
            response.getErrors().add("Não é possivel EXCLUIR o papel observador do SISTEMA");
            return ResponseEntity.badRequest().body(response);
        }

        papel.setStatus(StatusEnum.INATIVO);
        papelService.save(papel);
        return ResponseEntity.ok(new Response<>());
    }

    private void BuscaCombosContrato(PapelResponse papelResponse, IdRequest idRequest) {
        papelResponse.getListCombos().put("TipoCliente", segmentoService.getListCombo());
        //papelResponse.getListCombos().put("TipoCliente", TipoClienteEnum.getListCombo(false));
    }


}

