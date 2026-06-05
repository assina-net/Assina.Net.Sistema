package br.com.assinanet.repository;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.ContratoParteDocumento;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.projection.ContratoDocumentoProjection;
import br.com.assinanet.response.ContratoDocumentoVisualizaAssinaturasResponse;
import br.com.assinanet.response.ContratoDocumentoVisualizaResponse;
import org.springframework.data.domain.Pageable;
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
public interface ContratoDocumentoRepository extends JpaRepository<ContratoDocumento, UUID> {


    ContratoDocumento findByDocumentoOriginalSHA256(String hash);


    @Query(" select new br.com.assinanet.response.ContratoDocumentoVisualizaResponse( id,  documentoOriginal )" +
            " from ContratoDocumento c " +
            "WHERE id = :id ")
    ContratoDocumentoVisualizaResponse getDocumentoOriginal(UUID id);

    @Query(" select new br.com.assinanet.response.ContratoDocumentoVisualizaResponse( id,  documentoAssinado )" +
            " from ContratoDocumento c " +
            "WHERE id = :id ")
    ContratoDocumentoVisualizaResponse getDocumentoAssinadoOnly(UUID id);


    @Query(" select new br.com.assinanet.response.ContratoDocumentoVisualizaResponse( cd.id, " +
            "  cd.documentoAssinado, td.validacaoOnLine, c.custodiante )" +
            " from ContratoDocumento cd join cd.tipoDocumento td join cd.contrato c " +
            "WHERE cd.id = :id ")
    ContratoDocumentoVisualizaResponse getDocumentoAssinado(UUID id);



    @Query(" select new br.com.assinanet.response.ContratoDocumentoVisualizaAssinaturasResponse( " +
            " copd.id, " +
            " copa.nomeRazaoSocial, " +
            " copp.papel, " +
            " copd.carimboTempoAssinatura," +
            " copd.assinaturaDigital," +
            " copd.tipoAssinatura )" +
            " from ContratoParteDocumento copd" +
            " join copd.contratoPartePapel copp " +
            " join copp.contratoParte copa " +
            " join copd.documento codo" +
            " WHERE codo.id = :id ")
    List<ContratoDocumentoVisualizaAssinaturasResponse> getAssinaturas(UUID id);

    @Query(" select codo from ContratoDocumento codo " +
            "WHERE codo.contrato = :contrato " +
            " and codo.status = :status " +
            " and codo.tipoDocumento.assina = true ")
    List<ContratoDocumentoProjection> findByContratoStatus(Contrato contrato, StatusEnum status);

    @Query(" select codo from ContratoDocumento codo " +
            "WHERE codo.contrato = :contrato " +
            " and codo.status = :status " +
            " and codo.tipoDocumento.assina = true ")
    List<ContratoDocumento> documentosParaAssinar(Contrato contrato, StatusEnum status);

    @Query(" select codo from ContratoDocumento codo " +
            "WHERE codo.contrato = :contrato " +
            " and codo.status = :status " +
            " and codo.tipoDocumento.assina = false ")
    List<ContratoDocumento> documentosSemAssinatura(Contrato contrato, StatusEnum status);

    @Query("select distinct codo" +
            " from  Contrato cont" +
            "  join cont.partes copa on  copa.status = 'ATIVO' " +
            "  join copa.papel cppa " +
            "  join cont.documentos codo on codo.status = 'ATIVO'" +
            "  join codo.papel cdpa  " +
            "  left join ContratoParteDocumento cpdo on (  codo = cpdo.documento  and" +
            "                                              cppa =  cpdo.contratoPartePapel   )" +
            " where cont = :contrato" +
            " and cppa.papel = cdpa.papel " +
            " and codo.status = :status " +
            " and copa= :contratoParte " +
            " and cpdo is null" +
            " and codo.tipoDocumento.assina = true")
    List<ContratoDocumento> BuscaDocumentosParaAssinar(Contrato contrato, ContratoParte contratoParte, StatusEnum status);

    @Query("select distinct codo" +
            " from  Contrato cont" +
            "  join cont.partes copa on copa.status = 'ATIVO' " +
            "  join copa.papel cppa " +
            "  join cont.documentos codo on codo.status = 'ATIVO'  " +
            "  join codo.papel cdpa  " +
            " where cont = :contrato " +
            " and ( cppa.papel = cdpa.papel or cppa.papel.identificacao = 'OBSERVADOR' )  " +
            " and codo.status = :status " +
            " and copa in (:lstContratoParte )" +
            " and codo.tipoDocumento.assina = true ")
    List<ContratoDocumento> BuscaDocumentosParaAssinarProjection(Contrato contrato, List<ContratoParte> lstContratoParte, StatusEnum status);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoDocumento codo set codo.status = 'INATIVO' " +
            "WHERE codo.contrato = :contrato " +
            " and  not codo.id in ( :documentosAtivos )" +
            " and codo.status = 'ATIVO' ")
    void desativaDocumentosExcluidos(Contrato contrato, List<UUID> documentosAtivos);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoDocumento codo set codo.status = 'INATIVO' " +
            "WHERE codo.contrato = :contrato " +
            " and codo.status = 'ATIVO' ")
    void desativaDocumentosExcluidos(Contrato contrato);

    @Modifying
    @Transactional
    @Query(" DELETE" +
            " FROM ContratoDocumentoPapel cdpa " +
            " WHERE cdpa.contratoDocumento.id in ( :documentosAtivos ) " +
            " AND not cdpa.id in ( :documentosPapelAtivos )")
    void excluiDocumentosPapelExcluidos(List<UUID> documentosAtivos, List<UUID> documentosPapelAtivos);



    @Query("from ContratoDocumento" +
            " where  (  documentoOriginal is not null and documentoOriginalCompactado is null )" +
            " or (  documentoAssinado is not null and documentoAssinadoCompactado is null ) ")
    List<ContratoDocumento> verificaDocumentosNaoCompactados( Pageable pageable);

    @Query("select distinct cont " +
            " from Contrato cont" +
            "      join cont.documentos codo " +
            //"      join cont.custodiante cudo " +
            " where codo.documentoOriginal is not null" +
            "  order by cont.dataCriacao desc"
    )
    List<Contrato> verificaDocumentosParaMigracao();

    List<ContratoDocumento> findFirst100ByDocumentoOriginalIsNotNull();

}
