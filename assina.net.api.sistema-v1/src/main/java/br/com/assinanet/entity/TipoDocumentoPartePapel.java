package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class TipoDocumentoPartePapel {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTipoDocumentoParte", updatable = false)
    @Setter
    private TipoDocumentoParte tipoDocumentoParte;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPapel")
    @Setter
    private Papel papel;

    @JsonBackReference(value = "contratoParte")
    public TipoDocumentoParte getTipoDocumentoParte() {
        return tipoDocumentoParte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TipoDocumentoPartePapel that = (TipoDocumentoPartePapel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
