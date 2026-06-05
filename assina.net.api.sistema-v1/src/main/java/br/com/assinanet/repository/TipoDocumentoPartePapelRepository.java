package br.com.assinanet.repository;

import br.com.assinanet.entity.TipoDocumentoParte;
import br.com.assinanet.entity.TipoDocumentoPartePapel;
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
public interface TipoDocumentoPartePapelRepository extends JpaRepository<TipoDocumentoPartePapel, UUID> {

    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPartePapel  " +
            "WHERE tipoDocumentoParte = :tipoDocumentoParte " +
            " and  id not in ( :tipoDocumentoPapelAtivos )" )
    void desativaTipoDocumentoPapelExcluidas(TipoDocumentoParte tipoDocumentoParte, List<UUID> tipoDocumentoPapelAtivos);

    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoPartePapel  " +
            "WHERE tipoDocumentoParte = :tipoDocumentoParte ")
    void desativaTipoDocumentoPapelExcluidas(TipoDocumentoParte tipoDocumentoParte);


}
