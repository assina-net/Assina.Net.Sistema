package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum FlagEnum {

    NAO("NÃO", false),
    SIM("SIM", true);

    private final String descricao;
    private final Boolean opcao;

    FlagEnum(String descricao, Boolean opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getOpcao() {
        return opcao;
    }

    public FlagEnum find(Boolean opcao) {
        for (FlagEnum status : FlagEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

}
