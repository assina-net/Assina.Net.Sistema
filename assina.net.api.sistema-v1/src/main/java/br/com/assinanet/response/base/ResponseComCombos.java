package br.com.assinanet.response.base;

import br.com.assinanet.response.ComboListResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ResponseComCombos {

    //objeto para os combos
    public Map<String, List<ComboListResponse>> listCombos = new HashMap<>(0);

}
