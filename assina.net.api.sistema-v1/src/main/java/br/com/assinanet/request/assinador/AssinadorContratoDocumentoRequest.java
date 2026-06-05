package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.ContratoDocumentoPapel;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class AssinadorContratoDocumentoRequest {

    private UUID id;

    private UUID idDiretorio;

//    private Contrato contrato;
//
//    private String nomeDocumento;

    private StatusEnum status;

    private StatusDocumentoEnum statusDocumento;

    private TipoDocumento tipoDocumento;

    private String documentoOriginal;

    private String documentoOriginalSHA256;

//    private byte[] documentoAssinado;

    private String documentoAssinadoSHA256;

//    private String dadosOrigem;

    private List<ContratoDocumentoPapel> papel = new ArrayList<>(0);

}
