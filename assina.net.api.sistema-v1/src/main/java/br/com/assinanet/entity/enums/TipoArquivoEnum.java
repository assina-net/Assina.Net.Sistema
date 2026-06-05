package br.com.assinanet.entity.enums;

import br.com.assinanet.response.ComboListResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum TipoArquivoEnum {
    ORIGNAL("ORIGINAL"),
    ASSINADO("ASSINADO");

    private final String descricao;
    TipoArquivoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static List<ComboListResponse> getListCombo() {
        List<ComboListResponse> lstEnum = new ArrayList<>(0);
        TipoArquivoEnum[] values = TipoArquivoEnum.values();
        for (TipoArquivoEnum tipoPessoaEnum : TipoArquivoEnum.values()) {
            lstEnum.add(new ComboListResponse(tipoPessoaEnum.toString(), tipoPessoaEnum.getDescricao(), tipoPessoaEnum.getDescricao(), null));
        }

        lstEnum.sort(Comparator.comparing(ComboListResponse::getLabel));

        return lstEnum;
    }

}
