package br.com.assinanet.request;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.response.ContratoParteResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TipoDocumentoRequest   {

    TipoDocumento tipoDocumento;

    Cliente cliente;

    List<ContratoParteResponse> partes;

}


