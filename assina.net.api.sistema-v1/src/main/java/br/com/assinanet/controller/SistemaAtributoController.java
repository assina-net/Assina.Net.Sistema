package br.com.assinanet.controller;

import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.request.sistemaAtributo.FiltroSistemaAtributo;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente;
import br.com.assinanet.service.SistemaAtributoService;
import br.com.assinanet.util.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@RestController
@RequestMapping("/api/v1/config/sistemaAtributo")
@CrossOrigin(origins = "*")
public class SistemaAtributoController {

    private final SistemaAtributoService sistemaAtributoService;

    public SistemaAtributoController(SistemaAtributoService sistemaAtributoService) {
        this.sistemaAtributoService = sistemaAtributoService;
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<SistemaAtributoCliente>> findById(@RequestBody IdRequest idRequest) {

        Response<SistemaAtributoCliente> response = new Response<>();
        SistemaAtributoCliente sistemaAtributo = sistemaAtributoService.findByIdManutencao(idRequest);
        if (!Util.verifica(sistemaAtributo).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + sistemaAtributo.getId());
            return ResponseEntity.badRequest().body(response);
        }
        response.setData(sistemaAtributo);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<SistemaAtributoCliente>>> findAll(@RequestBody FiltroSistemaAtributo filtro, Pageable pageable) {

        Response<Page<SistemaAtributoCliente>> response = new Response<>();
        List<SistemaTipoAtributoEnum> atributos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(filtro.getCategoria());

        Page<SistemaAtributoCliente> sistemaAtributo = sistemaAtributoService.findAllManutencao(atributos, filtro.getCliente(), pageable);
        response.setData(sistemaAtributo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Response<SistemaAtributoCliente>> create(@RequestBody @Valid SistemaAtributoCliente sistemaAtributoCliente) {
        Response<SistemaAtributoCliente> response = new Response<>();
        try {
            response.setData(sistemaAtributoService.save(sistemaAtributoCliente));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<SistemaAtributoCliente>> update(@RequestBody @Valid SistemaAtributoCliente sistemaAtributoCliente) {
        Response<SistemaAtributoCliente> response = new Response<>();
        try {
            response.setData(sistemaAtributoService.save(sistemaAtributoCliente));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        try {
            sistemaAtributoService.deleteManutencao(idRequest);
            response.setData("Excluido");
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

}

