package br.com.assinanet.service;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.*;
import br.com.assinanet.models.RegistroModel;
import br.com.assinanet.repository.*;
import br.com.assinanet.task.EmailTask;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import br.com.assinanet.util.DateUtil2;
import br.com.assinanet.util.Util;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final SegmentoService segmentoService;
    private final PlanoService planoService;
    private final TipoTelefoneRepository tipoTelefoneRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioClienteRepository usuarioClienteRepository;

    private final PapelRepository papelRepository;

    private final SistemaAtributoService sistemaAtributoService;

    private final SistemaTipoAtributoRepository sistemaTipoAtributoRepository;

    private final EmailService emailService;


    public ClienteService(ClienteRepository repository, SegmentoService segmentoService, PlanoService planoService,
                          TipoTelefoneRepository tipoTelefoneRepository, PlanoRepository planoRepository,
                          UsuarioRepository usuarioRepository, PessoaRepository pessoaRepository,
                          TipoDocumentoRepository tipoDocumentoRepository, PasswordEncoder passwordEncoder,
                          UsuarioClienteRepository usuarioClienteRepository, PapelRepository papelRepository,
                          SistemaAtributoService sistemaAtributoService, SistemaTipoAtributoRepository sistemaTipoAtributoRepository,
                          EmailService emailService
        ) {
        this.repository = repository;
        this.segmentoService = segmentoService;
        this.tipoTelefoneRepository = tipoTelefoneRepository;
        this.planoService = planoService;
        this.planoRepository = planoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioClienteRepository = usuarioClienteRepository;
        this.papelRepository = papelRepository;
        this.sistemaAtributoService = sistemaAtributoService;
        this.sistemaTipoAtributoRepository = sistemaTipoAtributoRepository;
        this.emailService = emailService;
    }

    public Cliente findById(UUID id) {
        return repository.getOne(id);
    }

    public Cliente findByCpfCnpj(String cpfCnpj) {
        return repository.findByCpfCnpj(cpfCnpj);
    }

    public Cliente findByTipoCliente(Segmento segmento) {
        List<Cliente> clientes =repository.findBySegmento(segmento);
        Cliente cliente = null;
        if (clientes != null && clientes.size() > 0)
            cliente = clientes.get(0);

        //return repository.findBySegmento(segmento);
        return cliente;
    }

    public Cliente findBySegmento(Segmento segmentoSistema) {
        List<Cliente> clientes =repository.findBySegmento(segmentoSistema);
        Cliente cliente = null;
        if (clientes != null && clientes.size() > 0)
            cliente = clientes.get(0);

        //return repository.findBySegmento(segmentoSistema);
        return cliente;
    }

    public Page<Cliente> findAll(Cliente filtro, Pageable pageable) {

        if(filtro.getPlano().getId() == null){
            filtro.setPlano(null);
        }


        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Cliente> example = Example.of(filtro, matcher);
        Page<Cliente> clientes = repository.findAll(example, pageable);

        return clientes;
    }

    public Cliente salva(Cliente cliente) {

        if (cliente.getDataInicioContrato() == null) {
            cliente.setDataInicioContrato(DataUtil.getCarimboTempo().getDataCarimboTempo());
        }

        Cliente clienteSistema = retornaClienteSistema();

        Util.verifica(cliente.getPessoa()).ifPresent(pessoa ->
        {
            if (CommonsUtil.semValor(pessoa.getCliente()))
                pessoa.setCliente(clienteSistema);
        });

        Util.verifica(cliente.getPessoa().getPessoaEndereco()).ifPresent(enderecos ->
                enderecos.forEach(endereco -> {
                    endereco.setPessoa(cliente.getPessoa());
                    endereco.setDataAtualizacao(DataUtil.getCarimboTempo().getDataCarimboTempo());
                    if (CommonsUtil.semValor(endereco.getDataCadastramento())) {
                        endereco.setDataCadastramento(endereco.getDataAtualizacao());
                    }
                }));

        Util.verifica(cliente.getPessoa().getPessoaTelefone()).ifPresent(telefones ->
                telefones.forEach(telefone -> {
                    telefone.setPessoa(cliente.getPessoa());
                    telefone.setDataAtualizacao(DataUtil.getCarimboTempo().getDataCarimboTempo());
                    if (CommonsUtil.semValor(telefone.getDataCadastramento())) {
                        telefone.setDataCadastramento(telefone.getDataAtualizacao());
                    }
                }));

        cliente.setSegmento(segmentoService.findById(cliente.getSegmento().getId()));

        cliente.setPlano(planoService.findById(cliente.getPlano().getId()));

        return repository.save(cliente);
    }


    public Cliente retornaClienteSistema() {
        Segmento segmentoSistema = segmentoService.findByIdentificacao("SISTEMA");
        Cliente clienteSistema = findBySegmento(segmentoSistema);
        return clienteSistema;
    }

    public Boolean registrarCliente(RegistroModel registro) throws Exception {
        Cliente cliente = findByCpfCnpj(registro.getCpfCnpj());
        Cliente clienteSistema = retornaClienteSistema();

        if (cliente != null && StatusEnum.ATIVO.equals(cliente.getStatus())) {
            throw new Exception("CPF/CNPJ já cadastrado como cliente em nossa base");
        }
        Pessoa pessoa = null;
        if (cliente == null) {
            pessoa = pessoaRepository.findByCpfCnpj(registro.getCpfCnpj());
        } else {
            pessoa = cliente.getPessoa();
        }
        if (pessoa == null) {
            pessoa = new Pessoa();
            pessoa.setCliente(clienteSistema);
            pessoa.setTipoPessoa(registro.getTipoPessoa());
        }

        Pessoa pessoaUsuario = null;
        if (!CommonsUtil.semValor(registro.getCpfCnpjResponsavel())) {
            pessoaUsuario = pessoaRepository.findByCpfCnpj(registro.getCpfCnpj());
            if (pessoaUsuario == null) {
                pessoaUsuario = new Pessoa();
                pessoaUsuario.setCliente(clienteSistema);
                pessoaUsuario.setTipoPessoa(TipoPessoaEnum.FISICA);
            }
        }
        PessoaTelefone pessoaTelefone = null;
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setPessoa(pessoa);
            Segmento segmento = segmentoService.findByIdentificacao("CONTRATOSIMPLES");
            cliente.setSegmento(segmento);
            cliente.setPlano(null);
            if (TipoPessoaEnum.FISICA.equals(registro.getTipoPessoa())) {
                pessoa.setPessoaTelefone(new ArrayList<>(0));
                pessoaTelefone = new PessoaTelefone();
                pessoaTelefone.setPessoa(pessoa);
                pessoa.getPessoaTelefone().add(pessoaTelefone);
            } else {
                pessoaUsuario.setPessoaTelefone(new ArrayList<>(0));
                pessoaTelefone = new PessoaTelefone();
                pessoaTelefone.setPessoa(pessoaUsuario);
                pessoaUsuario.getPessoaTelefone().add(pessoaTelefone);
            }
        } else {
            if (!CommonsUtil.semValor(pessoa.getPessoaTelefone())) {
                pessoaTelefone = pessoa.getPessoaTelefone().get(0);
            } else {
                pessoa.setPessoaTelefone(new ArrayList<>(0));
                pessoaTelefone = new PessoaTelefone();
                pessoaTelefone.setPessoa(pessoa);
                pessoa.getPessoaTelefone().add(pessoaTelefone);
            }
        }
        pessoa.setNomeRazaoSocial(registro.getNomeRazaoSocial());
        pessoa.setCpfCnpj(registro.getCpfCnpj());
        pessoa.setTipoPessoa(registro.getTipoPessoa());
        pessoa.setCliente(clienteSistema);
        //Bonatte - 2023-11-30
        pessoa.setEmail( registro.getEmail() == null ? "sistema@assina.net" : registro.getEmail());

        if (TipoPessoaEnum.FISICA.equals(registro.getTipoPessoa())) {
            pessoa.setEmail(registro.getEmail());
        } else {
            pessoaUsuario.setNomeRazaoSocial(registro.getNomeRazaoSocialResponsavel());
            pessoaUsuario.setCpfCnpj(registro.getCpfCnpjResponsavel());
            pessoaUsuario.setEmail(registro.getEmail());
        }

        TipoTelefone tipoTelefone = tipoTelefoneRepository.findByIdentificacaoAndCliente("CELULAR", clienteSistema);
        pessoaTelefone.setTipoTelefone(tipoTelefone);
        pessoaTelefone.setNumero(registro.getCelular());
        pessoaTelefone.setDataAtualizacao(DateUtil2.getDataHoraAgora());
        pessoaTelefone.setDataCadastramento(DateUtil2.getDataHoraAgora());
        pessoaTelefone.setStatus(StatusEnum.ATIVO);

        cliente.setStatus(StatusEnum.ATIVO);
        cliente.setIndicacao(registro.getIndicacao());
        Plano plano = planoRepository.findById(registro.getIdPlano()).orElse(null);
        cliente.setPlano(plano);
        //Bonatte - 2023-11-30
        cliente.setNaoMostrar(false);
        cliente = salva(cliente);

        //criando o usuario e vinculando ao cliente
        Usuario usuario = null;
        String senha = null;
        if (TipoPessoaEnum.FISICA.equals(registro.getTipoPessoa()))
            usuario = usuarioRepository.findBycpf(registro.getCpfCnpj());
        else
            usuario = usuarioRepository.findBycpf(registro.getCpfCnpjResponsavel());

        if (usuario == null) {
            usuario = new Usuario();

            //Bonatte - 2023-11-30
            usuario.setStatus(StatusEnum.ATIVO);

            if (TipoPessoaEnum.FISICA.equals(registro.getTipoPessoa())) {
                usuario.setLogin(registro.getCpfCnpj());
                usuario.setPessoa(pessoa);
            } else {
                usuario.setLogin(registro.getCpfCnpjResponsavel());
                usuario.setPessoa(pessoaUsuario);
            }
            senha = Util.GenerateCommonLangPassword(true, false);
            usuario.setSenha(passwordEncoder.encode(senha));
        }
        usuario.setPerfil(PerfilEnum.ROLE_USUARIO);
        usuario = usuarioRepository.save(usuario);

        UsuarioCliente usuarioCliente = null;
        if (!CommonsUtil.semValor(usuario.getId()) && !CommonsUtil.semValor(cliente.getId()))
            usuarioCliente = usuarioClienteRepository.findByUsuarioAndCliente(usuario, cliente);

        if (usuarioCliente == null) {
            usuarioCliente = new UsuarioCliente();
            usuarioCliente.setCliente(cliente);
            usuarioCliente.setUsuario(usuario);
        }
        usuarioCliente.setStatus(StatusEnum.ATIVO);
        usuarioCliente.setPerfil(PerfilEnum.ROLE_ADMIN_CLIENTE);

        usuarioCliente = usuarioClienteRepository.save(usuarioCliente);

        //envia e-mail usuario
        emailService.EnviaEmailNovoClienteUsuario(cliente, usuario, senha);
