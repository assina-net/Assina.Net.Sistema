package br.com.assinanet.models;

import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.enums.TipoAssinaturaEnum;
import br.com.swconsultoria.pdf_signature.dom.AssinaturaModel;
import lombok.Getter;
import lombok.Setter;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;

@Getter
@Setter
public class AssinaturaExtendsModel extends AssinaturaModel {

    private Papel papel;

    private String email;

    X509Certificate certificadoX509;

    PrivateKey privateKey;

    Calendar signDate;

    CarimboTempoModel carimboTempo;

    private String ip;

    byte[] assinatura;

    String chaveAssinatura;

    private TipoAssinaturaEnum tipoAssinatura;

    private String nomePessoaJuridica;

    private String cnpjPessoaJuridica;
    public AssinaturaExtendsModel() {
    }

}
