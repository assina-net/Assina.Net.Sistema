package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    @Query(" select pess" +
            " from Pessoa pess " +
            " join pess.cliente clie " +
            " where pess.cpfCnpj = :cpfCnpj " +
            "and clie.segmento.identificacao = 'SISTEMA' ")
    Pessoa findByCpfCnpj(String cpfCnpj);

    Pessoa findByCpfCnpjAndTipoPessoaAndCliente(String cpfCnpj, TipoPessoaEnum tipoPessoa, Cliente cliente);

}
