package br.com.assinanet.util;


import br.com.assinanet.entity.exception.NegocioException;
import br.com.assinanet.models.CarimboTempoModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * @author Samuel Oliveira
 */

@Slf4j
public final class DataUtil {

    /**
     * Construtor privado para garantir o Singleton.
     */
    private DataUtil() {
    }


    /**
     * Retorna a String da data De Acordo com o formato dd/MM/yyyy
     *
     * @param data, formato
     * @return
     */
    public static String localDataToString(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return data.format(formatter);
    }

    /**
     * Retorna a String da data De Acordo com o formato "dd/MM/yyyy HH:mm"
     *
     * @param data, formato
     * @return
     */
    public static String formatarDataHora(LocalDateTime data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return data.format(formatter);
    }

    /**
     * Retorna a String da data no padrão MM/yyyy.
     *
     * @return
     */
    public static String getDataPadraoMMyyyy(LocalDate data) {
        return data.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    /**
     * Retorna a String da data atual no padrão MM/yyyy.
     *
     * @return
     */
    public static String getDataPadraoMMyyyy() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return now.format(formatter);
    }

    /**
     * Retorna o LocalDateTime da String no Formato yyyy-MM-dd'T'HH:mm:ss
     *
     * @param data, formato
     * @return
     */
    public static LocalDateTime StringToLocalDateTime(String data) {
        return LocalDateTime.parse(data);
    }

