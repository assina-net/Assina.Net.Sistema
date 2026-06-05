package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Cliente findByPessoa(Pessoa pessoa);

    List<Cliente> findBySegmento(Segmento segmento);

    List<Cliente> findByStatus(StatusEnum status);

    @Query("SELECT c FROM Cliente c " +
            "LEFT JOIN FETCH c.pessoa " +
            "LEFT JOIN FETCH c.segmento " +
            "LEFT JOIN FETCH c.plano " +
            "WHERE c.status = :status")
    List<Cliente> findByStatusWithAssociations(@Param("status") StatusEnum status);

    @Query(" select clie " +
            "from Cliente clie " +
            "     join clie.pessoa pess " +
            " WHERE clie.pessoa.cpfCnpj = :cnpj " +
            " and clie.status = 'ATIVO' ")
    Cliente findByCpfCnpj(String cnpj);

    @Query(" select distinct cont.custodiante " +
            " from Contrato cont " +
            "      join ContratoParte copa on ( cont = copa.contrato ) " +
            " where copa.cpfCnpj = :cpfCnpj ")
    List<Cliente> findByContratoCpfCnpj(String cpfCnpj);
}
