package br.com.assinanet.request;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */

public class ContratoParteAssinarRequest {

    @Getter
    @Setter
    Contrato contrato;

    @Getter
    @Setter
    Usuario usuario;

    @Getter
    @Setter
    boolean duplicata;

}

