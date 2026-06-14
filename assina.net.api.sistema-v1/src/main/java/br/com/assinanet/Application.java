package br.com.assinanet;


import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.repository.*;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.storage.StorageFactory;
import br.com.assinanet.storage.Storage;
import br.com.assinanet.storage.TipoStorage;
import br.com.assinanet.util.CommonsUtil;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

@SpringBootApplication
@EnableAsync
public class Application extends SpringBootServletInitializer {

    /**
     * Logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(Application.class);

    //@Autowired
    //private ContratoService contratoService;
    //  @Autowired
    //private EmailFilaService emailFilaService;
    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PlanoRepository planoRepository;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        LOGGER.info("Iniciando a app...");
        SpringApplication.run(Application.class, args);
        LOGGER.info("App iniciada...");

        //uploadStorage("teste/cliente1/Fatura-Local.pdf", "C:\\Users\\Bonatte\\Downloads\\Fatura-Local.pdf");
        //downloadStorage("teste/cliente1/Fatura-Local.pdf", "C:\\Users\\Bonatte\\Downloads\\Fatura-Local-Down.pdf");
        //uploadStorage("teste/cliente1/Fatura-Azure.pdf", "C:\\Users\\Bonatte\\Downloads\\Fatura-Azure.pdf");
        //downloadStorage("teste/cliente1/Fatura-Azure.pdf", "C:\\Users\\Bonatte\\Downloads\\Fatura-Local-Azure-Down.pdf");

    }

    public static void uploadStorage(String storageFilePath, String localFilePath) {
        try {
            Storage storage = StorageFactory.createStorage(TipoStorage.AZURE);
            byte[] fileBytes = Files.readAllBytes(Paths.get(localFilePath));
            storage.uploadFile(storageFilePath, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadStorage(String storageFilePath, String localFilePath) {
        Storage storage = StorageFactory.createStorage(TipoStorage.AZURE);
        try {
            byte[] bytes =  storage.downloadFile(storageFilePath);
            OutputStream out = new FileOutputStream(localFilePath);

            out.write(bytes);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }


    private static void testeContainerAzure() {
        LOGGER.info("--- Acessando o container");

        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        if (connectStr == null || connectStr.trim().isEmpty()) {
            connectStr = System.getProperty("AZURE_STORAGE_CONNECTION_STRING");
        }
        if (connectStr == null || connectStr.trim().isEmpty()) {
            throw new IllegalStateException("Configuracao obrigatoria nao informada: AZURE_STORAGE_CONNECTION_STRING");
        }
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();


        /* Create a new container client */
        BlobContainerClient containerClient = null;
        try {
            containerClient = blobServiceClient.createBlobContainer("my-container-test");
        } catch (BlobStorageException ex) {
            // The container may already exist, so don't throw an error
            if (!ex.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                throw ex;
            }
        }

