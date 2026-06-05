package br.com.assinanet.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioCliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.models.UsuarioClienteModel;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.request.UsuarioClienteRequest;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.UsuarioClienteResponse;
import br.com.assinanet.response.UsuarioEditarResponse;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.service.PessoaTelefoneService;
import br.com.assinanet.service.UsuarioClienteService;
import br.com.assinanet.service.UsuarioService;
import br.com.assinanet.task.EmailTask;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/usuario")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioClienteService usuarioClienteService;
    private final PasswordEncoder passwordEncoder;
    private final PessoaTelefoneService pessoaTelefoneService;
    private final ClienteService clienteService;

    public UsuarioController(UsuarioService usuarioService, UsuarioClienteService usuarioClienteService,
                             ClienteService clienteService, PasswordEncoder passwordEncoder,
                              PessoaTelefoneService pessoaTelefoneService) {
        this.usuarioService = usuarioService;
        this.usuarioClienteService = usuarioClienteService;
        this.passwordEncoder = passwordEncoder;
        this.pessoaTelefoneService = pessoaTelefoneService;
        this.clienteService = clienteService;
        //System.out.println("Construtor UsuarioController - usuario: " + usuarioService + " - Senha: " + passwordEncoder);
    }

    @PostMapping
    public ResponseEntity<Response<Usuario>> create(@RequestBody @Valid UsuarioClienteRequest usuario) {
        Response<Usuario> response = new Response<>();
        Thread thread = null;


        try {
            Usuario usuarioCadastrado = null;

            usuarioCadastrado = usuarioService.findBycpf(usuario.getUsuario().getPessoa().getCpfCnpj());
            if (usuarioCadastrado != null) {

                if (StatusEnum.ATIVO.equals(usuarioCadastrado.getStatus()))
                    usuario.getUsuario().setSenha(usuarioCadastrado.getSenha());

                if (CommonsUtil.semValor(usuario.getUsuario().getId()))
                    usuario.getUsuario().setId(usuarioCadastrado.getId());

            }


            String senha = Util.GenerateCommonLangPassword(true, false);
            if (CommonsUtil.semValor(usuario.getUsuario().getSenha())) {
                usuario.getUsuario().setSenha(senha);
                //envia e-mail usuario
                Runnable runnable = new EmailTask.EnviaNovoUsario(null, usuario.getUsuario(), senha);
                thread = new Thread(runnable);
                usuario.getUsuario().setSenha(passwordEncoder.encode(usuario.getUsuario().getSenha()));
            }


            UsuarioCliente usuarioCliente = new UsuarioCliente();
            BeanUtils.copyProperties(usuario, usuarioCliente );

            UsuarioCliente usuarioClienteSalvo = usuarioService.save(usuarioCliente);
            Usuario usuarioSalvo = usuarioService.findById(usuarioCliente.getUsuario().getId());
            usuarioClienteSalvo.setUsuario(usuarioSalvo);

            if (!PerfilEnum.ROLE_ASSINADOR.equals(usuario.getPerfilClienteSelecionado())) {
                usuarioClienteService.save(usuarioClienteSalvo);
            }

            response.setData(usuarioClienteSalvo.getUsuario());

        } catch (DuplicateKeyException dE) {
            response.getErrors().add("Login já registrado!");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        if (thread != null) {
            thread.start();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Usuario>> update(@RequestBody @Valid UsuarioClienteRequest usuario) {
        Response<Usuario> response = new Response<>();
        try {

            Util.verifica(usuario.getUsuario().getSenha()).ifPresent(password -> usuario.getUsuario().setSenha(passwordEncoder.encode(password)));

            UsuarioCliente usuarioCliente = new UsuarioCliente();
            BeanUtils.copyProperties(usuario, usuarioCliente );
            UsuarioCliente usuarioSalvo = usuarioService.save(usuarioCliente);

            if (!PerfilEnum.ROLE_ASSINADOR.equals(usuario.getPerfilClienteSelecionado())) {
                usuarioClienteService.save(usuarioSalvo);
            }

            response.setData(usuarioSalvo.getUsuario());

        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<UsuarioEditarResponse>> findById(@RequestBody IdRequest idRequest) {
        Response<UsuarioEditarResponse> response = new Response<>();
        UsuarioEditarResponse usuarioEditarResponse = new UsuarioEditarResponse();
        Usuario usuario = usuarioService.findById(idRequest.getId());
        if (!Util.verifica(usuario).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }

        usuarioEditarResponse.setUsuario(usuario);
        BuscaCombos(usuarioEditarResponse.getListCombos(), idRequest, false);
        response.setData(usuarioEditarResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "new")
    public ResponseEntity<Response<UsuarioEditarResponse>> carregaCombos(@RequestBody IdRequest idRequest) {
        Response<UsuarioEditarResponse> response = new Response<>();

        UsuarioEditarResponse usuarioEditarResponse = new UsuarioEditarResponse();

        usuarioEditarResponse.setUsuario(new Usuario());
        usuarioEditarResponse.getUsuario().setStatus(StatusEnum.ATIVO);
        usuarioEditarResponse.getUsuario().setPessoa(new Pessoa());
        usuarioEditarResponse.getUsuario().getPessoa().setCliente(new Cliente());

        BuscaCombos(usuarioEditarResponse.getListCombos(), idRequest, false);
        response.setData(usuarioEditarResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findByIdEditar")
    public ResponseEntity<Response<UsuarioClienteResponse>> findByIdEditar(@RequestBody UsuarioCliente filtro) {
        Response<UsuarioClienteResponse> response = new Response<>();

        UsuarioCliente usuarioCliente = usuarioClienteService.findByUsuarioId(filtro);
        if (usuarioCliente == null) {
            Usuario usuario = usuarioService.findById(filtro.getUsuario().getId());
            usuarioCliente = filtro;
            usuarioCliente.setUsuario(usuario);
            usuarioCliente.setPerfil(PerfilEnum.ROLE_ASSINADOR);
            usuarioCliente.setStatus(StatusEnum.ATIVO);
        }

        //sera a senha
        usuarioCliente.getUsuario().setSenha(null);

        UsuarioClienteResponse usuarioClienteResponse = new UsuarioClienteResponse(usuarioCliente);


        BuscaCombos(usuarioClienteResponse.getListCombos(), null, false);

        response.setData(usuarioClienteResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "inativar")
    public ResponseEntity<Response<String>> inativar(@RequestBody UsuarioCliente usuarioCliente) {
        Response<String> response = new Response<>();
        UsuarioCliente usuarioCliente1 = null;
        if (!CommonsUtil.semValor(usuarioCliente.getId())) {
            usuarioCliente1 = usuarioClienteService.findById(usuarioCliente.getId());
        } else {
            usuarioCliente1 = usuarioClienteService.findByUsuarioCliente(usuarioCliente.getUsuario(), usuarioCliente.getCliente());
        }

        if (!Util.verifica(usuarioCliente1).isPresent()) {
            response.getErrors().add("Registro não encontrado!!");
            return ResponseEntity.badRequest().body(response);
        }
        usuarioCliente1.setStatus(StatusEnum.INATIVO);


        usuarioService.save(usuarioCliente1);

        return ResponseEntity.ok(new Response<>());
    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<Usuario>>> findAll(@RequestBody UsuarioClienteRequest filtro, Pageable pageable) {
        Response<Page<Usuario>> response = new Response<>();
        Page<Usuario> usuarios = usuarioClienteService.findAll(filtro, pageable);
        response.setData(usuarios);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "getClientes")
    public ResponseEntity<Response<List<UsuarioClienteModel>>> getClientes(@RequestBody Usuario usuario) {
        Response<List<UsuarioClienteModel>> response = new Response<>();
        List<UsuarioClienteModel> clientes = usuarioService.getClientes(usuario);
        response.setData(clientes);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "getUsuarioCpfCnpj")
    public ResponseEntity<Response<Usuario>> getUsarioCpfCnpj(@RequestBody Usuario usuario) {
        Response<Usuario> response = new Response<>();
        Usuario usuarioResponse = usuarioService.findBycpf(usuario.getPessoa().getCpfCnpj());
        response.setData(usuarioResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "trocarSenha")
    public ResponseEntity<Response<String>> trocarSenha(@RequestBody @Valid Usuario usuario) {
        Response<String> response = new Response<>();
        try {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            usuarioService.save(usuario);
            response.setData("Senha alterada com sucesso !");
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    private void BuscaCombos(Map<String, List<ComboListResponse>> clienteResponse, IdRequest idRequest, boolean telaListagem) {
        Cliente cliente = new Cliente();
        if (idRequest != null)
            cliente.setId(idRequest.getIdCliente());
        else
            cliente = clienteService.retornaClienteSistema();

        if (!telaListagem) {
            clienteResponse.put("TipoTelefone", pessoaTelefoneService.getListCombo(cliente));
        }

    }
}

