package br.com.assinanet.task;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoLog;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.EmailFila;
import br.com.assinanet.entity.enums.*;
import br.com.assinanet.repository.ContratoParteRepository;
import br.com.assinanet.repository.EmailFilaRepository;
import br.com.assinanet.request.TokenSolicitacaoRequest;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.response.ContratoResponse;
import br.com.assinanet.service.*;
import br.com.assinanet.util.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@Service
public class CelularTask {

    protected static ContratoLogService contratoLogService;
    protected static CelularService celularService;
    protected static ContratoService contratoService;
    protected static EmailFilaRepository emailFilaRepository;
    protected static ContratoParteRepository contratoParteRepository;
    protected static SistemaAtributoService sistemaAtributoService;
    protected static EmailService emailService;
    protected static MensagemService mensagemService;
    protected static ContratoParteService contratoParteService;
    protected static UsuarioService usuarioService;

    public CelularTask(ContratoLogService contratoLogService,
                       CelularService celularService,
                       ContratoService contratoService,
                       EmailFilaRepository emailFilaRepository,
                       ContratoParteRepository contratoParteRepository,
                       SistemaAtributoService sistemaAtributoService,
                       EmailService emailService,
                       MensagemService mensagemService,
                       ContratoParteService contratoParteService,
                       UsuarioService usuarioService) {

        CelularTask.contratoLogService = contratoLogService;
        CelularTask.celularService = celularService;
        CelularTask.contratoService = contratoService;
        CelularTask.emailFilaRepository = emailFilaRepository;
        CelularTask.contratoParteRepository = contratoParteRepository;
        CelularTask.sistemaAtributoService = sistemaAtributoService;
        CelularTask.emailService = emailService;
        CelularTask.mensagemService = mensagemService;
        CelularTask.contratoParteService = contratoParteService;
        CelularTask.usuarioService = usuarioService;
    }

    public static class EnviaTokenSMS implements Runnable {


        private final TokenSolicitacaoRequest tokenSolicitacaoRequest;


        //private List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaTokenSMS(TokenSolicitacaoRequest tokenSolicitacaoRequest) {
            this.tokenSolicitacaoRequest = tokenSolicitacaoRequest;
        }

