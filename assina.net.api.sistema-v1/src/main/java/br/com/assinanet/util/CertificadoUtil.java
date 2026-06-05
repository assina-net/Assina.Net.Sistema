package br.com.assinanet.util;


import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.exception.NegocioException;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class CertificadoUtil {


    public static String _country = "Westeros",
            _organisation = "Targaryen",
            _location = "Valyria",
            _state = "Essos",
            _issuer = "Some Trusted CA";

    public CertificadoUtil(String country, String organisation, String location, String state, String issuer) {
        _country = country;
        _organisation = organisation;
        _location = location;
        _state = state;
        _issuer = issuer;
    }

    public static X509Certificate generate(PrivateKey privKey, PublicKey pubKey, int duration, String signAlg, boolean isSelfSigned, Usuario usuario) throws Exception {
        Provider BC = new BouncyCastleProvider();

        // distinguished name table.
        X500NameBuilder builder = createStdBuilder();
        builder.addRDN(RFC4519Style.cn, usuario.getPessoa().getNomeRazaoSocial() + ":" + usuario.getPessoa().getCpfCnpj());
        builder.addRDN(RFC4519Style.ou, "Site");
        builder.addRDN(RFC4519Style.uid, usuario.getPessoa().getId().toString());


        //X500NameBuilder builderEmissor = createStdBuilder();

        X500NameBuilder builderEmissor = new X500NameBuilder(RFC4519Style.INSTANCE);
        builderEmissor.addRDN(RFC4519Style.c, "Brasil");
        builderEmissor.addRDN(RFC4519Style.o,"Assina.net");
        builderEmissor.addRDN(RFC4519Style.l, "Campinas");
        builderEmissor.addRDN(RFC4519Style.st, "SP");
        builderEmissor.addRDN(RFC4519Style.ou, "Site");
        builderEmissor.addRDN(RFC4519Style.cn, "Assina.Net");


        // create the certificate
        ContentSigner sigGen = new JcaContentSignerBuilder(signAlg).build(privKey);
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                builderEmissor.build(),    //Issuer
                //cert1,
                new BigInteger(64, new SecureRandom()),      //Serial
                new Date(System.currentTimeMillis() - 50000),   //Valid from
                new Date((long) (System.currentTimeMillis() + duration * 8.65 * Math.pow(10, 7))),    //Valid to
                builder.build(),    //Subject
                pubKey              //Publickey to be associated with the certificate
        );


        certGen.addExtension(new ASN1ObjectIdentifier("2.5.29.15"), true, new X509KeyUsage(X509KeyUsage.encipherOnly));
        //certGen.addExtension(new ASN1ObjectIdentifier("2.5.29.37"), true, new DERSequence(KeyPurposeId.anyExtendedKeyUsage));
        //certGen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), true, new GeneralNames(new GeneralName[] { new GeneralName(GeneralName.rfc822Name, "test@test.test"), new GeneralName(GeneralName.dNSName, "dom.test.test") }));

//        certGen.addExtension((new ASN1ObjectIdentifier("2.16.76.1.3.1")).intern(), false, ("00000000" + usuario.getPessoa().getCpfCnpj() + "00000000000000000000000000     ").getBytes());
//        certGen.addExtension((new ASN1ObjectIdentifier("1.2.840.113549.1.1.11")).intern(), true, ("SHA256withRSA").getBytes());


        certGen.addExtension((new ASN1ObjectIdentifier("2.16.76.1.3.1")), false, new DERPrintableString("00000000" + usuario.getPessoa().getCpfCnpj() + "00000000000000000000000000     "));
        certGen.addExtension((new ASN1ObjectIdentifier("1.2.840.113549.1.1.11")), true, new DERPrintableString("SHA256withRSA"));


        X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));

        cert.checkValidity(new Date());


        if (isSelfSigned) {
            // check verifies in general
            cert.verify(pubKey);
            // check verifies with contained key
            cert.verify(cert.getPublicKey());
        }

        ByteArrayInputStream bIn = new ByteArrayInputStream(cert.getEncoded());
        CertificateFactory fact = CertificateFactory.getInstance("X.509", BC);

        return (X509Certificate) fact.generateCertificate(bIn);
    }

    private static X500NameBuilder createStdBuilder() {
        X500NameBuilder builder = new X500NameBuilder(RFC4519Style.INSTANCE);

        if (_country == null) {
            _country = "Brasil";
            _organisation = "Assina.net";
            _location = "Campinas";
            _state = "SP";
        }
        builder.addRDN(RFC4519Style.c, _country);
        builder.addRDN(RFC4519Style.o, _organisation);
        builder.addRDN(RFC4519Style.l, _location);
        if (_state != null)
            builder.addRDN(RFC4519Style.st, _state);


        //builder.addRDN(RFC4519Style.INSTANCE.stringToValue ("2.5.4.15", ""), _state);

        return builder;
    }


    public static byte[] RecriarP7S(byte[] documentoOriginal, byte[] assinaturaExistente, List<byte[]> assinaturasAdicionar) throws CMSException, IOException, NoSuchProviderException, NoSuchAlgorithmException {

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();


        if (!CommonsUtil.semValor(assinaturaExistente)) {
            CMSSignedData cmsSignedDataExistente = new CMSSignedData(assinaturaExistente);
            gen.addSigners(cmsSignedDataExistente.getSignerInfos());
            gen.addCertificates(cmsSignedDataExistente.getCertificates());
            gen.addCRLs(cmsSignedDataExistente.getCRLs());
        }

        for (byte[] assinaturaAdicionar : assinaturasAdicionar) {
            CMSSignedData cmsSignedDataAdicionar = new CMSSignedData(assinaturaAdicionar);
            gen.addSigners(cmsSignedDataAdicionar.getSignerInfos());
            gen.addCertificates(cmsSignedDataAdicionar.getCertificates());
            gen.addCRLs(cmsSignedDataAdicionar.getCRLs());
        }

        CMSProcessableByteArray msg = new CMSProcessableByteArray(documentoOriginal);
        CMSSignedData sigData = gen.generate(msg, true);

        return sigData.getEncoded();

    }


    /**
     * Monta o Certificado de acordo com a configuração informada
     *
     * @param certificado
     * @return
     * @throws CertificadoException
     * @throws NegocioException
     */
    public static Certificado montaCertificado(Certificado certificado) throws CertificadoException, NegocioException, FileNotFoundException {

        switch (certificado.getTipoCertificado()) {

            case ARQUIVO:
                return CertificadoService.certificadoPfx(certificado.getArquivo(), certificado.getSenha());
            case ARQUIVO_BYTES:
                return CertificadoService.certificadoPfxBytes(certificado.getArquivoBytes(), certificado.getSenha());
            case REPOSITORIO_MAC:
                return CertificadoService.listaCertificadosMac()
                        .stream().filter(cert -> cert.getNome().equals(certificado.getNome())).findFirst()
                        .orElseThrow(() -> new NegocioException("Certificado " + certificado.getNome() + " não encontrado"));
            case REPOSITORIO_WINDOWS:
                return CertificadoService.listaCertificadosWindows()
                        .stream().filter(cert -> cert.getNome().equals(certificado.getNome())).findFirst()
                        .orElseThrow(() -> new NegocioException("Certificado " + certificado.getNome() + " não encontrado"));
            case TOKEN_A3:
                return CertificadoService.certificadoA3(
                        certificado.getMarcaA3(),
                        certificado.getDllA3(),
                        certificado.getSenha(),
                        certificado.getNome(),
                        certificado.getSerialToken());
            default:
                throw new NegocioException("Tipo de certificado " + certificado.getTipoCertificado() + " não configurado");
        }
    }
}
