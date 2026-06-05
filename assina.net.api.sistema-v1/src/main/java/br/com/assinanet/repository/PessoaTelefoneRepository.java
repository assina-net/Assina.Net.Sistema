package br.com.assinanet.repository;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.PessoaEndereco;
import br.com.assinanet.entity.PessoaTelefone;
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
public interface PessoaTelefoneRepository extends JpaRepository<PessoaTelefone, UUID> {

    @Modifying
    @Transactional
    @Query(" UPDATE PessoaTelefone set status = 'INATIVO' " +
            "WHERE pessoa = :pessoa " +
            " and  not id in ( :telefonesAtivos )" +
            " and status = 'ATIVO' ")
    void desativaTelefonesExcluidas(Pessoa pessoa, List<UUID> telefonesAtivos);

    @Modifying
    @Transactional
    @Query(" UPDATE PessoaTelefone set status = 'INATIVO' " +
            "WHERE pessoa = :pessoa " +
            " and status = 'ATIVO' ")
    void  desativaTelefonesExcluidas(Pessoa pessoa);


}
