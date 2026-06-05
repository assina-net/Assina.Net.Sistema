package br.com.assinanet.response;

import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoDocumentoPapel;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;

import java.util.*;
@Getter
@Setter
public class ContratoDocumentoResponse {

    UUID id;


    UUID idDiretorio;

    String nomeDocumento;


    StatusEnum status;

    String descStatus;

    TipoDocumento tipoDocumento;

    String descTipoDocumentoEnum;

    StatusDocumentoEnum statusDocumento;

    String descStatusDocumento;


    String documento ;

    String documentoOriginalSHA256;

    private List<ContratoDocumentoPapel> papel = new ArrayList<>(0);

    public ContratoDocumentoResponse() {
        super();
    }

    public ContratoDocumentoResponse(ContratoDocumento contratoDocumento) {
        BeanUtils.copyProperties(contratoDocumento, this);

        Collections.sort(papel, (one, another) -> {
            int cmp = another.getPapel().getNome().compareTo(one.getPapel().getNome());
            return cmp;
        });


    }
}