        /* Upload the file to the container */
        BlobClient blobClient = containerClient.getBlobClient("b928044379e5faba6ea7be731576aed0.pdf");
        blobClient.uploadFromFile("C:\\home\\assinanet\\assina.net\\hml\\arquivos\\5cfdafd1-0cb7-497d-ae54-3a9db6a43399\\364104b7-0d25-4253-9be0-5cb94a1c954f\\c5b0269f-2a2e-42cb-8a69-4f5909e1aba8\\b928044379e5faba6ea7be731576aed0.pdf");

    }

    @Bean
    CommandLineRunner init(
              UsuarioRepository userRepository
            , ContratoRepository contratoRepository
            , PessoaRepository pessoaRepository
            , UsuarioClienteRepository usuarioClienteRepository
            , ClienteRepository clienteRepository
            , PasswordEncoder passwordEncoder
            , SegmentoRepository segmentoRepository
            , PapelRepository papelRepository
            , TipoTelefoneRepository tipoTelefoneRepository
            , TipoEnderecoRepository tipoEnderecoRepository
            , PlanoRepository planoRepository
        ) {
        return args -> {

            Cliente clienteSistema = clienteService.retornaClienteSistema();
            Usuario usuarioAdmin = userRepository.findByLogin("ADMIN");

            /*
            if (clienteSistema == null) {
                final String cnpjCliente = "00000000000000";
                final String nomeRazaoSocialCliente = "SISTEMA";

                criaClienteInicial(
                          userRepository
                        , pessoaRepository
                        , usuarioClienteRepository
                        , clienteRepository
                        , cnpjCliente
                        , nomeRazaoSocialCliente
                        , clienteSistema
                );
                clienteSistema = clienteService.retornaClienteSistema();

                initPlano(planoRepository, clienteSistema);
                initUsers(userRepository, pessoaRepository, passwordEncoder, clienteSistema);
                initPessoa(userRepository, pessoaRepository, usuarioClienteRepository, clienteRepository, clienteSistema);

                Segmento segmentoSistema = initSegmento(segmentoRepository, papelRepository, clienteSistema, "SISTEMA", "Sistema");
                clienteSistema.setSegmento(segmentoSistema);
                clienteService.salva(clienteSistema);
                initSegmento(segmentoRepository, papelRepository, clienteSistema, "FACTORING", "Factoring");
                initSegmento(segmentoRepository, papelRepository, clienteSistema, "FIDC", "FIDC");
                initSegmento(segmentoRepository, papelRepository, clienteSistema, "CONTRATOSIMPLES", "Contrato Simples");
                initSegmento(segmentoRepository, papelRepository, clienteSistema, "CORRETORASEGUROS", "Corretora Seguros");
                initSegmento(segmentoRepository, papelRepository, clienteSistema, "CONTABILIDADE", "Contabilidade");

                //Tipo Endereço
                initTipoEndereco(tipoEnderecoRepository, clienteSistema, "COMERCIAL", "Comercial");
                initTipoEndereco(tipoEnderecoRepository, clienteSistema, "RESIDENCIAL", "Residencial");
                initTipoEndereco(tipoEnderecoRepository, clienteSistema, "CORRESPONDENCIA", "Correspondencia");

                //Tipo Telefone
                initTipoTelefone(tipoTelefoneRepository, clienteSistema, "CELULAR", "Comercial");
                initTipoTelefone(tipoTelefoneRepository, clienteSistema, "COMERCIAL", "Residencial");
                initTipoTelefone(tipoTelefoneRepository, clienteSistema, "RESIDENCIAL", "Correspondencia");

            }
            */

            if (clienteSistema == null || usuarioAdmin == null) {
                System.out.println("==============================================================");
                System.out.println("[BOOTSTRAP_INCOMPLETO] Dados iniciais nao encontrados.");
                if (clienteSistema == null) {
                    System.out.println("[BOOTSTRAP_INCOMPLETO] Cliente SISTEMA nao encontrado.");
                }
                if (usuarioAdmin == null) {
                    System.out.println("[BOOTSTRAP_INCOMPLETO] Usuario ADMIN nao encontrado.");
                }
                System.out.println("[BOOTSTRAP_INCOMPLETO] Execute o script:");
                System.out.println("[BOOTSTRAP_INCOMPLETO] scripts/BootstrapSistemaProducao.sql");
                System.out.println("[BOOTSTRAP_INCOMPLETO] Depois, inicie novamente a aplicacao.");
                System.out.println("==============================================================");
                throw new IllegalStateException(
                        "Bootstrap inicial pendente. Execute scripts/BootstrapSistemaProducao.sql."
                );
            }
        };
    }

    private Plano initPlano(PlanoRepository planoRepository, Cliente clienteSistema) {
        Plano planoIlimitado = planoRepository.findByPeriodo("ILIMITADO");
        if (planoIlimitado == null) {
            planoIlimitado = new Plano(null,"Plano Ilimitado",StatusEnum.ATIVO,null,"ILIMITADO");
            planoRepository.save(planoIlimitado);
            clienteSistema.setPlano(planoIlimitado);
        }
        Plano planoGratuito = null;
        Optional<Plano> planoOpt  = planoRepository.findById(CommonsUtil.UUIDValue("C526767A-8789-4C55-9E44-6C9B7FC83B52"));
        if (planoOpt.isPresent())
            planoGratuito = planoOpt.get();

        if (planoGratuito == null) {
            planoGratuito = new Plano(CommonsUtil.UUIDValue("C526767A-8789-4C55-9E44-6C9B7FC83B52"),"Plano Gratuito - 5 Assinaturas mensais",StatusEnum.ATIVO,5,"MES");
            planoRepository.save(planoGratuito);
            //planoGratuito.setId(CommonsUtil.UUIDValue("C526767A-8789-4C55-9E44-6C9B7FC83B52"));
            //planoRepository.save(planoGratuito);
        }
        return planoIlimitado;
    }
    private void initUsers(UsuarioRepository userRepository, PessoaRepository pessoaRepository,
                           PasswordEncoder passwordEncoder, Cliente clienteSistema) {
        Usuario find = userRepository.findByLogin("ADMIN");
        String cpfCnpj = "00000000000";
        if (find == null) {
            Pessoa pessoa = pessoaRepository.findByCpfCnpj(cpfCnpj);
            if (pessoa == null) {
                pessoa = new Pessoa();
                pessoa.setTipoPessoa(TipoPessoaEnum.FISICA);
                pessoa.setNomeRazaoSocial("Administrador do Sistema");
                pessoa.setCpfCnpj(cpfCnpj);
                pessoa.setEmail("sistema@assina.net");
                pessoa.setCliente(clienteSistema);
            }

            Usuario admin = new Usuario();
            admin.setPessoa(pessoa);

            admin.setLogin("ADMIN");
            admin.setSenha(passwordEncoder.encode("Ass1n@Net2021"));
            admin.setPerfil(PerfilEnum.ROLE_ADMIN);
            admin.setStatus(StatusEnum.ATIVO);
            userRepository.save(admin);

        }
    }


    private void initPessoa(UsuarioRepository userRepository, PessoaRepository pessoaRepository,
                            UsuarioClienteRepository usuarioClienteRepository, ClienteRepository clienteRepository,
                            Cliente clienteSistema) {

        //novamente somente para vincular o usuario
        criaClienteInicial(userRepository
                , pessoaRepository
                , usuarioClienteRepository
                , clienteRepository
                , clienteSistema.getPessoa().getCpfCnpj()
                , clienteSistema.getPessoa().getNomeRazaoSocial()
                , clienteSistema
        );
    }
