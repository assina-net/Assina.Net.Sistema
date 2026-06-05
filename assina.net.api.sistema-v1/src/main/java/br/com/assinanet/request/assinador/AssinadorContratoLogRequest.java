package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.Contrato;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class AssinadorContratoLogRequest {

    private UUID id;

    private Contrato contrato;

    private AssinadorContratoDocumentoRequest contratoDocumento;

    private String log;

    private Date dataLog;

    private String gmtLog;

    private String carimboTempoLog;

    private Boolean logSistema;




}
