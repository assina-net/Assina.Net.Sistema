package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface PapelRepository extends JpaRepository<Papel, UUID> {

    @Query(" select distinct pape " +
            " from Papel pape" +
            " left join pape.papelTipoClientes patc " +
            " where pape.cliente = :cliente" +
            " and ( patc.segmento = :segmento or :segmento is null )" +
            " and pape.status = 'ATIVO'  ")
    List<Papel> findByCliente(Cliente cliente, Segmento segmento);



    @Query(" select distinct pape " +
            " from Papel pape " +
            " left join pape.papelTipoClientes patc " +
            " where pape.cliente = :cliente " +
            " and pape.identificacao = :identificacao " +
            " and ( patc.segmento = :segmento or :segmento is null ) "+
            " and ( :status is null or  pape.status = :status )  ")
    Papel findByIdentificacaoAndCliente(String identificacao, Cliente cliente , Segmento segmento, StatusEnum status);



    @Query(" select distinct pape " +
            " from Papel pape  " +
            " left join pape.papelTipoClientes patc  " +
            " where ( pape.cliente = :cliente or patc.segmento = :segmento ) " +
            " AND ( :nome is null or pape.nome = :nome  ) " +
            " AND pape.status = 'ATIVO'"
    )
    Page<Papel> findAllManutencao(Cliente cliente, Segmento segmento, String nome, Pageable pageable);

}
