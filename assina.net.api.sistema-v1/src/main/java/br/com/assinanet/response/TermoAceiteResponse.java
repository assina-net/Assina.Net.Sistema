package br.com.assinanet.response;

import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TermoAceiteResponse {


    UUID id;

    String nome;

    SistemaTipoAtributoEnum tipoTermo;

    String documento;

    Boolean aceite = false;


}
