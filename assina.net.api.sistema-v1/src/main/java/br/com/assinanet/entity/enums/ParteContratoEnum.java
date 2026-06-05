package br.com.assinanet.entity.enums;

import br.com.assinanet.util.CommonsUtil;
import lombok.Getter;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum ParteContratoEnum {

    Procurador('P', "Procurador"),
    Validador('V', "Validador");

    @Getter
    private final char valor;
    @Getter
    private final String descricao;

    ParteContratoEnum(char valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return CommonsUtil.stringValue(valor);
    }
}
