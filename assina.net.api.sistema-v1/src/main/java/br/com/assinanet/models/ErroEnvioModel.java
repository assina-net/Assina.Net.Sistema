package br.com.assinanet.models;

import br.com.assinanet.entity.enums.CodigoErrosEnvioEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


public class ErroEnvioModel {

    @JsonProperty("codigoErro")
    @Getter
    @Setter
    CodigoErrosEnvioEnum codigoErro;

    @Getter
    @Setter
    String descricaoErro;

    public ErroEnvioModel(CodigoErrosEnvioEnum codigoErro, String descricaoErro) {
        this.codigoErro = codigoErro;
        this.descricaoErro = descricaoErro;
    }


}
