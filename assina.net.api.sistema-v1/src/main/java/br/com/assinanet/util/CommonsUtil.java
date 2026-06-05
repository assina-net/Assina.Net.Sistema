package br.com.assinanet.util;


import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.models.ContratoModel;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.w3c.dom.NodeList;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;

public class CommonsUtil {

    @Autowired
    private static Environment env;


    /**
     * Rotinas utilitarias gerais
     */


    /**
     * Logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(CommonsUtil.class);
    public static final String TIME_ZONE = "America/Sao_Paulo";

    public static Double castAsDouble(Object value) {
        return value == null ? null : doubleValue(value);
    }

    public static Integer castAsInteger(Object value) {
        return value == null ? null : intValue(value);
    }

    public static Long castAsLong(Object value) {
        return value == null ? null : longValue(value);
    }

    public static boolean booleanValue(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Integer) {
            return ((Integer) object == 1);
        }

        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        }
        return Boolean.parseBoolean(object.toString());
    }

    public static final Integer integerValue(Object value) {
        return value == null ? null
                : (value instanceof Integer) ? (Integer) value
                : intValue(value);
    }

    public static int intValue(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return Integer.parseInt(object.toString());
    }

    public static int intValue(Object object, int defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return Integer.parseInt(object.toString());
    }

    public static long longValue(Object object) {
        if (object == null) {
            return 0L;
        }
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }
        return Long.parseLong(object.toString());
    }

    public static double doubleValue(Object object) {
        if (object == null) {
            return 0d;
        }
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        if (object instanceof String) {
            if (((String) object).isEmpty())
                return 0d;
        }
        return Double.parseDouble(object.toString());
    }

    public static UUID UUIDValue(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof byte[]) {
            return UUID.nameUUIDFromBytes((byte[]) object);
        }
        if (object instanceof String) {
            if (((String) object).isEmpty())
                return null;

            return UUID.fromString((String) object);
        }
        return null;
    }


    public static String stringValue(byte[]  object) {
        return object == null ? null :new String(object, StandardCharsets.UTF_8);
    }

    public static String stringValue(Object object) {
        return object == null ? null : object.toString();
    }

    public static String stringValueVazio(Object object) {
        return object == null ? "" : object.toString();
    }

    public static char charValue(Character object) {
        return object == null ? (char) 0 : object.charValue();
    }

    public static Character charValue(String object) {
        Character objChr = null;
        if (!semValor(object)) {
            char[] charArray = object.toCharArray();
            objChr = charArray[0];
        }
        return objChr;
    }

    public static short shortValue(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).shortValue();
        }
        return Short.parseShort(object.toString());
    }

    /**
     * Ajusta valor para valor monetário (arredondado com 2 casas decimais)
     *
     * @param valor
     * @return
     */
    public static final double valorMonetario(Object valor) {
        return getDecimalRound(doubleValue(valor), 2);
    }

    /**
     * Ajusta valor para valor monetário (arredondado com 2 casas decimais)
     *
     * @param valor
     * @return
     */
    public static final double valorMonetario(Double valor) {
        return getDecimalRound(doubleValue(valor), 2);
    }

    /**
     * Ajusta valor para valor monetário (arredondado com 2 casas decimais)
     *
     * @param valor
     * @return
     */
    public static final double valorMonetario(double valor) {
        return getDecimalRound(valor, 2);
    }

    /**
     * Ajusta valor para valor monetário (arredondado com 2 casas decimais)
     *
     * @param valor
     * @return
     */
    public static final double valorMonetarioNaoNegativo(double valor) {
        return valor <= 0d ? 0d : getDecimalRound(valor, 2);
    }

    /**
     * Arredonda um número para uma determinada quantidade de casas decimais
     *
     * @param value
     * @param decimals
     * @return número arredondado
     */
    public static double getDecimalRound(final double value, final int decimals) {
        final BigDecimal valorExato = new BigDecimal(value).setScale(decimals,
                RoundingMode.HALF_UP);
        return valorExato.doubleValue();
    }

    public static final int compare(Date o1, Date o2) {
        if (o1 != null) {
            if ( o2 == null){
                return 1;
            }else{
            return o1.compareTo(o2);
            }
        }
        if (o2 != null) {
            if ( o1 == null){
                return 1;
            }else {
                return o2.compareTo(o1);
            }
        }
        return 0;
    }

    public static final int compare(String o1, String o2) {
        if (o1 != null) {
            return o1.compareTo(o2);
        }
        if (o2 != null) {
            return o2.compareTo(o1);
        }
        return 0;
    }

