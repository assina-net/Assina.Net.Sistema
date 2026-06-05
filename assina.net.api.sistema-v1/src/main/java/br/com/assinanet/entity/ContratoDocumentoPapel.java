package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContratoDocumentoPapel {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idContratoDocumento", updatable = false)
    private ContratoDocumento contratoDocumento;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPapel")
    private Papel papel;

    @JsonBackReference(value = "contratoDocumento")
    public ContratoDocumento getContratoDocumento() {
        return contratoDocumento;
    }


    public ContratoDocumentoPapel(@NotNull ContratoDocumento contratoDocumento, @NotNull Papel papel) {
        this.contratoDocumento = contratoDocumento;
        this.papel = papel;
    }

    public ContratoDocumentoPapel(@NotNull Papel papel) {
        this.papel = papel;
    }

}
