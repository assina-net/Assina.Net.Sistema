package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
public class TipoTelefone {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idCliente")
    @NotNull
    private Cliente cliente;

    @NotNull
    private String nome;

    @NotNull
    private String identificacao;


    @Enumerated(EnumType.STRING)
    private StatusEnum status;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TipoTelefone that = (TipoTelefone) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
