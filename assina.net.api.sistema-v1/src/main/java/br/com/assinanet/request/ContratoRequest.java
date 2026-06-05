package br.com.assinanet.request;

import br.com.assinanet.entity.ContratoLog;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.response.ContratoParteResponse;
import br.com.assinanet.response.ContratoResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ContratoRequest {

    private UUID id;

    ContratoResponse contrato;

    List<ContratoLog> contratoLog;

    Map<String, List<ComboListResponse>> listCombos = new HashMap<>(0);

    boolean assinarDocumento;


}
