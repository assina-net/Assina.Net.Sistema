package br.com.assinanet.service;


import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.TipoEnvioMsgEnum;
import br.com.assinanet.util.CommonsUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MensagemService {


    private final static String TEMPLATE_LOCAL = "pt-BR";

    private TemplateEngine templateEngine;
    private final SistemaAtributoService sistemaAtributoService;

    public MensagemService(SistemaAtributoService sistemaAtributoService) {
        this.sistemaAtributoService = sistemaAtributoService;
    }

    public String criarEmailTemplate(SistemaTipoAtributoEnum template, Map<String, Object> variables) {
        return criarEmailTemplate(null, template, variables);
    }

    public String criarEmailTemplate(Cliente cliente, SistemaTipoAtributoEnum template, Map<String, Object> variables) {
        SistemaAtributo atributo = sistemaAtributoService.getSistemaAtributo(template, cliente);

        if ( CommonsUtil.semValor(atributo) && !CommonsUtil.semValor(cliente))
            atributo = sistemaAtributoService.getSistemaAtributo(template, null);

        if ( CommonsUtil.semValor(atributo))
            return null;

        Context context = new Context();
        context.setVariables(variables);

        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);

        SpringStandardDialect dialect = new SpringStandardDialect();
        dialect.setEnableSpringELCompiler(true);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setDialect(dialect);
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateResolver(templateResolver);

        return engine.process(atributo.getValorAtributo(), context);
    }

    public Map<TipoEnvioMsgEnum, List<ContratoParte>> montaListasEnvio(final SistemaTipoAtributoEnum sistemaTipoAtributoEnviaWhatsApp,
                                                                       final Contrato contrato, List<ContratoParte> partes){

        Map<TipoEnvioMsgEnum, List<ContratoParte>> result = new HashMap<TipoEnvioMsgEnum, List<ContratoParte>>(0);

        boolean enviaWhatsApp = sistemaAtributoService.getBoolean(sistemaTipoAtributoEnviaWhatsApp, contrato.getCustodiante());

        List<ContratoParte> lstContratoPartesFisicasEmail;
        List<ContratoParte> lstContratoPartesFisicasWhatsApp = null;

        if (enviaWhatsApp) {
            lstContratoPartesFisicasWhatsApp = partes.stream().filter(c -> !CommonsUtil.semValor(c.getCelular()) &&
                    !CommonsUtil.mesmoValor( contrato.getCustodiante().getPessoa().getCpfCnpj() , c.getCpfCnpj() ) ).collect(Collectors.toList());
            lstContratoPartesFisicasEmail = partes.stream().filter(c -> !CommonsUtil.semValor(c.getEmail()) ||
                    CommonsUtil.mesmoValor( contrato.getCustodiante().getPessoa().getCpfCnpj() , c.getCpfCnpj() ) ).collect(Collectors.toList());
        } else {
            lstContratoPartesFisicasEmail = partes;
        }
        if (!CommonsUtil.semValor(lstContratoPartesFisicasEmail) )
            result.put(TipoEnvioMsgEnum.EMAIL, lstContratoPartesFisicasEmail);

        if (!CommonsUtil.semValor(lstContratoPartesFisicasEmail) )
            result.put(TipoEnvioMsgEnum.WHATSAPP, lstContratoPartesFisicasWhatsApp);

        return result;
    }

    @Primary
    private TemplateEngine getTemplateEngine() {
        templateEngine = new TemplateEngine();
        StringTemplateResolver stringTemplateResolver = new StringTemplateResolver();
        templateEngine.setTemplateResolver(stringTemplateResolver);
        return templateEngine;
    }
//
//    private String getTemplateFromAttributes(String htmlContent, Map<String, Object> attr) {
//        templateEngine = getTemplateEngine();
//        Context context = new Context(new Locale(TEMPLATE_LOCAL));
//        if (!CollectionUtils.isEmpty(Collections.singleton(attr))) {
//            attr.forEach(context::setVariable);
//        }
//        return templateEngine.process(htmlContent, context);
//    }

}
