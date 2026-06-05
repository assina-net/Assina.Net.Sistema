package br.com.assinanet.service;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoParte;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.repository.ContratoParteRepository;
import br.com.assinanet.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Service
public class ContratoParteService {

    private final ContratoParteRepository repository;

    public ContratoParteService(ContratoParteRepository repository) {
        this.repository = repository;
    }

    public ContratoParte Save(ContratoParte contratoParte) {

        if (!ValidationUtil.validaEmail(contratoParte.getEmail())) {
            return null;
        }

        contratoParte.getPapel().forEach(contratoPartePapel -> contratoPartePapel.setContratoParte(contratoParte));
        contratoParte.getContatos().forEach(contratoParte1 -> contratoParte1.getPapel().forEach(contratoPartePapel -> {
            contratoPartePapel.setContratoParte(contratoParte1);
        }));

        return repository.save(contratoParte);
    }

    public List<ContratoParte> SaveAll(List<ContratoParte> contratoPartes) {
        return repository.saveAll(contratoPartes);
    }

    public ContratoParte findById(UUID id) {
        return repository.getOne(id);
    }

    public List<ContratoParte> getContratoParteByContratoIdCpfCnpj(UUID idContrato, String cpfCnpj) {
        Contrato contrato = new Contrato ();
        contrato.setId(idContrato);
        return repository.findByContratoUsuarioPessoaRequest(contrato, cpfCnpj, StatusEnum.ATIVO);
    }
}
