package br.com.assinanet.entity;


import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
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
public class PessoaTelefone {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idPessoa")
    private Pessoa pessoa;


    @ManyToOne
    @NotNull
    @JoinColumn(name = "idTipoTelefone")
    private TipoTelefone tipoTelefone;


    @ManyToOne
    @JoinColumn(name = "idPais")
    private Pais pais;

    private String numero;

    private String complemento;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastramento;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAtualizacao;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    public String getDescStatus() {
        if (this.status == null) {
            return null;
        }
        return this.status.getDescricao();
    }

    @JsonBackReference(value = "pessoa")
    public Pessoa getPessoa() {
        return pessoa;
    }


}
