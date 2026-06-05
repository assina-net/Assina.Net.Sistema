package br.com.assinanet.service;

import br.com.assinanet.models.CertificadoModel;
import org.bouncycastle.asn1.*;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CertificadoAdicionalService {


    private static final DERObjectIdentifier RESPONSAVEL = new DERObjectIdentifier("2.16.76.1.3.2");
    private static final DERObjectIdentifier RESPONSAVELCPF = new DERObjectIdentifier("2.16.76.1.3.4");
    private static final DERObjectIdentifier CPF = new DERObjectIdentifier("2.16.76.1.3.1");
    private static final DERObjectIdentifier CNPJ = new DERObjectIdentifier("2.16.76.1.3.3");
    private static final DERObjectIdentifier CHAVEUSO = new DERObjectIdentifier("2.5.29.15");


    public static void PreecheDadosAdicionais(CertificadoModel certificadoModel, X509Certificate cert1 ) {


        try {
            if (cert1.getSubjectDN().toString().contains("RFB e-CNPJ A1") ||
                    cert1.getSubjectDN().toString().contains("RFB e-CPF A1")){
                certificadoModel.setTipoCertificadoAN("A1");
            }else if (cert1.getSubjectDN().toString().contains("RFB e-CNPJ A3") ||
                    cert1.getSubjectDN().toString().contains("RFB e-CPF A3")){
                certificadoModel.setTipoCertificadoAN("A3");
            }

            certificadoModel.setSerialToken(cert1.getSerialNumber() + "");
            String emissor = cert1.getIssuerDN().getName().split(",")[0];
            emissor = emissor.substring(emissor.indexOf("=") + 1);
            certificadoModel.setEmissorCertificado(emissor);

            getInfoAdicionais(cert1, certificadoModel);
        } catch (CertificateParsingException e) {
            e.printStackTrace();
        }

    }


    private static void getInfoAdicionais(X509Certificate certificate, CertificadoModel certificadoModel)
            throws CertificateParsingException {

        Map<String, String> result = new HashMap<>(0);

        Collection<?> alternativeNames = X509ExtensionUtil.getSubjectAlternativeNames(certificate);
        for (Object alternativeName : alternativeNames) {
            if (alternativeName instanceof ArrayList) {
                ArrayList<?> listOfValues = (ArrayList<?>) alternativeName;
                Object value = listOfValues.get(1);
                if (value instanceof DERSequence) {
                    DERSequence derSequence = (DERSequence) value;
                    DERObjectIdentifier derObjectIdentifier = (DERObjectIdentifier) derSequence.getObjectAt(0);
                    DERTaggedObject derTaggedObject = (DERTaggedObject) derSequence.getObjectAt(1);
                    DERObject derObject = derTaggedObject.getObject();

                    String valueOfTag = "";
                    if (derObject instanceof DEROctetString) {
                        DEROctetString octet = (DEROctetString) derObject;
                        valueOfTag = new String(octet.getOctets());
                    } else if (derObject instanceof DERPrintableString) {
                        DERPrintableString octet = (DERPrintableString) derObject;
                        valueOfTag = new String(octet.getOctets());
                    } else if (derObject instanceof DERUTF8String) {
                        DERUTF8String str = (DERUTF8String) derObject;
                        valueOfTag = str.getString();
                    }

                    if ((valueOfTag != null) && (!"".equals(valueOfTag))) {
                        if (derObjectIdentifier.equals(RESPONSAVEL)) {
                            certificadoModel.setResponsavelNome(valueOfTag);
                        } else if (derObjectIdentifier.equals(RESPONSAVELCPF)) {
                            certificadoModel.setResponsavelCpf(valueOfTag.substring(8, 19));
                        } else if (derObjectIdentifier.equals(CPF)) {
                            certificadoModel.setTipoPessoa("FISICA");
                            if (certificadoModel.getCnpjCpf() == null)
                                certificadoModel.setCnpjCpf(valueOfTag.substring(8, 19));
                        } else if (derObjectIdentifier.equals(CNPJ)) {
                            certificadoModel.setTipoPessoa("JURIDICA");
                            if (certificadoModel.getCnpjCpf() == null)
                                certificadoModel.setCnpjCpf(valueOfTag);
                        } else if (derObjectIdentifier.equals(CHAVEUSO)) {
                            certificadoModel.setChaveUso(valueOfTag);
                        }
                    }
                }
            }
        }
    }

}
