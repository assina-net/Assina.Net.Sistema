package br.com.assinanet.task;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.*;
import br.com.assinanet.models.ArquivoDownloadModel;
import br.com.assinanet.repository.ContratoParteRepository;
import br.com.assinanet.repository.EmailFilaRepository;
import br.com.assinanet.request.TokenSolicitacaoRequest;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.response.ContratoResponse;
import br.com.assinanet.service.*;
import br.com.assinanet.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/02/2018 - 08:15
 */
@Service
@Slf4j
public class EmailTask {

    protected static EmailService emailService;
    protected static CelularService celularService;
    protected static ContratoService contratoService;
    protected static ContratoLogService contratoLogService;
    protected static SistemaAtributoService sistemaAtributoService;
    protected static MensagemService mensagemService;

    protected static ContratoParteRepository contratoParteRepository;
    protected static EmailFilaRepository emailFilaRepository;
    protected static ContratoParteService contratoParteService;
    protected static UsuarioService usuarioService;

    public EmailTask(EmailService emailService,
                     CelularService celularService,
                     ContratoService contratoService,
                     ContratoLogService contratoLogService,
                     ContratoParteRepository contratoParteRepository,
                     EmailFilaRepository emailFilaRepository,
                     SistemaAtributoService sistemaAtributoService,
                     MensagemService mensagemService,
                     ContratoParteService contratoParteService,
                     UsuarioService usuarioService) {
        EmailTask.emailService = emailService;
        EmailTask.celularService = celularService;
        EmailTask.contratoService = contratoService;
        EmailTask.contratoLogService = contratoLogService;
        EmailTask.contratoParteRepository = contratoParteRepository;
        EmailTask.emailFilaRepository = emailFilaRepository;
        EmailTask.sistemaAtributoService = sistemaAtributoService;
        EmailTask.mensagemService = mensagemService;
        EmailTask.contratoParteService = contratoParteService;
        EmailTask.usuarioService = usuarioService;
    }


    public static class EnviaTokenEmail implements Runnable {


        private final TokenSolicitacaoRequest tokenSolicitacaoRequest;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaTokenEmail(TokenSolicitacaoRequest tokenSolicitacaoRequest) {
            this.tokenSolicitacaoRequest = tokenSolicitacaoRequest;
        }

