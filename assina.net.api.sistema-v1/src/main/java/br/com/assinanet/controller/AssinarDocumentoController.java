package br.com.assinanet.controller;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusAssinaturaEnum;
import br.com.assinanet.entity.enums.StatusContratoEnum;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.models.ArquivoDownloadModel;
import br.com.assinanet.models.ContratoListaAssinaModel;
import br.com.assinanet.models.UsuarioClienteModel;
import br.com.assinanet.request.*;
import br.com.assinanet.response.*;
import br.com.assinanet.security.jwt.JwtTokenUtil;
import br.com.assinanet.security.jwt.JwtUser;
import br.com.assinanet.security.model.CurrentUser;
import br.com.assinanet.security.service.JwtUserDetailsService;
import br.com.assinanet.service.*;
import br.com.assinanet.task.CelularTask;
import br.com.assinanet.task.EmailTask;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import br.com.assinanet.util.DateUtil2;
import br.com.assinanet.util.Util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rufus.lzstring4java.LZString;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static br.com.assinanet.util.RequestUtil.*;


/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/assinar")
@CrossOrigin(origins = "*")
public class AssinarDocumentoController {

    @Autowired
    private final ContratoService contratoService;
    private final ContratoLogService contratoLogService;
    private final EmailService emailService;
    private final ContratoParteService contratoParteService;
    private final PapelService papelService;

    private final ContratoDocumentoService contratoDocumentoService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService usuarioDetailsService;
    private final UsuarioService usuarioService;

    @Autowired
    private final HttpServletRequest request;

    public AssinarDocumentoController(ContratoService contratoService,
                                      ContratoLogService contratoLogService,
                                      EmailService emailService,
                                      ContratoParteService contratoParteService, PapelService papelService, ContratoDocumentoService contratoDocumentoService, AuthenticationManager authenticationManager,
                                      JwtTokenUtil jwtTokenUtil,
                                      JwtUserDetailsService usuarioDetailsService,
                                      UsuarioService usuarioService,
                                      HttpServletRequest request) {
        this.contratoService = contratoService;
        this.contratoLogService = contratoLogService;
        this.emailService = emailService;
        this.contratoParteService = contratoParteService;
        this.papelService = papelService;
        this.contratoDocumentoService = contratoDocumentoService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.usuarioDetailsService = usuarioDetailsService;
        this.usuarioService = usuarioService;
        this.request = request;
    }

    @PostMapping(value = "validarChaveAcesso")
    ResponseEntity<Response<CurrentUser>> ValidarChaveAcesso(@RequestBody ValidarChaveAcesso chaveAcesso) {
        Response<CurrentUser> response = new Response<>();

        CurrentUser userAuthentication = contratoService.ValidaChaveAcesso(chaveAcesso.getChaveAcesso());
        if (userAuthentication == null) {
            response.getErrors().add("Token inválido !! ");
            return ResponseEntity.badRequest().body(response);
        }

        if (!userAuthentication.getUsuario().getPessoa().getCpfCnpj().startsWith(chaveAcesso.getCpf())) {
            response.getErrors().add("CPF inválido !! ");
            final UserDetails usuarioDetails = usuarioDetailsService.loadUserByUsername(userAuthentication.getUsuario().getLogin());
            final Usuario usuario = usuarioService.findByLogin(userAuthentication.getUsuario().getLogin());
            if (CommonsUtil.semValor(usuario.getQuantidadeTentativaAcesso()))
                usuario.setPrimeiraTentativaAcesso(new Date());
            usuario.setQuantidadeTentativaAcesso((CommonsUtil.intValue(usuario.getQuantidadeTentativaAcesso()) + 1));
            List<ContratoParte> contratoPartes = contratoParteService.getContratoParteByContratoIdCpfCnpj(userAuthentication.getIdContrato(), userAuthentication.getUsuario().getPessoa().getCpfCnpj());
            ContratoParte contratoParte = contratoPartes.stream().filter(c -> CommonsUtil.mesmoValor(chaveAcesso.getChaveAcesso(), c.getChaveAcesso())).findFirst().orElse(null);
            if (contratoParte != null) {
                usuario.setContratoParteAcesso(contratoParte);
            }
            usuarioService.save(usuario);

            //revoga o link de acesso
            if (usuario.getQuantidadeTentativaAcesso() > 2) {
                if (contratoParte != null) {
                    contratoParte.setValidadeChaveAcesso(new Date());
                    contratoParteService.Save(contratoParte);
                    emailService.EnviaBloqueioChaveAcessoEmail(contratoParte);
                    response.getErrors().add("Chave de acesso bloqueada. Favor entrar em contato com " + contratoParte.getContrato().getCustodiante().getPessoa().getNomeRazaoSocial());
                }

            }
            return ResponseEntity.badRequest().body(response);
        }


        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                grantedAuthorities.add(new SimpleGrantedAuthority(userAuthentication.getUsuario().getPerfil().toString()));

                return new JwtUser(userAuthentication.getUsuario().getId(), userAuthentication.getUsuario().getLogin(),
                        userAuthentication.getUsuario().getSenha(), 1, grantedAuthorities) {
                };
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean b) throws IllegalArgumentException {
                this.setAuthenticated(b);
            }

