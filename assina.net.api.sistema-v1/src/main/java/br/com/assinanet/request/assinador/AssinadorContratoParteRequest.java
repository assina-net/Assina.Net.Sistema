package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.ContratoPartePapel;
import br.com.assinanet.entity.enums.StatusAssinaturaEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AssinadorContratoParteRequest {

    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private Contrato contrato;

    @Getter
    @Setter
    private ContratoParte contratoPartePJ;

    @Getter
    @Setter
    private StatusEnum status;

    @Getter
    @Setter
    private TipoPessoaEnum tipoPessoa;

    @Getter
    @Setter
    private String cpfCnpj;

    @Getter
    @Setter
    private String nomeRazaoSocial;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String chaveAcesso;

    @Getter
    @Setter
    private Date validadeChaveAcesso;

    @Getter
    @Setter
    private StatusAssinaturaEnum statusAssinatura;

    @Getter
    @Setter
    private Boolean duplicatas;

    // Contatos
    @Getter
    @Setter
    private List<ContratoParte> contatos = new ArrayList<>(0);

    // papeis
    @Getter
    @Setter
    private List<ContratoPartePapel> papel = new ArrayList<>(0);

}
