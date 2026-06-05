package br.com.assinanet.controller;

import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.response.Response;
import br.com.assinanet.service.SegmentoService;
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
@RequestMapping("/api/v1/config/segmento")
@CrossOrigin(origins = "*")
public class SegmentoController {

    private final SegmentoService segmentoService;

    public SegmentoController(SegmentoService segmentoService) {
        this.segmentoService = segmentoService;
    }

    @PostMapping
    public ResponseEntity<Response<Segmento>> create(@RequestBody @Valid Segmento segmento) {
        Response<Segmento> response = new Response<>();
        try {
            response.setData(segmentoService.save(segmento));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Segmento>> update(@RequestBody @Valid Segmento segmento) {
        Response<Segmento> response = new Response<>();
        try {
            response.setData(segmentoService.save(segmento));
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<Segmento>> findById(@RequestBody IdRequest idRequest) {
        Response<Segmento> response = new Response<>();

        Segmento segmento = segmentoService.findById(idRequest.getId());
        if (!Util.verifica(segmento).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        response.setData(segmento);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<Segmento>>> findAll(@RequestBody Segmento filtro, Pageable pageable) {
        Response<Page<Segmento>> response = new Response<>();
        Page<Segmento> segmentos = segmentoService.findAll(filtro, pageable);

        response.setData(segmentos);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<Response<String>> delete(@RequestBody IdRequest idRequest) {
        Response<String> response = new Response<>();
        Segmento segmento = segmentoService.findById(idRequest.getId());
        if (!Util.verifica(segmento).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }
        segmento.setStatus(StatusEnum.INATIVO);
        segmentoService.save(segmento);
        return ResponseEntity.ok(new Response<>());
    }

}

