package br.com.assinanet.repository;

import br.com.assinanet.entity.Pessoa;
import br.com.assinanet.entity.PessoaEndereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface PessoaEnderecoRepository extends JpaRepository<PessoaEndereco, UUID> {

    List<PessoaEndereco> findByPessoa(Pessoa pessoa);


}
