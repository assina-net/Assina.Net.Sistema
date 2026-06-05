package br.com.assinanet.entity.enums;

public enum SistemaTipoAtributoCategoriaEnum {
    EMAIL_SENDER("Parametros de envio de email"),
    EMAIL_TEMPLATES("Templates de envio de email"),
    WHATSAPP_SENDER("Parametros de envio de whatsapp"),
    WHATSAPP_TEMPLATES("Templates de envio de email"),
    TELA_PARAMETROS("Templates de envio de email"),
    SISTEMA("Parametros gerais de Sistema"),
    PARAMETROS_CLIENTE("Parametros gerais do Cliente"),
    PARAMETROS_ALERTA("Parametros para Alertas"),
    TERMOS("Termos do sistema"),
    PARAMETROS_EMAIL("Parametros de envio via E-Mail"),
    PARAMETROS_SMS("Parametros de envio via SMS"),
    PARAMETROS_WHATSAPP("Parametros de envio via WhatsApp"),
    PARAMETRO_TIPO_STORAGE("Tipo de Repositorio para os arquivos: LOCAL, AZURE, AWS"),
    PARAMETROS_DIRETORIOS("Parametros de diretorios para salvar arquivos"),
    PARAMETRO_DIRETORIO_STORAGE_AZURE("Diretorio no container Azure para salvar os arquivos");
    private final String descricao;

    SistemaTipoAtributoCategoriaEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }


}