    /**
     * Recebe uma String nos formatos (ddMMYYYY ou yyyyMMdd) e retorna uma intancia de
     * {@link LocalDate}
     *
     * @param data
     * @return
     */
    public static LocalDate getStringToLocalDate(String data) {
        String isDate_ddMMyyyy = "\\d{2}[\\/\\-]\\d{2}[\\/\\-]\\d{4}";
        String isDate_yyyyMMdd = "\\d{4}[\\/\\-]\\d{2}[\\/\\-]\\d{2}";
        if (data.matches(isDate_ddMMyyyy)) {
            String[] arr = data.split("[\\/\\-]");
            return LocalDate.of(Integer.valueOf(arr[2]), Integer.valueOf(arr[1]), Integer.valueOf(arr[0]));
        } else if (data.matches(isDate_yyyyMMdd)) {
            String[] arr = data.split("[\\/\\-]");
            return LocalDate.of(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]));
        }
        return null;
    }

    /**
     * Este metodo recebe uma string no formato ddMMYY e converte para local date.
     * Ex: para obter uma instancia de local date da data 01/07/2016 o parametro passado deve ser '010716' sem barras('/')
     *
     * @param data
     * @return
     */
    public static LocalDate formataStringPadraoDDMMYYParaLocalDate(String data) {
        try {
            String sb = data.substring(0, 2) +
                    "/" +
                    data.substring(2, 4) +
                    "/" +
                    data.substring(4, 6);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            Date dataParse = sdf.parse(sb);

            Instant instant = Instant.ofEpochMilli(dataParse.getTime());
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();

            return localDate;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Receb uma string no formado yyyyMMdd e converte para localDate.
     * Ex: para obter uma instancia de local date da data 18/07/2016 o parametro passado deve ser '20160718' sem barras('/')
     *
     * @param data
     * @return
     */
    public static LocalDate formataStringPadraoYYYYMMDDParaLocalDate(String data) {
        try {
            int year = Integer.parseInt(data.substring(0, 4));
            int month = Integer.parseInt(data.substring(4, 6));
            int dayOfMonth = Integer.parseInt(data.substring(6, 8));

            return LocalDate.of(year, month, dayOfMonth);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retorna a Data FOrmatada para layout de Backup
     *
     * @return
     */
    public static String getDataFormatadaBackup() {
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH.mm");

        return LocalDateTime.now().format(formatoData);
    }

    /**
     * Receb uma string no formado MMyyy e converte para localDate.
     * Ex: para obter uma instancia de local date da data 01/03/2017 o parametro passado deve ser '032017' sem barras('/')
     *
     * @param data
     * @return
     */
    public static LocalDate formataStringPadraoMMyyyyParaLocalDate(String data) {
        try {
            int month = Integer.parseInt(data.substring(0, 2));
            int year = Integer.parseInt(data.substring(2, 6));
            int dayOfMonth = 1;
            return LocalDate.of(year, month, dayOfMonth);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retorna a Data do primeiro dia do mes corrente
     *
     * @return
     */
    public static LocalDate obterPrimeiroDiaDoMesCorrente() {
        LocalDate now = LocalDate.now();
        LocalDate primeiroDia = now.with(TemporalAdjusters.firstDayOfMonth());

        return primeiroDia;
    }

    /**
     * Retorna o primeiro dia do Mes da data informada.
     *
     * @param data
     * @return
     */
    public static LocalDate obterPrimeiroDiaDaData(LocalDate data) {
        return LocalDate.of(data.getYear(), data.getMonthValue(), 1);
    }

    /**
     * Retorna a data do ultimo dia do mês corrente
     *
     * @return
     */
    public static LocalDate obterUltimoDiaDoMesCorrente() {
        LocalDate now = LocalDate.now();
        LocalDate ultimoDia = now.with(TemporalAdjusters.lastDayOfMonth());

        return ultimoDia;
    }




    /**
     * Retorna a data do ultimo dia da data informada.
     *
     * @param data
     * @return
     */
    public static LocalDate obterUltimoDiaDoMes(LocalDate data) {
        return data.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Retorna a Data do Ultimo dia do mês anterior ao mes corrente
     *
     * @return
     */
    public static LocalDate obterUltimoDiaDoMesAnterior() {
        LocalDate now = LocalDate.now();
        LocalDate ultimoDiaDoMesAnterior = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        return ultimoDiaDoMesAnterior;
    }

    /**
     * Retorna a Data do Primeiro dia do mês anterior ao mes corrente.
     *
     * @return
     */
    public static LocalDate obterPrimeiroDiaDoMesAnterior() {
        LocalDate now = LocalDate.now();
        LocalDate primeiroDiaDoMesAnterior = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        return primeiroDiaDoMesAnterior;
    }

    /**
     * Calcula a diferença de horas entre a data passada e a data atual
     *
     * @param data
     * @return
     */
    public static Long calcularDiferencaHoras(LocalDateTime data) {
        LocalDateTime agora = LocalDateTime.now();
        return ChronoUnit.HOURS.between(data, agora);
    }

    /**
     * Retorna a data no formado da NFE
     *
     * @param dataASerFormatada
     * @return
     * @throws DatatypeConfigurationException
     * @throws NegocioException
     */
    public static String dataNfe(LocalDateTime dataASerFormatada) throws NegocioException {

        try {
            GregorianCalendar calendar = GregorianCalendar.from(Optional.ofNullable(dataASerFormatada).orElse(LocalDateTime.now()).atZone(ZoneId.of("Brazil/East")));
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            xmlCalendar.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);

            return (xmlCalendar.toString());

        } catch (DatatypeConfigurationException e) {
            throw new NegocioException(e);
        }
    }

    /**
     * Retorna a data da nfe informado convertida para LocalDateTime
     *
     * @param dataNfe
     * @return
     */
    public static LocalDateTime dataNfeToLocalDateTime(String dataNfe) {
        return LocalDateTime.parse(dataNfe.substring(0, 19));
    }

    /**
     * Recebe uma String e faz o parse para LocalDate, se o parse não for possivel retorna null.
     *
     * @param data
     * @return
     */
    public static LocalDate dataNfeToLocalDate(String data) {
        try {
            String regex = "\\d{4}-\\d{2}-\\d{2}";
            boolean isFormatado = data.matches(regex); // verifica se a string esta no formato yyyy-MM-dd
            if (!isFormatado) {
                data = data.substring(0, 10);
                isFormatado = data.matches(regex);
            }
            if (isFormatado) {
                return LocalDate.parse(data);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retorna o ano corrente com 2 digitos
     * Ex: ano 2016 - retorno 16
     *
     * @return
     */
    public static String getAnoCorrente2Digitos() {
        LocalDate now = LocalDate.now();
        return String.valueOf(now.getYear()).substring(2, 4);
    }

    /**
     * Retorna a diferença em dias entre duas datas
     *
     * @param dataIni
     * @param dataFim
     * @return
     */
    public static long difEntreDatasEmDias(LocalDate dataIni, LocalDate dataFim) {
        long diferencaEmDias = ChronoUnit.DAYS.between(dataIni, dataFim);
        return diferencaEmDias;
    }

    /**
     * Retorna a diferença em segundos entre duas datas
     *
     * @param dataIni
     * @param dataFim
     * @return
     */
    public static long difEntreDatasEmSegundos(LocalDateTime dataIni, LocalDateTime dataFim) {
        return ChronoUnit.SECONDS.between(dataIni, dataFim);
    }

    /**
     * Retorna a diferença em minutos entre duas datas
     *
     * @param dataIni
     * @param dataFim
     * @return
     */
    public static long difEntreDatasEmMinutos(LocalDateTime dataIni, LocalDateTime dataFim) {
        return ChronoUnit.MINUTES.between(dataIni, dataFim);
    }

    /**
     * Adiciona a quantidade de dias passados na data informada.
     *
     * @param data
     * @param dias
     * @return
     */
    public static LocalDate adicionarDiasData(LocalDate data, long dias) {
        return data.plusDays(dias);
    }

    /**
     * Adiciona a quantidade de dias passados na data atual
     *
     * @param dias
     * @return
     */
    public static LocalDate adicionarDiasDataAtual(long dias) {
        return LocalDate.now().plusDays(dias);
    }

    /**
     * Retrocede a quantidade de dias passados na data informada.
     *
     * @param data
     * @param dias
     * @return
     */
    public static LocalDate retrocederDiasData(LocalDate data, long dias) {
        return data.minusDays(dias);
    }

    /**
     * Retrocede a quantidade de meses passados na data informada.
     *
     * @param data
     * @param mes
     * @return
     */
    public static LocalDate retrocederMesesData(LocalDate data, long mes) {
        return data.minusMonths(mes);
    }

    /**
     * Retrocede a quantidade de anos passados na data informada.
     *
     * @param data
     * @param anos
     * @return
     */
    public static LocalDate retrocederAnosData(LocalDate data, long anos) {
        return data.minusYears(anos);
    }

    /**
     * Retorna um {@link LocalDate} com a data atual retrocedida dos
     * dias passados como parametro
     *
     * @param dias
     * @return
     */
    public static LocalDate retrocederDiasDataAtual(long dias) {
        return LocalDate.now().minusDays(dias);
    }

    /**
     * Retorna um {@link LocalDateTime} com a data atual retrocedida dos
     * dias passados como parametro
     *
     * @param dias
     * @return
     */
    public static LocalDateTime retrocederDiasDataHoraAtual(long dias) {
        return obterInicioDoDia(retrocederDiasDataAtual(dias));
    }

    /**
     * Converte LocalDate para Date
     *
     * @param localDate
     * @return
     */
    public static Date localDateAsDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converte {@link LocalDateTime} para {@link Date}
     *
     * @param data
     * @return
     */
    public static Date localDateTimeAsDate(LocalDateTime data) {
        return localDateAsDate(data.toLocalDate());
    }

    /**
     * Recebe um objeto {@link Date} e Converte para {@link LocalDate}
     *
     * @param data
     * @return
     */
    public static LocalDate dateAsLocalDate(Date data) {
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    /**
     * Recebe um objeto {@link Date} e Converte para {@link LocalDateTime}
     *
     * @param data
     * @return
     */
    public static LocalDateTime dateAsLocalDateTime(Date data) {
        LocalTime tempo = LocalTime.of(data.getHours(), data.getMinutes(), data.getSeconds());
        LocalDateTime dateTimeLocal = LocalDateTime.of(dateAsLocalDate(data), tempo);
        return dateTimeLocal;
    }

    /**
     * Retorna um localDateTima do inicio do dia da data informada
     * Ex: data informada: '2016-11-17' retorno: 2016-11-17T00:00:00
     *
     * @param data
     * @return
     */
    public static LocalDateTime obterInicioDoDia(LocalDate data) {
        LocalTime tempo = LocalTime.of(00, 00, 00);
        LocalDateTime comecoDoDia = LocalDateTime.of(data, tempo);
        return comecoDoDia;
    }

    /**
     * Retorna um localDateTima do fim do dia da data informada
     * Ex: data informada: '2016-11-17' retorno: 2016-11-17T23:59:59
     *
     * @param data
     * @return
     */
    public static LocalDateTime obterFimDoDia(LocalDate data) {
        LocalTime tempo = LocalTime.of(23, 59, 59);
        LocalDateTime ultimoInstanteDoDia = LocalDateTime.of(data, tempo);
        return ultimoInstanteDoDia;
    }

    /**
     * Retorna um LocalDateTime criado com a data e a hora informados.
     *
     * @param data
     * @param horas
     * @param minutos
     * @return
     */
    public static LocalDateTime criarDataComHoras(LocalDate data, int horas, int minutos, int segundos) {
        return LocalDateTime.of(data, LocalTime.of(horas == 24 ? 23 : horas, minutos, segundos));
    }

    /**
     * Cria local time a partir da hora e minuto
     * Ex: parametro 1741
     *
     * @param time
     * @return
     */
    public static LocalTime criarLocalTimeHoraMinuto(String time) {
        return LocalTime.of(Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(2, 4)), 59);
    }

    /**
     * Retorna true se a string no padrão localTime for maior que zero horas
     *
     * @param time
     * @return
     */
    public static boolean isLocalTimeMaiorQZeroHoras(String time) {
        return !Util.isEmpty(time) && !time.equals("0000");
    }

    /**
     * Retorna LocalTime formatado padrão HH:mm
     *
     * @param time
     * @return
     */
    public static String formatarLocalTimePadraoHHmm(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Formata a hora
     *
     * @param hora
     * @return
     */
    public static String formatarHora(String hora) {
        return formatarLocalTimePadraoHHmm(criarLocalTimeHoraMinuto(hora));
    }

    /**
     * Retorna um LocalDateTime pela data e time informados
     *
     * @param data
     * @param time
     * @return
     */
    public static LocalDateTime criarLocalDateTime(LocalDate data, LocalTime time) {
        return LocalDateTime.of(data, time);
    }

    /**
     * Cria um localDateTime de acordo com os parametro informados.
     *
     * @param dia
     * @param mes
     * @param hora
     * @param minuto
     * @return
     */
    public static LocalDateTime criarLocalDateTime(String dia, String mes, String hora, String minuto) {
        int anoCorrente = LocalDate.now().getYear();
        return LocalDateTime.of(anoCorrente, Integer.valueOf(mes), Integer.valueOf(dia), Integer.valueOf(hora), Integer.valueOf(minuto));
    }

    /**
     * Retorna um LocalDateTime com a data informada e a hora atual do sistema.
     *
     * @param data
     * @return
     */
    public static LocalDateTime criarLocalDateTime(LocalDate data) {
        return criarLocalDateTime(data, LocalTime.now());
    }

    /**
     * Converte LocalDateTime para java.sql.Timestamp
     *
     * @param data
     * @return
     */
    public static Timestamp localDateTimeToTimestamp(LocalDateTime data) {
        return Timestamp.valueOf(data);
    }

    /**
     * Retorna true se a data informada for menor ou igual a data de comparacao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMenorOuIgual(LocalDateTime data, LocalDateTime dataComparacao) {
        return data.isBefore(dataComparacao) || data.isEqual(dataComparacao);
    }

    /**
     * Retorna true se a data informada for maior ou igual a data de compracao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMaiorOuIgual(LocalDateTime data, LocalDateTime dataComparacao) {
        return data.isAfter(dataComparacao) || data.isEqual(dataComparacao);
    }

    /**
     * Retorna true se a hora informada for maior ou igual a hora de comparação
     *
     * @param hora
     * @param horaComparacao
     * @return
     */
    public static boolean isHoraMaiorOuIgual(LocalTime hora, LocalTime horaComparacao) {
        return hora.isAfter(horaComparacao) || hora.equals(horaComparacao);
    }

    /**
     * Retorna true se a hora informada for menor ou igual a hora de comparação
     *
     * @param hora
     * @param horaComparacao
     * @return
     */
    public static boolean isHoraMenorOuIgual(LocalTime hora, LocalTime horaComparacao) {
        return hora.isBefore(horaComparacao) || hora.equals(horaComparacao);
    }

    /**
     * Retorna true se a data informada for menor que a data de comparacao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMenor(LocalDateTime data, LocalDateTime dataComparacao) {
        return data.isBefore(dataComparacao);
    }

    /**
     * Retorna true se a data informada for menor ou igual a data de comparacao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMenorOuIgual(LocalDate data, LocalDate dataComparacao) {
        return data.isBefore(dataComparacao) || data.isEqual(dataComparacao);
    }

    /**
     * Retorna true se a data informada for maior ou igual a data de compracao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMaiorOuIgual(LocalDate data, LocalDate dataComparacao) {
        return data.isAfter(dataComparacao) || data.isEqual(dataComparacao);
    }

    /**
     * Retorna true se a data informada for maior que a data de compracao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMaior(LocalDate data, LocalDate dataComparacao) {
        return data.isAfter(dataComparacao);
    }

    /**
     * Retorna true se a data informada for menor que a data de compracao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataMenor(LocalDate data, LocalDate dataComparacao) {
        return data.isBefore(dataComparacao);
    }

    /**
     * Retorna true se a data informada for igual a data de compracao
     *
     * @param data
     * @param dataComparacao
     * @return
     */
    public static boolean isDataIgual(LocalDate data, LocalDate dataComparacao) {
        return data.isEqual(dataComparacao);
    }

    /**
     * Retorna o resultado da comparacao entre datas.
     *
     * @param data:       a data da compracao
     * @param dataInicio: a data inicial
     * @param dataFim:    a data final
     * @return
     */
    public static boolean filtrarIntervaloData(LocalDateTime data, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return isDataMaiorOuIgual(data, dataInicio) &&
                isDataMenorOuIgual(data, dataFim);
    }

    /**
     * Retorna o resultado da comparacao entre datas.
     *
     * @param data:       a data da compracao
     * @param dataInicio: a data inicial
     * @param dataFim:    a data final
     * @return
     */
    public static boolean filtrarIntervaloData(LocalDate data, LocalDate dataInicio, LocalDate dataFim) {
        return isDataMaiorOuIgual(data, dataInicio) &&
                isDataMenorOuIgual(data, dataFim);
    }

    /**
     * Retorna o resultado da comparação entre datas.
     *
     * @param hora
     * @param horaInicio
     * @param horaFim
     * @return
     */
    public static boolean filtrarIntervaloHora(LocalTime hora, LocalTime horaInicio, LocalTime horaFim) {
        return isHoraMaiorOuIgual(hora, horaInicio) &&
                isHoraMenorOuIgual(hora, horaFim);
    }

    /**
     * Retorna o nome do Mês
     *
     * @param data
     * @return
     */
    public static String getNomeMes(LocalDate data) {
        switch (data.getMonthValue()) {
            case 1:
                return "JANEIRO";
            case 2:
                return "FEVEREIRO";
            case 3:
                return "MARÇO";
            case 4:
                return "ABRIL";
            case 5:
                return "MAIO";
            case 6:
                return "JUNHO";
            case 7:
                return "JULHO";
            case 8:
                return "AGOSTO";
            case 9:
                return "SETEMBRO";
            case 10:
                return "OUTUBRO";
            case 11:
                return "NOVEMBRO";
            case 12:
                return "DEZEMBRO";

            default:
                return "";
        }
    }


    public static ZonedDateTime getCarimboTempo1() {
        ZonedDateTime time = null;
        LocalDateTime timeUTC = null;

        List<String> servidores = new ArrayList<>(0);
        servidores.add("a.st1.ntp.br");
        servidores.add("b.st1.ntp.br");
        servidores.add("c.st1.ntp.br");
        servidores.add("d.st1.ntp.br");
        servidores.add("a.ntp.br");
        servidores.add("b.ntp.br");
        servidores.add("c.ntp.br");
        servidores.add("gps.ntp.br");

        for (String ntpServer : servidores) {
            try {
                NTPUDPClient timeClient = new NTPUDPClient();
                InetAddress inetAddress = InetAddress.getByName(ntpServer);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);
                timeInfo.computeDetails();
                long actualTime = timeInfo.getReturnTime() + timeInfo.getOffset();
                timeUTC = LocalDateTime.ofEpochSecond(actualTime / 1000, (int) (actualTime % 1000) * 1000000, ZoneOffset.UTC);
                break;
            } catch (Exception ex) {
                continue;
            }
        }

        time = timeUTC.atZone(ZoneId.of(CommonsUtil.TIME_ZONE));

        return time;
    }

    public static CarimboTempoModel getCarimboTempo() {
        CarimboTempoModel time = new CarimboTempoModel();

        List<String> servidores = new ArrayList<>(0);
        //servidores.add("a.st1.ntp.br");
        //servidores.add("b.st1.ntp.br");
        //servidores.add("c.st1.ntp.br");
        //servidores.add("d.st1.ntp.br");
        //servidores.add("a.ntp.br");
        //servidores.add("b.ntp.br");
        //servidores.add("c.ntp.br");
        //servidores.add("gps.ntp.br");
        servidores.add("pool.ntp.org");
        servidores.add("time.google.com");
        servidores.add("time.windows.com");

        for (String ntpServer : servidores) {
            try {
                //log.info("NTP SERVIDOR: " + ntpServer + " inicio ");
                NTPUDPClient timeClient = new NTPUDPClient();
                timeClient.open();
                timeClient.setSoTimeout(500);
                InetAddress inetAddress = InetAddress.getByName(ntpServer);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);
                long returnTime = timeInfo.getReturnTime();
                time.setDataCarimboTempo(new Date(returnTime));
                //log.info("NTP SERVIDOR: " + ntpServer + " fim ");
                break;
            } catch (Exception ex) {
                log.error("NTP SERVIDOR: " + ntpServer + " error:  " + ex.getMessage()  );
                ex.printStackTrace();
                continue;
            }
        }

        if (CommonsUtil.semValor(time.getDataCarimboTempo())) {
            time.setDataCarimboTempo(new Date());
            log.info("NTP Hora Local");
        }

        if (!CommonsUtil.semValor(time.getDataCarimboTempo())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("XXX");
            dateFormat.setTimeZone(TimeZone.getDefault());
            time.setGmtCarimboTempo(dateFormat.format(time.getDataCarimboTempo()));

            dateFormat = new SimpleDateFormat("dd/MM/yyyy' as 'HH:mm:ss.SSS' (GMT 'XXX')'");
            dateFormat.setTimeZone(TimeZone.getDefault());
            time.setCarimboTempo(dateFormat.format(time.getDataCarimboTempo()));
        }
        return time;
    }

}