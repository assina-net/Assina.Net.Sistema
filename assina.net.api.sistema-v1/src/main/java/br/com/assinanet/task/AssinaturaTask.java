package br.com.assinanet.task;

import br.com.assinanet.Report.ReportDataSource;
import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.*;
import br.com.assinanet.models.AssinaturaExtendsModel;
import br.com.assinanet.models.AssinaturaReportModel;
import br.com.assinanet.models.CarimboTempoModel;
import br.com.assinanet.models.ContratoDuplicatasModel;
import br.com.assinanet.repository.ContratoDocumentoRepository;
import br.com.assinanet.repository.ContratoParteDocumentoRepository;
import br.com.assinanet.repository.ContratoParteRepository;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.assinanet.service.*;
import br.com.assinanet.util.*;
import com.azure.storage.blob.models.BlobStorageException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/02/2018 - 08:15
 */
@Service
@Slf4j
public class AssinaturaTask {

    protected static EmailService emailService;

    protected static ContratoService contratoService;
    protected static ContratoLogService contratoLogService;
    protected static PapelService papelService;
    protected static SistemaAtributoService sistemaAtributoService;
    protected static MensagemService mensagemService;

    protected static ContratoParteRepository contratoParteRepository;
    protected static ContratoDocumentoRepository contratoDocumentoRepository;
    protected static ContratoParteDocumentoRepository contratoParteDocumentoRepository;
    protected static ContratoDocumentoService contratoDocumentoService;

    public AssinaturaTask(EmailService emailService,
                          ContratoService contratoService,
                          ContratoLogService contratoLogService,
                          PapelService papelService,
                          SistemaAtributoService sistemaAtributoService,
                          MensagemService mensagemService,
                          ContratoParteRepository contratoParteRepository,
                          ContratoDocumentoRepository contratoDocumentoRepository,
                          ContratoParteDocumentoRepository contratoParteDocumentoRepository,
                          ContratoDocumentoService contratoDocumentoService) {
        AssinaturaTask.emailService = emailService;
        AssinaturaTask.contratoService = contratoService;
        AssinaturaTask.contratoLogService = contratoLogService;
        AssinaturaTask.papelService = papelService;
        AssinaturaTask.sistemaAtributoService = sistemaAtributoService;
        AssinaturaTask.contratoParteRepository = contratoParteRepository;
        AssinaturaTask.contratoDocumentoRepository = contratoDocumentoRepository;
        AssinaturaTask.contratoParteDocumentoRepository = contratoParteDocumentoRepository;
        AssinaturaTask.mensagemService = mensagemService;
        AssinaturaTask.contratoDocumentoService = contratoDocumentoService;
    }


    private static class DocumentoAssinaturas {

        @Getter
        @Setter
        ContratoDocumento documento;


        @Getter
        @Setter
        List<AssinaturaExtendsModel> assinaturas;


    }

    @Transactional
    public static class GeraAssinaturaDocumentos implements Runnable {

        private final Contrato contrato;
        private final List<ContratoParte> partes;

        private final List<SimpleMailMessage> menssagens = new ArrayList<>(0);


        public GeraAssinaturaDocumentos(Contrato contrato) {
            this.contrato = contrato;
            if (CommonsUtil.semValor(contrato.getPartes())) {
                this.partes = contratoService.getPartes(this.contrato);
            } else {
                this.partes = this.contrato.getPartes();
            }
        }


