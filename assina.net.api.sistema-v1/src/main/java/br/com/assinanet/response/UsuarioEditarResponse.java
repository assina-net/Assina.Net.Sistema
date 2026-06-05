package br.com.assinanet.response;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.response.base.ResponseComCombos;
import lombok.Getter;
import lombok.Setter;

public class UsuarioEditarResponse extends ResponseComCombos  {

    @Getter
    @Setter
    Usuario usuario;


}


