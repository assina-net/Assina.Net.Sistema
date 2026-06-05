package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
@Getter
@Setter
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    private String nomeRazaoSocial;

    @NotNull
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    private TipoPessoaEnum tipoPessoa;

    @Email(message = "Email inválido! Não pode conter espaços nem simbolos diferentes de ponto e o arroba")
    private String email;

    @ManyToOne
    @JoinColumn(name = "idCliente")
    @Setter
    private Cliente cliente;


    // endereco de pessoas
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Setter
    private List<PessoaEndereco> pessoaEndereco = new ArrayList<>(0);

    public List<PessoaEndereco> getPessoaEndereco() {
        if (pessoaEndereco != null) {

            pessoaEndereco.removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));

            //ordenando tipo endereco
            pessoaEndereco.sort((o1, o2) -> {

                int compare = CommonsUtil.compare(o1.getTipoEndereco(), o2.getTipoEndereco());
                if (compare != 0) {
                    return compare;
                }

                if (o1.getTipoEndereco() != null && o2.getTipoEndereco() != null) {
                    compare = CommonsUtil.stringValue(o2.getTipoEndereco().getNome().toUpperCase()).compareTo(CommonsUtil.stringValue(o1.getTipoEndereco().getNome().toUpperCase()));
                }

                return compare;
            });

        }
        return pessoaEndereco;

    }

    // endereco de pessoas
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Setter
    private List<PessoaTelefone> pessoaTelefone = new ArrayList<>(0);

    public List<PessoaTelefone> getPessoaTelefone() {
        if (pessoaTelefone != null) {

            pessoaTelefone.removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));

            //ordenando tipo telefone
            pessoaTelefone.sort((o1, o2) -> CommonsUtil.stringValue(o2.getTipoTelefone().getNome().toUpperCase()).compareTo(CommonsUtil.stringValue(o1.getTipoTelefone().getNome().toUpperCase())));
        }
        return pessoaTelefone;

    }


    // papeis, somente para cadastro de pessoas fisicas
    @OneToMany(mappedBy = "pessoaFisica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private List<PessoaFisicaPapel> papel = new ArrayList<>(0);

    // pessoas fisicas, somente para cadastro de pessoas juridicas
    @OneToMany(mappedBy = "pessoaJuridica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private List<PessoaJuridicaPessoaFisica> pessoasFisica = new ArrayList<>(0);


    @JsonBackReference(value = "cliente")
    public Cliente getCliente() {
        return cliente;
    }

    public Pessoa(ContratoParte contratoParte, Cliente cliente) {
        this.nomeRazaoSocial = contratoParte.getNomeRazaoSocial();
        this.cpfCnpj = contratoParte.getCpfCnpj();
        this.email = contratoParte.getEmail();
        this.tipoPessoa = contratoParte.getTipoPessoa();
        if (!CommonsUtil.semValor(cliente)) {
            this.cliente = cliente;
        }

    }

}
