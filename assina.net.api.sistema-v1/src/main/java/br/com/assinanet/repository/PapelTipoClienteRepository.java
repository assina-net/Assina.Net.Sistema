package br.com.assinanet.repository;

import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.PapelTipoCliente;
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
public interface PapelTipoClienteRepository extends JpaRepository<PapelTipoCliente, UUID> {


    @Modifying
    @Transactional
    @Query(" DELETE FROM PapelTipoCliente  " +
            "WHERE papel = :papel " +
            " and  id not in ( :papelTipoClienteAtivos )" )
    void desativaTipoDocumentoExcluidas(Papel papel, List<UUID> papelTipoClienteAtivos);

    @Modifying
    @Transactional
    @Query(" DELETE FROM PapelTipoCliente  " +
            "WHERE papel = :papel ")
    void desativaTipoDocumentoExcluidas(Papel papel);


}
