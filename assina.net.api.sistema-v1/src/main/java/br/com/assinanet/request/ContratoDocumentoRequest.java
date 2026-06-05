package br.com.assinanet.request;

import br.com.assinanet.entity.ContratoDocumentoPapel;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */

public class ContratoDocumentoRequest {

    @Getter
    @Setter
    UUID id;

    @Getter
    @Setter
    String nomeDocumento;

    @Getter
    @Setter
    StatusEnum status;

    @Getter
    @Setter
    StatusDocumentoEnum statusDocumento;

    @Getter
    @Setter
    TipoDocumento tipoDocumento;

    @Getter
    @Setter
    List<ContratoDocumentoPapel> papel;

    @Getter
    @Setter
    byte[] documento = new byte[]{};

}

