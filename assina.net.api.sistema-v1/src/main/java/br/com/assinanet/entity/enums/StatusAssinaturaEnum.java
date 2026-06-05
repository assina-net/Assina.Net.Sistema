package br.com.assinanet.entity.enums;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum StatusAssinaturaEnum {

    NAOASSINADO("NÃO ASSINADO", 0),
    ASSINADO("ASSINADO", 1),
    ASSINADOPARCIAL("ASSINADO PARCIAL", 2),
    NAOLIBERADO("NÃO LIBERADO", 3);

    private final String descricao;
    private final Integer opcao;

    StatusAssinaturaEnum(String descricao, Integer opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public StatusAssinaturaEnum find(Integer opcao) {
        for (StatusAssinaturaEnum status : StatusAssinaturaEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }


}

