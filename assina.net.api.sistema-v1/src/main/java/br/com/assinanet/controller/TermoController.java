package br.com.assinanet.controller;

import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.request.TermoAceiteRequest;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.TermoAceiteResponse;
import br.com.assinanet.service.UsuarioTermoService;
import br.com.assinanet.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/termo")
@CrossOrigin(origins = "*")
public class TermoController {

    @Autowired
    private final UsuarioTermoService usuarioTermoService;

    @Autowired
    private final HttpServletRequest request;

    public TermoController(UsuarioTermoService usuarioTermoService, HttpServletRequest request) {

        this.usuarioTermoService = usuarioTermoService;
        this.request = request;
    }

    @PostMapping(value = "termosPendentes")
    public @ResponseBody
    ResponseEntity<Response<Boolean>> termosPendentes(@RequestBody Usuario usuario) throws Exception {
        Response<Boolean> response = new Response<>();

        Boolean result = usuarioTermoService.termosPendentes(usuario) ;

        response.setData(result);
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "termosParaAssinatura")
    public @ResponseBody
    ResponseEntity<Response<List<TermoAceiteResponse>>> buscaTermosParaAceite(@RequestBody Usuario usuario) throws Exception {
        Response<List<TermoAceiteResponse>> response = new Response<>();

        List<TermoAceiteResponse> result = new ArrayList<>(0);

        List<SistemaAtributo> termos = usuarioTermoService.termosPendentesAssinatura(usuario);

        termos.forEach(termo -> {
            if (SistemaTipoAtributoEnum.TERMO_DE_USO_SISTEMA.equals(termo.getTipoAtributo().getTipoAtributo())) {
                AdicionaTermo(result, termo, "Termo de Uso");
            } else if (SistemaTipoAtributoEnum.TERMO_DE_PRIVACIDADE_SISTEMA.equals(termo.getTipoAtributo().getTipoAtributo())) {
                AdicionaTermo(result, termo, "Politica de Privacidade");
            }
        });

        response.setData(result);
        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "termosAceite")
    public @ResponseBody
    ResponseEntity<Response<Boolean>> termosAceite(@RequestBody TermoAceiteRequest termoAceiteRequest) throws Exception {

        Response<Boolean> response = new Response<>();
        termoAceiteRequest.setIp(RequestUtil.retornaIpCliente(request));

        Boolean result = usuarioTermoService.termosAceite(termoAceiteRequest);

        response.setData(result);
        return ResponseEntity.ok(response);

    }


    private void AdicionaTermo(List<TermoAceiteResponse> result, SistemaAtributo termoPrivacidade, String s) {
        TermoAceiteResponse termoPrivacidadeResonse = new TermoAceiteResponse();
        termoPrivacidadeResonse.setId(termoPrivacidade.getId());
        termoPrivacidadeResonse.setNome(s);
        termoPrivacidadeResonse.setTipoTermo(termoPrivacidade.getTipoAtributo().getTipoAtributo());
        termoPrivacidadeResonse.setDocumento(usuarioTermoService.carregaDocumentoTermo(termoPrivacidade));
        result.add(termoPrivacidadeResonse);
    }

}


