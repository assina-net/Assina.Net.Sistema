package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContratoCartaCessaoModel {

    private String identificaoCartaCessao;
    private byte[] documentoCartasCessao;
    List<String> Papel;

}