        @Transactional
        public void EnviaTokenAssinatura() {
            emailService.EnviaCodigoEmail(tokenSolicitacaoRequest);
        }

        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Task e-mail iniciada para envio de token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true));

            EnviaTokenAssinatura();
        }
    }

    public static class EnviaLiberacaoContratoAssinatura implements Runnable {


        private final Contrato contrato;
        private final List<ContratoParte> partes;
        private Boolean reenvio = false;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);

        public EnviaLiberacaoContratoAssinatura(ContratoResponse contrato, List<ContratoParteResponse> partes, Boolean reenvio) throws IOException {
            this.contrato = new Contrato(contrato);

            this.partes = new ArrayList<>(0);
            this.contrato.getPartes().forEach(contratoParte -> {
                if (TipoPessoaEnum.FISICA.equals(contratoParte.getTipoPessoa())) {
                    ContratoParteResponse findParte = partes.stream().filter(x -> x.getId().equals(contratoParte.getId())).findFirst().orElse(null);
                    if (findParte != null) {
                        this.partes.add(contratoParte);
                    }
                } else {
                    contratoParte.getContatos().forEach(contato -> {
                                ContratoParteResponse findParte = partes.stream().filter(x -> x.getId().equals(contato.getId())).findFirst().orElse(null);
                                if (findParte != null) {
                                    this.partes.add(contato);
                                }
                            }
                    );

                }
            });

            this.reenvio = reenvio;
        }

        public EnviaLiberacaoContratoAssinatura(Contrato contrato, List<ContratoParte> partes, Boolean reenvio) {
            this.contrato = contrato;
            this.partes = partes;
            this.reenvio = reenvio;
        }

        @Transactional
        public void enviaLiberacaoContratoAssinatura() {
            Map<String, Boolean> gerouChave = new HashMap<String, Boolean>(0);

            //orderna por cpfCnpj e duplicatas
            List<ContratoParte> lstContratoPartesFisicas = new ArrayList<>(0);

            partes.forEach(parte -> {
                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    lstContratoPartesFisicas.add(parte);
                } else {
                    parte.getContatos().forEach(contato -> {
                        lstContratoPartesFisicas.add(contato);
                    });
                }
            });

            Collections.sort(lstContratoPartesFisicas, new Comparator<ContratoParte>() {
                @Override
                public int compare(ContratoParte o1, ContratoParte o2) {
                    int result = CommonsUtil.compare(o1.getCpfCnpj(), o2.getCpfCnpj());

                    return result;
                }
            });


            contratoLogService.salva(new ContratoLog(contrato, null, "Gerando chaves de acesso. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            for (ContratoParte parte : lstContratoPartesFisicas) {

                if (StatusEnum.INATIVO.equals(parte.getStatus()))
                    continue;

                SimpleMailMessage msg = new SimpleMailMessage();
                if (!reenvio) {
                    msg.setSubject(contrato.getCustodiante().getPessoa().getNomeRazaoSocial() +
                            " solicitou a assinatura de : " + contrato.getAssunto());
                } else {
                    msg.setSubject("A assinatura de " + parte.getContrato().getAssunto() + " com a " +
                            parte.getContrato().getCustodiante().getPessoa().getNomeRazaoSocial() +
                            " continua pendente");
                }

                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    GeraChaveAcesso(msg, this.contrato, parte, gerouChave, SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO);
                    if (msg != null) {
                        this.menssagens.add(msg);
                    }
                } else {
                    List<ContratoParte> lstGerarChaveContato = parte.getContatos();
                    for (ContratoParte contato : lstGerarChaveContato) {
                        GeraChaveAcesso(msg, this.contrato, parte, gerouChave, SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO);
                        if (msg != null) {
                            this.menssagens.add(msg);
                        }
                    }
                }
            }

            contratoLogService.salva(new ContratoLog(contrato, null, "Chaves de acesso geradas. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            if (StatusContratoEnum.LIBERANDOASSINATURA.equals(contrato.getStatusContrato())) {
                contrato.setStatusContrato(StatusContratoEnum.LIBERADOASSINATURA);
                contrato.setLiberadoAssinatura(true);
                contratoService.Salva(contrato);
            }

            EnviaEmails();

            contratoLogService.salva(new ContratoLog(contrato, null, "Task de liberação da assinatura finalizada. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));
        }


        private void EnviaEmails() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Enviando e-email(" + menssagens.size() + "). Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            if (CommonsUtil.semValor(menssagens))
                return;


            for (SimpleMailMessage msg : menssagens) {
                ContratoLog contratoLogSucesso = null;
                ContratoLog contratoLogErro = null;

                if (!msg.getText().contains("ERRO:")) {
                    try {
                        contratoLogSucesso = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                                DataUtil.getCarimboTempo(), true);

                        contratoLogErro = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                                contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: ",
                                DataUtil.getCarimboTempo(), true);

                        emailService.sendEmailHtlm(msg, contratoLogSucesso);
                        contratoLogService.salva(contratoLogSucesso);

                    } catch (Exception e) {
                        //Salva fila para mandar via jog
                        EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJson(contratoLogSucesso), GsonUtil.toJson(contratoLogErro),
                                false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
                        emailFilaRepository.save(emailFila);

                        contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                        contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
                        contratoLogService.salva(contratoLogErro);
                    }
                }
            }

            contratoLogService.salva(new ContratoLog(contrato, null, "E-email enviados. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

        }


        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Task de liberação da assinatura iniciada. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));


            enviaLiberacaoContratoAssinatura();
        }
    }

    public static class EnviaLiberacaoContratoObservador implements Runnable {


        private final Contrato contrato;
        private final List<ContratoParte> partes;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);

        public EnviaLiberacaoContratoObservador(ContratoResponse contrato, List<ContratoParteResponse> partes) throws IOException {
            this.contrato = new Contrato(contrato);

            this.partes = new ArrayList<>(0);
            this.contrato.getPartes().forEach(contratoParte -> {
                if (TipoPessoaEnum.FISICA.equals(contratoParte.getTipoPessoa())) {
                    ContratoParteResponse findParte = partes.stream().filter(x -> x.getId().equals(contratoParte.getId())).findFirst().orElse(null);
                    if (findParte != null) {
                        this.partes.add(contratoParte);
                    }
                } else {
                    contratoParte.getContatos().forEach(contato -> {
                                ContratoParteResponse findParte = partes.stream().filter(x -> x.getId().equals(contato.getId())).findFirst().orElse(null);
                                if (findParte != null) {
                                    this.partes.add(contato);
                                }
                            }
                    );

                }
            });
        }

        public EnviaLiberacaoContratoObservador(Contrato contrato, List<ContratoParte> partes) {
            this.contrato = contrato;
            this.partes = partes;
        }

        @Transactional
        public void enviaLiberacaoContratoObservador() {
            Map<String, Boolean> gerouChave = new HashMap<String, Boolean>(0);

            //orderna por cpfCnpj e duplicatas
            List<ContratoParte> lstContratoPartesFisicas = new ArrayList<>(0);

            partes.forEach(parte -> {
                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    lstContratoPartesFisicas.add(parte);
                } else {
                    parte.getContatos().forEach(contato -> {
                        lstContratoPartesFisicas.add(contato);
                    });
                }
            });

            Collections.sort(lstContratoPartesFisicas, new Comparator<ContratoParte>() {
                @Override
                public int compare(ContratoParte o1, ContratoParte o2) {
                    int result = CommonsUtil.compare(o1.getCpfCnpj(), o2.getCpfCnpj());

                    return result;
                }
            });

