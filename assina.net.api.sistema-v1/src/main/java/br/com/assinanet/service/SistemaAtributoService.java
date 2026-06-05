package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.SistemaAtributo;
import br.com.assinanet.entity.SistemaTipoAtributo;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.SistemaAtributoRepository;
import br.com.assinanet.request.IdRequest;
import br.com.assinanet.request.TermoAceiteRequest;
import br.com.assinanet.response.SistemaAtributo.SistemaAtributoCliente;
import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.DataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;


/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class SistemaAtributoService {

    private final SistemaAtributoRepository sistemaAtributoRepository;

    public SistemaAtributoService(SistemaAtributoRepository sistemaAtributoRepository) {
        this.sistemaAtributoRepository = sistemaAtributoRepository;
    }

    public SistemaAtributo getSistemaAtributo(SistemaTipoAtributoEnum tipoAtributo,
                                              Cliente cliente) {
        final SistemaAtributo atributo = sistemaAtributoRepository.findByTipoAtributoAndCliente(tipoAtributo, cliente);
        return atributo;
    }

    public String getString(SistemaTipoAtributoEnum tipoAtributo,
                            Cliente cliente) {
        final SistemaAtributo atributo = sistemaAtributoRepository.findByTipoAtributoAndCliente(tipoAtributo, cliente);
        if ( atributo == null)
            return null;
        return atributo.getValorAtributo();
    }

    public boolean getBoolean(SistemaTipoAtributoEnum tipoAtributo,
                              Cliente cliente) {
        final String atributo = getString(tipoAtributo, cliente);
        return CommonsUtil.booleanValue(atributo);
    }

    public Integer getInteger(SistemaTipoAtributoEnum tipoAtributo,
                              Cliente cliente) {
        final String atributo = getString(tipoAtributo, cliente);
        return CommonsUtil.integerValue(atributo);
    }

    public Page<SistemaAtributoCliente> findAllManutencao(List<SistemaTipoAtributoEnum> tipoAtributo, UUID idCliente, Pageable pageable) {


//        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
//        Example<SistemaAtributo> example = Example.of(filtro, matcher);

        return sistemaAtributoRepository.findAllManutencao(tipoAtributo, idCliente, pageable);
    }

    public SistemaAtributoCliente findByIdManutencao(IdRequest idRequest) {
        return sistemaAtributoRepository.findByIdManutencao(idRequest.getId(), idRequest.getIdCliente());
    }

    public SistemaAtributo findById(UUID id) {
        return sistemaAtributoRepository.getOne(id);
    }

    public SistemaAtributo save(SistemaAtributo sistemaAtributo) {

        if ( CommonsUtil.semValor(sistemaAtributo.getStatus())){
            sistemaAtributo.setStatus(StatusEnum.ATIVO);
        }

        return sistemaAtributoRepository.save(sistemaAtributo);
    }

    public SistemaAtributoCliente save(SistemaAtributoCliente sistemaAtributoCliente) {
        SistemaAtributo sistemaAtributo = new SistemaAtributo();
        sistemaAtributo.setId(sistemaAtributoCliente.getIdSistemaATributo());
        sistemaAtributo.setTipoAtributo(sistemaAtributoCliente.getTipoAtributo());
        sistemaAtributo.setCliente(sistemaAtributoCliente.getCliente());
        sistemaAtributo.setDataAlteracao(DataUtil.getCarimboTempo().getDataCarimboTempo());
        sistemaAtributo.setValorAtributo(sistemaAtributoCliente.getValorAtributo());
        sistemaAtributo.setStatus(StatusEnum.ATIVO);
        sistemaAtributo = save(sistemaAtributo);

        return new SistemaAtributoCliente(sistemaAtributoCliente.getTipoAtributo(), sistemaAtributo);

    }

    public void deleteManutencao(IdRequest idRequest) {
        SistemaAtributoCliente sistemaAtributoCliente = findByIdManutencao(idRequest);
        if ( sistemaAtributoCliente.getIdSistemaATributo() != null) {
            SistemaAtributo sistemaAtributo = sistemaAtributoRepository.findById(sistemaAtributoCliente.getIdSistemaATributo()).orElse(null);
            sistemaAtributo.setStatus(StatusEnum.INATIVO);
            sistemaAtributo.setDataAlteracao(DataUtil.getCarimboTempo().getDataCarimboTempo());
            sistemaAtributoRepository.save(sistemaAtributo);
        }
    }


    public List<SistemaAtributo> retornaClientesComParametro(SistemaTipoAtributo sistemaTipoAtributo, String valorAtributo) {
        return sistemaAtributoRepository.retornaClientesComParametro(sistemaTipoAtributo, valorAtributo);
    }

    public List<Cliente> retornaClientescomParametrosDeAlerta(SistemaTipoAtributo atributoDiasAlerta, String valorAtributoDias,
                                                              SistemaTipoAtributo atributoHorariosAlerta, String valorAtributoHorarios) {
        return sistemaAtributoRepository.retornaClientesComParametrosDeAlerta(atributoDiasAlerta, valorAtributoDias, atributoHorariosAlerta, valorAtributoHorarios);
    }

    public List<Cliente> retornaClientesSemParametroDeAlerta(SistemaTipoAtributo atributoDiasAlerta, SistemaTipoAtributo atributoHorariosAlerta) {
        return sistemaAtributoRepository.retornaClientesSemParametroDeAlerta(atributoDiasAlerta, atributoHorariosAlerta);
    }



}
