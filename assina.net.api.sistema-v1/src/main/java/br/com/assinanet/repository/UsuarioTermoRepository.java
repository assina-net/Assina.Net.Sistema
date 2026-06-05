package br.com.assinanet.repository;

import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.UsuarioTermo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface UsuarioTermoRepository extends JpaRepository<UsuarioTermo, UUID> {

    @Query("select count(siat) " +
            " from SistemaAtributo siat " +
            " join siat.tipoAtributo tiat" +
            " left join UsuarioTermo  usts on siat = usts.sistemaAtributo and usts.usuario = :usuario " +
            " where siat.status = 'ATIVO'" +
            " and usts is null " +
            " and tiat.tipoAtributo in (:termos) ")
   int termosPendentes(Usuario usuario , List<SistemaTipoAtributoEnum> termos);


    @Query("select siat" +
            " from SistemaAtributo siat " +
            " join fetch siat.tipoAtributo tiat" +
            " left join UsuarioTermo  usts on siat = usts.sistemaAtributo and usts.usuario = :usuario " +
            " where siat.status = 'ATIVO'" +
            " and usts is null" +
            " and tiat.tipoAtributo in (:termos) ")
     List<SistemaAtributo> termosPendentesAssinatura(Usuario usuario , List<SistemaTipoAtributoEnum> termos);

}
