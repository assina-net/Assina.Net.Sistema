package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.TipoAssinaturaEnum;
import br.com.assinanet.request.assinador.AssinadorContratoParteDocumentoRequest;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import rufus.lzstring4java.LZString;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Base64;
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
public class ContratoParteDocumento {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "idContratoPartePapel")
    private ContratoPartePapel contratoPartePapel;

    @OneToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @NotNull
    @OneToOne
    @JoinColumn(name = "idContratoDocumento")
    private ContratoDocumento documento;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "assinaturaDigital") //, columnDefinition = "VARBINARY(MAX)")
    private byte[] assinaturaDigital;

    private String tokenAssinatura;

    private Date dataAssinatura;

    private String gmtAssinatura;

    private String carimboTempoAssinatura;

    private String ipAssinatura;

    @Enumerated(EnumType.STRING)
    private TipoAssinaturaEnum tipoAssinatura;

    private String serialTokenCertificado;

    @JsonBackReference(value = "contratoPartePapel")
    public ContratoPartePapel getContratoPartePapel() {
        return contratoPartePapel;
    }

    @JsonBackReference(value = "documento")
    public ContratoDocumento getDocumento() {
        return documento;
    }

    @JsonBackReference(value = "usuario")
    public Usuario getUsuario() {
        return usuario;
    }


    public ContratoParteDocumento(AssinadorContratoParteDocumentoRequest assinadorContratoParteDocumentoRequest) {
        BeanUtils.copyProperties(assinadorContratoParteDocumentoRequest, this, "assinaturaDigital");
        this.assinaturaDigital = Base64.getDecoder().decode(LZString.decompressFromUTF16(assinadorContratoParteDocumentoRequest.getAssinaturaDigital()));
        this.documento = new ContratoDocumento();
        BeanUtils.copyProperties(assinadorContratoParteDocumentoRequest.getDocumento(), this.documento, "documento");

    }


}
