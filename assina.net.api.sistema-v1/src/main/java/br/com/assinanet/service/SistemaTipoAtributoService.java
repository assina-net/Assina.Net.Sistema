package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.repository.SistemaAtributoRepository;
import br.com.assinanet.repository.SistemaTipoAtributoRepository;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class SistemaTipoAtributoService {

    private final SistemaTipoAtributoRepository sistemaTipoAtributoRepository;

    public SistemaTipoAtributoService(SistemaTipoAtributoRepository sistemaTipoAtributoRepository) {
        this.sistemaTipoAtributoRepository = sistemaTipoAtributoRepository;
    }

    public SistemaTipoAtributo findByTipoAtributo(SistemaTipoAtributoEnum tipoAtributo){
        return this.sistemaTipoAtributoRepository.findByTipoAtributo( tipoAtributo);
    }


}
