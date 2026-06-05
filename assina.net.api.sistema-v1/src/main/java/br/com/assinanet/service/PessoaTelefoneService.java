package br.com.assinanet.service;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.TipoTelefone;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.TipoTelefoneRepository;
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
public class PessoaTelefoneService {


    private final TipoTelefoneRepository tipoTelefoneRepository;
    private final ClienteService clienteService;



    public PessoaTelefoneService(TipoTelefoneRepository tipoTelefoneRepository, ClienteService clienteService) {
        this.tipoTelefoneRepository = tipoTelefoneRepository;
        this.clienteService = clienteService;
    }

    public List<ComboListResponse> getListCombo(Cliente cliente) {

        List<ComboListResponse> result = new ArrayList<>(0);

        Cliente clienteSistema = clienteService.retornaClienteSistema();

        List<TipoTelefone> tiposTelefone = tipoTelefoneRepository.findByClienteAndStatus(cliente, StatusEnum.ATIVO);
        if (!CommonsUtil.mesmoValor(clienteSistema, cliente)) {
            tiposTelefone.addAll(tipoTelefoneRepository.findByClienteAndStatus(clienteSistema ,  StatusEnum.ATIVO));
        }

        for (TipoTelefone tipoTelefone : tiposTelefone) {
                result.add(new ComboListResponse(tipoTelefone.getId(), tipoTelefone.getIdentificacao(), tipoTelefone.getNome()));
        }

        result.sort(Comparator.comparing(ComboListResponse::getLabel));

        return result;


    }

}
