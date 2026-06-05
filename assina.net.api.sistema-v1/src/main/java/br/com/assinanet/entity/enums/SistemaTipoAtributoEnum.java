package br.com.assinanet.entity.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SistemaTipoAtributoEnum {



    EMAIL_SENDER_HOST(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_PORT(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_USERNAME(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_PASSWORD(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_AUTHENTICATE(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_TLS_ENABLE(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_TLS_REQUIRED(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_SSL_ENABLE(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_SENDER_PROTOCOL(SistemaTipoAtributoCategoriaEnum.EMAIL_SENDER),
    EMAIL_NOVO_USUARIO(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_NOVO_USUARIO_CLIENTE_FISICA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_NOVO_USUARIO_CLIENTE_JURIDICA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),

    EMAIL_NOVO_CLIENTE_FISICA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_NOVO_CLIENTE_JURIDICA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_ESQUECEU_SENHA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_TOKEN_ASSINATURA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_CONTRATO_ASSINADO(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_CHAVE_ACESSO(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_RECUSA_ASSINATURA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_CHAVE_ACESSO_OBSERVADOR(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_CHAVE_ACESSO_BLOQUEIO(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),
    EMAIL_CHAVE_ACESSO_PROBLEMA(SistemaTipoAtributoCategoriaEnum.EMAIL_TEMPLATES),




    WHATSAPP_URL_SERVICO(SistemaTipoAtributoCategoriaEnum.WHATSAPP_SENDER),
    WHATSAPP_TOKEN_SERVICO(SistemaTipoAtributoCategoriaEnum.WHATSAPP_SENDER),

    WHATSAPP_TOKEN_ASSINATURA(SistemaTipoAtributoCategoriaEnum.WHATSAPP_TEMPLATES),
    WHATSAPP_CHAVE_ACESSO(SistemaTipoAtributoCategoriaEnum.WHATSAPP_TEMPLATES),
    WHATSAPP_CONTRATO_ASSINADO(SistemaTipoAtributoCategoriaEnum.WHATSAPP_TEMPLATES),
    WHATSAPP_RECUSA_ASSINATURA(SistemaTipoAtributoCategoriaEnum.WHATSAPP_TEMPLATES),
    WHATSAPP_CHAVE_ACESSO_OBSERVADOR(SistemaTipoAtributoCategoriaEnum.WHATSAPP_TEMPLATES),

    //CONSULTA_CONTRATO_MOSTRAR_PARTES(SistemaTipoAtributoCategoriaEnum.TELA_PARAMETROS),
    CONSULTA_CONTRATO_MOSTRAR_PARTES(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    CHAVE_ACESSO_GEOLOCALIZACAO_IPSTACK(SistemaTipoAtributoCategoriaEnum.SISTEMA),
    CHAVE_ACESSO_API_COMTELE(SistemaTipoAtributoCategoriaEnum.SISTEMA),
    QUANTIDADE_MAXIMA_PAGINA(SistemaTipoAtributoCategoriaEnum.SISTEMA),
    CAMPOS_UPPERCASE(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    ASSINAR_VIA_CERTIFICADO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    SOMENTE_INCLUIR_USUARIO_NO_CONTRATO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    MOSTRAR_OBSERVADOR_NO_CONTRATO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    BLOQUEIA_OBSERVADOR_DOCUMENTOS_PENDENTES(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    EXIBE_MENSAGEM_VALIDACAO_DE_DOCUMENTO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),
    PRAZO_CANCELAMENTO_CONTRATO_AUTOMATICO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_ALERTA),

    ASSINAR_VIA_EMAIL(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL),
    ENVIAR_EMAIL_OBSERVADOR_SOMENTE_APOS_ASSINATURA(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL),
    ENVIAR_ARQUIVOS_NO_EMAIL_CONTRATO_ASSINADOS(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL),
    NAO_ENVIAR_ARQUIVO_ORIGINAL_NO_EMAIL_CONTRATO_ASSINADOS(SistemaTipoAtributoCategoriaEnum.PARAMETROS_EMAIL),
    NAO_ENVIAR_ARQUIVO_ORIGINAL_NO_DOWNLOAD_CONTRATO_ASSINADOS(SistemaTipoAtributoCategoriaEnum.PARAMETROS_CLIENTE),

    ASSINAR_VIA_SMS(SistemaTipoAtributoCategoriaEnum.PARAMETROS_SMS),

    ASSINAR_VIA_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    ENVIAR_CONTRATO_LIBERADO_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    ENVIAR_CONTRATO_LIBERADO_OBSERVADOR_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    ENVIAR_CONTRATO_ASSINADO_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    ENVIAR_CONTRATO_PENDENTE_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    ENVIAR_CONTRATO_RECUSA_WHATSAPP(SistemaTipoAtributoCategoriaEnum.PARAMETROS_WHATSAPP),
    TELEFONE_WHATSAPP(SistemaTipoAtributoCategoriaEnum.SISTEMA),


    PRAZO_ENVIO_EMAILS_AUTOMATICO(SistemaTipoAtributoCategoriaEnum.PARAMETROS_ALERTA),
    DIAS_DA_SEMANA_PARA_ENVIO_DE_ALERTA(SistemaTipoAtributoCategoriaEnum.PARAMETROS_ALERTA),
    HORARIOS_PARA_ENVIO_DE_ALERTA(SistemaTipoAtributoCategoriaEnum.PARAMETROS_ALERTA),

    URL_ACESSO_SITE(SistemaTipoAtributoCategoriaEnum.SISTEMA),
    TERMO_DE_USO_SISTEMA(SistemaTipoAtributoCategoriaEnum.TERMOS),
    TERMO_DE_PRIVACIDADE_SISTEMA(SistemaTipoAtributoCategoriaEnum.TERMOS),


    TIPO_STORAGE(SistemaTipoAtributoCategoriaEnum.PARAMETRO_TIPO_STORAGE),

    DIRETORIO_STORAGE_AZURE(SistemaTipoAtributoCategoriaEnum.PARAMETRO_DIRETORIO_STORAGE_AZURE),

    DIRETORIO_ARQUIVOS(SistemaTipoAtributoCategoriaEnum.PARAMETROS_DIRETORIOS);


    private String descricao;

    private final SistemaTipoAtributoCategoriaEnum categoria;

    SistemaTipoAtributoEnum(SistemaTipoAtributoCategoriaEnum categoria) {
        this.categoria = categoria;
    }

    public SistemaTipoAtributoCategoriaEnum getCategoria() {
        return this.categoria;
    }

    public static List<SistemaTipoAtributoEnum> getSistemaAtributoPorCategoria(SistemaTipoAtributoCategoriaEnum categoria) {
        List<SistemaTipoAtributoEnum> result = new ArrayList<>(0);
        Arrays.asList(SistemaTipoAtributoEnum.values()).forEach(sistemaTipoAtributoEnum -> {
            if (categoria.equals(sistemaTipoAtributoEnum.categoria))
                result.add(sistemaTipoAtributoEnum);

        });
        return result;
    }

}