/*
    private void CriaFilaEmail() {
        Contrato contrato = new Contrato();
        contrato.setId(CommonsUtil.UUIDValue("489965A1-5483-4FB1-A7EA-00A6BC7A8796"));
        contrato.setIdentificador("Teste falha");
        contrato.setAssunto("Teste falha");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setSubject("Teste envio falha");
        msg.setText("testando envio de falha");
        msg.setTo("amurta@gmail.com");

        ContratoLog contratoLogSucesso = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true);

        ContratoLog contratoLogErro = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: ",
                DataUtil.getCarimboTempo(), true);


        //Salva fila para mandar via jog
        EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJson(contratoLogSucesso), GsonUtil.toJson(contratoLogErro),
                false, "Teste envio erro falha", contratoLogSucesso.getDataLog());
        emailFilaService.save(emailFila);
    }
*/

    private void criaClienteInicial(
              UsuarioRepository userRepository
            , PessoaRepository pessoaRepository
            , UsuarioClienteRepository usuarioClienteRepository
            , ClienteRepository clienteRepository
            , String cnpj
            , String nomeRazaoSocial
            //, TipoClienteEnum tipoCliente
            , Cliente clienteSistema) {

        Pessoa pessoa = pessoaRepository.findByCpfCnpj(cnpj);
        Usuario usuario = userRepository.findByLogin("ADMIN");

        if (pessoa == null) {
            pessoa = new Pessoa();
            pessoa.setCpfCnpj(cnpj);
            pessoa.setEmail("sistema@assina.net");
            pessoa.setNomeRazaoSocial(nomeRazaoSocial);
            pessoa.setTipoPessoa(TipoPessoaEnum.JURIDICA);
            pessoa.setCliente(clienteSistema);
        }

        Cliente cliente = clienteRepository.findByCpfCnpj(cnpj);
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setPessoa(pessoa);
            cliente.setDataInicioContrato(new Date());
            cliente.setStatus(StatusEnum.ATIVO);
            clienteRepository.save(cliente);
        }

        if (usuario != null) {
            UsuarioCliente usuarioCliente = usuarioClienteRepository.findByUsuarioAndCliente(usuario, cliente);
            if (usuarioCliente == null) {
                usuarioCliente = new UsuarioCliente();
                usuarioCliente.setCliente(cliente);
                usuarioCliente.setUsuario(usuario);
                usuarioCliente.setStatus(StatusEnum.ATIVO);
                usuarioClienteRepository.save(usuarioCliente);

            }
        }
    }

