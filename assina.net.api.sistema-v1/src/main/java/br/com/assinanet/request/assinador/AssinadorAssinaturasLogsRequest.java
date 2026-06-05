package br.com.assinanet.request.assinador;


import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AssinadorAssinaturasLogsRequest {

    @Getter
    @Setter
    Contrato contrato;

    @Getter
    @Setter
    ContratoParte contratoParteAssinou;

    @Getter
    @Setter
    List<AssinadorContratoParteDocumentoRequest> contratoParteDocumentos;

    @Getter
    @Setter
    List<AssinadorContratoLogRequest> logs;

}
