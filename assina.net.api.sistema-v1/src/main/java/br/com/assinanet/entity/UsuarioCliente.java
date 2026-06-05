package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsuarioCliente {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")

    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario; //id da tabela de usuario

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente; //id da tabela de usuario

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    public String getDescStatus() {
        if (this.status == null)
            return null;
        return this.status.getDescricao();
    }

    @Enumerated(EnumType.STRING)
    private PerfilEnum perfil;

    public String getDescPerfil() {
        if (this.perfil == null)
            return null;
        return this.perfil.getDescricao();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsuarioCliente that = (UsuarioCliente) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
