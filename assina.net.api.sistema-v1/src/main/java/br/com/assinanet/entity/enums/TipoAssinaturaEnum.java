package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum TipoAssinaturaEnum {
    CHAVEPRIVADA("CHAVE PRIVADA", 1),
    CERTIFICADOLOCAL("CERTIFICADO LOCAL", 2),
    CERTIFICADOREMOTO("CERTIFICADO REMOTO", 3);

    private final String descricao;

    private final Integer opcao;

    TipoAssinaturaEnum(String descricao, Integer opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public TipoAssinaturaEnum find(Boolean opcao) {
        for (TipoAssinaturaEnum status : TipoAssinaturaEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

}
