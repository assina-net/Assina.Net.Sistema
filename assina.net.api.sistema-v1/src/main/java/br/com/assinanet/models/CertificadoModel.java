package br.com.assinanet.models;

import br.com.swconsultoria.certificado.Certificado;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class CertificadoModel extends Certificado {

    private String responsavelNome;

    private String responsavelCpf;

    private String tipoPessoa;

    private String emissorCertificado;

    private String chaveUso;

    private String tipoCertificadoAN;

    private Boolean podeAssinar = false;

    public CertificadoModel() {
    }

    public CertificadoModel(Certificado certificado) {

        BeanUtils.copyProperties(certificado, this);

    }
}