        public void gerarAssinaturas() throws Exception {

            contratoLogService.salva(new ContratoLog(contrato, null, "Criando estrutura das assinaturas do contrato. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));

//            System.out.println("-----------------------------------------------------------------------------------------------");
//            System.out.println("Total de Assinaturas a ser gerada: "+partes.size());
//            for (ContratoParte contratoParte : partes) {
//                System.out.println("Gerando assinatura -> "
//                        +contratoParte.getContrato().getDataCriacao().toString()
//                        +" - "+contratoParte.getContrato().getIdentificador()
//                        +" - "+contratoParte.getNomeRazaoSocial());
//            }
//            System.out.println("-----------------------------------------------------------------------------------------------");

            List<ContratoParte> arrayTo = new ArrayList<>(0);
            Map<UUID, DocumentoAssinaturas> mapDocumentoAssinaturas = new HashMap<>(0);

            for (int iParte = 0; iParte < partes.size(); iParte++) {

                ContratoParte contratoParte = partes.get(iParte);

                if (StatusEnum.INATIVO.equals(contratoParte.getStatus()))
                    continue;

                if (TipoPessoaEnum.FISICA.equals(contratoParte.getTipoPessoa()) && contratoParte.getContratoPartePJ() == null) {
                    CriaRegistroAssinatura(mapDocumentoAssinaturas, contratoParte);

                    ContratoParte contratoParteFind = arrayTo.stream().filter(c -> c.getEmail() == contratoParte.getEmail()).findFirst().orElse(null);
                    if (CommonsUtil.semValor(contratoParteFind) &&
                            !CommonsUtil.semValor((contratoParte.getEmail()))) {
                        arrayTo.add(contratoParte);
                    }
                } else if (TipoPessoaEnum.JURIDICA.equals(contratoParte.getTipoPessoa())) {
                    List<ContratoParte> contatos = partes.stream().filter(c -> c.getContratoPartePJ() == contratoParte).collect(Collectors.toList());

                    if (CommonsUtil.semValor(contatos)) {
                        contatos = contratoParte.getContatos();
                    }

                    for (ContratoParte contato : contatos) {

                        if (StatusEnum.INATIVO.equals(contato.getStatus()))
                            continue;

                        CriaRegistroAssinatura(mapDocumentoAssinaturas, contato);

                        ContratoParte contratoParteFind = arrayTo.stream().filter(c -> c.getEmail() == contato.getEmail()).findFirst().orElse(null);
                        if (CommonsUtil.semValor(contratoParteFind) &&
                                !CommonsUtil.semValor((contato.getEmail()))) {
                            arrayTo.add(contato);
                        }
                    }
                }

            }
            contratoLogService.salva(new ContratoLog(contrato, null, "Criando arquivos assinados do contrato. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));


            if (InsereAssinaturasVisiveis(mapDocumentoAssinaturas)) {


                StatusContratoEnum statusContratoAnterior = contrato.getStatusContrato();

                if (!StatusContratoEnum.ASSINADO.equals(statusContratoAnterior)) {
                    boolean bAssinado = true;
                    for (ContratoDocumento documento : contrato.getDocumentos()) {
                        if (!StatusDocumentoEnum.ASSINADO.equals(documento.getStatusDocumento()) &&
                                CommonsUtil.booleanValue(documento.getTipoDocumento().getAssina())) {
                            bAssinado = false;
                            break;
                        }
                    }
                    if (bAssinado)
                        contrato.setStatusContrato(StatusContratoEnum.ASSINADO);
                }

                contratoService.Salva(contrato);

                //somente gera loga e envia se a situacao anterior nao for assinado e agora esta assinado
                if (!StatusContratoEnum.ASSINADO.equals(statusContratoAnterior) &&
                        StatusContratoEnum.ASSINADO.equals(contrato.getStatusContrato())) {
                    CarimboTempoModel carimbo = DataUtil.getCarimboTempo();
                    String sLog = "Documento assinado por todos os participantes.";
                    ContratoLog log = new ContratoLog(contrato, null, sLog, carimbo, false);
                    contratoLogService.salva(log);

                    contratoLogService.salva(new ContratoLog(contrato, null, "Fim da geração das assinaturas do contrato. Identificador: " +
                            contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                            DataUtil.getCarimboTempo(), true));

                    Map<TipoEnvioMsgEnum, List<ContratoParte>> mapEnvio = mensagemService.montaListasEnvio(SistemaTipoAtributoEnum.ENVIAR_CONTRATO_ASSINADO_WHATSAPP,
                            contrato, arrayTo);

                    if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.EMAIL))) {
                        Runnable runnable = new EmailTask.EnviaContratoAssinado(contrato, mapEnvio.get(TipoEnvioMsgEnum.EMAIL));
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }

                    if (!CommonsUtil.semValor(mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP))) {
                        Runnable runnable = new CelularTask.EnviaContratoAssinadoWhatsApp(contrato, mapEnvio.get(TipoEnvioMsgEnum.WHATSAPP));
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }
            }
        }


        private void CriaRegistroAssinatura(Map<UUID, DocumentoAssinaturas> mapDocumentoAssinaturas, ContratoParte contratoParte) {
            List<ContratoParteDocumento> contratoParteDocumentos = contratoParteDocumentoRepository.findByContratoParte(contratoParte);

            //reordena a lista por data de assinatura
            Collections.sort(contratoParteDocumentos, new Comparator<ContratoParteDocumento>() {
                @Override
                public int compare(ContratoParteDocumento o1, ContratoParteDocumento o2) {
                    if (o1.getDataAssinatura() == null)
                        return -1;
                    if (o2.getDataAssinatura() == null)
                        return 1;
                    return o1.getDataAssinatura().compareTo(o2.getDataAssinatura());
                }
            });


            for (ContratoParteDocumento contratoParteDocumento : contratoParteDocumentos) {
                DocumentoAssinaturas mapDocumento = mapDocumentoAssinaturas.get(contratoParteDocumento.getDocumento().getId());
                if (CommonsUtil.semValor(mapDocumento)) {
                    mapDocumento = new DocumentoAssinaturas();
                    contratoParteDocumento.getDocumento().setDocumentoAssinado(null);


                    if (!CommonsUtil.semValor(contratoParteDocumento.getDocumento().getDadosOrigem())) {
                        ContratoDuplicatasModel contratoDuplicatasModel = GsonUtil.fromJson(contratoParteDocumento.getDocumento().getDadosOrigem(), ContratoDuplicatasModel.class);
                        if (contratoDuplicatasModel == null) {
                            contratoDuplicatasModel = new ContratoDuplicatasModel();
                        }
                        contratoDuplicatasModel.setAssinaturas(new ArrayList<>(0));

                        String dadosOrigem = GsonUtil.toJson(contratoDuplicatasModel);
                        contratoParteDocumento.getDocumento().setDadosOrigem(dadosOrigem);
                    }
                    mapDocumento.setDocumento(contratoParteDocumento.getDocumento());
                    mapDocumento.setAssinaturas(new ArrayList<>(0));
                    mapDocumentoAssinaturas.put(contratoParteDocumento.getDocumento().getId(), mapDocumento);
                }

                mapDocumento.setDocumento(contratoParteDocumento.getDocumento());


                AssinaturaExtendsModel assinaturaExtendsModel = new AssinaturaExtendsModel();
                assinaturaExtendsModel.setNomeAssinatura(contratoParte.getNomeRazaoSocial() + (""));
                assinaturaExtendsModel.setEmail(contratoParte.getEmail());

                if (contratoParte.getContratoPartePJ() != null) {
                    assinaturaExtendsModel.setNomePessoaJuridica(contratoParte.getContratoPartePJ().getNomeRazaoSocial());
                    assinaturaExtendsModel.setCnpjPessoaJuridica(contratoParte.getContratoPartePJ().getCpfCnpj());
                }

                assinaturaExtendsModel.setLocalAssinatura("Servidor Assina.Net");
                assinaturaExtendsModel.setPapel(contratoParteDocumento.getContratoPartePapel().getPapel());
                if (CommonsUtil.semValor(contratoParteDocumento.getContratoPartePapel().getPapel().getIdentificacao()))
                    assinaturaExtendsModel.setMotivoAssinatura(contratoParteDocumento.getContratoPartePapel().getPapel().getIdentificacao());
                else
                    assinaturaExtendsModel.setMotivoAssinatura(contratoParteDocumento.getContratoPartePapel().getPapel().getNome());

                assinaturaExtendsModel.setAssinatura(contratoParteDocumento.getAssinaturaDigital());

                if (!CommonsUtil.semValor(contratoParteDocumento.getSerialTokenCertificado())) {
                    assinaturaExtendsModel.setChaveAssinatura(contratoParteDocumento.getSerialTokenCertificado());
                } else {
                    assinaturaExtendsModel.setChaveAssinatura(Util.GerarNomeArquivoAssinaturaPCKS7(contratoParteDocumento.getDocumento()));
                }

                assinaturaExtendsModel.setTipoAssinatura(contratoParteDocumento.getTipoAssinatura());

                CarimboTempoModel carimboTempo = new CarimboTempoModel();
                carimboTempo.setGmtCarimboTempo(contratoParteDocumento.getGmtAssinatura());
                carimboTempo.setCarimboTempo(contratoParteDocumento.getCarimboTempoAssinatura());
                carimboTempo.setDataCarimboTempo(contratoParteDocumento.getDataAssinatura());
                assinaturaExtendsModel.setCarimboTempo(carimboTempo);
                assinaturaExtendsModel.setIp(contratoParteDocumento.getIpAssinatura());

                String sTimeZone = "UTC";
                if (!CommonsUtil.semValor(contratoParteDocumento.getGmtAssinatura()) &&
                        contratoParteDocumento.getGmtAssinatura() != "Z") {
                    sTimeZone = "GMT" + contratoParteDocumento.getGmtAssinatura();
                }

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(sTimeZone));
                calendar.setTime(contratoParteDocumento.getDataAssinatura());
                assinaturaExtendsModel.setSignDate(calendar);

                mapDocumento.getAssinaturas().add(assinaturaExtendsModel);


                contratoParteDocumentoRepository.save(contratoParteDocumento);
            }
        }


