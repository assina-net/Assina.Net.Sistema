package br.com.assinanet.request.sistemaAtributo;

import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.SistemaTipoAtributoCategoriaEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FiltroSistemaAtributo {


    UUID cliente;

    SistemaTipoAtributoCategoriaEnum categoria;

    private SistemaAtributo filtro;

}
