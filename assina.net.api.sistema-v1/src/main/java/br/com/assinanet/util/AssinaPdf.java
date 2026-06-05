package br.com.assinanet.util;

import br.com.assinanet.entity.enums.TipoAssinaturaEnum;
import br.com.assinanet.models.AssinaturaExtendsModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssinaPdf extends CreateSignatureBase {
    private final AssinaturaExtendsModel assinaturaExtendsModel;


    @Getter
    @Setter
    private String tsaUrl;

    public AssinaPdf(AssinaturaExtendsModel assinaturaExtendsModel) throws Exception {
        super(assinaturaExtendsModel.getPrivateKey());
        this.assinaturaExtendsModel = assinaturaExtendsModel;
    }

//    public void assina() throws Exception {
//        if (!(new File(this.assinaturaModel.getCaminhoPdf())).exists()) {
//            throw new Exception("Pdf não encontrado");
//        } else {
//            //KeyStore keyStore = this.getKeystore(this.assinaturaModel.getCertificado());
//            FileOutputStream fos = new FileOutputStream(new File(this.assinaturaModel.getCaminhoPdfAssinado()));
//            Throwable var3 = null;
//
//            try {
//                PDDocument doc = PDDocument.load(new File(this.assinaturaModel.getCaminhoPdf()));
//                Throwable var5 = null;
//
//                try {
//                    this.criaAssinatura(doc, fos);
//                } catch (Throwable var28) {
//                    var5 = var28;
//                    throw var28;
//                } finally {
//                    if (doc != null) {
//                        if (var5 != null) {
//                            try {
//                                doc.close();
//                            } catch (Throwable var27) {
//                                var5.addSuppressed(var27);
//                            }
//                        } else {
//                            doc.close();
//                        }
//                    }
//
//                }
//            } catch (Throwable var30) {
//                var3 = var30;
//                throw var30;
//            } finally {
//                if (fos != null) {
//                    if (var3 != null) {
//                        try {
//                            fos.close();
//                        } catch (Throwable var26) {
//                            var3.addSuppressed(var26);
//                        }
//                    } else {
//                        fos.close();
//                    }
//                }
//
//            }
//
//        }
//    }

    public byte[] criaAssinatura(PDDocument document) throws Exception {
        int accessPermissions = SigUtils.getMDPPermission(document);

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        if (accessPermissions == 1) {
            throw new IllegalStateException("Mudanças no documento não são permitidas.");
        } else {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            if (TipoAssinaturaEnum.CHAVEPRIVADA.equals(this.assinaturaExtendsModel.getTipoAssinatura())) {
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_X509_RSA_SHA1);
            } else {
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            }
            //this.visibleSignatureProperties.buildSignature();
            signature.setName(this.assinaturaExtendsModel.getNomeAssinatura());
            signature.setLocation(this.assinaturaExtendsModel.getLocalAssinatura());
            signature.setReason(this.assinaturaExtendsModel.getMotivoAssinatura());
            signature.setSignDate(this.assinaturaExtendsModel.getSignDate());
            signature.setContactInfo("Assina.Net - wwww.assina.net");
            signature.setContents(this.assinaturaExtendsModel.getAssinatura());
            if (accessPermissions == 0) {
                SigUtils.setMDPPermission(document, signature, 2);
            }
            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setPreferredSignatureSize(18944);
            //signatureOptions.setPage(this.visibleSignatureProperties.getPage() - 1);

            document.addSignature(signature, this, signatureOptions);
            document.saveIncremental(saida);
            document.close();

            return saida.toByteArray();
        }
    }


//    private void validaInformacoes(AssinaturaModel assinaturaModel) throws Exception {
//        SigUtils.verifica(assinaturaModel.getCaminhoPdf()).orElseThrow(() -> {
//            return new Exception("É necessário informar o caminho do Pdf.");
//        });
//
//        SigUtils.verifica(assinaturaModel.getSenhaCertificado()).orElseThrow(() -> {
//            return new Exception("É necessário informar a senha do certificado");
//        });
//        SigUtils.verifica(assinaturaModel.getCaminhoPdfAssinado()).orElseThrow(() -> {
//            return new Exception("É necessário informar o destino do pdf");
//        });
//        SigUtils.verifica(assinaturaModel.getLocalAssinatura()).orElseThrow(() -> {
//            return new Exception("É necessário informar o local da assinatura");
//        });
//        SigUtils.verifica(assinaturaModel.getNomeAssinatura()).orElseThrow(() -> {
//            return new Exception("É necessário informar o nome da assinatura");
//        });
//        SigUtils.verifica(assinaturaModel.getMotivoAssinatura()).orElseThrow(() -> {
//            return new Exception("É necessário informar o motivo da assinatura");
//        });
//    }


    @Override
    public byte[] sign(InputStream inputStream) throws IOException {
        return new byte[0];
    }
}
