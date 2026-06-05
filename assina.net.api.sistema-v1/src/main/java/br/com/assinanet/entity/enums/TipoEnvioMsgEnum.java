package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum TipoEnvioMsgEnum {
    EMAIL("EMAIL", 1),
    WHATSAPP("WHATSAPP", 2),
    SMS("SMS", 3);


    private final String descricao;

    private final int opcao;

    TipoEnvioMsgEnum(String descricao, int opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getOpcao() {
        return opcao;
    }

    public TipoEnvioMsgEnum find(Boolean opcao) {
        for (TipoEnvioMsgEnum status : TipoEnvioMsgEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

}
