package br.com.assinanet.models;

import br.com.assinanet.entity.enums.CodigoErrosEnvioEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@JacksonXmlRootElement(localName = "retornoContrato")
public class RetornoEnvioModel {
    @Getter
    @Setter
    String idContrato;

    @Getter
    @Setter
    UUID idAssinaNet;

    @Setter
    int statusProcessamento;

    @JacksonXmlElementWrapper(localName = "erros")
    @JsonProperty("erro")
    @Getter
    @Setter
    List<ErroEnvioModel> lstErroEnvioModels = new ArrayList<>();

    //region Procedures
    public void adicionaErro(CodigoErrosEnvioEnum codigoErro) {
        adicionaErro(codigoErro, "");
    }

    public void adicionaErro(CodigoErrosEnvioEnum codigoErro, String mensagem) {
        this.lstErroEnvioModels.add(new ErroEnvioModel(codigoErro, codigoErro.getMensagemErro() + mensagem));
    }
    //endregion

    //region Get/Set
    @JsonProperty("statusProcessamento")
    public int getStatusProcessamento() {
        return (lstErroEnvioModels.size() == 0 ? 1 : 2);
    }
    //endregion
}
