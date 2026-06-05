package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoEndereco;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.TipoEnderecoRepository;
import br.com.assinanet.response.ComboListResponse;
import br.com.assinanet.util.CommonsUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class PessoaEnderecoService {


    private final TipoEnderecoRepository tipoEnderecoRepository;
    private final ClienteService clienteService;



    public PessoaEnderecoService(TipoEnderecoRepository tipoEnderecoRepository, ClienteService clienteService) {
        this.tipoEnderecoRepository = tipoEnderecoRepository;
        this.clienteService = clienteService;
    }

    public List<ComboListResponse> getListCombo(Cliente cliente) {

        List<ComboListResponse> result = new ArrayList<>(0);

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        List<TipoEndereco> tiposEndereco = tipoEnderecoRepository.findByClienteAndStatus(cliente, StatusEnum.ATIVO);
        if (!CommonsUtil.mesmoValor(clienteSistema, cliente)) {
            tiposEndereco.addAll(tipoEnderecoRepository.findByClienteAndStatus(clienteSistema ,  StatusEnum.ATIVO));
        }

        for (TipoEndereco tipoEndereco : tiposEndereco) {
                result.add(new ComboListResponse(tipoEndereco.getId(), tipoEndereco.getIdentificacao(), tipoEndereco.getNome()));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;


    }

}
