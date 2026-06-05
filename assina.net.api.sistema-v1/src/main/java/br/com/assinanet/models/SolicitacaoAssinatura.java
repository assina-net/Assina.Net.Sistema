package br.com.assinanet.models;

import br.com.swconsultoria.certificado.Certificado;

public class SolicitacaoAssinatura {
    private byte[] arquivoPdf;
    private Certificado certificado;
    private String nomeAssinatura;
    private String localAssinatura;
    private String motivoAssinatura;
    private String serialToken;

    public byte[] getArquivoPdf() {
        return arquivoPdf;
    }

    public void setArquivoPdf(byte[] arquivoPdf) {
        this.arquivoPdf = arquivoPdf;
    }

    public Certificado getCertificado() {
        return certificado;
    }

    public void setCertificado(Certificado certificado) {
        this.certificado = certificado;
    }

    public String getNomeAssinatura() {
        return nomeAssinatura;
    }

    public void setNomeAssinatura(String nomeAssinatura) {
        this.nomeAssinatura = nomeAssinatura;
    }

    public String getLocalAssinatura() {
        return localAssinatura;
    }

    public void setLocalAssinatura(String localAssinatura) {
        this.localAssinatura = localAssinatura;
    }

    public String getMotivoAssinatura() {
        return motivoAssinatura;
    }

    public void setMotivoAssinatura(String motivoAssinatura) {
        this.motivoAssinatura = motivoAssinatura;
    }

    public String getSerialToken() {
        return serialToken;
    }

    public void setSerialToken(String serialToken) {
        this.serialToken = serialToken;
    }
}
