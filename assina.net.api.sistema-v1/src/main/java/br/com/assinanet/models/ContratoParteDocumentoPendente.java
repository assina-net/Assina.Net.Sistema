package br.com.assinanet.models;

import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoParte;
import lombok.Getter;
import lombok.Setter;

public class ContratoParteDocumentoPendente {

    @Getter
    @Setter
    ContratoParte contratoParte;

    @Getter
    @Setter
    ContratoDocumento contratoDocumento;

    public ContratoParteDocumentoPendente(ContratoParte contratoParte, ContratoDocumento contratoDocumento) {
        this.contratoParte = contratoParte;
        this.contratoDocumento = contratoDocumento;
    }
}
