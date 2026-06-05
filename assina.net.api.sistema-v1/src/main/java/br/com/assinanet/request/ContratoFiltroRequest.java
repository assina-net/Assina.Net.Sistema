package br.com.assinanet.request;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.response.ContratoResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoFiltroRequest {


    Contrato contrato;

    Usuario usuario;

}
