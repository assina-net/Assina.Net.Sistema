package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.Papel;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.StatusContratoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, UUID> {


    List<Contrato> findByCustodianteAndIdentificador(Cliente custodiante, String identificador);

    @Query(" SELECT distinct cont " +
            "FROM Contrato cont " +
            "JOIN cont.partes part on  part.status = 'ATIVO'  and part.contratoPartePJ is null " +
            "LEFT JOIN part.papel part_papel " +
            "LEFT JOIN part.contatos paco on  paco.status = 'ATIVO' " +
            "LEFT JOIN paco.papel paco_papel " +
            "JOIN cont.custodiante clie " +
            "JOIN clie.pessoa clie_pess " +
            "LEFT JOIN UsuarioCliente uscl on clie = uscl.cliente AND :usuario = uscl.usuario  " +
            " WHERE cont.custodiante = :custodiante " +
            "AND cont.status = 'ATIVO' " +
            "AND ( uscl = null or uscl.status = 'ATIVO' or part.cpfCnpj <> clie_pess.cpfCnpj  ) " +
            "AND ( :identificador is null or cont.identificador like '%'+ :identificador+'%' ) " +
            "AND ( :assunto is null or cont.assunto like '%'+:assunto+'%' )   " +
            "AND ( :statusContrato is null or cont.statusContrato = :statusContrato  )   " +
            "AND ( ( :parteNomeRazaosocial ='' ) or" +
            "      ( part.nomeRazaoSocial like '%'+:parteNomeRazaosocial+'%' or" +
            "        part.cpfCnpj like '%'+:parteNomeRazaosocial+'%' or" +
            "        paco.nomeRazaoSocial like '%'+:parteNomeRazaosocial+'%' or" +
            "        paco.cpfCnpj like '%'+:parteNomeRazaosocial+'%'   " +
            "      )  " +
            "    ) " +
            "AND ( ( :parteCpfCnpj ='' ) or" +
            "      ( part.cpfCnpj = :parteCpfCnpj or" +
            "        paco.cpfCnpj = :parteCpfCnpj" +
            "      )  " +
            "    ) " +
            "AND ((:bloqueiaObservador = false or :papelObservador = null )" + //Bonatte - 2023-12-04 - Observador null quebrava
            "    or ( ( :papelObservador not in  part_papel.papel or  part_papel.papel = null ) and " +
            "         ( :papelObservador not in  paco_papel.papel or paco_papel.papel = null ) " +
            "       )) " +
            "AND cont.statusContrato in (:listStatusContrato) "
    )
    Page<Contrato> PesquisaContratoParaAssinatura(Usuario usuario, Cliente custodiante, String identificador, String assunto, StatusContratoEnum statusContrato,
                                                  String parteNomeRazaosocial, String parteCpfCnpj,
                                                  List<StatusContratoEnum> listStatusContrato, Boolean bloqueiaObservador, Papel papelObservador, Pageable pageable);

    List<Contrato> findByCustodianteAndDataSolicitacaoAssinaturaBetweenAndStatusContratoNotIn(Cliente cliente, Date inicio, Date fim, List<StatusContratoEnum> statusExclusao);

}