        private boolean InsereAssinaturasVisiveis(Map<UUID, DocumentoAssinaturas> mapIdDocumentoAssinaturas) throws Exception {
            boolean bInsereAssinaturasVisiveis = false;
            Integer qtdMaximaPagina = sistemaAtributoService.getInteger(SistemaTipoAtributoEnum.QUANTIDADE_MAXIMA_PAGINA, null);

            String visto = "";
            for (Map.Entry<UUID, DocumentoAssinaturas> idDocumentoAssinaturas : mapIdDocumentoAssinaturas.entrySet()) {
                DocumentoAssinaturas mapDocumentoAssinaturas = idDocumentoAssinaturas.getValue();


                ContratoDuplicatasModel contratoDuplicatasModel = GsonUtil.fromJson(mapDocumentoAssinaturas.getDocumento().getDadosOrigem(), ContratoDuplicatasModel.class);

                if (CommonsUtil.semValor(contratoDuplicatasModel)) {
                    contratoDuplicatasModel = new ContratoDuplicatasModel();
                }

                for (AssinaturaExtendsModel assinaturaExtendsModel : mapDocumentoAssinaturas.getAssinaturas()) {
                    if (CommonsUtil.semValor(contratoDuplicatasModel.getAssinaturas())) {
                        contratoDuplicatasModel.setAssinaturas(new ArrayList<>(0));
                    }
                    AssinaturaReportModel assinaturaReportModel = new AssinaturaReportModel();
                    assinaturaReportModel.setChaveAssinatura(assinaturaExtendsModel.getChaveAssinatura());
                    assinaturaReportModel.setDataAssinatura(assinaturaExtendsModel.getCarimboTempo().getCarimboTempo());

                    String descricaoAssinatura = "";

                    Papel papel = assinaturaExtendsModel.getPapel();
                    if (papel == null) {
                        papel = papelService.findByIdentificacaoAndCliente(assinaturaExtendsModel.getMotivoAssinatura(), mapDocumentoAssinaturas.getDocumento().getContrato().getCustodiante());
                        if (papel == null) {
                            papel = papelService.findByIdentificacaoAndCliente(assinaturaExtendsModel.getMotivoAssinatura(), mapDocumentoAssinaturas.getDocumento().getContrato().getCustodiante(), null);
                        }
                    }

                    if (papel == null) {
                        throw new Exception("Papel não encontrado");
                    }
                    String identificacoPapel = papel.getNome();
                    if (papel.getIdentificacao() != null)
                        identificacoPapel = papel.getIdentificacao().toUpperCase();

                    switch (identificacoPapel) {
                        case "EMITENTE":
                            descricaoAssinatura = "Emissao da duplicata";
                            break;
                        case "AVALISTA":
                            descricaoAssinatura = "Em aval a " + mapDocumentoAssinaturas.getDocumento().getContrato().getCustodiante().getPessoa().getNomeRazaoSocial()
                                    + ("");
                            break;
                        case "ENDOSSANTE":
                            descricaoAssinatura = "Endosso a " + mapDocumentoAssinaturas.getDocumento().getContrato().getCustodiante().getPessoa().getNomeRazaoSocial() +
                                    ("");
                            break;

                        default:
                            descricaoAssinatura = papel.getNome();
                            if (!CommonsUtil.semValor(assinaturaExtendsModel.getNomePessoaJuridica())) {
                                descricaoAssinatura = descricaoAssinatura + "\n" + assinaturaExtendsModel.getNomePessoaJuridica() +
                                        ("");
                            }

                    }
                    if (CommonsUtil.semValor(papel.getIdentificacao()))
                        assinaturaReportModel.setPapelAssinatura(papel.getNome());
                    else
                        assinaturaReportModel.setPapelAssinatura(papel.getIdentificacao());
                    assinaturaReportModel.setDescricaoAssinatura(descricaoAssinatura);
                    assinaturaReportModel.setTipoAssinatura(assinaturaExtendsModel.getTipoAssinatura().toString());

                    String sAssinatura = assinaturaExtendsModel.getNomeAssinatura() + "\n" +
                            assinaturaExtendsModel.getEmail() +
                            (TipoAssinaturaEnum.CHAVEPRIVADA.equals(assinaturaExtendsModel.getTipoAssinatura()) ?
                                    "\nChave Privada Assina.Net " : "\nCertificado digital") +
                            "\nIP: " + assinaturaExtendsModel.getIp();
                    assinaturaReportModel.setAssinatura(sAssinatura);

                    if ("EMITENTE".equalsIgnoreCase(papel.getIdentificacao()) &&
                            CommonsUtil.semValor(contratoDuplicatasModel.getAssinaturaEmissor()) &&
                            !CommonsUtil.semValor(contratoDuplicatasModel.getCedenteNome())) {

                        contratoDuplicatasModel.setAssinaturaEmissor(assinaturaReportModel.getAssinatura());
                        contratoDuplicatasModel.setDataAssinaturaEmissor(assinaturaReportModel.getDataAssinatura());
                        contratoDuplicatasModel.setTipoAssinaturaEmissor(assinaturaReportModel.getTipoAssinatura());
                        contratoDuplicatasModel.setChaveAssinaturaEmissor(assinaturaReportModel.getChaveAssinatura());


                    } else {
                        contratoDuplicatasModel.getAssinaturas().add(assinaturaReportModel);
                    }

                    String dadosOrigem = GsonUtil.toJson(contratoDuplicatasModel);
                    mapDocumentoAssinaturas.getDocumento().setDadosOrigem(dadosOrigem);

                    //ser for duplicata e tem dados de origem ja monta diferente, nao precisa de vistos na pagina
                    if ("DUPLICATA".equalsIgnoreCase(mapDocumentoAssinaturas.getDocumento().getTipoDocumento().getIdentificacao())) {
                        //  && !CommonsUtil.semValor(mapDocumentoAssinaturas.getDocumento().getDadosOrigem())
                        continue;
                    }


                    visto += assinaturaExtendsModel.getNomeAssinatura() +
                            " - " + papel.getNome() +
                            " - " + CommonsUtil.formataData(assinaturaExtendsModel.getSignDate().getTime(), "dd/MM/yyyy HH:mm:ss") +
                            " - GMT" + assinaturaExtendsModel.getCarimboTempo().getGmtCarimboTempo() +
                            " - IP: " + assinaturaExtendsModel.getIp() +
                            " *** ";


                }


            }

            ReportDataSource reportDataSource = new ReportDataSource();

            for (Map.Entry<UUID, DocumentoAssinaturas> idDocumentoAssinaturas : mapIdDocumentoAssinaturas.entrySet()) {
                DocumentoAssinaturas mapDocumentoAssinaturas = idDocumentoAssinaturas.getValue();
//                ContratoDocumento contratoDocumento = mapDocumentoAssinaturas.getDocumento();


                //pega o documento do contratro estava sobrescrevendo
                ContratoDocumento contratoDocumento = contrato.getDocumentos().stream().filter(d -> CommonsUtil.mesmoValor(d.getId(), mapDocumentoAssinaturas.getDocumento().getId())).findFirst().orElse(mapDocumentoAssinaturas.getDocumento());

                contratoDocumento.setDocumentoAssinado();
                List<TipoDocumentoPosicao> posicoesAssinatura = contratoDocumento.getTipoDocumento().getPosicoesAssinatura();
                if (!CommonsUtil.semValor(posicoesAssinatura))
                    posicoesAssinatura.sort(Comparator.comparing(TipoDocumentoPosicao::getPagina));

                byte[] docuAssinado;
                //verifica se é duplicata e se tem os dados de origem para regerar, caso contrario segue como arquivo normal.
                if ("DUPLICATA".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) && !CommonsUtil.semValor(contratoDocumento.getDadosOrigem())) {

                    List<ContratoDuplicatasModel> dataSource = new ArrayList<>(0);
                    ContratoDuplicatasModel contratoDuplicatasModel = GsonUtil.fromJson(mapDocumentoAssinaturas.getDocumento().getDadosOrigem(), ContratoDuplicatasModel.class);

                    dataSource.add(contratoDuplicatasModel);
                    docuAssinado = reportDataSource.geraRelatorioDuplicatasAssinadas(dataSource);
                } else {
                    PDDocument document;
                    try {
                        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = null;
                        if (CommonsUtil.semValor(contratoDocumento.getDocumentoAssinado())) {
                            contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(contratoDocumento), contratoDocumento.getDocumentoOriginalSHA256(), true, true);
                        } else {
                            contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio(Optional.of(contratoDocumento), contratoDocumento.getDocumentoAssinadoSHA256(), true, false);
                        }
                        if (contratoDocumentoVisualizaResponse == null) {
                            System.out.println("Contrato: " + contrato.getIdentificador() + " - InsereAssinaturasVisiveis -> contratoDocumentoVisualizaResponse retornou null");
                            continue;
                        }
                        document = PDDocument.load(contratoDocumentoVisualizaResponse.getDocumentoBytes());

                    } catch (BlobStorageException e) {
                        System.out.println("Contrato: " + contrato.getIdentificador() + "Id: " + contrato.getId() +  " Arquivo: " + contratoDocumento.getNomeDocumento() + " - Erro: " + e.getMessage());
                        return bInsereAssinaturasVisiveis;
                    } catch (Exception e) {
                        System.out.println("Contrato: " + contrato.getIdentificador() + " - Erro: " + e.getMessage());
                        return bInsereAssinaturasVisiveis;
                    }
                    if (document.getPages().getCount() > qtdMaximaPagina) {
                        return bInsereAssinaturasVisiveis;
                    }
                    int accessPermissions = SigUtils.getMDPPermission(document);
                    PDFRenderer pdfRenderer = new PDFRenderer(document);


                    if (!"DUPLICATA".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {
                        if (accessPermissions == 1) {
                            throw new IllegalStateException("Mudanças no documento não são permitidas.");
                        } else {

                            String sVerificador = String.format("Este documento é uma cópia do original, assinado digitalmente pelo portal de assinaturas Assina.net em %s. Para conferir o original, acesse o site %s e informe o código %s",
                                    mapDocumentoAssinaturas.getDocumento().getCarimboTempoAssinado(), "https://portal.assina.net", mapDocumentoAssinaturas.getDocumento().getId().toString());

                            for (PDPage page : document.getPages()) {
                                if (!CommonsUtil.mesmoValor(contrato.getCustodiante().getId(), UUID.fromString("620F18E8-F315-42DE-9242-523E9CC7CB06")))
                                    WriteTextToPage(document, page, visto);
                                else
                                    WriteTextToPage(document, page, visto, 6);

                                if (CommonsUtil.booleanValue(mapDocumentoAssinaturas.getDocumento().getTipoDocumento().getValidacaoOnLine())
                                        || sistemaAtributoService.getBoolean(SistemaTipoAtributoEnum.EXIBE_MENSAGEM_VALIDACAO_DE_DOCUMENTO, contratoDocumento.getContrato().getCustodiante())) {
                                    WriteVerticalTextToPage(document, page, sVerificador, 6);
                                }
                            }
                        }
                    }


                    ContratoDuplicatasModel contratoDuplicatasModel = GsonUtil.fromJson(mapDocumentoAssinaturas.getDocumento().getDadosOrigem(), ContratoDuplicatasModel.class);


                    int iQtdLinhaRelatorio = 5;
                    int iQtdColunaRelatorio = 3;
                    byte[] docuAssinaturas = null;

                    Map<String, byte[]> mapDocumentosAssinatura = new HashMap<>(0);

                    if ("PLACET_INICIACAO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "PLACET_REGULARIZACAO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "PLACET_EX_OFICIO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "QUITE_PLACET".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                        List<AssinaturaReportModel> dataSource = new ArrayList<>(0);
                        dataSource.addAll(contratoDuplicatasModel.getAssinaturas());
                        docuAssinaturas = reportDataSource.geraRelatorioAssinaturaUnica(dataSource);
                        mapDocumentosAssinatura.put("", docuAssinaturas);

                    } else if ("ATO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "DECRETO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "LEI".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "DECRETO_2".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "ATO_PAEL".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "ESTATUTO_DIR_EXEC".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())
                    ) {

                        for (AssinaturaReportModel assinatura : contratoDuplicatasModel.getAssinaturas()) {
                            List<AssinaturaReportModel> dataSource = new ArrayList<>(0);
                            dataSource.add(assinatura);
                            docuAssinaturas = reportDataSource.geraRelatorioAssinaturaUnica(dataSource);
                            mapDocumentosAssinatura.put(assinatura.getPapelAssinatura(), docuAssinaturas);
                        }

                    } else if ("ALVARA_TRIBUTARIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "ATO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "DECRETO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "LEI_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "CERTIDAO_TRIBUTARIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "CONTRATO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "CONVENIO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                            "MEMORANDO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                        for (AssinaturaReportModel assinatura : contratoDuplicatasModel.getAssinaturas()) {
                            List<AssinaturaReportModel> dataSource = new ArrayList<>(0);
                            dataSource.add(assinatura);
                            docuAssinaturas = reportDataSource.geraRelatorioAssinaturaUnica(dataSource, "rptAssinaturasUnicaPrefeitura");
                            mapDocumentosAssinatura.put(assinatura.getPapelAssinatura(), docuAssinaturas);
                        }
                    } else if ("OFICIO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                        for (AssinaturaReportModel assinatura : contratoDuplicatasModel.getAssinaturas()) {
                            List<AssinaturaReportModel> dataSource = new ArrayList<>(0);
                            dataSource.add(assinatura);
                            docuAssinaturas = reportDataSource.geraRelatorioAssinaturaUnica(dataSource, "rptAssinaturasUnicaPrefeitura2");
                            mapDocumentosAssinatura.put(assinatura.getPapelAssinatura(), docuAssinaturas);
                        }
                    } else if (!CommonsUtil.semValor(contratoDocumento.getTipoDocumento().getPosicoesAssinatura())) {
                        //assinatras com posiconamento
                        for (AssinaturaReportModel assinatura : contratoDuplicatasModel.getAssinaturas()) {
                            List<AssinaturaReportModel> dataSource = new ArrayList<>(0);
                            dataSource.add(assinatura);
                            docuAssinaturas = reportDataSource.geraRelatorioAssinaturaUnica(dataSource);
                            mapDocumentosAssinatura.put(assinatura.getPapelAssinatura(), docuAssinaturas);
                        }
                    } else {
                        //assinatras sem posiconamento
                        List<ContratoDuplicatasModel> dataSource = new ArrayList<>(0);

                        if (contratoDuplicatasModel.getAssinaturas().size() < iQtdLinhaRelatorio * iQtdColunaRelatorio) {
                            List<AssinaturaReportModel> lstAssinaturas = new ArrayList<>(0);
                            //monta uma matriz das assinturas
                            for (int j = 0; j < iQtdLinhaRelatorio * iQtdColunaRelatorio; j++) {
                                AssinaturaReportModel assinaturaReportModel = new AssinaturaReportModel();
                                lstAssinaturas.add(assinaturaReportModel);
                            }
                            //coloca na ordem
                            for (int i = 0; i < contratoDuplicatasModel.getAssinaturas().size(); i++) {
                                int iPosicao = ((iQtdLinhaRelatorio * Math.floorMod(i, iQtdColunaRelatorio))) + (i / iQtdColunaRelatorio);
                                lstAssinaturas.remove(iPosicao);
                                lstAssinaturas.add(iPosicao, contratoDuplicatasModel.getAssinaturas().get(i));
                            }
                            contratoDuplicatasModel.setAssinaturas(lstAssinaturas);
                        }
                        dataSource.add(contratoDuplicatasModel);
                        docuAssinaturas = reportDataSource.geraRelatorioAssinaturas(dataSource);
                        mapDocumentosAssinatura.put("", docuAssinaturas);
                    }

