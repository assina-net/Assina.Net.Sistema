package br.com.assinanet.request;

import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class IdRequest {


    UUID id;

    private Usuario usuario;

    UUID idCliente;
}