//            lstContratoPartesFisicas.forEach(parte -> {
//                //if (!cnpjPreRequisitos.contains(parte.getCpfCnpj())) {
//                   // if (parte.getRequisitoAssinatura() == null) {
//                        lstGerarChave.add(parte);
//                   // } else {
//                   //     cnpjPreRequisitos.add(parte.getCpfCnpj());
//                   // }
//               // }
//            });


            contratoLogService.salva(new ContratoLog(contrato, null, "Gerando chaves de acesso. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            for (ContratoParte parte : lstContratoPartesFisicas) {

                if (StatusEnum.INATIVO.equals(parte.getStatus()))
                    continue;

                SimpleMailMessage msg = new SimpleMailMessage();

                msg.setSubject(contrato.getCustodiante().getPessoa().getNomeRazaoSocial() +
                        "  disponibilizou para acompanhamento : " + contrato.getAssunto());

                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    GeraChaveAcesso(msg, this.contrato, parte, gerouChave, SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO_OBSERVADOR);
                    if (msg != null) {
                        this.menssagens.add(msg);
                    }

                } else {
                    List<ContratoParte> lstGerarChaveContato = parte.getContatos();
                    for (ContratoParte contato : lstGerarChaveContato) {
                        GeraChaveAcesso(msg, this.contrato, contato, gerouChave, SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO_OBSERVADOR);
                        if (msg != null) {
                            this.menssagens.add(msg);
                        }
                    }
                }
            }

            contratoLogService.salva(new ContratoLog(contrato, null, "Chaves de acesso geradas. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            EnviaEmails();

            contratoLogService.salva(new ContratoLog(contrato, null, "Task de liberação da assinatura finalizada. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));
        }


        private void EnviaEmails() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Enviando e-email(" + menssagens.size() + "). Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            if (CommonsUtil.semValor(menssagens))
                return;


            for (SimpleMailMessage msg : menssagens) {
                ContratoLog contratoLogSucesso = null;
                ContratoLog contratoLogErro = null;

                try {
                    contratoLogSucesso = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                            contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                            DataUtil.getCarimboTempo(), true);

                    contratoLogErro = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                            contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: ",
                            DataUtil.getCarimboTempo(), true);

                    emailService.sendEmailHtlm(msg, contratoLogSucesso);
                    contratoLogService.salva(contratoLogSucesso);

                } catch (Exception e) {
                    //Salva fila para mandar via jog
                    EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJson(contratoLogSucesso), GsonUtil.toJson(contratoLogErro),
                            false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
                    emailFilaRepository.save(emailFila);

                    contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                    contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
                    contratoLogService.salva(contratoLogErro);
                }
            }

            contratoLogService.salva(new ContratoLog(contrato, null, "E-email enviados. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

        }


        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Task de liberação dos observadores iniciada. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));


            enviaLiberacaoContratoObservador();
        }
    }

    private static SimpleMailMessage GeraChaveAcesso(SimpleMailMessage msg, Contrato contrato, ContratoParte parte, Map<String, Boolean> gerouChave, SistemaTipoAtributoEnum templateEmail) {

        contratoLogService.salva(new ContratoLog(contrato, null, "Gerando chave de acesso e token  para " +
                parte.getNomeRazaoSocial(),
                DataUtil.getCarimboTempo(), true));


        if (gerouChave.containsKey(parte.getCpfCnpj())) {
            return null;
        }

        boolean alterou = false;
        if (CommonsUtil.semValor(parte.getTokenAssinatura())) {
            contratoLogService.salva(new ContratoLog(contrato, null, "Gerando token para " +
                    parte.getNomeRazaoSocial(),
                    DataUtil.getCarimboTempo(), true));

            alterou = true;
            parte.setTokenAssinatura(Util.GenerateCommonLangPassword(false, true));
            parte.setStatusAssinatura(StatusAssinaturaEnum.NAOASSINADO);
            parte.setLiberadoAssinatura(true);
            contratoLogService.salva(new ContratoLog(contrato, null, "Token gerado para " +
                    parte.getNomeRazaoSocial(),
                    DataUtil.getCarimboTempo(), true));
        }

        if (CommonsUtil.semValor(parte.getChaveAcesso()) || (parte.getValidadeChaveAcesso() != null && DateUtil2.isAfterDateMinutes(parte.getValidadeChaveAcesso(), DateUtil2.getDataHoraAgora()))) {
            ContratoParte parteAtualizada = contratoParteService.findById(parte.getId());
            if (CommonsUtil.semValor(parteAtualizada.getChaveAcesso()) || (parteAtualizada.getValidadeChaveAcesso() != null && DateUtil2.isAfterDateMinutes(parteAtualizada.getValidadeChaveAcesso(), DateUtil2.getDataHoraAgora()))) {

                contratoLogService.salva(new ContratoLog(contrato, null, "Gerando chave de acesso para " +
                        parte.getNomeRazaoSocial(),
                        DataUtil.getCarimboTempo(), true));

                alterou = true;
                String frase = parte.getId() + DataUtil.getCarimboTempo().getCarimboTempo();
                String strongAESKey = Util.ToHex(Util.gerarHash(frase, "SHA-256"));
                parte.setChaveAcesso(strongAESKey);
                parte.setValidadeChaveAcesso(null);
                usuarioService.desbloqueio(parte.getCpfCnpj());

                contratoLogService.salva(new ContratoLog(contrato, null, "Chave de acesso gerada para " +
                        parte.getNomeRazaoSocial(),
                        DataUtil.getCarimboTempo(), true));
            } else {
                parte.setChaveAcesso(parteAtualizada.getChaveAcesso());
                parte.setValidadeChaveAcesso(parteAtualizada.getValidadeChaveAcesso());
            }
        }


        gerouChave.put(parte.getCpfCnpj(), true);

        if (alterou) {

            contratoLogService.salva(new ContratoLog(contrato, null, "Salvando chave de acesso e token gerados para  " +
                    parte.getNomeRazaoSocial(),
                    DataUtil.getCarimboTempo(), true));

            contratoParteService.Save(parte);

            contratoLogService.salva(new ContratoLog(contrato, null, "Chave de acesso e token gerados e salvos para " +
                    parte.getNomeRazaoSocial(),
                    DataUtil.getCarimboTempo(), true));
        }

        contratoLogService.salva(new ContratoLog(contrato, null, "Criando e-mail para " +
                parte.getNomeRazaoSocial(),
                DataUtil.getCarimboTempo(), true));


        msg.setTo(parte.getEmail());

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("chaveAcesso", parte.getChaveAcesso());
        variables.put("parte", parte.getNomeRazaoSocial());
        variables.put("empresa", parte.getContrato().getCustodiante().getPessoa().getNomeRazaoSocial());
        variables.put("assunto", parte.getContrato().getAssunto());

        String urlSite = sistemaAtributoService.getString(SistemaTipoAtributoEnum.URL_ACESSO_SITE, null);
        if (urlSite != null) {
            variables.put("acessoURL", urlSite + "/#/validarChaveAcesso?token=" + parte.getChaveAcesso());

            msg.setText(mensagemService.criarEmailTemplate(templateEmail, variables));


            contratoLogService.salva(new ContratoLog(contrato, null, "E-mail gerado para " +
                    parte.getNomeRazaoSocial(),
                    DataUtil.getCarimboTempo(), true));
        } else {
            msg.setText("ERRO: URL ACESSO SITE não configurada! Favor comunicar o suporte");
        }

        return msg;
    }

    public static class EnviaContratoAssinado implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaContratoAssinado(Contrato contrato, List<ContratoParte> partes) {
            this.contrato = contrato;
            this.partes = partes;
        }

        @Transactional
        public void enviaContratoAssinado() {

            Boolean enviaArquivos = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.ENVIAR_ARQUIVOS_NO_EMAIL_CONTRATO_ASSINADOS, contrato.getCustodiante());
            Boolean enviaArquivosOriginais = !sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.NAO_ENVIAR_ARQUIVO_ORIGINAL_NO_EMAIL_CONTRATO_ASSINADOS, contrato.getCustodiante());


            List<ArquivoDownloadModel> arquivos = null;
            if (enviaArquivos) {
                arquivos = new ArrayList<>(0);
                ArquivoDownloadModel arquivoDownloadModel = contratoService.getDownload(contrato, enviaArquivosOriginais);
                arquivos.add(arquivoDownloadModel);
            }

            for (ContratoParte parte : partes) {

                emailService.EnviaEmailSolicitacaoVigente(contrato, parte, arquivos);

            }

            contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail finalizada para envio de contrato vigente. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

        }

        @Override
        public void run() {
            contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail iniciada para envio de contrato vigente. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            enviaContratoAssinado();
        }
    }

    public static class EnviaNovoUsario implements Runnable {


        private final Usuario usuario;
        private final Contrato contrato;
        private final String senha;


        public EnviaNovoUsario(Contrato contrato, Usuario usuario, String senha) {
            this.usuario = usuario;
            this.contrato = contrato;
            this.senha = senha;
        }

        @Transactional
        public void EnviaTokenAssinatura() {
            emailService.EnviaEmailNovoUsuario(contrato, usuario, senha);

            contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail finalizada para envio de usuario para " +
                    usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

        }

        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail iniciada para envio de usuario para " +
                    usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

            EnviaTokenAssinatura();
        }
    }


    public static class EnviaNovoCliente implements Runnable {


        private final Usuario usuario;
        private final Cliente cliente;
        private final String senha;


        public EnviaNovoCliente(Cliente cliente, Usuario usuario, String senha) {
            this.usuario = usuario;
            this.cliente = cliente;
            this.senha = senha;
        }

        @Transactional
        public void EnviaEmailNovoCliente() {
            emailService.EnviaEmailNovoClienteUsuario(cliente, usuario, senha);
        }

        @Override
        public void run() {
            EnviaEmailNovoCliente();
        }
    }

    public static class EnviaEmailFalha implements Runnable {


        private final EmailFila emailFila;
        private final SimpleMailMessage msg;
        private final ContratoLog contratoLogSucesso;
        private final ContratoLog contratoLogFalha;


        public EnviaEmailFalha(EmailFila emailFila) {
            this.emailFila = emailFila;
            msg = GsonUtil.fromJson(this.emailFila.getEmail(), SimpleMailMessage.class);
            contratoLogSucesso = GsonUtil.fromJson(this.emailFila.getLogSucesso(), ContratoLog.class);
            contratoLogFalha = GsonUtil.fromJson(this.emailFila.getLogFalha(), ContratoLog.class);
        }

        @Transactional
        public void EnviaEmail() {
            emailFila.setDataUltimoEnvio(DataUtil.getCarimboTempo().getDataCarimboTempo());
            try {
                emailService.sendEmailHtlm(msg, contratoLogSucesso);
                emailFila.setEnviado(true);
                contratoLogSucesso.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                contratoLogService.salva(contratoLogSucesso);

            } catch (Exception e) {
                contratoLogFalha.setLog(contratoLogFalha.getLog() + e.getMessage());
                contratoLogFalha.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                contratoLogService.salva(contratoLogFalha);
            }
            emailFilaRepository.save(emailFila);
        }

        @Override
        public void run() {
            EnviaEmail();
        }
    }

    public static class EnviaContratoRecusado implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaContratoRecusado(Contrato contrato, List<ContratoParte> partes) {
            this.contrato = contrato;
            this.partes = partes;
        }

        @Transactional
        public void enviaContratoRecusado() {


            ContratoParte contratoParteCancelamento = partes.stream().filter(p -> CommonsUtil.mesmoValor(p.getCpfCnpj(), contrato.getUsuarioRecusa().getPessoa().getCpfCnpj())).findAny().orElse(null);

            if (contratoParteCancelamento != null) {
                for (ContratoParte parte : partes) {
                    if (!CommonsUtil.semValor(parte.getContatos())) {
                        contratoParteCancelamento = parte.getContatos().stream().filter(p -> CommonsUtil.mesmoValor(p.getCpfCnpj(), contrato.getUsuarioRecusa().getPessoa().getCpfCnpj())).findAny().orElse(null);
                        if (contratoParteCancelamento != null) {
                            break;
                        }
                    }
                }

            }

            if (contratoParteCancelamento != null) {

                Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_RECUSA_WHATSAPP,
                        contrato, partes);

                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                    for (ContratoParte parte : (mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                        emailService.EnviaEmailContratoRecusado(contrato, parte, contratoParteCancelamento);
                    }
                }
                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                    for (ContratoParte parte : (mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                        celularService.EnviaEmailContratoRecusadoWhatsApp(contrato, parte, contratoParteCancelamento);
                    }
                }

                contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail finalizada para envio de contrato recusado. Identificador: " +
                        contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                        DataUtil.getCarimboTempo(), true));
            } else {
                contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail envio de contrato recusado sem parte cancelada. Identificador: " +
                        contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                        DataUtil.getCarimboTempo(), true));
            }

        }

        @Override
        public void run() {
            contratoLogService.salva(new ContratoLog(contrato, null, "Task e-mail iniciada para envio de contrato recusado. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            enviaContratoRecusado();
        }
    }

}