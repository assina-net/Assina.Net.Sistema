package br.com.assinanet.Report;

import br.com.assinanet.models.AssinaturaReportModel;
import br.com.assinanet.models.ContratoDuplicatasModel;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.GsonUtil;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.*;
import java.util.*;


public class ReportDataSource {

    private static final Log LOGGER = LogFactory.getLog(ReportDataSource.class);

    public static Collection<ContratoDuplicatasModel> createBeanCollection() {
        Collection<ContratoDuplicatasModel> coll = new ArrayList<>();
        String jsonString = "[{\"cedenteNome\":\"Teste Avalista\",\"cedenteCNPJ\":\"777777777000177\",\"cedenteInscricaoEstadual\":\"\",\"cedenteEndereco\":\"Rua Zerillo Perreira Lopes\",\"cedenteBairro\":\"Pq Alto Taquaral\",\"cedenteCEP\":\"13087757\",\"cedenteCidade\":\"Campinas\",\"cedenteUF\":\"SP\",\"cedenteTelefone\":\"1999999999\",\"duplicataTipo\":\"DUPLICATA DE VENDA MERCANTIL\",\"duplicataDataEmissao\":\"29012020\",\"faturaNumero\":\"1\",\"duplicataValor\":\"1000\",\"duplicataValorExtenso\":\"dez reais\",\"duplicataOrdem\":\"1\",\"duplicataDataVencimento\":\"29102020\",\"duplicataCondicoesEspeciais\":\"\\u0026nbsp;\",\"duplicataPracaPagamento\":\"Campinas\",\"sacadoNome\":\"Alexandre\",\"sacadoCNPJ\":\"555555555000155\",\"sacadoInscricaoEstadual\":\"\",\"sacadoEndereco\":\"Rua do Sacado\",\"sacadoBairro\":\"Pq Alto Taqural\",\"sacadoCidade\":\"Campinas\",\"sacadoCEP\":\"13087757\",\"sacadoUF\":\"SP\"},{\"cedenteNome\":\"Teste Avalista\",\"cedenteCNPJ\":\"777777777000177\",\"cedenteInscricaoEstadual\":\"\",\"cedenteEndereco\":\"Rua Zerillo Perreira Lopes\",\"cedenteBairro\":\"Pq Alto Taquaral\",\"cedenteCEP\":\"13087757\",\"cedenteCidade\":\"Campinas\",\"cedenteUF\":\"SP\",\"cedenteTelefone\":\"1999999999\",\"duplicataTipo\":\"DUPLICATA DE VENDA MERCANTIL\",\"duplicataDataEmissao\":\"29012020\",\"faturaNumero\":\"1\",\"duplicataValor\":\"1000\",\"duplicataValorExtenso\":\"dez reais\",\"duplicataOrdem\":\"2\",\"duplicataDataVencimento\":\"29102020\",\"duplicataCondicoesEspeciais\":\"\\u0026nbsp;\",\"duplicataPracaPagamento\":\"Campinas\",\"sacadoNome\":\"Alexandre\",\"sacadoCNPJ\":\"555555555000155\",\"sacadoInscricaoEstadual\":\"\",\"sacadoEndereco\":\"Rua do Sacado\",\"sacadoBairro\":\"Pq Alto Taqural\",\"sacadoCidade\":\"Campinas\",\"sacadoCEP\":\"13087757\",\"sacadoUF\":\"SP\"}]";


        coll = GsonUtil.fromJson(jsonString, GsonUtil.getColletionType(new ContratoDuplicatasModel()));

        return coll;
    }

    public final byte[] geraRelatorioDuplicatasAssinadas(List<ContratoDuplicatasModel> resultados) {
        JasperReport jasperReport = BuscaRelatorioDuplicatas("rptDuplicataAssinada");
        JasperReport jasperSubReport = BuscaRelatorioDuplicatas("rptAssinaturas");
        Map<String, Object> reportParameter = new HashMap<String, Object>();
        reportParameter.put("SUB_REPORT_ASSINATURAS", jasperSubReport);

        return ProcessaRelatorio(resultados, jasperReport, reportParameter);
    }

    public final byte[] geraRelatorioDuplicatas(List<ContratoDuplicatasModel> resultados) {


        JasperReport jasperReport = BuscaRelatorioDuplicatas("rptDuplicata");
        return ProcessaRelatorio(resultados, jasperReport, null);
    }

    public final byte[] geraRelatorioAssinaturaUnica(List<AssinaturaReportModel> resultados) {
        //TODO: padrao em DOCX
        //InputStream template = this.getClass().getResourceAsStream("/templates/assinaturaPadrao.docx");
        //return geraRelatorioAssinaturaUnicaWord(resultados, template);
        return geraRelatorioAssinaturaUnica(resultados, "rptAssinaturasUnica");

    }

