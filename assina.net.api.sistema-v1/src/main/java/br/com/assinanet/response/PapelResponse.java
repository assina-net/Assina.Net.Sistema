package br.com.assinanet.response;

import br.com.assinanet.entity.Papel;
import br.com.assinanet.response.base.ResponseComCombos;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PapelResponse extends ResponseComCombos {

    @Getter
    @Setter
    Papel papel;

}


