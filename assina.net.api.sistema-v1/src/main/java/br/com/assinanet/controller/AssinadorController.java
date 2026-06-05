package br.com.assinanet.controller;

import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.enums.TipoArquivoEnum;
import br.com.assinanet.request.assinador.*;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.assinanet.service.ContratoDocumentoService;
import br.com.assinanet.service.ContratoService;
import br.com.assinanet.util.ArquivoUtil;
import br.com.assinanet.util.CommonsUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@RestController
@RequestMapping("/api/v1/assinador")
@CrossOrigin(origins = "*")
public class AssinadorController {

    @Autowired
    private final ContratoService contratoService;
    @Autowired
    private final ContratoDocumentoService contratoDocumentoService;


    @Autowired
    private final HttpServletRequest request;

    public AssinadorController(ContratoService contratoService, ContratoDocumentoService contratoDocumentoService, HttpServletRequest request) {
        this.contratoService = contratoService;
        this.contratoDocumentoService = contratoDocumentoService;
        this.request = request;
    }

    @PostMapping(value = "buscaPartes")
    public @ResponseBody
    List<AssinadorContratoParteRequest> BuscaPartes(@RequestBody AssinadorBuscaPartesRequest parametros) throws Exception {
        List<AssinadorContratoParteRequest> result = new ArrayList<>(0);

        List<ContratoParte> partes = contratoService.BuscaPartesParaAssinatura(parametros.getContrato(), parametros.getCpfCnpj());

        // pq nao leva o contratoPartePJ por conta do loop infinito
        partes.forEach(contratoParte -> {
            AssinadorContratoParteRequest assinadorContratoParteRequest = new AssinadorContratoParteRequest();
            BeanUtils.copyProperties(contratoParte, assinadorContratoParteRequest);
            result.add(assinadorContratoParteRequest);
        });


        return result;
    }

    @PostMapping(value = "buscaDocumentos")
    public @ResponseBody
    List<AssinadorContratoDocumentoRequest> BuscaDocumentos(@RequestBody AssinadorBuscaDocumentosRequest parametros) throws Exception {

        List<AssinadorContratoDocumentoRequest> result = new ArrayList<>(0);

        List<ContratoDocumento> documentos = contratoService.BuscaDocumentosParaAssinar(parametros.getContrato(), parametros.getContratoParte());
        PDDocument documentFull = new PDDocument();

        // pq nao leva o contratoParteAssinando por conta do loop infinito
        documentos.forEach(documento -> {

            AssinadorContratoDocumentoRequest assinadorContratoDocumentoRequest = new AssinadorContratoDocumentoRequest();
            BeanUtils.copyProperties(documento, assinadorContratoDocumentoRequest, "documentoOriginal");

            ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse =null;
            try {
                    contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(documento), documento.getDocumentoOriginalSHA256(), false, true);
                    if (CommonsUtil.semValor(contratoDocumentoVisualizaResponse)){
                        contratoDocumentoVisualizaResponse = new ContratoDocumentoVisualizaResponse(Optional.of(documento), documento.getDocumentoOriginal(), false);
                    }
            } catch (Exception e) {
            }

            //compactando com LZString
            assinadorContratoDocumentoRequest.setDocumentoOriginal(contratoDocumentoVisualizaResponse.getDocumentoPDF()); ;
//           try{
//               PDDocument document = PDDocument.load(documento.getDocumentoAssinado());
//
//               for (int iPageAssinatura = 0; iPageAssinatura < document.getPages().getCount(); iPageAssinatura++) {
//                   documentFull.addPage(document.getPage(iPageAssinatura));
//               }
//            } catch (Exception e) {
//
//        }
        result.add(assinadorContratoDocumentoRequest);
        });

//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        PDDocument destination = null;

//        for (ContratoDocumento documento : documentos) {
//            byte[] pdf = documento.getDocumentoAssinado();
//
//            try{
//                PDDocument document = PDDocument.load(pdf);
//
//                for (int iPageAssinatura = 0; iPageAssinatura < document.getPages().getCount(); iPageAssinatura++) {
//                    documentFull.addPage(document.getPage(iPageAssinatura));
//                }
//            } catch (Exception e) {
//
//            }
//
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdf);
//            PDDocument source = null;
//            try {
//                source = PDDocument.load(inputStream);
//            } catch (Exception e) {
//            }
//
//            if (destination == null && source != null) {
//                destination = new PDDocument(source.getDocument());
//            } else {
//                for (int iPageAssinatura = 0; iPageAssinatura < source.getPages().getCount(); iPageAssinatura++) {
//                    destination.addPage(source.getPage(iPageAssinatura));
//                }
//            }
//        }
//
//        destination.save(outputStream);
//        destination.close();

//        //compactando com LZString
//        AssinadorContratoDocumentoRequest assinadorContratoDocumentoRequest = new AssinadorContratoDocumentoRequest();
//        assinadorContratoDocumentoRequest.setDocumentoOriginal(LZString.compressToUTF16(Base64.getEncoder().encodeToString(outputStream.toByteArray()) )) ;
//        result.add(assinadorContratoDocumentoRequest);

        return result;
    }

    @PostMapping(value = "salvaAssinaturas")
    public @ResponseBody
    boolean SalvaAssinaturas(@RequestBody AssinadorAssinaturasLogsRequest parametros) throws Exception {
        contratoService.SalvaAssinaturasLocal(parametros);
        return true;
    }

    @PostMapping(value = "verificaContratoAssinado")
    public @ResponseBody
    boolean verificaContratoAssinado(@RequestBody AssinadorAssinaturasLogsRequest parametros) throws Exception {
        boolean gerarArquivoAssinado = false;
        contratoService.VerificaContratoAssinado(parametros.getContrato(), parametros.getContratoParteAssinou(), gerarArquivoAssinado);

        return true;
    }

}


