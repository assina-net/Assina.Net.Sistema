package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioCliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.SistemaTipoAtributoCategoriaEnum;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.models.UsuarioClienteModel;
import br.com.assinanet.repository.*;
import br.com.assinanet.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Service
public class UsuarioService {


    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioClienteRepository usuarioClienteRepository;
    private final SistemaAtributoRepository sistemaAtributoRepository;
    private final PessoaTelefoneRepository pessoaTelefoneRepository;


    public UsuarioService(PasswordEncoder passwordEncoder, EmailService emailService,
                          ClienteService clienteService,
                          UsuarioRepository usuarioRepository,
                          PessoaRepository pessoaRepository,
                          UsuarioClienteRepository usuarioClienterepository,
                          ClienteRepository clienteRepository,
                          SistemaAtributoRepository sistemaAtributoRepository, PessoaTelefoneRepository pessoaTelefoneRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.clienteService = clienteService;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioClienteRepository = usuarioClienterepository;
        this.clienteRepository = clienteRepository;
        this.sistemaAtributoRepository = sistemaAtributoRepository;
        this.pessoaTelefoneRepository = pessoaTelefoneRepository;
    }

    public Usuario findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    public Usuario findBycpf(String login) {
        return usuarioRepository.findBycpf(login);
    }

    public UsuarioCliente save(UsuarioCliente usuarioCliente) {

        Cliente clienteSistema = clienteService.retornaClienteSistema();
        if (usuarioCliente.getUsuario().getPessoa().getCliente() == null) {
            usuarioCliente.getUsuario().getPessoa().setCliente(clienteSistema);
        }


        //se o cliente é do sistema marca o mesmo perifil para o usuario
        if (CommonsUtil.mesmoValor(clienteSistema, usuarioCliente.getCliente())) {
            usuarioCliente.getUsuario().setPerfil(usuarioCliente.getPerfil());
        }


        Usuario usuario = usuarioCliente.getUsuario();
        if (!CommonsUtil.mesmoValor(usuario.getLogin(), "ADMIN")) {
            usuario.setLogin(usuario.getPessoa().getCpfCnpj());
        }


        save(usuario);


        return usuarioCliente;

    }

    public Usuario save(Usuario usuario) {
        if (Util.verifica(usuario.getId()).isPresent() && Util.verifica(usuario.getSenha()).isEmpty()) {
            Usuario user = findById(usuario.getId());
            usuario.setSenha(user.getSenha());
        }
        if (Util.verifica(usuario.getPessoa().getId()).isEmpty()) {
            usuario.setPessoa(pessoaRepository.save(usuario.getPessoa()));
        }

        List<UUID> telefonesAtivos = new ArrayList<>(0);

        Util.verifica(usuario.getPessoa().getPessoaTelefone()).ifPresent(telefones ->
                telefones.forEach(telefone -> {
                    if (telefone.getId() != null)
                        telefonesAtivos.add(telefone.getId());
                    telefone.setPessoa(usuario.getPessoa());
                    telefone.setDataAtualizacao(DataUtil.getCarimboTempo().getDataCarimboTempo());
                    if (CommonsUtil.semValor(telefone.getDataCadastramento())) {
                        telefone.setDataCadastramento(telefone.getDataAtualizacao());
                    }
                }));


        if (usuario.getPessoa().getId() != null)
            if (!CommonsUtil.semValor(telefonesAtivos)) {
                pessoaTelefoneRepository.desativaTelefonesExcluidas(usuario.getPessoa(), telefonesAtivos);
            } else {
                pessoaTelefoneRepository.desativaTelefonesExcluidas(usuario.getPessoa());
            }

        return usuarioRepository.save(usuario);
    }

    public Usuario findById(UUID id) {
        return usuarioRepository.getOne(id);
    }

    public Usuario findByChaveEsqueceuSenha(String Token) {
        return usuarioRepository.findByChaveEsqueceuSenha(Token);
    }

    public void delete(UUID id) {
        usuarioRepository.deleteById(id);
    }

    public List<UsuarioClienteModel> getClientes(Usuario usuario) {

        List<UsuarioClienteModel> result = new ArrayList<>(0);

        List<SistemaTipoAtributoEnum> sistemaAtributos = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.TELA_PARAMETROS);
        List<Cliente> clientes = new ArrayList<>();

