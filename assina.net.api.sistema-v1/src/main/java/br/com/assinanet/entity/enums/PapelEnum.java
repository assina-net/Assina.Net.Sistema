package br.com.assinanet.entity.enums;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum PapelEnum {

    OBSERVADOR("OBSERVADOR");

    private final String descricao;

    PapelEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
