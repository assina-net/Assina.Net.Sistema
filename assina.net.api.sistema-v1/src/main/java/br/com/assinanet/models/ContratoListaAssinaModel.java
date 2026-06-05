package br.com.assinanet.models;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.enums.StatusContratoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ContratoListaAssinaModel {

    @Getter
    @Setter
    private UUID id;


    @Getter
    @Setter
    private Date dataCriacao;

    @Getter
    @Setter
    private String identificador;

    @Getter
    @Setter
    private String assunto;

    @Getter
    @Setter
    private String tipoContrato;

    @Getter
    @Setter
    private String moeda;

    @Getter
    @Setter
    private BigDecimal valorContratro;

    @Getter
    @Setter
    private Cliente custodiante; //id da tabela de usuario

    @Getter
    @Setter
    private Integer tipoIntegracao;

    @Getter
    @Setter
    private StatusContratoEnum statusContrato;

    @Getter
    @Setter
    private Boolean liberadoAssinatura;

    @Getter
    @Setter
    private Boolean validado;

    @Getter
    @Setter
    private String validacaoMensagem;

    public String getDescStatusContrato() {
        return this.statusContrato.getDescricao();
    }

    @Getter
    @Setter
    private StatusEnum status;

    public String getDescStatus() {
        return this.status.getDescricao();
    }

    @Getter
    @Setter
    private Date dataSolicitacaoAssinatura;

    @Getter
    @Setter
    private String gmtSolicitacaoAssinatura;

    @Getter
    @Setter
    private String carimboTempoSolicitacaoAssinatura;

    @Getter
    @Setter
    List<ContratoParte> partes;

    @Getter
    @Setter
    Map<Papel, List<ContratoParte>> mapPapel;

}
