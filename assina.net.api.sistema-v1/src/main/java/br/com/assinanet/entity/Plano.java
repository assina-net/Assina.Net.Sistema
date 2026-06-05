package br.com.assinanet.entity;

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
public class Plano {

    @Id
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    private String nome;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    private Integer quantidadeDocumentos;

    private String periodo;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Plano plano = (Plano) o;
        return id != null && Objects.equals(id, plano.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}