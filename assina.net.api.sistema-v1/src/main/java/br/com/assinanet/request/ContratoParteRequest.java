package br.com.assinanet.request;

import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ContratoParteRequest {

    @Getter
    @Setter
    UUID clienteSelecionado;

    @Getter
    @Setter
    ContratoParte contratoParte;

    @Getter
    @Setter
    Usuario usuario;

}
