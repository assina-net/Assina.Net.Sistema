package br.com.assinanet.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Classe Util para trabalhar com {@link BigDecimal}
 *
 * @author Denilson Godinho
 */
public final class BigDecimalUtil {

    /**
     * Construtor privado para garantir o Singleton.
     */
    private BigDecimalUtil() {
    }

    /**
     * Retorna o calculo do iposto
     * baseCalculo / 100 * aliquota
     *
     * @param baseCalculo
     * @param aliquota
     * @return
     */
    public static BigDecimal calcularImposto(BigDecimal baseCalculo, BigDecimal aliquota) {
        return divide(baseCalculo, new BigDecimal("100")).multiply(aliquota);
    }

    /**
     * Recebe um Stream de {@link BigDecimal} e retorna a soma.
     *
     * @param stream
     * @return
     */
    public static BigDecimal somar(Stream<BigDecimal> stream) {
        return stream.reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Efetua a divisão dos valores setando o scale para 4
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divide(BigDecimal v1, BigDecimal v2) {
        /*se o divisor for zero retorna zero porque é impossivel dividir por zero.*/
        if (isValorZero(v2)) return BigDecimal.ZERO;
        return v1.divide(v2, 4, RoundingMode.HALF_UP);
    }

    /**
     * Efetua a divisão dos valores setando o scale para o scale passado
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divide(BigDecimal v1, BigDecimal v2, int scale) {
        /*se o divisor for zero retorna zero porque é impossivel dividir por zero.*/
        if (isValorZero(v2)) return BigDecimal.ZERO;
        return v1.divide(v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * Efetua a multiplicação entre os valores informados
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal multiplicar(BigDecimal v1, BigDecimal v2) {
        return v1.multiply(v2).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a porcentagem de um valor em cima de outro
     * Ex: valor1 = 100, valor2 = 10 a porcentagem de 10 em cima de 100 é = 10
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static BigDecimal calcularPorcentagemValor(BigDecimal valor1, BigDecimal valor2) {
        return divide(valor2.multiply(new BigDecimal("100")), valor1);
    }

    /**
     * Calcula a porcenta em cima do valor
     *
     * @param valor
     * @param porcentagem
     * @return
     */
    public static BigDecimal calcularPorcentagem(BigDecimal valor, BigDecimal porcentagem) {
        return divide(porcentagem, new BigDecimal("100")).multiply(valor);
    }

    /**
     * Retorna true se o bigDecimal informado é != de nulo e > que 0
     *
     * @param valor
     * @return
     */
    public static boolean isNotNullEMaiorQZero(BigDecimal valor) {
        return !Util.isEmpty(valor) && maiorQZero(valor);
    }

    /**
     * Retorna true se o valor em {@link BigDecimal} informado for diferente de nulo e
     * menor que zero
     *
     * @param valor
     * @return
     */
    public static boolean isNotNullEMenorQZero(BigDecimal valor) {
        return !Util.isEmpty(valor) && menorQZero(valor);
    }

    /**
     * Verifica se o valor passado em BigDecimal é maior que zero.
     *
     * @param valor
     * @return
     */
    public static boolean maiorQZero(BigDecimal valor) {
        return valor.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Retorna true se o valor passado em {@link BigDecimal} for menor que zero
     *
     * @param valor
     * @return
     */
    public static boolean menorQZero(BigDecimal valor) {
        return valor.compareTo(BigDecimal.ZERO) == -1;
    }

    /**
     * Retorna true se o valor for igual a zero
     *
     * @param valor
     * @return
     */
    public static boolean isValorZero(BigDecimal valor) {
        return isValoresIguais(valor, BigDecimal.ZERO);
    }

    /**
     * Retorna true se os dois valores passados em BigDecimal forem iguais,
     * false se forem diferentes.
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static boolean isValoresIguais(BigDecimal valor1, BigDecimal valor2) {
        return valor1.compareTo(valor2) == 0;
    }

    /**
     * Retorna true se o valor1 for maior que o valor2
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static boolean isValor1MaiorQValor2(BigDecimal valor1, BigDecimal valor2) {
        return valor1.compareTo(valor2) == 1;
    }

    /**
     * Retorna true se o valor1 for menor que o valor2
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static boolean isValor1MenorQValor2(BigDecimal valor1, BigDecimal valor2) {
        return valor1.compareTo(valor2) == -1;
    }

    /**
     * Retorna true se o valor 1 for maior ou igual ao valor 2
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static boolean isValor1MaiorOuIgualQValor2(BigDecimal valor1, BigDecimal valor2) {
        return isValor1MaiorQValor2(valor1, valor2) || isValoresIguais(valor1, valor2);
    }

    /**
     * Retorna true se o valor 1 for menor ou igual ao valor 2
     *
     * @param valor1
     * @param valor2
     * @return
     */
    public static boolean isValor1MenorOuIgualQValor2(BigDecimal valor1, BigDecimal valor2) {
        return isValor1MenorQValor2(valor1, valor2) || isValoresIguais(valor1, valor2);
    }

    /**
     * Retorna o proprio valor ou zero se for nulo
     *
     * @param valor
     * @return
     */
    public static BigDecimal valorOuZeroSeNull(BigDecimal valor) {
        return Util.isEmpty(valor) ? BigDecimal.ZERO : valor;
    }

    /**
     * Formata decimal no padrao pt_br
     *
     * @param valor
     * @return
     */
    public static String formatarDecimal(BigDecimal valor) {
        NumberFormat nf = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        if (!Util.isEmpty(valor)) {
            double doubleValue = valor.doubleValue();
            return nf.format(doubleValue);
        } else {
            return "0,00";
        }
    }

    /**
     * Retorna uma String do valor bigdecimal sem casas decimais
     *
     * @param valor
     * @return
     */
    public static String bigDecimalSemCasas(BigDecimal valor) {
        return formatarCasasDecimais(valor, 0);
    }

    /**
     * Retorna uma String do valor bigdecimal com 1 casas decimais
     *
     * @param valor
     * @return
     */
    public static String bigDecimal1Casas(BigDecimal valor) {
        return formatarCasasDecimais(valor, 1);
    }

    /**
     * Retorna uma String do valor bigdecimal com 2 casas decimais
     *
     * @param valor
     * @return
     */
    public static String bigDecimal2Casas(BigDecimal valor) {
        return formatarCasasDecimais(valor, 2);
    }

    /**
     * Retorna uma String do valor bigdecimal com 3 casas decimais
     *
     * @param valor
     * @return
     */
    public static String bigDecimal3Casas(BigDecimal valor) {
        return formatarCasasDecimais(valor, 3);
    }

    /**
     * Retorna uma String do valor bigdecimal com 4 casas decimais
     *
     * @param valor
     * @return
     */
    public static String bigDecimal4Casas(BigDecimal valor) {
        return formatarCasasDecimais(valor, 4);
    }


    /**
     * Retorna uma String do valor BigDecimal formatando o numero de casas decimais
     * de acordo com o scale informado.
     *
     * @param valor
     * @param scale
     * @return
     */
    private static String formatarCasasDecimais(BigDecimal valor, int scale) {
        if (!Util.isEmpty(valor)) {
            return valor.setScale(scale, RoundingMode.HALF_UP).toString();
        } else {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP).toString();
        }
    }

    /**
     * Retorna o {@link BigDecimal} passado formatando as casas decimais
     * conforme o scale informado.
     *
     * @param valor
     * @param scale
     * @return
     */
    public static BigDecimal formatarDecimal(BigDecimal valor, int scale) {
        if (Util.isEmpty(valor)) return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        return valor.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Formata o valor passado para o padrão de moeda pt_br
     *
     * @param valor
     * @param casasDecimais
     * @return
     */
    public static String formatarMoeda(BigDecimal valor, int casasDecimais) {
        DecimalFormat formato = new DecimalFormat("##,###,###,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        formato.setMinimumFractionDigits(casasDecimais);
        formato.setParseBigDecimal(true);
        return formato.format(valor);
    }

    /**
     * Converte o valor em {@link String} para {@link BigDecimal}
     * Obs: se o valor em {@link String} for nulo ou vazio retorna um {@link BigDecimal} com valor zero.
     *
     * @param valor
     * @return
     */
    public static BigDecimal converterStringToBigDecimal(String valor) {
        if (Util.isEmpty(valor)) return BigDecimal.ZERO;
        return new BigDecimal(valor);
    }

    /**
     * Formata o bigDecimal para o formato de preço da automação
     *
     * @param valor
     * @return
     */
    public static String formatarPrecoAutomacao(BigDecimal valor) {
        String val = valor.toString().replace(".", "");
        return Util.completarZerosADireita(Integer.valueOf(val), 4);
    }

    /**
     * Converte o valor para negativo
     *
     * @param valor
     * @return
     */
    public static BigDecimal converterParaNegativo(BigDecimal valor) {
        return valorOuZeroSeNull(valor).multiply(new BigDecimal(-1));
    }

    /**
     * Converte o valor para positivo se negativo
     *
     * @param valor
     * @return
     */
    public static BigDecimal converterParaPositivoSeNegativo(BigDecimal valor) {
        if (menorQZero(valor)) return valor.negate();
        return valor;
    }

    /**
     * Converte o valor para negativo se positivo
     *
     * @param valor
     * @return
     */
    public static BigDecimal converterParaNegativoSePositivo(BigDecimal valor) {
        if (maiorQZero(valor)) return converterParaNegativo(valor);
        return valor;
    }

}