            @Override
            public String getName() {
                return null;
            }
        };


        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails usuarioDetails = usuarioDetailsService.loadUserByUsername(userAuthentication.getUsuario().getLogin());
        final String token = jwtTokenUtil.generateToken(usuarioDetails);
        final Usuario usuario = usuarioService.findByLogin(userAuthentication.getUsuario().getLogin());
        List<UsuarioClienteModel> clientes = usuarioService.getClientes(usuario);

        //validando se o perfil nao for assinador se tem algum cliente com o mesmo peril, senão vira assinador e salva na tabela
        if (clientes.stream().filter(c -> c.getPerfil() == usuario.getPerfil()).findAny().orElse(null) == null) {
            usuario.setPerfil(PerfilEnum.ROLE_ASSINADOR);
        }
        usuario.setPrimeiraTentativaAcesso(null);
        usuario.setQuantidadeTentativaAcesso(0);
        usuario.setEnvioEmailTentativaAcesso(false);
        usuarioService.save(usuario);
        response.setData(new CurrentUser(token, new UsuarioResponse(usuario), clientes, userAuthentication.getIdContrato(), userAuthentication.getIdCliente()));
        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "buscarFiltro")
    public ResponseEntity<Response<Page<ContratoListaAssinaModel>>> findAll(@RequestBody ContratoFiltroRequest filtro, Pageable pageable) {
        Response<Page<ContratoListaAssinaModel>> response = new Response<>();

        if (!CommonsUtil.semValor(filtro.getContrato().getCustodiante())) {
            Page<ContratoListaAssinaModel> contratos = contratoService.findAllAssina(filtro, pageable);
            response.setData(contratos);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "findById")
    public ResponseEntity<Response<ContratoRequest>> findById(@RequestBody IdRequest idRequest) {
        Response<ContratoRequest> response = new Response<>();
        ContratoRequest contratoRequest = new ContratoRequest();
        Contrato contrato = contratoService.findById(idRequest.getId());
        if (!Util.verifica(contrato).isPresent()) {
            response.getErrors().add("Registro não encontrado, id:" + idRequest.getId());
            return ResponseEntity.badRequest().body(response);
        }

        contratoRequest.setContrato(new ContratoResponse(contrato, "documentos"));

        ContratoParteAssinarRequest contratoParteAssinarRequest = new ContratoParteAssinarRequest();
        contratoParteAssinarRequest.setContrato(contrato);
        contratoParteAssinarRequest.setUsuario(idRequest.getUsuario());

        ContratoParte contratoParteAssinatura = null;
        for (ContratoParteResponse contratoParteResponse : contratoRequest.getContrato().getPartes()) {
            //somente se foi liberado
            contratoParteAssinatura = VerificaAssinaturaPendente(idRequest, contratoRequest, contratoParteResponse);
            if (!CommonsUtil.semValor(contratoParteAssinatura)) {
                break;
            } else {
                if (!CommonsUtil.semValor(contratoParteResponse.getContatos())) {
                    for (ContratoParteResponse contatoParteResponse : contratoParteResponse.getContatos()) {
                        contratoParteAssinatura = VerificaAssinaturaPendente(idRequest, contratoRequest, contatoParteResponse);
                        if (!CommonsUtil.semValor(contratoParteAssinatura)) {
                            break;
                        }
                    }
                    if (!CommonsUtil.semValor(contratoParteAssinatura)) {
                        break;
                    }
                }
            }
        }

        if (!CommonsUtil.semValor(contratoParteAssinatura)) {
            ContratoDocumentoAssinaturaRequest contratoDocumentoAssinaturaRequest = new ContratoDocumentoAssinaturaRequest();
            contratoDocumentoAssinaturaRequest.setContrato(contrato);
            contratoDocumentoAssinaturaRequest.setContratoParte(contratoParteAssinatura);
            contratoRequest.getContrato().setDocumentos(contratoService.getDocumentosParaAssinar(contratoDocumentoAssinaturaRequest));
        } else {
            List<ContratoDocumentoResponse> contratoDocumento = new ArrayList<>(0);
            List<ContratoDocumento> documentos = contrato.getDocumentos();
            String arrayPrimeiroDoc = null;

            for (ContratoDocumento documento : documentos) {
                try {
                    if (arrayPrimeiroDoc == null) {
                        if (StatusContratoEnum.ASSINADO.equals(contrato.getStatusContrato()) && documento.getDocumentoAssinadoSHA256() != null) {
                            ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(documento), documento.getDocumentoAssinadoSHA256(), false, false);

                            if (!CommonsUtil.semValor(contratoDocumentoVisualizaResponse))
                                arrayPrimeiroDoc = contratoDocumentoVisualizaResponse.getDocumentoPDF();
                            else {
                                arrayPrimeiroDoc = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documento.getDocumentoAssinado()));
                            }
                        } else {
                            if (documento.getDocumentoOriginalSHA256() != null) {
                                ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(documento), documento.getDocumentoOriginalSHA256(), false, true);
                                if (!CommonsUtil.semValor(contratoDocumentoVisualizaResponse))
                                    arrayPrimeiroDoc = contratoDocumentoVisualizaResponse.getDocumentoPDF();
                                else {
                                    arrayPrimeiroDoc = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documento.getDocumentoOriginal()));
                                }
                            }
                        }
                    }
                    contratoDocumento.add(new ContratoDocumentoResponse(documento));
                } catch (Exception e) {

                }
            }
            if (contratoDocumento.size() == 1) {
                //TODO: compactado
                contratoDocumento.get(0).setDocumento(arrayPrimeiroDoc);
            }
            contratoDocumento.sort((o1, o2) -> {
                int compare = Integer.compare(CommonsUtil.intValue(o1.getTipoDocumento().getOrdem()), CommonsUtil.intValue(o2.getTipoDocumento().getOrdem()));
                if (compare != 0) {
                    return compare;
                }

                compare = CommonsUtil.compare(o1.getNomeDocumento(), o2.getNomeDocumento());
                return compare;
            });


            contratoRequest.getContrato().setDocumentos(contratoDocumento);
        }

        List<ContratoLog> resultLog = contratoLogService.getLog(contrato, null);
        for (ContratoLog log : resultLog) {
            log.setLog(log.getLog().replace("\n", "<BR>"));
        }
        contratoRequest.setContratoLog(resultLog);
        contratoRequest.setId(contratoRequest.getContrato().getId());

        response.setData(contratoRequest);
        return ResponseEntity.ok(response);
    }

    private ContratoParte VerificaAssinaturaPendente(@RequestBody IdRequest idRequest, ContratoRequest contratoRequest,
                                                     ContratoParteResponse contratoParteResponse) {
        if ((StatusAssinaturaEnum.NAOASSINADO.equals(contratoParteResponse.getStatusAssinatura()) ||
                StatusAssinaturaEnum.ASSINADOPARCIAL.equals(contratoParteResponse.getStatusAssinatura())) &&
                CommonsUtil.mesmoValor(idRequest.getUsuario().getPessoa().getCpfCnpj(), contratoParteResponse.getCpfCnpj())) {

            Papel papelObesrvador = papelService.findByIdentificacaoAndCliente("OBSERVADOR", null);
            contratoRequest.setAssinarDocumento(!ehObseravdor(contratoParteResponse, papelObesrvador));
            return new ContratoParte(contratoParteResponse);

        }
        return null;
    }

    private boolean ehObseravdor(ContratoParteResponse parte, Papel observador) {
        for (ContratoPartePapel papel : parte.getPapel()) {
            if (observador != null
                    && papel != null
                    && papel.getPapel() != null
                    && CommonsUtil.mesmoValor(papel.getPapel().getId(), observador.getId())) {
                return true;
            }
        }
        return false;
    }

    @PostMapping(value = "partesUsuario")
    public ResponseEntity<Response<List<ContratoParteResponse>>> getPartesUsuario(@RequestBody ContratoParteAssinarRequest contratoParteAssinarRequest) {
        Response<List<ContratoParteResponse>> response = new Response<>();
        List<ContratoParteResponse> contratoParte = new ArrayList<>(0);

        List<ContratoParte> result = contratoService.getPartes(contratoParteAssinarRequest);
        for (ContratoParte contratoParte1 : result) {
            ContratoParteResponse contratoParteResponse = new ContratoParteResponse(contratoParte1);
            contratoParte.add(contratoParteResponse);
        }


        response.setData(contratoParte);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "documentos")
    public ResponseEntity<Response<List<ContratoDocumentoResponse>>> getDocumentos(@RequestBody ContratoDocumentoAssinaturaRequest contratoDocumentoAssinaturaRequest) {
        Response<List<ContratoDocumentoResponse>> response = new Response<>();
        List<ContratoDocumentoResponse> documentos = contratoService.getDocumentosParaAssinar(contratoDocumentoAssinaturaRequest);
        response.setData(documentos);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "documentoPdf")
    public ResponseEntity<Response<ContratoDocumentoVisualizaResponse>> getDocumentoPdf(@RequestBody ContratoDocumentoRequest documento) {
        Response<ContratoDocumentoVisualizaResponse> response = new Response<>();
        ContratoDocumentoVisualizaResponse documentoPDF;
        if (StatusDocumentoEnum.ASSINADO.equals(documento.getStatusDocumento())
                && CommonsUtil.booleanValue(documento.getTipoDocumento().getAssina())) {
            documentoPDF = contratoService.getDocumentoAssiandoPDF(documento);
            if (documentoPDF == null) {
                documentoPDF = contratoService.getDocumentoPDF(documento);
            }
        } else {
            documentoPDF = contratoService.getDocumentoPDF(documento);
        }


        response.setData(documentoPDF);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "documentosPdf")
    public ResponseEntity<Response<ContratoDocumentoVisualizaResponse>> getDocumentosPdf(@RequestBody List<ContratoDocumentoRequest> documentos) {
        Response<ContratoDocumentoVisualizaResponse> response = new Response<>();
        ContratoDocumentoVisualizaResponse documentoPDF;

        List<byte[]> pdfsToMerge = new ArrayList<>();
        for (ContratoDocumentoRequest documento : documentos) {
            if ("CSVDUPLICATAS".equals(documento.getTipoDocumento().getIdentificacao()))
                continue;

            if (StatusDocumentoEnum.ASSINADO.equals(documento.getStatusDocumento()) && CommonsUtil.booleanValue(documento.getTipoDocumento().getAssina())) {
                pdfsToMerge.add(contratoService.getDocumentoAssiandoOnlyPDF(documento).getDocumentoBytes());
            } else {
                pdfsToMerge.add(contratoService.getDocumentoOriginalOnlyPDF(documento).getDocumentoBytes());
            }
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PDDocument destination = new PDDocument();

            for (byte[] pdf : pdfsToMerge) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(pdf);
                PDDocument source = null;
                try {
                    source = PDDocument.load(inputStream);
                } catch (Exception e) {
                    continue;
                }

//                if (destination == null && source != null) {
//                    destination = new PDDocument(source.getDocument());
//                } else {
                for (int iPageAssinatura = 0; iPageAssinatura < source.getPages().getCount(); iPageAssinatura++) {
                    destination.addPage(source.getPage(iPageAssinatura));
                }
//                }
            }
            destination.save(outputStream);
            //destination.close();
            documentoPDF = new ContratoDocumentoVisualizaResponse((Optional<ContratoDocumento>) null, outputStream.toByteArray(), false);
            //destination.close();
            response.setData(documentoPDF);
        } catch (IOException e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "enviaCodigo")
    public ResponseEntity<Response<AssinaTokenResponse>> EnviaCodigo(@RequestBody TokenSolicitacaoRequest TokenSolicitacaoRequest) {
        Response<AssinaTokenResponse> response = new Response<>();
        try {

            AssinaTokenResponse assinaTokenResponse = new AssinaTokenResponse();

            Usuario usuario = usuarioService.findById(TokenSolicitacaoRequest.getUsuario().getId());
            Contrato contrato = TokenSolicitacaoRequest.getContrato();

            if (CommonsUtil.semValor(usuario.getTokenAssinatura()) || CommonsUtil.semValor(usuario.getValidadeTokenAssinatura()) ||
                    DateUtil2.isAfterDateMinutes(usuario.getValidadeTokenAssinatura(), DataUtil.getCarimboTempo().getDataCarimboTempo())) {
                usuario.setTokenAssinatura(Util.GenerateCommonLangPassword(false, true));
                usuario.setValidadeTokenAssinatura(DateUtil2.adicionarPeriodo(DataUtil.getCarimboTempo().getDataCarimboTempo(), 15, Calendar.MINUTE));
                usuarioService.save(usuario);
            }

            TokenSolicitacaoRequest.getUsuario().setTokenAssinatura(usuario.getTokenAssinatura());
            TokenSolicitacaoRequest.getUsuario().setValidadeTokenAssinatura(usuario.getValidadeTokenAssinatura());
            assinaTokenResponse.setTokenAssinatura(usuario.getTokenAssinatura());
            assinaTokenResponse.setValidadeTokenAssinatura(usuario.getValidadeTokenAssinatura());

            contratoLogService.salva(new ContratoLog(contrato, null, "Solicitação do token para " +
                    TokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + TokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true));

            String mensagem = "";
            if (CommonsUtil.mesmoValor(TokenSolicitacaoRequest.getTipoEnvio(), "EMAIL")) {
                Runnable runnable = new EmailTask.EnviaTokenEmail(TokenSolicitacaoRequest);
                Thread thread = new Thread(runnable);
                thread.start();
                mensagem = "Código enviado para o e-mail: " + TokenSolicitacaoRequest.getDestino();
            } else if (CommonsUtil.mesmoValor(TokenSolicitacaoRequest.getTipoEnvio(), "SMS")) {
                Runnable runnable = new CelularTask.EnviaTokenSMS(TokenSolicitacaoRequest);
                Thread thread = new Thread(runnable);
                thread.start();
                mensagem = "Código enviado para o celular: " + TokenSolicitacaoRequest.getDestino();
            } else if (CommonsUtil.mesmoValor(TokenSolicitacaoRequest.getTipoEnvio(), "WHATSAPP")) {
                Runnable runnable = new CelularTask.EnviaTokenWhatsApp(TokenSolicitacaoRequest);
                Thread thread = new Thread(runnable);
                thread.start();
                mensagem = "Código enviado para o celular: " + TokenSolicitacaoRequest.getDestino();
            }

            assinaTokenResponse.setMensagem(mensagem);

            response.setData(assinaTokenResponse);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "assinarViaCodigo")
    public ResponseEntity<Response<String>> AssinarViaCodigo(@RequestBody ContratoParteAssinaturaRequest contratoParteAssinatura) {
        Response<String> response = new Response<>();
        try {

            contratoParteAssinatura.setIpCliente(retornaIpCliente(request));

            Boolean assinaEmLote = false;
            String result = contratoService.AssinarViaCodigo(contratoParteAssinatura, assinaEmLote);
            response.setData(result);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "assinarViaCodigoLote")
    public ResponseEntity<Response<String>> AssinarViaCodigoLote(@RequestBody ContratoParteAssinaturaRequest contratoParteAssinatura) {
        Response<String> response = new Response<>();
        try {
            contratoParteAssinatura.setIpCliente(retornaIpCliente(request));
            String result = contratoService.AssinarViaCodigoLote(contratoParteAssinatura);
            response.setData(result);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "download")
    public ResponseEntity<Response<ArquivoDownloadModel>> Download(@RequestBody Contrato contrato) throws IOException {

        Response<ArquivoDownloadModel> response = new Response<>();
        try {
            ArquivoDownloadModel arquivoDownloadModel = contratoService.getDownload(contrato);
            response.setData(arquivoDownloadModel);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "log")
    public ResponseEntity<Response<List<ContratoLog>>> getLog(@RequestBody ContratoDocumentoLogRequest contratoDocumentoLog) throws IOException {

        Response<List<ContratoLog>> response = new Response<>();
        try {
            List<ContratoLog> result = contratoLogService.getLog(contratoDocumentoLog.getContrato(), contratoDocumentoLog.getDocumento());

            for (ContratoLog log : result) {
                log.setLog(log.getLog().replace("\n", "<BR>"));
            }
            response.setData(result);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "contratoParteAssinaturaRequest")
    public ResponseEntity<Response<ContratoParteAssinaturaRequest>> ConfiguraAssinadorReques(@RequestBody ContratoParteAssinaturaRequest contratoParteAssinatura) {
        Response<ContratoParteAssinaturaRequest> response = new Response<>();

        try {
            ContratoParte contratoParteRequest = contratoParteAssinatura.getContratoParte();

            ContratoParteAssinaturaRequest contratoParteAssinaturaRequest = new ContratoParteAssinaturaRequest();

            contratoParteAssinaturaRequest.setIpCliente(retornaIpCliente(request));
            contratoParteAssinaturaRequest.setServerPort(request.getServerPort());
            contratoParteAssinaturaRequest.setServidor(request.getServerName());
            contratoParteAssinaturaRequest.setServidorSeguro(retornaIsSecure(request));
            contratoParteAssinaturaRequest.setProtocolo(retornaProtocolo(request));

            response.setData(contratoParteAssinaturaRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "salvarPartesAlteradas")
    public ResponseEntity<Response<String>> SalvarPartesAlteradas(@RequestBody List<ContratoParte> contratoPartes) {
        Response<String> response = new Response<>();
        try {
            contratoPartes.forEach(contratoParte -> {
                contratoParte.getPapel().forEach(contratoPartePapel -> {
                    contratoPartePapel.setContratoParte(contratoParte);
                });
                contratoParte.getContatos().forEach(contratoParte1 -> {
                    contratoParte1.getPapel().forEach(contratoPartePapel -> {
                        contratoPartePapel.setContratoParte(contratoParte1);
                    });
                });
            });
            contratoParteService.SaveAll(contratoPartes);
            response.setData("Partes alteradas com sucesso !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}