        sistemaAtributos.addAll(SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE));
        sistemaAtributos.addAll(SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_SMS));
        sistemaAtributos.addAll(SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL));
        sistemaAtributos.addAll(SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP));

        //se o perfil é admin ve todas as empresas com perfil de admin
        if (PerfilEnum.ROLE_ADMIN.equals(usuario.getPerfil())) {
            //List<Cliente> clientes = clienteRepository.findByStatus(StatusEnum.ATIVO);
            clientes = clienteRepository.findByStatusWithAssociations(StatusEnum.ATIVO);

            clientes.forEach(cliente -> {
                UsuarioClienteModel usuarioClienteModel = new UsuarioClienteModel(cliente, usuario.getPerfil());
                result.add(usuarioClienteModel);
            });
        } else {
            List<UsuarioCliente> usuarioClientes = usuarioClienteRepository.getClientes(usuario);

            //clientes com acesso
            usuarioClientes.forEach(usuarioCliente -> {

                UsuarioClienteModel usuarioClienteModel = new UsuarioClienteModel(usuarioCliente.getCliente(), usuarioCliente.getPerfil());
                result.add(usuarioClienteModel);

            });

            //clientes com solicitacaoes de assinatura
            List<Cliente> assinaturaClientes = clienteRepository.findByContratoCpfCnpj(usuario.getPessoa().getCpfCnpj());
            assinaturaClientes.forEach(cliente -> {
                Optional<UsuarioClienteModel> pesquisa = result.stream().filter(x -> x.getCliente().getId() == cliente.getId()).findAny();
                if (pesquisa.isEmpty()) {
                    result.add(new UsuarioClienteModel(cliente, PerfilEnum.ROLE_ASSINADOR));
                }
            });
        }

        // Buscar todos os atributos para os clientes em uma única consulta
        List<SistemaAtributo> listaAtributos = sistemaAtributoRepository.buscaSistemaAtributosParaClientes(sistemaAtributos, clientes);

        // Agrupar os atributos por cliente
        Map<UUID, List<SistemaAtributo>> atributosPorCliente = listaAtributos.stream()
                .collect(Collectors.groupingBy(siat -> siat.getCliente().getId()));

        // Associar os atributos aos modelos de cliente
        result.forEach(usuarioClienteModel -> {
            List<SistemaAtributo> atributosCliente = atributosPorCliente.getOrDefault(usuarioClienteModel.getCliente().getId(), Collections.emptyList());
            atributosCliente.forEach(atributo ->
                    usuarioClienteModel.getSistemaAtributo().put(atributo.getTipoAtributo().getTipoAtributo(), atributo.getValorAtributo())
            );
        });

//        result.forEach(usuarioClienteModel -> {
//            //preenche parametros dos clientes
//            List<SistemaAtributo> listaATributos = sistemaAtributoRepository.buscaSistemaAtributos(sistemaAtributos, usuarioClienteModel.getCliente());
//            listaATributos.forEach(atributo -> usuarioClienteModel.getSistemaAtributo().put(atributo.getTipoAtributo().getTipoAtributo(), atributo.getValorAtributo())
//            );
//        });

        result.sort(Comparator.comparing(o -> o.getCliente().getPessoa().getNomeRazaoSocial().toUpperCase()));

        return result;
    }

    public String enviarRecuperacaoSenha(String login) throws IOException, MessagingException {
        Usuario usuario = findByLogin(login);

        String frase = usuario.getId() + DataUtil.getCarimboTempo().getCarimboTempo();
        String strongAESKey = Util.ToHex(Util.gerarHash(frase, "SHA-256"));

        usuario.setChaveEsqueceuSenha(strongAESKey);
        CarimboTempoModel carimboTempoModel = DataUtil.getCarimboTempo();
        usuario.setValidadeEsqueceuSenha(DateUtil2.adicionarPeriodo(carimboTempoModel.getDataCarimboTempo(), 30, Calendar.MINUTE));

        emailService.EnviaRecuperarSenha(usuario);
        save(usuario);


        return "";
    }

    public void desbloqueio(String cpfCnpj) {
        Usuario usuario = findByLogin(cpfCnpj);
        if (usuario != null) {
            usuario.setPrimeiraTentativaAcesso(null);
            usuario.setQuantidadeTentativaAcesso(0);
            usuario.setEnvioEmailTentativaAcesso(false);
            save(usuario);
        }
    }


}
