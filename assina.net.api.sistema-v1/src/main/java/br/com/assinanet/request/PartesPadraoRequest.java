package br.com.assinanet.request;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PartesPadraoRequest {

     Cliente cliente;

     TipoDocumento tipoDocumento;

}
