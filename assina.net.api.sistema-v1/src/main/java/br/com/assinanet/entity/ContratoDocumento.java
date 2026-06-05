package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.response.ContratoDocumentoResponse;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;
import rufus.lzstring4java.LZString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContratoDocumento {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @Type(type = "uuid-char")
    private UUID idDiretorio;


    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrato", nullable = false, updatable = false)
    private Contrato contrato;

    @Column(updatable = false)
    private String nomeDocumento;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    public String getDescStatus() {
        return this.status.getDescricao();
    }

    @Enumerated(EnumType.STRING)
    private StatusDocumentoEnum statusDocumento;


    private Date dataAssinado;

    private String gmtAssinado;

    private String carimboTempoAssinado;


    public String getDescStatusDocumento() {
        if (this.statusDocumento == null)
            return null;
        return this.statusDocumento.getDescricao();
    }

    @NotNull
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoDocumento", updatable = false)
    private TipoDocumento tipoDocumento;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "documentoOriginal", nullable = true, updatable = false)
    private byte[] documentoOriginal;

//    @Column(updatable = false)
    @Column(name = "documento_originalSHA256", nullable = true, updatable = true)
    private String documentoOriginalSHA256;


    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "documentoAssinado", nullable = true, updatable = false)
    private byte[] documentoAssinado;

    private String documentoAssinadoSHA256;

    @Lob
    @Column(updatable = false)
    private String dadosOrigem;

    // papeis
    @OneToMany(mappedBy = "contratoDocumento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(updatable = false)
    private List<ContratoDocumentoPapel> papel = new ArrayList<>(0);

    @OneToMany(mappedBy = "documento", fetch = FetchType.LAZY)
    @org.springframework.data.annotation.Transient
    private List<ContratoParteDocumento> assinaturaPapeis;

    @JsonBackReference("contrato")
    public Contrato getContrato() {
        return contrato;
    }


    public void setDocumentoAssinado(){
        CarimboTempoModel tempo = DataUtil.getCarimboTempo();

        if ( dataAssinado == null) {
          dataAssinado = tempo.getDataCarimboTempo();
          gmtAssinado = tempo.getGmtCarimboTempo();
          carimboTempoAssinado = tempo.getCarimboTempo();
        }
        statusDocumento = StatusDocumentoEnum.ASSINADO;

        System.out.println("Documento Assinado: "+getNomeDocumento());

    }

    public byte[] getDocumentoOriginal() {
        return documentoOriginal;
    }

    public void setDocumentoOriginal(byte[] documentoOriginal) {
        this.documentoOriginal = documentoOriginal;
//        if ( documentoOriginal != null)
//            this.documentoOriginalSHA256 = DigestUtils.sha256Hex(documentoOriginal);
    }

    public void setDocumentoAssinado(byte[] documentoAssinado) {
        this.documentoAssinado = documentoAssinado;
//        if (documentoAssinado != null)
//            this.documentoAssinadoSHA256 = DigestUtils.sha256Hex(documentoAssinado);
    }

    public ContratoDocumento(ContratoDocumentoResponse contratoDocumentoResponse) {

        BeanUtils.copyProperties(contratoDocumentoResponse, this);
        if (!CommonsUtil.semValor(contratoDocumentoResponse.getDocumento()) &&
                CommonsUtil.semValor(contratoDocumentoResponse.getId())) {
            this.setDocumentoOriginal(org.apache.commons.codec.binary.Base64.decodeBase64(LZString.decompressFromUTF16(contratoDocumentoResponse.getDocumento())));
        }
    }
}
