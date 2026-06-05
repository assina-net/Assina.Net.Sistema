package br.com.assinanet.entity.enums;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum StatusDocumentoEnum {

    NAOASSINADO("NÃO ASSINADO", 0),
    ASSINADO("ASSINADO", 1),
    PARCIALMENTEASSINADO("PARCIALMENTE ASSINADO", 2),
    NAOASSINA("NÃO ASSINA", 3);

    private final String descricao;

    private final Integer opcao;

    StatusDocumentoEnum(String descricao, Integer opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public StatusDocumentoEnum find(Integer opcao) {
        for (StatusDocumentoEnum status : StatusDocumentoEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }


}

