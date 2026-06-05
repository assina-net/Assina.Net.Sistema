package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusContratoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.response.ContratoDocumentoResponse;
import br.com.assinanet.response.ContratoResponse;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DateUtil2;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contrato {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @Type(type = "uuid-char")
    private UUID idDiretorio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    @Type(type = "uuid-char")
    private Usuario remetente;

    private Date dataCriacao;

    @NotNull
    private String identificador;

    @NotNull
    private String assunto;

    private String tipoContrato;

    private String moeda;

    private BigDecimal valorContratro;

    @NotNull
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", updatable = false)
    private Cliente custodiante; //id da tabela de usuario

    private Integer tipoIntegracao;

    @Enumerated(EnumType.STRING)
    private StatusContratoEnum statusContrato;

    public void setStatusContrato( StatusContratoEnum statusContrato){
        this.statusContrato = statusContrato;
        this.dataStatusContrato = DateUtil2.getDataHoraAgora();
    }

    private Date dataStatusContrato;

    private Boolean liberadoAssinatura;

    private Boolean validado;

    private String validacaoMensagem;

    public String getDescStatusContrato() {
        if (this.statusContrato == null) {
            return null;
        }
        return this.statusContrato.getDescricao();
    }

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    public String getDescStatus() {
        if (this.status == null) {
            return null;
        }
        return this.status.getDescricao();
    }

    private Date dataSolicitacaoAssinatura;

    private String gmtSolicitacaoAssinatura;

    private String carimboTempoSolicitacaoAssinatura;

    @ManyToOne
    @JoinColumn(name = "idUsuarioSolicitacaoAssinatura")
    private Usuario usuarioSolicitacaoAssinatura;

    private String motivoCancelamento;

    private Date dataCancelamento;

    @ManyToOne
    @JoinColumn(name = "idUsuarioCancelamento")
    private Usuario usuarioCancelamento;

    private String motivoRecusa;

    private Date dataRecusa;

    @ManyToOne
    @JoinColumn(name = "idUsuarioRecusa")
    private Usuario usuarioRecusa;


    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoParte> partes;

    @JsonBackReference(value = "partes")
    public List<ContratoParte> getPartes() {
        return partes;
    }

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoDocumento> documentos;

    @JsonBackReference(value = "documentos")
    public List<ContratoDocumento> getDocumentos() {

        if (!CommonsUtil.semValor(documentos)) {
            documentos.removeIf(e -> StatusEnum.INATIVO.equals(e.getStatus()));
        }

        return documentos;
    }

    public Contrato(ContratoResponse contratoResponse)  throws IOException {


        BeanUtils.copyProperties(contratoResponse, this, "partes", "documentos");
        if (getIdDiretorio() == null) {
            setIdDiretorio(UUID.randomUUID());
        }

        setPartes(new ArrayList<>(0));
        if (!CommonsUtil.semValor(contratoResponse.getPartes())) {
            contratoResponse.getPartes().forEach(contratoParteResponse -> {
                ContratoParte contratoParte = new ContratoParte(contratoParteResponse);
                contratoParte.setContrato(this);
                contratoParte.getContatos().forEach(contato -> {
                    contato.setContrato(this);
                    contato.getPapel().forEach(papel ->
                        papel.setContratoParte(contato)
                    );
                    contato.setContratoPartePJ(contratoParte);
                });
                contratoParte.getPapel().forEach(papel ->
                    papel.setContratoParte(contratoParte)
                );
                getPartes().add(contratoParte);
            });
        }

        setDocumentos(new ArrayList<>(0));
        if (!CommonsUtil.semValor(contratoResponse.getDocumentos())) {
            for (ContratoDocumentoResponse contratoDocumentoResponse : contratoResponse.getDocumentos()) {
                ContratoDocumento contratoDocumento = new ContratoDocumento(contratoDocumentoResponse);
                contratoDocumento.setContrato(this);
                contratoDocumento.getPapel().forEach(papel ->
                    papel.setContratoDocumento(contratoDocumento)
                );
                getDocumentos().add(contratoDocumento);
            }
        }

    }

}
