package br.com.assinanet.entity.enums;

import br.com.assinanet.response.ComboListResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum xTipoClienteEnum {
    SISTEMA("Sistema", 0),
    CONTRATOSIMPLES("Contrato Simples", 1),
    FACTORING("Factoring", 2),
    FIDC("FIDC", 3),
    CORRETORASEGUROS("Corretora Sesguros", 4);

    private final String descricao;

    private final Integer opcao;

    xTipoClienteEnum(String descricao, Integer opcao) {
        this.descricao = descricao;
        this.opcao = opcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public br.com.assinanet.entity.enums.xTipoClienteEnum find(Boolean opcao) {
        for (br.com.assinanet.entity.enums.xTipoClienteEnum status : br.com.assinanet.entity.enums.xTipoClienteEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }



    public static List<ComboListResponse> getListCombo(Boolean mostraSistema) {
        List<ComboListResponse> lstEnum = new ArrayList<>(0);
        xTipoClienteEnum[] values = xTipoClienteEnum.values();
        for (xTipoClienteEnum tipoClienteEnum : xTipoClienteEnum.values()) {
            if ( !mostraSistema &&  SISTEMA.equals(tipoClienteEnum) )
                continue;

            lstEnum.add(new ComboListResponse(tipoClienteEnum.toString(), tipoClienteEnum.getDescricao(), tipoClienteEnum.getDescricao(), null));
        }

        lstEnum.sort(Comparator.comparing(ComboListResponse::getLabel));

        return lstEnum;
    }


}
