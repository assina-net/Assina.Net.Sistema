package br.com.assinanet.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ValidationUtil {

    public static final int VALIDATION_UTIL_PESSOA_FISICA = 1;
    public static final int VALIDATION_UTIL_PESSOA_JURIDICA = 2;


    /**
     * Logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(ValidationUtil.class);

    public static final boolean validaCpfCnpj(final String cpfCnpj) {
        final String cpfCnpjNumero = CommonsUtil.somenteNumeros(cpfCnpj);
        boolean result = false;
        if (cpfCnpjNumero.length() == 14) {
            result = validaCnpj(cpfCnpjNumero);
        }
        if (cpfCnpjNumero.length() == 11) {
            result = validaCpf(cpfCnpjNumero);
        }
        return result;
    }

    public static final boolean validaCnpj(final String cpfCnpj) {
        return getCnpjCpf(VALIDATION_UTIL_PESSOA_JURIDICA, cpfCnpj, false) != null;
    }

    public static final boolean validaCpf(final String cpfCnpj) {
        return getCnpjCpf(VALIDATION_UTIL_PESSOA_FISICA, cpfCnpj, false) != null;
    }

    public static String getCnpjCpf(final String txtIdentificacaoCliente) {
        return getCnpjCpf(null, txtIdentificacaoCliente, false);
    }

    public static String getCnpjCpf(final Integer tipoInscricao,
                                    final String txtIdentificacaoCliente) {
        return getCnpjCpf(tipoInscricao, txtIdentificacaoCliente, false);
    }


    private static final Set<String> CPF_CNPJ_INVALIDOS;

    static {
        final Set<String> tmp = new HashSet<String>(0);
//        tmp.add("00000000000");
//        tmp.add("11111111111");
//        tmp.add("22222222222");
//        tmp.add("33333333333");
//        tmp.add("44444444444");
//        tmp.add("55555555555");
//        tmp.add("66666666666");
//        tmp.add("77777777777");
//        tmp.add("88888888888");
//        tmp.add("99999999999");
//        tmp.add("00000000000000");
        CPF_CNPJ_INVALIDOS = Collections.unmodifiableSet(tmp);
    }

    /**
     * @param tipoInscricao           Pessoa física (1) ou jurídica (2), ou NULL para qualquer
     * @param txtIdentificacaoCliente
     * @return
     */
    public static String getCnpjCpf(final Integer tipoInscricao,
                                    final String txtIdentificacaoCliente,
                                    final boolean isPermiteCalcularDigito) {
        String result = null;
        if (txtIdentificacaoCliente != null) {
            // Verifica se aceita CNPJ e/ou CPF
            final boolean isPermiteCPF = tipoInscricao == null
                    || tipoInscricao.intValue() == 1;
            final boolean isPermiteCNPJ = tipoInscricao == null
                    || tipoInscricao.intValue() == 2;
            // Remove brancos
            String cnpjCpf = CommonsUtil.removeEspacos(txtIdentificacaoCliente);
            cnpjCpf = CommonsUtil.somenteNumeros(cnpjCpf);
            switch (cnpjCpf.length()) {
                case 13:
                    cnpjCpf = "0" + cnpjCpf;
                    // passa para teste com 14 digitos
                case 14:
                    // CNPJ: 88888888444499 char(14)
                    if (isPermiteCNPJ) {
                        result = verificaDigitoVericacaoCnpj(cnpjCpf);
                    }
                    if (result == null && isPermiteCPF) {
                        if (txtIdentificacaoCliente.startsWith("000")) {
                            // CPF: 000 88888888899 char(11)
                            result = verificaDigitoVericacaoCpf(cnpjCpf
                                    .substring(3));
                        } else if (txtIdentificacaoCliente.endsWith("000")) {
                            // CPF: 88888888899 000 char(11)
                            result = verificaDigitoVericacaoCpf(cnpjCpf.substring(
                                    0, 11));
                        }
                    }
                    break;
                case 12:
                    // CNPJ: 888888889999 char(12) e calcular dígitos
                    if (isPermiteCNPJ) {
                        if (isPermiteCalcularDigito) {
                            result = calculaDigitoVericacaoCnpj(cnpjCpf);
                        } else {
                            result = verificaDigitoVericacaoCnpj("00" + cnpjCpf);
                        }
                    }
                    if (result == null && isPermiteCPF) {
                        if (txtIdentificacaoCliente.startsWith("0")) {
                            // CPF: 0 88888888899 char(11)
                            result = verificaDigitoVericacaoCpf(cnpjCpf
                                    .substring(1));
                        } else if (txtIdentificacaoCliente.endsWith("0")) {
                            // CPF: 88888888899 0 char(11)
                            result = verificaDigitoVericacaoCpf(cnpjCpf.substring(
                                    0, 11));
                        }
                    }
                    break;
                case 11:
                    // CPF: 88888888899 char(11) ou CNPJ: 000 88888888899 char(11)
                    if (isPermiteCPF) {
                        result = verificaDigitoVericacaoCpf(cnpjCpf);
                    }
                    if (result == null && isPermiteCNPJ) {
                        result = verificaDigitoVericacaoCnpj("000" + cnpjCpf);
                    }
                    break;
                case 10:
                    // CPF: 0 88888888899 char(10) ou CNPJ: 0000 8888888899 char(10)
                    if (isPermiteCPF) {
                        result = verificaDigitoVericacaoCpf("0" + cnpjCpf);
                    }
                    if (result == null && isPermiteCNPJ) {
                        result = verificaDigitoVericacaoCnpj("0000" + cnpjCpf);
                    }
                    break;
                case 9:
                    // CPF: 888888888 char(9) e calcular dígitos
                    if (isPermiteCPF && isPermiteCalcularDigito) {
                        result = calculaDigitoVericacaoCpf(cnpjCpf);
                    }
                    break;
                case 8:
                    // CNPJ: 88888888 char(8) completar com 0001 e calcular dígitos
                    if (isPermiteCNPJ && isPermiteCalcularDigito) {
                        result = calculaDigitoVericacaoCnpj(cnpjCpf + "0001");
                    }
                    break;
                default:
                    if (cnpjCpf.length() > 14) {
                        final int len = cnpjCpf.length();
                        // Verifica CNPJ completo no final
                        if (isPermiteCNPJ) {
                            String cnpjCpfTmp = cnpjCpf.substring(len - 14);
                            if (verificaDigitoVericacaoCnpj(cnpjCpfTmp) != null) {
                                if (allZeros(cnpjCpf.substring(0, len - 14))) {
                                    result = cnpjCpfTmp;
                                    break;
                                }
                            }
                        }
                        // Verifica CNPJ completo no inicio
                        if (isPermiteCNPJ) {
                            String cnpjCpfTmp = cnpjCpf.substring(0, 14);
                            if (verificaDigitoVericacaoCnpj(cnpjCpfTmp) != null) {
                                if (allZeros(cnpjCpf.substring(14))) {
                                    result = cnpjCpfTmp;
                                    break;
                                }
                            }
                        }
                        // Verifica CPF completo no final
                        if (isPermiteCPF) {
                            String cnpjCpfTmp = cnpjCpf.substring(len - 11);
                            if (verificaDigitoVericacaoCpf(cnpjCpfTmp) != null) {
                                if (allZeros(cnpjCpf.substring(0, len - 11))) {
                                    result = cnpjCpfTmp;
                                    break;
                                }
                            }
                        }
                        // Verifica CPF completo no inicio
                        if (isPermiteCPF) {
                            String cnpjCpfTmp = cnpjCpf.substring(0, 11);
                            if (verificaDigitoVericacaoCpf(cnpjCpfTmp) != null) {
                                if (allZeros(cnpjCpf.substring(11))) {
                                    result = cnpjCpfTmp;
                                    break;
                                }
                            }
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Validação falhou para: cnpjCpf=[" + cnpjCpf
                                + "]");
                    }
            }
        }
        if (CPF_CNPJ_INVALIDOS.contains(result)) {
            result = null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getCnpjCpf: tipoInscricao[" + tipoInscricao
                    + "] txt[" + txtIdentificacaoCliente + "] result[" + result
                    + "]");
        }
        return result;
    }

    private static boolean allZeros(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private static String verificaDigitoVericacaoCpf(String cpf) {
        return cpf != null && cpf.equals(calculaDigitoVericacaoCpf(cpf)) ? cpf
                : null;
    }

    private static String verificaDigitoVericacaoCnpj(String cnpj) {
        return cnpj != null && cnpj.equals(calculaDigitoVericacaoCnpj(cnpj)) ? cnpj
                : null;
    }

    private static String calculaDigitoVericacaoCpf(String cpf) {
        if (cpf.length() == 11) {
            cpf = cpf.substring(0, 9);
        } else if (cpf.length() != 9) {
            return null;
        }
        char[] digitos = cpf.toCharArray();
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digitos[i] - '0') * pesosCpf1[i];
        }
        int resto = sum % 11;
        int digito1 = resto < 2 ? 0 : (11 - resto);
        sum = digito1 * pesosCpf2[9];
        for (int i = 0; i < 9; i++) {
            sum += (digitos[i] - '0') * pesosCpf2[i];
        }
        int resto2 = sum % 11;
        int digito2 = resto2 < 2 ? 0 : (11 - resto2);
        return cpf + (char) (digito1 + '0') + (char) (digito2 + '0');
    }

    final static int[] pesosCnpj1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    final static int[] pesosCnpj2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    final static int[] pesosCpf1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    final static int[] pesosCpf2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    private static String calculaDigitoVericacaoCnpj(String cnpj) {
        if (cnpj.length() == 14) {
            cnpj = cnpj.substring(0, 12);
        } else if (cnpj.length() != 12) {
            return null;
        }
        char[] digitos = cnpj.toCharArray();
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (digitos[i] - '0') * pesosCnpj1[i];
        }
        int resto = sum % 11;
        int digito1 = resto < 2 ? 0 : (11 - resto);
        sum = digito1 * pesosCnpj2[12];
        for (int i = 0; i < 12; i++) {
            sum += (digitos[i] - '0') * pesosCnpj2[i];
        }
        int resto2 = sum % 11;
        int digito2 = resto2 < 2 ? 0 : (11 - resto2);
        return cnpj + (char) (digito1 + '0') + (char) (digito2 + '0');
    }

    public static final boolean validaEmail(final String email) {
        if (CommonsUtil.semValor(email)) {
            return false;
        }

        EmailValidator validator = EmailValidator.getInstance();

        return validator.isValid(email);
    }

    public static boolean validaTXTouPDF(byte[] documento) {
        boolean ehPDF = true;
        boolean ehTXT = true;
        ehPDF = validaPDF(documento);
        ehTXT = validaTXT(documento);

        return ehPDF || ehTXT;

    }

    public static boolean validaTXT(byte[] documento) {
        //testando txt
        for (Byte bdoc : documento) {
            if (bdoc > 0x00 && bdoc < 0x09) {
                return false;
            }
        }
        return true;
    }

    public static boolean validaPDF(byte[] documento) {
        //testando pdf
        try {
            PDDocument doc =  PDDocument.load(documento);
            doc.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
