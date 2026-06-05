package br.com.assinanet.request;

import br.com.assinanet.entity.ContratoLog;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.response.ContratoResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContratoLiberacaoLoteRequest {

    @Getter
    @Setter
    Usuario usuario;

    @Getter
    @Setter
    List<UUID> contratos;

}
