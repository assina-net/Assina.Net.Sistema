package br.com.assinanet.entity.enums;

import br.com.assinanet.response.ComboListResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum TipoPessoaEnum {
    FISICA("FISICA", 'F'),
    JURIDICA("JURIDICA", 'J');

    private final String descricao;

    private final char opcao;

    TipoPessoaEnum(String descricao, char opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public char getOpcao() {
        return opcao;
    }

    public TipoPessoaEnum find(Boolean opcao) {
        for (TipoPessoaEnum status : TipoPessoaEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

    public static List<ComboListResponse> getListCombo() {
        List<ComboListResponse> lstEnum = new ArrayList<>(0);
        TipoPessoaEnum[] values = TipoPessoaEnum.values();
        for (TipoPessoaEnum tipoPessoaEnum : TipoPessoaEnum.values()) {
            lstEnum.add(new ComboListResponse(tipoPessoaEnum.toString(), tipoPessoaEnum.getDescricao(), tipoPessoaEnum.getDescricao(), null));
        }

        lstEnum.sort(Comparator.comparing(ComboListResponse::getLabel));

        return lstEnum;
    }

}
