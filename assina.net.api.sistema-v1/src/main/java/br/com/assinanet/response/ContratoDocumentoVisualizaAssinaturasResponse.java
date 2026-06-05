package br.com.assinanet.response;


import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.enums.TipoAssinaturaEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ContratoDocumentoVisualizaAssinaturasResponse {

    private UUID id;

    private String nomeRazaoSocial;

    private String cpfCnpj;

    private Papel papel;

    private String dataAssinatura;

    private byte[] assinaturaDigital;

    private TipoAssinaturaEnum tipoAssinatura;

    private String emissorCertificado;

    private String nomeCertificado;



    private String responsavel;

    private String responsavelCPF;

    public ContratoDocumentoVisualizaAssinaturasResponse(UUID id, String nomeRazaoSocial, Papel papel,
                                                         String dataAssinatura, byte[] assinaturaDigital,
                                                         TipoAssinaturaEnum tipoAssinatura) {
        this.id = id;
        this.nomeRazaoSocial = nomeRazaoSocial;
        this.papel = papel;
        this.dataAssinatura = dataAssinatura;
        this.assinaturaDigital = assinaturaDigital;
        this.tipoAssinatura = tipoAssinatura;
    }
}