                    // PDDocument documentAssinatura = PDDocument.load(docuAssinaturas);


                    PDDocument documentAssinatura = null;
                    int iAssinatura = 0;
                    int qtdAssinaturas = mapDocumentosAssinatura.entrySet().size();

                    if (document.getPages().getCount() > qtdMaximaPagina) {
                        break;
                    }

                    for (Map.Entry<String, byte[]> assinatura : mapDocumentosAssinatura.entrySet()) {
                        documentAssinatura = PDDocument.load(assinatura.getValue());
                        int paginaAtual = 0;

                        for (int iPage = 0; iPage < document.getPages().getCount(); iPage++) {

                            paginaAtual++;
                            //implementacao do Samuel
                            if (!CommonsUtil.semValor(posicoesAssinatura)) {

                                for (TipoDocumentoPosicao tipoDocumentoPosicao : posicoesAssinatura) {

                                    int pageNum = paginaAtual - 1;
                                    if (!CommonsUtil.semValor(tipoDocumentoPosicao.getPagina())) {
                                        if (CommonsUtil.mesmoValor(tipoDocumentoPosicao.getPagina(), -1)) {
                                            pageNum = document.getPages().getCount() - 1;
                                        } else {
                                            pageNum = tipoDocumentoPosicao.getPagina() - 1;
                                        }
                                    }

                                    if (!CommonsUtil.semValor(tipoDocumentoPosicao.getPapel())) {
                                        if ((!CommonsUtil.semValor(tipoDocumentoPosicao.getPapel().getPapel().getIdentificacao())) &&
                                                !CommonsUtil.mesmoValor(assinatura.getKey().toUpperCase(), tipoDocumentoPosicao.getPapel().getPapel().getIdentificacao().toUpperCase()) ||
                                                (CommonsUtil.semValor(tipoDocumentoPosicao.getPapel().getPapel().getIdentificacao())) &&
                                                        !CommonsUtil.mesmoValor(assinatura.getKey().toUpperCase(), tipoDocumentoPosicao.getPapel().getPapel().getNome().toUpperCase())) {
                                            continue;
                                        }
                                    }
                                    if (!CommonsUtil.mesmoValor(pageNum + 1, paginaAtual) &&
                                            !CommonsUtil.semValor(tipoDocumentoPosicao.getPagina())) {
                                        continue;
                                    }

                                    criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, 0, tipoDocumentoPosicao.getX(), tipoDocumentoPosicao.getY(), tipoDocumentoPosicao.getWidth(), tipoDocumentoPosicao.getHeight(), false);

                                    /*for (int pageNumStart = 0; pageNumStart < document.getNumberOfPages(); ++pageNumStart) {
                                        for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                            criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNumStart, pageNumAssinatura, tipoDocumentoPosicao.getX(), tipoDocumentoPosicao.getY(), tipoDocumentoPosicao.getWidth(), tipoDocumentoPosicao.getHeight(), false);
                                        }
                                    }*/
                                }

                            } else if ("PLACET_INICIACAO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "PLACET_REGULARIZACAO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                                for (int pageNum = 0; pageNum < document.getNumberOfPages(); ++pageNum) {
                                    for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 275, 190, 150, 150, true);
                                    }
                                }
                            } else if ("PLACET_EX_OFICIO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "QUITE_PLACET".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {
                                for (int pageNum = 0; pageNum < document.getNumberOfPages(); ++pageNum) {
                                    for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 275, 75, 150, 150, true);
                                    }
                                }
                            } else if ("ATO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "DECRETO".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "LEI".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "SECRETARIO_GUARDA_SELOS")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 70, 150, 150, true);

                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SEC_GABINETE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 300, 150, 150, true);

                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SERENÍSSIMO_GRÃO_MESTRE") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "SERENISSIMO_GRAO_MESTRE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 500, 150, 150, true);
                                    }
                                }
                            } else if ("ATO_PAEL".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PRESIDENTE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 75, 180, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "PVICE_PRESIDENTE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 180, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SVICE_PRESIDENTE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 370, 180, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "ORADOR")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 150, 60, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SECRETÁRIO")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 300, 60, 150, 120, true);
                                    }
                                }
                            } else if ("ESTATUTO_DIR_EXEC".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {

                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {

                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PRES_EXEC") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "VICE_PRES_EXEC") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SECRETARIA") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SECRETARIA_ADJUNTA")
                                    ) {
                                        //penulitima folha
                                        pageNum = document.getPages().getCount() - 2;
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SOCIAL_CULTURAL") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SOCIAL_CULTURAL_ADJUNTA") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_FINANCEIRA") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_FINANCEIRA_ADJUNTA")
                                    ) {
                                        //ulitima folha
                                        pageNum = document.getPages().getCount() - 1;
                                    }


                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PRES_EXEC")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 580, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "VICE_PRES_EXEC")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 430, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SECRETARIA")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 290, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SECRETARIA_ADJUNTA")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 140, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SOCIAL_CULTURAL")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 580, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_SOCIAL_CULTURAL_ADJUNTA")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 420, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_FINANCEIRA")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 270, 150, 120, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "DIR_FINANCEIRA_ADJUNTA")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 223, 100, 150, 120, true);
                                    }

                                }

                            } else if ("DECRETO_2".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {
                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "SECRETARIO_GUARDA_SELOS")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 60, 150, 140, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SERENÍSSIMO_GRÃO_MESTRE") ||
                                            CommonsUtil.mesmoValor(assinatura.getKey(), "SERENISSIMO_GRAO_MESTRE")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 473, 150, 140, true);
                                    } else if (CommonsUtil.mesmoValor(assinatura.getKey(), "SECRETARIO_ADMIN")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 222, 275, 150, 140, true);
                                    }
                                }
                            } else if ("ALVARA_TRIBUTARIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {
                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PREF_MUNICIPAL")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 180, 50, 203, 105, true);
                                        // lateral direita  - criarASsinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 310, 70, 203, 105);
                                    }
                                }

