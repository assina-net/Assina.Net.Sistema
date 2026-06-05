package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

public class ContratoContatoModel {

    @Getter
    @Setter
    String nome;

    @Getter
    @Setter
    String email;

    @Getter
    @Setter
    String cpf;

    //region  Construct
    public ContratoContatoModel() {
    }

    public ContratoContatoModel(String nome, String email, String cpf) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
    }
    //endregion

}
