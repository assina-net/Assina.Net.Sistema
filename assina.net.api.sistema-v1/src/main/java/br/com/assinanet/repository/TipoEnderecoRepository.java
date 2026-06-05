package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoEndereco;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface TipoEnderecoRepository extends JpaRepository<TipoEndereco, UUID> {


    List<TipoEndereco> findByClienteAndStatus(Cliente cliente, StatusEnum status);

    TipoEndereco findByIdentificacao( String identificacao);

}