    public final byte[] geraRelatorioAssinaturaUnica(List<AssinaturaReportModel> resultados, String reportName) {
        JasperReport jasperReport = BuscaRelatorioDuplicatas(reportName);
        Map<String, Object> reportParameter = new HashMap<String, Object>();
        return ProcessaRelatorio(resultados, jasperReport, reportParameter);
    }

    public final byte[] geraRelatorioAssinaturaUnicaWord(List<AssinaturaReportModel> resultados, InputStream templateWord) {

        List<InputStream> inputs = new ArrayList<>();
        for (AssinaturaReportModel assinatura : resultados) {
            ByteArrayOutputStream result = null;
            try {
                result = writePdf(assinatura, templateWord);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream inputStream = new ByteArrayInputStream(result.toByteArray());
            inputs.add(inputStream);
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.addSources(inputs);
        PDFmerger.setDestinationStream(result);
        try {
            PDFmerger.mergeDocuments();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toByteArray();
    }

    private ByteArrayOutputStream writePdf(AssinaturaReportModel t, InputStream templateWord) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            //Prepara o documento
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(templateWord, TemplateEngineKind.Freemarker);

            //Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.DOCX4J);
            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.DOCX4J);

            //Adiciona propriedades para o context
            IContext ctx = report.createContext();
            ctx.put("t", t);

            //gera arquivo de saida
            report.convert(ctx, options, outputStream);
            PDDocument document = PDDocument.load(outputStream.toByteArray());
            PDPage pageDoc = document.getPage(0);

            pageDoc = pageDoc;


        } catch (Exception ex) {
            try {
                outputStream.close();
            } catch (IOException e) {
                //
            }
            throw new RuntimeException(ex);
        }
        FileOutputStream filetemp = new FileOutputStream("T:/Trabalho/Assina.Net/testes/assinatura.pdf");
        filetemp.write(outputStream.toByteArray());
        filetemp.close();

        return outputStream;
    }

    private void copy(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];

            while (true) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1)
                    break;
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final byte[] geraRelatorioAssinaturas(List<ContratoDuplicatasModel> resultados) {

        JasperReport jasperReport = BuscaRelatorioDuplicatas("rptAditivoAssinaturas");
        JasperReport jasperSubReport = BuscaRelatorioDuplicatas("rptAssinaturas");
        Map<String, Object> reportParameter = new HashMap<String, Object>();

        reportParameter.put("SUB_REPORT_ASSINATURAS", jasperSubReport);
        return ProcessaRelatorio(resultados, jasperReport, reportParameter);
    }


    public final byte[] geraRelatorioQrCode(InputStream qrCodePng, UUID contratoDocumentoId) {
        List<Integer> dataSource = new ArrayList<>(Arrays.asList(0));
        JasperReport jasperReport = BuscaRelatorioDuplicatas("rptQRCode");
        Map<String, Object> reportParameter = new HashMap<>();
        reportParameter.put("QRCODE_IMAGE", qrCodePng);
        reportParameter.put("CODIGO_DOCUMENTO", contratoDocumentoId.toString());
        return ProcessaRelatorio(dataSource, jasperReport, reportParameter);
    }

    private byte[] ProcessaRelatorio(Collection<?> resultados, JasperReport jasperReport,
                                     Map<String, Object> reportParameter) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        //definindo PT-BR
        Locale myLocale = new Locale("pt", "BR"); //Portuguese
        parameters.put(JRParameter.REPORT_LOCALE, myLocale);

        if (!CommonsUtil.semValor(reportParameter)) {
            parameters.putAll(reportParameter);
        }

        try {
            //gera o relatorio
            JasperPrint relatorio = JasperFillManager.fillReport(jasperReport, parameters,
                    new JRBeanCollectionDataSource(resultados));
            //converte para byte[]
            byte[] output = JasperExportManager.exportReportToPdf(relatorio);
            return output;
        } catch (JRException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JasperReport BuscaRelatorioDuplicatas(String report) {
        JasperReport jasperReport = null;
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/Report/" + report + ".jasper");

            if (resourceAsStream == null) {
                LOGGER.error("Compilando relatório: " + report
                        + "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                        + "@@@ Compilando relatório: " + report
                        + "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                resourceAsStream = getClass().getResourceAsStream("/Report/" + report + ".jrxml");
                JasperDesign jasperDesign = JRXmlLoader.load(resourceAsStream);

                jasperReport = JasperCompileManager.compileReport(jasperDesign);
            } else {
                jasperReport = (JasperReport) JRLoader.loadObject(resourceAsStream);
            }

            return jasperReport;

        } catch (JRException e) {
            LOGGER.error("getRelatorio: EXCEPTION: " + e.getMessage());
        }
        return null;
    }


}
