package br.com.assinanet.request.assinador;

import br.com.assinanet.entity.ContratoPartePapel;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.TipoAssinaturaEnum;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class AssinadorContratoParteDocumentoRequest {

    private UUID id;

    private ContratoPartePapel contratoPartePapel;

    private Usuario usuario;

    private AssinadorContratoDocumentoRequest documento;

    private String assinaturaDigital;

    private String tokenAssinatura;

    private Date dataAssinatura;

    private String gmtAssinatura;

    private String carimboTempoAssinatura;

    private String ipAssinatura;

    @Enumerated(EnumType.STRING)
    private TipoAssinaturaEnum tipoAssinatura;

    private String serialTokenCertificado;

}
