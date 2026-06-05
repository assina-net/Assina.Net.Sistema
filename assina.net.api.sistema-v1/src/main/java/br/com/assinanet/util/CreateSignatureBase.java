package br.com.assinanet.util;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public abstract class CreateSignatureBase implements SignatureInterface {
    private PrivateKey privateKey;
    private Certificate[] certificateChain;
    private String tsaUrl;
    private boolean externalSigning;

    public CreateSignatureBase(PrivateKey privateKey) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        this.privateKey = privateKey;
    }

    public CreateSignatureBase(KeyStore keystore, char[] pin) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        Enumeration<String> aliases = keystore.aliases();
        Certificate cert = null;

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            this.setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
            Certificate[] certChain = keystore.getCertificateChain(alias);
            if (certChain != null) {
                this.setCertificateChain(certChain);
                cert = certChain[0];
                if (cert instanceof X509Certificate) {
                    ((X509Certificate) cert).checkValidity();
                    SigUtils.checkCertificateUsage((X509Certificate) cert);
                }
                break;
            }
        }

        if (cert == null) {
            throw new IOException("Certificado não encontrado");
        }
    }

    public final void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public final void setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }

    public void setTsaUrl(String tsaUrl) {
        this.tsaUrl = tsaUrl;
    }

//    public byte[] sign(InputStream content) throws IOException {
//        try {
//            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
//            X509Certificate cert = (X509Certificate)this.certificateChain[0];
//            ContentSigner sha1Signer = (new JcaContentSignerBuilder("SHA256WithRSA")).build(this.privateKey);
//            gen.addSignerInfoGenerator((new JcaSignerInfoGeneratorBuilder((new JcaDigestCalculatorProviderBuilder()).build())).build(sha1Signer, cert));
//            gen.addCertificates(new JcaCertStore(Arrays.asList(this.certificateChain)));
//            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
//            CMSSignedData signedData = gen.generate(msg, false);
//            if (this.tsaUrl != null && this.tsaUrl.length() > 0) {
//                ValidationTimeStamp validation = new ValidationTimeStamp(this.tsaUrl);
//                signedData = validation.addSignedTimeStamp(signedData);
//            }
//
//            return signedData.getEncoded();
//        } catch (CMSException | OperatorCreationException | GeneralSecurityException var8) {
//            throw new IOException(var8);
//        }
//    }
}