/*
    private void initContratos(ClienteRepository clienteRepository, ContratoRepository contratoRepository) {
        //verifica se tem o contrato modelo cadastradro
//        o id do usuario admin tem que ser "3877ae22-0c97-4657-acce-0ac16f5fdb45"
        Cliente custodiante = clienteRepository.findByCpfCnpj("96685367000165");

        List<Contrato> contrato = contratoRepository.findByCustodianteAndIdentificador(custodiante, "72818");
        if (CommonsUtil.semValor(contrato)) {
            String resultado = contratoService.EnvioDocumentoXML(contratoService.CriaContatoModel());
            System.out.println(resultado);
        }
    }
*/

    private Segmento initSegmento(
              SegmentoRepository segmentoRepository
            , PapelRepository papelRepository
            , Cliente clienteSistema
            , String identifacacaoSegmento
            , String nomeSegmento) {
        Segmento segmento = segmentoRepository.findByIdentificacao(identifacacaoSegmento);
        if (segmento == null) {
            segmento = new Segmento();
            segmento.setStatus(StatusEnum.ATIVO);
            segmento.setIdentificacao(identifacacaoSegmento);
            segmento.setNome(nomeSegmento);
            segmento = segmentoRepository.save(segmento);
        }
        return segmento;

    }

    private TipoEndereco initTipoEndereco(
              TipoEnderecoRepository tipoEnderecoRepository
            , Cliente clienteSistema
            , String identifacacao
            , String nome) {
        TipoEndereco tipoEndereco = tipoEnderecoRepository.findByIdentificacao(identifacacao);
        if (tipoEndereco == null) {
            tipoEndereco = new TipoEndereco();
            tipoEndereco.setStatus(StatusEnum.ATIVO);
            tipoEndereco.setIdentificacao(identifacacao);
            tipoEndereco.setNome(nome);
            tipoEndereco.setCliente(clienteSistema);
            tipoEndereco = tipoEnderecoRepository.save(tipoEndereco);
        }
        return tipoEndereco;
    }

    private TipoTelefone initTipoTelefone(
            TipoTelefoneRepository tipoTelefoneRepository
            , Cliente clienteSistema
            , String identifacacao
            , String nome) {
        TipoTelefone tipoTelefone = tipoTelefoneRepository.findByIdentificacaoAndCliente(identifacacao, clienteSistema);
        if (tipoTelefone == null) {
            tipoTelefone = new TipoTelefone();
            tipoTelefone.setStatus(StatusEnum.ATIVO);
            tipoTelefone.setIdentificacao(identifacacao);
            tipoTelefone.setNome(nome);
            tipoTelefone.setCliente(clienteSistema);
            tipoTelefone = tipoTelefoneRepository.save(tipoTelefone);
        }
        return tipoTelefone;
    }




}


