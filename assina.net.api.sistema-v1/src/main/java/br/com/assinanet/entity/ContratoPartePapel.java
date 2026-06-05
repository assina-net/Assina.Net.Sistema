package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class ContratoPartePapel {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idContratoParte", updatable = false)
    @Setter
    private ContratoParte contratoParte;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPapel")
    @Setter
    private Papel papel;

    @JsonBackReference(value = "contratoParte")
    public ContratoParte getContratoParte() {
        return contratoParte;
    }

    public ContratoPartePapel(TipoDocumentoPartePapel contratoPartePapel) {
        this.id = contratoPartePapel.getId();
        this.papel = contratoPartePapel.getPapel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ContratoPartePapel that = (ContratoPartePapel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
