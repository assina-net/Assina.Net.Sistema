package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "idPessoa")
    private Pessoa pessoa;

    @Column(unique = true)
    @NotNull
    private String login;

    @JsonProperty(access = Access.WRITE_ONLY)
    @NotNull
    private String senha;

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    private PerfilEnum perfil;

    private Boolean assinaturaPendente;

    @Column(name = "chavePrivada") //, columnDefinition = "VARBINARY(MAX)")
    private byte[] chavePrivada;

    @Column(name = "chavePublica") //, columnDefinition = "VARBINARY(MAX)")
    private byte[] chavePublica;

    private String chaveEsqueceuSenha;

    private Date validadeEsqueceuSenha;

    private String tokenAssinatura;

    private Date validadeTokenAssinatura;

    private Integer quantidadeTentativaAcesso;

    private Date primeiraTentativaAcesso;

    private Boolean envioEmailTentativaAcesso;


    @OneToOne
    @JoinColumn(name = "idContratoParteAcesso")
    private ContratoParte contratoParteAcesso;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Usuario usuario = (Usuario) o;
        return id != null && Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}