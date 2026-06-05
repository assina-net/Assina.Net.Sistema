package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ContratoModel {

    private String remetente;

    private String identificador;

    private String assunto;

    private String tipoContrato;

    private String moeda;

    private Double valorContratro;

    private UUID codigoCustodiante; //id da tabela de cliente

    private int tipoIntegracao;

    private List<ContratoProcuradorModel> procuradores;

    private List<ContratoValidadorModel> validadores;

    private byte[] documento;

    private List<ContratoDuplicatasModel> duplicatas;

    private byte[] documentoDuplicatas;

    private List<ContratoCartaCessaoModel> documentosCartasCessao;

    public ContratoModel() {
        procuradores = new ArrayList<>(0);
        validadores = new ArrayList<>(0);
        duplicatas = new ArrayList<>(0);
    }
}
