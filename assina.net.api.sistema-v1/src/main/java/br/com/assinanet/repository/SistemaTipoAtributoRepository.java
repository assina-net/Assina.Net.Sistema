package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface SistemaTipoAtributoRepository extends JpaRepository<SistemaTipoAtributo, UUID> {

    SistemaTipoAtributo findByTipoAtributo(SistemaTipoAtributoEnum tipoAtributo);
}
