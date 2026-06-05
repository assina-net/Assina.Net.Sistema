package br.com.assinanet.request;

import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class ContratoCancelamentoRequest {

    @Getter
    @Setter
    Usuario usuario;

    @Getter
    @Setter
    List<UUID> contratos;

    @Getter
    @Setter
    String motivo;


}
