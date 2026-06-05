package br.com.assinanet.request;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UsuarioClienteRequest {

    private UUID id;

    private Usuario usuario; //id da tabela de usuario

    private Cliente cliente; //id da tabela de usuario

    private StatusEnum status;

    public String getDescStatus() {
        if (this.status == null)
            return null;
        return this.status.getDescricao();
    }

    private PerfilEnum perfil;

    public String getDescPerfil() {
        if (this.perfil == null)
            return null;
        return this.perfil.getDescricao();
    }

    private PerfilEnum perfilClienteSelecionado;
}


