package br.com.assinanet.security.controller;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.models.UsuarioClienteModel;
import br.com.assinanet.response.Response;
import br.com.assinanet.response.UsuarioResponse;
import br.com.assinanet.security.jwt.JwtAuthenticationRequest;
import br.com.assinanet.security.jwt.JwtTokenUtil;
import br.com.assinanet.security.model.CurrentUser;
import br.com.assinanet.security.service.JwtUserDetailsService;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.service.UsuarioService;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import br.com.assinanet.util.DateUtil2;
import br.com.assinanet.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:51
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1")
public class AuthenticationRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService usuarioDetailsService;
    private final UsuarioService service;
    private final ClienteService clienteService;

    @Autowired
    public AuthenticationRestController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtUserDetailsService usuarioDetailsService, UsuarioService service, ClienteService clienteService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.usuarioDetailsService = usuarioDetailsService;
        this.service = service;
        this.clienteService = clienteService;
    }

    @PostMapping(value = "auth")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getLogin(),
                        authenticationRequest.getSenha()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails usuarioDetails = usuarioDetailsService.loadUserByUsername(authenticationRequest.getLogin());
        final String token = jwtTokenUtil.generateToken(usuarioDetails);
        final Usuario usuario = service.findByLogin(authenticationRequest.getLogin());
        if (!CommonsUtil.semValor(usuario.getChaveEsqueceuSenha())) {
            usuario.setChaveEsqueceuSenha(null);
            usuario.setValidadeEsqueceuSenha(null);
            service.save(usuario);
        }
        List<UsuarioClienteModel> clientes = service.getClientes(usuario);

        //validando se o perfil nao for assinador se tem algum cliente com o mesmo peril, senão vira assinador e salva na tabela
        if (clientes.stream().filter(c -> c.getPerfil() == usuario.getPerfil()).findAny().orElse(null) == null) {
            usuario.setPerfil(PerfilEnum.ROLE_ASSINADOR);
        }
        usuario.setPrimeiraTentativaAcesso(null);
        usuario.setQuantidadeTentativaAcesso(0);
        usuario.setEnvioEmailTentativaAcesso(false);
        service.save(usuario);

        return ResponseEntity.ok(new CurrentUser(token, new UsuarioResponse(usuario), clientes));
        //return ResponseEntity.ok(new CurrentUser(token, new UsuarioResponse(usuario), null));
    }

    @PostMapping(value = "lost")
    public ResponseEntity<Response<?>> lostPassword(@RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {
        Response<String> response = new Response<>();
        try {
            service.enviarRecuperacaoSenha(authenticationRequest.getLogin());
            response.setData("Recuperação de senha enviada para o e-mail cadastrado ");
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "validarTokenAlterSenha")
    public ResponseEntity<Response<?>> ValidarTokenAlterSenha(@RequestBody String tokenRequest) {
        Response<CurrentUser> response = new Response<>();
        Usuario usuario = service.findByChaveEsqueceuSenha(tokenRequest);

        if (Util.verifica(usuario).isEmpty()) {
            response.getErrors().add("Token não encontrado");
            return ResponseEntity.badRequest().body(response);
        } else {
            CarimboTempoModel carimboTempoModel = DataUtil.getCarimboTempo();
            if (DateUtil2.getMinutesBetweenDates(usuario.getValidadeEsqueceuSenha(), carimboTempoModel.getDataCarimboTempo()) > 30) {
                response.getErrors().add("Token expirado.<br>Favor solicitar novamente.");
                return ResponseEntity.badRequest().body(response);
            }
        }

        final UserDetails usuarioDetails = usuarioDetailsService.loadUserByUsername(usuario.getLogin());
        final String token = jwtTokenUtil.generateToken(usuarioDetails);
        CurrentUser currentUser = new CurrentUser(token, new UsuarioResponse(usuario), null);
        response.setData(currentUser);
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "refresh")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String usuarioname = jwtTokenUtil.getUsernameFromToken(token);
        final Usuario usuario = service.findByLogin(usuarioname);
        List<UsuarioClienteModel> clientes = service.getClientes(usuario);

        //validando se o perfil nao for assinador se tem algum cliente com o mesmo peril, senão vira assinador e salva na tabela
        if (clientes.stream().filter(c -> c.getPerfil() == usuario.getPerfil()).findAny().orElse(null) == null) {
            usuario.setPerfil(PerfilEnum.ROLE_ASSINADOR);
            service.save(usuario);
        }


        if (jwtTokenUtil.canTokenBeRefreshed(token)) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new CurrentUser(refreshedToken, new UsuarioResponse(usuario), clientes));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping(value = "gerarTokenIntegracao/{idCliente}")
    public String gerarTokenIntegracao(@PathVariable("idCliente")  UUID idCliente) {
        Cliente cliente = clienteService.findById(idCliente);
        if (CommonsUtil.semValor(cliente))
            return null;
        return jwtTokenUtil.generateTokenCliente(cliente);
    }

}

