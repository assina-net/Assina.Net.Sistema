package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TipoDocumentoPapel {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")

    private UUID id;

    //@NotNull
    @ManyToOne
    @JoinColumn(name = "idTipoDocumento")
    @NotNull
    private TipoDocumento tipoDocumento;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPapel")
    @NotNull
    private Papel papel;

    @JsonBackReference(value = "tipoDocumento")
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    private Boolean token;

    private Boolean certificate;

}
