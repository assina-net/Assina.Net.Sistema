package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.request.TipoDocumentoRequest;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.TipoDocumentoResponse;
import br.com.assinanet.service.PapelService;
import br.com.assinanet.service.SegmentoService;
import br.com.assinanet.service.TipoDocumentoService;
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
@RequestMapping("/api/v1/config/tipoDocumento")
@CrossOrigin(origins = "*")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;
    private final PapelService papelService;
    private final SegmentoService segmentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService, PapelService papelService, SegmentoService segmentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
        this.papelService = papelService;
        this.segmentoService = segmentoService;
    }

    @PostMapping
    public ResponseEntity<Response<TipoDocumentoRequest>> create(@RequestBody @Valid TipoDocumentoRequest tipoDocumentoRequest) {
        Response<TipoDocumentoRequest> response = new Response<>();
        try {
            response.setData(tipoDocumentoService.save(tipoDocumentoRequest));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<TipoDocumentoRequest>> update(@RequestBody @Valid TipoDocumentoRequest tipoDocumentoRequest) {
        Response<TipoDocumentoRequest> response = new Response<>();
        try {
            response.setData(tipoDocumentoService.save(tipoDocumentoRequest));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<TipoDocumentoResponse>> findById(@RequestBody IdRequest idRequest) {
        Response<TipoDocumentoResponse> response = new Response<>();

        TipoDocumento tipoDocumento = tipoDocumentoService.findById(idRequest.getId());

        if (!Util.verifica(tipoDocumento).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }

        TipoDocumentoResponse tipoDocumentoResponse = new TipoDocumentoResponse();
        tipoDocumentoResponse.setTipoDocumento(tipoDocumento);
        BuscaCombos(tipoDocumentoResponse, idRequest);

        Cliente cliente = new Cliente();
        cliente.setId(idRequest.getIdCliente());
        tipoDocumentoResponse.setPartes(tipoDocumentoService.findPartes(cliente, tipoDocumento));

        response.setData(tipoDocumentoResponse);
        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "new")
    public ResponseEntity<Response<TipoDocumentoResponse>> carregaCombos(@RequestBody IdRequest idRequest) {
        Response<TipoDocumentoResponse> response = new Response<>();
        TipoDocumentoResponse tipoDocumentoResponse = new TipoDocumentoResponse();
        tipoDocumentoResponse.setTipoDocumento(new TipoDocumento());
        tipoDocumentoResponse.getTipoDocumento().setStatus(StatusEnum.ATIVO);

        Cliente cliente = new Cliente();
        cliente.setId(idRequest.getIdCliente());
        tipoDocumentoResponse.getTipoDocumento().setCliente(cliente);

        BuscaCombos(tipoDocumentoResponse, idRequest);
        response.setData(tipoDocumentoResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<TipoDocumento>>> findAll(@RequestBody TipoDocumento filtro, Pageable pageable) {
        Response<Page<TipoDocumento>> response = new Response<>();
        Page<TipoDocumento> tiposDocumento = tipoDocumentoService.findAll(filtro, pageable);

//        //ordenando as sublists e retirando as inativas
//        for (TipoDocumento tipoDocumento : tiposDocumento) {
//
//           // tipoDocumento.TratamentoPapeis();
//
//            if (!CommonsUtil.semValor(tipoDocumento.getTipoDocumentoTipoClientes()))
//                tipoDocumento.getTipoDocumentoTipoClientes();
//
//        }
        response.setData(tiposDocumento);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        TipoDocumento tipoDocumento = tipoDocumentoService.findById(idRequest.getId());
        if (!Util.verifica(tipoDocumento).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        tipoDocumento.setStatus(StatusEnum.INATIVO);
        tipoDocumentoService.save(tipoDocumento);
        return ResponseEntity.ok(new Response<>());
    }

    private void BuscaCombos(TipoDocumentoResponse tipoDocumentoResponse, IdRequest idRequest) {

        //tipoDocumentoResponse.getListCombos().put("TipoCliente",  TipoClienteEnum.getListCombo(false));
        tipoDocumentoResponse.getListCombos().put("TipoCliente", segmentoService.getListCombo());
        Cliente cliente = new Cliente();
        cliente.setId(idRequest.getIdCliente());

        tipoDocumentoResponse.getListCombos().put("Papel", papelService.getListCombo(cliente));
    }

}

