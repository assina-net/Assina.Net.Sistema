package br.com.assinanet.request;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */

@Getter
@Setter
public class TokenSolicitacaoRequest {

    Contrato contrato;

    Usuario usuario;

    String tipoEnvio;

    String destino;

}

