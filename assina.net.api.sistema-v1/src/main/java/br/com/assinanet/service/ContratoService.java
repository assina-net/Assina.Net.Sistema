package br.com.assinanet.service;

import br.com.assinanet.Report.ReportDataSource;
import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.*;
import br.com.assinanet.entity.exception.NegocioException;
import br.com.assinanet.entity.projection.ContratoDocumentoProjection;
import br.com.assinanet.models.*;
import br.com.assinanet.repository.*;
import br.com.assinanet.request.*;
import br.com.assinanet.request.assinador.AssinadorAssinaturasLogsRequest;
import br.com.assinanet.request.assinador.AssinadorContratoParteDocumentoRequest;
import br.com.assinanet.response.*;
import br.com.assinanet.security.model.CurrentUser;
import br.com.assinanet.task.AssinaturaTask;
import br.com.assinanet.task.CelularTask;
import br.com.assinanet.task.EmailTask;
import br.com.assinanet.util.*;
import br.com.swconsultoria.certificado.Certificado;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;
import org.jfree.date.DateUtilities;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ContratoService {

    static final String TAG_NAO_EXISTENTE = "dfasdfmasdofsadfkmsdafeowi$%$#%!";

    private final ContratoRepository contratoRepository;
    private final ContratoParteRepository contratoParteRepository;
    private final ContratoDocumentoRepository contratoDocumentoRepository;
    private final ContratoParteDocumentoRepository contratoParteDocumentoRepository;
    private final ContratoPartePapelRepository contratoPartePapelRepository;
    private final PessoaRepository pessoaRepository;
    private final TipoTelefoneRepository tipoTelefoneRepository;

    private final UsuarioService usuarioService;
    private final UsuarioClienteService usuarioClienteService;
    private final ContratoParteService contratoParteService;

    private final PessoaService pessoaService;
    private final ClienteService clienteService;
    private final ContratoLogService contratoLogService;
    private final EmailService emailService;
    private final PapelService papelService;
    private final PlanoService planoService;

    private final ContratoDocumentoService contratoDocumentoService;

    private final TipoDocumentoService tipoDocumentoService;
    private final PasswordEncoder passwordEncoder;
    private final SistemaAtributoService sistemaAtributoService;
    private final MensagemService mensagemService;
    private final PlanoRepository planoRepository;

    @Autowired
    private Environment env;

    public ContratoService(ContratoRepository contratoRepository,
                           ContratoParteRepository contratoParteRepository, ContratoDocumentoRepository contratoDocumentoRepository,
                           ContratoParteDocumentoRepository contratoParteDocumentoRepository, ContratoPartePapelRepository contratoPartePapelRepository,
                           PessoaRepository pessoaRepository, TipoTelefoneRepository tipoTelefoneRepository, UsuarioService usuarioService,
                           UsuarioClienteService usuarioClienteService, ContratoParteService contratoParteService, PessoaService pessoaService, ClienteService clienteService,
                           ContratoLogService contratoLogService, EmailService emailService, PapelService papelService, PlanoService planoService, TipoDocumentoService tipoDocumentoService,
                           PasswordEncoder passwordEncoder,
                           SistemaAtributoService sistemaAtributoService, MensagemService mensagemService,
                           PlanoRepository planoRepository, ContratoDocumentoService contratoDocumentoService) {
        this.contratoRepository = contratoRepository;
        this.contratoParteRepository = contratoParteRepository;
        this.contratoDocumentoRepository = contratoDocumentoRepository;
        this.contratoParteDocumentoRepository = contratoParteDocumentoRepository;
        this.contratoPartePapelRepository = contratoPartePapelRepository;
        this.pessoaRepository = pessoaRepository;
        this.tipoTelefoneRepository = tipoTelefoneRepository;
        this.usuarioService = usuarioService;
        this.usuarioClienteService = usuarioClienteService;
        this.contratoParteService = contratoParteService;
        this.pessoaService = pessoaService;
        this.clienteService = clienteService;
        this.contratoLogService = contratoLogService;

        this.emailService = emailService;
        this.papelService = papelService;
        this.planoService = planoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.passwordEncoder = passwordEncoder;

        this.sistemaAtributoService = sistemaAtributoService;
        this.mensagemService = mensagemService;
        this.planoRepository = planoRepository;
        this.contratoDocumentoService = contratoDocumentoService;
    }


    public ContratoRequest Salva(ContratoRequest contratoRequest) throws IOException, NegocioException, NoSuchAlgorithmException {
        return Salva(contratoRequest, false);
    }

    public ContratoRequest Salva(ContratoRequest contratoRequest, boolean validarLiberacao) throws IOException, NegocioException, NoSuchAlgorithmException {

        if (!CommonsUtil.semValor(contratoRequest.getContrato().getCustodiante().getId()) &&
                (CommonsUtil.semValor(contratoRequest.getContrato().getCustodiante().getPessoa()) ||
                        CommonsUtil.semValor(contratoRequest.getContrato().getCustodiante().getPessoa().getNomeRazaoSocial()))) {
            contratoRequest.getContrato().setCustodiante(clienteService.findById(contratoRequest.getContrato().getCustodiante().getId()));

        }

        if (CommonsUtil.semValor(contratoRequest.getContrato().getDataCriacao())) {
            CarimboTempoModel carimbo = DataUtil.getCarimboTempo();
            contratoRequest.getContrato().setDataCriacao(carimbo.getDataCarimboTempo());

        }


        Contrato contrato = new Contrato(contratoRequest.getContrato());

        if (contrato.getIdDiretorio() == null) {
            contrato.setIdDiretorio(UUID.randomUUID());
        }

        //remove as partes inativas antes de salvar
        List<UUID> partesAtivas = new ArrayList<>(0);
        List<UUID> partesPapelAtivas = new ArrayList<>(0);

        //somente as partes ativas
        for (ContratoParte parte : contrato.getPartes()) {
            if (!CommonsUtil.semValor(parte.getId())) {
                partesAtivas.add(parte.getId());

                parte.getPapel().forEach(papel -> {
                    if (!CommonsUtil.semValor(papel.getId()))
                        partesPapelAtivas.add(papel.getId());

                });
            }
            if (!CommonsUtil.semValor(parte.getContatos())) {
                for (ContratoParte contato : parte.getContatos()) {
                    if (!CommonsUtil.semValor(contato.getId())) {
                        partesAtivas.add(contato.getId());
                        contato.getPapel().forEach(papel -> {
                            if (!CommonsUtil.semValor(papel.getId()))
                                partesPapelAtivas.add(papel.getId());
                        });
                    }
                }
            }
        }

        if (!CommonsUtil.semValor(partesAtivas)) {
            contratoParteRepository.desativaPartesExcluidas(contrato, partesAtivas);
        } else if (contrato.getId() != null) {
            contratoParteRepository.desativaPartesExcluidas(contrato);
        }

        if (!CommonsUtil.semValor(partesPapelAtivas)) {
            contratoParteRepository.excluiDocumentosPapelExcluidos(partesAtivas, partesPapelAtivas);
        } else if (!CommonsUtil.semValor(partesAtivas)) {
            contratoParteRepository.excluiDocumentosPapelExcluidos(partesAtivas);
        }

        contrato = Salva(contrato, validarLiberacao);


        //cria os contatos
        pessoaService.CriaPessoaPeloContratoParte(contrato.getPartes(), contrato.getCustodiante(), null, null);

        contratoRequest.setContrato(new ContratoResponse(contrato));

        return contratoRequest;
    }

    public ContratoDocumento atualizaDocumentoEmStorage(ContratoDocumento documento) {
        //persiste em arquivo o arquivo enviado
        if (!CommonsUtil.semValor(documento.getDocumentoOriginal())) { //&& !documento.getDocumentoOriginalSHA256().endsWith(".bin")) {
            try {
                contratoDocumentoService.persisteArquivoEmDiretorio(documento, TipoArquivoEnum.ORIGNAL);
            } catch (NegocioException | NoSuchAlgorithmException e) {
            }
        }
        if (!CommonsUtil.semValor(documento.getDocumentoAssinado())){ // && !documento.getDocumentoAssinadoSHA256().endsWith(".bin")) {
            try {
                contratoDocumentoService.persisteArquivoEmDiretorio(documento, TipoArquivoEnum.ASSINADO);
            } catch (NegocioException | NoSuchAlgorithmException e) {
            }
        }
        return documento;
    }

    public Contrato atualizaDocumentoEmStorage(Contrato contrato) {
        //remove os documentos inativas antes de salvar
        List<UUID> documentoAtivos = new ArrayList<>(0);
        List<UUID> documentoPapelAtivos = new ArrayList<>(0);
        for (ContratoDocumento documento : contrato.getDocumentos()) {
            if (documento.getIdDiretorio() == null)
                documento.setIdDiretorio(UUID.randomUUID());

            //persiste em arquivo o arquivo enviado
            if (!CommonsUtil.semValor(documento.getDocumentoOriginal())) { //&& !documento.getDocumentoOriginalSHA256().endsWith(".bin")) {
                try {
                    contratoDocumentoService.persisteArquivoEmDiretorio(documento, TipoArquivoEnum.ORIGNAL);
                } catch (NegocioException | NoSuchAlgorithmException e) {
                }
            }

            if (!CommonsUtil.semValor(documento.getDocumentoAssinado())){ // && !documento.getDocumentoAssinadoSHA256().endsWith(".bin")) {
                try {
                    contratoDocumentoService.persisteArquivoEmDiretorio(documento, TipoArquivoEnum.ASSINADO);
                } catch (NegocioException | NoSuchAlgorithmException e) {
                }
            }

            documento.setTipoDocumento(tipoDocumentoService.findById(documento.getTipoDocumento().getId()));
            documento.setDocumentoOriginal(null);
            if (!CommonsUtil.semValor(documento.getId())) {
                documentoAtivos.add(documento.getId());
                documento.getPapel().forEach(papel -> {
                    if (!CommonsUtil.semValor(papel.getId()))
                        documentoPapelAtivos.add(papel.getId());
                });
            }
        }

        if (!CommonsUtil.semValor(documentoAtivos)) {
            contratoDocumentoRepository.desativaDocumentosExcluidos(contrato, documentoAtivos);
            contratoDocumentoRepository.excluiDocumentosPapelExcluidos(documentoAtivos, documentoPapelAtivos);

        } else if (contrato.getId() != null) {
            contratoDocumentoRepository.desativaDocumentosExcluidos(contrato);
        }

        return contrato;
    }

    public Contrato Salva(Contrato contrato) {
        return Salva(contrato, false);
    }

    public Contrato Salva(Contrato contrato, boolean validarLiberacao) {
        if (contrato.getLiberadoAssinatura() == null) {
            contrato.setLiberadoAssinatura(false);
        }

        if (validarLiberacao) {
            ValidaLiberacaoAssinatura(contrato);
        } else {
            contrato.setValidado(false);
            contrato.setValidacaoMensagem(null);
        }

        contrato = atualizaDocumentoEmStorage(contrato);

        Contrato contratoSalvo = contratoRepository.save(contrato);
        return contratoSalvo;
    }

    public Contrato findById(UUID id) {

        Contrato contrato = contratoRepository.getOne(id);
        if (contrato != null)
            contrato = atualizaDocumentoEmStorage(contrato);

        return contrato;
    }

    public void delete(UUID id) {
        contratoRepository.deleteById(id);
    }


    public Page<Contrato> findAll(Contrato filtro, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        Example<Contrato> example = Example.of(filtro, matcher);
        return contratoRepository.findAll(example, pageable);
    }

    public Page<Contrato> findAllAssinar(Contrato filtro, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase()
                .withTransformer("status",
                        //can be replaced with lambda
                        new ExampleMatcher.PropertyValueTransformer() {
                            @Override
                            public Optional<Object> apply(Optional<Object> o) {
                                if (o.isPresent()) {
                                    return Optional.of(((String) o.get()).toUpperCase());
                                }
                                return o;
                            }
                        });
        Example<Contrato> example = Example.of(filtro, matcher);
        return contratoRepository.findAll(example, pageable);
    }

    public Page<ContratoListaCadastroModel> findAllCadastro(Contrato filtro, Pageable pageable) {

        Page<Contrato> contratos = findAll(filtro, pageable);

        List<ContratoListaCadastroModel> lstModel = new ArrayList<>(0);

        contratos.forEach(contrato -> {
            ContratoListaCadastroModel contratoListaAssinaModel = new ContratoListaCadastroModel();
            try {

                BeanUtils.copyProperties(contrato, contratoListaAssinaModel);

                List<ContratoParte> partes = getPartes(contrato);

                Map<Papel, List<ContratoParte>> mapPapel = new HashMap<>(0);

//                partes.forEach(contratoParte -> {
//                    if (!CommonsUtil.semValor(contratoParte.getContatos())) {
//
//                        contratoParte.getContatos().removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));
//
//                        contratoParte.getContatos().forEach(e -> criaMapPapel(mapPapel, e));
////
////                        for (int i = contratoParte.getContatos().size() - 1; i >= 0; i--) {
////                            if (StatusEnum.INATIVO.equals(contratoParte.getContatos().get(i).getStatus())) {
////                                contratoParte.getContatos().remove(i);
////                            } else {
////                                criaMapPapel(mapPapel, contratoParte.getContatos().get(i));
////
////                            }
////                        }
//                    } else {
//                        criaMapPapel(mapPapel, contratoParte);
//                    }
//
//                    Collections.sort(contratoParte.getContatos(), (o1, o2) -> {
//                        int compare = CommonsUtil.compare(o1.getNomeRazaoSocial(), o2.getNomeRazaoSocial());
//                        return compare;
//                    });
//
//                });


                contratoListaAssinaModel.setMapPapel(mapPapel);
                lstModel.add(contratoListaAssinaModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (lstModel != null) {
            Collections.sort(lstModel, new Comparator<ContratoListaCadastroModel>() {
                @Override
                public int compare(ContratoListaCadastroModel o1, ContratoListaCadastroModel o2) {
                    int compare = CommonsUtil.compare(o1.getDataCriacao(), o2.getDataCriacao()) * -1;
                    return compare;
                }
            });
        }

        return new PageImpl<>(
                lstModel,
                pageable,
                contratos.getTotalElements()
        );

    }

    private void criaMapPapel(Map<Papel, List<ContratoParte>> mapPapel, ContratoParte contratoParte) {

        final ContratoParte contratoParteAdicionar;
        if (CommonsUtil.semValor(contratoParte.getContratoPartePJ())) {
            contratoParteAdicionar = contratoParte;
        } else {
            contratoParteAdicionar = contratoParte.getContratoPartePJ();
        }

        contratoParte.getPapel().forEach(papel -> {
            List<ContratoParte> partes = mapPapel.get(papel.getPapel());
            if (CommonsUtil.semValor(partes)) {
                partes = new ArrayList<>(0);
            }
            if (!partes.contains(contratoParteAdicionar)) {
                partes.add(contratoParteAdicionar);
                mapPapel.put(papel.getPapel(), partes);
            }
        });
    }

    public Page<ContratoListaAssinaModel> findAllAssina(ContratoFiltroRequest filtro, Pageable pageable) {

        List<StatusContratoEnum> listStatusContrato = new ArrayList<>(Arrays.asList(StatusContratoEnum.LIBERADOASSINATURA, StatusContratoEnum.PARCIALMENTEASSINADO));
        Boolean observadorBloqueadocumentosPendentes = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.BLOQUEIA_OBSERVADOR_DOCUMENTOS_PENDENTES, filtro.getContrato().getCustodiante());
        Papel papelObesrvador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);

        Page<Contrato> data = contratoRepository.PesquisaContratoParaAssinatura(filtro.getUsuario(), filtro.getContrato().getCustodiante(), filtro.getContrato().getIdentificador(),
                filtro.getContrato().getAssunto(), filtro.getContrato().getStatusContrato(),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getNomeRazaoSocial()),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getCpfCnpj()),
                listStatusContrato, observadorBloqueadocumentosPendentes, papelObesrvador, pageable);

        return ProcessaListaContratoAssinaturas(pageable, data);

    }

    public Page<ContratoListaAssinaModel> findAllVigentes(ContratoFiltroRequest filtro, Pageable pageable) {

        List<StatusContratoEnum> listStatusContrato = new ArrayList<>(Arrays.asList(StatusContratoEnum.ASSINADO, StatusContratoEnum.GERANDOASSINATURAS));
        Boolean observadorBloqueadocumentosPendentes = false; //ja vigente

        Page<Contrato> data = contratoRepository.PesquisaContratoParaAssinatura(filtro.getUsuario(), filtro.getContrato().getCustodiante(), filtro.getContrato().getIdentificador(),
                filtro.getContrato().getAssunto(), filtro.getContrato().getStatusContrato(),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getNomeRazaoSocial()),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getCpfCnpj()),
                listStatusContrato, observadorBloqueadocumentosPendentes, null, pageable);

        return ProcessaListaContratoAssinaturas(pageable, data);

    }

    public Page<ContratoListaAssinaModel> findAllRecusados(ContratoFiltroRequest filtro, Pageable pageable) {

        List<StatusContratoEnum> listStatusContrato = new ArrayList<>(Arrays.asList(StatusContratoEnum.RECUSADO));
        Boolean observadorBloqueadocumentosPendentes = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.BLOQUEIA_OBSERVADOR_DOCUMENTOS_PENDENTES, filtro.getContrato().getCustodiante());
        Papel papelObesrvador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);


        Page<Contrato> data = contratoRepository.PesquisaContratoParaAssinatura(filtro.getUsuario(), filtro.getContrato().getCustodiante(), filtro.getContrato().getIdentificador(),
                filtro.getContrato().getAssunto(), filtro.getContrato().getStatusContrato(),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getNomeRazaoSocial()),
                (filtro.getContrato().getPartes() == null ? null : filtro.getContrato().getPartes().get(0).getCpfCnpj()),
                listStatusContrato, observadorBloqueadocumentosPendentes, papelObesrvador, pageable);

        return ProcessaListaContratoAssinaturas(pageable, data);

    }

    private Page<ContratoListaAssinaModel> ProcessaListaContratoAssinaturas(Pageable pageable, Page<Contrato> data) {
        List<ContratoListaAssinaModel> lstModel = new ArrayList<>(0);

        data.forEach(contrato -> {
            ContratoListaAssinaModel contratoListaAssinaModel = new ContratoListaAssinaModel();
            try {

                BeanUtils.copyProperties(contrato, contratoListaAssinaModel);

                List<ContratoParte> partes = getPartes(contrato);


                partes.forEach(contratoParte -> {
                    if (!CommonsUtil.semValor(contratoParte.getContatos())) {
                        contratoParte.getContatos().removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));
//                        for (int i = contratoParte.getContatos().size() - 1; i >= 0; i--) {
//                            if (StatusEnum.INATIVO.equals(contratoParte.getContatos().get(i).getStatus())) {
//                                contratoParte.getContatos().remove(i);
//                            }
//                        }
                    }


                    Collections.sort(contratoParte.getContatos(), (o1, o2) -> {
                        int compare = CommonsUtil.compare(o1.getNomeRazaoSocial(), o2.getNomeRazaoSocial());
                        return compare;
                    });

                });
                contratoListaAssinaModel.setPartes(partes);
                lstModel.add(contratoListaAssinaModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return new PageImpl<>(
                lstModel,
                pageable,
                data.getTotalElements()
        );
    }

    public List<ContratoParte> getPartes(Contrato contrato) {

        List<ContratoParte> contratoPartes = contratoParteRepository.findByContratoStatus(contrato, StatusEnum.ATIVO);
        //remove contatos inativos
//        for (ContratoParte contraparte : contratoPartes) {
//
//            for (int i = contraparte.getContatos().size() - 1; i >= 0; i--) {
//                if (StatusEnum.INATIVO.equals(contraparte.getContatos().get(i).getStatus())) {
//                    contraparte.getContatos().remove(i);
//                }
//            }
//        }
        return contratoPartes;
    }

    public List<ContratoParte> getPartes(ContratoParteAssinarRequest contratoParteAssinarRequest) {
        return contratoParteRepository.findByContratoUsuarioPessoaRequest(contratoParteAssinarRequest.getContrato(),
                contratoParteAssinarRequest.getUsuario().getPessoa().getCpfCnpj(), StatusEnum.ATIVO);
    }

    public List<ContratoDocumentoResponse> getDocumentosParaAssinar(ContratoDocumentoAssinaturaRequest contratoDocumentoAssinaturaRequest) {

        if (contratoDocumentoAssinaturaRequest.getContratoParte() == null) {
            return new ArrayList<>(0);
        }
        List<ContratoParte> lstContratoParte = BuscaPartesParaAssinatura(contratoDocumentoAssinaturaRequest.getContrato(),
                contratoDocumentoAssinaturaRequest.getContratoParte().getCpfCnpj());


        List<ContratoDocumento> documentos = contratoDocumentoRepository.BuscaDocumentosParaAssinarProjection(contratoDocumentoAssinaturaRequest.getContrato(),
                lstContratoParte, StatusEnum.ATIVO);


        if (CommonsUtil.semValor(documentos)) {
            return new ArrayList<>(0);
        }


        List<ContratoDocumentoResponse> result = new ArrayList<>(0);
        documentos.forEach(documento -> {
                    ContratoDocumentoResponse contratoDocumentoProjection = new ContratoDocumentoResponse();
                    BeanUtils.copyProperties(documento, contratoDocumentoProjection);
                    result.add(contratoDocumentoProjection);
                }
        );

        if (result.size() > 1) {
            Collections.sort(result, new Comparator<ContratoDocumentoResponse>() {
                @Override
                public int compare(ContratoDocumentoResponse o1, ContratoDocumentoResponse o2) {
                    int compare = Integer.compare(CommonsUtil.intValue(o1.getTipoDocumento().getOrdem()), CommonsUtil.intValue(o2.getTipoDocumento().getOrdem()));
                    if (compare != 0) {
                        return compare;
                    }

                    compare = CommonsUtil.compare(o1.getNomeDocumento(), o2.getNomeDocumento());
                    return compare;
                }
            });
        } else {
            try {
                ContratoDocumento contratoDocumento = documentos.get(0);
                ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(contratoDocumento), contratoDocumento.getDocumentoOriginalSHA256(), false, true);
                // String documentoBase64 = Base64.getEncoder().encodeToString(documentos.get(0).getDocumentoOriginal());
                result.get(0).setDocumento(contratoDocumentoVisualizaResponse.getDocumentoPDF());
            } catch (NoSuchAlgorithmException | NegocioException e) {
                result.get(0).setDocumento(null);
            }

        }

        return result;
    }

    public List<ContratoDocumentoProjection> getDocumentos(Contrato contrato) {
        List<ContratoDocumentoProjection> result = contratoDocumentoRepository.findByContratoStatus(contrato, StatusEnum.ATIVO);

        Collections.sort(result, new Comparator<ContratoDocumentoProjection>() {
            @Override
            public int compare(ContratoDocumentoProjection o1, ContratoDocumentoProjection o2) {
                int compare = Integer.compare(o1.getTipoDocumento().getOrdem(), o2.getTipoDocumento().getOrdem());
                if (compare != 0) {
                    return compare;
                }

                compare = CommonsUtil.compare(o1.getNomeDocumento(), o2.getNomeDocumento());
                return compare;
            }
        });

        return result;
    }

    public ContratoDocumentoVisualizaResponse getDocumentoPDF(ContratoDocumentoRequest documento) {

        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = null;
        Optional<ContratoDocumento> contratoDocumento = contratoDocumentoRepository.findById(documento.getId());
        try {
            contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(contratoDocumento, contratoDocumento.get().getDocumentoOriginalSHA256(), false, true);
        } catch (NoSuchAlgorithmException | NegocioException e) {

            contratoDocumentoVisualizaResponse = contratoDocumentoRepository.getDocumentoOriginal(documento.getId());
            throw new RuntimeException(e);
        }

        return contratoDocumentoVisualizaResponse;
    }

    public ContratoDocumentoVisualizaResponse getDocumentoAssiandoOnlyPDF(ContratoDocumentoRequest documento) {

        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = null;
        Optional<ContratoDocumento> contratoDocumento = contratoDocumentoRepository.findById(documento.getId());
        try {
            contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(contratoDocumento, contratoDocumento.get().getDocumentoAssinadoSHA256(), true, false);
        } catch (NoSuchAlgorithmException | NegocioException e) {
            contratoDocumentoVisualizaResponse = contratoDocumentoRepository.getDocumentoAssinadoOnly(documento.getId());
            throw new RuntimeException(e);
        }

        return contratoDocumentoVisualizaResponse;
    }

    public ContratoDocumentoVisualizaResponse getDocumentoOriginalOnlyPDF(ContratoDocumentoRequest documento) {

        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = null;
        Optional<ContratoDocumento> contratoDocumento = contratoDocumentoRepository.findById(documento.getId());
        try {
            contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(contratoDocumento, contratoDocumento.get().getDocumentoOriginalSHA256(), true, true);
        } catch (NoSuchAlgorithmException | NegocioException e) {
            contratoDocumentoVisualizaResponse = contratoDocumentoRepository.getDocumentoOriginal(documento.getId());
            throw new RuntimeException(e);
        }

        return contratoDocumentoVisualizaResponse;
    }


    public ContratoDocumentoVisualizaResponse getDocumentoAssiandoPDF(ContratoDocumentoRequest documento) {

        ContratoDocumentoVisualizaResponse response;

        Optional<ContratoDocumento> contratoDocumento = contratoDocumentoRepository.findById(documento.getId());

        try {
            response = contratoDocumentoService.getArquivoEmDiretorio(contratoDocumento, contratoDocumento.get().getDocumentoAssinadoSHA256(), false, false);
        } catch (NoSuchAlgorithmException | NegocioException e) {
            response = contratoDocumentoRepository.getDocumentoAssinadoOnly(contratoDocumento.get().getId());
            throw new RuntimeException(e);
        }

//        ContratoDocumentoVisualizaResponse response = contratoDocumentoRepository.getDocumentoAssinado(documento.getId());


        List<ContratoDocumentoVisualizaAssinaturasResponse> assinaturas = contratoDocumentoRepository.getAssinaturas(documento.getId());

        assinaturas.forEach(assinatura -> {
            try {

                CertificadoAdicionalService certificadoAdicionalService = new CertificadoAdicionalService();

                CMSSignedData sigData = new CMSSignedData(assinatura.getAssinaturaDigital());
                Store store = sigData.getCertificates();

                ArrayList<X509CertificateHolder> certificateHolders = (ArrayList<X509CertificateHolder>) store.getMatches(null);

                JcaX509CertificateConverter converter = new JcaX509CertificateConverter();

                //somente o primeiro
                X509CertificateHolder holder = certificateHolders.iterator().next();

                X509Certificate cert = converter.getCertificate(holder);
                CertificadoModel certificadoModel = new CertificadoModel();

                String dn = cert.getSubjectDN().getName();
                LdapName ldapDN = new LdapName(dn);
                for (Rdn rdn : ldapDN.getRdns()) {
                    if (CommonsUtil.mesmoValor("CN", rdn.getType())) {
                        certificadoModel.setNome(rdn.getValue().toString());
                        break;
                    }
                }

                dn = cert.getIssuerDN().getName();
                ldapDN = new LdapName(dn);
                for (Rdn rdn : ldapDN.getRdns()) {
                    if (CommonsUtil.mesmoValor("CN", rdn.getType())) {
                        certificadoModel.setEmissorCertificado(rdn.getValue().toString());
                        break;
                    }
                }


                CertificadoAdicionalService.PreecheDadosAdicionais(certificadoModel, cert);

                assinatura.setEmissorCertificado(certificadoModel.getEmissorCertificado());
                assinatura.setNomeCertificado(certificadoModel.getNome());
                assinatura.setCpfCnpj(certificadoModel.getCnpjCpf());
                assinatura.setResponsavel(certificadoModel.getResponsavelNome());
                assinatura.setResponsavelCPF(certificadoModel.getResponsavelCpf());


                assinatura.setAssinaturaDigital(null);
            } catch (CMSException | CertificateException | InvalidNameException e) {
                e.printStackTrace();
            }
        });
        if (response != null) {
            response.setAssinaturas(assinaturas);
        }

        return response;


    }

    public ArquivoDownloadModel getDownload(Contrato contrato) {
        Boolean enviaArquivosOriginais = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.NAO_ENVIAR_ARQUIVO_ORIGINAL_NO_DOWNLOAD_CONTRATO_ASSINADOS, contrato.getCustodiante());
        return getDownload(contrato, !enviaArquivosOriginais);
    }


    public ArquivoDownloadModel getDownload(Contrato contrato, boolean enviaOriginais) {
        ArquivoDownloadModel result = new ArquivoDownloadModel();
        try {

            Map<String, byte[]> arquivos = new HashMap<>(0);

            List<ContratoDocumento> documentos = contratoDocumentoRepository.documentosParaAssinar(contrato, StatusEnum.ATIVO);


            adicionaArquivoDownload(enviaOriginais, arquivos, documentos);

            documentos = contratoDocumentoRepository.documentosSemAssinatura(contrato, StatusEnum.ATIVO);
            adicionaArquivoDownload(enviaOriginais, arquivos, documentos);

            Map<UUID, List<ContratoParteDocumento>> mapDocumentoAssinaturas = new HashMap<>(0);

            List<ContratoParte> partes = getPartes(contrato);
            for (ContratoParte contratoParte : partes) {
                if (TipoPessoaEnum.FISICA.equals(contratoParte.getTipoPessoa()) &&
                        !PapelEnum.OBSERVADOR.getDescricao().equals(contratoParte.getPapel().get(0).getPapel().getIdentificacao())) {
                    MontaMapaAssinaturas(mapDocumentoAssinaturas, contratoParte);
                } else {
                    for (ContratoParte contatoParte : contratoParte.getContatos()) {
                        if (!PapelEnum.OBSERVADOR.getDescricao().equals(contatoParte.getPapel().get(0).getPapel().getIdentificacao()))
                            MontaMapaAssinaturas(mapDocumentoAssinaturas, contatoParte);
                    }
                }
            }


            mapDocumentoAssinaturas.values().forEach(assinaturas -> {
                GeraArquivoAssinatura(arquivos, assinaturas);
            });

            result.setArquivoByte(CompactadorUtil.compactarZipByte(arquivos));
            result.setArquivoNome(contrato.getAssunto() + ".zip");
            result.setMediaType(MediaType.MULTIPART_FORM_DATA);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void adicionaArquivoDownload(boolean enviaOriginais, Map<String, byte[]> arquivos, List<ContratoDocumento> documentos) {
        if (documentos == null)
            return;

        for (ContratoDocumento contratoDocumento : documentos) {

            String nomeArquivo = (contratoDocumento.getTipoDocumento().getNome().replace(' ', '_') + " " + contratoDocumento.getNomeDocumento().replace('?', '_').replace('/', '_').replace('\\', '_'));
            if (nomeArquivo.length() > 80)
                nomeArquivo = nomeArquivo.substring(1, 80);


            if (CommonsUtil.booleanValue(contratoDocumento.getTipoDocumento().getAssina())) {
                ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse;
                try {
                    contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(contratoDocumento), contratoDocumento.getDocumentoAssinadoSHA256(), true, false);
                } catch (NoSuchAlgorithmException | NegocioException e) {
                    contratoDocumentoVisualizaResponse = contratoDocumentoRepository.getDocumentoAssinadoOnly(contratoDocumento.getId());
                    throw new RuntimeException(e);
                }
                if (contratoDocumentoVisualizaResponse != null) {
                    arquivos.put(nomeArquivo
                            + "Assinado.pdf", contratoDocumentoVisualizaResponse.getDocumentoBytes());
                }

            }
            if ( enviaOriginais || !CommonsUtil.booleanValue(contratoDocumento.getTipoDocumento().getAssina())) {

                ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse;
                try {
                    contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(contratoDocumento), contratoDocumento.getDocumentoOriginalSHA256(), true, true);
                } catch (NoSuchAlgorithmException | NegocioException e) {
                    contratoDocumentoVisualizaResponse = contratoDocumentoRepository.getDocumentoAssinadoOnly(contratoDocumento.getId());
                    throw new RuntimeException(e);
                }

                arquivos.put(nomeArquivo, contratoDocumentoVisualizaResponse.getDocumentoBytes());
            }

//            if (contratoDocumento.getDocumentoAssinado() != null) {
//                arquivos.put(nomeArquivo
//                        + "Assinado.pdf", contratoDocumento.getDocumentoAssinado());
//            }
        }
    }

    private void MontaMapaAssinaturas(Map<UUID, List<ContratoParteDocumento>> mapDocumentoAssinaturas, ContratoParte contratoParte) {
        List<ContratoParteDocumento> parteDocumentos = contratoParteDocumentoRepository.findByContratoParte(contratoParte);
        for (ContratoParteDocumento parteDocumento : parteDocumentos) {
            List<ContratoParteDocumento> lstAssinaturas = mapDocumentoAssinaturas.get(parteDocumento.getDocumento().getId());
            if (CommonsUtil.semValor(lstAssinaturas)) {
                lstAssinaturas = new ArrayList<>(0);
            }
            lstAssinaturas.add(parteDocumento);
            mapDocumentoAssinaturas.put(parteDocumento.getDocumento().getId(), lstAssinaturas);
        }
    }

    private void GeraArquivoAssinatura(Map<String, byte[]> arquivos, List<ContratoParteDocumento> lstAssinaturas) {

        List<byte[]> assinaturas = new ArrayList<>(0);


        String nomeArquivoP7sMontado = Util.GerarNomeArquivoAssinaturaPCKS7(lstAssinaturas.get(0).getDocumento());
        byte[] documentoOriginal = lstAssinaturas.get(0).getDocumento().getDocumentoOriginal();
        try {
            ContratoDocumento documento = lstAssinaturas.get(0).getDocumento();
            ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(documento), documento.getDocumentoOriginalSHA256(), true, true);
            documentoOriginal = contratoDocumentoVisualizaResponse.getDocumentoBytes();
        } catch (NoSuchAlgorithmException | NegocioException e) {
            documentoOriginal = null;
        }
        byte[] arquivoP7sMontado = arquivos.get(nomeArquivoP7sMontado);

        //ordena por hora de assinatura
        Collections.sort(lstAssinaturas, new Comparator<ContratoParteDocumento>() {
            @Override
            public int compare(ContratoParteDocumento o1, ContratoParteDocumento o2) {
                return o1.getDataAssinatura().compareTo(o2.getDataAssinatura());
            }
        });


        for (ContratoParteDocumento parteDocumento : lstAssinaturas) {
            assinaturas.add(parteDocumento.getAssinaturaDigital());
        }

        try {
            byte[] arquivoP7sAgrupado = CertificadoUtil.RecriarP7S(documentoOriginal, arquivoP7sMontado, assinaturas);
            arquivos.put(nomeArquivoP7sMontado, arquivoP7sAgrupado);
        } catch (CMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

    }

    private String GerarNomeArquivoAssinaturaPDF(ContratoDocumento documento, ContratoParte contratoParte, ContratoPartePapel contratoPartePapel) {

        //parteDocumento.getDocumento().getNomeDocumento().replace('/', '_').replace('\\', '_') +                ".p7s"

        return documento.getNomeDocumento().replace('/', '_').replace('\\', '_') + "_" +
                contratoParte.getCpfCnpj() + "_" +
                contratoPartePapel.getPapel().getNome().replace(' ', '_') + ".pdf";
    }

    public UUID SaveContrato(ContratoModel contratoModel) throws NoSuchAlgorithmException, NegocioException {
        Contrato contrato = new Contrato();
        if (contrato.getIdDiretorio() == null) {
            contrato.setIdDiretorio(UUID.randomUUID());
        }
        if (contratoModel != null && !CommonsUtil.semValor(contratoModel.getCodigoCustodiante())) {

            contrato.setStatus(StatusEnum.ATIVO);
            contrato.setValorContratro(new BigDecimal(contratoModel.getValorContratro()));
            contrato.setAssunto(contratoModel.getAssunto());
            contrato.setIdentificador(contratoModel.getIdentificador());
            contrato.setMoeda(contratoModel.getMoeda());
            contrato.setRemetente(usuarioService.findByLogin(contratoModel.getRemetente()));
            contrato.setTipoContrato(contratoModel.getTipoContrato());
            contrato.setStatusContrato(StatusContratoEnum.NAOLIBERADOASSINTAURA);
            CarimboTempoModel carimbo = DataUtil.getCarimboTempo();
            contrato.setDataCriacao(carimbo.getDataCarimboTempo());
            Cliente custodiante = clienteService.findById(contratoModel.getCodigoCustodiante());
            contrato.setCustodiante(custodiante);

            contrato.setTipoIntegracao(contratoModel.getTipoIntegracao());


            //repository.save(contrato);


            List<ContratoParte> partes = new ArrayList<>(0);

            List<Papel> papelAditivo = new ArrayList<>(0);
            List<Papel> papelDuplicatas = new ArrayList<>(0);

            //List<ContratoPartePapel> partesPapel = new ArrayList<>(0);
            contrato.setPartes(new ArrayList<>(0));
            for (ContratoProcuradorModel empresaProcurador : contratoModel.getProcuradores()) {
                ContratoParte contratoParte = new ContratoParte();
                contratoParte.setStatus(StatusEnum.ATIVO);
                contratoParte.setContrato(contrato);
                contratoParte.setCpfCnpj(empresaProcurador.getCnpj());
                contratoParte.setTipoPessoa(TipoPessoaEnum.JURIDICA);
                contratoParte.setNomeRazaoSocial(empresaProcurador.getRazaoSocial());
                //  contratoParte.setParteContrato(ParteContratoEnum.Procurador);
                //contratoParte.setDuplicatas(false);
                contratoParte.setStatusAssinatura(StatusAssinaturaEnum.NAOLIBERADO);

                for (ContratoContatoProcuradorModel procurador : empresaProcurador.getContatoProcurador()) {

                    ContratoParte contratoContato = contratoParte.getContatos().stream().filter(x -> x.getCpfCnpj().equals(procurador.getCpf())).findFirst().orElse(null);
                    boolean achouContato = true;
                    if (contratoContato == null) {
                        achouContato = false;
                        contratoContato = new ContratoParte();
                        contratoContato.setContrato(contrato);
                        contratoContato.setStatus(StatusEnum.ATIVO);
                        contratoContato.setNomeRazaoSocial(procurador.getNome());
                        contratoContato.setCpfCnpj(procurador.getCpf());
                        contratoContato.setTipoPessoa(TipoPessoaEnum.FISICA);
                        contratoContato.setStatusAssinatura(StatusAssinaturaEnum.NAOLIBERADO);
                        //contratoContato.setDuplicatas(CommonsUtil.booleanValue(procurador.getDuplicatas()));
                        contratoContato.setEmail(procurador.getEmail());
                        contratoContato.setContratoPartePJ(contratoParte);

                        contratoContato.setPapel(new ArrayList<>(0));
                    }

                    for (String papelProcurador : procurador.getPapel()) {
                        ContratoPartePapel contratoPartePapel = new ContratoPartePapel();
                        contratoPartePapel.setContratoParte(contratoContato);

                        Papel papel = papelService.findByIdentificacaoAndCliente(papelProcurador.toUpperCase(), custodiante);
                        contratoPartePapel.setPapel(papel);

                        //contratoContato.getPapel().add(contratoPartePapel);
                        contratoContato.getPapel().add(contratoPartePapel);

                        if (!CommonsUtil.booleanValue(procurador.getDuplicatas())) {
                            if (!papelAditivo.contains(contratoPartePapel.getPapel())) {
                                papelAditivo.add(contratoPartePapel.getPapel());
                            }
                        } else {
                            if (!papelDuplicatas.contains(contratoPartePapel.getPapel())) {
                                papelDuplicatas.add(contratoPartePapel.getPapel());
                            }
                        }
                    }

                    //partes.add(contratoContato);
                    if (!achouContato) {
                        contratoParte.getContatos().add(contratoContato);
                    }
                }

                contrato.getPartes().add(contratoParte);

                partes.add(contratoParte);

            }

            for (ContratoValidadorModel empresaValidador : contratoModel.getValidadores()) {
                ContratoParte contratoParte = new ContratoParte();
                contratoParte.setStatus(StatusEnum.ATIVO);
                contratoParte.setContrato(contrato);
                contratoParte.setCpfCnpj(empresaValidador.getCnpj());
                contratoParte.setTipoPessoa(TipoPessoaEnum.JURIDICA);
                // contratoParte.setDuplicatas(false);
                contratoParte.setNomeRazaoSocial(empresaValidador.getRazaoSocial());
                // contratoParte.setParteContrato(ParteContratoEnum.Validador);
                contratoParte.setStatusAssinatura(StatusAssinaturaEnum.NAOLIBERADO);
                for (ContratoContatoModel validador : empresaValidador.getContatoValidador()) {
                    ContratoParte contratoContato = new ContratoParte();
                    contratoContato.setContrato(contrato);
                    contratoContato.setStatus(StatusEnum.ATIVO);
                    contratoContato.setStatusAssinatura(StatusAssinaturaEnum.NAOLIBERADO);
                    //contratoContato.setAssinou(false);
                    contratoContato.setNomeRazaoSocial(validador.getNome());
                    contratoContato.setCpfCnpj(validador.getCpf());
                    contratoContato.setTipoPessoa(TipoPessoaEnum.FISICA);
                    contratoContato.setEmail(validador.getEmail());
                    contratoContato.setContratoPartePJ(contratoParte);

                    //isso aqui pode dar errro
                    Papel papel = papelService.findByIdentificacaoAndCliente("VALIDADOR", custodiante);
                    if (!papelAditivo.contains(papel)) {
                        papelAditivo.add(papel);
                    }

                    //partes.add(contratoContato);
                    contratoParte.getContatos().add(contratoContato);
                }
                contrato.getPartes().add(contratoParte);
                partes.add(contratoParte);
            }

            contrato.setDocumentos(new ArrayList<>(0));
            if (contrato.getTipoIntegracao() == 1 || contrato.getTipoIntegracao() == 2) {
                // List<ContratoDocumento> documentos = new ArrayList<>(0);
                ContratoDocumento aditivo = new ContratoDocumento();
                aditivo.setContrato(contrato);
                aditivo.setDocumentoOriginal(contratoModel.getDocumento());
                aditivo.setStatus(StatusEnum.ATIVO);

                TipoDocumento tipoDocumentoAditivo = tipoDocumentoService.findByIdentificacaoAndCliente("ADITIVO", custodiante, null);

                aditivo.setTipoDocumento(tipoDocumentoAditivo);
                aditivo.setStatusDocumento(StatusDocumentoEnum.NAOASSINADO);
                aditivo.setNomeDocumento(contratoModel.getIdentificador() + " - ADITIVO");
                aditivo.setPapel(new ArrayList<>(0));


                papelAditivo.forEach(papel -> {
                    aditivo.getPapel().add(new ContratoDocumentoPapel(aditivo, papel));
                });

                if (aditivo.getIdDiretorio() == null)
                    aditivo.setIdDiretorio(UUID.randomUUID());

                //persiste em arquivo o arquivo enviado
                if (!CommonsUtil.semValor(aditivo.getDocumentoOriginal()))
                    contratoDocumentoService.persisteArquivoEmDiretorio(aditivo, TipoArquivoEnum.ORIGNAL);


                contrato.getDocumentos().add(aditivo);


                ContratoDocumento duplicatas = new ContratoDocumento();
                duplicatas.setContrato(contrato);
                duplicatas.setDocumentoOriginal(contratoModel.getDocumentoDuplicatas());
                duplicatas.setStatus(StatusEnum.ATIVO);

                TipoDocumento tipoDocumentoCsvDuplicatas = tipoDocumentoService.findByIdentificacaoAndCliente("CSVDUPLICATAS", custodiante, null);

                duplicatas.setTipoDocumento(tipoDocumentoCsvDuplicatas);
                duplicatas.setStatusDocumento(StatusDocumentoEnum.NAOASSINADO);
                duplicatas.setNomeDocumento(contratoModel.getIdentificador() + " - CSV DUPLICATAS");

                if (duplicatas.getIdDiretorio() == null)
                    duplicatas.setIdDiretorio(UUID.randomUUID());

                //persiste em arquivo o arquivo enviado
                //if (!CommonsUtil.semValor(duplicatas.getDocumentoOriginal()))
                    //contratoDocumentoService.persisteArquivoEmDiretorio(duplicatas, TipoArquivoEnum.ORIGNAL);


                contrato.getDocumentos().add(duplicatas);
                // documentos.add(duplicatas);

                TipoDocumento tipoDocumentoDuplicata = tipoDocumentoService.findByIdentificacaoAndCliente("DUPLICATA", custodiante, null);

                for (ContratoDuplicatasModel duplicata : contratoModel.getDuplicatas()) {
                    ContratoDocumento documento = new ContratoDocumento();
                    documento.setContrato(contrato);
                    documento.setDocumentoOriginal(duplicata.getDocumentoDuplicatas());
                    documento.setStatus(StatusEnum.ATIVO);
                    documento.setTipoDocumento(tipoDocumentoDuplicata);
                    documento.setStatusDocumento(StatusDocumentoEnum.NAOASSINADO);
                    documento.setNomeDocumento(duplicata.getFaturaNumero());

                    duplicata.setDocumentoDuplicatas(null);

                    //salva origem dos dados para gerar duplicata assinada depois
                    String dadosOrigem = GsonUtil.toJson(duplicata);
                    documento.setDadosOrigem(dadosOrigem);

                    documento.setPapel(new ArrayList<>(0));
                    papelDuplicatas.forEach(papel -> {
                        documento.getPapel().add(new ContratoDocumentoPapel(documento, papel));
                    });

                    if (documento.getIdDiretorio() == null)
                        documento.setIdDiretorio(UUID.randomUUID());

                    //persiste em arquivo o arquivo enviado
                    if (!CommonsUtil.semValor(documento.getDocumentoOriginal()))
                        contratoDocumentoService.persisteArquivoEmDiretorio(documento, TipoArquivoEnum.ORIGNAL);


                    contrato.getDocumentos().add(documento);
                    //  documentos.add(documento);


                }


                TipoDocumento tipoDocumentoCartaCessao = tipoDocumentoService.findByIdentificacaoAndCliente("CARTA_CESSAO", custodiante, null);


                for (ContratoCartaCessaoModel cartaCessao : contratoModel.getDocumentosCartasCessao()) {
                    ContratoDocumento documento = new ContratoDocumento();


                    documento.setContrato(contrato);
                    documento.setStatus(StatusEnum.ATIVO);
                    documento.setTipoDocumento(tipoDocumentoCartaCessao);
                    documento.setStatusDocumento(StatusDocumentoEnum.NAOASSINADO);

                    documento.setNomeDocumento(cartaCessao.getIdentificaoCartaCessao());
                    documento.setDocumentoOriginal(cartaCessao.getDocumentoCartasCessao());

                    documento.setPapel(new ArrayList<>(0));

                    cartaCessao.getPapel().forEach(sPapel -> {
                        Papel papel = papelService.findByIdentificacaoAndCliente(sPapel.toUpperCase(), custodiante);
                        documento.getPapel().add(new ContratoDocumentoPapel(documento, papel));
                    });
                    contrato.getDocumentos().add(documento);
                }

            }

            contrato = Salva(contrato);

            pessoaService.CriaPessoaPeloContratoParte(contrato.getPartes(), contrato.getCustodiante(), null, null);
            return contrato.getId();

        } else {
            return null;
        }
    }

    public void VerificaContratoAssinado(Contrato contrato, ContratoParte contratoParteAssinando, Boolean gerarArquivoAssinado) throws Exception {

        Papel papelObesrvador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);

        contratoLogService.salva(new ContratoLog(contrato, null, "Inicio da verificação do contrato assinado. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));


        contrato = findById(contrato.getId());


        List<ContratoParte> partes = contratoParteRepository.findByContratoUsuarioPessoaRequest(contrato,
                contratoParteAssinando.getCpfCnpj(), StatusEnum.ATIVO);

        for (ContratoParte parte : partes) {
            List<ContratoParteDocumentoPendente> listaPendentesParte = contratoParteDocumentoRepository.verificaAssinaturasPendentesContratoCpfCnpj(contrato, parte.getCpfCnpj());
            if (CommonsUtil.semValor(listaPendentesParte)) {
                parte.setStatusAssinatura(StatusAssinaturaEnum.ASSINADO);
            } else {
                parte.setStatusAssinatura(StatusAssinaturaEnum.ASSINADOPARCIAL);
            }
            contratoParteService.Save(parte);
        }


        List<ContratoParteDocumentoPendente> listaPendentes = contratoParteDocumentoRepository.verificaAssinaturasPendentes(contrato);

        List<UUID> listPartesPendentes = new ArrayList<>(0);
        List<UUID> listDocumentoPendentes = new ArrayList<>(0);

        for (ContratoParteDocumentoPendente pendente : listaPendentes) {
            listPartesPendentes.add(pendente.getContratoParte().getId());
            listDocumentoPendentes.add(pendente.getContratoDocumento().getId());
        }

        if (CommonsUtil.semValor(listaPendentes)) {

            contrato.setStatusContrato(StatusContratoEnum.GERANDOASSINATURAS);
            Salva(contrato);

            contratoParteDocumentoRepository.concluiContratoParteParciais(contrato, null, 0);
            contratoParteDocumentoRepository.concluiContratoDocumentoParciais(contrato, null, 0);

            //finaliza as assinaturas nos documentos
            if (gerarArquivoAssinado) {
                Runnable runnable = new AssinaturaTask.GeraAssinaturaDocumentos(contrato);
                Thread thread = new Thread(runnable);
                thread.start();
            }

        } else {

            contratoParteDocumentoRepository.concluiContratoParteParciais(contrato, listPartesPendentes, listPartesPendentes.size());
            contratoParteDocumentoRepository.concluiContratoDocumentoParciais(contrato, listDocumentoPendentes, listDocumentoPendentes.size());

            //valida se a parte que assintou ainda tem alguma pendencia, se nao verifica se é para enviar para alguma parte que tem pre-requisito
            AtomicBoolean temPendencias = new AtomicBoolean(false);
            List<ContratoParte> lstContratoPartesFisicas = new ArrayList<>(0);


            List<ContratoParte> lstPartesAssinatura = new ArrayList<>(0);
            List<ContratoParte> lstPartesObservador = new ArrayList<>(0);
            listaPendentes.forEach(pendente -> {
                if (TipoPessoaEnum.FISICA.equals(pendente.getContratoParte().getTipoPessoa())) {
                    if (ehObseravdor(pendente.getContratoParte(), papelObesrvador)) {
                        lstPartesObservador.add(pendente.getContratoParte());
                    } else {
                        lstPartesAssinatura.add(pendente.getContratoParte());
                    }
                } else {
                    pendente.getContratoParte().getContatos().forEach(contato -> {
                        if (ehObseravdor(contato, papelObesrvador)) {
                            lstPartesObservador.add(contato);
                        } else {
                            lstPartesAssinatura.add(contato);
                        }
                    });
                }
            });


            try {
                //monta lista de pessoas fisicas que tem requisitos para quem assinou , se ele nao tive mais pedencia
                for (ContratoParteDocumentoPendente pendente : listaPendentes) {
                    //verfica se a pessoa que assinou ainda tem pendencia, se tiver nao libera para os outros
                    if (CommonsUtil.mesmoValor(pendente.getContratoParte().getCpfCnpj(), contratoParteAssinando.getCpfCnpj())) {
                        lstContratoPartesFisicas = new ArrayList<>(0);
                        break;
                    }

                    if (TipoPessoaEnum.FISICA.equals(pendente.getContratoParte().getTipoPessoa())) {
                        if (CommonsUtil.mesmoValor(pendente.getContratoParte().getRequisitoAssinatura(), contratoParteAssinando.getCpfCnpj())) {
                            lstContratoPartesFisicas.add(pendente.getContratoParte());
                        }
                    } else {
                        for (ContratoParte contato : pendente.getContratoParte().getContatos()) {
                            if (CommonsUtil.mesmoValor(contato.getRequisitoAssinatura(), contratoParteAssinando.getCpfCnpj())) {
                                lstContratoPartesFisicas.add(contato);
                            }
                        }
                    }
                }

                if (!CommonsUtil.semValor(lstContratoPartesFisicas)) {


                    Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_LIBERADO_WHATSAPP,
                            contrato, lstContratoPartesFisicas);

                    if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                        Runnable runnable = new EmailTask.EnviaLiberacaoContratoAssinatura(contrato, mapEnvio.get(TipoEnvioMsgEnum.EMAIL), false);
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                    if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                        Runnable runnable = new CelularTask.EnviaLiberacaoContratoAssinaturaWhatsApp(contrato, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP), false);
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }

            } catch (Exception e) {
            }


        }

        contratoLogService.salva(new ContratoLog(contrato, null, "Fim da verificação do contrato assinado. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));


    }


    public List<ContratoParte> BuscaPartesParaAssinatura(Contrato contrato, String cpfCnpj) {
        return contratoParteRepository.findByContratoAndCpfCnpjAndStatus(contrato, cpfCnpj, StatusEnum.ATIVO);
    }

    public String AssinarViaCodigoLote(ContratoParteAssinaturaRequest contratoParteAssinatura) throws Exception {


        validaPin(contratoParteAssinatura, contratoParteAssinatura.getUsuario());

        Boolean assinaEmLote = true;

        for (UUID idContrato : contratoParteAssinatura.getContratos()) {
            //monta contrato para assinatura
            Contrato contrato = new Contrato();
            contrato.setId(idContrato);

            ContratoParteAssinaturaRequest contratoParteAssinaturaAssina = new ContratoParteAssinaturaRequest();
            BeanUtils.copyProperties(contratoParteAssinatura, contratoParteAssinaturaAssina, "contratos");
            contratoParteAssinaturaAssina.getContratoParte().setContrato(contrato);
            AssinarViaCodigo(contratoParteAssinaturaAssina, assinaEmLote);
        }

        return "Documento(s) assinados.";
    }

    private void validaPin(ContratoParteAssinaturaRequest contratoParteAssinatura, Usuario usuario) throws Exception {
        Usuario usuarioValida = usuarioService.findById(contratoParteAssinatura.getUsuario().getId());

        if (DateUtil2.isAfterDateMinutes(usuarioValida.getValidadeTokenAssinatura(), DataUtil.getCarimboTempo().getDataCarimboTempo())) {
            contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null,
                    "Pin expirado: usuario: " +
                            usuario.getLogin() + " e-mail: " + usuario.getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

            throw new Exception("Pin expirado, favor pedir reenvio");
        }
    }

    public String gerarAssinaturaNovamente(Contrato contrato) throws Exception {

        Optional<Contrato> contratodb = contratoRepository.findById(contrato.getId());

        for (ContratoDocumento contratoDocumento : contratodb.get().getDocumentos()) {
            contratoDocumento.setDocumentoAssinado(null);
            contratoDocumentoRepository.save(contratoDocumento);
        }

        contratodb.get().setStatusContrato(StatusContratoEnum.GERANDOASSINATURAS);
        contratoRepository.save(contratodb.get());
        return "Gerando assinatura(s) novamente !";
    }

    public String AssinarViaCodigo(ContratoParteAssinaturaRequest contratoParteAssinatura, boolean assinaEmLote) throws Exception {

        ContratoParte contratoParteRequest = contratoParteAssinatura.getContratoParte();
        Usuario usuario = contratoParteAssinatura.getUsuario();
        boolean gerarArquivoAssinado = false;

        if (!assinaEmLote) {
            validaPin(contratoParteAssinatura, usuario);
            //se é individual ja gera o contrato assinado
            gerarArquivoAssinado = true;
        }

        Contrato contrato = findById(contratoParteRequest.getContrato().getId());

        contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null, "Inicio da assinatura via código " +
                contratoParteAssinatura.getContratoParte().getNomeRazaoSocial() + " e-mail: " + contratoParteAssinatura.getContratoParte().getEmail(),
                DataUtil.getCarimboTempo(), true));

        if (StatusContratoEnum.LIBERADOASSINATURA.equals(contrato.getStatusContrato())) {
            contrato.setStatusContrato(StatusContratoEnum.PARCIALMENTEASSINADO);
            contratoRepository.save(contrato);
        }


        List<ContratoParte> contratoPartes = BuscaPartesParaAssinatura(contratoParteRequest.getContrato(), contratoParteRequest.getCpfCnpj());

        List<ContratoParteDocumento> contratoParteDocumentos = new ArrayList<>(0);

        //Busca o usuário com as chaves, o do login não tem
        // usuario = usuarioService.findById(usuario.getId());
        if (CommonsUtil.semValor(usuario.getChavePrivada()) ||
                CommonsUtil.semValor(usuario.getChavePublica())) {

            contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null,
                    "Gerando chaves privada e publica. Usuário: " +
                            usuario.getLogin() + " e-mail: " + usuario.getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

            KeyPair kp = null;
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                kp = kpg.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            usuario.setChavePrivada(kp.getPrivate().getEncoded());
            usuario.setChavePublica(kp.getPublic().getEncoded());

            usuarioService.save(usuario);
        }

        /* Generate public key. */
        PKCS8EncodedKeySpec ksPKCS8 = new PKCS8EncodedKeySpec(usuario.getChavePrivada());
        KeyFactory kfPvt = KeyFactory.getInstance("RSA");
        PrivateKey pvt = kfPvt.generatePrivate(ksPKCS8);


        /* Generate public key. */
        X509EncodedKeySpec ksX509 = new X509EncodedKeySpec(usuario.getChavePublica());
        KeyFactory kfPub = KeyFactory.getInstance("RSA");
        PublicKey pub = kfPub.generatePublic(ksX509);


        contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null,
                "Gerando certificado. Usuário: " +
                        usuario.getLogin() + " e-mail: " + usuario.getPessoa().getEmail(),
                DataUtil.getCarimboTempo(), true));


        PessoaEndereco pessoaEndereco;
        if (!CommonsUtil.semValor(usuario.getPessoa().getPessoaEndereco())) {
            pessoaEndereco = usuario.getPessoa().getPessoaEndereco().get(0);
        } else {
            pessoaEndereco = pessoaService.PreencheEnderecoPessoa(contratoParteAssinatura, usuario);
            usuario.getPessoa().setPessoaEndereco(new ArrayList<>(0));
            usuario.getPessoa().getPessoaEndereco().add(pessoaEndereco);
        }

        CertificadoUtil certificadoUtil = new CertificadoUtil(pessoaEndereco.getPais().getNome(), "", pessoaEndereco.getMunicipio(),
                pessoaEndereco.getEstado(), "Assina.net");

        X509Certificate cert509 = certificadoUtil.generate(pvt, pub, 360, "SHA256WithRSAEncryption", true, usuario);

        contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null,
                "Certificado gerado. Usuário: " +
                        usuario.getLogin() + " e-mail: " + usuario.getPessoa().getEmail(),
                DataUtil.getCarimboTempo(), true));

        for (ContratoParte contratoParte : contratoPartes) {

            List<ContratoDocumento> documentos = BuscaDocumentosParaAssinar(contrato, contratoParte);

            for (ContratoDocumento contratoDocumento : documentos) {

                try {

                    for (ContratoPartePapel papel : contratoParte.getPapel()) {


                        //verificando primeiro se o papel é para este documento
                        if (contratoDocumento.getPapel().stream().filter(p -> p.getPapel() == papel.getPapel()).findFirst().orElse(null) == null) {
                            continue;
                        }

                        //Agora valida se permite a assinatura por token
                        TipoDocumentoPapel tipoDocumentoPapel = contratoDocumento.getTipoDocumento().getPapeis().stream().filter(t ->
                                CommonsUtil.booleanValue(t.getToken()) && t.getPapel().equals(papel.getPapel())).findFirst().orElse(null);

                        if (tipoDocumentoPapel == null) {
                            continue;
                        }

                        ContratoParteDocumento contratoParteDocumento = new ContratoParteDocumento();
                        // contratoParteDocumento.setContratoParte(contratoParte);
                        contratoParteDocumento.setUsuario(usuario);
                        contratoParteDocumento.setContratoPartePapel(papel);

                        CarimboTempoModel carimbo = DataUtil.getCarimboTempo();
                        if (!CommonsUtil.semValor(carimbo)) {
                            contratoParteDocumento.setDataAssinatura(carimbo.getDataCarimboTempo());
                            contratoParteDocumento.setGmtAssinatura(carimbo.getGmtCarimboTempo());
                            contratoParteDocumento.setCarimboTempoAssinatura(carimbo.getCarimboTempo());
                        }
                        contratoParteDocumento.setIpAssinatura(contratoParteAssinatura.getIpCliente());
                        contratoParteDocumento.setTokenAssinatura(contratoParte.getTokenAssinatura());


                        contratoParteDocumento.setDocumento(contratoDocumento);
                        contratoParteDocumento.setTipoAssinatura(TipoAssinaturaEnum.CHAVEPRIVADA);


                        contratoParteDocumento.setAssinaturaDigital(AssinaDocumentoChavePrivada(cert509, pvt, contratoDocumento, usuario));

                        String sLog = usuario.getPessoa().getNomeRazaoSocial() +
                                //"\n" +
                                //  (CommonsUtil.semValor(cpfCnpj) ? "" : cpfCnpj +
                                //           "\ne-mail: " + contratoParte.getEmail() +
                                //           "\nip: " + contratoParteAssinatura.getIpCliente()) +
                                "\nAssinou como " + papel.getPapel().getNome() +
                                ((contratoParte.getContratoPartePJ() != null) ? " representando a empresa " +
                                        contratoParte.getContratoPartePJ().getNomeRazaoSocial() +
                                        ("") : "") +
                                ".";
                        ContratoLog log = new ContratoLog(contrato, contratoDocumento, sLog, carimbo, false);
                        contratoLogService.salva(log);

                        if (ValidaDocumentoAssinado(contratoParteDocumento.getDocumento())) {
                            contratoDocumento.setDocumentoAssinado();
                        } else {
                            contratoDocumento.setStatusDocumento(StatusDocumentoEnum.PARCIALMENTEASSINADO);
                        }


                        contratoParteDocumentos.add(contratoParteDocumento);

                    }


                } catch (Exception e) {
                    contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null,
                            "Erro assinadndo documento: " + contratoDocumento.getNomeDocumento() + " usuario: " +
                                    usuario.getLogin() + " e-mail: " + usuario.getPessoa().getEmail()
                                    + " erro: " + e.getMessage(),
                            DataUtil.getCarimboTempo(), true));
                    throw new Exception(e.getMessage());
                }

                //salva por documento
                if (!CommonsUtil.semValor(contratoParteDocumentos)) {
                    contratoParteDocumentoRepository.saveAll(contratoParteDocumentos);
                }
                contratoParteDocumentos = new ArrayList<>(0);

            }


            contratoParte.setStatusAssinatura(StatusAssinaturaEnum.ASSINADO);
            contratoParteRepository.save(contratoParte);

        }

        contratoLogService.salva(new ContratoLog(contratoParteAssinatura.getContratoParte().getContrato(), null, "Fim da assinatura via código " +
                contratoParteAssinatura.getContratoParte().getNomeRazaoSocial() + " e-mail: " + contratoParteAssinatura.getContratoParte().getEmail(),
                DataUtil.getCarimboTempo(), true));


        VerificaContratoAssinado(contrato, contratoParteRequest, gerarArquivoAssinado);


        return "Documento(s) assinados.";

    }


    private byte[] ValidarChaveAssinatura()
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, IOException {

        /* Read all the public key bytes */
        Path path = Paths.get("D:\\Trabalho\\Assina.Net\\assinatura.sign");
        byte[] signFile = Files.readAllBytes(path);
        path = Paths.get("D:\\Trabalho\\Assina.Net\\chaveValidadora.pub");
        byte[] chavePublica = Files.readAllBytes(path);
        path = Paths.get("D:\\Trabalho\\Assina.Net\\documentoOriginal.pdf");
        byte[] documento = Files.readAllBytes(path);

        /* Generate public key. */
        X509EncodedKeySpec ksX509 = new X509EncodedKeySpec(chavePublica);
        KeyFactory kfPub = KeyFactory.getInstance("RSA");
        PublicKey pub = kfPub.generatePublic(ksX509);


        Signature signV = Signature.getInstance("SHA256withRSA");
        signV.initVerify(pub);

        InputStream inV = null;
        try {
            inV = new ByteArrayInputStream(documento);
            byte[] buf = new byte[2048];
            int len;
            while ((len = inV.read(buf)) != -1) {
                signV.update(buf, 0, len);
            }
        } finally {
            if (inV != null) inV.close();
        }

        /* Read the signature bytes from file */


        if (signV.verify(signFile)) {
            return signFile;
        } else {
            return null;
        }

    }

    private byte[] AssinaDocumentoChavePrivada(X509Certificate cert509, PrivateKey pvt, ContratoDocumento documento, Usuario usuario)
            throws Exception {


        SolicitacaoAssinatura solicitacaoAssinatura = new SolicitacaoAssinatura();
        Certificado cert = new Certificado();
        cert.setNome(usuario.getPessoa().getNomeRazaoSocial());
        cert.setArquivoBytes(cert509.getEncoded());
        solicitacaoAssinatura.setCertificado(cert);
        solicitacaoAssinatura.setLocalAssinatura("SAO PAULO");
        solicitacaoAssinatura.setMotivoAssinatura("TESTE");
        solicitacaoAssinatura.setNomeAssinatura("ASSINATURA TESTE");

        //Monta Objeto de assinatura
        AssinaturaExtendsModel assinaturaModel = new AssinaturaExtendsModel();
        assinaturaModel.setNomeAssinatura(solicitacaoAssinatura.getNomeAssinatura());
        assinaturaModel.setLocalAssinatura(solicitacaoAssinatura.getLocalAssinatura());
        assinaturaModel.setMotivoAssinatura(solicitacaoAssinatura.getMotivoAssinatura());
        assinaturaModel.setCertificadoX509(cert509);
        assinaturaModel.setPrivateKey(pvt);


        AssinaArquivoSimples assinaArquivoSimples = new AssinaArquivoSimples(assinaturaModel, documento, contratoDocumentoService);
        return assinaArquivoSimples.assina();

    }


    public String ReenviaSolicitacaoAssintura(ContratoParteResponse contratoParteResponse) {

        Papel papelObesrvador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);
        List<ContratoParte> partes = new ArrayList<ContratoParte>(List.of(new ContratoParte(contratoParteResponse)));
        Contrato contrato = contratoParteResponse.getContrato();

        if (!StatusContratoEnum.ASSINADO.equals(contrato.getStatusContrato())) {
            if (ehObseravdor(new ContratoParte(contratoParteResponse), papelObesrvador)) {
                Runnable runnable = new EmailTask.EnviaLiberacaoContratoObservador(contrato, partes);
                Thread thread = new Thread(runnable);
                thread.start();
            } else {

                Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_LIBERADO_WHATSAPP,
                        contrato, partes);

                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                    Runnable runnable = new EmailTask.EnviaLiberacaoContratoAssinatura(contrato, mapEnvio.get(TipoEnvioMsgEnum.EMAIL), false);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                    Runnable runnable = new CelularTask.EnviaLiberacaoContratoAssinaturaWhatsApp(contrato, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP), false);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            }
        } else {
            //Contrato assinado
            Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_ASSINADO_WHATSAPP,
                    contrato, partes);

            if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                Runnable runnable = new EmailTask.EnviaContratoAssinado(contrato, mapEnvio.get(TipoEnvioMsgEnum.EMAIL));
                Thread thread = new Thread(runnable);
                thread.start();
            }

            if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                Runnable runnable = new CelularTask.EnviaContratoAssinadoWhatsApp(contrato, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP));
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
        return null;

    }


    public String LiberarAssinaturaLote(ContratoLiberacaoLoteRequest contratoLiberacaoLoteRequest) {

        contratoLiberacaoLoteRequest.getContratos().forEach(id -> {
            Contrato contrato = findById(id);
            contrato.setUsuarioSolicitacaoAssinatura(contratoLiberacaoLoteRequest.getUsuario());
            ExecutaLiberacaoAssinatura(contrato);
        });
        return null;

    }

    public ContratoRequest LiberarAssinatura(ContratoRequest contratoRequest) throws Exception {

        //Salva sempres para acertar base de dados
        try {
            contratoRequest = Salva(contratoRequest, true);
        } catch (RuntimeException e) {
            if (isErroConfiguracaoStorage(e)) {
                contratoRequest.getContrato().setValidado(false);
                contratoRequest.getContrato().setValidacaoMensagem(montaMensagemConfiguracaoStorage(e));
                return contratoRequest;
            }
            throw e;
        }

        if (!contratoRequest.getContrato().getValidado()) {
            return contratoRequest;
        }

        Contrato contrato = findById(contratoRequest.getContrato().getId());

        try {
            ExecutaLiberacaoAssinatura(contrato);
        } catch (RuntimeException e) {
            if (isErroConfiguracaoStorage(e)) {
                contratoRequest.getContrato().setValidado(false);
                contratoRequest.getContrato().setValidacaoMensagem(montaMensagemConfiguracaoStorage(e));
                return contratoRequest;
            }
            throw e;
        }

        return contratoRequest;

    }

    private boolean isErroConfiguracaoStorage(Throwable throwable) {
        Throwable atual = throwable;
        while (atual != null) {
            String mensagem = atual.getMessage();
            if (mensagem != null) {
                String mensagemUpper = mensagem.toUpperCase();
                if (mensagemUpper.contains("INVALID CONNECTION STRING")
                        || mensagemUpper.contains("AZURE_STORAGE_CONNECTION_STRING")
                        || mensagemUpper.contains("CONFIGURACAO OBRIGATORIA NAO INFORMADA")
                        || mensagemUpper.contains("CONFIGURAÇÃO OBRIGATÓRIA NÃO INFORMADA")) {
                    return true;
                }
            }
            atual = atual.getCause();
        }
        return false;
    }

    private String montaMensagemConfiguracaoStorage(Throwable throwable) {
        return "Não foi possível liberar para assinatura porque o armazenamento dos arquivos não está configurado corretamente. "
                + "Verifique a configuração AZURE_STORAGE_CONNECTION_STRING ou configure o armazenamento local para este ambiente.";
    }

    private void ExecutaLiberacaoAssinatura(Contrato contrato) {


        contratoLogService.salva(new ContratoLog(contrato, null, "Validando a liberação da assinatura. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));

        Papel papelObservador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);

        contrato.setStatusContrato(StatusContratoEnum.LIBERANDOASSINATURA);

        contratoLogService.salva(new ContratoLog(contrato, null, "Inicio da liberação da assinatura. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));

        //ZonedDateTime carimbo = DataUtil.getCarimboTempo();
        CarimboTempoModel carimbo = DataUtil.getCarimboTempo();
        if (!CommonsUtil.semValor(carimbo)) {
            contrato.setDataSolicitacaoAssinatura(carimbo.getDataCarimboTempo());
            contrato.setGmtSolicitacaoAssinatura(carimbo.getGmtCarimboTempo());
            contrato.setCarimboTempoSolicitacaoAssinatura(carimbo.getCarimboTempo());
        }

        String sLog = contrato.getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() +
                // (CommonsUtil.semValor(cpfCnpj) ? "" : " (" + cpfCnpj + ")") +
                " solicitou as assinaturas.";
        ContratoLog log = new ContratoLog(contrato, null, sLog, carimbo, false);
        contratoLogService.salva(log);

        //salva dados enviados
        contrato = Salva(contrato);

        List<ContratoParte> lstContratoPartesFisicasSemRequisistos = new ArrayList<>(0);

        List<ContratoParte> lstPartesAssinatura = new ArrayList<>(0);
        List<ContratoParte> lstPartesObservador = new ArrayList<>(0);
        contrato.getPartes().forEach(parte -> {
            if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                if (ehObseravdor(parte, papelObservador)) {
                    lstPartesObservador.add(parte);
                } else {
                    lstPartesAssinatura.add(parte);
                }
            } else {
                parte.getContatos().forEach(contato -> {
                    if (ehObseravdor(contato, papelObservador)) {
                        lstPartesObservador.add(contato);
                    } else {
                        lstPartesAssinatura.add(contato);
                    }
                });
            }
        });

        lstPartesAssinatura.forEach(parte -> {
            if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa()) && CommonsUtil.semValor(parte.getRequisitoAssinatura())) {
                if (!lstContratoPartesFisicasSemRequisistos.contains(parte))
                    lstContratoPartesFisicasSemRequisistos.add(parte);
            } else {
                parte.getContatos().forEach(contato -> {
                    if (CommonsUtil.semValor(contato.getRequisitoAssinatura())) {
                        if (!lstContratoPartesFisicasSemRequisistos.contains(contato))
                            lstContratoPartesFisicasSemRequisistos.add(contato);
                    }
                });
            }
        });


        Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_LIBERADO_WHATSAPP,
                contrato, lstContratoPartesFisicasSemRequisistos);

        if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
            Runnable runnableAssinatura = new EmailTask.EnviaLiberacaoContratoAssinatura(contrato, mapEnvio.get(TipoEnvioMsgEnum.EMAIL), false);
            Thread threadAssinatura = new Thread(runnableAssinatura);
            threadAssinatura.start();
        }
        if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
            Runnable runnable = new CelularTask.EnviaLiberacaoContratoAssinaturaWhatsApp(contrato, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP), false);
            Thread thread = new Thread(runnable);
            thread.start();
        }

        Boolean enviaEmailObservadorSomenteLiberacao = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.ENVIAR_EMAIL_OBSERVADOR_SOMENTE_APOS_ASSINATURA, contrato.getCustodiante());

        if (!enviaEmailObservadorSomenteLiberacao) {

            Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvioObservacao = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_LIBERADO_OBSERVADOR_WHATSAPP,
                    contrato, lstPartesObservador);

            if (!CommonsUtil.semValor(mapEnvioObservacao.get(TipoEnvioMsgEnum.EMAIL))) {

            }

            if (!CommonsUtil.semValor(mapEnvioObservacao.get(TipoEnvioMsgEnum.EMAIL))) {
                Runnable runnableObservador = new EmailTask.EnviaLiberacaoContratoObservador(contrato, mapEnvioObservacao.get(TipoEnvioMsgEnum.EMAIL));
                Thread threadObservador = new Thread(runnableObservador);
                threadObservador.start();
            }

            if (!CommonsUtil.semValor(mapEnvioObservacao.get(TipoEnvioMsgEnum.WHATSAPP))) {
                Runnable runnable = new CelularTask.EnviaLiberacaoContratoObservadorWhatsApp(contrato, mapEnvioObservacao.get(TipoEnvioMsgEnum.WHATSAPP));
                Thread thread = new Thread(runnable);
                thread.start();
            }


        }
    }

    private boolean ehObseravdor(ContratoParte parte, Papel papelObservador) {

        if (papelObservador != null) {
            for (ContratoPartePapel papel : parte.getPapel()) {
                if (CommonsUtil.mesmoValor(papel.getPapel().getId(), papelObservador.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ValidaLiberacaoAssinatura(Contrato contrato) {
        Map<UUID, Boolean> mapContratoPartePapel = new HashMap<>(0);
        Map<UUID, List<ContratoParte>> mapContratoParte = new HashMap<>(0);

        Papel papelObservador = papelService.findByIdentificacaoAndCliente(PapelEnum.OBSERVADOR.getDescricao(), null);

        contrato.setValidado(false);
        if (contrato.getCustodiante().getPlano() != null) {
            Plano plano = planoService.findById(contrato.getCustodiante().getPlano().getId());
            Date dataInicio;
            if (!plano.getPeriodo().equals("ILIMITADO")) {
                switch (plano.getPeriodo()) {
                    case "MES":
                        dataInicio = DateUtil2.getFirstDayOfMonth(DateUtil2.getDataHoraAgora());
                        break;
                    default:
                        dataInicio = DateUtil2.getDataHoraAgora();
                }
                Date dataFim = DateUtil2.getDataHoraAgora();

                List<StatusContratoEnum> statusExclusao = new ArrayList<>(Arrays.asList(StatusContratoEnum.CANCELADO, StatusContratoEnum.RECUSADO));
                List<Contrato> contratosPeriodo = contratoRepository.findByCustodianteAndDataSolicitacaoAssinaturaBetweenAndStatusContratoNotIn(contrato.getCustodiante(), dataInicio, dataFim, statusExclusao);
                int qtdDocumentosAtivos = contratosPeriodo.size();
                if (!contratosPeriodo.contains(contrato))
                    qtdDocumentosAtivos++;

                if (qtdDocumentosAtivos > plano.getQuantidadeDocumentos()) {
                    String exception = "A quantidade de documentos para assinatura do seu plano atingiu o máximo.\nEntre em contato com seu representante ou com a central de suporte para um upgrade";
                    contrato.setValidacaoMensagem(exception);
                    return;
                }
            }
        }

        contratoLogService.salva(new ContratoLog(contrato, null, "Buscando ContratoPartes. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));

        for (ContratoParte contratoParte1 : contrato.getPartes()) {
            if (TipoPessoaEnum.FISICA.equals(contratoParte1.getTipoPessoa())) {
                if (!CriaMapContratoPartePapel(mapContratoPartePapel, mapContratoParte, contratoParte1, papelObservador)) {
                    String exception = "A parte(s) " + contratoParte1.getNomeRazaoSocial() +
                            " não possui um papel.";
                    contrato.setValidacaoMensagem(exception);
                    return;
                }
            } else {
                if (!CommonsUtil.semValor(contratoParte1.getContatos())) {

                    for (ContratoParte contato : contratoParte1.getContatos()) {
                        if (!CriaMapContratoPartePapel(mapContratoPartePapel, mapContratoParte, contato, papelObservador)) {
                            String exception = "O contato " + contato.getNomeRazaoSocial() + " da parte " + contratoParte1.getNomeRazaoSocial() +
                                    " não possui um papel.";
                            contrato.setValidacaoMensagem(exception);
                            return;
                        }
                    }
                }
            }
        }

        contratoLogService.salva(new ContratoLog(contrato, null, "Validando se todos os documntos serão assinado. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));
        Map<Papel, Boolean> mapContratoDocumento = new HashMap<>(0);

        for (ContratoDocumento contratoDocumento : contrato.getDocumentos()) {
            TipoDocumento tipoDocumento = tipoDocumentoService.findById(contratoDocumento.getTipoDocumento().getId());
            if (tipoDocumento != null){
                contratoDocumento.setTipoDocumento(tipoDocumento);
            }
        }

        if (contrato.getDocumentos().stream().filter(x -> StatusEnum.ATIVO.equals(x.getStatus()) && CommonsUtil.booleanValue( x.getTipoDocumento().getAssina())).collect(Collectors.toList()).size() == 0) {
            String exception = "É preciso ter pelo menos um documento para assinatura";
            contrato.setValidacaoMensagem(exception);
            return;
        }

        for (ContratoDocumento contratoDocumento : contrato.getDocumentos()) {
            if (!CommonsUtil.booleanValue( contratoDocumento.getTipoDocumento().getAssina()))
                continue;
            byte[] documentoOriginal = contratoDocumento.getDocumentoOriginal();
            if (CommonsUtil.semValor(documentoOriginal)) {
                documentoOriginal = getDocumentoOriginalParaValidacao(contratoDocumento);
            }
            if (CommonsUtil.semValor(documentoOriginal)) {
                String exception = "Não foi possível localizar o arquivo original do documento " + contratoDocumento.getNomeDocumento() +
                        ". Salve novamente o arquivo ou verifique se ele está disponível no armazenamento configurado.";
                contrato.setValidacaoMensagem(exception);
                return;
            }
            //valida se é um pdf valido
            PDDocument document;
            try {
                document = PDDocument.load(documentoOriginal);
                Integer qtdMaximaPagina = sistemaAtributoService.getInteger(SistemaTipoAtributoEnum.QUANTIDADE_MAXIMA_PAGINA, null);
                if (document.getPages().getCount() > qtdMaximaPagina) {
                    String exception = "O documento " + contratoDocumento.getNomeDocumento() +
                            " possui mais que " + qtdMaximaPagina + " Páginas";
                    contrato.setValidacaoMensagem(exception);
                    return;
                }

                document.close();
            } catch (Exception e) {
                String exception = "O documento " + contratoDocumento.getNomeDocumento() +
                        " não foi reconhecido como um pdf válido";
                contrato.setValidacaoMensagem(exception);
                return;
            }


            for (ContratoDocumentoPapel contratoDocumentoPapel : contratoDocumento.getPapel()) {
                if (papelObservador != null && CommonsUtil.mesmoValor(contratoDocumentoPapel.getPapel().getId(), papelObservador.getId()))
                    continue;

                Boolean boolPapelUsado = mapContratoPartePapel.get(contratoDocumentoPapel.getPapel().getId());
                if (CommonsUtil.semValor(boolPapelUsado)) {
                    String exception = "O documento " + contratoDocumento.getNomeDocumento() +
                            " precisa de uma assinatura do papel " + contratoDocumentoPapel.getPapel().getNome();
                    contrato.setValidacaoMensagem(exception);
                    return;
                }
                if (!boolPapelUsado) {
                    //marca que este papel será usado
                    mapContratoPartePapel.put(contratoDocumentoPapel.getPapel().getId(), true);
                }
            }
        }

        contratoLogService.salva(new ContratoLog(contrato, null, "Validando se todos os papeis das partes serão utlizados. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));
        for (Map.Entry<UUID, Boolean> entry : mapContratoPartePapel.entrySet()) {
            if (!entry.getValue()) {
                String parte = "";
                String concatenar = "";
                for (ContratoParte contratoParte : mapContratoParte.get(entry.getKey())) {
                    parte = parte + concatenar + contratoParte.getNomeRazaoSocial();
                    concatenar = ", ";
                }
                Papel papel = papelService.findById(entry.getKey());
                String exception = "A(s) parte(s) " + parte +
                        " possui(em) o papel " + papel.getNome() + " que não será usado em nenhum documento.";
                contrato.setValidacaoMensagem(exception);
                return;
            }
        }


        contratoLogService.salva(new ContratoLog(contrato, null, "Validando se os prerequistos são válidos. Identificador: " +
                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                DataUtil.getCarimboTempo(), true));

        for (ContratoParte contratoParte : contrato.getPartes()) {
            if (!CommonsUtil.semValor(contratoParte.getRequisitoAssinatura())) {
                ContratoParte validacao = contratoParte;

                while (validacao != null) {
                    if (contratoParte.getRequisitoAssinatura() != null) {

                        String requisitoAssinatura = validacao.getRequisitoAssinatura();
                        validacao = null;

                        for (ContratoParte parte : contrato.getPartes()) {
                            if (CommonsUtil.mesmoValor(parte.getCpfCnpj(), requisitoAssinatura)) {
                                validacao = parte;
                                break;
                            }
                            if (TipoPessoaEnum.JURIDICA.equals(parte.getTipoPessoa())) {
                                for (ContratoParte contato : parte.getContatos()) {
                                    if (CommonsUtil.mesmoValor(contato.getCpfCnpj(), requisitoAssinatura)) {
                                        validacao = contato;
                                        break;
                                    }
                                }
                            }
                        }

                        //não achou uma inicial
                        if (CommonsUtil.semValor(validacao) || CommonsUtil.mesmoValor(validacao.getRequisitoAssinatura(), contratoParte.getCpfCnpj())) {
                            String exception = "A parte " + contratoParte.getNomeRazaoSocial() +
                                    " possui um requisito de assinatura inválido. ";
                            contrato.setValidacaoMensagem(exception);
                            return;
                        }
                        //achou uma assinatura inicial
                        if (CommonsUtil.semValor(validacao.getRequisitoAssinatura())) {
                            break;
                        }
                    }
                }
            }
        }

        contrato.setValidado(true);
        contrato.setValidacaoMensagem(null);
    }

    private byte[] getDocumentoOriginalParaValidacao(ContratoDocumento contratoDocumento) {
        byte[] documentoOriginal = null;

        try {
            ContratoDocumentoVisualizaResponse response = contratoDocumentoService.getArquivoEmDiretorio(
                    Optional.of(contratoDocumento), contratoDocumento.getDocumentoOriginalSHA256(), false, true);
            documentoOriginal = getDocumentoBytes(response);
        } catch (NoSuchAlgorithmException | NegocioException | RuntimeException e) {
            documentoOriginal = getDocumentoOriginalBase(contratoDocumento);
        }

        if (CommonsUtil.semValor(documentoOriginal)) {
            documentoOriginal = getDocumentoOriginalBase(contratoDocumento);
        }

        return documentoOriginal;
    }

    private byte[] getDocumentoOriginalBase(ContratoDocumento contratoDocumento) {
        if (CommonsUtil.semValor(contratoDocumento) || CommonsUtil.semValor(contratoDocumento.getId())) {
            return null;
        }

        ContratoDocumentoVisualizaResponse response = contratoDocumentoRepository.getDocumentoOriginal(contratoDocumento.getId());
        return getDocumentoBytes(response);
    }

    private byte[] getDocumentoBytes(ContratoDocumentoVisualizaResponse response) {
        if (response == null) {
            return null;
        }

        if (!CommonsUtil.semValor(response.getDocumentoBytes())) {
            return response.getDocumentoBytes();
        }

        try {
            if (!CommonsUtil.semValor(response.getDocumentoPDF())) {
                return response.getDocumento();
            }
        } catch (RuntimeException e) {
            return null;
        }

        return null;
    }

    private Boolean CriaMapContratoPartePapel(Map<UUID, Boolean> mapContratoPartePapel, Map<UUID, List<ContratoParte>> mapContratoParte,
                                              ContratoParte contratoParte, Papel papelObservador) {

        boolean result = !CommonsUtil.semValor(contratoParte.getPapel());

        contratoParte.getPapel().forEach(contratoPartePapel -> {

            if (papelObservador == null || !CommonsUtil.mesmoValor(contratoPartePapel.getPapel().getId(), papelObservador.getId())) {

                Boolean boolPapelUsado = mapContratoPartePapel.get(contratoPartePapel.getPapel());
                if (CommonsUtil.semValor(boolPapelUsado)) {
                    mapContratoPartePapel.put(contratoPartePapel.getPapel().getId(), true);
                }

                List<ContratoParte> lstContratoParte = mapContratoParte.get(contratoPartePapel.getPapel());
                if (CommonsUtil.semValor(lstContratoParte)) {
                    lstContratoParte = new ArrayList<>(0);
                }
                lstContratoParte.add(contratoParte);
                mapContratoParte.put(contratoPartePapel.getPapel().getId(), lstContratoParte);
            }

        });

        return result;
    }

    public CurrentUser ValidaChaveAcesso(String chaveAcesso) {
        CurrentUser userAuthentication = null;

        ContratoParte contratoParte = contratoParteRepository.findByStatusChaveAcesso(StatusEnum.ATIVO, chaveAcesso);
        if (!CommonsUtil.semValor(contratoParte)) {
            Usuario usuario = usuarioService.findBycpf(contratoParte.getCpfCnpj());

            if (CommonsUtil.semValor(usuario)) {


                contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Gerando novo usuario para " +
                        contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail()
                        + " celular: " + CommonsUtil.stringValueVazio(contratoParte.getCelular()),
                        DataUtil.getCarimboTempo(), true));

                Pessoa pessoa = pessoaService.findByCpfCnpj(contratoParte.getCpfCnpj());
                Cliente clienteSistema = clienteService.retornaClienteSistema();

                if (CommonsUtil.semValor(pessoa)) {
                    pessoa = new Pessoa();
                    pessoa.setTipoPessoa(TipoPessoaEnum.FISICA);
                    pessoa.setNomeRazaoSocial(contratoParte.getNomeRazaoSocial());
                    pessoa.setCpfCnpj(contratoParte.getCpfCnpj());
                    pessoa.setEmail(contratoParte.getEmail());
                    pessoa.setCliente(clienteSistema);

                    if (!CommonsUtil.semValor(contratoParte.getCelular())) {
                        if (CommonsUtil.semValor(pessoa.getPessoaTelefone())) {
                            pessoa.setPessoaTelefone(new ArrayList<>(0));
                        }

                        PessoaTelefone pessoaTelefone = new PessoaTelefone();
                        pessoaTelefone.setNumero(contratoParte.getCelular());
                        TipoTelefone tipoTelefone = tipoTelefoneRepository.findByIdentificacaoAndCliente("CELULAR", clienteSistema);
                        pessoaTelefone.setTipoTelefone(tipoTelefone);
                    }

                    pessoa = pessoaService.save(pessoa);
                }

                usuario = new Usuario();
                usuario.setPessoa(pessoa);
                String login = contratoParte.getCpfCnpj(); // contratoParte.getEmail().substring(0, contratoParte.getEmail().indexOf("@"));

                //login = ValidaLoginExistente(login);

                usuario.setLogin(login);

                String senha = Util.GenerateCommonLangPassword(true, false);
//                usuario.setSenha(passwordEncoder.encode(senha));
                usuario.setSenha(passwordEncoder.encode(senha));
                usuario.setPerfil(PerfilEnum.ROLE_ASSINADOR);
                usuario.setStatus(StatusEnum.ATIVO);
                usuario = usuarioService.save(usuario);

                contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Novo usuario gerado para " +
                        usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail(),
                        DataUtil.getCarimboTempo(), true));


                //envia e-mail usuario
                Runnable runnable = new EmailTask.EnviaNovoUsario(contratoParte.getContrato(), usuario, senha);
                Thread thread = new Thread(runnable);
                thread.start();
            }

            userAuthentication = new CurrentUser(null, new UsuarioResponse(usuario), null, contratoParte.getContrato().getId(), contratoParte.getContrato().getCustodiante().getId());
        }
        return userAuthentication;
    }

    public boolean ExcluiProcurador(ContratoParte ContratoParte) {
        ContratoParte.setStatus(StatusEnum.INATIVO);
        contratoParteRepository.save(ContratoParte);
        return true;
    }

    public List<ContratoDocumento> BuscaDocumentosParaAssinar(Contrato contrato, ContratoParte contratoParte) {
        return contratoDocumentoRepository.BuscaDocumentosParaAssinar(contrato, contratoParte, StatusEnum.ATIVO);
    }

    public boolean SalvaAssinaturasLocal(AssinadorAssinaturasLogsRequest assinaturas) throws Exception {


        Contrato contrato = findById(assinaturas.getContrato().getId());
        if (StatusContratoEnum.LIBERADOASSINATURA.equals(contrato.getStatusContrato())) {
            contrato.setStatusContrato(StatusContratoEnum.PARCIALMENTEASSINADO);
            contratoRepository.save(contrato);
        }

        List<ContratoParteDocumento> contratoParteDocumentos = new ArrayList<>(0);
        if (!CommonsUtil.semValor(assinaturas.getContratoParteDocumentos())) {
            for (AssinadorContratoParteDocumentoRequest contratoParteDocumento : assinaturas.getContratoParteDocumentos()) {
                contratoParteDocumentos.add(new ContratoParteDocumento(contratoParteDocumento));
            }

            contratoParteDocumentos = contratoParteDocumentoRepository.saveAll(contratoParteDocumentos);
        }

        if (!CommonsUtil.semValor(contratoParteDocumentos)) {
            List<ContratoParteDocumento> documentos = contratoParteDocumentos.stream().filter(StreamUtil.distinctByKey(p -> p.getDocumento().getId()))
                    .collect(Collectors.toList());

            List<UUID> listIdTipoDocumento = contratoParteDocumentos.stream().map(t -> t.getDocumento().getTipoDocumento().getId()).distinct().collect(Collectors.toList());

            List<TipoDocumento> listTipoDocumento = tipoDocumentoService.findByIdIn(listIdTipoDocumento);

            for (ContratoParteDocumento contratoParteDocumento : documentos) {
                ContratoDocumento contratoDocumento = contratoParteDocumento.getDocumento();
                contratoDocumento.getPapel().forEach(papel -> {
                    papel.setContratoDocumento(contratoDocumento);
                });

                TipoDocumento tipoDocumento = listTipoDocumento.stream().filter(t -> CommonsUtil.mesmoValor(t.getId(), contratoParteDocumento.getDocumento().getTipoDocumento().getId())).findFirst()
                        .orElse(null);
                contratoDocumento.setTipoDocumento(tipoDocumento);

                if (ValidaDocumentoAssinado(contratoParteDocumento.getDocumento())) {
                    contratoDocumento.setDocumentoAssinado();
                } else {
                    contratoDocumento.setStatusDocumento(StatusDocumentoEnum.PARCIALMENTEASSINADO);
                }
                contratoDocumentoRepository.save(contratoDocumento);
            }

        }

        List<ContratoLog> contratoLogs = new ArrayList<>(0);
        if (!CommonsUtil.semValor(assinaturas.getLogs())) {
            assinaturas.getLogs().forEach(log -> {
                contratoLogs.add(new ContratoLog(log));
            });
        }


        if (!CommonsUtil.semValor(contratoLogs))
            contratoLogService.SalvaLogsRemotos(contratoLogs);

        return true;

    }

    public void MigrarDocumentoParaStorage() {


        if (!CommonsUtil.booleanValue(env.getProperty("JobMigrarDocumentoParaStorage"))) {
            System.out.println("JobMigrarDocumentoParaStorage - MigrarDocumentoParaStorage() - Desabilitado");
            return;
        }

        String fileNameJob = "JobMigrarDocumentoParaStorage.txt";
        File fileJob = new File(fileNameJob);
        if (fileJob.exists()) {
            System.out.println("JobMigrarDocumentoParaStorage - MigrarDocumentoParaStorage() - Em Execução...");
            return;
        }


        //List<Contrato> documentos = contratoDocumentoRepository.verificaDocumentosParaMigracao();
        List<ContratoDocumento> documentos = contratoDocumentoRepository.findFirst100ByDocumentoOriginalIsNotNull();
        if (documentos == null) {
            return;
        }

        String inicio = Instant.now().toString();

        OutputStream os = null; // nome do arquivo que será escrito
        try {

            os = new FileOutputStream(fileJob);
            Writer wr = new OutputStreamWriter(os); // criação de um escritor
            BufferedWriter br = new BufferedWriter(wr); // adiciono a um escritor de buffer

            System.out.println("---------------------------------------------------------------------------");
            System.out.println("ContratoService - TOTAL DE DOCUMENTOS em MigrarDocumentoParaStorage() : "+documentos.size());
            System.out.println("");
            System.out.println("Inicio: "+ inicio);
            System.out.println("");

            br.write("---------------------------------------------------------------------------");
            br.newLine();
            br.write("ContratoService - TOTAL DE DOCUMENTOS em MigrarDocumentoParaStorage() : "+documentos.size());
            br.newLine();
            br.newLine();
            br.write("Inicio: "+ Instant.now().toString());
            br.newLine();
            int contador = 0;
            int total = documentos.size();
            for (ContratoDocumento documento : documentos) {
                //Contrato contrato = documento.getContrato();
                contador++;
                System.out.println("   > Migrando: "+contador+"/"+total+" - "+documento.getNomeDocumento());
                br.write("   > Migrando: "+documento.getNomeDocumento());
                br.newLine();

                atualizaDocumentoEmStorage(documento);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (fileJob.exists()) {
                System.out.println("");
                System.out.println("* * * * * Finalizando o JobMigrarDocumentoParaStorage * * * * * ");
                System.out.println("");
                System.out.println("Inicio: "+ inicio);
                System.out.println("Fim   : "+ Instant.now().toString());
                System.out.println("---------------------------------------------------------------------------");

                fileJob.delete();
            }
        }
    }

    public void GerarContratosAssinadosPendentes() {


        if (!CommonsUtil.booleanValue(env.getProperty("JobGerarContratosAssinadosPendentes"))) {
            System.out.println("JobGerarContratosAssinadosPendentes - GerarContratosAssinadosPendentes() - Desabilitado");
            return;
        }

        String fileNameJob = "JobGerarContratosAssinadosPendentes.txt";
        File fileJob = new File(fileNameJob);
        if (fileJob.exists()) {
            System.out.println("JobGerarContratosAssinadosPendentes - GerarContratosAssinadosPendentes() - Em Execução...");
            return;
        }

        Date dataReferencia = DateUtil2.adicionarPeriodo(DateUtil2.getDataHoraAgora(), -2, Calendar.MINUTE);
        List<Contrato> contratos = contratoParteDocumentoRepository.verificaContratosAssinadoComDocumentosAssinadosPendentes(dataReferencia);
        if (contratos == null) {
            return;
        }

        int contadorContrato = 1;
        int totalContratosJob = 50;

        OutputStream os = null; // nome do arquivo que será escrito
        try {
            os = new FileOutputStream(fileJob);
            Writer wr = new OutputStreamWriter(os); // criação de um escritor
            BufferedWriter br = new BufferedWriter(wr); // adiciono a um escritor de buffer

            br.write("---------------------------------------------------------------------------");
            br.newLine();
            br.write("ContratoService - TOTAL DE CONTRATOS em GerarContratosAssinadosPendentes() : "+contratos.size()+" - Data Ref.: "+dataReferencia.toString());
            br.newLine();
            br.newLine();
            br.write("Inicio: "+ Instant.now().toString());
            br.newLine();

            System.out.println("---------------------------------------------------------------------------");
            System.out.println("ContratoService - TOTAL DE CONTRATOS em GerarContratosAssinadosPendentes() : "+contratos.size()+" - Data Ref.: "+dataReferencia.toString());
            System.out.println();
            System.out.println("Inicio: "+ Instant.now().toString());

            for (Contrato contrato : contratos) {
                System.out.println("   > GerarContratosAssinadosPendentes para: "+contrato.getCustodiante().getPessoa().getNomeRazaoSocial()
                        +" - "+contrato.getIdentificador()
                        +" - Data: "+contrato.getDataCriacao().toString());
                br.write("   > GerarContratosAssinadosPendentes para: "+contrato.getCustodiante().getPessoa().getNomeRazaoSocial()
                        +" - "+contrato.getIdentificador()
                        +" - Data: "+contrato.getDataCriacao().toString());
                br.newLine();
                contadorContrato++;
                if (contadorContrato > totalContratosJob )
                    break;
            }

            contadorContrato = 0;
            for (Contrato contrato : contratos) {
                try {
                    for (int iparte = contrato.getPartes().size() - 1; iparte >= 0; iparte--) {
                        ContratoParte parte = contrato.getPartes().get(iparte);
                        if (parte.getContratoPartePJ() != null) {
                            contrato.getPartes().remove(parte);
                        }
                    }

                    Runnable runnable = new AssinaturaTask.GeraAssinaturaDocumentos(contrato);
                    Thread thread = new Thread(runnable);
                    thread.setName("assin-" + contrato.getId().toString());
                    thread.start();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                contadorContrato++;
                if (contadorContrato > totalContratosJob )
                    break;

            }
            br.write("Final: "+ Instant.now().toString());
            br.newLine();
            br.write("---------------------------------------------------------------------------");
            System.out.println("Final: "+ Instant.now().toString());
            System.out.println("---------------------------------------------------------------------------");
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileJob.exists()) {
                System.out.println("* * * * * Finalizando o JobGerarContratosAssinadosPendentes * * * * * ");
                fileJob.delete();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void VerificarContratosAssinadosComStatusGerandoAssinatura() {

        Date dataReferencia = DateUtil2.adicionarPeriodo(DateUtil2.getDataHoraAgora(), -5, Calendar.MINUTE);

        List<Contrato> contratos = contratoParteDocumentoRepository.verificaContratosAssinadosComStatusGerandoAssinatura(dataReferencia);

        contratos.forEach(contrato -> {
            contrato.setStatusContrato(StatusContratoEnum.ASSINADO);
        });
        try {
            contratoRepository.saveAll(contratos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void VerificarContratosLiberadosParaAssinatura() {

        Date dataReferencia = DateUtil2.adicionarPeriodo(DateUtil2.getDataHoraAgora(), -10, Calendar.MINUTE);

        List<Contrato> contratos = contratoParteDocumentoRepository.VerificarContratosLiberadosParaAssinatura(dataReferencia);

        contratos.forEach(this::ExecutaLiberacaoAssinatura);
    }

    public void cancelarContratosPendentes() {

        if (!CommonsUtil.booleanValue(env.getProperty("JobCancelarContratosPendentes"))) {
            System.out.println("JobCancelarContratosPendentes - cancelarContratosPendentes - Desabilitado");
            return;
        }

        Date dataHoje = DataUtil.getCarimboTempo().getDataCarimboTempo();

        List<Contrato> contratos = contratoParteDocumentoRepository.verificaContratosNaoAssinadosParaCancelamento(dataHoje);

        contratos.forEach(contrato -> {
            //contrato.setUsuarioCancelamento();
            contrato.setDataCancelamento(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contrato.setStatusContrato(StatusContratoEnum.CANCELADO);
            contrato.setMotivoCancelamento("Cancelamento automático");
            contratoLogService.salva(new ContratoLog(contrato, null, "Cancelamento efetuado automáticamente.",
                    DataUtil.getCarimboTempo(), true));
            contrato = Salva(contrato);
        });

    }

    public void cancelarContrato(ContratoCancelamentoRequest contratoCancelamentoRequest) {
        for (UUID idContrato : contratoCancelamentoRequest.getContratos()) {
            Contrato contrato = findById(idContrato);
            contrato.setUsuarioCancelamento(contratoCancelamentoRequest.getUsuario());
            contrato.setDataCancelamento(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contrato.setStatusContrato(StatusContratoEnum.CANCELADO);
            contrato.setMotivoCancelamento(contratoCancelamentoRequest.getMotivo());

            contratoLogService.salva(new ContratoLog(contrato, null, "Cancelamento efetuado por " +
                    contratoCancelamentoRequest.getUsuario().getPessoa().getNomeRazaoSocial(),
                    //" cpf: " +
                    //contratoCancelamentoRequest.getUsuario().getPessoa().getCpfCnpj().toString(),
                    DataUtil.getCarimboTempo(), true));
            contrato = Salva(contrato);


        }
    }

    public void recusarContrato(ContratoCancelamentoRequest contratoCancelamentoRequest) {
        Thread thread = null;

        for (UUID idContrato : contratoCancelamentoRequest.getContratos()) {
            Contrato contrato = findById(idContrato);
            contrato.setUsuarioRecusa(contratoCancelamentoRequest.getUsuario());
            contrato.setDataRecusa(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contrato.setStatusContrato(StatusContratoEnum.RECUSADO);
            contrato.setMotivoRecusa(contratoCancelamentoRequest.getMotivo());

            List<ContratoPartePapel> contratoPartePapels = new ArrayList<>(0);

            //reset situacao partes
            contrato.getPartes().forEach(parte -> {
                parte.setChaveAcesso(null);
                parte.setTokenAssinatura(null);
                parte.setStatusAssinatura(StatusAssinaturaEnum.NAOASSINADO);

                contratoPartePapels.addAll(parte.getPapel());
            });

            //reset documentos
            contrato.getDocumentos().forEach(documento -> {
                documento.setStatusDocumento(StatusDocumentoEnum.NAOASSINADO);
                documento.setDocumentoAssinado(null);
            });


            contratoParteDocumentoRepository.resetContratoParteDocumento(contratoPartePapels);


            contratoLogService.salva(new ContratoLog(contrato, null, "Cancelamento efetuado por " +
                    contratoCancelamentoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " cpf: " +
                    contratoCancelamentoRequest.getUsuario().getPessoa().getCpfCnpj(),
                    DataUtil.getCarimboTempo(), true));
            contrato = Salva(contrato);

            Runnable runnable = new EmailTask.EnviaContratoRecusado(contrato, contrato.getPartes());
            thread = new Thread(runnable);
            thread.start();

        }
    }

    private Boolean ValidaDocumentoAssinado(ContratoDocumento contratoDocumento) {
        List<ContratoParteDocumentoPendente> pendentes = contratoParteDocumentoRepository.verificaAssinaturasPendentesDocumento(contratoDocumento);
        return CommonsUtil.semValor(pendentes);
    }

    public void GerarDocumentosCompactados() {
//        System.out.println("GerarDocumentosCompactados iniciado...");
//
//        Pageable topTen = PageRequest.of(0, 100);
//
//        List<ContratoDocumento> contratosDocumento = contratoDocumentoRepository.verificaDocumentosNaoCompactados(topTen);
//
//        if (CommonsUtil.semValor(contratosDocumento)) {
//            return;
//        }
//
//        int iTotalDocs = contratosDocumento.size();
//        int iDocs = 0;
//        System.out.println("GerarDocumentosCompactados: " + iTotalDocs + " docs");
//
//
//        for (ContratoDocumento contratoDocumento : contratosDocumento) {
//            if (contratoDocumento.getDocumentoOriginal() != null)
//                contratoDocumento.setDocumentoOriginalCompactado(LZString.compressToUTF16(Base64.getEncoder().encodeToString(contratoDocumento.getDocumentoOriginal())));
//            if (contratoDocumento.getDocumentoAssinado() != null)
//                contratoDocumento.setDocumentoAssinadoCompactado(LZString.compressToUTF16(Base64.getEncoder().encodeToString(contratoDocumento.getDocumentoAssinado())));
//
//
//            contratoDocumentoRepository.save(contratoDocumento);
//            iDocs++;
//            System.out.println("GerarDocumentosCompactados: " + iDocs + "/" + iTotalDocs + " docs");
//        }

    }

    //region Integração

    //region Envio Documento

    public String EnvioDocumentoXML(String arquivoXML) {
        RetornoEnvioModel retornoEnvioModel = new RetornoEnvioModel();

        ContratoModel contrato = new ContratoModel();
        try {

            String decodedString = arquivoXML;

            //arquivo xml em base64
            if (!decodedString.contains("<contrato>")) {
                byte[] xmlDecode = org.apache.commons.codec.binary.Base64.decodeBase64(arquivoXML);
                decodedString = new String(xmlDecode);
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource isXML = new InputSource(new StringReader(decodedString));

            Document doc = dBuilder.parse(isXML);
            doc.getDocumentElement().normalize();

            String codigoCustodiante = getValue("codigoCustodiante", doc.getDocumentElement());
            ValidaCustodiante(codigoCustodiante, retornoEnvioModel);

            String indicaTipoInf = getValue("indicaTipoInf", doc.getDocumentElement());
            ValidaInidicaTipoInf(indicaTipoInf, retornoEnvioModel);

            String tipoContrato = getValue("tipoContrato", doc.getDocumentElement());
            ValidaTipoContrato(tipoContrato, retornoEnvioModel);

            String moeda = getValue("moeda", doc.getDocumentElement());
            ValidaMoeda(retornoEnvioModel, moeda);

            String valorContrato = getValue("valorContrato", doc.getDocumentElement());
            ValidaValorContrato(valorContrato, retornoEnvioModel);

            String remetente = getValue("remetente", doc.getDocumentElement());
            ValidaRemetente(remetente, retornoEnvioModel);

            String identificador = getValue("identificador", doc.getDocumentElement());
            ValidaIdentificador(retornoEnvioModel, identificador);

            String assunto = getValue("assunto", doc.getDocumentElement());
            ValidaAssunto(assunto, retornoEnvioModel);

            String assinaturaDuplicatasObrigatorias = getValue("assinaturaDuplicatasObrigatoria", doc.getDocumentElement());

//            Retirado o layout, sem funcao
//            NodeList nodeIinfEmpresas = doc.getElementsByTagName("infEmpresas");
//            if (nodeIinfEmpresas.getLength() > 1) {
//                retorno.adicionaErro(CodigoErrosEnum.Erro_127);
//            }

            Cliente custodiante = clienteService.findById(UUID.fromString(codigoCustodiante));
            ValidaProcuradores(doc, assinaturaDuplicatasObrigatorias, retornoEnvioModel, contrato, custodiante);
            ValidaValidadores(doc, retornoEnvioModel, contrato);
            ValidaDocumento(doc, retornoEnvioModel, contrato);
            ValidaDocumentoCartaCessao(doc, retornoEnvioModel, contrato);
            ValidaDuplicatas(doc, assinaturaDuplicatasObrigatorias, retornoEnvioModel, contrato);

            if (retornoEnvioModel.getStatusProcessamento() == 1) {
                contrato.setRemetente(remetente);
                contrato.setIdentificador(identificador);
                contrato.setAssunto(assunto);
                contrato.setTipoContrato(tipoContrato);
                contrato.setMoeda(moeda);
                contrato.setValorContratro(CommonsUtil.doubleValue(valorContrato));
                contrato.setCodigoCustodiante(CommonsUtil.UUIDValue(codigoCustodiante));
                contrato.setTipoIntegracao(CommonsUtil.intValue(indicaTipoInf));

                // retornoEnvioModel.setContrato(contrato);

                //Salvar na base de dados
                retornoEnvioModel.setIdAssinaNet(SaveContrato(contrato));

            }

        } catch (Exception ex) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_35, ex.getMessage());
            //ex.printStackTrace();
        }

        return CommonsUtil.jaxbObjectToXML(retornoEnvioModel);
    }

    private void ValidaMoeda(RetornoEnvioModel retornoEnvioModel, String moeda) {
        if (TAG_NAO_EXISTENTE.equals(moeda)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: moeda");
        } else if (CommonsUtil.semValor(moeda)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: moeda");
        }
        //TODO: Validar moeda cadastrado na base de dados, erros 20
    }

    private void ValidaTipoContrato(String tipoContrato, RetornoEnvioModel retornoEnvioModel) {
        if (TAG_NAO_EXISTENTE.equals(tipoContrato)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: tipoContrato");
        } else if (CommonsUtil.semValor(tipoContrato)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: tipoContrato");
        }
        //TODO: Validar tipo de contrato cadastrado na base de dados, erros 18 e 19
    }

    private void ValidaAssunto(String assunto, RetornoEnvioModel retornoEnvioModel) {
        if (TAG_NAO_EXISTENTE.equals(assunto)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: assunto");
        } else if (CommonsUtil.semValor(assunto)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: assunto");
        }
    }

    private void ValidaValorContrato(String valorContrato, RetornoEnvioModel retornoEnvioModel) {

        if (TAG_NAO_EXISTENTE.equals(valorContrato)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: valorContrato");
        } else if (CommonsUtil.semValor(valorContrato)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: valorContrato");
        } else {
            try {
                double valor = CommonsUtil.castAsDouble(valorContrato);
            } catch (Exception e) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_21);
            }

            if (!valorContrato.substring(valorContrato.length() - 3).startsWith(".")) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_22);
            }
        }
    }

    private void ValidaInidicaTipoInf(String indicaTipoInf, RetornoEnvioModel retornoEnvioModel) {
        if (TAG_NAO_EXISTENTE.equals(indicaTipoInf)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: indicaTipoInf");
        } else if (CommonsUtil.semValor(indicaTipoInf)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: indicaTipoInf");
        } else {
            if (!CommonsUtil.eSomenteNumero(indicaTipoInf)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_3);
            } else {
                if (!(indicaTipoInf.equals("1") || indicaTipoInf.equals("2"))) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_4);
                }
            }
        }
    }

    private void ValidaDuplicatas(Document doc, String assinaturaDuplicatasObrigatorias, RetornoEnvioModel
            retornoEnvioModel,
                                  ContratoModel contrato) {
        String duplicatas = getValue("documentoDuplicatas", doc.getDocumentElement());

        if (CommonsUtil.semValor(duplicatas)) {
            return;
        }

        if (!CommonsUtil.semValor(duplicatas) && CommonsUtil.semValor(assinaturaDuplicatasObrigatorias)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_40);
        }

        byte[] bArrayDuplicata = null;

        try {
            bArrayDuplicata = org.apache.commons.codec.binary.Base64.decodeBase64(duplicatas);
        } catch (Exception e) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_42);
            return;
        }

        try {

            if (!ValidationUtil.validaTXT(bArrayDuplicata)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_43);
                return;
            }

            CSVParser parser = null;
            try {
                parser = CSVFormat.DEFAULT.parse(
                        new InputStreamReader(new ByteArrayInputStream(bArrayDuplicata), StandardCharsets.UTF_8));
            } catch (IOException e) {
                parser.close();
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_43);
                return;
            }

            ReportDataSource reportDataSource = new ReportDataSource();

            for (CSVRecord record : parser) {
                try {
                    //valida qtd de colunas

                    if (record.size() != 25) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_45, "Linha " + record.getParser().getCurrentLineNumber() + ": contém " + record.size() + " campos.");
                    } else {
                        ContratoDuplicatasModel contratoDuplicata = new ContratoDuplicatasModel();

                        CommonsUtil.simpleCopyCSVProperties(contratoDuplicata, record,
                                "cedenteNome", "cedenteEndereco", "cedenteBairro", "cedenteCEP", "cedenteCidade",
                                "cedenteUF", "cedenteTelefone", "cedenteCNPJ", "cedenteInscricaoEstadual", "duplicataDataEmissao",
                                "faturaNumero", "duplicataValor", "duplicataOrdem", "duplicataDataVencimento", "duplicataCondicoesEspeciais",
                                "sacadoNome", "sacadoEndereco", "sacadoBairro", "sacadoCidade", "sacadoCEP", "sacadoUF", "duplicataPracaPagamento",
                                "sacadoCNPJ", "sacadoInscricaoEstadual", "duplicataTipo");

                        if (CommonsUtil.mesmoValor("Prestação de Serviço", contratoDuplicata.getDuplicataTipo())) {
                            contratoDuplicata.setDuplicataTipo("DUPLICATA DE PRESTAÇÃO DE SERVIÇOS");
                        } else {
                            contratoDuplicata.setDuplicataTipo("DUPLICATA DE VENDA MERCANTIL");
                        }

                        contratoDuplicata.setDuplicataValorExtenso(CommonsUtil.converteValorEmExtenso(contratoDuplicata.getDuplicataValorDouble()));
                        List<ContratoDuplicatasModel> dataSource = new ArrayList<>(0);
                        dataSource.add(contratoDuplicata);
                        contratoDuplicata.setDocumentoDuplicatas(reportDataSource.geraRelatorioDuplicatas(dataSource));

                        contrato.getDuplicatas().add(contratoDuplicata);

                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error at line "
                            + parser.getCurrentLineNumber(), e);
                }
            }
            parser.close();


            contrato.setDocumentoDuplicatas(bArrayDuplicata);

//            OutputStream os = new FileOutputStream( "duplicatas.pdf");
//            os.write( contrato.getDocumentoDulicatas());
//            os.close();

        } catch (Exception ex) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_35, ex.getMessage());
        }

    }

    private void ValidaDocumento(Document doc, RetornoEnvioModel retornoEnvioModel, ContratoModel contrato) {
        String documento = getValue("documento", doc.getDocumentElement());

        if (TAG_NAO_EXISTENTE.equals(documento)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: documento");
            return;
        } else if (CommonsUtil.semValor(documento)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: documento");
            return;
        }

        byte[] bArrayDocumento = null;
        try {
            bArrayDocumento = org.apache.commons.codec.binary.Base64.decodeBase64(documento);
        } catch (Exception e) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_30);
            return;
        }

        if (!ValidationUtil.validaTXTouPDF(bArrayDocumento)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_31);
            return;
        }

        contrato.setDocumento(bArrayDocumento);
    }

    private void ValidaDocumentoCartaCessao(Document doc, RetornoEnvioModel retornoEnvioModel, ContratoModel contrato) {

        NodeList nodeDocumentosCartaCessao = doc.getElementsByTagName("documentoCartasCessao");
        contrato.setDocumentosCartasCessao(new ArrayList<>(0));

        for (int i = 0; i < nodeDocumentosCartaCessao.getLength(); i++) {
            Node node = nodeDocumentosCartaCessao.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ContratoCartaCessaoModel contratoCartaCessaoModel = new ContratoCartaCessaoModel();

                Element element = (Element) node;
                String identificaoCartaCessao = getValue("identificaoCartaCessao", element);
                String conteudoCartaCessao = getValue("conteudoCartaCessao", element);
                String papelCartaCessao = getValue("papel", element);

                contratoCartaCessaoModel.setIdentificaoCartaCessao(identificaoCartaCessao);
                contratoCartaCessaoModel.setDocumentoCartasCessao(org.apache.commons.codec.binary.Base64.decodeBase64(conteudoCartaCessao));
                contratoCartaCessaoModel.setPapel(new ArrayList<>(0));
                String[] papelProc = papelCartaCessao.split(",");
                for (String papelenviado : papelProc) {
                    contratoCartaCessaoModel.getPapel().add(papelenviado);
                }

                contrato.getDocumentosCartasCessao().add(contratoCartaCessaoModel);
            }
        }

    }


    private void ValidaProcuradores(Document doc, String assinaturaDuplicatasObrigatorias, RetornoEnvioModel
            retornoEnvioModel, ContratoModel contrato, Cliente custodiante) {
        //procuradores
        NodeList nodeProcuradores = doc.getElementsByTagName("infProcuradores");

        if (nodeProcuradores == null) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores");
            return;
        } else if (CommonsUtil.semValor(nodeProcuradores)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores");
            return;
        }

        Element elemProcuradores = (Element) nodeProcuradores.item(0);
        NodeList nodeEmpresa = elemProcuradores.getElementsByTagName("empresa");

        boolean assinaDuplicata = false;
        for (int i = 0; i < nodeEmpresa.getLength(); i++) {
            Node node = nodeEmpresa.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                ContratoProcuradorModel contratoProcurador = new ContratoProcuradorModel();

                Element element = (Element) node;
                String cnpjProcurador = getValue("cnpj", element);
                if (ValidaCnpjProcurador(retornoEnvioModel, cnpjProcurador)) {
                    contratoProcurador.setCnpj(cnpjProcurador);
                }

                String razaoSocialProcurador = getValue("razaoSocial", element);
                if (TAG_NAO_EXISTENTE.equals(razaoSocialProcurador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/razaoSocial");
                } else if (CommonsUtil.semValor(razaoSocialProcurador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/razaoSocial");
                } else {
                    contratoProcurador.setRazaoSocial(razaoSocialProcurador);
                }

                NodeList nodeProcurador = element.getElementsByTagName("procurador");

                if (CommonsUtil.semValor(nodeProcurador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_36, " Empresa:" + razaoSocialProcurador);
                    continue;
                }

                for (int j = 0; j < nodeProcurador.getLength(); j++) {

                    boolean erroValidador = false;

                    Element elementProcurador = (Element) nodeProcurador.item(j);
                    String nomeProcurador = getValue("nome", elementProcurador);
                    if (TAG_NAO_EXISTENTE.equals(nomeProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/Procurador/nome");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(nomeProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/Procurador/nome");
                        erroValidador = true;
                    }

                    String cpfProcurador = getValue("cpf", elementProcurador);
                    ValidaCpfProcurador(retornoEnvioModel, erroValidador, cpfProcurador);

                    String emailProcurador = getValue("email", elementProcurador);
                    validaEmailProcurador(retornoEnvioModel, erroValidador, emailProcurador);

                    String duplicatasProcurador = getValue("duplicatas", elementProcurador);
                    if (TAG_NAO_EXISTENTE.equals(duplicatasProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/Procurador/duplicatas");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(duplicatasProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/Procurador/duplicatas");
                        erroValidador = true;
                    } else {
                        if (!(duplicatasProcurador.equals("0") || duplicatasProcurador.equals("1"))) {
                            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_47, " Procurador:" + nomeProcurador + " - Valor:" + duplicatasProcurador);
                            erroValidador = true;
                        }
                        if (duplicatasProcurador.equals("1")) {
                            assinaDuplicata = true;
                        }
                    }

                    String papelProcurador = getValue("papel", elementProcurador);
                    List<String> papelProc = null;
                    if (TAG_NAO_EXISTENTE.equals(papelProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/Procurador/papel");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(papelProcurador)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/Procurador/papel");
                        erroValidador = true;
                    } else {
                        papelProc = Arrays.asList(papelProcurador.split(","));
                        for (String papelenviado : papelProc) {
                            Papel papel = papelService.findByIdentificacaoAndCliente(papelenviado.toUpperCase(), custodiante);
                            if (papel == null) {
                                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_35,
                                        " Tag: infProcuradores/Empresa/Procurador/papel, desconhecido identifição:" + papelenviado);
                                erroValidador = true;
                            }
                        }
                    }

                    if (!erroValidador) {
                        contratoProcurador.getContatoProcurador().add(new ContratoContatoProcuradorModel(nomeProcurador, emailProcurador, cpfProcurador,
                                CommonsUtil.intValue(duplicatasProcurador), papelProc));
                    }

                }
                contrato.getProcuradores().add(contratoProcurador);
            }
        }
        if (!CommonsUtil.semValor(assinaturaDuplicatasObrigatorias) && assinaturaDuplicatasObrigatorias.equals("1") && !assinaDuplicata) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_37);
        }
    }

    private void ValidaValidadores(Document doc, RetornoEnvioModel retornoEnvioModel, ContratoModel contrato) {
        //procuradores

        NodeList nodeValidadores = doc.getElementsByTagName("infValidadores");

        if (nodeValidadores == null) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores");
            return;
        }

        Element elemValidadores = (Element) nodeValidadores.item(0);
        NodeList nodeEmpresa = elemValidadores.getElementsByTagName("empresa");
        //pode sair sem erro, não obrigatorio
        if (nodeEmpresa == null) {
            return;
        }

        for (int i = 0; i < nodeEmpresa.getLength(); i++) {
            Node node = nodeEmpresa.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                ContratoValidadorModel contratoValidador = new ContratoValidadorModel();

                Element element = (Element) node;

                String razaoSocialValidadores = getValue("razaoSocial", element);
                if (TAG_NAO_EXISTENTE.equals(razaoSocialValidadores)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores/Empresa/razaoSocial");
                } else if (CommonsUtil.semValor(razaoSocialValidadores)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infValidadores/Empresa/razaoSocial");
                } else {
                    contratoValidador.setRazaoSocial(razaoSocialValidadores);
                }

                String cnpjValidadores = getValue("cnpj", element);
                if (TAG_NAO_EXISTENTE.equals(cnpjValidadores)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores/Empresa/cnpj");
                } else if (CommonsUtil.semValor(cnpjValidadores)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infValidadores/Empresa/cnpj");
                } else {
                    if (!CommonsUtil.eSomenteNumero(cnpjValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_23, " CNPJ: " + cnpjValidadores);
                    } else {
                        if (cnpjValidadores.length() != 14) {
                            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_24, " CNPJ: " + cnpjValidadores);
                        } else if (!ValidationUtil.validaCnpj(cnpjValidadores)) {
                            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_25, " CNPJ: " + cnpjValidadores);
                        } else {
                            contratoValidador.setCnpj(cnpjValidadores);
                        }
                    }
                }

                NodeList nodeValidador = element.getElementsByTagName("validador");
                if (CommonsUtil.semValor(nodeValidador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_36, " Empresa:" + razaoSocialValidadores);
                    continue;
                }

                for (int j = 0; j < nodeValidador.getLength(); j++) {
                    boolean erroValidador = false;

                    Element elementValidador = (Element) nodeValidador.item(j);
                    String nomeValidadores = getValue("nome", elementValidador);
                    if (TAG_NAO_EXISTENTE.equals(nomeValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores/Empresa/validador/nome");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(nomeValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infValidadores/Empresa/validador/nome");
                        erroValidador = true;
                    }

                    String cpfValidadores = getValue("cpf", elementValidador);
                    if (TAG_NAO_EXISTENTE.equals(cpfValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores/Empresa/validador/cpf");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(cpfValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infValidadores/Empresa/validador/cpf");
                        erroValidador = true;
                    } else {
                        if (!CommonsUtil.eSomenteNumero(cpfValidadores)) {
                            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_26, " CPF: " + cpfValidadores);
                            erroValidador = true;
                        } else {
                            if (cpfValidadores.length() != 11) {
                                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_27, " CPF: " + cpfValidadores);
                                erroValidador = true;
                            } else if (!ValidationUtil.validaCpf(cpfValidadores)) {
                                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_28, " CPF: " + cpfValidadores);
                                erroValidador = true;
                            }
                        }
                    }

                    String emailValidadores = getValue("email", elementValidador);
                    if (TAG_NAO_EXISTENTE.equals(emailValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infValidadores/Empresa/validador/email");
                        erroValidador = true;
                    } else if (CommonsUtil.semValor(emailValidadores)) {
                        retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infValidadores/Empresa/validador/email");
                        erroValidador = true;
                    } else {
                        if (!ValidationUtil.validaEmail(emailValidadores)) {
                            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_29, " E-mail: " + emailValidadores);
                            erroValidador = true;
                        }
                    }

                    if (!erroValidador) {
                        contratoValidador.getContatoValidador().add(new ContratoContatoModel(nomeValidadores, emailValidadores, cpfValidadores));
                    }

                }

                contrato.getValidadores().add(contratoValidador);
            }
        }

    }

    private String getValue(String tag, Element element) {
        if (element == null) {
            return null;
        }
        NodeList nodeTag = element.getElementsByTagName(tag);
        if (CommonsUtil.semValor(nodeTag)) {
            return TAG_NAO_EXISTENTE;
        }
        NodeList nodes = nodeTag.item(0).getChildNodes();
        Node node = nodes.item(0);
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    //endregion

    //region Exclusao Procurador
    public String ExclusaoProcurador(String arquivoXML) {
        RetornoEnvioModel retornoEnvioModel = new RetornoEnvioModel();

        //ExclusaoProcuradorModel exclusaoProcurador = new ExclusaoProcuradorModel();
        try {

            String decodedString = arquivoXML;

            //arquivo xml em base64
            if (!decodedString.contains("<contrato>")) {
                byte[] xmlDecode = org.apache.commons.codec.binary.Base64.decodeBase64(arquivoXML);
                decodedString = new String(xmlDecode);
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource isXML = new InputSource(new StringReader(decodedString));

            Document doc = dBuilder.parse(isXML);
            doc.getDocumentElement().normalize();

            boolean erroValidador = false;
            // Element elemContrato = doc.getElementById("contrato");

            String codigoCustodiante = getValue("codigoCustodiante", doc.getDocumentElement());
            ValidaCustodiante(codigoCustodiante, retornoEnvioModel);

            String remetente = getValue("remetente", doc.getDocumentElement());
            ValidaRemetente(remetente, retornoEnvioModel);

            String identificador = getValue("identificador", doc.getDocumentElement());
            ValidaIdentificador(retornoEnvioModel, identificador);

            String cnpjEmpresa = getValue("cnpjEmpresa", doc.getDocumentElement());
            ValidaCnpjProcurador(retornoEnvioModel, cnpjEmpresa);

            String cpfProcurador = getValue("cpfProcurador", doc.getDocumentElement());
            ValidaCpfProcurador(retornoEnvioModel, erroValidador, cpfProcurador);

            // ContratoContato contato = contratoContatoRepository.localizaProcurador(CommonsUtil.UUIDValue(codigoCustodiante), identificador, cnpjEmpresa, cpfProcurador);

            ContratoParte contato = new ContratoParte();
            if (contato == null) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_100);
            }

            /*
            if (contato == null || CommonsUtil.booleanValue(contato.getAssinou())) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_102);
            }
             */


            String papelProcurador = getValue("papelProcurador", doc.getDocumentElement());
            boolean encontrouPapel = false;
            if (contato != null) {
                for (ContratoPartePapel contratoContatoPapel : contato.getPapel()) {
                    if (CommonsUtil.mesmoValor(papelProcurador, contratoContatoPapel.getPapel().getNome())) {
                        encontrouPapel = true;
                        break;
                    }
                }
            }
            if (!encontrouPapel) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_103);
            }

            String emailProcurador = getValue("emailProcurador", doc.getDocumentElement());
            validaEmailProcurador(retornoEnvioModel, erroValidador, emailProcurador);

            if (contato == null || !CommonsUtil.mesmoValor(contato.getEmail(), emailProcurador)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_104);
            }

            if (retornoEnvioModel.getStatusProcessamento() == 1) {
                //Salvar na base de dados
                ExcluiProcurador(contato);
            }

        } catch (Exception ex) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_35, ex.getMessage());
            //ex.printStackTrace();
        }

        return CommonsUtil.jaxbObjectToXML(retornoEnvioModel);
    }

    //endregion

    private void ValidaCustodiante(String codigoCustodiante, RetornoEnvioModel retornoEnvioModel) {
        if (TAG_NAO_EXISTENTE.equals(codigoCustodiante)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: codigoCustodiante");
        } else if (CommonsUtil.semValor(codigoCustodiante)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: codigoCustodiante");
        } else {
            if (CommonsUtil.semValor(codigoCustodiante) || !CommonsUtil.eUUID(codigoCustodiante)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_1, "código custodiante: " + codigoCustodiante);
            } else if (CommonsUtil.semValor(clienteService.findById(CommonsUtil.UUIDValue(codigoCustodiante)))) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_2);
            }
        }
    }

    private void ValidaRemetente(String remetente, RetornoEnvioModel retornoEnvioModel) {
        if (TAG_NAO_EXISTENTE.equals(remetente)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: remetente");
        } else if (CommonsUtil.semValor(remetente)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: remetente");
        } else if (CommonsUtil.semValor(usuarioService.findByLogin(remetente))) {
            //Validar remetente (e-mail) cadastrado na base de dados, erros 128
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_128, " Login: " + remetente);
        }

    }

    private void ValidaIdentificador(RetornoEnvioModel retornoEnvioModel, String identificador) {
        if (TAG_NAO_EXISTENTE.equals(identificador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: identificador");
        } else if (CommonsUtil.semValor(identificador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: identificador");
        }
        retornoEnvioModel.setIdContrato(identificador);
    }


    //region communs

    private boolean ValidaCnpjProcurador(RetornoEnvioModel retornoEnvioModel, String cnpjProcurador) {
        if (TAG_NAO_EXISTENTE.equals(cnpjProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/cnpj");
        } else if (CommonsUtil.semValor(cnpjProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/cnpj");
        } else {
            if (!CommonsUtil.eSomenteNumero(cnpjProcurador)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_5);
            } else {
                if (cnpjProcurador.length() != 14) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_6, " CNPJ:" + cnpjProcurador);
                } else if (!ValidationUtil.validaCnpj(cnpjProcurador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_7, " CNPJ:" + cnpjProcurador);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void ValidaCpfProcurador(RetornoEnvioModel retornoEnvioModel, boolean erroValidador, String
            cpfProcurador) {
        if (TAG_NAO_EXISTENTE.equals(cpfProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/Procurador/cpf");
            erroValidador = true;
        } else if (CommonsUtil.semValor(cpfProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/Procurador/cpf");
            erroValidador = true;
        } else {
            if (!CommonsUtil.eSomenteNumero(cpfProcurador)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_8, " CPF: " + cpfProcurador);
                erroValidador = true;
            } else {
                if (cpfProcurador.length() != 11) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_9, " CPF: " + cpfProcurador);
                    erroValidador = true;
                } else if (!ValidationUtil.validaCpf(cpfProcurador)) {
                    retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_10, " CPF: " + cpfProcurador);
                    erroValidador = true;
                }
            }
        }
    }

    private void validaEmailProcurador(RetornoEnvioModel retornoEnvioModel, boolean erroValidador, String
            emailProcurador) {
        if (TAG_NAO_EXISTENTE.equals(emailProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_33, " Tag: infProcuradores/Empresa/Procurador/email");
            erroValidador = true;
        } else if (CommonsUtil.semValor(emailProcurador)) {
            retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_32, " Tag: infProcuradores/Empresa/Procurador/email");
            erroValidador = true;
        } else {
            if (!ValidationUtil.validaEmail(emailProcurador)) {
                retornoEnvioModel.adicionaErro(CodigoErrosEnvioEnum.Erro_11, " E-mail: " + emailProcurador);
                erroValidador = true;
            }
        }
    }

    //endregion

    //endregion
}

