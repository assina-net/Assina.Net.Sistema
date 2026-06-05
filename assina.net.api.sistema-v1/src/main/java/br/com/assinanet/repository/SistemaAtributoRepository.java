package br.com.assinanet.repository;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Repository
public interface SistemaAtributoRepository extends JpaRepository<SistemaAtributo, UUID> {

    @Query(" select siat " +
            " from SistemaAtributo siat" +
            "  join siat.tipoAtributo tiat   " +
            " WHERE siat.status = 'ATIVO' " +
            " and tiat.tipoAtributo = :tipoAtributo " +
            " and  ( siat.cliente = :cliente  or  ( siat.cliente.segmento.identificacao = 'SISTEMA' and :cliente is null  ) ) ")
    //SistemaAtributo findByTipoAtributoAndCliente(SistemaTipoAtributoEnum tipoAtributo, Cliente cliente);
    SistemaAtributo findByTipoAtributoAndCliente(SistemaTipoAtributoEnum tipoAtributo, Cliente cliente);

    @Query(" select siat " +
            " from SistemaAtributo siat" +
            "  join siat.tipoAtributo tiat   " +
            " WHERE siat.status = 'ATIVO' " +
            " and tiat.tipoAtributo in ( :tipoAtributo ) " +
            " and  ( siat.cliente = :cliente  or  ( siat.cliente.segmento.identificacao = 'SISTEMA' and :cliente is null  ) ) ")
    List<SistemaAtributo> buscaSistemaAtributos(List<SistemaTipoAtributoEnum> tipoAtributo, Cliente cliente);

    @Query("select siat from SistemaAtributo siat " +
            "join siat.tipoAtributo tiat " +
            "where siat.status = 'ATIVO' " +
            "and tiat.tipoAtributo in (:tipoAtributo) " +
            "and (siat.cliente in (:clientes))")
    List<SistemaAtributo> buscaSistemaAtributosParaClientes(
            @Param("tipoAtributo") List<SistemaTipoAtributoEnum> tipoAtributo,
            @Param("clientes") List<Cliente> clientes);

    @Query(" select new br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente(sita, siat )  " +
            " from SistemaTipoAtributo sita " +
            "  left join SistemaAtributo siat on sita = siat.tipoAtributo and  siat.cliente.id = :idCliente and siat.status = 'ATIVO' " +
            " WHERE sita.tipoAtributo in ( :tipoAtributo ) " +
            "order by sita.descricao ")
    Page<SistemaAtributoCliente> findAllManutencao(List<SistemaTipoAtributoEnum> tipoAtributo, UUID idCliente, Pageable pageable);

    @Query(" select new br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente(sita, siat )  " +
            " from SistemaTipoAtributo sita " +
            "  left join SistemaAtributo siat on sita = siat.tipoAtributo and  siat.cliente.id = :idCliente and siat.status = 'ATIVO' " +
            " WHERE  sita.tipoAtributo.id =  :idTipoAtributo ")
    SistemaAtributoCliente findByIdManutencao(UUID idTipoAtributo, UUID idCliente);


    @Query(" select siat " +
            " from SistemaAtributo siat" +
            "  join fetch siat.cliente " +
            " WHERE siat.status = 'ATIVO' " +
            " and siat.tipoAtributo = :sistemaTipoAtributo  " +
            " and siat.valorAtributo like '%' + :valorAtributo + '%' ")
    List<SistemaAtributo> retornaClientesComParametro(SistemaTipoAtributo sistemaTipoAtributo, String valorAtributo);


    @Query("select distinct clie" +
            " from Cliente clie" +
            " inner join SistemaAtributo siat_Dias with siat_Dias.cliente = clie " +
            "                                       and siat_Dias.tipoAtributo = :atributoDiasAlerta " +
            "                                       and siat_Dias.valorAtributo like '%' + :valorAtributoDias + '%' " +
            "                                       and siat_Dias.status = 'ATIVO' " +
            " inner join SistemaAtributo siat_Horarios with siat_Horarios.cliente = clie " +
            "                                           and siat_Horarios.tipoAtributo = :atributoHorariosAlerta  " +
            "                                           and siat_Horarios.valorAtributo like '%' + :valorAtributoHorarios + '%' " +
            "                                           and siat_Horarios.status = 'ATIVO' "
    )
    List<Cliente> retornaClientesComParametrosDeAlerta(SistemaTipoAtributo atributoDiasAlerta, String valorAtributoDias,
                                                       SistemaTipoAtributo atributoHorariosAlerta, String valorAtributoHorarios);


    @Query("select distinct clie" +
            " from Cliente clie" +
            " left join SistemaAtributo siat_Dias with siat_Dias.cliente = clie  " +
            "                                      and siat_Dias.tipoAtributo = :atributoDiasAlerta " +
            "                                      and siat_Dias.status = 'ATIVO' " +
            " left join SistemaAtributo siat_Horarios with siat_Horarios.cliente = clie " +
            "                                         and siat_Horarios.tipoAtributo = :atributoHorariosAlerta " +
            "                                         and siat_Horarios.status = 'ATIVO' " +
            " where siat_Dias.id is null or " +
            "       siat_Horarios.id is null  "
    )
    List<Cliente> retornaClientesSemParametroDeAlerta(SistemaTipoAtributo atributoDiasAlerta, SistemaTipoAtributo atributoHorariosAlerta);


}
