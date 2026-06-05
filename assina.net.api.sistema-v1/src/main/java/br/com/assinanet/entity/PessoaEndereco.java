package br.com.assinanet.entity;


import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
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
public class PessoaEndereco {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idPessoa")
    private Pessoa pessoa;


    @ManyToOne
    @JoinColumn(name = "idTipoEndereco")
    private TipoEndereco tipoEndereco;

    @ManyToOne
    @JoinColumn(name = "idPais")

    private Pais pais;

    private String endereco;

    private String numero;

    private String complemento;

    private String bairro;

    private String municipio;

    private String estado;

    private String cep;

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
