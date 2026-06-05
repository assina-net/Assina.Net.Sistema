package br.com.assinanet.entity.projection;

import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.rest.core.config.Projection;

import java.util.UUID;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
@Projection(name = "ContratoDocumentoProjection", types = {ContratoDocumento.class})
public interface ContratoDocumentoProjection {

    UUID getId();

    String getNomeDocumento();

    StatusEnum getStatus();

    String getDescStatus();

    TipoDocumento getTipoDocumento();

    StatusDocumentoEnum getStatusDocumento();

    String getDescStatusDocumento();

    byte[] documento = new byte[]{};


}

