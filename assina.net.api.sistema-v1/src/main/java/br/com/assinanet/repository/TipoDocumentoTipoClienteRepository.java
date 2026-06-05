package br.com.assinanet.repository;

import br.com.assinanet.entity.TipoDocumento;
import br.com.assinanet.entity.TipoDocumentoTipoCliente;
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
public interface TipoDocumentoTipoClienteRepository extends JpaRepository<TipoDocumentoTipoCliente, UUID> {




    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoTipoCliente  " +
            "WHERE tipoDocumento = :tipoDocumento " +
            " and  id not in ( :tipoDocumentoTipoClienteAtivos )" )
    void desativaTipoDocumentoExcluidas(TipoDocumento tipoDocumento, List<UUID> tipoDocumentoTipoClienteAtivos);

    @Modifying
    @Transactional
    @Query(" DELETE FROM TipoDocumentoTipoCliente  " +
            "WHERE tipoDocumento = :tipoDocumento ")
    void desativaTipoDocumentoExcluidas(TipoDocumento tipoDocumento);


}
