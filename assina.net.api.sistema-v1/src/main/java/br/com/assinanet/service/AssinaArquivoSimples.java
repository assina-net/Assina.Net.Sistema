package br.com.assinanet.service;

import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.models.AssinaturaExtendsModel;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import br.com.swconsultoria.pdf_signature.dom.AssinaturaModel;
import br.com.swconsultoria.pdf_signature.utils.SigUtils;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssinaArquivoSimples {
    private final ContratoDocumentoService contratoDocumentoService;
    private final AssinaturaExtendsModel assinaturaModel;

    ContratoDocumento documento;

    @Getter
    @Setter
    private String tsaUrl;

    public AssinaArquivoSimples(AssinaturaExtendsModel assinaturaModel, ContratoDocumento documento, ContratoDocumentoService contratoDocumentoService) throws Exception {
        this.assinaturaModel = assinaturaModel;
        this.documento = documento;
        this.contratoDocumentoService = contratoDocumentoService;

        if (assinaturaModel.getTsa() != null && !assinaturaModel.getTsa().equals("")) {
            setTsaUrl(assinaturaModel.getTsa());
        }
    }

    public byte[] assina() throws Exception {

        ContratoDocumentoVisualizaResponse contratoDocumentoVisualizaResponse = contratoDocumentoService.getArquivoEmDiretorio( Optional.of(documento) , documento.getDocumentoOriginalSHA256(), true, true);
        if (contratoDocumentoVisualizaResponse.getDocumentoBytes() == null) {
            throw new Exception("Documento Original não encontrado");
        }

        InputStream documentoOriginal = new ByteArrayInputStream(contratoDocumentoVisualizaResponse.getDocumentoBytes());

        return sign(documentoOriginal);

    }

    /**
     * Valida os dados
     *
     * @param assinaturaModel
     * @throws Exception
     */
    private void validaInformacoes(AssinaturaModel assinaturaModel) throws Exception {
        SigUtils.verifica(assinaturaModel.getCertificado()).orElseThrow(() -> new Exception("É necessário informar o certificado."));
        SigUtils.verifica(assinaturaModel.getSenhaCertificado()).orElseThrow(() -> new Exception("É necessário informar a senha do certificado"));
    }

    public byte[] sign(InputStream content) throws IOException {
        try {


            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            List certList = new ArrayList();
            certList.add(this.assinaturaModel.getCertificadoX509());

            Store certs = new JcaCertStore(certList);

            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(this.assinaturaModel.getPrivateKey());
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, this.assinaturaModel.getCertificadoX509()));
            gen.addCertificates(certs);

            CMSProcessableByteArray msg = new CMSProcessableByteArray(toByteArray(content));
            CMSSignedData signedData = gen.generate(msg, false);

//            if (assinaturaModel.getTsa() != null && assinaturaModel.getTsa().length() > 0) {
//                ValidationTimeStamp validation = new ValidationTimeStamp(assinaturaModel.getTsa());
//                signedData = validation.addSignedTimeStamp(signedData);
//            }

            return signedData.getEncoded();

        } catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
            throw new IOException(e);
        }
    }

    public static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

}

