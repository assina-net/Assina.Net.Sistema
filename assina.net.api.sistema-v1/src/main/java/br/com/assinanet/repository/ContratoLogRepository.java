package br.com.assinanet.repository;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Repository
public interface ContratoLogRepository extends JpaRepository<ContratoLog, UUID> {

    @Query("from ContratoLog" +
            " where contrato = :contrato " +
            "  and ( contratoDocumento = :contratoDocumento or contratoDocumento is null ) " +
            "  and ( logSistema = false or logSistema is null ) ")
    List<ContratoLog> findByContratoAndContratoDocumento(Contrato contrato, ContratoDocumento contratoDocumento);

    List<ContratoLog> findByContratoAndLogSistema(Contrato contrato, Boolean logSistema);

}
