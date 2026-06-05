package br.com.assinanet.response;

import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.response.base.ResponseComCombos;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TipoDocumentoResponse extends ResponseComCombos  {

    TipoDocumento tipoDocumento;

    List<ContratoParteResponse> partes;

}


