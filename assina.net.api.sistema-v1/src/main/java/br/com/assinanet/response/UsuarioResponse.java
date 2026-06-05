package br.com.assinanet.response;

import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class UsuarioResponse {

    private UUID id;

    private PessoaResponse pessoa;

    private String login;

    private String senha;

    private StatusEnum status;

    private PerfilEnum perfil;

    private Boolean assinaturaPendente;

    private byte[] chavePrivada;

    private byte[] chavePublica;

    private String chaveEsqueceuSenha;

    private Date validadeEsqueceuSenha;

    private String tokenAssinatura;


    public String getDescPerfil() {
        if (this.perfil == null)
            return null;
        return this.perfil.getDescricao();
    }

    public String getDescStatus() {
        if (this.status == null)
            return null;
        return this.status.getDescricao();
    }

    public UsuarioResponse(Usuario usuario) {
        BeanUtils.copyProperties(usuario, this);
        this.setPessoa(new PessoaResponse(usuario.getPessoa()));
    }

}
