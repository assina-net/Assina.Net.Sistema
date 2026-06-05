package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum StatusEnum {
    INATIVO("INATIVO", false),
    ATIVO("ATIVO", true);

    private final String descricao;

    private final Boolean opcao;

    StatusEnum(String descricao, Boolean opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getOpcao() {
        return opcao;
    }

    public StatusEnum find(Boolean opcao) {
        for (StatusEnum status : StatusEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

}
