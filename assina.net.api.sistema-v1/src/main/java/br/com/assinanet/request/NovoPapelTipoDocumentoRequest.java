package br.com.assinanet.request;

import br.com.assinanet.entity.Papel;
import lombok.Data;

import java.util.UUID;

@Data
public class NovoPapelTipoDocumentoRequest {
    Papel papel;
    UUID idTipoDocumento;
}
