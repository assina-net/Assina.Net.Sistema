package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ContratoProcuradorModel {

    @Getter
    @Setter
    String cnpj;

    @Getter
    @Setter
    String razaoSocial;

    @Getter
    @Setter
    List<ContratoContatoProcuradorModel> contatoProcurador;

    //region  Construct
    public ContratoProcuradorModel() {
        this.contatoProcurador = new ArrayList<>(0);
    }
    //endregion


}
