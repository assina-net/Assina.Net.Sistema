package br.com.assinanet.repository;

import br.com.assinanet.entity.*;
import br.com.assinanet.models.ContratoParteDocumentoPendente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface ContratoParteDocumentoRepository extends JpaRepository<ContratoParteDocumento, UUID> {

    @Query("from ContratoParteDocumento" +
            " where contratoPartePapel.contratoParte = :contratoParte ")
    List<ContratoParteDocumento> findByContratoParte(ContratoParte contratoParte);

    @Query("select new br.com.assinanet.models.ContratoParteDocumentoPendente ( copa, codo ) " +
            " from Contrato cont" +
            "      join cont.partes copa on copa.status = 'ATIVO' and copa.tipoPessoa = 'FISICA' " +
            "      join copa.papel cppa " +
            "      join cont.documentos codo on codo.status = 'ATIVO'" +
            "      join codo.papel cdpa " +
            "      left join ContratoParteDocumento copd on cppa = copd.contratoPartePapel and" +
            "                                               codo = copd.documento  " +
            " where cont  = :contrato " +
            "  and cppa.papel = cdpa.papel " +
            "  and copd.id is null " +
            "  and codo.tipoDocumento.assina = true "
    )
    List<ContratoParteDocumentoPendente> verificaAssinaturasPendentes(Contrato contrato);

    @Query("select new br.com.assinanet.models.ContratoParteDocumentoPendente ( copa, codo ) " +
            " from Contrato cont" +
            "      join cont.partes copa on copa.status = 'ATIVO' and copa.tipoPessoa = 'FISICA' and copa.cpfCnpj=:cpfCnpj " +
            "      join copa.papel cppa  " +
            "      join cont.documentos codo on codo.status = 'ATIVO'" +
            "      join codo.papel cdpa  " +
            "      left join ContratoParteDocumento copd on cppa = copd.contratoPartePapel and" +
            "                                               codo = copd.documento  " +
            " where cont  = :contrato " +
            "  and cppa.papel = cdpa.papel " +
            "  and copd.id is null " +
            "  and codo.tipoDocumento.assina = true ")
    List<ContratoParteDocumentoPendente> verificaAssinaturasPendentesContratoCpfCnpj(Contrato contrato, String cpfCnpj);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoParte copa set copa.statusAssinatura = 'ASSINADO' " +
            "WHERE copa.contrato = :contrato " +
            " and  copa.status = 'ATIVO' " +
            " and ( not copa.id in ( :partesPendentes )  or  :qtdPendente = 0   ) " +
            " and copa.statusAssinatura != 'ASSINADO' ")
    void concluiContratoParteParciais(Contrato contrato, List<UUID> partesPendentes, int qtdPendente);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoDocumento codo set codo.statusDocumento = 'ASSINADO' " +
            "WHERE codo.contrato = :contrato " +
            " and  codo.status = 'ATIVO' " +
            " and  ( not codo.id in ( :documentosPendentes ) or  :qtdPendente = 0  ) " +
            " and codo.statusDocumento != 'ASSINADO' ")
    void concluiContratoDocumentoParciais(Contrato contrato, List<UUID> documentosPendentes, int qtdPendente);


    @Query("select new br.com.assinanet.models.ContratoParteDocumentoPendente ( copa, codo ) " +
            " from Contrato cont" +
            "      join cont.partes copa on copa.status = 'ATIVO' and copa.tipoPessoa = 'FISICA' " +
            "      join copa.papel cppa  " +
            "      join cont.documentos codo on codo.status = 'ATIVO'" +
            "      join codo.papel cdpa  " +
            "      left join ContratoParteDocumento copd on cppa = copd.contratoPartePapel and" +
            "                                               codo = copd.documento  " +
            "      join codo.tipoDocumento tido  " +
            " where codo = :contratoDocumento " +
            "  and cppa.papel = cdpa.papel " +
            "  and copd.id is null " +
            " and  tido.assina = true")
    List<ContratoParteDocumentoPendente> verificaAssinaturasPendentesDocumento(ContratoDocumento contratoDocumento);

    @Query("select distinct cont " +
            " from Contrato cont" +
            "      join cont.documentos codo on codo.status = 'ATIVO' and codo.statusDocumento = 'ASSINADO'" +
            "      join codo.tipoDocumento tido " +
            "      join codo.assinaturaPapeis aspa " +
            "      join cont.custodiante cudo " +
            //" where ( cont.statusContrato in (  'ASSINADO' , 'GERANDOASSINATURAS') " +
            " where   ( cont.statusContrato in (   'GERANDOASSINATURAS')  or " +
            "         ( cont.statusContrato in (  'ASSINADO' ) and codo.documentoAssinadoSHA256 is null and codo.documentoAssinado is null and tido.assina = true) ) " +
            "  and cont.dataStatusContrato <= :dataReferencia " +
            "  and cont.status = 'ATIVO'" +
            "  and tido.assina = true" +
            "  and codo.status = 'ATIVO'  " +
            "  order by cont.dataCriacao desc "
    )
    List<Contrato> verificaContratosAssinadoComDocumentosAssinadosPendentes(Date dataReferencia);

    @Query("select distinct  cont " +
            " from Contrato cont" +
            "      join cont.documentos codo on codo.status = 'ATIVO' and codo.documentoAssinadoSHA256 is null " +
            "      join codo.tipoDocumento tido " +
            " where cont.statusContrato in ( 'GERANDOASSINATURAS' , 'ASSINADO' ) " +
            "  and cont.dataStatusContrato <= :dataReferencia " +
            "  and cont.status = 'ATIVO'" +
            "  and tido.assina = true" +
            "  and codo.status = 'ATIVO'" //+
            //"  and codo is null " +
            //" order by cont.dataStatusContrato desc "
    )
    List<Contrato> verificaContratosAssinadosComStatusGerandoAssinatura(Date dataReferencia);

    @Query("select distinct  cont " +
            " from Contrato cont" +
            " where cont.statusContrato in ( 'LIBERANDOASSINATURA' ) " +
            "  and cont.dataStatusContrato <= :dataReferencia " +
            "  and cont.status = 'ATIVO'" +
            //"  and codo is null " +
            " order by cont.dataStatusContrato desc "
    )
    List<Contrato> VerificarContratosLiberadosParaAssinatura(Date dataReferencia);



    @Query("select distinct cont " +
            " from Contrato cont" +
            " join SistemaAtributo siat on ( cont.custodiante = siat.cliente)" +
            " join siat.tipoAtributo tiat on ( siat.tipoAtributo = tiat ) " +
            " where cont.statusContrato not in ( 'CANCELADO', 'RECUSADO', 'ASSINADO' ) " +
            "  and cont.status = 'ATIVO'" +
            "  and tiat.tipoAtributo = 'PRAZO_CANCELAMENTO_CONTRATO_AUTOMATICO'" +
            "  and siat.valorAtributo is not null" +
            "  and  datediff(day, cont.dataSolicitacaoAssinatura,:hoje) > cast(siat.valorAtributo as integer) ")
    List<Contrato> verificaContratosNaoAssinadosParaCancelamento(Date hoje);


    @Modifying
    @Transactional
    @Query(" DELETE FROM ContratoParteDocumento copd " +
            " WHERE copd.contratoPartePapel in ( :contratoPartePapels )")
    void resetContratoParteDocumento(List<ContratoPartePapel> contratoPartePapels);


}
