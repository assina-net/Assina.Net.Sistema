package br.com.assinanet.response;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusContratoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.util.CommonsUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
public class ContratoResponse {


    private UUID id;
    private UUID idDiretorio;
    private Usuario remetente;

    private Date dataCriacao;

    private String identificador;

    private String assunto;

    private String tipoContrato;

    private String moeda;

    private BigDecimal valorContratro;

    private Cliente custodiante; //id da tabela de usuario

    private Integer tipoIntegracao;

    private StatusContratoEnum statusContrato;

    private Boolean liberadoAssinatura;

    private Boolean validado;

    private String validacaoMensagem;

    public String getDescStatusContrato() {
        if (this.statusContrato == null) {
            return null;
        }
        return this.statusContrato.getDescricao();
    }

    private StatusEnum status;

    public String getDescStatus() {
        if (this.status == null) {
            return null;
        }
        return this.status.getDescricao();
    }

    private Date dataSolicitacaoAssinatura;

    private String gmtSolicitacaoAssinatura;

    private String carimboTempoSolicitacaoAssinatura;

    private Usuario usuarioSolicitacaoAssinatura;

    private List<ContratoDocumentoResponse> documentos;

    private List<ContratoParteResponse> partes;


    public ContratoResponse() {
    }

    public ContratoResponse(Contrato contrato) {
        BeanUtils.copyProperties(contrato, this, "partes", "documentos");
        CarregaPartes(contrato);
        CarregaDocumentos(contrato);
    }


    public ContratoResponse(Contrato contrato, String... ignorar) {
        BeanUtils.copyProperties(contrato, this, "partes", "documentos");
        if (!Arrays.asList(ignorar).contains("partes")) {
            CarregaPartes(contrato);
        }
        if (!Arrays.asList(ignorar).contains("documentos")) {
            CarregaDocumentos(contrato);
        }

    }

    private void CarregaDocumentos(Contrato contrato) {
        List<ContratoDocumentoResponse> contratoDocumento = new ArrayList<>(0);
        List<ContratoDocumento> documentos = contrato.getDocumentos();
        documentos.forEach(documento -> {

            if (!CommonsUtil.mesmoValor(documento.getTipoDocumento().getIdentificacao(), "CSVDUPLICATAS")) {
                contratoDocumento.add(new ContratoDocumentoResponse(documento));
            }
        });

        contratoDocumento.sort((o1, o2) -> {
            int compare = Integer.compare(CommonsUtil.intValue(o1.getTipoDocumento().getOrdem()), CommonsUtil.intValue(o2.getTipoDocumento().getOrdem()));
            if (compare != 0) {
                return compare;
            }

            compare = CommonsUtil.compare(o1.getNomeDocumento(), o2.getNomeDocumento());
            return compare;
        });

        this.setDocumentos(contratoDocumento);
    }

    private void CarregaPartes(Contrato contrato) {
        List<ContratoParteResponse> contratoParte = new ArrayList<>(0);
        List<ContratoParte> partes = contrato.getPartes();
        partes.forEach(parte -> {
            if (StatusEnum.ATIVO.equals(parte.getStatus()) && parte.getContratoPartePJ() == null) {
                ContratoParteResponse contratoParteResponse = new ContratoParteResponse(parte);

//                for (int i = contratoParteResponse.getContatos().size() - 1; i >= 0; i--) {
//                    if (StatusEnum.INATIVO.equals(contratoParteResponse.getContatos().get(i).getStatus())) {
//                        contratoParteResponse.getContatos().remove(i);
//                    }
//                }
                contratoParte.add(contratoParteResponse);
            }
        });
        this.setPartes(contratoParte);
    }

}

