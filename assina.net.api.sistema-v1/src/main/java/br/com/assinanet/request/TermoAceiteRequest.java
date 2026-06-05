package br.com.assinanet.request;

import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.response.TermoAceiteResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TermoAceiteRequest {

    Usuario usuario;
    String ip;
    List<TermoAceiteResponse> termos;

}
