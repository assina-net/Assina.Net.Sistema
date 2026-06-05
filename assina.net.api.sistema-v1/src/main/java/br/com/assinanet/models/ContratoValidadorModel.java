package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ContratoValidadorModel {

    @Getter
    @Setter
    String cnpj;

    @Getter
    @Setter
    String razaoSocial;

    @Getter
    @Setter
    List<ContratoContatoModel> contatoValidador;

    //region  Construct
    public ContratoValidadorModel() {
        this.contatoValidador = new ArrayList<>(0);
    }
    //endregion
}
