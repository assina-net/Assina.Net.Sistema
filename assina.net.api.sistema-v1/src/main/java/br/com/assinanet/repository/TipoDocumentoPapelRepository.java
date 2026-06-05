package br.com.assinanet.repository;

import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoPapel;
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
public interface TipoDocumentoPapelRepository extends JpaRepository<TipoDocumentoPapel, UUID> {




    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPapel  " +
            "WHERE tipoDocumento = :tipoDocumento " +
            " and  id not in ( :tipoDocumentoPapelAtivos )" )
    void desativaTipoDocumentoPapelExcluidas(TipoDocumento tipoDocumento, List<UUID> tipoDocumentoPapelAtivos);

    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPapel  " +
            "WHERE tipoDocumento = :tipoDocumento ")
    void desativaTipoDocumentoPapelExcluidas(TipoDocumento tipoDocumento);


}
