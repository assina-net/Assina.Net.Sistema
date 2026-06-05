package br.com.assinanet.entity.enums;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum StatusContratoEnum {

    NAOLIBERADOASSINTAURA("NÃO LIBERADO PARA ASSINATURA", 0),
    ASSINADO("ASSINADO", 1),
    LIBERADOASSINATURA("LIBERADO ASSINATURA", 1),
    PARCIALMENTEASSINADO("PARCIALMENTE ASSINADO", 1),
    LIBERANDOASSINATURA("LIBERANDO ASSINATURA", 1),
    GERANDOASSINATURAS("GERANDO ASSINATURAS", 1),

    GERANDOASSINATURA("GERANDO ASSINATURA", 1),

    CANCELADO("CANCELADO", 1),
    RECUSADO("RECUSADO", 1);

    private final String descricao;
    private final Integer opcao;

    StatusContratoEnum(String descricao, Integer opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public StatusContratoEnum find(Integer opcao) {
        for (StatusContratoEnum status : StatusContratoEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }


}

