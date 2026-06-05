package br.com.assinanet.response;

import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoPapel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ComboListResponse {

    UUID id;

    private String value;

    private String label;

    private List<ComboListResponse> subList;

    public ComboListResponse(UUID id, String value, String label) {
        this.id = id;
        this.value = value;
        this.label = label;
    }


    public ComboListResponse(String value, String label, String descricao, List<ComboListResponse> subList) {
        this.value = value;
        this.label = label;
        this.subList = subList;
    }

    public ComboListResponse(TipoDocumento tipoDocumento) {
        this.id = tipoDocumento.getId();
        this.value = tipoDocumento.getIdentificacao();
        this.label = tipoDocumento.getNome();


        this.subList = new ArrayList<>(0);
        for (TipoDocumentoPapel tipoDocumentoPapel : tipoDocumento.getPapeis()) {
            this.subList.add(new ComboListResponse(tipoDocumentoPapel.getPapel()));
        }

    }

    public ComboListResponse(Papel papel) {
        this.id = papel.getId();
        this.value = papel.getIdentificacao();
        this.label = papel.getNome();
    }
}
