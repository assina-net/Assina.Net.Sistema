package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
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
public class UsuarioTermo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario; //id da tabela de usuario

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idSistemaAtributo")
    private SistemaAtributo sistemaAtributo; //id da tabela de usuario

    private Date dataAceite;

    private String ip;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsuarioTermo that = (UsuarioTermo) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
