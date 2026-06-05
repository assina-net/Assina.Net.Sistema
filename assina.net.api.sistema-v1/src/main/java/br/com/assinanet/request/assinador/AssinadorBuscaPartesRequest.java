package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.Contrato;
import lombok.Getter;
import lombok.Setter;

public class AssinadorBuscaPartesRequest {

    @Getter
    @Setter
    Contrato contrato;

    @Getter
    @Setter
    String cpfCnpj;
}
