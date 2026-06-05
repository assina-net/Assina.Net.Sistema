package br.com.assinanet.response;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.response.base.ResponseComCombos;
import lombok.Getter;
import lombok.Setter;

public class ClienteResponse extends ResponseComCombos  {

    @Getter
    @Setter
    Cliente cliente;


}


