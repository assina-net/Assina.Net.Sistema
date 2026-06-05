package br.com.assinanet.entity;

import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class TipoDocumentoPosicao {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idTipoDocumento")
    @NotNull
    private TipoDocumento tipoDocumento;

    @ManyToOne
    @JoinColumn(name = "idTipoDocumentoPapel")
    private TipoDocumentoPapel papel;

    private float x;

    private float y;

    private float width;

    private float height;

    private Integer pagina;

    @JsonBackReference(value = "tipoDocumento")
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public Integer getPagina() {
        return CommonsUtil.intValue(pagina);
    }
}
