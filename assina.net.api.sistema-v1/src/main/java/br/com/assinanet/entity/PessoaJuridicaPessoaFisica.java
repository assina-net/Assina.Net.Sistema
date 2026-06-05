package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"idPessoaJuridica", "idPessoaFisica"})})
public class PessoaJuridicaPessoaFisica {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @ManyToOne
    @Setter
    @JoinColumn(name = "idPessoaJuridica")
    private Pessoa pessoaJuridica;

    @ManyToOne
    @JoinColumn(name = "idPessoaFisica")
    @Getter
    @Setter
    private Pessoa pessoaFisica;

    // papeis que foram utilizados na última vez
    @OneToMany(mappedBy = "pessoaJuridicaPessoaFisica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private List<PessoaFisicaPapel> papel = new ArrayList<>(0);

    @JsonBackReference(value = "pessoaJuridica")
    public Pessoa getPessoaJuridica() {
        return pessoaJuridica;
    }

}
