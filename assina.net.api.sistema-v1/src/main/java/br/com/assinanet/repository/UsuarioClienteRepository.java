package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioCliente;
import br.com.assinanet.entity.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface UsuarioClienteRepository extends JpaRepository<UsuarioCliente, UUID> {


    UsuarioCliente findByUsuarioAndClienteAndStatus(Usuario usuario, Cliente cliente, StatusEnum status);

    UsuarioCliente findByUsuarioIdAndCliente(UUID id, Cliente cliente);

    UsuarioCliente findByUsuarioAndCliente(Usuario usuario, Cliente cliente);


    @Query(" select uscl " +
            " from UsuarioCliente uscl " +
            "  join uscl.cliente clie join clie.pessoa pess" +
            " WHERE uscl.usuario = :usuario " +
            " and uscl.status = 'ATIVO' " +
            " order by pess.nomeRazaoSocial ")
    List<UsuarioCliente> getClientes(Usuario usuario);

}
