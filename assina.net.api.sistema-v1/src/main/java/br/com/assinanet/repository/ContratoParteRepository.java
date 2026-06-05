package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.enums.StatusEnum;
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
public interface ContratoParteRepository extends JpaRepository<ContratoParte, UUID> {

    List<ContratoParte> findByContratoAndCpfCnpjAndStatus(Contrato contrato, String cpfCnpj, StatusEnum status);

    @Query(" from ContratoParte  " +
            "WHERE contrato = :contrato " +
            " and status = :status" +
            " and contratoPartePJ is null ")
    List<ContratoParte> findByContratoStatus(Contrato contrato, StatusEnum status);

    @Query(" from ContratoParte " +
            "WHERE contrato = :contrato " +
            " and cpfCnpj = :cpfCnpj " +
            " and status = :status")
    List<ContratoParte> findByContratoUsuarioPessoaRequest(Contrato contrato, String cpfCnpj, StatusEnum status);

    @Query(" select copa" +
            " from ContratoParte copa " +
            "  join copa.contrato cont " +
            "WHERE copa.chaveAcesso = :chaveAcesso " +
            " and copa.status = :status" +
            " and cont.status = :status" +
            " and cont.statusContrato not in ( 'CANCELADO' , 'RECUSADO' ) " +
            " and ( copa.validadeChaveAcesso is null or copa.validadeChaveAcesso > CURRENT_TIMESTAMP ) ")
    ContratoParte findByStatusChaveAcesso(StatusEnum status, String chaveAcesso);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoParte copa set copa.status = 'INATIVO' " +
            "WHERE copa.contrato = :contrato " +
            " and  not copa.id in ( :partesAtivas )" +
            " and copa.status = 'ATIVO' ")
    void desativaPartesExcluidas(Contrato contrato, List<UUID> partesAtivas);

    @Modifying
    @Transactional
    @Query(" UPDATE ContratoParte copa set copa.status = 'INATIVO' " +
            "WHERE copa.contrato = :contrato " +
            " and copa.status = 'ATIVO' ")
    void  desativaPartesExcluidas(Contrato contrato);


    @Modifying
    @Transactional
    @Query(" DELETE" +
            " FROM ContratoPartePapel cppa " +
            " WHERE cppa.contratoParte.id in ( :partesAtivas ) " +
            " AND not cppa.id in ( :partesPapelAtivas )")
    void excluiDocumentosPapelExcluidos(List<UUID> partesAtivas, List<UUID> partesPapelAtivas);

    @Modifying
    @Transactional
    @Query(" DELETE" +
            " FROM ContratoPartePapel cppa " +
            " WHERE cppa.contratoParte.id in ( :partesAtivas ) ")
    void excluiDocumentosPapelExcluidos(List<UUID> partesAtivas);


    @Query(" select distinct copa " +
            " from ContratoParte copa " +
            "  join copa.papel copp " +
            "  join copp.papel papel " +
            "  join copa.contrato cont " +
            "  join SistemaTipoAtributo tiat_cancelamento on ( tiat_cancelamento.tipoAtributo = 'PRAZO_CANCELAMENTO_CONTRATO_AUTOMATICO' )  " +
            "  left join SistemaAtributo siat_cancelamento on ( cont.custodiante = siat_cancelamento.cliente and tiat_cancelamento = siat_cancelamento.tipoAtributo )" +
            "  join SistemaTipoAtributo tiat_parar on ( tiat_parar.tipoAtributo = 'PRAZO_ENVIO_EMAILS_AUTOMATICO' )  " +
            "  left join SistemaAtributo siat_parar on ( cont.custodiante = siat_parar.cliente and tiat_parar = siat_parar.tipoAtributo )" +
            "WHERE copa.statusAssinatura = 'NAOASSINADO'" +
            "  and copa.status = 'ATIVO' " +
            "  and cont.statusContrato not in ( 'INATIVO',  'CANCELADO', 'RECUSADO',  'NAOLIBERADOASSINTAURA' ) " +
            "  and cont.custodiante in ( :clientes ) " +
            "  and (  siat_cancelamento.valorAtributo is null or " +
            "         datediff(day,  cont.dataSolicitacaoAssinatura, :hoje) <= siat_cancelamento.valorAtributo " +
            "      ) " +
            "  and (  siat_parar.valorAtributo is null or " +
            "         datediff(day,  cont.dataSolicitacaoAssinatura, :hoje) <= siat_parar.valorAtributo " +
            "      ) " +
            "  and datediff(day,  cont.dataSolicitacaoAssinatura, :hoje) > 1" +
            "  and ( copa.validadeChaveAcesso is null or copa.validadeChaveAcesso > CURRENT_TIMESTAMP ) "
    )
    List<ContratoParte> listaAssinaturaPendentes(Date hoje, List<Cliente> clientes);
}
