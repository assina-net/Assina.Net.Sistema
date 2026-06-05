package br.com.assinanet.repository;

import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoParte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface TipoDocumentoParteRepository extends JpaRepository<TipoDocumentoParte, UUID> {

    @Query("FROM TipoDocumentoParte  " +
            "WHERE tipoDocumento = :tipoDocumento " +
            " and  id not in ( :tipoDocumentoParteAtivos )" )
    List<TipoDocumentoParte> desativaTipoDocumentoParteExcluidas(TipoDocumento tipoDocumento, List<UUID> tipoDocumentoParteAtivos);

    @Query("FROM TipoDocumentoParte  " +
            "WHERE tipoDocumento = :tipoDocumento ")
    List<TipoDocumentoParte> desativaTipoDocumentoParteExcluidas(TipoDocumento tipoDocumento);


}
