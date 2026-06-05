package br.com.assinanet.repository;

import br.com.assinanet.entity.Usuario;
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
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Usuario findByLogin(String login);

    Usuario findByChaveEsqueceuSenha(String ChaveEsqueceuSenha);

    @Query(" select usua " +
            "from Usuario usua " +
            "     join usua.pessoa pess" +
            "      join pess.cliente clie " +
            " WHERE pess.cpfCnpj = :cpf " +
            " and clie.segmento.identificacao = 'SISTEMA'  ")
    Usuario findBycpf(String cpf);

    @Query(" select usua " +
            "from Usuario usua " +
            " WHERE ( usua.envioEmailTentativaAcesso = false or envioEmailTentativaAcesso = null ) " +
            " and usua.primeiraTentativaAcesso < :dataReferencia  " +
            " and usua.quantidadeTentativaAcesso between 1 and 2 ")
    List<Usuario> listaUsuariosProblemaAcesso(Date dataReferencia);

}
