package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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
public class TipoDocumentoParte {
    /*
    br.com.assinanet.request.ContratoParteRequest
    alterar aqui também
    */

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "idTipoDocumento")
    @NotNull
    private TipoDocumento tipoDocumento;

    @OneToOne(cascade = CascadeType.PERSIST , fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", updatable = false)
    Cliente cliente;

    @OneToOne(cascade = CascadeType.PERSIST , fetch = FetchType.LAZY)
    @JoinColumn(name = "idPessoa")
    private Pessoa pessoa;

    @OneToOne(cascade = CascadeType.PERSIST , fetch = FetchType.LAZY)
    @JoinColumn(name = "idPessoaPJ")
    private Pessoa pessoaJuridica;

    // papeis
    @OneToMany(mappedBy = "tipoDocumentoParte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TipoDocumentoPartePapel> papel = new ArrayList<>(0);

    @JsonBackReference("tipoDocumento")
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

}
