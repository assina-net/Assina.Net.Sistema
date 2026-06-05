package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusAssinaturaEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.*;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContratoParte {
    /*
    br.com.assinanet.request.ContratoParteRequest
    alterar aqui também
    */

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "idContrato", updatable = false)
    private Contrato contrato;

    @OneToOne
    @JoinColumn(name = "idContratoPartePj", updatable = false)
    private ContratoParte contratoPartePJ;


    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    private TipoPessoaEnum tipoPessoa;

    @NotNull
    private String cpfCnpj;

    @NotNull
    private String nomeRazaoSocial;

    @Email(message = "Email inválido! Não pode conter espaços nem simbolos diferentes de ponto e o arroba")
    private String email;


    private String celular;

    private String chaveAcesso;

    private Date validadeChaveAcesso;

    private String tokenAssinatura;

    @Enumerated(EnumType.STRING)
    private StatusAssinaturaEnum statusAssinatura;

    public String getStatusAssinaturaDesc() {
        if (this.statusAssinatura == null) {
            return null;
        }
        return this.statusAssinatura.getDescricao();
    }

    private String requisitoAssinatura;

    private Boolean liberadoAssinatura;

    // Contatos
    @OneToMany(mappedBy = "contratoPartePJ", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoParte> contatos = new ArrayList<>(0);

    public List<ContratoParte> getContatos() {
        if (!CommonsUtil.semValor(contatos)) {
            contatos.removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));
            contatos.sort(Comparator.comparing(ContratoParte::getNomeRazaoSocial));
        }
        return contatos;
    }

    // papeis
    @OneToMany(mappedBy = "contratoParte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoPartePapel> papel = new ArrayList<>(0);

    @JsonBackReference("contrato")
    public Contrato getContrato() {
        return contrato;
    }

    @JsonBackReference("contratoPartePJ")
    public ContratoParte getContratoPartePJ() {
        return contratoPartePJ;
    }

    public ContratoParte(ContratoParteResponse parteRequest) {

        BeanUtils.copyProperties(parteRequest, this, "contatos", "papel");

        if (parteRequest.getPapel() != null) {
            this.papel = new ArrayList<>(0);
            parteRequest.getPapel().forEach(papelRequest ->
                this.papel.add(papelRequest)
            );
        } else {
            this.papel = null;
        }

        if (parteRequest.getContatos() != null) {
            this.contatos = new ArrayList<>(0);
            parteRequest.getContatos().forEach(contato ->
                this.contatos.add(new ContratoParte(contato))
            );
        } else {
            this.contatos = null;
        }
    }

}