//        Runnable runnable = new EmailTask.EnviaNovoCliente(cliente, usuario, senha);
//        Thread thread = new Thread(runnable);
//        thread.start();

        //Criando tipo de documento padrao para o cliente
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setCliente(cliente);
        //Bonatte - 2023-11-30
        //tipoDocumento.setNome("Documento Padrão");
        tipoDocumento.setNome("CONTRATO");
        tipoDocumento.setIdentificacao("CONTRATO");
        tipoDocumento.setAssina(true);
        tipoDocumento.setValidacaoOnLine(true);
        tipoDocumento.setQrcode(true);
        tipoDocumento.setStatus(StatusEnum.ATIVO);
        tipoDocumento = this.tipoDocumentoRepository.save(tipoDocumento);


        //Bonatte - 2023-11-30
        //CONTRATANTE
        Papel contratante = new Papel();
        contratante.setAssina(true);
        contratante.setCliente(cliente);
        contratante.setIdentificacao("CONTRATANTE");
        contratante.setNome("CONTRATANTE");
        contratante.setStatus(StatusEnum.ATIVO);
        contratante = this.papelRepository.save(contratante);

        TipoDocumentoPapel tipoDocumentoPapelContratante = new TipoDocumentoPapel();
        tipoDocumentoPapelContratante.setTipoDocumento(tipoDocumento);
        tipoDocumentoPapelContratante.setPapel(contratante);
        tipoDocumentoPapelContratante.setToken(true);
        tipoDocumentoPapelContratante.setCertificate(true);

        tipoDocumento.getPapeis().add(tipoDocumentoPapelContratante);

        //CONTRATADO
        Papel contratado = new Papel();
        contratado.setAssina(true);
        contratado.setCliente(cliente);
        contratado.setIdentificacao("CONTRATADO");
        contratado.setNome("CONTRATADO");
        contratado.setStatus(StatusEnum.ATIVO);
        contratado = this.papelRepository.save(contratado);

        TipoDocumentoPapel tipoDocumentoPapelContratada = new TipoDocumentoPapel();
        tipoDocumentoPapelContratada.setTipoDocumento(tipoDocumento);
        tipoDocumentoPapelContratada.setPapel(contratado);
        tipoDocumentoPapelContratada.setToken(true);
        tipoDocumentoPapelContratada.setCertificate(true);

        tipoDocumento.getPapeis().add(tipoDocumentoPapelContratada);

        //TESTEMUNHA
        Papel testemunha = new Papel();
        testemunha.setAssina(true);
        testemunha.setCliente(cliente);
        testemunha.setIdentificacao("TESTEMUNHA");
        testemunha.setNome("TESTEMUNHA");
        testemunha.setStatus(StatusEnum.ATIVO);
        testemunha = this.papelRepository.save(testemunha);

        TipoDocumentoPapel tipoDocumentoPapelTestemunha = new TipoDocumentoPapel();
        tipoDocumentoPapelTestemunha.setTipoDocumento(tipoDocumento);
        tipoDocumentoPapelTestemunha.setPapel(testemunha);
        tipoDocumentoPapelTestemunha.setToken(true);
        tipoDocumentoPapelTestemunha.setCertificate(true);
        tipoDocumento.getPapeis().add(tipoDocumentoPapelTestemunha);

        tipoDocumento = this.tipoDocumentoRepository.save(tipoDocumento);

        List<SistemaTipoAtributoEnum> atributosEmail = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL);
        List<SistemaTipoAtributoEnum> atributosSms = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_SMS);
        List<SistemaTipoAtributoEnum> atributosWhatsApp = SistemaTipoAtributoEnum.getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP);

        List<SistemaTipoAtributoEnum> atributos = new ArrayList<>();
        if (atributosEmail != null)
            atributos.addAll(atributosEmail);
        if (atributosSms != null)
            atributos.addAll(atributosSms);
        if (atributosWhatsApp != null)
            atributos.addAll(atributosWhatsApp);

        for (SistemaTipoAtributoEnum atributo : atributos) {
            SistemaAtributo sistemaAtributoSistema = sistemaAtributoService.getSistemaAtributo(atributo, clienteSistema);

           if (sistemaAtributoSistema != null) {
               SistemaAtributo sistemaAtributoCliente = new SistemaAtributo();
               sistemaAtributoCliente.setCliente(cliente);
               sistemaAtributoCliente.setTipoAtributo(sistemaAtributoSistema.getTipoAtributo());
               sistemaAtributoCliente.setValorAtributo(sistemaAtributoSistema.getValorAtributo());
               sistemaAtributoCliente.setDataAlteracao(DateUtil2.getDataHoraAgora());
               sistemaAtributoService.save(sistemaAtributoCliente);
           }
        }

        //SistemaTipoAtributo sistemaTipoAtributoAssinaSMS = sistemaTipoAtributoRepository.findByTipoAtributo(SistemaTipoAtributoEnum.ASSINAR_VIA_SMS);


        return true;
    }

}
