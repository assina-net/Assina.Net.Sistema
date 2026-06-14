package br.com.assinanet.service;


import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.TipoEnvioMsgEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.models.ArquivoDownloadModel;
import br.com.assinanet.repository.ContratoParteRepository;
import br.com.assinanet.repository.UsuarioRepository;
import br.com.assinanet.request.TokenSolicitacaoRequest;
import br.com.assinanet.task.CelularTask;
import br.com.assinanet.task.EmailTask;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import br.com.assinanet.util.DateUtil2;
import br.com.assinanet.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmailService {


    private final static String TEMPLATE_LOCAL = "pt-BR";
    protected static ContratoLogService contratoLogService;
    //private final ClienteService clienteService;
    private final EmailFilaService emailFilaService;
    private final ContratoParteRepository contratoParteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SistemaAtributoService sistemaAtributoService;
    private final SistemaTipoAtributoService sistemaTipoAtributoService;
    private final MensagemService mensagemService;

    private final JavaMailSender javaMailSender;
    private TemplateEngine templateEngine;

    @Autowired
    private Environment env;

    public EmailService(ContratoLogService contratoLogService,
                        //ClienteService clienteService,
                        ContratoParteRepository contratoParteRepository,
                        EmailFilaService emailFilaService,
                        UsuarioRepository usuarioRepository, SistemaAtributoService sistemaAtributoService,
                        SistemaTipoAtributoService sistemaTipoAtributoService, MensagemService mensagemService) {
        EmailService.contratoLogService = contratoLogService;
        //this.clienteService = clienteService;
        this.emailFilaService = emailFilaService;
        this.contratoParteRepository = contratoParteRepository;
        this.usuarioRepository = usuarioRepository;
        this.sistemaAtributoService = sistemaAtributoService;
        this.sistemaTipoAtributoService = sistemaTipoAtributoService;
        this.mensagemService = mensagemService;
        this.javaMailSender = getJavaMailSender();
    }

    public void sendEmailHtlm(SimpleMailMessage msg, ContratoLog contratoLog) throws MessagingException, IOException {

        if (CommonsUtil.semValor(msg.getText()))
            return;

        MimeMessage mail = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

        Cliente cliente = null;
        try {
            cliente = contratoLog.getContrato().getCustodiante();
        } catch (Exception e) {
        }

        //Verifica se o cliente possui configuracao de SMTP para envio de emails sob sua responsabilidade
        String from = sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_USERNAME, cliente);
        //se o from do cliente retornou null, busca a configuracao do sistema
        if (from == null) {
            from = sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_USERNAME, null);
        }
        if (from != null) {
            helper.setFrom(from, "Portal Assina.Net");
            helper.addBcc("sistema@assina.net");
            helper.addBcc("bonatte@assina.net");


            helper.setTo(msg.getTo());
            helper.setSubject(msg.getSubject());
            helper.setText(msg.getText(), true);

            javaMailSender.send(mail);
        } else {
            msg.setText("ERRO: Serviço de envio de e-mail não configurado!");
        }

    }

    public void sendEmailHtlm(SimpleMailMessage msg, List<ArquivoDownloadModel> arquivos) throws MessagingException, IOException {

        MimeMessage mail = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");


        String from = sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_USERNAME, null);
        if (from != null) {
            helper.setFrom(from);
            helper.addBcc("sistema@assina.net");
            helper.addBcc("bonatte@assina.net");


            helper.setTo(msg.getTo());
            helper.setSubject(msg.getSubject());
            helper.setText(msg.getText(), true);


            if (arquivos != null) {
                for (ArquivoDownloadModel arquivo : arquivos) {
                    final InputStreamSource attachmentSource = new ByteArrayResource(arquivo.getArquivoByte());
                    helper.addAttachment(arquivo.getArquivoNome(), attachmentSource);
                }
            }

            javaMailSender.send(mail);
        } else {
            msg.setText("ERRO: Serviço de envio de e-mail não configurado!");
        }

    }

    private JavaMailSender getJavaMailSender() {

        Cliente cliente = null;

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        try {
            mailSender.setHost(sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_HOST, cliente));
            mailSender.setPort(sistemaAtributoService.getInteger(SistemaTipoAtributoEnum.EMAIL_SENDER_PORT, cliente));

            mailSender.setUsername(sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_USERNAME, cliente));
            mailSender.setPassword(sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_PASSWORD, cliente));

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_AUTHENTICATE, cliente));
            props.put("mail.smtp.starttls.enable", sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_TLS_ENABLE, cliente));
            props.put("mail.smtp.starttls.required", sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_TLS_REQUIRED, cliente));
            props.put("mail.smtp.ssl.enable", sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_SSL_ENABLE, cliente));
            props.put("mail.transport.protocol", sistemaAtributoService.getString(SistemaTipoAtributoEnum.EMAIL_SENDER_PROTOCOL, cliente));
        } catch (Exception e) {
        }
        return mailSender;
    }

    public void sendEmailText(SimpleMailMessage msg) {

        javaMailSender.send(msg);

    }

    void sendEmailWithAttachment() throws MessagingException, IOException {

        MimeMessage msg = javaMailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo("to_@email");

        helper.setSubject("Testing from Spring Boot");

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText("<h1>Check attachment for image!</h1>", true);

        // hard coded a file path
        //FileSystemResource file = new FileSystemResource(new File("path/android.png"));

        helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

        javaMailSender.send(msg);

    }

    public void EnviaCodigoEmail(TokenSolicitacaoRequest tokenSolicitacaoRequest) {

        contratoLogService.salva(new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Montagem do e-mail do token para " +
                tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                DataUtil.getCarimboTempo(), true));

        // ContratoParte contratoParteEmail = contratoParteRepository.getOne(contratoParte.getId());

        SimpleMailMessage msg = new SimpleMailMessage();

        if (tokenSolicitacaoRequest.getContrato() != null) {
            Contrato contrato = tokenSolicitacaoRequest.getContrato();
            msg.setSubject("Código de assinatura de " + contrato.getAssunto() + ": " + tokenSolicitacaoRequest.getUsuario().getTokenAssinatura());
            msg.setTo(tokenSolicitacaoRequest.getDestino());
            Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
            variables.put("assunto", contrato.getAssunto());
            variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
            variables.put("token", tokenSolicitacaoRequest.getUsuario().getTokenAssinatura());


            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
            dateFormat.setTimeZone(TimeZone.getDefault());
            variables.put("tokenValidade", dateFormat.format(tokenSolicitacaoRequest.getUsuario().getValidadeTokenAssinatura()));

            variables.put("parte", tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial());
            msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_TOKEN_ASSINATURA, variables));
        }

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogService.salva(new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Enviando e-mail do token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true));


            contratoLogSucesso = new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "E-mail enviado com o token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(tokenSolicitacaoRequest.getContrato(), null, "Erro enviando e-mail com o token para " +
                    tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial() + " e-mail: " + tokenSolicitacaoRequest.getDestino() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendEmailHtlm(msg, contratoLogSucesso);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
        //return "Código enviado para o e-mail: " + contratoParte.getEmail();

    }

    public void EnviaEmailNovoUsuario(Contrato contrato, Usuario usuario, String senha) {

        contratoLogService.salva(new ContratoLog(contrato, null, "Enviando e-mail de novo usuário enviado para " +
                usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail(),
                DataUtil.getCarimboTempo(), true));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setSubject("Usuário Assina.net");
        msg.setTo(usuario.getPessoa().getEmail());

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("usuario", usuario.getLogin());
        variables.put("senha", senha);
        variables.put("nome", usuario.getPessoa().getNomeRazaoSocial());
        //TODO: remover apos atualizar template
        variables.put("parte", usuario.getPessoa().getNomeRazaoSocial());

        msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_NOVO_USUARIO, variables));

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogSucesso = new ContratoLog(contrato, null, "E-mail de novo usuário enviado para " +
                    usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contrato, null, "Erro enviando e-mail de novo usuário para " +
                    usuario.getPessoa().getNomeRazaoSocial() + " e-mail: " + usuario.getPessoa().getEmail() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendEmailHtlm(msg, contratoLogSucesso);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);
        }
    }

    public void EnviaEmailNovoClienteUsuario(Cliente cliente, Usuario usuario, String senha) {


        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setSubject("Usuário Assina.net");
        msg.setTo(usuario.getPessoa().getEmail());
        //Bonatte - 2023-11-30
        msg.setCc("bonatte@assina.net");

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("nome", usuario.getPessoa().getNomeRazaoSocial());
        variables.put("usuario", usuario.getLogin());
        SistemaTipoAtributoEnum template = null;
        if (!CommonsUtil.semValor(senha)) {
            variables.put("senha", senha);
            template = SistemaTipoAtributoEnum.EMAIL_NOVO_USUARIO_CLIENTE_FISICA;
            if (TipoPessoaEnum.JURIDICA.equals(cliente.getPessoa().getTipoPessoa())) {
                variables.put("cliente", cliente.getPessoa().getNomeRazaoSocial());
                template = SistemaTipoAtributoEnum.EMAIL_NOVO_USUARIO_CLIENTE_JURIDICA;
            }
        } else {
            template = SistemaTipoAtributoEnum.EMAIL_NOVO_CLIENTE_FISICA;
            if (TipoPessoaEnum.JURIDICA.equals(cliente.getPessoa().getTipoPessoa())) {
                variables.put("cliente", cliente.getPessoa().getNomeRazaoSocial());
                template = SistemaTipoAtributoEnum.EMAIL_NOVO_CLIENTE_JURIDICA;
            }
        }
        msg.setText(mensagemService.criarEmailTemplate(template, variables));

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {
            sendEmailHtlm(msg, contratoLogSucesso);
        } catch (Exception e) {

        }
    }


    public void EnviaRecuperarSenha(Usuario usuario) throws IOException, MessagingException {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setSubject("Usuário Assina.net - Recuperação de senha");
        msg.setTo(usuario.getPessoa().getEmail());
        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();

        String urlSite = sistemaAtributoService.getString(SistemaTipoAtributoEnum.URL_ACESSO_SITE, null);
        variables.put("acessoURL", urlSite + "/#/recuperarSenha?token=" + usuario.getChaveEsqueceuSenha());

        variables.put("nome", usuario.getPessoa().getNomeRazaoSocial());

        msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_ESQUECEU_SENHA, variables));

        sendEmailHtlm(msg, (ContratoLog) null);

    }

    public void EnviaEmailSolicitacaoVigente(Contrato contrato, ContratoParte contratoParte, List<ArquivoDownloadModel> arquivos) {

        contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando e-mail do contrato vigente para " +
                contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail(),
                DataUtil.getCarimboTempo(), true));

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setSubject("A solicitação feita por " + contrato.getCustodiante().getPessoa().getNomeRazaoSocial() +
                " está vigente");

        msg.setTo(contratoParte.getEmail());

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("assunto", contrato.getAssunto());
        variables.put("identificador", contrato.getIdentificador());
        variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
        variables.put("parte", contratoParte.getNomeRazaoSocial());

        String urlSite = sistemaAtributoService.getString(SistemaTipoAtributoEnum.URL_ACESSO_SITE, null);
        variables.put("acessoURL", urlSite + "/#/validarChaveAcesso?token=" + contratoParte.getChaveAcesso());

        //TODO: remover apos atualizar template
        variables.put("contratoParte", contratoParte.getNomeRazaoSocial());
        variables.put("siteURL", urlSite + "/#/validarChaveAcesso?token=" + contratoParte.getChaveAcesso());


        msg.setText(mensagemService.criarEmailTemplate(contrato.getCustodiante(), SistemaTipoAtributoEnum.EMAIL_CONTRATO_ASSINADO, variables));

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-mail do contrato vigente enviado para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contrato, null, "Erro enviando e-mail do contrato vigente enviado  para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendEmailHtlm(msg, arquivos);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
    }

    public void ReenviarEmailAssinaturaDisponivel() {

        if (!CommonsUtil.booleanValue(env.getProperty("JobEnviarEmailAssinaturaPendente"))) {
            System.out.println("JobEnviarEmailAssinaturaPendente - ReenviarEmailAssinaturaDisponivel - Desabilitado");
            return;
        }

        Integer weekDay = DateUtil2.getDiaDaSemana(DateUtil2.getDataHoje());
        String hora = DateUtil2.getInicioHoraMinuto(DateUtil2.getDataHoraAgora());

        SistemaTipoAtributo atributoDiasAlerta = sistemaTipoAtributoService.findByTipoAtributo(SistemaTipoAtributoEnum.DIAS_DA_SEMANA_PARA_ENVIO_DE_ALERTA);
        SistemaTipoAtributo atributoHorariosAlerta = sistemaTipoAtributoService.findByTipoAtributo(SistemaTipoAtributoEnum.HORARIOS_PARA_ENVIO_DE_ALERTA);

        List<Cliente> clientes = sistemaAtributoService.retornaClientescomParametrosDeAlerta(atributoDiasAlerta, weekDay.toString(),
                atributoHorariosAlerta, hora);

        if (clientes.stream().filter(c -> "SISTEMA".equals(c.getSegmento().getIdentificacao())).findAny().orElse(null) != null) {
            clientes.addAll(sistemaAtributoService.retornaClientesSemParametroDeAlerta(atributoDiasAlerta, atributoHorariosAlerta));
        }

        if (CommonsUtil.semValor(clientes))
            return;

        Date dataHoje = DataUtil.getCarimboTempo().getDataCarimboTempo();
        List<ContratoParte> partes = contratoParteRepository.listaAssinaturaPendentes(dataHoje, clientes);

        if (CommonsUtil.semValor(partes))
            return;

        //orderna por Contrato
        Collections.sort(partes, Comparator.comparing(o -> o.getContrato().getId()));

        Map<String, Boolean> gerouChave = new HashMap<String, Boolean>(0);


        Contrato contratoAtual = partes.get(0).getContrato();
        List<ContratoParte> partesEmail = new ArrayList<>(0);

        for (ContratoParte parte : partes) {

            if (contratoAtual != parte.getContrato()) {

                Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_PENDENTE_WHATSAPP,
                        parte.getContrato(), partesEmail);

                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                    Runnable runnable = new EmailTask.EnviaLiberacaoContratoAssinatura(contratoAtual, mapEnvio.get(TipoEnvioMsgEnum.EMAIL), true);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }

                if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                    Runnable runnable = new CelularTask.EnviaLiberacaoContratoAssinaturaWhatsApp(contratoAtual, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP), true);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                partesEmail = new ArrayList<>(0);
                contratoAtual = parte.getContrato();
            }


            if (TipoPessoaEnum.FISICA.equals(parte.getTipoPessoa())) {
                partesEmail.add(parte);
                //GeraChaveAcesso(msg, parte, gerouChave);
            } else {
                for (ContratoParte contato : parte.getContatos()) {
                    partesEmail.add(contato);
                    //GeraChaveAcesso(msg, contato, gerouChave);
                }
            }
        }

        //roda o ultimo contrato
        Runnable runnable = new EmailTask.EnviaLiberacaoContratoAssinatura(contratoAtual, partesEmail, true); // or an anonymous class, or lambda...
        Thread thread = new Thread(runnable);
        thread.start();


    }

    public void EnviaEmailContratoRecusado(Contrato contrato, ContratoParte contratoParte, ContratoParte contratoParteCancelamento) {

        contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando e-mail do contrato vigente para " +
                contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail(),
                DataUtil.getCarimboTempo(), true));

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setSubject("Alguém recusou a assinatura de  " + contrato.getAssunto());

        msg.setTo(contratoParte.getEmail());

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("assunto", contrato.getAssunto());
        variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
        variables.put("parte", contratoParte.getNomeRazaoSocial());
        variables.put("usuarioCancelamento", contratoParteCancelamento.getNomeRazaoSocial());
        variables.put("motivoCancelamento", contrato.getMotivoRecusa());


        msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_RECUSA_ASSINATURA, variables));
        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-mail do contrato recusado enviado para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contrato, null, "Erro enviando e-mail do contrato recusado enviado  para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendEmailHtlm(msg, contratoLogSucesso);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
    }

    public void EnviarEmailsPendentesComErro() {

        if (!CommonsUtil.booleanValue(env.getProperty("JobEnviarEmailsPendentesComErro"))) {
            System.out.println("JobEnviarEmailsPendentesComErro - EnviarEmailsPendentesComErro - Desabilitado");
            return;
        }

        List<EmailFila> emails = emailFilaService.findByEnviadoOrderByDataUltimoEnvio(false);

        if (CommonsUtil.semValor(emails))
            return;

        for (EmailFila email : emails) {
            if (!TipoEnvioMsgEnum.EMAIL.equals(email.getTipoEnvio()))
                continue;

            Runnable runnable = null;
            try {
                runnable = new EmailTask.EnviaEmailFalha(email); // or an anonymous class, or lambda...
            } catch (Exception e) {
                continue;
            }
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void EnviaBloqueioChaveAcessoEmail(ContratoParte contratoParte) {

        contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Montagem do e-mail da revogação do token para " +
                contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                DataUtil.getCarimboTempo(), true));

        // ContratoParte contratoParteEmail = contratoParteRepository.getOne(contratoParte.getId());

        SimpleMailMessage msg = new SimpleMailMessage();

        if (contratoParte.getContrato() != null) {
            Contrato contrato = contratoParte.getContrato();
            msg.setSubject("Bloqueio chave de acesso de " + contrato.getAssunto() + ": " + contratoParte.getNomeRazaoSocial());
            msg.setTo(contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail());
            Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
            variables.put("assunto", contrato.getAssunto());
            variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
            variables.put("parte", contratoParte.getNomeRazaoSocial());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
            dateFormat.setTimeZone(TimeZone.getDefault());
            variables.put("dataBloqueio", dateFormat.format(contratoParte.getValidadeChaveAcesso()));
            msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO_BLOQUEIO, variables));
        }

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando do e-mail da revogação do token para " +
                    contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

            contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-mail da revogação do token enviado para " +
                    contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contratoParte.getContrato(), null, "Erro enviando e-mail da revogação do token para " +
                    contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true);

            sendEmailHtlm(msg, contratoLogSucesso);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
        //return "Código enviado para o e-mail: " + contratoParte.getEmail();

    }


    public void EnviaProblemaChaveAcessoEmail() {


        if (!CommonsUtil.booleanValue(env.getProperty("JobEnviarEmailsProblemaChaveAcesso"))) {
            System.out.println("JobEnviarEmailsProblemaChaveAcesso - EnviaProblemaChaveAcessoEmail - Desabilitado");
            return;
        }

        Date dataReferencia = DateUtil2.adicionarPeriodo(DateUtil2.getDataHoraAgora(), -30, Calendar.MINUTE);
        List<Usuario> usuarios = usuarioRepository.listaUsuariosProblemaAcesso(dataReferencia);

        if (CommonsUtil.semValor(usuarios))
            return;

        for (Usuario usuario : usuarios) {

            ContratoParte contratoParte = usuario.getContratoParteAcesso();

            contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Montagem do e-mail de problema de acesso para " +
                    contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                    DataUtil.getCarimboTempo(), true));

            // ContratoParte contratoParteEmail = contratoParteRepository.getOne(contratoParte.getId());

            SimpleMailMessage msg = new SimpleMailMessage();

            if (contratoParte.getContrato() != null) {
                Contrato contrato = contratoParte.getContrato();
                msg.setSubject("Problemas ao acessar " + contrato.getAssunto() + ": " + contratoParte.getNomeRazaoSocial());
                msg.setTo(contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail());
                Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
                variables.put("assunto", contrato.getAssunto());
                variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
                variables.put("parte", contratoParte.getNomeRazaoSocial());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
                dateFormat.setTimeZone(TimeZone.getDefault());
                variables.put("dataTentativa", dateFormat.format(usuario.getPrimeiraTentativaAcesso()));
                msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.EMAIL_CHAVE_ACESSO_PROBLEMA, variables));
            }

            ContratoLog contratoLogSucesso = null;
            ContratoLog contratoLogErro = null;
            try {

                contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando do e-mail de problema de acesso para " +
                        contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                        DataUtil.getCarimboTempo(), true));

                contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-mail de problema de acesso enviado para " +
                        contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                        DataUtil.getCarimboTempo(), true);

                contratoLogErro = new ContratoLog(contratoParte.getContrato(), null, "Erro enviando e-mail de problema de acesso para " +
                        contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getNomeRazaoSocial() + " e-mail: " + contratoParte.getContrato().getUsuarioSolicitacaoAssinatura().getPessoa().getEmail(),
                        DataUtil.getCarimboTempo(), true);

                sendEmailHtlm(msg, contratoLogSucesso);

                contratoLogService.salva(contratoLogSucesso);

            } catch (Exception e) {
                //Salva fila para mandar via jog
                EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJsonContratoLog(contratoLogSucesso), GsonUtil.toJsonContratoLog(contratoLogErro),
                        false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
                emailFilaService.save(emailFila);

                contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
                contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
                contratoLogService.salva(contratoLogErro);

            }

            usuario.setEnvioEmailTentativaAcesso(true);
            usuarioRepository.save(usuario);

        }
        //return "Código enviado para o e-mail: " + contratoParte.getEmail();

    }

}
