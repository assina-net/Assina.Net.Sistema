package br.com.assinanet.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AssinaTokenResponse {

    private String mensagem;

    private String tokenAssinatura;

    private Date validadeTokenAssinatura;
}

