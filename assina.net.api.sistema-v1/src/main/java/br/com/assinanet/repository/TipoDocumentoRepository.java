package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Segmento;
import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoParte;
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
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, UUID> {


    List<TipoDocumento> findByIdIn(List<UUID> ids);

    @Query(" select distinct tido " +
            " from TipoDocumento tido" +
            " left join tido.tipoDocumentoTipoClientes titc " +
            " where tido.cliente = :cliente" +
            " and ( titc.segmento = :segmento or :segmento is null )" +
            " and tido.status = 'ATIVO'  ")
    List<TipoDocumento> findByCliente(Cliente cliente, Segmento segmento);


    @Query(" select distinct tido " +
            " from TipoDocumento tido" +
            " left join tido.tipoDocumentoTipoClientes titc " +
            " where tido.cliente = :cliente" +
            " and tido.identificacao = :identificacao " +
            " and ( titc.segmento = :segmento or :segmento is null )" +
            " and ( tido.status = :status or  :status is null )  ")
    TipoDocumento findByIdentificacaoAndCliente(String identificacao, Cliente cliente, Segmento segmento, String status);


    @Query(" select distinct tido " +
            " from TipoDocumento tido" +
            " left join tido.tipoDocumentoTipoClientes titc " +
            " where ( tido.cliente = :cliente or titc.segmento = :segmento ) " +
            " AND ( :nome is null or tido.nome = :nome  ) " +
            " AND ( :assina is null or tido.assina = :assina  )" +
            " AND tido.status = 'ATIVO'")
    Page<TipoDocumento> findAllManutencao(Cliente cliente, Segmento segmento, String nome, Boolean assina, Pageable pageable);


    @Query(" select distinct tidp " +
            " from TipoDocumentoParte tidp" +
            " left join tidp.pessoa pess " +
            " where  tidp.tipoDocumento = :tipoDocumento  and" +
            " tidp.cliente = :cliente " )
    List<TipoDocumentoParte> findPartes(Cliente cliente, TipoDocumento tipoDocumento);

}
