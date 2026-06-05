package br.com.assinanet.entity;

import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.request.assinador.AssinadorContratoLogRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.beans.BeanUtils;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
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
public class ContratoLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "idContrato")
    private Contrato contrato;

    @ManyToOne
    @JoinColumn(name = "idContratoDocumento")
    private ContratoDocumento contratoDocumento;

    @NotNull
    @Lob
    private String log;

    @NotNull
    private Date dataLog;

    @NotNull
    private String gmtLog;

    @NotNull
    private String carimboTempoLog;

    private Boolean logSistema;


    public ContratoLog(@NotNull Contrato contrato, ContratoDocumento contratoDocumento, String log, CarimboTempoModel carimbo, Boolean logSistema) {
        this.contrato = contrato;
        this.contratoDocumento = contratoDocumento;
        this.log = log;
        this.dataLog = carimbo.getDataCarimboTempo();
        this.gmtLog = carimbo.getGmtCarimboTempo();
        this.carimboTempoLog = carimbo.getCarimboTempo();
        this.logSistema = logSistema;
    }

    public ContratoLog(AssinadorContratoLogRequest assinadorContratoLogRequest) {
        BeanUtils.copyProperties(assinadorContratoLogRequest, this);
        this.contratoDocumento = new ContratoDocumento();
        BeanUtils.copyProperties( assinadorContratoLogRequest.getContratoDocumento(),this.contratoDocumento, "documento");
    }

}
