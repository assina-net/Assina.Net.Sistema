package br.com.assinanet.response;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioCliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.response.base.ResponseComCombos;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class UsuarioClienteResponse  extends ResponseComCombos {

    private UUID id;

    private UsuarioResponse usuario; //id da tabela de usuario

    private Cliente  cliente; //id da tabela de usuario

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


    public UsuarioClienteResponse ( UsuarioCliente usuarioCliente){
        BeanUtils.copyProperties(usuarioCliente, this );
        this.setUsuario(new UsuarioResponse(usuarioCliente.getUsuario()));
    }
}
