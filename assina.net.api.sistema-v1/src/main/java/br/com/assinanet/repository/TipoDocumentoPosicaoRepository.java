package br.com.assinanet.repository;

import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoPosicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface TipoDocumentoPosicaoRepository extends JpaRepository<TipoDocumentoPosicao, UUID> {




    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPosicao  " +
            "WHERE tipoDocumento = :tipoDocumento " +
            " and  id not in ( :posicoesAtivas )" )
    void desativaTipoDocumentoPosicoesExcluidas(TipoDocumento tipoDocumento, List<UUID> posicoesAtivas);

    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPosicao  " +
            "WHERE tipoDocumento = :tipoDocumento ")
    void desativaTipoDocumentoPosicoesExcluidas(TipoDocumento tipoDocumento);


}
