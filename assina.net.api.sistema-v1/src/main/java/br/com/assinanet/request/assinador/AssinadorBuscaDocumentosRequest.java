package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import lombok.Getter;
import lombok.Setter;

public class AssinadorBuscaDocumentosRequest {

    @Getter
    @Setter
    Contrato contrato;

    @Getter
    @Setter
    ContratoParte contratoParte;
}