    public static final int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null ) {
            return 0;
        }
        if (o1 != null) {
            return 1;
        }
        if (o2 != null) {
            return 0;
        }
        return 0;
    }


    public static final int compare(Double o1, Double o2) {
        if (o1 != null) {
            return o1.compareTo(o2);
        }
        if (o2 != null) {
            return o2.compareTo(o1);
        }
        return 0;
    }

    public static final int compare(int o1, int o2) {
        return o1 - o2;
    }

    public static final boolean mesmoValor(Number a, Number b) {
        return Objects.equals(a, b);
    }

    public static final boolean mesmoValor(String a, String b) {
        return Objects.equals(a, b);
    }

    public static final boolean mesmoValor(UUID a, UUID b) {
        return Objects.equals(a, b);
    }
    public static final boolean mesmoValor(Cliente a, Cliente b) {
        return a == null ? b == null : mesmoValor(a.getId(), b.getId());
    }

    public static final boolean mesmoValor(Character a, Character b) {
        return Objects.equals(a, b);
    }

    public static final boolean mesmoValor(Date a, Date b) {
        return Objects.equals(a, b);
    }

    public static boolean semValor(Object value) {
        return value == null;
    }

    public static boolean semValor(Cliente value) {
        return value == null || CommonsUtil.semValor(value.getId());
    }

    public static boolean semValor(Usuario value) {
        return value == null || CommonsUtil.semValor(value.getId());
    }


    public static boolean semValor(Pessoa value) {
        return value == null || CommonsUtil.semValor(value.getId());
    }

    public static boolean semValor(NodeList value) {
        return value == null || value.getLength() == 0;
    }


    public static boolean semValor(StringBuilder value) {
        return value == null || value.length() == 0;
    }

    public static final boolean semValor(Integer value) {
        return value == null || value.intValue() == 0;
    }

    public static final boolean semValor(Character value) {
        return value == null || value.charValue() == 0;
    }

    public static final boolean semValor(Double value) {
        return value == null || value.doubleValue() == 0d;
    }

    public static final boolean semValor(Date value) {
        return value == null;
    }

    /**
     * Verifica se a string é nula ou vazia.
     *
     * @param value
     * @return
     */
    public static final boolean semValor(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final boolean semValorNossoNumero(final String nossoNumero) {
        try {
            return semValor(nossoNumero) || intValue(nossoNumero.trim()) == 0;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    /**
     * Verifica se uma coleção é nula ou está vazia.
     *
     * @param value
     * @return
     */
    public static final boolean semValor(Collection<?> value) {
        return value == null || value.isEmpty();
    }

    public static final boolean semValor(Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    public static final <T> boolean semValor(T[] value) {
        return value == null || value.length <= 0;
    }

    public static final byte[] toByteArray(List<String> contents) {
        final StringBuilder c = new StringBuilder();
        if (contents != null) {
            for (String line : contents) {
                c.append(line).append("\r\n");
            }
        }
        final String s = c.toString();
        if (s == null || s.isEmpty()) {
            return new byte[0];
        }
        return s.getBytes();
    }

    public static final InputStream streamData(List<String> contents) {
        InputStream result = null;
        if (contents != null && !contents.isEmpty()) {
            result = new ByteArrayInputStream(toByteArray(contents));
        }
        return result;
    }

    /**
     * Formatador geral para valores monetários
     */
    public static final DecimalFormatSymbols PT_BR_SYMBOLS;

    static {
        PT_BR_SYMBOLS = new DecimalFormatSymbols(new Locale("pt_BR"));
        PT_BR_SYMBOLS.setDecimalSeparator(',');
        PT_BR_SYMBOLS.setMonetaryDecimalSeparator(',');
        PT_BR_SYMBOLS.setGroupingSeparator('.');
        PT_BR_SYMBOLS.setCurrencySymbol("R$");
    }

    public static final String formataNumero(Number numero, String formato) {
        final NumberFormat formatador = new DecimalFormat(formato,
                PT_BR_SYMBOLS);
        return formatador.format(numero);
    }

    public static final String formataCEP(String cep) {
        if (cep == null) {
            return null;
        }
        switch (cep.length()) {
            case 8:
                return cep.substring(0, 2) + "." + cep.substring(2, 5) + "-"
                        + cep.substring(5);
            case 5:
                return cep.substring(0, 2) + "." + cep.substring(2);
        }
        return cep;
    }

    public static final String formataHora(String hora) {
        if (hora == null) {
            return null;
        }
        switch (hora.length()) {
            case 6:
                return hora.substring(0, 2) + ":" + hora.substring(2, 4) + ":"
                        + hora.substring(4);
            case 4:
                return hora.substring(0, 2) + ":" + hora.substring(2);
        }
        return hora;
    }

    public static final String formataValorMonetario(Number valor) {
        return formataNumero(valor, "#,##0.00");
    }

    /**
     * Formata a data no padrão dd/MM/yyyy
     *
     * @param data
     * @return
     */
    public static final String formataData(Date data) {
        String result = "";
        if (data == null) {
            return result;
        }
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        result = df.format(data);
        return result;
    }

    public static final String formataData(Date data, String pattern) {
        String result = "";
        if (data == null) {
            return result;
        }
        DateFormat df = new SimpleDateFormat(pattern);
        result = df.format(data);
        return result;
    }

    public static byte[] readFully(InputStream stream) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static byte[] loadFile(String sourcePath) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourcePath);
            return readFully(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static final String formataTextoTamanhoArquivo(Number tamanho) {
        if (tamanho != null) {
            long bytes = tamanho.longValue();
            if (bytes < 0L) {
                return "";
            } else if (bytes < KILOBYTE) {
                return bytes + " B";
            } else if (bytes < MEGABYTE) {
                return (bytes / KILOBYTE) + " KB";
            } else if (bytes < GIGABYTE) {
                return (bytes / MEGABYTE) + " MB";
            } else if (bytes < TERABYTE) {
                return (bytes / GIGABYTE) + " GB";
            } else {
                return (bytes / TERABYTE) + " TB";
            }
        }
        return "";
    }

    private static final Long KILOBYTE = Long.valueOf(1024L);
    private static final Long MEGABYTE = Long.valueOf(1024L * 1024L);
    private static final Long GIGABYTE = Long.valueOf(1024L * 1024L * 1024L);
    private static final Long TERABYTE = Long.valueOf(1024L * 1024L * 1024L * 1024L);

    public static final String trimNull(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    public static final Double zeroAsNull(double valor) {
        return valor == 0d ? null : valor;
    }

    public static final Double zeroAsNull(Double valor) {
        return valor == null || valor.doubleValue() == 0d ? null : valor;
    }

    public static final byte[] carregaBytesArquivo(final File file) {
        byte[] result = null;
        {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("carregaBytesArquivo: carregando bytes de ["
                        + file.getPath() + "]");
            }
            try {
                final FileInputStream fin = new FileInputStream(file);
                try {
                    result = new byte[(int) file.length()];
                    fin.read(result);
                } catch (IOException e) {
                    LOGGER.error("carregaBytesArquivo: IOException:"
                            + e.getMessage());
                    result = null;
                }
                try {
                    fin.close();
                } catch (IOException ignored) {
                    LOGGER.warn("carregaBytesArquivo: IOException (ignorado):"
                            + ignored.getMessage());
                    ignored.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("carregaBytesArquivo: FileNotFoundException:"
                        + e.getMessage());
            }
        }
        return result;
    }

    public static final <T> List<T> getList(T valor) {
        final List<T> lista = new ArrayList<T>();
        lista.add(valor);
        return lista;
    }

    public static final <T> String[] getListStringToArray(Collection<T> valores) {
        String[] itemsArray = new String[valores.size()];
        itemsArray = valores.toArray(itemsArray);
        return itemsArray;
    }


    public static final <T> List<T> toList(Collection<T> valores) {
        if (valores == null) {
            return null;
        }
        final List<T> lista = new ArrayList<T>();
        lista.addAll(valores);
        return lista;
    }


    public static final <T> List<T> enumToList(T[] valores) {
        List<T> lstEnum = new ArrayList<T>(0);
        Collections.addAll(lstEnum, valores);
        return lstEnum;
    }

    public static final <T> List<T> merge(List<T> destino, List<T> novos) {
        if (destino == null) {
            return novos;
        }

        if (novos != null) {
            destino.addAll(novos);
        }

        return destino;

    }

    public static final boolean isEmailValido(final String email) {
        return !CommonsUtil.semValor(email) && email.contains("@");
    }

    /**
     * net.assina.leitorXML.CommonsUtil#ajustaTamanhoString(java.lang.String, int, boolean, java.lang.String)
     * sem o parametro filler
     *
     * @param original
     * @param tamanho
     * @param ajustaDireita
     * @return
     */

    public static final String ajustaTamanhoString(final String original,
                                                   final int tamanho, final boolean ajustaDireita) {
        return ajustaTamanhoString(original, tamanho, ajustaDireita, null);
    }

    /**
     * Ajusta tamanho de um String Se String maior que tamanho - Se ajusta a
     * Direita: pega do final do String até o tamanho - Se ajusta a Esquerda:
     * pega do inicio do String até o tamanho Se String menor que tamanho - Se
     * ajusta a Direita: Mantém original a direita, e completa com Filler até o
     * tamanho - Se ajusta a Esquerda: Mantém original a esquerda, e completa
     * com Filler até o tamanho Se String mesmo tamanho que original, retorna
     * original.
     *
     * @param original
     * @param tamanho
     * @param ajustaDireita
     * @param filler
     * @return
     */
    public static final String ajustaTamanhoString(String original,
                                                   final int tamanho, final boolean ajustaDireita, String filler) {

        if (original == null) {
            original = "";
        }

        if (original.length() == tamanho) {
            return original;
        }

        if (original.length() > tamanho) {
            if (ajustaDireita) {
                return original.substring(original.length() - tamanho);
            } else {
                return original.substring(0, tamanho);
            }
        }

        filler = semValor(filler) ? " " : filler;

        // original.length() < tamanho
        if (ajustaDireita) {
            return StringUtils.repeat(filler, tamanho - original.length())
                    + original;
        } else {
            return original
                    + StringUtils.repeat(filler, tamanho - original.length());
        }

    }

    public static final <T, Y> void simpleCopyProperties(T destiny, Y source,
                                                         String... excludedFields) throws Exception {

        try {

            Class<?> classSource = source.getClass();
            Class<?> classDestiny = destiny.getClass();

            while (classSource != null && classDestiny != null) {
                Field[] fields = classSource.getDeclaredFields();
                List<String> lstExcludedFields = new ArrayList<String>();
                lstExcludedFields.add("class");
                if (excludedFields != null) {
                    lstExcludedFields.addAll(Arrays.asList(excludedFields));
                }
                for (Field field : fields) {

                    if (!lstExcludedFields.contains(field.getName())
                            && (field.getType() == String.class
                            || field.getType() == Character.class
                            || field.getType() == Date.class
                            || field.getType() == Boolean.class
                            || field.getType() == Short.class
                            || field.getType() == Integer.class
                            || field.getType() == Long.class
                            || field.getType() == Float.class
                            || field.getType() == UUID.class
                            || field.getType() == Double.class
                            || field.getType() == BigDecimal.class)) {

                        field.setAccessible(true);

                        try {
                            Field destinyField = classDestiny
                                    .getDeclaredField(field.getName());

                            Object value = field.get(source);

                            destinyField.setAccessible(true);
                            destinyField.set(destiny, value);
                        } catch (NoSuchFieldException e) {
                            // Se não encontra campo na classe de destino,
                            // continua
                            // para o próximo campo silenciosamente.
                        }

                    }

                }

                classSource = classSource.getSuperclass();
                classDestiny = classDestiny.getSuperclass();

            }
        } catch (SecurityException e) {
            throw new Exception("simpleFieldTransfer error", e);
        } catch (IllegalArgumentException e) {
            throw new Exception("simpleFieldTransfer error", e);
        } catch (IllegalAccessException e) {
            throw new Exception("simpleFieldTransfer error", e);
        }

    }

    public static final String formataCnpjCpf(String cnpjCpf,
                                              boolean isUsarPrefixos) {
        String result = "";
        if (cnpjCpf != null) {
            switch (cnpjCpf.length()) {
                case 11:
                    result = (isUsarPrefixos ? "CPF " : "")
                            + cnpjCpf.substring(0, 3) + "."
                            + cnpjCpf.substring(3, 6) + "."
                            + cnpjCpf.substring(6, 9) + "-" + cnpjCpf.substring(9);
                    break;
                case 14:
                    result = (isUsarPrefixos ? "CNPJ " : "")
                            + cnpjCpf.substring(0, 2) + "."
                            + cnpjCpf.substring(2, 5) + "."
                            + cnpjCpf.substring(5, 8) + "/"
                            + cnpjCpf.substring(8, 12) + "-"
                            + cnpjCpf.substring(12);
                    break;
                default:
                    result = cnpjCpf;
                    break;
            }
        }
        return result;
    }

    /* Retorna uma string com 0 na esquerda */
    public static final String strZero(String sVar, int iTamanho) {
        String sAux;
        String sRet;

        // Tirando espaços da variavel
        if (sVar != null) {
            sAux = sVar.trim();
        } else {
            sAux = "";
        }
        sRet = sAux;

        // Completando com zeros a esquerda
        for (int iCon = 0; iCon < ((iTamanho - sAux.length())); iCon++) {
            sRet = '0' + sRet;
        }

        // Retorna..
        return sRet;
    }

    /* Retorna uma string com 0 na esquerda */
    public static final String strZeroDireita(String sVar, int iTamanho) {
        String sAux;
        String sRet;

        // Tirando espaços da variavel
        if (sVar != null) {
            sAux = sVar.trim();
        } else {
            sAux = "";
        }
        sRet = sAux;

        // Completando com zeros a esquerda
        for (int iCon = 0; iCon < ((iTamanho - sAux.length())); iCon++) {
            sRet = sRet + '0';
        }

        // Retorna..
        return sRet;
    }


    public static String removeAcentos(String str) {

        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;

    }

    public static final String removeEspacos(String s) {
        return s == null ? null : s.replace(Character.toString((char) 160), "")
                .replaceAll("\\s+", "");
    }

    public static final String somenteNumeros(String s) {
        return s == null ? null : s.replaceAll("[^\\d]", "");
    }

    public static final String removeCaracteresInvalidos(String s) {

        if (s == null) {
            return null;
        }

        if ("".equals(s)) {
            return "";
        }

        StringBuilder sb = new StringBuilder(s);

        for (int i = 0; i < s.length(); i++) {

            int code = sb.charAt(i);

            if ((code < 32 && code != '\n' && code != '\r') || code > 254) {
                sb.setCharAt(i, ' ');
            }
        }

        return sb.toString();
    }

    public static boolean eSomenteNumero(String s) {
        String sNumeros = somenteNumeros(s);

        return s.equals(sNumeros);

    }

    public static boolean eUUID(String s) {
        try {
            return UUIDValue(s) != null;
        } catch (Exception e) {
            return false;
        }
    }


    public static String montaCMC7(Integer numeroBancoCheque,
                                   Integer codigoCompensacaoCheque, String codigoAgenciaCheque,
                                   String codigoContaCorrenteCheque, String numeroCheque) {

        if (CommonsUtil.semValor(codigoCompensacaoCheque)) {
            codigoCompensacaoCheque = 18;
        }

        String C1 = calculaDigitoVerificador11(strZero(
                stringValue(numeroBancoCheque), 3)
                + strZero(stringValue(codigoAgenciaCheque), 4));
        String C2 = calculaDigitoVerificador11(strZero(
                stringValue(codigoCompensacaoCheque), 3)
                + strZero(stringValue(numeroCheque), 6) + "5");
        String C3 = calculaDigitoVerificador11(strZero(
                stringValue(codigoContaCorrenteCheque), 10));

        String sCmc7 = strZero(stringValue(numeroBancoCheque), 3);
        sCmc7 += strZero(stringValue(codigoAgenciaCheque), 4);
        sCmc7 += C2;
        sCmc7 += strZero(stringValue(codigoCompensacaoCheque), 3);
        sCmc7 += strZero(stringValue(numeroCheque), 6);
        sCmc7 += "5";
        sCmc7 += C1;
        sCmc7 += strZero(stringValue(codigoContaCorrenteCheque), 10);
        sCmc7 += C3;

        // vo.setCodigoCompensacaoCheque(CommonsUtil.intValue(vo.getCmc7()
        // .substring(10, 12)));
        // vo.setNumeroDocumento(vo.getCmc7().substring(13, 19));
        // vo.setNumeroBancoCheque(CommonsUtil.intValue(vo.getCmc7().substring(1,
        // 4)));
        // vo.setCodigoAgenciaCheque(vo.getCmc7().substring(4, 8));
        // vo.setCodigoContaCorrenteCheque(vo.getCmc7().substring(25, 31));

        return sCmc7;

    }

    public static Boolean validaCMC7(String codigoCmc7Cheque) {
        String sCmc7 = somenteNumeros(codigoCmc7Cheque);
        if (mesmoValor(30, sCmc7.length())) {

            String C1 = calculaDigitoVerificador11(sCmc7.substring(0, 7));
            String C2 = calculaDigitoVerificador11(sCmc7.substring(8, 18));
            String C3 = calculaDigitoVerificador11(sCmc7.substring(19, 29));

            return mesmoValor(C1, sCmc7.substring(18, 19))
                    && mesmoValor(C2, sCmc7.substring(7, 8))
                    && mesmoValor(C3, sCmc7.substring(29, 30));

        }

        return false;

    }

    private static String calculaDigitoVerificador11(String numero) {

        char[] digitos = numero.toCharArray();
        int sum = 0;

        ArrayList<Integer> fator = new ArrayList<Integer>();

        int ifator = 0;
        int resto = 0;

        String digito = "";

        for (int i = 2; i >= 1; i--) {
            fator.add(i);
        }

        for (int i = numero.length() - 1; i >= 0; i--) {

            int multi = (digitos[i] - '0') * fator.get(ifator);

            char[] multiDigitos = String.valueOf(multi).toCharArray();

            for (int j = multiDigitos.length - 1; j >= 0; j--) {
                sum += (multiDigitos[j] - '0');
            }

            if (ifator == (fator.size() - 1)) {
                ifator = 0;
            } else {
                ifator += 1;
            }
        }

        resto = sum % 10;

        int digito1 = (10 - resto);
        if (digito1 > 9) {
            digito = "0";
        } else {
            digito = "" + digito1;
        }

        return (digito + "0").substring(0, 1);

    }


    /**
     * Retorna o sistema operacional que o sistema está rodando
     * Util para definir os locais de arquivos em ambiente Windows e Linux
     * Bonatte: 06/03/2015
     * retorna true se for WINDOWS e false se for LINUX
     */
    public static final boolean sistemaWindows() {
        String sistemaOperacional = System.getProperty("os.name").toUpperCase();
        boolean result = sistemaOperacional.contains("WINDOWS");
        //System.out.println("Rodando em Windows: "+sistemaOperacional);
        return result;
    }

    public static final String formataMesAno(String mesAno) {
        if (mesAno == null) {
            mesAno = "";
        }
        if (!mesAno.isEmpty() && (CommonsUtil.eSomenteNumero(mesAno))) {
            Integer mes = Integer.parseInt(mesAno.substring(0, 2));
            Integer mesPosicao = ((mes - 1) * 3);

            String ano = mesAno.substring(2, 6);

            String meses = "JanFevMarAbrMaiJunJulAgoSetOutNovDez";
            String mesExtenso = meses.substring(mesPosicao, mesPosicao + 3);

            mesAno = mesExtenso + "/" + ano;
        }
        return mesAno;
    }

    public static final String formataMesAnoFull(String mesAno) {
        if (mesAno == null) {
            mesAno = "";
        }
        if (!mesAno.isEmpty() && (CommonsUtil.eSomenteNumero(mesAno))) {
            Integer mes = Integer.parseInt(mesAno.substring(0, 2));
            Integer mesPosicao = ((mes - 1) * 9);

            String ano = mesAno.substring(2, 6);

            String meses = "Janeiro  FevereiroMarço    Abril    Maio     Junho    Julho    Agosto   Setembro Outubro  Novembro Dezembro ";
            String mesExtenso = meses.substring(mesPosicao, mesPosicao + 9);

            mesAno = mesExtenso.trim() + "/" + ano;
        }
        return mesAno;
    }

    public static String formataAnoMes(String anoMes) {
        if (anoMes == null) {
            anoMes = "";
        }
        if (!CommonsUtil.semValor(anoMes)) {
            String mesAno = anoMes.substring(2, 4) + anoMes.substring(0, 2);
            anoMes = formataMesAno(mesAno);
        }
        return anoMes;
    }

    public static String retornaTipoPessoa(String cpf_cnpj) {
        String tipoPessoa = "";
        if (cpf_cnpj.trim().length() == 11) {
            tipoPessoa = "F";
        } else {
            tipoPessoa = "J";
        }
        return tipoPessoa;
    }

//	public static String retornaProduto(String cmc7) {
//		String retorno = "";
//		if (!CommonsUtil.semValor(cmc7))
//			retorno = "PR";
//		else
//			retorno = "PP";
//		return retorno;
//	}

    public static String retornaSituacaoCpfCnpj(String cpfCnpj, String codigoSituacao) {
        String descricaoSituacao = "";
        if (CommonsUtil.mesmoValor(retornaTipoPessoa(cpfCnpj), "F")) {
            descricaoSituacao = retornaSituacaoCPF(codigoSituacao);
        } else {
            descricaoSituacao = retornaSituacaoCNPJ(codigoSituacao);
        }
        return descricaoSituacao;
    }

    public static String retornaSituacaoCPF(String codigoSituacao) {
        String descricaoSituacao = "";
        switch (codigoSituacao) {
            case "2":
                descricaoSituacao = "REGULAR";
                break;
            case "3":
                descricaoSituacao = "PENDENTE DE REGULARIZAÇÃO";
                break;
            case "6":
                descricaoSituacao = "SUSPENSA";
                break;
            case "9":
                descricaoSituacao = "CANCELADA";
                break;
            case "4":
                descricaoSituacao = "NULA";
                break;
        }
        return descricaoSituacao;
    }

    public static String retornaSituacaoCNPJ(String codigoSituacao) {
        String descricaoSituacao = "";
        switch (codigoSituacao) {
            case "2":
                descricaoSituacao = "ATIVA";
                break;
            case "6":
                descricaoSituacao = "SUSPENSA";
                break;
            case "0":
                descricaoSituacao = "INÁPTA";
                break;
            case "7":
                descricaoSituacao = "BAIXADA";
                break;
            case "4":
                descricaoSituacao = "NULA";
                break;
        }
        return descricaoSituacao;
    }


    public static Date stringToDate(String data, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date date = formatter.parse(data);
            return date;
        } catch (ParseException e) {
            return null;
        }

    }


    public static String jaxbObjectToXML(Object objeto) {
        try {

            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String xml = xmlMapper.writeValueAsString(objeto);

            return xml;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object jaxbXMLToObject(String xml, Object t) {
        try {

            Class<?> classObjeto = t.getClass();

            XmlMapper xmlMapper = new XmlMapper();
            ContratoModel value = xmlMapper.readValue(xml, ContratoModel.class);
            return value;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static final <T, Y> void simpleCopyCSVProperties(T destiny, CSVRecord source,
                                                            String... csvFields) throws Exception {

        try {

//            Class<?> classSource = source.getClass();
            Class<?> classDestiny = destiny.getClass();

            while (source != null && csvFields != null && classDestiny != null) {
                List<String> lstCsvFields = new ArrayList<String>();
                lstCsvFields.addAll(Arrays.asList(csvFields));

                for (String field : lstCsvFields) {

//                    if (!lstExcludedFields.contains(field.getName())
//                            && (field.getType() == String.class
//                            || field.getType() == Character.class
//                            || field.getType() == Date.class
//                            || field.getType() == Boolean.class
//                            || field.getType() == Short.class
//                            || field.getType() == Integer.class
//                            || field.getType() == Long.class
//                            || field.getType() == Float.class
//                            || field.getType() == Double.class || field
//                            .getType() == BigDecimal.class)) {

                    // field.setAccessible(true);

                    try {
                        Field destinyField = classDestiny
                                .getDeclaredField(field);

                        Object value = source.get(lstCsvFields.indexOf(field));

                        destinyField.setAccessible(true);
                        destinyField.set(destiny, value);
                    } catch (NoSuchFieldException e) {
                        // Se não encontra campo na classe de destino,
                        // continua
                        // para o próximo campo silenciosamente.
                    }

//                    }

                }
                classDestiny = classDestiny.getSuperclass();

            }
        } catch (SecurityException e) {
            throw new Exception("simpleFieldTransfer error", e);
        } catch (IllegalArgumentException e) {
            throw new Exception("simpleFieldTransfer error", e);
        } catch (IllegalAccessException e) {
            throw new Exception("simpleFieldTransfer error", e);
        }

    }


    public static final <T, Y> String SubstituiCamposChaves(String texto, Y source,
                                                            String... excludedFields) throws Exception {

        String retorno = texto;
        try {

            Class<?> classSource = source.getClass();

            while (classSource != null && texto != null) {
                Field[] fields = classSource.getDeclaredFields();
                List<String> lstExcludedFields = new ArrayList<String>();
                lstExcludedFields.add("class");
                if (excludedFields != null) {
                    lstExcludedFields.addAll(Arrays.asList(excludedFields));
                }
                for (Field field : fields) {

                    if (!lstExcludedFields.contains(field.getName())
                            && (field.getType() == String.class
                            || field.getType() == Character.class
                            || field.getType() == Date.class
                            || field.getType() == Boolean.class
                            || field.getType() == Short.class
                            || field.getType() == Integer.class
                            || field.getType() == Long.class
                            || field.getType() == Float.class
                            || field.getType() == Double.class || field
                            .getType() == BigDecimal.class)) {

                        field.setAccessible(true);

                        try {

                            Object value = field.get(source);
                            String strValue = value.toString();
                            retorno = retorno.replaceAll("<#" + field.getName() + ">", strValue);


                        } catch (Exception e) {
                            // Se não encontra campo na classe de destino,
                            // continua
                            // para o próximo campo silenciosamente.
                        }

                    }

                }

                classSource = classSource.getSuperclass();

            }
        } catch (SecurityException e) {
            throw new Exception("simpleFieldTransfer error", e);
        } catch (IllegalArgumentException e) {
            throw new Exception("simpleFieldTransfer error", e);
        }

        return retorno;

    }


    /**
     * Armazena valores unitários usados na conversão de valor para string
     */
    private static final String[] unidade = {"", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove",
            "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"};
    /**
     * Armazena valores de dezena usados na conversão de valor para string
     */
    private static final String[] dezena = {"", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta",
            "oitenta", "noventa"};

    /**
     * Armazena valores de centena usados na conversão de valor para string
     */
    private static final String[] centena = {"", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
            "seiscentos", "setecentos", "oitocentos", "novecentos"};

    /**
     * Armazena descrições numéricas no singular usadas na conversão de valor
     * para string
     */
    private static final String[] qualificaS = {"", "mil", "milhão", "bilhão", "trilhão"};

    /**
     * Armazena descrições numéricas no plural usadas na conversão de valor para
     * string
     */
    private static final String[] qualificaP = {"", "mil", "milhões", "bilhões", "trilhões"};


    public static final String converteValorEmExtenso(Double valor) {
        String result = "";
        if (!CommonsUtil.semValor(valor)) {
            result = processaNumeroEmTexto(valor, true, true);
            if (result != null) {
                result.toLowerCase();
            }
        }

        return result;
    }

    /**
     * Converte uma string que representa um bloco de números em seu respectivo
     * texto por extenso. Se a flag monetário for true, '1' será escrito como
     * "hum".
     */
    private static final String processaNumeroEmTexto(double vlr, boolean monetario, boolean adicionaDescMoeda) {
        if (vlr == 0)
            return ("zero");

        if (LOGGER.isDebugEnabled()) {
            System.out.print("valor recebido: " + vlr);
        }
        vlr = CommonsUtil.getDecimalRound(vlr, 2);

        if (LOGGER.isDebugEnabled()) {
            System.out.println(", valor convertido: " + vlr);
        }

        long inteiro = (long) Math.abs(vlr); // parte inteira do valor
        double resto = vlr - inteiro; // parte fracionária do valor

        String vlrS = String.valueOf(inteiro);
        if (vlrS.length() > 15)
            return ("Erro: valor superior a 999 trilhões.");

        String s = "", saux, vlrP;
        String centavos = String.valueOf((int) Math.round(resto * 100));

        // definindo o extenso da parte inteira do valor
        int n, unid, dez, cent = 0, tam, i = 0;
        boolean umReal = false, centenaOuMilhar = false;
        while (!vlrS.equals("0")) {
            tam = vlrS.length();
            // retira do valor a 1a. parte, 2a. parte, por exemplo, para
            // 123456789:
            // 1a. parte = 789 (centena)
            // 2a. parte = 456 (mil)
            // 3a. parte = 123 (milhões)
            if (tam > 3) {
                vlrP = vlrS.substring(tam - 3, tam);
                vlrS = vlrS.substring(0, tam - 3);
            } else { // última parte do valor
                vlrP = vlrS;
                vlrS = "0";
            }
            if (!vlrP.equals("000")) {
                saux = "";
                if (vlrP.equals("100"))
                    saux = "cem";
                else {
                    n = Integer.parseInt(vlrP, 10); // para n = 371, tem-se:
                    cent = n / 100; // cent = 3 (centena trezentos)
                    dez = (n % 100) / 10; // dez = 7 (dezena setenta)
                    unid = (n % 100) % 10; // unid = 1 (unidade um)
                    if (cent != 0)
                        saux = centena[cent];
                    if ((n % 100) <= 19) {
                        if (saux.length() != 0) {
                            if ((n % 100) > 0) {
                                saux = saux + " e " + getUnidade((n % 100), monetario);
                            }
                        } else
                            saux = getUnidade((n % 100), monetario);
                    } else {
                        if (saux.length() != 0) {
                            if (dez > 0) {
                                saux = saux + " e " + dezena[dez];
                            }
                        } else
                            saux = dezena[dez];
                        if (unid != 0) {
                            if (saux.length() != 0) {
                                if (unid > 0) {
                                    saux = saux + " e " + getUnidade(unid, monetario);
                                }
                            } else
                                saux = getUnidade(unid, monetario);
                        }
                    }
                }
                if (vlrP.equals("1") || vlrP.equals("001")) {
                    if (i == 0) // 1a. parte do valor (um real)
                        umReal = true;
                    else
                        saux = saux + " " + qualificaS[i];
                } else if (i != 0)
                    saux = saux + " " + qualificaP[i];
                if (s.trim().length() != 0) {
                    s = saux + " e " + s;
                } else
                    s = saux;
            }
            if (((i == 0) || (i == 1)) && s.length() != 0)
                centenaOuMilhar = true; // tem centena ou mil no valor
            i = i + 1; // próximo qualificador: 1- mil, 2- milhão, 3- bilhão,
        }

        if (s.length() != 0 && adicionaDescMoeda) {
            if (umReal)
                s = s + " real";
            else if (centenaOuMilhar)
                s = s + " reais";
            else
                s = s + " de reais";
        }

        // definindo o extenso dos centavos do valor
        if (!centavos.equals("0")) { // valor com centavos
            // se não é valor somente com centavos
            if (s.length() != 0)
                s = s + " e ";
            if (centavos.equals("1") && adicionaDescMoeda) {
                String umCentavo = "um centavo";
                if (monetario) {
                    umCentavo = "h" + umCentavo;
                }

                s = s + umCentavo;
            } else {
                n = Integer.parseInt(centavos, 10);
                if (n <= 19)
                    s = s + getUnidade(n, monetario);
                else { // para n = 37, tem-se:
                    unid = n % 10; // unid = 37 % 10 = 7 (unidade sete)
                    dez = n / 10; // dez = 37 / 10 = 3 (dezena trinta)
                    s = s + dezena[dez];
                    if (unid != 0)
                        s = s + " e " + getUnidade(unid, monetario);
                }
                if (adicionaDescMoeda) {
                    s = s + " centavos";
                }
            }
        }
        return (s);
    }

    /**
     * Retorna o valor unitário do array <CODE>unidade</CODE> usando o
     * <CODE>indice</CODE>. Controla se deve adicionar o "h" antes da palavra
     * "um" usando o parâmetro <CODE>monetario</CODE>
     *
     * @param indice
     * @param monetario
     * @return
     */
    private static String getUnidade(int indice, boolean monetario) {
        String retorno = unidade[indice];
        if (indice == 1 && monetario) {
            retorno = "h" + retorno;
        }
        return retorno;
    }


    public static final void dropOutput(InputStream in) throws IOException {
        StringBuffer line = null; // usado apenas em debug
        while (true) {
            int c = in.read();
            if (c == -1) {
                if (LOGGER.isDebugEnabled()) {
                    if (line != null) {
                        LOGGER.debug("[LP] " + line);
                    }
                }
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                if (line == null) {
                    line = new StringBuffer();
                }
                if (c == 13) {
                    LOGGER.debug("[LP] " + line);
                    line = null;
                }
                if (c >= 32) {
                    line.append((char) c);
                }
            }
            // System.err.print((char) c);
        }
    }

    /**
     * Escreve o <CODE>conteudoArquivo</CODE> no arquivo de nome
     * <CODE>nomeArquivo</CODE> com extensão <CODE>extensaoArquivo</CODE> no
     * diretório <CODE>diretorioArquivo</CODE>
     *
     * @param diretorioArquivo
     * @param nomeArquivo
     * @param extensaoArquivo
     * @param conteudoArquivo
     * @return retorna o path canônico do arquivo
     * @throws Exception
     */
    public static String escreveArquivo(String diretorioArquivo, String nomeArquivo, String extensaoArquivo,
                                        String conteudoArquivo) throws Exception {

        FileWriter writer = null;
        File file = null;
        if (diretorioArquivo == null || diretorioArquivo.isEmpty() || nomeArquivo == null || nomeArquivo.isEmpty()
                || extensaoArquivo == null || extensaoArquivo.isEmpty()) {
            throw new Exception("parâmetro inválido");
        }
        try {
            File dir = new File(diretorioArquivo);
            file = File.createTempFile(nomeArquivo, "." + extensaoArquivo, dir);

            writer = new FileWriter(file);
            writer.write(conteudoArquivo);
            writer.flush();
        } catch (Exception e) {
            throw new Exception("escreveArquivo Exception", e);
        } finally {
            fecharStream(writer);
        }

        if (file != null) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // return file.toString();
        }
        return null;
    }

    /**
     * Fecha o fluxo de um Closeable checando se este não é nulo
     *
     * @param toClose objeto a ser fechado
     */
    public static void fecharStream(Closeable toClose) {
        if (toClose != null) {
            try {
                toClose.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar Closeable: " + e);
            }
        }
    }

    //variaveis padroes para template de mensagem
    public static Map<String, Object> getVariaveisPadroes() {
        Map<String, Object> variables = new HashMap<>(0);

        CarimboTempoModel carimboTempoModel = DataUtil.getCarimboTempo();
        variables.put("dataEnvio", carimboTempoModel.getCarimboTempo());

        return variables;
    }
}


