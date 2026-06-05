package br.com.assinanet.request;

import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import lombok.Getter;
import lombok.Setter;

public class UsuarioRequest {

    @Getter
    @Setter
    private Usuario usuario;

    @Getter
    @Setter
    private PerfilEnum perfil;
}
