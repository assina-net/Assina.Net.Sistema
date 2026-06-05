package br.com.assinanet.models;

import br.com.assinanet.util.CommonsUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContratoDuplicatasModel {

    String cedenteNome;

    String cedenteCNPJ;

    String cedenteInscricaoEstadual;

    String cedenteEndereco;

    String cedenteBairro;

    String cedenteCEP;

    String cedenteCidade;

    String cedenteUF;

    String cedenteTelefone;

    String duplicataTipo;

    String duplicataDataEmissao;

    String faturaNumero;

    String duplicataValor;

    String duplicataValorExtenso;

    String duplicataOrdem;

    String duplicataDataVencimento;

    String duplicataCondicoesEspeciais;

    String duplicataPracaPagamento;

    String sacadoNome;

    String sacadoCNPJ;

    String sacadoInscricaoEstadual;

    String sacadoEndereco;

    String sacadoBairro;

    String sacadoCidade;

    String sacadoCEP;

    String sacadoUF;

    private byte[] documentoDuplicatas;

    List<AssinaturaReportModel> assinaturas;

    String assinaturaEmissor;

    String dataAssinaturaEmissor;

    String tipoAssinaturaEmissor;

    String chaveAssinaturaEmissor;

    //region  GetSet
    public String getDuplicataDataEmissao() {
        if (CommonsUtil.semValor(duplicataDataEmissao)) {
            return "";
        }
        return CommonsUtil.formataData(CommonsUtil.stringToDate(duplicataDataEmissao, "ddMMyyyy"), "dd/MM/yyyy");
    }

    public String getDuplicataValor() {
        if (CommonsUtil.semValor(duplicataValor)) {
            return "";
        }
        return CommonsUtil.formataValorMonetario(CommonsUtil.doubleValue(duplicataValor) / 100d);
    }

    public double getDuplicataValorDouble() {
        if (CommonsUtil.semValor(duplicataValor)) {
            return 0;
        }
        return CommonsUtil.doubleValue(duplicataValor) / 100d;
    }

    public String getDuplicataDataVencimento() {

        if (CommonsUtil.semValor(duplicataDataVencimento)) {
            return "";
        }
        return CommonsUtil.formataData(CommonsUtil.stringToDate(duplicataDataVencimento, "ddMMyyyy"), "dd/MM/yyyy");
    }
    //endregion
}
