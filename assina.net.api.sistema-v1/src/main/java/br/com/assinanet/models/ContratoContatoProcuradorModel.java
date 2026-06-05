package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ContratoContatoProcuradorModel extends ContratoContatoModel {

    @Getter
    @Setter
    int duplicatas;

    @Getter
    @Setter
    List<String> Papel;

    //region  Construct
    public ContratoContatoProcuradorModel(String nome, String email, String cpf, int duplicatas, List<String> papel) {
        super(nome, email, cpf);
        this.duplicatas = duplicatas;
        Papel = papel;
    }
    //endregion

}
