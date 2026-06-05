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
@Setter
@Getter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"idPessoaJuridicaPessoaFisica", "idPessoaFisica", "idPapel"})})
public class PessoaFisicaPapel  {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idPessoaFisica")
    private Pessoa pessoaFisica;

    @ManyToOne
    @JoinColumn(name = "idPessoaJuridicaPessoaFisica")
    private PessoaJuridicaPessoaFisica pessoaJuridicaPessoaFisica;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPapel")
    private Papel papel;

    @JsonBackReference(value = "pessoaJuridicaPessoaFisica")
    public PessoaJuridicaPessoaFisica getPessoaJuridicaPessoaFisica() {
        return pessoaJuridicaPessoaFisica;
    }

    @JsonBackReference(value = "pessoaFisica")
    public Pessoa getPessoaFisica() {
        return pessoaFisica;
    }

}
