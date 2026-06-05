package br.com.assinanet.service;

import br.com.assinanet.Sms.ISms;
import br.com.assinanet.Sms.SmsComtele;
import br.com.assinanet.Sms.SmsConteudo;
import br.com.assinanet.Whastapp.Sms.WhatsAppApi;
import br.com.assinanet.Whastapp.Sms.WhatsAppConteudo;
import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.TipoEnvioMsgEnum;
import br.com.assinanet.models.ArquivoDownloadModel;
import br.com.assinanet.request.TokenSolicitacaoRequest;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import br.com.assinanet.util.GsonUtil;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CelularService {

    private final SistemaAtributoService sistemaAtributoService;
    private final SistemaTipoAtributoService sistemaTipoAtributoService;
    protected static ContratoLogService contratoLogService;
    private final EmailFilaService emailFilaService;
    private final MensagemService mensagemService;

    public CelularService(SistemaAtributoService sistemaAtributoService,
                          SistemaTipoAtributoService sistemaTipoAtributoService,
                          ContratoLogService contratoLogService, EmailFilaService emailFilaService, MensagemService mensagemService) {
        this.sistemaAtributoService = sistemaAtributoService;
        this.sistemaTipoAtributoService = sistemaTipoAtributoService;
        this.mensagemService = mensagemService;
        CelularService.contratoLogService = contratoLogService;
        this.emailFilaService = emailFilaService;
    }

    public void enviaCodigoSMS(TokenSolicitacaoRequest tokenSolicitacaoRequest) throws Exception {
        ISms sms = new SmsComtele();

        SmsConteudo smsConteudo = new SmsConteudo();
        String apiComtele = sistemaAtributoService.getString(SistemaTipoAtributoEnum.CHAVE_ACESSO_API_COMTELE, tokenSolicitacaoRequest.getContrato().getCustodiante());
        if (apiComtele == null)
            apiComtele = sistemaAtributoService.getString(SistemaTipoAtributoEnum.CHAVE_ACESSO_API_COMTELE, null);

        smsConteudo.setApi(apiComtele);
        smsConteudo.setRemetente("Assina.Net");
        String sConteudo = "Assina,Net: Código para assinatura:\n" + tokenSolicitacaoRequest.getUsuario().getTokenAssinatura()+"\n";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
        dateFormat.setTimeZone(TimeZone.getDefault());
        sConteudo += "    Valido até : " + dateFormat.format(tokenSolicitacaoRequest.getUsuario().getValidadeTokenAssinatura());
        smsConteudo.setConteudo(sConteudo);
        smsConteudo.setDestinatarios(CommonsUtil.somenteNumeros(tokenSolicitacaoRequest.getDestino()));
        try {
            sms.enviaSMS(smsConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enviaCodigoWhatsApp(TokenSolicitacaoRequest tokenSolicitacaoRequest) throws Exception {
        WhatsAppApi whatsAppApi = new WhatsAppApi();

        WhatsAppConteudo smsConteudo = new WhatsAppConteudo();


        String telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, tokenSolicitacaoRequest.getContrato().getCustodiante());
        if (telefoneWhatsAPP == null)
            telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, null);
        smsConteudo.setRemetente(telefoneWhatsAPP);

        String sURL  = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_URL_SERVICO, tokenSolicitacaoRequest.getContrato().getCustodiante());
        if (sURL == null)
            sURL = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_URL_SERVICO, null);
        smsConteudo.setApi(sURL);

        String sToken  = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_TOKEN_SERVICO, tokenSolicitacaoRequest.getContrato().getCustodiante());
        if (sToken == null)
            sToken = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_TOKEN_SERVICO, null);
        smsConteudo.setToken(sToken);


        Contrato contrato = tokenSolicitacaoRequest.getContrato();
        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("assunto", contrato.getAssunto());
        variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
        variables.put("token", tokenSolicitacaoRequest.getUsuario().getTokenAssinatura());


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
        dateFormat.setTimeZone(TimeZone.getDefault());
        variables.put("tokenValidade", dateFormat.format(tokenSolicitacaoRequest.getUsuario().getValidadeTokenAssinatura()));

        variables.put("parte", tokenSolicitacaoRequest.getUsuario().getPessoa().getNomeRazaoSocial());
        String sConteudo = mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.WHATSAPP_TOKEN_ASSINATURA, variables);
        if (CommonsUtil.semValor( sConteudo ))
            return;

        smsConteudo.setConteudo(sConteudo);
        smsConteudo.setDestinatarios(CommonsUtil.somenteNumeros(tokenSolicitacaoRequest.getDestino()));


        try {
            whatsAppApi.enviaWhatsApp(smsConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enviaEmailSolicitacaoVigenteWhatsApp(Contrato contrato, ContratoParte contratoParte, List<ArquivoDownloadModel> arquivos) {

        contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando WhatsApp do contrato vigente para " +
                contratoParte.getNomeRazaoSocial() + " celular: " + contratoParte.getCelular(),
                DataUtil.getCarimboTempo(), true));

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setSubject("A solicitação feita por " + contrato.getCustodiante().getPessoa().getNomeRazaoSocial() +
                " está vigente");

        msg.setTo(contratoParte.getCelular());

        String telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, contrato.getCustodiante());
        if (telefoneWhatsAPP == null)
            telefoneWhatsAPP = sistemaAtributoService.getString(SistemaTipoAtributoEnum.TELEFONE_WHATSAPP, null);

        msg.setFrom(telefoneWhatsAPP);

        Map<String, Object> variables = CommonsUtil.getVariaveisPadroes();
        variables.put("assunto", contrato.getAssunto());
        variables.put("empresa", contrato.getCustodiante().getPessoa().getNomeRazaoSocial());
        variables.put("parte", contratoParte.getNomeRazaoSocial());

        String urlSite = sistemaAtributoService.getString(SistemaTipoAtributoEnum.URL_ACESSO_SITE, null);
        variables.put("acessoURL", urlSite + "/#/validarChaveAcesso?token=" + contratoParte.getChaveAcesso());

        //TODO: remover apos atualizar template
        variables.put("contratoParte", contratoParte.getNomeRazaoSocial());
        variables.put("siteURL", urlSite + "/#/validarChaveAcesso?token=" + contratoParte.getChaveAcesso());


        msg.setText(mensagemService.criarEmailTemplate( contrato.getCustodiante(), SistemaTipoAtributoEnum.WHATSAPP_CONTRATO_ASSINADO, variables));

        if (CommonsUtil.semValor( msg.getText() ))
            return;

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-WhatsApp do contrato vigente enviado para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getCelular(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contrato, null, "Erro enviando WhatsApp do contrato vigente enviado  para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getCelular() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendWhatsApp(msg);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJson(contratoLogSucesso), GsonUtil.toJson(contratoLogErro),
                    false, TipoEnvioMsgEnum.WHATSAPP, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
    }

    public void EnviaEmailContratoRecusadoWhatsApp(Contrato contrato, ContratoParte contratoParte, ContratoParte contratoParteCancelamento) {

        contratoLogService.salva(new ContratoLog(contratoParte.getContrato(), null, "Enviando whatsapp do contrato vigente para " +
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


        msg.setText(mensagemService.criarEmailTemplate(SistemaTipoAtributoEnum.WHATSAPP_RECUSA_ASSINATURA, variables));
        if (CommonsUtil.semValor( msg.getText() ))
            return;

        ContratoLog contratoLogSucesso = null;
        ContratoLog contratoLogErro = null;
        try {

            contratoLogSucesso = new ContratoLog(contratoParte.getContrato(), null, "E-mail do contrato recusado enviado para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail(),
                    DataUtil.getCarimboTempo(), true);

            contratoLogErro = new ContratoLog(contrato, null, "Erro enviando e-mail do contrato recusado enviado  para " +
                    contratoParte.getNomeRazaoSocial() + " e-mail: " + contratoParte.getEmail() + " erro: ",
                    DataUtil.getCarimboTempo(), true);

            sendWhatsApp(msg);

            contratoLogService.salva(contratoLogSucesso);

        } catch (Exception e) {
            //Salva fila para mandar via jog
            EmailFila emailFila = new EmailFila(null, DataUtil.getCarimboTempo().getDataCarimboTempo(), GsonUtil.toJson(msg), GsonUtil.toJson(contratoLogSucesso), GsonUtil.toJson(contratoLogErro),
                    false, TipoEnvioMsgEnum.EMAIL, e.getMessage(), contratoLogSucesso.getDataLog());
            emailFilaService.save(emailFila);

            contratoLogErro.setDataLog(DataUtil.getCarimboTempo().getDataCarimboTempo());
            contratoLogErro.setLog(contratoLogErro.getLog() + e.getMessage());
            contratoLogService.salva(contratoLogErro);

        }
    }

    public void sendWhatsApp(SimpleMailMessage msg) throws Exception {

        WhatsAppApi whatsAppApi = new WhatsAppApi();

        WhatsAppConteudo smsConteudo = new WhatsAppConteudo();

        //smsConteudo.setApi(apiComtele);
        smsConteudo.setRemetente(msg.getFrom());
        //texto limpando tags html
        smsConteudo.setConteudo(msg.getSubject() + "\n\n" + msg.getText());
        String telefone = CommonsUtil.somenteNumeros(msg.getTo()[0]);
        smsConteudo.setDestinatarios(telefone);
        Cliente cliente = null;
        String sURL  = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_URL_SERVICO, cliente);
        smsConteudo.setApi(sURL);

        String sToken  = sistemaAtributoService.getString(SistemaTipoAtributoEnum.WHATSAPP_TOKEN_SERVICO, cliente);
        smsConteudo.setToken(sToken);

        try {
            whatsAppApi.enviaWhatsApp(smsConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
