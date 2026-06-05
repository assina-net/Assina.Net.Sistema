package br.com.assinanet.request;

import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
@Getter
@Setter
public class ContratoParteAssinaturaRequest {


    ContratoParte contratoParte;

    Usuario usuario;

    String ipCliente;

    String servidor;

    boolean servidorSeguro;

    String protocolo;

    Integer serverPort = 80;

    String nomeCertificado;

    String pinCertificado;

    List<UUID> contratos;

}

