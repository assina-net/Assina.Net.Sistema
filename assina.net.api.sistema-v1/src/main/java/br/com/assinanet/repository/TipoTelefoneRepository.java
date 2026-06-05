package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoTelefone;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface TipoTelefoneRepository extends JpaRepository<TipoTelefone, UUID> {

    TipoTelefone findByIdentificacaoAndCliente( String identificacao, Cliente cliente);

    List<TipoTelefone> findByClienteAndStatus(Cliente cliente, StatusEnum status);

}
