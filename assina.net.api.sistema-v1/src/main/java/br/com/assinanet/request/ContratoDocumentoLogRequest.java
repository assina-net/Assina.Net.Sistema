package br.com.assinanet.request;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import lombok.Getter;
import lombok.Setter;

public class ContratoDocumentoLogRequest {

    @Getter
    @Setter
    Contrato contrato;

    @Getter
    @Setter
    ContratoDocumento documento;

}
