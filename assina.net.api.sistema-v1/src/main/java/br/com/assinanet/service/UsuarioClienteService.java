package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioCliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.UsuarioClienteRepository;
import br.com.assinanet.repository.UsuarioRepository;
import br.com.assinanet.request.UsuarioClienteRequest;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.GsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Function;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class UsuarioClienteService {

    private final UsuarioClienteRepository usuarioClienteRepository;
    private final UsuarioRepository usuarioRepository;

    private final UsuarioService usuarioService;
    private final ClienteService clienteService;

    public UsuarioClienteService(UsuarioClienteRepository usuarioClienteRepository, UsuarioRepository usuarioRepository, UsuarioService usuarioService, ClienteService clienteService) {
        this.usuarioClienteRepository = usuarioClienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }


    public UsuarioCliente findById(UUID id) {
        return usuarioClienteRepository.getOne(id);
    }

    public UsuarioCliente findByUsuarioCliente(Usuario usuario, Cliente cliente) {
        return usuarioClienteRepository.findByUsuarioAndClienteAndStatus(usuario, cliente, StatusEnum.ATIVO);
    }

    public UsuarioCliente findByUsuarioId(UsuarioCliente usuarioCliente) {
        return usuarioClienteRepository.findByUsuarioIdAndCliente(usuarioCliente.getUsuario().getId(), usuarioCliente.getCliente());
    }

    public Usuario save(UsuarioCliente usuarioCliente) {

        Usuario usuario = usuarioCliente.getUsuario();
        usuario = this.usuarioService.save(usuario);

        UsuarioCliente usuarioClienteVerifica = findByUsuarioCliente(usuario, usuarioCliente.getCliente());
        if (usuarioClienteVerifica == null) {
            usuarioCliente.setUsuario(usuario);
            Cliente cliente = clienteService.findById(usuarioCliente.getCliente().getId());
            usuarioCliente.setCliente(cliente);
            usuarioCliente.setStatus(StatusEnum.ATIVO);
            usuarioClienteRepository.save(usuarioCliente);
        }
       // usuarioCliente.setPerfil(usuario.getPerfil());
        //usuarioClienteRepository.save(usuarioCliente);

        return usuario;

    }

    public Page<Usuario> findAll(UsuarioClienteRequest filtro, Pageable pageable) {

        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();

        UsuarioCliente filtroUsuarioCliente = GsonUtil.fromJson( GsonUtil.toJson(filtro), UsuarioCliente.class);

        if (!CommonsUtil.semValor(filtro.getCliente())) {
            Example<UsuarioCliente> example = Example.of(filtroUsuarioCliente, matcher);
            Page<UsuarioCliente> usuarioClientePage = usuarioClienteRepository.findAll(example, pageable);
            Page<Usuario> usuarioPage = usuarioClientePage.map(new Function<UsuarioCliente, Usuario>() {
                @Override
                public Usuario apply(UsuarioCliente entity) {
                    Usuario dto = entity.getUsuario();
                    //seta o perfil e o status do ClienteUsuario ao usuario enviado para a tela.
                    dto.setPerfil(entity.getPerfil());
                    dto.setStatus(entity.getStatus());
                    // Conversion logic
                    return dto;
                }
            });
            return usuarioPage;
        } else if (!PerfilEnum.ROLE_ADMIN.equals(filtro.getPerfilClienteSelecionado())) {
            Usuario pesquisaUsuario = new Usuario();
            BeanUtils.copyProperties(filtro.getUsuario(), pesquisaUsuario);
            Example<Usuario> example = Example.of(pesquisaUsuario, matcher);
            Page<Usuario> usuarioPage = usuarioRepository.findAll(example, pageable);
            return usuarioPage;
        }
        return null;

    }

}

