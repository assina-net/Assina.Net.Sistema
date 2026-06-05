package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.models.RegistroModel;
import br.com.assinanet.response.PessoaCheckResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.UsuarioResponse;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.service.UsuarioService;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registro")
@CrossOrigin(origins = "*")
public class RegistroController {
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    public RegistroController(ClienteService clienteService,
                              UsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Response<Boolean>> create(@RequestBody RegistroModel registro) {
        Response<Boolean> response = new Response<>();
        if (!CommonsUtil.semValor(registro.getRgie()))
            response.setData(true);
        else {
            try {
                clienteService.registrarCliente(registro);
                response.setData(true);
            } catch (Exception e) {
                response.getErrors().add(e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{cpfCnpj}")
    public ResponseEntity<Response<PessoaCheckResponse>> pesquisaClienteCadastrado(@PathVariable("cpfCnpj") String cpfCnpj) {
        Response<PessoaCheckResponse> response = new Response<>();
        Cliente cliente = clienteService.findByCpfCnpj(cpfCnpj);
        if (cliente != null){
            PessoaCheckResponse pessoaCheckResponse = new PessoaCheckResponse(cliente.getPessoa().getNomeRazaoSocial(), cliente.getPessoa().getCpfCnpj());
            response.setData(pessoaCheckResponse);
        }else{
            response.setData(null);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{cpfCnpj}")
    public ResponseEntity<Response<PessoaCheckResponse>> pesquisaUsuarioCadastrado(@PathVariable("cpfCnpj") String cpfCnpj) {
        Response<PessoaCheckResponse> response = new Response<>();
        Usuario usuario = usuarioService.findBycpf(cpfCnpj);
        PessoaCheckResponse pessoaCheckResponse = new PessoaCheckResponse(usuario.getPessoa().getNomeRazaoSocial(), usuario.getPessoa().getCpfCnpj());
        response.setData(pessoaCheckResponse);
        return ResponseEntity.ok(response);
    }

}