        @Transactional
        public void EnviaTokenAssinatura() throws Exception {
            celularService.enviaCodigoSMS(tokenSolicitacaoRequest);
        }


        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Task SMS iniciada para envio de token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true));

            try {
                EnviaTokenAssinatura();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class EnviaTokenWhatsApp implements Runnable {


        private final TokenSolicitacaoRequest tokenSolicitacaoRequest;


        //private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaTokenWhatsApp(TokenSolicitacaoRequest tokenSolicitacaoRequest) {
            this.tokenSolicitacaoRequest = tokenSolicitacaoRequest;
        }

        @Transactional
        public void EnviaTokenAssinatura() throws Exception {
            celularService.enviaCodigoWhatsApp(tokenSolicitacaoRequest);
        }


        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Task WhatsApp iniciada para envio de token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true));

            try {
                EnviaTokenAssinatura();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class EnviaLiberacaoContratoAssinaturaWhatsApp implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;
        private final Boolean reenvio;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);

        public EnviaLiberacaoContratoAssinaturaWhatsApp(ContratoResponse contrato, List<ContratoParteResponse> partes, Boolean reenvio) throws IOException {
            this.contrato = new Contrato(contrato);

            this.partes = new ArrayList<>(0);
            this.contrato.getPartes().forEach(contratoParte -> {
                if (TipoPessoaEnum.FISICA.equals(contratoParte.getTipoPessoa())) {
                    partes.stream().filter(x -> x.getId().equals(contratoParte.getId())).findFirst().ifPresent(findParte -> this.partes.add(contratoParte));
                } else {
                    contratoParte.getContatos().forEach(contato -> partes.stream().filter(x -> x.getId().equals(contato.getId())).findFirst().ifPresent(findParte -> this.partes.add(contato))
                    );

                }
            });

            this.reenvio = reenvio;
        }

        public EnviaLiberacaoContratoAssinaturaWhatsApp(Contrato contrato, List<ContratoParte> partes, Boolean reenvio) {
            this.contrato = contrato;
            this.partes = partes;
            this.reenvio = reenvio;
        }

        @Transactional
        private void enviaLiberacaoContratoAssinatura() {
            Map<String, Boolean> gerouChave = new HashMap<>(0);

            //orderna por cpfCnpj e duplicatas
            List<ContratoParte> lstContratoPartesFisicas = new ArrayList<>(0);

            partes.forEach(parte -> {
                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    lstContratoPartesFisicas.add(parte);
                } else {
                    lstContratoPartesFisicas.addAll(parte.getContatos());
                }
            });

            lstContratoPartesFisicas.sort((o1, o2) -> CommonsUtil.compare(o1.getCpfCnpj(), o2.getCpfCnpj()));

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


                String telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, parte.getContrato().getCustodiante());
                if (telefoneWhatsAPP == null)
                    telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, null);

                msg.setFrom(telefoneWhatsAPP);

                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    if (GeraChaveAcesso(msg, this.contrato, parte, gerouChave, SistemaTipoAtributoEnum.WHATSAPP_CHAVE_ACESSO)) {
                        menssagens.add(msg);
                    }
                } else {
                    List<ContratoParte> lstGerarChaveContato = parte.getContatos();
                    for (ContratoParte contato : lstGerarChaveContato) {
                        if (GeraChaveAcesso(msg, this.contrato, contato, gerouChave, SistemaTipoAtributoEnum.WHATSAPP_CHAVE_ACESSO)) {
                            menssagens.add(msg);
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

            EnviaWhatsApp();

            contratoLogService.salva(new ContratoLog(contrato, null, "Task de liberação da assinatura finalizada. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));
        }

        private void EnviaWhatsApp() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Enviando e-email(" + menssagens.size() + "). Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            if (CommonsUtil.semValor(menssagens))
                return;


            for (SimpleMailMessage msg : menssagens) {
                ContratoLog contratoLogSucesso = null;
                ContratoLog contratoLogErro = null;
                if (msg.getText() == null) {
                    contratoLogService.salva(new ContratoLog(contrato, null,
                            "WhatsApp ignorado por estar sem conteúdo. Identificador: "
                                    + contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                            DataUtil.getCarimboTempo(), true));
                    continue;
                }
                if (!msg.getText().contains("ERRO:")) {
                    try {
                        contratoLogSucesso = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                                contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                                DataUtil.getCarimboTempo(), true);

                        contratoLogErro = new ContratoLog(contrato, null, "Enviando e-email para " + msg.getTo()[0] + ". Identificador: " +
                                contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: ",
                                DataUtil.getCarimboTempo(), true);

                        celularService.sendWhatsApp(msg);
                        contratoLogService.salva(contratoLogSucesso);

                    } catch (Exception e) {
                        //Salva fila para mandar via jog
                        EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                                false, TipoEnvioMsgEnum.WHATSAPP, e.getMessage(), contratoLogSucesso.getDataLog());
                        emailFilaRepository.save(emailFila);

                        contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                        contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
                        contratoLogService.salva(contratoLogErro);
                    }
                } else {
                    contratoLogService.salva(new ContratoLog(contrato, null,
                            "WhatsApp não enviado: " + msg.getText() + " Identificador: "
                                    + contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                            DataUtil.getCarimboTempo(), true));
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

    public static class EnviaLiberacaoContratoObservadorWhatsApp implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);

        public EnviaLiberacaoContratoObservadorWhatsApp(Contrato contrato, List<ContratoParte> partes) {
            this.contrato = contrato;
            this.partes = partes;
        }

        @Transactional
        public void enviaLiberacaoContratoObservador() {
            Map<String, Boolean> gerouChave = new HashMap<>(0);

            //orderna por cpfCnpj e duplicatas
            List<ContratoParte> lstContratoPartesFisicas = new ArrayList<>(0);

            partes.forEach(parte -> {
                if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                    lstContratoPartesFisicas.add(parte);
                } else {
                    lstContratoPartesFisicas.addAll(parte.getContatos());
                }
            });

            lstContratoPartesFisicas.sort((o1, o2) -> CommonsUtil.compare(o1.getCpfCnpj(), o2.getCpfCnpj()));

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
                    if (GeraChaveAcesso(msg, this.contrato, parte, gerouChave, SistemaTipoAtributoEnum.WHATSAPP_CHAVE_ACESSO_OBSERVADOR)) {
                        menssagens.add(msg);
                    }
                } else {
                    List<ContratoParte> lstGerarChaveContato = parte.getContatos();
                    for (ContratoParte contato : lstGerarChaveContato) {
                        if (GeraChaveAcesso(msg, this.contrato, contato, gerouChave, SistemaTipoAtributoEnum.WHATSAPP_CHAVE_ACESSO_OBSERVADOR)) {
                            menssagens.add(msg);
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

            contratoLogService.salva(new ContratoLog(contrato, null, "Enviando whatsapp(" + menssagens.size() + "). Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

            if (CommonsUtil.semValor(menssagens))
                return;


            for (SimpleMailMessage msg : menssagens) {
                ContratoLog contratoLogSucesso = null;
                ContratoLog contratoLogErro = null;

                try {
                    contratoLogSucesso = new ContratoLog(contrato, null, "Enviando whatsapp para " + msg.getTo()[0] + ". Identificador: " +
                            contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                            DataUtil.getCarimboTempo(), true);

                    contratoLogErro = new ContratoLog(contrato, null, "Enviando whatsapp para " + msg.getTo()[0] + ". Identificador: " +
                            contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: ",
                            DataUtil.getCarimboTempo(), true);

                    celularService.sendWhatsApp(msg);
                    contratoLogService.salva(contratoLogSucesso);

                } catch (Exception e) {
                    //Salva fila para mandar via jog
                    EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
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

    public static class EnviaContratoAssinadoWhatsApp implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;

        //private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public EnviaContratoAssinadoWhatsApp(Contrato contrato, List<ContratoParte> partes) {
            this.contrato = contrato;
            this.partes = partes;
        }

        @Transactional
        public void enviaContratoAssinado() {

//            Boolean enviaArquivos = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.ENVIAR_ARQUIVOS_NO_EMAIL_CONTRATO_ASSINADOS, contrato.getCustodiante());
//            Boolean enviaArquivosOriginais = sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.NAO_ENVIAR_ARQUIVO_ORIGINAL_NO_EMAIL_CONTRATO_ASSINADOS, contrato.getCustodiante());

//            List<ArquivoDownloadModel> arquivos = null;
//            if (enviaArquivos) {
//                arquivos = new ArrayList<>(0);
//                ArquivoDownloadModel arquivoDownloadModel = contratoService.getDownload(contrato, enviaArquivosOriginais);
//                arquivos.add(arquivoDownloadModel);
//            }

            for (ContratoParte parte : partes) {

                celularService.enviaEmailSolicitacaoVigenteWhatsApp(contrato, parte, null);

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

    private static boolean GeraChaveAcesso(SimpleMailMessage msg, Contrato contrato, ContratoParte parte, Map<String, Boolean> gerouChave, SistemaTipoAtributoEnum templateEmail) {

        contratoLogService.salva(new ContratoLog(contrato, null, "Gerando chave de acesso e token  para " +
                parte.getNomeRazaoSocial(),
                DataUtil.getCarimboTempo(), true));


        if (gerouChave.containsKey(parte.getCpfCnpj())) {
            return false;
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

        if (CommonsUtil.semValor(parte.getChaveAcesso()) || ( parte.getValidadeChaveAcesso() != null && DateUtil2.isAfterDateMinutes(parte.getValidadeChaveAcesso(), DateUtil2.getDataHoraAgora()))) {

            ContratoParte parteAtualizada = contratoParteService.findById(parte.getId());
            if (CommonsUtil.semValor(parteAtualizada.getChaveAcesso()) ||( parteAtualizada.getValidadeChaveAcesso() != null && DateUtil2.isAfterDateMinutes(parteAtualizada.getValidadeChaveAcesso(), DateUtil2.getDataHoraAgora()))) {

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


        msg.setTo(parte.getCelular());

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

        return true;
    }

}
