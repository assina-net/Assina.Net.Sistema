package br.com.assinanet.response.SistemaAtributo;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SistemaAtributoCliente {
    //este id é do SistemaTipoAtributo, pois lista todos na tela
    private UUID id;

    private UUID idSistemaATributo;

    private SistemaTipoAtributo tipoAtributo;

    private String valorAtributo;

    private Date dataAlteracao;

    private StatusEnum status;

    private Cliente cliente;



    public SistemaAtributoCliente(SistemaTipoAtributo sistemaTipoAtributo, SistemaAtributo sistemaAtributo) {
        id = sistemaTipoAtributo.getId();

        tipoAtributo = sistemaTipoAtributo;
        if  ( sistemaAtributo != null ) {
            idSistemaATributo = sistemaAtributo.getId();
            valorAtributo = sistemaAtributo.getValorAtributo();
            dataAlteracao = sistemaAtributo.getDataAlteracao();
            status  = sistemaAtributo.getStatus();
            cliente = sistemaAtributo.getCliente();
        }
    }


}