//                            } else if ("OFÍCIO_ADMINISTRAÇÃO".equals(contratoDocumento.getTipoDocumento().getIdentificacao().toUpperCase())) {
//                                int pageNum = 0;
//                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
//                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PREF_MUNICIPAL")) {
//                                        criarASsinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 180, 50, 203, 105);
//                                        // lateral direita  - criarASsinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 310, 70, 203, 105);
//                                    }
//                                }

                            } else if ("ATO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "DECRETO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "LEI_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())) {
                                int pageNum = document.getPages().getCount() - 1;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    if (CommonsUtil.mesmoValor(assinatura.getKey(), "PREF_MUNICIPAL")) {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 90, 200, 203, 105, true);
                                    } else {
                                        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, 307, 200, 203, 105, true);
                                    }
                                }

                            } else if ("CERTIDAO_TRIBUTARIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "MEMORANDO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "CONVENIO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao()) ||
                                    "CONTRATO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())
                            ) {
                                int pageNum = document.getPages().getCount() - 1;
                                int posicaoInicial = 110 + (qtdAssinaturas % 2) * 110;
                                int altura = 200 - ((iAssinatura - (iAssinatura % 2)) / 2) * 100;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, (posicaoInicial + iAssinatura * 207), altura, 203, 105, true);
                                }

                            } else if ("OFICIO_CONVENIO_PREFVGS".equalsIgnoreCase(contratoDocumento.getTipoDocumento().getIdentificacao())
                            ) {
                                int pageNum = document.getPages().getCount() - 1;
                                int posicaoInicial = 140 + (qtdAssinaturas % 2) * 110;
                                int altura = 200 - ((iAssinatura - (iAssinatura % 2)) / 2) * 100;

                                for (int pageNumAssinatura = 0; pageNumAssinatura < documentAssinatura.getNumberOfPages(); pageNumAssinatura++) {
                                    criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNum, pageNumAssinatura, (posicaoInicial + iAssinatura * 280), altura, 113, 38, true);
                                }

                            } else {
                                for (int iPageAssinatura = 0; iPageAssinatura < documentAssinatura.getPages().getCount(); iPageAssinatura++) {
                                    document.addPage(documentAssinatura.getPage(iPageAssinatura));
                                }
                                break;
                            }
                        }
                        iAssinatura++;

                    }

                    document.setAllSecurityToBeRemoved(true);

                    if (CommonsUtil.booleanValue(mapDocumentoAssinaturas.getDocumento().getTipoDocumento().getValidacaoOnLine()) &&
                            CommonsUtil.booleanValue(mapDocumentoAssinaturas.getDocumento().getTipoDocumento().getQrcode())) {
                        PDPage qrPage = new PDPage();
                        InputStream qrCodePng = QRCodeGeneratorService.getQRImage(sistemaAtributoService.getString(SistemaTipoAtributoEnum.URL_ACESSO_SITE, null) + "/#/validarAssinatura?documento=" + contratoDocumento.getId(), 100, 100);
                        PDDocument documentQrCode = PDDocument.load(reportDataSource.geraRelatorioQrCode(qrCodePng, contratoDocumento.getId()));
                        document.addPage(documentQrCode.getPage(0));
                    }

                    ByteArrayOutputStream saida = new ByteArrayOutputStream();
                    document.save(saida);
                    document.close();
                    if (documentAssinatura != null) {
                        try {
                            documentAssinatura.close();
                            //IOUtils.closeQuietly(documentAssinatura);
                        } catch (Exception e) {
                        }
                    }


                    docuAssinado = saida.toByteArray();
                }
                contratoDocumento.setDocumentoAssinado(docuAssinado);
                contratoDocumentoService.persisteArquivoEmDiretorio(contratoDocumento, TipoArquivoEnum.ASSINADO);
                contratoDocumento.setDocumentoAssinado(null);


                contratoDocumentoRepository.save(contratoDocumento);
            }

            bInsereAssinaturasVisiveis = true;
            return bInsereAssinaturasVisiveis;
        }


        /**
         * Writes given text to PDF page.
         *
         * @param pdfDoc An instance of PDF Document.
         * @param page   An instance of PDF Page.
         * @param text   Text to be written.
         */
        private static void WriteTextToPage(PDDocument pdfDoc, PDPage page, String text) throws Exception {
            WriteTextToPage(pdfDoc, page, text, 4);
        }

        /**
         * Writes given text to PDF page.
         *
         * @param pdfDoc   An instance of PDF Document.
         * @param page     An instance of PDF Page.
         * @param text     Text to be written.
         * @param fontSize Font Size of Text to be written.
         */
        private static void WriteTextToPage(PDDocument pdfDoc, PDPage page, String text, int fontSize) throws Exception {
            int textPosition = (int) (page.getBBox().getHeight() * (60f / 100f));


            PDRectangle pageSize = page.getMediaBox();

            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();


            boolean isLandscape = pageSize.getWidth() > pageSize.getHeight();
            if (isLandscape) {
                pageWidth = pageSize.getHeight();
                pageHeight = pageSize.getWidth();
            }


            PDPageContentStream stream = new PDPageContentStream(pdfDoc, page, PDPageContentStream.AppendMode.APPEND, true);

            if (!isLandscape) {
                gravarTexto(text, fontSize, stream, 10f, 10f, 0d);
            } else {
                float x = page.getCropBox().getWidth() - 10 + page.getCropBox().getLowerLeftX();
                gravarTexto(text, fontSize, stream, x, 10f, 90d);
            }

//            if (isLandscape) {
//                for( int i = 0; i <= 400; i = i+10 )
//                gravarTexto(CommonsUtil.stringValue(i) , fontSize, stream, Float.valueOf(CommonsUtil.stringValue(i)),Float.valueOf(CommonsUtil.stringValue(i)), 0d);
//            }

            stream.close();
        }

        private static void gravarTexto(String text, int fontSize, PDPageContentStream stream, Float x, Float y, Double rotate) throws IOException {
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, fontSize);
            float stringWidth = PDType1Font.HELVETICA.getStringWidth(text) * fontSize
                    / 1000f;


            Matrix matrix = new Matrix();
            //matrix.rotate(rotate);

            stream.setNonStrokingColor(Color.black);
            stream.setStrokingColor(Color.black);

            stream.transform(Matrix.getRotateInstance(Math.toRadians(rotate), x, y));
            stream.showText(text);
            stream.endText();
        }


        private static void WriteVerticalTextToPage(PDDocument pdfDoc, PDPage page, String text, int fontSize) throws Exception {

            PDRectangle pageSize = page.getMediaBox();
            boolean isLandscape = pageSize.getWidth() > pageSize.getHeight();

            PDPageContentStream stream = new PDPageContentStream(pdfDoc, page, PDPageContentStream.AppendMode.APPEND, true);
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, fontSize);
            float stringWidth = PDType1Font.HELVETICA.getStringWidth(text) * fontSize
                    / 1000f;

            Matrix matrix = new Matrix();
            stream.setNonStrokingColor(Color.black);
            stream.setStrokingColor(Color.black);

            if (!isLandscape) {
                stream.transform(Matrix.getRotateInstance(Math.toRadians(90), page.getCropBox().getWidth() - 12 + page.getCropBox().getLowerLeftX(), 20));
            } else {
                stream.transform(Matrix.getRotateInstance(Math.toRadians(90), pageSize.getHeight() - 20, 20));
            }

            stream.showText(text);

            stream.endText();
            stream.close();

        }


        @Override
        public void run() {

            contratoLogService.salva(new ContratoLog(contrato, null, "Inicio da geração das assinaturas do contrato. Identificador: " +
                    contrato.getIdentificador() + " assunto: " + contrato.getAssunto(),
                    DataUtil.getCarimboTempo(), true));
            try {
                gerarAssinaturas();
            } catch (Exception e) {
                contratoLogService.salva(new ContratoLog(contrato, null, "Erro assinando arquivos do contrato. Identificador: " +
                        contrato.getIdentificador() + " assunto: " + contrato.getAssunto() + " erro: " + e.getMessage(),
                        DataUtil.getCarimboTempo(), true));
                e.printStackTrace();
            }
        }
    }

   /* public static void main(String[] args) {
        try{
            PDDocument document = PDDocument.load(Files.readAllBytes(Paths.get("d:/teste/teste.pdf")));
            PDDocument assinatura = PDDocument.load(Files.readAllBytes(Paths.get("d:/teste/assinatura.pdf")));

            criarASsinaturaIndividuaisVisiveis(document, assinatura, 0, 0,65,682,155,64);
            document.setAllSecurityToBeRemoved(true);

            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            document.save(saida);
            document.close();

            Files.write(Paths.get("d:/teste/ass.pdf"), saida.toByteArray());

        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    private static void criarAssinaturaIndividuaisVisiveis(PDDocument document, PDDocument documentAssinatura, int pageNumDocument, int pageNumAssinatura,
                                                           float x, float y, float width, float height) throws IOException {
        criarAssinaturaIndividuaisVisiveis(document, documentAssinatura, pageNumDocument, pageNumAssinatura, x, y, width, height, false);
    }

    private static void criarAssinaturaIndividuaisVisiveis(PDDocument document, PDDocument documentAssinatura, int pageNumDocument, int pageNumAssinatura,
                                                           float x, float y, float width, float height, boolean posicaoFixaCodigo) throws IOException {
        PDPage pageDoc = document.getPage(pageNumDocument);

        PDFRenderer pdfRendererAss = new PDFRenderer(documentAssinatura);

        BufferedImage bim = pdfRendererAss.renderImageWithDPI(pageNumAssinatura, 600, ImageType.RGB);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bim, "png", baos);
        byte[] bytes = baos.toByteArray();

        if (!posicaoFixaCodigo) {
            //Modificações Para Posições Mapeadas
            y = -y + pageDoc.getMediaBox().getUpperRightY() - height;
            if (y - 20 < 0) {
                y = pageDoc.getMediaBox().getHeight() - 20 - height;
            }
        }

        //Creating PDImageXObject object
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, bytes, "Ass");

        //creating the PDPageContentStream object
        PDPageContentStream contents = new PDPageContentStream(document, pageDoc, PDPageContentStream.AppendMode.APPEND, true, true);

        //Drawing the image in the PDF document
        contents.drawImage(pdImage, x, y, width, height);
        //Closing the PDPageContentStream object
        contents.close();
    }
}